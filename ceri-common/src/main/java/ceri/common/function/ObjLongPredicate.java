package ceri.common.function;

import java.util.Objects;

public interface ObjLongPredicate<T> {

	boolean test(T t, long value);

	default ObjLongPredicate<T> and(ObjLongPredicate<? super T> other) {
		Objects.requireNonNull(other);
		return (t, l) -> test(t, l) && other.test(t, l);
	}

	default ObjLongPredicate<T> negate() {
		return (t, l) -> !test(t, l);
	}

	default ObjLongPredicate<T> or(ObjLongPredicate<? super T> other) {
		Objects.requireNonNull(other);
		return (t, l) -> test(t, l) || other.test(t, l);
	}
}
