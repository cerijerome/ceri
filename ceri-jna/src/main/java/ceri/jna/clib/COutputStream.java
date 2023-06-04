package ceri.jna.clib;

import java.io.OutputStream;
import ceri.jna.clib.jna.CError;
import ceri.jna.clib.jna.CException;
import ceri.jna.clib.jna.CUnistd;
import ceri.jna.util.JnaUtil;
import ceri.jna.util.ThreadBuffers;

/**
 * OutputStream for a file descriptor.
 */
public class COutputStream extends OutputStream {
	private final ThreadBuffers buffers = ThreadBuffers.of();
	private final int fd;
	private volatile boolean closed = false;

	public static COutputStream of(int fd) {
		return new COutputStream(fd);
	}

	private COutputStream(int fd) {
		this.fd = fd;
	}

	public int bufferSize() {
		return Math.toIntExact(buffers.size());
	}

	public void bufferSize(int size) {
		buffers.size(size);
	}

	@Override
	public void write(int b) throws CException {
		verifyOpen();
		@SuppressWarnings("resource")
		var buffer = buffers.get();
		buffer.setByte(0, (byte) b);
		verifyWrite(CUnistd.write(b, buffer, 1), 1);
	}

	@Override
	public void write(byte[] b, int off, int len) throws CException {
		verifyOpen();
		@SuppressWarnings("resource")
		var buffer = buffers.get();
		int n = CUnistd.writeAll(fd, buffer, JnaUtil.intSize(buffer), b, off, len);
		verifyWrite(n, len);
	}

	@Override
	public void flush() throws CException {
		verifyOpen();
	}

	@Override
	public void close() throws CException {
		closed = true;
		buffers.close();
	}

	private void verifyWrite(int actual, int expected) throws CException {
		if (actual == expected) return;
		throw CException.general("Incomplete write: %d/%d bytes", actual, expected);
	}

	private void verifyOpen() throws CException {
		if (closed) throw CException.of(CError.EBADF, "Closed");
	}
}
