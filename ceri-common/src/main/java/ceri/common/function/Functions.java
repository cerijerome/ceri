package ceri.common.function;

/**
 * Interfaces that do not throw checked exceptions.
 */
public class Functions {
	private Functions() {}

	/**
	 * Functional interface that only allows runtime exceptions.
	 */
	@FunctionalInterface
	public interface Function<T, R>
		extends Excepts.Function<RuntimeException, T, R>, java.util.function.Function<T, R> {}

	/**
	 * Functional interface that only allows runtime exceptions.
	 */
	@FunctionalInterface
	public interface BoolFunction<T> extends Excepts.BoolFunction<RuntimeException, T> {}

	/**
	 * Functional interface that only allows runtime exceptions.
	 */
	@FunctionalInterface
	public interface ByteFunction<T> extends Excepts.ByteFunction<RuntimeException, T> {}

	/**
	 * Functional interface that only allows runtime exceptions.
	 */
	@FunctionalInterface
	public interface IntFunction<T>
		extends Excepts.IntFunction<RuntimeException, T>, java.util.function.IntFunction<T> {}

	/**
	 * Functional interface that only allows runtime exceptions.
	 */
	@FunctionalInterface
	public interface LongFunction<T>
		extends Excepts.LongFunction<RuntimeException, T>, java.util.function.LongFunction<T> {}

	/**
	 * Functional interface that only allows runtime exceptions.
	 */
	@FunctionalInterface
	public interface DoubleFunction<T>
		extends Excepts.DoubleFunction<RuntimeException, T>, java.util.function.DoubleFunction<T> {}

	/**
	 * Functional interface that only allows runtime exceptions.
	 */
	@FunctionalInterface
	public interface ToBoolFunction<T> extends Excepts.ToBoolFunction<RuntimeException, T> {}

	/**
	 * Functional interface that only allows runtime exceptions.
	 */
	@FunctionalInterface
	public interface ToByteFunction<T> extends Excepts.ToByteFunction<RuntimeException, T> {}

	/**
	 * Functional interface that only allows runtime exceptions.
	 */
	@FunctionalInterface
	public interface ToIntFunction<T>
		extends Excepts.ToIntFunction<RuntimeException, T>, java.util.function.ToIntFunction<T> {}

	/**
	 * Functional interface that only allows runtime exceptions.
	 */
	@FunctionalInterface
	public interface ToLongFunction<T>
		extends Excepts.ToLongFunction<RuntimeException, T>, java.util.function.ToLongFunction<T> {}

	/**
	 * Functional interface that only allows runtime exceptions.
	 */
	@FunctionalInterface
	public interface ToDoubleFunction<T> extends Excepts.ToDoubleFunction<RuntimeException, T>,
		java.util.function.ToDoubleFunction<T> {}

	/**
	 * Functional interface that only allows runtime exceptions.
	 */
	@FunctionalInterface
	public interface BoolToByteFunction extends Excepts.BoolToByteFunction<RuntimeException> {}

	/**
	 * Functional interface that only allows runtime exceptions.
	 */
	@FunctionalInterface
	public interface BoolToIntFunction extends Excepts.BoolToIntFunction<RuntimeException> {}

	/**
	 * Functional interface that only allows runtime exceptions.
	 */
	@FunctionalInterface
	public interface BoolToLongFunction extends Excepts.BoolToLongFunction<RuntimeException> {}

	/**
	 * Functional interface that only allows runtime exceptions.
	 */
	@FunctionalInterface
	public interface BoolToDoubleFunction extends Excepts.BoolToDoubleFunction<RuntimeException> {}

	/**
	 * Functional interface that only allows runtime exceptions.
	 */
	@FunctionalInterface
	public interface ByteToBoolFunction extends Excepts.ByteToBoolFunction<RuntimeException> {}

	/**
	 * Functional interface that only allows runtime exceptions.
	 */
	@FunctionalInterface
	public interface ByteToIntFunction extends Excepts.ByteToIntFunction<RuntimeException> {}

	/**
	 * Functional interface that only allows runtime exceptions.
	 */
	@FunctionalInterface
	public interface ByteToLongFunction extends Excepts.ByteToLongFunction<RuntimeException> {}

	/**
	 * Functional interface that only allows runtime exceptions.
	 */
	@FunctionalInterface
	public interface ByteToDoubleFunction extends Excepts.ByteToDoubleFunction<RuntimeException> {}

	/**
	 * Functional interface that only allows runtime exceptions.
	 */
	@FunctionalInterface
	public interface IntToBoolFunction extends Excepts.IntToBoolFunction<RuntimeException> {}

	/**
	 * Functional interface that only allows runtime exceptions.
	 */
	@FunctionalInterface
	public interface IntToByteFunction extends Excepts.IntToByteFunction<RuntimeException> {}

	/**
	 * Functional interface that only allows runtime exceptions.
	 */
	@FunctionalInterface
	public interface IntToLongFunction
		extends Excepts.IntToLongFunction<RuntimeException>, java.util.function.IntToLongFunction {}

	/**
	 * Functional interface that only allows runtime exceptions.
	 */
	@FunctionalInterface
	public interface IntToDoubleFunction extends Excepts.IntToDoubleFunction<RuntimeException>,
		java.util.function.IntToDoubleFunction {}

	/**
	 * Functional interface that only allows runtime exceptions.
	 */
	@FunctionalInterface
	public interface LongToBoolFunction extends Excepts.LongToBoolFunction<RuntimeException> {}

	/**
	 * Functional interface that only allows runtime exceptions.
	 */
	@FunctionalInterface
	public interface LongToByteFunction extends Excepts.LongToByteFunction<RuntimeException> {}

	/**
	 * Functional interface that only allows runtime exceptions.
	 */
	@FunctionalInterface
	public interface LongToIntFunction
		extends Excepts.LongToIntFunction<RuntimeException>, java.util.function.LongToIntFunction {}

	/**
	 * Functional interface that only allows runtime exceptions.
	 */
	@FunctionalInterface
	public interface LongToDoubleFunction extends Excepts.LongToDoubleFunction<RuntimeException>,
		java.util.function.LongToDoubleFunction {}

	/**
	 * Functional interface that only allows runtime exceptions.
	 */
	@FunctionalInterface
	public interface DoubleToBoolFunction extends Excepts.DoubleToBoolFunction<RuntimeException> {}

	/**
	 * Functional interface that only allows runtime exceptions.
	 */
	@FunctionalInterface
	public interface DoubleToByteFunction extends Excepts.DoubleToByteFunction<RuntimeException> {}

	/**
	 * Functional interface that only allows runtime exceptions.
	 */
	@FunctionalInterface
	public interface DoubleToIntFunction extends Excepts.DoubleToIntFunction<RuntimeException>,
		java.util.function.DoubleToIntFunction {}

	/**
	 * Functional interface that only allows runtime exceptions.
	 */
	@FunctionalInterface
	public interface DoubleToLongFunction extends Excepts.DoubleToLongFunction<RuntimeException>,
		java.util.function.DoubleToLongFunction {}

	// Bi-functions

	/**
	 * Functional interface that only allows runtime exceptions.
	 */
	@FunctionalInterface
	public interface BiFunction<T, U, R> extends Excepts.BiFunction<RuntimeException, T, U, R>,
		java.util.function.BiFunction<T, U, R> {}

	/**
	 * Functional interface that only allows runtime exceptions.
	 */
	@FunctionalInterface
	public interface BinFunction<T, R>
		extends BiFunction<T, T, R>, Excepts.BinFunction<RuntimeException, T, R> {}

	/**
	 * Functional interface that only allows runtime exceptions.
	 */
	@FunctionalInterface
	public interface ObjBoolFunction<T, R>
		extends Excepts.ObjBoolFunction<RuntimeException, T, R> {}

	/**
	 * Functional interface that only allows runtime exceptions.
	 */
	@FunctionalInterface
	public interface ObjByteFunction<T, R>
		extends Excepts.ObjByteFunction<RuntimeException, T, R> {}

	/**
	 * Functional interface that only allows runtime exceptions.
	 */
	@FunctionalInterface
	public interface ObjIntFunction<T, R> extends Excepts.ObjIntFunction<RuntimeException, T, R> {}

	/**
	 * Functional interface that only allows runtime exceptions.
	 */
	@FunctionalInterface
	public interface ObjLongFunction<T, R>
		extends Excepts.ObjLongFunction<RuntimeException, T, R> {}

	/**
	 * Functional interface that only allows runtime exceptions.
	 */
	@FunctionalInterface
	public interface ObjDoubleFunction<T, R>
		extends Excepts.ObjDoubleFunction<RuntimeException, T, R> {}

	/**
	 * Functional interface that only allows runtime exceptions.
	 */
	@FunctionalInterface
	public interface BoolBiFunction<T> extends Excepts.BoolBiFunction<RuntimeException, T> {}

	/**
	 * Functional interface that only allows runtime exceptions.
	 */
	@FunctionalInterface
	public interface ByteBiFunction<T> extends Excepts.ByteBiFunction<RuntimeException, T> {}

	/**
	 * Functional interface that only allows runtime exceptions.
	 */
	@FunctionalInterface
	public interface IntBiFunction<T> extends Excepts.IntBiFunction<RuntimeException, T> {}

	/**
	 * Functional interface that only allows runtime exceptions.
	 */
	@FunctionalInterface
	public interface LongBiFunction<T> extends Excepts.LongBiFunction<RuntimeException, T> {}

	/**
	 * Functional interface that only allows runtime exceptions.
	 */
	@FunctionalInterface
	public interface DoubleBiFunction<T> extends Excepts.DoubleBiFunction<RuntimeException, T> {}

	/**
	 * Functional interface that only allows runtime exceptions.
	 */
	@FunctionalInterface
	public interface ToBoolBiFunction<T, U>
		extends Excepts.ToBoolBiFunction<RuntimeException, T, U> {}

	/**
	 * Functional interface that only allows runtime exceptions.
	 */
	@FunctionalInterface
	public interface ToByteBiFunction<T, U>
		extends Excepts.ToByteBiFunction<RuntimeException, T, U> {}

	/**
	 * Functional interface that only allows runtime exceptions.
	 */
	@FunctionalInterface
	public interface ToIntBiFunction<T, U> extends Excepts.ToIntBiFunction<RuntimeException, T, U>,
		java.util.function.ToIntBiFunction<T, U> {}

	/**
	 * Functional interface that only allows runtime exceptions.
	 */
	@FunctionalInterface
	public interface ToLongBiFunction<T, U>
		extends Excepts.ToLongBiFunction<RuntimeException, T, U>,
		java.util.function.ToLongBiFunction<T, U> {}

	/**
	 * Functional interface that only allows runtime exceptions.
	 */
	@FunctionalInterface
	public interface ToDoubleBiFunction<T, U>
		extends Excepts.ToDoubleBiFunction<RuntimeException, T, U>,
		java.util.function.ToDoubleBiFunction<T, U> {}

	// Operators

	/**
	 * Functional interface that only allows runtime exceptions.
	 */
	public interface Operator<T> extends Function<T, T>, Excepts.Operator<RuntimeException, T>,
		java.util.function.UnaryOperator<T> {}

	/**
	 * Functional interface that only allows runtime exceptions.
	 */
	@FunctionalInterface
	public interface BoolOperator extends Excepts.BoolOperator<RuntimeException> {}

	/**
	 * Functional interface that only allows runtime exceptions.
	 */
	@FunctionalInterface
	public interface ByteOperator extends Excepts.ByteOperator<RuntimeException> {}

	/**
	 * Functional interface that only allows runtime exceptions.
	 */
	@FunctionalInterface
	public interface IntOperator
		extends Excepts.IntOperator<RuntimeException>, java.util.function.IntUnaryOperator {}

	/**
	 * Functional interface that only allows runtime exceptions.
	 */
	@FunctionalInterface
	public interface LongOperator
		extends Excepts.LongOperator<RuntimeException>, java.util.function.LongUnaryOperator {}

	/**
	 * Functional interface that only allows runtime exceptions.
	 */
	@FunctionalInterface
	public interface DoubleOperator
		extends Excepts.DoubleOperator<RuntimeException>, java.util.function.DoubleUnaryOperator {}

	// Binary operators

	/**
	 * Functional interface that only allows runtime exceptions.
	 */
	public interface BiOperator<T> extends BiFunction<T, T, T>,
		Excepts.BiOperator<RuntimeException, T>, java.util.function.BinaryOperator<T> {}

	/**
	 * Functional interface that only allows runtime exceptions.
	 */
	@FunctionalInterface
	public interface BoolBiOperator extends Excepts.BoolBiOperator<RuntimeException> {}

	/**
	 * Functional interface that only allows runtime exceptions.
	 */
	@FunctionalInterface
	public interface ByteBiOperator extends Excepts.ByteBiOperator<RuntimeException> {}

	/**
	 * Functional interface that only allows runtime exceptions.
	 */
	@FunctionalInterface
	public interface IntBiOperator
		extends Excepts.IntBiOperator<RuntimeException>, java.util.function.IntBinaryOperator {}

	/**
	 * Functional interface that only allows runtime exceptions.
	 */
	@FunctionalInterface
	public interface LongBiOperator
		extends Excepts.LongBiOperator<RuntimeException>, java.util.function.LongBinaryOperator {}

	/**
	 * Functional interface that only allows runtime exceptions.
	 */
	@FunctionalInterface
	public interface DoubleBiOperator extends Excepts.DoubleBiOperator<RuntimeException>,
		java.util.function.DoubleBinaryOperator {}

	// Object + operators

	/**
	 * Functional interface that throws specific exceptions.
	 */
	@FunctionalInterface
	public interface ObjBoolOperator<T> extends Excepts.ObjBoolOperator<RuntimeException, T> {}

	/**
	 * Functional interface that throws specific exceptions.
	 */
	@FunctionalInterface
	public interface ObjByteOperator<T> extends Excepts.ObjByteOperator<RuntimeException, T> {}

	/**
	 * Functional interface that throws specific exceptions.
	 */
	@FunctionalInterface
	public interface ObjIntOperator<T> extends Excepts.ObjIntOperator<RuntimeException, T> {}

	/**
	 * Functional interface that throws specific exceptions.
	 */
	@FunctionalInterface
	public interface ObjLongOperator<T> extends Excepts.ObjLongOperator<RuntimeException, T> {}

	/**
	 * Functional interface that throws specific exceptions.
	 */
	@FunctionalInterface
	public interface ObjDoubleOperator<T> extends Excepts.ObjDoubleOperator<RuntimeException, T> {}

	// Predicates

	/**
	 * Functional interface that only allows runtime exceptions.
	 */
	@FunctionalInterface
	public interface Predicate<T>
		extends Excepts.Predicate<RuntimeException, T>, java.util.function.Predicate<T> {}

	/**
	 * Functional interface that only allows runtime exceptions.
	 */
	@FunctionalInterface
	public interface BoolPredicate extends Excepts.BoolPredicate<RuntimeException> {}

	/**
	 * Functional interface that only allows runtime exceptions.
	 */
	@FunctionalInterface
	public interface BytePredicate extends Excepts.BytePredicate<RuntimeException> {}

	/**
	 * Functional interface that only allows runtime exceptions.
	 */
	@FunctionalInterface
	public interface IntPredicate
		extends Excepts.IntPredicate<RuntimeException>, java.util.function.IntPredicate {}

	/**
	 * Functional interface that only allows runtime exceptions.
	 */
	@FunctionalInterface
	public interface LongPredicate
		extends Excepts.LongPredicate<RuntimeException>, java.util.function.LongPredicate {}

	/**
	 * Functional interface that only allows runtime exceptions.
	 */
	@FunctionalInterface
	public interface DoublePredicate
		extends Excepts.DoublePredicate<RuntimeException>, java.util.function.DoublePredicate {}

	// Bi-predicates

	/**
	 * Functional interface that only allows runtime exceptions.
	 */
	@FunctionalInterface
	public interface BiPredicate<T, U>
		extends Excepts.BiPredicate<RuntimeException, T, U>, java.util.function.BiPredicate<T, U> {}

	/**
	 * Functional interface that only allows runtime exceptions.
	 */
	@FunctionalInterface
	public interface BinPredicate<T>
		extends BiPredicate<T, T>, Excepts.BinPredicate<RuntimeException, T> {}

	/**
	 * Functional interface that only allows runtime exceptions.
	 */
	@FunctionalInterface
	public interface ObjBoolPredicate<T> extends Excepts.ObjBoolPredicate<RuntimeException, T> {}

	/**
	 * Functional interface that only allows runtime exceptions.
	 */
	@FunctionalInterface
	public interface ObjBytePredicate<T> extends Excepts.ObjBytePredicate<RuntimeException, T> {}

	/**
	 * Functional interface that only allows runtime exceptions.
	 */
	@FunctionalInterface
	public interface ObjIntPredicate<T> extends Excepts.ObjIntPredicate<RuntimeException, T> {}

	/**
	 * Functional interface that only allows runtime exceptions.
	 */
	@FunctionalInterface
	public interface ObjLongPredicate<T> extends Excepts.ObjLongPredicate<RuntimeException, T> {}

	/**
	 * Functional interface that only allows runtime exceptions.
	 */
	@FunctionalInterface
	public interface ObjDoublePredicate<T>
		extends Excepts.ObjDoublePredicate<RuntimeException, T> {}

	/**
	 * Functional interface that only allows runtime exceptions.
	 */
	@FunctionalInterface
	public interface BoolBiPredicate extends Excepts.BoolBiPredicate<RuntimeException> {}

	/**
	 * Functional interface that only allows runtime exceptions.
	 */
	@FunctionalInterface
	public interface ByteBiPredicate extends Excepts.ByteBiPredicate<RuntimeException> {}

	/**
	 * Functional interface that only allows runtime exceptions.
	 */
	@FunctionalInterface
	public interface IntBiPredicate extends Excepts.IntBiPredicate<RuntimeException> {}

	/**
	 * Functional interface that only allows runtime exceptions.
	 */
	@FunctionalInterface
	public interface LongBiPredicate extends Excepts.LongBiPredicate<RuntimeException> {}

	/**
	 * Functional interface that only allows runtime exceptions.
	 */
	@FunctionalInterface
	public interface DoubleBiPredicate extends Excepts.DoubleBiPredicate<RuntimeException> {}

	// Consumers

	/**
	 * Functional interface that only allows runtime exceptions.
	 */
	@FunctionalInterface
	public interface Consumer<T>
		extends Excepts.Consumer<RuntimeException, T>, java.util.function.Consumer<T> {}

	/**
	 * Functional interface that only allows runtime exceptions.
	 */
	@FunctionalInterface
	public interface BoolConsumer extends Excepts.BoolConsumer<RuntimeException> {}

	/**
	 * Functional interface that only allows runtime exceptions.
	 */
	@FunctionalInterface
	public interface ByteConsumer extends Excepts.ByteConsumer<RuntimeException> {}

	/**
	 * Functional interface that only allows runtime exceptions.
	 */
	@FunctionalInterface
	public interface IntConsumer
		extends Excepts.IntConsumer<RuntimeException>, java.util.function.IntConsumer {}

	/**
	 * Functional interface that only allows runtime exceptions.
	 */
	@FunctionalInterface
	public interface LongConsumer
		extends Excepts.LongConsumer<RuntimeException>, java.util.function.LongConsumer {}

	/**
	 * Functional interface that only allows runtime exceptions.
	 */
	@FunctionalInterface
	public interface DoubleConsumer
		extends Excepts.DoubleConsumer<RuntimeException>, java.util.function.DoubleConsumer {}

	// Bi-consumers

	/**
	 * Functional interface that only allows runtime exceptions.
	 */
	@FunctionalInterface
	public interface BiConsumer<T, U>
		extends Excepts.BiConsumer<RuntimeException, T, U>, java.util.function.BiConsumer<T, U> {}

	/**
	 * Functional interface that only allows runtime exceptions.
	 */
	@FunctionalInterface
	public interface BinConsumer<T>
		extends BiConsumer<T, T>, Excepts.BinConsumer<RuntimeException, T> {}

	/**
	 * Functional interface that only allows runtime exceptions.
	 */
	@FunctionalInterface
	public interface ObjBoolConsumer<T> extends Excepts.ObjBoolConsumer<RuntimeException, T> {}

	/**
	 * Functional interface that only allows runtime exceptions.
	 */
	@FunctionalInterface
	public interface ObjByteConsumer<T> extends Excepts.ObjByteConsumer<RuntimeException, T> {}

	/**
	 * Functional interface that only allows runtime exceptions.
	 */
	@FunctionalInterface
	public interface ObjIntConsumer<T>
		extends Excepts.ObjIntConsumer<RuntimeException, T>, java.util.function.ObjIntConsumer<T> {}

	/**
	 * Functional interface that only allows runtime exceptions.
	 */
	@FunctionalInterface
	public interface ObjLongConsumer<T> extends Excepts.ObjLongConsumer<RuntimeException, T>,
		java.util.function.ObjLongConsumer<T> {}

	/**
	 * Functional interface that only allows runtime exceptions.
	 */
	@FunctionalInterface
	public interface ObjDoubleConsumer<T> extends Excepts.ObjDoubleConsumer<RuntimeException, T>,
		java.util.function.ObjDoubleConsumer<T> {}

	/**
	 * Functional interface that only allows runtime exceptions.
	 */
	@FunctionalInterface
	public interface BoolBiConsumer extends Excepts.BoolBiConsumer<RuntimeException> {}

	/**
	 * Functional interface that only allows runtime exceptions.
	 */
	@FunctionalInterface
	public interface ByteBiConsumer extends Excepts.ByteBiConsumer<RuntimeException> {}

	/**
	 * Functional interface that only allows runtime exceptions.
	 */
	@FunctionalInterface
	public interface IntBiConsumer extends Excepts.IntBiConsumer<RuntimeException> {}

	/**
	 * Functional interface that only allows runtime exceptions.
	 */
	@FunctionalInterface
	public interface LongBiConsumer extends Excepts.LongBiConsumer<RuntimeException> {}

	/**
	 * Functional interface that only allows runtime exceptions.
	 */
	@FunctionalInterface
	public interface DoubleBiConsumer extends Excepts.DoubleBiConsumer<RuntimeException> {}

	// Tri-consumers

	/**
	 * Functional interface that only allows runtime exceptions.
	 */
	@FunctionalInterface
	public interface BiObjIntConsumer<T, U>
		extends Excepts.BiObjIntConsumer<RuntimeException, T, U> {}

	/**
	 * Functional interface that only allows runtime exceptions.
	 */
	@FunctionalInterface
	public interface ObjBiIntConsumer<T> extends Excepts.ObjBiIntConsumer<RuntimeException, T> {}

	// Suppliers

	/**
	 * Functional interface that only allows runtime exceptions.
	 */
	@FunctionalInterface
	public interface Supplier<T>
		extends Excepts.Supplier<RuntimeException, T>, java.util.function.Supplier<T> {}

	/**
	 * Functional interface that only allows runtime exceptions.
	 */
	@FunctionalInterface
	public interface BoolSupplier
		extends Excepts.BoolSupplier<RuntimeException> /* java.util.function.BooleanSupplier */ {}

	/**
	 * Functional interface that only allows runtime exceptions.
	 */
	@FunctionalInterface
	public interface ByteSupplier extends Excepts.ByteSupplier<RuntimeException> {}

	/**
	 * Functional interface that only allows runtime exceptions.
	 */
	@FunctionalInterface
	public interface IntSupplier
		extends Excepts.IntSupplier<RuntimeException>, java.util.function.IntSupplier {}

	/**
	 * Functional interface that only allows runtime exceptions.
	 */
	@FunctionalInterface
	public interface LongSupplier
		extends Excepts.LongSupplier<RuntimeException>, java.util.function.LongSupplier {}

	/**
	 * Functional interface that only allows runtime exceptions.
	 */
	@FunctionalInterface
	public interface DoubleSupplier
		extends Excepts.DoubleSupplier<RuntimeException>, java.util.function.DoubleSupplier {}

	// Runnables

	/**
	 * Functional interface that only allows runtime exceptions.
	 */
	@FunctionalInterface
	public interface Runnable extends Excepts.Runnable<RuntimeException>, java.lang.Runnable {}

	// Other

	/**
	 * Functional interface that only allows runtime exceptions.
	 */
	@FunctionalInterface
	public interface Closeable extends Excepts.Closeable<RuntimeException> {}
}
