package ceri.common.util;

import static ceri.common.test.TestUtil.assertThat;
import static ceri.common.test.TestUtil.assertThrown;
import static org.hamcrest.CoreMatchers.is;
import org.junit.Test;

public class CounterBehavior {

	@Test
	public void shouldIncrementCount() {
		Counter c = Counter.of();
		assertThat(c.count(), is(0L));
		assertThat(c.inc(), is(1L));
		assertThat(c.inc(100), is(101L));
		assertThat(c.count(), is(101L));
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
		assertThat(c.intCount(), is(Integer.MAX_VALUE - 1));
		assertThat(c.intInc(), is(Integer.MAX_VALUE));
		assertThrown(() -> c.intInc());
		assertThrown(() -> c.intCount());
		assertThat(c.count(), is(Integer.MAX_VALUE + 1L));
	}

	@Test
	public void shouldProvideStringRepresentation() {
		Counter c = Counter.of(777);
		assertThat(c.toString(), is("777"));
	}

}
