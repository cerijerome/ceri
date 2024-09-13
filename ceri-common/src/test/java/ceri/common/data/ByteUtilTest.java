package ceri.common.data;

import static ceri.common.collection.ArrayUtil.bytes;
import static ceri.common.test.AssertUtil.assertArray;
import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertFalse;
import static ceri.common.test.AssertUtil.assertNull;
import static ceri.common.test.AssertUtil.assertPrivateConstructor;
import static ceri.common.test.AssertUtil.assertStream;
import static ceri.common.test.AssertUtil.assertThrown;
import static ceri.common.test.AssertUtil.assertTrue;
import static ceri.common.test.TestUtil.provider;
import static java.nio.charset.StandardCharsets.UTF_8;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.stream.IntStream;
import org.junit.Test;
import ceri.common.collection.ArrayUtil;
import ceri.common.collection.CollectionUtil;
import ceri.common.test.Captor;

public class ByteUtilTest {

	@Test
	public void testConstructorIsPrivate() {
		assertPrivateConstructor(ByteUtil.class);
	}

	@Test
	public void testBitIteratorHigh() {
		Captor<Boolean> captor = Captor.of();
		for (boolean b : CollectionUtil
			.iterable(ByteUtil.bitIterator(true, provider(0xa9, 0, 0xff))))
			captor.accept(b);
		captor.verify(true, false, true, false, true, false, false, true, //
			false, false, false, false, false, false, false, false, //
			true, true, true, true, true, true, true, true);
	}

	@Test
	public void testBitIteratorLow() {
		Captor<Boolean> captor = Captor.of();
		for (boolean b : CollectionUtil
			.iterable(ByteUtil.bitIterator(false, provider(0xa9, 0, 0xff))))
			captor.accept(b);
		captor.verify(true, false, false, true, false, true, false, true, //
			false, false, false, false, false, false, false, false, //
			true, true, true, true, true, true, true, true);
	}

	@Test
	public void testIterateMaskInt() {
		Captor.OfInt captor = Captor.ofInt();
		ByteUtil.iterateMask(true, 0x84211248, captor::accept);
		captor.verifyInt(31, 26, 21, 16, 12, 9, 6, 3);
		captor.reset();
		ByteUtil.iterateMask(false, 0x84211248, captor::accept);
		captor.verifyInt(3, 6, 9, 12, 16, 21, 26, 31);
	}

	@Test
	public void testIterateMaskLong() {
		Captor.OfInt captor = Captor.ofInt();
		ByteUtil.iterateMask(true, 0x8040201010204080L, captor::accept);
		captor.verifyInt(63, 54, 45, 36, 28, 21, 14, 7);
		captor.reset();
		ByteUtil.iterateMask(false, 0x8040201010204080L, captor::accept);
		captor.verifyInt(7, 14, 21, 28, 36, 45, 54, 63);
	}

	@Test
	public void testReduceMaskLong() {
		assertEquals(ByteUtil.reduceMask(true, 0x8040201010204080L, 0L, (bit, n) -> n + 1), 8L);
		assertEquals(ByteUtil.reduceMask(false, 0x8040201010204080L, 0L, (bit, n) -> n + 1), 8L);
	}

	@Test
	public void testReduceMaskInt() {
		assertEquals(ByteUtil.reduceMaskInt(true, 0x80402010, 0, (bit, n) -> n + 1), 4);
		assertEquals(ByteUtil.reduceMaskInt(false, 0x80402010, 0, (bit, n) -> n + 1), 4);
	}

	@Test
	public void testToHex() {
		byte[] b = ArrayUtil.bytes(-1, 0, 127, 128);
		assertNull(ByteUtil.toHex((byte[]) null, ""));
		assertNull(ByteUtil.toHex((byte[]) null, 0, 0, ""));
		assertEquals(ByteUtil.toHex(b, ""), "ff007f80");
		assertEquals(ByteUtil.toHex(b, ":"), "ff:00:7f:80");
		assertEquals(ByteUtil.toHex(b, "-"), "ff-00-7f-80");
	}

	@Test
	public void testFromHex() {
		assertArray(ByteUtil.fromHex("abcde").copy(0), 0x0a, 0xbc, 0xde);
		assertArray(ByteUtil.fromHex("abcdef").copy(0), 0xab, 0xcd, 0xef);
		assertNull(ByteUtil.fromHex(null));
		assertArray(ByteUtil.fromHex("").copy(0));
	}

	@Test
	public void testStreamOf() {
		byte[] b = ArrayUtil.bytes(-1, 0, 1, 127, 128);
		assertStream(ByteUtil.ustream(b), 0xff, 0, 1, 0x7f, 0x80);
		assertStream(ByteUtil.ustream(-1, 0, 1, 127, 128), 0xff, 0, 1, 0x7f, 0x80);
	}

	@Test
	public void testToByteArray() {
		assertArray(ByteUtil.bytes(List.of(-1, 0, 127, 128)), -1, 0, 127, 128);
		assertArray(ByteUtil.bytes(IntStream.of(-1, 0, 127, 128)), -1, 0, 127, 128);
	}

	@Test
	public void testBytesFromBuffer() {
		ByteBuffer buffer = ByteBuffer.wrap(bytes(1, 2, 3, 4, 5));
		buffer.position(2).limit(4);
		assertArray(ByteUtil.bytes(buffer), 3, 4);
	}

	@Test
	public void testFill() {
		assertArray(ByteUtil.fill(3, 0xff), 0xff, 0xff, 0xff);
		assertArray(ByteUtil.fill(0, 0xff));
	}

	@Test
	public void testReadByteArrayFromByteBuffer() {
		assertArray(ByteUtil.readFrom(null, 1, 0));
		ByteBuffer buffer = ByteBuffer.wrap(ArrayUtil.bytes(1, 2, 3, 4, 5));
		assertArray(ByteUtil.readFrom(buffer, 1, 3), 2, 3, 4);
	}

	@Test
	public void testReadFromByteBuffer() {
		ByteBuffer buffer = ByteBuffer.wrap(ArrayUtil.bytes(1, 2, 3, 4, 5));
		byte[] bytes = new byte[3];
		assertEquals(ByteUtil.readFrom(buffer, 1, bytes), 3);
		assertArray(bytes, 2, 3, 4);
	}

	@Test
	public void testWriteToByteBuffer() {
		byte[] bytes = new byte[5];
		ByteBuffer buffer = ByteBuffer.wrap(bytes);
		assertEquals(ByteUtil.writeTo(buffer, 1, 1, 2, 3), 3);
		assertArray(bytes, 0, 1, 2, 3, 0);
	}

	@Test
	public void testWriteTo() {
		ByteArrayOutputStream b = new ByteArrayOutputStream();
		ByteProvider im = ByteProvider.of(-1, 0, 128);
		ByteUtil.writeTo(b, -1, 2);
		ByteUtil.writeTo(b, new byte[] { 3, 4, 5 }, 1);
		ByteUtil.writeTo(b, im);
		ByteUtil.writeTo(b, im, 2);
		ByteUtil.writeTo(b, im, 0, 1);
		assertArray(b.toByteArray(), -1, 2, 4, 5, -1, 0, 128, 128, -1);
	}

	@Test
	public void testWriteToWithExceptions() {
		ByteProvider badProvider = badProvider(3);
		ByteArrayOutputStream b = new ByteArrayOutputStream();
		assertThrown(() -> ByteUtil.writeTo(b, badProvider));
		assertThrown(() -> ByteUtil.writeTo(b, badProvider, 1));
		assertThrown(() -> ByteUtil.writeTo(b, badProvider, 0, 2));
	}

	private static ByteProvider badProvider(int length) {
		return new ByteProvider() {
			@Override
			public int writeTo(int index, OutputStream out, int length) throws IOException {
				throw new IOException("generated");
			}

			@Override
			public byte getByte(int index) {
				return 0;
			}

			@Override
			public int length() {
				return length;
			}
		};
	}

	@Test
	public void testToAscii() {
		assertArray(ByteUtil.toAscii("\0\t\r\ntest").copy(0), //
			0, '\t', '\r', '\n', 't', 'e', 's', 't');
	}

	@Test
	public void testFromAscii() {
		assertEquals(ByteUtil.fromAscii(0, '\t', '\r', '\n', 't', 'e', 's', 't'), "\0\t\r\ntest");
	}

	@Test
	public void testFromNullTerm() {
		assertEquals(ByteUtil.fromNullTerm(bytes(0, 't', 'e', 's', 't'), UTF_8), "");
		assertEquals(ByteUtil.fromNullTerm(bytes('t', 'e', 's', 't'), UTF_8), "test");
		assertEquals(ByteUtil.fromNullTerm(bytes('\t', '\r', '\n', 0, 't', 'e', 's', 't'), UTF_8),
			"\t\r\n");
		assertEquals(ByteUtil.fromNullTerm(provider('t', 'e', 's', 't', 0, 0), UTF_8), "test");
	}

	@Test
	public void testApplyBit() {
		assertEquals(ByteUtil.applyBits(0, false, 0), 0L);
		assertEquals(ByteUtil.applyBits(0, true, 0), 1L);
		assertEquals(ByteUtil.applyBits(0, false, 63), 0L);
		assertEquals(ByteUtil.applyBits(0, true, 63), 0x8000000000000000L);
		assertEquals(ByteUtil.applyBits(0, false, 64), 0L);
		assertEquals(ByteUtil.applyBits(0, true, 65), 0L);
		assertEquals(ByteUtil.applyBits(-1L, false, 0), 0xfffffffffffffffeL);
		assertEquals(ByteUtil.applyBits(-1L, true, 0), 0xffffffffffffffffL);
		assertEquals(ByteUtil.applyBits(-1L, false, 63), 0x7fffffffffffffffL);
		assertEquals(ByteUtil.applyBits(-1L, true, 63), 0xffffffffffffffffL);
		assertEquals(ByteUtil.applyBits(-1L, false, 64), 0xffffffffffffffffL);
		assertEquals(ByteUtil.applyBits(-1L, true, 65), 0xffffffffffffffffL);
	}

	@Test
	public void testApplyBitInt() {
		assertEquals(ByteUtil.applyBitsInt(0, false, 0), 0);
		assertEquals(ByteUtil.applyBitsInt(0, true, 0), 1);
		assertEquals(ByteUtil.applyBitsInt(0, false, 31), 0);
		assertEquals(ByteUtil.applyBitsInt(0, true, 31), 0x80000000);
		assertEquals(ByteUtil.applyBitsInt(0, false, 32), 0);
		assertEquals(ByteUtil.applyBitsInt(0, true, 33), 0);
		assertEquals(ByteUtil.applyBitsInt(-1, false, 0), 0xfffffffe);
		assertEquals(ByteUtil.applyBitsInt(-1, true, 0), 0xffffffff);
		assertEquals(ByteUtil.applyBitsInt(-1, false, 31), 0x7fffffff);
		assertEquals(ByteUtil.applyBitsInt(-1, true, 31), 0xffffffff);
		assertEquals(ByteUtil.applyBitsInt(-1, false, 32), 0xffffffff);
		assertEquals(ByteUtil.applyBitsInt(-1, true, 33), 0xffffffff);
	}

	@Test
	public void testApplyMask() {
		assertEquals(ByteUtil.applyMask(0xffff0000_ffff0000L, 0xffff00_00ffff00L, false),
			0xff000000_ff000000L);
		assertEquals(ByteUtil.applyMask(0xffff0000_ffff0000L, 0xffff00_00ffff00L, true),
			0xffffff00_ffffff00L);
	}

	@Test
	public void testApplyMaskInt() {
		assertEquals(ByteUtil.applyMaskInt(0xffff0000, 0x00ffff00, false), 0xff000000);
		assertEquals(ByteUtil.applyMaskInt(0xffff0000, 0x00ffff00, true), 0xffffff00);
	}

	@Test
	public void testMaskInt() {
		assertEquals(ByteUtil.maskInt(0), 0);
		assertEquals(ByteUtil.maskInt(32), 0xffffffff);
		assertEquals(ByteUtil.maskInt(11), 0x7ff);
		assertEquals(ByteUtil.maskInt(5, 11), 0xffe0);
		assertEquals(ByteUtil.maskInt(5, 32), 0xffffffe0);
		assertEquals(ByteUtil.maskInt(32, 5), 0);
	}

	@Test
	public void testMask() {
		assertEquals(ByteUtil.mask(0), 0L);
		assertEquals(ByteUtil.mask(7), 0x7fL);
		assertEquals(ByteUtil.mask(64), 0xffffffff_ffffffffL);
		assertEquals(ByteUtil.mask(100), 0xffffffff_ffffffffL);
		assertEquals(ByteUtil.mask(10, 0), 0L);
		assertEquals(ByteUtil.mask(10, 7), 0x1fc00L);
		assertEquals(ByteUtil.mask(10, 64), 0xffffffff_fffffc00L);
		assertEquals(ByteUtil.mask(64, 10), 0L);
	}

	@Test
	public void testMaskOfBits() {
		assertEquals(ByteUtil.maskOfBits((int[]) null), 0L);
		assertEquals(ByteUtil.maskOfBits((List<Integer>) null), 0L);
		assertEquals(ByteUtil.maskOfBits((IntStream) null), 0L);
		assertEquals(ByteUtil.maskOfBits(64, 63, 32, 31, 16, 15, 8, 7, 0, -1),
			0x8000_0001_8001_8181L);
		assertEquals(ByteUtil.maskOfBits(List.of(64, 63, 32, 31, 16, 15, 8, 7, 0, -1)),
			0x8000_0001_8001_8181L);
		assertEquals(ByteUtil.maskOfBits(IntStream.of(64, 63, 32, 31, 16, 15, 8, 7, 0, -1)),
			0x8000_0001_8001_8181L);
	}

	@Test
	public void testMaskOfBitsInt() {
		assertEquals(ByteUtil.maskOfBitsInt((int[]) null), 0);
		assertEquals(ByteUtil.maskOfBitsInt((List<Integer>) null), 0);
		assertEquals(ByteUtil.maskOfBitsInt((IntStream) null), 0);
		assertEquals(ByteUtil.maskOfBitsInt(32, 31, 16, 15, 8, 7, 0, -1), 0x80018181);
		assertEquals(ByteUtil.maskOfBitsInt(List.of(32, 31, 16, 15, 8, 7, 0, -1)), 0x80018181);
		assertEquals(ByteUtil.maskOfBitsInt(IntStream.of(32, 31, 16, 15, 8, 7, 0, -1)), 0x80018181);
	}

	@Test
	public void testMaskOfBit() {
		assertEquals(ByteUtil.maskOfBit(true, 63), 0x8000_0000_0000_0000L);
		assertEquals(ByteUtil.maskOfBit(false, 63), 0L);
		assertEquals(ByteUtil.maskOfBit(true, 64), 0L);
		assertEquals(ByteUtil.maskOfBit(true, -1), 0L);
	}

	@Test
	public void testMaskOfBitInt() {
		assertEquals(ByteUtil.maskOfBitInt(true, 31), 0x80000000);
		assertEquals(ByteUtil.maskOfBitInt(false, 31), 0);
		assertEquals(ByteUtil.maskOfBitInt(true, 32), 0);
		assertEquals(ByteUtil.maskOfBitInt(true, -1), 0);
	}

	@Test
	public void testIndexMask() {
		assertEquals(ByteUtil.indexMask(List.of()), 0L);
		assertEquals(ByteUtil.indexMask(List.of(), "a", "b"), 0L);
		assertEquals(ByteUtil.indexMask(List.of("a", "b", "c", "d")), 0L);
		assertEquals(ByteUtil.indexMask(List.of("a", "b", "c", "d"), "c", "a", "d"), 0b1101L);
		assertEquals(ByteUtil.indexMask(List.of("a", "b", "c"), "b", "e", "a"), 0b11L);
	}

	@Test
	public void testIndexMaskInt() {
		assertEquals(ByteUtil.indexMaskInt(List.of()), 0);
		assertEquals(ByteUtil.indexMaskInt(List.of(), "a", "b"), 0);
		assertEquals(ByteUtil.indexMaskInt(List.of("a", "b", "c", "d")), 0);
		assertEquals(ByteUtil.indexMaskInt(List.of("a", "b", "c", "d"), "c", "a", "d"), 0b1101);
		assertEquals(ByteUtil.indexMaskInt(List.of("a", "b", "c"), "b", "e", "a"), 0b11);
	}

	@Test
	public void testBits() {
		assertArray(ByteUtil.bits(0));
		assertArray(ByteUtil.bits(0x80402010), 4, 13, 22, 31);
		assertArray(ByteUtil.bits(-1), IntStream.range(0, 32).toArray());
		assertArray(ByteUtil.bits(0L));
		assertArray(ByteUtil.bits(0x8000400020001000L), 12, 29, 46, 63);
		assertArray(ByteUtil.bits(-1L), IntStream.range(0, 64).toArray());
	}

	@Test
	public void testBit() {
		assertFalse(ByteUtil.bit(0, 0));
		assertFalse(ByteUtil.bit(0, 63));
		assertTrue(ByteUtil.bit(Long.MIN_VALUE, 63));
		assertFalse(ByteUtil.bit(Long.MAX_VALUE, 63));
		for (int i = 0; i < 63; i++)
			assertTrue(ByteUtil.bit(Long.MAX_VALUE, i));
		assertFalse(ByteUtil.bit(0x5a, 0));
		assertTrue(ByteUtil.bit(0x5a, 1));
		assertFalse(ByteUtil.bit(0x5a, 2));
		assertTrue(ByteUtil.bit(0x5a, 3));
		assertTrue(ByteUtil.bit(0x5a, 4));
		assertFalse(ByteUtil.bit(0x5a, 5));
		assertTrue(ByteUtil.bit(0x5a, 6));
		assertFalse(ByteUtil.bit(0x5a, 7));
	}

	@Test
	public void testToMsb() {
		assertArray(ByteUtil.toMsb(Short.MIN_VALUE), 0x80, 0);
		assertArray(ByteUtil.toMsb(Integer.MIN_VALUE), 0x80, 0, 0, 0);
		assertArray(ByteUtil.toMsb(Long.MIN_VALUE), 0x80, 0, 0, 0, 0, 0, 0, 0);
	}

	@Test
	public void testToLsb() {
		assertArray(ByteUtil.toLsb(Short.MIN_VALUE), 0, 0x80);
		assertArray(ByteUtil.toLsb(Integer.MIN_VALUE), 0, 0, 0, 0x80);
		assertArray(ByteUtil.toLsb(Long.MIN_VALUE), 0, 0, 0, 0, 0, 0, 0, 0x80);
	}

	@Test
	public void testWriteMsb() {
		byte[] b = new byte[8];
		assertEquals(ByteUtil.writeMsb(0xabcd_ef01_2345_6789L, b), 8);
		assertArray(b, 0xab, 0xcd, 0xef, 0x01, 0x23, 0x45, 0x67, 0x89);
		b = new byte[8];
		assertEquals(ByteUtil.writeMsb(0xabcd_ef01_2345_6789L, b, 1, 3), 4);
		assertArray(b, 0, 0x45, 0x67, 0x89, 0, 0, 0, 0);
	}

	@Test
	public void testWriteLsb() {
		byte[] b = new byte[8];
		assertEquals(ByteUtil.writeLsb(0xabcd_ef01_2345_6789L, b), 8);
		assertArray(b, 0x89, 0x67, 0x45, 0x23, 0x01, 0xef, 0xcd, 0xab);
		b = new byte[8];
		assertEquals(ByteUtil.writeLsb(0xabcd_ef01_2345_6789L, b, 1, 3), 4);
		assertArray(b, 0, 0x89, 0x67, 0x45, 0, 0, 0, 0);
	}

	@Test
	public void testFromMsb() {
		assertEquals(ByteUtil.fromMsb(0xab, 0xcd, 0xef, 0x01, 0x23, 0x45, 0x67, 0x89),
			0xabcd_ef01_2345_6789L);
		assertEquals(
			ByteUtil.fromMsb(ArrayUtil.bytes(0xab, 0xcd, 0xef, 0x01, 0x23, 0x45, 0x67, 0x89), 1, 3),
			0xcdef01L);
	}

	@Test
	public void testFromLsb() {
		assertEquals(ByteUtil.fromLsb(0xab, 0xcd, 0xef, 0x01, 0x23, 0x45, 0x67, 0x89),
			0x8967_4523_01ef_cdabL);
		assertEquals(ByteUtil.fromLsb( //
			ArrayUtil.bytes(0xab, 0xcd, 0xef, 0x01, 0x23, 0x45, 0x67, 0x89), 1, 3), 0x01efcdL);
	}

	@Test
	public void testByteAt() {
		assertEquals(ByteUtil.byteAt(0xfedcba9876543210L, 0), (byte) 0x10);
		assertEquals(ByteUtil.byteAt(0xfedcba9876543210L, 7), (byte) 0xfe);
		assertEquals(ByteUtil.ubyteAt(0xfedcba9876543210L, 0), (short) 0x10);
		assertEquals(ByteUtil.ubyteAt(0xfedcba9876543210L, 7), (short) 0xfe);
	}

	@Test
	public void testShiftBits() {
		assertEquals(ByteUtil.shiftBits((byte) 0, 1), (byte) 0);
		assertEquals(ByteUtil.shiftBits((byte) 0x96, 0), (byte) 0x96);
		assertEquals(ByteUtil.shiftBits((byte) 0x96, 1), (byte) 0x4b);
		assertEquals(ByteUtil.shiftBits((byte) 0x96, -1), (byte) 0x2c);
		assertEquals(ByteUtil.shiftBits((byte) 0x96, 7), (byte) 0x01);
		assertEquals(ByteUtil.shiftBits((byte) 0x96, -7), (byte) 0);
		assertEquals(ByteUtil.shiftBits((byte) 0x96, 8), (byte) 0);
		assertEquals(ByteUtil.shiftBits((byte) 0x96, -8), (byte) 0);
		assertEquals(ByteUtil.shiftBits((byte) 0, 1), (byte) 0);
		assertEquals(ByteUtil.shiftBits((short) 0, 15), (short) 0);
		assertEquals(ByteUtil.shiftBits(0, 31), 0);
		assertEquals(ByteUtil.shiftBits(0L, 63), 0L);
	}

	@Test
	public void testShift() {
		assertEquals(ByteUtil.shift((short) 0xabcd, 0), (short) 0xabcd);
		assertEquals(ByteUtil.shift((short) 0xabcd, 1), (short) 0xab);
		assertEquals(ByteUtil.shift((short) 0xabcd, -1), (short) 0xcd00);
		assertEquals(ByteUtil.shift((short) 0xabcd, 2), (short) 0);
		assertEquals(ByteUtil.shift((short) 0xabcd, -2), (short) 0);
		assertEquals(ByteUtil.shift(0xabcd_0123, 0), 0xabcd_0123);
		assertEquals(ByteUtil.shift(0xabcd_0123, 1), 0xab_cd01);
		assertEquals(ByteUtil.shift(0xabcd_0123, -1), 0xcd01_2300);
		assertEquals(ByteUtil.shift(0xabcd_0123, 3), 0xab);
		assertEquals(ByteUtil.shift(0xabcd_0123, -3), 0x2300_0000);
		assertEquals(ByteUtil.shift(0xabcd_0123, 4), 0);
		assertEquals(ByteUtil.shift(0xabcd_0123, -4), 0);
		assertEquals(ByteUtil.shift(0xabcd_0123_ef45_6789L, 0), 0xabcd_0123_ef45_6789L);
		assertEquals(ByteUtil.shift(0xabcd_0123_ef45_6789L, 1), 0xab_cd01_23ef_4567L);
		assertEquals(ByteUtil.shift(0xabcd_0123_ef45_6789L, -1), 0xcd01_23ef_4567_8900L);
		assertEquals(ByteUtil.shift(0xabcd_0123_ef45_6789L, 7), 0xabL);
		assertEquals(ByteUtil.shift(0xabcd_0123_ef45_6789L, -7), 0x8900_0000_0000_0000L);
		assertEquals(ByteUtil.shift(0xabcd_0123_ef45_6789L, 8), 0L);
		assertEquals(ByteUtil.shift(0xabcd_0123_ef45_6789L, -8), 0L);
		assertEquals(ByteUtil.shift(0xabcd_0123_ef45_6789L, 9), 0L);
		assertEquals(ByteUtil.shift(0xabcd_0123_ef45_6789L, -9), 0L);
	}

	@Test
	public void testInvert() {
		assertEquals(ByteUtil.invertByte(0x96), (byte) 0x69);
		assertEquals(ByteUtil.invertByte(0x69), (byte) 0x96);
		assertEquals(ByteUtil.invertShort(0x9669), (short) 0x6996);
		assertEquals(ByteUtil.invertShort(0x6996), (short) 0x9669);
	}

	@Test
	public void testReverse() {
		assertEquals(ByteUtil.reverseByte(0x96), (byte) 0x69);
		assertEquals(ByteUtil.reverseByte(0x6), (byte) 0x60);
		assertEquals(ByteUtil.reverseByte(0x169), (byte) 0x96);
		assertEquals(ByteUtil.reverseShort(0x9696), (short) 0x6969);
		assertEquals(ByteUtil.reverseShort(0x6969), (short) 0x9696);
		assertEquals(ByteUtil.reverseShort(0x16969), (short) 0x9696);
		assertEquals(ByteUtil.reverseAsInt(0x96, 7), 0x34); // 10010110 -> 00110100
		assertEquals(ByteUtil.reverse(0x96, 33), 0xd2000000L);
	}

}
