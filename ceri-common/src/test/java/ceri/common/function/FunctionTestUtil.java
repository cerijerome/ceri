package ceri.common.function;

import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntConsumer;
import java.util.function.IntFunction;
import java.util.function.IntPredicate;
import java.util.function.IntSupplier;
import java.util.function.IntUnaryOperator;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.ToIntFunction;

/**
 * Utilities to help test functions.
 */
public class FunctionTestUtil {

	/*
	 * Function types to help with testing: 0 => throws RuntimeException 1 => throws IOException
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

	public static ExceptionBiConsumer<IOException, Integer, Integer> biConsumer() {
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

	public static ExceptionToIntFunction<IOException, Integer> toIntFunction() {
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

	public static ExceptionBiFunction<IOException, Integer, Integer, Integer> biFunction() {
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

	public static ExceptionBiPredicate<IOException, Integer, Integer> biPredicate() {
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

		public static BiConsumer<Integer, Integer> biConsumer() {
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

		public static ToIntFunction<Integer> toIntFunction() {
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

		public static BiFunction<Integer, Integer, Integer> biFunction() {
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

		public static BiPredicate<Integer, Integer> biPredicate() {
			return (i, j) -> {
				if (i == 0 || j == 0) throw new RuntimeException("0");
				return i > 0;
			};
		}
	}
}
