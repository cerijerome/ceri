package ceri.common.data;

import org.junit.Test;
import ceri.common.test.Assert;

public class XorBehavior {

	@Test
	public void shouldXorBytes() {
		Xor xor = new Xor();
		Assert.equal(xor.add(0, 1, 2).value(), (byte) 3);
		ByteProvider data = ByteProvider.of(4, 5);
		Assert.equal(xor.add(data).value(), (byte) 2);
	}

}
