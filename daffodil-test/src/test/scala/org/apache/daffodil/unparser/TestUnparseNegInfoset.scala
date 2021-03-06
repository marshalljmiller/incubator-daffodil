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

package org.apache.daffodil.unparser

import org.junit.Test
import org.junit.AfterClass
import org.apache.daffodil.util._
import org.apache.daffodil.tdml.DFDLTestSuite

object TestUnparseNegInfoset {
  val testDir = "/org/apache/daffodil/unparser/"
  val aa = testDir + "unparseNegInfosetTest.tdml"
  var runner = new DFDLTestSuite(Misc.getRequiredResource(aa), validateTDMLFile = false)

  @AfterClass def tearDown() {
    runner = null
  }
}

class TestUnparseNegInfoset {
  import TestUnparseNegInfoset._

  @Test def test_schemaElementRoot1Good() { runner.runOneTest("schemaElementRoot1Good") }
  @Test def test_schemaElementRoot2Good() { runner.runOneTest("schemaElementRoot2Good") }

  @Test def test_unexpectedNextNone() { runner.runOneTest("unexpectedNextNone") }
  @Test def test_unexpectedNextSingle() { runner.runOneTest("unexpectedNextSingle") }
  @Test def test_unexpectedNextMultiple() { runner.runOneTest("unexpectedNextMultiple") }

  @Test def test_uenxpectedChildNone() { runner.runOneTest("unexpectedChildNone") }
  @Test def test_unexpectedChildSingle() { runner.runOneTest("unexpectedChildSingle") }
  @Test def test_unexpectedChildMultiple() { runner.runOneTest("unexpectedChildMultiple") }
  @Test def test_unexpectedChildSameAsSibling() { runner.runOneTest("unexpectedChildSameAsSibling") }

  @Test def test_unexpectedRootSingle() { runner.runOneTest("unexpectedRootSingle") }

  @Test def test_nilledTrueNonNillable() { runner.runOneTest("nilledTrueNonNillable") }
  @Test def test_nilledFalseNonNillable() { runner.runOneTest("nilledFalseNonNillable") }
  @Test def test_nilledSimpleWithContent() { runner.runOneTest("nilledSimpleWithContent") }
  @Test def test_nilledComplexWithContent() { runner.runOneTest("nilledComplexWithContent") }
  @Test def test_nilledBadValue() { runner.runOneTest("nilledBadValue") }
}
