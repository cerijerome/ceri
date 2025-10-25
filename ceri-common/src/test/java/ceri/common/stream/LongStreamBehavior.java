package ceri.common.stream;

import static ceri.common.test.Assert.assertArray;
import static ceri.common.test.Assert.assertEquals;
import static ceri.common.test.Assert.assertIterator;
import static ceri.common.test.Assert.assertStream;
import static ceri.common.test.Assert.fail;
import java.util.List;
import java.util.PrimitiveIterator;
import org.junit.Test;
import ceri.common.collect.Iterables;
import ceri.common.collect.Iterators;
import ceri.common.test.Captor;

public class LongStreamBehavior {

	@Test
	public void testOf() throws Exception {
		assertEquals(LongStream.of((long[]) null).isEmpty(), true);
		assertEquals(LongStream.of().isEmpty(), true);
		assertStream(LongStream.of(1, 2, 3), 1L, 2L, 3L);
	}

	@Test
	public void testRange() {
		assertStream(LongStream.slice(-1, 0));
		assertStream(LongStream.slice(-1, 1), -1L);
		assertStream(LongStream.slice(-1, 2), -1L, 0L);
	}

	@Test
	public void testFromIterable() {
		assertStream(LongStream.from((List<Number>) null));
		assertStream(LongStream.from(Iterables.ofNull()));
		assertStream(LongStream.from(List.of(-1.1, 0.6, 1.9)), -1L, 0L, 1L);
	}

	@Test
	public void testFromIterator() {
		assertStream(LongStream.from((PrimitiveIterator.OfLong) null));
		assertStream(LongStream.from(Iterators.nullLong));
		assertStream(LongStream.from(java.util.stream.LongStream.of(-1, 0, 1).iterator()), -1L, 0L,
			1L);
	}

	@Test
	public void shouldFilterElements() {
		assertStream(testStream().filter(null), -1L, 0L, 1L, 0L);
		assertStream(testStream().filter(i -> i != 0L), -1L, 1L);
	}

	@Test
	public void shouldMatchAny() {
		assertEquals(testStream().anyMatch(null), true);
		assertEquals(testStream().anyMatch(i -> i > 1), false);
		assertEquals(testStream().anyMatch(i -> i > 0), true);
	}

	@Test
	public void shouldMatchAll() {
		assertEquals(testStream().allMatch(null), true);
		assertEquals(testStream().allMatch(i -> i > -1), false);
		assertEquals(testStream().allMatch(i -> i >= -1), true);
	}

	@Test
	public void shouldMatchNone() {
		assertEquals(testStream().noneMatch(null), false);
		assertEquals(testStream().noneMatch(i -> i > 0), false);
		assertEquals(testStream().noneMatch(i -> i > 1), true);
	}

	@Test
	public void shouldBoxElements() throws Exception {
		assertStream(LongStream.empty().boxed());
		assertStream(testStream().boxed().map(String::valueOf), "-1", "0", "1", "0");
	}

	@Test
	public void shouldCastElementsToInt() throws Exception {
		assertStream(LongStream.empty().ints());
		assertStream(testStream().ints(), -1, 0, 1, 0);
	}

	@Test
	public void shouldMapElements() throws Exception {
		assertStream(LongStream.empty().map(null));
		assertStream(LongStream.empty().map(_ -> fail()));
		assertStream(testStream().map(null));
		assertStream(testStream().map(i -> i + 1), 0, 1, 2, 1);
	}

	@Test
	public void shouldMapElementsToInt() throws Exception {
		assertStream(LongStream.empty().mapToInt(null));
		assertStream(testStream().mapToInt(i -> (int) i), -1, 0, 1, 0);
	}

	@Test
	public void shouldMapElementsToDouble() throws Exception {
		assertStream(LongStream.empty().mapToDouble(null));
		assertStream(testStream().mapToDouble(i -> i), -1.0, 0.0, 1.0, 0.0);
	}

	@Test
	public void shouldFlatMapElements() throws Exception {
		assertStream(LongStream.empty().flatMap(null));
		assertStream(LongStream.empty().flatMap(_ -> fail()));
		assertStream(testStream().flatMap(null));
		assertStream(testStream().flatMap(i -> LongStream.of(i - 1, i + 1)), -2, 0, -1, 1, 0, 2, -1,
			1);
	}

	@Test
	public void shouldLimitElements() throws Exception {
		assertStream(LongStream.empty().limit(3));
		assertStream(testStream().limit(0));
		assertStream(testStream().limit(2), -1, 0);
		assertStream(testStream().limit(5), -1, 0, 1, 0);
	}

	@Test
	public void shouldProvideDistinctElements() throws Exception {
		assertStream(LongStream.empty().distinct());
		assertStream(testStream().distinct(), -1, 0, 1);
	}

	@Test
	public void shouldProvideSortedElements() throws Exception {
		assertStream(LongStream.empty().sorted());
		assertStream(testStream().sorted(), -1, 0, 0, 1);
	}

	@Test
	public void shouldProvideNextElement() throws Exception {
		assertEquals(LongStream.empty().next(), null);
		assertEquals(LongStream.empty().next(3), 3L);
		var stream = testStream();
		assertEquals(stream.next(3), -1L);
		assertEquals(stream.next(), 0L);
		assertEquals(stream.next(), 1L);
		assertEquals(stream.next(3), 0L);
		assertEquals(stream.next(), null);
		assertEquals(stream.next(3), 3L);
	}

	@Test
	public void shouldSkipElements() {
		assertStream(testStream().skip(2), 1, 0);
		assertStream(testStream().skip(5));
	}
	
	@Test
	public void shouldDetermineIfEmpty() throws Exception {
		assertEquals(LongStream.empty().isEmpty(), true);
		var stream = LongStream.of(1);
		assertEquals(stream.isEmpty(), false);
		assertEquals(stream.isEmpty(), true);
		assertEquals(stream.isEmpty(), true);
	}

	@Test
	public void shouldDetermineCount() throws Exception {
		assertEquals(LongStream.empty().count(), 0L);
		assertEquals(testStream().count(), 4L);
	}

	@Test
	public void shouldProvideIterator() throws Exception {
		assertIterator(LongStream.empty().iterator());
		assertIterator(testStream().iterator(), -1L, 0L, 1L, 0L);
	}

	@Test
	public void shouldIterateForEach() throws Exception {
		var captor = Captor.of();
		LongStream.empty().forEach(captor::accept);
		captor.verify();
		testStream().forEach(captor::accept);
		captor.verify(-1L, 0L, 1L, 0L);
	}

	@Test
	public void shouldCollectElements() throws Exception {
		LongStream.empty().collect(Captor::of, Captor::accept).verify();
		assertEquals(testStream().collect(null), null);
		assertEquals(testStream().collect(null, (_, _) -> {}), null);
		assertEquals(testStream().collect(() -> null, (_, _) -> {}), null);
		testStream().collect(Captor::of, Captor::accept).verify(-1L, 0L, 1L, 0L);
		assertArray(testStream().collect(Collect.Longs.sortedArray), -1, 0, 0, 1);
	}

	@Test
	public void shouldDetermineMin() throws Exception {
		assertEquals(LongStream.empty().min(0L), 0L);
		assertEquals(testStream().min(0L), -1L);
	}

	@Test
	public void shouldDetermineMax() throws Exception {
		assertEquals(LongStream.empty().max(0L), 0L);
		assertEquals(testStream().max(0L), 1L);
	}

	@Test
	public void shouldDetermineSum() {
		assertEquals(testStream().sum(), 0L);
		assertEquals(testStream().skip(2).sum(), 1L);
	}

	@Test
	public void shouldDetermineAverage() {
		assertEquals(testStream().average(), 0.0);
		assertEquals(testStream().skip(2).average(), 0.5);
	}

	@Test
	public void shouldReduceElements() throws Exception {
		assertEquals(LongStream.empty().reduce((_, _) -> 0L), null);
		assertEquals(LongStream.empty().reduce((_, _) -> 0L, 3), 3L);
		assertEquals(testStream().reduce(null), null);
		assertEquals(testStream().reduce(null, 3), 3L);
		assertEquals(testStream().filter(i -> i > 1L).reduce((_, _) -> 0L), null);
	}

	private static LongStream<RuntimeException> testStream() {
		return LongStream.of(-1, 0, 1, 0);
	}
}
