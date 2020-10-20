package ceri.common.util;

import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertFalse;
import static ceri.common.test.AssertUtil.assertNull;
import static ceri.common.test.AssertUtil.assertTrue;
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
		assertEquals(count(), 0);
		assertFalse(cache.hasValue());
		assertEquals(cache.get(), 1);
		assertTrue(cache.hasValue());
		assertEquals(count(), 2);
		assertEquals(cache.get(), 1);
	}

	@Test
	public void shouldAllowNulls() {
		var cache = ValueCache.of(() -> null);
		assertFalse(cache.hasValue());
		assertNull(cache.get());
		assertTrue(cache.hasValue());
	}

	@Test
	public void shouldHaveStringRepresentation() {
		var cache = ValueCache.of(() -> null);
		assertFalse(cache.toString().isEmpty());
		cache.get();
		assertFalse(cache.toString().isEmpty());
	}

	private int count() {
		return count++;
	}

}
