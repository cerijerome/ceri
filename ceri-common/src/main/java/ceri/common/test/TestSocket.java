package ceri.common.test;

import static ceri.common.io.IoUtil.IO_ADAPTER;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import ceri.common.net.HostPort;

/**
 * A test socket that delegates to test streams for i/o.
 */
public class TestSocket extends Socket {
	public final CallSync.Accept<HostPort> remote = CallSync.consumer(HostPort.NULL, true);
	public final CallSync.Get<Integer> localPort = CallSync.supplier(0);
	public final TestInputStream in;
	public final TestOutputStream out;

	public static TestSocket of() {
		return new TestSocket();
	}

	private TestSocket() {
		in = TestInputStream.of();
		out = TestOutputStream.of();
	}

	public TestSocket connect(String host, int port) throws IOException {
		remote.accept(HostPort.of(host, port), IO_ADAPTER);
		return this;
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
		return remote.value().port(0);
	}

	@Override
	public int getLocalPort() {
		return localPort.get();
	}

	@Override
	public void close() throws IOException {
		in.close();
		out.close();
	}

}
