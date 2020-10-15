package ceri.common.collection;

import static ceri.common.test.TestUtil.assertFalse;
import static ceri.common.test.TestUtil.assertThat;
import static org.hamcrest.CoreMatchers.is;
import java.util.HashMap;
import java.util.Map;
import org.junit.Assume;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class FixedSizeCacheBehavior {
	private static Map<Integer, String> map;
	private Map<Integer, String> cache;

	@BeforeClass
	public static void initMap() {
		map = new HashMap<>();
		map.put(1, "one");
		map.put(2, "two");
		map.put(3, "three");
		Assume.assumeThat(map.size(), is(3));
	}

	@Before
	public void initCache() {
		cache = new FixedSizeCache<>(3);
		cache.putAll(map);
		assertThat(cache, is(map));
	}

	@Test
	public void shouldNotExceedMaxSize() {
		cache.put(4, "four");
		assertThat(cache.size(), is(3));
		cache.put(5, "five");
		assertThat(cache.size(), is(3));
		cache.put(6, "six");
		assertThat(cache.size(), is(3));
	}

	@Test
	public void shouldRemoveOldestItem() {
		cache.put(4, "four");
		assertFalse(cache.containsKey(1));
	}

}
