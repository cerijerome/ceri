package ceri.common.function;

import static ceri.common.function.FunctionTestUtil.longPredicate;
import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertFalse;
import static ceri.common.test.AssertUtil.assertThrown;
import static ceri.common.test.AssertUtil.assertTrue;
import java.io.IOException;
import java.util.function.LongPredicate;
import org.junit.Test;
import ceri.common.function.FunctionTestUtil.Std;

public class ExceptionLongPredicateBehavior {

	@Test
	public void shouldAllowNaming() throws IOException {
		ExceptionLongPredicate<IOException> p = longPredicate().name("name");
		assertEquals(p.toString(), "name");
		assertTrue(p.test(2));
		assertFalse(p.test(-1));
		assertThrown(IOException.class, () -> p.test(1));
		assertThrown(RuntimeException.class, () -> p.test(0));
	}

	@Test
	public void shouldConvertToPredicate() {
		LongPredicate p = longPredicate().asPredicate();
		assertTrue(p.test(2));
		assertThrown(RuntimeException.class, () -> p.test(1));
		assertThrown(RuntimeException.class, () -> p.test(0));
	}

	@Test
	public void shouldConvertFromPredicate() {
		ExceptionLongPredicate<RuntimeException> p = ExceptionLongPredicate.of(Std.longPredicate());
		assertTrue(p.test(1));
		assertFalse(p.test(-1));
		assertThrown(() -> p.test(0));
	}

	@Test
	public void shouldNegateTest() throws IOException {
		ExceptionLongPredicate<IOException> p0 = longPredicate();
		ExceptionLongPredicate<IOException> p = p0.negate();
		assertTrue(p0.test(2));
		assertFalse(p.test(2));
		assertTrue(p.test(-1));
		assertThrown(IOException.class, () -> p.test(1));
		assertThrown(RuntimeException.class, () -> p.test(0));
	}

	@Test
	public void shouldLogicallyOrTests() throws IOException {
		ExceptionLongPredicate<IOException> p = longPredicate().or(i -> i < -1);
		assertTrue(p.test(2));
		assertTrue(p.test(-2));
		assertFalse(p.test(-1));
		assertThrown(IOException.class, () -> p.test(1));
		assertThrown(RuntimeException.class, () -> p.test(0));
	}

	@Test
	public void shouldLogicallyAndTests() throws IOException {
		ExceptionLongPredicate<IOException> p = longPredicate().and(i -> i < 3);
		assertTrue(p.test(2));
		assertFalse(p.test(3));
		assertFalse(p.test(-1));
		assertThrown(IOException.class, () -> p.test(1));
		assertThrown(RuntimeException.class, () -> p.test(0));
	}

}
