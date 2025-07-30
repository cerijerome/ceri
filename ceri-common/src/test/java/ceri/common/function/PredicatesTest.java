package ceri.common.function;

import static ceri.common.test.AssertUtil.assertFalse;
import static ceri.common.test.AssertUtil.assertPrivateConstructor;
import static ceri.common.test.AssertUtil.assertTrue;
import org.junit.Test;

public class PredicatesTest {

	@Test
	public void testConstructorIsPrivate() {
		assertPrivateConstructor(Predicates.class);
	}

	private enum T {
		a,
		ab,
		bc,
		abc
	}

	@Test
	public void testTesting() {
		var filter = Predicates.testing(String::length, Predicates.gt(3));
		assertFalse(filter.test(null));
		assertTrue(filter.test("test"));
	}

	@Test
	public void testTestingInt() {
		var filter = Predicates.testingInt(String::length, i -> i > 0);
		assertFalse(filter.test(null));
		assertFalse(filter.test(""));
		assertTrue(filter.test("test"));
	}

	@Test
	public void testTestingLong() {
		var filter = Predicates.testingLong(String::length, l -> l > 0);
		assertFalse(filter.test(null));
		assertFalse(filter.test(""));
		assertTrue(filter.test("test"));
	}

	@Test
	public void testTestingDouble() {
		var filter = Predicates.testingDouble(String::length, d -> d > 0.0);
		assertFalse(filter.test(null));
		assertFalse(filter.test(""));
		assertTrue(filter.test("test"));
	}

	@Test
	public void testEqNoCase() throws Exception {
		assertTrue(Predicates.eqIgnoreCase(null).test(null));
		assertFalse(Predicates.eqIgnoreCase("").test(null));
		assertFalse(Predicates.eqIgnoreCase(null).test(""));
		var filter = Predicates.eqIgnoreCase("aBc");
		assertTrue(filter.test("aBc"));
		assertTrue(filter.test("abc"));
		assertTrue(filter.test("ABC"));
		assertFalse(filter.test("aBcd"));
	}

	@Test
	public void testEq() {
		var filter = Predicates.eqAny(-1, 1);
		assertTrue(filter.test(1));
		assertTrue(filter.test(-1));
		assertFalse(filter.test(0));
		assertFalse(filter.test(null));
	}

	@Test
	public void testNot() throws Exception {
		assertFalse(Predicates.not(Predicates.yes()).test(null));
		var filter = Predicates.not(Predicates.eq(0));
		assertTrue(filter.test(1));
		assertFalse(filter.test(0));
		assertTrue(filter.test(-1));
		assertFalse(Predicates.not(null).test(true));
	}

	@Test
	public void testAnd() throws Exception {
		assertTrue(Predicates.and().test(true));
		var filter1 = Predicates.eq(1.0);
		var filter2 = Predicates.not(Predicates.eq(null));
		var fAll = Predicates.and(filter1, filter2);
		assertTrue(fAll.test(1.0));
		assertFalse(fAll.test(null));
		assertFalse(fAll.test(Double.NaN));
	}

}
