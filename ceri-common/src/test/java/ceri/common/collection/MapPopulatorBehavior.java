package ceri.common.collection;

import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertUnordered;
import java.util.HashMap;
import java.util.Map;
import org.junit.Test;

public class MapPopulatorBehavior {

	@Test
	public void shouldInitializeFromGivenData() {
		Map<String, Integer> map = MapPopulator.of("A", 1, "ABC", 3, "BC", 2).map;
		assertUnordered(map.keySet(), "A", "ABC", "BC");
		assertUnordered(map.values(), 1, 3, 2);
	}

	@Test
	public void shouldWrapGivenMapForStorage() {
		Map<Integer, String> map0 = new HashMap<>();
		Map<Integer, String> map1 = MapPopulator.wrap(map0).put(1, "1").map;
		Map<Integer, String> map2 = MapPopulator.wrap(map0).putAll(map1).map;
		assertEquals(map0, map1);
		assertEquals(map1, map2);
	}

}
