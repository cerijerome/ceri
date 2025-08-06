package ceri.common.net;

import static ceri.common.test.AssertUtil.assertArray;
import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertFalse;
import static ceri.common.test.AssertUtil.assertTrue;
import java.io.IOException;
import java.net.InetAddress;
import org.junit.Test;
import ceri.common.array.ArrayUtil;
import ceri.common.data.ByteArray;
import ceri.common.data.ByteProvider;
import ceri.common.reflect.Reflect;

public class UdpChannelBehavior {

	@Test
	public void shouldReturnEmptyIfNoDatagram() throws IOException {
		if (!isNetworkAvailable()) return;
		try (var udp = UdpChannel.of(0)) {
			udp.blocking(false);
			var received = udp.receive(5);
			assertEquals(received.address(), null);
			assertArray(received.bytes());
		}
	}

	@Test
	public void shouldUnicastDatagram() throws IOException {
		if (!isNetworkAvailable()) return;
		try (var udp0 = UdpChannel.of(0); var udp1 = UdpChannel.of(0)) {
			udp0.unicast(udp1.port, ByteProvider.of(1, 2, 3));
			assertArray(udp1.select(5).bytes(), 1, 2, 3);
			udp1.send("localhost", udp0.port, ByteArray.Mutable.wrap(3, 2, 1));
			assertArray(udp0.select(5).bytes(), 3, 2, 1);
			udp0.send(NetUtil.localAddress(), udp1.port, ByteProvider.of(1, 3, 2));
			assertArray(udp1.select(5).bytes(), 1, 3, 2);

		}
	}

	@Test
	public void shouldBroadcastDatagram() throws IOException {
		if (!isNetworkAvailable()) return;
		try (var udp0 = UdpChannel.of(0); var udp1 = UdpChannel.of(0)) {
			udp0.broadcast(udp1.port, ByteProvider.of(4, 5, 6));
			assertArray(udp1.select(5).bytes(), 4, 5, 6);
		}
	}

	@Test
	public void shouldJoinMulticastGroup() throws IOException {
		if (!isNetworkAvailable()) return;
		var multicastAddress = InetAddress.getByAddress(ArrayUtil.bytes.of(239, 255, 1, 1));
		try (var udp0 = UdpChannel.of(0); var udp1 = UdpChannel.of(0)) {
			assertTrue(udp1.join(multicastAddress));
			assertFalse(udp1.join(multicastAddress)); // already joined
			udp0.send(multicastAddress, udp1.port, ByteProvider.of(1, 2, 3));
			assertArray(udp1.receive(3).bytes(), 1, 2, 3);
			assertTrue(udp1.drop(multicastAddress));
			assertFalse(udp1.drop(multicastAddress)); // already dropped
		}
	}

	private static boolean isNetworkAvailable() throws IOException {
		if (NetUtil.localInterface() != null) return true;
		var caller = Reflect.previousCaller(1);
		System.err.printf("Network unavailable for test: %s.%s\n", caller.cls, caller.method);
		return false;
	}
}
