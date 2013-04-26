package edu.illinois.ncsa.daffodil.japi

import edu.illinois.ncsa.daffodil.compiler.{ Compiler => SCompiler }
import edu.illinois.ncsa.daffodil.exceptions.Assert
import edu.illinois.ncsa.daffodil.debugger.Debugger
import edu.illinois.ncsa.daffodil.api.{ Diagnostic => SDiagnostic }
import scala.collection.JavaConversions._
import edu.illinois.ncsa.daffodil.api.DFDL
import java.nio.channels.ReadableByteChannel
import java.nio.channels.WritableByteChannel
import edu.illinois.ncsa.daffodil.processors.{ ParseResult => SParseResult }
import edu.illinois.ncsa.daffodil.compiler.{ ProcessorFactory => SProcessorFactory }
import edu.illinois.ncsa.daffodil.processors.{ DataProcessor => SDataProcessor }
import edu.illinois.ncsa.daffodil.processors.InfosetDocument
import edu.illinois.ncsa.daffodil.xml.XMLUtils
import edu.illinois.ncsa.daffodil.api.{ WithDiagnostics => SWithDiagnostics }
import edu.illinois.ncsa.daffodil.api.{ DataLocation => SDataLocation }
import edu.illinois.ncsa.daffodil.api.{ LocationInSchemaFile => SLocationInSchemaFile }

/**
 * API Suitable for Java programmers to use.
 */
object Daffodil {

  def compiler(): Compiler = new Compiler()

}

class Compiler {
  private val sCompiler = SCompiler()

  def compile(schemaFileNames: Array[String]): ProcessorFactory = {
    val (_, pf) = sCompiler.compileInternal(schemaFileNames.toSeq)
    new ProcessorFactory(pf)
  }

  def reload(fileNameOfSavedParser: String): DFDL.ProcessorFactory = {
    sCompiler.reload(fileNameOfSavedParser)
  }

  def setDistinguishedRootNode(name: String, namespace: String): Unit =
    sCompiler.setDistinguishedRootNode(name, namespace)

  def setExternalDFDLVariable(name: String, namespace: String, value: String): Unit = {
    sCompiler.setExternalDFDLVariable(name, namespace, value)
  }

  def setDebugging(flag: Boolean) {
    sCompiler.setDebugging(flag)
  }

}

class ProcessorFactory(pf: SProcessorFactory)
  extends WithDiagnostics(pf) {

  def setDistinguishedRootNode(name: String, namespace: String): Unit =
    pf.setDistinguishedRootNode(name, namespace)

  def onPath(path: String) = {
    val dp = pf.onPath(path).asInstanceOf[SDataProcessor]
    new DataProcessor(dp)
  }

}

abstract class WithDiagnostics(wd: SWithDiagnostics) {
  def isError = wd.isError
  def canProceed = wd.canProceed
  def getDiagnostics: java.util.List[Diagnostic] = wd.getDiagnostics.map { new Diagnostic(_) } // implicitly converts to the java collection
}

class Diagnostic(d: SDiagnostic) {
  def getMessage(): String = d.getMessage

  override def toString() = d.toString
  /**
   * Get data location information relevant to this diagnostic object.
   *
   * For example, this might be a file name, and position within the file.
   */
  def getDataLocations: java.util.List[DataLocation] = d.getDataLocations.map { new DataLocation(_) }

  /**
   * Get schema location information relevant to this diagnostic object.
   *
   * For example, this might be a file name of a schema, and position within the schema file.
   */
  def getLocationsInSchemaFiles: java.util.List[LocationInSchemaFile] =
    d.getLocationsInSchemaFiles.map { new LocationInSchemaFile(_) }

  /**
   * Determine if a diagnostic object represents an error or something less serious.
   */
  def isError = d.isError

  /**
   * Positively get these things. No returning 'null' and making caller figure out
   * whether to look for cause object.
   */
  def getSomeCause: Throwable = d.getSomeCause.get
  def getSomeMessage: String = d.getSomeMessage.get
}

class DataLocation(dl: SDataLocation) {
  override def toString() = dl.toString
  def isAtEnd() = dl.isAtEnd
}

class LocationInSchemaFile(lsf: SLocationInSchemaFile) {
  override def toString() = lsf.locationDescription
}

class DataProcessor(dp: SDataProcessor)
  extends WithDiagnostics(dp) {

  def save(output: WritableByteChannel): Unit = dp.save(output)

  /**
   * Unparses (that is, serializes) data to the output, returns an object which contains any diagnostics.
   */
  // def unparse(output: WritableByteChannel, doc: org.jdom.Document): JUnparseResult

  /**
   * Returns an object which contains the result, and/or diagnostic information.
   */
  def parse(input: ReadableByteChannel): ParseResult = {
    val pr = dp.parse(input, -1).asInstanceOf[SParseResult]
    new ParseResult(pr)
  }

}

class ParseResult(pr: SParseResult)
  extends WithDiagnostics(pr) {

  /**
   * Throws IllegalStateException if you call this when isError is true
   * because in that case there is no result document.
   */
  def result(): org.jdom.Document = {

    // TODO: avoid conversion to/from scala.xml.Nodes just to scrub unneeded
    // attributes, scrub hidden elements. Should be able to start from the raw
    // jdom document.
    // val raw = pr.resultState.infoset.asInstanceOf[InfosetDocument].jDoc

    val scalaNodeResult = pr.result
    val rootElem = XMLUtils.elem2Element(scalaNodeResult)
    val docNode = new org.jdom.Document()
    docNode.setRootElement(rootElem)
    docNode
  }

}