package ceri.common.net;

import java.io.IOException;
import java.net.InetAddress;
import org.junit.After;
import org.junit.Test;
import ceri.common.array.Array;
import ceri.common.data.ByteArray;
import ceri.common.data.ByteProvider;
import ceri.common.reflect.Reflect;
import ceri.common.test.Assert;
import ceri.common.test.Testing;

public class UdpChannelBehavior {
	private UdpChannel udp0 = null;
	private UdpChannel udp1 = null;

	@After
	public void after() {
		udp1 = Testing.close(udp1);
		udp0 = Testing.close(udp0);
	}

	@Test
	public void shouldReturnEmptyIfNoDatagram() throws IOException {
		if (!isNetworkAvailable()) return;
		udp0 = UdpChannel.of(0);
		udp0.blocking(false);
		var received = udp0.receive(5);
		Assert.equal(received.address(), null);
		Assert.array(received.bytes());
	}

	@Test
	public void shouldUnicastDatagram() throws IOException {
		if (!isNetworkAvailable()) return;
		udp0 = UdpChannel.of(0);
		udp1 = UdpChannel.of(0);
		udp0.unicast(udp1.port, ByteProvider.of(1, 2, 3));
		Assert.array(udp1.select(5).bytes(), 1, 2, 3);
		udp1.send("localhost", udp0.port, ByteArray.Mutable.wrap(3, 2, 1));
		Assert.array(udp0.select(5).bytes(), 3, 2, 1);
		udp0.send(Net.localAddress(), udp1.port, ByteProvider.of(1, 3, 2));
		Assert.array(udp1.select(5).bytes(), 1, 3, 2);
	}

	@Test
	public void shouldBroadcastDatagram() throws IOException {
		if (!isNetworkAvailable()) return;
		udp0 = UdpChannel.of(0);
		udp1 = UdpChannel.of(0);
		udp0.broadcast(udp1.port, ByteProvider.of(4, 5, 6));
		Assert.array(udp1.select(5).bytes(), 4, 5, 6);
	}

	@Test
	public void shouldJoinMulticastGroup() throws IOException {
		if (!isNetworkAvailable()) return;
		var multicastAddress = InetAddress.getByAddress(Array.BYTE.of(239, 255, 1, 1));
		udp0 = UdpChannel.of(0);
		udp1 = UdpChannel.of(0);
		Assert.yes(udp1.join(multicastAddress));
		Assert.no(udp1.join(multicastAddress)); // already joined
		udp0.send(multicastAddress, udp1.port, ByteProvider.of(1, 2, 3));
		Assert.array(udp1.receive(3).bytes(), 1, 2, 3);
		Assert.yes(udp1.drop(multicastAddress));
		Assert.no(udp1.drop(multicastAddress)); // already dropped
	}

	@Test
	public void shouldProvideStringRepresentation() throws IOException {
		if (!isNetworkAvailable()) return;
		udp0 = UdpChannel.of(0);
		Assert.find(udp0, "\\Q%s\\E", udp0.port);
	}

	private static boolean isNetworkAvailable() throws IOException {
		if (Net.localInterface() != null) return true;
		var caller = Reflect.previousCaller(1);
		System.err.printf("Network unavailable for test: %s.%s\n", caller.cls, caller.method);
		return false;
	}
}
