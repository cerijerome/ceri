package ceri.log.io;

import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertThrown;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import org.junit.Test;
import ceri.common.event.Listenable;
import ceri.common.io.StateChange;
import ceri.common.net.HostPort;

public class SocketConnectorBehavior {

	@Test
	public void shouldProvideDefaultBroken() throws IOException {
		try (var con = new ExampleConnector()) {
			assertThrown(con::broken);
		}
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldProvideNullImplementation() throws IOException {
		try (var con = SocketConnector.ofNull()) {
			assertEquals(con.hostPort(), HostPort.NULL);
			con.listeners().listen(x -> {});
			con.broken();
			con.connect();
			con.in().read();
			con.out().write(0);
		}
	}

	public static class ExampleConnector implements SocketConnector {

		@Override
		public Listenable<StateChange> listeners() {
			return null;
		}

		@Override
		public void connect() throws IOException {}

		@Override
		public HostPort hostPort() {
			return HostPort.NULL;
		}

		@Override
		public InputStream in() {
			return null;
		}

		@Override
		public OutputStream out() {
			return null;
		}

		@Override
		public void close() throws IOException {}
	}

}
