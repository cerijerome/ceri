package ceri.common.data;

import static ceri.common.test.Assert.assertEquals;
import org.junit.Test;

public class XorBehavior {

	@Test
	public void shouldXorBytes() {
		Xor xor = new Xor();
		assertEquals(xor.add(0, 1, 2).value(), (byte) 3);
		ByteProvider data = ByteProvider.of(4, 5);
		assertEquals(xor.add(data).value(), (byte) 2);
	}

}
