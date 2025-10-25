package ceri.common.net;

import static ceri.common.test.Assert.assertArray;
import static ceri.common.test.Assert.assertPrivateConstructor;
import static java.util.Objects.requireNonNull;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import org.junit.Test;
import ceri.common.data.ByteProvider;
import ceri.common.data.ByteUtil;
import ceri.common.test.Assert;

public class UdpUtilTest {

	@Test
	public void testConstructorIsPrivate() {
		assertPrivateConstructor(UdpUtil.class);
	}

	@Test
	public void testHostPort() throws IOException {
		InetAddress address = InetAddress.getLocalHost();
		try (TestDatagramSocket socket = TestDatagramSocket.of()) {
			Assert.isNull(UdpUtil.hostPort(null));
			socket.getInetAddress.autoResponse(() -> null);
			Assert.thrown(() -> UdpUtil.hostPort(socket));
			socket.getInetAddress.autoResponses(address);
			socket.getPort.autoResponses(777);
			HostPortBehavior.assertHostPort(UdpUtil.hostPort(socket), address.getHostAddress(),
				777);
		}
	}

	@Test
	public void testToPacket() throws UnknownHostException {
		InetAddress address = InetAddress.getLocalHost();
		DatagramPacket packet = UdpUtil.toPacket(ByteUtil.toAscii("test"), address, 0);
		assertArray(packet.getData(), 't', 'e', 's', 't');
	}

	@Test
	public void testReceiveWithTimeout() throws IOException {
		byte[] buffer = new byte[100];
		try (TestDatagramSocket socket = TestDatagramSocket.of()) {
			socket.receive.error.setFrom(SocketTimeoutException::new);
			Assert.isNull(UdpUtil.receive(socket, buffer));
		}
	}

	@Test
	public void testReceive() throws IOException {
		byte[] buffer = new byte[100];
		try (TestDatagramSocket socket = TestDatagramSocket.of()) {
			ByteProvider data = UdpUtil.receive(socket, buffer);
			requireNonNull(data);
			assertArray(data.copy(0), buffer);
		}
	}

}
