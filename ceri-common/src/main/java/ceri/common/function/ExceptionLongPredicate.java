package ceri.common.function;

import static ceri.common.util.ExceptionAdapter.RUNTIME;
import java.util.Objects;
import java.util.function.LongPredicate;

/**
 * Function that can throw exceptions.
 */
public interface ExceptionLongPredicate<E extends Exception> {

	boolean test(long value) throws E;

	default ExceptionLongPredicate<E> and(ExceptionLongPredicate<? extends E> other) {
		Objects.requireNonNull(other);
		return (t) -> test(t) && other.test(t);
	}

	default ExceptionLongPredicate<E> negate() {
		return t -> !test(t);
	}

	default ExceptionLongPredicate<E> or(ExceptionLongPredicate<? extends E> other) {
		Objects.requireNonNull(other);
		return t -> test(t) || other.test(t);
	}

	default ExceptionLongPredicate<E> name(String name) {
		return name(this, name);
	}

	default LongPredicate asPredicate() {
		return i -> RUNTIME.getBoolean(() -> test(i));
	}

	static ExceptionLongPredicate<RuntimeException> of(LongPredicate predicate) {
		Objects.requireNonNull(predicate);
		return predicate::test;
	}

	static <E extends Exception> ExceptionLongPredicate<E> name(ExceptionLongPredicate<E> predicate,
		String name) {
		Objects.requireNonNull(predicate);
		return new ExceptionLongPredicate<>() {
			@Override
			public boolean test(long i) throws E {
				return predicate.test(i);
			}

			@Override
			public String toString() {
				return name;
			}
		};
	}
}
