package ceri.common.function;

import static ceri.common.test.AssertUtil.assertEquals;
import org.junit.Before;
import org.junit.Test;

public class FunctionalBehavior {
	private Fn fnull;
	private Fn ftest;

	public static class Fn implements Functional<String> {
		private final String s;
		public int calls = 0;

		public Fn(String s) {
			this.s = s;
		}

		@Override
		public <E extends Exception, R> R apply(ExceptionFunction<E, String, R> function) throws E {
			calls++;
			return function.apply(s);
		}
	}

	@Before
	public void before() {
		fnull = new Fn(null);
		ftest = new Fn("test");
	}

	@Test
	public void shouldApplyFunction() {
		assertEquals(fnull.apply(_ -> null), null);
		assertEquals(fnull.apply(_ -> 0), 0);
		assertEquals(ftest.apply(_ -> null), null);
		assertEquals(ftest.apply(s -> s.length()), 4);
		assertEquals(fnull.calls, 2);
		assertEquals(ftest.calls, 2);
	}

	@Test
	public void shouldAcceptConsumer() {
		fnull.accept(s -> assertEquals(s, null));
		ftest.accept(s -> assertEquals(s, "test"));
		assertEquals(fnull.calls, 1);
		assertEquals(ftest.calls, 1);
	}

	@Test
	public void shouldGetSupplier() {
		assertEquals(fnull.get(() -> null), null);
		assertEquals(fnull.get(() -> 1), 1);
		assertEquals(ftest.get(() -> null), null);
		assertEquals(ftest.get(() -> 1), 1);
		assertEquals(fnull.calls, 2);
		assertEquals(ftest.calls, 2);
	}

	@Test
	public void shouldRunRunnable() {
		fnull.run(() -> {});
		ftest.run(() -> {});
		assertEquals(fnull.calls, 1);
		assertEquals(ftest.calls, 1);
	}

}
