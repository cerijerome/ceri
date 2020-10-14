package ceri.common.function;

import static ceri.common.test.TestUtil.assertThrown;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static ceri.common.test.TestUtil.assertThat;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import ceri.common.function.FunctionTestUtil.Std;

public class ObjIntPredicateBehavior {

	@Test
	public void shouldNegateTest() {
		ObjIntPredicate<Integer> p0 = Std.objIntPredicate();
		ObjIntPredicate<Integer> p = p0.negate();
		assertThat(p0.test(2, 2), is(true));
		assertThat(p.test(2, 2), is(false));
		assertThat(p.test(-2, 2), is(true));
		assertThrown(RuntimeException.class, () -> p.test(0, 2));
	}

	@Test
	public void shouldLogicallyOrTests() {
		ObjIntPredicate<Integer> p = Std.objIntPredicate().or((i, j) -> i < -1);
		assertTrue(p.test(2, 2));
		assertTrue(p.test(-2, 2));
		assertFalse(p.test(-1, 2));
		assertThrown(RuntimeException.class, () -> p.test(0, 2));
	}

	@Test
	public void shouldLogicallyAndTests() {
		ObjIntPredicate<Integer> p = Std.objIntPredicate().and((i, j) -> i < 3);
		assertTrue(p.test(2, 2));
		assertFalse(p.test(3, 2));
		assertFalse(p.test(-1, 2));
		assertThrown(RuntimeException.class, () -> p.test(0, 2));
	}

}
