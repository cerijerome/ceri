package ceri.common.function;

import static ceri.common.function.FunctionTestUtil.objLongPredicate;
import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertFalse;
import static ceri.common.test.AssertUtil.assertThrown;
import static ceri.common.test.AssertUtil.assertTrue;
import java.io.IOException;
import org.junit.Test;
import ceri.common.function.FunctionTestUtil.Std;

public class ExceptionObjLongPredicateBehavior {

	@Test
	public void shouldAllowNaming() throws IOException {
		ExceptionObjLongPredicate<IOException, Integer> p = objLongPredicate().name("name");
		assertEquals(p.toString(), "name");
		assertTrue(p.test(2, 2));
		assertFalse(p.test(-1, 2));
		assertThrown(IOException.class, () -> p.test(1, 2));
		assertThrown(RuntimeException.class, () -> p.test(0, 2));
	}

	@Test
	public void shouldConvertToPredicate() {
		ObjLongPredicate<Integer> p = objLongPredicate().asPredicate();
		assertTrue(p.test(2, 2));
		assertThrown(RuntimeException.class, () -> p.test(1, 2));
		assertThrown(RuntimeException.class, () -> p.test(0, 2));
	}

	@Test
	public void shouldConvertFromPredicate() {
		ExceptionObjLongPredicate<RuntimeException, Integer> p =
			ExceptionObjLongPredicate.of(Std.objLongPredicate());
		assertTrue(p.test(1, 2));
		assertFalse(p.test(-1, 2));
		assertThrown(() -> p.test(0, 2));
	}

	@Test
	public void shouldNegateTest() throws IOException {
		ExceptionObjLongPredicate<IOException, Integer> p0 = objLongPredicate();
		ExceptionObjLongPredicate<IOException, Integer> p = p0.negate();
		assertTrue(p0.test(2, 2));
		assertFalse(p.test(2, 2));
		assertTrue(p.test(-2, 2));
		assertThrown(IOException.class, () -> p.test(1, 2));
		assertThrown(RuntimeException.class, () -> p.test(0, 2));
	}

	@Test
	public void shouldLogicallyOrTests() throws IOException {
		ExceptionObjLongPredicate<IOException, Integer> p = objLongPredicate().or((i, _) -> i < -1);
		assertTrue(p.test(2, 2));
		assertTrue(p.test(-2, 2));
		assertFalse(p.test(-1, 2));
		assertThrown(IOException.class, () -> p.test(1, 2));
		assertThrown(RuntimeException.class, () -> p.test(0, 2));
	}

	@Test
	public void shouldLogicallyAndTests() throws IOException {
		ExceptionObjLongPredicate<IOException, Integer> p = objLongPredicate().and((i, _) -> i < 3);
		assertTrue(p.test(2, 2));
		assertFalse(p.test(3, 2));
		assertFalse(p.test(-1, 2));
		assertThrown(IOException.class, () -> p.test(1, 2));
		assertThrown(RuntimeException.class, () -> p.test(0, 2));
	}

}
