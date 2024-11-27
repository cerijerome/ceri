package ceri.common.io;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * An input stream that polls the underlying stream until data is available. This allows I/O to
 * always be interruptible, but at the expense of performance due to polling.
 */
public class PollingInputStream extends FilterInputStream {
	private final long pollingMs;
	private final long timeoutMs;
	private final AtomicBoolean closed = new AtomicBoolean(false);

	/**
	 * Constructor with given polling interval and unlimited wait time for data to be available.
	 */
	public PollingInputStream(InputStream in, long pollingMs) {
		this(in, pollingMs, 0);
	}

	/**
	 * Constructor with given polling interval and maximum wait time for data to be available. Use
	 * timeout of 0 for unlimited wait.
	 */
	public PollingInputStream(InputStream in, long pollingMs, long timeoutMs) {
		super(in);
		this.pollingMs = pollingMs;
		this.timeoutMs = timeoutMs;
	}

	/**
	 * Attempts to read 1 byte within time limit, and throws IoTimeout exception if not available.
	 */
	@Override
	public int read() throws IOException {
		waitForData(1);
		return in.read();
	}

	/**
	 * Attempts to read bytes within time limit, and throws IoTimeout exception if not available.
	 */
	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		waitForData(len);
		return in.read(b, off, len);
	}

	@Override
	public void close() throws IOException {
		if (closed.getAndSet(true)) return;
		super.close();
	}

	private void waitForData(int count) throws IOException {
		if (!closed.get()) IoUtil.pollForData(in, count, timeoutMs, pollingMs);
	}

}
