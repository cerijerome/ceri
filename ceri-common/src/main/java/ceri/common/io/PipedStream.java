package ceri.common.io;

import static ceri.common.exception.ExceptionUtil.shouldNotThrow;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

/**
 * Creates a paired PipedInputStream and PipedOutputStream.
 */
public class PipedStream implements Closeable {
	private final PipedInputStream in;
	private final PipedOutputStream out;

	public static PipedStream of() {
		return new PipedStream();
	}

	private PipedStream() {
		out = new PipedOutputStream();
		in = shouldNotThrow(() -> new PipedInputStream(out));
	}

	public InputStream in() {
		return in;
	}

	public OutputStream out() {
		return out;
	}

	public void clear() throws IOException {
		IoUtil.clear(in);
	}

	@Override
	public void close() {
		IoUtil.close(in);
		IoUtil.close(out);
	}
}
