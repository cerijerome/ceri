package ceri.common.io;

import java.io.InputStream;

/**
 * An input stream that returns no data. Unlike InputStream.nullInputStream, this returns 0 from
 * read(), rather than appear as if end of stream is reached.
 */
public class NullInputStream extends InputStream {

	NullInputStream() {}

	@Override
	public int read() {
		return 0;
	}

	@Override
	public int read(byte[] b, int off, int len) {
		return len;
	}

	@Override
	public byte[] readAllBytes() {
		return new byte[0];
	}

	@Override
	public int readNBytes(byte[] b, int off, int len) {
		return len;
	}
}
