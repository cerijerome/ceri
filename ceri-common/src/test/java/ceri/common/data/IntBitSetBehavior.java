package ceri.common.data;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import org.junit.Test;

public class IntBitSetBehavior {

	@Test
	public void shouldOrBits() {
		IntBitSet b = IntBitSet.of();
		b.or(IntBitSet.of(0xff));
		assertThat(b.value(), is(0xff));
	}

}
