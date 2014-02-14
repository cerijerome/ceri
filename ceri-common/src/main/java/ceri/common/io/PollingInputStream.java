package ceri.common.io;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import ceri.common.util.BasicUtil;

/**
 * An input stream that polls the underlying stream until data is available. This allows I/O to
 * always be interruptible, but at the expense of performance due to polling.
 */
public class PollingInputStream extends FilterInputStream {
	private final long pollingMs;
	private final long timeoutMs;
	private volatile boolean closed = false;

	/**
	 * Constructor with given polling interval and maximum wait time for data to be available.
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
		closed = true;
		super.close();
	}

	private void waitForData(int count) throws IOException {
		if (closed) return;
		long t = System.currentTimeMillis();
		while (true) {
			if (in.available() >= count) return;
			if (System.currentTimeMillis() - t > timeoutMs) throw new IoTimeoutException(
				"No bytes available within " + timeoutMs + "ms");
			BasicUtil.delay(pollingMs);
		}
	}

}
