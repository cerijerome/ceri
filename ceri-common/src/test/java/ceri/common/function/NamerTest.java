package ceri.common.function;

import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertFalse;
import static ceri.common.test.AssertUtil.assertNotEquals;
import static ceri.common.test.AssertUtil.assertTrue;
import java.util.function.Function;
import java.util.function.IntPredicate;
import java.util.function.Predicate;
import org.junit.Test;

public class NamerTest {

	@Test
	public void testUnnamedLambda() {
		assertFalse(Namer.unnamedLambda(null));
		assertFalse(Namer.unnamedLambda(new Object() {
			@Override
			public String toString() {
				return null;
			}
		}));
		assertFalse(Namer.unnamedLambda(new Object()));
		IntPredicate p = i -> true;
		assertFalse(Namer.unnamedLambda(Namer.intPredicate(p, "test")));
		assertTrue(Namer.unnamedLambda(p));
	}

	@Test
	public void testLambda() {
		Function<?, ?> fn = i -> i;
		assertEquals(Namer.lambda(fn), "[lambda]");
		assertNotEquals(Namer.lambda(this), "[lambda]");
	}
	
	@Test
	public void testPredicate() {
		Predicate<String> p = Namer.predicate(s -> !s.isEmpty(), "test");
		assertFalse(p.test(""));
		assertTrue(p.test("abc"));
		assertEquals(p.toString(), "test");
	}

	@Test
	public void testIntPredicate() {
		IntPredicate p = Namer.intPredicate(i -> i > 0, "test");
		assertFalse(p.test(0));
		assertTrue(p.test(1));
		assertEquals(p.toString(), "test");
	}

}
