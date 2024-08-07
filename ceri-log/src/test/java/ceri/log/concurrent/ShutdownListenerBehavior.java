package ceri.log.concurrent;

import static ceri.common.test.AssertUtil.assertThrown;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import org.junit.Test;
import ceri.common.concurrent.ConcurrentUtil;

public class ShutdownListenerBehavior {

	@Test
	public void shouldShutdownWithSocketData() throws IOException {
		try (ShutdownListener shutdown = ShutdownListener.of()) {
			send("x", shutdown.port());
			shutdown.await();
		}
	}

	@Test
	public void shouldManuallyShutdown() throws IOException {
		try (ShutdownListener shutdown = ShutdownListener.of()) {
			shutdown.stop();
			shutdown.await();
		}
	}

	@Test
	public void shouldThrowErrorIfInterrupted() throws IOException {
		try (ShutdownListener shutdown = ShutdownListener.of()) {
			ConcurrentUtil.interrupt();
			assertThrown(() -> shutdown.await());
		}
	}

	private void send(String data, int port) throws IOException {
		try (Socket s = new Socket("localhost", port)) {
			@SuppressWarnings("resource")
			OutputStream out = s.getOutputStream();
			out.write(data.getBytes(StandardCharsets.ISO_8859_1));
			out.flush();
		}
	}
}
