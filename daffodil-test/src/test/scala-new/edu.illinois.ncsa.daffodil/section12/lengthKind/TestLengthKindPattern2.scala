package edu.illinois.ncsa.daffodil.section12.lengthKind

import junit.framework.Assert._
import org.scalatest.junit.JUnitSuite
import org.junit.Test
import scala.xml._
import edu.illinois.ncsa.daffodil.xml.XMLUtils
import edu.illinois.ncsa.daffodil.xml.XMLUtils._
import edu.illinois.ncsa.daffodil.compiler.Compiler
import edu.illinois.ncsa.daffodil.util._
import edu.illinois.ncsa.daffodil.tdml.DFDLTestSuite
import java.io.File

//class TestLengthKindPattern2 extends JUnitSuite {
//  val testDir = "/edu.illinois.ncsa.daffodil/section12/lengthKind/"
//  val aa = testDir + "PatternTests.tdml"
//  lazy val runner = new DFDLTestSuite(Misc.getRequiredResource(aa))
//
//  // @Test def testLengthKindPatternCompound() { runner.runOneTest("LengthKindPatternCompound") }
//  // @Test def test_LengthPatternIllegalBits_02() { runner.runOneTest("LengthPatternIllegalBits_02") }
//
//}