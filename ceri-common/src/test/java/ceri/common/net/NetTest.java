package ceri.common.net;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Objects;
import org.junit.Test;
import ceri.common.test.Assert;

public class NetTest {

	@Test
	public void testConstructorIsPrivate() {
		Assert.privateConstructor(Net.class);
	}

	@Test
	public void testUrl() {
		Net.url("http://example.com");
		Net.url("https://example");
		Assert.thrown(IllegalArgumentException.class, () -> Net.url("https://"));
	}

	@Test
	public void testRequireResolved() throws UnknownHostException {
		Assert.thrown(
			() -> Net.requireResolved(InetSocketAddress.createUnresolved("localhost", 0)));
		Net.requireResolved(new InetSocketAddress("localhost", 0));
	}

	@Test
	public void testIsLocalhost() {
		for (String address : Arrays.asList("localhost", "Localhost", "127.0.0.1", "127.1",
			"127.255.255.255", "::1", "0:0:0:0:0:0:0:1", "0000::0001", "00:01"))
			Assert.equal(Net.isLocalhost(address), true, address);
		for (String address : Arrays.asList(null, "", "local_host", "128.0.0.1", "127", "0.0.0.0",
			"::0", "0:0:0:0:0:0:1:1", "0000::0000"))
			Assert.equal(Net.isLocalhost(address), false, address);
	}

	@Test
	public void testLocalAddressFor() {
		Assert.equal(Net.localAddressFor(null), null);
	}

	@Test
	public void testLocalAddresses() throws SocketException {
		Net.localAddress();
		Net.localAddresses();
		Net.findLocalAddress(Objects::nonNull);
	}

	@Test
	public void testHasLocalAddress() throws SocketException {
		NetworkInterface.networkInterfaces().iterator().forEachRemaining(Net::hasLocalAddress);
	}

	@Test
	public void testLocalInterfaceAddresses() throws SocketException {
		Net.localInterface();
		var iface = NetworkInterface.networkInterfaces().findFirst().orElse(null);
		Net.localIp4AddressFor(iface);
	}

	@Test
	public void testRegularIp4Address() throws SocketException {
		Inet4Address addr = Net.localIp4Address();
		if (addr == null) return; // not connected
		Assert.yes(addr.isSiteLocalAddress());
		Assert.no(AddressType.isSpecial(addr));
		NetworkInterface n = Net.localInterface();
		addr = Net.localIp4AddressFor(n);
		Assert.yes(addr.isSiteLocalAddress());
		Assert.no(AddressType.isSpecial(addr));
	}

	@Test
	public void testRegularAddress() throws SocketException {
		InetAddress addr = Net.localAddress();
		if (addr == null) return; // not connected
		Assert.yes(addr.isSiteLocalAddress());
		Assert.no(AddressType.isSpecial(addr));
		NetworkInterface n = Net.localInterface();
		addr = Net.localAddressFor(n);
		Assert.yes(addr.isSiteLocalAddress());
		Assert.no(AddressType.isSpecial(addr));
	}

	@Test
	public void testBroadcast() throws SocketException {
		Net.localBroadcast();
		Assert.equal(Net.broadcast(null), null);
		Net.broadcast(NetworkInterface.networkInterfaces().findFirst().orElse(null));
	}

}
