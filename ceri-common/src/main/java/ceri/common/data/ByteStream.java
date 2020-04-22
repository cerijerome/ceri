package ceri.common.data;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import ceri.common.function.ExceptionRunnable;
import ceri.common.function.Fluent;
import ceri.common.io.IoUtil;
import ceri.common.io.RuntimeIoException;
import ceri.common.util.ExceptionAdapter;

/**
 * {@link ByteWriter} wrapper for a {@link java.io.OutputStream}. This provides sequential writing
 * of bytes. The type T allows typed access to the OutputStream methods, such as
 * ByteArrayOutputStream.toByteArray().
 */
public class ByteStream {
	private static final ExceptionAdapter<RuntimeIoException> ioAdapter = IoUtil.RUNTIME_IO_ADAPTER;

	public static Reader reader(InputStream in) {
		return new Reader(in);
	}

	public static Writer writer(OutputStream out) {
		return new Writer(out);
	}

	private ByteStream() {}

	/**
	 * {@link ByteReader} wrapper for a {@link java.io.InputStream}. This provides sequential
	 * reading of bytes. The type T allows typed access to the InputStream methods.
	 */
	public static class Reader implements ByteReader, Fluent<Reader> {
		private static final int READ_LIMIT_DEF = 8 * 1024;
		private final InputStream in;

		private Reader(InputStream in) {
			this.in = in;
		}

		/* ByteReader overrides */

		@Override
		public byte readByte() {
			int b = ioAdapter.getInt(in::read);
			if (b < 0) throw new RuntimeIoException("End of stream");
			return (byte) b;
		}

		@Override
		public Reader skip(int length) {
			ioAdapter.run(() -> in.skipNBytes(length));
			return this;
		}

		@Override
		public byte[] readBytes(int length) {
			byte[] bytes = ioAdapter.get(() -> in.readNBytes(length));
			verifyLength(bytes.length, length);
			return bytes;
		}

		@Override
		public int readInto(byte[] dest, int offset, int length) {
			int n = ioAdapter.getInt(() -> in.readNBytes(dest, offset, length));
			verifyLength(n, length);
			return offset + n;
		}

		@Override
		public int readInto(ByteReceiver receiver, int offset, int length) {
			int i = ioAdapter.getInt(() -> receiver.readFrom(offset, in, length));
			verifyLength(i - offset, length);
			return i;
		}

		@Override
		public int transferTo(OutputStream out, int length) throws IOException {
			return ByteReader.transferBufferTo(this, out, length);
		}

		/* InputStream methods */

		/**
		 * Returns the number of bytes available for reading without blocking. If not supported by
		 * the InputStream, it returns 0. Wraps any IOExceptions with unchecked RuntimeIoException.
		 */
		public int available() {
			return ioAdapter.getInt(in::available);
		}

		/**
		 * Returns true if the InputStream supports mark/reset.
		 */
		public boolean markSupported() {
			return in.markSupported();
		}

		/**
		 * Marks the current position, with a default maximum read limit. A call to reset() will
		 * reset the stream position if supported.
		 */
		public Reader mark() {
			return mark(READ_LIMIT_DEF);
		}

		/**
		 * Marks the current position, with the given read limit. A call to reset() will reset the
		 * stream position if supported.
		 */
		public Reader mark(int readlimit) {
			in.mark(readlimit);
			return this;
		}

		/**
		 * Moves the stream to the mark() position, if supported by the InputStream. Wraps any
		 * IOExceptions with unchecked RuntimeIoException.
		 */
		public Reader reset() {
			ioAdapter.run(in::reset);
			return this;
		}

		private void verifyLength(int actual, int expected) {
			if (actual < expected) throw new RuntimeIoException(
				String.format("Incomplete read: %d/%d", actual, expected));
		}
	}

	public static class Writer implements ByteWriter<Writer> {
		private final OutputStream out;

		private Writer(OutputStream out) {
			this.out = out;
		}

		/* ByteWriter overrides */

		@Override
		public Writer writeByte(int value) {
			return run(() -> out.write(value));
		}

		@Override
		public Writer fill(int length, int value) {
			return writeFrom(ByteUtil.fill(length, value));
		}

		@Override
		public Writer writeFrom(byte[] array, int offset, int length) {
			return run(() -> out.write(array, offset, length));
		}

		@Override
		public Writer writeFrom(ByteProvider provider, int offset, int length) {
			return run(() -> provider.writeTo(offset, out, length));
		}

		@Override
		public int transferFrom(InputStream in, int length) throws IOException {
			return ByteWriter.transferBufferFrom(this, in, length);
		}

		/* OutputStream methods */

		/**
		 * Flushes the stream if supported by the wrapped stream, otherwise no-op.
		 */
		public Writer flush() {
			return run(out::flush);
		}

		private Writer run(ExceptionRunnable<IOException> runnable) {
			ioAdapter.run(runnable);
			return this;
		}
	}

}
