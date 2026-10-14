package ceri.common.collect;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import org.junit.jupiter.api.Test;
import ceri.common.function.Excepts;
import ceri.common.function.Filters;
import ceri.common.function.Functions;
import ceri.common.test.Assert;
import ceri.common.test.Captor;

public class IterablesTest {
	private static final List<Integer> list = Lists.ofAll(-1, null, 1);
	private static final List<Integer> list2 = Lists.ofAll(-1, 1);
	private static final Iterable<Integer> nullIterable = () -> null;
	private static final Iterator<Integer> nullIterator = null;
	private static final List<Integer> nullList = null;
	private static final Integer[] nullArray = null;
	private static final Functions.Consumer<Object> nullConsumer = null;
	private static final Functions.Consumer<Object> emptyConsumer = _ -> {};
	private static final Functions.Predicate<Object> nullPredicate = null;
	private static final Functions.Predicate<Object> notNull = Filters.NON_NULL;
	private static final Excepts.Predicate<IOException, Object> notNullEx = notNull.castEx();
	private static final Functions.Predicate<Object> isNull = Filters.IS_NULL;
	private static final Excepts.Predicate<IOException, Object> isNullEx = isNull.castEx();
	private static final Functions.ObjIntPredicate<Object> gt0 = (_, i) -> i > 0;
	private static final Excepts.ObjIntPredicate<IOException, Object> gt0Ex = gt0.castEx();
	private static final Functions.ObjIntPredicate<Object> lt1 = (_, i) -> i < 1;
	private static final Excepts.ObjIntPredicate<IOException, Object> lt1Ex = lt1.castEx();

	private static Iterator<Integer> iter() {
		return list.iterator();
	}

	@Test
	public void testConstructorIsPrivate() {
		Assert.privateConstructor(Iterables.class, Iterables.Filter.class);
	}

	@Test
	public void testFilterCast() {
		Functions.Predicate<List<Integer>> filter =
			Iterables.Filter.cast(Iterables.Filter.any(notNull));
		Assert.equal(filter.test(list), true);
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
		Assert.equal(Iterables.Filter.hasAnyOf(nullArray).test(null), false);
		Assert.equal(Iterables.Filter.hasAny(nullList).test(null), false);
		var filter = Iterables.Filter.hasAnyOf(null, 1);
		Assert.equal(filter.test(null), false);
		Assert.equal(filter.test(Lists.ofAll(-1)), false);
		Assert.equal(filter.test(Lists.ofAll(1, -1, null)), true);
	}

	@Test
	public void testFilterAny() throws Exception {
		Assert.equal(Iterables.Filter.test(null, Iterables.Filter.any(notNull)), false);
		Assert.equal(Iterables.Filter.test(null, Iterables.Filter.any(isNull)), false);
		Assert.equal(Iterables.Filter.test(list, Iterables.Filter.any(null)), false);
		Assert.equal(Iterables.Filter.test(list, Iterables.Filter.any(notNull)), true);
		Assert.equal(Iterables.Filter.test(list, Iterables.Filter.any(isNull)), true);
		Assert.equal(Iterables.Filter.test(list2, Iterables.Filter.any(isNull)), false);
		Assert.equal(Iterables.Filter.test(null, Iterables.Filter.anyEx(notNullEx)), false);
		Assert.equal(Iterables.Filter.test(null, Iterables.Filter.anyEx(isNullEx)), false);
		Assert.equal(Iterables.Filter.test(list, Iterables.Filter.anyEx(null)), false);
		Assert.equal(Iterables.Filter.test(list, Iterables.Filter.anyEx(notNullEx)), true);
		Assert.equal(Iterables.Filter.test(list, Iterables.Filter.anyEx(isNullEx)), true);
		Assert.equal(Iterables.Filter.test(list2, Iterables.Filter.anyEx(isNullEx)), false);
	}

	@Test
	public void testFilterAll() throws Exception {
		Assert.equal(Iterables.Filter.test(null, Iterables.Filter.all(notNull)), false);
		Assert.equal(Iterables.Filter.test(null, Iterables.Filter.all(isNull)), false);
		Assert.equal(Iterables.Filter.test(list, Iterables.Filter.all(null)), false);
		Assert.equal(Iterables.Filter.test(list, Iterables.Filter.all(notNull)), false);
		Assert.equal(Iterables.Filter.test(list, Iterables.Filter.all(isNull)), false);
		Assert.equal(Iterables.Filter.test(list2, Iterables.Filter.all(notNull)), true);
		Assert.equal(Iterables.Filter.test(null, Iterables.Filter.allEx(notNullEx)), false);
		Assert.equal(Iterables.Filter.test(null, Iterables.Filter.allEx(isNullEx)), false);
		Assert.equal(Iterables.Filter.test(list, Iterables.Filter.allEx(null)), false);
		Assert.equal(Iterables.Filter.test(list, Iterables.Filter.allEx(notNullEx)), false);
		Assert.equal(Iterables.Filter.test(list, Iterables.Filter.allEx(isNullEx)), false);
		Assert.equal(Iterables.Filter.test(list2, Iterables.Filter.allEx(notNullEx)), true);
	}

	@Test
	public void testFilterAnyIndex() throws Exception {
		Assert.equal(Iterables.Filter.anyIndex(null).test(null), false);
		Assert.equal(Iterables.Filter.anyIndex(gt0).test(null), false);
		Assert.equal(Iterables.Filter.anyIndex(gt0).test(Lists.ofAll(1)), false);
		Assert.equal(Iterables.Filter.anyIndex(gt0).test(Lists.ofAll(1, -1)), true);
		Assert.equal(Iterables.Filter.anyIndexEx(null).test(null), false);
		Assert.equal(Iterables.Filter.anyIndexEx(gt0Ex).test(null), false);
		Assert.equal(Iterables.Filter.anyIndexEx(gt0Ex).test(Lists.ofAll(1)), false);
		Assert.equal(Iterables.Filter.anyIndexEx(gt0Ex).test(Lists.ofAll(1, -1)), true);
	}

	@Test
	public void testFilterAllIndex() throws Exception {
		Assert.equal(Iterables.Filter.allIndex(null).test(null), false);
		Assert.equal(Iterables.Filter.allIndex(lt1).test(null), false);
		Assert.equal(Iterables.Filter.allIndex(lt1).test(Lists.ofAll(1)), true);
		Assert.equal(Iterables.Filter.allIndex(lt1).test(Lists.ofAll(1, -1)), false);
		Assert.equal(Iterables.Filter.allIndexEx(null).test(null), false);
		Assert.equal(Iterables.Filter.allIndexEx(lt1Ex).test(null), false);
		Assert.equal(Iterables.Filter.allIndexEx(lt1Ex).test(Lists.ofAll(1)), true);
		Assert.equal(Iterables.Filter.allIndexEx(lt1Ex).test(Lists.ofAll(1, -1)), false);
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
		Assert.equal(Iterables.first(Iterables.ofNull()), null);
		Assert.equal(Iterables.first(Iterables.ofNull(), 0), 0);
		Assert.equal(Iterables.first(list), -1);
		Assert.equal(Iterables.first(list, 0), -1);
	}

	@Test
	public void testNth() {
		Assert.equal(Iterables.nth(null, 1), null);
		Assert.equal(Iterables.nth(null, 1, 0), 0);
		Assert.equal(Iterables.nth(nullIterable, 1), null);
		Assert.equal(Iterables.nth(nullIterable, 1, 0), 0);
		Assert.equal(Iterables.nth(Iterables.ofNull(), 1), null);
		Assert.equal(Iterables.nth(Iterables.ofNull(), 1, 0), 0);
		Assert.equal(Iterables.nth(list, 1), null);
		Assert.equal(Iterables.nth(list, 1, 0), null);
	}

	@Test
	public void testForEach() {
		Assert.equal(Iterables.forEach(null, emptyConsumer), 0);
		Assert.equal(Iterables.forEach(nullIterable, emptyConsumer), 0);
		Assert.equal(Iterables.forEach(Iterables.ofNull(), emptyConsumer), 0);
		Assert.equal(Iterables.forEach(list, nullConsumer), 0);
		Assert.equal(Iterables.forEach(list, emptyConsumer), 3);
		Captor.of().apply(c -> Assert.equal(Iterables.forEach(list, c), 3)).verify(-1, null, 1);
	}

	@Test
	public void testRemoveIf() {
		Assert.equal(Iterables.removeIf(null, notNull), 0);
		Assert.equal(Iterables.removeIf(nullIterable, notNull), 0);
		Assert.equal(Iterables.removeIf(Iterables.ofNull(), notNull), 0);
		Assert.equal(Iterables.removeIf(list, nullPredicate), 0);
		assertList(Lists.ofAll(-1, null, 1),
			l -> Assert.equal(Iterables.removeIf(l, nullPredicate), 0), -1, null, 1);
		assertList(Lists.ofAll(-1, null, 1), l -> Assert.equal(Iterables.removeIf(l, notNull), 2),
			(Integer) null);
	}

	@SafeVarargs
	private static <E extends Exception, T, L extends List<T>> void assertList(L list,
		Excepts.Consumer<E, L> consumer, T... expected) throws E {
		consumer.accept(list);
		Assert.ordered(list, expected);
	}
}
