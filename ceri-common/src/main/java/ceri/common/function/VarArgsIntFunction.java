package ceri.common.function;

/**
 * Function that accepts int varargs and returns a result.
 */
public interface VarArgsIntFunction<R> {
	R apply(int... values);
}
