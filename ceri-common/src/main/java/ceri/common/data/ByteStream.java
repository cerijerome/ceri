package ceri.common.data;

import java.io.ByteArrayOutputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import ceri.common.data.ByteArray.Immutable;
import ceri.common.data.ByteArray.Mutable;
import ceri.common.function.ExceptionRunnable;
import ceri.common.function.Fluent;
import ceri.common.io.IoUtil;
import ceri.common.io.RuntimeIoException;
import ceri.common.util.BasicUtil;
import ceri.common.util.ExceptionAdapter;

/**
 * Container class for {@link ByteReader} and {@link ByteWriter} wrappers for I/O streams. The
 * wrappers provide sequential access to stream bytes. The {@link Encoder} class enables building of
 * variable-length byte arrays using an underlying {@link ByteArrayOutputStream}.
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

	/**
	 * Concrete class for output stream writer.
	 */
	public static class Writer extends AbstractWriter<OutputStream, Writer> {
		private Writer(OutputStream out) {
			super(out);
		}

		/**
		 * Flushes the stream if supported by the wrapped stream, otherwise no-op.
		 */
		public Writer flush() {
			return run(out::flush);
		}
	}

	/**
	 * Class to enable building of variable-sized byte arrays.
	 */
	public static class Encoder extends AbstractWriter<ByteArrayOutputStream, Encoder> {
		
		static Encoder of() {
			return new Encoder();
		}
		
		private Encoder() {
			super(new ByteArrayOutputStream());
		}

		public byte[] bytes() {
			return out.toByteArray();
		}

		public Immutable immutable() {
			return Immutable.wrap(bytes());
		}

		public Mutable mutable() {
			return Mutable.wrap(bytes());
		}
	}

	/**
	 * Base abstract class for output stream writer.
	 */
	private static abstract class AbstractWriter<S extends OutputStream, //
		T extends AbstractWriter<S, T>> implements ByteWriter<T> {
		final S out;

		private AbstractWriter(S out) {
			this.out = out;
		}

		@Override
		public T writeByte(int value) {
			return run(() -> out.write(value));
		}

		@Override
		public T fill(int length, int value) {
			return writeFrom(ByteUtil.fill(length, value));
		}

		@Override
		public T writeFrom(byte[] array, int offset, int length) {
			return run(() -> out.write(array, offset, length));
		}

		@Override
		public T writeFrom(ByteProvider provider, int offset, int length) {
			return run(() -> provider.writeTo(offset, out, length));
		}

		@Override
		public int transferFrom(InputStream in, int length) throws IOException {
			return ByteWriter.transferBufferFrom(this, in, length);
		}

		T run(ExceptionRunnable<IOException> runnable) {
			ioAdapter.run(runnable);
			return BasicUtil.uncheckedCast(this);
		}
	}

}
