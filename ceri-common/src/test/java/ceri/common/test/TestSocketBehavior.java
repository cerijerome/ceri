package ceri.common.test;

import static ceri.common.data.Bytes.toAscii;
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
			Assert.equal(socket.getPort(), 123);
		}
	}

	@Test
	public void shouldProvideLocalPort() throws IOException {
		try (var socket = TestSocket.of()) {
			socket.localPort.autoResponses(456);
			Assert.equal(socket.getLocalPort(), 456);
		}
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldProvideInputStream() throws IOException {
		try (var socket = TestSocket.of()) {
			socket.in.to.writeAscii("test");
			Assert.read(socket.getInputStream(), toAscii("test"));
		}
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldProvideOutputStream() throws IOException {
		try (var socket = TestSocket.of()) {
			socket.getOutputStream().write(toAscii("test").copy(0));
			Assert.read(socket.out.from, toAscii("test"));
		}
	}
}
