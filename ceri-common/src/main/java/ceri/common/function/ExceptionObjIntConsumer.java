package ceri.common.function;

import static ceri.common.exception.ExceptionAdapter.RUNTIME;
import java.util.Objects;
import java.util.function.ObjIntConsumer;

/**
 * Function that can throw exceptions.
 */
public interface ExceptionObjIntConsumer<E extends Exception, T> {
	void accept(T t, int value) throws E;

	default ExceptionObjIntConsumer<E, T>
		andThen(ExceptionObjIntConsumer<? extends E, ? super T> after) {
		Objects.requireNonNull(after);
		return (t, u) -> {
			accept(t, u);
			after.accept(t, u);
		};
	}

	default ObjIntConsumer<T> asObjIntConsumer() {
		return (t, i) -> RUNTIME.run(() -> accept(t, i));
	}

	static <T> ExceptionObjIntConsumer<RuntimeException, T> of(ObjIntConsumer<T> fn) {
		Objects.requireNonNull(fn);
		return fn::accept;
	}
}
