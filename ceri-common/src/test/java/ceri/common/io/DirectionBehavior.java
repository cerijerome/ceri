package ceri.common.io;

import static ceri.common.test.AssertUtil.assertEquals;
import org.junit.Test;

public class DirectionBehavior {

	@Test
	public void shouldGetFromBoolean() {
		assertEquals(Direction.from(null), Direction.none);
		assertEquals(Direction.from(true), Direction.out);
		assertEquals(Direction.from(false), Direction.in);
	}

}
