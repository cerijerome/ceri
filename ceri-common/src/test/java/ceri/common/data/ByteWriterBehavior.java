package ceri.common.data;

import static ceri.common.collection.ArrayUtil.bytes;
import static ceri.common.test.AssertUtil.assertArray;
import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertThrown;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import org.junit.Test;
import ceri.common.collection.ArrayUtil;
import ceri.common.data.ByteArray.Mutable;
import ceri.common.function.Excepts.Consumer;
import ceri.common.test.TestUtil;
import ceri.common.validation.ValidationUtil;

public class ByteWriterBehavior {
	private static final boolean msb = ByteUtil.IS_BIG_ENDIAN;
	private static final byte[] ascii = "abcde".getBytes(StandardCharsets.ISO_8859_1);
	private static final byte[] utf8 = "abcde".getBytes(StandardCharsets.UTF_8);
	private static final byte[] defCset = "abcde".getBytes(Charset.defaultCharset());

	@Test
	public void shouldSkipBytes() {
		assertBytes(3, w -> w.skip(0), 0, 0, 0);
		assertBytes(5, w -> w.writeByte(1).skip(3).writeByte(0xff), 1, 0, 0, 0, 0xff);
	}

	@Test
	public void shouldWritePrimitiveValues() {
		assertBytes(1, w -> w.writeBool(false), 0);
		assertBytes(1, w -> w.writeBool(true), 1);
		assertBytes(1, w -> w.writeByte(-1), -1);
		assertBytes(2, w -> w.writeShort(0x7f80), msb ? bytes(0x7f, 0x80) : bytes(0x80, 0x7f));
		assertBytes(4, w -> w.writeInt(0x1007f80),
			msb ? bytes(1, 0, 0x7f, 0x80) : bytes(0x80, 0x7f, 0, 1));
		assertBytes(8, w -> w.writeLong(0xff01007f80L),
			msb ? bytes(0, 0, 0, 0xff, 1, 0, 0x7f, 0x80) : bytes(0x80, 0x7f, 0, 1, 0xff, 0, 0, 0));
		assertBytes(4, w -> w.writeFloat(Float.intBitsToFloat(0x1007f80)),
			msb ? bytes(1, 0, 0x7f, 0x80) : bytes(0x80, 0x7f, 0, 1));
		assertBytes(8, w -> w.writeDouble(Double.longBitsToDouble(0xff01007f80L)),
			msb ? bytes(0, 0, 0, 0xff, 1, 0, 0x7f, 0x80) : bytes(0x80, 0x7f, 0, 1, 0xff, 0, 0, 0));
	}

	@Test
	public void shouldWriteByteAlignedValues() {
		assertBytes(2, w -> w.writeShortMsb(0x7f80), 0x7f, 0x80);
		assertBytes(2, w -> w.writeShortLsb(0x7f80), 0x80, 0x7f);
		assertBytes(4, w -> w.writeIntMsb(0x1007f80), 1, 0, 0x7f, 0x80);
		assertBytes(4, w -> w.writeIntLsb(0x1007f80), 0x80, 0x7f, 0, 1);
		assertBytes(8, w -> w.writeLongMsb(0xff01007f80L), 0, 0, 0, 0xff, 1, 0, 0x7f, 0x80);
		assertBytes(8, w -> w.writeLongLsb(0xff01007f80L), 0x80, 0x7f, 0, 1, 0xff, 0, 0, 0);
		assertBytes(4, w -> w.writeFloatMsb(Float.intBitsToFloat(0x1007f80)), 1, 0, 0x7f, 0x80);
		assertBytes(4, w -> w.writeFloatLsb(Float.intBitsToFloat(0x1007f80)), 0x80, 0x7f, 0, 1);
		assertBytes(8, w -> w.writeDoubleMsb(Double.longBitsToDouble(0xff01007f80L)), 0, 0, 0, 0xff,
			1, 0, 0x7f, 0x80);
		assertBytes(8, w -> w.writeDoubleLsb(Double.longBitsToDouble(0xff01007f80L)), 0x80, 0x7f, 0,
			1, 0xff, 0, 0, 0);
	}

	@Test
	public void shouldWriteEncodedString() {
		assertBytes(5, w -> w.writeAscii("abcde"), ascii);
		assertBytes(5, w -> w.writeUtf8("abcde"), utf8);
		assertBytes(5, w -> w.writeString("abcde"), defCset);
	}

	@Test
	public void shouldFillBytes() {
		assertBytes(3, w -> w.fill(0, 0xff), 0, 0, 0);
		assertBytes(3, w -> w.fill(2, 0xff), 0xff, 0xff, 0);
		assertThrown(() -> writer(3).fill(4, 0xff));
	}

	@Test
	public void shouldWriteFromByteArray() {
		assertBytes(3, w -> w.writeBytes(1, 2, 3), 1, 2, 3);
		assertThrown(() -> writer(3).writeBytes(1, 2, 3, 4));
	}

	@Test
	public void shouldWriteFromByteProvider() {
		assertBytes(3, w -> w.writeFrom(Mutable.wrap(1, 2, 3)), 1, 2, 3);
		assertThrown(() -> writer(3).writeFrom(Mutable.wrap(1, 2, 3, 4)));
	}

	@Test
	public void shouldTransferFromInputStream() throws IOException {
		try (InputStream in = TestUtil.inputStream(1, 2, 3)) {
			assertBytes(3, w -> assertEquals(w.transferFrom(in, 2), 2), 1, 2, 0);
			assertBytes(3, w -> assertEquals(w.transferFrom(in, 2), 1), 3, 0, 0);
		}
	}

	@Test
	public void shouldTransferBufferFromInputStream() throws IOException {
		try (InputStream in = TestUtil.inputStream(1, 2, 3)) {
			assertBytes(3, w -> assertEquals(ByteWriter.transferBufferFrom(w, in, 2), 2), 1, 2, 0);
			assertBytes(3, w -> assertEquals(ByteWriter.transferBufferFrom(w, in, 2), 1), 3, 0, 0);
		}
	}

	/**
	 * Creates a ByteWriter wrapping a fixed-size byte array, executes the action on the
	 * ByteReceiver, and asserts the bytes in the array.
	 */
	private static <E extends Exception> void assertBytes(int size,
		Consumer<E, ByteWriter<?>> action, int... bytes) throws E {
		assertBytes(size, action, ArrayUtil.bytes(bytes));
	}

	/**
	 * Creates a ByteWriter wrapping a fixed-size byte array, executes the action on the
	 * ByteReceiver, and asserts the bytes in the array.
	 */
	private static <E extends Exception> void assertBytes(int size,
		Consumer<E, ByteWriter<?>> action, byte[] bytes) throws E {
		Holder holder = Holder.of(size);
		action.accept(holder.writer);
		assertArray(holder.bytes, bytes);
	}

	/**
	 * Class to hold a byte array and a simple ByteReceiver wrapper.
	 */
	public static class Holder {
		public final byte[] bytes;
		public final SimpleByteWriter writer;

		public static Holder of(int size) {
			byte[] bytes = new byte[size];
			return new Holder(bytes, new SimpleByteWriter(bytes, 0, bytes.length));
		}

		private Holder(byte[] bytes, SimpleByteWriter writer) {
			this.bytes = bytes;
			this.writer = writer;
		}

		public Holder reset() {
			Arrays.fill(bytes, (byte) 0);
			writer.reset();
			return this;
		}
	}

	private static ByteWriter<?> writer(int size) {
		return writer(new byte[size]);
	}

	/**
	 * Simple ByteWriter implementation, wrapping bytes.
	 */
	public static ByteWriter<?> writer(byte[] bytes) {
		return writer(bytes, 0, bytes.length);
	}

	/**
	 * Returns a simple ByteWriter implementation, wrapping bytes.
	 */
	public static ByteWriter<?> writer(byte[] bytes, int offset, int length) {
		return new SimpleByteWriter(bytes, offset, length);
	}

	/**
	 * Simple ByteWriter implementation, wrapping bytes.
	 */
	public static class SimpleByteWriter implements ByteWriter<SimpleByteWriter> {
		private final byte[] bytes;
		private final int offset;
		private final int length;
		private int index = 0;

		private SimpleByteWriter(byte[] bytes, int offset, int length) {
			this.bytes = bytes;
			this.offset = offset;
			this.length = length;
		}

		@Override
		public SimpleByteWriter writeByte(int value) {
			ValidationUtil.validateIndex(length, index);
			bytes[offset + index++] = (byte) value;
			return this;
		}

		public SimpleByteWriter reset() {
			index = 0;
			return this;
		}
	}

}
