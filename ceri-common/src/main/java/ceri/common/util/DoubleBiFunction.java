package ceri.common.util;

import java.util.Objects;
import java.util.function.Function;

public interface DoubleBiFunction<R> {

	R apply(double t, double u);

	default <V> DoubleBiFunction<V> andThen(Function<? super R, ? extends V> after) {
		Objects.requireNonNull(after);
		return (t, u) -> after.apply(apply(t, u));
	}
	
}
