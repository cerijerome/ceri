package ceri.common.function;

/**
 * Interfaces with throwable methods.
 */
public class Throws {
	private Throws() {}

	/**
	 * Functional interface that allows throwable.
	 */
	@FunctionalInterface
	public interface Function<T, R> {
		/**
		 * Accepts the type and returns the result, with possible throwable.
		 */
		R apply(T t) throws Throwable;
	}

	/**
	 * Functional interface that allows throwable.
	 */
	@FunctionalInterface
	public interface BoolFunction<T> {
		/**
		 * Accepts the type and returns the result, with possible throwable.
		 */
		T apply(boolean b) throws Throwable;
	}

	/**
	 * Functional interface that allows throwable.
	 */
	@FunctionalInterface
	public interface ByteFunction<T> {
		/**
		 * Accepts the type and returns the result, with possible throwable.
		 */
		T apply(byte b) throws Throwable;
	}

	/**
	 * Functional interface that allows throwable.
	 */
	@FunctionalInterface
	public interface IntFunction<T> {
		/**
		 * Accepts the type and returns the result, with possible throwable.
		 */
		T apply(int i) throws Throwable;
	}

	/**
	 * Functional interface that allows throwable.
	 */
	@FunctionalInterface
	public interface LongFunction<T> {
		/**
		 * Accepts the type and returns the result, with possible throwable.
		 */
		T apply(long l) throws Throwable;
	}

	/**
	 * Functional interface that allows throwable.
	 */
	@FunctionalInterface
	public interface DoubleFunction<T> {
		/**
		 * Accepts the type and returns the result, with possible throwable.
		 */
		T apply(double d) throws Throwable;
	}

	/**
	 * Functional interface that allows throwable.
	 */
	@FunctionalInterface
	public interface ToBoolFunction<T> {
		/**
		 * Accepts the type and returns the result, with possible throwable.
		 */
		boolean applyAsBool(T t) throws Throwable;
	}

	/**
	 * Functional interface that allows throwable.
	 */
	@FunctionalInterface
	public interface ToByteFunction<T> {
		/**
		 * Accepts the type and returns the result, with possible throwable.
		 */
		byte applyAsByte(T t) throws Throwable;
	}

	/**
	 * Functional interface that allows throwable.
	 */
	@FunctionalInterface
	public interface ToIntFunction<T> {
		/**
		 * Accepts the type and returns the result, with possible throwable.
		 */
		int applyAsInt(T t) throws Throwable;
	}

	/**
	 * Functional interface that allows throwable.
	 */
	@FunctionalInterface
	public interface ToLongFunction<T> {
		/**
		 * Accepts the type and returns the result, with possible throwable.
		 */
		long applyAsLong(T t) throws Throwable;
	}

	/**
	 * Functional interface that allows throwable.
	 */
	@FunctionalInterface
	public interface ToDoubleFunction<T> {
		/**
		 * Accepts the type and returns the result, with possible throwable.
		 */
		double applyAsDouble(T t) throws Throwable;
	}

	/**
	 * Functional interface that allows throwable.
	 */
	@FunctionalInterface
	public interface BoolToByteFunction {
		/**
		 * Accepts the type and returns the result, with possible throwable.
		 */
		byte applyAsByte(boolean b) throws Throwable;
	}

	/**
	 * Functional interface that allows throwable.
	 */
	@FunctionalInterface
	public interface BoolToIntFunction {
		/**
		 * Accepts the type and returns the result, with possible throwable.
		 */
		int applyAsInt(boolean b) throws Throwable;
	}

	/**
	 * Functional interface that allows throwable.
	 */
	@FunctionalInterface
	public interface BoolToLongFunction {
		/**
		 * Accepts the type and returns the result, with possible throwable.
		 */
		long applyAsLong(boolean b) throws Throwable;
	}

	/**
	 * Functional interface that allows throwable.
	 */
	@FunctionalInterface
	public interface BoolToDoubleFunction {
		/**
		 * Accepts the type and returns the result, with possible throwable.
		 */
		double applyAsDouble(boolean b) throws Throwable;
	}

	/**
	 * Functional interface that allows throwable.
	 */
	@FunctionalInterface
	public interface ByteToBoolFunction {
		/**
		 * Accepts the type and returns the result, with possible throwable.
		 */
		boolean applyAsBool(byte b) throws Throwable;
	}

	/**
	 * Functional interface that allows throwable.
	 */
	@FunctionalInterface
	public interface ByteToIntFunction {
		/**
		 * Accepts the type and returns the result, with possible throwable.
		 */
		int applyAsInt(byte b) throws Throwable;
	}

	/**
	 * Functional interface that allows throwable.
	 */
	@FunctionalInterface
	public interface ByteToLongFunction {
		/**
		 * Accepts the type and returns the result, with possible throwable.
		 */
		long applyAsLong(byte b) throws Throwable;
	}

	/**
	 * Functional interface that allows throwable.
	 */
	@FunctionalInterface
	public interface ByteToDoubleFunction {
		/**
		 * Accepts the type and returns the result, with possible throwable.
		 */
		double applyAsDouble(byte b) throws Throwable;
	}

	/**
	 * Functional interface that allows throwable.
	 */
	@FunctionalInterface
	public interface IntToBoolFunction {
		/**
		 * Accepts the type and returns the result, with possible throwable.
		 */
		boolean applyAsBool(int i) throws Throwable;
	}

	/**
	 * Functional interface that allows throwable.
	 */
	@FunctionalInterface
	public interface IntToByteFunction {
		/**
		 * Accepts the type and returns the result, with possible throwable.
		 */
		byte applyAsByte(int i) throws Throwable;
	}

	/**
	 * Functional interface that allows throwable.
	 */
	@FunctionalInterface
	public interface IntToLongFunction {
		/**
		 * Accepts the type and returns the result, with possible throwable.
		 */
		long applyAsLong(int i) throws Throwable;
	}

	/**
	 * Functional interface that allows throwable.
	 */
	@FunctionalInterface
	public interface IntToDoubleFunction {
		/**
		 * Accepts the type and returns the result, with possible throwable.
		 */
		double applyAsDouble(int i) throws Throwable;
	}

	/**
	 * Functional interface that allows throwable.
	 */
	@FunctionalInterface
	public interface LongToBoolFunction {
		/**
		 * Accepts the type and returns the result, with possible throwable.
		 */
		boolean applyAsBool(long l) throws Throwable;
	}

	/**
	 * Functional interface that allows throwable.
	 */
	@FunctionalInterface
	public interface LongToByteFunction {
		/**
		 * Accepts the type and returns the result, with possible throwable.
		 */
		byte applyAsByte(long l) throws Throwable;
	}

	/**
	 * Functional interface that allows throwable.
	 */
	@FunctionalInterface
	public interface LongToIntFunction {
		/**
		 * Accepts the type and returns the result, with possible throwable.
		 */
		int applyAsInt(long l) throws Throwable;
	}

	/**
	 * Functional interface that allows throwable.
	 */
	@FunctionalInterface
	public interface LongToDoubleFunction {
		/**
		 * Accepts the type and returns the result, with possible throwable.
		 */
		double applyAsDouble(long l) throws Throwable;
	}

	/**
	 * Functional interface that allows throwable.
	 */
	@FunctionalInterface
	public interface DoubleToBoolFunction {
		/**
		 * Accepts the type and returns the result, with possible throwable.
		 */
		boolean applyAsBool(double d) throws Throwable;
	}

	/**
	 * Functional interface that allows throwable.
	 */
	@FunctionalInterface
	public interface DoubleToByteFunction {
		/**
		 * Accepts the type and returns the result, with possible throwable.
		 */
		byte applyAsByte(double d) throws Throwable;
	}

	/**
	 * Functional interface that allows throwable.
	 */
	@FunctionalInterface
	public interface DoubleToIntFunction {
		/**
		 * Accepts the type and returns the result, with possible throwable.
		 */
		int applyAsInt(double d) throws Throwable;
	}

	/**
	 * Functional interface that allows throwable.
	 */
	@FunctionalInterface
	public interface DoubleToLongFunction {
		/**
		 * Accepts the type and returns the result, with possible throwable.
		 */
		long applyAsLong(double d) throws Throwable;
	}

	// Bi-functions

	/**
	 * Functional interface that allows throwable.
	 */
	@FunctionalInterface
	public interface BiFunction<T, U, R> {
		/**
		 * Accepts the types and returns the result, with possible throwable.
		 */
		R apply(T t, U u) throws Throwable;
	}

	/**
	 * Functional interface that allows throwable.
	 */
	@FunctionalInterface
	public interface BinFunction<T, R> extends BiFunction<T, T, R> {}

	/**
	 * Functional interface that allows throwable.
	 */
	@FunctionalInterface
	public interface ObjBoolFunction<T, R> {
		/**
		 * Accepts the types and returns the result, with possible throwable.
		 */
		R apply(T t, boolean b) throws Throwable;
	}

	/**
	 * Functional interface that allows throwable.
	 */
	@FunctionalInterface
	public interface ObjByteFunction<T, R> {
		/**
		 * Accepts the types and returns the result, with possible throwable.
		 */
		R apply(T t, byte b) throws Throwable;
	}

	/**
	 * Functional interface that allows throwable.
	 */
	@FunctionalInterface
	public interface ObjIntFunction<T, R> {
		/**
		 * Accepts the types and returns the result, with possible throwable.
		 */
		R apply(T t, int i) throws Throwable;
	}

	/**
	 * Functional interface that allows throwable.
	 */
	@FunctionalInterface
	public interface ObjLongFunction<T, R> {
		/**
		 * Accepts the types and returns the result, with possible throwable.
		 */
		R apply(T t, long l) throws Throwable;
	}

	/**
	 * Functional interface that allows throwable.
	 */
	@FunctionalInterface
	public interface ObjDoubleFunction<T, R> {
		/**
		 * Accepts the types and returns the result, with possible throwable.
		 */
		R apply(T t, double d) throws Throwable;
	}

	/**
	 * Functional interface that allows throwable.
	 */
	@FunctionalInterface
	public interface BoolBiFunction<T> {
		/**
		 * Accepts the types and returns the result, with possible throwable.
		 */
		T apply(boolean l, boolean r) throws Throwable;
	}

	/**
	 * Functional interface that allows throwable.
	 */
	@FunctionalInterface
	public interface ByteBiFunction<T> {
		/**
		 * Accepts the types and returns the result, with possible throwable.
		 */
		T apply(byte l, byte r) throws Throwable;
	}

	/**
	 * Functional interface that allows throwable.
	 */
	@FunctionalInterface
	public interface IntBiFunction<T> {
		/**
		 * Accepts the types and returns the result, with possible throwable.
		 */
		T apply(int l, int r) throws Throwable;
	}

	/**
	 * Functional interface that allows throwable.
	 */
	@FunctionalInterface
	public interface LongBiFunction<T> {
		/**
		 * Accepts the types and returns the result, with possible throwable.
		 */
		T apply(long l, long r) throws Throwable;
	}

	/**
	 * Functional interface that allows throwable.
	 */
	@FunctionalInterface
	public interface DoubleBiFunction<T> {
		/**
		 * Accepts the types and returns the result, with possible throwable.
		 */
		T apply(double l, double r) throws Throwable;
	}

	/**
	 * Functional interface that allows throwable.
	 */
	@FunctionalInterface
	public interface ToBoolBiFunction<T, U> {
		/**
		 * Accepts the type and returns the result, with possible throwable.
		 */
		boolean applyAsBool(T t, U u) throws Throwable;
	}

	/**
	 * Functional interface that allows throwable.
	 */
	@FunctionalInterface
	public interface ToByteBiFunction<T, U> {
		/**
		 * Accepts the type and returns the result, with possible throwable.
		 */
		byte applyAsByte(T t, U u) throws Throwable;
	}

	/**
	 * Functional interface that allows throwable.
	 */
	@FunctionalInterface
	public interface ToIntBiFunction<T, U> {
		/**
		 * Accepts the type and returns the result, with possible throwable.
		 */
		int applyAsInt(T t, U u) throws Throwable;
	}

	/**
	 * Functional interface that allows throwable.
	 */
	@FunctionalInterface
	public interface ToLongBiFunction<T, U> {
		/**
		 * Accepts the type and returns the result, with possible throwable.
		 */
		long applyAsLong(T t, U u) throws Throwable;
	}

	/**
	 * Functional interface that allows throwable.
	 */
	@FunctionalInterface
	public interface ToDoubleBiFunction<T, U> {
		/**
		 * Accepts the type and returns the result, with possible throwable.
		 */
		double applyAsDouble(T t, U u) throws Throwable;
	}

	// Operators

	/**
	 * Functional interface that allows throwable.
	 */
	public interface Operator<T> extends Function<T, T> {}

	/**
	 * Functional interface that allows throwable.
	 */
	@FunctionalInterface
	public interface BoolOperator {
		/**
		 * Accepts the type and returns the result, with possible throwable.
		 */
		boolean applyAsBool(boolean b) throws Throwable;
	}

	/**
	 * Functional interface that allows throwable.
	 */
	@FunctionalInterface
	public interface ByteOperator {
		/**
		 * Accepts the type and returns the result, with possible throwable.
		 */
		byte applyAsByte(byte b) throws Throwable;
	}

	/**
	 * Functional interface that allows throwable.
	 */
	@FunctionalInterface
	public interface IntOperator {
		/**
		 * Accepts the type and returns the result, with possible throwable.
		 */
		int applyAsInt(int i) throws Throwable;
	}

	/**
	 * Functional interface that allows throwable.
	 */
	@FunctionalInterface
	public interface LongOperator {
		/**
		 * Accepts the type and returns the result, with possible throwable.
		 */
		long applyAsLong(long l) throws Throwable;
	}

	/**
	 * Functional interface that allows throwable.
	 */
	@FunctionalInterface
	public interface DoubleOperator {
		/**
		 * Accepts the type and returns the result, with possible throwable.
		 */
		double applyAsDouble(double d) throws Throwable;
	}

	// Binary operators

	/**
	 * Functional interface that allows throwable.
	 */
	public interface BiOperator<T> extends BiFunction<T, T, T> {}

	/**
	 * Functional interface that allows throwable.
	 */
	@FunctionalInterface
	public interface BoolBiOperator {
		/**
		 * Accepts the types and returns the result, with possible throwable.
		 */
		boolean applyAsBool(boolean l, boolean r) throws Throwable;
	}

	/**
	 * Functional interface that allows throwable.
	 */
	@FunctionalInterface
	public interface ByteBiOperator {
		/**
		 * Accepts the types and returns the result, with possible throwable.
		 */
		byte applyAsByte(byte l, byte r) throws Throwable;
	}

	/**
	 * Functional interface that allows throwable.
	 */
	@FunctionalInterface
	public interface IntBiOperator {
		/**
		 * Accepts the types and returns the result, with possible throwable.
		 */
		int applyAsInt(int l, int r) throws Throwable;
	}

	/**
	 * Functional interface that allows throwable.
	 */
	@FunctionalInterface
	public interface LongBiOperator {
		/**
		 * Accepts the types and returns the result, with possible throwable.
		 */
		long applyAsLong(long l, long r) throws Throwable;
	}

	/**
	 * Functional interface that allows throwable.
	 */
	@FunctionalInterface
	public interface DoubleBiOperator {
		/**
		 * Accepts the types and returns the result, with possible throwable.
		 */
		double applyAsDouble(double l, double r) throws Throwable;
	}

	// Object + operators
	
	/**
	 * Functional interface that throws specific exceptions.
	 */
	@FunctionalInterface
	public interface ObjBoolOperator<T> {
		/**
		 * Accepts the types and returns the result, with possible throwable.
		 */
		boolean applyAsBool(T t, boolean b) throws Throwable;
	}
	
	/**
	 * Functional interface that throws specific exceptions.
	 */
	@FunctionalInterface
	public interface ObjByteOperator<T> {
		/**
		 * Accepts the types and returns the result, with possible throwable.
		 */
		byte applyAsByte(T t, byte b) throws Throwable;
	}
	
	/**
	 * Functional interface that throws specific exceptions.
	 */
	@FunctionalInterface
	public interface ObjIntOperator<T> {
		/**
		 * Accepts the types and returns the result, with possible throwable.
		 */
		int applyAsInt(T t, int i) throws Throwable;
	}
	
	/**
	 * Functional interface that throws specific exceptions.
	 */
	@FunctionalInterface
	public interface ObjLongOperator<T> {
		/**
		 * Accepts the types and returns the result, with possible throwable.
		 */
		long applyAsLong(T t, long l) throws Throwable;
	}
	
	/**
	 * Functional interface that throws specific exceptions.
	 */
	@FunctionalInterface
	public interface ObjDoubleOperator<T> {
		/**
		 * Accepts the types and returns the result, with possible throwable.
		 */
		double applyAsDouble(T t, double d) throws Throwable;
	}
	
	// Predicates

	/**
	 * Functional interface that allows throwable.
	 */
	@FunctionalInterface
	public interface Predicate<T> {
		/**
		 * Tests the type, with possible throwable.
		 */
		boolean test(T t) throws Throwable;
	}

	/**
	 * Functional interface that allows throwable.
	 */
	@FunctionalInterface
	public interface BoolPredicate {
		/**
		 * Tests the type, with possible throwable.
		 */
		boolean test(boolean b) throws Throwable;
	}

	/**
	 * Functional interface that allows throwable.
	 */
	@FunctionalInterface
	public interface BytePredicate {
		/**
		 * Tests the type, with possible throwable.
		 */
		boolean test(byte b) throws Throwable;
	}

	/**
	 * Functional interface that allows throwable.
	 */
	@FunctionalInterface
	public interface IntPredicate {
		/**
		 * Tests the type, with possible throwable.
		 */
		boolean test(int i) throws Throwable;
	}

	/**
	 * Functional interface that allows throwable.
	 */
	@FunctionalInterface
	public interface LongPredicate {
		/**
		 * Tests the type, with possible throwable.
		 */
		boolean test(long l) throws Throwable;
	}

	/**
	 * Functional interface that allows throwable.
	 */
	@FunctionalInterface
	public interface DoublePredicate {
		/**
		 * Tests the type, with possible throwable.
		 */
		boolean test(double d) throws Throwable;
	}

	// Bi-predicates

	/**
	 * Functional interface that allows throwable.
	 */
	@FunctionalInterface
	public interface BiPredicate<T, U> {
		/**
		 * Tests the types and returns the result, with possible throwable.
		 */
		boolean test(T t, U u) throws Throwable;
	}

	/**
	 * Functional interface that allows throwable.
	 */
	@FunctionalInterface
	public interface BinPredicate<T> extends BiPredicate<T, T> {}

	/**
	 * Functional interface that allows throwable.
	 */
	@FunctionalInterface
	public interface ObjBoolPredicate<T> {
		/**
		 * Tests the types and returns the result, with possible throwable.
		 */
		boolean test(T t, boolean b) throws Throwable;
	}

	/**
	 * Functional interface that allows throwable.
	 */
	@FunctionalInterface
	public interface ObjBytePredicate<T> {
		/**
		 * Tests the types and returns the result, with possible throwable.
		 */
		boolean test(T t, byte b) throws Throwable;
	}

	/**
	 * Functional interface that allows throwable.
	 */
	@FunctionalInterface
	public interface ObjIntPredicate<T> {
		/**
		 * Tests the types and returns the result, with possible throwable.
		 */
		boolean test(T t, int i) throws Throwable;
	}

	/**
	 * Functional interface that allows throwable.
	 */
	@FunctionalInterface
	public interface ObjLongPredicate<T> {
		/**
		 * Tests the types and returns the result, with possible throwable.
		 */
		boolean test(T t, long l) throws Throwable;
	}

	/**
	 * Functional interface that allows throwable.
	 */
	@FunctionalInterface
	public interface ObjDoublePredicate<T> {
		/**
		 * Tests the types and returns the result, with possible throwable.
		 */
		boolean test(T t, double d) throws Throwable;
	}

	/**
	 * Functional interface that allows throwable.
	 */
	@FunctionalInterface
	public interface BoolBiPredicate {
		/**
		 * Tests the types and returns the result, with possible throwable.
		 */
		boolean test(boolean l, boolean r) throws Throwable;
	}

	/**
	 * Functional interface that allows throwable.
	 */
	@FunctionalInterface
	public interface ByteBiPredicate {
		/**
		 * Tests the types and returns the result, with possible throwable.
		 */
		boolean test(byte l, byte r) throws Throwable;
	}

	/**
	 * Functional interface that allows throwable.
	 */
	@FunctionalInterface
	public interface IntBiPredicate {
		/**
		 * Tests the types and returns the result, with possible throwable.
		 */
		boolean test(int l, int r) throws Throwable;
	}

	/**
	 * Functional interface that allows throwable.
	 */
	@FunctionalInterface
	public interface LongBiPredicate {
		/**
		 * Tests the types and returns the result, with possible throwable.
		 */
		boolean test(long l, long r) throws Throwable;
	}

	/**
	 * Functional interface that allows throwable.
	 */
	@FunctionalInterface
	public interface DoubleBiPredicate {
		/**
		 * Tests the types and returns the result, with possible throwable.
		 */
		boolean test(double l, double r) throws Throwable;
	}

	// Consumers

	/**
	 * Functional interface that allows throwable.
	 */
	@FunctionalInterface
	public interface Consumer<T> {
		/**
		 * Accepts the type, with possible throwable.
		 */
		void accept(T t) throws Throwable;
	}

	/**
	 * Functional interface that allows throwable.
	 */
	@FunctionalInterface
	public interface BoolConsumer {
		/**
		 * Accepts the type, with possible throwable.
		 */
		void accept(boolean b) throws Throwable;
	}

	/**
	 * Functional interface that allows throwable.
	 */
	@FunctionalInterface
	public interface ByteConsumer {
		/**
		 * Accepts the type, with possible throwable.
		 */
		void accept(byte b) throws Throwable;
	}

	/**
	 * Functional interface that allows throwable.
	 */
	@FunctionalInterface
	public interface IntConsumer {
		/**
		 * Accepts the type, with possible throwable.
		 */
		void accept(int i) throws Throwable;
	}

	/**
	 * Functional interface that allows throwable.
	 */
	@FunctionalInterface
	public interface LongConsumer {
		/**
		 * Accepts the type, with possible throwable.
		 */
		void accept(long l) throws Throwable;
	}

	/**
	 * Functional interface that allows throwable.
	 */
	@FunctionalInterface
	public interface DoubleConsumer {
		/**
		 * Accepts the type, with possible throwable.
		 */
		void accept(double d) throws Throwable;
	}

	// Bi-consumers

	/**
	 * Functional interface that allows throwable.
	 */
	@FunctionalInterface
	public interface BiConsumer<T, U> {
		/**
		 * Accepts the types, with possible throwable.
		 */
		void accept(T t, U u) throws Throwable;
	}

	/**
	 * Functional interface that allows throwable.
	 */
	@FunctionalInterface
	public interface BinConsumer<T> extends BiConsumer<T, T> {}

	/**
	 * Functional interface that allows throwable.
	 */
	@FunctionalInterface
	public interface ObjBoolConsumer<T> {
		/**
		 * Accepts the types, with possible throwable.
		 */
		void accept(T t, boolean b) throws Throwable;
	}

	/**
	 * Functional interface that allows throwable.
	 */
	@FunctionalInterface
	public interface ObjByteConsumer<T> {
		/**
		 * Accepts the types, with possible throwable.
		 */
		void accept(T t, byte b) throws Throwable;
	}

	/**
	 * Functional interface that allows throwable.
	 */
	@FunctionalInterface
	public interface ObjIntConsumer<T> {
		/**
		 * Accepts the types, with possible throwable.
		 */
		void accept(T t, int i) throws Throwable;
	}

	/**
	 * Functional interface that allows throwable.
	 */
	@FunctionalInterface
	public interface ObjLongConsumer<T> {
		/**
		 * Accepts the types, with possible throwable.
		 */
		void accept(T t, long l) throws Throwable;
	}

	/**
	 * Functional interface that allows throwable.
	 */
	@FunctionalInterface
	public interface ObjDoubleConsumer<T> {
		/**
		 * Accepts the types, with possible throwable.
		 */
		void accept(T t, double d) throws Throwable;
	}

	/**
	 * Functional interface that allows throwable.
	 */
	@FunctionalInterface
	public interface BoolBiConsumer {
		/**
		 * Accepts the types, with possible throwable.
		 */
		void accept(boolean l, boolean r) throws Throwable;
	}

	/**
	 * Functional interface that allows throwable.
	 */
	@FunctionalInterface
	public interface ByteBiConsumer {
		/**
		 * Accepts the types, with possible throwable.
		 */
		void accept(byte l, byte r) throws Throwable;
	}

	/**
	 * Functional interface that allows throwable.
	 */
	@FunctionalInterface
	public interface IntBiConsumer {
		/**
		 * Accepts the types, with possible throwable.
		 */
		void accept(int l, int r) throws Throwable;
	}

	/**
	 * Functional interface that allows throwable.
	 */
	@FunctionalInterface
	public interface LongBiConsumer {
		/**
		 * Accepts the types, with possible throwable.
		 */
		void accept(long l, long r) throws Throwable;
	}

	/**
	 * Functional interface that allows throwable.
	 */
	@FunctionalInterface
	public interface DoubleBiConsumer {
		/**
		 * Accepts the types, with possible throwable.
		 */
		void accept(double l, double r) throws Throwable;
	}

	// Tri-consumers

	/**
	 * Functional interface that allows throwable.
	 */
	@FunctionalInterface
	public interface BiObjIntConsumer<T, U> {
		/**
		 * Accepts the types, with possible throwable.
		 */
		void accept(T t, U u, int i) throws Throwable;
	}

	/**
	 * Functional interface that allows throwable.
	 */
	@FunctionalInterface
	public interface ObjBiIntConsumer<T> {
		/**
		 * Accepts the types, with possible throwable.
		 */
		void accept(T t, int i0, int i1) throws Throwable;
	}

	// Suppliers

	/**
	 * Functional interface that allows throwable.
	 */
	@FunctionalInterface
	public interface Supplier<T> {
		/**
		 * Supplies the type, with possible throwable.
		 */
		T get() throws Throwable;
	}

	/**
	 * Functional interface that allows throwable.
	 */
	@FunctionalInterface
	public interface BoolSupplier {
		/**
		 * Supplies the type, with possible throwable.
		 */
		boolean getAsBool() throws Throwable;
	}

	/**
	 * Functional interface that allows throwable.
	 */
	@FunctionalInterface
	public interface ByteSupplier {
		/**
		 * Supplies the type, with possible throwable.
		 */
		byte getAsByte() throws Throwable;
	}

	/**
	 * Functional interface that allows throwable.
	 */
	@FunctionalInterface
	public interface IntSupplier {
		/**
		 * Supplies the type, with possible throwable.
		 */
		int getAsInt() throws Throwable;
	}

	/**
	 * Functional interface that allows throwable.
	 */
	@FunctionalInterface
	public interface LongSupplier {
		/**
		 * Supplies the type, with possible throwable.
		 */
		long getAsLong() throws Throwable;
	}

	/**
	 * Functional interface that allows throwable.
	 */
	@FunctionalInterface
	public interface DoubleSupplier {
		/**
		 * Supplies the type, with possible throwable.
		 */
		double getAsDouble() throws Throwable;
	}

	// Runnables

	/**
	 * Functional interface that allows throwable.
	 */
	@FunctionalInterface
	public interface Runnable {
		/**
		 * Executes with possible throwable.
		 */
		void run() throws Throwable;
	}
}
