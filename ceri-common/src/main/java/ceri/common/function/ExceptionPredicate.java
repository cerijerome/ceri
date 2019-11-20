package ceri.common.function;

import static ceri.common.util.ExceptionAdapter.RUNTIME;
import java.util.Objects;
import java.util.function.Predicate;

/**
 * Function that can throw exceptions.
 */
public interface ExceptionPredicate<E extends Exception, T> {

	boolean test(T t) throws E;

	default ExceptionPredicate<E, T> and(ExceptionPredicate<? extends E, ? super T> other) {
		Objects.requireNonNull(other);
		return (t) -> test(t) && other.test(t);
	}

	default ExceptionPredicate<E, T> negate() {
		return t -> !test(t);
	}

	default ExceptionPredicate<E, T> or(ExceptionPredicate<? extends E, ? super T> other) {
		Objects.requireNonNull(other);
		return t -> test(t) || other.test(t);
	}

	default ExceptionPredicate<E, T> name(String name) {
		return name(this, name);
	}

	default Predicate<T> asPredicate() {
		return (t) -> RUNTIME.get(() -> test(t));
	}

	static <T> ExceptionPredicate<RuntimeException, T> of(Predicate<T> predicate) {
		return predicate::test;
	}

	static <E extends Exception, T> ExceptionPredicate<E, T>
		name(ExceptionPredicate<E, T> predicate, String name) {
		return new ExceptionPredicate<>() {
			@Override
			public boolean test(T t) throws E {
				return predicate.test(t);
			}

			@Override
			public String toString() {
				return name;
			}
		};
	}
}
