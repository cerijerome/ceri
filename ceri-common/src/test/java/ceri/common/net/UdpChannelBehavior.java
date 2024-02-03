package ceri.common.net;

import static ceri.common.test.AssertUtil.assertArray;
import static ceri.common.test.AssertUtil.assertEquals;
import java.io.IOException;
import org.junit.Test;
import ceri.common.data.ByteArray;
import ceri.common.data.ByteProvider;

public class UdpChannelBehavior {

	@Test
	public void shouldReturnEmptyIfNoDatagram() throws IOException {
		try (var udp = UdpChannel.of(0)) {
			udp.blocking(false);
			var received = udp.receive(5);
			assertEquals(received.address(), null);
			assertArray(received.bytes());
		}
	}

	@Test
	public void shouldUnicastDatagram() throws IOException {
		try (var udp0 = UdpChannel.of(0); var udp1 = UdpChannel.of(0)) {
			udp0.unicast(udp1.port, ByteProvider.of(1, 2, 3));
			assertArray(udp1.select(5).bytes(), 1, 2, 3);
			udp1.unicast("localhost", udp0.port, ByteArray.Mutable.wrap(3, 2, 1));
			assertArray(udp0.select(5).bytes(), 3, 2, 1);
			udp0.unicast(NetUtil.localAddress(), udp1.port, ByteProvider.of(1, 3, 2));
			assertArray(udp1.select(5).bytes(), 1, 3, 2);

		}
	}

	@Test
	public void shouldBroadcastDatagram() throws IOException {
		try (var udp0 = UdpChannel.of(0); var udp1 = UdpChannel.of(0)) {
			udp0.broadcast(udp1.port, ByteProvider.of(4, 5, 6));
			assertArray(udp1.select(5).bytes(), 4, 5, 6);
		}
	}

}
