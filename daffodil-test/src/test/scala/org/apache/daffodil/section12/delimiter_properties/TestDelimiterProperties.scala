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

package org.apache.daffodil.section12.delimiter_properties

import org.junit.Test
import org.apache.daffodil.tdml.Runner
import org.junit.AfterClass

object TestDelimiterProperties {
  val testDir_01 = "/org/apache/daffodil/ibm-tests/"
  val runner_01 = Runner(testDir_01, "dpaext1.tdml")

  val testDir_02 = "/org/apache/daffodil/section12/delimiter_properties/"
  val runner_02 = Runner(testDir_02, "DelimiterProperties.tdml")

  @AfterClass def shutDown {
    runner_01.reset
    runner_02.reset
  }

}

class TestDelimiterProperties {

  import TestDelimiterProperties._

  @Test def test_delimiter_12_01() { runner_01.runOneTest("delimiter_12_01") }
  @Test def test_delimiter_12_02() { runner_01.runOneTest("delimiter_12_02") }
  @Test def test_delimiter_12_03() { runner_01.runOneTest("delimiter_12_03") }
  @Test def test_delimiter_12_04() { runner_01.runOneTest("delimiter_12_04") }

  @Test def test_DelimProp_01() = { runner_02.runOneTest("DelimProp_01") }
  @Test def test_ParseSequence4() { runner_02.runOneTest("ParseSequence4") }
  @Test def test_ParseSequence5() { runner_02.runOneTest("ParseSequence5") }
  //@Test def testParseSequence_4a() { runner_02.runOneTest("ParseSequence_4a") }
  @Test def test_DelimProp_02() { runner_02.runOneTest("DelimProp_02") }
  @Test def test_DelimProp_03() { runner_02.runOneTest("DelimProp_03") }
  @Test def test_DelimProp_04() { runner_02.runOneTest("DelimProp_04") }
  @Test def test_DelimProp_05() { runner_02.runOneTest("DelimProp_05") }
  @Test def test_DelimProp_06() { runner_02.runOneTest("DelimProp_06") }
  @Test def test_DelimProp_07() { runner_02.runOneTest("DelimProp_07") }
  @Test def test_initiatedContentSimple1() { runner_02.runOneTest("initiatedContentSimple1") }
  @Test def test_Lesson4_initiators_terminators() { runner_02.runOneTest("Lesson4_initiators_terminators") }

  @Test def test_DelimProp_10() = { runner_02.runOneTest("DelimProp_10") }
  @Test def test_DelimProp_10_01() = { runner_02.runOneTest("DelimProp_10_01") }

  @Test def test_E1() = { runner_02.runOneTest("E1") }

  @Test def test_ReqFieldMissingAndSepIsPrefixOfTerminator_Prefix() = {
    runner_02.runOneTest("ReqFieldMissingAndSepIsPrefixOfTerminator_Prefix")
  }
  @Test def test_ReqFieldMissingAndSepIsPrefixOfTerminator_Infix() = {
    runner_02.runOneTest("ReqFieldMissingAndSepIsPrefixOfTerminator_Infix")
  }
  @Test def test_ReqFieldMissingAndSepIsPrefixOfTerminator_Postfix() = {
    runner_02.runOneTest("ReqFieldMissingAndSepIsPrefixOfTerminator_Postfix")
  }

  @Test def test_OptionalWSPTermWithExplicitLength() = {
    runner_02.runOneTest("OptionalWSPTermWithExplicitLength")
  }
  @Test def test_OptionalWSPTermWithExplicitLength2() = {
    runner_02.runOneTest("OptionalWSPTermWithExplicitLength2")
  }

  @Test def test_delims_ignorecase_01() = { runner_02.runOneTest("delims_ignorecase_01") }
}
