package ceri.common.net;

import java.io.IOException;
import java.net.InetAddress;
import org.junit.Test;
import ceri.common.test.Assert;

public class AddressTypeBehavior {

	@Test
	public void testIsSpecial() throws IOException {
		Assert.yes(AddressType.isSpecial(InetAddress.getByName("0.0.0.0")));
		Assert.yes(AddressType.isSpecial(InetAddress.getByName("127.0.0.1")));
		Assert.yes(AddressType.isSpecial(InetAddress.getByName("169.254.0.0")));
		Assert.yes(AddressType.isSpecial(InetAddress.getByName("224.0.0.0")));
		Assert.yes(AddressType.isSpecial(InetAddress.getByName("225.225.225.225")));
		Assert.no(AddressType.isSpecial(InetAddress.getByName("240.0.0.0")));
		Assert.no(AddressType.isSpecial(InetAddress.getByName("10.0.0.1")));
	}

}
