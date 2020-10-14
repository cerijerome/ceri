package ceri.common.collection;

import static ceri.common.test.TestUtil.assertCollection;
import static org.hamcrest.CoreMatchers.is;
import static ceri.common.test.TestUtil.assertThat;
import java.util.HashMap;
import java.util.Map;
import org.junit.Test;

public class MapPopulatorBehavior {

	@Test
	public void shouldInitializeFromGivenData() {
		Map<String, Integer> map = MapPopulator.of("A", 1, "ABC", 3, "BC", 2).map;
		assertCollection(map.keySet(), "A", "ABC", "BC");
		assertCollection(map.values(), 1, 3, 2);
	}

	@Test
	public void shouldWrapGivenMapForStorage() {
		Map<Integer, String> map0 = new HashMap<>();
		Map<Integer, String> map1 = MapPopulator.wrap(map0).put(1, "1").map;
		Map<Integer, String> map2 = MapPopulator.wrap(map0).putAll(map1).map;
		assertThat(map0, is(map1));
		assertThat(map1, is(map2));
	}

}
