package ceri.common.function;

import static ceri.common.exception.ExceptionAdapter.RUNTIME;
import java.util.Objects;

/**
 * Function that can throw exceptions.
 */
public interface ExceptionObjLongPredicate<E extends Exception, T> {
	boolean test(T t, long value) throws E;

	default ExceptionObjLongPredicate<E, T>
		and(ExceptionObjLongPredicate<? extends E, ? super T> other) {
		Objects.requireNonNull(other);
		return (t, l) -> test(t, l) && other.test(t, l);
	}

	default ExceptionObjLongPredicate<E, T> negate() {
		return (t, l) -> !test(t, l);
	}

	default ExceptionObjLongPredicate<E, T>
		or(ExceptionObjLongPredicate<? extends E, ? super T> other) {
		Objects.requireNonNull(other);
		return (t, l) -> test(t, l) || other.test(t, l);
	}

	default ExceptionObjLongPredicate<E, T> name(String name) {
		return name(this, name);
	}

	default ObjLongPredicate<T> asPredicate() {
		return (t, l) -> RUNTIME.getBoolean(() -> test(t, l));
	}

	static <T> ExceptionObjLongPredicate<RuntimeException, T> of(ObjLongPredicate<T> predicate) {
		Objects.requireNonNull(predicate);
		return predicate::test;
	}

	static <E extends Exception, T> ExceptionObjLongPredicate<E, T>
		name(ExceptionObjLongPredicate<E, T> predicate, String name) {
		Objects.requireNonNull(predicate);
		return new ExceptionObjLongPredicate<>() {
			@Override
			public boolean test(T t, long l) throws E {
				return predicate.test(t, l);
			}

			@Override
			public String toString() {
				return name;
			}
		};
	}
}
