package ceri.common.stream;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Iterator;
import java.util.List;
import java.util.Spliterator;
import java.util.stream.Collector;
import org.junit.Test;
import ceri.common.collect.Immutable;
import ceri.common.collect.Lists;
import ceri.common.collect.Maps;
import ceri.common.collect.Sets;
import ceri.common.except.ExceptionAdapter;
import ceri.common.function.Compares;
import ceri.common.function.Excepts;
import ceri.common.function.Functions;
import ceri.common.test.Assert;
import ceri.common.test.Captor;
import ceri.common.test.TestCollection;
import ceri.common.text.Joiner;

public class StreamBehavior {
	private static final List<Integer> nullList = null;
	private static final List<Integer> emptyList = Immutable.list();
	private static final List<Integer> list = Immutable.listOf(-1, null, 1);
	private static final Iterator<Integer> nullIterator = null;
	private static final Spliterator<Integer> nullSpliterator = null;
	private static final java.util.stream.Stream<Integer> nullStream = null;
	private static final Integer[] nullArray = null;
	private static final Integer[] emptyArray = new Integer[0];
	private static final Stream.ExAdapter<IOException> adapter =
		e -> (e instanceof UncheckedIOException uie) ? uie.getCause() : null;

	private static Integer[] array() {
		return list.toArray(Integer[]::new);
	}

	private static class Rt {
		private static final Stream.Rt<Integer> empty = Stream.Rt.empty();
		private static final Functions.Predicate<Integer> no = _ -> false;
		private static final Functions.Predicate<Integer> yes = _ -> true;
		private static final Functions.Predicate<Integer> pred = i -> i != null && i >= 0;
		private static final Functions.Function<Object, Object> fn = String::valueOf;
		private static final Functions.ToIntFunction<Integer> intFn = i -> i == null ? 0 : -i;
		private static final Functions.ToLongFunction<Integer> longFn = i -> i == null ? 0 : -i;
		private static final Functions.ToDoubleFunction<Integer> doubleFn = i -> i == null ? 0 : -i;
		private static final Functions.Function<Integer, Iterable<Integer>> expandFn =
			i -> i == null ? null : Lists.ofAll(-i, null, i);
		private static final Functions.Function<Integer, Stream.Rt<Integer>> flatFn =
			i -> i == null ? null : Rt.of(-i, null, i);

		private Rt() {}

		@SafeVarargs
		private static <T> Stream.Rt<T> of(T... values) {
			return Stream.ofAll(values);
		}

		private static Stream.Rt<Integer> stream() {
			return of(-1, null, 1, 0);
		}
	}

	private static class Ex {
		private static final Stream.Ex<IOException, Integer> empty = Stream.Ex.empty();
		private static final Excepts.Predicate<IOException, Integer> no = _ -> false;
		private static final Excepts.Predicate<IOException, Integer> yes = _ -> true;
		private static final Excepts.Predicate<IOException, Integer> pred =
			i -> i != null && i >= 0;
		private static final Excepts.Function<IOException, Object, Object> fn = String::valueOf;
		private static final Excepts.ToIntFunction<IOException, Integer> intFn =
			i -> i == null ? 0 : -i;
		private static final Excepts.ToLongFunction<IOException, Integer> longFn =
			i -> i == null ? 0 : -i;
		private static final Excepts.ToDoubleFunction<IOException, Integer> doubleFn =
			i -> i == null ? 0 : -i;
		private static final Excepts.Function<IOException, Integer, Iterable<Integer>> expandFn =
			i -> i == null ? null : Lists.ofAll(-i, null, i);
		private static final Excepts.Function<IOException, Integer, //
			Stream.Ex<IOException, Integer>> flatFn = i -> i == null ? null : Ex.of(-i, null, i);

		private Ex() {}

		@SafeVarargs
		private static <T> Stream.Ex<IOException, T> of(T... values) {
			return Stream.ofAll(values).ex();
		}

		private static Stream.Ex<IOException, Integer> stream() {
			return of(-1, null, 1, 0);
		}

		private static Stream.Ex<IOException, Integer> io() {
			return Rt.stream().filterEx(i -> {
				if (i == null) throw new IOException();
				return true;
			});
		}
	}

	@Test
	public void testEmpty() throws IOException {
		Assert.stream(Rt.empty);
		Assert.same(Stream.Rt.empty(), Stream.Rt.empty());
		Assert.stream(Ex.empty);
		Assert.same(Stream.Ex.empty(), Stream.Ex.empty());
	}

	@Test
	public void testOf() {
		Assert.stream(Stream.ofAll(nullArray));
		Assert.stream(Stream.ofAll(emptyArray));
		Assert.stream(Stream.ofAll(array()), -1, null, 1);
		Assert.stream(Stream.of(nullArray, 1));
		Assert.stream(Stream.of(emptyArray, 1));
		Assert.stream(Stream.of(array(), 1), null, 1);
	}

	@Test
	public void testFrom() throws IOException {
		Assert.stream(Stream.from(nullList));
		Assert.stream(Stream.from(emptyList));
		Assert.stream(Stream.from(list), -1, null, 1);
		Assert.stream(Stream.from(nullIterator));
		Assert.stream(Stream.from(emptyList.iterator()));
		Assert.stream(Stream.from(list.iterator()), -1, null, 1);
		Assert.stream(Stream.from(nullStream));
		Assert.stream(Stream.from(emptyList.stream()));
		Assert.stream(Stream.from(list.stream()), -1, null, 1);
		Assert.stream(Stream.from(nullSpliterator));
		Assert.stream(Stream.from(emptyList.spliterator()));
		Assert.stream(Stream.from(list.spliterator()), -1, null, 1);
		Assert.stream(Stream.from(nullList, adapter));
		Assert.stream(Stream.from(emptyList, adapter));
		Assert.stream(Stream.from(nullIterator, adapter));
		Assert.stream(Stream.from(emptyList.iterator(), adapter));
		Assert.stream(Stream.from(nullStream, adapter));
		Assert.stream(Stream.from(emptyList.stream(), adapter));
		Assert.stream(Stream.from(nullSpliterator, adapter));
		Assert.stream(Stream.from(emptyList.spliterator(), adapter));
	}

	@Test
	public void testIteratorExceptionAdapter() throws IOException {
		var uie = new UncheckedIOException(new IOException("test"));
		var ise = new IllegalStateException("test");
		var iterator = TestCollection.iteratorOf(1, 2, 3);
		iterator.next.set(ise, uie, null);
		var stream = Stream.<IOException, Integer>from(iterator, null);
		Assert.thrown(IllegalStateException.class, stream::next);
		Assert.thrown(UncheckedIOException.class, stream::next);
		Assert.stream(stream, 1, 2, 3);
		iterator = TestCollection.iteratorOf(1, 2, 3);
		iterator.next.set(ise, uie, null);
		stream =
			Stream.from(iterator, e -> (e instanceof UncheckedIOException u) ? u.getCause() : null);
		Assert.thrown(IllegalStateException.class, stream::next);
		Assert.thrown(IOException.class, stream::next);
		Assert.stream(stream, 1, 2, 3);
	}

	@Test
	public void testSpliteratorExceptionAdapter() throws IOException {
		var uie = new UncheckedIOException(new IOException("test"));
		var ise = new IllegalStateException("test");
		var spliterator = TestCollection.spliteratorOf(1, 2, 3);
		spliterator.tryAdvance.set(ise, uie, null);
		var stream = Stream.<IOException, Integer>from(spliterator, null);
		Assert.thrown(IllegalStateException.class, stream::next);
		Assert.thrown(UncheckedIOException.class, stream::next);
		Assert.stream(stream, 1, 2, 3);
		spliterator = TestCollection.spliteratorOf(1, 2, 3);
		spliterator.tryAdvance.set(ise, uie, null);
		stream = Stream.from(spliterator,
			e -> (e instanceof UncheckedIOException u) ? u.getCause() : null);
		Assert.thrown(IllegalStateException.class, stream::next);
		Assert.thrown(IOException.class, stream::next);
		Assert.stream(stream, 1, 2, 3);
	}

	@Test
	public void testUnmap() {
		var map = Immutable.mapOf(Maps::link, -1, "B", null, "A", 1, null);
		Assert.stream(Stream.Rt.unmap(null, map));
		Assert.stream(Stream.Rt.unmap((k, v) -> "" + k + v, null));
		Assert.stream(Stream.Rt.unmap((k, v) -> "" + k + v, map), "-1B", "nullA", "1null");
		Assert.stream(Stream.Ex.unmap(null, map));
		Assert.stream(Stream.Ex.unmap((k, v) -> "" + k + v, null));
		Assert.stream(Stream.Ex.unmap((k, v) -> "" + k + v, map), "-1B", "nullA", "1null");
	}

	@Test
	public void testMerge() throws IOException {
		Assert.stream(Stream.merge(new Stream.Rt[0]));
		Assert.stream(Stream.merge(Rt.of(1, 2), Rt.empty, Rt.of(3), null), 1, 2, 3);
		Assert.stream(Stream.merge(Ex.of(1, 2), Ex.empty, Ex.of(3), null), 1, 2, 3);
	}

	@Test
	public void shouldAdaptForExceptions() throws IOException {
		Assert.stream(Rt.empty.ex());
		Assert.stream(Rt.empty.ex(ExceptionAdapter.io));
		Assert.stream(Rt.of(1, 2, 3).ex(), 1, 2, 3);
		Assert.stream(Rt.of(1, 2, 3).ex(null), 1, 2, 3);
		Assert.stream(Rt.of(1, 2, 3).ex(ExceptionAdapter.io), 1, 2, 3);
		Assert.stream(Ex.empty.ex(ExceptionAdapter.io));
		Assert.stream(Ex.of(1, 2, 3).ex(null));
		Assert.stream(Ex.of(1, 2, 3).ex(ExceptionAdapter.runtime), 1, 2, 3);
	}

	@Test
	public void shouldWrapExceptions() {
		Assert.stream(Rt.empty.runtime());
		Assert.stream(Rt.stream().runtime(), -1, null, 1, 0);
		Assert.stream(Ex.empty.runtime());
		Assert.stream(Ex.stream().runtime(), -1, null, 1, 0);
		Assert.runtime(() -> Ex.io().runtime().toArray());
	}

	@Test
	public void shouldFilterElements() throws IOException {
		Assert.stream(Rt.empty.filter(null));
		Assert.stream(Rt.empty.filterEx(null));
		Assert.stream(Rt.empty.filter(Rt.pred));
		Assert.stream(Rt.empty.filterEx(Rt.pred));
		Assert.stream(Rt.stream().filter(null), -1, null, 1, 0);
		Assert.stream(Rt.stream().filterEx(null), -1, null, 1, 0);
		Assert.stream(Rt.stream().filter(Rt.no));
		Assert.stream(Rt.stream().filter(Rt.yes), -1, null, 1, 0);
		Assert.stream(Rt.stream().filter(Rt.pred), 1, 0);
		Assert.stream(Rt.stream().filterEx(Ex.pred), 1, 0);
		Assert.stream(Ex.empty.filter(null));
		Assert.stream(Ex.empty.filterRt(null));
		Assert.stream(Ex.empty.filter(Ex.pred));
		Assert.stream(Ex.empty.filterRt(Rt.pred));
		Assert.stream(Ex.stream().filter(null), -1, null, 1, 0);
		Assert.stream(Ex.stream().filterRt(null), -1, null, 1, 0);
		Assert.stream(Ex.stream().filter(Ex.no));
		Assert.stream(Ex.stream().filter(Ex.yes), -1, null, 1, 0);
		Assert.stream(Ex.stream().filter(Ex.pred), 1, 0);
		Assert.stream(Ex.stream().filterRt(Rt.pred), 1, 0);
	}

	@Test
	public void shouldFilterInstances() throws IOException {
		Assert.stream(Rt.empty.instances(null));
		Assert.stream(Rt.empty.instances(Object.class));
		Assert.stream(Rt.stream().instances(null));
		Assert.stream(Rt.stream().instances(Object.class), -1, 1, 0);
		Assert.stream(Rt.stream().instances(Integer.class), -1, 1, 0);
		Assert.stream(Rt.stream().instances(Long.class));
		Assert.stream(Ex.empty.instances(null));
		Assert.stream(Ex.empty.instances(Object.class));
		Assert.stream(Ex.stream().instances(null));
		Assert.stream(Ex.stream().instances(Object.class), -1, 1, 0);
		Assert.stream(Ex.stream().instances(Integer.class), -1, 1, 0);
		Assert.stream(Ex.stream().instances(Long.class));
	}

	@Test
	public void shouldFilterNulls() throws IOException {
		Assert.stream(Rt.empty.nonNull());
		Assert.stream(Rt.stream().nonNull(), -1, 1, 0);
		Assert.stream(Ex.empty.nonNull());
		Assert.stream(Ex.stream().nonNull(), -1, 1, 0);
	}

	@Test
	public void shouldMatchAny() throws IOException {
		Assert.equal(Rt.empty.anyMatch(null), false);
		Assert.equal(Rt.empty.anyMatch(Rt.no), false);
		Assert.equal(Rt.empty.anyMatch(Rt.yes), false);
		Assert.equal(Rt.empty.anyMatch(Rt.pred), false);
		Assert.equal(Rt.stream().anyMatch(null), true);
		Assert.equal(Rt.stream().anyMatch(Rt.no), false);
		Assert.equal(Rt.stream().anyMatch(Rt.yes), true);
		Assert.equal(Rt.stream().anyMatch(Rt.pred), true);
		Assert.equal(Ex.empty.anyMatch(null), false);
		Assert.equal(Ex.empty.anyMatch(Ex.no), false);
		Assert.equal(Ex.empty.anyMatch(Ex.yes), false);
		Assert.equal(Ex.empty.anyMatch(Ex.pred), false);
		Assert.equal(Ex.stream().anyMatch(null), true);
		Assert.equal(Ex.stream().anyMatch(Ex.no), false);
		Assert.equal(Ex.stream().anyMatch(Ex.yes), true);
		Assert.equal(Ex.stream().anyMatch(Ex.pred), true);
	}

	@Test
	public void shouldMatchAll() throws IOException {
		Assert.equal(Rt.empty.allMatch(null), true);
		Assert.equal(Rt.empty.allMatch(Rt.no), true);
		Assert.equal(Rt.empty.allMatch(Rt.yes), true);
		Assert.equal(Rt.empty.allMatch(Rt.pred), true);
		Assert.equal(Rt.stream().allMatch(null), true);
		Assert.equal(Rt.stream().allMatch(Rt.no), false);
		Assert.equal(Rt.stream().allMatch(Rt.yes), true);
		Assert.equal(Rt.stream().allMatch(Rt.pred), false);
		Assert.equal(Ex.empty.allMatch(null), true);
		Assert.equal(Ex.empty.allMatch(Ex.no), true);
		Assert.equal(Ex.empty.allMatch(Ex.yes), true);
		Assert.equal(Ex.empty.allMatch(Ex.pred), true);
		Assert.equal(Ex.stream().allMatch(null), true);
		Assert.equal(Ex.stream().allMatch(Ex.no), false);
		Assert.equal(Ex.stream().allMatch(Ex.yes), true);
		Assert.equal(Ex.stream().allMatch(Ex.pred), false);
	}

	@Test
	public void shouldMatchNone() throws IOException {
		Assert.equal(Rt.empty.noneMatch(null), true);
		Assert.equal(Rt.empty.noneMatch(Rt.no), true);
		Assert.equal(Rt.empty.noneMatch(Rt.yes), true);
		Assert.equal(Rt.empty.noneMatch(Rt.pred), true);
		Assert.equal(Rt.stream().noneMatch(null), false);
		Assert.equal(Rt.stream().noneMatch(Rt.no), true);
		Assert.equal(Rt.stream().noneMatch(Rt.yes), false);
		Assert.equal(Rt.stream().noneMatch(Rt.pred), false);
		Assert.equal(Ex.empty.noneMatch(null), true);
		Assert.equal(Ex.empty.noneMatch(Ex.no), true);
		Assert.equal(Ex.empty.noneMatch(Ex.yes), true);
		Assert.equal(Ex.empty.noneMatch(Ex.pred), true);
		Assert.equal(Ex.stream().noneMatch(null), false);
		Assert.equal(Ex.stream().noneMatch(Ex.no), true);
		Assert.equal(Ex.stream().noneMatch(Ex.yes), false);
		Assert.equal(Ex.stream().noneMatch(Ex.pred), false);
	}

	@Test
	public void shouldMapToString() throws IOException {
		Assert.stream(Rt.empty.string());
		Assert.stream(Rt.stream().string(), "-1", "", "1", "0");
		Assert.stream(Ex.empty.string());
		Assert.stream(Ex.stream().string(), "-1", "", "1", "0");
	}

	@Test
	public void shouldMapElements() throws IOException {
		Assert.stream(Rt.empty.map(null));
		Assert.stream(Rt.empty.map(Rt.fn));
		Assert.stream(Rt.empty.mapEx(Ex.fn));
		Assert.stream(Rt.stream().map(null));
		Assert.stream(Rt.stream().map(Rt.fn), "-1", "null", "1", "0");
		Assert.stream(Rt.stream().mapEx(Ex.fn), "-1", "null", "1", "0");
		Assert.stream(Ex.empty.map(null));
		Assert.stream(Ex.empty.map(Ex.fn));
		Assert.stream(Ex.empty.mapRt(Rt.fn));
		Assert.stream(Ex.stream().map(null));
		Assert.stream(Ex.stream().map(Ex.fn), "-1", "null", "1", "0");
		Assert.stream(Ex.stream().mapRt(Rt.fn), "-1", "null", "1", "0");
	}

	@Test
	public void shouldMapElementsToInt() throws IOException {
		Assert.stream(Rt.empty.mapToInt(null));
		Assert.stream(Rt.empty.mapToInt(Rt.intFn));
		Assert.stream(Rt.empty.mapToIntEx(Ex.intFn));
		Assert.stream(Rt.stream().mapToInt(null));
		Assert.stream(Rt.stream().mapToInt(Rt.intFn), 1, 0, -1, 0);
		Assert.stream(Rt.stream().mapToIntEx(Ex.intFn), 1, 0, -1, 0);
		Assert.stream(Ex.empty.mapToInt(null));
		Assert.stream(Ex.empty.mapToInt(Ex.intFn));
		Assert.stream(Ex.empty.mapToIntRt(Rt.intFn));
		Assert.stream(Ex.stream().mapToInt(null));
		Assert.stream(Ex.stream().mapToInt(Ex.intFn), 1, 0, -1, 0);
		Assert.stream(Ex.stream().mapToIntRt(Rt.intFn), 1, 0, -1, 0);
	}

	@Test
	public void shouldMapElementsToLong() throws IOException {
		Assert.stream(Rt.empty.mapToLong(null));
		Assert.stream(Rt.empty.mapToLong(Rt.longFn));
		Assert.stream(Rt.empty.mapToLongEx(Ex.longFn));
		Assert.stream(Rt.stream().mapToLong(null));
		Assert.stream(Rt.stream().mapToLong(Rt.longFn), 1L, 0L, -1L, 0L);
		Assert.stream(Rt.stream().mapToLongEx(Ex.longFn), 1L, 0L, -1L, 0L);
		Assert.stream(Ex.empty.mapToLong(null));
		Assert.stream(Ex.empty.mapToLong(Ex.longFn));
		Assert.stream(Ex.empty.mapToLongRt(Rt.longFn));
		Assert.stream(Ex.stream().mapToLong(null));
		Assert.stream(Ex.stream().mapToLong(Ex.longFn), 1L, 0L, -1L, 0L);
		Assert.stream(Ex.stream().mapToLongRt(Rt.longFn), 1L, 0L, -1L, 0L);
	}

	@Test
	public void shouldMapElementsToDouble() throws IOException {
		Assert.stream(Rt.empty.mapToDouble(null));
		Assert.stream(Rt.empty.mapToDouble(Rt.doubleFn));
		Assert.stream(Rt.empty.mapToDoubleEx(Ex.doubleFn));
		Assert.stream(Rt.stream().mapToDouble(null));
		Assert.stream(Rt.stream().mapToDouble(Rt.doubleFn), 1.0, 0.0, -1.0, 0.0);
		Assert.stream(Rt.stream().mapToDoubleEx(Ex.doubleFn), 1.0, 0.0, -1.0, 0.0);
		Assert.stream(Ex.empty.mapToDouble(null));
		Assert.stream(Ex.empty.mapToDouble(Ex.doubleFn));
		Assert.stream(Ex.empty.mapToDoubleRt(Rt.doubleFn));
		Assert.stream(Ex.stream().mapToDouble(null));
		Assert.stream(Ex.stream().mapToDouble(Ex.doubleFn), 1.0, 0.0, -1.0, 0.0);
		Assert.stream(Ex.stream().mapToDoubleRt(Rt.doubleFn), 1.0, 0.0, -1.0, 0.0);
	}

	@Test
	public void shouldExpandElements() throws IOException {
		Assert.stream(Rt.empty.expand(null));
		Assert.stream(Rt.empty.expand(Rt.expandFn));
		Assert.stream(Rt.empty.expandEx(Ex.expandFn));
		Assert.stream(Rt.stream().expand(null));
		Assert.stream(Rt.stream().expand(Rt.expandFn), 1, null, -1, -1, null, 1, 0, null, 0);
		Assert.stream(Rt.stream().expandEx(Ex.expandFn), 1, null, -1, -1, null, 1, 0, null, 0);
		Assert.stream(Ex.empty.expand(null));
		Assert.stream(Ex.empty.expand(Ex.expandFn));
		Assert.stream(Ex.empty.expandRt(Rt.expandFn));
		Assert.stream(Ex.stream().expand(null));
		Assert.stream(Ex.stream().expand(Ex.expandFn), 1, null, -1, -1, null, 1, 0, null, 0);
		Assert.stream(Ex.stream().expandRt(Rt.expandFn), 1, null, -1, -1, null, 1, 0, null, 0);
	}

	@Test
	public void shouldFlatMapElements() throws IOException {
		Assert.stream(Rt.empty.flatMap(null));
		Assert.stream(Rt.empty.flatMap(Rt.flatFn));
		Assert.stream(Rt.empty.flatMapEx(Ex.flatFn));
		Assert.stream(Rt.stream().flatMap(null));
		Assert.stream(Rt.stream().flatMap(Rt.flatFn), 1, null, -1, -1, null, 1, 0, null, 0);
		Assert.stream(Rt.stream().flatMapEx(Ex.flatFn), 1, null, -1, -1, null, 1, 0, null, 0);
		Assert.stream(Ex.empty.flatMap(null));
		Assert.stream(Ex.empty.flatMap(Ex.flatFn));
		Assert.stream(Ex.empty.flatMapRt(Rt.flatFn));
		Assert.stream(Ex.stream().flatMap(null));
		Assert.stream(Ex.stream().flatMap(Ex.flatFn), 1, null, -1, -1, null, 1, 0, null, 0);
		Assert.stream(Ex.stream().flatMapRt(Rt.flatFn), 1, null, -1, -1, null, 1, 0, null, 0);
	}

	@Test
	public void shouldLimitElements() throws IOException {
		Assert.stream(Rt.empty.limit(3));
		Assert.stream(Rt.stream().limit(0));
		Assert.stream(Rt.stream().limit(2), -1, null);
		Assert.stream(Rt.stream().limit(5), -1, null, 1, 0);
		Assert.stream(Ex.empty.limit(3));
		Assert.stream(Ex.stream().limit(0));
		Assert.stream(Ex.stream().limit(2), -1, null);
		Assert.stream(Ex.stream().limit(5), -1, null, 1, 0);
	}

	@Test
	public void shouldProvideDistinctElements() throws IOException {
		Assert.stream(Rt.empty.distinct());
		Assert.stream(Rt.of(1, 0, null, 0, -1, null).distinct(), 1, 0, null, -1);
		Assert.stream(Ex.empty.distinct());
		Assert.stream(Ex.of(1, 0, null, 0, -1, null).distinct(), 1, 0, null, -1);
	}

	@Test
	public void shouldProvideSortedElements() throws IOException {
		Assert.stream(Rt.empty.sorted((_, _) -> 0));
		Assert.stream(Rt.stream().sorted(Compares.of()), null, -1, 0, 1);
		Assert.stream(Ex.empty.sorted((_, _) -> 0));
		Assert.stream(Ex.stream().sorted(Compares.of()), null, -1, 0, 1);
	}

	@Test
	public void shouldProvideNextElement() throws IOException {
		Assert.equal(Rt.empty.next(), null);
		Assert.equal(Rt.empty.next(3), 3);
		var rt = Rt.stream();
		Assert.equal(rt.next(3), -1);
		Assert.equal(rt.next(), null);
		Assert.equal(rt.next(), 1);
		Assert.equal(rt.next(3), 0);
		Assert.equal(rt.next(), null);
		Assert.equal(rt.next(3), 3);
		Assert.equal(Ex.empty.next(), null);
		Assert.equal(Ex.empty.next(3), 3);
		var ex = Ex.stream();
		Assert.equal(ex.next(3), -1);
		Assert.equal(ex.next(), null);
		Assert.equal(ex.next(), 1);
		Assert.equal(ex.next(3), 0);
		Assert.equal(ex.next(), null);
		Assert.equal(ex.next(3), 3);
	}

	@Test
	public void shouldSkipElements() throws IOException {
		Assert.stream(Rt.empty.skip(2));
		Assert.stream(Rt.stream().skip(2), 1, 0);
		Assert.stream(Rt.stream().skip(5));
		Assert.stream(Ex.empty.skip(2));
		Assert.stream(Ex.stream().skip(2), 1, 0);
		Assert.stream(Ex.stream().skip(5));
	}

	@Test
	public void shouldDetermineIfEmpty() throws IOException {
		Assert.equal(Rt.empty.isEmpty(), true);
		var rt = Rt.of(1);
		Assert.equal(rt.isEmpty(), false);
		Assert.equal(rt.isEmpty(), true);
		Assert.equal(rt.isEmpty(), true);
		Assert.equal(Ex.empty.isEmpty(), true);
		var ex = Ex.of(1);
		Assert.equal(ex.isEmpty(), false);
		Assert.equal(ex.isEmpty(), true);
		Assert.equal(ex.isEmpty(), true);
	}

	@Test
	public void shouldDetermineCount() throws IOException {
		Assert.equal(Rt.empty.count(), 0L);
		Assert.equal(Rt.stream().count(), 4L);
		Assert.equal(Ex.empty.count(), 0L);
		Assert.equal(Ex.stream().count(), 4L);
	}

	@Test
	public void shouldProvideIterator() {
		Assert.ordered(Rt.empty.iterable());
		Assert.ordered(Rt.stream().iterable(), -1, null, 1, 0);
		Assert.ordered(Ex.empty.iterable());
		Assert.ordered(Ex.stream().iterable(), -1, null, 1, 0);
	}

	@Test
	public void shouldConsumeEachElement() throws IOException {
		Captor.of().apply(Rt.empty::forEach).verify();
		Captor.of().apply(Rt.empty::forEachEx).verify();
		Captor.of().apply(Rt.stream()::forEach).verify(-1, null, 1, 0);
		Captor.of().apply(Rt.stream()::forEachEx).verify(-1, null, 1, 0);
		Captor.of().apply(c -> Ex.empty.forEach(c::accept)).verify();
		Captor.of().apply(Ex.empty::forEachRt).verify();
		Captor.of().apply(c -> Ex.stream().forEach(c::accept)).verify(-1, null, 1, 0);
		Captor.of().apply(Ex.stream()::forEachRt).verify(-1, null, 1, 0);
	}

	@Test
	public void shouldAddToCollection() throws IOException {
		Assert.unordered(Rt.empty.add(Sets.of()));
		Assert.equal(Rt.stream().add(nullList), null);
		Assert.unordered(Rt.stream().add(Sets.of()), -1, 0, null, 1);
		Assert.unordered(Ex.empty.add(Sets.of()));
		Assert.equal(Ex.stream().add(nullList), null);
		Assert.unordered(Ex.stream().add(Sets.of()), -1, 0, null, 1);
	}

	@Test
	public void shouldPutInMap() throws IOException {
		Assert.equal(Rt.stream().put(null, i -> i), null);
		Assert.map(Rt.stream().put(Maps.of(), null));
		Assert.map(Rt.stream().put(Maps.of(), i -> i), -1, -1, null, null, 1, 1, 0, 0);
		Assert.equal(Rt.stream().put(null, i -> i, Rt.fn), null);
		Assert.map(Rt.stream().put(Maps.of(), null, Rt.fn));
		Assert.map(Rt.stream().put(Maps.of(), i -> i, null));
		Assert.map(Rt.stream().put(Maps.of(), i -> i, Rt.fn), -1, "-1", null, "null", 1, "1", 0,
			"0");
		Assert.map(Rt.stream().put(null, Maps.of(), i -> i, Rt.fn));
		Assert.equal(Rt.stream().put(Maps.Put.def, null, i -> i, Rt.fn), null);
		Assert.map(Rt.stream().put(Maps.Put.def, Maps.of(), null, Rt.fn));
		Assert.map(Rt.stream().put(Maps.Put.def, Maps.of(), i -> i, null));
		Assert.map(Rt.stream().put(Maps.Put.first, Maps.of(), i -> i, Rt.fn), -1, "-1", null,
			"null", 1, "1", 0, "0");
		Assert.equal(Ex.stream().put(null, i -> i), null);
		Assert.map(Ex.stream().put(Maps.of(), null));
		Assert.map(Ex.stream().put(Maps.of(), i -> i), -1, -1, null, null, 1, 1, 0, 0);
		Assert.equal(Ex.stream().put(null, i -> i, Ex.fn), null);
		Assert.map(Ex.stream().put(Maps.of(), null, Ex.fn));
		Assert.map(Ex.stream().put(Maps.of(), i -> i, null));
		Assert.map(Ex.stream().put(Maps.of(), i -> i, Ex.fn), -1, "-1", null, "null", 1, "1", 0,
			"0");
		Assert.map(Ex.stream().put(null, Maps.of(), i -> i, Ex.fn));
		Assert.equal(Ex.stream().put(Maps.Put.def, null, i -> i, Ex.fn), null);
		Assert.map(Ex.stream().put(Maps.Put.def, Maps.of(), null, Ex.fn));
		Assert.map(Ex.stream().put(Maps.Put.def, Maps.of(), i -> i, null));
		Assert.map(Ex.stream().put(Maps.Put.first, Maps.of(), i -> i, Ex.fn), -1, "-1", null,
			"null", 1, "1", 0, "0");
	}

	@Test
	public void shouldCollectToArray() throws IOException {
		Assert.array(Rt.empty.toArray());
		Assert.array(Rt.empty.toArray(Integer.class));
		Assert.array(Rt.stream().toArray(), -1, null, 1, 0);
		Assert.equal(Rt.stream().toArray(null), null);
		Assert.array(Rt.stream().toArray(Integer.class), -1, null, 1, 0);
		Assert.array(Ex.empty.toArray());
		Assert.array(Ex.empty.toArray(Integer.class));
		Assert.array(Ex.stream().toArray(), -1, null, 1, 0);
		Assert.equal(Ex.stream().toArray(null), null);
		Assert.array(Ex.stream().toArray(Integer.class), -1, null, 1, 0);
	}

	@Test
	public void shouldCollectToSet() throws IOException {
		Assert.unordered(Rt.empty.toSet());
		Assert.unordered(Rt.stream().toSet(), -1, 0, null, 1);
		Assert.unordered(Rt.of(1, 0, null, 0, -1, null).toSet(), 1, 0, null, -1);
		Assert.unordered(Ex.empty.toSet());
		Assert.unordered(Ex.stream().toSet(), -1, 0, null, 1);
		Assert.unordered(Ex.of(1, 0, null, 0, -1, null).toSet(), 1, 0, null, -1);
	}

	@Test
	public void shouldCollectToList() throws IOException {
		Assert.ordered(Rt.empty.toList());
		Assert.ordered(Rt.stream().toList(), -1, null, 1, 0);
		Assert.ordered(Rt.of(1, 0, null, 0, -1, null).toList(), 1, 0, null, 0, -1, null);
		Assert.ordered(Ex.empty.toList());
		Assert.ordered(Ex.stream().toList(), -1, null, 1, 0);
		Assert.ordered(Ex.of(1, 0, null, 0, -1, null).toList(), 1, 0, null, 0, -1, null);
	}

	@Test
	public void shouldCollectToMap() throws IOException {
		Assert.map(Rt.empty.toMap(t -> t));
		Assert.map(Rt.stream().toMap(i -> i), -1, -1, 0, 0, null, null, 1, 1);
		Assert.map(Rt.stream().toMap(i -> i, _ -> 0), -1, 0, 0, 0, null, 0, 1, 0);
		Assert.map(Ex.empty.toMap(t -> t));
		Assert.map(Ex.stream().toMap(i -> i), -1, -1, 0, 0, null, null, 1, 1);
		Assert.map(Ex.stream().toMap(i -> i, _ -> 0), -1, 0, 0, 0, null, 0, 1, 0);
	}

	@Test
	public void shouldCollectWithCollector() throws IOException {
		Assert.equal(Rt.empty.collect(Joiner.OR), "");
		Assert.equal(Rt.stream().collect((Collector<Integer, ?, ?>) null), null);
		Assert.equal(Rt.stream().collect(Joiner.OR), "-1|null|1|0");
		Assert.equal(Ex.empty.collect(Joiner.OR), "");
		Assert.equal(Ex.stream().collect((Collector<Integer, ?, ?>) null), null);
		Assert.equal(Ex.stream().collect(Joiner.OR), "-1|null|1|0");
	}

	@Test
	public void shouldCollectWithAccumulator() throws IOException {
		Assert.equal(Rt.empty.collect(StringBuilder::new, StringBuilder::append, //
			StringBuilder::toString), "");
		Assert.equal(Rt.stream().collect(null, (_, _) -> {}, _ -> ""), null);
		Assert.equal(Rt.stream().collect(StringBuilder::new, (_, _) -> {}, null), null);
		Assert.equal(Rt.stream().collect(StringBuilder::new, null, _ -> ""), "");
		Assert.equal(Rt.stream().collect(() -> null, (_, _) -> {}, _ -> ""), "");
		Assert.equal(Rt.stream().collect(StringBuilder::new, StringBuilder::append, //
			b -> "[" + b + "]"), "[-1null10]");
		Assert.equal(Ex.empty.collect(StringBuilder::new, StringBuilder::append, //
			StringBuilder::toString), "");
		Assert.equal(Ex.stream().collect(null, (_, _) -> {}, _ -> ""), null);
		Assert.equal(Ex.stream().collect(StringBuilder::new, (_, _) -> {}, null), null);
		Assert.equal(Ex.stream().collect(StringBuilder::new, null, _ -> ""), "");
		Assert.equal(Ex.stream().collect(() -> null, (_, _) -> {}, _ -> ""), "");
		Assert.equal(Ex.stream().collect(StringBuilder::new, StringBuilder::append, //
			b -> "[" + b + "]"), "[-1null10]");
	}

	@Test
	public void shouldReduceElements() throws IOException {
		Assert.equal(Rt.empty.reduce((_, _) -> 1), null);
		Assert.equal(Rt.stream().reduce(null), null);
		Assert.equal(Rt.stream().reduce(null, 1), 1);
		Assert.equal(Rt.stream().reduce((i, _) -> i), -1);
		Assert.equal(Rt.stream().reduce((i, _) -> i, 3), -1);
		Assert.equal(Ex.empty.reduce((_, _) -> 1, 3), 3);
		Assert.equal(Ex.stream().reduce(null), null);
		Assert.equal(Ex.stream().reduce(null, 1), 1);
		Assert.equal(Ex.stream().reduce((i, _) -> i), -1);
		Assert.equal(Ex.stream().reduce((i, _) -> i, 3), -1);
	}
}
