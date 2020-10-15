package ceri.common.data;

import static ceri.common.test.TestUtil.assertThat;
import static org.hamcrest.CoreMatchers.is;
import org.junit.Test;

public class XorBehavior {

	@Test
	public void shouldXorBytes() {
		Xor xor = new Xor();
		assertThat(xor.add(0, 1, 2).value(), is((byte) 3));
		ByteProvider data = ByteArray.Immutable.wrap(4, 5);
		assertThat(xor.add(data).value(), is((byte) 2));
	}

}
