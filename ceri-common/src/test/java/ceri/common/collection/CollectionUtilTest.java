package ceri.common.collection;

import static ceri.common.test.AssertUtil.assertCollection;
import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertFalse;
import static ceri.common.test.AssertUtil.assertIterable;
import static ceri.common.test.AssertUtil.assertNull;
import static ceri.common.test.AssertUtil.assertPrivateConstructor;
import static ceri.common.test.AssertUtil.assertThrown;
import static ceri.common.test.AssertUtil.assertTrue;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import org.junit.Test;

public class CollectionUtilTest {

	@Test
	public void testConstructorIsPrivate() {
		assertPrivateConstructor(CollectionUtil.class);
	}

	@Test
	public void testContainsAll() {
		assertFalse(CollectionUtil.containsAll(null));
		assertTrue(CollectionUtil.containsAll(List.of()));
		assertFalse(CollectionUtil.containsAll(List.of(1), List.<Integer>of()));
		assertFalse(CollectionUtil.containsAll(List.of(), 1));
		Integer[] nullArray = null;
		assertFalse(CollectionUtil.containsAll(List.of(), nullArray));
		List<Integer> list = List.of(-1, 0, 1);
		assertFalse(CollectionUtil.containsAll(list, -2, 2));
		assertFalse(CollectionUtil.containsAll(list, -2, -1));
		assertTrue(CollectionUtil.containsAll(list, -1, 0));
	}

	@Test
	public void testContainsAny() {
		assertFalse(CollectionUtil.containsAny(null));
		assertFalse(CollectionUtil.containsAny(List.of()));
		assertFalse(CollectionUtil.containsAny(List.of(), (List<?>) null));
		List<Integer> empty = List.of();
		assertFalse(CollectionUtil.containsAny(List.of(1), empty));
		assertFalse(CollectionUtil.containsAny(List.of(), 1));
		List<Integer> list = List.of(-1, 0, 1);
		assertFalse(CollectionUtil.containsAny(list, -2, 2));
		assertTrue(CollectionUtil.containsAny(list, -2, -1));
	}

	@Test
	public void testGetOrDefault() {
		List<Integer> list = List.of(100, 10, 1000);
		assertNull(CollectionUtil.getOrDefault(list, -1, null));
		assertEquals(CollectionUtil.getOrDefault(list, -1, 1), 1);
		assertEquals(CollectionUtil.getOrDefault(list, 0, null), 100);
		assertEquals(CollectionUtil.getOrDefault(list, 3, -1), -1);
		assertEquals(CollectionUtil.getOrDefault(List.of(), 1, -1), -1);
	}

	@Test
	public void testGetAdapted() {
		var map = MapPopulator.of(1, "1", 2, "2", 3, null).map;
		assertEquals(CollectionUtil.getAdapted(map, 1, (k, v) -> v.repeat(k)), "1");
		assertEquals(CollectionUtil.getAdapted(map, 2, (k, v) -> v.repeat(k)), "22");
		assertEquals(CollectionUtil.getAdapted(map, 3, (k, v) -> v.repeat(k)), null);
	}

	@Test
	public void testInvert() {
		assertEquals(CollectionUtil.invert(Map.of()), Map.of());
		assertEquals(CollectionUtil.invert(Map.of(1, "1")), Map.of("1", 1));
		assertTrue(CollectionUtil.invert(Map.of(1, "1", 2, "1")).containsKey("1"));
	}

	@Test
	public void testFill() {
		List<Integer> list = ArrayUtil.asList(1, 2, 3, 4, 5);
		assertEquals(CollectionUtil.fill(list, 1, 2, 0), 3);
		assertIterable(list, 1, 0, 0, 4, 5);
	}

	@Test
	public void testInsert() {
		List<Integer> list1 = ArrayUtil.asList(1, 2, 3);
		List<Integer> list2 = ArrayUtil.asList(4, 5, 6);
		assertEquals(CollectionUtil.insert(list2, 1, list1, 1, 2), 3);
		assertIterable(list1, 1, 5, 6, 2, 3);
	}

	@Test
	public void testCopy() {
		List<Integer> list1 = ArrayUtil.asList(1, 2, 3);
		List<Integer> list2 = ArrayUtil.asList(4, 5, 6);
		assertEquals(CollectionUtil.copy(list2, 1, list1, 1, 2), 3);
		assertIterable(list1, 1, 5, 6);
	}

	@Test
	public void testTransformValues() {
		Map<Integer, String> map = MapPopulator.of(1, "1", 3, "333", 2, "22").map;
		Map<Integer, Integer> imap = CollectionUtil.transformValues(String::length, map);
		assertCollection(imap.keySet(), 1, 3, 2);
		assertCollection(imap.values(), 1, 3, 2);
		imap = CollectionUtil.transformValues((i, s) -> i + s.length(), map);
		assertCollection(imap.keySet(), 1, 3, 2);
		assertCollection(imap.values(), 2, 6, 4);
	}

	@Test
	public void testTransformKeys() {
		Map<Double, String> map = MapPopulator.of(1.1, "1.10", 3.3, "3.3000", 2.2, "2.200").map;
		Map<Integer, String> imap = CollectionUtil.transformKeys(Double::intValue, map);
		assertCollection(imap.keySet(), 1, 3, 2);
		assertCollection(imap.values(), "1.10", "3.3000", "2.200");
		imap = CollectionUtil.transformKeys((d, s) -> d.intValue() + s.length(), map);
		assertCollection(imap.keySet(), 5, 9, 7);
		assertCollection(imap.values(), "1.10", "3.3000", "2.200");
	}

	@Test
	public void testTransform() {
		Map<Double, String> map = MapPopulator.of(1.1, "1.10", 3.3, "3.3000", 2.2, "2.200").map;
		Map<Integer, Integer> imap =
			CollectionUtil.transform(Double::intValue, String::length, map);
		assertCollection(imap.keySet(), 1, 3, 2);
		assertCollection(imap.values(), 4, 6, 5);
		imap = CollectionUtil.transform((d, s) -> s.length(), (d, s) -> d.intValue(), map);
		assertCollection(imap.keySet(), 4, 6, 5);
		assertCollection(imap.values(), 1, 3, 2);
	}

	@Test
	public void testToMap() {
		List<String> list = List.of("A", "ABC", "AB");
		Map<?, ?> map = CollectionUtil.toMap(String::length, list);
		assertEquals(map, Map.of(1, "A", 3, "ABC", 2, "AB"));
		map = CollectionUtil.toMap(String::toLowerCase, String::length, list);
		assertEquals(map, Map.of("a", 1, "abc", 3, "ab", 2));
	}

	@Test
	public void testToList() {
		Map<Integer, String> map = MapPopulator.wrap(new LinkedHashMap<Integer, String>()) //
			.put(1, "1").put(0, null).put(4, "4").put(2, "2").put(-2, "-2").map;
		List<String> list = CollectionUtil.toList((i, s) -> String.valueOf(s) + i, map);
		assertIterable(list, "11", "null0", "44", "22", "-2-2");
	}

	@Test
	public void testSortByValue() {
		Map<Integer, String> map = MapPopulator.wrap(new LinkedHashMap<Integer, String>()) //
			.put(1, "1").put(0, null).put(4, "4").put(2, "2").put(-2, "-2").map;
		map = CollectionUtil.sortByValue(map);
		assertIterable(map.keySet(), 0, -2, 1, 2, 4);
		assertIterable(map.values(), null, "-2", "1", "2", "4");
	}

	@Test
	public void testToArray() {
		assertThrown(() -> CollectionUtil.toArray(Collections.emptyList(), Integer.TYPE));
		Number[] numbers = CollectionUtil.toArray(Arrays.asList(1, 2, 3), Number.class);
		assertCollection(numbers, 1, 2, 3);
	}

	@Test
	public void testAddAll() {
		List<String> list = CollectionUtil.addAll(new ArrayList<>(), "1", "2", "3");
		assertEquals(list, Arrays.asList("1", "2", "3"));
		assertIterable(CollectionUtil.addAll(list, list), "1", "2", "3", "1", "2", "3");
	}

	@Test
	public void testPutAll() {
		Map<String, Integer> map = new LinkedHashMap<>(Map.of("a", 1, "b", 2, "c", 3));
		assertEquals(map, Map.of("a", 1, "b", 2, "c", 3));
	}

	@Test
	public void testLastKey() {
		assertNull(CollectionUtil.lastKey(new TreeMap<>()));
		assertEquals(CollectionUtil.lastKey(new TreeMap<>(Map.of(0, "0", -1, "-1", 1, "1"))), 1);
	}

	@Test
	public void testLast() {
		assertNull(CollectionUtil.last(null));
		assertNull(CollectionUtil.last(Collections.emptyList()));
		assertEquals(CollectionUtil.last(Arrays.asList("1", "2", "3")), "3");
		Set<Integer> ii = new LinkedHashSet<>();
		assertNull(CollectionUtil.last(ii));
		Collections.addAll(ii, 1, 0, -1);
		assertEquals(CollectionUtil.last(ii), -1);
	}

	@Test
	public void testFirst() {
		assertNull(CollectionUtil.first(null));
		assertNull(CollectionUtil.first(Collections.emptySet()));
		assertEquals(CollectionUtil.first(Arrays.asList("1", "2", "3")), "1");
		Set<String> set = new LinkedHashSet<>();
		Collections.addAll(set, "1", "2", "3");
		assertEquals(CollectionUtil.first(set), "1");
	}

	@Test
	public void testGetFromSet() {
		Set<String> set = new LinkedHashSet<>(Arrays.asList("1", "2", "3"));
		assertEquals(CollectionUtil.get(0, set), "1");
		assertEquals(CollectionUtil.get(1, set), "2");
		assertEquals(CollectionUtil.get(2, set), "3");
		assertNull(CollectionUtil.get(-1, set));
		assertNull(CollectionUtil.get(3, set));
		assertNull(CollectionUtil.get(0, null));
	}

	@Test
	public void testIntersect() {
		List<Integer> list1 = ArrayUtil.asList(0, 1, 2, 3, 4);
		List<Integer> list2 = ArrayUtil.asList(1, 3, 5, 7, 9);
		CollectionUtil.intersect(list1, list2);
		assertEquals(list1, Arrays.asList(1, 3));
	}

	@Test
	public void testRemoveAll() {
		List<Integer> list = ArrayUtil.asList(0, 1, 2, 3, 4);
		CollectionUtil.removeAll(list, 1, 3, 5, 7, 9);
		assertEquals(list, Arrays.asList(0, 2, 4));
	}

	@Test
	public void testCollectionRemoveIf() {
		Set<String> set = new HashSet<>(Set.of("a", "ab", "abc"));
		assertEquals(CollectionUtil.removeIf(set, t -> t.contains("b")), 2);
		assertCollection(set, "a");
	}

	@Test
	public void testMapRemoveIf() {
		Map<String, Integer> map = new HashMap<>(Map.of("a", 1, "ab", 2, "abc", 3));
		assertEquals(CollectionUtil.removeIf(map, (k, v) -> k.contains("b")), 2);
		assertEquals(map, Map.of("a", 1));
	}

	@Test
	public void testReverse() {
		List<Integer> list = Arrays.asList(1, 2, 3);
		assertEquals(CollectionUtil.reverse(list), List.of(3, 2, 1));
	}

	@Test
	public void testKey() {
		assertEquals(CollectionUtil.key(Collections.emptyMap(), "a"), (Integer) null);
		Map<Integer, String> map = MapPopulator.wrap(new LinkedHashMap<Integer, String>()) //
			.put(1, "1").put(-1, null).put(2, "2").put(22, "2").put(-2, null).map;
		assertEquals(CollectionUtil.key(map, "1"), 1);
		assertEquals(CollectionUtil.key(map, "2"), 2);
		assertEquals(CollectionUtil.key(map, new String("2")), 2);
		assertEquals(CollectionUtil.key(map, null), -1);
	}

	@Test
	public void testKeys() {
		Map<Integer, String> map = MapPopulator.wrap(new LinkedHashMap<Integer, String>()) //
			.put(1, "1").put(-1, null).put(2, "2").put(22, "2").put(-2, null).map;
		assertCollection(CollectionUtil.keys(map, "1"), 1);
		assertCollection(CollectionUtil.keys(map, "2"), 2, 22);
		assertCollection(CollectionUtil.keys(map, new String("2")), 2, 22);
		assertCollection(CollectionUtil.keys(map, null), -1, -2);
	}

}
