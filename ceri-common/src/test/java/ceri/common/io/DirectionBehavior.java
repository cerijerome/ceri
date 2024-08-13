package ceri.common.io;

import static ceri.common.test.AssertUtil.assertEquals;
import org.junit.Test;

public class DirectionBehavior {

	@Test
	public void testIn() {
		assertEquals(Direction.in(null), false);
		assertEquals(Direction.in(Direction.none), false);
		assertEquals(Direction.in(Direction.in), true);
		assertEquals(Direction.in(Direction.out), false);
		assertEquals(Direction.in(Direction.duplex), true);
	}

	@Test
	public void testOut() {
		assertEquals(Direction.out(null), false);
		assertEquals(Direction.out(Direction.none), false);
		assertEquals(Direction.out(Direction.in), false);
		assertEquals(Direction.out(Direction.out), true);
		assertEquals(Direction.out(Direction.duplex), true);
	}

	@Test
	public void shouldGetFromBoolean() {
		assertEquals(Direction.from(null), Direction.none);
		assertEquals(Direction.from(true), Direction.out);
		assertEquals(Direction.from(false), Direction.in);
	}

}
