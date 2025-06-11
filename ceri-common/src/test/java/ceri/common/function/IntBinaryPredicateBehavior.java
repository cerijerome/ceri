package ceri.common.function;

import static ceri.common.test.AssertUtil.assertFalse;
import static ceri.common.test.AssertUtil.assertRte;
import static ceri.common.test.AssertUtil.assertTrue;
import org.junit.Test;
import ceri.common.function.FunctionTestUtil.Std;

public class IntBinaryPredicateBehavior {

	@Test
	public void shouldNegateTest() {
		IntBinaryPredicate p0 = Std.intBinaryPredicate();
		IntBinaryPredicate p = p0.negate();
		assertTrue(p0.test(2, 2));
		assertFalse(p.test(2, 2));
		assertTrue(p.test(-2, 2));
		assertRte(() -> p.test(0, 2));
	}
}
