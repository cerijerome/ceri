package ceri.common.function;

import static ceri.common.exception.ExceptionAdapter.RUNTIME;
import java.util.Objects;

/**
 * Function that can throw exceptions.
 */
public interface ExceptionObjIntPredicate<E extends Exception, T> {
	boolean test(T t, int value) throws E;

	default ExceptionObjIntPredicate<E, T>
		and(ExceptionObjIntPredicate<? extends E, ? super T> other) {
		Objects.requireNonNull(other);
		return (t, i) -> test(t, i) && other.test(t, i);
	}

	default ExceptionObjIntPredicate<E, T> negate() {
		return (t, i) -> !test(t, i);
	}

	default ExceptionObjIntPredicate<E, T>
		or(ExceptionObjIntPredicate<? extends E, ? super T> other) {
		Objects.requireNonNull(other);
		return (t, i) -> test(t, i) || other.test(t, i);
	}

	default ExceptionObjIntPredicate<E, T> name(String name) {
		return name(this, name);
	}

	default ObjIntPredicate<T> asPredicate() {
		return (t, i) -> RUNTIME.getBoolean(() -> test(t, i));
	}

	static <T> ExceptionObjIntPredicate<RuntimeException, T> of(ObjIntPredicate<T> predicate) {
		Objects.requireNonNull(predicate);
		return predicate::test;
	}

	static <E extends Exception, T> ExceptionObjIntPredicate<E, T>
		name(ExceptionObjIntPredicate<E, T> predicate, String name) {
		Objects.requireNonNull(predicate);
		return new ExceptionObjIntPredicate<>() {
			@Override
			public boolean test(T t, int i) throws E {
				return predicate.test(t, i);
			}

			@Override
			public String toString() {
				return name;
			}
		};
	}
}
