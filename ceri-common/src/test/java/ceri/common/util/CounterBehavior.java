package ceri.common.util;

import org.junit.Test;
import ceri.common.test.Assert;

public class CounterBehavior {

	@Test
	public void shouldIncrementIntCount() {
		var c = Counter.of(0);
		Assert.equal(c.get(), 0);
		Assert.equal(c.inc(111), 111);
		Assert.equal(c.preInc(-333), 111);
		Assert.equal(c.get(), -222);
	}

	@Test
	public void shouldIncrementLongCount() {
		var c = Counter.of(0L);
		Assert.equal(c.get(), 0L);
		Assert.equal(c.inc(111L), 111L);
		Assert.equal(c.preInc(-333L), 111L);
		Assert.equal(c.get(), -222L);
	}

	@Test
	public void shouldNotOverflowIntCount() {
		var c = Counter.of(Integer.MAX_VALUE);
		Assert.thrown(() -> c.inc(1));
		c.set(Integer.MIN_VALUE);
		Assert.thrown(() -> c.inc(-1));
	}

	@Test
	public void shouldNotOverflowLongCount() {
		var c = Counter.of(Long.MAX_VALUE);
		Assert.thrown(() -> c.inc(1));
		c.set(Long.MIN_VALUE);
		Assert.thrown(() -> c.inc(-1));
	}

	@Test
	public void shouldProvideStringRepresentation() {
		Assert.string(Counter.of(777), "777");
		Assert.string(Counter.of(777L), "777");
	}
}
