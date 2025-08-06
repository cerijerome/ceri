package ceri.common.collection;

import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertUnordered;
import static ceri.common.test.AssertUtil.assertUnsupported;
import java.util.HashMap;
import java.util.Map;
import org.junit.Test;

public class MapsTest {

	@Test
	public void shouldBuildMapOfEntries() {
		var b = Maps.Builder.of("A", 1, "B", null, null, 3, "D", 4, null, null);
		assertUnordered(b.map.keySet(), "A", "B", "D", null);
		assertUnordered(b.map.values(), 1, null, 4, null);
	}

	@Test
	public void shouldBuildMapWithEntries() {
		var b = Maps.Builder.of().put("A", 1, "B", null, null, 3, "D", 4, null, null);
		assertUnordered(b.map.keySet(), "A", "B", "D", null);
		assertUnordered(b.map.values(), 1, null, 4, null);
	}

	@Test
	public void shouldBuildUnmodifiableMap() {
		var m = Maps.Builder.linked().put("A", 1).unmodifiable();
		assertUnsupported(() -> m.put("A", 1));
	}
	
	@Test
	public void shouldWrapGivenMapForStorage() {
		Map<Integer, String> map0 = new HashMap<>();
		Map<Integer, String> map1 = Maps.Builder.of(map0).put(1, "1").map;
		Map<Integer, String> map2 = Maps.Builder.of(map0).putAll(map1).map;
		assertEquals(map0, map1);
		assertEquals(map1, map2);
	}

}
