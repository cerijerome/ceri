package ceri.common.function;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;

/**
 * Functional interfaces currently missing from java.
 */
public class Funcs {
	private Funcs() {}

	/**
	 * Functional interface.
	 */
	@FunctionalInterface
	public interface BoolFunction<T> {
		/**
		 * Accepts the type and returns the result.
		 */
		T apply(boolean b);
	}

	/**
	 * Functional interface.
	 */
	@FunctionalInterface
	public interface ByteFunction<T> {
		/**
		 * Accepts the type and returns the result.
		 */
		T apply(byte b);
	}

	/**
	 * Functional interface.
	 */
	@FunctionalInterface
	public interface ToBoolFunction<T> {
		/**
		 * Accepts the type and returns the result.
		 */
		boolean applyAsBool(T t);
	}

	/**
	 * Functional interface.
	 */
	@FunctionalInterface
	public interface ToByteFunction<T> {
		/**
		 * Accepts the type and returns the result.
		 */
		byte applyAsByte(T t);
	}

	/**
	 * Functional interface.
	 */
	@FunctionalInterface
	public interface BoolToByteFunction {
		/**
		 * Accepts the type and returns the result.
		 */
		byte applyAsByte(boolean b);
	}

	/**
	 * Functional interface.
	 */
	@FunctionalInterface
	public interface BoolToIntFunction {
		/**
		 * Accepts the type and returns the result.
		 */
		int applyAsInt(boolean b);
	}

	/**
	 * Functional interface.
	 */
	@FunctionalInterface
	public interface BoolToLongFunction {
		/**
		 * Accepts the type and returns the result.
		 */
		long applyAsLong(boolean b);
	}

	/**
	 * Functional interface.
	 */
	@FunctionalInterface
	public interface BoolToDoubleFunction {
		/**
		 * Accepts the type and returns the result.
		 */
		double applyAsDouble(boolean b);
	}

	/**
	 * Functional interface.
	 */
	@FunctionalInterface
	public interface ByteToBoolFunction {
		/**
		 * Accepts the type and returns the result.
		 */
		boolean applyAsBool(byte b);
	}

	/**
	 * Functional interface.
	 */
	@FunctionalInterface
	public interface ByteToIntFunction {
		/**
		 * Accepts the type and returns the result.
		 */
		int applyAsInt(byte b);
	}

	/**
	 * Functional interface.
	 */
	@FunctionalInterface
	public interface ByteToLongFunction {
		/**
		 * Accepts the type and returns the result.
		 */
		long applyAsLong(byte b);
	}

	/**
	 * Functional interface.
	 */
	@FunctionalInterface
	public interface ByteToDoubleFunction {
		/**
		 * Accepts the type and returns the result.
		 */
		double applyAsDouble(byte b);
	}

	/**
	 * Functional interface.
	 */
	@FunctionalInterface
	public interface IntToBoolFunction {
		/**
		 * Accepts the type and returns the result.
		 */
		boolean applyAsBool(int i);
	}

	/**
	 * Functional interface.
	 */
	@FunctionalInterface
	public interface IntToByteFunction {
		/**
		 * Accepts the type and returns the result.
		 */
		byte applyAsByte(int i);
	}

	/**
	 * Functional interface.
	 */
	@FunctionalInterface
	public interface LongToBoolFunction {
		/**
		 * Accepts the type and returns the result.
		 */
		boolean applyAsBool(long l);
	}

	/**
	 * Functional interface.
	 */
	@FunctionalInterface
	public interface LongToByteFunction {
		/**
		 * Accepts the type and returns the result.
		 */
		byte applyAsByte(long l);
	}

	/**
	 * Functional interface.
	 */
	@FunctionalInterface
	public interface DoubleToBoolFunction {
		/**
		 * Accepts the type and returns the result.
		 */
		boolean applyAsBool(double d);
	}

	/**
	 * Functional interface.
	 */
	@FunctionalInterface
	public interface DoubleToByteFunction {
		/**
		 * Accepts the type and returns the result.
		 */
		byte applyAsByte(double d);
	}

	// Bi-functions

	/**
	 * Functional interface.
	 */
	@FunctionalInterface
	public interface BinFunction<T, R> extends BiFunction<T, T, R> {}

	/**
	 * Functional interface.
	 */
	@FunctionalInterface
	public interface ObjBoolFunction<T, R> {
		/**
		 * Accepts the types and returns the result.
		 */
		R apply(T t, boolean b);
	}

	/**
	 * Functional interface.
	 */
	@FunctionalInterface
	public interface ObjByteFunction<T, R> {
		/**
		 * Accepts the types and returns the result.
		 */
		R apply(T t, byte b);
	}

	/**
	 * Functional interface.
	 */
	@FunctionalInterface
	public interface ObjIntFunction<T, R> {
		/**
		 * Accepts the types and returns the result.
		 */
		R apply(T t, int i);
	}

	/**
	 * Functional interface.
	 */
	@FunctionalInterface
	public interface ObjLongFunction<T, R> {
		/**
		 * Accepts the types and returns the result.
		 */
		R apply(T t, long l);
	}

	/**
	 * Functional interface.
	 */
	@FunctionalInterface
	public interface ObjDoubleFunction<T, R> {
		/**
		 * Accepts the types and returns the result.
		 */
		R apply(T t, double d);
	}

	/**
	 * Functional interface.
	 */
	@FunctionalInterface
	public interface BoolBiFunction<T> {
		/**
		 * Accepts the types and returns the result.
		 */
		T apply(boolean l, boolean r);
	}

	/**
	 * Functional interface.
	 */
	@FunctionalInterface
	public interface ByteBiFunction<T> {
		/**
		 * Accepts the types and returns the result.
		 */
		T apply(byte l, byte r);
	}

	/**
	 * Functional interface.
	 */
	@FunctionalInterface
	public interface IntBiFunction<T> {
		/**
		 * Accepts the types and returns the result.
		 */
		T apply(int l, int r);
	}

	/**
	 * Functional interface.
	 */
	@FunctionalInterface
	public interface LongBiFunction<T> {
		/**
		 * Accepts the types and returns the result.
		 */
		T apply(long l, long r);
	}

	/**
	 * Functional interface.
	 */
	@FunctionalInterface
	public interface DoubleBiFunction<T> {
		/**
		 * Accepts the types and returns the result.
		 */
		T apply(double l, double r);
	}

	/**
	 * Functional interface.
	 */
	@FunctionalInterface
	public interface ToBoolBiFunction<T, U> {
		/**
		 * Accepts the type and returns the result.
		 */
		boolean applyAsBool(T t, U u);
	}

	/**
	 * Functional interface.
	 */
	@FunctionalInterface
	public interface ToByteBiFunction<T, U> {
		/**
		 * Accepts the type and returns the result.
		 */
		byte applyAsByte(T t, U u);
	}

	// Operators

	/**
	 * Functional interface.
	 */
	@FunctionalInterface
	public interface BoolOperator {
		/**
		 * Accepts the type and returns the result.
		 */
		boolean applyAsBool(boolean b);
	}

	/**
	 * Functional interface.
	 */
	@FunctionalInterface
	public interface ByteOperator {
		/**
		 * Accepts the type and returns the result.
		 */
		byte applyAsByte(byte b);
	}

	// Binary operators

	/**
	 * Functional interface.
	 */
	@FunctionalInterface
	public interface BoolBiOperator {
		/**
		 * Accepts the types and returns the result.
		 */
		boolean applyAsBool(boolean l, boolean r);
	}

	/**
	 * Functional interface.
	 */
	@FunctionalInterface
	public interface ByteBiOperator {
		/**
		 * Accepts the types and returns the result.
		 */
		byte applyAsByte(byte l, byte r);
	}

	// Predicates

	/**
	 * Functional interface.
	 */
	@FunctionalInterface
	public interface BoolPredicate {
		/**
		 * Tests the type.
		 */
		boolean test(boolean b);
	}

	/**
	 * Functional interface.
	 */
	@FunctionalInterface
	public interface BytePredicate {
		/**
		 * Tests the type.
		 */
		boolean test(byte b);
	}

	// Bi-predicates

	/**
	 * Functional interface.
	 */
	@FunctionalInterface
	public interface BinPredicate<T> extends BiPredicate<T, T> {}

	/**
	 * Functional interface.
	 */
	@FunctionalInterface
	public interface ObjBoolPredicate<T> {
		/**
		 * Tests the types and returns the result.
		 */
		boolean test(T t, boolean b);
	}

	/**
	 * Functional interface.
	 */
	@FunctionalInterface
	public interface ObjBytePredicate<T> {
		/**
		 * Tests the types and returns the result.
		 */
		boolean test(T t, byte b);
	}

	/**
	 * Functional interface.
	 */
	@FunctionalInterface
	public interface ObjIntPredicate<T> {
		/**
		 * Tests the types and returns the result.
		 */
		boolean test(T t, int i);
	}

	/**
	 * Functional interface.
	 */
	@FunctionalInterface
	public interface ObjLongPredicate<T> {
		/**
		 * Tests the types and returns the result.
		 */
		boolean test(T t, long l);
	}

	/**
	 * Functional interface.
	 */
	@FunctionalInterface
	public interface ObjDoublePredicate<T> {
		/**
		 * Tests the types and returns the result.
		 */
		boolean test(T t, double d);
	}

	/**
	 * Functional interface.
	 */
	@FunctionalInterface
	public interface BoolBiPredicate {
		/**
		 * Tests the types and returns the result.
		 */
		boolean test(boolean l, boolean r);
	}

	/**
	 * Functional interface.
	 */
	@FunctionalInterface
	public interface ByteBiPredicate {
		/**
		 * Tests the types and returns the result.
		 */
		boolean test(byte l, byte r);
	}

	/**
	 * Functional interface.
	 */
	@FunctionalInterface
	public interface IntBiPredicate {
		/**
		 * Tests the types and returns the result.
		 */
		boolean test(int l, int r);
	}

	/**
	 * Functional interface.
	 */
	@FunctionalInterface
	public interface LongBiPredicate {
		/**
		 * Tests the types and returns the result.
		 */
		boolean test(long l, long r);
	}

	/**
	 * Functional interface.
	 */
	@FunctionalInterface
	public interface DoubleBiPredicate {
		/**
		 * Tests the types and returns the result.
		 */
		boolean test(double l, double r);
	}

	// Consumers

	/**
	 * Functional interface.
	 */
	@FunctionalInterface
	public interface BoolConsumer {
		/**
		 * Accepts the type.
		 */
		void accept(boolean b);
	}

	/**
	 * Functional interface.
	 */
	@FunctionalInterface
	public interface ByteConsumer {
		/**
		 * Accepts the type.
		 */
		void accept(byte b);
	}

	// Bi-consumers

	/**
	 * Functional interface.
	 */
	@FunctionalInterface
	public interface BinConsumer<T> extends BiConsumer<T, T> {}
	
	/**
	 * Functional interface.
	 */
	@FunctionalInterface
	public interface ObjBoolConsumer<T> {
		/**
		 * Accepts the types.
		 */
		void accept(T t, boolean b);
	}

	/**
	 * Functional interface.
	 */
	@FunctionalInterface
	public interface ObjByteConsumer<T> {
		/**
		 * Accepts the types.
		 */
		void accept(T t, byte b);
	}

	/**
	 * Functional interface.
	 */
	@FunctionalInterface
	public interface BoolBiConsumer {
		/**
		 * Accepts the types.
		 */
		void accept(boolean l, boolean r);
	}

	/**
	 * Functional interface.
	 */
	@FunctionalInterface
	public interface ByteBiConsumer {
		/**
		 * Accepts the types.
		 */
		void accept(byte l, byte r);
	}

	/**
	 * Functional interface.
	 */
	@FunctionalInterface
	public interface IntBiConsumer {
		/**
		 * Accepts the types.
		 */
		void accept(int l, int r);
	}

	/**
	 * Functional interface.
	 */
	@FunctionalInterface
	public interface LongBiConsumer {
		/**
		 * Accepts the types.
		 */
		void accept(long l, long r);
	}

	/**
	 * Functional interface.
	 */
	@FunctionalInterface
	public interface DoubleBiConsumer {
		/**
		 * Accepts the types.
		 */
		void accept(double l, double r);
	}

	// Suppliers

	/**
	 * Functional interface.
	 */
	@FunctionalInterface
	public interface ByteSupplier {
		/**
		 * Supplies the type.
		 */
		byte getAsByte();
	}
}
