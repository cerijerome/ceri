package ceri.common.function;

import ceri.common.reflect.Reflect;

/**
 * Interfaces that throw specific exceptions.
 */
public class Excepts {
	private Excepts() {}

	/**
	 * Functional interface that throws specific exceptions.
	 */
	@FunctionalInterface
	public interface Function<E extends Exception, T, R> extends Throws.Function<T, R> {
		@Override
		R apply(T t) throws E;
	}

	/**
	 * Functional interface that throws specific exceptions.
	 */
	@FunctionalInterface
	public interface BoolFunction<E extends Exception, T> extends Throws.BoolFunction<T> {
		@Override
		T apply(boolean b) throws E;
	}

	/**
	 * Functional interface that throws specific exceptions.
	 */
	@FunctionalInterface
	public interface ByteFunction<E extends Exception, T> extends Throws.ByteFunction<T> {
		@Override
		T apply(byte b) throws E;
	}

	/**
	 * Functional interface that throws specific exceptions.
	 */
	@FunctionalInterface
	public interface IntFunction<E extends Exception, T> extends Throws.IntFunction<T> {
		@Override
		T apply(int i) throws E;
	}

	/**
	 * Functional interface that throws specific exceptions.
	 */
	@FunctionalInterface
	public interface LongFunction<E extends Exception, T> extends Throws.LongFunction<T> {
		@Override
		T apply(long l) throws E;
	}

	/**
	 * Functional interface that throws specific exceptions.
	 */
	@FunctionalInterface
	public interface DoubleFunction<E extends Exception, T> extends Throws.DoubleFunction<T> {
		@Override
		T apply(double d) throws E;
	}

	/**
	 * Functional interface that throws specific exceptions.
	 */
	@FunctionalInterface
	public interface ToBoolFunction<E extends Exception, T> extends Throws.ToBoolFunction<T> {
		@Override
		boolean applyAsBool(T t) throws E;
	}

	/**
	 * Functional interface that throws specific exceptions.
	 */
	@FunctionalInterface
	public interface ToByteFunction<E extends Exception, T> extends Throws.ToByteFunction<T> {
		@Override
		byte applyAsByte(T t) throws E;
	}

	/**
	 * Functional interface that throws specific exceptions.
	 */
	@FunctionalInterface
	public interface ToIntFunction<E extends Exception, T> extends Throws.ToIntFunction<T> {
		@Override
		int applyAsInt(T t) throws E;
	}

	/**
	 * Functional interface that throws specific exceptions.
	 */
	@FunctionalInterface
	public interface ToLongFunction<E extends Exception, T> extends Throws.ToLongFunction<T> {
		@Override
		long applyAsLong(T t) throws E;
	}

	/**
	 * Functional interface that throws specific exceptions.
	 */
	@FunctionalInterface
	public interface ToDoubleFunction<E extends Exception, T> extends Throws.ToDoubleFunction<T> {
		@Override
		double applyAsDouble(T t) throws E;
	}

	/**
	 * Functional interface that throws specific exceptions.
	 */
	@FunctionalInterface
	public interface BoolToByteFunction<E extends Exception> extends Throws.BoolToByteFunction {
		@Override
		byte applyAsByte(boolean b) throws E;
	}

	/**
	 * Functional interface that throws specific exceptions.
	 */
	@FunctionalInterface
	public interface BoolToIntFunction<E extends Exception> extends Throws.BoolToIntFunction {
		@Override
		int applyAsInt(boolean b) throws E;
	}

	/**
	 * Functional interface that throws specific exceptions.
	 */
	@FunctionalInterface
	public interface BoolToLongFunction<E extends Exception> extends Throws.BoolToLongFunction {
		@Override
		long applyAsLong(boolean b) throws E;
	}

	/**
	 * Functional interface that throws specific exceptions.
	 */
	@FunctionalInterface
	public interface BoolToDoubleFunction<E extends Exception> extends Throws.BoolToDoubleFunction {
		@Override
		double applyAsDouble(boolean b) throws E;
	}

	/**
	 * Functional interface that throws specific exceptions.
	 */
	@FunctionalInterface
	public interface ByteToBoolFunction<E extends Exception> extends Throws.ByteToBoolFunction {
		@Override
		boolean applyAsBool(byte b) throws E;
	}

	/**
	 * Functional interface that throws specific exceptions.
	 */
	@FunctionalInterface
	public interface ByteToIntFunction<E extends Exception> extends Throws.ByteToIntFunction {
		@Override
		int applyAsInt(byte b) throws E;
	}

	/**
	 * Functional interface that throws specific exceptions.
	 */
	@FunctionalInterface
	public interface ByteToLongFunction<E extends Exception> extends Throws.ByteToLongFunction {
		@Override
		long applyAsLong(byte b) throws E;
	}

	/**
	 * Functional interface that throws specific exceptions.
	 */
	@FunctionalInterface
	public interface ByteToDoubleFunction<E extends Exception> extends Throws.ByteToDoubleFunction {
		@Override
		double applyAsDouble(byte b) throws E;
	}

	/**
	 * Functional interface that throws specific exceptions.
	 */
	@FunctionalInterface
	public interface IntToBoolFunction<E extends Exception> extends Throws.IntToBoolFunction {
		@Override
		boolean applyAsBool(int i) throws E;
	}

	/**
	 * Functional interface that throws specific exceptions.
	 */
	@FunctionalInterface
	public interface IntToByteFunction<E extends Exception> extends Throws.IntToByteFunction {
		@Override
		byte applyAsByte(int i) throws E;
	}

	/**
	 * Functional interface that throws specific exceptions.
	 */
	@FunctionalInterface
	public interface IntToLongFunction<E extends Exception> extends Throws.IntToLongFunction {
		@Override
		long applyAsLong(int i) throws E;
	}

	/**
	 * Functional interface that throws specific exceptions.
	 */
	@FunctionalInterface
	public interface IntToDoubleFunction<E extends Exception> extends Throws.IntToDoubleFunction {
		@Override
		double applyAsDouble(int i) throws E;
	}

	/**
	 * Functional interface that throws specific exceptions.
	 */
	@FunctionalInterface
	public interface LongToBoolFunction<E extends Exception> extends Throws.LongToBoolFunction {
		@Override
		boolean applyAsBool(long l) throws E;
	}

	/**
	 * Functional interface that throws specific exceptions.
	 */
	@FunctionalInterface
	public interface LongToByteFunction<E extends Exception> extends Throws.LongToByteFunction {
		@Override
		byte applyAsByte(long l) throws E;
	}

	/**
	 * Functional interface that throws specific exceptions.
	 */
	@FunctionalInterface
	public interface LongToIntFunction<E extends Exception> extends Throws.LongToIntFunction {
		@Override
		int applyAsInt(long l) throws E;
	}

	/**
	 * Functional interface that throws specific exceptions.
	 */
	@FunctionalInterface
	public interface LongToDoubleFunction<E extends Exception> extends Throws.LongToDoubleFunction {
		@Override
		double applyAsDouble(long l) throws E;
	}

	/**
	 * Functional interface that throws specific exceptions.
	 */
	@FunctionalInterface
	public interface DoubleToBoolFunction<E extends Exception> extends Throws.DoubleToBoolFunction {
		@Override
		boolean applyAsBool(double d) throws E;
	}

	/**
	 * Functional interface that throws specific exceptions.
	 */
	@FunctionalInterface
	public interface DoubleToByteFunction<E extends Exception> extends Throws.DoubleToByteFunction {
		@Override
		byte applyAsByte(double d) throws E;
	}

	/**
	 * Functional interface that throws specific exceptions.
	 */
	@FunctionalInterface
	public interface DoubleToIntFunction<E extends Exception> extends Throws.DoubleToIntFunction {
		@Override
		int applyAsInt(double d) throws E;
	}

	/**
	 * Functional interface that throws specific exceptions.
	 */
	@FunctionalInterface
	public interface DoubleToLongFunction<E extends Exception> extends Throws.DoubleToLongFunction {
		@Override
		long applyAsLong(double d) throws E;
	}

	// Bi-functions

	/**
	 * Functional interface that throws specific exceptions.
	 */
	@FunctionalInterface
	public interface BiFunction<E extends Exception, T, U, R> extends Throws.BiFunction<T, U, R> {
		@Override
		R apply(T t, U u) throws E;
	}

	/**
	 * Functional interface that throws specific exceptions.
	 */
	@FunctionalInterface
	public interface BinFunction<E extends Exception, T, R>
		extends BiFunction<E, T, T, R>, Throws.BinFunction<T, R> {}

	/**
	 * Functional interface that throws specific exceptions.
	 */
	@FunctionalInterface
	public interface ObjBoolFunction<E extends Exception, T, R>
		extends Throws.ObjBoolFunction<T, R> {
		@Override
		R apply(T t, boolean b) throws E;
	}

	/**
	 * Functional interface that throws specific exceptions.
	 */
	@FunctionalInterface
	public interface ObjByteFunction<E extends Exception, T, R>
		extends Throws.ObjByteFunction<T, R> {
		@Override
		R apply(T t, byte b) throws E;
	}

	/**
	 * Functional interface that throws specific exceptions.
	 */
	@FunctionalInterface
	public interface ObjIntFunction<E extends Exception, T, R> extends Throws.ObjIntFunction<T, R> {
		@Override
		R apply(T t, int i) throws E;
	}

	/**
	 * Functional interface that throws specific exceptions.
	 */
	@FunctionalInterface
	public interface ObjLongFunction<E extends Exception, T, R>
		extends Throws.ObjLongFunction<T, R> {
		@Override
		R apply(T t, long l) throws E;
	}

	/**
	 * Functional interface that throws specific exceptions.
	 */
	@FunctionalInterface
	public interface ObjDoubleFunction<E extends Exception, T, R>
		extends Throws.ObjDoubleFunction<T, R> {
		@Override
		R apply(T t, double d) throws E;
	}

	/**
	 * Functional interface that throws specific exceptions.
	 */
	@FunctionalInterface
	public interface BoolBiFunction<E extends Exception, T> extends Throws.BoolBiFunction<T> {
		@Override
		T apply(boolean l, boolean r) throws E;
	}

	/**
	 * Functional interface that throws specific exceptions.
	 */
	@FunctionalInterface
	public interface ByteBiFunction<E extends Exception, T> extends Throws.ByteBiFunction<T> {
		@Override
		T apply(byte l, byte r) throws E;
	}

	/**
	 * Functional interface that throws specific exceptions.
	 */
	@FunctionalInterface
	public interface IntBiFunction<E extends Exception, T> extends Throws.IntBiFunction<T> {
		@Override
		T apply(int l, int r) throws E;
	}

	/**
	 * Functional interface that throws specific exceptions.
	 */
	@FunctionalInterface
	public interface LongBiFunction<E extends Exception, T> extends Throws.LongBiFunction<T> {
		@Override
		T apply(long l, long r) throws E;
	}

	/**
	 * Functional interface that throws specific exceptions.
	 */
	@FunctionalInterface
	public interface DoubleBiFunction<E extends Exception, T> extends Throws.DoubleBiFunction<T> {
		@Override
		T apply(double l, double r) throws E;
	}

	/**
	 * Functional interface that throws specific exceptions.
	 */
	@FunctionalInterface
	public interface ToBoolBiFunction<E extends Exception, T, U>
		extends Throws.ToBoolBiFunction<T, U> {
		@Override
		boolean applyAsBool(T t, U u) throws E;
	}

	/**
	 * Functional interface that throws specific exceptions.
	 */
	@FunctionalInterface
	public interface ToByteBiFunction<E extends Exception, T, U>
		extends Throws.ToByteBiFunction<T, U> {
		@Override
		byte applyAsByte(T t, U u) throws E;
	}

	/**
	 * Functional interface that throws specific exceptions.
	 */
	@FunctionalInterface
	public interface ToIntBiFunction<E extends Exception, T, U>
		extends Throws.ToIntBiFunction<T, U> {
		@Override
		int applyAsInt(T t, U u) throws E;
	}

	/**
	 * Functional interface that throws specific exceptions.
	 */
	@FunctionalInterface
	public interface ToLongBiFunction<E extends Exception, T, U>
		extends Throws.ToLongBiFunction<T, U> {
		@Override
		long applyAsLong(T t, U u) throws E;
	}

	/**
	 * Functional interface that throws specific exceptions.
	 */
	@FunctionalInterface
	public interface ToDoubleBiFunction<E extends Exception, T, U>
		extends Throws.ToDoubleBiFunction<T, U> {
		@Override
		double applyAsDouble(T t, U u) throws E;
	}

	// Operators

	/**
	 * Functional interface that throws specific exceptions.
	 */
	public interface Operator<E extends Exception, T>
		extends Function<E, T, T>, Throws.Operator<T> {}

	/**
	 * Functional interface that throws specific exceptions.
	 */
	@FunctionalInterface
	public interface BoolOperator<E extends Exception> extends Throws.BoolOperator {
		@Override
		boolean applyAsBool(boolean b) throws E;
	}

	/**
	 * Functional interface that throws specific exceptions.
	 */
	@FunctionalInterface
	public interface ByteOperator<E extends Exception> extends Throws.ByteOperator {
		@Override
		byte applyAsByte(byte b) throws E;
	}

	/**
	 * Functional interface that throws specific exceptions.
	 */
	@FunctionalInterface
	public interface IntOperator<E extends Exception> extends Throws.IntOperator {
		@Override
		int applyAsInt(int i) throws E;
	}

	/**
	 * Functional interface that throws specific exceptions.
	 */
	@FunctionalInterface
	public interface LongOperator<E extends Exception> extends Throws.LongOperator {
		@Override
		long applyAsLong(long l) throws E;
	}

	/**
	 * Functional interface that throws specific exceptions.
	 */
	@FunctionalInterface
	public interface DoubleOperator<E extends Exception> extends Throws.DoubleOperator {
		@Override
		double applyAsDouble(double d) throws E;
	}

	// Binary operators

	/**
	 * Functional interface that throws specific exceptions.
	 */
	public interface BiOperator<E extends Exception, T>
		extends BiFunction<E, T, T, T>, Throws.BiOperator<T> {}

	/**
	 * Functional interface that throws specific exceptions.
	 */
	@FunctionalInterface
	public interface BoolBiOperator<E extends Exception> extends Throws.BoolBiOperator {
		@Override
		boolean applyAsBool(boolean l, boolean r) throws E;
	}

	/**
	 * Functional interface that throws specific exceptions.
	 */
	@FunctionalInterface
	public interface ByteBiOperator<E extends Exception> extends Throws.ByteBiOperator {
		@Override
		byte applyAsByte(byte l, byte r) throws E;
	}

	/**
	 * Functional interface that throws specific exceptions.
	 */
	@FunctionalInterface
	public interface IntBiOperator<E extends Exception> extends Throws.IntBiOperator {
		@Override
		int applyAsInt(int l, int r) throws E;
	}

	/**
	 * Functional interface that throws specific exceptions.
	 */
	@FunctionalInterface
	public interface LongBiOperator<E extends Exception> extends Throws.LongBiOperator {
		@Override
		long applyAsLong(long l, long r) throws E;
	}

	/**
	 * Functional interface that throws specific exceptions.
	 */
	@FunctionalInterface
	public interface DoubleBiOperator<E extends Exception> extends Throws.DoubleBiOperator {
		@Override
		double applyAsDouble(double l, double r) throws E;
	}

	// Predicates

	public static void main(String[] args) {
		Excepts.Predicate<RuntimeException, Object> ep = x -> x == null;
		Functions.Predicate<Object> fp = Reflect.unchecked(ep);
		System.out.println(ep.test(null));
		System.out.println(fp.test(null));
	}
	
	/**
	 * Functional interface that throws specific exceptions.
	 */
	@FunctionalInterface
	public interface Predicate<E extends Exception, T> extends Throws.Predicate<T> {
		@Override
		boolean test(T t) throws E;
		
		default Predicate<E, T> not() {
			return t -> !test(t);
		}
		
		default Predicate<E, T> and(Predicate<? extends E, ? super T> predicate) {
			return predicate == null ? this : t -> test(t) && predicate.test(t);
		}
		
		default Predicate<E, T> or(Predicate<? extends E, ? super T> predicate) {
			return predicate == null ? this : t -> test(t) || predicate.test(t);
		}
	}

	/**
	 * Functional interface that throws specific exceptions.
	 */
	@FunctionalInterface
	public interface BoolPredicate<E extends Exception> extends Throws.BoolPredicate {
		@Override
		boolean test(boolean b) throws E;
	}

	/**
	 * Functional interface that throws specific exceptions.
	 */
	@FunctionalInterface
	public interface BytePredicate<E extends Exception> extends Throws.BytePredicate {
		@Override
		boolean test(byte b) throws E;
	}

	/**
	 * Functional interface that throws specific exceptions.
	 */
	@FunctionalInterface
	public interface IntPredicate<E extends Exception> extends Throws.IntPredicate {
		@Override
		boolean test(int i) throws E;
	}

	/**
	 * Functional interface that throws specific exceptions.
	 */
	@FunctionalInterface
	public interface LongPredicate<E extends Exception> extends Throws.LongPredicate {
		@Override
		boolean test(long l) throws E;
	}

	/**
	 * Functional interface that throws specific exceptions.
	 */
	@FunctionalInterface
	public interface DoublePredicate<E extends Exception> extends Throws.DoublePredicate {
		@Override
		boolean test(double d) throws E;
	}

	// Bi-predicates

	/**
	 * Functional interface that throws specific exceptions.
	 */
	@FunctionalInterface
	public interface BiPredicate<E extends Exception, T, U> extends Throws.BiPredicate<T, U> {
		@Override
		boolean test(T t, U u) throws E;
	}

	/**
	 * Functional interface that throws specific exceptions.
	 */
	@FunctionalInterface
	public interface BinPredicate<E extends Exception, T>
		extends BiPredicate<E, T, T>, Throws.BinPredicate<T> {}

	/**
	 * Functional interface that throws specific exceptions.
	 */
	@FunctionalInterface
	public interface ObjBoolPredicate<E extends Exception, T> extends Throws.ObjBoolPredicate<T> {
		@Override
		boolean test(T t, boolean b) throws E;
	}

	/**
	 * Functional interface that throws specific exceptions.
	 */
	@FunctionalInterface
	public interface ObjBytePredicate<E extends Exception, T> extends Throws.ObjBytePredicate<T> {
		@Override
		boolean test(T t, byte b) throws E;
	}

	/**
	 * Functional interface that throws specific exceptions.
	 */
	@FunctionalInterface
	public interface ObjIntPredicate<E extends Exception, T> extends Throws.ObjIntPredicate<T> {
		@Override
		boolean test(T t, int i) throws E;
	}

	/**
	 * Functional interface that throws specific exceptions.
	 */
	@FunctionalInterface
	public interface ObjLongPredicate<E extends Exception, T> extends Throws.ObjLongPredicate<T> {
		@Override
		boolean test(T t, long l) throws E;
	}

	/**
	 * Functional interface that throws specific exceptions.
	 */
	@FunctionalInterface
	public interface ObjDoublePredicate<E extends Exception, T>
		extends Throws.ObjDoublePredicate<T> {
		@Override
		boolean test(T t, double d) throws E;
	}

	/**
	 * Functional interface that throws specific exceptions.
	 */
	@FunctionalInterface
	public interface BoolBiPredicate<E extends Exception> extends Throws.BoolBiPredicate {
		@Override
		boolean test(boolean l, boolean r) throws E;
	}

	/**
	 * Functional interface that throws specific exceptions.
	 */
	@FunctionalInterface
	public interface ByteBiPredicate<E extends Exception> extends Throws.ByteBiPredicate {
		@Override
		boolean test(byte l, byte r) throws E;
	}

	/**
	 * Functional interface that throws specific exceptions.
	 */
	@FunctionalInterface
	public interface IntBiPredicate<E extends Exception> extends Throws.IntBiPredicate {
		@Override
		boolean test(int l, int r) throws E;
	}

	/**
	 * Functional interface that throws specific exceptions.
	 */
	@FunctionalInterface
	public interface LongBiPredicate<E extends Exception> extends Throws.LongBiPredicate {
		@Override
		boolean test(long l, long r) throws E;
	}

	/**
	 * Functional interface that throws specific exceptions.
	 */
	@FunctionalInterface
	public interface DoubleBiPredicate<E extends Exception> extends Throws.DoubleBiPredicate {
		@Override
		boolean test(double l, double r) throws E;
	}

	// Consumers

	/**
	 * Functional interface that throws specific exceptions.
	 */
	@FunctionalInterface
	public interface Consumer<E extends Exception, T> extends Throws.Consumer<T> {
		@Override
		void accept(T t) throws E;
	}

	/**
	 * Functional interface that throws specific exceptions.
	 */
	@FunctionalInterface
	public interface BoolConsumer<E extends Exception> extends Throws.BoolConsumer {
		@Override
		void accept(boolean b) throws E;
	}

	/**
	 * Functional interface that throws specific exceptions.
	 */
	@FunctionalInterface
	public interface ByteConsumer<E extends Exception> extends Throws.ByteConsumer {
		@Override
		void accept(byte b) throws E;
	}

	/**
	 * Functional interface that throws specific exceptions.
	 */
	@FunctionalInterface
	public interface IntConsumer<E extends Exception> extends Throws.IntConsumer {
		@Override
		void accept(int i) throws E;
	}

	/**
	 * Functional interface that throws specific exceptions.
	 */
	@FunctionalInterface
	public interface LongConsumer<E extends Exception> extends Throws.LongConsumer {
		@Override
		void accept(long l) throws E;
	}

	/**
	 * Functional interface that throws specific exceptions.
	 */
	@FunctionalInterface
	public interface DoubleConsumer<E extends Exception> extends Throws.DoubleConsumer {
		@Override
		void accept(double d) throws E;
	}

	// Bi-consumers

	/**
	 * Functional interface that throws specific exceptions.
	 */
	@FunctionalInterface
	public interface BiConsumer<E extends Exception, T, U> extends Throws.BiConsumer<T, U> {
		@Override
		void accept(T t, U u) throws E;
	}

	/**
	 * Functional interface that throws specific exceptions.
	 */
	@FunctionalInterface
	public interface BinConsumer<E extends Exception, T>
		extends BiConsumer<E, T, T>, Throws.BinConsumer<T> {}

	/**
	 * Functional interface that throws specific exceptions.
	 */
	@FunctionalInterface
	public interface ObjBoolConsumer<E extends Exception, T> extends Throws.ObjBoolConsumer<T> {
		@Override
		void accept(T t, boolean b) throws E;
	}

	/**
	 * Functional interface that throws specific exceptions.
	 */
	@FunctionalInterface
	public interface ObjByteConsumer<E extends Exception, T> extends Throws.ObjByteConsumer<T> {
		@Override
		void accept(T t, byte b) throws E;
	}

	/**
	 * Functional interface that throws specific exceptions.
	 */
	@FunctionalInterface
	public interface ObjIntConsumer<E extends Exception, T> extends Throws.ObjIntConsumer<T> {
		@Override
		void accept(T t, int i) throws E;
	}

	/**
	 * Functional interface that throws specific exceptions.
	 */
	@FunctionalInterface
	public interface ObjLongConsumer<E extends Exception, T> extends Throws.ObjLongConsumer<T> {
		@Override
		void accept(T t, long l) throws E;
	}

	/**
	 * Functional interface that throws specific exceptions.
	 */
	@FunctionalInterface
	public interface ObjDoubleConsumer<E extends Exception, T> extends Throws.ObjDoubleConsumer<T> {
		@Override
		void accept(T t, double d) throws E;
	}

	/**
	 * Functional interface that throws specific exceptions.
	 */
	@FunctionalInterface
	public interface BoolBiConsumer<E extends Exception> extends Throws.BoolBiConsumer {
		@Override
		void accept(boolean l, boolean r) throws E;
	}

	/**
	 * Functional interface that throws specific exceptions.
	 */
	@FunctionalInterface
	public interface ByteBiConsumer<E extends Exception> extends Throws.ByteBiConsumer {
		@Override
		void accept(byte l, byte r) throws E;
	}

	/**
	 * Functional interface that throws specific exceptions.
	 */
	@FunctionalInterface
	public interface IntBiConsumer<E extends Exception> extends Throws.IntBiConsumer {
		@Override
		void accept(int l, int r) throws E;
	}

	/**
	 * Functional interface that throws specific exceptions.
	 */
	@FunctionalInterface
	public interface LongBiConsumer<E extends Exception> extends Throws.LongBiConsumer {
		@Override
		void accept(long l, long r) throws E;
	}

	/**
	 * Functional interface that throws specific exceptions.
	 */
	@FunctionalInterface
	public interface DoubleBiConsumer<E extends Exception> extends Throws.DoubleBiConsumer {
		@Override
		void accept(double l, double r) throws E;
	}

	// Tri-consumers

	/**
	 * Functional interface that throws specific exceptions.
	 */
	@FunctionalInterface
	public interface BiObjIntConsumer<E extends Exception, T, U>
		extends Throws.BiObjIntConsumer<T, U> {
		@Override
		void accept(T t, U u, int i) throws E;
	}

	/**
	 * Functional interface that throws specific exceptions.
	 */
	@FunctionalInterface
	public interface ObjBiIntConsumer<E extends Exception, T> extends Throws.ObjBiIntConsumer<T> {
		@Override
		void accept(T t, int i0, int i1) throws E;
	}

	// Suppliers

	/**
	 * Functional interface that throws specific exceptions.
	 */
	@FunctionalInterface
	public interface Supplier<E extends Exception, T> extends Throws.Supplier<T> {
		@Override
		T get() throws E;
	}

	/**
	 * Functional interface that throws specific exceptions.
	 */
	@FunctionalInterface
	public interface BoolSupplier<E extends Exception> extends Throws.BoolSupplier {
		@Override
		boolean getAsBool() throws E;
	}

	/**
	 * Functional interface that throws specific exceptions.
	 */
	@FunctionalInterface
	public interface ByteSupplier<E extends Exception> extends Throws.ByteSupplier {
		@Override
		byte getAsByte() throws E;
	}

	/**
	 * Functional interface that throws specific exceptions.
	 */
	@FunctionalInterface
	public interface IntSupplier<E extends Exception> extends Throws.IntSupplier {
		@Override
		int getAsInt() throws E;
	}

	/**
	 * Functional interface that throws specific exceptions.
	 */
	@FunctionalInterface
	public interface LongSupplier<E extends Exception> extends Throws.LongSupplier {
		@Override
		long getAsLong() throws E;
	}

	/**
	 * Functional interface that throws specific exceptions.
	 */
	@FunctionalInterface
	public interface DoubleSupplier<E extends Exception> extends Throws.DoubleSupplier {
		@Override
		double getAsDouble() throws E;
	}

	// Runnables

	/**
	 * Functional interface that throws specific exceptions.
	 */
	@FunctionalInterface
	public interface Runnable<E extends Exception> extends Throws.Runnable {
		@Override
		void run() throws E;
	}

	// Other

	/**
	 * Functional interface that throws specific exceptions.
	 */
	@FunctionalInterface
	public interface Closeable<E extends Exception> extends AutoCloseable {
		@Override
		void close() throws E;
	}
}
