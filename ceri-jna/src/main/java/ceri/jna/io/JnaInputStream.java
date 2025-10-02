package ceri.jna.io;

import java.io.IOException;
import java.io.InputStream;
import com.sun.jna.Memory;
import ceri.common.util.Validate;
import ceri.jna.util.JnaUtil;
import ceri.jna.util.ThreadBuffers;

/**
 * Base InputStream using thread buffers.
 */
public abstract class JnaInputStream extends InputStream {
	private final ThreadBuffers buffers = ThreadBuffers.of();
	private volatile boolean closed = false;

	/**
	 * Set the transfer buffer size.
	 */
	public void bufferSize(int size) {
		buffers.size(size);
	}

	/**
	 * Get the transfer buffer size.
	 */
	public int bufferSize() {
		return Math.toIntExact(buffers.size());
	}

	@Override
	public int available() throws IOException {
		ensureOpen();
		return super.available();
	}

	@SuppressWarnings("resource")
	@Override
	public int read() throws IOException {
		ensureOpen();
		var buffer = buffers.get();
		int n = read(buffer, 1);
		return n > 0 ? JnaUtil.ubyte(buffer, 0) : -1;
	}

	@SuppressWarnings("resource")
	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		Validate.slice(b.length, off, len);
		ensureOpen();
		if (len == 0) return 0;
		var buffer = buffers.get();
		int n = read(buffer, Math.min(len, JnaUtil.intSize(buffer)));
		JnaUtil.read(buffer, b, off, n);
		return n > 0 ? n : -1;
	}

	@Override
	public void close() {
		closed = true;
		buffers.close();
	}

	/**
	 * Executes a read into the given buffer. Returns the actual number of bytes read.
	 */
	protected abstract int read(Memory buffer, int len) throws IOException;

	/**
	 * Ensures the stream is currently open.
	 */
	protected void ensureOpen() throws IOException {
		if (closed()) throw new IOException("Closed");
	}

	/**
	 * Returns true if the stream has been closed.
	 */
	protected final boolean closed() {
		return closed;
	}
}
