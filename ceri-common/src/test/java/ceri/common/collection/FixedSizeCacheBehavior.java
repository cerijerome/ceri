package ceri.common.collection;

import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertFalse;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;

public class FixedSizeCacheBehavior {
	private static Map<Integer, String> map = ImmutableUtil.asMap(1, "one", 2, "two", 3, "three");
	private Map<Integer, String> cache;

	@Before
	public void before() {
		cache = new FixedSizeCache<>(3);
		cache.putAll(map);
		assertEquals(cache, map);
	}

	@Test
	public void shouldNotExceedMaxSize() {
		cache.put(4, "four");
		assertEquals(cache.size(), 3);
		cache.put(5, "five");
		assertEquals(cache.size(), 3);
		cache.put(6, "six");
		assertEquals(cache.size(), 3);
	}

	@Test
	public void shouldRemoveOldestItem() {
		cache.put(4, "four");
		assertFalse(cache.containsKey(1));
	}

}
