package ceri.common.collection;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Deque;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.NavigableSet;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.TreeSet;
import org.junit.Test;

public class CollectionUtilTest {

	@Test(expected=NoSuchElementException.class)
	public void testReverseListIteratorShouldIterateListInReverse() {
		Iterator<String> iterator = CollectionUtil.reverseListIterator(Arrays.asList("A", "B", "C"));
		assertThat(iterator.next(), is("C"));
		assertThat(iterator.next(), is("B"));
		assertThat(iterator.next(), is("A"));
		iterator.next();
	}

	@Test
	public void testAddAll() {
		List<String> list = CollectionUtil.addAll(new ArrayList<String>(), "1", "2", "3");
		assertThat(list, is(Arrays.asList("1", "2", "3")));
	}

	@Test
	public void testFirst() {
		assertThat(CollectionUtil.first(Arrays.asList("1", "2", "3")), is("1"));
		Set<String> set = new LinkedHashSet<>();
		Collections.addAll(set, "1", "2", "3");
		assertThat(CollectionUtil.first(set), is("1"));
	}

	@Test
	public void testLast() {
		assertThat(CollectionUtil.last(Arrays.asList("1", "2", "3")), is("3"));
		Set<String> set = new LinkedHashSet<>();
		Collections.addAll(set, "1", "2", "3");
		assertThat(CollectionUtil.last(set), is("3"));
	}

	@Test
	public void testGet() {
		assertThat(CollectionUtil.get(Arrays.asList("1", "2", "3"), 1), is("2"));
		Set<String> set = new LinkedHashSet<>();
		Collections.addAll(set, "1", "2", "3");
		assertThat(CollectionUtil.get(set, 1), is("2"));
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
		Map<Integer, String> map = new LinkedHashMap<>();
		map.put(1, "1");
		map.put(-1, null);
		map.put(2, "2");
		map.put(22, "2");
		map.put(-2, null);
		assertThat(CollectionUtil.key(map, "1"), is(1));
		assertThat(CollectionUtil.key(map, "2"), is(2));
		assertThat(CollectionUtil.key(map, null), is(-1));
	}

	@Test
	public void testKeys() {
		Map<Integer, String> map = new LinkedHashMap<>();
		map.put(1, "1");
		map.put(-1, null);
		map.put(2, "2");
		map.put(22, "2");
		map.put(-2, null);
		assertEquals(CollectionUtil.keys(map, "1"), Collections.singleton(1));
		assertEquals(CollectionUtil.keys(map, "2"), new HashSet<>(Arrays.asList(2, 22)));
		assertEquals(CollectionUtil.keys(map, null), new HashSet<>(Arrays.asList(-1, -2)));
	}

}
