package ceri.common.net;

import static ceri.common.test.AssertUtil.assertFalse;
import static ceri.common.test.AssertUtil.assertTrue;
import java.io.IOException;
import java.net.InetAddress;
import org.junit.Test;

public class AddressTypeBehavior {

	@Test
	public void testIsSpecial() throws IOException {
		assertTrue(AddressType.isSpecial(InetAddress.getByName("0.0.0.0")));
		assertTrue(AddressType.isSpecial(InetAddress.getByName("127.0.0.1")));
		assertTrue(AddressType.isSpecial(InetAddress.getByName("169.254.0.0")));
		assertTrue(AddressType.isSpecial(InetAddress.getByName("224.0.0.0")));
		assertTrue(AddressType.isSpecial(InetAddress.getByName("225.225.225.225")));
		assertFalse(AddressType.isSpecial(InetAddress.getByName("240.0.0.0")));
		assertFalse(AddressType.isSpecial(InetAddress.getByName("10.0.0.1")));
	}

}
