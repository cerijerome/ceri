package ceri.common.test;

import static ceri.common.data.ByteUtil.toAscii;
import static ceri.common.test.Assert.assertEquals;
import static ceri.common.test.Assert.assertRead;
import java.io.IOException;
import org.junit.Test;
import ceri.common.net.HostPort;

public class TestSocketBehavior {

	@SuppressWarnings("resource")
	@Test
	public void shouldConnect() throws IOException {
		try (var socket = TestSocket.of()) {
			socket.connect("test", 123);
			socket.remote.assertAuto(HostPort.of("test", 123));
			assertEquals(socket.getPort(), 123);
		}
	}

	@Test
	public void shouldProvideLocalPort() throws IOException {
		try (var socket = TestSocket.of()) {
			socket.localPort.autoResponses(456);
			assertEquals(socket.getLocalPort(), 456);
		}
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldProvideInputStream() throws IOException {
		try (var socket = TestSocket.of()) {
			socket.in.to.writeAscii("test");
			assertRead(socket.getInputStream(), toAscii("test"));
		}
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldProvideOutputStream() throws IOException {
		try (var socket = TestSocket.of()) {
			socket.getOutputStream().write(toAscii("test").copy(0));
			assertRead(socket.out.from, toAscii("test"));
		}
	}
}
