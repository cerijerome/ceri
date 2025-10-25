package ceri.common.collect;

import static ceri.common.collect.Iterators.ofNull;
import static ceri.common.test.Assert.assertEquals;
import static ceri.common.test.Assert.assertIterator;
import static ceri.common.test.Assert.noSuchElement;
import java.util.Iterator;
import java.util.List;
import org.junit.Test;
import ceri.common.function.Functions;
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
		noSuchElement(Iterators.ofNull()::next);
		noSuchElement(Iterators.nullInt::next);
		noSuchElement(Iterators.nullLong::next);
		noSuchElement(Iterators.nullDouble::next);
	}

	@Test
	public void testNext() {
		assertEquals(Iterators.next(null), null);
		assertEquals(Iterators.next(null, 0), 0);
		assertEquals(Iterators.next(nullIterator), null);
		assertEquals(Iterators.next(nullIterator, 0), 0);
		assertEquals(Iterators.next(ofNull()), null);
		assertEquals(Iterators.next(ofNull(), 0), 0);
		assertEquals(Iterators.next(iter()), -1);
		assertEquals(Iterators.next(iter(), 0), -1);
	}

	@Test
	public void testForEach() {
		assertEquals(Iterators.forEach(null, emptyConsumer), 0);
		assertEquals(Iterators.forEach(nullIterator, emptyConsumer), 0);
		assertEquals(Iterators.forEach(ofNull(), emptyConsumer), 0);
		assertEquals(Iterators.forEach(iter(), nullConsumer), 0);
		assertEquals(Iterators.forEach(iter(), emptyConsumer), 3);
		Captor.of().apply(c -> assertEquals(Iterators.forEach(iter(), c), 3)).verify(-1, null, 1);
	}

	@Test
	public void testIndexed() {
		assertIterator(Iterators.indexed(null, _ -> 0));
		assertIterator(Iterators.indexed(_ -> true, null));
		assertIterator(Iterators.indexed(3, String::valueOf), "0", "1", "2");
	}

	@Test
	public void testIntIndexed() {
		assertIterator(Iterators.intIndexed(null, _ -> 0));
		assertIterator(Iterators.intIndexed(_ -> true, null));
		assertIterator(Iterators.intIndexed(3, i -> -i), 0, -1, -2);
	}

	@Test
	public void testLongIndexed() {
		assertIterator(Iterators.longIndexed(null, _ -> 0));
		assertIterator(Iterators.longIndexed(_ -> true, null));
		assertIterator(Iterators.longIndexed(3, l -> -l), 0L, -1L, -2L);
	}

	@Test
	public void testDoubleIndexed() {
		assertIterator(Iterators.doubleIndexed(null, _ -> 0));
		assertIterator(Iterators.doubleIndexed(_ -> true, null));
		assertIterator(Iterators.doubleIndexed(3, d -> -d), 0.0, -1.0, -2.0);
	}
}
