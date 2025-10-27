package ceri.common.function;

import java.util.function.Function;
import java.util.function.IntPredicate;
import org.junit.Test;
import ceri.common.test.Assert;

public class LambdasTest {

	@Test
	public void testIsAnon() {
		Assert.no(Lambdas.isAnon(null));
		Assert.no(Lambdas.isAnon(new Object() {
			@Override
			public String toString() {
				return null;
			}
		}));
		Assert.no(Lambdas.isAnon(new Object()));
		IntPredicate p = _ -> true;
		Assert.yes(Lambdas.isAnon(p));
	}

	@Test
	public void testLambda() {
		Function<?, ?> fn = i -> i;
		Assert.equal(Lambdas.name(fn), "[lambda]");
		Assert.notEqual(Lambdas.name(this), "[lambda]");
	}
}
