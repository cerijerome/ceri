package ceri.common.io;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import ceri.common.util.Validate;

/**
 * A buffered output stream. The same as java.io.BufferedOutputStream, except the buffer will be
 * emptied if an exception occurs during flushing. This prevents data from accumulating when the
 * wrapped stream is in a bad state.
 */
public class BufferedOutputStream extends FilterOutputStream {
	private static final int BUFFER_SIZE_DEF = 8192;
	private byte buffer[];
	private int count;

	public BufferedOutputStream(OutputStream out) {
		this(out, BUFFER_SIZE_DEF);
	}

	public BufferedOutputStream(OutputStream out, int size) {
		super(out);
		Validate.min(size, 0);
		buffer = new byte[size];
	}

	@Override
	public synchronized void write(int b) throws IOException {
		if (count >= buffer.length) flushBuffer();
		buffer[count++] = (byte) b;
	}

	@Override
	public synchronized void write(byte b[], int off, int len) throws IOException {
		if (len > buffer.length - count) flushBuffer();
		if (len >= buffer.length) out.write(b, off, len); // write directly
		else copyToBuffer(b, off, len);
	}

	@Override
	public synchronized void flush() throws IOException {
		flushBuffer();
		out.flush();
	}

	private void copyToBuffer(byte[] b, int off, int len) {
		System.arraycopy(b, off, buffer, count, len);
		count += len;
	}

	private void flushBuffer() throws IOException {
		int n = count;
		count = 0; // buffer always emptied, even if exception on write
		if (n > 0) out.write(buffer, 0, n);
	}
}
