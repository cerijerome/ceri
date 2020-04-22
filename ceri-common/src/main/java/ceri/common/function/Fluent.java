package ceri.common.function;

import ceri.common.util.BasicUtil;

/**
 * Provides fluent chaining methods. T must be declared as the correct 'this' sub-class.
 */
public interface Fluent<T> {

	default <E extends Exception> T apply(ExceptionConsumer<E, ? super T> consumer) throws E {
		T typedThis = BasicUtil.uncheckedCast(this);
		if (consumer != null) // or throw?
			consumer.accept(typedThis);
		return typedThis;
	}

	default <E extends Exception, U> U map(ExceptionFunction<E, ? super T, U> fn) throws E {
		if (fn == null) return null; // or throw?
		T typedThis = BasicUtil.uncheckedCast(this);
		return fn.apply(typedThis);
	}

	default <E extends Exception> int mapToInt(ExceptionToIntFunction<E, ? super T> fn) throws E {
		if (fn == null) return 0; // or throw?
		T typedThis = BasicUtil.uncheckedCast(this);
		return fn.applyAsInt(typedThis);
	}

}
