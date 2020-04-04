package ceri.common.data;

import static ceri.common.test.TestUtil.assertArray;
import static ceri.common.test.TestUtil.assertThrown;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Arrays;
import org.junit.Test;
import ceri.common.collection.ArrayUtil;
import ceri.common.data.ByteArray.Mutable;
import ceri.common.function.ExceptionConsumer;
import ceri.common.test.TestUtil;

public class NavigableByteWriterBehavior {
	private static final byte[] defCset = "abcde".getBytes(Charset.defaultCharset());

	@Test
	public void shouldWrapByteArray() {
		byte[] bytes = ArrayUtil.bytes(0, -1, 2, -3, 4);
		NavigableByteWriter.wrap(bytes).skip(2).fill(0xff);
		assertArray(bytes, 0, -1, 0xff, 0xff, 0xff);
	}

	@Test
	public void shouldWrapByteReceiver() {
		byte[] bytes = ArrayUtil.bytes(0, -1, 2, -3, 4);
		NavigableByteWriter.wrap(Mutable.wrap(bytes), 1).skip(2).fill(0xff);
		assertArray(bytes, 0, -1, 2, 0xff, 0xff);
	}

	@Test
	public void shouldMarkAndResetPosition() {
		Holder h = Holder.of(5);
		h.writer.skip(2).mark().fill(0xff);
		assertThat(h.writer.marked(), is(3));
		assertArray(h.bytes, 0, 0, 0xff, 0xff, 0xff);
		h.writer.reset().fill(2, 0);
		assertArray(h.bytes, 0, 0, 0, 0, 0xff);
	}

	@Test
	public void shouldWriteByte() {
		assertBytes(3, w -> w.writeByte(1).writeByte(0xff), 1, 0xff, 0);
		assertThrown(() -> writer(0).writeByte(0xff));
	}

	@Test
	public void shouldWriteByteAlignedValues() {
		assertBytes(4, w -> w.writeEndian(0xff8000, 3, true), 0xff, 0x80, 0, 0);
		assertBytes(4, w -> w.writeEndian(0xff8000, 3, false), 0, 0x80, 0xff, 0);
	}

	@Test
	public void shouldWriteEncodedString() {
		assertBytes(5, w -> w.writeString("abcde"), defCset);
	}

	@Test
	public void shouldFillBytes() {
		assertBytes(5, w -> w.fill(2, 0xff).fill(1), 0xff, 0xff, 1, 1, 1);
		assertThrown(() -> writer(2).fill(3, 0xff));
	}

	@Test
	public void shouldWriteFromByteArray() {
		assertBytes(3, w -> w.writeFrom(1, 2, 3), 1, 2, 3);
		assertThrown(() -> writer(3).writeFrom(1, 2, 3, 4));
	}

	@Test
	public void shouldTransferFromInputStream() throws IOException {
		try (InputStream in = TestUtil.inputStream(1, 2, 3)) {
			assertBytes(2, w -> assertThat(w.transferFrom(in), is(2)), 1, 2);
			assertBytes(3, w -> assertThat(w.transferFrom(in, 2), is(1)), 3, 0, 0);
			assertThrown(() -> writer(2).transferFrom(in, 3));
		}
	}

	@Test
	public void shouldSliceProvidedByteRange() {
		assertBytes(5, w -> w.skip(2).slice().fill(0xff), 0, 0, 0xff, 0xff, 0xff);
		assertBytes(3, w -> w.skip(1).slice(0).fill(0xff), 0, 0, 0);
		assertBytes(5, w -> w.skip(3).slice(-2).fill(0xff), 0, 0xff, 0xff, 0, 0);
		assertThrown(() -> writer(3).skip(2).slice(2));
	}

	/**
	 * Create writer wrapper for array of given length, execute an action on it, then verify the
	 * byte array.
	 */
	private static <E extends Exception> void assertBytes(int size,
		ExceptionConsumer<E, NavigableByteWriter> action, int... bytes) throws E {
		assertBytes(size, action, ArrayUtil.bytes(bytes));
	}

	/**
	 * Create wrapper for sized byte array; used for exception checking.
	 */
	private static NavigableByteWriter writer(int size) {
		return NavigableByteWriter.wrap(new byte[size]);
	}

	/**
	 * Creates a NavigableByteWriter wrapping a fixed-size byte array, executes the action on the
	 * ByteReceiver, and asserts the bytes in the array.
	 */
	private static <E extends Exception> void assertBytes(int size,
		ExceptionConsumer<E, NavigableByteWriter> action, byte[] bytes) throws E {
		Holder holder = Holder.of(size);
		action.accept(holder.writer);
		assertArray(holder.bytes, bytes);
	}

	/**
	 * Class to hold a byte array and a simple ByteReceiver wrapper.
	 */
	public static class Holder {
		public final byte[] bytes;
		public final NavigableByteWriter writer;

		public static Holder of(int size) {
			byte[] bytes = new byte[size];
			return new Holder(bytes, NavigableByteWriter.wrap(bytes));
		}

		private Holder(byte[] bytes, NavigableByteWriter writer) {
			this.bytes = bytes;
			this.writer = writer;
		}

		public Holder reset() {
			Arrays.fill(bytes, (byte) 0);
			writer.reset();
			return this;
		}
	}

}
