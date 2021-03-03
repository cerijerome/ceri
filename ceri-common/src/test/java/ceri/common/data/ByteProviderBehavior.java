package ceri.common.data;

import static ceri.common.test.AssertUtil.assertArray;
import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertFalse;
import static ceri.common.test.AssertUtil.assertStream;
import static ceri.common.test.AssertUtil.assertThrown;
import static ceri.common.test.AssertUtil.assertTrue;
import static java.nio.charset.StandardCharsets.UTF_8;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import org.junit.Test;
import ceri.common.collection.ArrayUtil;
import ceri.common.data.ByteProvider.Reader;
import ceri.common.data.ByteReceiverBehavior.Holder;
import ceri.common.test.Captor;

public class ByteProviderBehavior {
	private static final boolean msb = ByteUtil.BIG_ENDIAN;
	private static final ByteProvider bp = provider(0, -1, 2, -3, 4, -5, 6, -7, 8, -9);
	private static final byte[] ascii = "abcde".getBytes(StandardCharsets.ISO_8859_1);
	private static final byte[] utf8 = "abcde".getBytes(StandardCharsets.UTF_8);
	private static final byte[] defCset = "abcde".getBytes(Charset.defaultCharset());

	/* ByteProvider tests */

	@Test
	public void testOf() {
		assertArray(ByteProvider.of(0xff, 0, 0x80, 0x7f).copy(0), 0xff, 0, 0x80, 0x7f);
		assertArray(ByteProvider.of(Byte.MAX_VALUE, Byte.MIN_VALUE).copy(0), 0x7f, 0x80);
	}

	@Test
	public void testToHex() {
		assertEquals(ByteProvider.toHex(bp),
			"[0x0, 0xff, 0x2, 0xfd, 0x4, 0xfb, 0x6, 0xf9, 0x8, 0xf7](10)");
		assertEquals(ByteProvider.toHex(bp, 3), "[0x0, 0xff, ...](10)");
	}

	@Test
	public void testToString() {
		assertEquals(ByteProvider.toString(bp), "[0, -1, 2, -3, 4, -5, 6, -7, 8, -9](10)");
		assertEquals(ByteProvider.toString(bp, 3), "[0, -1, ...](10)");
	}

	@Test
	public void shouldProvideAnEmptyInstance() {
		assertEquals(ByteProvider.empty().length(), 0);
		assertTrue(ByteProvider.empty().isEmpty());
		assertThrown(() -> ByteProvider.empty().getByte(0));
	}

	@Test
	public void shouldIterateValues() {
		Captor.Int captor = Captor.ofInt();
		for (int i : ByteProvider.empty())
			captor.accept(i);
		captor.verifyInt();
		for (int i : provider(-1, 0, 1, Byte.MIN_VALUE, Byte.MAX_VALUE, 0xff))
			captor.accept(i);
		captor.verifyInt(0xff, 0, 1, 0x80, 0x7f, 0xff);
	}

	@Test
	public void shouldDetermineIfEmpty() {
		assertFalse(bp.isEmpty());
		assertTrue(ByteProvider.empty().isEmpty());
	}

	@Test
	public void shouldProvidePrimitiveValues() {
		assertFalse(bp.getBool(0));
		assertTrue(bp.getBool(1));
		assertEquals(bp.getByte(1), (byte) -1);
		assertEquals(bp.getByte(2), (byte) 2);
		assertEquals(bp.getShort(1), (short) (msb ? 0xff02 : 0x2ff));
		assertEquals(bp.getInt(2), msb ? 0x02fd04fb : 0xfb04fd02);
		assertEquals(bp.getLong(1), msb ? 0xff02fd04fb06f908L : 0x8f906fb04fd02ffL);
		assertEquals(bp.getFloat(2), Float.intBitsToFloat(msb ? 0x02fd04fb : 0xfb04fd02));
		assertEquals(bp.getDouble(1),
			Double.longBitsToDouble(msb ? 0xff02fd04fb06f908L : 0x8f906fb04fd02ffL));
	}

	@Test
	public void shouldProvideUnsignedValues() {
		assertEquals(bp.getUbyte(1), (short) 0xff);
		assertEquals(bp.getUbyte(2), (short) 2);
		assertEquals(bp.getUshort(1), msb ? 0xff02 : 0x2ff);
		assertEquals(bp.getUint(1), msb ? 0xff02fd04L : 0x4fd02ffL);
	}

	@Test
	public void shouldProvideByteAlignedValues() {
		assertEquals(bp.getShortMsb(1), (short) 0xff02);
		assertEquals(bp.getShortLsb(1), (short) 0x2ff);
		assertEquals(bp.getIntMsb(2), 0x02fd04fb);
		assertEquals(bp.getIntLsb(2), 0xfb04fd02);
		assertEquals(bp.getLongMsb(1), 0xff02fd04fb06f908L);
		assertEquals(bp.getLongLsb(1), 0x8f906fb04fd02ffL);
		assertEquals(bp.getFloatMsb(2), Float.intBitsToFloat(0x02fd04fb));
		assertEquals(bp.getFloatLsb(2), Float.intBitsToFloat(0xfb04fd02));
		assertEquals(bp.getDoubleMsb(1), Double.longBitsToDouble(0xff02fd04fb06f908L));
		assertEquals(bp.getDoubleLsb(1), Double.longBitsToDouble(0x8f906fb04fd02ffL));
	}

	@Test
	public void shouldProvideByteAlignedUnsignedValues() {
		assertEquals(bp.getUshortMsb(1), 0xff02);
		assertEquals(bp.getUshortLsb(1), 0x2ff);
		assertEquals(bp.getUintMsb(2), 0x02fd04fbL);
		assertEquals(bp.getUintLsb(2), 0xfb04fd02L);
	}

	@Test
	public void shouldProvideDecodedStrings() {
		assertEquals(provider(ascii).getAscii(0), "abcde");
		assertEquals(provider(ascii).getAscii(2, 2), "cd");
		assertEquals(provider(utf8).getUtf8(0), "abcde");
		assertEquals(provider(utf8).getUtf8(2, 2), "cd");
		assertEquals(provider(utf8).getString(0, UTF_8), "abcde");
		assertEquals(provider(defCset).getString(0), "abcde");
	}

	@Test
	public void shouldSliceProvidedByteRange() {
		assertTrue(bp.slice(10).isEmpty());
		assertArray(bp.slice(5, 0).copy(0));
		assertEquals(bp.slice(0), bp);
		assertEquals(bp.slice(0, 10), bp);
		assertThrown(() -> bp.slice(1, 10));
		assertThrown(() -> bp.slice(0, 9));
	}

	@Test
	public void shouldProvideACopyOfBytes() {
		assertArray(bp.copy(5, 0));
		assertArray(bp.copy(5, 3), -5, 6, -7);
	}

	@Test
	public void shouldCopyToByteArray() {
		byte[] bytes = new byte[5];
		assertEquals(bp.copyTo(1, bytes), 6);
		assertArray(bytes, -1, 2, -3, 4, -5);
		assertThrown(() -> bp.copyTo(6, bytes));
		assertThrown(() -> bp.copyTo(-1, bytes));
		assertThrown(() -> bp.copyTo(1, bytes, 0, 6));
	}

	@Test
	public void shouldCopyToReceiver() {
		Holder h = Holder.of(5);
		assertEquals(bp.copyTo(1, h.receiver), 6);
		assertArray(h.bytes, -1, 2, -3, 4, -5);
		assertThrown(() -> bp.copyTo(6, h.receiver));
		assertThrown(() -> bp.copyTo(-1, h.receiver));
		assertThrown(() -> bp.copyTo(1, h.receiver, 0, 6));
	}

	@Test
	public void shouldWriteToOutputStream() throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		assertEquals(bp.writeTo(5, out), 10);
		assertArray(out.toByteArray(), -5, 6, -7, 8, -9);
		out.reset();
		assertEquals(ByteProvider.writeBufferTo(bp, 5, out, 3), 8);
		assertArray(out.toByteArray(), -5, 6, -7);
	}

	@Test
	public void shouldStreamUnsignedBytes() {
		assertStream(bp.ustream(0), 0, 0xff, 2, 0xfd, 4, 0xfb, 6, 0xf9, 8, 0xf7);
		assertThrown(() -> bp.ustream(0, 11));
	}

	@Test
	public void shouldDetermineIfBytesAreEqual() {
		assertTrue(bp.isEqualTo(5, -5, 6, -7, 8, -9));
		assertFalse(bp.isEqualTo(5, -5, 6, -7, 8, 9));
		byte[] bytes = ArrayUtil.bytes(0, -1, 2, -3, 4);
		assertTrue(bp.isEqualTo(0, bytes));
		assertFalse(bp.isEqualTo(0, bytes, 0, 6));
		assertFalse(bp.isEqualTo(9, -9, 0));
	}

	@Test
	public void shouldDetermineIfProvidedBytesAreEqual() {
		assertTrue(bp.isEqualTo(0, bp));
		assertTrue(bp.isEqualTo(5, bp, 5));
		assertTrue(bp.isEqualTo(5, bp, 5, 3));
		assertTrue(bp.isEqualTo(1, provider(-1, 2, -3)));
		assertFalse(bp.isEqualTo(1, provider(1, 2, -3)));
		assertFalse(bp.isEqualTo(0, provider(1, 2, 3), 0, 4));
		assertFalse(bp.isEqualTo(9, provider(1, 2, 3)));
	}

	@Test
	public void shouldDetermineIndexOfBytes() {
		assertEquals(bp.indexOf(0, -1, 2, -3), 1);
		assertEquals(bp.indexOf(0, -1, 2, 3), -1);
		assertEquals(bp.indexOf(8, -1, 2, -3), -1);
		assertEquals(bp.indexOf(0, ArrayUtil.bytes(-1, 2, -3), 0, 4), -1);
	}

	@Test
	public void shouldDetermineIndexOfProvidedBytes() {
		assertEquals(bp.indexOf(0, provider(-1, 2, -3)), 1);
		assertEquals(bp.indexOf(0, provider(-1, 2, 3)), -1);
		assertEquals(bp.indexOf(8, provider(-1, 2, -3)), -1);
		assertEquals(bp.indexOf(0, provider(-1, 2, -3), 0, 4), -1);
	}

	@Test
	public void shouldDetermineLastIndexOfBytes() {
		ByteProvider bp = provider(0, -1, 2, -1, 0, 2, -1, 0);
		assertEquals(bp.lastIndexOf(0, 2, -1), 5);
		assertEquals(bp.lastIndexOf(0, 2, 1), -1);
		assertEquals(bp.lastIndexOf(7, 0, -1), -1);
		assertEquals(bp.lastIndexOf(0, ArrayUtil.bytes(2, -1, 0), 0, 4), -1);
	}

	@Test
	public void shouldDetermineLastIndexOfProviderBytes() {
		ByteProvider bp = provider(0, -1, 2, -1, 0, 2, -1, 0);
		assertEquals(bp.lastIndexOf(0, provider(2, -1)), 5);
		assertEquals(bp.lastIndexOf(0, provider(2, 1)), -1);
		assertEquals(bp.lastIndexOf(7, provider(0, -1)), -1);
		assertEquals(bp.lastIndexOf(0, provider(2, -1, 0), 0, 4), -1);
	}

	@Test
	public void shouldProvideReaderAccessToBytes() {
		assertArray(bp.reader(5).readBytes(), -5, 6, -7, 8, -9);
		assertArray(bp.reader(5, 0).readBytes());
		assertArray(bp.reader(10, 0).readBytes());
		assertThrown(() -> bp.reader(10, 1));
		assertThrown(() -> bp.reader(11, 0));
	}

	/* ByteProvider.Reader tests */

	@Test
	public void shouldReadByte() {
		assertEquals(bp.reader(1).readByte(), (byte) -1);
		assertThrown(() -> bp.reader(1, 0).readByte());
	}

	@Test
	public void shouldReadEndian() {
		assertEquals(bp.reader(6).readEndian(3, false), 0x08f906L);
		assertEquals(bp.reader(6).readEndian(3, true), 0x06f908L);
	}

	@Test
	public void shouldReadStrings() {
		assertEquals(provider(ascii).reader(2, 2).readAscii(), "cd");
		assertEquals(provider(utf8).reader(1).readUtf8(), "bcde");
		assertEquals(provider(defCset).reader(0).readString(), "abcde");
		assertEquals(provider(utf8).reader(3).readString(UTF_8), "de");
	}

	@Test
	public void shouldReadIntoByteArray() {
		byte[] bytes = new byte[4];
		bp.reader(5).readInto(bytes);
		assertArray(bytes, -5, 6, -7, 8);
	}

	@Test
	public void shouldReadIntoByteReceiver() {
		byte[] bytes = new byte[4];
		ByteReceiver br = ByteArray.Mutable.wrap(bytes);
		bp.reader(5).readInto(br);
		assertArray(bytes, -5, 6, -7, 8);
	}

	@Test
	public void shouldTransferToOutputStream() throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		assertEquals(bp.reader(4).transferTo(out), 6);
		assertArray(out.toByteArray(), 4, -5, 6, -7, 8, -9);
	}

	@Test
	public void shouldStreamReaderUnsignedBytes() {
		assertStream(bp.reader(6).ustream(), 6, 0xf9, 8, 0xf7);
		assertThrown(() -> bp.reader(0).ustream(11));
	}

	@Test
	public void shouldReturnReaderByteProvider() {
		assertEquals(bp.reader(0).provider(), bp);
		assertTrue(bp.reader(5, 0).provider().isEmpty());
		assertThrown(() -> bp.reader(5).provider()); // slice() fails
	}

	@Test
	public void shouldSliceReader() {
		Reader r0 = bp.reader(6);
		Reader r1 = r0.slice();
		Reader r2 = r0.slice(3);
		assertThrown(() -> r0.slice(5));
		assertThrown(() -> r0.slice(-2));
		assertArray(r0.readBytes(), 6, -7, 8, -9);
		assertArray(r1.readBytes(), 6, -7, 8, -9);
		assertArray(r2.readBytes(), 6, -7, 8);
	}

	/* Support methods */

	public static ByteProvider provider(int... values) {
		return provider(ArrayUtil.bytes(values));
	}

	public static ByteProvider provider(byte[] bytes) {
		return new ByteProvider() {
			@Override
			public byte getByte(int index) {
				return bytes[index];
			}

			@Override
			public int length() {
				return bytes.length;
			}
		};
	}
}
