package ceri.common.function;

import static ceri.common.exception.ExceptionAdapter.RUNTIME;
import java.util.Objects;

/**
 * Function that can throw exceptions.
 */
public interface ExceptionObjLongConsumer<E extends Exception, T> {
	void accept(T t, long value) throws E;

	default ExceptionObjLongConsumer<E, T>
		andThen(ExceptionObjLongConsumer<? extends E, ? super T> after) {
		Objects.requireNonNull(after);
		return (t, l) -> {
			accept(t, l);
			after.accept(t, l);
		};
	}

	default ObjLongConsumer<T> asObjLongConsumer() {
		return (t, l) -> RUNTIME.run(() -> accept(t, l));
	}

	static <T> ExceptionObjLongConsumer<RuntimeException, T> of(ObjLongConsumer<T> fn) {
		Objects.requireNonNull(fn);
		return fn::accept;
	}
}
