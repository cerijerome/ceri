package ceri.jna.io;

import static ceri.common.collection.ArrayUtil.validateRange;
import java.io.IOException;
import java.io.OutputStream;
import com.sun.jna.Memory;
import ceri.common.io.IncompleteTransferException;
import ceri.jna.util.JnaUtil;
import ceri.jna.util.ThreadBuffers;

/**
 * OutputStream for a file descriptor.
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
		validateRange(b.length, off, len);
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

	protected abstract int write(Memory buffer, int len) throws IOException;

	protected void flushBytes() throws IOException {
		super.flush();	
	}
	
	protected void ensureOpen() throws IOException {
		if (closed()) throw new IOException("Closed");
	}

	protected final boolean closed() {
		return closed;
	}

	private int writeAll(Memory buffer, byte[] b, int off, int len) throws IOException {
		int size = JnaUtil.intSize(buffer);
		int rem = len;
		while (rem > 0) {
			int n = Math.min(rem, size);
			JnaUtil.write(buffer, b, off, n);
			int m = write(buffer, n);
			off += m;
			rem -= m;
			if (m < n) break;
		}
		return len - rem;
	}

	private void verifyWrite(int actual, int expected) throws IOException {
		IncompleteTransferException.verify(actual, expected);
	}
}
