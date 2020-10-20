package ceri.log.io;

import static ceri.common.test.AssertUtil.assertRead;
import static ceri.common.test.TestUtil.threadRun;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import ceri.common.collection.ArrayUtil;
import ceri.common.net.HostPort;
import ceri.common.test.CallSync;
import ceri.common.test.TestPipedConnector;

public class SelfHealingSocketConnectorBehavior {
	private static CallSync.Accept<HostPort> open = CallSync.consumer(null, false);
	private static TestSocket socket;
	private static SelfHealingSocketConnector con;

	@BeforeClass
	public static void beforeClass() throws InterruptedException {
		socket = new TestSocket();
		SelfHealingSocketConfig config = SelfHealingSocketConfig.builder("test", 123)
			.factory((host, port) -> openSocket(host, port)).build();
		con = SelfHealingSocketConnector.of(config);
		// Make sure connector has opened socket
		try (var exec = threadRun(con::connect)) {
			open.assertCall(HostPort.of("test", 123));
			exec.get();
		}
	}

	@Before
	public void before() {
		open.reset();
		socket.con.reset(true);
	}

	@AfterClass
	public static void afterClass() throws IOException {
		con.close();
		socket.close();
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldWriteData() throws IOException {
		con.out().write(ArrayUtil.bytes(1, 2, 3));
		assertRead(socket.con.from, 1, 2, 3);
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldReadData() throws IOException {
		socket.con.to.writeBytes(1, 2, 3);
		assertRead(con.in(), 1, 2, 3);
	}

	private static Socket openSocket(String host, int port) {
		HostPort hostPort = HostPort.of(host, port);
		open.accept(hostPort);
		return socket;
	}

	private static class TestSocket extends Socket {
		public final TestPipedConnector con;

		public TestSocket() {
			con = TestPipedConnector.of();
		}

		@Override
		public InputStream getInputStream() throws IOException {
			return con.in();
		}

		@Override
		public OutputStream getOutputStream() throws IOException {
			return con.out();
		}

		@Override
		public synchronized void close() throws IOException {
			con.close();
			super.close();
		}
	}
}
