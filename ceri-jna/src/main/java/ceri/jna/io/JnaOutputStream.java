package ceri.jna.io;

import java.io.IOException;
import java.io.OutputStream;
import com.sun.jna.Memory;
import ceri.common.io.IoExceptions;
import ceri.common.util.Validate;
import ceri.jna.util.JnaUtil;
import ceri.jna.util.ThreadBuffers;

/**
 * Base OutputStream using thread buffers.
 */
public abstract class JnaOutputStream extends OutputStream {
	private final ThreadBuffers buffers = ThreadBuffers.of();
	private volatile boolean closed = false;

	public int bufferSize() {
		return Math.toIntExact(buffers.size());
	}

	public void bufferSize(int size) {
		buffers.size(size);
	}

	@SuppressWarnings("resource")
	@Override
	public void write(int b) throws IOException {
		ensureOpen();
		var buffer = buffers.get();
		buffer.setByte(0, (byte) b);
		verifyWrite(write(buffer, 1), 1);
	}

	@SuppressWarnings("resource")
	@Override
	public void write(byte[] b, int off, int len) throws IOException {
		Validate.validateSlice(b.length, off, len);
		ensureOpen();
		verifyWrite(writeAll(buffers.get(), b, off, len), len);
	}

	@Override
	public void flush() throws IOException {
		ensureOpen();
		flushBytes();
	}

	@Override
	public void close() {
		closed = true;
		buffers.close();
	}

	/**
	 * Executes a write from the given buffer. Returns the actual number of bytes written.
	 */
	protected abstract int write(Memory buffer, int len) throws IOException;

	/**
	 * Flushes the stream. Called only after verifying the stream is open.
	 */
	protected void flushBytes() throws IOException {
		super.flush();
	}

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

	private int writeAll(Memory buffer, byte[] b, int off, int len) throws IOException {
		int rem = len;
		while (rem > 0) {
			int n = Math.min(rem, JnaUtil.intSize(buffer));
			JnaUtil.write(buffer, b, off, n);
			int m = writeBlock(buffer, n);
			off += m;
			rem -= m;
			if (m < n) break;
		}
		return len - rem;
	}

	private int writeBlock(Memory buffer, int len) throws IOException {
		int off = 0;
		while (off < len) {
			@SuppressWarnings("resource")
			int m = write(JnaUtil.share(buffer, off), len - off);
			if (m <= 0) break;
			off += m;
		}
		return Math.min(off, len);
	}

	private void verifyWrite(int actual, int expected) throws IOException {
		IoExceptions.Incomplete.verify(actual, expected);
	}
}
