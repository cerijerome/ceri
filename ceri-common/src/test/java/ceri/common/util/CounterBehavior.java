package ceri.common.util;

import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertThrown;
import org.junit.Test;

public class CounterBehavior {

	@Test
	public void shouldIncrementCount() {
		Counter c = Counter.of();
		assertEquals(c.count(), 0L);
		assertEquals(c.inc(), 1L);
		assertEquals(c.inc(100), 101L);
		assertEquals(c.count(), 101L);
	}

	@Test
	public void shouldNotOverflowCount() {
		Counter c = Counter.of(Long.MAX_VALUE);
		assertThrown(() -> c.inc());
		c.set(Long.MIN_VALUE);
		assertThrown(() -> c.inc(-1));
	}

	@Test
	public void shouldReturnIntCount() {
		Counter c = Counter.of(Integer.MAX_VALUE - 1);
		assertEquals(c.intCount(), Integer.MAX_VALUE - 1);
		assertEquals(c.intInc(), Integer.MAX_VALUE);
		assertThrown(() -> c.intInc());
		assertThrown(() -> c.intCount());
		assertEquals(c.count(), Integer.MAX_VALUE + 1L);
	}

	@Test
	public void shouldProvideStringRepresentation() {
		Counter c = Counter.of(777);
		assertEquals(c.toString(), "777");
	}

}
