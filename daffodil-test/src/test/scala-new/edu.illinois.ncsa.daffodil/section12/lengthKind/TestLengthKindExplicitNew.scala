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
import edu.illinois.ncsa.daffodil.debugger.Debugger.withDebugger
import edu.illinois.ncsa.daffodil.debugger.Debugger

class TestLengthKindExplicitNew extends JUnitSuite {
  val testDir = "/edu.illinois.ncsa.daffodil/section12/lengthKind/"
  val aa = testDir + "ExplicitTests.tdml"
  lazy val runner = new DFDLTestSuite(Misc.getRequiredResource(aa))

  // Debug Template
  // @Test def test_name() = Debugger.withDebugger { 
  // LoggingDefaults.setLoggingLevel(LogLevel.Debug)
  // runner.runOneTest("test_name") 
  // }

  @Test def test_ExplicitLengthBitsNotFixed() = { runner.runOneTest("test_ExplicitLengthBitsNotFixed") }
  @Test def test_ExplicitLengthBitsFixed() = { runner.runOneTest("test_ExplicitLengthBitsFixed") }
  @Test def test_ExplicitLengthCharsNotFixed() = { runner.runOneTest("test_ExplicitLengthCharsNotFixed") }
  @Test def test_ExplicitLengthCharsFixed() = { runner.runOneTest("test_ExplicitLengthCharsFixed") }
  
}
