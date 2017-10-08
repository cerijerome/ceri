package ceri.common.data;

import static ceri.common.data.ByteUtil.bytes;
import static ceri.common.test.TestUtil.assertArray;
import static ceri.common.test.TestUtil.assertException;
import static ceri.common.test.TestUtil.assertPrivateConstructor;
import static ceri.common.test.TestUtil.exerciseEnum;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doThrow;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.stream.IntStream;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import ceri.common.collection.ImmutableByteArray;

public class ByteUtilTest {
	@Mock
	ByteArrayOutputStream badByteArrayOutputStream;
	@Mock
	ImmutableByteArray badImmutableByteArray;

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
		ImmutableByteArray b = ImmutableByteArray.wrap(-1, 0, 127, 128);
		assertThat(ByteUtil.toHex(b, ":"), is("ff:00:7f:80"));
	}

	@Test
	public void testToByteArray() {
		assertArray(ByteUtil.toByteArray(IntStream.of(-1, 0, 127, 128)), -1, 0, 127, 128);
	}

	@Test
	public void testWriteTo() {
		ByteArrayOutputStream b = new ByteArrayOutputStream();
		ImmutableByteArray im = ImmutableByteArray.wrap(-1, 0, 128);
		ByteUtil.writeTo(b, -1, 2);
		ByteUtil.writeTo(b, new byte[] { 3, 4, 5 }, 1);
		ByteUtil.writeTo(b, im);
		ByteUtil.writeTo(b, im, 2);
		ByteUtil.writeTo(b, im, 0, 1);
		assertArray(b.toByteArray(), -1, 2, 4, 5, -1, 0, 128, 128, -1);
	}

	@Test
	public void testWriteToWithExceptions() throws IOException {
		ByteArrayOutputStream b = badByteArrayOutputStream;
		ImmutableByteArray im = badImmutableByteArray;
		doThrow(new IOException()).when(badByteArrayOutputStream).write(any());
		doThrow(new IOException()).when(badImmutableByteArray).writeTo(any());
		doThrow(new IOException()).when(badImmutableByteArray).writeTo(any(), anyInt());
		doThrow(new IOException()).when(badImmutableByteArray).writeTo(any(), anyInt(), anyInt());
		assertException(() -> ByteUtil.writeTo(b, -1, 2));
		assertException(() -> ByteUtil.writeTo(b, im));
		assertException(() -> ByteUtil.writeTo(b, im, 1));
		assertException(() -> ByteUtil.writeTo(b, im, 0, 2));
	}

	@Test
	public void testToAscii() {
		assertArray(ByteUtil.toAscii("\0\t\r\ntest"), 0, '\t', '\r', '\n', 't', 'e', 's', 't');
	}

	@Test
	public void testFromAscii() {
		assertThat(ByteUtil.fromAscii(0, '\t', '\r', '\n', 't', 'e', 's', 't'), is("\0\t\r\ntest"));
		assertThat(ByteUtil.fromAscii( //
			ImmutableByteArray.wrap(0, '\t', '\r', '\n', 't', 'e', 's', 't')), is("\0\t\r\ntest"));
	}

	@Test
	public void testMask() {
		assertThat(ByteUtil.mask(63, 32, 31, 16, 15, 8, 7, 0), is(0x8000_0001_8001_8181L));
		assertThat(ByteUtil.mask(64), is(0L));
		assertThat(ByteUtil.mask(true, 63), is(0x8000_0000_0000_0000L));
		assertThat(ByteUtil.mask(false, 63), is(0L));

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
	public void testToBigEndian() {
		assertArray(ByteUtil.toBigEndian(Short.MIN_VALUE), 0x80, 0);
		assertArray(ByteUtil.toBigEndian(Integer.MIN_VALUE), 0x80, 0, 0, 0);
		assertArray(ByteUtil.toBigEndian(Long.MIN_VALUE), 0x80, 0, 0, 0, 0, 0, 0, 0);
	}

	@Test
	public void testToLittleEndian() {
		assertArray(ByteUtil.toLittleEndian(Short.MIN_VALUE), 0, 0x80);
		assertArray(ByteUtil.toLittleEndian(Integer.MIN_VALUE), 0, 0, 0, 0x80);
		assertArray(ByteUtil.toLittleEndian(Long.MIN_VALUE), 0, 0, 0, 0, 0, 0, 0, 0x80);
	}

	@Test
	public void testWriteBigEndian() {
		byte[] b = new byte[8];
		assertThat(ByteUtil.writeBigEndian(0xabcd_ef01_2345_6789L, b), is(8));
		assertArray(b, 0xab, 0xcd, 0xef, 0x01, 0x23, 0x45, 0x67, 0x89);
		b = new byte[8];
		assertThat(ByteUtil.writeBigEndian(0xabcd_ef01_2345_6789L, b, 1, 3), is(4));
		assertArray(b, 0, 0x45, 0x67, 0x89, 0, 0, 0, 0);
	}

	@Test
	public void testWriteLittleEndian() {
		byte[] b = new byte[8];
		assertThat(ByteUtil.writeLittleEndian(0xabcd_ef01_2345_6789L, b), is(8));
		assertArray(b, 0x89, 0x67, 0x45, 0x23, 0x01, 0xef, 0xcd, 0xab);
		b = new byte[8];
		assertThat(ByteUtil.writeLittleEndian(0xabcd_ef01_2345_6789L, b, 1, 3), is(4));
		assertArray(b, 0, 0x89, 0x67, 0x45, 0, 0, 0, 0);
	}

	@Test
	public void testFromBigEndian() {
		assertThat(ByteUtil.fromBigEndian(0xab, 0xcd, 0xef, 0x01, 0x23, 0x45, 0x67, 0x89),
			is(0xabcd_ef01_2345_6789L));
		assertThat(ByteUtil.fromBigEndian(ImmutableByteArray.wrap( //
			0xab, 0xcd, 0xef, 0x01, 0x23, 0x45, 0x67, 0x89)), is(0xabcd_ef01_2345_6789L));
		assertThat(ByteUtil.fromBigEndian( //
			bytes(0xab, 0xcd, 0xef, 0x01, 0x23, 0x45, 0x67, 0x89), 1, 3), is(0xcdef01L));
	}

	@Test
	public void testFromLittleEndian() {
		assertThat(ByteUtil.fromLittleEndian(0xab, 0xcd, 0xef, 0x01, 0x23, 0x45, 0x67, 0x89),
			is(0x8967_4523_01ef_cdabL));
		assertThat(ByteUtil.fromLittleEndian(ImmutableByteArray.wrap( //
			0xab, 0xcd, 0xef, 0x01, 0x23, 0x45, 0x67, 0x89)), is(0x8967_4523_01ef_cdabL));
		assertThat(ByteUtil.fromLittleEndian( //
			bytes(0xab, 0xcd, 0xef, 0x01, 0x23, 0x45, 0x67, 0x89), 1, 3), is(0x01efcdL));
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
		assertThat(ByteUtil.invert((byte) 0x96), is((byte) 0x69));
		assertThat(ByteUtil.invert((byte) 0x69), is((byte) 0x96));
		assertThat(ByteUtil.invert((short) 0x9669), is((short) 0x6996));
		assertThat(ByteUtil.invert((short) 0x6996), is((short) 0x9669));
	}

	@Test
	public void testReverse() {
		assertThat(ByteUtil.reverse((byte) 0x96), is((byte) 0x69));
		assertThat(ByteUtil.reverse((byte) 0x69), is((byte) 0x96));
		assertThat(ByteUtil.reverse((short) 0x9696), is((short) 0x6969));
		assertThat(ByteUtil.reverse((short) 0x6969), is((short) 0x9696));
	}

	@Test
	public void testFill() {
		byte[] b = bytes(-1, 1, 0, 127, 128);
		ByteUtil.fill(b, 0x8f, 2, 2);
		assertArray(b, -1, 1, 0x8f, 0x8f, 128);
		assertException(() -> ByteUtil.fill(b, 0x8f, 2, 4));
	}

	@Test
	public void testPad() {
		exerciseEnum(ByteUtil.Align.class);
		byte[] b = bytes(-1, 1, 0, 127, 128);
		assertArray(ByteUtil.padL(b, 0), -1, 1, 0, 127, 128);
		assertArray(ByteUtil.padL(b, 4), -1, 1, 0, 127, 128);
		assertArray(ByteUtil.padL(b, 5), -1, 1, 0, 127, 128);
		assertArray(ByteUtil.padL(b, 6), 0, -1, 1, 0, 127, 128);
		assertArray(ByteUtil.padL(b, 8, 0xee), 0xee, 0xee, 0xee, -1, 1, 0, 127, 128);
		assertArray(ByteUtil.padR(b, 0), -1, 1, 0, 127, 128);
		assertArray(ByteUtil.padR(b, 4), -1, 1, 0, 127, 128);
		assertArray(ByteUtil.padR(b, 5), -1, 1, 0, 127, 128);
		assertArray(ByteUtil.padR(b, 6), -1, 1, 0, 127, 128, 0);
		assertArray(ByteUtil.padR(b, 8, 0xee), -1, 1, 0, 127, 128, 0xee, 0xee, 0xee);
	}

}
