package ceri.common.collection;

import static ceri.common.test.TestUtil.assertException;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
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
	public void testIterableShouldIterateItems() {
		List<String> list = new ArrayList<>();
		Collections.addAll(list, "A", "B", "C");
		int i = 0;
		for (String s : ImmutableUtil.iterable(list)) {
			assertThat(s, is(list.get(i++)));
		}
	}

	@Test(expected=UnsupportedOperationException.class)
	public void testIterableShouldNotAllowRemovals() {
		List<String> list = new ArrayList<>();
		Collections.addAll(list, "A", "B", "C");
		Iterable<String> iterable = ImmutableUtil.iterable(list);
		Iterator<String> iterator = iterable.iterator();
		assertThat(iterator.next(), is("A"));
		iterator.remove();
	}

	@Test
	public void testCopy() {
		final List<Integer> list = ImmutableUtil.asList(1, 2, 3, 4, 5);
		assertThat(list, is(Arrays.asList(1, 2, 3, 4, 5)));
		assertImmutableList(list);
	}

	@Test
	public void testArrayCopyAsList() {
		final List<Integer> list = ImmutableUtil.arrayAsList(new Integer[] { 1, 2, 3, 4, 5 });
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

	private static void assertImmutableMap(final Map<?, ?> map) {
		assertImmutableCollection(map.entrySet());
		assertImmutableCollection(map.keySet());
		assertImmutableCollection(map.values());
		assertException(new Runnable() {
			@Override
			public void run() {
				map.clear();
			}
		});
		assertException(new Runnable() {
			@Override
			public void run() {
				map.put(null, null);
			}
		});
		assertException(new Runnable() {
			@Override
			public void run() {
				Map<Object, Object> objMap = BasicUtil.uncheckedCast(map);
				objMap.putAll(map);
			}
		});
		assertException(new Runnable() {
			@Override
			public void run() {
				map.remove(null);
			}
		});
	}
	
	private static void assertImmutableList(final List<?> list) {
		assertImmutableCollection(list);
		assertImmutableIterator(list.listIterator());
		assertImmutableIterator(list.listIterator(0));
		assertException(new Runnable() {
			@Override
			public void run() {
				list.add(0, null);
			}
		});
		assertException(new Runnable() {
			@Override
			public void run() {
				list.addAll(0, null);
			}
		});
		assertException(new Runnable() {
			@Override
			public void run() {
				list.remove(0);
			}
		});
		assertException(new Runnable() {
			@Override
			public void run() {
				list.set(0, null);
			}
		});
	}
	
	private static void assertImmutableCollection(final Collection<?> collection) {
		assertImmutableIterator(collection.iterator());
		assertException(new Runnable() {
			@Override
			public void run() {
				collection.add(null);
			}
		});
		assertException(new Runnable() {
			@Override
			public void run() {
				collection.addAll(null);
			}
		});
		assertException(new Runnable() {
			@Override
			public void run() {
				collection.clear();
			}
		});
		assertException(new Runnable() {
			@Override
			public void run() {
				collection.remove(null);
			}
		});
		assertException(new Runnable() {
			@Override
			public void run() {
				collection.removeAll(Collections.emptySet());
			}
		});
		assertException(new Runnable() {
			@Override
			public void run() {
				collection.retainAll(Collections.emptySet());
			}
		});
	}

	private static void assertImmutableIterator(final Iterator<?> iterator) {
		if (!iterator.hasNext()) return;
		assertException(new Runnable() {
			@Override
			public void run() {
				iterator.next();
				iterator.remove();
			}
		});
	}
	
}
