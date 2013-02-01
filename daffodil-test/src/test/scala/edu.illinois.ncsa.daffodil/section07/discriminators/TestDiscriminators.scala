package edu.illinois.ncsa.daffodil.section07.discriminators

import org.scalatest.junit.JUnitSuite
import org.junit.Test
import junit.framework.Assert._
import edu.illinois.ncsa.daffodil.tdml.DFDLTestSuite
import edu.illinois.ncsa.daffodil.util.Misc
import edu.illinois.ncsa.daffodil.debugger.Debugger

class TestDiscriminators extends JUnitSuite {
  val testDir = "/edu.illinois.ncsa.daffodil/section07/discriminators/"
  val tdml = testDir + "discriminator.tdml"
  lazy val runner = new DFDLTestSuite(Misc.getRequiredResource(tdml))

  @Test def test_assertions_discriminatorGuidesChoice() { runner.runOneTest("discriminatorGuidesChoice") }
  @Test def test_assertions_discriminatorGuidesChoice2() { runner.runOneTest("discriminatorGuidesChoice2") }
  @Test def test_assertions_discriminatorGuidesChoice3() { runner.runOneTest("discriminatorGuidesChoice3") }
  @Test def test_assertions_discriminatorGuidesChoice4() { runner.runOneTest("discriminatorGuidesChoice4") }
  @Test def test_assertions_discriminatorGuidesChoice5() { runner.runOneTest("discriminatorGuidesChoice5") }

  @Test def test_discriminators_discrimPatternPass() { runner.runOneTest("discrimPatternPass") }
  @Test def test_discriminators_discrimPatternFail() { runner.runOneTest("discrimPatternFail") }

  @Test def test_discriminators_discrimPatternFail2() { runner.runOneTest("discrimPatternFail2") }
  @Test def test_discriminators_discrimPatternFail3() { runner.runOneTest("discrimPatternFail3") }
  @Test def test_discriminators_choiceBranchDiscrim() { runner.runOneTest("choiceBranchDiscrim") }

  @Test def test_discriminators_discrimInvalidSchema() { runner.runOneTest("discrimInvalidSchema") }
  @Test def test_discriminators_discrimOnSimpleType() { runner.runOneTest("discrimOnSimpleType") }
  @Test def test_discriminators_discrimOnGroupRef() { runner.runOneTest("discrimOnGroupRef") }
  @Test def test_discriminators_discrimOnGroupRef2() { runner.runOneTest("discrimOnGroupRef2") }
  @Test def test_discriminators_discrimOnElementRef() { runner.runOneTest("discrimOnElementRef") }
  @Test def test_choiceBranchDiscrimFail() = { runner.runOneTest("choiceBranchDiscrimFail") }

  @Test def test_discrimPatternMatch() = { runner.runOneTest("discrimPatternMatch") }
  @Test def test_discrimPatternNoMatch() = { runner.runOneTest("discrimPatternNoMatch") }
  
  @Test def test_discrimExpression_01() = { runner.runOneTest("discrimExpression_01") }
  @Test def test_discrimExpression_02() = { runner.runOneTest("discrimExpression_02") }
  @Test def test_discrimExpression_03() = { runner.runOneTest("discrimExpression_03") }
}