package ceri.common.io;

/**
 * A ByteBufferStream that blocks on writing if the buffer exceeds max size, and
 * blocks on reading if not closed and no bytes available. Writing and reading
 * should occur in different threads.
 */
public class BlockingBufferStream extends ByteBufferStream {
	private final int maxSize;

	public BlockingBufferStream() {
		this(0, 0);
	}
	
	public BlockingBufferStream(int initialSize, int maxSize) {
		super(initialSize);
		if (maxSize == 0) maxSize = ByteBufferStream.BUFFER_SIZE_DEFAULT;
		if (maxSize < initialSize) throw new IllegalArgumentException(
			"maxSize cannot be smaller than initalSize");
		this.maxSize = maxSize;
	}

	@Override
	public synchronized void close() {
		super.close();
		notifyAll();
	}

	@Override
	public synchronized void write(int b) {
		if (closed()) return;
		try {
			waitToWrite();
			if (closed()) return;
			super.write(b);
			notifyAll();
		} catch (InterruptedException e) {
			// exit gracefully
		}
	}

	@Override
	public synchronized void write(byte[] b, int off, int len) {
		if (b == null) throw new NullPointerException();
		if (off < 0 || len < 0 || len > b.length - off) throw new IndexOutOfBoundsException();
		if (len == 0) return;
		if (closed()) return;
		try {
			// write what you can first then block
			waitToWrite();
			if (closed()) return;
			int max = availableForWriting();
			if (max < len) {
				write(b, off, max);
				write(b, off + max, len - max);
				return;
			}
			super.write(b, off, len);
			notifyAll();
		} catch (InterruptedException e) {
			// exit gracefully
		}
	}

	@Override
	protected synchronized int read() {
		if (closed() && available() == 0) return -1;
		int read = -1;
		try {
			waitToRead();
			read = super.read();
			notifyAll();
		} catch (InterruptedException e) {
			// exit gracefully
		}
		return read;
	}

	@Override
	protected synchronized int read(byte[] b, int off, int len) {
		if (b == null) throw new NullPointerException();
		if (off < 0 || len < 0 || len > b.length - off) throw new IndexOutOfBoundsException();
		if (len == 0) return 0;
		if (closed() && available() == 0) return -1;
		int read = -1;
		try {
			waitToRead();
			read = super.read(b, off, len);
			notifyAll();
		} catch (InterruptedException e) {
			// exit gracefully
		}
		return read;
	}

	private int availableForWriting() {
		return maxSize - available();
	}

	private void waitToWrite() throws InterruptedException {
		while (!closed() && availableForWriting() == 0) wait();
	}

	private void waitToRead() throws InterruptedException {
		while (!closed() && available() == 0) wait();
	}

}
