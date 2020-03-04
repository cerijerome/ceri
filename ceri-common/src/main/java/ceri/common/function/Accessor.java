package ceri.common.function;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Interface to get and set values within an object. See also IntAccessor.
 */
public interface Accessor<T> {

	void set(T value);

	T get();

	static <T> Accessor<T> of(Supplier<T> getFn) {
		return of(getFn, null);
	}

	static <T> Accessor<T> of(Consumer<T> setFn) {
		return of(null, setFn);
	}

	/**
	 * Create accessor from get and set functions. Either may be null.
	 */
	static <T> Accessor<T> of(Supplier<T> getFn, Consumer<T> setFn) {
		return new Accessor<>() {
			@Override
			public void set(T value) {
				if (setFn == null) throw new UnsupportedOperationException();
				setFn.accept(value);
			}

			@Override
			public T get() {
				if (getFn == null) throw new UnsupportedOperationException();
				return getFn.get();
			}
		};
	}

	static <S, T> Accessor.Typed<S, T> typed(Function<S, T> getFn, BiConsumer<S, T> setFn) {
		return new Typed<>(getFn, setFn);
	}

	class Typed<S, T> {
		public final Function<S, T> getFn;
		public final BiConsumer<S, T> setFn;

		Typed(Function<S, T> getFn, BiConsumer<S, T> setFn) {
			this.getFn = getFn;
			this.setFn = setFn;
		}

		public Accessor<T> from(S s) {
			return Accessor.of(() -> get(s), t -> set(s, t));
		}

		public T get(S s) {
			if (getFn == null) throw new UnsupportedOperationException();
			return getFn.apply(s);
		}

		public void set(S s, T t) {
			if (setFn == null) throw new UnsupportedOperationException();
			setFn.accept(s, t);
		}
	}

}
