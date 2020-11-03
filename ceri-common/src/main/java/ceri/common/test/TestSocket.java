package ceri.common.test;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import ceri.common.net.HostPort;

/**
 * A test socket that delegates to a test streams for i/o.
 */
public class TestSocket extends Socket {
	public final HostPort hostPort;
	private final int localPort;
	public final TestInputStream in;
	public final TestOutputStream out;

	public static TestSocket of() {
		return of(HostPort.localhost(0), 0);
	}

	public static TestSocket of(HostPort hostPort, int localPort) {
		return new TestSocket(hostPort, localPort);
	}

	private TestSocket(HostPort hostPort, int localPort) {
		in = TestInputStream.of();
		out = TestOutputStream.of();
		this.hostPort = hostPort;
		this.localPort = localPort;
	}

	@Override
	public InputStream getInputStream() throws IOException {
		return in;
	}

	@Override
	public OutputStream getOutputStream() throws IOException {
		return out;
	}

	@Override
	public int getPort() {
		return hostPort.port(0);
	}

	@Override
	public int getLocalPort() {
		return localPort;
	}

	@Override
	public void close() throws IOException {
		in.close();
		out.close();
	}
}
