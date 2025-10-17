package ceri.common.function;

import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertPrivateConstructor;
import java.util.Set;
import org.junit.Test;
import ceri.common.text.Strings;

public class FiltersTest {
	private static final String nullStr = null;
	private static final Integer[] nullArr = null;
	private static final Set<Integer> nullSet = null;

	@Test
	public void testConstructorIsPrivate() {
		assertPrivateConstructor(Filters.class);
	}

	@Test
	public void testCast() {
		Excepts.Predicate<IllegalArgumentException, Object> p0 = o -> !String.valueOf(o).isEmpty();
		Excepts.Predicate<RuntimeException, String> p = Filters.cast(p0);
		assertTest(p, true, nullStr);
		assertTest(p, false, "");
	}

	@Test
	public void testEqualAny() {
		assertTest(Filters.equalAnyOf(nullArr), false, null, 0, 1);
		assertTest(Filters.equalAnyOf(), false, null, 0, 1);
		assertTest(Filters.equalAnyOf(-1, 1), true, -1, 1);
		assertTest(Filters.equalAnyOf(-1, 1), false, null, 0);
		assertTest(Filters.equalAny(nullSet), false, null, 0, 1);
	}

	@Test
	public void testEqualPrimitive() {
		assertTest(Filters.equal(1), true, 1);
		assertTest(Filters.equal(1), false, -1, 0);
		assertTest(Filters.equal(1L), true, 1);
		assertTest(Filters.equal(1L), false, -1, 0);
		assertTest(Filters.equal(1.0), true, 1);
		assertTest(Filters.equal(1.0), false, -1, 0);
	}

	@Test
	public void testInstance() {
		assertTest(Filters.instance(null), true, nullStr);
		assertTest(Filters.instance(null), true, "");
		assertTest(Filters.instance(Object.class), false, nullStr);
		assertTest(Filters.instance(Object.class), true, "", 1);
		assertTest(Filters.instance(String.class), false, null, 1);
		assertTest(Filters.instance(String.class), true, "");
	}

	@Test
	public void testNullYes() {
		assertTest(Filters.nullYes(null), true, nullStr);
		assertTest(Filters.nullYes(null), false, "", 1);
		assertTest(Filters.nullYes(Filters.equal("")), true, null, "");
		assertTest(Filters.nullYes(Filters.equal("")), false, " ");
	}

	@Test
	public void testNot() throws Exception {
		assertEquals(Filters.not(Filters.yes()).test(null), false);
		var filter = Filters.not(Filters.equal((Integer) 0));
		assertTest(filter, true, 1, -1);
		assertTest(filter, false, 0);
		assertEquals(Filters.not(null).test(true), false);
	}

	@Test
	public void testTesting() {
		assertTest(Filters.testing(null, Filters.yes()), false, nullStr, "", 1);
		assertTest(Filters.testing(String::valueOf, null), false, nullStr, "", 1);
		assertTest(Filters.testing(String::length, Filters.gt(3)), false, nullStr);
		assertTest(Filters.testing(String::length, Filters.gt(3)), true, "test");
	}

	@Test
	public void testTestingInt() {
		assertTest(Filters.testingInt(null, _ -> true), false, null, "");
		assertTest(Filters.testingInt(String::length, null), false, null, "");
		assertTest(Filters.testingInt(String::length, i -> i > 0), false, null, "");
		assertTest(Filters.testingInt(String::length, i -> i > 0), true, "test");
	}

	@Test
	public void testTestingLong() {
		assertTest(Filters.testingLong(null, _ -> true), false, null, "");
		assertTest(Filters.testingLong(String::length, null), false, null, "");
		assertTest(Filters.testingLong(String::length, l -> l > 0), false, null, "");
		assertTest(Filters.testingLong(String::length, l -> l > 0), true, "test");
	}

	@Test
	public void testTestingDouble() {
		assertTest(Filters.testingDouble(null, _ -> true), false, null, "");
		assertTest(Filters.testingDouble(String::length, null), false, null, "");
		assertTest(Filters.testingDouble(String::length, d -> d > 0.0), false, null, "");
		assertTest(Filters.testingDouble(String::length, d -> d > 0.0), true, "test");
	}

	@Test
	public void testBiTesting() {
		assertTest(Filters.biTesting(null, s -> Strings.trim(s).length(), (i, j) -> i == j), false,
			null, "", " ", "a");
		assertTest(Filters.biTesting(String::length, null, (i, j) -> i == j), false, null, "", " ",
			"a");
		assertTest(Filters.biTesting(String::length, s -> Strings.trim(s).length(), null), false,
			null, "", " ", "a");
		var bp = Filters.biTesting(String::length, s -> Strings.trim(s).length(), (i, j) -> i == j);
		assertTest(bp, false, null, " ", "a ");
		assertTest(bp, true, "", "a b");
	}

	@Test
	public void testAnd() throws Exception {
		var filter1 = Filters.equal((Double) 1.0);
		var filter2 = Filters.not(Filters.equal(null));
		assertTest(Filters.andOf(), false, nullStr);
		assertTest(Filters.andOf(), true, "");
		assertTest(Filters.andOf(filter1, filter2), true, 1.0);
		assertTest(Filters.andOf(filter1, filter2), false, null, Double.NaN);
		assertTest(Filters.andOf(null, filter2), false, 1.0);
	}

	@Test
	public void testOr() throws Exception {
		var filter1 = Filters.equal((Double) 1.0);
		var filter2 = Filters.lte(0.0);
		assertTest(Filters.orOf(), false, nullStr, "");
		assertTest(Filters.orOf(filter1, filter2), true, 1.0, -1.0);
		assertTest(Filters.orOf(filter1, filter2), false, null, Double.NaN);
		assertTest(Filters.orOf(null, filter2), false, 1.0);
	}

	@Test
	public void testGt() {
		assertTest(Filters.gte(null), false, nullStr);
		assertTest(Filters.gte(null), true, "", "b");
		assertTest(Filters.gte("c"), false, null, "", "b");
		assertTest(Filters.gte("c"), true, "c", "d");
		assertTest(Filters.gt(null), false, nullStr);
		assertTest(Filters.gt(null), true, "", "b");
		assertTest(Filters.gt("c"), false, null, "", "c");
		assertTest(Filters.gt("c"), true, "d", "e");
	}

	@Test
	public void testLt() {
		assertTest(Filters.lte(null), false, nullStr);
		assertTest(Filters.lte(null), true, "", "b");
		assertTest(Filters.lte("c"), false, null, "cc");
		assertTest(Filters.lte("c"), true, "", "c");
		assertTest(Filters.lt(null), false, nullStr);
		assertTest(Filters.lt(null), true, "", "b");
		assertTest(Filters.lt("c"), false, null, "c", "cc");
		assertTest(Filters.lt("c"), true, "", "b");
	}

	@SafeVarargs
	private static <E extends Exception, T> void assertTest(
		Excepts.Predicate<E, ? super T> predicate, boolean result, T... values) throws E {
		for (var value : values)
			assertEquals(predicate.test(value), result, "%s", value);
	}

	private static <E extends Exception> void assertTest(Excepts.IntPredicate<E> predicate,
		boolean result, int... values) throws E {
		for (var value : values)
			assertEquals(predicate.test(value), result, "%s", value);
	}

	private static <E extends Exception> void assertTest(Excepts.LongPredicate<E> predicate,
		boolean result, long... values) throws E {
		for (var value : values)
			assertEquals(predicate.test(value), result, "%s", value);
	}

	private static <E extends Exception> void assertTest(Excepts.DoublePredicate<E> predicate,
		boolean result, double... values) throws E {
		for (var value : values)
			assertEquals(predicate.test(value), result, "%s", value);
	}
}
