package ceri.common.function;

public interface IntBinaryPredicate {

	boolean test(int left, int right);

	default IntBinaryPredicate negate() {
		return (l, r) -> !test(l, r);
	}
}
