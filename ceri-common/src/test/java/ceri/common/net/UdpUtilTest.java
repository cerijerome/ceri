package ceri.common.net;

import static ceri.common.test.TestUtil.assertArray;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import ceri.common.collection.ImmutableByteArray;
import ceri.common.data.ByteUtil;

public class UdpUtilTest {
	private @Mock DatagramSocket socket;
	private @Mock InetAddress address;
	private byte[] buffer;

	@Before
	public void init() {
		MockitoAnnotations.initMocks(this);
		buffer = new byte[100];
	}

	@Test
	public void testToPacket() {
		DatagramPacket packet = UdpUtil.toPacket(ByteUtil.toAscii("test"), address, 0);
		assertArray(packet.getData(), 't', 'e', 's', 't');
	}

	@Test
	public void testReceiveWithTimeout() throws IOException {
		doThrow(SocketTimeoutException.class).when(socket).receive(any());
		assertNull(UdpUtil.receive(socket, buffer));
	}

	@Test
	public void testReceive() throws IOException {
		ImmutableByteArray data = UdpUtil.receive(socket, buffer);
		assertArray(data.copy(), buffer);
	}

}
