package daffodil.section02.processing_errors

import junit.framework.Assert._
import org.scalatest.junit.JUnitSuite
import org.junit.Test
import scala.xml._
import daffodil.xml.XMLUtils
import daffodil.xml.XMLUtils._
import daffodil.compiler.Compiler
import daffodil.util._
import daffodil.tdml.DFDLTestSuite
import java.io.File
import daffodil.debugger.Debugger

class TestProcessingErrors extends JUnitSuite {
  val testDir = "/daffodil/section02/processing_errors/"
  val aa = testDir + "ProcessingErrors.tdml"
  lazy val runner = new DFDLTestSuite(Misc.getRequiredResource(aa))
  
  @Test def test_upaInvalidSchema() { runner.runOneTest("upaInvalidSchema") }
  @Test def test_upaInvalidSchema2() { runner.runOneTest("upaInvalidSchema2") }

  }