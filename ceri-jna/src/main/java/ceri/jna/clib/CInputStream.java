package ceri.jna.clib;

import java.io.IOException;
import java.io.InputStream;
import ceri.jna.clib.jna.CError;
import ceri.jna.clib.jna.CException;
import ceri.jna.clib.jna.CIoctl;
import ceri.jna.clib.jna.CUnistd;
import ceri.jna.util.JnaUtil;
import ceri.jna.util.ThreadBuffers;

/**
 * InputStream for a file descriptor.
 */
public class CInputStream extends InputStream {
	private final ThreadBuffers buffers = ThreadBuffers.of();
	private final int fd;
	private volatile boolean closed = false;

	public static CInputStream of(int fd) {
		return new CInputStream(fd);
	}

	protected CInputStream(int fd) {
		this.fd = fd;
	}

	public int bufferSize() {
		return Math.toIntExact(buffers.size());
	}

	public void bufferSize(int size) {
		buffers.size(size);
	}

	@Override
	public int available() throws IOException {
		verifyOpen();
		return CIoctl.fionread(fd);
	}

	@SuppressWarnings("resource")
	@Override
	public int read() throws IOException {
		verifyOpen();
		var buffer = buffers.get();
		int n = CUnistd.read(fd, buffer, 1);
		return n > 0 ? JnaUtil.ubyte(buffer, 0) : -1;
	}

	@SuppressWarnings("resource")
	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		verifyOpen();
		if (len == 0) return 0;
		var buffer = buffers.get();
		len = (int) Math.min(buffer.size(), len);
		int n = CUnistd.read(fd,  buffer, b, off, len);
		return n > 0 ? n : -1;
	}

	@Override
	public void close() {
		closed = true;
		buffers.close();
	}

	private void verifyOpen() throws CException {
		if (closed) throw CException.of(CError.EBADF, "Closed");
	}
}
