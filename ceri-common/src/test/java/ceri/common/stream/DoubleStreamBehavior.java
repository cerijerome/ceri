package ceri.common.stream;

import java.util.List;
import java.util.PrimitiveIterator;
import org.junit.Test;
import ceri.common.collect.Iterables;
import ceri.common.collect.Iterators;
import ceri.common.test.Assert;
import ceri.common.test.Captor;

public class DoubleStreamBehavior {

	@Test
	public void testOf() throws Exception {
		Assert.equal(DoubleStream.of((double[]) null).isEmpty(), true);
		Assert.equal(DoubleStream.of().isEmpty(), true);
		Assert.stream(DoubleStream.of(1, 2, 3), 1, 2, 3);
	}

	@Test
	public void testSegment() throws Exception {
		Assert.stream(Streams.segment(0));
		Assert.stream(Streams.segment(1), 0.0);
		Assert.stream(Streams.segment(2), 0.0, 1.0);
		Assert.stream(Streams.segment(3), 0.0, 0.5, 1.0);
	}

	@Test
	public void testFromIterable() {
		Assert.stream(DoubleStream.from((List<Number>) null));
		Assert.stream(DoubleStream.from(Iterables.ofNull()));
		Assert.stream(DoubleStream.from(List.of(-1, 0, 1)), -1.0, 0.0, 1.0);
	}

	@Test
	public void testFromIterator() {
		Assert.stream(DoubleStream.from((PrimitiveIterator.OfDouble) null));
		Assert.stream(DoubleStream.from(Iterators.nullDouble));
		Assert.stream(DoubleStream.from(java.util.stream.DoubleStream.of(-1, 0, 1).iterator()), -1.0,
			0.0, 1.0);
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
		Assert.stream(DoubleStream.empty().boxed());
		Assert.stream(testStream().boxed().map(String::valueOf), "-1.0", "0.0", "1.0", "0.0");
	}

	@Test
	public void shouldMapElements() throws Exception {
		Assert.stream(DoubleStream.empty().map(null));
		Assert.stream(DoubleStream.empty().map(_ -> Assert.fail()));
		Assert.stream(testStream().map(null));
		Assert.stream(testStream().map(i -> i + 1), 0, 1, 2, 1);
	}

	@Test
	public void shouldMapElementsToInt() throws Exception {
		Assert.stream(DoubleStream.empty().mapToInt(null));
		Assert.stream(testStream().mapToInt(d -> (int) d), -1, 0, 1, 0);
	}

	@Test
	public void shouldMapElementsToLong() throws Exception {
		Assert.stream(DoubleStream.empty().mapToLong(null));
		Assert.stream(testStream().mapToLong(d -> (long) d), -1, 0, 1, 0);
	}

	@Test
	public void shouldFlatMapElements() throws Exception {
		Assert.stream(DoubleStream.empty().flatMap(null));
		Assert.stream(DoubleStream.empty().flatMap(_ -> Assert.fail()));
		Assert.stream(testStream().flatMap(null));
		Assert.stream(testStream().flatMap(i -> DoubleStream.of(i - 1, i + 1)), -2, 0, -1, 1, 0, 2,
			-1, 1);
	}

	@Test
	public void shouldLimitElements() throws Exception {
		Assert.stream(DoubleStream.empty().limit(3));
		Assert.stream(testStream().limit(0));
		Assert.stream(testStream().limit(2), -1, 0);
		Assert.stream(testStream().limit(5), -1, 0, 1, 0);
	}

	@Test
	public void shouldProvideDistinctElements() throws Exception {
		Assert.stream(DoubleStream.empty().distinct());
		Assert.stream(testStream().distinct(), -1, 0, 1);
	}

	@Test
	public void shouldProvideSortedElements() throws Exception {
		Assert.stream(DoubleStream.empty().sorted());
		Assert.stream(testStream().sorted(), -1, 0, 0, 1);
	}

	@Test
	public void shouldProvideNextElement() throws Exception {
		Assert.equal(DoubleStream.empty().next(), null);
		Assert.equal(DoubleStream.empty().next(3), 3.0);
		var stream = testStream();
		Assert.equal(stream.next(3), -1.0);
		Assert.equal(stream.next(), 0.0);
		Assert.equal(stream.next(), 1.0);
		Assert.equal(stream.next(3), 0.0);
		Assert.equal(stream.next(), null);
		Assert.equal(stream.next(3), 3.0);
	}

	@Test
	public void shouldSkipElements() {
		Assert.stream(testStream().skip(2), 1, 0);
		Assert.stream(testStream().skip(5));
	}

	@Test
	public void shouldDetermineIfEmpty() throws Exception {
		Assert.equal(DoubleStream.empty().isEmpty(), true);
		var stream = DoubleStream.of(1);
		Assert.equal(stream.isEmpty(), false);
		Assert.equal(stream.isEmpty(), true);
		Assert.equal(stream.isEmpty(), true);
	}

	@Test
	public void shouldDetermineCount() throws Exception {
		Assert.equal(DoubleStream.empty().count(), 0L);
		Assert.equal(testStream().count(), 4L);
	}

	@Test
	public void shouldProvideIterator() throws Exception {
		Assert.iterator(DoubleStream.empty().iterator());
		Assert.iterator(testStream().iterator(), -1.0, 0.0, 1.0, 0.0);
	}

	@Test
	public void shouldIterateForEach() throws Exception {
		var captor = Captor.of();
		DoubleStream.empty().forEach(captor::accept);
		captor.verify();
		testStream().forEach(captor::accept);
		captor.verify(-1.0, 0.0, 1.0, 0.0);
	}

	@Test
	public void shouldCollectElements() throws Exception {
		DoubleStream.empty().collect(Captor::of, Captor::accept).verify();
		Assert.equal(testStream().collect(null), null);
		Assert.equal(testStream().collect(null, (_, _) -> {}), null);
		Assert.equal(testStream().collect(() -> null, (_, _) -> {}), null);
		testStream().collect(Captor::of, Captor::accept).verify(-1.0, 0.0, 1.0, 0.0);
		Assert.array(testStream().collect(Collect.Doubles.sortedArray), -1, 0, 0, 1);
	}

	@Test
	public void shouldDetermineMin() throws Exception {
		Assert.equal(DoubleStream.empty().min(0), 0.0);
		Assert.equal(testStream().min(0), -1.0);
	}

	@Test
	public void shouldDetermineMax() throws Exception {
		Assert.equal(DoubleStream.empty().max(0), 0.0);
		Assert.equal(testStream().max(0), 1.0);
	}

	@Test
	public void shouldDetermineSum() {
		Assert.equal(testStream().sum(), 0.0);
		Assert.equal(testStream().skip(2).sum(), 1.0);
	}

	@Test
	public void shouldDetermineAverage() {
		Assert.equal(testStream().average(), 0.0);
		Assert.equal(testStream().skip(2).average(), 0.5);
	}

	@Test
	public void shouldReduceElements() throws Exception {
		Assert.equal(DoubleStream.empty().reduce((_, _) -> 0), null);
		Assert.equal(DoubleStream.empty().reduce((_, _) -> 0, 3), 3.0);
		Assert.equal(testStream().reduce(null), null);
		Assert.equal(testStream().reduce(null, 3), 3.0);
		Assert.equal(testStream().filter(i -> i > 1).reduce((_, _) -> 0), null);
		Assert.equal(testStream().filter(i -> i > 1).reduce((_, _) -> 0, 3), 3.0);
	}

	private static DoubleStream<RuntimeException> testStream() {
		return DoubleStream.of(-1, 0, 1, 0);
	}
}
