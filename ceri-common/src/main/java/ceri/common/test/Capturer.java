package ceri.common.test;

import static ceri.common.test.TestUtil.assertList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import ceri.common.function.ExceptionBiConsumer;
import ceri.common.function.ExceptionConsumer;
import ceri.common.function.ExceptionIntConsumer;
import ceri.common.util.PrimitiveUtil;

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

	public <E extends Exception> ExceptionConsumer<E, T> toEx(Class<E> cls) {
		return toEx();
	}

	public <E extends Exception> ExceptionConsumer<E, T> toEx() {
		return this::accept;
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

	public static class Int extends Capturer<Integer> implements IntConsumer {
		@Override
		public void accept(int value) {
			accept(Integer.valueOf(value));
		}

		public <E extends Exception> ExceptionIntConsumer<E> toExInt(Class<E> cls) {
			return toExInt();
		}

		public <E extends Exception> ExceptionIntConsumer<E> toExInt() {
			return this::accept;
		}

		public Capturer.Int reset() {
			values.clear();
			return this;
		}

		public final void verifyInt(int... values) {
			verify(PrimitiveUtil.asList(values));
		}
	}

	public static class Bi<T, U> implements BiConsumer<T, U> {
		public final Capturer<T> first;
		public final Capturer<U> second;

		Bi(Capturer<T> first, Capturer<U> second) {
			this.first = first;
			this.second = second;
		}

		public <E extends Exception> ExceptionBiConsumer<E, T, U> toEx(Class<E> cls) {
			return toEx();
		}

		public <E extends Exception> ExceptionBiConsumer<E, T, U> toEx() {
			return this::accept;
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
