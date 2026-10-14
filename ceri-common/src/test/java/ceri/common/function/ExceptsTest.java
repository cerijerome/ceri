package ceri.common.function;

import java.io.IOException;
import org.junit.Test;
import ceri.common.test.Assert;

public class ExceptsTest {

	@Test
	public void testConstructorIsPrivate() {
		Assert.privateConstructor(Excepts.class);
	}

	@Test
	public void testPredicate() throws IOException {
		Excepts.Predicate<IOException, Number> p0 = t -> String.valueOf(t).length() > 1;
		Excepts.Predicate<IOException, Integer> p1 = p0.cast();
		Assert.equal(p0.test(1), false);
		Assert.equal(p1.test(1), false);
		Assert.equal(p0.test(10), true);
		Assert.equal(p1.test(10), true);
	}

	@Test
	public void testBiPredicate() throws IOException {
		Excepts.BiPredicate<IOException, Number, Number> p0 =
			(t, u) -> String.valueOf(t).length() == String.valueOf(u).length();
		Excepts.BiPredicate<IOException, Integer, Double> p1 = p0.cast();
		Assert.equal(p0.test(1, 1.0), false);
		Assert.equal(p1.test(1, 1.0), false);
		Assert.equal(p0.test(100, 1.0), true);
		Assert.equal(p1.test(100, 1.0), true);
	}

	@Test
	public void testObjBoolPredicate() throws IOException {
		Excepts.ObjBoolPredicate<IOException, Number> p0 =
			(t, u) -> String.valueOf(t).length() == String.valueOf(u).length();
		Excepts.ObjBoolPredicate<IOException, Integer> p1 = p0.cast();
		Assert.equal(p0.test(1, true), false);
		Assert.equal(p1.test(1, true), false);
		Assert.equal(p0.test(1000, true), true);
		Assert.equal(p1.test(1000, true), true);
	}

	@Test
	public void testObjBytePredicate() throws IOException {
		Excepts.ObjBytePredicate<IOException, Number> p0 =
			(t, u) -> String.valueOf(t).length() == String.valueOf(u).length();
		Excepts.ObjBytePredicate<IOException, Integer> p1 = p0.cast();
		Assert.equal(p0.test(10, (byte) 1), false);
		Assert.equal(p1.test(10, (byte) 1), false);
		Assert.equal(p0.test(2, (byte) 1), true);
		Assert.equal(p1.test(2, (byte) 1), true);
	}

	@Test
	public void testObjIntPredicate() throws IOException {
		Excepts.ObjIntPredicate<IOException, Number> p0 =
			(t, u) -> String.valueOf(t).length() == String.valueOf(u).length();
		Excepts.ObjIntPredicate<IOException, Integer> p1 = p0.cast();
		Assert.equal(p0.test(10, 1), false);
		Assert.equal(p1.test(10, 1), false);
		Assert.equal(p0.test(2, 1), true);
		Assert.equal(p1.test(2, 1), true);
	}

	@Test
	public void testObjLongPredicate() throws IOException {
		Excepts.ObjLongPredicate<IOException, Number> p0 =
			(t, u) -> String.valueOf(t).length() == String.valueOf(u).length();
		Excepts.ObjLongPredicate<IOException, Integer> p1 = p0.cast();
		Assert.equal(p0.test(10, 1), false);
		Assert.equal(p1.test(10, 1), false);
		Assert.equal(p0.test(2, 1), true);
		Assert.equal(p1.test(2, 1), true);
	}

	@Test
	public void testObjDoublePredicate() throws IOException {
		Excepts.ObjDoublePredicate<IOException, Number> p0 =
			(t, u) -> String.valueOf(t).length() == String.valueOf(u).length();
		Excepts.ObjDoublePredicate<IOException, Integer> p1 = p0.cast();
		Assert.equal(p0.test(10, 1.0), false);
		Assert.equal(p1.test(10, 1.0), false);
		Assert.equal(p0.test(100, 1.0), true);
		Assert.equal(p1.test(100, 1.0), true);
	}
}
