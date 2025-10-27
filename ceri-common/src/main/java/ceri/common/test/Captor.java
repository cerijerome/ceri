package ceri.common.test;

import java.util.List;
import ceri.common.array.ArrayUtil;
import ceri.common.collect.Immutable;
import ceri.common.collect.Lists;
import ceri.common.function.Excepts;
import ceri.common.function.Functions;

/**
 * Simple consumer to collect values during testing, then verify.
 */
public class Captor<T> implements Functions.Consumer<T> {
	public final List<T> values = Lists.of();

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

	public static <T> Captor.N<T> ofN() {
		return new Captor.N<>();
	}

	@Override
	public void accept(T t) {
		values.add(t);
	}

	public <R> R accept(T t, R response) {
		accept(t);
		return response;
	}

	public <E extends Exception> Captor<T> apply(Excepts.Consumer<E, Captor<T>> consumer) throws E {
		if (consumer != null) consumer.accept(this);
		return this;
	}

	public Captor<T> reset() {
		values.clear();
		return this;
	}

	@SafeVarargs
	public final void verify(T... values) {
		verify(Lists.wrap(values));
	}

	public void verify(List<T> values) {
		Assert.list(this.values, values);
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

		public <R> R accept(T t, U u, R response) {
			accept(t, u);
			return response;
		}

		public <E extends Exception> Bi<T, U> apply(Excepts.Consumer<E, Bi<T, U>> consumer)
			throws E {
			if (consumer != null) consumer.accept(this);
			return this;
		}

		public void verify(List<T> ts, List<U> us) {
			first.verify(ts);
			second.verify(us);
		}

		public void verify() {
			first.verify();
			second.verify();
		}

		public void verify(T t, U u) {
			first.verify(t);
			second.verify(u);
		}

		public void verify(T t0, U u0, T t1, U u1) {
			first.verify(t0, t1);
			second.verify(u0, u1);
		}

		public void verify(T t0, U u0, T t1, U u1, T t2, U u2) {
			first.verify(t0, t1, t2);
			second.verify(u0, u1, u2);
		}

		public void verify(T t0, U u0, T t1, U u1, T t2, U u2, T t3, U u3) {
			first.verify(t0, t1, t2, t3);
			second.verify(u0, u1, u2, u3);
		}

		public void verify(T t0, U u0, T t1, U u1, T t2, U u2, T t3, U u3, T t4, U u4) {
			first.verify(t0, t1, t2, t3, t4);
			second.verify(u0, u1, u2, u3, u4);
		}
	}

	public static class N<T> extends Captor<List<T>> {
		@SafeVarargs
		public final void acceptAll(T... ts) {
			super.accept(Immutable.listOf(ts));
		}
	}
}
