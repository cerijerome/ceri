package ceri.common.data;

import java.io.EOFException;
import java.io.FilterInputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import ceri.common.function.Excepts.Runnable;
import ceri.common.function.Excepts.Supplier;
import ceri.common.function.Fluent;
import ceri.common.io.IoExceptions;

/**
 * Container class for {@link ByteReader} and {@link ByteWriter} wrappers for I/O streams. The
 * wrappers provide sequential access to stream bytes.
 */
public class ByteStream {

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
	public static class Reader extends FilterInputStream implements ByteReader, Fluent<Reader> {

		private Reader(InputStream in) {
			super(in);
		}

		/* ByteReader overrides */

		@Override
		public byte readByte() {
			int b = get(in::read);
			if (b < 0) throw IoExceptions.RuntimeEof.of();
			return (byte) b;
		}

		@Override
		public Reader skip(int length) {
			run(() -> in.skipNBytes(length));
			return this;
		}

		@Override
		public byte[] readBytes(int length) {
			byte[] bytes = get(() -> in.readNBytes(length));
			checkEof(bytes.length, length);
			return bytes;
		}

		@Override
		public int readInto(byte[] dest, int offset, int length) {
			int n = get(() -> in.readNBytes(dest, offset, length));
			checkEof(n, length);
			return offset + n;
		}

		@Override
		public int readInto(ByteReceiver receiver, int offset, int length) {
			int i = get(() -> receiver.readFrom(offset, in, length));
			checkEof(i - offset, length);
			return i;
		}

		@Override
		public int transferTo(OutputStream out, int length) throws IOException {
			return ByteReader.transferBufferTo(this, out, length);
		}

		private static void checkEof(int actual, int expected) {
			if (actual < expected)
				throw IoExceptions.RuntimeEof.of("Incomplete read: %d/%d", actual, expected);
		}
	}

	/**
	 * Concrete class for output stream writer.
	 */
	public static class Writer extends FilterOutputStream implements ByteWriter<Writer> {

		private Writer(OutputStream out) {
			super(out);
		}

		@Override
		public Writer writeByte(int value) {
			run(() -> out.write(value));
			return this;
		}

		@Override
		public Writer fill(int length, int value) {
			return writeFrom(ByteUtil.fill(length, value));
		}

		@Override
		public Writer writeFrom(byte[] array, int offset, int length) {
			run(() -> out.write(array, offset, length));
			return this;
		}

		@Override
		public Writer writeFrom(ByteProvider provider, int offset, int length) {
			run(() -> provider.writeTo(offset, out, length));
			return this;
		}

		@Override
		public int transferFrom(InputStream in, int length) throws IOException {
			return ByteWriter.transferBufferFrom(this, in, length);
		}
	}

	private static void run(Runnable<IOException> runnable) {
		try {
			runnable.run();
		} catch (EOFException e) {
			throw IoExceptions.RuntimeEof.of(e, e.getMessage());
		} catch (IOException e) {
			throw new IoExceptions.Runtime(e);
		}
	}

	private static <T> T get(Supplier<IOException, T> supplier) {
		try {
			return supplier.get();
		} catch (IOException e) {
			throw new IoExceptions.Runtime(e);
		}
	}
}
