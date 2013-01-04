package ceri.common.test;

import static ceri.common.test.TestUtil.assertException;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import ceri.common.util.BasicUtil;

/**
 * Assertions to make sure various objects throw exceptions when mutable methods are called.
 */
public class TestImmutable {

	public static void assertImmutableMap(final Map<?, ?> map) {
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
	
	public static void assertImmutableList(final List<?> list) {
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
	
	public static void assertImmutableCollection(final Collection<?> collection) {
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

	public static void assertImmutableIterator(final Iterator<?> iterator) {
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
