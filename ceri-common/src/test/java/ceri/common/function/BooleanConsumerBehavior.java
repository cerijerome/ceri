package ceri.common.function;

import static ceri.common.test.TestUtil.assertArray;
import org.junit.Test;

public class BooleanConsumerBehavior {

	@Test
	public void shouldCombineWithAndThen() {
		boolean[] bb = new boolean[] { false, false };
		BooleanConsumer c = b -> bb[0] = b;
		c = c.andThen(b -> bb[1] = b);
		c.accept(true);
		assertArray(bb, true, true);
	}

}
