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

package org.apache.daffodil.grammar

import org.apache.daffodil.exceptions.Assert
import org.apache.daffodil.Implicits._; object INoWarn { ImplicitsSuppressUnusedImportWarning() }
import org.apache.daffodil.processors.unparsers.SeqCompUnparser
import org.apache.daffodil.dsom._
import org.apache.daffodil.grammar.primitives.Nada
import org.apache.daffodil.processors.parsers.SeqCompParser
import org.apache.daffodil.processors.parsers.AltCompParser
import org.apache.daffodil.compiler.ForUnparser
import org.apache.daffodil.compiler.ForParser
import org.apache.daffodil.processors.unparsers.NadaUnparser
import org.apache.daffodil.processors.parsers.NadaParser
import org.apache.daffodil.processors.unparsers.EmptyGramUnparser

abstract class UnaryGram(context: Term, rr: => Gram) extends NamedGram(context) {
  private lazy val r = rr

  final override lazy val gram = {
    if (r.isEmpty) EmptyGram
    else this
  }
}

/**
 * BinaryGram isn't really 'binary' it's n-ary. It is called binary because it comes from
 * the binary grammar operations ~ and |, but in the abstract syntax tree we want
 * these flattened to lists of children so that a ~ b ~ c is ONE SeqComp with 3 children, not a tree
 * of two binary SeqComps.
 */
abstract class BinaryGram(context: SchemaComponent, childrenArg: Seq[Gram]) extends Gram(context) {

  requiredEvaluations(children)

  protected def op: String
  protected def open: String
  protected def close: String

  protected final lazy val children = {
    val c = childrenArg
    c
  }

  override def toString = {
    Assert.invariant(children != null)
    Assert.invariant(open != null)
    Assert.invariant(op != null)
    Assert.invariant(close != null)
    def toStringpq(p: String, q: String) =
      open + p + " " + op + " " + q + close
    val res = children.map { _.toString }.reduce { toStringpq _ }
    res
  }
}

/**
 * Sequential composition of grammar terms.
 *
 * Flattens nests of these into a flat list of terms.
 */
object SeqComp {
  def apply(context: SchemaComponent, p: => Gram, q: => Gram) = {
    val children = (p, q) match {
      case (ps: SeqComp, qs: SeqComp) => {
        ps.children ++ qs.children
      }
      case (ps: SeqComp, _) => ps.children ++ List(q)
      case (_, qs: SeqComp) => p +: qs.children
      case (_, _) => List(p, q)
    }
    val res = new SeqComp(context, children)
    res
  }
}

class SeqComp private (context: SchemaComponent, children: Seq[Gram]) extends BinaryGram(context, children) {
  protected final override def op = "~"
  protected final override def open = "("
  protected final override def close = ")"

  Assert.invariant(!children.exists { _.isInstanceOf[Nada] })

  lazy val parserChildren = children.filter(_.forWhat != ForUnparser).map { _.parser }.filterNot { _.isInstanceOf[NadaParser] }

  final override lazy val parser = {
    if (parserChildren.isEmpty) new NadaParser(context.runtimeData)
    else if (parserChildren.length == 1) parserChildren(0)
    else new SeqCompParser(context.runtimeData, parserChildren.toArray)
  }

  lazy val unparserChildren = {
    val unparserKeptChildren =
      children.filter(
        x =>
          !x.isEmpty && (x.forWhat != ForParser))
    val unparsers =
      unparserKeptChildren.map { x =>
        x.unparser
      }.filterNot { _.isInstanceOf[NadaUnparser] }
    unparsers
  }

  final override lazy val unparser = {
    if (unparserChildren.isEmpty) new NadaUnparser(context.runtimeData)
    else if (unparserChildren.length == 1) unparserChildren(0)
    else new SeqCompUnparser(context.runtimeData, unparserChildren.toArray)
  }
}

/**
 * Alternative composition of grammar terms.
 *
 * Flattens nests of these into a single flat list.
 */
object AltComp {
  def apply(context: SchemaComponent, p: => Gram, q: => Gram) = {
    val children = (p, q) match {
      case (ps: AltComp, qs: AltComp) => {
        ps.children ++ qs.children
      }
      case (ps: AltComp, _) => ps.children ++ List(q)
      case (_, qs: AltComp) => p +: qs.children
      case (_, _) => List(p, q)
    }
    val res = new AltComp(context, children)
    res
  }
}

class AltComp private (context: SchemaComponent, children: Seq[Gram]) extends BinaryGram(context, children)
  with HasNoUnparser {
  protected final override def op = "|"
  protected final override def open = "["
  protected final override def close = "]"

  final override lazy val parser = new AltCompParser(context.runtimeData, children.map { _.parser })
}

object EmptyGram extends Gram(null) {
  override def isEmpty = true
  override def toString = "Empty"

  override lazy val parser = new NadaParser(null)
  override lazy val unparser = // hasNoUnparser
    // we depend on this unparser being returned, even though it cannot be called to unparse anything.
    // As there are unit tests which test attributes where those attributes cause a DummyUnparser to be created.
    // That is later optimized out (or needs to be).
    //
    // FIXME: switch back to hasNoUnparser, find where this is being used and have it not ask for
    // this unparser.
    new EmptyGramUnparser(null) // DummyUnparser(Misc.getNameFromClass(this))

}

object ErrorGram extends Gram(null) with HasNoUnparser {
  override def isEmpty = false
  override def toString = "Error"

  override lazy val parser = hasNoParser // new ErrorParser
}

abstract class NamedGram(context: SchemaComponent) extends Gram(context) {
  // Note: keep the toString really simple.
  // It causes much grief if toString uses complicated things that can fail or
  // that end up needing the name of this NamedGram again.

  override def name = context match {
    case nm: NamedMixin => nm.name
    case _ => super.name
  }

  override def toString = "<" + name + ">" + super.name + "</" + name + ">"
}

/**
 * Primitives will derive from this base
 */
abstract class Terminal(contextArg: SchemaComponent, guard: Boolean)
  extends NamedGram(contextArg) {

  override def isEmpty = !guard

}
