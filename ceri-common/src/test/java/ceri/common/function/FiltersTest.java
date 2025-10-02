package ceri.common.function;

import static ceri.common.test.AssertUtil.assertFalse;
import static ceri.common.test.AssertUtil.assertPrivateConstructor;
import static ceri.common.test.AssertUtil.assertTrue;
import org.junit.Test;

public class FiltersTest {

	@Test
	public void testConstructorIsPrivate() {
		assertPrivateConstructor(Filters.class);
	}

	private enum T {
		a,
		ab,
		bc,
		abc
	}

	@Test
	public void testTesting() {
		var filter = Filters.testing(String::length, Filters.gt(3));
		assertFalse(filter.test(null));
		assertTrue(filter.test("test"));
	}

	@Test
	public void testTestingInt() {
		var filter = Filters.testingInt(String::length, i -> i > 0);
		assertFalse(filter.test(null));
		assertFalse(filter.test(""));
		assertTrue(filter.test("test"));
	}

	@Test
	public void testTestingLong() {
		var filter = Filters.testingLong(String::length, l -> l > 0);
		assertFalse(filter.test(null));
		assertFalse(filter.test(""));
		assertTrue(filter.test("test"));
	}

	@Test
	public void testTestingDouble() {
		var filter = Filters.testingDouble(String::length, d -> d > 0.0);
		assertFalse(filter.test(null));
		assertFalse(filter.test(""));
		assertTrue(filter.test("test"));
	}

	@Test
	public void testEq() throws Exception {
		var filter = Filters.eqAny(-1, 1);
		assertTrue(filter.test(1));
		assertTrue(filter.test(-1));
		assertFalse(filter.test(0));
		assertFalse(filter.test(null));
	}

	@Test
	public void testNot() throws Exception {
		assertFalse(Filters.not(Filters.yes()).test(null));
		var filter = Filters.not(Filters.eq(0));
		assertTrue(filter.test(1));
		assertFalse(filter.test(0));
		assertTrue(filter.test(-1));
		assertFalse(Filters.not(null).test(true));
	}

	@Test
	public void testAnd() throws Exception {
		assertTrue(Filters.and().test(true));
		var filter1 = Filters.eq(1.0);
		var filter2 = Filters.not(Filters.eq(null));
		var fAll = Filters.and(filter1, filter2);
		assertTrue(fAll.test(1.0));
		assertFalse(fAll.test(null));
		assertFalse(fAll.test(Double.NaN));
	}

}
