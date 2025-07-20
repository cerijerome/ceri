package ceri.common.util;

import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertString;
import static ceri.common.test.AssertUtil.assertThrown;
import org.junit.Test;

public class CounterBehavior {

	@Test
	public void shouldIncrementIntCount() {
		var c = Counter.ofInt(0);
		assertEquals(c.count(), 0);
		assertEquals(c.inc(111), 111);
		assertEquals(c.preInc(-333), 111);
		assertEquals(c.count(), -222);
	}

	@Test
	public void shouldIncrementLongCount() {
		var c = Counter.ofLong(0L);
		assertEquals(c.count(), 0L);
		assertEquals(c.inc(111L), 111L);
		assertEquals(c.preInc(-333L), 111L);
		assertEquals(c.count(), -222L);
	}

	@Test
	public void shouldNotOverflowIntCount() {
		var c = Counter.ofInt(Integer.MAX_VALUE);
		assertThrown(() -> c.inc(1));
		c.set(Integer.MIN_VALUE);
		assertThrown(() -> c.inc(-1));
	}

	@Test
	public void shouldNotOverflowLongCount() {
		var c = Counter.ofLong(Long.MAX_VALUE);
		assertThrown(() -> c.inc(1));
		c.set(Long.MIN_VALUE);
		assertThrown(() -> c.inc(-1));
	}

	@Test
	public void shouldProvideStringRepresentation() {
		assertString(Counter.ofInt(777), "777");
		assertString(Counter.ofLong(777), "777");
	}

}
