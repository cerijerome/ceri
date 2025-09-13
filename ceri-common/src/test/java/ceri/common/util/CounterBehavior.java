package ceri.common.util;

import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertString;
import static ceri.common.test.AssertUtil.assertThrown;
import org.junit.Test;

public class CounterBehavior {

	@Test
	public void shouldIncrementIntCount() {
		var c = Counter.of(0);
		assertEquals(c.get(), 0);
		assertEquals(c.inc(111), 111);
		assertEquals(c.preInc(-333), 111);
		assertEquals(c.get(), -222);
	}

	@Test
	public void shouldIncrementLongCount() {
		var c = Counter.of(0L);
		assertEquals(c.get(), 0L);
		assertEquals(c.inc(111L), 111L);
		assertEquals(c.preInc(-333L), 111L);
		assertEquals(c.get(), -222L);
	}

	@Test
	public void shouldNotOverflowIntCount() {
		var c = Counter.of(Integer.MAX_VALUE);
		assertThrown(() -> c.inc(1));
		c.set(Integer.MIN_VALUE);
		assertThrown(() -> c.inc(-1));
	}

	@Test
	public void shouldNotOverflowLongCount() {
		var c = Counter.of(Long.MAX_VALUE);
		assertThrown(() -> c.inc(1));
		c.set(Long.MIN_VALUE);
		assertThrown(() -> c.inc(-1));
	}

	@Test
	public void shouldProvideStringRepresentation() {
		assertString(Counter.of(777), "777");
		assertString(Counter.of(777), "777");
	}

}
