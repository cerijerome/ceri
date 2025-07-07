package ceri.common.collection;

import static ceri.common.test.AssertUtil.assertCollection;
import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertFalse;
import static ceri.common.test.AssertUtil.assertIoe;
import static ceri.common.test.AssertUtil.assertIterable;
import static ceri.common.test.AssertUtil.assertMap;
import static ceri.common.test.AssertUtil.assertNull;
import static ceri.common.test.AssertUtil.assertPrivateConstructor;
import static ceri.common.test.AssertUtil.assertRte;
import static ceri.common.test.AssertUtil.assertThrown;
import static ceri.common.test.AssertUtil.assertTrue;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Stream;
import org.junit.Test;
import ceri.common.function.FunctionTestUtil.E;
import ceri.common.test.Captor;

public class CollectionUtilTest {

	@Test
	public void testConstructorIsPrivate() {
		assertPrivateConstructor(CollectionUtil.class);
	}

	@Test
	public void testEmptyCollection() {
		assertEquals(CollectionUtil.empty((Collection<?>) null), true);
		assertEquals(CollectionUtil.empty(List.of()), true);
		assertEquals(CollectionUtil.empty(ImmutableUtil.asList((String) null)), false);
		assertEquals(CollectionUtil.empty(List.of("a")), false);
	}

	@Test
	public void testNonEmptyCollection() {
		assertEquals(CollectionUtil.nonEmpty((Collection<?>) null), false);
		assertEquals(CollectionUtil.nonEmpty(List.of()), false);
		assertEquals(CollectionUtil.nonEmpty(ImmutableUtil.asList((String) null)), true);
		assertEquals(CollectionUtil.nonEmpty(List.of("a")), true);
	}

	@Test
	public void testEmptyMap() {
		assertEquals(CollectionUtil.empty((Map<?, ?>) null), true);
		assertEquals(CollectionUtil.empty(Map.of()), true);
		assertEquals(CollectionUtil.empty(ImmutableUtil.asMap(null, null)), false);
		assertEquals(CollectionUtil.empty(Map.of("a", 1)), false);
	}

	@Test
	public void testNonEmptyMap() {
		assertEquals(CollectionUtil.nonEmpty((Map<?, ?>) null), false);
		assertEquals(CollectionUtil.nonEmpty(Map.of()), false);
		assertEquals(CollectionUtil.nonEmpty(ImmutableUtil.asMap(null, null)), true);
		assertEquals(CollectionUtil.nonEmpty(Map.of("a", 1)), true);
	}

	@Test
	public void testForEachMapEntry() {
		var map = ImmutableUtil.asMap("1", 1, "2", 2, "3", 3);
		var captor = Captor.ofBi();
		CollectionUtil.forEach(map, (k, v) -> captor.accept(k, v));
		captor.first.verify("1", "2", "3");
		captor.second.verify(1, 2, 3);
	}

	@Test
	public void testForEachIterable() {
		var capturer = Captor.ofInt();
		assertIoe(() -> CollectionUtil.forEach(Arrays.asList(1, 2, 3), E.consumer));
		assertRte(() -> CollectionUtil.forEach(Arrays.asList(0, 1, 2), E.consumer));
		CollectionUtil.forEach(Arrays.asList(1, 2, 3), capturer.reset()::accept);
		capturer.verify(1, 2, 3);
	}

	@Test
	public void testComputeMapping() {
		var map = new LinkedHashMap<String, Integer>();
		assertEquals(CollectionUtil.compute(map, "abc", (k, _) -> k.length()), 3);
		assertEquals(CollectionUtil.compute(map, "abc", (k, v) -> k.length() + v), 6);
		assertEquals(CollectionUtil.compute(map, "abc", (_, _) -> null), null);
		assertEquals(map.size(), 0);
	}

	@Test
	public void testComputeMappingIfAbsent() {
		var map = new LinkedHashMap<String, Integer>();
		assertEquals(CollectionUtil.computeIfAbsent(map, "abc", k -> k.length()), 3);
		assertEquals(CollectionUtil.computeIfAbsent(map, "abc", k -> k.length() - 1), 3);
		assertEquals(CollectionUtil.computeIfAbsent(map, "abc", _ -> null), 3);
		assertEquals(map.size(), 1);
	}

	@Test
	public void testComputeMappingIfPresent() {
		var map = new LinkedHashMap<String, Integer>();
		assertEquals(CollectionUtil.computeIfPresent(map, "abc", (k, _) -> k.length()), null);
		assertEquals(map.put("abc", 0), null);
		assertEquals(CollectionUtil.computeIfPresent(map, "abc", (k, _) -> k.length()), 3);
		assertEquals(CollectionUtil.computeIfPresent(map, "abc", (_, _) -> null), null);
		assertEquals(map.size(), 0);
	}

	@Test
	public void testReplaceAllMappings() {
		var map = new LinkedHashMap<String, Integer>();
		CollectionUtil.replaceAll(map, (k, _) -> k.length());
		assertEquals(map.put("a", 0), null);
		assertEquals(map.put("abc", 0), null);
		CollectionUtil.replaceAll(map, (k, _) -> k.length());
		assertEquals(map.get("a"), 1);
		assertEquals(map.get("abc"), 3);
		CollectionUtil.replaceAll(map, (_, _) -> null);
		assertEquals(map.get("a"), null);
		assertEquals(map.get("abc"), null);
		assertEquals(map.size(), 2);
	}

	@Test
	public void testMergedMappedValue() {
		var map = new LinkedHashMap<String, Integer>();
		assertEquals(CollectionUtil.merge(map, "abc", 1, (_, _) -> 3), 1);
		assertEquals(CollectionUtil.merge(map, "abc", 1, (_, _) -> 3), 3);
		assertEquals(CollectionUtil.merge(map, "abc", 1, (v1, v2) -> v1 + v2), 4);
		assertEquals(map.size(), 1);
	}

	@Test
	public void testContainsAll() {
		assertFalse(CollectionUtil.containsAll(null));
		assertTrue(CollectionUtil.containsAll(List.of()));
		assertFalse(CollectionUtil.containsAll(List.of(1), List.<Integer>of()));
		assertFalse(CollectionUtil.containsAll(List.of(), 1));
		Integer[] nullArray = null;
		assertFalse(CollectionUtil.containsAll(List.of(), nullArray));
		var list = List.of(-1, 0, 1);
		assertFalse(CollectionUtil.containsAll(list, -2, 2));
		assertFalse(CollectionUtil.containsAll(list, -2, -1));
		assertTrue(CollectionUtil.containsAll(list, -1, 0));
	}

	@Test
	public void testContainsAny() {
		assertFalse(CollectionUtil.containsAny(null));
		assertFalse(CollectionUtil.containsAny(List.of()));
		assertFalse(CollectionUtil.containsAny(List.of(), (List<?>) null));
		var empty = List.<Integer>of();
		assertFalse(CollectionUtil.containsAny(List.of(1), empty));
		assertFalse(CollectionUtil.containsAny(List.of(), 1));
		var list = List.of(-1, 0, 1);
		assertFalse(CollectionUtil.containsAny(list, -2, 2));
		assertTrue(CollectionUtil.containsAny(list, -2, -1));
	}

	@Test
	public void testGetOrDefault() {
		var list = List.of(100, 10, 1000);
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
		var list = ArrayUtil.asList(1, 2, 3, 4, 5);
		assertEquals(CollectionUtil.fill(list, 1, 2, 0), 3);
		assertIterable(list, 1, 0, 0, 4, 5);
	}

	@Test
	public void testInsert() {
		var list1 = ArrayUtil.asList(1, 2, 3);
		var list2 = ArrayUtil.asList(4, 5, 6);
		assertEquals(CollectionUtil.insert(list2, 1, list1, 1, 2), 3);
		assertIterable(list1, 1, 5, 6, 2, 3);
	}

	@Test
	public void testCopy() {
		var list1 = ArrayUtil.asList(1, 2, 3);
		var list2 = ArrayUtil.asList(4, 5, 6);
		assertEquals(CollectionUtil.copy(list2, 1, list1, 1, 2), 3);
		assertIterable(list1, 1, 5, 6);
	}

	@Test
	public void testTransformValues() {
		var map = MapPopulator.of(1, "1", 3, "333", 2, "22").map;
		var imap = CollectionUtil.transformValues(String::length, map);
		assertCollection(imap.keySet(), 1, 3, 2);
		assertCollection(imap.values(), 1, 3, 2);
		imap = CollectionUtil.transformValues((i, s) -> i + s.length(), map);
		assertCollection(imap.keySet(), 1, 3, 2);
		assertCollection(imap.values(), 2, 6, 4);
	}

	@Test
	public void testTransformKeys() {
		var map = MapPopulator.of(1.1, "1.10", 3.3, "3.3000", 2.2, "2.200").map;
		var imap = CollectionUtil.transformKeys(Double::intValue, map);
		assertCollection(imap.keySet(), 1, 3, 2);
		assertCollection(imap.values(), "1.10", "3.3000", "2.200");
		imap = CollectionUtil.transformKeys((d, s) -> d.intValue() + s.length(), map);
		assertCollection(imap.keySet(), 5, 9, 7);
		assertCollection(imap.values(), "1.10", "3.3000", "2.200");
	}

	@Test
	public void testTransform() {
		var map = MapPopulator.of(1.1, "1.10", 3.3, "3.3000", 2.2, "2.200").map;
		var imap = CollectionUtil.transform(Double::intValue, String::length, map);
		assertCollection(imap.keySet(), 1, 3, 2);
		assertCollection(imap.values(), 4, 6, 5);
		imap = CollectionUtil.transform((_, s) -> s.length(), (d, _) -> d.intValue(), map);
		assertCollection(imap.keySet(), 4, 6, 5);
		assertCollection(imap.values(), 1, 3, 2);
	}

	@Test
	public void testToMap() {
		var list = List.of("A", "ABC", "AB");
		Map<?, ?> map = CollectionUtil.toMap(String::length, list);
		assertEquals(map, Map.of(1, "A", 3, "ABC", 2, "AB"));
		map = CollectionUtil.toMap(String::toLowerCase, String::length, list);
		assertEquals(map, Map.of("a", 1, "abc", 3, "ab", 2));
	}

	@Test
	public void testToIndexMap() {
		var map = CollectionUtil.toIndexMap(List.of("A", "B", "C"));
		assertMap(map, 0, "A", 1, "B", 2, "C");
		map = CollectionUtil.toIndexMap((s, i) -> s + i, List.of("A", "B", "C"));
		assertMap(map, 0, "A0", 1, "B1", 2, "C2");
	}

	@Test
	public void testToList() {
		var map = MapPopulator.wrap(new LinkedHashMap<Integer, String>()).put(1, "1").put(0, null)
			.put(4, "4").put(2, "2").put(-2, "-2").map;
		var list = CollectionUtil.toList((i, s) -> String.valueOf(s) + i, map);
		assertIterable(list, "11", "null0", "44", "22", "-2-2");
	}

	@Test
	public void testJoinAsSet() {
		assertCollection(CollectionUtil.joinAsSet("a", Arrays.asList(null, "b")), "a", "b", null);
		assertCollection(CollectionUtil.joinAsSet(null, Arrays.asList("a", "b")), "a", "b", null);
	}

	@Test
	public void testSortByValue() {
		var map = MapPopulator.wrap(new LinkedHashMap<Integer, String>()).put(1, "1").put(0, null)
			.put(4, "4").put(2, "2").put(-2, "-2").map;
		map = CollectionUtil.sortByValue(map);
		assertIterable(map.keySet(), 0, -2, 1, 2, 4);
		assertIterable(map.values(), null, "-2", "1", "2", "4");
	}

	@Test
	public void testToArray() {
		assertThrown(() -> CollectionUtil.toArray(Collections.emptyList(), Integer.TYPE));
		var numbers = CollectionUtil.toArray(Arrays.asList(1, 2, 3), Number.class);
		assertCollection(numbers, 1, 2, 3);
	}

	@Test
	public void testCollectStream() {
		var set = CollectionUtil.collect(Stream.of("3", "1", "2"), TreeSet::new);
		assertIterable(set, "1", "2", "3");
	}

	@Test
	public void testAddAll() {
		var list = CollectionUtil.addAll(new ArrayList<>(), "1", "2", "3");
		assertEquals(list, Arrays.asList("1", "2", "3"));
		assertIterable(CollectionUtil.addAll(list, list), "1", "2", "3", "1", "2", "3");
	}

	@Test
	public void testPutAll() {
		var map = new LinkedHashMap<>(Map.of("a", 1, "b", 2, "c", 3));
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
		var ii = new LinkedHashSet<Integer>();
		assertNull(CollectionUtil.last(ii));
		Collections.addAll(ii, 1, 0, -1);
		assertEquals(CollectionUtil.last(ii), -1);
	}

	@Test
	public void testFirst() {
		assertNull(CollectionUtil.first(null));
		assertNull(CollectionUtil.first(Collections.emptySet()));
		assertEquals(CollectionUtil.first(Arrays.asList("1", "2", "3")), "1");
		var set = new LinkedHashSet<String>();
		Collections.addAll(set, "1", "2", "3");
		assertEquals(CollectionUtil.first(set), "1");
	}

	@Test
	public void testGetFromSet() {
		var set = new LinkedHashSet<>(Arrays.asList("1", "2", "3"));
		assertEquals(CollectionUtil.get(0, set), "1");
		assertEquals(CollectionUtil.get(1, set), "2");
		assertEquals(CollectionUtil.get(2, set), "3");
		assertNull(CollectionUtil.get(-1, set));
		assertNull(CollectionUtil.get(3, set));
		assertNull(CollectionUtil.get(0, null));
	}

	@Test
	public void testIntersect() {
		var list1 = ArrayUtil.asList(0, 1, 2, 3, 4);
		var list2 = ArrayUtil.asList(1, 3, 5, 7, 9);
		CollectionUtil.intersect(list1, list2);
		assertEquals(list1, Arrays.asList(1, 3));
	}

	@Test
	public void testRemoveAll() {
		var list = ArrayUtil.asList(0, 1, 2, 3, 4);
		CollectionUtil.removeAll(list, 1, 3, 5, 7, 9);
		assertEquals(list, Arrays.asList(0, 2, 4));
	}

	@Test
	public void testCollectionRemoveIf() {
		var set = new HashSet<>(Set.of("a", "ab", "abc"));
		assertEquals(CollectionUtil.removeIf(set, t -> t.contains("b")), 2);
		assertCollection(set, "a");
	}

	@Test
	public void testMapRemoveIf() {
		var map = new HashMap<>(Map.of("a", 1, "ab", 2, "abc", 3));
		assertEquals(CollectionUtil.removeIf(map, (k, _) -> k.contains("b")), 2);
		assertEquals(map, Map.of("a", 1));
	}

	@Test
	public void testReverse() {
		var list = Arrays.asList(1, 2, 3);
		assertEquals(CollectionUtil.reverse(list), List.of(3, 2, 1));
	}

	@Test
	public void testKey() {
		assertEquals(CollectionUtil.key(Collections.emptyMap(), "a"), (Integer) null);
		var map = MapPopulator.wrap(new LinkedHashMap<Integer, String>()).put(1, "1").put(-1, null)
			.put(2, "2").put(22, "2").put(-2, null).map;
		assertEquals(CollectionUtil.key(map, "1"), 1);
		assertEquals(CollectionUtil.key(map, "2"), 2);
		assertEquals(CollectionUtil.key(map, new String("2")), 2);
		assertEquals(CollectionUtil.key(map, null), -1);
	}

	@Test
	public void testKeys() {
		var map = MapPopulator.wrap(new LinkedHashMap<Integer, String>()).put(1, "1").put(-1, null)
			.put(2, "2").put(22, "2").put(-2, null).map;
		assertCollection(CollectionUtil.keys(map, "1"), 1);
		assertCollection(CollectionUtil.keys(map, "2"), 2, 22);
		assertCollection(CollectionUtil.keys(map, new String("2")), 2, 22);
		assertCollection(CollectionUtil.keys(map, null), -1, -2);
	}

	@Test
	public void shouldNotExceedMaxSizeOfCache() {
		var cache = CollectionUtil.putAll(CollectionUtil.fixedSizeCache(3),
			Map.of(1, "one", 2, "two", 3, "three"));
		cache.put(4, "four");
		assertEquals(cache.size(), 3);
		cache.put(5, "five");
		assertEquals(cache.size(), 3);
		cache.put(6, "six");
		assertEquals(cache.size(), 3);
	}

	@Test
	public void shouldRemoveOldestItemFromCache() {
		var cache = CollectionUtil.putAll(CollectionUtil.fixedSizeCache(3), Map.of(1, "one"));
		CollectionUtil.putAll(cache, Map.of(2, "two", 3, "three"));
		cache.put(4, "four");
		assertFalse(cache.containsKey(1));
	}

	@Test
	public void testIdentityHashSet() {
		var set = CollectionUtil.identityHashSet(Set.of("test"));
		set.add(new String("test"));
		set.add(new String("test"));
		assertEquals(set.size(), 3);
	}

}
