package ceri.common.function;

import java.util.Objects;
import ceri.common.function.Excepts.Consumer;
import ceri.common.function.Excepts.Function;
import ceri.common.function.Excepts.ToIntFunction;
import ceri.common.reflect.Reflect;

/**
 * Provides fluent chaining methods. T must be declared as the correct 'this' sub-class.
 */
public interface Fluent<T> {

	static <E extends Exception, T> T apply(T t, Consumer<E, ? super T> consumer) throws E {
		if (t != null && consumer != null) consumer.accept(t);
		return t;
	}

	default <E extends Exception> T apply(Consumer<E, ? super T> consumer) throws E {
		return apply(Reflect.unchecked(this), consumer);
	}

	default <E extends Exception, U> U map(Function<E, ? super T, U> fn) throws E {
		Objects.requireNonNull(fn);
		return fn.apply(Reflect.unchecked(this));
	}

	default <E extends Exception> int mapToInt(ToIntFunction<E, ? super T> fn) throws E {
		Objects.requireNonNull(fn);
		return fn.applyAsInt(Reflect.unchecked(this));
	}
}
