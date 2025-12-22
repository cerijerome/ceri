package ceri.common.data;

import static java.nio.charset.StandardCharsets.UTF_8;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteOrder;
import java.util.List;
import java.util.stream.IntStream;
import org.junit.Test;
import ceri.common.array.Array;
import ceri.common.collect.Iterables;
import ceri.common.stream.Streams;
import ceri.common.test.Assert;
import ceri.common.test.Captor;

public class BytesTest {

	@Test
	public void testConstructorIsPrivate() {
		Assert.privateConstructor(Bytes.class);
	}

	@Test
	public void testByteOrderSymbol() {
		Assert.string(Bytes.Order.symbol((Bytes.Order) null), "");
		Assert.string(Bytes.Order.symbol(Bytes.Order.big), ">");
		Assert.string(Bytes.Order.symbol(Bytes.Order.little), "<");
		Assert.string(Bytes.Order.symbol((ByteOrder) null), "");
	}

	@Test
	public void testByteOrderIsValid() {
		Assert.equal(Bytes.Order.isValid(null), false);
		Assert.equal(Bytes.Order.isValid(Bytes.Order.unspecified), false);
		Assert.equal(Bytes.Order.isValid(Bytes.Order.platform), true);
	}

	@Test
	public void testByteOrder() {
		Assert.equal(Bytes.Order.order(null), ByteOrder.nativeOrder());
		Assert.equal(Bytes.Order.order(Bytes.Order.big), ByteOrder.BIG_ENDIAN);
		Assert.equal(Bytes.Order.order(Bytes.Order.little), ByteOrder.LITTLE_ENDIAN);
	}

	@Test
	public void testBitIteratorHigh() {
		var captor = Captor.<Boolean>of();
		for (boolean b : Iterables.of(Bytes.bitIterator(true, ByteProvider.of(0xa9, 0, 0xff))))
			captor.accept(b);
		captor.verify(true, false, true, false, true, false, false, true, //
			false, false, false, false, false, false, false, false, //
			true, true, true, true, true, true, true, true);
	}

	@Test
	public void testBitIteratorLow() {
		var captor = Captor.<Boolean>of();
		for (boolean b : Iterables.of(Bytes.bitIterator(false, ByteProvider.of(0xa9, 0, 0xff))))
			captor.accept(b);
		captor.verify(true, false, false, true, false, true, false, true, //
			false, false, false, false, false, false, false, false, //
			true, true, true, true, true, true, true, true);
	}

	@Test
	public void testIterateMaskInt() {
		var captor = Captor.ofInt();
		Bytes.iterateMask(true, 0x84211248, captor::accept);
		captor.verifyInt(31, 26, 21, 16, 12, 9, 6, 3);
		captor.reset();
		Bytes.iterateMask(false, 0x84211248, captor::accept);
		captor.verifyInt(3, 6, 9, 12, 16, 21, 26, 31);
	}

	@Test
	public void testIterateMaskLong() {
		var captor = Captor.ofInt();
		Bytes.iterateMask(true, 0x8040201010204080L, captor::accept);
		captor.verifyInt(63, 54, 45, 36, 28, 21, 14, 7);
		captor.reset();
		Bytes.iterateMask(false, 0x8040201010204080L, captor::accept);
		captor.verifyInt(7, 14, 21, 28, 36, 45, 54, 63);
	}

	@Test
	public void testReduceMaskLong() {
		Assert.equal(Bytes.reduceMask(true, 0x8040201010204080L, 0L, (_, n) -> n + 1), 8L);
		Assert.equal(Bytes.reduceMask(false, 0x8040201010204080L, 0L, (_, n) -> n + 1), 8L);
	}

	@Test
	public void testReduceMaskInt() {
		Assert.equal(Bytes.reduceMaskInt(true, 0x80402010, 0, (_, n) -> n + 1), 4);
		Assert.equal(Bytes.reduceMaskInt(false, 0x80402010, 0, (_, n) -> n + 1), 4);
	}

	@Test
	public void testToHex() {
		byte[] b = Array.bytes.of(-1, 0, 127, 128);
		Assert.isNull(Bytes.toHex((byte[]) null, ""));
		Assert.isNull(Bytes.toHex((byte[]) null, 0, 0, ""));
		Assert.equal(Bytes.toHex(b, ""), "ff007f80");
		Assert.equal(Bytes.toHex(b, ":"), "ff:00:7f:80");
		Assert.equal(Bytes.toHex(b, "-"), "ff-00-7f-80");
	}

	@Test
	public void testFromHex() {
		Assert.array(Bytes.fromHex("abcde").copy(0), 0x0a, 0xbc, 0xde);
		Assert.array(Bytes.fromHex("abcdef").copy(0), 0xab, 0xcd, 0xef);
		Assert.isNull(Bytes.fromHex(null));
		Assert.array(Bytes.fromHex("").copy(0));
	}

	@Test
	public void testStreamOf() {
		byte[] b = Array.bytes.of(-1, 0, 1, 127, 128);
		Assert.stream(Bytes.ustream(b), 0xff, 0, 1, 0x7f, 0x80);
		Assert.stream(Bytes.ustream(-1, 0, 1, 127, 128), 0xff, 0, 1, 0x7f, 0x80);
	}

	@Test
	public void testToByteArray() {
		Assert.array(Bytes.bytes(List.of(-1, 0, 127, 128)), -1, 0, 127, 128);
		Assert.array(Bytes.bytes(Streams.ints(-1, 0, 127, 128)), -1, 0, 127, 128);
	}

	@Test
	public void testBytesFromBuffer() {
		var buffer = Bytes.buffer(1, 2, 3, 4, 5);
		buffer.position(2).limit(4);
		Assert.array(Bytes.bytes(buffer), 3, 4);
	}

	@Test
	public void testFill() {
		Assert.array(Bytes.fill(3, 0xff), 0xff, 0xff, 0xff);
		Assert.array(Bytes.fill(0, 0xff));
	}

	@Test
	public void testReadByteArrayFromByteBuffer() {
		Assert.array(Bytes.readFrom(null, 1, 0));
		var buffer = Bytes.buffer(1, 2, 3, 4, 5);
		Assert.array(Bytes.readFrom(buffer, 1, 3), 2, 3, 4);
	}

	@Test
	public void testReadFromByteBuffer() {
		var buffer = Bytes.buffer(1, 2, 3, 4, 5);
		byte[] bytes = new byte[3];
		Assert.equal(Bytes.readFrom(buffer, 1, bytes), 3);
		Assert.array(bytes, 2, 3, 4);
	}

	@Test
	public void testWriteToByteBuffer() {
		byte[] bytes = new byte[5];
		var buffer = Bytes.buffer(bytes);
		Assert.equal(Bytes.writeTo(buffer, 1, 1, 2, 3), 3);
		Assert.array(bytes, 0, 1, 2, 3, 0);
	}

	@Test
	public void testWriteTo() {
		var b = new ByteArrayOutputStream();
		var im = ByteProvider.of(-1, 0, 128);
		Bytes.writeTo(b, -1, 2);
		Bytes.writeTo(b, new byte[] { 3, 4, 5 }, 1);
		Bytes.writeTo(b, im);
		Bytes.writeTo(b, im, 2);
		Bytes.writeTo(b, im, 0, 1);
		Assert.array(b.toByteArray(), -1, 2, 4, 5, -1, 0, 128, 128, -1);
	}

	@Test
	public void testWriteToWithExceptions() {
		var badProvider = badProvider(3);
		var b = new ByteArrayOutputStream();
		Assert.thrown(() -> Bytes.writeTo(b, badProvider));
		Assert.thrown(() -> Bytes.writeTo(b, badProvider, 1));
		Assert.thrown(() -> Bytes.writeTo(b, badProvider, 0, 2));
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
		Assert.array(Bytes.toAscii("\0\t\r\ntest").copy(0), //
			0, '\t', '\r', '\n', 't', 'e', 's', 't');
	}

	@Test
	public void testFromAscii() {
		Assert.equal(Bytes.fromAscii(0, '\t', '\r', '\n', 't', 'e', 's', 't'), "\0\t\r\ntest");
	}

	@Test
	public void testFromNullTerm() {
		Assert.equal(Bytes.fromNullTerm(Array.bytes.of(0, 't', 'e', 's', 't'), UTF_8), "");
		Assert.equal(Bytes.fromNullTerm(Array.bytes.of('t', 'e', 's', 't'), UTF_8), "test");
		Assert.equal(
			Bytes.fromNullTerm(Array.bytes.of('\t', '\r', '\n', 0, 't', 'e', 's', 't'), UTF_8),
			"\t\r\n");
		Assert.equal(Bytes.fromNullTerm(ByteProvider.of('t', 'e', 's', 't', 0, 0), UTF_8), "test");
	}

	@Test
	public void testGetValue() {
		Assert.equal(Bytes.getValue(0L, 4, 0), 0L);
		Assert.equal(Bytes.getValue(-1L, 4, 32), 0xfL);
		Assert.equal(Bytes.getValue(-1L, 64, 0), -1L);
		Assert.equal(Bytes.getValue(0xfedcba9876543210L, 16, 40), 0xdcbaL);
		Assert.equal(Bytes.getValue(0xfedcba9876543210L, 32, 48), 0xfedcL);
	}

	@Test
	public void testGetValueInt() {
		Assert.equal(Bytes.getValueInt(0, 4, 0), 0);
		Assert.equal(Bytes.getValueInt(-1, 4, 16), 0xf);
		Assert.equal(Bytes.getValueInt(-1, 32, 0), -1);
		Assert.equal(Bytes.getValueInt(0xfedcba98, 16, 8), 0xdcba);
		Assert.equal(Bytes.getValueInt(0xfedcba98, 32, 16), 0xfedc);
	}

	@Test
	public void testSetValue() {
		Assert.equal(Bytes.setValue(0L, 4, 0, 0b10101L), 0x5L);
		Assert.equal(Bytes.setValue(-1L, 4, 32, 0b10101L), 0xfffffff5ffffffffL);
		Assert.equal(Bytes.setValue(-1L, 0, 32, 0b10101L), -1L);
		Assert.equal(Bytes.setValue(-1L, 64, 0, 0b10101L), 0x15L);
		Assert.equal(Bytes.setValue(-1L, 0, 0, 0b10101L), -1L);
	}

	@Test
	public void testSetValueInt() {
		Assert.equal(Bytes.setValueInt(0, 4, 0, 0b10101), 0x5);
		Assert.equal(Bytes.setValueInt(-1, 4, 16, 0b10101), 0xfff5ffff);
		Assert.equal(Bytes.setValueInt(-1, 0, 16, 0b10101), -1);
		Assert.equal(Bytes.setValueInt(-1, 32, 0, 0b10101), 0x15);
		Assert.equal(Bytes.setValueInt(-1, 0, 0, 0b10101), -1);
	}

	@Test
	public void testApplyBit() {
		Assert.equal(Bytes.applyBits(0, false, 0), 0L);
		Assert.equal(Bytes.applyBits(0, true, 0), 1L);
		Assert.equal(Bytes.applyBits(0, false, 63), 0L);
		Assert.equal(Bytes.applyBits(0, true, 63), 0x8000000000000000L);
		Assert.equal(Bytes.applyBits(0, false, 64), 0L);
		Assert.equal(Bytes.applyBits(0, true, 65), 0L);
		Assert.equal(Bytes.applyBits(-1L, false, 0), 0xfffffffffffffffeL);
		Assert.equal(Bytes.applyBits(-1L, true, 0), 0xffffffffffffffffL);
		Assert.equal(Bytes.applyBits(-1L, false, 63), 0x7fffffffffffffffL);
		Assert.equal(Bytes.applyBits(-1L, true, 63), 0xffffffffffffffffL);
		Assert.equal(Bytes.applyBits(-1L, false, 64), 0xffffffffffffffffL);
		Assert.equal(Bytes.applyBits(-1L, true, 65), 0xffffffffffffffffL);
	}

	@Test
	public void testApplyBitInt() {
		Assert.equal(Bytes.applyBitsInt(0, false, 0), 0);
		Assert.equal(Bytes.applyBitsInt(0, true, 0), 1);
		Assert.equal(Bytes.applyBitsInt(0, false, 31), 0);
		Assert.equal(Bytes.applyBitsInt(0, true, 31), 0x80000000);
		Assert.equal(Bytes.applyBitsInt(0, false, 32), 0);
		Assert.equal(Bytes.applyBitsInt(0, true, 33), 0);
		Assert.equal(Bytes.applyBitsInt(-1, false, 0), 0xfffffffe);
		Assert.equal(Bytes.applyBitsInt(-1, true, 0), 0xffffffff);
		Assert.equal(Bytes.applyBitsInt(-1, false, 31), 0x7fffffff);
		Assert.equal(Bytes.applyBitsInt(-1, true, 31), 0xffffffff);
		Assert.equal(Bytes.applyBitsInt(-1, false, 32), 0xffffffff);
		Assert.equal(Bytes.applyBitsInt(-1, true, 33), 0xffffffff);
	}

	@Test
	public void testApplyMask() {
		Assert.equal(Bytes.applyMask(0xffff0000_ffff0000L, 0x00ffff00_00ffff00L, false),
			0xff000000_ff000000L);
		Assert.equal(Bytes.applyMask(0xffff0000_ffff0000L, 0x00ffff00_00ffff00L, true),
			0xffffff00_ffffff00L);
		Assert.equal(
			Bytes.applyMask(0xffff0000_ffff0000L, 0x00ffff00_00ffff00L, 0x12345678_12345678L),
			0xff345600_ff345600L);
	}

	@Test
	public void testApplyMaskInt() {
		Assert.equal(Bytes.applyMaskInt(0xffff0000, 0x00ffff00, false), 0xff000000);
		Assert.equal(Bytes.applyMaskInt(0xffff0000, 0x00ffff00, true), 0xffffff00);
		Assert.equal(Bytes.applyMaskInt(0xffff0000, 0x00ffff00, 0x12345678), 0xff345600);
	}

	@Test
	public void testMaskInt() {
		Assert.equal(Bytes.maskInt(0), 0);
		Assert.equal(Bytes.maskInt(32), 0xffffffff);
		Assert.equal(Bytes.maskInt(11), 0x7ff);
		Assert.equal(Bytes.maskInt(5, 11), 0xffe0);
		Assert.equal(Bytes.maskInt(5, 32), 0xffffffe0);
		Assert.equal(Bytes.maskInt(32, 5), 0);
	}

	@Test
	public void testMask() {
		Assert.equal(Bytes.mask(0), 0L);
		Assert.equal(Bytes.mask(7), 0x7fL);
		Assert.equal(Bytes.mask(64), 0xffffffff_ffffffffL);
		Assert.equal(Bytes.mask(100), 0xffffffff_ffffffffL);
		Assert.equal(Bytes.mask(10, 0), 0L);
		Assert.equal(Bytes.mask(10, 7), 0x1fc00L);
		Assert.equal(Bytes.mask(10, 64), 0xffffffff_fffffc00L);
		Assert.equal(Bytes.mask(64, 10), 0L);
	}

	@Test
	public void testMaskOfBits() {
		Assert.equal(Bytes.maskOfBits((int[]) null), 0L);
		Assert.equal(Bytes.maskOfBits((List<Integer>) null), 0L);
		Assert.equal(Bytes.maskOfBits(64, 63, 32, 31, 16, 15, 8, 7, 0, -1), 0x8000_0001_8001_8181L);
		Assert.equal(Bytes.maskOfBits(List.of(64, 63, 32, 31, 16, 15, 8, 7, 0, -1)),
			0x8000_0001_8001_8181L);
	}

	@Test
	public void testMaskOfBitsInt() {
		Assert.equal(Bytes.maskOfBitsInt((int[]) null), 0);
		Assert.equal(Bytes.maskOfBitsInt((List<Integer>) null), 0);
		Assert.equal(Bytes.maskOfBitsInt(32, 31, 16, 15, 8, 7, 0, -1), 0x80018181);
		Assert.equal(Bytes.maskOfBitsInt(List.of(32, 31, 16, 15, 8, 7, 0, -1)), 0x80018181);
	}

	@Test
	public void testMaskOfBit() {
		Assert.equal(Bytes.maskOfBit(true, 63), 0x8000_0000_0000_0000L);
		Assert.equal(Bytes.maskOfBit(false, 63), 0L);
		Assert.equal(Bytes.maskOfBit(true, 64), 0L);
		Assert.equal(Bytes.maskOfBit(true, -1), 0L);
	}

	@Test
	public void testMaskOfBitInt() {
		Assert.equal(Bytes.maskOfBitInt(true, 31), 0x80000000);
		Assert.equal(Bytes.maskOfBitInt(false, 31), 0);
		Assert.equal(Bytes.maskOfBitInt(true, 32), 0);
		Assert.equal(Bytes.maskOfBitInt(true, -1), 0);
	}

	@Test
	public void testMasked() {
		Assert.equal(Bytes.masked(0), true);
		Assert.equal(Bytes.masked(1), true);
		Assert.equal(Bytes.masked(0, 1), false);
		Assert.equal(Bytes.masked(1, 0), true);
		Assert.equal(Bytes.masked(1, 1), false);
		Assert.equal(Bytes.masked(0xf, 0, 1, 2, 3), true);
		Assert.equal(Bytes.masked(0xf, 0, 1, 2, 3, 4), false);
	}

	@Test
	public void testBits() {
		Assert.array(Bytes.bits(0));
		Assert.array(Bytes.bits(0x80402010), 4, 13, 22, 31);
		Assert.array(Bytes.bits(-1), IntStream.range(0, 32).toArray());
		Assert.array(Bytes.bits(0L));
		Assert.array(Bytes.bits(0x8000400020001000L), 12, 29, 46, 63);
		Assert.array(Bytes.bits(-1L), IntStream.range(0, 64).toArray());
	}

	@Test
	public void testBit() {
		Assert.no(Bytes.bit(0, 0));
		Assert.no(Bytes.bit(0, 63));
		Assert.yes(Bytes.bit(Long.MIN_VALUE, 63));
		Assert.no(Bytes.bit(Long.MAX_VALUE, 63));
		for (int i = 0; i < 63; i++)
			Assert.yes(Bytes.bit(Long.MAX_VALUE, i));
		Assert.no(Bytes.bit(0x5a, 0));
		Assert.yes(Bytes.bit(0x5a, 1));
		Assert.no(Bytes.bit(0x5a, 2));
		Assert.yes(Bytes.bit(0x5a, 3));
		Assert.yes(Bytes.bit(0x5a, 4));
		Assert.no(Bytes.bit(0x5a, 5));
		Assert.yes(Bytes.bit(0x5a, 6));
		Assert.no(Bytes.bit(0x5a, 7));
	}

	@Test
	public void testToMsb() {
		Assert.array(Bytes.toMsb(Short.MIN_VALUE), 0x80, 0);
		Assert.array(Bytes.toMsb(Integer.MIN_VALUE), 0x80, 0, 0, 0);
		Assert.array(Bytes.toMsb(Long.MIN_VALUE), 0x80, 0, 0, 0, 0, 0, 0, 0);
	}

	@Test
	public void testToLsb() {
		Assert.array(Bytes.toLsb(Short.MIN_VALUE), 0, 0x80);
		Assert.array(Bytes.toLsb(Integer.MIN_VALUE), 0, 0, 0, 0x80);
		Assert.array(Bytes.toLsb(Long.MIN_VALUE), 0, 0, 0, 0, 0, 0, 0, 0x80);
	}

	@Test
	public void testWriteMsb() {
		byte[] b = new byte[8];
		Assert.equal(Bytes.writeMsb(0xabcd_ef01_2345_6789L, b), 8);
		Assert.array(b, 0xab, 0xcd, 0xef, 0x01, 0x23, 0x45, 0x67, 0x89);
		b = new byte[8];
		Assert.equal(Bytes.writeMsb(0xabcd_ef01_2345_6789L, b, 1, 3), 4);
		Assert.array(b, 0, 0x45, 0x67, 0x89, 0, 0, 0, 0);
	}

	@Test
	public void testWriteLsb() {
		byte[] b = new byte[8];
		Assert.equal(Bytes.writeLsb(0xabcd_ef01_2345_6789L, b), 8);
		Assert.array(b, 0x89, 0x67, 0x45, 0x23, 0x01, 0xef, 0xcd, 0xab);
		b = new byte[8];
		Assert.equal(Bytes.writeLsb(0xabcd_ef01_2345_6789L, b, 1, 3), 4);
		Assert.array(b, 0, 0x89, 0x67, 0x45, 0, 0, 0, 0);
	}

	@Test
	public void testFromMsb() {
		Assert.equal(Bytes.fromMsb(0xab, 0xcd, 0xef, 0x01, 0x23, 0x45, 0x67, 0x89),
			0xabcd_ef01_2345_6789L);
		Assert.equal(
			Bytes.fromMsb(Array.bytes.of(0xab, 0xcd, 0xef, 0x01, 0x23, 0x45, 0x67, 0x89), 1, 3),
			0xcdef01L);
	}

	@Test
	public void testFromLsb() {
		Assert.equal(Bytes.fromLsb(0xab, 0xcd, 0xef, 0x01, 0x23, 0x45, 0x67, 0x89),
			0x8967_4523_01ef_cdabL);
		Assert.equal(Bytes.fromLsb( //
			Array.bytes.of(0xab, 0xcd, 0xef, 0x01, 0x23, 0x45, 0x67, 0x89), 1, 3), 0x01efcdL);
	}

	@Test
	public void testByteAt() {
		Assert.equal(Bytes.byteAt(0xfedcba9876543210L, 0), (byte) 0x10);
		Assert.equal(Bytes.byteAt(0xfedcba9876543210L, 7), (byte) 0xfe);
		Assert.equal(Bytes.ubyteAt(0xfedcba9876543210L, 0), (short) 0x10);
		Assert.equal(Bytes.ubyteAt(0xfedcba9876543210L, 7), (short) 0xfe);
	}

	@Test
	public void testShiftBits() {
		Assert.equal(Bytes.shiftBits((byte) 0, 1), (byte) 0);
		Assert.equal(Bytes.shiftBits((byte) 0x96, 0), (byte) 0x96);
		Assert.equal(Bytes.shiftBits((byte) 0x96, 1), (byte) 0x4b);
		Assert.equal(Bytes.shiftBits((byte) 0x96, -1), (byte) 0x2c);
		Assert.equal(Bytes.shiftBits((byte) 0x96, 7), (byte) 0x01);
		Assert.equal(Bytes.shiftBits((byte) 0x96, -7), (byte) 0);
		Assert.equal(Bytes.shiftBits((byte) 0x96, 8), (byte) 0);
		Assert.equal(Bytes.shiftBits((byte) 0x96, -8), (byte) 0);
		Assert.equal(Bytes.shiftBits((byte) 0, 1), (byte) 0);
		Assert.equal(Bytes.shiftBits((short) 0, 15), (short) 0);
		Assert.equal(Bytes.shiftBits(0, 31), 0);
		Assert.equal(Bytes.shiftBits(0L, 63), 0L);
	}

	@Test
	public void testShift() {
		Assert.equal(Bytes.shift((short) 0xabcd, 0), (short) 0xabcd);
		Assert.equal(Bytes.shift((short) 0xabcd, 1), (short) 0xab);
		Assert.equal(Bytes.shift((short) 0xabcd, -1), (short) 0xcd00);
		Assert.equal(Bytes.shift((short) 0xabcd, 2), (short) 0);
		Assert.equal(Bytes.shift((short) 0xabcd, -2), (short) 0);
		Assert.equal(Bytes.shift(0xabcd_0123, 0), 0xabcd_0123);
		Assert.equal(Bytes.shift(0xabcd_0123, 1), 0xab_cd01);
		Assert.equal(Bytes.shift(0xabcd_0123, -1), 0xcd01_2300);
		Assert.equal(Bytes.shift(0xabcd_0123, 3), 0xab);
		Assert.equal(Bytes.shift(0xabcd_0123, -3), 0x2300_0000);
		Assert.equal(Bytes.shift(0xabcd_0123, 4), 0);
		Assert.equal(Bytes.shift(0xabcd_0123, -4), 0);
		Assert.equal(Bytes.shift(0xabcd_0123_ef45_6789L, 0), 0xabcd_0123_ef45_6789L);
		Assert.equal(Bytes.shift(0xabcd_0123_ef45_6789L, 1), 0xab_cd01_23ef_4567L);
		Assert.equal(Bytes.shift(0xabcd_0123_ef45_6789L, -1), 0xcd01_23ef_4567_8900L);
		Assert.equal(Bytes.shift(0xabcd_0123_ef45_6789L, 7), 0xabL);
		Assert.equal(Bytes.shift(0xabcd_0123_ef45_6789L, -7), 0x8900_0000_0000_0000L);
		Assert.equal(Bytes.shift(0xabcd_0123_ef45_6789L, 8), 0L);
		Assert.equal(Bytes.shift(0xabcd_0123_ef45_6789L, -8), 0L);
		Assert.equal(Bytes.shift(0xabcd_0123_ef45_6789L, 9), 0L);
		Assert.equal(Bytes.shift(0xabcd_0123_ef45_6789L, -9), 0L);
	}

	@Test
	public void testInvert() {
		Assert.equal(Bytes.invertByte(0x96), (byte) 0x69);
		Assert.equal(Bytes.invertByte(0x69), (byte) 0x96);
		Assert.equal(Bytes.invertShort(0x9669), (short) 0x6996);
		Assert.equal(Bytes.invertShort(0x6996), (short) 0x9669);
	}

	@Test
	public void testReverse() {
		Assert.equal(Bytes.reverseByte(0x96), (byte) 0x69);
		Assert.equal(Bytes.reverseByte(0x6), (byte) 0x60);
		Assert.equal(Bytes.reverseByte(0x169), (byte) 0x96);
		Assert.equal(Bytes.reverseShort(0x9696), (short) 0x6969);
		Assert.equal(Bytes.reverseShort(0x6969), (short) 0x9696);
		Assert.equal(Bytes.reverseShort(0x16969), (short) 0x9696);
		Assert.equal(Bytes.reverseAsInt(0x96, 7), 0x34); // 10010110 -> 00110100
		Assert.equal(Bytes.reverse(0x96, 33), 0xd2000000L);
	}
}
