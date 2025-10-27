package ceri.common.data;

import static java.nio.charset.StandardCharsets.UTF_8;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import org.junit.Test;
import ceri.common.array.ArrayUtil;
import ceri.common.data.ByteProvider.Reader;
import ceri.common.data.ByteReceiverBehavior.Holder;
import ceri.common.math.Maths;
import ceri.common.test.Assert;
import ceri.common.test.Captor;

public class ByteProviderBehavior {
	private static final boolean msb = ByteUtil.IS_BIG_ENDIAN;
	private static final ByteProvider bp = provider(0, -1, 2, -3, 4, -5, 6, -7, 8, -9);
	private static final byte[] ascii = "abcde".getBytes(StandardCharsets.ISO_8859_1);
	private static final byte[] utf8 = "abcde".getBytes(StandardCharsets.UTF_8);
	private static final byte[] defCset = "abcde".getBytes(Charset.defaultCharset());

	/* ByteProvider tests */

	@Test
	public void testOf() {
		Assert.array(ByteProvider.of(0xff, 0, 0x80, 0x7f).copy(0), 0xff, 0, 0x80, 0x7f);
		Assert.array(ByteProvider.of(Byte.MAX_VALUE, Byte.MIN_VALUE).copy(0), 0x7f, 0x80);
	}

	@Test
	public void testCopyOf() {
		byte[] bytes = { Byte.MAX_VALUE, Byte.MIN_VALUE };
		var of = ByteProvider.of(bytes);
		var copyOf = ByteProvider.copyOf(bytes);
		Arrays.fill(bytes, (byte) 0);
		Assert.array(of.copy(0), 0, 0);
		Assert.array(copyOf.copy(0), 0x7f, 0x80);
	}

	@Test
	public void testToHex() {
		Assert.equal(ByteProvider.toHex(bp), "[0x00,0xff,0x02,0xfd,0x04,0xfb,0x06,...](10)");
	}

	@Test
	public void testToString() {
		Assert.equal(ByteProvider.toString(bp), "[0,-1,2,-3,4,-5,6,...](10)");
		Assert.equal(ByteProvider.toString(Maths::ubyte, bp), "[0,255,2,253,4,251,6,...](10)");
	}

	@Test
	public void shouldProvideAnEmptyInstance() {
		Assert.equal(ByteProvider.empty().length(), 0);
		Assert.yes(ByteProvider.empty().isEmpty());
		Assert.thrown(() -> ByteProvider.empty().getByte(0));
	}

	@Test
	public void shouldIterateValues() {
		Captor.OfInt captor = Captor.ofInt();
		for (int i : ByteProvider.empty())
			captor.accept(i);
		captor.verifyInt();
		for (int i : provider(-1, 0, 1, Byte.MIN_VALUE, Byte.MAX_VALUE, 0xff))
			captor.accept(i);
		captor.verifyInt(0xff, 0, 1, 0x80, 0x7f, 0xff);
	}

	@Test
	public void shouldDetermineIfEmpty() {
		Assert.no(bp.isEmpty());
		Assert.yes(ByteProvider.empty().isEmpty());
	}

	@Test
	public void shouldProvidePrimitiveValues() {
		Assert.no(bp.getBool(0));
		Assert.yes(bp.getBool(1));
		Assert.equal(bp.getByte(1), (byte) -1);
		Assert.equal(bp.getByte(2), (byte) 2);
		Assert.equal(bp.getShort(1), (short) (msb ? 0xff02 : 0x2ff));
		Assert.equal(bp.getInt(2), msb ? 0x02fd04fb : 0xfb04fd02);
		Assert.equal(bp.getLong(1), msb ? 0xff02fd04fb06f908L : 0x8f906fb04fd02ffL);
		Assert.equal(bp.getFloat(2), Float.intBitsToFloat(msb ? 0x02fd04fb : 0xfb04fd02));
		Assert.equal(bp.getDouble(1),
			Double.longBitsToDouble(msb ? 0xff02fd04fb06f908L : 0x8f906fb04fd02ffL));
	}

	@Test
	public void shouldProvideUnsignedValues() {
		Assert.equal(bp.getUbyte(1), (short) 0xff);
		Assert.equal(bp.getUbyte(2), (short) 2);
		Assert.equal(bp.getUshort(1), msb ? 0xff02 : 0x2ff);
		Assert.equal(bp.getUint(1), msb ? 0xff02fd04L : 0x4fd02ffL);
	}

	@Test
	public void shouldProvideByteAlignedValues() {
		Assert.equal(bp.getShortMsb(1), (short) 0xff02);
		Assert.equal(bp.getShortLsb(1), (short) 0x2ff);
		Assert.equal(bp.getIntMsb(2), 0x02fd04fb);
		Assert.equal(bp.getIntLsb(2), 0xfb04fd02);
		Assert.equal(bp.getLongMsb(1), 0xff02fd04fb06f908L);
		Assert.equal(bp.getLongLsb(1), 0x8f906fb04fd02ffL);
		Assert.equal(bp.getFloatMsb(2), Float.intBitsToFloat(0x02fd04fb));
		Assert.equal(bp.getFloatLsb(2), Float.intBitsToFloat(0xfb04fd02));
		Assert.equal(bp.getDoubleMsb(1), Double.longBitsToDouble(0xff02fd04fb06f908L));
		Assert.equal(bp.getDoubleLsb(1), Double.longBitsToDouble(0x8f906fb04fd02ffL));
	}

	@Test
	public void shouldProvideByteAlignedUnsignedValues() {
		Assert.equal(bp.getUshortMsb(1), 0xff02);
		Assert.equal(bp.getUshortLsb(1), 0x2ff);
		Assert.equal(bp.getUintMsb(2), 0x02fd04fbL);
		Assert.equal(bp.getUintLsb(2), 0xfb04fd02L);
	}

	@Test
	public void shouldProvideDecodedStrings() {
		Assert.equal(provider(ascii).getAscii(0), "abcde");
		Assert.equal(provider(ascii).getAscii(2, 2), "cd");
		Assert.equal(provider(utf8).getUtf8(0), "abcde");
		Assert.equal(provider(utf8).getUtf8(2, 2), "cd");
		Assert.equal(provider(utf8).getString(0, UTF_8), "abcde");
		Assert.equal(provider(defCset).getString(0), "abcde");
	}

	@Test
	public void shouldSliceProvidedByteRange() {
		Assert.yes(bp.slice(10).isEmpty());
		Assert.array(bp.slice(5, 0).copy(0));
		Assert.equal(bp.slice(0), bp);
		Assert.equal(bp.slice(0, 10), bp);
		Assert.thrown(() -> bp.slice(1, 10));
		Assert.thrown(() -> bp.slice(0, 9));
	}

	@Test
	public void shouldProvideACopyOfBytes() {
		Assert.array(bp.copy(5, 0));
		Assert.array(bp.copy(5, 3), -5, 6, -7);
	}

	@Test
	public void shouldCopyToByteArray() {
		byte[] bytes = new byte[5];
		Assert.equal(bp.copyTo(1, bytes), 6);
		Assert.array(bytes, -1, 2, -3, 4, -5);
		Assert.thrown(() -> bp.copyTo(6, bytes));
		Assert.thrown(() -> bp.copyTo(-1, bytes));
		Assert.thrown(() -> bp.copyTo(1, bytes, 0, 6));
	}

	@Test
	public void shouldCopyToReceiver() {
		Holder h = Holder.of(5);
		Assert.equal(bp.copyTo(1, h.receiver), 6);
		Assert.array(h.bytes, -1, 2, -3, 4, -5);
		Assert.thrown(() -> bp.copyTo(6, h.receiver));
		Assert.thrown(() -> bp.copyTo(-1, h.receiver));
		Assert.thrown(() -> bp.copyTo(1, h.receiver, 0, 6));
	}

	@Test
	public void shouldWriteToOutputStream() throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		Assert.equal(bp.writeTo(5, out), 10);
		Assert.array(out.toByteArray(), -5, 6, -7, 8, -9);
		out.reset();
		Assert.equal(ByteProvider.writeBufferTo(bp, 5, out, 3), 8);
		Assert.array(out.toByteArray(), -5, 6, -7);
	}

	@Test
	public void shouldStreamUnsignedBytes() {
		Assert.stream(bp.ustream(0), 0, 0xff, 2, 0xfd, 4, 0xfb, 6, 0xf9, 8, 0xf7);
		Assert.thrown(() -> bp.ustream(0, 11));
	}

	@Test
	public void shouldDetermineIfBytesAreEqual() {
		Assert.yes(bp.isEqualTo(5, -5, 6, -7, 8, -9));
		Assert.no(bp.isEqualTo(5, -5, 6, -7, 8, 9));
		byte[] bytes = ArrayUtil.bytes.of(0, -1, 2, -3, 4);
		Assert.yes(bp.isEqualTo(0, bytes));
		Assert.no(bp.isEqualTo(0, bytes, 0, 6));
		Assert.no(bp.isEqualTo(9, -9, 0));
	}

	@Test
	public void shouldDetermineIfProvidedBytesAreEqual() {
		Assert.yes(bp.isEqualTo(0, bp));
		Assert.yes(bp.isEqualTo(5, bp, 5));
		Assert.yes(bp.isEqualTo(5, bp, 5, 3));
		Assert.yes(bp.isEqualTo(1, provider(-1, 2, -3)));
		Assert.no(bp.isEqualTo(1, provider(1, 2, -3)));
		Assert.no(bp.isEqualTo(0, provider(1, 2, 3), 0, 4));
		Assert.no(bp.isEqualTo(9, provider(1, 2, 3)));
	}

	@Test
	public void shouldDetermineIfContains() {
		Assert.equal(bp.contains(-1, 2, -3), true);
		Assert.equal(bp.contains(-1, 2, 3), false);
		Assert.equal(bp.contains(ArrayUtil.bytes.of(-1, 2, -3)), true);
		Assert.equal(bp.contains(ArrayUtil.bytes.of(-1, 2, 3)), false);
	}

	@Test
	public void shouldDetermineIndexOfBytes() {
		Assert.equal(bp.indexOf(0, -1, 2, -3), 1);
		Assert.equal(bp.indexOf(0, -1, 2, 3), -1);
		Assert.equal(bp.indexOf(8, -1, 2, -3), -1);
		Assert.equal(bp.indexOf(0, ArrayUtil.bytes.of(-1, 2, -3), 0, 4), -1);
	}

	@Test
	public void shouldDetermineIndexOfProvidedBytes() {
		Assert.equal(bp.indexOf(0, provider(-1, 2, -3)), 1);
		Assert.equal(bp.indexOf(0, provider(-1, 2, 3)), -1);
		Assert.equal(bp.indexOf(8, provider(-1, 2, -3)), -1);
		Assert.equal(bp.indexOf(0, provider(-1, 2, -3), 0, 4), -1);
	}

	@Test
	public void shouldDetermineLastIndexOfBytes() {
		ByteProvider bp = provider(0, -1, 2, -1, 0, 2, -1, 0);
		Assert.equal(bp.lastIndexOf(0, 2, -1), 5);
		Assert.equal(bp.lastIndexOf(0, 2, 1), -1);
		Assert.equal(bp.lastIndexOf(7, 0, -1), -1);
		Assert.equal(bp.lastIndexOf(0, ArrayUtil.bytes.of(2, -1, 0), 0, 4), -1);
	}

	@Test
	public void shouldDetermineLastIndexOfProviderBytes() {
		ByteProvider bp = provider(0, -1, 2, -1, 0, 2, -1, 0);
		Assert.equal(bp.lastIndexOf(0, provider(2, -1)), 5);
		Assert.equal(bp.lastIndexOf(0, provider(2, 1)), -1);
		Assert.equal(bp.lastIndexOf(7, provider(0, -1)), -1);
		Assert.equal(bp.lastIndexOf(0, provider(2, -1, 0), 0, 4), -1);
	}

	@Test
	public void shouldProvideReaderAccessToBytes() {
		Assert.array(bp.reader(5).readBytes(), -5, 6, -7, 8, -9);
		Assert.array(bp.reader(5, 0).readBytes());
		Assert.array(bp.reader(10, 0).readBytes());
		Assert.thrown(() -> bp.reader(10, 1));
		Assert.thrown(() -> bp.reader(11, 0));
	}

	@Test
	public void shouldCreateBuffer() {
		ByteProvider bp = provider(1, 2, 3, 4, 5);
		Assert.array(bp.toBuffer(1, 3).array(), 2, 3, 4);
		Assert.array(bp.toBuffer(3).array(), 4, 5);
	}

	/* ByteProvider.Reader<?> tests */

	@Test
	public void shouldReadByte() {
		Assert.equal(bp.reader(1).readByte(), (byte) -1);
		Assert.thrown(() -> bp.reader(1, 0).readByte());
	}

	@Test
	public void shouldReadEndian() {
		Assert.equal(bp.reader(6).readEndian(3, false), 0x08f906L);
		Assert.equal(bp.reader(6).readEndian(3, true), 0x06f908L);
	}

	@Test
	public void shouldReadStrings() {
		Assert.equal(provider(ascii).reader(2, 2).readAscii(), "cd");
		Assert.equal(provider(utf8).reader(1).readUtf8(), "bcde");
		Assert.equal(provider(defCset).reader(0).readString(), "abcde");
		Assert.equal(provider(utf8).reader(3).readString(UTF_8), "de");
	}

	@Test
	public void shouldReadIntoByteArray() {
		byte[] bytes = new byte[4];
		bp.reader(5).readInto(bytes);
		Assert.array(bytes, -5, 6, -7, 8);
	}

	@Test
	public void shouldReadIntoByteReceiver() {
		byte[] bytes = new byte[4];
		ByteReceiver br = ByteArray.Mutable.wrap(bytes);
		bp.reader(5).readInto(br);
		Assert.array(bytes, -5, 6, -7, 8);
	}

	@Test
	public void shouldTransferToOutputStream() throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		Assert.equal(bp.reader(4).transferTo(out), 6);
		Assert.array(out.toByteArray(), 4, -5, 6, -7, 8, -9);
	}

	@Test
	public void shouldStreamReaderUnsignedBytes() {
		Assert.stream(bp.reader(6).ustream(), 6, 0xf9, 8, 0xf7);
		Assert.thrown(() -> bp.reader(0).ustream(11));
	}

	@Test
	public void shouldReturnReaderByteProvider() {
		Assert.equal(bp.reader(0).provider(), bp);
		Assert.yes(bp.reader(5, 0).provider().isEmpty());
		Assert.thrown(() -> bp.reader(5).provider()); // slice() fails
	}

	@Test
	public void shouldSliceReader() {
		Reader<?> r0 = bp.reader(6);
		Reader<?> r1 = r0.slice();
		Reader<?> r2 = r0.slice(3);
		Assert.thrown(() -> r0.slice(5));
		Assert.thrown(() -> r0.slice(-2));
		Assert.array(r0.readBytes(), 6, -7, 8, -9);
		Assert.array(r1.readBytes(), 6, -7, 8, -9);
		Assert.array(r2.readBytes(), 6, -7, 8);
	}

	/* Support methods */

	public static ByteProvider provider(int... values) {
		return provider(ArrayUtil.bytes.of(values));
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
