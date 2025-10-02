package ceri.common.function;

import static ceri.common.test.AssertUtil.assertEquals;
import org.junit.Before;
import org.junit.Test;
import ceri.common.function.Excepts.Function;

public class AccessibleBehavior {
	private A nullA;
	private A testA;

	public static class A implements Accessible<String> {
		private final String s;
		public int calls = 0;

		public A(String s) {
			this.s = s;
		}

		@Override
		public <E extends Exception, R> R apply(Function<E, ? super String, R> function) throws E {
			calls++;
			return function.apply(s);
		}
	}

	@Before
	public void before() {
		nullA = new A(null);
		testA = new A("test");
	}

	@Test
	public void shouldApplyFunction() {
		assertEquals(nullA.apply(_ -> null), null);
		assertEquals(nullA.apply(_ -> 0), 0);
		assertEquals(testA.apply(_ -> null), null);
		assertEquals(testA.apply(s -> s.length()), 4);
		assertEquals(nullA.calls, 2);
		assertEquals(testA.calls, 2);
	}

	@Test
	public void shouldAcceptConsumer() {
		nullA.accept(s -> assertEquals(s, null));
		testA.accept(s -> assertEquals(s, "test"));
		assertEquals(nullA.calls, 1);
		assertEquals(testA.calls, 1);
	}
}
