package ceri.common.net;

import static ceri.common.test.TestUtil.assertPrivateConstructor;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;
import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Arrays;
import java.util.Objects;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

public class NetUtilTest {
	private static InetAddress address;

	@BeforeClass
	public static void beforeClass() {
		address = Mockito.mock(InetAddress.class);
	}

	@Before
	public void before() {
		Mockito.reset(address); // to reduce testing time
	}

	@Test
	public void testConstructorIsPrivate() {
		assertPrivateConstructor(NetUtil.class);
	}

	@Test
	public void testIsLocalhost() {
		for (String address : Arrays.asList("localhost", "Localhost", "127.0.0.1", "127.1",
			"127.255.255.255", "::1", "0:0:0:0:0:0:0:1", "0000::0001", "00:01"))
			assertThat(address, NetUtil.isLocalhost(address), is(true));
		for (String address : Arrays.asList(null, "", "local_host", "128.0.0.1", "127", "0.0.0.0",
			"::0", "0:0:0:0:0:0:1:1", "0000::0000"))
			assertThat(address, NetUtil.isLocalhost(address), is(false));
	}

	@Test
	public void testIsRegularAddressForSpecialAddresses() throws IOException {
		assertFalse(NetUtil.isRegularAddress(InetAddress.getByName("0.0.0.0")));
		assertFalse(NetUtil.isRegularAddress(InetAddress.getByName("127.0.0.1")));
		assertFalse(NetUtil.isRegularAddress(InetAddress.getByName("169.254.0.0")));
		assertFalse(NetUtil.isRegularAddress(InetAddress.getByName("224.0.0.0")));
		assertTrue(NetUtil.isRegularAddress(InetAddress.getByName("10.0.0.1")));
	}

	@Test
	public void testIsRegularAddressForAnyLocal() {
		when(address.isSiteLocalAddress()).thenReturn(true);
		when(address.isAnyLocalAddress()).thenReturn(true);
		assertFalse(NetUtil.isRegularAddress(address));
	}

	@Test
	public void testIsRegularAddressForLinkLocal() {
		when(address.isSiteLocalAddress()).thenReturn(true);
		when(address.isLinkLocalAddress()).thenReturn(true);
		assertFalse(NetUtil.isRegularAddress(address));
	}

	@Test
	public void testIsRegularAddressForLoopback() {
		when(address.isSiteLocalAddress()).thenReturn(true);
		when(address.isLoopbackAddress()).thenReturn(true);
		assertFalse(NetUtil.isRegularAddress(address));
	}

	@Test
	public void testIsRegularAddressForMulticast() {
		when(address.isSiteLocalAddress()).thenReturn(true);
		when(address.isMulticastAddress()).thenReturn(true);
		assertFalse(NetUtil.isRegularAddress(address));
	}

	@Test
	public void testLocalAddresses() throws SocketException {
		NetUtil.localAddresses();
		NetUtil.findLocalAddress(Objects::nonNull);
	}

	@Test
	public void testRegularAddress() throws SocketException {
		InetAddress addr = NetUtil.regularAddress();
		if (addr == null) return; // not connected
		assertTrue(NetUtil.isRegularAddress(addr));
		NetworkInterface n = NetUtil.regularInterface();
		addr = NetUtil.regularAddressFor(n);
		assertTrue(NetUtil.isRegularAddress(addr));
	}

}
