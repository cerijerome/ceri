package ceri.common.collection;

import static ceri.common.collection.CollectionUtil.asSet;
import static ceri.common.test.AssertUtil.assertCollection;
import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertIterable;
import static ceri.common.test.AssertUtil.assertNotNull;
import static ceri.common.test.AssertUtil.assertNull;
import static ceri.common.test.AssertUtil.assertPrivateConstructor;
import static ceri.common.test.AssertUtil.assertThrown;
import static ceri.common.test.AssertUtil.assertTrue;
import static ceri.common.test.AssertUtil.fail;
import static ceri.common.test.TestUtil.testMap;
import static java.util.Arrays.asList;
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
import java.util.stream.Stream;
import org.junit.Test;
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
		assertCollection(one, E.A);
		assertImmutableCollection(two);
		assertCollection(two, E.ABC, E.BC);
	}

	@Test
	public void testEnumRange() {
		Set<E> set = ImmutableUtil.enumRange(E.A, E.BC);
		assertImmutableCollection(set);
		assertCollection(set, E.A, E.ABC, E.BC);
		set = ImmutableUtil.enumRange(E.ABC, E.BC);
		assertImmutableCollection(set);
		assertCollection(set, E.ABC, E.BC);
		set = ImmutableUtil.enumRange(E.A, E.A);
		assertImmutableCollection(set);
		assertCollection(set, E.A);
		assertThrown(() -> ImmutableUtil.enumRange(E.BC, E.A));
	}

	@Test
	public void testUnboundedEnumRange() {
		Set<E> set = ImmutableUtil.enumRange(null, null);
		assertImmutableCollection(set);
		assertCollection(set);
		set = ImmutableUtil.enumRange(null, E.ABC);
		assertImmutableCollection(set);
		assertCollection(set, E.A, E.ABC);
		set = ImmutableUtil.enumRange(E.ABC, null);
		assertImmutableCollection(set);
		assertCollection(set, E.ABC, E.BC);
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
		assertCollection(set, "1", "2", "3");
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

	@Test(expected = UnsupportedOperationException.class)
	public void testIterableShouldNotAllowRemovals() {
		List<String> list = new ArrayList<>();
		Collections.addAll(list, "A", "B", "C");
		Iterable<String> iterable = ImmutableUtil.iterable(list);
		Iterator<String> iterator = iterable.iterator();
		assertEquals(iterator.next(), "A");
		iterator.remove();
	}

	@Test
	public void testAsList() {
		final List<Integer> list = ImmutableUtil.asList(new Integer[] { 1, 2, 3, 4, 5 });
		assertEquals(list, Arrays.asList(1, 2, 3, 4, 5));
		assertImmutableList(list);
	}

	@Test
	public void testCopyAsMapOfSets() {
		assertTrue(ImmutableUtil.copyAsMapOfSets(new HashMap<>()).isEmpty());
		Map<String, Set<Integer>> srcMap = testMap( //
			"123", asSet(1, 2, 3), "4", asSet(4), "", asSet(), "null", null);
		Map<String, Set<Integer>> copy = testMap( //
			"123", asSet(1, 2, 3), "4", asSet(4), "", asSet(), "null", null);
		final Map<String, Set<Integer>> map = ImmutableUtil.copyAsMapOfSets(srcMap);
		assertTrue(srcMap.get("123").remove(1));
		assertNotNull(srcMap.remove("4"));
		assertEquals(map, copy);
		assertImmutableMap(map);
	}

	@Test
	public void testCopyAsMapOfLists() {
		assertNull(ImmutableUtil.copyAsMapOfLists(null));
		assertTrue(ImmutableUtil.copyAsMapOfLists(new HashMap<>()).isEmpty());
		Map<String, List<Integer>> srcMap = testMap( //
			"123", asList(1, 2, 3), "4", asList(4), "", asList(), "null", null);
		Map<String, List<Integer>> copy = testMap( //
			"123", asList(1, 2, 3), "4", asList(4), "", asList(), "null", null);
		final Map<String, List<Integer>> map = ImmutableUtil.copyAsMapOfLists(srcMap);
		assertEquals(srcMap.get("123").set(0, -1), 1);
		assertNotNull(srcMap.remove("4"));
		assertEquals(map, copy);
		assertImmutableMap(map);
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
	public void testCopyAsNavigableSet() {
		assertIterable(ImmutableUtil.copyAsNavigableSet(asSet()));
		Set<Integer> srcSet = new HashSet<>();
		Collections.addAll(srcSet, 2, 3, 1, 5, 4);
		final NavigableSet<Integer> set = ImmutableUtil.copyAsNavigableSet(srcSet);
		srcSet.remove(0);
		assertIterable(set, 1, 2, 3, 4, 5);
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
		assertIterable(map.keySet(), 1, 2, 3, 4, 5);
		assertImmutableMap(map);
	}

	@Test
	public void testCollectAsList() {
		List<String> list = ImmutableUtil.collectAsList(Stream.of("1", "2", "3", "4", "5"));
		assertEquals(list, Arrays.asList("1", "2", "3", "4", "5"));
		assertImmutableList(list);
	}

	@Test
	public void testCollectAsSet() {
		Set<String> set = ImmutableUtil.collectAsSet(Stream.of("1", "2", "3", "4", "5"));
		assertEquals(set, asSet("1", "2", "3", "4", "5"));
		assertImmutableCollection(set);
	}

	@Test
	public void testCollectAsNavigableSet() {
		NavigableSet<String> set =
			ImmutableUtil.collectAsNavigableSet(Stream.of("4", "1", "3", "5", "2"));
		assertEquals(set, asSet("1", "2", "3", "4", "5"));
		assertImmutableCollection(set);
	}

	@Test
	public void testConvertAsList() {
		List<Integer> list =
			ImmutableUtil.convertAsList(Integer::parseInt, "1", "2", "3", "4", "5");
		assertEquals(list, Arrays.asList(1, 2, 3, 4, 5));
		assertImmutableList(list);
	}

	@Test
	public void testConvertAsSet() {
		Set<Integer> set = ImmutableUtil.convertAsSet(Integer::parseInt, "1", "2", "3", "4", "5");
		assertEquals(set, new HashSet<>(Arrays.asList(1, 2, 3, 4, 5)));
		assertImmutableCollection(set);
	}

	@Test
	public void testConvertAsNavigableSet() {
		NavigableSet<Integer> set =
			ImmutableUtil.convertAsNavigableSet(Integer::parseInt, "5", "4", "1", "3", "2");
		assertIterable(set, 1, 2, 3, 4, 5);
		assertImmutableCollection(set);
	}

	@Test
	public void testConvertAsMap() {
		Map<String, Integer> map = ImmutableUtil.convertAsMap(String::valueOf, 1, 3, 2);
		assertImmutableMap(map);
		assertEquals(map, Map.of("1", 1, "3", 3, "2", 2));
		map = ImmutableUtil.convertAsMap(String::valueOf, i -> i + 1, new Integer[] { 1, 3, 2 });
		assertEquals(map, Map.of("1", 2, "3", 4, "2", 3));
		map = ImmutableUtil.convertAsMap(String::valueOf, i -> i + 1, List.of(1, 3, 2));
		assertEquals(map, Map.of("1", 2, "3", 4, "2", 3));
	}

	@Test
	public void testConvertStreamAsMap() {
		Map<String, Integer> map = ImmutableUtil.convertAsMap(String::valueOf, Stream.of(1, 3, 2));
		assertImmutableMap(map);
		assertEquals(map, Map.of("1", 1, "3", 3, "2", 2));
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
				Map<Object, Object> objMap = BasicUtil.uncheckedCast(map);
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
