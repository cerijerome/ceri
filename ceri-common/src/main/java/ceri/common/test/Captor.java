package ceri.common.test;

import static ceri.common.test.AssertUtil.assertList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import java.util.function.LongConsumer;
import ceri.common.collection.ArrayUtil;
import ceri.common.function.Excepts;

/**
 * Simple consumer to collect values during testing, then verify.
 */
public class Captor<T> implements Consumer<T>, Excepts.Consumer<RuntimeException, T> {
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

	public static class OfInt extends Captor<Integer> implements IntConsumer, LongConsumer,
		Excepts.IntConsumer<RuntimeException>, Excepts.LongConsumer<RuntimeException> {
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
			verify(ArrayUtil.intList(values));
		}

		public int[] ints() {
			return ArrayUtil.ints(values);
		}
	}

	public static class OfLong extends Captor<Long>
		implements LongConsumer, Excepts.LongConsumer<RuntimeException> {
		@Override
		public void accept(long value) {
			accept(Long.valueOf(value));
		}

		@Override
		public Captor.OfLong reset() {
			values.clear();
			return this;
		}

		public final void verifyLong(long... values) {
			verify(ArrayUtil.longList(values));
		}

		public long[] longs() {
			return ArrayUtil.longs(values);
		}
	}

	public static class Bi<T, U>
		implements BiConsumer<T, U>, Excepts.BiConsumer<RuntimeException, T, U> {
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
