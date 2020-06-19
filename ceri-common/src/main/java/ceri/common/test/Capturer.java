package ceri.common.test;

import static ceri.common.test.TestUtil.assertList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import java.util.function.LongConsumer;
import ceri.common.collection.ArrayUtil;

/**
 * Simple consumer to collect values during testing, then verify.
 */
public class Capturer<T> implements Consumer<T> {
	public final List<T> values = new ArrayList<>();

	public static <T> Capturer<T> of() {
		return new Capturer<>();
	}

	public static Capturer.Int ofInt() {
		return new Capturer.Int();
	}

	public static <T, U> Capturer.Bi<T, U> ofBi() {
		return new Capturer.Bi<>(of(), of());
	}

	@Override
	public void accept(T t) {
		values.add(t);
	}

	public Capturer<T> reset() {
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

	public static class Int extends Capturer<Integer> implements IntConsumer, LongConsumer {
		@Override
		public void accept(int value) {
			accept(Integer.valueOf(value));
		}

		@Override
		public void accept(long value) {
			accept(Math.toIntExact(value));
		}

		@Override
		public Capturer.Int reset() {
			values.clear();
			return this;
		}

		public final void verifyInt(int... values) {
			verify(ArrayUtil.intList(values));
		}
	}

	public static class Bi<T, U> implements BiConsumer<T, U> {
		public final Capturer<T> first;
		public final Capturer<U> second;

		Bi(Capturer<T> first, Capturer<U> second) {
			this.first = first;
			this.second = second;
		}

		public Capturer.Bi<T, U> reset() {
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
