package ceri.common.data;

import static ceri.common.test.TestUtil.assertArray;
import static ceri.common.test.TestUtil.assertPrivateConstructor;
import static ceri.common.test.TestUtil.assertStream;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doThrow;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.stream.IntStream;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import ceri.common.collection.ArrayUtil;
import ceri.common.test.TestUtil;

public class ByteUtilTest {
	@Mock
	ByteArrayOutputStream badByteArrayOutputStream;
	@Mock
	ByteProvider badByteProvider;

	@Before
	public void init() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void testConstructorIsPrivate() {
		assertPrivateConstructor(ByteUtil.class);
	}

	@Test
	public void testToHex() {
		ByteProvider b = ByteArray.Immutable.wrap(-1, 0, 127, 128);
		assertNull(ByteUtil.toHex((ByteProvider) null, ""));
		assertNull(ByteUtil.toHex((byte[]) null, ""));
		assertThat(ByteUtil.toHex(b, ""), is("ff007f80"));
		assertThat(ByteUtil.toHex(b, ":"), is("ff:00:7f:80"));
		assertThat(ByteUtil.toHex(b.copy(), "-"), is("ff-00-7f-80"));
	}

	@Test
	public void testFromHex() {
		assertArray(ByteUtil.fromHex("abcde").copy(), 0x0a, 0xbc, 0xde);
		assertArray(ByteUtil.fromHex("abcdef").copy(), 0xab, 0xcd, 0xef);
		assertNull(ByteUtil.fromHex(null));
		assertArray(ByteUtil.fromHex("").copy());
	}

	@Test
	public void testStreamOf() {
		byte[] b = ArrayUtil.bytes(-1, 0, 1, 127, 128);
		assertStream(ByteUtil.ustream(b), 0xff, 0, 1, 0x7f, 0x80);
	}

	@Test
	public void testToByteArray() {
		assertArray(ByteUtil.bytes(List.of(-1, 0, 127, 128)), -1, 0, 127, 128);
		assertArray(ByteUtil.bytes(IntStream.of(-1, 0, 127, 128)), -1, 0, 127, 128);
	}

	@Test
	public void testWriteTo() {
		ByteArrayOutputStream b = new ByteArrayOutputStream();
		ByteProvider im = ByteArray.Immutable.wrap(-1, 0, 128);
		ByteUtil.writeTo(b, -1, 2);
		ByteUtil.writeTo(b, new byte[] { 3, 4, 5 }, 1);
		ByteUtil.writeTo(b, im);
		ByteUtil.writeTo(b, im, 2);
		ByteUtil.writeTo(b, im, 0, 1);
		assertArray(b.toByteArray(), -1, 2, 4, 5, -1, 0, 128, 128, -1);
	}

	@SuppressWarnings("resource")
	@Test
	public void testWriteToWithExceptions() throws IOException {
		ByteArrayOutputStream b = badByteArrayOutputStream;
		ByteProvider im = badByteProvider;
		doThrow(new IOException()).when(badByteArrayOutputStream).write(any());
		doThrow(new RuntimeException()).when(badByteArrayOutputStream).write(any(), anyInt(),
			anyInt());
		doThrow(new IOException()).when(badByteProvider).writeTo(any(OutputStream.class));
		doThrow(new IOException()).when(badByteProvider).writeTo(any(OutputStream.class), anyInt());
		doThrow(new IOException()).when(badByteProvider).writeTo(anyInt(), any(OutputStream.class),
			anyInt());
		TestUtil.assertThrown(() -> ByteUtil.writeTo(b, -1, 2));
		TestUtil.assertThrown(() -> ByteUtil.writeTo(b, im));
		TestUtil.assertThrown(() -> ByteUtil.writeTo(b, im, 1));
		TestUtil.assertThrown(() -> ByteUtil.writeTo(b, im, 0, 2));
	}

	@Test
	public void testToAscii() {
		assertArray(ByteUtil.toAscii("\0\t\r\ntest").copy(), //
			0, '\t', '\r', '\n', 't', 'e', 's', 't');
	}

	@Test
	public void testFromAscii() {
		assertThat(ByteUtil.fromAscii(0, '\t', '\r', '\n', 't', 'e', 's', 't'), is("\0\t\r\ntest"));
	}

	@Test
	public void testApply() {
		assertThat(ByteUtil.applyMask(0xffff0000_ffff0000L, 0xffff00_00ffff00L, false),
			is(0xff000000_ff000000L));
		assertThat(ByteUtil.applyMask(0xffff0000_ffff0000L, 0xffff00_00ffff00L, true),
			is(0xffffff00_ffffff00L));
	}

	@Test
	public void testApplyInt() {
		assertThat(ByteUtil.applyMaskInt(0xffff0000, 0x00ffff00, false), is(0xff000000));
		assertThat(ByteUtil.applyMaskInt(0xffff0000, 0x00ffff00, true), is(0xffffff00));
	}

	@Test
	public void testMaskInt() {
		assertThat(ByteUtil.maskInt(0), is(0));
		assertThat(ByteUtil.maskInt(32), is(0xffffffff));
		assertThat(ByteUtil.maskInt(11), is(0x7ff));
		assertThat(ByteUtil.maskInt(5, 11), is(0xffe0));
	}

	@Test
	public void testMask() {
		assertThat(ByteUtil.mask(0), is(0L));
		assertThat(ByteUtil.mask(7), is(0x7fL));
		assertThat(ByteUtil.mask(64), is(0xffffffff_ffffffffL));
		assertThat(ByteUtil.mask(100), is(0xffffffff_ffffffffL));
		assertThat(ByteUtil.mask(10, 0), is(0L));
		assertThat(ByteUtil.mask(10, 7), is(0x1fc00L));
		assertThat(ByteUtil.mask(10, 64), is(0xffffffff_fffffc00L));
	}

	@Test
	public void testMaskOfBits() {
		assertThat(ByteUtil.maskOfBits((int[]) null), is(0L));
		assertThat(ByteUtil.maskOfBits((List<Integer>) null), is(0L));
		assertThat(ByteUtil.maskOfBits(63, 32, 31, 16, 15, 8, 7, 0), is(0x8000_0001_8001_8181L));
		assertThat(ByteUtil.maskOfBits(List.of(63, 32, 31, 16, 15, 8, 7, 0)),
			is(0x8000_0001_8001_8181L));
		assertThat(ByteUtil.maskOfBits(64), is(0L));
		assertThat(ByteUtil.maskOfBit(true, 63), is(0x8000_0000_0000_0000L));
		assertThat(ByteUtil.maskOfBit(false, 63), is(0L));
	}

	@Test
	public void testBit() {
		assertThat(ByteUtil.bit(0, 0), is(false));
		assertThat(ByteUtil.bit(0, 63), is(false));
		assertThat(ByteUtil.bit(Long.MIN_VALUE, 63), is(true));
		assertThat(ByteUtil.bit(Long.MAX_VALUE, 63), is(false));
		for (int i = 0; i < 63; i++)
			assertThat(ByteUtil.bit(Long.MAX_VALUE, i), is(true));
		assertThat(ByteUtil.bit(0x5a, 0), is(false));
		assertThat(ByteUtil.bit(0x5a, 1), is(true));
		assertThat(ByteUtil.bit(0x5a, 2), is(false));
		assertThat(ByteUtil.bit(0x5a, 3), is(true));
		assertThat(ByteUtil.bit(0x5a, 4), is(true));
		assertThat(ByteUtil.bit(0x5a, 5), is(false));
		assertThat(ByteUtil.bit(0x5a, 6), is(true));
		assertThat(ByteUtil.bit(0x5a, 7), is(false));
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
		assertThat(ByteUtil.writeMsb(0xabcd_ef01_2345_6789L, b), is(8));
		assertArray(b, 0xab, 0xcd, 0xef, 0x01, 0x23, 0x45, 0x67, 0x89);
		b = new byte[8];
		assertThat(ByteUtil.writeMsb(0xabcd_ef01_2345_6789L, b, 1, 3), is(4));
		assertArray(b, 0, 0x45, 0x67, 0x89, 0, 0, 0, 0);
	}

	@Test
	public void testWriteLsb() {
		byte[] b = new byte[8];
		assertThat(ByteUtil.writeLsb(0xabcd_ef01_2345_6789L, b), is(8));
		assertArray(b, 0x89, 0x67, 0x45, 0x23, 0x01, 0xef, 0xcd, 0xab);
		b = new byte[8];
		assertThat(ByteUtil.writeLsb(0xabcd_ef01_2345_6789L, b, 1, 3), is(4));
		assertArray(b, 0, 0x89, 0x67, 0x45, 0, 0, 0, 0);
	}

	@Test
	public void testFromMsb() {
		assertThat(ByteUtil.fromMsb(0xab, 0xcd, 0xef, 0x01, 0x23, 0x45, 0x67, 0x89),
			is(0xabcd_ef01_2345_6789L));
		assertThat(ByteUtil.fromMsb( //
			ArrayUtil.bytes(0xab, 0xcd, 0xef, 0x01, 0x23, 0x45, 0x67, 0x89), 1, 3), is(0xcdef01L));
	}

	@Test
	public void testFromLsb() {
		assertThat(ByteUtil.fromLsb(0xab, 0xcd, 0xef, 0x01, 0x23, 0x45, 0x67, 0x89),
			is(0x8967_4523_01ef_cdabL));
		assertThat(ByteUtil.fromLsb( //
			ArrayUtil.bytes(0xab, 0xcd, 0xef, 0x01, 0x23, 0x45, 0x67, 0x89), 1, 3), is(0x01efcdL));
	}

	@Test
	public void testShiftBits() {
		assertThat(ByteUtil.shiftBits((byte) 0, 1), is((byte) 0));
		assertThat(ByteUtil.shiftBits((byte) 0x96, 0), is((byte) 0x96));
		assertThat(ByteUtil.shiftBits((byte) 0x96, 1), is((byte) 0x4b));
		assertThat(ByteUtil.shiftBits((byte) 0x96, -1), is((byte) 0x2c));
		assertThat(ByteUtil.shiftBits((byte) 0x96, 7), is((byte) 0x01));
		assertThat(ByteUtil.shiftBits((byte) 0x96, -7), is((byte) 0));
		assertThat(ByteUtil.shiftBits((byte) 0x96, 8), is((byte) 0));
		assertThat(ByteUtil.shiftBits((byte) 0x96, -8), is((byte) 0));
		assertThat(ByteUtil.shiftBits((byte) 0, 1), is((byte) 0));
		assertThat(ByteUtil.shiftBits((short) 0, 15), is((short) 0));
		assertThat(ByteUtil.shiftBits(0, 31), is(0));
		assertThat(ByteUtil.shiftBits(0L, 63), is(0L));
	}

	@Test
	public void testShift() {
		assertThat(ByteUtil.shift((short) 0xabcd, 0), is((short) 0xabcd));
		assertThat(ByteUtil.shift((short) 0xabcd, 1), is((short) 0xab));
		assertThat(ByteUtil.shift((short) 0xabcd, -1), is((short) 0xcd00));
		assertThat(ByteUtil.shift((short) 0xabcd, 2), is((short) 0));
		assertThat(ByteUtil.shift((short) 0xabcd, -2), is((short) 0));
		assertThat(ByteUtil.shift(0xabcd_0123, 0), is(0xabcd_0123));
		assertThat(ByteUtil.shift(0xabcd_0123, 1), is(0xab_cd01));
		assertThat(ByteUtil.shift(0xabcd_0123, -1), is(0xcd01_2300));
		assertThat(ByteUtil.shift(0xabcd_0123, 3), is(0xab));
		assertThat(ByteUtil.shift(0xabcd_0123, -3), is(0x2300_0000));
		assertThat(ByteUtil.shift(0xabcd_0123, 4), is(0));
		assertThat(ByteUtil.shift(0xabcd_0123, -4), is(0));
		assertThat(ByteUtil.shift(0xabcd_0123_ef45_6789L, 0), is(0xabcd_0123_ef45_6789L));
		assertThat(ByteUtil.shift(0xabcd_0123_ef45_6789L, 1), is(0xab_cd01_23ef_4567L));
		assertThat(ByteUtil.shift(0xabcd_0123_ef45_6789L, -1), is(0xcd01_23ef_4567_8900L));
		assertThat(ByteUtil.shift(0xabcd_0123_ef45_6789L, 7), is(0xabL));
		assertThat(ByteUtil.shift(0xabcd_0123_ef45_6789L, -7), is(0x8900_0000_0000_0000L));
		assertThat(ByteUtil.shift(0xabcd_0123_ef45_6789L, 8), is(0L));
		assertThat(ByteUtil.shift(0xabcd_0123_ef45_6789L, -8), is(0L));
		assertThat(ByteUtil.shift(0xabcd_0123_ef45_6789L, 9), is(0L));
		assertThat(ByteUtil.shift(0xabcd_0123_ef45_6789L, -9), is(0L));
	}

	@Test
	public void testInvert() {
		assertThat(ByteUtil.invertByte(0x96), is((byte) 0x69));
		assertThat(ByteUtil.invertByte(0x69), is((byte) 0x96));
		assertThat(ByteUtil.invertShort(0x9669), is((short) 0x6996));
		assertThat(ByteUtil.invertShort(0x6996), is((short) 0x9669));
	}

	@Test
	public void testReverse() {
		assertThat(ByteUtil.reverseByte(0x96), is((byte) 0x69));
		assertThat(ByteUtil.reverseByte(0x69), is((byte) 0x96));
		assertThat(ByteUtil.reverseShort(0x9696), is((short) 0x6969));
		assertThat(ByteUtil.reverseShort(0x6969), is((short) 0x9696));
	}

}
