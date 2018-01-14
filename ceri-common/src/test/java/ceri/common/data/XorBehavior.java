package ceri.common.data;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import org.junit.Test;
import ceri.common.collection.ImmutableByteArray;

public class XorBehavior {

	@Test
	public void shouldXorBytes() {
		Xor xor = new Xor();
		assertThat(xor.add(0, 1, 2).value(), is((byte) 3));
		ImmutableByteArray data = ImmutableByteArray.wrap(4, 5);
		assertThat(xor.add(data).value(), is((byte) 2));
	}

}
