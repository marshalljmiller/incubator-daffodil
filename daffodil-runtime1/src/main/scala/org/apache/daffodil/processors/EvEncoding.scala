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

package org.apache.daffodil.processors

import org.apache.daffodil.dsom._
import org.apache.daffodil.processors.charset.BitsCharset
import org.apache.daffodil.processors.charset.CharsetUtils
import org.apache.daffodil.exceptions.Assert
import org.apache.daffodil.util.MaybeInt
import org.apache.daffodil.cookers.FillByteCooker
import org.apache.daffodil.cookers.EncodingCooker
import org.apache.daffodil.processors.charset.NBitsWidth_BitsCharset
import org.apache.daffodil.processors.charset.BitsCharsetWrappingJavaCharset

/*
 * The way encoding works, is if a EncodingChangeParser or Unparser is
 * added to the processor by the DFDL compiler, then when that processor is evaluated
 * it will invoke these EVs to obtain the right charset decoder and encoder.
 *
 * The encoder or decoder actually being used when characters are processed
 * is stored in the data stream by the change operation so that it is there when needed.
 *
 * Note that this implies for situations where backtracking can occur (parsing) or
 * the outputValueCalc with forward reference (unparsing), that the ChangeEncoding processor
 * must be re-evaluated (in general) if any change has occurred since, so that the
 * encoder/decoder we start using when we back up to the earlier position is the right
 * one.
 *
 * Often whole schemas will use only one encoding however, so the DFDL compiler may
 * optimize out the ChangeEncoding processors except the very first one.
 */

/**
 * Encoding is a string, so there is no converter.
 */
abstract class EncodingEvBase(override val expr: CompiledExpression[String], trd: TermRuntimeData)
  extends EvaluatableConvertedExpression[String, String](
    expr,
    EncodingCooker, // cooker insures upper-case and trimmed of whitespace.
    trd)
  with InfosetCachedEvaluatable[String] {
  override lazy val runtimeDependencies = Nil

  override protected def compute(state: ParseOrUnparseState): String = {
    // compute via the cooker first
    val encoding = super.compute(state)

    if (encoding == "UTF-16" || encoding == "UTF-32") {
      // TODO: Use byte order mark (from P/UState or another EV?) to determine
      // if encoding should be BE or LE. Note that this means that if encoding
      // is UTF-16 or UTF-32, then we cannot calculate EncodingEv at compile
      // time since it maybe depend on the a parse value

      // If BOM doesn't exist, then default to BE.
      encoding + "BE"
    } else {
      encoding
    }
  }
}

final class EncodingEv(expr: CompiledExpression[String], trd: TermRuntimeData)
  extends EncodingEvBase(expr, trd)

abstract class CharsetEvBase(encodingEv: EncodingEvBase, val trd: TermRuntimeData)
  extends Evaluatable[BitsCharset](trd)
  with InfosetCachedEvaluatable[BitsCharset] {

  override lazy val runtimeDependencies = Seq(encodingEv)

  private def checkCharset(state: ParseOrUnparseState, bitsCharset: BitsCharset) {
    bitsCharset match {
      case nbsc: NBitsWidth_BitsCharset =>
        trd.schemaDefinitionError("Only encodings with byte-sized code units are allowed to be specified using a runtime-valued expression. " +
          "Encodings with 7 or fewer bits in their code units must be specified as a literal encoding name in the DFDL schema. " +
          "The encoding found was '%s'.", bitsCharset.name)
      case _ => // do nothing
    }
  }

  override def compute(state: ParseOrUnparseState) = {
    val encString = encodingEv.evaluate(state)
    val cs = CharsetUtils.getCharset(encString)
    Assert.invariant(cs ne null)
    val dcs = CharsetUtils.getCharset(cs.name.toUpperCase)
    if (!encodingEv.isConstant) checkCharset(state, dcs)
    dcs
  }
}

final class CharsetEv(encodingEv: EncodingEv, trd: TermRuntimeData)
  extends CharsetEvBase(encodingEv, trd)

class FillByteEv(fillByteRaw: String, charsetEv: CharsetEv, val trd: TermRuntimeData)
  extends Evaluatable[Integer](trd)
  with InfosetCachedEvaluatable[Integer] {

  override lazy val runtimeDependencies = Seq(charsetEv)

  private val maybeSingleRawByteValue: MaybeInt = {
    val RawByte = """\%\#r([0-9a-fA-F]{2})\;""".r
    val mfb = fillByteRaw match {
      case RawByte(hex) =>
        MaybeInt(Integer.parseInt(hex, 16))
      case _ => MaybeInt.Nope
    }
    mfb
  }

  override protected def compute(state: ParseOrUnparseState): Integer = {
    val res =
      if (maybeSingleRawByteValue.isDefined) {
        // fillByte was a single raw byte, don't have to worry about encoding
        maybeSingleRawByteValue.get
      } else {
        // not a single raw byte, need to cook and encode it
        val cookedFillByte = FillByteCooker.cook(fillByteRaw, trd, true)
        Assert.invariant(cookedFillByte.length == 1)

        val bitsCharset = charsetEv.evaluate(state)
        bitsCharset match {
          case _: NBitsWidth_BitsCharset => {
            state.SDE("The fillByte property cannot be specified as a" +
              " character ('%s') when the dfdl:encoding property is '%s' because that" +
              " encoding is not a single-byte character set.", fillByteRaw, bitsCharset.name)
          }
          case cs: BitsCharsetWrappingJavaCharset => {
            val bytes = cookedFillByte.getBytes(cs.javaCharset)
            Assert.invariant(bytes.length > 0)
            if (bytes.length > 1) {
              state.SDE("The fillByte property must be a single-byte" +
                " character, but for encoding '%s' the specified character '%s'" +
                " occupies %d bytes", bitsCharset.name, cookedFillByte, bytes.length)
            }
            bytes(0).toInt
          }
        }
      }
    res
  }

}

