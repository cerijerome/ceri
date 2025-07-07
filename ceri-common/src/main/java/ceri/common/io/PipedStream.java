package ceri.common.io;

import static ceri.common.exception.ExceptionAdapter.shouldNotThrow;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import ceri.common.concurrent.ConcurrentUtil;
import ceri.common.function.Excepts.RuntimeCloseable;
import ceri.common.util.CloseableUtil;

/**
 * Creates a paired PipedInputStream and PipedOutputStream.
 */
public class PipedStream implements RuntimeCloseable {
	private static final int DEFAULT_SIZE = 1024;
	private final PipedInputStream in;
	private final PipedOutputStream out;

	/**
	 * Two combined PipedStreams to provide an InputStream and OutputStream, a feed to supply the
	 * InputStream, and a sink to collect the data written to the OutputStream. Typically used to
	 * test device control logic that requires i/o streams to a hardware device.
	 */
	public static class Connector implements RuntimeCloseable {
		public final PipedStream pipedIn;
		public final PipedStream pipedOut;

		private Connector(int inSize, int outSize) {
			pipedIn = PipedStream.of(inSize);
			pipedOut = PipedStream.of(outSize);
		}

		public InputStream in() {
			return pipedIn.in();
		}

		public OutputStream out() {
			return pipedOut.out();
		}

		public OutputStream inFeed() {
			return pipedIn.out();
		}

		public InputStream outSink() {
			return pipedOut.in();
		}

		public void clear() throws IOException {
			pipedIn.clear();
			pipedOut.clear();
		}

		@Override
		public void close() {
			pipedIn.close();
			pipedOut.close();
		}
	}

	public static PipedStream.Connector connector() {
		return connector(DEFAULT_SIZE, DEFAULT_SIZE);
	}

	public static PipedStream.Connector connector(int inSize, int outSize) {
		return new Connector(inSize, outSize);
	}

	public static PipedStream of() {
		return of(DEFAULT_SIZE);
	}

	public static PipedStream of(int size) {
		return new PipedStream(size);
	}

	private PipedStream(int size) {
		out = new PipedOutputStream();
		in = shouldNotThrow.get(() -> new PipedInputStream(out, size));
	}

	public InputStream in() {
		return in;
	}

	public OutputStream out() {
		return out;
	}

	/**
	 * Wait for PipedInputStream to read available bytes.
	 */
	public void awaitRead(int pollMs) throws IOException {
		while (in.available() > 0)
			ConcurrentUtil.delay(pollMs);
	}

	/**
	 * Wait for PipedInputStream to read available bytes. Returns false if timeout exceeded.
	 */
	public boolean awaitRead(int pollMs, int timeoutMs) throws IOException {
		long t = System.currentTimeMillis() + timeoutMs;
		while (true) {
			if (in.available() == 0) return true;
			if (System.currentTimeMillis() >= t) return false;
			ConcurrentUtil.delay(pollMs);
		}
	}

	/**
	 * Clear available bytes.
	 */
	public void clear() throws IOException {
		out.flush();
		IoUtil.clear(in);
	}

	@Override
	public void close() {
		CloseableUtil.close(in, out);
	}
}
