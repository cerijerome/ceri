package ceri.common.io;

import static ceri.common.collection.ArrayUtil.EMPTY_BYTE;
import static ceri.common.collection.ArrayUtil.bytes;
import static ceri.common.function.FunctionUtil.safeApply;
import java.io.FilterInputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Objects;
import ceri.common.function.ExceptionFunction;
import ceri.common.function.ExceptionIntConsumer;
import ceri.common.function.ExceptionIntSupplier;
import ceri.common.function.ExceptionObjIntPredicate;
import ceri.common.math.MathUtil;

/**
 * Utilities for creating input and output streams.
 */
public class IoStreamUtil {
	private static final int MAX_SKIP_BUFFER_SIZE = 2048; // from InputStream

	private IoStreamUtil() {}

	/**
	 * An input stream that returns no data. Unlike InputStream.nullInputStream, this returns 0 from
	 * read(), rather than appear as if end of stream is reached.
	 */
	public static class NullIn extends InputStream {
		private volatile boolean closed = false;

		protected NullIn() {}

		@Override
		public int available() throws IOException {
			ensureOpen();
			return 0;
		}

		@Override
		public int read() throws IOException {
			ensureOpen();
			return 0;
		}

		@Override
		public int read(byte[] b, int off, int len) throws IOException {
			ensureOpen();
			return len;
		}

		@Override
		public byte[] readAllBytes() throws IOException {
			ensureOpen();
			return EMPTY_BYTE;
		}

		@Override
		public int readNBytes(byte[] b, int off, int len) throws IOException {
			ensureOpen();
			return len;
		}

		@Override
		public void close() throws IOException {
			closed = true;
		}

		protected void ensureOpen() throws IOException {
			if (closed) throw new IOException("Stream closed");
		}
	}

	/**
	 * Interface for stream read operations.
	 */
	public static interface Read {
		/**
		 * Reads into byte array at given offset. Returns actual number of bytes read.
		 */
		int read(byte[] b, int offset, int length) throws IOException;
	}

	/**
	 * Interface for handling or delegating read operations.
	 */
	public static interface FilterRead {
		/**
		 * Optionally reads into byte array at given offset. Returns actual number of bytes read, or
		 * null if delegating to wrapped stream.
		 */
		Integer read(InputStream in, byte[] b, int offset, int length) throws IOException;
	}

	/**
	 * Interface for stream write operations.
	 */
	public static interface Write {
		/**
		 * Writes bytes from array at given offset.
		 */
		void write(byte[] b, int offset, int length) throws IOException;
	}

	/**
	 * Interface for handling or delegating stream write operations.
	 */
	public static interface FilterWrite {
		/**
		 * Optionally writes bytes from array at given offset. Returns false if delegating to
		 * wrapped stream.
		 */
		boolean write(OutputStream out, byte[] b, int offset, int length) throws IOException;
	}

	/**
	 * Provides a stream that absorbs calls until the stream is closed. Unlike
	 * InputStream.nullInputStream, this returns 0 from read(), rather than appear as if end of
	 * stream is reached.
	 */
	public static InputStream nullIn() {
		return new NullIn();
	}

	/**
	 * Provides a stream that absorbs calls until the stream is closed.
	 */
	public static OutputStream nullOut() {
		return OutputStream.nullOutputStream();
	}

	/**
	 * Returns a stream based on given read function.
	 */
	public static InputStream in(ExceptionIntSupplier<IOException> readFn) {
		return in(readFn, null);
	}

	/**
	 * Returns a stream based on given read and available functions.
	 */
	public static InputStream in(ExceptionIntSupplier<IOException> readFn,
		ExceptionIntSupplier<IOException> availableFn) {
		return new InputStream() {
			@Override
			public int available() throws IOException {
				return IoStreamUtil.available(availableFn);
			}

			@Override
			public int read() throws IOException {
				return IoStreamUtil.read(readFn);
			}
		};
	}

	/**
	 * Returns a stream based on given read function.
	 */
	public static InputStream in(Read readFn) {
		return in(readFn, null);
	}

	/**
	 * Returns a stream based on given read and available functions.
	 */
	public static InputStream in(Read readFn, ExceptionIntSupplier<IOException> availableFn) {
		return new InputStream() {
			@Override
			public int available() throws IOException {
				return IoStreamUtil.available(availableFn);
			}

			@Override
			public int read() throws IOException {
				return IoStreamUtil.read(readFn);
			}

			@Override
			public int read(byte[] b, int off, int len) throws IOException {
				return IoStreamUtil.read(readFn, b, off, len);
			}
		};
	}

	/**
	 * Returns a filtered stream that can handle or delegate read methods to the wrapped stream.
	 */
	public static InputStream filterIn(InputStream in,
		ExceptionFunction<IOException, InputStream, Integer> readFn) {
		return filterIn(in, readFn, null);
	}

	/**
	 * Returns a filtered stream that can handle or delegate read and available methods to the
	 * wrapped stream.
	 */
	public static InputStream filterIn(InputStream in,
		ExceptionFunction<IOException, InputStream, Integer> readFn,
		ExceptionFunction<IOException, InputStream, Integer> availableFn) {
		return new FilterInputStream(in) {
			@Override
			public int available() throws IOException {
				return IoStreamUtil.available(in, availableFn);
			}

			@Override
			public int read() throws IOException {
				return IoStreamUtil.read(in, readFn);
			}

			@Override
			public int read(byte[] b, int off, int len) throws IOException {
				return IoStreamUtil.read(in, readFn, b, off, len);
			}

			@Override
			public long skip(long n) throws IOException {
				return IoStreamUtil.skip(this, n);
			}
		};
	}

	/**
	 * Returns a filtered stream that can handle or delegate read methods to the wrapped stream.
	 */
	public static InputStream filterIn(InputStream in, FilterRead readFn) {
		return filterIn(in, readFn, null);
	}

	/**
	 * Returns a filtered stream that can handle or delegate read and available methods to the
	 * wrapped stream.
	 */
	public static InputStream filterIn(InputStream in, FilterRead readFn,
		ExceptionFunction<IOException, InputStream, Integer> availableFn) {
		return new FilterInputStream(in) {
			@Override
			public int available() throws IOException {
				return IoStreamUtil.available(in, availableFn);
			}

			@Override
			public int read() throws IOException {
				return IoStreamUtil.read(in, readFn);
			}

			@Override
			public int read(byte[] b, int off, int len) throws IOException {
				return IoStreamUtil.read(in, readFn, b, off, len);
			}

			@Override
			public long skip(long n) throws IOException {
				return IoStreamUtil.skip(this, n);
			}
		};
	}

	/**
	 * Returns a stream based on given write function.
	 */
	public static OutputStream out(ExceptionIntConsumer<IOException> writeFn) {
		return new OutputStream() {
			@Override
			public void write(int b) throws IOException {
				if (writeFn != null) writeFn.accept(b);
			}
		};
	}

	/**
	 * Returns a stream based on given write function.
	 */
	public static OutputStream out(Write writeFn) {
		return new OutputStream() {
			@Override
			public void write(int b) throws IOException {
				if (writeFn != null) write(bytes(b), 0, 1);
			}

			@Override
			public void write(byte[] b, int off, int len) throws IOException {
				Objects.checkFromIndexSize(off, len, b.length);
				if (writeFn != null) writeFn.write(b, off, len);
			}
		};
	}

	/**
	 * Returns a stream based on given write function.
	 */
	public static OutputStream filterOut(OutputStream out,
		ExceptionObjIntPredicate<IOException, OutputStream> writeFn) {
		return new FilterOutputStream(out) {
			@Override
			public void write(int b) throws IOException {
				IoStreamUtil.write(out, writeFn, b);
			}

			@Override
			public void write(byte[] b, int off, int len) throws IOException {
				IoStreamUtil.write(out, writeFn, b, off, len);
			}
		};
	}

	/**
	 * Returns a filtered stream that can handle or delegate write methods to the wrapped stream.
	 */
	public static OutputStream filterOut(OutputStream out, FilterWrite writeFn) {
		return new FilterOutputStream(out) {
			@Override
			public void write(int b) throws IOException {
				IoStreamUtil.write(out, writeFn, b);
			}

			@Override
			public void write(byte[] b, int off, int len) throws IOException {
				IoStreamUtil.write(out, writeFn, b, off, len);
			}
		};
	}

	/* InputStream methods */

	private static int available(ExceptionIntSupplier<IOException> availableFn) throws IOException {
		if (availableFn == null) return 0;
		return availableFn.getAsInt();
	}

	private static int read(ExceptionIntSupplier<IOException> readFn) throws IOException {
		if (readFn == null) return 0;
		return readFn.getAsInt();
	}

	private static int read(Read readFn) throws IOException {
		if (readFn == null) return 0;
		byte[] b = new byte[1];
		int n = readFn.read(b, 0, 1);
		return n == -1 ? n : MathUtil.ubyte(b[0]);
	}

	private static int read(Read readFn, byte b[], int off, int len) throws IOException {
		Objects.checkFromIndexSize(off, len, b.length);
		return safeApply(readFn, r -> r.read(b, off, len), len);
	}

	/* FilterInputStream methods */

	private static long skip(InputStream in, long n) throws IOException {
		if (n <= 0) return 0;
		long rem = n;
		byte[] buffer = new byte[(int) Math.min(MAX_SKIP_BUFFER_SIZE, n)];
		while (rem > 0) {
			int r = in.read(buffer, 0, (int) Math.min(buffer.length, rem));
			if (r < 0) break;
			rem -= r;
		}
		return n - rem;
	}

	private static int available(InputStream in,
		ExceptionFunction<IOException, InputStream, Integer> availableFn) throws IOException {
		Integer n = safeApply(availableFn, a -> a.apply(in));
		return n != null ? n : in.available();
	}

	private static int read(InputStream in,
		ExceptionFunction<IOException, InputStream, Integer> readFn) throws IOException {
		Integer n = safeApply(readFn, r -> r.apply(in));
		return n != null ? n : in.read();
	}

	private static int read(InputStream in,
		ExceptionFunction<IOException, InputStream, Integer> readFn, byte b[], int off, int len)
		throws IOException {
		if (readFn == null) return in.read(b, off, len);
		Objects.checkFromIndexSize(off, len, b.length);
		if (len == 0) return 0;
		int c = read(in, readFn);
		if (c == -1) return -1;
		b[off] = (byte) c;
		int i = 1;
		try {
			for (; i < len; i++) {
				c = read(in, readFn);
				if (c == -1) break;
				b[off + i] = (byte) c;
			}
		} catch (IOException ee) {}
		return i;
	}

	private static int read(InputStream in, FilterRead readFn) throws IOException {
		if (readFn == null) return in.read();
		byte[] b = new byte[1];
		int n = read(in, readFn, b, 0, 1);
		return n == -1 ? n : MathUtil.ubyte(b[0]);
	}

	private static int read(InputStream in, FilterRead readFn, byte b[], int off, int len)
		throws IOException {
		Objects.checkFromIndexSize(off, len, b.length);
		Integer n = safeApply(readFn, r -> r.read(in, b, off, len));
		return n != null ? n : in.read(b, off, len);
	}

	/* FilterOutputStream methods */

	private static void write(OutputStream out,
		ExceptionObjIntPredicate<IOException, OutputStream> writeFn, int b) throws IOException {
		if (!safeApply(writeFn, w -> w.test(out, b), false)) out.write(b);
	}

	private static void write(OutputStream out,
		ExceptionObjIntPredicate<IOException, OutputStream> writeFn, byte[] b, int off, int len)
		throws IOException {
		if (writeFn == null) out.write(b, off, len);
		else {
			Objects.checkFromIndexSize(off, len, b.length);
			for (int i = 0; i < len; i++)
				write(out, writeFn, b[off + i]);
		}
	}

	private static void write(OutputStream out, FilterWrite writeFn, int b) throws IOException {
		if (writeFn == null) out.write(b);
		else write(out, writeFn, bytes(b), 0, 1);
	}

	private static void write(OutputStream out, FilterWrite writeFn, byte[] b, int off, int len)
		throws IOException {
		Objects.checkFromIndexSize(off, len, b.length);
		if (!safeApply(writeFn, w -> w.write(out, b, off, len), false)) out.write(b, off, len);
	}

}
