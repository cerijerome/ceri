package ceri.common.net;

import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertFalse;
import static ceri.common.test.AssertUtil.assertPrivateConstructor;
import static ceri.common.test.AssertUtil.assertThrown;
import static ceri.common.test.AssertUtil.assertTrue;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Objects;
import org.junit.Test;

public class NetUtilTest {

	@Test
	public void testConstructorIsPrivate() {
		assertPrivateConstructor(NetUtil.class);
	}

	@Test
	public void testUrl() {
		NetUtil.url("http://example.com");
		NetUtil.url("https://example");
		assertThrown(IllegalArgumentException.class, () -> NetUtil.url("https://"));
	}

	@Test
	public void testRequireResolved() throws UnknownHostException {
		assertThrown(
			() -> NetUtil.requireResolved(InetSocketAddress.createUnresolved("localhost", 0)));
		NetUtil.requireResolved(new InetSocketAddress("localhost", 0));
	}

	@Test
	public void testIsLocalhost() {
		for (String address : Arrays.asList("localhost", "Localhost", "127.0.0.1", "127.1",
			"127.255.255.255", "::1", "0:0:0:0:0:0:0:1", "0000::0001", "00:01"))
			assertEquals(NetUtil.isLocalhost(address), true, address);
		for (String address : Arrays.asList(null, "", "local_host", "128.0.0.1", "127", "0.0.0.0",
			"::0", "0:0:0:0:0:0:1:1", "0000::0000"))
			assertEquals(NetUtil.isLocalhost(address), false, address);
	}

	@Test
	public void testLocalAddressFor() {
		assertEquals(NetUtil.localAddressFor(null), null);
	}

	@Test
	public void testLocalAddresses() throws SocketException {
		NetUtil.localAddress();
		NetUtil.localAddresses();
		NetUtil.findLocalAddress(Objects::nonNull);
	}

	@Test
	public void testHasLocalAddress() throws SocketException {
		NetworkInterface.networkInterfaces().iterator().forEachRemaining(NetUtil::hasLocalAddress);
	}

	@Test
	public void testLocalInterfaceAddresses() throws SocketException {
		NetUtil.localInterface();
		var iface = NetworkInterface.networkInterfaces().findFirst().orElse(null);
		NetUtil.localIp4AddressFor(iface);
	}

	@Test
	public void testRegularIp4Address() throws SocketException {
		Inet4Address addr = NetUtil.localIp4Address();
		if (addr == null) return; // not connected
		assertTrue(addr.isSiteLocalAddress());
		assertFalse(AddressType.isSpecial(addr));
		NetworkInterface n = NetUtil.localInterface();
		addr = NetUtil.localIp4AddressFor(n);
		assertTrue(addr.isSiteLocalAddress());
		assertFalse(AddressType.isSpecial(addr));
	}

	@Test
	public void testRegularAddress() throws SocketException {
		InetAddress addr = NetUtil.localAddress();
		if (addr == null) return; // not connected
		assertTrue(addr.isSiteLocalAddress());
		assertFalse(AddressType.isSpecial(addr));
		NetworkInterface n = NetUtil.localInterface();
		addr = NetUtil.localAddressFor(n);
		assertTrue(addr.isSiteLocalAddress());
		assertFalse(AddressType.isSpecial(addr));
	}

	@Test
	public void testBroadcast() throws SocketException {
		NetUtil.localBroadcast();
		assertEquals(NetUtil.broadcast(null), null);
		NetUtil.broadcast(NetworkInterface.networkInterfaces().findFirst().orElse(null));
	}

}
