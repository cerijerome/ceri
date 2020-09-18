package ceri.common.io;

import java.io.Closeable;
import java.io.InputStream;
import java.io.OutputStream;
import ceri.common.data.ByteStream;
import ceri.common.function.FunctionUtil;

/**
 * Provides a connector with piped streams for input and output.
 */
public class PipedConnector implements Closeable {
	private final PipedStream in = PipedStream.of();
	private final PipedStream out = PipedStream.of();
	public final ByteStream.Reader from; // read from source
	public final ByteStream.Writer to; // write to source

	public static PipedConnector of() {
		return new PipedConnector();
	}

	@SuppressWarnings("resource")
	protected PipedConnector() {
		to = ByteStream.writer(in.out());
		from = ByteStream.reader(out.in());
	}

	public void clear() {
		FunctionUtil.execSilently(in::clear);
		FunctionUtil.execSilently(out::clear);
	}

	public InputStream in() {
		return in.in();
	}

	public OutputStream out() {
		return out.out();
	}

	@Override
	public void close() {
		in.close();
		out.close();
	}

}