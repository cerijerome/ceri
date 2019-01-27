package ceri.common.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Utilities for extending input and output streams.
 */
public class IoStreamUtil {

	private IoStreamUtil() {}

	/**
	 * InputStream with abstract byte array read method.
	 */
	public static abstract class In extends InputStream {
		@Override
		public int read() throws IOException {
			byte[] b = new byte[1];
			int count = read(b, 0, 1);
			if (count == -1) return -1;
			return b[0] & 0xff;
		}

		@Override
		public abstract int read(byte[] b, int off, int len) throws IOException;
	}

	/**
	 * OutputStream with abstract byte array write method.
	 */
	public static abstract class Out extends OutputStream {
		@Override
		public void write(int b) throws IOException {
			byte[] buffer = new byte[] { (byte) b };
			write(buffer, 0, 1);
		}

		@Override
		public abstract void write(byte[] b, int off, int len) throws IOException;
	}

	public static interface ByteReader {
		int read(byte[] b, int off, int len) throws IOException;
	}

	public static interface ByteWriter {
		void write(byte[] b, int off, int len) throws IOException;
	}

	public static InputStream in(ByteReader reader) {
		return new In() {
			@Override
			public int read(byte[] b, int off, int len) throws IOException {
				return reader.read(b, off, len);
			}
		};
	}

	public static OutputStream out(ByteWriter writer) {
		return new Out() {
			@Override
			public void write(byte[] b, int off, int len) throws IOException {
				writer.write(b, off, len);
			}
		};
	}

}
