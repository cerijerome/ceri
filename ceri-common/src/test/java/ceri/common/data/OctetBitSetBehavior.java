package ceri.common.data;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import org.junit.Test;

public class OctetBitSetBehavior {

	@Test
	public void should() {
		OctetBitSet b = OctetBitSet.create();
		b.or(OctetBitSet.of(0xff));
		assertThat(b.value(), is((byte) -1));
	}

}
