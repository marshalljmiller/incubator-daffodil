/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.daffodil.processors.parsers

import org.apache.daffodil.api.ValidationMode
import org.apache.daffodil.processors.ChoiceDispatchKeyEv
import org.apache.daffodil.processors.DelimiterParseEv
import org.apache.daffodil.processors.ElementRuntimeData
import org.apache.daffodil.processors.EscapeSchemeParseEv
import org.apache.daffodil.processors.RuntimeData
import org.apache.daffodil.processors.Success
import org.apache.daffodil.util.LogLevel
import org.apache.daffodil.processors.TermRuntimeData
import org.apache.daffodil.processors.Evaluatable

class ComplexTypeParser(rd: RuntimeData, bodyParser: Parser)
  extends CombinatorParser(rd) {
  override def nom = "ComplexType"

  override lazy val runtimeDependencies = Nil

  override lazy val childProcessors = Seq(bodyParser)

  def parse(start: PState): Unit = {
    start.mpstate.childIndexStack.push(1L) // one-based indexing
    bodyParser.parse1(start)
    start.mpstate.childIndexStack.pop()
    ()
  }
}

/**
 * The purpose of this parser is to create/evaluate delimiter DFAs
 * and push them to the delimiter stack (bring them in scope) for
 * subsequent (internal/body) parse steps.  Then on the way out pop
 * the delimiter DFAs (bring them out of scope) after
 * the internal/body parser has completed.
 */
class DelimiterStackParser(delimiters: Array[DelimiterParseEv],
  ctxt: RuntimeData, bodyParser: Parser)
  extends CombinatorParser(ctxt) {

  override lazy val childProcessors = List(bodyParser)

  override lazy val runtimeDependencies = delimiters.toSeq

  def parse(start: PState): Unit = {

    val newLocalIndex = start.mpstate.delimiters.length
    start.mpstate.delimitersLocalIndexStack.push(newLocalIndex)

    // evaluate and add delimiters to the stack
    var i: Int = 0
    while (i < delimiters.length) {
      start.mpstate.delimiters ++= delimiters(i).evaluate(start)
      i += 1
    }

    // set the index of the newly added delimiters
    val newDelimLen = start.mpstate.delimiters.length
    i = newLocalIndex
    while (i < newDelimLen) {
      start.mpstate.delimiters(i).indexInDelimiterStack = i
      i += 1
    }

    // parse
    bodyParser.parse1(start)

    // pop delimiters
    start.mpstate.delimiters.reduceToSize(start.mpstate.delimitersLocalIndexStack.pop)
  }
}

/**
 * *
 * This parser should only ever be called when a dynamic escape scheme exists
 * so the escape scheme is evaluated in the right scope. If a constant
 * escape scheme exists, the Evaluatable should store the constant and this
 * should never be called.
 *
 * Note that the escape scheme evaluatable (and its dependencies) are manually
 * cached, so upon exiting scope the cache must be invalidated.
 */
class DynamicEscapeSchemeParser(escapeScheme: EscapeSchemeParseEv,
  ctxt: TermRuntimeData, bodyParser: Parser)
  extends CombinatorParser(ctxt) {

  override lazy val childProcessors = Seq(bodyParser)

  override lazy val runtimeDependencies = List(escapeScheme)

  def parse(start: PState): Unit = {
    // evaluate the dynamic escape scheme in the correct scope. the resulting
    // value is cached in the Evaluatable (since it is manually cached) and
    // future parsers that use this escape scheme will use that cached value.
    escapeScheme.newCache(start)
    escapeScheme.evaluate(start)

    // Parse
    bodyParser.parse1(start)

    // invalidate the escape scheme cache
    escapeScheme.invalidateCache(start)
  }
}

class SequenceCombinatorParser(rd: TermRuntimeData, bodyParser: Parser)
  extends CombinatorParser(rd) {
  override def nom = "Sequence"

  override lazy val runtimeDependencies: Seq[Evaluatable[AnyRef]] = Nil

  override lazy val childProcessors = Seq(bodyParser)

  def parse(start: PState): Unit = {
    start.mpstate.groupIndexStack.push(1L) // one-based indexing

    bodyParser.parse1(start)

    start.mpstate.groupIndexStack.pop()
    start.mpstate.moveOverOneGroupIndexOnly()
    ()
  }
}

/**
 * This is essentially just a wrapper around the bodyParser, which is an
 * AltCompParser. This is only here to maintain symmetry with the unparse side,
 * which has a more complicated unparser that differs from an AltCompUnparser.
 */
class ChoiceCombinatorParser(rd: TermRuntimeData, bodyParser: Parser)
  extends CombinatorParser(rd) {
  override def nom = "Choice"

  override lazy val runtimeDependencies = Nil

  override lazy val childProcessors = Seq(bodyParser)

  def parse(start: PState): Unit = {
    bodyParser.parse1(start)
  }
}

class ChoiceDispatchCombinatorParser(rd: TermRuntimeData, dispatchKeyEv: ChoiceDispatchKeyEv, dispatchBranchKeyMap: Map[String, Parser])
  extends CombinatorParser(rd) {
  override def nom = "ChoiceDispatch"

  override lazy val runtimeDependencies = Nil

  override lazy val childProcessors = dispatchBranchKeyMap.values.toSeq

  def parse(pstate: PState): Unit = {
    val key = dispatchKeyEv.evaluate(pstate)

    val parserOpt = dispatchBranchKeyMap.get(key)
    if (parserOpt.isEmpty) {
      val diag = new ChoiceDispatchNoMatch(context.schemaFileLocation, pstate, key)
      pstate.setFailed(diag)
    } else {
      val parser = parserOpt.get

      // Note that we are intentionally not pushing/popping a new
      // discriminator here, as is done in the ChoiceCombinatorParser and
      // AltCompParser. This has the effect that if a branch of this direct
      // dispatch choice specifies a discriminator, then it will discriminate a
      // point of uncertainty outside of the choice. If we pushed a new
      // discriminator here if would essentially ignore discriminators on a
      // choice branch.

      log(LogLevel.Debug, "Dispatching to choice alternative: %s", parser)
      parser.parse1(pstate)

      if (pstate.processorStatus eq Success) {
        log(LogLevel.Debug, "Choice dispatch success: %s", parser)
      } else {
        log(LogLevel.Debug, "Choice dispatch failed: %s", parser)
        val diag = new ChoiceDispatchFailed(context.schemaFileLocation, pstate, pstate.diagnostics)
        pstate.setFailed(diag)
      }
    }
  }
}

class ArrayCombinatorParser(erd: ElementRuntimeData, bodyParser: Parser)
  extends CombinatorParser(erd) {
  override def nom = "Array"
  override lazy val childProcessors = Seq(bodyParser)

  override lazy val runtimeDependencies = Nil

  def parse(start: PState): Unit = {

    start.mpstate.arrayIndexStack.push(1L) // one-based indexing
    start.mpstate.occursBoundsStack.push(start.tunable.maxOccursBounds)

    bodyParser.parse1(start)

    val actualOccurs = start.mpstate.arrayIndexStack.pop()
    start.mpstate.occursBoundsStack.pop()

    if (start.processorStatus ne Success) return

    val shouldValidate =
      start.dataProc.isDefined && start.dataProc.value.getValidationMode != ValidationMode.Off

    if (shouldValidate && erd.minOccurs.isDefined && erd.maxOccurs.isDefined) {
      val minO = erd.minOccurs.get
      val maxO = erd.maxOccurs.get
      val isUnbounded = maxO == -1
      val occurrence = actualOccurs - 1

      if (isUnbounded && occurrence < minO)
        start.validationError("%s occurred '%s' times when it was expected to be a " +
          "minimum of '%s' and a maximum of 'UNBOUNDED' times.", erd.diagnosticDebugName,
          occurrence, minO)
      else if (!isUnbounded && (occurrence < minO || occurrence > maxO))
        start.validationError("%s occurred '%s' times when it was expected to be a " +
          "minimum of '%s' and a maximum of '%s' times.", erd.diagnosticDebugName,
          occurrence, minO, maxO)
      else {
        //ok
      }
    }
  }
}

// This follows the same behavior as Arrays for parsing
class OptionalCombinatorParser(erd: ElementRuntimeData, bodyParser: Parser) extends ArrayCombinatorParser(erd, bodyParser) {
  override def nom = "Optional"
}
