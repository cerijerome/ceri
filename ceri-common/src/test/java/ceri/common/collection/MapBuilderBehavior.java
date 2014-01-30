package ceri.common.collection;

import static ceri.common.test.TestUtil.assertException;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.junit.Test;

public class MapBuilderBehavior {

	@Test
	public void shouldWrapGivenMapForStorage() {
		Map<Integer, String> map0 = new HashMap<>();
		Map<Integer, String> map1 = MapBuilder.create(map0).put(1, "1").build();
		Map<Integer, String> map2 = MapBuilder.create(map0).putAll(map1).build();
		assertThat(map0, is(map1));
		assertThat(map1, is(map2));
	}

	@Test
	public void shouldBeImmutable() {
		final Map<Integer, String> map1 = MapBuilder.<Integer, String>create().put(0, "0").build();
		assertException(UnsupportedOperationException.class, new Runnable() {
			@Override
			public void run() {
				map1.clear();
			}
		});
		assertException(UnsupportedOperationException.class, new Runnable() {
			@Override
			public void run() {
				map1.put(1, "1");
			}
		});
		assertException(UnsupportedOperationException.class, new Runnable() {
			@Override
			public void run() {
				map1.putAll(Collections.singletonMap(1, "1"));
			}
		});
		assertException(UnsupportedOperationException.class, new Runnable() {
			@Override
			public void run() {
				map1.remove(0);
			}
		});
		assertThat(map1, is(Collections.singletonMap(0, "0")));
	}

}
