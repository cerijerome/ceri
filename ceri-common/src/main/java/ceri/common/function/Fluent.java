package ceri.common.function;

import java.util.Objects;
import ceri.common.util.BasicUtil;

/**
 * Provides fluent chaining methods. T must be declared as the correct 'this' sub-class.
 */
public interface Fluent<T> {

	static <E extends Exception, T> T apply(T t, ExceptionConsumer<E, ? super T> consumer)
		throws E {
		if (t != null && consumer != null) consumer.accept(t);
		return t;
	}

	default <E extends Exception> T apply(ExceptionConsumer<E, ? super T> consumer) throws E {
		return apply(BasicUtil.<T>uncheckedCast(this), consumer);
	}

	default <E extends Exception, U> U map(ExceptionFunction<E, ? super T, U> fn) throws E {
		Objects.requireNonNull(fn);
		T typedThis = BasicUtil.uncheckedCast(this);
		return fn.apply(typedThis);
	}

	default <E extends Exception> int mapToInt(ExceptionToIntFunction<E, ? super T> fn) throws E {
		Objects.requireNonNull(fn);
		T typedThis = BasicUtil.uncheckedCast(this);
		return fn.applyAsInt(typedThis);
	}

}
