package ceri.common.collection;

import static ceri.common.test.TestUtil.assertCollection;
import static ceri.common.test.TestUtil.assertException;
import static ceri.common.test.TestUtil.assertIterable;
import static ceri.common.test.TestUtil.assertPrivateConstructor;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.NavigableSet;
import java.util.NoSuchElementException;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import org.junit.Test;

public class CollectionUtilTest {

	@Test
	public void testConstructorIsPrivate() {
		assertPrivateConstructor(CollectionUtil.class);
	}

	@Test
	public void testInvert() {
		assertThat(CollectionUtil.invert(Map.of()), is(Map.of()));
		assertThat(CollectionUtil.invert(Map.of(1, "1")), is(Map.of("1", 1)));
		assertTrue(CollectionUtil.invert(Map.of(1, "1", 2, "1")).containsKey("1"));
	}

	@Test
	public void testFill() {
		List<Integer> list = ArrayUtil.asList(1, 2, 3, 4, 5);
		assertThat(CollectionUtil.fill(list, 1, 2, 0), is(3));
		assertIterable(list, 1, 0, 0, 4, 5);
	}

	@Test
	public void testInsert() {
		List<Integer> list1 = ArrayUtil.asList(1, 2, 3);
		List<Integer> list2 = ArrayUtil.asList(4, 5, 6);
		assertThat(CollectionUtil.insert(list2, 1, list1, 1, 2), is(3));
		assertIterable(list1, 1, 5, 6, 2, 3);
	}

	@Test
	public void testCopy() {
		List<Integer> list1 = ArrayUtil.asList(1, 2, 3);
		List<Integer> list2 = ArrayUtil.asList(4, 5, 6);
		assertThat(CollectionUtil.copy(list2, 1, list1, 1, 2), is(3));
		assertIterable(list1, 1, 5, 6);
	}

	@Test
	public void testTransformValues() {
		Map<Integer, String> map = MapPopulator.of(1, "1", 3, "333", 2, "22").map;
		Map<Integer, Integer> imap = CollectionUtil.transformValues(s -> s.length(), map);
		assertCollection(imap.keySet(), 1, 3, 2);
		assertCollection(imap.values(), 1, 3, 2);
		imap = CollectionUtil.transformValues((i, s) -> i + s.length(), map);
		assertCollection(imap.keySet(), 1, 3, 2);
		assertCollection(imap.values(), 2, 6, 4);
	}

	@Test
	public void testTransformKeys() {
		Map<Double, String> map = MapPopulator.of(1.1, "1.10", 3.3, "3.3000", 2.2, "2.200").map;
		Map<Integer, String> imap = CollectionUtil.transformKeys(d -> d.intValue(), map);
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
			CollectionUtil.transform(d -> d.intValue(), s -> s.length(), map);
		assertCollection(imap.keySet(), 1, 3, 2);
		assertCollection(imap.values(), 4, 6, 5);
		imap = CollectionUtil.transform((d, s) -> s.length(), (d, s) -> d.intValue(), map);
		assertCollection(imap.keySet(), 4, 6, 5);
		assertCollection(imap.values(), 1, 3, 2);
	}

	@Test
	public void testToMap() {
		List<String> list = List.of("A", "ABC", "AB");
		Map<?, ?> map = CollectionUtil.toMap(s -> s.length(), list);
		assertThat(map, is(Map.of(1, "A", 3, "ABC", 2, "AB")));
		map = CollectionUtil.toMap(s -> s.toLowerCase(), s -> s.length(), list);
		assertThat(map, is(Map.of("a", 1, "abc", 3, "ab", 2)));
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
	public void testIterable() {
		Properties props = new Properties();
		props.put("A", 1);
		props.put("B", 2);
		props.put("C", 3);
		Iterable<Object> iterable = CollectionUtil.iterable(props.propertyNames());
		Set<String> set = new HashSet<>();
		for (Object obj : iterable)
			set.add((String) obj);
		assertCollection(set, "A", "B", "C");
		assertException(() -> iterable.iterator().remove());
	}

	@Test
	public void testToArray() {
		assertException(() -> CollectionUtil.toArray(Collections.emptyList(), Integer.TYPE));
		Number[] numbers = CollectionUtil.toArray(Arrays.asList(1, 2, 3), Number.class);
		assertCollection(numbers, 1, 2, 3);
	}

	@Test
	public void testDefaultValueMap() {
		Map<String, Integer> map = new HashMap<>();
		map = CollectionUtil.defaultValueMap(map, 0);
		assertThat(map.get(""), is(0));
		assertTrue(map.isEmpty());
		map.put("", -1);
		assertThat(map.size(), is(1));
		assertTrue(map.containsKey(""));
		assertTrue(map.containsValue(-1));
		assertThat(map.get(""), is(-1));
	}

	@Test
	public void testReverseListIterator() {
		final Iterator<String> iterator =
			CollectionUtil.reverseListIterator(ArrayUtil.asList("A", "B", "C"));
		assertThat(iterator.next(), is("C"));
		iterator.remove();
		assertThat(iterator.next(), is("B"));
		assertThat(iterator.next(), is("A"));
		assertFalse(iterator.hasNext());
		assertException(NoSuchElementException.class, () -> iterator.next());
	}

	@Test
	public void testAddAll() {
		List<String> list = CollectionUtil.addAll(new ArrayList<String>(), "1", "2", "3");
		assertThat(list, is(Arrays.asList("1", "2", "3")));
		assertThat(CollectionUtil.addAll(list, list),
			is(Arrays.asList("1", "2", "3", "1", "2", "3")));
	}

	@Test
	public void testFirst() {
		assertNull(CollectionUtil.first(null));
		assertNull(CollectionUtil.first(Collections.emptySet()));
		assertThat(CollectionUtil.first(Arrays.asList("1", "2", "3")), is("1"));
		Set<String> set = new LinkedHashSet<>();
		Collections.addAll(set, "1", "2", "3");
		assertThat(CollectionUtil.first(set), is("1"));
	}

	@Test
	public void testLast() {
		assertNull(CollectionUtil.last(null));
		assertNull(CollectionUtil.last(Collections.emptyList()));
		assertThat(CollectionUtil.last(Arrays.asList("1", "2", "3")), is("3"));
		Set<Integer> ii = new LinkedHashSet<>();
		assertNull(CollectionUtil.last(ii));
		Collections.addAll(ii, 1, 0, -1);
		assertThat(CollectionUtil.last(ii), is(-1));
	}

	@Test
	public void testIntersect() {
		List<Integer> list1 = ArrayUtil.asList(0, 1, 2, 3, 4);
		List<Integer> list2 = ArrayUtil.asList(1, 3, 5, 7, 9);
		CollectionUtil.intersect(list1, list2);
		assertThat(list1, is(Arrays.asList(1, 3)));
	}

	@Test
	public void testRemoveAll() {
		List<Integer> list = ArrayUtil.asList(0, 1, 2, 3, 4);
		CollectionUtil.removeAll(list, 1, 3, 5, 7, 9);
		assertThat(list, is(Arrays.asList(0, 2, 4)));
	}

	@Test
	public void testReverse() {
		List<Integer> list = Arrays.asList(1, 2, 3);
		assertThat(CollectionUtil.reverse(list), is(List.of(3, 2, 1)));
	}

	@Test(expected = NoSuchElementException.class)
	public void testReverseIterableList() {
		Iterator<Integer> iterator =
			CollectionUtil.reverseIterableList(Arrays.asList(1, 2, 3)).iterator();
		assertThat(iterator.next(), is(3));
		assertThat(iterator.next(), is(2));
		assertThat(iterator.next(), is(1));
		iterator.next();
	}

	@Test(expected = NoSuchElementException.class)
	public void testReverseIterableQueue() {
		Deque<Integer> queue = new ArrayDeque<>();
		Collections.addAll(queue, 1, 2, 3);
		Iterator<Integer> iterator = CollectionUtil.reverseIterableQueue(queue).iterator();
		assertThat(iterator.next(), is(3));
		assertThat(iterator.next(), is(2));
		assertThat(iterator.next(), is(1));
		iterator.next();
	}

	@Test(expected = NoSuchElementException.class)
	public void testReverseIterableSet() {
		NavigableSet<Integer> set = new TreeSet<>();
		Collections.addAll(set, 1, 2, 3);
		Iterator<Integer> iterator = CollectionUtil.reverseIterableSet(set).iterator();
		assertThat(iterator.next(), is(3));
		assertThat(iterator.next(), is(2));
		assertThat(iterator.next(), is(1));
		iterator.next();
	}

	@Test
	public void testKey() {
		assertThat(CollectionUtil.key(Collections.emptyMap(), "a"), is((Integer) null));
		Map<Integer, String> map = MapPopulator.wrap(new LinkedHashMap<Integer, String>()) //
			.put(1, "1").put(-1, null).put(2, "2").put(22, "2").put(-2, null).map;
		assertThat(CollectionUtil.key(map, "1"), is(1));
		assertThat(CollectionUtil.key(map, "2"), is(2));
		assertThat(CollectionUtil.key(map, new String("2")), is(2));
		assertThat(CollectionUtil.key(map, null), is(-1));
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
