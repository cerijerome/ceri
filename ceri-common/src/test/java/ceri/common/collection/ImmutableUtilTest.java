package ceri.common.collection;

import static ceri.common.test.TestUtil.assertPrivateConstructor;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.Test;
import ceri.common.util.BasicUtil;

public class ImmutableUtilTest {

	@Test
	public void testConstructorIsPrivate() {
		assertPrivateConstructor(ImmutableUtil.class);
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
	public void testIterableShouldIterateItems() {
		List<String> list = new ArrayList<>();
		Collections.addAll(list, "A", "B", "C");
		int i = 0;
		for (String s : ImmutableUtil.iterable(list)) {
			assertThat(s, is(list.get(i++)));
		}
	}

	@Test(expected = UnsupportedOperationException.class)
	public void testIterableShouldNotAllowRemovals() {
		List<String> list = new ArrayList<>();
		Collections.addAll(list, "A", "B", "C");
		Iterable<String> iterable = ImmutableUtil.iterable(list);
		Iterator<String> iterator = iterable.iterator();
		assertThat(iterator.next(), is("A"));
		iterator.remove();
	}

	@Test
	public void testAsList() {
		final List<Integer> list = ImmutableUtil.asList(new Integer[] { 1, 2, 3, 4, 5 });
		assertThat(list, is(Arrays.asList(1, 2, 3, 4, 5)));
		assertImmutableList(list);
	}

	@Test
	public void testCopyAsList() {
		List<Integer> srcList = new ArrayList<>();
		Collections.addAll(srcList, 1, 2, 3, 4, 5);
		List<Integer> copy = new ArrayList<>(srcList);
		final List<Integer> list = ImmutableUtil.copyAsList(srcList);
		srcList.remove(0);
		assertThat(list, is(copy));
		assertImmutableList(list);
	}

	@Test
	public void testCopyAsSet() {
		Set<Integer> srcSet = new HashSet<>();
		Collections.addAll(srcSet, 1, 2, 3, 4, 5);
		Set<Integer> copy = new HashSet<>(srcSet);
		final Set<Integer> set = ImmutableUtil.copyAsSet(srcSet);
		srcSet.remove(0);
		assertThat(set, is(copy));
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
		assertThat(map, is(copy));
		assertImmutableMap(map);
	}

	@Test
	public void testConvertAsList() {
		List<Integer> list =
			ImmutableUtil.convertAsList((s) -> Integer.parseInt(s), "1", "2", "3", "4", "5");
		assertThat(list, is(Arrays.asList(1, 2, 3, 4, 5)));
		assertImmutableList(list);
	}

	@Test
	public void testConvertAsSet() {
		Set<Integer> set =
			ImmutableUtil.convertAsSet((s) -> Integer.parseInt(s), "1", "2", "3", "4", "5");
		assertThat(set, is(new HashSet<>(Arrays.asList(1, 2, 3, 4, 5))));
		assertImmutableCollection(set);
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
