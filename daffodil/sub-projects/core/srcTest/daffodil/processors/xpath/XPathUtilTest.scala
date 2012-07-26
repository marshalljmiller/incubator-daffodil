package daffodil.processors.xpath

import org.jdom.Document
import org.jdom.Element
import org.jdom.Text
import org.scalatest.FunSuite
import org.scalatest.matchers.ShouldMatchers
import scala.math.Pi
import daffodil.processors.VariableMap
//import daffodil.processors.xpath.XPathUtil
//import daffodil.processors.xpath.StringResult
//import daffodil.processors.xpath.NodeResult
import daffodil.xml._
import org.scalatest.junit.JUnit3Suite
import junit.framework.Assert._


class XPathUtilTest extends JUnit3Suite with ShouldMatchers {

  def testXPath1() { // (my first XPath test") {
    
    // <root><child1><child2><child3>19</child3></child2></child1></root>
    
    val text = new Text("19")
    val child3 = new Element("child3")
    val child2 = new Element("child2")
    val child1 = new Element("child1")
    val root = new Element("root")
    
    child3 addContent(text)
    child2 addContent(child3)
    child1 addContent(child2)
    root addContent(child1)
    
    val document = new Document(root)
    
    val result = XPathUtil evalExpressionFromString("/root/child1/child2/child3",new VariableMap(),root,Nil)
    
    result match {
      case NodeResult(x) => x.getText() should equal("19")
      case _ => fail
    }
  }
  
  def testXPath2() { // (my second XPath test") {
    
    // <root><child1><child2><child3>19</child3></child2></child1></root>
    
    val text = new Text("19")
    val child3 = new Element("child3")
    val child2 = new Element("child2")
    val child1 = new Element("child1")
    val root = new Element("root")
    
    child3 addContent(text)
    child2 addContent(child3)
    child1 addContent(child2)
    root addContent(child1)
    
    val document = new Document(root)
    
    var variables = new VariableMap() defineVariable("pi", XMLUtils.XSD_DOUBLE, new Namespaces())
    variables setVariable("pi",Pi.toString,new Namespaces())
    
    val result = XPathUtil evalExpressionFromString("$pi",variables,root,Nil)
    
    result match {
      case StringResult(x) => x should equal (Pi.toString)
      case _ => fail
    }
  }
  
  def testXPath3() { // (my third XPath test") {
    
    /*    	
     * <root>
     * 		<level1_1>
     * 			<level2_1>
     * 				<level3_1>19</level3_1>
     * 			</level2_1>
     * 		</level1_1>
     * 		<level1_2>
     * 			<level2_2>
     * 				<level3_2>42</level3_2>
     * 			</level2_2>
     *  	<level1_2>
     * </root>
     
     */
     
    
    val level3_1 = new Element("level3_1")
    val level3_2 = new Element("level3_2")
    val level2_1 = new Element("level2_1")
    val level2_2 = new Element("level2_2")
    val level1_1 = new Element("level1_1")
    val level1_2 = new Element("level1_2")
    val root = new Element("root")
    
    root addContent(level1_1)
    root addContent(level1_2)
    level1_1 addContent(level2_1)
    level1_2 addContent(level2_2)
    level2_1 addContent(level3_1)
    level2_2 addContent(level3_2)
    level3_1 addContent(new Text("19"))
    level3_2 addContent(new Text("42"))
    
        
    val document = new Document(root)
    val result = XPathUtil evalExpressionFromString("../../level1_2/level2_2/level3_2",new VariableMap(),level2_1,Nil)
    
    result match {
      case NodeResult(x) => x.getText should equal ("42")
      case _ => fail
    }
  }
  
  /**
   * Illustrates xpath with QNames so it is specific about the 
   * namespaces.
   */
   def testXPathWithQNamesInXPathForNamespaces() { 
    
    // <f:root xmlns:f="foobarNS><f:child1>19</f:child1></f:root>
    val text = new Text("19")
    val child1 = new Element("child1", "urn:foobarNS")
    val root = new Element("root", "urn:foobarNS")
    child1 addContent(text)
    root addContent(child1)
    val document = new Document(root)
       
    val ns = List(org.jdom.Namespace.getNamespace("f", "urn:foobarNS"))
    val result = XPathUtil evalExpressionFromString("/f:root/f:child1",
        new VariableMap(),
        root,
        ns)
    
    result match {
      case NodeResult(x) => x.getText() should equal("19")
      case _ => {
        println(result) 
        fail
      }
    }
  }
 
   /**
    * This illustrates the other way of getting the namespace information.
    * If the JDOM Elements are constructed with the 3-arg constructor that takes
    * both a prefix and a namespace uri (and the name), then one can get the namespaces
    * directly from the element itself. 
    */
  def testXPathWithQNamesInXPathForNamespaces2() { 
    
    // <f:root xmlns:f="foobarNS><f:child1>19</f:child1></f:root>
    val text = new Text("19")
    val child1 = new Element("child1", "f", "urn:foobarNS")
    val root = new Element("root", "f", "urn:foobarNS")
    child1 addContent(text)
    root addContent(child1)
    val document = new Document(root)
       
    val ns = XMLUtils.jdomNamespaceBindings(root)
    val result = XPathUtil evalExpressionFromString("/f:root/f:child1",
        new VariableMap(),
        root,
        ns)
    
    result match {
      case NodeResult(x) => x.getText() should equal("19")
      case _ => {
        println(result) 
        fail
      }
    }
  }
  
  /**
   * Test if converting the XML from Scala's representation changes anything.
   */
   def testXPathWithQNamesInXPathForNamespaces3() { 
    
    val root = XMLUtils.elem2Element(
        <f:root xmlns:f="urn:foobarNS"><f:child1>19</f:child1></f:root>)
    val document = new Document(root)
       
    val ns = XMLUtils.jdomNamespaceBindings(root)
    val result = XPathUtil evalExpressionFromString("/f:root/f:child1",
        new VariableMap(),
        root,
        ns)
    
    result match {
      case NodeResult(x) => x.getText() should equal("19")
      case _ => {
        println(result) 
        fail
      }
    }
  }
   
   /**
   * Test text() in paths
   */
   def testXPathWithTextInPath() { 
    
    val root = XMLUtils.elem2Element(
        <f:root xmlns:f="urn:foobarNS"><f:child1>19</f:child1></f:root>)
//    val text = new Text("19")
//    val child1 = new Element("child1", "f", "urn:foobarNS")
//    val root = new Element("root", "f", "urn:foobarNS")
//    child1 addContent(text)
//    root addContent(child1)
    val document = new Document(root)
       
    val ns = XMLUtils.jdomNamespaceBindings(root)
    val result : XPathResult = XPathUtil evalExpressionFromString("/f:root/f:child1/text()",
        new VariableMap(),
        root,
        ns)
    
    result match {
      case StringResult(x) => x should equal("19")
      case _ => {
        println(result) 
        fail
      }
    }
  }
  

        
}
