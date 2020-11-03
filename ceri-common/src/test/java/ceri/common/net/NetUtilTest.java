package ceri.common.net;

import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertFalse;
import static ceri.common.test.AssertUtil.assertPrivateConstructor;
import static ceri.common.test.AssertUtil.assertTrue;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Arrays;
import java.util.Objects;
import org.junit.Test;

public class NetUtilTest {

	@Test
	public void testConstructorIsPrivate() {
		assertPrivateConstructor(NetUtil.class);
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
	public void testLocalAddresses() throws SocketException {
		NetUtil.localAddresses();
		NetUtil.findLocalAddress(Objects::nonNull);
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

}
