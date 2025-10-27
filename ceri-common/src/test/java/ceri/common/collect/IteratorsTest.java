package ceri.common.collect;

import java.util.Iterator;
import java.util.List;
import org.junit.Test;
import ceri.common.function.Functions;
import ceri.common.test.Assert;
import ceri.common.test.Captor;

public class IteratorsTest {
	private static final List<Integer> list = Lists.ofAll(-1, null, 1);
	private static final Iterator<Integer> nullIterator = null;
	private static final Functions.Consumer<Object> nullConsumer = null;
	private static final Functions.Consumer<Object> emptyConsumer = _ -> {};

	private static Iterator<Integer> iter() {
		return list.iterator();
	}

	@Test
	public void testNull() {
		Assert.noSuchElement(Iterators.ofNull()::next);
		Assert.noSuchElement(Iterators.nullInt::next);
		Assert.noSuchElement(Iterators.nullLong::next);
		Assert.noSuchElement(Iterators.nullDouble::next);
	}

	@Test
	public void testNext() {
		Assert.equal(Iterators.next(null), null);
		Assert.equal(Iterators.next(null, 0), 0);
		Assert.equal(Iterators.next(nullIterator), null);
		Assert.equal(Iterators.next(nullIterator, 0), 0);
		Assert.equal(Iterators.next(Iterators.ofNull()), null);
		Assert.equal(Iterators.next(Iterators.ofNull(), 0), 0);
		Assert.equal(Iterators.next(iter()), -1);
		Assert.equal(Iterators.next(iter(), 0), -1);
	}

	@Test
	public void testForEach() {
		Assert.equal(Iterators.forEach(null, emptyConsumer), 0);
		Assert.equal(Iterators.forEach(nullIterator, emptyConsumer), 0);
		Assert.equal(Iterators.forEach(Iterators.ofNull(), emptyConsumer), 0);
		Assert.equal(Iterators.forEach(iter(), nullConsumer), 0);
		Assert.equal(Iterators.forEach(iter(), emptyConsumer), 3);
		Captor.of().apply(c -> Assert.equal(Iterators.forEach(iter(), c), 3)).verify(-1, null, 1);
	}

	@Test
	public void testIndexed() {
		Assert.iterator(Iterators.indexed(null, _ -> 0));
		Assert.iterator(Iterators.indexed(_ -> true, null));
		Assert.iterator(Iterators.indexed(3, String::valueOf), "0", "1", "2");
	}

	@Test
	public void testIntIndexed() {
		Assert.iterator(Iterators.intIndexed(null, _ -> 0));
		Assert.iterator(Iterators.intIndexed(_ -> true, null));
		Assert.iterator(Iterators.intIndexed(3, i -> -i), 0, -1, -2);
	}

	@Test
	public void testLongIndexed() {
		Assert.iterator(Iterators.longIndexed(null, _ -> 0));
		Assert.iterator(Iterators.longIndexed(_ -> true, null));
		Assert.iterator(Iterators.longIndexed(3, l -> -l), 0L, -1L, -2L);
	}

	@Test
	public void testDoubleIndexed() {
		Assert.iterator(Iterators.doubleIndexed(null, _ -> 0));
		Assert.iterator(Iterators.doubleIndexed(_ -> true, null));
		Assert.iterator(Iterators.doubleIndexed(3, d -> -d), 0.0, -1.0, -2.0);
	}
}
