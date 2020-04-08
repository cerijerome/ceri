package ceri.common.data;

import static ceri.common.test.TestUtil.assertArray;
import static ceri.common.test.TestUtil.assertByte;
import static ceri.common.test.TestUtil.assertStream;
import static ceri.common.test.TestUtil.assertThrown;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import org.junit.Test;
import ceri.common.collection.ArrayUtil;
import ceri.common.data.ByteArray.Mutable;

public class ByteReaderBehavior {
	private static final boolean msb = ByteUtil.BIG_ENDIAN;
	private static final byte[] ascii = "abcde".getBytes(StandardCharsets.ISO_8859_1);
	private static final byte[] utf8 = "abcde".getBytes(StandardCharsets.UTF_8);
	private static final byte[] defCset = "abcde".getBytes(Charset.defaultCharset());

	@Test
	public void shouldSkipBytes() {
		assertRemaining(reader(1, 2, 3).skip(0), 1, 2, 3);
		assertRemaining(reader(1, 2, 3, 4, 5).skip(3), 4, 5);
		assertThrown(() -> reader(1, 2, 3).skip(4));
	}

	@Test
	public void shouldReadPrimitiveValues() {
		assertThat(reader(0).readBool(), is(false));
		assertThat(reader(-1).readBool(), is(true));
		assertThat(reader(-1).readByte(), is((byte) -1));
		assertThat(reader(0x80, 0x7f).readShort(), is((short) (msb ? 0x807f : 0x7f80)));
		assertThat(reader(0x80, 0x7f, 0, 1).readInt(), is(msb ? 0x807f0001 : 0x1007f80));
		assertThat(reader(0x80, 0x7f, 0, 1, 0xff, 0, 0, 0).readLong(),
			is(msb ? 0x807f0001ff000000L : 0xff01007f80L));
		assertThat(reader(0x80, 0x7f, 0, 1).readFloat(),
			is(Float.intBitsToFloat(msb ? 0x807f0001 : 0x1007f80)));
		assertThat(reader(0x80, 0x7f, 0, 1, 0xff, 0, 0, 0).readDouble(),
			is(Double.longBitsToDouble(msb ? 0x807f0001ff000000L : 0xff01007f80L)));
	}

	@Test
	public void shouldReadUnsignedValues() {
		assertThat(reader(-1).readUbyte(), is((short) 0xff));
		assertThat(reader(0x80).readUbyte(), is((short) 0x80));
		assertThat(reader(-1, 2).readUshort(), is(msb ? 0xff02 : 0x2ff));
		assertThat(reader(-1, 2, -3, 4).readUint(), is(msb ? 0xff02fd04L : 0x4fd02ffL));
	}

	@Test
	public void shouldReadByteAlignedValues() {
		assertThat(reader(0x80, 0x7f).readShortMsb(), is((short) 0x807f));
		assertThat(reader(0x80, 0x7f).readShortLsb(), is((short) 0x7f80));
		assertThat(reader(0x80, 0x7f, 0, 1).readIntMsb(), is(0x807f0001));
		assertThat(reader(0x80, 0x7f, 0, 1).readIntLsb(), is(0x1007f80));
		assertThat(reader(0x80, 0x7f, 0, 1, 0xff, 0, 0, 0).readLongMsb(), is(0x807f0001ff000000L));
		assertThat(reader(0x80, 0x7f, 0, 1, 0xff, 0, 0, 0).readLongLsb(), is(0xff01007f80L));
		assertThat(reader(0x80, 0x7f, 0, 1).readFloatMsb(), is(Float.intBitsToFloat(0x807f0001)));
		assertThat(reader(0x80, 0x7f, 0, 1).readFloatLsb(), is(Float.intBitsToFloat(0x1007f80)));
		assertThat(reader(0x80, 0x7f, 0, 1, 0xff, 0, 0, 0).readDoubleMsb(),
			is(Double.longBitsToDouble(0x807f0001ff000000L)));
		assertThat(reader(0x80, 0x7f, 0, 1, 0xff, 0, 0, 0).readDoubleLsb(),
			is(Double.longBitsToDouble(0xff01007f80L)));
	}

	@Test
	public void shouldReadByteAlignedUnsignedValues() {
		assertThat(reader(-1, 2).readUshortMsb(), is(0xff02));
		assertThat(reader(-1, 2).readUshortLsb(), is(0x2ff));
		assertThat(reader(-1, 2, -3, 4).readUintMsb(), is(0xff02fd04L));
		assertThat(reader(-1, 2, -3, 4).readUintLsb(), is(0x4fd02ffL));
	}

	@Test
	public void shouldProvideDecodedStrings() {
		assertThat(reader(ascii).readAscii(5), is("abcde"));
		assertThat(reader(ascii).readAscii(2), is("ab"));
		assertThat(reader(utf8).readUtf8(5), is("abcde"));
		assertThat(reader(utf8).readUtf8(2), is("ab"));
		assertThat(reader(defCset).readString(5), is("abcde"));
	}

	@Test
	public void shouldProviderBytesAsAHexString() {
		assertThat(reader(0, -1, 2, -3).toHex(4, ":"), is("00:ff:02:fd"));
		assertThat(reader(0, -1, 2, -3).toHex(2, ":"), is("00:ff"));
	}

	@Test
	public void shouldReadBytes() {
		assertArray(reader(1, 2, 3).readBytes(0));
		assertArray(reader(1, 2, 3).readBytes(3), 1, 2, 3);
		assertThrown(() -> reader(1, 2, 3).readBytes(4));
	}

	@Test
	public void shouldReadIntoByteArray() {
		byte[] bytes = new byte[3];
		assertThat(reader(0, -1, 2, -3, 4).readInto(bytes), is(3));
		assertArray(bytes, 0, -1, 2);
		assertThrown(() -> reader(0, -1, 2, -3, 4).readInto(bytes, 1, 3));
		assertThrown(() -> reader(0, -1).readInto(bytes));
	}

	@Test
	public void shouldReadIntoByteReceiver() {
		byte[] bytes = new byte[3];
		assertThat(reader(0, -1, 2, -3, 4).readInto(Mutable.wrap(bytes)), is(3));
		assertArray(bytes, 0, -1, 2);
		assertThrown(() -> reader(0, -1, 2, -3, 4).readInto(Mutable.wrap(bytes), 1, 3));
		assertThrown(() -> reader(0, -1).readInto(Mutable.wrap(bytes)));
	}

	@Test
	public void shouldTransferToOutputStream() throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		assertThat(reader(0, -1, 2, -3, 4).transferTo(out, 3), is(3));
		assertArray(out.toByteArray(), 0, -1, 2);
		out.reset();
		assertThat(ByteReader.transferBufferTo(reader(0, -1, 2), out, 3), is(3));
		assertArray(out.toByteArray(), 0, -1, 2);
	}

	@Test
	public void shouldStreamUnsignedBytes() {
		assertStream(reader(0, -1, 2, -3, 4).ustream(5), 0, 0xff, 2, 0xfd, 4);
		assertThrown(() -> reader(0, -1, 2).ustream(5).toArray());
	}

	private static void assertRemaining(ByteReader reader, int... bytes) {
		for (int b : bytes)
			assertByte(reader.readByte(), b);
		assertThrown(() -> reader.readByte());
	}

	private static ByteReader reader(int... bytes) {
		return reader(ArrayUtil.bytes(bytes));
	}

	private static ByteReader reader(byte[] bytes) {
		return reader(bytes, 0, bytes.length);
	}

	private static ByteReader reader(byte[] bytes, int offset, int length) {
		return new ByteReader() {
			private int pos = 0;

			@Override
			public byte readByte() {
				ArrayUtil.validateIndex(length, pos);
				return bytes[offset + pos++];
			}
		};
	}
}
