package ceri.common.stream;

import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertStream;
import static ceri.common.test.AssertUtil.fail;
import java.util.List;
import java.util.PrimitiveIterator;
import org.junit.Test;
import ceri.common.collection.Iterables;
import ceri.common.collection.Iterators;
import ceri.common.test.Captor;

public class IntStreamBehavior {

	@Test
	public void testOf() throws Exception {
		assertEquals(IntStream.of((int[]) null).isEmpty(), true);
		assertEquals(IntStream.of().isEmpty(), true);
		assertStream(IntStream.of(1, 2, 3), 1, 2, 3);
	}

	@Test
	public void testRange() {
		assertStream(IntStream.range(-1, 0));
		assertStream(IntStream.range(-1, 1), -1);
		assertStream(IntStream.range(-1, 2), -1, 0);
	}

	@Test
	public void testFromIterable() {
		assertStream(IntStream.from((List<Number>) null));
		assertStream(IntStream.from(Iterables.ofNull()));
		assertStream(IntStream.from(List.of(-1.1, 0.6, 1.9)), -1, 0, 1);
	}

	@Test
	public void testFromIterator() {
		assertStream(IntStream.from((PrimitiveIterator.OfInt) null));
		assertStream(IntStream.from(Iterators.nullInt));
		assertStream(IntStream.from(java.util.stream.IntStream.of(-1, 0, 1).iterator()), -1, 0, 1);
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
		assertStream(IntStream.empty().boxed());
		assertStream(testStream().boxed().map(String::valueOf), "-1", "0", "1", "0");
	}

	@Test
	public void shouldMapElements() throws Exception {
		assertStream(IntStream.empty().map(null));
		assertStream(IntStream.empty().map(_ -> fail()));
		assertStream(testStream().map(null));
		assertStream(testStream().map(i -> i + 1), 0, 1, 2, 1);
	}

	@Test
	public void shouldFlatMapElements() throws Exception {
		assertStream(IntStream.empty().flatMap(null));
		assertStream(IntStream.empty().flatMap(_ -> fail()));
		assertStream(testStream().flatMap(null));
		assertStream(testStream().flatMap(i -> IntStream.of(i - 1, i + 1)), -2, 0, -1, 1, 0, 2, -1,
			1);
	}

	@Test
	public void shouldLimitElements() throws Exception {
		assertStream(IntStream.empty().limit(3));
		assertStream(testStream().limit(0));
		assertStream(testStream().limit(2), -1, 0);
		assertStream(testStream().limit(5), -1, 0, 1, 0);
	}

	@Test
	public void shouldProvideDistinctElements() throws Exception {
		assertStream(IntStream.empty().distinct());
		assertStream(testStream().distinct(), -1, 0, 1);
	}

	@Test
	public void shouldProvideSortedElements() throws Exception {
		assertStream(IntStream.empty().sorted());
		assertStream(testStream().sorted(), -1, 0, 0, 1);
	}

	@Test
	public void shouldProvideNextElement() throws Exception {
		assertEquals(IntStream.empty().next(), null);
		assertEquals(IntStream.empty().next(3), 3);
		var stream = testStream();
		assertEquals(stream.next(3), -1);
		assertEquals(stream.next(), 0);
		assertEquals(stream.next(), 1);
		assertEquals(stream.next(3), 0);
		assertEquals(stream.next(), null);
		assertEquals(stream.next(3), 3);

	}

	@Test
	public void shouldDetermineIfEmpty() throws Exception {
		assertEquals(IntStream.empty().isEmpty(), true);
		var stream = IntStream.of(1);
		assertEquals(stream.isEmpty(), false);
		assertEquals(stream.isEmpty(), true);
		assertEquals(stream.isEmpty(), true);
	}

	@Test
	public void shouldDetermineCount() throws Exception {
		assertEquals(IntStream.empty().count(), 0L);
		assertEquals(testStream().count(), 4L);
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
		assertEquals(testStream().collect(null, (_, _) -> {}), null);
		assertEquals(testStream().collect(() -> null, (_, _) -> {}), null);
		testStream().collect(Captor::of, Captor::accept).verify(-1, 0, 1, 0);
	}

	@Test
	public void shouldReduceElements() throws Exception {
		assertEquals(IntStream.empty().reduce((_, _) -> 0), null);
		assertEquals(IntStream.empty().reduce(3, (_, _) -> 0), 3);
		assertEquals(testStream().reduce(null), null);
		assertEquals(testStream().reduce(3, null), 3);
		assertEquals(testStream().filter(i -> i > 1).reduce((_, _) -> 0), null);
	}

	private static IntStream<RuntimeException> testStream() {
		return IntStream.of(-1, 0, 1, 0);
	}
}
