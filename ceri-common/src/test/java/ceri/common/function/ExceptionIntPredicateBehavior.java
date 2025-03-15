package ceri.common.function;

import static ceri.common.function.FunctionTestUtil.intPredicate;
import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertFalse;
import static ceri.common.test.AssertUtil.assertIoe;
import static ceri.common.test.AssertUtil.assertRte;
import static ceri.common.test.AssertUtil.assertThrown;
import static ceri.common.test.AssertUtil.assertTrue;
import java.io.IOException;
import java.util.function.IntPredicate;
import org.junit.Test;
import ceri.common.function.FunctionTestUtil.Std;

public class ExceptionIntPredicateBehavior {

	@Test
	public void shouldAllowNaming() throws IOException {
		ExceptionIntPredicate<IOException> p = intPredicate().name("name");
		assertEquals(p.toString(), "name");
		assertTrue(p.test(2));
		assertFalse(p.test(-1));
		assertIoe(() -> p.test(1));
		assertRte(() -> p.test(0));
	}

	@Test
	public void shouldConvertToPredicate() {
		IntPredicate p = intPredicate().asPredicate();
		assertTrue(p.test(2));
		assertRte(() -> p.test(1));
		assertRte(() -> p.test(0));
	}

	@Test
	public void shouldConvertFromPredicate() {
		ExceptionIntPredicate<RuntimeException> p = ExceptionIntPredicate.of(Std.intPredicate());
		assertTrue(p.test(1));
		assertFalse(p.test(-1));
		assertThrown(() -> p.test(0));
	}

	@Test
	public void shouldNegateTest() throws IOException {
		ExceptionIntPredicate<IOException> p0 = intPredicate();
		ExceptionIntPredicate<IOException> p = p0.negate();
		assertTrue(p0.test(2));
		assertFalse(p.test(2));
		assertTrue(p.test(-1));
		assertIoe(() -> p.test(1));
		assertRte(() -> p.test(0));
	}

	@Test
	public void shouldLogicallyOrTests() throws IOException {
		ExceptionIntPredicate<IOException> p = intPredicate().or(i -> i < -1);
		assertTrue(p.test(2));
		assertTrue(p.test(-2));
		assertFalse(p.test(-1));
		assertIoe(() -> p.test(1));
		assertRte(() -> p.test(0));
	}

	@Test
	public void shouldLogicallyAndTests() throws IOException {
		ExceptionIntPredicate<IOException> p = intPredicate().and(i -> i < 3);
		assertTrue(p.test(2));
		assertFalse(p.test(3));
		assertFalse(p.test(-1));
		assertIoe(() -> p.test(1));
		assertRte(() -> p.test(0));
	}

}
