package ceri.common.data;

import static ceri.common.test.TestUtil.provider;
import static java.nio.charset.StandardCharsets.UTF_8;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.stream.IntStream;
import org.junit.Test;
import ceri.common.array.ArrayUtil;
import ceri.common.collect.Iterables;
import ceri.common.stream.Streams;
import ceri.common.test.Assert;
import ceri.common.test.Captor;

public class ByteUtilTest {

	@Test
	public void testConstructorIsPrivate() {
		Assert.privateConstructor(ByteUtil.class);
	}

	@Test
	public void testBitIteratorHigh() {
		Captor<Boolean> captor = Captor.of();
		for (boolean b : Iterables.of(ByteUtil.bitIterator(true, provider(0xa9, 0, 0xff))))
			captor.accept(b);
		captor.verify(true, false, true, false, true, false, false, true, //
			false, false, false, false, false, false, false, false, //
			true, true, true, true, true, true, true, true);
	}

	@Test
	public void testBitIteratorLow() {
		Captor<Boolean> captor = Captor.of();
		for (boolean b : Iterables.of(ByteUtil.bitIterator(false, provider(0xa9, 0, 0xff))))
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
		Assert.equal(ByteUtil.reduceMask(true, 0x8040201010204080L, 0L, (_, n) -> n + 1), 8L);
		Assert.equal(ByteUtil.reduceMask(false, 0x8040201010204080L, 0L, (_, n) -> n + 1), 8L);
	}

	@Test
	public void testReduceMaskInt() {
		Assert.equal(ByteUtil.reduceMaskInt(true, 0x80402010, 0, (_, n) -> n + 1), 4);
		Assert.equal(ByteUtil.reduceMaskInt(false, 0x80402010, 0, (_, n) -> n + 1), 4);
	}

	@Test
	public void testToHex() {
		byte[] b = ArrayUtil.bytes.of(-1, 0, 127, 128);
		Assert.isNull(ByteUtil.toHex((byte[]) null, ""));
		Assert.isNull(ByteUtil.toHex((byte[]) null, 0, 0, ""));
		Assert.equal(ByteUtil.toHex(b, ""), "ff007f80");
		Assert.equal(ByteUtil.toHex(b, ":"), "ff:00:7f:80");
		Assert.equal(ByteUtil.toHex(b, "-"), "ff-00-7f-80");
	}

	@Test
	public void testFromHex() {
		Assert.array(ByteUtil.fromHex("abcde").copy(0), 0x0a, 0xbc, 0xde);
		Assert.array(ByteUtil.fromHex("abcdef").copy(0), 0xab, 0xcd, 0xef);
		Assert.isNull(ByteUtil.fromHex(null));
		Assert.array(ByteUtil.fromHex("").copy(0));
	}

	@Test
	public void testStreamOf() {
		byte[] b = ArrayUtil.bytes.of(-1, 0, 1, 127, 128);
		Assert.stream(ByteUtil.ustream(b), 0xff, 0, 1, 0x7f, 0x80);
		Assert.stream(ByteUtil.ustream(-1, 0, 1, 127, 128), 0xff, 0, 1, 0x7f, 0x80);
	}

	@Test
	public void testToByteArray() {
		Assert.array(ByteUtil.bytes(List.of(-1, 0, 127, 128)), -1, 0, 127, 128);
		Assert.array(ByteUtil.bytes(Streams.ints(-1, 0, 127, 128)), -1, 0, 127, 128);
	}

	@Test
	public void testBytesFromBuffer() {
		ByteBuffer buffer = ByteBuffer.wrap(ArrayUtil.bytes.of(1, 2, 3, 4, 5));
		buffer.position(2).limit(4);
		Assert.array(ByteUtil.bytes(buffer), 3, 4);
	}

	@Test
	public void testFill() {
		Assert.array(ByteUtil.fill(3, 0xff), 0xff, 0xff, 0xff);
		Assert.array(ByteUtil.fill(0, 0xff));
	}

	@Test
	public void testReadByteArrayFromByteBuffer() {
		Assert.array(ByteUtil.readFrom(null, 1, 0));
		ByteBuffer buffer = ByteBuffer.wrap(ArrayUtil.bytes.of(1, 2, 3, 4, 5));
		Assert.array(ByteUtil.readFrom(buffer, 1, 3), 2, 3, 4);
	}

	@Test
	public void testReadFromByteBuffer() {
		ByteBuffer buffer = ByteBuffer.wrap(ArrayUtil.bytes.of(1, 2, 3, 4, 5));
		byte[] bytes = new byte[3];
		Assert.equal(ByteUtil.readFrom(buffer, 1, bytes), 3);
		Assert.array(bytes, 2, 3, 4);
	}

	@Test
	public void testWriteToByteBuffer() {
		byte[] bytes = new byte[5];
		ByteBuffer buffer = ByteBuffer.wrap(bytes);
		Assert.equal(ByteUtil.writeTo(buffer, 1, 1, 2, 3), 3);
		Assert.array(bytes, 0, 1, 2, 3, 0);
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
		Assert.array(b.toByteArray(), -1, 2, 4, 5, -1, 0, 128, 128, -1);
	}

	@Test
	public void testWriteToWithExceptions() {
		ByteProvider badProvider = badProvider(3);
		ByteArrayOutputStream b = new ByteArrayOutputStream();
		Assert.thrown(() -> ByteUtil.writeTo(b, badProvider));
		Assert.thrown(() -> ByteUtil.writeTo(b, badProvider, 1));
		Assert.thrown(() -> ByteUtil.writeTo(b, badProvider, 0, 2));
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
		Assert.array(ByteUtil.toAscii("\0\t\r\ntest").copy(0), //
			0, '\t', '\r', '\n', 't', 'e', 's', 't');
	}

	@Test
	public void testFromAscii() {
		Assert.equal(ByteUtil.fromAscii(0, '\t', '\r', '\n', 't', 'e', 's', 't'), "\0\t\r\ntest");
	}

	@Test
	public void testFromNullTerm() {
		Assert.equal(ByteUtil.fromNullTerm(ArrayUtil.bytes.of(0, 't', 'e', 's', 't'), UTF_8), "");
		Assert.equal(ByteUtil.fromNullTerm(ArrayUtil.bytes.of('t', 'e', 's', 't'), UTF_8), "test");
		Assert.equal(ByteUtil.fromNullTerm(
			ArrayUtil.bytes.of('\t', '\r', '\n', 0, 't', 'e', 's', 't'), UTF_8), "\t\r\n");
		Assert.equal(ByteUtil.fromNullTerm(provider('t', 'e', 's', 't', 0, 0), UTF_8), "test");
	}

	@Test
	public void testGetValue() {
		Assert.equal(ByteUtil.getValue(0L, 4, 0), 0L);
		Assert.equal(ByteUtil.getValue(-1L, 4, 32), 0xfL);
		Assert.equal(ByteUtil.getValue(-1L, 64, 0), -1L);
		Assert.equal(ByteUtil.getValue(0xfedcba9876543210L, 16, 40), 0xdcbaL);
		Assert.equal(ByteUtil.getValue(0xfedcba9876543210L, 32, 48), 0xfedcL);
	}

	@Test
	public void testGetValueInt() {
		Assert.equal(ByteUtil.getValueInt(0, 4, 0), 0);
		Assert.equal(ByteUtil.getValueInt(-1, 4, 16), 0xf);
		Assert.equal(ByteUtil.getValueInt(-1, 32, 0), -1);
		Assert.equal(ByteUtil.getValueInt(0xfedcba98, 16, 8), 0xdcba);
		Assert.equal(ByteUtil.getValueInt(0xfedcba98, 32, 16), 0xfedc);
	}

	@Test
	public void testSetValue() {
		Assert.equal(ByteUtil.setValue(0L, 4, 0, 0b10101L), 0x5L);
		Assert.equal(ByteUtil.setValue(-1L, 4, 32, 0b10101L), 0xfffffff5ffffffffL);
		Assert.equal(ByteUtil.setValue(-1L, 0, 32, 0b10101L), -1L);
		Assert.equal(ByteUtil.setValue(-1L, 64, 0, 0b10101L), 0x15L);
		Assert.equal(ByteUtil.setValue(-1L, 0, 0, 0b10101L), -1L);
	}

	@Test
	public void testSetValueInt() {
		Assert.equal(ByteUtil.setValueInt(0, 4, 0, 0b10101), 0x5);
		Assert.equal(ByteUtil.setValueInt(-1, 4, 16, 0b10101), 0xfff5ffff);
		Assert.equal(ByteUtil.setValueInt(-1, 0, 16, 0b10101), -1);
		Assert.equal(ByteUtil.setValueInt(-1, 32, 0, 0b10101), 0x15);
		Assert.equal(ByteUtil.setValueInt(-1, 0, 0, 0b10101), -1);
	}

	@Test
	public void testApplyBit() {
		Assert.equal(ByteUtil.applyBits(0, false, 0), 0L);
		Assert.equal(ByteUtil.applyBits(0, true, 0), 1L);
		Assert.equal(ByteUtil.applyBits(0, false, 63), 0L);
		Assert.equal(ByteUtil.applyBits(0, true, 63), 0x8000000000000000L);
		Assert.equal(ByteUtil.applyBits(0, false, 64), 0L);
		Assert.equal(ByteUtil.applyBits(0, true, 65), 0L);
		Assert.equal(ByteUtil.applyBits(-1L, false, 0), 0xfffffffffffffffeL);
		Assert.equal(ByteUtil.applyBits(-1L, true, 0), 0xffffffffffffffffL);
		Assert.equal(ByteUtil.applyBits(-1L, false, 63), 0x7fffffffffffffffL);
		Assert.equal(ByteUtil.applyBits(-1L, true, 63), 0xffffffffffffffffL);
		Assert.equal(ByteUtil.applyBits(-1L, false, 64), 0xffffffffffffffffL);
		Assert.equal(ByteUtil.applyBits(-1L, true, 65), 0xffffffffffffffffL);
	}

	@Test
	public void testApplyBitInt() {
		Assert.equal(ByteUtil.applyBitsInt(0, false, 0), 0);
		Assert.equal(ByteUtil.applyBitsInt(0, true, 0), 1);
		Assert.equal(ByteUtil.applyBitsInt(0, false, 31), 0);
		Assert.equal(ByteUtil.applyBitsInt(0, true, 31), 0x80000000);
		Assert.equal(ByteUtil.applyBitsInt(0, false, 32), 0);
		Assert.equal(ByteUtil.applyBitsInt(0, true, 33), 0);
		Assert.equal(ByteUtil.applyBitsInt(-1, false, 0), 0xfffffffe);
		Assert.equal(ByteUtil.applyBitsInt(-1, true, 0), 0xffffffff);
		Assert.equal(ByteUtil.applyBitsInt(-1, false, 31), 0x7fffffff);
		Assert.equal(ByteUtil.applyBitsInt(-1, true, 31), 0xffffffff);
		Assert.equal(ByteUtil.applyBitsInt(-1, false, 32), 0xffffffff);
		Assert.equal(ByteUtil.applyBitsInt(-1, true, 33), 0xffffffff);
	}

	@Test
	public void testApplyMask() {
		Assert.equal(ByteUtil.applyMask(0xffff0000_ffff0000L, 0x00ffff00_00ffff00L, false),
			0xff000000_ff000000L);
		Assert.equal(ByteUtil.applyMask(0xffff0000_ffff0000L, 0x00ffff00_00ffff00L, true),
			0xffffff00_ffffff00L);
		Assert.equal(
			ByteUtil.applyMask(0xffff0000_ffff0000L, 0x00ffff00_00ffff00L, 0x12345678_12345678L),
			0xff345600_ff345600L);
	}

	@Test
	public void testApplyMaskInt() {
		Assert.equal(ByteUtil.applyMaskInt(0xffff0000, 0x00ffff00, false), 0xff000000);
		Assert.equal(ByteUtil.applyMaskInt(0xffff0000, 0x00ffff00, true), 0xffffff00);
		Assert.equal(ByteUtil.applyMaskInt(0xffff0000, 0x00ffff00, 0x12345678), 0xff345600);
	}

	@Test
	public void testMaskInt() {
		Assert.equal(ByteUtil.maskInt(0), 0);
		Assert.equal(ByteUtil.maskInt(32), 0xffffffff);
		Assert.equal(ByteUtil.maskInt(11), 0x7ff);
		Assert.equal(ByteUtil.maskInt(5, 11), 0xffe0);
		Assert.equal(ByteUtil.maskInt(5, 32), 0xffffffe0);
		Assert.equal(ByteUtil.maskInt(32, 5), 0);
	}

	@Test
	public void testMask() {
		Assert.equal(ByteUtil.mask(0), 0L);
		Assert.equal(ByteUtil.mask(7), 0x7fL);
		Assert.equal(ByteUtil.mask(64), 0xffffffff_ffffffffL);
		Assert.equal(ByteUtil.mask(100), 0xffffffff_ffffffffL);
		Assert.equal(ByteUtil.mask(10, 0), 0L);
		Assert.equal(ByteUtil.mask(10, 7), 0x1fc00L);
		Assert.equal(ByteUtil.mask(10, 64), 0xffffffff_fffffc00L);
		Assert.equal(ByteUtil.mask(64, 10), 0L);
	}

	@Test
	public void testMaskOfBits() {
		Assert.equal(ByteUtil.maskOfBits((int[]) null), 0L);
		Assert.equal(ByteUtil.maskOfBits((List<Integer>) null), 0L);
		Assert.equal(ByteUtil.maskOfBits(64, 63, 32, 31, 16, 15, 8, 7, 0, -1),
			0x8000_0001_8001_8181L);
		Assert.equal(ByteUtil.maskOfBits(List.of(64, 63, 32, 31, 16, 15, 8, 7, 0, -1)),
			0x8000_0001_8001_8181L);
	}

	@Test
	public void testMaskOfBitsInt() {
		Assert.equal(ByteUtil.maskOfBitsInt((int[]) null), 0);
		Assert.equal(ByteUtil.maskOfBitsInt((List<Integer>) null), 0);
		Assert.equal(ByteUtil.maskOfBitsInt(32, 31, 16, 15, 8, 7, 0, -1), 0x80018181);
		Assert.equal(ByteUtil.maskOfBitsInt(List.of(32, 31, 16, 15, 8, 7, 0, -1)), 0x80018181);
	}

	@Test
	public void testMaskOfBit() {
		Assert.equal(ByteUtil.maskOfBit(true, 63), 0x8000_0000_0000_0000L);
		Assert.equal(ByteUtil.maskOfBit(false, 63), 0L);
		Assert.equal(ByteUtil.maskOfBit(true, 64), 0L);
		Assert.equal(ByteUtil.maskOfBit(true, -1), 0L);
	}

	@Test
	public void testMaskOfBitInt() {
		Assert.equal(ByteUtil.maskOfBitInt(true, 31), 0x80000000);
		Assert.equal(ByteUtil.maskOfBitInt(false, 31), 0);
		Assert.equal(ByteUtil.maskOfBitInt(true, 32), 0);
		Assert.equal(ByteUtil.maskOfBitInt(true, -1), 0);
	}

	@Test
	public void testMasked() {
		Assert.equal(ByteUtil.masked(0), true);
		Assert.equal(ByteUtil.masked(1), true);
		Assert.equal(ByteUtil.masked(0, 1), false);
		Assert.equal(ByteUtil.masked(1, 0), true);
		Assert.equal(ByteUtil.masked(1, 1), false);
		Assert.equal(ByteUtil.masked(0xf, 0, 1, 2, 3), true);
		Assert.equal(ByteUtil.masked(0xf, 0, 1, 2, 3, 4), false);
	}

	@Test
	public void testBits() {
		Assert.array(ByteUtil.bits(0));
		Assert.array(ByteUtil.bits(0x80402010), 4, 13, 22, 31);
		Assert.array(ByteUtil.bits(-1), IntStream.range(0, 32).toArray());
		Assert.array(ByteUtil.bits(0L));
		Assert.array(ByteUtil.bits(0x8000400020001000L), 12, 29, 46, 63);
		Assert.array(ByteUtil.bits(-1L), IntStream.range(0, 64).toArray());
	}

	@Test
	public void testBit() {
		Assert.no(ByteUtil.bit(0, 0));
		Assert.no(ByteUtil.bit(0, 63));
		Assert.yes(ByteUtil.bit(Long.MIN_VALUE, 63));
		Assert.no(ByteUtil.bit(Long.MAX_VALUE, 63));
		for (int i = 0; i < 63; i++)
			Assert.yes(ByteUtil.bit(Long.MAX_VALUE, i));
		Assert.no(ByteUtil.bit(0x5a, 0));
		Assert.yes(ByteUtil.bit(0x5a, 1));
		Assert.no(ByteUtil.bit(0x5a, 2));
		Assert.yes(ByteUtil.bit(0x5a, 3));
		Assert.yes(ByteUtil.bit(0x5a, 4));
		Assert.no(ByteUtil.bit(0x5a, 5));
		Assert.yes(ByteUtil.bit(0x5a, 6));
		Assert.no(ByteUtil.bit(0x5a, 7));
	}

	@Test
	public void testToMsb() {
		Assert.array(ByteUtil.toMsb(Short.MIN_VALUE), 0x80, 0);
		Assert.array(ByteUtil.toMsb(Integer.MIN_VALUE), 0x80, 0, 0, 0);
		Assert.array(ByteUtil.toMsb(Long.MIN_VALUE), 0x80, 0, 0, 0, 0, 0, 0, 0);
	}

	@Test
	public void testToLsb() {
		Assert.array(ByteUtil.toLsb(Short.MIN_VALUE), 0, 0x80);
		Assert.array(ByteUtil.toLsb(Integer.MIN_VALUE), 0, 0, 0, 0x80);
		Assert.array(ByteUtil.toLsb(Long.MIN_VALUE), 0, 0, 0, 0, 0, 0, 0, 0x80);
	}

	@Test
	public void testWriteMsb() {
		byte[] b = new byte[8];
		Assert.equal(ByteUtil.writeMsb(0xabcd_ef01_2345_6789L, b), 8);
		Assert.array(b, 0xab, 0xcd, 0xef, 0x01, 0x23, 0x45, 0x67, 0x89);
		b = new byte[8];
		Assert.equal(ByteUtil.writeMsb(0xabcd_ef01_2345_6789L, b, 1, 3), 4);
		Assert.array(b, 0, 0x45, 0x67, 0x89, 0, 0, 0, 0);
	}

	@Test
	public void testWriteLsb() {
		byte[] b = new byte[8];
		Assert.equal(ByteUtil.writeLsb(0xabcd_ef01_2345_6789L, b), 8);
		Assert.array(b, 0x89, 0x67, 0x45, 0x23, 0x01, 0xef, 0xcd, 0xab);
		b = new byte[8];
		Assert.equal(ByteUtil.writeLsb(0xabcd_ef01_2345_6789L, b, 1, 3), 4);
		Assert.array(b, 0, 0x89, 0x67, 0x45, 0, 0, 0, 0);
	}

	@Test
	public void testFromMsb() {
		Assert.equal(ByteUtil.fromMsb(0xab, 0xcd, 0xef, 0x01, 0x23, 0x45, 0x67, 0x89),
			0xabcd_ef01_2345_6789L);
		Assert.equal(ByteUtil.fromMsb(
			ArrayUtil.bytes.of(0xab, 0xcd, 0xef, 0x01, 0x23, 0x45, 0x67, 0x89), 1, 3), 0xcdef01L);
	}

	@Test
	public void testFromLsb() {
		Assert.equal(ByteUtil.fromLsb(0xab, 0xcd, 0xef, 0x01, 0x23, 0x45, 0x67, 0x89),
			0x8967_4523_01ef_cdabL);
		Assert.equal(ByteUtil.fromLsb( //
			ArrayUtil.bytes.of(0xab, 0xcd, 0xef, 0x01, 0x23, 0x45, 0x67, 0x89), 1, 3), 0x01efcdL);
	}

	@Test
	public void testByteAt() {
		Assert.equal(ByteUtil.byteAt(0xfedcba9876543210L, 0), (byte) 0x10);
		Assert.equal(ByteUtil.byteAt(0xfedcba9876543210L, 7), (byte) 0xfe);
		Assert.equal(ByteUtil.ubyteAt(0xfedcba9876543210L, 0), (short) 0x10);
		Assert.equal(ByteUtil.ubyteAt(0xfedcba9876543210L, 7), (short) 0xfe);
	}

	@Test
	public void testShiftBits() {
		Assert.equal(ByteUtil.shiftBits((byte) 0, 1), (byte) 0);
		Assert.equal(ByteUtil.shiftBits((byte) 0x96, 0), (byte) 0x96);
		Assert.equal(ByteUtil.shiftBits((byte) 0x96, 1), (byte) 0x4b);
		Assert.equal(ByteUtil.shiftBits((byte) 0x96, -1), (byte) 0x2c);
		Assert.equal(ByteUtil.shiftBits((byte) 0x96, 7), (byte) 0x01);
		Assert.equal(ByteUtil.shiftBits((byte) 0x96, -7), (byte) 0);
		Assert.equal(ByteUtil.shiftBits((byte) 0x96, 8), (byte) 0);
		Assert.equal(ByteUtil.shiftBits((byte) 0x96, -8), (byte) 0);
		Assert.equal(ByteUtil.shiftBits((byte) 0, 1), (byte) 0);
		Assert.equal(ByteUtil.shiftBits((short) 0, 15), (short) 0);
		Assert.equal(ByteUtil.shiftBits(0, 31), 0);
		Assert.equal(ByteUtil.shiftBits(0L, 63), 0L);
	}

	@Test
	public void testShift() {
		Assert.equal(ByteUtil.shift((short) 0xabcd, 0), (short) 0xabcd);
		Assert.equal(ByteUtil.shift((short) 0xabcd, 1), (short) 0xab);
		Assert.equal(ByteUtil.shift((short) 0xabcd, -1), (short) 0xcd00);
		Assert.equal(ByteUtil.shift((short) 0xabcd, 2), (short) 0);
		Assert.equal(ByteUtil.shift((short) 0xabcd, -2), (short) 0);
		Assert.equal(ByteUtil.shift(0xabcd_0123, 0), 0xabcd_0123);
		Assert.equal(ByteUtil.shift(0xabcd_0123, 1), 0xab_cd01);
		Assert.equal(ByteUtil.shift(0xabcd_0123, -1), 0xcd01_2300);
		Assert.equal(ByteUtil.shift(0xabcd_0123, 3), 0xab);
		Assert.equal(ByteUtil.shift(0xabcd_0123, -3), 0x2300_0000);
		Assert.equal(ByteUtil.shift(0xabcd_0123, 4), 0);
		Assert.equal(ByteUtil.shift(0xabcd_0123, -4), 0);
		Assert.equal(ByteUtil.shift(0xabcd_0123_ef45_6789L, 0), 0xabcd_0123_ef45_6789L);
		Assert.equal(ByteUtil.shift(0xabcd_0123_ef45_6789L, 1), 0xab_cd01_23ef_4567L);
		Assert.equal(ByteUtil.shift(0xabcd_0123_ef45_6789L, -1), 0xcd01_23ef_4567_8900L);
		Assert.equal(ByteUtil.shift(0xabcd_0123_ef45_6789L, 7), 0xabL);
		Assert.equal(ByteUtil.shift(0xabcd_0123_ef45_6789L, -7), 0x8900_0000_0000_0000L);
		Assert.equal(ByteUtil.shift(0xabcd_0123_ef45_6789L, 8), 0L);
		Assert.equal(ByteUtil.shift(0xabcd_0123_ef45_6789L, -8), 0L);
		Assert.equal(ByteUtil.shift(0xabcd_0123_ef45_6789L, 9), 0L);
		Assert.equal(ByteUtil.shift(0xabcd_0123_ef45_6789L, -9), 0L);
	}

	@Test
	public void testInvert() {
		Assert.equal(ByteUtil.invertByte(0x96), (byte) 0x69);
		Assert.equal(ByteUtil.invertByte(0x69), (byte) 0x96);
		Assert.equal(ByteUtil.invertShort(0x9669), (short) 0x6996);
		Assert.equal(ByteUtil.invertShort(0x6996), (short) 0x9669);
	}

	@Test
	public void testReverse() {
		Assert.equal(ByteUtil.reverseByte(0x96), (byte) 0x69);
		Assert.equal(ByteUtil.reverseByte(0x6), (byte) 0x60);
		Assert.equal(ByteUtil.reverseByte(0x169), (byte) 0x96);
		Assert.equal(ByteUtil.reverseShort(0x9696), (short) 0x6969);
		Assert.equal(ByteUtil.reverseShort(0x6969), (short) 0x9696);
		Assert.equal(ByteUtil.reverseShort(0x16969), (short) 0x9696);
		Assert.equal(ByteUtil.reverseAsInt(0x96, 7), 0x34); // 10010110 -> 00110100
		Assert.equal(ByteUtil.reverse(0x96, 33), 0xd2000000L);
	}

}
