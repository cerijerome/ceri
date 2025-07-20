package ceri.common.test;

import static ceri.common.test.AssertUtil.assertList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import ceri.common.array.ArrayUtil;
import ceri.common.function.Functions;

/**
 * Simple consumer to collect values during testing, then verify.
 */
public class Captor<T> implements Functions.Consumer<T> {
	public final List<T> values = new ArrayList<>();

	public static <T> Captor<T> of() {
		return new Captor<>();
	}

	public static Captor.OfInt ofInt() {
		return new Captor.OfInt();
	}

	public static Captor.OfLong ofLong() {
		return new Captor.OfLong();
	}

	public static <T, U> Captor.Bi<T, U> ofBi() {
		return new Captor.Bi<>(of(), of());
	}

	@Override
	public void accept(T t) {
		values.add(t);
	}

	public Captor<T> reset() {
		values.clear();
		return this;
	}

	@SafeVarargs
	public final void verify(T... values) {
		verify(Arrays.asList(values));
	}

	public void verify(List<T> values) {
		assertList(this.values, values);
	}

	public static class OfInt extends Captor<Integer>
		implements Functions.IntConsumer, Functions.LongConsumer {
		@Override
		public void accept(int value) {
			accept(Integer.valueOf(value));
		}

		@Override
		public void accept(long value) {
			accept(Math.toIntExact(value));
		}

		@Override
		public Captor.OfInt reset() {
			values.clear();
			return this;
		}

		public final void verifyInt(int... values) {
			verify(ArrayUtil.ints.list(values));
		}

		public int[] ints() {
			return ArrayUtil.ints.unboxed(values);
		}
	}

	public static class OfLong extends Captor<Long> implements Functions.LongConsumer {
		@Override
		public void accept(long value) {
			accept(Long.valueOf(value));
		}

		@Override
		public OfLong reset() {
			values.clear();
			return this;
		}

		public final void verifyLong(long... values) {
			verify(ArrayUtil.longs.list(values));
		}

		public long[] longs() {
			return ArrayUtil.longs.unboxed(values);
		}
	}

	public static class Bi<T, U> implements Functions.BiConsumer<T, U> {
		public final Captor<T> first;
		public final Captor<U> second;

		Bi(Captor<T> first, Captor<U> second) {
			this.first = first;
			this.second = second;
		}

		public Captor.Bi<T, U> reset() {
			first.reset();
			second.reset();
			return this;
		}

		@Override
		public void accept(T t, U u) {
			first.accept(t);
			second.accept(u);
		}
	}
}
