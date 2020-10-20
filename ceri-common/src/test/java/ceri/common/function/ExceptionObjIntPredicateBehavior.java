package ceri.common.function;

import static ceri.common.function.FunctionTestUtil.objIntPredicate;
import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertFalse;
import static ceri.common.test.AssertUtil.assertThrown;
import static ceri.common.test.AssertUtil.assertTrue;
import java.io.IOException;
import org.junit.Test;
import ceri.common.function.FunctionTestUtil.Std;

public class ExceptionObjIntPredicateBehavior {

	@Test
	public void shouldAllowNaming() throws IOException {
		ExceptionObjIntPredicate<IOException, Integer> p = objIntPredicate().name("name");
		assertEquals(p.toString(), "name");
		assertTrue(p.test(2, 2));
		assertFalse(p.test(-1, 2));
		assertThrown(IOException.class, () -> p.test(1, 2));
		assertThrown(RuntimeException.class, () -> p.test(0, 2));
	}

	@Test
	public void shouldConvertToBiPredicate() {
		ObjIntPredicate<Integer> p = objIntPredicate().asPredicate();
		assertTrue(p.test(2, 2));
		assertThrown(RuntimeException.class, () -> p.test(1, 2));
		assertThrown(RuntimeException.class, () -> p.test(0, 2));
	}

	@Test
	public void shouldConvertFromBiPredicate() {
		ExceptionObjIntPredicate<RuntimeException, Integer> p =
			ExceptionObjIntPredicate.of(Std.objIntPredicate());
		assertTrue(p.test(1, 2));
		assertFalse(p.test(-1, 2));
		assertThrown(() -> p.test(0, 2));
	}

	@Test
	public void shouldNegateTest() throws IOException {
		ExceptionObjIntPredicate<IOException, Integer> p0 = objIntPredicate();
		ExceptionObjIntPredicate<IOException, Integer> p = p0.negate();
		assertTrue(p0.test(2, 2));
		assertFalse(p.test(2, 2));
		assertTrue(p.test(-2, 2));
		assertThrown(IOException.class, () -> p.test(1, 2));
		assertThrown(RuntimeException.class, () -> p.test(0, 2));
	}

	@Test
	public void shouldLogicallyOrTests() throws IOException {
		ExceptionObjIntPredicate<IOException, Integer> p = objIntPredicate().or((i, j) -> i < -1);
		assertTrue(p.test(2, 2));
		assertTrue(p.test(-2, 2));
		assertFalse(p.test(-1, 2));
		assertThrown(IOException.class, () -> p.test(1, 2));
		assertThrown(RuntimeException.class, () -> p.test(0, 2));
	}

	@Test
	public void shouldLogicallyAndTests() throws IOException {
		ExceptionObjIntPredicate<IOException, Integer> p = objIntPredicate().and((i, j) -> i < 3);
		assertTrue(p.test(2, 2));
		assertFalse(p.test(3, 2));
		assertFalse(p.test(-1, 2));
		assertThrown(IOException.class, () -> p.test(1, 2));
		assertThrown(RuntimeException.class, () -> p.test(0, 2));
	}

}
