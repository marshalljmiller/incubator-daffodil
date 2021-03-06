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

package org.apache.daffodil.dpath

import org.apache.daffodil.dsom.DPathElementCompileInfo
import org.apache.daffodil.exceptions.Assert
import org.apache.daffodil.infoset.DIArray
import org.apache.daffodil.infoset.DIElement
import org.apache.daffodil.infoset.InfosetNoSuchChildElementException

/**
 * Moves to above the root element so that an absolute path
 * that begins with the name of the root element will descend into
 * the root element.
 */
case object ToRoot extends RecipeOp {
  override def run(dstate: DState) {
    val rootDoc = dstate.currentElement.toRootDoc
    dstate.setCurrentNode(rootDoc)
  }
}
case object SelfMove extends RecipeOp {
  override def run(dstate: DState) {
    // do this entirely so it will fail at constant compile time
    // also serves as a sort of assertion check.
    dstate.selfMove()
  }
}

case object UpMove extends RecipeOp {
  override def run(dstate: DState) {
    val now = dstate.currentElement
    val n = now.toParent
    dstate.setCurrentNode(n)
  }
}

case object UpMoveArray extends RecipeOp {
  override def run(dstate: DState) {
    val now = dstate.currentElement
    Assert.invariant(now.toParent.array.isDefined)
    val n = now.toParent.array.get
    dstate.setCurrentNode(n.asInstanceOf[DIArray])
  }
}

/**
 * Down to a non-array element. Can be optional or scalar.
 */
case class DownElement(info: DPathElementCompileInfo) extends RecipeOp {

  override def run(dstate: DState) {
    val now = dstate.currentComplex
    // TODO PE ? if doesn't exist should be a processing error.
    // It will throw and so will be a PE, but may be poor diagnostic.
    val c = dstate.withRetryIfBlocking(now.getChild(info))
    dstate.setCurrentNode(c.asInstanceOf[DIElement])
  }

  override def toXML = {
    toXML(info.name)
  }

}

/**
 * Move down to an occurrence of an array element.
 */
case class DownArrayOccurrence(info: DPathElementCompileInfo, indexRecipe: CompiledDPath)
  extends RecipeOpWithSubRecipes(indexRecipe) {

  val childNamedQName = info.namedQName

  override def run(dstate: DState) {
    val savedCurrentElement = dstate.currentComplex
    indexRecipe.run(dstate)
    val index = dstate.index
    // restore the saved node since the above .run set it to null. This is
    // necessary since one of the following functions could throw, leaving the
    // current node to null. And future calls depend on a current node to be set
    dstate.setCurrentNode(savedCurrentElement)
    val childArrayElementERD = dstate.withRetryIfBlocking(savedCurrentElement.getChildArray(info).asInstanceOf[DIArray].erd)
    val arr = dstate.withRetryIfBlocking(savedCurrentElement.getChildArray(childArrayElementERD))
    val occurrence = dstate.withRetryIfBlocking(arr.getOccurrence(index)) // will throw on out of bounds
    dstate.setCurrentNode(occurrence.asInstanceOf[DIElement])
  }

  override def toXML = {
    toXML(new scala.xml.Text(info.name) ++ indexRecipe.toXML)
  }

}

/*
 * down to an array object containing all occurrences
 */
case class DownArray(info: DPathElementCompileInfo) extends RecipeOp {

  override def run(dstate: DState) {
    val now = dstate.currentComplex
    val arr = dstate.withRetryIfBlocking(now.getChildArray(info))
    Assert.invariant(arr ne null)
    dstate.setCurrentNode(arr.asInstanceOf[DIArray])
  }

  override def toXML = {
    toXML(info.name)
  }

}

case class DownArrayExists(info: DPathElementCompileInfo) extends RecipeOp {

  override def run(dstate: DState) {
    val now = dstate.currentComplex
    val arr = dstate.withRetryIfBlocking(now.getChildArray(info))

    if ((arr eq null) || arr.length == 0)
      throw new InfosetNoSuchChildElementException(now, info)
  }

  override def toXML = {
    toXML(info.name)
  }

}
