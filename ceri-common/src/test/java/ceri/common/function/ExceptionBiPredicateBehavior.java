package ceri.common.function;

import static ceri.common.function.FunctionTestUtil.biPredicate;
import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertFalse;
import static ceri.common.test.AssertUtil.assertIoe;
import static ceri.common.test.AssertUtil.assertRte;
import static ceri.common.test.AssertUtil.assertTrue;
import java.io.IOException;
import java.util.function.BiPredicate;
import org.junit.Test;
import ceri.common.function.FunctionTestUtil.Std;

public class ExceptionBiPredicateBehavior {

	@Test
	public void shouldAllowNaming() throws IOException {
		ExceptionBiPredicate<IOException, Integer, Integer> p = biPredicate().name("name");
		assertEquals(p.toString(), "name");
		assertTrue(p.test(2, 2));
		assertFalse(p.test(-1, 2));
		assertIoe(() -> p.test(1, 2));
		assertRte(() -> p.test(0, 2));
	}

	@Test
	public void shouldConvertToBiPredicate() {
		BiPredicate<Integer, Integer> p = biPredicate().asBiPredicate();
		assertTrue(p.test(2, 2));
		assertRte(() -> p.test(1, 2));
		assertRte(() -> p.test(0, 2));
	}

	@Test
	public void shouldConvertFromBiPredicate() {
		ExceptionBiPredicate<RuntimeException, Integer, Integer> p =
			ExceptionBiPredicate.of(Std.biPredicate());
		assertTrue(p.test(1, 2));
		assertFalse(p.test(-1, 2));
		assertRte(() -> p.test(0, 2));
	}

	@Test
	public void shouldNegateTest() throws IOException {
		ExceptionBiPredicate<IOException, Integer, Integer> p0 = biPredicate();
		ExceptionBiPredicate<IOException, Integer, Integer> p = p0.negate();
		assertTrue(p0.test(2, 2));
		assertFalse(p.test(2, 2));
		assertTrue(p.test(-2, 2));
		assertIoe(() -> p.test(1, 2));
		assertRte(() -> p.test(0, 2));
	}

	@Test
	public void shouldLogicallyOrTests() throws IOException {
		ExceptionBiPredicate<IOException, Integer, Integer> p = biPredicate().or((i, _) -> i < -1);
		assertTrue(p.test(2, 2));
		assertTrue(p.test(-2, 2));
		assertFalse(p.test(-1, 2));
		assertIoe(() -> p.test(1, 2));
		assertRte(() -> p.test(0, 2));
	}

	@Test
	public void shouldLogicallyAndTests() throws IOException {
		ExceptionBiPredicate<IOException, Integer, Integer> p = biPredicate().and((i, _) -> i < 3);
		assertTrue(p.test(2, 2));
		assertFalse(p.test(3, 2));
		assertFalse(p.test(-1, 2));
		assertIoe(() -> p.test(1, 2));
		assertRte(() -> p.test(0, 2));
	}

}
