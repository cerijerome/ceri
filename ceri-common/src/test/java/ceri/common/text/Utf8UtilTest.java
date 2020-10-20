package ceri.common.text;

import static ceri.common.test.AssertUtil.assertArray;
import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertFalse;
import static ceri.common.test.AssertUtil.assertPrivateConstructor;
import static ceri.common.test.AssertUtil.assertThrown;
import static ceri.common.test.AssertUtil.assertTrue;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharsetEncoder;
import org.junit.Test;
import ceri.common.collection.ArrayUtil;
import ceri.common.data.ByteArray;

public class Utf8UtilTest {
	private static final int _1B = 'A';
	private static final int _2B = 0xa9; // copyright
	private static final int _3B = 0x2103; // degree celsius
	private static final int _4B = 0x1d400; // mathematical bold A
	private static final String COMBO = new StringBuilder().appendCodePoint(0).appendCodePoint(_1B)
		.appendCodePoint(_2B).appendCodePoint(_3B).appendCodePoint(_4B).toString();

	@Test
	public void testConstructorIsPrivate() {
		assertPrivateConstructor(Utf8Util.class);
	}

	@Test
	public void testEncoder() {
		CharsetEncoder encoder = Utf8Util.encoder();
		byte[] b = new byte[11];
		ByteBuffer bb = ByteBuffer.wrap(b);
		encoder.encode(CharBuffer.wrap(COMBO), bb, true);
		assertArray(b, 0, 'A', 0xc2, 0xa9, 0xe2, 0x84, 0x83, 0xf0, 0x9d, 0x90, 0x80);
	}

	@Test
	public void testCeilingStart() {
		byte[] b = Utf8Util.encode(COMBO);
		assertThrown(() -> Utf8Util.ceilingStart(b, -1));
		assertEquals(Utf8Util.ceilingStart(b, 0), 0);
		assertEquals(Utf8Util.ceilingStart(b, 1), 1);
		assertEquals(Utf8Util.ceilingStart(b, 2), 2);
		assertEquals(Utf8Util.ceilingStart(b, 3), 4);
		assertEquals(Utf8Util.ceilingStart(b, 4), 4);
		assertEquals(Utf8Util.ceilingStart(b, 5), 7);
		assertEquals(Utf8Util.ceilingStart(b, 6), 7);
		assertEquals(Utf8Util.ceilingStart(b, 7), 7);
		assertEquals(Utf8Util.ceilingStart(b, 8), -1);
		assertEquals(Utf8Util.ceilingStart(b, 9), -1);
		assertEquals(Utf8Util.ceilingStart(b, 10), -1);
		assertThrown(() -> Utf8Util.ceilingStart(b, 11));
		assertThrown(() -> Utf8Util.ceilingStart(b, Integer.MIN_VALUE));
		assertThrown(() -> Utf8Util.ceilingStart(b, Integer.MAX_VALUE));
	}

	@Test
	public void testHigherStart() {
		byte[] b = Utf8Util.encode(COMBO);
		assertThrown(() -> Utf8Util.higherStart(b, -1));
		assertEquals(Utf8Util.higherStart(b, 0), 1);
		assertEquals(Utf8Util.higherStart(b, 1), 2);
		assertEquals(Utf8Util.higherStart(b, 2), 4);
		assertEquals(Utf8Util.higherStart(b, 3), 4);
		assertEquals(Utf8Util.higherStart(b, 4), 7);
		assertEquals(Utf8Util.higherStart(b, 5), 7);
		assertEquals(Utf8Util.higherStart(b, 6), 7);
		assertEquals(Utf8Util.higherStart(b, 7), -1);
		assertEquals(Utf8Util.higherStart(b, 8), -1);
		assertEquals(Utf8Util.higherStart(b, 9), -1);
		assertEquals(Utf8Util.higherStart(b, 10), -1);
		assertThrown(() -> Utf8Util.higherStart(b, 11));
		assertThrown(() -> Utf8Util.higherStart(b, Integer.MIN_VALUE));
		assertThrown(() -> Utf8Util.higherStart(b, Integer.MAX_VALUE));
	}

	@Test
	public void testFloorStart() {
		byte[] b = Utf8Util.encode(COMBO);
		assertThrown(() -> Utf8Util.floorStart(b, -1));
		assertEquals(Utf8Util.floorStart(b, 0), 0);
		assertEquals(Utf8Util.floorStart(b, 1), 1);
		assertEquals(Utf8Util.floorStart(b, 2), 2);
		assertEquals(Utf8Util.floorStart(b, 3), 2);
		assertEquals(Utf8Util.floorStart(b, 4), 4);
		assertEquals(Utf8Util.floorStart(b, 5), 4);
		assertEquals(Utf8Util.floorStart(b, 6), 4);
		assertEquals(Utf8Util.floorStart(b, 7), 7);
		assertEquals(Utf8Util.floorStart(b, 8), 7);
		assertEquals(Utf8Util.floorStart(b, 9), 7);
		assertEquals(Utf8Util.floorStart(b, 10), 7);
		assertThrown(() -> Utf8Util.floorStart(b, 11));
		assertThrown(() -> Utf8Util.floorStart(b, Integer.MIN_VALUE));
		assertThrown(() -> Utf8Util.floorStart(b, Integer.MAX_VALUE));
		byte[] b2 = new byte[] { (byte) 0x90, (byte) 0x80, 'A', (byte) 0xc2, (byte) 0xa9 };
		assertEquals(Utf8Util.floorStart(b2, 0), -1);
		assertEquals(Utf8Util.floorStart(b2, 1), -1);
		assertEquals(Utf8Util.floorStart(b2, 2), 2);
		assertEquals(Utf8Util.floorStart(b2, 3), 3);
		assertEquals(Utf8Util.floorStart(b2, 4), 3);
	}

	@Test
	public void testLowerStart() {
		byte[] b = Utf8Util.encode(COMBO);
		assertThrown(() -> Utf8Util.lowerStart(b, -1));
		assertEquals(Utf8Util.lowerStart(b, 0), -1);
		assertEquals(Utf8Util.lowerStart(b, 1), 0);
		assertEquals(Utf8Util.lowerStart(b, 2), 1);
		assertEquals(Utf8Util.lowerStart(b, 3), 2);
		assertEquals(Utf8Util.lowerStart(b, 4), 2);
		assertEquals(Utf8Util.lowerStart(b, 5), 4);
		assertEquals(Utf8Util.lowerStart(b, 6), 4);
		assertEquals(Utf8Util.lowerStart(b, 7), 4);
		assertEquals(Utf8Util.lowerStart(b, 8), 7);
		assertEquals(Utf8Util.lowerStart(b, 9), 7);
		assertEquals(Utf8Util.lowerStart(b, 10), 7);
		assertThrown(() -> Utf8Util.lowerStart(b, 11));
		assertThrown(() -> Utf8Util.lowerStart(b, Integer.MIN_VALUE));
		assertThrown(() -> Utf8Util.lowerStart(b, Integer.MAX_VALUE));
		byte[] b2 = new byte[] { (byte) 0x90, (byte) 0x80, 'A', (byte) 0xc2, (byte) 0xa9 };
		assertEquals(Utf8Util.lowerStart(b2, 0), -1);
		assertEquals(Utf8Util.lowerStart(b2, 1), -1);
		assertEquals(Utf8Util.lowerStart(b2, 2), -1);
		assertEquals(Utf8Util.lowerStart(b2, 3), 2);
		assertEquals(Utf8Util.lowerStart(b2, 4), 3);
	}

	@Test
	public void testIsNByte() {
		assertTrue(Utf8Util.is1Byte((byte) 0));
		assertFalse(Utf8Util.is2ByteStart((byte) 0));
		assertFalse(Utf8Util.is3ByteStart((byte) 0));
		assertFalse(Utf8Util.is4ByteStart((byte) 0));
		//
		assertTrue(Utf8Util.is1Byte((byte) 'A'));
		assertFalse(Utf8Util.is2ByteStart((byte) 'A'));
		assertFalse(Utf8Util.is3ByteStart((byte) 'A'));
		assertFalse(Utf8Util.is4ByteStart((byte) 'A'));
		//
		assertFalse(Utf8Util.is1Byte((byte) 0xc2));
		assertTrue(Utf8Util.is2ByteStart((byte) 0xc2));
		assertFalse(Utf8Util.is3ByteStart((byte) 0xc2));
		assertFalse(Utf8Util.is4ByteStart((byte) 0xc2));
		//
		assertFalse(Utf8Util.is1Byte((byte) 0xe2));
		assertFalse(Utf8Util.is2ByteStart((byte) 0xe2));
		assertTrue(Utf8Util.is3ByteStart((byte) 0xe2));
		assertFalse(Utf8Util.is4ByteStart((byte) 0xe2));
		//
		assertFalse(Utf8Util.is1Byte((byte) 0xf0));
		assertFalse(Utf8Util.is2ByteStart((byte) 0xf0));
		assertFalse(Utf8Util.is3ByteStart((byte) 0xf0));
		assertTrue(Utf8Util.is4ByteStart((byte) 0xf0));
	}

	@Test
	public void testIsStart() {
		assertTrue(Utf8Util.isStart((byte) 0));
		assertTrue(Utf8Util.isStart((byte) 'A'));
		assertTrue(Utf8Util.isStart((byte) 0xc2));
		assertFalse(Utf8Util.isStart((byte) 0xa9));
		assertTrue(Utf8Util.isStart((byte) 0xe2));
		assertFalse(Utf8Util.isStart((byte) 0x84));
		assertFalse(Utf8Util.isStart((byte) 0x83));
		assertTrue(Utf8Util.isStart((byte) 0xf0));
		assertFalse(Utf8Util.isStart((byte) 0x9d));
		assertFalse(Utf8Util.isStart((byte) 0x90));
		assertFalse(Utf8Util.isStart((byte) 0x80));
	}

	@Test
	public void testIsSubsequent() {
		assertFalse(Utf8Util.isSubsequent((byte) 0));
		assertFalse(Utf8Util.isSubsequent((byte) 'A'));
		assertFalse(Utf8Util.isSubsequent((byte) 0xc2));
		assertTrue(Utf8Util.isSubsequent((byte) 0xa9));
		assertFalse(Utf8Util.isSubsequent((byte) 0xe2));
		assertTrue(Utf8Util.isSubsequent((byte) 0x84));
		assertTrue(Utf8Util.isSubsequent((byte) 0x83));
		assertFalse(Utf8Util.isSubsequent((byte) 0xf0));
		assertTrue(Utf8Util.isSubsequent((byte) 0x9d));
		assertTrue(Utf8Util.isSubsequent((byte) 0x90));
		assertTrue(Utf8Util.isSubsequent((byte) 0x80));
	}

	@Test
	public void testByteCount() {
		assertEquals(Utf8Util.byteCount(-1), 0);
		assertEquals(Utf8Util.byteCount(0), 1);
		assertEquals(Utf8Util.byteCount(_1B), 1);
		assertEquals(Utf8Util.byteCount(_2B), 2);
		assertEquals(Utf8Util.byteCount(_3B), 3);
		assertEquals(Utf8Util.byteCount(_4B), 4);
		assertEquals(Utf8Util.byteCount(Integer.MIN_VALUE), 0);
		assertEquals(Utf8Util.byteCount(Integer.MAX_VALUE), 0);
		assertEquals(Utf8Util.byteCount(COMBO), 11);
	}

	@Test
	public void testDecodeBytes() {
		assertEquals(Utf8Util.decode(ArrayUtil.bytes('A')), StringUtil.toString(_1B));
		assertEquals(Utf8Util.decode(ArrayUtil.bytes(0xc2, 0xa9)), StringUtil.toString(_2B));
		assertEquals(Utf8Util.decode(ArrayUtil.bytes(0xe2, 0x84, 0x83)), StringUtil.toString(_3B));
		assertEquals(Utf8Util.decode(ArrayUtil.bytes(0xf0, 0x9d, 0x90, 0x80)),
			StringUtil.toString(_4B));
	}

	@Test
	public void testDecodeByteProvider() {
		assertEquals(Utf8Util.decode(ByteArray.Immutable.wrap('A')), StringUtil.toString(_1B));
		assertEquals(Utf8Util.decode(ByteArray.Immutable.wrap(0xc2, 0xa9)),
			StringUtil.toString(_2B));
		assertEquals(Utf8Util.decode(ByteArray.Immutable.wrap(0xe2, 0x84, 0x83)),
			StringUtil.toString(_3B));
		assertEquals(Utf8Util.decode(ByteArray.Immutable.wrap(0xf0, 0x9d, 0x90, 0x80)),
			StringUtil.toString(_4B));
	}

	@Test
	public void testEncodeBytes() {
		assertArray(Utf8Util.encode(-1));
		assertArray(Utf8Util.encode(0), 0);
		assertArray(Utf8Util.encode(_1B), 'A');
		assertArray(Utf8Util.encode(_2B), 0xc2, 0xa9);
		assertArray(Utf8Util.encode(_3B), 0xe2, 0x84, 0x83);
		assertArray(Utf8Util.encode(_4B), 0xf0, 0x9d, 0x90, 0x80);
		assertArray(Utf8Util.encode(Integer.MIN_VALUE));
		assertArray(Utf8Util.encode(Integer.MAX_VALUE));
		assertArray(Utf8Util.encode(COMBO), 0, 'A', 0xc2, 0xa9, 0xe2, 0x84, 0x83, 0xf0, 0x9d, 0x90,
			0x80);
	}

	@Test
	public void testEncodeTo() {
		byte[] b = new byte[11];
		assertEquals(Utf8Util.encodeTo(-1, b, 0), 0);
		assertEquals(Utf8Util.encodeTo(0, b, 0), 1);
		assertEquals(Utf8Util.encodeTo(_1B, b, 1), 2);
		assertEquals(Utf8Util.encodeTo(_2B, b, 2), 4);
		assertEquals(Utf8Util.encodeTo(_3B, b, 4), 7);
		assertEquals(Utf8Util.encodeTo(_4B, b, 7), 11);
		assertEquals(Utf8Util.encodeTo(Integer.MIN_VALUE, b, 7), 7);
		assertEquals(Utf8Util.encodeTo(Integer.MAX_VALUE, b, 7), 7);
		assertArray(b, 0, 'A', 0xc2, 0xa9, 0xe2, 0x84, 0x83, 0xf0, 0x9d, 0x90, 0x80);
		b = new byte[11];
		assertEquals(Utf8Util.encodeTo(COMBO, b, 0), 11);
		assertArray(b, 0, 'A', 0xc2, 0xa9, 0xe2, 0x84, 0x83, 0xf0, 0x9d, 0x90, 0x80);
	}

}
