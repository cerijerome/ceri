package ceri.common.function;

import static ceri.common.function.FunctionTestUtil.objIntPredicate;
import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertFalse;
import static ceri.common.test.AssertUtil.assertIoe;
import static ceri.common.test.AssertUtil.assertRte;
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
		assertIoe(() -> p.test(1, 2));
		assertRte(() -> p.test(0, 2));
	}

	@Test
	public void shouldConvertToPredicate() {
		ObjIntPredicate<Integer> p = objIntPredicate().asPredicate();
		assertTrue(p.test(2, 2));
		assertRte(() -> p.test(1, 2));
		assertRte(() -> p.test(0, 2));
	}

	@Test
	public void shouldConvertFromPredicate() {
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
		assertIoe(() -> p.test(1, 2));
		assertRte(() -> p.test(0, 2));
	}

	@Test
	public void shouldLogicallyOrTests() throws IOException {
		ExceptionObjIntPredicate<IOException, Integer> p = objIntPredicate().or((i, _) -> i < -1);
		assertTrue(p.test(2, 2));
		assertTrue(p.test(-2, 2));
		assertFalse(p.test(-1, 2));
		assertIoe(() -> p.test(1, 2));
		assertRte(() -> p.test(0, 2));
	}

	@Test
	public void shouldLogicallyAndTests() throws IOException {
		ExceptionObjIntPredicate<IOException, Integer> p = objIntPredicate().and((i, _) -> i < 3);
		assertTrue(p.test(2, 2));
		assertFalse(p.test(3, 2));
		assertFalse(p.test(-1, 2));
		assertIoe(() -> p.test(1, 2));
		assertRte(() -> p.test(0, 2));
	}

}
