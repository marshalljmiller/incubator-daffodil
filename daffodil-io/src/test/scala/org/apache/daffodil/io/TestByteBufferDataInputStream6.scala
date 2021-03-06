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

package org.apache.daffodil.io

import org.junit.Test
import org.junit.Assert._
import java.nio.ByteBuffer
import org.apache.daffodil.schema.annotation.props.gen.ByteOrder
import org.apache.daffodil.schema.annotation.props.gen.BitOrder
import java.nio.CharBuffer
import org.apache.daffodil.util.MaybeULong

class TestByteBufferDataInputStream6 {

  val beFinfo = FormatInfoForUnitTest()

  val leFinfo = FormatInfoForUnitTest()
  leFinfo.byteOrder = ByteOrder.LittleEndian

  val lsbfFinfo = FormatInfoForUnitTest()
  lsbfFinfo.byteOrder = ByteOrder.LittleEndian
  lsbfFinfo.bitOrder = BitOrder.LeastSignificantBitFirst

  @Test def testUnalignedByteArrayMSBFirst() {
    val bb = ByteBuffer.allocate(8)
    bb.order(java.nio.ByteOrder.BIG_ENDIAN)
    val fb = bb.asLongBuffer()
    fb.position(0)
    val data = 0xF102030405060708L
    fb.put(data)
    val bytes = bb.array()
    val dis = ByteBufferDataInputStream(bytes)
    dis.skip(1, beFinfo) // move over one bit
    val arr = dis.getByteArray((8 * 8) - 1, beFinfo)
    assertEquals(8, arr.size)
    assertEquals(0x71.toByte, arr(0))
    assertEquals(0x02.toByte, arr(1))
    assertEquals(0x03.toByte, arr(2))
    assertEquals(0x04.toByte, arr(3))
    assertEquals(0x05.toByte, arr(4))
    assertEquals(0x06.toByte, arr(5))
    assertEquals(0x07.toByte, arr(6))
    assertEquals(0x08.toByte, arr(7))
    assertEquals(64, dis.bitPos0b)
  }

  @Test def testUnalignedByteArrayLittleEndianMSBFirst() {
    val bb = ByteBuffer.allocate(8)
    bb.order(java.nio.ByteOrder.LITTLE_ENDIAN)
    val fb = bb.asLongBuffer()
    fb.position(0)
    val data = 0xF102030405060708L
    fb.put(data)
    val bytes = bb.array()
    val dis = ByteBufferDataInputStream(bytes)
    dis.skip(1, leFinfo)
    assertEquals(1, dis.bitPos0b)
    val arr = dis.getByteArray((8 * 8) - 1, leFinfo)
    assertEquals(8, arr.size)
    assertEquals(0x71.toByte, arr(0))
    assertEquals(0x05.toByte, arr(1))
    assertEquals(0x06.toByte, arr(2))
    assertEquals(0x08.toByte, arr(3))
    assertEquals(0x0A.toByte, arr(4))
    assertEquals(0x0C.toByte, arr(5))
    assertEquals(0x0E.toByte, arr(6))
    assertEquals(0x10.toByte, arr(7))
    assertEquals(64, dis.bitPos0b)
  }

  @Test def testUnalignedByteArrayLittleEndianLSBFirst() {
    val bb = ByteBuffer.allocate(8)
    bb.order(java.nio.ByteOrder.LITTLE_ENDIAN)
    val fb = bb.asLongBuffer()
    fb.position(0)
    val data = 0x01020304050607F8L
    fb.put(data)
    val bytes = bb.array()
    val dis = ByteBufferDataInputStream(bytes)
    dis.skip(1, lsbfFinfo)
    val arr = dis.getByteArray(((8 * 8) - 1), lsbfFinfo)
    assertEquals(8, arr.size)
    assertEquals(0x00.toByte, arr(0))
    assertEquals(0x81.toByte, arr(1))
    assertEquals(0x01.toByte, arr(2))
    assertEquals(0x82.toByte, arr(3))
    assertEquals(0x02.toByte, arr(4))
    assertEquals(0x83.toByte, arr(5))
    assertEquals(0x03.toByte, arr(6))
    assertEquals(0xFC.toByte, arr(7))
    assertEquals(64, dis.bitPos0b)
  }

  /**
   * Tests of unaligned char buffer - when charset has mandatory 8-bit alignment
   *
   * These just insure that we move over to the mandatory alignment before decoding
   * any characters.
   */
  @Test def testFillCharBuffer1 {
    val dis = ByteBufferDataInputStream("01".getBytes())
    val cb = CharBuffer.allocate(1)
    dis.getSignedLong(1, beFinfo)
    val ml = dis.fillCharBuffer(cb, beFinfo)
    assertTrue(ml.isDefined)
    assertEquals(1, ml.get)
    assertEquals(16, dis.bitPos0b)
    assertEquals('1', cb.get(0))
  }

  @Test def testFillCharBuffer2 {
    val dis = ByteBufferDataInputStream("0年月日".getBytes("utf-8"))
    val cb = CharBuffer.allocate(3)
    dis.getSignedLong(4, beFinfo)
    val ml = dis.fillCharBuffer(cb, beFinfo)
    assertTrue(ml.isDefined)
    assertEquals(3, ml.get)
    assertEquals('年', cb.get(0))
    assertEquals('月', cb.get(1))
    assertEquals('日', cb.get(2))
    assertEquals(80, dis.bitPos0b)
  }

  @Test def testFillCharBufferDataEndsMidByte {
    val dis = ByteBufferDataInputStream("年月日".getBytes("utf-8"))
    dis.setBitLimit0b(MaybeULong((8 * 6) + 2)) // 2 extra bits after first 2 chars
    val cb = CharBuffer.allocate(3)
    val ml = dis.fillCharBuffer(cb, beFinfo)
    assertTrue(ml.isDefined)
    assertEquals(2, ml.get)
    assertEquals('年', cb.get(0))
    assertEquals('月', cb.get(1))
    assertEquals(8 * 6, dis.bitPos0b)
  }

  @Test def testFillCharBufferDataEndsMidByte2 {
    val dis = ByteBufferDataInputStream("年月日".getBytes("utf-8"))
    dis.setBitLimit0b(MaybeULong((8 * 6) + 2)) // 2 extra bits after first 2 chars
    val cb = CharBuffer.allocate(3)
    val ml = dis.fillCharBuffer(cb, beFinfo)
    assertTrue(ml.isDefined)
    assertEquals(2, ml.get)
    assertEquals('年', cb.get(0))
    assertEquals('月', cb.get(1))
    assertEquals(8 * 6, dis.bitPos0b)
    cb.clear()
    val ml2 = dis.fillCharBuffer(cb, beFinfo) // ask for next character
    assertEquals(MaybeULong.Nope, ml2)
  }

  @Test def testFillCharBufferDataEndsMidByte3 {
    val dis = ByteBufferDataInputStream("年月日".getBytes("utf-8"))
    dis.setBitLimit0b(MaybeULong((8 * 6) + 10)) // 1 more byte plus 2 extra bits after first 2 chars
    val cb = CharBuffer.allocate(3)
    val ml = dis.fillCharBuffer(cb, beFinfo)
    assertTrue(ml.isDefined)
    assertEquals(2, ml.get)
    assertEquals('年', cb.get(0))
    assertEquals('月', cb.get(1))
    assertEquals(8 * 6, dis.bitPos0b)
    cb.clear()
    val ml2 = dis.fillCharBuffer(cb, beFinfo) // ask for next character
    //
    // because it has 1 more byte available, it doesn't stop the attempt to decode
    // and that attempt fails and we replace it.
    //
    // Note that if there aren't enough bits for a single byte, then
    // we won't even decode at all. It will just return Nope.
    //
    assertEquals(MaybeULong(1), ml2)
    assertEquals(this.unicodeReplacementCharacter, cb.get(0))
    assertEquals(8 * 7, dis.bitPos0b)
  }

  def unicodeReplacementCharacter = '\uFFFD'

  /**
   * Tests of char iteration with skips of bits that force
   * re-aligning to mandatory alignment boundaries
   *
   * These just insure that we move over to the mandatory alignment before decoding
   * any characters.
   */

  @Test def testCharIteratorWithInterruptingBitSkips1 {
    val dis = ByteBufferDataInputStream("0年1月2日".getBytes("utf-8"))
    val iter = dis.asIteratorChar
    iter.setFormatInfo(beFinfo)
    dis.skip(1, beFinfo)
    assertTrue(iter.hasNext) // examining a character here requires aligning to mandatory alignment of 8 bit boundary.
    assertEquals(1, dis.bitPos0b)
    assertEquals('年', iter.next)
    assertEquals(32, dis.bitPos0b)
    dis.skip(1, beFinfo)
    assertTrue(iter.hasNext)
    assertEquals(33, dis.bitPos0b)
    assertEquals('月', iter.next)
    assertEquals(64, dis.bitPos0b)
    dis.skip(1, beFinfo)
    assertTrue(iter.hasNext)
    assertEquals('日', iter.next)
    assertEquals(96, dis.bitPos0b)
    assertFalse(dis.skip(1, beFinfo))
    assertFalse(iter.hasNext)
  }

  /**
   * This test shows that if you do a bad-practice thing, and
   * actually move the bitPos between hasNext() and next(), that
   * the "right thing" happens, which is that the iterator notices this,
   *
   * Also shows that hasNext() doesn't ever move the bitPos even
   * if it has to align to a mandatory character alignment boundary.
   */
  @Test def testCharIteratorWithInterruptingBitSkipsBetweenHasNextAndNext {
    val dis = ByteBufferDataInputStream("0年1月2日".getBytes("utf-8"))
    val iter = dis.asIteratorChar
    iter.setFormatInfo(beFinfo)
    dis.skip(1, beFinfo)
    assertTrue(iter.hasNext) // examining a character here requires aligning to mandatory alignment of 8 bit boundary.
    assertEquals(1, dis.bitPos0b)
    dis.skip(1, beFinfo) // this skip should invalidate the character cached by hasNext.
    assertEquals(2, dis.bitPos0b)
    assertTrue(iter.hasNext)
    assertEquals(2, dis.bitPos0b)
    val c = iter.next
    assertEquals(32, dis.bitPos0b) // has next doesn't cause movement even to align to mandatory.
    assertEquals('年', c)
    assertTrue(iter.hasNext)
    dis.skip(4, beFinfo)
    assertEquals(36, dis.bitPos0b)
    val d = iter.next
    assertEquals(64, dis.bitPos0b)
    assertEquals('月', d)
  }

}
