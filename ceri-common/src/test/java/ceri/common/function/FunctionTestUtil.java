package ceri.common.function;

import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.DoubleSupplier;
import java.util.function.Function;
import java.util.function.IntBinaryOperator;
import java.util.function.IntConsumer;
import java.util.function.IntFunction;
import java.util.function.IntPredicate;
import java.util.function.IntSupplier;
import java.util.function.IntUnaryOperator;
import java.util.function.LongConsumer;
import java.util.function.LongFunction;
import java.util.function.LongPredicate;
import java.util.function.LongSupplier;
import java.util.function.LongUnaryOperator;
import java.util.function.ObjIntConsumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.ToIntFunction;
import java.util.function.ToLongFunction;

/**
 * Utilities to help test functions.
 */
public class FunctionTestUtil {

	/*
	 * Function types to help with testing: 0 => throws RuntimeException, 1 => throws IOException
	 */

	public static ExceptionRunnable<IOException> runnable(int i) {
		return () -> {
			if (i == 1) throw new IOException("1");
			if (i == 0) throw new RuntimeException("0");
		};
	}

	public static ExceptionSupplier<IOException, Integer> supplier(int i) {
		return () -> {
			if (i == 1) throw new IOException("1");
			if (i == 0) throw new RuntimeException("0");
			return i;
		};
	}

	public static ExceptionBooleanSupplier<IOException> booleanSupplier(Boolean b) {
		return () -> {
			if (b == null) throw new RuntimeException("null");
			if (!b) throw new IOException("false");
			return b;
		};
	}

	public static ExceptionIntSupplier<IOException> intSupplier(int i) {
		return () -> {
			if (i == 1) throw new IOException("1");
			if (i == 0) throw new RuntimeException("0");
			return i;
		};
	}

	public static ExceptionLongSupplier<IOException> longSupplier(long l) {
		return () -> {
			if (l == 1) throw new IOException("1");
			if (l == 0) throw new RuntimeException("0");
			return l;
		};
	}

	public static ExceptionDoubleSupplier<IOException> doubleSupplier(double d) {
		return () -> {
			if (d == 1.0) throw new IOException("1.0");
			if (d == 0.0) throw new RuntimeException("0.0");
			return d;
		};
	}

	public static ExceptionConsumer<IOException, Integer> consumer() {
		return i -> {
			if (i == 1) throw new IOException("1");
			if (i == 0) throw new RuntimeException("0");
		};
	}

	public static ExceptionIntConsumer<IOException> intConsumer() {
		return i -> {
			if (i == 1) throw new IOException("1");
			if (i == 0) throw new RuntimeException("0");
		};
	}

	public static ExceptionLongConsumer<IOException> longConsumer() {
		return i -> {
			if (i == 1) throw new IOException("1");
			if (i == 0) throw new RuntimeException("0");
		};
	}

	public static ExceptionBiConsumer<IOException, Integer, Integer> biConsumer() {
		return (i, j) -> {
			if (i == 1 || j == 1) throw new IOException("1");
			if (i == 0 || j == 0) throw new RuntimeException("0");
		};
	}

	public static ExceptionObjIntConsumer<IOException, Integer> objIntConsumer() {
		return (i, j) -> {
			if (i == 1 || j == 1) throw new IOException("1");
			if (i == 0 || j == 0) throw new RuntimeException("0");
		};
	}

	public static ExceptionFunction<IOException, Integer, Integer> function() {
		return i -> {
			if (i == 1) throw new IOException("1");
			if (i == 0) throw new RuntimeException("0");
			return i;
		};
	}

	public static ExceptionIntFunction<IOException, Integer> intFunction() {
		return i -> {
			if (i == 1) throw new IOException("1");
			if (i == 0) throw new RuntimeException("0");
			return i;
		};
	}

	public static ExceptionLongFunction<IOException, Integer> longFunction() {
		return i -> {
			if (i == 1) throw new IOException("1");
			if (i == 0) throw new RuntimeException("0");
			return Math.toIntExact(i);
		};
	}

	public static ExceptionToIntFunction<IOException, Integer> toIntFunction() {
		return i -> {
			if (i == 1) throw new IOException("1");
			if (i == 0) throw new RuntimeException("0");
			return i;
		};
	}

	public static ExceptionToLongFunction<IOException, Integer> toLongFunction() {
		return i -> {
			if (i == 1) throw new IOException("1");
			if (i == 0) throw new RuntimeException("0");
			return i;
		};
	}

	public static ExceptionIntUnaryOperator<IOException> intUnaryOperator() {
		return i -> {
			if (i == 1) throw new IOException("1");
			if (i == 0) throw new RuntimeException("0");
			return i;
		};
	}

	public static ExceptionLongUnaryOperator<IOException> longUnaryOperator() {
		return i -> {
			if (i == 1) throw new IOException("1");
			if (i == 0) throw new RuntimeException("0");
			return i;
		};
	}

	public static ExceptionObjIntFunction<IOException, Integer, Integer> objIntFunction() {
		return (i, j) -> {
			if (i == 1 || j == 1) throw new IOException("1");
			if (i == 0 || j == 0) throw new RuntimeException("0");
			return i + j;
		};
	}

	public static ExceptionBiFunction<IOException, Integer, Integer, Integer> biFunction() {
		return (i, j) -> {
			if (i == 1 || j == 1) throw new IOException("1");
			if (i == 0 || j == 0) throw new RuntimeException("0");
			return i + j;
		};
	}

	public static ExceptionIntBinaryOperator<IOException> intBinaryOperator() {
		return (i, j) -> {
			if (i == 1 || j == 1) throw new IOException("1");
			if (i == 0 || j == 0) throw new RuntimeException("0");
			return i + j;
		};
	}

	public static ExceptionPredicate<IOException, Integer> predicate() {
		return i -> {
			if (i == 1) throw new IOException("1");
			if (i == 0) throw new RuntimeException("0");
			return i > 0;
		};
	}

	public static ExceptionIntPredicate<IOException> intPredicate() {
		return i -> {
			if (i == 1) throw new IOException("1");
			if (i == 0) throw new RuntimeException("0");
			return i > 0;
		};
	}

	public static ExceptionLongPredicate<IOException> longPredicate() {
		return i -> {
			if (i == 1) throw new IOException("1");
			if (i == 0) throw new RuntimeException("0");
			return i > 0;
		};
	}

	public static ExceptionBiPredicate<IOException, Integer, Integer> biPredicate() {
		return (i, j) -> {
			if (i == 1 || j == 1) throw new IOException("1");
			if (i == 0 || j == 0) throw new RuntimeException("0");
			return i > 0;
		};
	}

	public static ExceptionObjIntPredicate<IOException, Integer> objIntPredicate() {
		return (i, j) -> {
			if (i == 1 || j == 1) throw new IOException("1");
			if (i == 0 || j == 0) throw new RuntimeException("0");
			return i > 0;
		};
	}

	/**
	 * Standard function types to help with testing: 0 => throws RuntimeException
	 */
	public static class Std {

		public static Runnable runnable(int i) {
			return () -> {
				if (i == 0) throw new RuntimeException("0");
			};
		}

		public static Callable<Integer> callable(int i) {
			return () -> {
				if (i == 1) throw new IOException("1");
				if (i == 0) throw new RuntimeException("0");
				return i;
			};
		}

		public static Supplier<Integer> supplier(int i) {
			return () -> {
				if (i == 0) throw new RuntimeException("0");
				return i;
			};
		}

		public static BooleanSupplier booleanSupplier(boolean b) {
			return () -> {
				if (!b) throw new RuntimeException("false");
				return b;
			};
		}

		public static IntSupplier intSupplier(int i) {
			return () -> {
				if (i == 0) throw new RuntimeException("0");
				return i;
			};
		}

		public static LongSupplier longSupplier(long l) {
			return () -> {
				if (l == 0) throw new RuntimeException("0");
				return l;
			};
		}

		public static DoubleSupplier doubleSupplier(double d) {
			return () -> {
				if (d == 0.0) throw new RuntimeException("0.0");
				return d;
			};
		}

		public static Consumer<Integer> consumer() {
			return i -> {
				if (i == 0) throw new RuntimeException("0");
			};
		}

		public static IntConsumer intConsumer() {
			return i -> {
				if (i == 0) throw new RuntimeException("0");
			};
		}

		public static LongConsumer longConsumer() {
			return i -> {
				if (i == 0) throw new RuntimeException("0");
			};
		}

		public static BiConsumer<Integer, Integer> biConsumer() {
			return (i, j) -> {
				if (i == 0 || j == 0) throw new RuntimeException("0");
			};
		}

		public static ObjIntConsumer<Integer> objIntConsumer() {
			return (i, j) -> {
				if (i == 0 || j == 0) throw new RuntimeException("0");
			};
		}

		public static Function<Integer, Integer> function() {
			return i -> {
				if (i == 0) throw new RuntimeException("0");
				return i;
			};
		}

		public static IntFunction<Integer> intFunction() {
			return i -> {
				if (i == 0) throw new RuntimeException("0");
				return i;
			};
		}

		public static LongFunction<Integer> longFunction() {
			return i -> {
				if (i == 0) throw new RuntimeException("0");
				return Math.toIntExact(i);
			};
		}

		public static ToIntFunction<Integer> toIntFunction() {
			return i -> {
				if (i == 0) throw new RuntimeException("0");
				return i;
			};
		}

		public static ToLongFunction<Integer> toLongFunction() {
			return i -> {
				if (i == 0) throw new RuntimeException("0");
				return i;
			};
		}

		public static IntUnaryOperator intUnaryOperator() {
			return i -> {
				if (i == 0) throw new RuntimeException("0");
				return i;
			};
		}

		public static LongUnaryOperator longUnaryOperator() {
			return i -> {
				if (i == 0) throw new RuntimeException("0");
				return i;
			};
		}

		public static ObjIntFunction<Integer, Integer> objIntFunction() {
			return (i, j) -> {
				if (i == 0 || j == 0) throw new RuntimeException("0");
				return i + j;
			};
		}

		public static BiFunction<Integer, Integer, Integer> biFunction() {
			return (i, j) -> {
				if (i == 0 || j == 0) throw new RuntimeException("0");
				return i + j;
			};
		}

		public static IntBinaryOperator intBinaryOperator() {
			return (i, j) -> {
				if (i == 0 || j == 0) throw new RuntimeException("0");
				return i + j;
			};
		}

		public static Predicate<Integer> predicate() {
			return i -> {
				if (i == 0) throw new RuntimeException("0");
				return i > 0;
			};
		}

		public static IntPredicate intPredicate() {
			return i -> {
				if (i == 0) throw new RuntimeException("0");
				return i > 0;
			};
		}

		public static LongPredicate longPredicate() {
			return i -> {
				if (i == 0) throw new RuntimeException("0");
				return i > 0;
			};
		}

		public static BiPredicate<Integer, Integer> biPredicate() {
			return (i, j) -> {
				if (i == 0 || j == 0) throw new RuntimeException("0");
				return i > 0;
			};
		}

		public static ObjIntPredicate<Integer> objIntPredicate() {
			return (i, j) -> {
				if (i == 0 || j == 0) throw new RuntimeException("0");
				return i > 0;
			};
		}
	}
}
