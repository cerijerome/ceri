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

public class DoubleStreamBehavior {

	@Test
	public void testOf() throws Exception {
		assertEquals(DoubleStream.of((double[]) null).isEmpty(), true);
		assertEquals(DoubleStream.of().isEmpty(), true);
		assertStream(DoubleStream.of(1, 2, 3), 1, 2, 3);
	}

	@Test
	public void testSegment() throws Exception {
		assertStream(Streams.segment(0));
		assertStream(Streams.segment(1), 0.0);
		assertStream(Streams.segment(2), 0.0, 1.0);
		assertStream(Streams.segment(3), 0.0, 0.5, 1.0);
	}

	@Test
	public void testFromIterable() {
		assertStream(DoubleStream.from((List<Number>) null));
		assertStream(DoubleStream.from(Iterables.ofNull()));
		assertStream(DoubleStream.from(List.of(-1, 0, 1)), -1.0, 0.0, 1.0);
	}

	@Test
	public void testFromIterator() {
		assertStream(DoubleStream.from((PrimitiveIterator.OfDouble) null));
		assertStream(DoubleStream.from(Iterators.nullDouble));
		assertStream(DoubleStream.from(java.util.stream.DoubleStream.of(-1, 0, 1).iterator()), -1.0,
			0.0, 1.0);
	}

	@Test
	public void shouldFilterElements() {
		assertStream(testStream().filter(null), -1, 0, 1, 0);
		assertStream(testStream().filter(i -> i != 0), -1, 1);
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
		assertStream(DoubleStream.empty().boxed());
		assertStream(testStream().boxed().map(String::valueOf), "-1.0", "0.0", "1.0", "0.0");
	}

	@Test
	public void shouldMapElements() throws Exception {
		assertStream(DoubleStream.empty().map(null));
		assertStream(DoubleStream.empty().map(_ -> fail()));
		assertStream(testStream().map(null));
		assertStream(testStream().map(i -> i + 1), 0, 1, 2, 1);
	}

	@Test
	public void shouldMapElementsToInt() throws Exception {
		assertStream(DoubleStream.empty().mapToInt(null));
		assertStream(testStream().mapToInt(d -> (int) d), -1, 0, 1, 0);
	}

	@Test
	public void shouldMapElementsToLong() throws Exception {
		assertStream(DoubleStream.empty().mapToLong(null));
		assertStream(testStream().mapToLong(d -> (long) d), -1, 0, 1, 0);
	}

	@Test
	public void shouldFlatMapElements() throws Exception {
		assertStream(DoubleStream.empty().flatMap(null));
		assertStream(DoubleStream.empty().flatMap(_ -> fail()));
		assertStream(testStream().flatMap(null));
		assertStream(testStream().flatMap(i -> DoubleStream.of(i - 1, i + 1)), -2, 0, -1, 1, 0, 2,
			-1, 1);
	}

	@Test
	public void shouldLimitElements() throws Exception {
		assertStream(DoubleStream.empty().limit(3));
		assertStream(testStream().limit(0));
		assertStream(testStream().limit(2), -1, 0);
		assertStream(testStream().limit(5), -1, 0, 1, 0);
	}

	@Test
	public void shouldProvideDistinctElements() throws Exception {
		assertStream(DoubleStream.empty().distinct());
		assertStream(testStream().distinct(), -1, 0, 1);
	}

	@Test
	public void shouldProvideSortedElements() throws Exception {
		assertStream(DoubleStream.empty().sorted());
		assertStream(testStream().sorted(), -1, 0, 0, 1);
	}

	@Test
	public void shouldProvideNextElement() throws Exception {
		assertEquals(DoubleStream.empty().next(), null);
		assertEquals(DoubleStream.empty().next(3), 3.0);
		var stream = testStream();
		assertEquals(stream.next(3), -1.0);
		assertEquals(stream.next(), 0.0);
		assertEquals(stream.next(), 1.0);
		assertEquals(stream.next(3), 0.0);
		assertEquals(stream.next(), null);
		assertEquals(stream.next(3), 3.0);
	}

	@Test
	public void shouldSkipElements() {
		assertStream(testStream().skip(2), 1, 0);
		assertStream(testStream().skip(5));
	}

	@Test
	public void shouldDetermineIfEmpty() throws Exception {
		assertEquals(DoubleStream.empty().isEmpty(), true);
		var stream = DoubleStream.of(1);
		assertEquals(stream.isEmpty(), false);
		assertEquals(stream.isEmpty(), true);
		assertEquals(stream.isEmpty(), true);
	}

	@Test
	public void shouldDetermineCount() throws Exception {
		assertEquals(DoubleStream.empty().count(), 0L);
		assertEquals(testStream().count(), 4L);
	}

	@Test
	public void shouldProvideIterator() throws Exception {
		assertIterator(DoubleStream.empty().iterator());
		assertIterator(testStream().iterator(), -1.0, 0.0, 1.0, 0.0);
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
		assertEquals(testStream().collect(null), null);
		assertEquals(testStream().collect(null, (_, _) -> {}), null);
		assertEquals(testStream().collect(() -> null, (_, _) -> {}), null);
		testStream().collect(Captor::of, Captor::accept).verify(-1.0, 0.0, 1.0, 0.0);
		assertArray(testStream().collect(Collect.Doubles.sortedArray), -1, 0, 0, 1);
	}

	@Test
	public void shouldDetermineMin() throws Exception {
		assertEquals(DoubleStream.empty().min(0), 0.0);
		assertEquals(testStream().min(0), -1.0);
	}

	@Test
	public void shouldDetermineMax() throws Exception {
		assertEquals(DoubleStream.empty().max(0), 0.0);
		assertEquals(testStream().max(0), 1.0);
	}

	@Test
	public void shouldDetermineSum() {
		assertEquals(testStream().sum(), 0.0);
		assertEquals(testStream().skip(2).sum(), 1.0);
	}

	@Test
	public void shouldDetermineAverage() {
		assertEquals(testStream().average(), 0.0);
		assertEquals(testStream().skip(2).average(), 0.5);
	}

	@Test
	public void shouldReduceElements() throws Exception {
		assertEquals(DoubleStream.empty().reduce((_, _) -> 0), null);
		assertEquals(DoubleStream.empty().reduce((_, _) -> 0, 3), 3.0);
		assertEquals(testStream().reduce(null), null);
		assertEquals(testStream().reduce(null, 3), 3.0);
		assertEquals(testStream().filter(i -> i > 1).reduce((_, _) -> 0), null);
		assertEquals(testStream().filter(i -> i > 1).reduce((_, _) -> 0, 3), 3.0);
	}

	private static DoubleStream<RuntimeException> testStream() {
		return DoubleStream.of(-1, 0, 1, 0);
	}
}
