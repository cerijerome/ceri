package ceri.common.function;

import static ceri.common.exception.ExceptionAdapter.RUNTIME;
import java.util.Objects;
import java.util.function.IntPredicate;

/**
 * Function that can throw exceptions.
 */
public interface ExceptionIntPredicate<E extends Exception> {

	boolean test(int value) throws E;

	default ExceptionIntPredicate<E> and(ExceptionIntPredicate<? extends E> other) {
		Objects.requireNonNull(other);
		return (t) -> test(t) && other.test(t);
	}

	default ExceptionIntPredicate<E> negate() {
		return t -> !test(t);
	}

	default ExceptionIntPredicate<E> or(ExceptionIntPredicate<? extends E> other) {
		Objects.requireNonNull(other);
		return t -> test(t) || other.test(t);
	}

	default ExceptionIntPredicate<E> name(String name) {
		return name(this, name);
	}

	default IntPredicate asPredicate() {
		return i -> RUNTIME.getBoolean(() -> test(i));
	}

	static ExceptionIntPredicate<RuntimeException> of(IntPredicate predicate) {
		Objects.requireNonNull(predicate);
		return predicate::test;
	}

	static <E extends Exception> ExceptionIntPredicate<E> name(ExceptionIntPredicate<E> predicate,
		String name) {
		Objects.requireNonNull(predicate);
		return new ExceptionIntPredicate<>() {
			@Override
			public boolean test(int i) throws E {
				return predicate.test(i);
			}

			@Override
			public String toString() {
				return name;
			}
		};
	}
}
