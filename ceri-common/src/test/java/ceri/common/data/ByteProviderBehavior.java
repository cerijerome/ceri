package ceri.common.data;

import static ceri.common.test.TestUtil.assertArray;
import static ceri.common.test.TestUtil.assertStream;
import static ceri.common.test.TestUtil.assertThrown;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import org.junit.Test;
import ceri.common.collection.ArrayUtil;
import ceri.common.data.ByteReceiverBehavior.Holder;

public class ByteProviderBehavior {
	private static final boolean msb = ByteUtil.BIG_ENDIAN;
	private static final ByteProvider bp = provider(0, -1, 2, -3, 4, -5, 6, -7, 8, -9);
	private static final byte[] ascii = "abcde".getBytes(StandardCharsets.ISO_8859_1);
	private static final byte[] utf8 = "abcde".getBytes(StandardCharsets.UTF_8);
	private static final byte[] defCset = "abcde".getBytes(Charset.defaultCharset());

	@Test
	public void shouldProvidePrimitiveValues() {
		assertThat(bp.getBool(0), is(false));
		assertThat(bp.getBool(1), is(true));
		assertThat(bp.getByte(1), is((byte) -1));
		assertThat(bp.getByte(2), is((byte) 2));
		assertThat(bp.getShort(1), is((short) (msb ? 0xff02 : 0x2ff)));
		assertThat(bp.getInt(2), is(msb ? 0x02fd04fb : 0xfb04fd02));
		assertThat(bp.getLong(1), is(msb ? 0xff02fd04fb06f908L : 0x8f906fb04fd02ffL));
		assertThat(bp.getFloat(2), is(Float.intBitsToFloat(msb ? 0x02fd04fb : 0xfb04fd02)));
		assertThat(bp.getDouble(1),
			is(Double.longBitsToDouble(msb ? 0xff02fd04fb06f908L : 0x8f906fb04fd02ffL)));
	}

	@Test
	public void shouldProvideUnsignedValues() {
		assertThat(bp.getUbyte(1), is((short) 0xff));
		assertThat(bp.getUbyte(2), is((short) 2));
		assertThat(bp.getUshort(1), is(msb ? 0xff02 : 0x2ff));
		assertThat(bp.getUint(1), is(msb ? 0xff02fd04L : 0x4fd02ffL));
	}

	@Test
	public void shouldProvideByteAlignedValues() {
		assertThat(bp.getShortMsb(1), is((short) 0xff02));
		assertThat(bp.getShortLsb(1), is((short) 0x2ff));
		assertThat(bp.getIntMsb(2), is(0x02fd04fb));
		assertThat(bp.getIntLsb(2), is(0xfb04fd02));
		assertThat(bp.getLongMsb(1), is(0xff02fd04fb06f908L));
		assertThat(bp.getLongLsb(1), is(0x8f906fb04fd02ffL));
		assertThat(bp.getFloatMsb(2), is(Float.intBitsToFloat(0x02fd04fb)));
		assertThat(bp.getFloatLsb(2), is(Float.intBitsToFloat(0xfb04fd02)));
		assertThat(bp.getDoubleMsb(1), is(Double.longBitsToDouble(0xff02fd04fb06f908L)));
		assertThat(bp.getDoubleLsb(1), is(Double.longBitsToDouble(0x8f906fb04fd02ffL)));
	}

	@Test
	public void shouldProvideByteAlignedUnsignedValues() {
		assertThat(bp.getUshortMsb(1), is(0xff02));
		assertThat(bp.getUshortLsb(1), is(0x2ff));
		assertThat(bp.getUintMsb(2), is(0x02fd04fbL));
		assertThat(bp.getUintLsb(2), is(0xfb04fd02L));
	}

	@Test
	public void shouldProvideDecodedStrings() {
		assertThat(provider(ascii).getAscii(0), is("abcde"));
		assertThat(provider(ascii).getAscii(2, 2), is("cd"));
		assertThat(provider(utf8).getUtf8(0), is("abcde"));
		assertThat(provider(utf8).getUtf8(2, 2), is("cd"));
		assertThat(provider(defCset).getString(0), is("abcde"));
	}

	@Test
	public void shouldProviderBytesAsAHexString() {
		ByteProvider bp = provider(0, -1, 2, -3);
		assertThat(bp.getHex(0, ":"), is("00:ff:02:fd"));
		assertThat(bp.getHex(2, ":"), is("02:fd"));
	}

	@Test
	public void shouldSliceProvidedByteRange() {
		assertThat(bp.slice(10).isEmpty(), is(true));
		assertArray(bp.slice(5, 0).copy(0));
		assertThat(bp.slice(9).isEmpty(), is(false));
		assertArray(bp.slice(6).copy(0), 6, -7, 8, -9);
		assertArray(bp.slice(6, -2).copy(0), 4, -5);
		assertTrue(bp.slice(0) == bp);
		assertArray(bp.slice(0, 2).copy(0), 0, -1);
	}

	@Test
	public void shouldProvideACopyOfBytes() {
		assertArray(bp.copy(5, 0));
		assertArray(bp.copy(5, 3), -5, 6, -7);
	}

	@Test
	public void shouldCopyToByteArray() {
		byte[] bytes = new byte[5];
		assertThat(bp.copyTo(1, bytes), is(6));
		assertArray(bytes, -1, 2, -3, 4, -5);
		assertThrown(() -> bp.copyTo(6, bytes));
		assertThrown(() -> bp.copyTo(-1, bytes));
		assertThrown(() -> bp.copyTo(1, bytes, 0, 6));
	}

	@Test
	public void shouldCopyToReceiver() {
		Holder h = ByteReceiverBehavior.holder(5);
		assertThat(bp.copyTo(1, h.receiver), is(6));
		assertArray(h.bytes, -1, 2, -3, 4, -5);
		assertThrown(() -> bp.copyTo(6, h.receiver));
		assertThrown(() -> bp.copyTo(-1, h.receiver));
		assertThrown(() -> bp.copyTo(1, h.receiver, 0, 6));
	}

	@Test
	public void shouldWriteToOutputStream() throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		assertThat(bp.writeTo(5, out), is(10));
		assertArray(out.toByteArray(), -5, 6, -7, 8, -9);
		out.reset();
		assertThat(ByteProvider.writeBufferTo(bp, 5, out, 3), is(8));
		assertArray(out.toByteArray(), -5, 6, -7);
	}

	@Test
	public void shouldStreamUnsignedBytes() {
		assertStream(bp.ustream(0), 0, 0xff, 2, 0xfd, 4, 0xfb, 6, 0xf9, 8, 0xf7);
		assertThrown(() -> bp.ustream(0, 11));
	}

	@Test
	public void shouldDetermineIfBytesAreEqual() {
		assertThat(bp.isEqualTo(5, -5, 6, -7, 8, -9), is(true));
		assertThat(bp.isEqualTo(5, -5, 6, -7, 8, 9), is(false));
		byte[] bytes = ArrayUtil.bytes(0, -1, 2, -3, 4);
		assertThat(bp.isEqualTo(0, bytes), is(true));
		assertThat(bp.isEqualTo(0, bytes, 0, 6), is(false));
		assertThat(bp.isEqualTo(9, -9, 0), is(false));
	}

	@Test
	public void shouldDetermineIfProvidedBytesAreEqual() {
		assertThat(bp.isEqualTo(0, bp), is(true));
		assertThat(bp.isEqualTo(5, bp, 5), is(true));
		assertThat(bp.isEqualTo(5, bp, 5, 3), is(true));
		assertThat(bp.isEqualTo(1, provider(-1, 2, -3)), is(true));
		assertThat(bp.isEqualTo(1, provider(1, 2, -3)), is(false));
		assertThat(bp.isEqualTo(0, provider(1, 2, 3), 0, 4), is(false));
		assertThat(bp.isEqualTo(9, provider(1, 2, 3)), is(false));
	}

	@Test
	public void shouldDetermineIndexOfBytes() {
		assertThat(bp.indexOf(0, -1, 2, -3), is(1));
		assertThat(bp.indexOf(0, -1, 2, 3), is(-1));
		assertThat(bp.indexOf(8, -1, 2, -3), is(-1));
		assertThat(bp.indexOf(0, ArrayUtil.bytes(-1, 2, -3), 0, 4), is(-1));
	}

	@Test
	public void shouldDetermineIndexOfProvidedBytes() {
		assertThat(bp.indexOf(0, provider(-1, 2, -3)), is(1));
		assertThat(bp.indexOf(0, provider(-1, 2, 3)), is(-1));
		assertThat(bp.indexOf(8, provider(-1, 2, -3)), is(-1));
		assertThat(bp.indexOf(0, provider(-1, 2, -3), 0, 4), is(-1));
	}

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
