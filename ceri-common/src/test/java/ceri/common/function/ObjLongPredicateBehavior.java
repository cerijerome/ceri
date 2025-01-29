package ceri.common.function;

import static ceri.common.test.AssertUtil.assertFalse;
import static ceri.common.test.AssertUtil.assertThrown;
import static ceri.common.test.AssertUtil.assertTrue;
import org.junit.Test;
import ceri.common.function.FunctionTestUtil.Std;

public class ObjLongPredicateBehavior {

	@Test
	public void shouldNegateTest() {
		ObjLongPredicate<Integer> p0 = Std.objLongPredicate();
		ObjLongPredicate<Integer> p = p0.negate();
		assertTrue(p0.test(2, 2));
		assertFalse(p.test(2, 2));
		assertTrue(p.test(-2, 2));
		assertThrown(RuntimeException.class, () -> p.test(0, 2));
	}

	@Test
	public void shouldLogicallyOrTests() {
		ObjLongPredicate<Integer> p = Std.objLongPredicate().or((i, _) -> i < -1);
		assertTrue(p.test(2, 2));
		assertTrue(p.test(-2, 2));
		assertFalse(p.test(-1, 2));
		assertThrown(RuntimeException.class, () -> p.test(0, 2));
	}

	@Test
	public void shouldLogicallyAndTests() {
		ObjLongPredicate<Integer> p = Std.objLongPredicate().and((i, _) -> i < 3);
		assertTrue(p.test(2, 2));
		assertFalse(p.test(3, 2));
		assertFalse(p.test(-1, 2));
		assertThrown(RuntimeException.class, () -> p.test(0, 2));
	}

}
