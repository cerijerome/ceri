package ceri.common.function;

import java.util.Iterator;
import java.util.function.IntPredicate;
import org.junit.Test;
import ceri.common.test.Assert;

public class LambdasTest {

	public interface NotLambda1 {
		int apply(int i);
		long apply(long l);
	}

	public interface NotLambda2 {
		int apply(int i);
		int apply(int i, int j);
	}

	public interface NotLambda3 {
		int apply(int i);
		int apply(long l);
	}

	@Test
	public void testType() {
		Functions.IntBiOperator op = (i1, i2) -> i1 + i2;
		Assert.equal(Lambdas.type(null), null);
		Assert.equal(Lambdas.type(new Object()), null);
		Assert.equal(Lambdas.type(""), Comparable.class); // or Constable, ConstantDesc
		Assert.equal(Lambdas.type(op), Functions.IntBiOperator.class);
	}

	@Test
	public void testMethod() {
		Assert.equal(Lambdas.method(String.class), null);
		Assert.equal(Lambdas.method(Iterator.class), null);
		Assert.equal(Lambdas.method(NotLambda1.class), null);
		Assert.equal(Lambdas.method(NotLambda2.class), null);
		Assert.equal(Lambdas.method(NotLambda3.class), null);
		Assert.equal(Lambdas.method(Functions.Function.class).getName(), "apply");
	}

	@Test
	public void testRegister() {
		Functions.Function<?, ?> fn = i -> i;
		Assert.equal(Lambdas.register(null, "null"), null);
		Assert.equal(Lambdas.registered(null), null);
		Assert.equal(Lambdas.registered(fn), null);
		Assert.equal(Lambdas.register(fn, "fn"), fn);
		Assert.equal(Lambdas.registered(fn), "fn");
	}

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
	public void testName() {
		Functions.Function<?, ?> fn = i -> i;
		Assert.equal(Lambdas.name(fn), "[lambda]");
		Assert.notEqual(Lambdas.name(this), "[lambda]");
	}

	@Test
	public void testNameOrHash() {
		Functions.Function<?, ?> fn = i -> i;
		Assert.find(Lambdas.nameOrHash(1), "1");
		Assert.find(Lambdas.nameOrHash(fn), "Function#([0-9a-f]+)");
		Lambdas.register(fn, "fn");
		Assert.find(Lambdas.nameOrHash(fn), "fn");
	}
}
