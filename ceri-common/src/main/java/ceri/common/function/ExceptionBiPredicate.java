package ceri.common.function;

import static ceri.common.util.ExceptionAdapter.RUNTIME;
import java.util.Objects;
import java.util.function.BiPredicate;

public interface ExceptionBiPredicate<E extends Exception, T, U> {

	boolean test(T t, U u) throws E;

	default ExceptionBiPredicate<E, T, U> and(ExceptionBiPredicate<E, ? super T, ? super U> other) {
		Objects.requireNonNull(other);
		return (T t, U u) -> test(t, u) && other.test(t, u);
	}

	default ExceptionBiPredicate<E, T, U> negate() {
		return (T t, U u) -> !test(t, u);
	}

	default ExceptionBiPredicate<E, T, U> or(ExceptionBiPredicate<E, ? super T, ? super U> other) {
		Objects.requireNonNull(other);
		return (T t, U u) -> test(t, u) || other.test(t, u);
	}

	default ExceptionBiPredicate<E, T, U> name(String name) {
		return name(this, name);
	}

	default BiPredicate<T, U> asBiPredicate() {
		return (t, u) -> RUNTIME.get(() -> test(t, u));
	}

	static <T, U> ExceptionBiPredicate<RuntimeException, T, U> of(BiPredicate<T, U> predicate) {
		return predicate::test;
	}

	static <E extends Exception, T, U> ExceptionBiPredicate<E, T, U>
		name(ExceptionBiPredicate<E, T, U> predicate, String name) {
		return new ExceptionBiPredicate<>() {
			@Override
			public boolean test(T t, U u) throws E {
				return predicate.test(t, u);
			}

			@Override
			public String toString() {
				return name;
			}
		};
	}
}
