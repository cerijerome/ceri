package ceri.common.io;

import java.io.InputStream;

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
