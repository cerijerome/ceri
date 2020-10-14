package ceri.common.util;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNull;
import static ceri.common.test.TestUtil.assertThat;
import org.junit.Before;
import org.junit.Test;

public class ValueCacheBehavior {
	private int count = 0;

	@Before
	public void before() {
		count = 0;
	}

	@Test
	public void shouldOnlyLoadOnce() {
		var cache = ValueCache.of(this::count);
		assertThat(count(), is(0));
		assertThat(cache.hasValue(), is(false));
		assertThat(cache.get(), is(1));
		assertThat(cache.hasValue(), is(true));
		assertThat(count(), is(2));
		assertThat(cache.get(), is(1));
	}

	@Test
	public void shouldAllowNulls() {
		var cache = ValueCache.of(() -> null);
		assertThat(cache.hasValue(), is(false));
		assertNull(cache.get());
		assertThat(cache.hasValue(), is(true));
	}

	@Test
	public void shouldHaveStringRepresentation() {
		var cache = ValueCache.of(() -> null);
		assertThat(cache.toString().isEmpty(), is(false));
		cache.get();
		assertThat(cache.toString().isEmpty(), is(false));
	}

	private int count() {
		return count++;
	}

}
