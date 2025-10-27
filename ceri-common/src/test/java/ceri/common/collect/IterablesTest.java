package ceri.common.collect;

import static ceri.common.collect.Iterables.ofNull;
import java.util.Iterator;
import java.util.List;
import org.junit.Test;
import ceri.common.function.Excepts;
import ceri.common.function.Filters;
import ceri.common.function.Functions;
import ceri.common.test.Assert;
import ceri.common.test.Captor;

public class IterablesTest {
	private static final List<Integer> list = Lists.ofAll(-1, null, 1);
	private static final Iterable<Integer> nullIterable = () -> null;
	private static final Iterator<Integer> nullIterator = null;
	private static final List<Integer> nullList = null;
	private static final Integer[] nullArray = null;
	private static final Functions.Consumer<Object> nullConsumer = null;
	private static final Functions.Consumer<Object> emptyConsumer = _ -> {};
	private static final Functions.Predicate<Object> nullPredicate = null;
	private static final Functions.Predicate<Object> predicate = t -> t != null;

	private static Iterator<Integer> iter() {
		return list.iterator();
	}

	@Test
	public void testConstructorIsPrivate() {
		Assert.privateConstructor(Iterables.class, Iterables.Filter.class);
	}

	@Test
	public void testFilterHas() throws Exception {
		var filter = Iterables.Filter.has(null);
		Assert.equal(filter.test(null), false);
		Assert.equal(filter.test(Lists.ofAll(1, -1)), false);
		Assert.equal(filter.test(Lists.ofAll(1, -1, null)), true);
	}

	@Test
	public void testFilterHasAny() throws Exception {
		Assert.equal(Iterables.Filter.hasAny(nullArray).test(null), false);
		Assert.equal(Iterables.Filter.hasAny(nullList).test(null), false);
		var filter = Iterables.Filter.hasAny(null, 1);
		Assert.equal(filter.test(null), false);
		Assert.equal(filter.test(Lists.ofAll(-1)), false);
		Assert.equal(filter.test(Lists.ofAll(1, -1, null)), true);
	}

	@Test
	public void testFilterAll() throws Exception {
		Assert.equal(Iterables.Filter.all(null).test(null), false);
		Assert.equal(Iterables.Filter.all(Filters.nonNull()).test(null), false);
		Assert.equal(Iterables.Filter.all(Filters.nonNull()).test(Lists.ofAll(1, null)), false);
		Assert.equal(Iterables.Filter.all(Filters.nonNull()).test(Lists.ofAll(1, -1)), true);
	}

	@Test
	public void testFilterAnyIndex() throws Exception {
		Assert.equal(Iterables.Filter.anyIndex(null).test(null), false);
		Assert.equal(Iterables.Filter.anyIndex((_, i) -> i > 0).test(null), false);
		Assert.equal(Iterables.Filter.anyIndex((_, i) -> i > 0).test(Lists.ofAll(1)), false);
		Assert.equal(Iterables.Filter.anyIndex((_, i) -> i > 0).test(Lists.ofAll(1, -1)), true);
	}

	@Test
	public void testFilterAllIndex() throws Exception {
		Assert.equal(Iterables.Filter.allIndex(null).test(null), false);
		Assert.equal(Iterables.Filter.allIndex((_, i) -> i < 1).test(null), false);
		Assert.equal(Iterables.Filter.allIndex((_, i) -> i < 1).test(Lists.ofAll(1)), true);
		Assert.equal(Iterables.Filter.allIndex((_, i) -> i < 1).test(Lists.ofAll(1, -1)), false);
	}

	@Test
	public void testOf() {
		Assert.ordered(Iterables.of(nullIterator));
		Assert.ordered(Iterables.of(Iterators.ofNull()));
		Assert.ordered(Iterables.of(iter()), -1, null, 1);
	}

	@Test
	public void testFirst() {
		Assert.equal(Iterables.first(null), null);
		Assert.equal(Iterables.first(null, 0), 0);
		Assert.equal(Iterables.first(nullIterable), null);
		Assert.equal(Iterables.first(nullIterable, 0), 0);
		Assert.equal(Iterables.first(ofNull()), null);
		Assert.equal(Iterables.first(ofNull(), 0), 0);
		Assert.equal(Iterables.first(list), -1);
		Assert.equal(Iterables.first(list, 0), -1);
	}

	@Test
	public void testNth() {
		Assert.equal(Iterables.nth(null, 1), null);
		Assert.equal(Iterables.nth(null, 1, 0), 0);
		Assert.equal(Iterables.nth(nullIterable, 1), null);
		Assert.equal(Iterables.nth(nullIterable, 1, 0), 0);
		Assert.equal(Iterables.nth(ofNull(), 1), null);
		Assert.equal(Iterables.nth(ofNull(), 1, 0), 0);
		Assert.equal(Iterables.nth(list, 1), null);
		Assert.equal(Iterables.nth(list, 1, 0), null);
	}

	@Test
	public void testForEach() {
		Assert.equal(Iterables.forEach(null, emptyConsumer), 0);
		Assert.equal(Iterables.forEach(nullIterable, emptyConsumer), 0);
		Assert.equal(Iterables.forEach(ofNull(), emptyConsumer), 0);
		Assert.equal(Iterables.forEach(list, nullConsumer), 0);
		Assert.equal(Iterables.forEach(list, emptyConsumer), 3);
		Captor.of().apply(c -> Assert.equal(Iterables.forEach(list, c), 3)).verify(-1, null, 1);
	}

	@Test
	public void testRemoveIf() {
		Assert.equal(Iterables.removeIf(null, predicate), 0);
		Assert.equal(Iterables.removeIf(nullIterable, predicate), 0);
		Assert.equal(Iterables.removeIf(ofNull(), predicate), 0);
		Assert.equal(Iterables.removeIf(list, nullPredicate), 0);
		assertList(Lists.ofAll(-1, null, 1),
			l -> Assert.equal(Iterables.removeIf(l, nullPredicate), 0), -1, null, 1);
		assertList(Lists.ofAll(-1, null, 1), l -> Assert.equal(Iterables.removeIf(l, predicate), 2),
			(Integer) null);
	}

	@SafeVarargs
	private static <E extends Exception, T, L extends List<T>> void assertList(L list,
		Excepts.Consumer<E, L> consumer, T... expected) throws E {
		consumer.accept(list);
		Assert.ordered(list, expected);
	}
}
