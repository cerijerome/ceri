package ceri.common.collection;

import static ceri.common.collection.CollectionUtil.asSet;
import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertMap;
import static ceri.common.test.AssertUtil.assertNotNull;
import static ceri.common.test.AssertUtil.assertNull;
import static ceri.common.test.AssertUtil.assertOrdered;
import static ceri.common.test.AssertUtil.assertPrivateConstructor;
import static ceri.common.test.AssertUtil.assertThrown;
import static ceri.common.test.AssertUtil.assertTrue;
import static ceri.common.test.AssertUtil.assertUnordered;
import static ceri.common.test.AssertUtil.fail;
import static ceri.common.test.TestUtil.testMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.NavigableSet;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Stream;
import org.junit.Test;
import ceri.common.stream.StreamUtil;
import ceri.common.util.BasicUtil;

public class ImmutableUtilTest {

	@Test
	public void testConstructorIsPrivate() {
		assertPrivateConstructor(ImmutableUtil.class);
	}

	private enum E {
		A,
		ABC,
		BC;
	}

	@Test
	public void testIntSet() {
		assertUnordered(ImmutableUtil.intSet());
		assertImmutableCollection(ImmutableUtil.intSet());
		assertUnordered(ImmutableUtil.intSet(Integer.MIN_VALUE, 0, Integer.MAX_VALUE),
			Integer.MIN_VALUE, 0, Integer.MAX_VALUE);
		assertImmutableCollection(ImmutableUtil.intSet(Integer.MIN_VALUE, 0, Integer.MAX_VALUE));
	}

	@Test
	public void testLongSet() {
		assertUnordered(ImmutableUtil.longSet());
		assertImmutableCollection(ImmutableUtil.longSet());
		assertUnordered(ImmutableUtil.longSet(Long.MIN_VALUE, 0, Long.MAX_VALUE), Long.MIN_VALUE,
			0L, Long.MAX_VALUE);
		assertImmutableCollection(ImmutableUtil.longSet(Long.MIN_VALUE, 0, Long.MAX_VALUE));
	}

	@Test
	public void testDoubleSet() {
		assertUnordered(ImmutableUtil.doubleSet());
		assertImmutableCollection(ImmutableUtil.doubleSet());
		assertUnordered(ImmutableUtil.doubleSet(Double.MIN_VALUE, 0, Double.MAX_VALUE),
			Double.MIN_VALUE, 0.0, Double.MAX_VALUE);
		assertImmutableCollection(ImmutableUtil.doubleSet(Double.MIN_VALUE, 0, Double.MAX_VALUE));
	}

	@Test
	public void testIntList() {
		assertOrdered(ImmutableUtil.intList());
		assertImmutableList(ImmutableUtil.intList());
		assertOrdered(ImmutableUtil.intList(Integer.MIN_VALUE, 0, Integer.MAX_VALUE),
			Integer.MIN_VALUE, 0, Integer.MAX_VALUE);
		assertImmutableList(ImmutableUtil.intList(Integer.MIN_VALUE, 0, Integer.MAX_VALUE));
	}

	@Test
	public void testLongList() {
		assertOrdered(ImmutableUtil.longList());
		assertImmutableList(ImmutableUtil.longList());
		assertOrdered(ImmutableUtil.longList(Long.MIN_VALUE, 0, Long.MAX_VALUE), Long.MIN_VALUE, 0L,
			Long.MAX_VALUE);
		assertImmutableList(ImmutableUtil.longList(Long.MIN_VALUE, 0, Long.MAX_VALUE));
	}

	@Test
	public void testDoubleList() {
		assertOrdered(ImmutableUtil.doubleList());
		assertImmutableList(ImmutableUtil.doubleList());
		assertOrdered(ImmutableUtil.doubleList(Double.MIN_VALUE, 0, Double.MAX_VALUE),
			Double.MIN_VALUE, 0.0, Double.MAX_VALUE);
		assertImmutableList(ImmutableUtil.doubleList(Double.MIN_VALUE, 0, Double.MAX_VALUE));
	}

	@Test
	public void testInvert() {
		Map<String, Integer> map = ImmutableUtil.invert(Map.of(1, "1", 2, "2"));
		assertImmutableMap(map);
		assertEquals(map, Map.of("1", 1, "2", 2));
	}

	@Test
	public void testEnumMap() {
		Map<Integer, E> map = ImmutableUtil.enumMap(e -> e.name().length(), E.class);
		assertImmutableMap(map);
		assertEquals(map, Map.of(1, E.A, 3, E.ABC, 2, E.BC));
	}

	@Test
	public void testEnumSet() {
		Set<E> one = ImmutableUtil.enumSet(E.A);
		Set<E> two = ImmutableUtil.enumSet(E.ABC, E.BC);
		assertImmutableCollection(one);
		assertUnordered(one, E.A);
		assertImmutableCollection(two);
		assertUnordered(two, E.ABC, E.BC);
	}

	@Test
	public void testEnumRange() {
		Set<E> set = ImmutableUtil.enumRange(E.A, E.BC);
		assertImmutableCollection(set);
		assertUnordered(set, E.A, E.ABC, E.BC);
		set = ImmutableUtil.enumRange(E.ABC, E.BC);
		assertImmutableCollection(set);
		assertUnordered(set, E.ABC, E.BC);
		set = ImmutableUtil.enumRange(E.A, E.A);
		assertImmutableCollection(set);
		assertUnordered(set, E.A);
		assertThrown(() -> ImmutableUtil.enumRange(E.BC, E.A));
	}

	@Test
	public void testUnboundedEnumRange() {
		Set<E> set = ImmutableUtil.enumRange(null, null);
		assertImmutableCollection(set);
		assertUnordered(set);
		set = ImmutableUtil.enumRange(null, E.ABC);
		assertImmutableCollection(set);
		assertUnordered(set, E.A, E.ABC);
		set = ImmutableUtil.enumRange(E.ABC, null);
		assertImmutableCollection(set);
		assertUnordered(set, E.ABC, E.BC);
	}

	@Test
	public void testEmptyCopyAsSet() {
		Set<?> set = ImmutableUtil.copyAsSet(new HashSet<>());
		assertTrue(set.isEmpty());
		assertImmutableCollection(set);
	}

	@Test
	public void testEmptyCopyAsList() {
		List<?> list = ImmutableUtil.copyAsList(new ArrayList<>());
		assertTrue(list.isEmpty());
		assertImmutableList(list);
	}

	@Test
	public void testAsSet() {
		Set<String> set = ImmutableUtil.asSet("1", "2", "3");
		assertUnordered(set, "1", "2", "3");
		assertImmutableCollection(set);
	}

	@Test
	public void testEmptyAsSet() {
		Set<?> set = ImmutableUtil.asSet();
		assertTrue(set.isEmpty());
		assertImmutableCollection(set);
	}

	@Test
	public void testEmptyAsList() {
		List<?> list = ImmutableUtil.asList();
		assertTrue(list.isEmpty());
		assertImmutableList(list);
	}

	@Test
	public void testEmptyCopyAsMap() {
		Map<?, ?> map = ImmutableUtil.copyAsMap(new HashMap<>());
		assertTrue(map.isEmpty());
		assertImmutableMap(map);
	}

	@Test
	public void testEmptyCopyAsNavigableMap() {
		Map<?, ?> map = ImmutableUtil.copyAsNavigableMap(new HashMap<>());
		assertTrue(map.isEmpty());
		assertImmutableMap(map);
	}

	@Test
	public void testIterableShouldIterateItems() {
		List<String> list = new ArrayList<>();
		Collections.addAll(list, "A", "B", "C");
		int i = 0;
		for (String s : ImmutableUtil.iterable(list)) {
			assertEquals(s, list.get(i++));
		}
	}

	@Test
	public void testIterableShouldNotAllowRemovals() {
		List<String> list = new ArrayList<>();
		Collections.addAll(list, "A", "B", "C");
		Iterable<String> iterable = ImmutableUtil.iterable(list);
		Iterator<String> iterator = iterable.iterator();
		assertEquals(iterator.next(), "A");
		assertThrown(UnsupportedOperationException.class, iterator::remove);
	}

	@Test
	public void testJoinAsList() {
		var list = ImmutableUtil.joinAsList(List.of("a", "b"), Set.of("c"), List.of(),
			Arrays.asList(null, "d"));
		assertOrdered(list, "a", "b", "c", null, "d");
		assertImmutableList(list);
	}

	@Test
	public void testJoinAsSet() {
		var set = ImmutableUtil.joinAsSet(List.of("a", "b"), Set.of("c"), List.of(),
			Arrays.asList(null, "d"));
		assertUnordered(set, "a", "b", "c", "d", null);
		assertImmutableCollection(set);
	}

	@Test
	public void testWrapEmptyMap() {
		Map<String, Integer> map = new HashMap<>();
		Map<String, Integer> immutableMap = ImmutableUtil.wrapMap(map);
		assertImmutableMap(immutableMap);
		assertMap(immutableMap);
		map.put("A", 1);
		assertMap(immutableMap);
	}

	@Test
	public void testWrapMap() {
		Map<String, Integer> map = new HashMap<>();
		map.put("A", 1);
		map.put("B", null);
		Map<String, Integer> immutableMap = ImmutableUtil.wrapMap(map);
		assertImmutableMap(immutableMap);
		assertMap(immutableMap, "A", 1, "B", null);
		map.put("C", 3);
		assertMap(immutableMap, "A", 1, "B", null, "C", 3);
	}

	@Test
	public void testWrapAsSubList() {
		var array = new Integer[] { 1, 2, 3, 4, 5 };
		assertThrown(() -> ImmutableUtil.wrapAsList(array, 3, 6));
		assertThrown(() -> ImmutableUtil.wrapAsList(array, 6, 6));
		assertOrdered(ImmutableUtil.wrapAsList(array, 0, 0));
		assertOrdered(ImmutableUtil.wrapAsList(array, 5, 5));
		final List<Integer> list = ImmutableUtil.wrapAsList(array, 1, 4);
		assertEquals(list, Arrays.asList(2, 3, 4));
		assertImmutableList(list);
		array[2] = 0;
		assertEquals(list, Arrays.asList(2, 0, 4));
	}

	@Test
	public void testWrapAsList() {
		var array = new Integer[] { 1, 2, 3, 4, 5 };
		final List<Integer> list = ImmutableUtil.wrapAsList(array);
		assertEquals(list, Arrays.asList(1, 2, 3, 4, 5));
		assertImmutableList(list);
		array[2] = 0;
		assertEquals(list, Arrays.asList(1, 2, 0, 4, 5));
	}

	@Test
	public void testAsList() {
		final List<Integer> list = ImmutableUtil.asList(new Integer[] { 1, 2, 3, 4, 5 });
		assertEquals(list, Arrays.asList(1, 2, 3, 4, 5));
		assertImmutableList(list);
	}

	@Test
	public void testAsMapOfSets() {
		var srcMap = new HashMap<>(Map.of("123", asSet(1, 2, 3), "4", asSet(4)));
		final var map = ImmutableUtil.asMapOfSets(srcMap);
		assertImmutableMap(map);
		assertImmutableCollection(map.get("123"));
		assertImmutableCollection(map.get("4"));
		srcMap.get("4").add(5);
		assertUnordered(map.get("4"), 4, 5);
	}

	@Test
	public void testAsEmptyMapOfSets() {
		var srcMap = new HashMap<String, Set<Integer>>();
		final var map = ImmutableUtil.asMapOfSets(srcMap);
		assertImmutableMap(map);
		assertTrue(map.isEmpty());
	}

	@Test
	public void testAsMapOfLists() {
		var srcMap =
			new HashMap<>(Map.of("123", Mutables.asList(1, 2, 3), "4", Mutables.asList(4)));
		final var map = ImmutableUtil.asMapOfLists(srcMap);
		assertImmutableMap(map);
		assertImmutableList(map.get("123"));
		assertImmutableList(map.get("4"));
		srcMap.get("4").add(5);
		assertOrdered(map.get("4"), 4, 5);
	}

	@Test
	public void testAsEmptyMapOfLists() {
		var srcMap = new HashMap<String, List<Integer>>();
		final var map = ImmutableUtil.asMapOfLists(srcMap);
		assertImmutableMap(map);
		assertTrue(map.isEmpty());
	}

	@Test
	public void testAsMapOfMaps() {
		var srcMap = new HashMap<>(Map.of("123", new HashMap<>(Map.of("1", 1, "2", 2, "3", 3)), "4",
			new HashMap<>(Map.of("4", 4))));
		final var map = ImmutableUtil.asMapOfMaps(srcMap);
		assertImmutableMap(map);
		assertImmutableMap(map.get("123"));
		assertImmutableMap(map.get("4"));
		srcMap.get("4").put("5", 5);
		assertMap(map.get("4"), "4", 4, "5", 5);
	}

	@Test
	public void testAsEmptyMapOfMaps() {
		var srcMap = new HashMap<String, Map<String, Integer>>();
		final var map = ImmutableUtil.asMapOfMaps(srcMap);
		assertImmutableMap(map);
		assertTrue(map.isEmpty());
	}

	@Test
	public void testCopyAsMapOfSets() {
		assertNull(ImmutableUtil.copyAsMapOfSets(null));
		assertTrue(ImmutableUtil.copyAsMapOfSets(new HashMap<>()).isEmpty());
		Map<String, Set<Integer>> srcMap =
			testMap("123", asSet(1, 2, 3), "4", asSet(4), "", asSet(), "null", null);
		Map<String, Set<Integer>> copy =
			testMap("123", asSet(1, 2, 3), "4", asSet(4), "", asSet(), "null", null);
		final Map<String, Set<Integer>> map = ImmutableUtil.copyAsMapOfSets(srcMap);
		assertTrue(srcMap.get("123").remove(1));
		assertNotNull(srcMap.remove("4"));
		assertEquals(map, copy);
		assertImmutableMap(map);
		assertImmutableCollection(map.get("123"));
		assertImmutableCollection(map.get("4"));
	}

	@Test
	public void testCopyAsMapOfLists() {
		assertNull(ImmutableUtil.copyAsMapOfLists(null));
		assertTrue(ImmutableUtil.copyAsMapOfLists(new HashMap<>()).isEmpty());
		Map<String, List<Integer>> srcMap = testMap("123", Mutables.asList(1, 2, 3), "4",
			Mutables.asList(4), "", Mutables.asList(), "null", null);
		Map<String, List<Integer>> copy = testMap("123", Mutables.asList(1, 2, 3), "4",
			Mutables.asList(4), "", Mutables.asList(), "null", null);
		final Map<String, List<Integer>> map = ImmutableUtil.copyAsMapOfLists(srcMap);
		assertEquals(srcMap.get("123").set(0, -1), 1);
		assertNotNull(srcMap.remove("4"));
		assertEquals(map, copy);
		assertImmutableMap(map);
		assertImmutableList(map.get("123"));
		assertImmutableList(map.get("4"));
	}

	@Test
	public void testCopyAsMapOfMaps() {
		assertNull(ImmutableUtil.copyAsMapOfMaps(null));
		assertTrue(ImmutableUtil.copyAsMapOfMaps(new HashMap<>()).isEmpty());
		Map<String, Map<String, Integer>> srcMap = testMap( //
			"123", new HashMap<>(Map.of("1", 1, "2", 2, "3", 3)), "4",
			new HashMap<>(Map.of(4, "4")), "", new HashMap<>(), "null", null);
		Map<String, Map<String, Integer>> copy = testMap( //
			"123", new HashMap<>(Map.of("1", 1, "2", 2, "3", 3)), "4",
			new HashMap<>(Map.of(4, "4")), "", new HashMap<>(), "null", null);
		final Map<String, Map<String, Integer>> map = ImmutableUtil.copyAsMapOfMaps(srcMap);
		assertEquals(srcMap.get("123").remove("1"), 1);
		assertNotNull(srcMap.remove("4"));
		assertEquals(map, copy);
		assertImmutableMap(map);
		assertImmutableMap(map.get("123"));
		assertImmutableMap(map.get("4"));
	}

	@Test
	public void testCopyAsList() {
		assertNull(ImmutableUtil.copyAsList(null));
		List<Integer> srcList = new ArrayList<>();
		Collections.addAll(srcList, 1, 2, 3, 4, 5);
		List<Integer> copy = new ArrayList<>(srcList);
		final List<Integer> list = ImmutableUtil.copyAsList(srcList);
		srcList.remove(0);
		assertEquals(list, copy);
		assertImmutableList(list);
	}

	@Test
	public void testReverseAsList() {
		assertNull(ImmutableUtil.reverseAsList(null));
		assertImmutableList(ImmutableUtil.reverseAsList(new HashSet<>()));
	}

	@Test
	public void testCopyAsNavigableSet() {
		assertOrdered(ImmutableUtil.copyAsNavigableSet(asSet()));
		Set<Integer> srcSet = new HashSet<>();
		Collections.addAll(srcSet, 2, 3, 1, 5, 4);
		final NavigableSet<Integer> set = ImmutableUtil.copyAsNavigableSet(srcSet);
		srcSet.remove(0);
		assertOrdered(set, 1, 2, 3, 4, 5);
		assertImmutableCollection(set);
	}

	@Test
	public void testCopyAsSet() {
		Set<Integer> srcSet = new HashSet<>();
		Collections.addAll(srcSet, 1, 2, 3, 4, 5);
		Set<Integer> copy = new HashSet<>(srcSet);
		final Set<Integer> set = ImmutableUtil.copyAsSet(srcSet);
		srcSet.remove(0);
		assertEquals(set, copy);
		assertImmutableCollection(set);
	}

	@Test
	public void testCopyAsMap() {
		Map<Integer, String> srcMap = new HashMap<>();
		srcMap.put(1, "1");
		srcMap.put(2, "2");
		srcMap.put(3, "3");
		srcMap.put(4, "4");
		srcMap.put(5, "5");
		Map<Integer, String> copy = new HashMap<>(srcMap);
		final Map<Integer, String> map = ImmutableUtil.copyAsMap(srcMap);
		srcMap.remove(1);
		assertEquals(map, copy);
		assertImmutableMap(map);
	}

	@Test
	public void testCopyAsNavigableMap() {
		Map<Integer, String> srcMap = new HashMap<>();
		srcMap.put(2, "2");
		srcMap.put(5, "5");
		srcMap.put(3, "3");
		srcMap.put(1, "1");
		srcMap.put(4, "4");
		Map<Integer, String> copy = new TreeMap<>(srcMap);
		final NavigableMap<Integer, String> map = ImmutableUtil.copyAsNavigableMap(srcMap);
		srcMap.remove(1);
		assertEquals(map, copy);
		assertOrdered(map.keySet(), 1, 2, 3, 4, 5);
		assertImmutableMap(map);
	}

	@Test
	public void testCollectIterableAsList() {
		var list = ImmutableUtil.collectAsList(Arrays.asList("1", "2", "3", "4", "5"));
		assertEquals(list, Arrays.asList("1", "2", "3", "4", "5"));
		assertImmutableList(list);
	}

	@Test
	public void testCollectIterableAsSet() {
		var set = ImmutableUtil.collectAsSet(Arrays.asList("1", "2", "3", "4", "5"));
		assertEquals(set, asSet("1", "2", "3", "4", "5"));
		assertImmutableCollection(set);
	}

	@Test
	public void testCollectStreamAsList() {
		var list = ImmutableUtil.collectAsList(Stream.of("1", "2", "3", "4", "5"));
		assertEquals(list, Arrays.asList("1", "2", "3", "4", "5"));
		assertImmutableList(list);
	}

	@Test
	public void testCollectStreamAsSet() {
		var set = ImmutableUtil.collectAsSet(Stream.of("1", "2", "3", "4", "5"));
		assertEquals(set, asSet("1", "2", "3", "4", "5"));
		assertImmutableCollection(set);
	}

	@Test
	public void testCollectStreamAsNavigableSet() {
		var set = ImmutableUtil.collectAsNavigableSet(Stream.of("4", "1", "3", "5", "2"));
		assertEquals(set, asSet("1", "2", "3", "4", "5"));
		assertImmutableCollection(set);
	}

	@Test
	public void testConvertAsList() {
		var list = ImmutableUtil.convertAsList(Integer::parseInt, "1", "2", "3", "4", "5");
		assertEquals(list, Arrays.asList(1, 2, 3, 4, 5));
		assertImmutableList(list);
	}

	@Test
	public void testConvertAsSet() {
		var set = ImmutableUtil.convertAsSet(Integer::parseInt, "1", "2", "3", "4", "5");
		assertEquals(set, new HashSet<>(Arrays.asList(1, 2, 3, 4, 5)));
		assertImmutableCollection(set);
	}

	@Test
	public void testConvertAsNavigableSet() {
		var set = ImmutableUtil.convertAsNavigableSet(Integer::parseInt, "5", "4", "1", "3", "2");
		assertOrdered(set, 1, 2, 3, 4, 5);
		assertImmutableCollection(set);
	}

	@Test
	public void testConvertAllAsMap() {
		var map = ImmutableUtil.convertAllAsMap(String::valueOf, 1, 3, 2);
		assertImmutableMap(map);
		assertEquals(map, Map.of("1", 1, "3", 3, "2", 2));
		map = ImmutableUtil.convertAllAsMap(String::valueOf, i -> i + 1, new Integer[] { 1, 3, 2 });
		assertEquals(map, Map.of("1", 2, "3", 4, "2", 3));
	}

	@Test
	public void testConvertAsMap() {
		var map = ImmutableUtil.convertAsMap(String::valueOf, List.of(1, 3, 2));
		assertEquals(map, Map.of("1", 1, "3", 3, "2", 2));
		map = ImmutableUtil.convertAsMap(String::valueOf, i -> i + 1, List.of(1, 3, 2));
		assertEquals(map, Map.of("1", 2, "3", 4, "2", 3));
	}

	@Test
	public void testConvertStreamAsMap() {
		var map = ImmutableUtil.convertAsMap(String::valueOf, Stream.of(1, 3, 2));
		assertImmutableMap(map);
		assertEquals(map, Map.of("1", 1, "3", 3, "2", 2));
	}

	@Test
	public void testConvertAsMapWithMerge() {
		Function<Integer, Integer> keyFn = i -> String.valueOf(i).length();
		Function<Integer, Integer> valueFn = i -> -i;
		var map = ImmutableUtil.convertAsMap(keyFn, valueFn, StreamUtil.merge(true),
			List.of(1, 22, 333, 44));
		assertImmutableMap(map);
		assertEquals(map, Map.of(1, -1, 2, -22, 3, -333));
		map = ImmutableUtil.convertAsMap(keyFn, valueFn, StreamUtil.merge(false),
			List.of(1, 22, 333, 44));
		assertImmutableMap(map);
		assertEquals(map, Map.of(1, -1, 2, -44, 3, -333));
	}

	@Test
	public void testConvertStreamAsMapWithMerge() {
		Function<Integer, Integer> keyFn = i -> String.valueOf(i).length();
		Function<Integer, Integer> valueFn = i -> -i;
		var map =
			ImmutableUtil.convertAsMap(keyFn, StreamUtil.merge(true), Stream.of(1, 22, 333, 44));
		assertImmutableMap(map);
		assertEquals(map, Map.of(1, 1, 2, 22, 3, 333));
		map = ImmutableUtil.convertAsMap(keyFn, StreamUtil.merge(false), Stream.of(1, 22, 333, 44));
		assertImmutableMap(map);
		assertEquals(map, Map.of(1, 1, 2, 44, 3, 333));
		map = ImmutableUtil.convertAsMap(keyFn, valueFn, StreamUtil.merge(false),
			Stream.of(1, 22, 333, 44));
		assertImmutableMap(map);
		assertEquals(map, Map.of(1, -1, 2, -44, 3, -333));
	}

	private static void assertImmutableMap(final Map<?, ?> map) {
		assertImmutableCollection(map.entrySet());
		assertImmutableCollection(map.keySet());
		assertImmutableCollection(map.values());
		if (!map.isEmpty()) try {
			map.clear();
			fail();
		} catch (Exception e) {}
		try {
			map.put(null, null);
			fail();
		} catch (Exception e) {}
		if (!map.isEmpty()) {
			try {
				Map<Object, Object> objMap = BasicUtil.unchecked(map);
				objMap.putAll(map);
				fail();
			} catch (Exception e) {}
			try {
				map.remove(null);
				fail();
			} catch (Exception e) {}
		}
	}

	private static void assertImmutableList(final List<?> list) {
		assertImmutableCollection(list);
		assertImmutableIterator(list.listIterator());
		assertImmutableIterator(list.listIterator(0));
		try {
			list.add(0, null);
			fail();
		} catch (Exception e) {}
		try {
			list.addAll(0, null);
			fail();
		} catch (Exception e) {}
		try {
			list.set(0, null);
			fail();
		} catch (Exception e) {}
		if (!list.isEmpty()) {
			try {
				list.remove(0);
				fail();
			} catch (Exception e) {}
		}
	}

	private static void assertImmutableCollection(final Collection<?> collection) {
		assertImmutableIterator(collection.iterator());
		try {
			collection.add(null);
			fail();
		} catch (Exception e) {}
		try {
			collection.addAll(null);
			fail();
		} catch (Exception e) {}
		if (!collection.isEmpty()) {
			try {
				collection.clear();
				fail();
			} catch (Exception e) {}
			try {
				collection.remove(null);
				fail();
			} catch (Exception e) {}
			try {
				collection.removeAll(Collections.emptySet());
				fail();
			} catch (Exception e) {}
			try {
				collection.retainAll(Collections.emptySet());
				fail();
			} catch (Exception e) {}
		}
	}

	private static void assertImmutableIterator(final Iterator<?> iterator) {
		if (!iterator.hasNext()) return;
		try {
			iterator.next();
			iterator.remove();
			fail();
		} catch (Exception e) {}
	}

}
