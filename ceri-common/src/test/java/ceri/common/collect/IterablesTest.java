package ceri.common.collect;

import static ceri.common.collect.Iterables.ofNull;
import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertOrdered;
import java.util.Iterator;
import java.util.List;
import org.junit.Test;
import ceri.common.function.Excepts;
import ceri.common.function.Functions;
import ceri.common.test.Captor;

public class IterablesTest {
	private static final List<Integer> list = Lists.ofAll(-1, null, 1);
	private static final Iterable<Integer> nullIterable = () -> null;
	private static final Iterator<Integer> nullIterator = null;
	private static final Functions.Consumer<Object> nullConsumer = null;
	private static final Functions.Consumer<Object> emptyConsumer = _ -> {};
	private static final Functions.Predicate<Object> nullPredicate = null;
	private static final Functions.Predicate<Object> predicate = t -> t != null;

	private static Iterator<Integer> iter() {
		return list.iterator();
	}

	@Test
	public void testOf() {
		assertOrdered(Iterables.of(nullIterator));
		assertOrdered(Iterables.of(Iterators.ofNull()));
		assertOrdered(Iterables.of(iter()), -1, null, 1);
	}

	@Test
	public void testFirst() {
		assertEquals(Iterables.first(null), null);
		assertEquals(Iterables.first(null, 0), 0);
		assertEquals(Iterables.first(nullIterable), null);
		assertEquals(Iterables.first(nullIterable, 0), 0);
		assertEquals(Iterables.first(ofNull()), null);
		assertEquals(Iterables.first(ofNull(), 0), 0);
		assertEquals(Iterables.first(list), -1);
		assertEquals(Iterables.first(list, 0), -1);
	}

	@Test
	public void testNth() {
		assertEquals(Iterables.nth(null, 1), null);
		assertEquals(Iterables.nth(null, 1, 0), 0);
		assertEquals(Iterables.nth(nullIterable, 1), null);
		assertEquals(Iterables.nth(nullIterable, 1, 0), 0);
		assertEquals(Iterables.nth(ofNull(), 1), null);
		assertEquals(Iterables.nth(ofNull(), 1, 0), 0);
		assertEquals(Iterables.nth(list, 1), null);
		assertEquals(Iterables.nth(list, 1, 0), null);
	}

	@Test
	public void testForEach() {
		assertEquals(Iterables.forEach(null, emptyConsumer), 0);
		assertEquals(Iterables.forEach(nullIterable, emptyConsumer), 0);
		assertEquals(Iterables.forEach(ofNull(), emptyConsumer), 0);
		assertEquals(Iterables.forEach(list, nullConsumer), 0);
		assertEquals(Iterables.forEach(list, emptyConsumer), 3);
		Captor.of().apply(c -> assertEquals(Iterables.forEach(list, c), 3)).verify(-1, null, 1);
	}

	@Test
	public void testRemoveIf() {
		assertEquals(Iterables.removeIf(null, predicate), 0);
		assertEquals(Iterables.removeIf(nullIterable, predicate), 0);
		assertEquals(Iterables.removeIf(ofNull(), predicate), 0);
		assertEquals(Iterables.removeIf(list, nullPredicate), 0);
		assertList(Lists.ofAll(-1, null, 1),
			l -> assertEquals(Iterables.removeIf(l, nullPredicate), 0), -1, null, 1);
		assertList(Lists.ofAll(-1, null, 1), l -> assertEquals(Iterables.removeIf(l, predicate), 2),
			(Integer) null);
	}

	@SafeVarargs
	private static <E extends Exception, T, L extends List<T>> void assertList(L list,
		Excepts.Consumer<E, L> consumer, T... expected) throws E {
		consumer.accept(list);
		assertOrdered(list, expected);
	}
}
