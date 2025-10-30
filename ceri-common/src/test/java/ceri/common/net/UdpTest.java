package ceri.common.net;

import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import org.junit.Test;
import ceri.common.data.Bytes;
import ceri.common.test.Assert;

public class UdpTest {

	@Test
	public void testConstructorIsPrivate() {
		Assert.privateConstructor(Udp.class);
	}

	@Test
	public void testHostPort() throws IOException {
		var address = InetAddress.getLocalHost();
		try (var socket = TestDatagramSocket.of()) {
			Assert.isNull(Udp.hostPort(null));
			socket.getInetAddress.autoResponse(() -> null);
			Assert.thrown(() -> Udp.hostPort(socket));
			socket.getInetAddress.autoResponses(address);
			socket.getPort.autoResponses(777);
			HostPortBehavior.assertHostPort(Udp.hostPort(socket), address.getHostAddress(),
				777);
		}
	}

	@Test
	public void testToPacket() throws UnknownHostException {
		var address = InetAddress.getLocalHost();
		var packet = Udp.toPacket(Bytes.toAscii("test"), address, 0);
		Assert.array(packet.getData(), 't', 'e', 's', 't');
	}

	@Test
	public void testReceiveWithTimeout() throws IOException {
		byte[] buffer = new byte[100];
		try (var socket = TestDatagramSocket.of()) {
			socket.receive.error.setFrom(SocketTimeoutException::new);
			Assert.isNull(Udp.receive(socket, buffer));
		}
	}

	@Test
	public void testReceive() throws IOException {
		byte[] buffer = new byte[100];
		try (var socket = TestDatagramSocket.of()) {
			var data = Udp.receive(socket, buffer);
			Assert.notNull(data);
			Assert.array(data.copy(0), buffer);
		}
	}
}
