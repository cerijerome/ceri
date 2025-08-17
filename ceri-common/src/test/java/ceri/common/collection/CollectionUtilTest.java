package ceri.common.collection;

import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertFalse;
import static ceri.common.test.AssertUtil.assertIoe;
import static ceri.common.test.AssertUtil.assertMap;
import static ceri.common.test.AssertUtil.assertNull;
import static ceri.common.test.AssertUtil.assertOrdered;
import static ceri.common.test.AssertUtil.assertPrivateConstructor;
import static ceri.common.test.AssertUtil.assertRte;
import static ceri.common.test.AssertUtil.assertTrue;
import static ceri.common.test.AssertUtil.assertUnordered;
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
		assertEquals(CollectionUtil.empty(Immutable.listOf((String) null)), false);
		assertEquals(CollectionUtil.empty(List.of("a")), false);
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
		var map = Immutable.mapOf(1, "1", 2, "2", 3, null);
		assertEquals(CollectionUtil.getAdapted(map, 1, (k, v) -> v.repeat(k)), "1");
		assertEquals(CollectionUtil.getAdapted(map, 2, (k, v) -> v.repeat(k)), "22");
		assertEquals(CollectionUtil.getAdapted(map, 3, (k, v) -> v.repeat(k)), null);
	}

	@Test
	public void testFill() {
		var list = Lists.ofAll(1, 2, 3, 4, 5);
		assertEquals(CollectionUtil.fill(list, 1, 2, 0), 3);
		assertOrdered(list, 1, 0, 0, 4, 5);
	}

	@Test
	public void testInsert() {
		var list1 = Lists.ofAll(1, 2, 3);
		var list2 = Lists.ofAll(4, 5, 6);
		assertOrdered(Lists.insert(list1, 1, list2), 1, 4, 5, 6, 2, 3);
	}

	@Test
	public void testToMap() {
		var list = List.of("A", "ABC", "AB");
		Map<?, ?> map = CollectionUtil.toMap(String::toLowerCase, String::length, list);
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
	public void testSortByValue() {
		var m = Maps.put(Maps.link(), 1, "1", 0, null, 4, "4", 2, "2", -2, "-2");
		var map = CollectionUtil.sortByValue(m);
		assertOrdered(map.keySet(), 0, -2, 1, 2, 4);
		assertOrdered(map.values(), null, "-2", "1", "2", "4");
	}

	@Test
	public void testCollectStream() {
		var set = CollectionUtil.collect(Stream.of("3", "1", "2"), TreeSet::new);
		assertOrdered(set, "1", "2", "3");
	}

	@Test
	public void testAddAll() {
		var list = CollectionUtil.addAll(new ArrayList<>(), "1", "2", "3");
		assertEquals(list, Arrays.asList("1", "2", "3"));
		assertOrdered(CollectionUtil.addAll(list, list), "1", "2", "3", "1", "2", "3");
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
		assertNull(CollectionUtil.last(List.of()));
		assertEquals(CollectionUtil.last(Arrays.asList("1", "2", "3")), "3");
		var ii = new LinkedHashSet<Integer>();
		assertNull(CollectionUtil.last(ii));
		Collections.addAll(ii, 1, 0, -1);
		assertEquals(CollectionUtil.last(ii), -1);
	}

	@Test
	public void testIntersect() {
		var list1 = Lists.ofAll(0, 1, 2, 3, 4);
		var list2 = Lists.ofAll(1, 3, 5, 7, 9);
		CollectionUtil.intersect(list1, list2);
		assertEquals(list1, Arrays.asList(1, 3));
	}

	@Test
	public void testRemoveAll() {
		var list = Lists.ofAll(0, 1, 2, 3, 4);
		CollectionUtil.removeAll(list, 1, 3, 5, 7, 9);
		assertEquals(list, Arrays.asList(0, 2, 4));
	}

	@Test
	public void testCollectionRemoveIf() {
		var set = new HashSet<>(Set.of("a", "ab", "abc"));
		assertEquals(CollectionUtil.removeIf(set, t -> t.contains("b")), 2);
		assertUnordered(set, "a");
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
		assertEquals(CollectionUtil.key(Map.of(), "a"), (Integer) null);
		var map = Immutable.mapOf(1, "1", -1, null, 2, "2", 22, "2", -2, null);
		assertEquals(CollectionUtil.key(map, "1"), 1);
		assertEquals(CollectionUtil.key(map, "2"), 2);
		assertEquals(CollectionUtil.key(map, new String("2")), 2);
		assertEquals(CollectionUtil.key(map, null), -1);
	}

	@Test
	public void testKeys() {
		var map = Immutable.mapOf(1, "1", -1, null, 2, "2", 22, "2", -2, null);
		assertUnordered(CollectionUtil.keys(map, "1"), 1);
		assertUnordered(CollectionUtil.keys(map, "2"), 2, 22);
		assertUnordered(CollectionUtil.keys(map, new String("2")), 2, 22);
		assertUnordered(CollectionUtil.keys(map, null), -1, -2);
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
}
