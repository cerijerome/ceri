package ceri.common.function;

import java.util.Objects;

public interface ObjIntPredicate<T> {

	boolean test(T t, int value);

	default ObjIntPredicate<T> and(ObjIntPredicate<? super T> other) {
		Objects.requireNonNull(other);
		return (t, i) -> test(t, i) && other.test(t, i);
	}

	default ObjIntPredicate<T> negate() {
		return (t, i) -> !test(t, i);
	}

	default ObjIntPredicate<T> or(ObjIntPredicate<? super T> other) {
		Objects.requireNonNull(other);
		return (t, i) -> test(t, i) || other.test(t, i);
	}
}
