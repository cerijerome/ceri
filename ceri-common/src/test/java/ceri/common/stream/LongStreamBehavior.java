package ceri.common.stream;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;
import java.util.PrimitiveIterator;
import java.util.Spliterator;
import org.junit.Test;
import ceri.common.array.Array;
import ceri.common.collect.Immutable;
import ceri.common.except.ExceptionAdapter;
import ceri.common.function.Excepts;
import ceri.common.function.Functions;
import ceri.common.test.Assert;
import ceri.common.test.Captor;
import ceri.common.test.TestCollection;
import ceri.common.text.Joiner;

public class LongStreamBehavior {
	private static final List<Long> nullList = null;
	private static final List<Long> emptyList = Immutable.list();
	private static final List<Long> list = Immutable.listOf(-1L, 0L, 1L);
	private static final PrimitiveIterator.OfLong nullIterator = null;
	private static final Spliterator.OfLong nullSpliterator = null;
	private static final java.util.stream.LongStream nullStream = null;
	private static final long[] nullArray = null;
	private static final long[] emptyArray = Array.LONG.empty;
	private static final long[] array = { -1, 0, 1 };
	private static final Stream.ExAdapter<IOException> adapter =
		e -> (e instanceof UncheckedIOException uie) ? uie.getCause() : null;

	private static java.util.stream.LongStream longStream(long... values) {
		return java.util.stream.LongStream.of(values);
	}

	private static PrimitiveIterator.OfLong iterator(long... values) {
		return longStream(values).iterator();
	}

	private static Spliterator.OfLong spliterator(long... values) {
		return longStream(values).spliterator();
	}

	private static class Rt {
		private static final LongStream.Rt empty = LongStream.Rt.EMPTY;
		private static final Functions.LongPredicate no = _ -> false;
		private static final Functions.LongPredicate yes = _ -> true;
		private static final Functions.LongPredicate pred = i -> i >= 0;
		private static final Functions.LongOperator op = i -> -i;
		private static final Functions.LongToIntFunction longFn = i -> (int) -i;
		private static final Functions.LongToDoubleFunction doubleFn = i -> -i;
		private static final Functions.LongFunction<Object> objFn = String::valueOf;
		private static final Functions.LongFunction<LongStream.Rt> flatFn = i -> Rt.of(-i, 0, i);

		private Rt() {}

		@SafeVarargs
		private static LongStream.Rt of(long... values) {
			return LongStream.of(values);
		}

		private static LongStream.Rt stream() {
			return of(-1, 0, 1);
		}
	}

	private static class Ex {
		private static final LongStream.Ex<IOException> empty = LongStream.Ex.empty();
		private static final Excepts.LongPredicate<IOException> no = _ -> false;
		private static final Excepts.LongPredicate<IOException> yes = _ -> true;
		private static final Excepts.LongPredicate<IOException> pred = i -> i >= 0;
		private static final Excepts.LongOperator<IOException> op = i -> -i;
		private static final Excepts.LongToIntFunction<IOException> longFn = i -> (int) -i;
		private static final Excepts.LongToDoubleFunction<IOException> doubleFn = i -> -i;
		private static final Excepts.LongFunction<IOException, Object> objFn = String::valueOf;
		private static final Excepts.LongFunction<IOException, LongStream.Ex<IOException>> flatFn =
			i -> Ex.of(-i, 0, i);

		private Ex() {}

		@SafeVarargs
		private static LongStream.Ex<IOException> of(long... values) {
			return LongStream.of(values).ex();
		}

		private static LongStream.Ex<IOException> stream() {
			return of(-1, 0, 1);
		}

		private static LongStream.Ex<IOException> io() {
			return Rt.stream().filterEx(i -> {
				if (i == 0) throw new IOException();
				return true;
			});
		}
	}

	@Test
	public void testEmpty() throws IOException {
		Assert.stream(Rt.empty);
		Assert.stream(Ex.empty);
		Assert.same(LongStream.Ex.empty(), LongStream.Ex.empty());
	}

	@Test
	public void testOf() {
		Assert.stream(LongStream.of(nullArray));
		Assert.stream(LongStream.of(emptyArray));
		Assert.stream(LongStream.of(array), -1, 0, 1);
		Assert.stream(LongStream.of(nullArray, 1));
		Assert.stream(LongStream.of(emptyArray, 1));
		Assert.stream(LongStream.of(array, 1), 0, 1);
	}

	@Test
	public void testSlice() {
		Assert.stream(LongStream.slice(-1, 0));
		Assert.stream(LongStream.slice(-1, 1), -1);
		Assert.stream(LongStream.slice(-1, 2), -1, 0);
	}

	@Test
	public void testFrom() throws IOException {
		Assert.stream(LongStream.from(nullList));
		Assert.stream(LongStream.from(emptyList));
		Assert.stream(LongStream.from(list), -1, 0, 1);
		Assert.stream(LongStream.from(nullIterator));
		Assert.stream(LongStream.from(iterator()));
		Assert.stream(LongStream.from(iterator(-1, 0, 1)), -1, 0, 1);
		Assert.stream(LongStream.from(nullStream));
		Assert.stream(LongStream.from(longStream()));
		Assert.stream(LongStream.from(longStream(-1, 0, 1)), -1, 0, 1);
		Assert.stream(LongStream.from(nullSpliterator));
		Assert.stream(LongStream.from(spliterator()));
		Assert.stream(LongStream.from(spliterator(-1, 0, 1)), -1, 0, 1);
		Assert.stream(LongStream.from(nullList, adapter));
		Assert.stream(LongStream.from(emptyList, adapter));
		Assert.stream(LongStream.from(nullIterator, adapter));
		Assert.stream(LongStream.from(iterator(), adapter));
		Assert.stream(LongStream.from(iterator(-1, 0, 1), adapter), -1, 0, 1);
		Assert.stream(LongStream.from(nullStream, adapter));
		Assert.stream(LongStream.from(longStream(), adapter));
		Assert.stream(LongStream.from(nullSpliterator, adapter));
		Assert.stream(LongStream.from(spliterator(), adapter));
	}

	@Test
	public void testIteratorExceptionAdapter() throws IOException {
		var uie = new UncheckedIOException(new IOException("test"));
		var ise = new IllegalStateException("test");
		var iterator = TestCollection.longIterator(1, 2, 3);
		iterator.next.set(ise, uie, null);
		var stream = LongStream.<IOException>from(iterator, null);
		Assert.thrown(IllegalStateException.class, stream::next);
		Assert.thrown(UncheckedIOException.class, stream::next);
		Assert.stream(stream, 1, 2, 3);
		iterator = TestCollection.longIterator(1, 2, 3);
		iterator.next.set(ise, uie, null);
		stream = LongStream.from(iterator,
			e -> (e instanceof UncheckedIOException u) ? u.getCause() : null);
		Assert.thrown(IllegalStateException.class, stream::next);
		Assert.thrown(IOException.class, stream::next);
		Assert.stream(stream, 1, 2, 3);
	}

	@Test
	public void testSpliteratorExceptionAdapter() throws IOException {
		var uie = new UncheckedIOException(new IOException("test"));
		var ise = new IllegalStateException("test");
		var spliterator = TestCollection.longSpliterator(1, 2, 3);
		spliterator.tryAdvance.set(ise, uie, null);
		var stream = LongStream.<IOException>from(spliterator, null);
		Assert.thrown(IllegalStateException.class, stream::next);
		Assert.thrown(UncheckedIOException.class, stream::next);
		Assert.stream(stream, 1, 2, 3);
		spliterator = TestCollection.longSpliterator(1, 2, 3);
		spliterator.tryAdvance.set(ise, uie, null);
		stream = LongStream.from(spliterator,
			e -> (e instanceof UncheckedIOException u) ? u.getCause() : null);
		Assert.thrown(IllegalStateException.class, stream::next);
		Assert.thrown(IOException.class, stream::next);
		Assert.stream(stream, 1, 2, 3);
	}

	@Test
	public void testMerge() throws IOException {
		Assert.stream(LongStream.merge());
		Assert.stream(LongStream.merge((LongStream.Rt[]) null));
		Assert.stream(LongStream.merge(Rt.of(1, 2), Rt.empty, Rt.of(3), null), 1, 2, 3);
		Assert.stream(LongStream.merge((LongStream.Ex<IOException>[]) null));
		Assert.stream(LongStream.merge(Ex.of(1, 2), Ex.empty, Ex.of(3), null), 1, 2, 3);
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
		Assert.stream(Rt.stream().runtime(), -1, 0, 1);
		Assert.stream(Ex.empty.runtime());
		Assert.stream(Ex.stream().runtime(), -1, 0, 1);
		Assert.runtime(() -> Ex.io().runtime().toArray());
	}

	@Test
	public void shouldFilterElements() throws IOException {
		Assert.stream(Rt.empty.filter(null));
		Assert.stream(Rt.empty.filterEx(null));
		Assert.stream(Rt.empty.filter(Rt.pred));
		Assert.stream(Rt.empty.filterEx(Rt.pred));
		Assert.stream(Rt.stream().filter(null), -1, 0, 1);
		Assert.stream(Rt.stream().filterEx(null), -1, 0, 1);
		Assert.stream(Rt.stream().filter(Rt.no));
		Assert.stream(Rt.stream().filter(Rt.yes), -1, 0, 1);
		Assert.stream(Rt.stream().filter(Rt.pred), 0, 1);
		Assert.stream(Rt.stream().filterEx(Ex.pred), 0, 1);
		Assert.stream(Ex.empty.filter(null));
		Assert.stream(Ex.empty.filterRt(null));
		Assert.stream(Ex.empty.filter(Ex.pred));
		Assert.stream(Ex.empty.filterRt(Rt.pred));
		Assert.stream(Ex.stream().filter(null), -1, 0, 1);
		Assert.stream(Ex.stream().filterRt(null), -1, 0, 1);
		Assert.stream(Ex.stream().filter(Ex.no));
		Assert.stream(Ex.stream().filter(Ex.yes), -1, 0, 1);
		Assert.stream(Ex.stream().filter(Ex.pred), 0, 1);
		Assert.stream(Ex.stream().filterRt(Rt.pred), 0, 1);
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
	public void shouldBoxElements() throws IOException {
		Assert.stream(Rt.empty.boxed());
		Assert.stream(Rt.stream().boxed(), -1L, 0L, 1L);
		Assert.stream(Ex.empty.boxed());
		Assert.stream(Ex.stream().boxed(), -1L, 0L, 1L);
	}

	@Test
	public void shouldProvideIntElements() throws IOException {
		Assert.stream(Rt.empty.ints());
		Assert.stream(Rt.stream().ints(), -1, 0, 1);
		Assert.stream(Ex.empty.ints());
		Assert.stream(Ex.stream().ints(), -1, 0, 1);
	}

	@Test
	public void shouldMapElements() throws IOException {
		Assert.stream(Rt.empty.map(null));
		Assert.stream(Rt.empty.map(Rt.op));
		Assert.stream(Rt.empty.mapEx(Ex.op));
		Assert.stream(Rt.stream().map(null));
		Assert.stream(Rt.stream().map(Rt.op), 1, 0, -1);
		Assert.stream(Rt.stream().mapEx(Ex.op), 1, 0, -1);
		Assert.stream(Ex.empty.map(null));
		Assert.stream(Ex.empty.map(Ex.op));
		Assert.stream(Ex.empty.mapRt(Rt.op));
		Assert.stream(Ex.stream().map(null));
		Assert.stream(Ex.stream().map(Ex.op), 1, 0, -1);
		Assert.stream(Ex.stream().mapRt(Rt.op), 1, 0, -1);
	}

	@Test
	public void shouldMapElementsToInt() throws IOException {
		Assert.stream(Rt.empty.mapToInt(null));
		Assert.stream(Rt.empty.mapToInt(Rt.longFn));
		Assert.stream(Rt.empty.mapToIntEx(Ex.longFn));
		Assert.stream(Rt.stream().mapToInt(null));
		Assert.stream(Rt.stream().mapToInt(Rt.longFn), 1, 0, -1);
		Assert.stream(Rt.stream().mapToIntEx(Ex.longFn), 1, 0, -1);
		Assert.stream(Ex.empty.mapToInt(null));
		Assert.stream(Ex.empty.mapToInt(Ex.longFn));
		Assert.stream(Ex.empty.mapToIntRt(Rt.longFn));
		Assert.stream(Ex.stream().mapToInt(null));
		Assert.stream(Ex.stream().mapToInt(Ex.longFn), 1, 0, -1);
		Assert.stream(Ex.stream().mapToIntRt(Rt.longFn), 1, 0, -1);
	}

	@Test
	public void shouldMapElementsToDouble() throws IOException {
		Assert.stream(Rt.empty.mapToDouble(null));
		Assert.stream(Rt.empty.mapToDouble(Rt.doubleFn));
		Assert.stream(Rt.empty.mapToDoubleEx(Ex.doubleFn));
		Assert.stream(Rt.stream().mapToDouble(null));
		Assert.stream(Rt.stream().mapToDouble(Rt.doubleFn), 1.0, 0.0, -1.0);
		Assert.stream(Rt.stream().mapToDoubleEx(Ex.doubleFn), 1.0, 0.0, -1.0);
		Assert.stream(Ex.empty.mapToDouble(null));
		Assert.stream(Ex.empty.mapToDouble(Ex.doubleFn));
		Assert.stream(Ex.empty.mapToDoubleRt(Rt.doubleFn));
		Assert.stream(Ex.stream().mapToDouble(null));
		Assert.stream(Ex.stream().mapToDouble(Ex.doubleFn), 1.0, 0.0, -1.0);
		Assert.stream(Ex.stream().mapToDoubleRt(Rt.doubleFn), 1.0, 0.0, -1.0);
	}

	@Test
	public void shouldMapElementsToObj() throws IOException {
		Assert.stream(Rt.empty.mapToObj(null));
		Assert.stream(Rt.empty.mapToObj(Rt.objFn));
		Assert.stream(Rt.empty.mapToObjEx(Ex.objFn));
		Assert.stream(Rt.stream().mapToObj(null));
		Assert.stream(Rt.stream().mapToObj(Rt.objFn), "-1", "0", "1");
		Assert.stream(Rt.stream().mapToObjEx(Ex.objFn), "-1", "0", "1");
		Assert.stream(Ex.empty.mapToObj(null));
		Assert.stream(Ex.empty.mapToObj(Ex.objFn));
		Assert.stream(Ex.empty.mapToObjRt(Rt.objFn));
		Assert.stream(Ex.stream().mapToObj(null));
		Assert.stream(Ex.stream().mapToObj(Ex.objFn), "-1", "0", "1");
		Assert.stream(Ex.stream().mapToObjRt(Rt.objFn), "-1", "0", "1");
	}

	@Test
	public void shouldFlatMapElements() throws IOException {
		Assert.stream(Rt.empty.flatMap(null));
		Assert.stream(Rt.empty.flatMap(Rt.flatFn));
		Assert.stream(Rt.empty.flatMapEx(Ex.flatFn));
		Assert.stream(Rt.stream().flatMap(null));
		Assert.stream(Rt.stream().flatMap(Rt.flatFn), 1, 0, -1, 0, 0, 0, -1, 0, 1);
		Assert.stream(Rt.stream().flatMapEx(Ex.flatFn), 1, 0, -1, 0, 0, 0, -1, 0, 1);
		Assert.stream(Ex.empty.flatMap(null));
		Assert.stream(Ex.empty.flatMap(Ex.flatFn));
		Assert.stream(Ex.empty.flatMapRt(Rt.flatFn));
		Assert.stream(Ex.stream().flatMap(null));
		Assert.stream(Ex.stream().flatMap(Ex.flatFn), 1, 0, -1, 0, 0, 0, -1, 0, 1);
		Assert.stream(Ex.stream().flatMapRt(Rt.flatFn), 1, 0, -1, 0, 0, 0, -1, 0, 1);
	}

	@Test
	public void shouldLimitElements() throws IOException {
		Assert.stream(Rt.empty.limit(3));
		Assert.stream(Rt.stream().limit(0));
		Assert.stream(Rt.stream().limit(2), -1, 0);
		Assert.stream(Rt.stream().limit(5), -1, 0, 1);
		Assert.stream(Ex.empty.limit(3));
		Assert.stream(Ex.stream().limit(0));
		Assert.stream(Ex.stream().limit(2), -1, 0);
		Assert.stream(Ex.stream().limit(5), -1, 0, 1);
	}

	@Test
	public void shouldProvideDistinctElements() throws IOException {
		Assert.stream(Rt.empty.distinct());
		Assert.stream(Rt.of(1, 0, 0, -1, 1, -1).distinct(), 1, 0, -1);
		Assert.stream(Ex.empty.distinct());
		Assert.stream(Ex.of(1, 0, 0, -1, 1, -1).distinct(), 1, 0, -1);
	}

	@Test
	public void shouldProvideSortedElements() throws IOException {
		Assert.stream(Rt.empty.sorted());
		Assert.stream(Rt.of(0, -1, 1).sorted(), -1, 0, 1);
		Assert.stream(Ex.empty.sorted());
		Assert.stream(Ex.of(0, -1, 1).sorted(), -1, 0, 1);
	}

	@Test
	public void shouldProvideNextElement() throws IOException {
		Assert.equal(Rt.empty.next(), null);
		Assert.equal(Rt.empty.next(3), 3L);
		var rt = Rt.stream();
		Assert.equal(rt.next(3), -1L);
		Assert.equal(rt.next(), 0L);
		Assert.equal(rt.next(3), 1L);
		Assert.equal(rt.next(), null);
		Assert.equal(rt.next(3), 3L);
		Assert.equal(Ex.empty.next(), null);
		Assert.equal(Ex.empty.next(3), 3L);
		var ex = Ex.stream();
		Assert.equal(ex.next(3), -1L);
		Assert.equal(ex.next(), 0L);
		Assert.equal(ex.next(3), 1L);
		Assert.equal(ex.next(), null);
		Assert.equal(ex.next(3), 3L);
	}

	@Test
	public void shouldSkipElements() throws IOException {
		Assert.stream(Rt.empty.skip(2));
		Assert.stream(Rt.stream().skip(2), 1);
		Assert.stream(Rt.stream().skip(5));
		Assert.stream(Ex.empty.skip(2));
		Assert.stream(Ex.stream().skip(2), 1);
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
		Assert.equal(Rt.stream().count(), 3L);
		Assert.equal(Ex.empty.count(), 0L);
		Assert.equal(Ex.stream().count(), 3L);
	}

	@Test
	public void shouldProvideIterator() {
		Assert.iterator(Rt.empty.iterator());
		Assert.iterator(Rt.stream().iterator(), -1L, 0L, 1L);
		Assert.iterator(Ex.empty.iterator());
		Assert.iterator(Ex.stream().iterator(), -1L, 0L, 1L);
	}

	@Test
	public void shouldConsumeEachElement() throws IOException {
		Captor.ofLong().applyLong(Rt.empty::forEach).verifyLong();
		Captor.ofLong().applyLong(Rt.empty::forEachEx).verifyLong();
		Captor.ofLong().applyLong(Rt.stream()::forEach).verifyLong(-1, 0, 1);
		Captor.ofLong().applyLong(Rt.stream()::forEachEx).verifyLong(-1, 0, 1);
		Captor.ofLong().applyLong(c -> Ex.empty.forEach(c::accept)).verifyLong();
		Captor.ofLong().applyLong(Ex.empty::forEachRt).verifyLong();
		Captor.ofLong().applyLong(c -> Ex.stream().forEach(c::accept)).verifyLong(-1, 0, 1);
		Captor.ofLong().applyLong(Ex.stream()::forEachRt).verifyLong(-1, 0, 1);
	}

	@Test
	public void shouldCollectWithCollector() throws IOException {
		Assert.equal(Rt.empty.collect(Joiner.OR), "");
		Assert.equal(Rt.stream().collect((LongStream.Collector<Long, ?>) null), null);
		Assert.equal(Rt.stream().collect(Joiner.OR), "-1|0|1");
		Assert.array(Rt.of(1, 0, -1).collect(Collect.Longs.sortedArray), -1, 0, 1);
		Assert.equal(Ex.empty.collect(Joiner.OR), "");
		Assert.equal(Ex.stream().collect((LongStream.Collector<Long, ?>) null), null);
		Assert.equal(Ex.stream().collect(Joiner.OR), "-1|0|1");
		Assert.array(Ex.of(1, 0, -1).collect(Collect.Longs.sortedArray), -1, 0, 1);
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
			b -> "[" + b + "]"), "[-101]");
		Rt.stream().collect(Captor::of, Captor::accept).verify(-1L, 0L, 1L);
		Assert.equal(Ex.empty.collect(StringBuilder::new, StringBuilder::append, //
			StringBuilder::toString), "");
		Assert.equal(Ex.stream().collect(null, (_, _) -> {}, _ -> ""), null);
		Assert.equal(Ex.stream().collect(StringBuilder::new, (_, _) -> {}, null), null);
		Assert.equal(Ex.stream().collect(StringBuilder::new, null, _ -> ""), "");
		Assert.equal(Ex.stream().collect(() -> null, (_, _) -> {}, _ -> ""), "");
		Assert.equal(Ex.stream().collect(StringBuilder::new, StringBuilder::append, //
			b -> "[" + b + "]"), "[-101]");
		Ex.stream().collect(Captor::of, Captor::accept).verify(-1L, 0L, 1L);
	}

	@Test
	public void shouldReduceElements() throws IOException {
		Assert.equal(Rt.empty.reduce((_, _) -> 1), null);
		Assert.equal(Rt.stream().reduce(null), null);
		Assert.equal(Rt.stream().reduce(null, 1), 1L);
		Assert.equal(Rt.stream().reduce((i, _) -> i), -1L);
		Assert.equal(Rt.stream().reduce((i, _) -> i, 3), -1L);
		Assert.equal(Ex.empty.reduce((_, _) -> 1, 3), 3L);
		Assert.equal(Ex.stream().reduce(null), null);
		Assert.equal(Ex.stream().reduce(null, 1), 1L);
		Assert.equal(Ex.stream().reduce((i, _) -> i), -1L);
		Assert.equal(Ex.stream().reduce((i, _) -> i, 3), -1L);
	}

	@Test
	public void shouldDetermineMin() throws IOException {
		Assert.equal(Rt.empty.min(0), 0L);
		Assert.equal(Rt.stream().min(0), -1L);
		Assert.equal(Rt.of(1, 0, -1).min(0), -1L);
		Assert.equal(Ex.empty.min(0), 0L);
		Assert.equal(Ex.stream().min(0), -1L);
		Assert.equal(Ex.of(1, 0, -1).min(0), -1L);
	}

	@Test
	public void shouldDetermineMax() throws IOException {
		Assert.equal(Rt.empty.max(0), 0L);
		Assert.equal(Rt.stream().max(0), 1L);
		Assert.equal(Rt.of(1, 0, -1).max(0), 1L);
		Assert.equal(Ex.empty.max(0), 0L);
		Assert.equal(Ex.stream().max(0), 1L);
		Assert.equal(Ex.of(1, 0, -1).max(0), 1L);
	}

	@Test
	public void shouldDetermineSum() throws IOException {
		Assert.equal(Rt.empty.sum(), 0L);
		Assert.equal(Rt.of(0, -1).sum(), -1L);
		Assert.equal(Ex.empty.sum(), 0L);
		Assert.equal(Ex.of(0, -1).sum(), -1L);
	}

	@Test
	public void shouldDetermineAverage() throws IOException {
		Assert.equal(Rt.empty.average(), 0.0);
		Assert.equal(Rt.of(0, -1).average(), -0.5);
		Assert.equal(Ex.empty.average(), 0.0);
		Assert.equal(Ex.of(0, -1).average(), -0.5);
	}

	@Test
	public void shouldUseReducers() throws IOException {
		Assert.equal(Rt.empty.reduce(Reduce.Longs.and()), null);
		Assert.equal(Rt.of(7, 14).reduce(null), null);
		Assert.equal(Rt.of(7, 14).reduce(Reduce.Longs.and()), 6L);
		Assert.equal(Rt.of(7, 14).reduce(Reduce.Longs.or()), 15L);
		Assert.equal(Rt.of(7, 14).reduce(Reduce.Longs.xor()), 9L);
		Assert.equal(Ex.empty.reduce(Reduce.Longs.and()), null);
		Assert.equal(Ex.of(7, 14).reduce(null), null);
		Assert.equal(Ex.of(7, 14).reduce(Reduce.Longs.and()), 6L);
		Assert.equal(Ex.of(7, 14).reduce(Reduce.Longs.or()), 15L);
		Assert.equal(Ex.of(7, 14).reduce(Reduce.Longs.xor()), 9L);
	}
}
