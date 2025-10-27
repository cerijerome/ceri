package ceri.common.stream;

import java.util.List;
import java.util.PrimitiveIterator;
import org.junit.Test;
import ceri.common.collect.Iterables;
import ceri.common.collect.Iterators;
import ceri.common.test.Assert;
import ceri.common.test.Captor;

public class LongStreamBehavior {

	@Test
	public void testOf() throws Exception {
		Assert.equal(LongStream.of((long[]) null).isEmpty(), true);
		Assert.equal(LongStream.of().isEmpty(), true);
		Assert.stream(LongStream.of(1, 2, 3), 1L, 2L, 3L);
	}

	@Test
	public void testRange() {
		Assert.stream(LongStream.slice(-1, 0));
		Assert.stream(LongStream.slice(-1, 1), -1L);
		Assert.stream(LongStream.slice(-1, 2), -1L, 0L);
	}

	@Test
	public void testFromIterable() {
		Assert.stream(LongStream.from((List<Number>) null));
		Assert.stream(LongStream.from(Iterables.ofNull()));
		Assert.stream(LongStream.from(List.of(-1.1, 0.6, 1.9)), -1L, 0L, 1L);
	}

	@Test
	public void testFromIterator() {
		Assert.stream(LongStream.from((PrimitiveIterator.OfLong) null));
		Assert.stream(LongStream.from(Iterators.nullLong));
		Assert.stream(LongStream.from(java.util.stream.LongStream.of(-1, 0, 1).iterator()), -1L, 0L,
			1L);
	}

	@Test
	public void shouldFilterElements() {
		Assert.stream(testStream().filter(null), -1L, 0L, 1L, 0L);
		Assert.stream(testStream().filter(i -> i != 0L), -1L, 1L);
	}

	@Test
	public void shouldMatchAny() {
		Assert.equal(testStream().anyMatch(null), true);
		Assert.equal(testStream().anyMatch(i -> i > 1), false);
		Assert.equal(testStream().anyMatch(i -> i > 0), true);
	}

	@Test
	public void shouldMatchAll() {
		Assert.equal(testStream().allMatch(null), true);
		Assert.equal(testStream().allMatch(i -> i > -1), false);
		Assert.equal(testStream().allMatch(i -> i >= -1), true);
	}

	@Test
	public void shouldMatchNone() {
		Assert.equal(testStream().noneMatch(null), false);
		Assert.equal(testStream().noneMatch(i -> i > 0), false);
		Assert.equal(testStream().noneMatch(i -> i > 1), true);
	}

	@Test
	public void shouldBoxElements() throws Exception {
		Assert.stream(LongStream.empty().boxed());
		Assert.stream(testStream().boxed().map(String::valueOf), "-1", "0", "1", "0");
	}

	@Test
	public void shouldCastElementsToInt() throws Exception {
		Assert.stream(LongStream.empty().ints());
		Assert.stream(testStream().ints(), -1, 0, 1, 0);
	}

	@Test
	public void shouldMapElements() throws Exception {
		Assert.stream(LongStream.empty().map(null));
		Assert.stream(LongStream.empty().map(_ -> Assert.fail()));
		Assert.stream(testStream().map(null));
		Assert.stream(testStream().map(i -> i + 1), 0, 1, 2, 1);
	}

	@Test
	public void shouldMapElementsToInt() throws Exception {
		Assert.stream(LongStream.empty().mapToInt(null));
		Assert.stream(testStream().mapToInt(i -> (int) i), -1, 0, 1, 0);
	}

	@Test
	public void shouldMapElementsToDouble() throws Exception {
		Assert.stream(LongStream.empty().mapToDouble(null));
		Assert.stream(testStream().mapToDouble(i -> i), -1.0, 0.0, 1.0, 0.0);
	}

	@Test
	public void shouldFlatMapElements() throws Exception {
		Assert.stream(LongStream.empty().flatMap(null));
		Assert.stream(LongStream.empty().flatMap(_ -> Assert.fail()));
		Assert.stream(testStream().flatMap(null));
		Assert.stream(testStream().flatMap(i -> LongStream.of(i - 1, i + 1)), -2, 0, -1, 1, 0, 2, -1,
			1);
	}

	@Test
	public void shouldLimitElements() throws Exception {
		Assert.stream(LongStream.empty().limit(3));
		Assert.stream(testStream().limit(0));
		Assert.stream(testStream().limit(2), -1, 0);
		Assert.stream(testStream().limit(5), -1, 0, 1, 0);
	}

	@Test
	public void shouldProvideDistinctElements() throws Exception {
		Assert.stream(LongStream.empty().distinct());
		Assert.stream(testStream().distinct(), -1, 0, 1);
	}

	@Test
	public void shouldProvideSortedElements() throws Exception {
		Assert.stream(LongStream.empty().sorted());
		Assert.stream(testStream().sorted(), -1, 0, 0, 1);
	}

	@Test
	public void shouldProvideNextElement() throws Exception {
		Assert.equal(LongStream.empty().next(), null);
		Assert.equal(LongStream.empty().next(3), 3L);
		var stream = testStream();
		Assert.equal(stream.next(3), -1L);
		Assert.equal(stream.next(), 0L);
		Assert.equal(stream.next(), 1L);
		Assert.equal(stream.next(3), 0L);
		Assert.equal(stream.next(), null);
		Assert.equal(stream.next(3), 3L);
	}

	@Test
	public void shouldSkipElements() {
		Assert.stream(testStream().skip(2), 1, 0);
		Assert.stream(testStream().skip(5));
	}
	
	@Test
	public void shouldDetermineIfEmpty() throws Exception {
		Assert.equal(LongStream.empty().isEmpty(), true);
		var stream = LongStream.of(1);
		Assert.equal(stream.isEmpty(), false);
		Assert.equal(stream.isEmpty(), true);
		Assert.equal(stream.isEmpty(), true);
	}

	@Test
	public void shouldDetermineCount() throws Exception {
		Assert.equal(LongStream.empty().count(), 0L);
		Assert.equal(testStream().count(), 4L);
	}

	@Test
	public void shouldProvideIterator() throws Exception {
		Assert.iterator(LongStream.empty().iterator());
		Assert.iterator(testStream().iterator(), -1L, 0L, 1L, 0L);
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
		Assert.equal(testStream().collect(null), null);
		Assert.equal(testStream().collect(null, (_, _) -> {}), null);
		Assert.equal(testStream().collect(() -> null, (_, _) -> {}), null);
		testStream().collect(Captor::of, Captor::accept).verify(-1L, 0L, 1L, 0L);
		Assert.array(testStream().collect(Collect.Longs.sortedArray), -1, 0, 0, 1);
	}

	@Test
	public void shouldDetermineMin() throws Exception {
		Assert.equal(LongStream.empty().min(0L), 0L);
		Assert.equal(testStream().min(0L), -1L);
	}

	@Test
	public void shouldDetermineMax() throws Exception {
		Assert.equal(LongStream.empty().max(0L), 0L);
		Assert.equal(testStream().max(0L), 1L);
	}

	@Test
	public void shouldDetermineSum() {
		Assert.equal(testStream().sum(), 0L);
		Assert.equal(testStream().skip(2).sum(), 1L);
	}

	@Test
	public void shouldDetermineAverage() {
		Assert.equal(testStream().average(), 0.0);
		Assert.equal(testStream().skip(2).average(), 0.5);
	}

	@Test
	public void shouldReduceElements() throws Exception {
		Assert.equal(LongStream.empty().reduce((_, _) -> 0L), null);
		Assert.equal(LongStream.empty().reduce((_, _) -> 0L, 3), 3L);
		Assert.equal(testStream().reduce(null), null);
		Assert.equal(testStream().reduce(null, 3), 3L);
		Assert.equal(testStream().filter(i -> i > 1L).reduce((_, _) -> 0L), null);
	}

	private static LongStream<RuntimeException> testStream() {
		return LongStream.of(-1, 0, 1, 0);
	}
}
