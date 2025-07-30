package ceri.common.function;

import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertFalse;
import static ceri.common.test.AssertUtil.assertNotEquals;
import static ceri.common.test.AssertUtil.assertTrue;
import java.util.function.Function;
import java.util.function.IntPredicate;
import org.junit.Test;

public class LambdasTest {

	@Test
	public void testIsAnon() {
		assertFalse(Lambdas.isAnon(null));
		assertFalse(Lambdas.isAnon(new Object() {
			@Override
			public String toString() {
				return null;
			}
		}));
		assertFalse(Lambdas.isAnon(new Object()));
		IntPredicate p = _ -> true;
		assertTrue(Lambdas.isAnon(p));
	}

	@Test
	public void testLambda() {
		Function<?, ?> fn = i -> i;
		assertEquals(Lambdas.name(fn), "[lambda]");
		assertNotEquals(Lambdas.name(this), "[lambda]");
	}
}
