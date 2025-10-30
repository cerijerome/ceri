package ceri.common.data;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import org.junit.Test;
import ceri.common.array.Array;
import ceri.common.data.ByteArray.Mutable;
import ceri.common.test.Assert;
import ceri.common.util.Validate;

public class ByteReaderBehavior {
	private static final boolean msb = Bytes.IS_BIG_ENDIAN;
	private static final byte[] ascii = "abcde".getBytes(StandardCharsets.ISO_8859_1);
	private static final byte[] utf8 = "abcde".getBytes(StandardCharsets.UTF_8);
	private static final byte[] defCset = "abcde".getBytes(Charset.defaultCharset());

	@Test
	public void shouldSkipBytes() {
		assertRemaining(reader(1, 2, 3).skip(0), 1, 2, 3);
		assertRemaining(reader(1, 2, 3, 4, 5).skip(3), 4, 5);
		Assert.thrown(() -> reader(1, 2, 3).skip(4));
	}

	@Test
	public void shouldReadPrimitiveValues() {
		Assert.no(reader(0).readBool());
		Assert.yes(reader(-1).readBool());
		Assert.equal(reader(-1).readByte(), (byte) -1);
		Assert.equal(reader(0x80, 0x7f).readShort(), (short) (msb ? 0x807f : 0x7f80));
		Assert.equal(reader(0x80, 0x7f, 0, 1).readInt(), msb ? 0x807f0001 : 0x1007f80);
		Assert.equal(reader(0x80, 0x7f, 0, 1, 0xff, 0, 0, 0).readLong(),
			msb ? 0x807f0001ff000000L : 0xff01007f80L);
		Assert.equal(reader(0x80, 0x7f, 0, 1).readFloat(),
			Float.intBitsToFloat(msb ? 0x807f0001 : 0x1007f80));
		Assert.equal(reader(0x80, 0x7f, 0, 1, 0xff, 0, 0, 0).readDouble(),
			Double.longBitsToDouble(msb ? 0x807f0001ff000000L : 0xff01007f80L));
	}

	@Test
	public void shouldReadUnsignedValues() {
		Assert.equal(reader(-1).readUbyte(), (short) 0xff);
		Assert.equal(reader(0x80).readUbyte(), (short) 0x80);
		Assert.equal(reader(-1, 2).readUshort(), msb ? 0xff02 : 0x2ff);
		Assert.equal(reader(-1, 2, -3, 4).readUint(), msb ? 0xff02fd04L : 0x4fd02ffL);
	}

	@Test
	public void shouldReadByteAlignedValues() {
		Assert.equal(reader(0x80, 0x7f).readShortMsb(), (short) 0x807f);
		Assert.equal(reader(0x80, 0x7f).readShortLsb(), (short) 0x7f80);
		Assert.equal(reader(0x80, 0x7f, 0, 1).readIntMsb(), 0x807f0001);
		Assert.equal(reader(0x80, 0x7f, 0, 1).readIntLsb(), 0x1007f80);
		Assert.equal(reader(0x80, 0x7f, 0, 1, 0xff, 0, 0, 0).readLongMsb(), 0x807f0001ff000000L);
		Assert.equal(reader(0x80, 0x7f, 0, 1, 0xff, 0, 0, 0).readLongLsb(), 0xff01007f80L);
		Assert.equal(reader(0x80, 0x7f, 0, 1).readFloatMsb(), Float.intBitsToFloat(0x807f0001));
		Assert.equal(reader(0x80, 0x7f, 0, 1).readFloatLsb(), Float.intBitsToFloat(0x1007f80));
		Assert.equal(reader(0x80, 0x7f, 0, 1, 0xff, 0, 0, 0).readDoubleMsb(),
			Double.longBitsToDouble(0x807f0001ff000000L));
		Assert.equal(reader(0x80, 0x7f, 0, 1, 0xff, 0, 0, 0).readDoubleLsb(),
			Double.longBitsToDouble(0xff01007f80L));
	}

	@Test
	public void shouldReadByteAlignedUnsignedValues() {
		Assert.equal(reader(-1, 2).readUshortMsb(), 0xff02);
		Assert.equal(reader(-1, 2).readUshortLsb(), 0x2ff);
		Assert.equal(reader(-1, 2, -3, 4).readUintMsb(), 0xff02fd04L);
		Assert.equal(reader(-1, 2, -3, 4).readUintLsb(), 0x4fd02ffL);
	}

	@Test
	public void shouldProvideDecodedStrings() {
		Assert.equal(reader(ascii).readAscii(5), "abcde");
		Assert.equal(reader(ascii).readAscii(2), "ab");
		Assert.equal(reader(utf8).readUtf8(5), "abcde");
		Assert.equal(reader(utf8).readUtf8(2), "ab");
		Assert.equal(reader(defCset).readString(5), "abcde");
	}

	@Test
	public void shouldReadBytes() {
		Assert.array(reader(1, 2, 3).readBytes(0));
		Assert.array(reader(1, 2, 3).readBytes(3), 1, 2, 3);
		Assert.thrown(() -> reader(1, 2, 3).readBytes(4));
	}

	@Test
	public void shouldReadIntoByteArray() {
		byte[] bytes = new byte[3];
		Assert.equal(reader(0, -1, 2, -3, 4).readInto(bytes), 3);
		Assert.array(bytes, 0, -1, 2);
		Assert.thrown(() -> reader(0, -1, 2, -3, 4).readInto(bytes, 1, 3));
		Assert.thrown(() -> reader(0, -1).readInto(bytes));
	}

	@Test
	public void shouldReadIntoByteReceiver() {
		byte[] bytes = new byte[3];
		Assert.equal(reader(0, -1, 2, -3, 4).readInto(Mutable.wrap(bytes)), 3);
		Assert.array(bytes, 0, -1, 2);
		Assert.thrown(() -> reader(0, -1, 2, -3, 4).readInto(Mutable.wrap(bytes), 1, 3));
		Assert.thrown(() -> reader(0, -1).readInto(Mutable.wrap(bytes)));
	}

	@Test
	public void shouldTransferToOutputStream() throws IOException {
		var out = new ByteArrayOutputStream();
		Assert.equal(reader(0, -1, 2, -3, 4).transferTo(out, 3), 3);
		Assert.array(out.toByteArray(), 0, -1, 2);
		out.reset();
		Assert.equal(ByteReader.transferBufferTo(reader(0, -1, 2), out, 3), 3);
		Assert.array(out.toByteArray(), 0, -1, 2);
	}

	@Test
	public void shouldStreamUnsignedBytes() {
		Assert.stream(reader(0, -1, 2, -3, 4).ustream(5), 0, 0xff, 2, 0xfd, 4);
		Assert.thrown(() -> reader(0, -1, 2).ustream(5).toArray());
	}

	private static void assertRemaining(ByteReader reader, int... bytes) {
		for (int b : bytes)
			Assert.equals(reader.readByte(), b);
		Assert.thrown(() -> reader.readByte());
	}

	private static ByteReader reader(int... bytes) {
		return reader(Array.bytes.of(bytes));
	}

	private static ByteReader reader(byte[] bytes) {
		return reader(bytes, 0, bytes.length);
	}

	private static ByteReader reader(byte[] bytes, int offset, int length) {
		return new ByteReader() {
			private int pos = 0;

			@Override
			public byte readByte() {
				Validate.index(length, pos);
				return bytes[offset + pos++];
			}
		};
	}
}
