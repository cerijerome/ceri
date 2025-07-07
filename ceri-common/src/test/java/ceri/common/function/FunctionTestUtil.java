package ceri.common.function;

import ceri.common.test.TestUtil.Ioe;
import ceri.common.test.TestUtil.Rte;

/**
 * Function types to help with testing: 0 => throws RuntimeException, 1 => throws IOException
 */
public class FunctionTestUtil {

	public static class S {
		private S() {}

		public static final Funcs.BinConsumer<Integer> binConsumer = (l, r) -> fn(l, r);

		private static int fn(int l, int r) {
			if (l == 0 || r == 0) throw new Rte("0");
			return l + r;
		}
	}

	public static class T {
		private T() {}
	}

	public static class E {
		private E() {}

		public static final Excepts.Function<Ioe, Integer, Integer> function = i -> fn(i, i);
		public static final Excepts.IntFunction<Ioe, Integer> intFunction = i -> fn(i, i);
		public static final Excepts.ToIntFunction<Ioe, Integer> toIntFunction = i -> fn(i, i);
		public static final Excepts.IntOperator<Ioe> intOperator = i -> fn(i, i);
		public static final Excepts.Predicate<Ioe, Integer> predicate = i -> fn(i, i) > 0;
		public static final Excepts.IntPredicate<Ioe> intPredicate = i -> fn(i, i) > 0;
		public static final Excepts.Consumer<Ioe, Integer> consumer = i -> fn(i, i);
		public static final Excepts.IntConsumer<Ioe> intConsumer = i -> fn(i, i);

		public static final Excepts.Supplier<Ioe, Integer> supplier(int i) {
			return () -> fn(i, i);
		}

		public static final Excepts.Runnable<Ioe> runnable(int i) {
			return () -> fn(i, i);
		}

		private static int fn(int l, int r) throws Ioe {
			if (l == 1 || r == 1) throw new Ioe("1");
			if (l == 0 || r == 0) throw new Rte("0");
			return l;
		}
	}
}
