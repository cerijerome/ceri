package ceri.common.io;

import org.junit.Test;
import ceri.common.test.Assert;

public class DirectionBehavior {

	@Test
	public void testIn() {
		Assert.equal(Direction.in(null), false);
		Assert.equal(Direction.in(Direction.none), false);
		Assert.equal(Direction.in(Direction.in), true);
		Assert.equal(Direction.in(Direction.out), false);
		Assert.equal(Direction.in(Direction.duplex), true);
	}

	@Test
	public void testOut() {
		Assert.equal(Direction.out(null), false);
		Assert.equal(Direction.out(Direction.none), false);
		Assert.equal(Direction.out(Direction.in), false);
		Assert.equal(Direction.out(Direction.out), true);
		Assert.equal(Direction.out(Direction.duplex), true);
	}

	@Test
	public void shouldGetFromBoolean() {
		Assert.equal(Direction.from(null), Direction.none);
		Assert.equal(Direction.from(true), Direction.out);
		Assert.equal(Direction.from(false), Direction.in);
	}

}
