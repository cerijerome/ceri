package ceri.common.stream;

import java.util.List;
import java.util.PrimitiveIterator;
import org.junit.Test;
import ceri.common.collect.Iterables;
import ceri.common.collect.Iterators;
import ceri.common.test.Assert;
import ceri.common.test.Captor;

public class IntStreamBehavior {

	@Test
	public void testOf() throws Exception {
		Assert.equal(IntStream.of((int[]) null).isEmpty(), true);
		Assert.equal(IntStream.of().isEmpty(), true);
		Assert.stream(IntStream.of(1, 2, 3), 1, 2, 3);
	}

	@Test
	public void testRange() {
		Assert.stream(IntStream.slice(-1, 0));
		Assert.stream(IntStream.slice(-1, 1), -1);
		Assert.stream(IntStream.slice(-1, 2), -1, 0);
	}

	@Test
	public void testFromIterable() {
		Assert.stream(IntStream.from((List<Number>) null));
		Assert.stream(IntStream.from(Iterables.ofNull()));
		Assert.stream(IntStream.from(() -> null));
		Assert.stream(IntStream.from(List.of(-1.1, 0.6, 1.9)), -1, 0, 1);
	}

	@Test
	public void testFromIterator() {
		Assert.stream(IntStream.from((PrimitiveIterator.OfInt) null));
		Assert.stream(IntStream.from(Iterators.nullInt));
		Assert.stream(IntStream.from(java.util.stream.IntStream.of(-1, 0, 1).iterator()), -1, 0, 1);
	}

	@Test
	public void shouldFilterElements() {
		Assert.stream(testStream().filter(null), -1, 0, 1, 0);
		Assert.stream(testStream().filter(i -> i != 0), -1, 1);
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
		Assert.stream(IntStream.empty().boxed());
		Assert.stream(testStream().boxed().map(String::valueOf), "-1", "0", "1", "0");
	}

	@Test
	public void shouldProvideUnsignedElements() throws Exception {
		Assert.stream(IntStream.empty().unsigned());
		Assert.array(testStream().unsigned().toArray(), 0xffffffffL, 0L, 1L, 0L);
	}

	@Test
	public void shouldMapElements() throws Exception {
		Assert.stream(IntStream.empty().map(null));
		Assert.stream(IntStream.empty().map(_ -> Assert.fail()));
		Assert.stream(testStream().map(null));
		Assert.stream(testStream().map(i -> i + 1), 0, 1, 2, 1);
	}

	@Test
	public void shouldMapElementsToDouble() throws Exception {
		Assert.stream(IntStream.empty().mapToDouble(null));
		Assert.stream(testStream().mapToDouble(i -> i), -1.0, 0.0, 1.0, 0.0);
	}

	@Test
	public void shouldFlatMapElements() throws Exception {
		Assert.stream(IntStream.empty().flatMap(null));
		Assert.stream(IntStream.empty().flatMap(_ -> Assert.fail()));
		Assert.stream(testStream().flatMap(null));
		Assert.stream(testStream().flatMap(i -> IntStream.of(i - 1, i + 1)), -2, 0, -1, 1, 0, 2, -1,
			1);
	}

	@Test
	public void shouldLimitElements() throws Exception {
		Assert.stream(IntStream.empty().limit(3));
		Assert.stream(testStream().limit(0));
		Assert.stream(testStream().limit(2), -1, 0);
		Assert.stream(testStream().limit(5), -1, 0, 1, 0);
	}

	@Test
	public void shouldProvideDistinctElements() throws Exception {
		Assert.stream(IntStream.empty().distinct());
		Assert.stream(testStream().distinct(), -1, 0, 1);
	}

	@Test
	public void shouldProvideSortedElements() throws Exception {
		Assert.stream(IntStream.empty().sorted());
		Assert.stream(testStream().sorted(), -1, 0, 0, 1);
	}

	@Test
	public void shouldProvideNextElement() throws Exception {
		Assert.equal(IntStream.empty().next(), null);
		Assert.equal(IntStream.empty().next(3), 3);
		var stream = testStream();
		Assert.equal(stream.next(3), -1);
		Assert.equal(stream.next(), 0);
		Assert.equal(stream.next(), 1);
		Assert.equal(stream.next(3), 0);
		Assert.equal(stream.next(), null);
		Assert.equal(stream.next(3), 3);
	}

	@Test
	public void shouldSkipElements() {
		Assert.stream(testStream().skip(2), 1, 0);
		Assert.stream(testStream().skip(5));
	}

	@Test
	public void shouldDetermineIfEmpty() throws Exception {
		Assert.equal(IntStream.empty().isEmpty(), true);
		var stream = IntStream.of(1);
		Assert.equal(stream.isEmpty(), false);
		Assert.equal(stream.isEmpty(), true);
		Assert.equal(stream.isEmpty(), true);
	}

	@Test
	public void shouldDetermineCount() throws Exception {
		Assert.equal(IntStream.empty().count(), 0L);
		Assert.equal(testStream().count(), 4L);
	}

	@Test
	public void shouldProvideIterator() throws Exception {
		Assert.iterator(IntStream.empty().iterator());
		Assert.iterator(testStream().iterator(), -1, 0, 1, 0);
	}

	@Test
	public void shouldIterateForEach() throws Exception {
		var captor = Captor.of();
		IntStream.empty().forEach(captor::accept);
		captor.verify();
		testStream().forEach(captor::accept);
		captor.verify(-1, 0, 1, 0);
	}

	@Test
	public void shouldCollectElements() throws Exception {
		IntStream.empty().collect(Captor::of, Captor::accept).verify();
		Assert.equal(testStream().collect(null), null);
		Assert.equal(testStream().collect(null, (_, _) -> {}), null);
		Assert.equal(testStream().collect(() -> null, (_, _) -> {}), null);
		testStream().collect(Captor::of, Captor::accept).verify(-1, 0, 1, 0);
		Assert.array(testStream().collect(Collect.Ints.sortedArray), -1, 0, 0, 1);
	}

	@Test
	public void shouldDetermineMin() throws Exception {
		Assert.equal(IntStream.empty().min(0), 0);
		Assert.equal(testStream().min(0), -1);
	}

	@Test
	public void shouldDetermineMax() throws Exception {
		Assert.equal(IntStream.empty().max(0), 0);
		Assert.equal(testStream().max(0), 1);
	}

	@Test
	public void shouldDetermineSum() {
		Assert.equal(testStream().sum(), 0);
		Assert.equal(testStream().skip(2).sum(), 1);
	}

	@Test
	public void shouldDetermineAverage() {
		Assert.equal(testStream().average(), 0.0);
		Assert.equal(testStream().skip(2).average(), 0.5);
	}

	@Test
	public void shouldReduceElements() throws Exception {
		Assert.equal(IntStream.empty().reduce((_, _) -> 0), null);
		Assert.equal(IntStream.empty().reduce((_, _) -> 0, 3), 3);
		Assert.equal(testStream().reduce(null), null);
		Assert.equal(testStream().reduce(null, 3), 3);
		Assert.equal(testStream().filter(i -> i > 1).reduce((_, _) -> 0), null);
	}

	@Test
	public void shouldUseReducers() {
		Assert.equal(Streams.ints(7, 14).reduce(Reduce.Ints.and()), 6);
		Assert.equal(Streams.ints(7, 14).reduce(Reduce.Ints.or()), 15);
		Assert.equal(Streams.ints(7, 14).reduce(Reduce.Ints.xor()), 9);
	}

	private static IntStream<RuntimeException> testStream() {
		return IntStream.of(-1, 0, 1, 0);
	}
}
