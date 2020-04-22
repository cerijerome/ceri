package ceri.x10.cm11a;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import ceri.common.data.ByteStream;
import ceri.common.io.PollingInputStream;

public class Cm11aTestConnector implements Cm11aConnector {
	private final PipedInputStream in;
	private final PipedOutputStream out;
	public final ByteStream.Reader from;
	public final ByteStream.Writer to;
	// public final DataOutputStream to;
	// public final DataInputStream from;

	@SuppressWarnings("resource")
	public Cm11aTestConnector(int pollingMs, int timeoutMs) throws IOException {
		out = new PipedOutputStream();
		InputStream is = new PipedInputStream(out);
		from = ByteStream.reader(new PollingInputStream(is, pollingMs, timeoutMs));
		in = new PipedInputStream();
		to = ByteStream.writer(new PipedOutputStream(in));
	}

	@Override
	public InputStream in() {
		return in;
	}

	@Override
	public OutputStream out() {
		return out;
	}

	@Override
	public void close() throws IOException {
		in.close();
		out.close();
	}

}
