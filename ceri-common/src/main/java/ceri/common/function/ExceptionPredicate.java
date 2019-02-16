package ceri.common.function;

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

	default Predicate<T> asPredicate() {
		return t -> {
			try {
				return test(t);
			} catch (RuntimeException e) {
				throw e;
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		};
	}

	static <T> ExceptionPredicate<RuntimeException, T> of(Predicate<T> predicate) {
		return t -> predicate.test(t);
	}
}
