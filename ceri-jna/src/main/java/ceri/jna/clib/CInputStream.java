package ceri.jna.clib;

import java.io.IOException;
import com.sun.jna.Memory;
import ceri.jna.clib.jna.CException;
import ceri.jna.clib.jna.CIoctl;
import ceri.jna.clib.jna.CUnistd;
import ceri.jna.io.JnaInputStream;

/**
 * InputStream for a file descriptor.
 */
public class CInputStream extends JnaInputStream {
	private final int fd;

	public static CInputStream of(int fd) {
		return new CInputStream(fd);
	}

	protected CInputStream(int fd) {
		this.fd = fd;
	}

	@Override
	public int available() throws IOException {
		ensureOpen();
		return CIoctl.fionread(fd);
	}
	
	@Override
	protected int read(Memory buffer, int len) throws IOException {
		return CUnistd.read(fd, buffer, len);
	}

	@Override
	protected void ensureOpen() throws CException {
		if (closed()) throw ErrNo.EBADF.error("Closed");
	}
}
