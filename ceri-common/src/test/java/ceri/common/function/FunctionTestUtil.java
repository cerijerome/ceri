package ceri.common.function;

import java.io.IOException;

/**
 * Function types to help with testing: 0 => throws RuntimeException, 1 => throws IOException
 */
public class FunctionTestUtil {

	public static class F {
		private F() {}

		public static final Functions.BinConsumer<Integer> binConsumer = (l, r) -> fn(l, r);

		private static int fn(int l, int r) {
			if (l == 0 || r == 0) throw new RuntimeException("0");
			return l + r;
		}
	}

	public static class T {
		private T() {}
	}

	public static class E {
		private E() {}

		public static final Excepts.Function<IOException, Integer, Integer> function =
			i -> fn(i, i);
		public static final Excepts.IntFunction<IOException, Integer> intFunction = i -> fn(i, i);
		public static final Excepts.ToIntFunction<IOException, Integer> toIntFunction =
			i -> fn(i, i);
		public static final Excepts.IntOperator<IOException> intOperator = i -> fn(i, i);
		public static final Excepts.Predicate<IOException, Integer> predicate = i -> fn(i, i) > 0;
		public static final Excepts.IntPredicate<IOException> intPredicate = i -> fn(i, i) > 0;
		public static final Excepts.Consumer<IOException, Integer> consumer = i -> fn(i, i);
		public static final Excepts.IntConsumer<IOException> intConsumer = i -> fn(i, i);

		public static final Excepts.Supplier<IOException, Integer> supplier(int i) {
			return () -> fn(i, i);
		}

		public static final Excepts.Runnable<IOException> runnable(int i) {
			return () -> fn(i, i);
		}

		private static int fn(int l, int r) throws IOException {
			if (l == 1 || r == 1) throw new IOException("1");
			if (l == 0 || r == 0) throw new RuntimeException("0");
			return l;
		}
	}
}
