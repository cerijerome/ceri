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
import ceri.common.array.ArrayUtil;
import ceri.common.data.ByteProvider;

public class Utf8Test {
	private static final int _1B = 'A';
	private static final int _2B = 0xa9; // copyright
	private static final int _3B = 0x2103; // degree celsius
	private static final int _4B = 0x1d400; // mathematical bold A
	private static final String COMBO = new StringBuilder().appendCodePoint(0).appendCodePoint(_1B)
		.appendCodePoint(_2B).appendCodePoint(_3B).appendCodePoint(_4B).toString();

	@Test
	public void testConstructorIsPrivate() {
		assertPrivateConstructor(Utf8.class);
	}

	@Test
	public void testEncoder() {
		CharsetEncoder encoder = Utf8.encoder();
		byte[] b = new byte[11];
		ByteBuffer bb = ByteBuffer.wrap(b);
		encoder.encode(CharBuffer.wrap(COMBO), bb, true);
		assertArray(b, 0, 'A', 0xc2, 0xa9, 0xe2, 0x84, 0x83, 0xf0, 0x9d, 0x90, 0x80);
	}

	@Test
	public void testCeilingStart() {
		byte[] b = Utf8.encode(COMBO);
		assertThrown(() -> Utf8.ceilingStart(b, -1));
		assertEquals(Utf8.ceilingStart(b, 0), 0);
		assertEquals(Utf8.ceilingStart(b, 1), 1);
		assertEquals(Utf8.ceilingStart(b, 2), 2);
		assertEquals(Utf8.ceilingStart(b, 3), 4);
		assertEquals(Utf8.ceilingStart(b, 4), 4);
		assertEquals(Utf8.ceilingStart(b, 5), 7);
		assertEquals(Utf8.ceilingStart(b, 6), 7);
		assertEquals(Utf8.ceilingStart(b, 7), 7);
		assertEquals(Utf8.ceilingStart(b, 8), -1);
		assertEquals(Utf8.ceilingStart(b, 9), -1);
		assertEquals(Utf8.ceilingStart(b, 10), -1);
		assertThrown(() -> Utf8.ceilingStart(b, 11));
		assertThrown(() -> Utf8.ceilingStart(b, Integer.MIN_VALUE));
		assertThrown(() -> Utf8.ceilingStart(b, Integer.MAX_VALUE));
	}

	@Test
	public void testHigherStart() {
		byte[] b = Utf8.encode(COMBO);
		assertThrown(() -> Utf8.higherStart(b, -1));
		assertEquals(Utf8.higherStart(b, 0), 1);
		assertEquals(Utf8.higherStart(b, 1), 2);
		assertEquals(Utf8.higherStart(b, 2), 4);
		assertEquals(Utf8.higherStart(b, 3), 4);
		assertEquals(Utf8.higherStart(b, 4), 7);
		assertEquals(Utf8.higherStart(b, 5), 7);
		assertEquals(Utf8.higherStart(b, 6), 7);
		assertEquals(Utf8.higherStart(b, 7), -1);
		assertEquals(Utf8.higherStart(b, 8), -1);
		assertEquals(Utf8.higherStart(b, 9), -1);
		assertEquals(Utf8.higherStart(b, 10), -1);
		assertThrown(() -> Utf8.higherStart(b, 11));
		assertThrown(() -> Utf8.higherStart(b, Integer.MIN_VALUE));
		assertThrown(() -> Utf8.higherStart(b, Integer.MAX_VALUE));
	}

	@Test
	public void testFloorStart() {
		byte[] b = Utf8.encode(COMBO);
		assertThrown(() -> Utf8.floorStart(b, -1));
		assertEquals(Utf8.floorStart(b, 0), 0);
		assertEquals(Utf8.floorStart(b, 1), 1);
		assertEquals(Utf8.floorStart(b, 2), 2);
		assertEquals(Utf8.floorStart(b, 3), 2);
		assertEquals(Utf8.floorStart(b, 4), 4);
		assertEquals(Utf8.floorStart(b, 5), 4);
		assertEquals(Utf8.floorStart(b, 6), 4);
		assertEquals(Utf8.floorStart(b, 7), 7);
		assertEquals(Utf8.floorStart(b, 8), 7);
		assertEquals(Utf8.floorStart(b, 9), 7);
		assertEquals(Utf8.floorStart(b, 10), 7);
		assertThrown(() -> Utf8.floorStart(b, 11));
		assertThrown(() -> Utf8.floorStart(b, Integer.MIN_VALUE));
		assertThrown(() -> Utf8.floorStart(b, Integer.MAX_VALUE));
		byte[] b2 = new byte[] { (byte) 0x90, (byte) 0x80, 'A', (byte) 0xc2, (byte) 0xa9 };
		assertEquals(Utf8.floorStart(b2, 0), -1);
		assertEquals(Utf8.floorStart(b2, 1), -1);
		assertEquals(Utf8.floorStart(b2, 2), 2);
		assertEquals(Utf8.floorStart(b2, 3), 3);
		assertEquals(Utf8.floorStart(b2, 4), 3);
	}

	@Test
	public void testLowerStart() {
		byte[] b = Utf8.encode(COMBO);
		assertThrown(() -> Utf8.lowerStart(b, -1));
		assertEquals(Utf8.lowerStart(b, 0), -1);
		assertEquals(Utf8.lowerStart(b, 1), 0);
		assertEquals(Utf8.lowerStart(b, 2), 1);
		assertEquals(Utf8.lowerStart(b, 3), 2);
		assertEquals(Utf8.lowerStart(b, 4), 2);
		assertEquals(Utf8.lowerStart(b, 5), 4);
		assertEquals(Utf8.lowerStart(b, 6), 4);
		assertEquals(Utf8.lowerStart(b, 7), 4);
		assertEquals(Utf8.lowerStart(b, 8), 7);
		assertEquals(Utf8.lowerStart(b, 9), 7);
		assertEquals(Utf8.lowerStart(b, 10), 7);
		assertThrown(() -> Utf8.lowerStart(b, 11));
		assertThrown(() -> Utf8.lowerStart(b, Integer.MIN_VALUE));
		assertThrown(() -> Utf8.lowerStart(b, Integer.MAX_VALUE));
		byte[] b2 = new byte[] { (byte) 0x90, (byte) 0x80, 'A', (byte) 0xc2, (byte) 0xa9 };
		assertEquals(Utf8.lowerStart(b2, 0), -1);
		assertEquals(Utf8.lowerStart(b2, 1), -1);
		assertEquals(Utf8.lowerStart(b2, 2), -1);
		assertEquals(Utf8.lowerStart(b2, 3), 2);
		assertEquals(Utf8.lowerStart(b2, 4), 3);
	}

	@Test
	public void testIsNByte() {
		assertTrue(Utf8.is1Byte((byte) 0));
		assertFalse(Utf8.is2ByteStart((byte) 0));
		assertFalse(Utf8.is3ByteStart((byte) 0));
		assertFalse(Utf8.is4ByteStart((byte) 0));
		//
		assertTrue(Utf8.is1Byte((byte) 'A'));
		assertFalse(Utf8.is2ByteStart((byte) 'A'));
		assertFalse(Utf8.is3ByteStart((byte) 'A'));
		assertFalse(Utf8.is4ByteStart((byte) 'A'));
		//
		assertFalse(Utf8.is1Byte((byte) 0xc2));
		assertTrue(Utf8.is2ByteStart((byte) 0xc2));
		assertFalse(Utf8.is3ByteStart((byte) 0xc2));
		assertFalse(Utf8.is4ByteStart((byte) 0xc2));
		//
		assertFalse(Utf8.is1Byte((byte) 0xe2));
		assertFalse(Utf8.is2ByteStart((byte) 0xe2));
		assertTrue(Utf8.is3ByteStart((byte) 0xe2));
		assertFalse(Utf8.is4ByteStart((byte) 0xe2));
		//
		assertFalse(Utf8.is1Byte((byte) 0xf0));
		assertFalse(Utf8.is2ByteStart((byte) 0xf0));
		assertFalse(Utf8.is3ByteStart((byte) 0xf0));
		assertTrue(Utf8.is4ByteStart((byte) 0xf0));
	}

	@Test
	public void testIsStart() {
		assertTrue(Utf8.isStart((byte) 0));
		assertTrue(Utf8.isStart((byte) 'A'));
		assertTrue(Utf8.isStart((byte) 0xc2));
		assertFalse(Utf8.isStart((byte) 0xa9));
		assertTrue(Utf8.isStart((byte) 0xe2));
		assertFalse(Utf8.isStart((byte) 0x84));
		assertFalse(Utf8.isStart((byte) 0x83));
		assertTrue(Utf8.isStart((byte) 0xf0));
		assertFalse(Utf8.isStart((byte) 0x9d));
		assertFalse(Utf8.isStart((byte) 0x90));
		assertFalse(Utf8.isStart((byte) 0x80));
	}

	@Test
	public void testIsSubsequent() {
		assertFalse(Utf8.isSubsequent((byte) 0));
		assertFalse(Utf8.isSubsequent((byte) 'A'));
		assertFalse(Utf8.isSubsequent((byte) 0xc2));
		assertTrue(Utf8.isSubsequent((byte) 0xa9));
		assertFalse(Utf8.isSubsequent((byte) 0xe2));
		assertTrue(Utf8.isSubsequent((byte) 0x84));
		assertTrue(Utf8.isSubsequent((byte) 0x83));
		assertFalse(Utf8.isSubsequent((byte) 0xf0));
		assertTrue(Utf8.isSubsequent((byte) 0x9d));
		assertTrue(Utf8.isSubsequent((byte) 0x90));
		assertTrue(Utf8.isSubsequent((byte) 0x80));
	}

	@Test
	public void testByteCount() {
		assertEquals(Utf8.byteCount(-1), 0);
		assertEquals(Utf8.byteCount(0), 1);
		assertEquals(Utf8.byteCount(_1B), 1);
		assertEquals(Utf8.byteCount(_2B), 2);
		assertEquals(Utf8.byteCount(_3B), 3);
		assertEquals(Utf8.byteCount(_4B), 4);
		assertEquals(Utf8.byteCount(Integer.MIN_VALUE), 0);
		assertEquals(Utf8.byteCount(Integer.MAX_VALUE), 0);
		assertEquals(Utf8.byteCount(COMBO), 11);
	}

	@Test
	public void testDecodeBytes() {
		assertEquals(Utf8.decode(ArrayUtil.bytes.of('A')), Strings.of(_1B));
		assertEquals(Utf8.decode(ArrayUtil.bytes.of(0xc2, 0xa9)), Strings.of(_2B));
		assertEquals(Utf8.decode(ArrayUtil.bytes.of(0xe2, 0x84, 0x83)), Strings.of(_3B));
		assertEquals(Utf8.decode(ArrayUtil.bytes.of(0xf0, 0x9d, 0x90, 0x80)), Strings.of(_4B));
	}

	@Test
	public void testDecodeByteProvider() {
		assertEquals(Utf8.decode(ByteProvider.of('A')), Strings.of(_1B));
		assertEquals(Utf8.decode(ByteProvider.of(0xc2, 0xa9)), Strings.of(_2B));
		assertEquals(Utf8.decode(ByteProvider.of(0xe2, 0x84, 0x83)), Strings.of(_3B));
		assertEquals(Utf8.decode(ByteProvider.of(0xf0, 0x9d, 0x90, 0x80)), Strings.of(_4B));
	}

	@Test
	public void testEncodeBytes() {
		assertArray(Utf8.encode(-1));
		assertArray(Utf8.encode(0), 0);
		assertArray(Utf8.encode(_1B), 'A');
		assertArray(Utf8.encode(_2B), 0xc2, 0xa9);
		assertArray(Utf8.encode(_3B), 0xe2, 0x84, 0x83);
		assertArray(Utf8.encode(_4B), 0xf0, 0x9d, 0x90, 0x80);
		assertArray(Utf8.encode(Integer.MIN_VALUE));
		assertArray(Utf8.encode(Integer.MAX_VALUE));
		assertArray(Utf8.encode(COMBO), 0, 'A', 0xc2, 0xa9, 0xe2, 0x84, 0x83, 0xf0, 0x9d, 0x90,
			0x80);
	}

	@Test
	public void testEncodeTo() {
		byte[] b = new byte[11];
		assertEquals(Utf8.encodeTo(-1, b, 0), 0);
		assertEquals(Utf8.encodeTo(0, b, 0), 1);
		assertEquals(Utf8.encodeTo(_1B, b, 1), 2);
		assertEquals(Utf8.encodeTo(_2B, b, 2), 4);
		assertEquals(Utf8.encodeTo(_3B, b, 4), 7);
		assertEquals(Utf8.encodeTo(_4B, b, 7), 11);
		assertEquals(Utf8.encodeTo(Integer.MIN_VALUE, b, 7), 7);
		assertEquals(Utf8.encodeTo(Integer.MAX_VALUE, b, 7), 7);
		assertArray(b, 0, 'A', 0xc2, 0xa9, 0xe2, 0x84, 0x83, 0xf0, 0x9d, 0x90, 0x80);
		b = new byte[11];
		assertEquals(Utf8.encodeTo(COMBO, b, 0), 11);
		assertArray(b, 0, 'A', 0xc2, 0xa9, 0xe2, 0x84, 0x83, 0xf0, 0x9d, 0x90, 0x80);
	}

}
