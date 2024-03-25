package ceri.jna.clib;

import java.io.IOException;
import com.sun.jna.Memory;
import ceri.jna.clib.jna.CException;
import ceri.jna.clib.jna.CIoctl;
import ceri.jna.clib.jna.CUnistd;
import ceri.jna.io.JnaOutputStream;

/**
 * OutputStream for a file descriptor.
 */
public class COutputStream extends JnaOutputStream {
	private final int fd;

	public static COutputStream of(int fd) {
		return new COutputStream(fd);
	}

	protected COutputStream(int fd) {
		this.fd = fd;
	}

	/**
	 * Returns the number of bytes in the output queue.
	 */
	public int queued() throws IOException {
		return CIoctl.tiocoutq(fd);
	}

	@Override
	protected int write(Memory buffer, int len) throws IOException {
		return CUnistd.write(fd, buffer, len);
	}

	@Override
	protected void ensureOpen() throws CException {
		if (closed()) throw ErrNo.EBADF.error("Closed");
	}
}
