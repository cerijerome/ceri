package ceri.x10.cm11a;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import ceri.common.io.PollingInputStream;

public class Cm11aTestConnector implements Cm11aConnector {
	private final InputStream is;
	private final PipedInputStream in;
	private final PipedOutputStream out;
	public final DataOutputStream to;
	public final DataInputStream from;

	public Cm11aTestConnector(int pollingMs, int timeoutMs) throws IOException {
		out = new PipedOutputStream();
		is = new PipedInputStream(out);
		from = new DataInputStream(new PollingInputStream(is, pollingMs, timeoutMs));
		in = new PipedInputStream();
		to = new DataOutputStream(new PipedOutputStream(in));
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
		from.close();
		to.close();
	}

}
