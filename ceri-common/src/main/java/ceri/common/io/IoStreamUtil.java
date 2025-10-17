package ceri.common.io;

import java.io.FilterInputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Objects;
import ceri.common.array.ArrayUtil;
import ceri.common.function.Excepts;
import ceri.common.function.Functional;
import ceri.common.math.Maths;

/**
 * Utilities for creating input and output streams.
 */
public class IoStreamUtil {
	private static final int MAX_SKIP_BUFFER_SIZE = 2048; // from InputStream
	/** No-op, stateless, input stream. */
	public static final NullIn nullIn = new NullIn();
	/** No-op, stateless, output stream. */
	public static final NullOut nullOut = new NullOut();

	private IoStreamUtil() {}

	/**
	 * An no-op, stateless input stream. Unlike InputStream.nullInputStream, this returns 0 for
	 * read(), and the requested length for other read(...) calls. It does not keep track of closed
	 * state.
	 */
	public static class NullIn extends InputStream {
		private NullIn() {}

		@Override
		public int read() {
			return 0;
		}

		@Override
		public byte[] readAllBytes() {
			return ArrayUtil.bytes.empty; // 1-byte instead?
		}

		@Override
		public long transferTo(OutputStream out) throws IOException {
			return 0;
		}
	}

	/**
	 * An no-op, stateless output stream. Unlike OutputStream.nullOutputStream, this does not keep
	 * track of closed state.
	 */
	public static class NullOut extends OutputStream {
		private NullOut() {}

		@Override
		public void write(int b) {}
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
	 * Returns a stream based on given read function.
	 */
	public static InputStream in(Excepts.IntSupplier<IOException> readFn) {
		return in(readFn, null);
	}

	/**
	 * Returns a stream based on given read and available functions.
	 */
	public static InputStream in(Excepts.IntSupplier<IOException> readFn,
		Excepts.IntSupplier<IOException> availableFn) {
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
	public static InputStream in(Read readFn, Excepts.IntSupplier<IOException> availableFn) {
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
		Excepts.Function<IOException, InputStream, Integer> readFn) {
		return filterIn(in, readFn, null);
	}

	/**
	 * Returns a filtered stream that can handle or delegate read and available methods to the
	 * wrapped stream.
	 */
	public static InputStream filterIn(InputStream in,
		Excepts.Function<IOException, InputStream, Integer> readFn,
		Excepts.Function<IOException, InputStream, Integer> availableFn) {
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
		Excepts.Function<IOException, InputStream, Integer> availableFn) {
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
	public static OutputStream out(Excepts.IntConsumer<IOException> writeFn) {
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
				if (writeFn != null) write(ArrayUtil.bytes.of(b), 0, 1);
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
		Excepts.ObjIntPredicate<IOException, OutputStream> writeFn) {
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

	private static int available(Excepts.IntSupplier<IOException> availableFn) throws IOException {
		if (availableFn == null) return 0;
		return availableFn.getAsInt();
	}

	private static int read(Excepts.IntSupplier<IOException> readFn) throws IOException {
		if (readFn == null) return 0;
		return readFn.getAsInt();
	}

	private static int read(Read readFn) throws IOException {
		if (readFn == null) return 0;
		byte[] b = new byte[1];
		int n = readFn.read(b, 0, 1);
		return n == -1 ? n : Maths.ubyte(b[0]);
	}

	private static int read(Read readFn, byte b[], int off, int len) throws IOException {
		Objects.checkFromIndexSize(off, len, b.length);
		return Functional.apply(r -> r.read(b, off, len), readFn, len);
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
		Excepts.Function<IOException, InputStream, Integer> availableFn) throws IOException {
		var n = Functional.apply(a -> a.apply(in), availableFn);
		return n != null ? n : in.available();
	}

	private static int read(InputStream in,
		Excepts.Function<IOException, InputStream, Integer> readFn) throws IOException {
		var n = Functional.apply(r -> r.apply(in), readFn);
		return n != null ? n : in.read();
	}

	private static int read(InputStream in,
		Excepts.Function<IOException, InputStream, Integer> readFn, byte b[], int off, int len)
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
		return n == -1 ? n : Maths.ubyte(b[0]);
	}

	private static int read(InputStream in, FilterRead readFn, byte b[], int off, int len)
		throws IOException {
		Objects.checkFromIndexSize(off, len, b.length);
		var n = Functional.apply(r -> r.read(in, b, off, len), readFn);
		return n != null ? n : in.read(b, off, len);
	}

	/* FilterOutputStream methods */

	private static void write(OutputStream out,
		Excepts.ObjIntPredicate<IOException, OutputStream> writeFn, int b) throws IOException {
		if (!Functional.apply(w -> w.test(out, b), writeFn, false)) out.write(b);
	}

	private static void write(OutputStream out,
		Excepts.ObjIntPredicate<IOException, OutputStream> writeFn, byte[] b, int off, int len)
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
		else write(out, writeFn, ArrayUtil.bytes.of(b), 0, 1);
	}

	private static void write(OutputStream out, FilterWrite writeFn, byte[] b, int off, int len)
		throws IOException {
		Objects.checkFromIndexSize(off, len, b.length);
		if (!Functional.apply(w -> w.write(out, b, off, len), writeFn, false))
			out.write(b, off, len);
	}
}
