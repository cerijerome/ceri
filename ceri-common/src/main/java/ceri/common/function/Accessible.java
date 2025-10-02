package ceri.common.function;

import ceri.common.function.Excepts.Consumer;
import ceri.common.function.Excepts.Function;
import ceri.common.function.Excepts.IntConsumer;
import ceri.common.function.Excepts.IntFunction;
import ceri.common.function.Excepts.LongConsumer;
import ceri.common.function.Excepts.LongFunction;

/**
 * Interface that accepts various functions, passing an instance to the function.
 */
public interface Accessible<T> {

	/**
	 * Applies the function.
	 */
	<E extends Exception, R> R apply(Function<E, ? super T, R> function) throws E;

	/**
	 * Accepts the consumer.
	 */
	default <E extends Exception> void accept(Consumer<E, ? super T> consumer) throws E {
		apply(t -> {
			consumer.accept(t);
			return null;
		});
	}

	/**
	 * Create an implementation with a fixed instance.
	 */
	static <T> Accessible<T> of(T t) {
		// why is f -> f.apply(t) not working?
		return new Accessible<>() {
			@Override
			public <E extends Exception, R> R apply(Function<E, ? super T, R> function) throws E {
				return function.apply(t);
			}
		};
	}

	/**
	 * Interface that accepts various functions, passing an int to the function.
	 */
	interface OfInt {
		/**
		 * Applies the function.
		 */
		<E extends Exception, R> R apply(IntFunction<E, R> function) throws E;

		/**
		 * Accepts the consumer.
		 */
		default <E extends Exception> void accept(IntConsumer<E> consumer) throws E {
			apply(t -> {
				consumer.accept(t);
				return null;
			});
		}

		/**
		 * Create an implementation with a fixed int.
		 */
		static OfInt of(int i) {
			// why is f -> f.apply(t) not working?
			return new OfInt() {
				@Override
				public <E extends Exception, T> T apply(IntFunction<E, T> function) throws E {
					return function.apply(i);
				}
			};
		}
	}

	/**
	 * Interface that accepts various functions, passing a fixed long to the functions.
	 */
	interface OfLong {
		/**
		 * Applies the function.
		 */
		<E extends Exception, R> R apply(LongFunction<E, R> function) throws E;

		/**
		 * Accepts the consumer.
		 */
		default <E extends Exception> void accept(LongConsumer<E> consumer) throws E {
			apply(t -> {
				consumer.accept(t);
				return null;
			});
		}

		/**
		 * Create an implementation with a fixed long.
		 */
		static OfLong of(int i) {
			// why is f -> f.apply(t) not working?
			return new OfLong() {
				@Override
				public <E extends Exception, T> T apply(LongFunction<E, T> function) throws E {
					return function.apply(i);
				}
			};
		}
	}
}
