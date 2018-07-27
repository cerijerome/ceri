package ceri.common.io;

import static ceri.common.validation.ValidationUtil.validateMax;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.BufferUnderflowException;
import ceri.common.collection.ArrayUtil;
import ceri.common.text.ToStringHelper;

/**
 * A byte buffer for writing and reading. Buffer grows as necessary during writes. Will throw a
 * BufferUnderflowException if reading from an empty non-closed buffer. If blocking behavior is
 * required use Piped streams.
 */
public class ByteBufferStream extends ByteArrayOutputStream {
	static final int BUFFER_SIZE_DEFAULT = 1024;
	private final InputStream in;
	private boolean closed = false;

	public ByteBufferStream() {
		this(0);
	}

	public ByteBufferStream(int size) {
		super(size == 0 ? BUFFER_SIZE_DEFAULT : size);
		in = new In();
	}

	class In extends InputStream {
		@Override
		public int read() {
			return ByteBufferStream.this.read();
		}

		@Override
		public int read(byte[] b, int off, int len) {
			return ByteBufferStream.this.read(b, off, len);
		}

		@Override
		public int available() {
			return ByteBufferStream.this.available();
		}
	}

	public InputStream asInputStream() {
		return in;
	}

	public boolean closed() {
		return closed;
	}

	@Override
	public String toString() {
		return ToStringHelper.createByClass(this).field("count", count).field("complete", closed)
			.toString();
	}

	@Override
	public void write(int b) {
		if (closed) throw new IllegalStateException("Output is closed.");
		super.write(b);
	}

	@Override
	public void write(byte[] b, int off, int len) {
		if (closed) throw new IllegalStateException("Output is closed.");
		super.write(b, off, len);
	}

	@Override
	public void close() {
		closed = true;
	}

	protected int read() {
		if (available() == 0) {
			if (closed) return -1;
			throw new BufferUnderflowException();
		}
		int value = 0xff & buf[0];
		compact(1);
		return value;
	}

	protected int read(byte[] b, int off, int len) {
		if (b == null) throw new NullPointerException();
		ArrayUtil.validateSlice(b.length, off, len);
		if (len == 0) return 0;
		int available = available();
		if (closed && available == 0) return -1;
		if (available == 0) throw new BufferUnderflowException();
		if (len > available) len = available;
		System.arraycopy(buf, 0, b, off, len);
		compact(len);
		return len;
	}

	protected int available() {
		return count;
	}

	private void compact(int offset) {
		validateMax(offset, count);
		int len = count - offset;
		if (len > 0) System.arraycopy(buf, offset, buf, 0, len);
		count = len;
	}

}
