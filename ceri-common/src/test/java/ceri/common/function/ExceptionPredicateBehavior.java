package ceri.common.function;

import static ceri.common.function.FunctionTestUtil.predicate;
import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertFalse;
import static ceri.common.test.AssertUtil.assertThrown;
import static ceri.common.test.AssertUtil.assertTrue;
import java.io.IOException;
import java.util.function.Predicate;
import org.junit.Test;
import ceri.common.function.FunctionTestUtil.Std;

public class ExceptionPredicateBehavior {

	@Test
	public void shouldAllowNaming() throws IOException {
		ExceptionPredicate<IOException, Integer> p = predicate().name("name");
		assertEquals(p.toString(), "name");
		assertTrue(p.test(2));
		assertFalse(p.test(-1));
		assertThrown(IOException.class, () -> p.test(1));
		assertThrown(RuntimeException.class, () -> p.test(0));
	}

	@Test
	public void shouldConvertToPredicate() {
		Predicate<Integer> p = predicate().asPredicate();
		assertTrue(p.test(2));
		assertThrown(RuntimeException.class, () -> p.test(1));
		assertThrown(RuntimeException.class, () -> p.test(0));
	}

	@Test
	public void shouldConvertFromPredicate() {
		ExceptionPredicate<RuntimeException, Integer> p = ExceptionPredicate.of(Std.predicate());
		assertTrue(p.test(1));
		assertFalse(p.test(-1));
		assertThrown(() -> p.test(0));
	}

	@Test
	public void shouldNegateTest() throws IOException {
		ExceptionPredicate<IOException, Integer> p0 = predicate();
		ExceptionPredicate<IOException, Integer> p = p0.negate();
		assertTrue(p0.test(2));
		assertFalse(p.test(2));
		assertFalse(p0.test(-1));
		assertTrue(p.test(-1));
		assertThrown(IOException.class, () -> p.test(1));
		assertThrown(RuntimeException.class, () -> p.test(0));
	}

	@Test
	public void shouldLogicallyOrTests() throws IOException {
		ExceptionPredicate<IOException, Integer> p = predicate().or(i -> i < -1);
		assertTrue(p.test(2));
		assertTrue(p.test(-2));
		assertFalse(p.test(-1));
		assertThrown(IOException.class, () -> p.test(1));
		assertThrown(RuntimeException.class, () -> p.test(0));
	}

	@Test
	public void shouldLogicallyAndTests() throws IOException {
		ExceptionPredicate<IOException, Integer> p = predicate().and(i -> i < 3);
		assertTrue(p.test(2));
		assertFalse(p.test(3));
		assertFalse(p.test(-1));
		assertThrown(IOException.class, () -> p.test(1));
		assertThrown(RuntimeException.class, () -> p.test(0));
	}

	@Test
	public void shouldAdaptPredicate() throws IOException {
		ExceptionPredicate<IOException, String> p =
			ExceptionPredicate.testing(String::length, i -> i > 0);
		assertFalse(p.test(""));
		assertTrue(p.test("x"));
	}

	@Test
	public void shouldAdaptIntPredicate() throws IOException {
		ExceptionPredicate<IOException, String> p =
			ExceptionPredicate.testingInt(String::length, i -> i > 0);
		assertFalse(p.test(""));
		assertTrue(p.test("x"));
	}

}
