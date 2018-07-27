package ceri.common.net;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Objects;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class NetUtilTest {
	private @Mock InetAddress address;

	@Before
	public void init() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void testIsRegularAddress() throws IOException {
		assertFalse(NetUtil.isRegularAddress(InetAddress.getByName("0.0.0.0")));
		assertFalse(NetUtil.isRegularAddress(InetAddress.getByName("127.0.0.1")));
		assertFalse(NetUtil.isRegularAddress(InetAddress.getByName("169.254.0.0")));
		assertFalse(NetUtil.isRegularAddress(InetAddress.getByName("224.0.0.0")));
		assertTrue(NetUtil.isRegularAddress(InetAddress.getByName("10.0.0.1")));
	}

	@Test
	public void testLocalAddresses() throws SocketException {
		NetUtil.localAddresses();
		NetUtil.findLocalAddress(Objects::nonNull);
	}

	@Test
	public void testRegularAddress() throws SocketException {
		InetAddress addr = NetUtil.regularAddress();
		assertTrue(NetUtil.isRegularAddress(addr));
		NetworkInterface n = NetUtil.regularInterface();
		addr = NetUtil.regularAddressFor(n);
		assertTrue(NetUtil.isRegularAddress(addr));
	}

}
