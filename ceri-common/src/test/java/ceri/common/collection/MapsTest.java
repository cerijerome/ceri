package ceri.common.collection;

import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertMap;
import static ceri.common.test.AssertUtil.assertUnordered;
import static ceri.common.test.AssertUtil.assertUnsupported;
import java.util.HashMap;
import java.util.Map;
import org.junit.Test;

public class MapsTest {
	private final Map<Integer, String> map =
		Maps.Builder.of(1, "A", null, "B", 3, null).unmodifiable();

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
	public void shouldBuildWrappedMap() {
		Map<Integer, String> map0 = new HashMap<>();
		Map<Integer, String> map1 = Maps.Builder.of(map0).put(1, "1").map;
		Map<Integer, String> map2 = Maps.Builder.of(map0).putAll(map1).map;
		assertEquals(map0, map1);
		assertEquals(map1, map2);
	}

	@Test
	public void testMap() throws Exception {
		assertMap(Maps.map(i -> i, i -> s(i), Lists.of(1, null, 3)), 1, "1", null, null, 3, "3");
	}
	
	@Test
	public void testAdapt() throws Exception {
		assertMap(Maps.adapt(null, map));
		assertMap(Maps.adapt(i -> s(i), map), "1", "A", null, "B", "3", null);
		assertMap(Maps.adapt(null, s -> i(s), map));
		assertMap(Maps.adapt(i -> s(i), null, map));
		assertMap(Maps.adapt(i -> i, s -> s, null));
		assertMap(Maps.adapt(i -> s(i), s -> i(s), map), "1", 65, null, 66, "3",
			null);
		assertEquals(Maps.adaptPut(null, i -> s(i), s -> i(s), map), null);
	}

	@Test
	public void testBiAdapt() throws Exception {
		assertMap(Maps.biAdapt(null, map));
		assertMap(Maps.biAdapt((i, _) -> s(i), map), "1", "A", null, "B", "3", null);
		assertMap(Maps.biAdapt(null, (_, s) -> i(s), map));
		assertMap(Maps.biAdapt((i, _) -> s(i), null, map));
		assertMap(Maps.biAdapt((i, _) -> i, (_, s) -> s, null));
		assertMap(Maps.biAdapt((i, _) -> s(i), (_, s) -> i(s), map), "1", 65, null, 66, "3",
			null);
		assertEquals(Maps.biAdaptPut(null, (i, _) -> s(i), (_, s) -> i(s), map), null);
	}

	@Test
	public void testKeys() throws Exception {
		assertUnordered(Maps.keys(null, map).toSet(), 1, null, 3);
		assertUnordered(Maps.keys((_, s) -> s != null, null).toSet());
		assertUnordered(Maps.keys((_, s) -> s != null, map).toSet(), 1, null);
	}

	@Test
	public void testValues() throws Exception {
		assertUnordered(Maps.values(null, map).toSet(), "A", "B", null);
		assertUnordered(Maps.values((i, _) -> i != null, null).toSet());
		assertUnordered(Maps.values((i, _) -> i != null, map).toSet(), "A", null);
	}

	@Test
	public void testInvert() {
		assertMap(Maps.invert(this.map), "A", 1, "B", null, null, 3);
	}

	private static String s(Integer i) {
		if (i == null) return null;
		return "" + i;
	}

	private static Integer i(String s) {
		if (s == null || s.isEmpty()) return null;
		return (int) s.charAt(0);
	}
}
