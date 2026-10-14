package ceri.common.function;

import java.io.IOException;
import org.junit.jupiter.api.Test;
import ceri.common.test.Assert;

public class FunctionsTest {

	@Test
	public void testConstructorIsPrivate() {
		Assert.privateConstructor(Functions.class);
	}

	@Test
	public void testPredicate() throws IOException {
		Functions.Predicate<Number> p0 = t -> String.valueOf(t).length() > 1;
		Functions.Predicate<Integer> p1 = p0.cast();
		Excepts.Predicate<IOException, Integer> p2 = p0.castEx();
		Assert.equal(p0.test(1), false);
		Assert.equal(p1.test(1), false);
		Assert.equal(p2.test(1), false);
		Assert.equal(p0.test(10), true);
		Assert.equal(p1.test(10), true);
		Assert.equal(p2.test(10), true);
	}

	@Test
	public void testBiPredicate() throws IOException {
		Functions.BiPredicate<Number, Number> p0 =
			(t, u) -> String.valueOf(t).length() == String.valueOf(u).length();
		Functions.BiPredicate<Integer, Double> p1 = p0.cast();
		Excepts.BiPredicate<IOException, Integer, Double> p2 = p0.castEx();
		Assert.equal(p0.test(1, 1.0), false);
		Assert.equal(p1.test(1, 1.0), false);
		Assert.equal(p2.test(1, 1.0), false);
		Assert.equal(p0.test(100, 1.0), true);
		Assert.equal(p1.test(100, 1.0), true);
		Assert.equal(p2.test(100, 1.0), true);
	}

	@Test
	public void testObjBoolPredicate() throws IOException {
		Functions.ObjBoolPredicate<Number> p0 =
			(t, u) -> String.valueOf(t).length() == String.valueOf(u).length();
		Functions.ObjBoolPredicate<Integer> p1 = p0.cast();
		Excepts.ObjBoolPredicate<IOException, Integer> p2 = p0.castEx();
		Assert.equal(p0.test(1, true), false);
		Assert.equal(p1.test(1, true), false);
		Assert.equal(p2.test(1, true), false);
		Assert.equal(p0.test(1000, true), true);
		Assert.equal(p1.test(1000, true), true);
		Assert.equal(p2.test(1000, true), true);
	}

	@Test
	public void testObjBytePredicate() throws IOException {
		Functions.ObjBytePredicate<Number> p0 =
			(t, u) -> String.valueOf(t).length() == String.valueOf(u).length();
		Functions.ObjBytePredicate<Integer> p1 = p0.cast();
		Excepts.ObjBytePredicate<IOException, Integer> p2 = p0.castEx();
		Assert.equal(p0.test(10, (byte) 1), false);
		Assert.equal(p1.test(10, (byte) 1), false);
		Assert.equal(p2.test(10, (byte) 1), false);
		Assert.equal(p0.test(2, (byte) 1), true);
		Assert.equal(p1.test(2, (byte) 1), true);
		Assert.equal(p2.test(2, (byte) 1), true);
	}

	@Test
	public void testObjIntPredicate() throws IOException {
		Functions.ObjIntPredicate<Number> p0 =
			(t, u) -> String.valueOf(t).length() == String.valueOf(u).length();
		Functions.ObjIntPredicate<Integer> p1 = p0.cast();
		Excepts.ObjIntPredicate<IOException, Integer> p2 = p0.castEx();
		Assert.equal(p0.test(10, 1), false);
		Assert.equal(p1.test(10, 1), false);
		Assert.equal(p2.test(10, 1), false);
		Assert.equal(p0.test(2, 1), true);
		Assert.equal(p1.test(2, 1), true);
		Assert.equal(p2.test(2, 1), true);
	}

	@Test
	public void testObjLongPredicate() throws IOException {
		Functions.ObjLongPredicate<Number> p0 =
			(t, u) -> String.valueOf(t).length() == String.valueOf(u).length();
		Functions.ObjLongPredicate<Integer> p1 = p0.cast();
		Excepts.ObjLongPredicate<IOException, Integer> p2 = p0.castEx();
		Assert.equal(p0.test(10, 1), false);
		Assert.equal(p1.test(10, 1), false);
		Assert.equal(p2.test(10, 1), false);
		Assert.equal(p0.test(2, 1), true);
		Assert.equal(p1.test(2, 1), true);
		Assert.equal(p2.test(2, 1), true);
	}

	@Test
	public void testObjDoublePredicate() throws IOException {
		Functions.ObjDoublePredicate<Number> p0 =
			(t, u) -> String.valueOf(t).length() == String.valueOf(u).length();
		Functions.ObjDoublePredicate<Integer> p1 = p0.cast();
		Excepts.ObjDoublePredicate<IOException, Integer> p2 = p0.castEx();
		Assert.equal(p0.test(10, 1.0), false);
		Assert.equal(p1.test(10, 1.0), false);
		Assert.equal(p2.test(10, 1.0), false);
		Assert.equal(p0.test(100, 1.0), true);
		Assert.equal(p1.test(100, 1.0), true);
		Assert.equal(p2.test(100, 1.0), true);
	}
}
