package ceri.common.function;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import ceri.common.collect.Sets;
import ceri.common.test.Assert;
import ceri.common.text.Strings;

public class FiltersTest {
	private static final String nullStr = null;
	private static final Integer[] nullArr = null;
	private static final Set<Integer> nullSet = null;

	@Test
	public void testConstructorIsPrivate() {
		Assert.privateConstructor(Filters.class);
	}

	@Test
	public void testNullsSafe() {
		Assert.equal(Filters.Nulls.safe(null), Filters.Nulls.fail);
		Assert.equal(Filters.Nulls.safe(Filters.Nulls.none), Filters.Nulls.fail);
		Assert.equal(Filters.Nulls.safe(Filters.Nulls.no), Filters.Nulls.no);
		Assert.equal(Filters.Nulls.safe(Filters.Nulls.yes), Filters.Nulls.yes);
		Assert.equal(Filters.Nulls.safe(Filters.Nulls.fail), Filters.Nulls.fail);
	}

	@Test
	public void testCast() {
		Excepts.Predicate<IllegalArgumentException, Object> p0 = o -> !String.valueOf(o).isEmpty();
		Excepts.Predicate<RuntimeException, String> p = Filters.cast(p0);
		assertTest(p, true, nullStr);
		assertTest(p, false, "");
	}

	@Test
	public void testCastInt() throws IOException {
		Functions.IntPredicate p0 = n -> n > 0;
		Excepts.IntPredicate<IOException> p = Filters.castIntEx(p0);
		assertTest(p, true, 1, 2);
		assertTest(p, false, -1, 0);
	}

	@Test
	public void testCastLong() throws IOException {
		Functions.LongPredicate p0 = n -> n > 0;
		Excepts.LongPredicate<IOException> p = Filters.castLongEx(p0);
		assertTest(p, true, 1, 2);
		assertTest(p, false, -1, 0);
	}

	@Test
	public void testCastDouble() throws IOException {
		Functions.DoublePredicate p0 = n -> n > 0;
		Excepts.DoublePredicate<IOException> p = Filters.castDoubleEx(p0);
		assertTest(p, true, 1, 2);
		assertTest(p, false, -1, 0);
	}

	@Test
	public void testCastBi() throws IOException {
		Excepts.BiPredicate<IOException, String, Integer> p = Filters.castEx(Strings.Filter.EQUALS);
		Assert.equal(p.test("1", 1), true);
		Assert.equal(p.test("1", 0), false);
	}

	@Test
	public void testCastObjBool() throws IOException {
		Functions.ObjBoolPredicate<String> p0 = Strings.Filter.EQUALS::test;
		Excepts.ObjBoolPredicate<IOException, String> p = Filters.castEx(p0);
		Assert.equal(p.test("true", true), true);
		Assert.equal(p.test("true", false), false);
	}

	@Test
	public void testCastObjByte() throws IOException {
		Functions.ObjBytePredicate<String> p0 = Strings.Filter.EQUALS::test;
		Excepts.ObjBytePredicate<IOException, String> p = Filters.castEx(p0);
		Assert.equal(p.test("1", (byte) 1), true);
		Assert.equal(p.test("1", (byte) 0), false);
	}

	@Test
	public void testCastObjInt() throws IOException {
		Functions.ObjIntPredicate<String> p0 = Strings.Filter.EQUALS::test;
		Excepts.ObjIntPredicate<IOException, String> p = Filters.castEx(p0);
		Assert.equal(p.test("1", 1), true);
		Assert.equal(p.test("1", 0), false);
	}

	@Test
	public void testCastObjLong() throws IOException {
		Functions.ObjLongPredicate<String> p0 = Strings.Filter.EQUALS::test;
		Excepts.ObjLongPredicate<IOException, String> p = Filters.castEx(p0);
		Assert.equal(p.test("1", 1), true);
		Assert.equal(p.test("1", 0), false);
	}

	@Test
	public void testCastObjDouble() throws IOException {
		Functions.ObjDoublePredicate<String> p0 = Strings.Filter.EQUALS::test;
		Excepts.ObjDoublePredicate<IOException, String> p = Filters.castEx(p0);
		Assert.equal(p.test("1.0", 1), true);
		Assert.equal(p.test("1.0", 0), false);
	}

	@Test
	public void testEx() throws IOException {
		Excepts.Predicate<RuntimeException, Object> p0 = o -> !String.valueOf(o).isEmpty();
		Excepts.Predicate<IOException, String> p = Filters.castEx(p0);
		assertTest(p, true, nullStr);
		assertTest(p, false, "");
	}

	@Test
	public void testTestAny() {
		Assert.equal(Filters.testAnyOf(null, 1, Filters.gt(0), Filters.lt(0)), true);
		Assert.equal(Filters.testAnyOf(null, 1, Filters.gt(1), Filters.lt(0)), false);
	}

	@Test
	public void testTestAll() {
		Assert.equal(Filters.testAllOf(null, 1, Filters.gt(0), Filters.lt(0)), false);
		Assert.equal(Filters.testAllOf(null, 1, Filters.gt(0), Filters.lt(2)), true);
	}

	@Test
	public void testTestAnyElement() {
		Assert.equal(Filters.testAnyElement(null, 1, List.of(-1, 1), null), false);
		Assert.equal(Filters.testAnyElement(null, 1, null, Filters.EQUALS), false);
		Assert.equal(Filters.testAnyElement(null, 1, List.of(-1, 1), Filters.EQUALS), true);
		Assert.equal(Filters.testAnyElement(null, 1, List.of(-1, 0), Filters.EQUALS), false);
	}

	@Test
	public void testTestAllElements() {
		Assert.equal(Filters.testAllElements(null, 1, List.of(-1, 1), null), false);
		Assert.equal(Filters.testAllElements(null, 1, null, Filters.EQUALS), false);
		Assert.equal(Filters.testAllElements(null, 1, List.of(1, 1), Filters.EQUALS), true);
		Assert.equal(Filters.testAllElements(null, 1, List.of(1, 0), Filters.EQUALS), false);
	}

	@Test
	public void testEqualAny() throws Exception {
		assertTest(Filters.equalAnyOf(nullArr), false, null, 0, 1);
		assertTest(Filters.equalAnyOf(), false, null, 0, 1);
		assertTest(Filters.equalAnyOf(-1, 1), true, -1, 1);
		assertTest(Filters.equalAnyOf(-1, 1), false, null, 0);
		assertTest(Filters.equalAny(nullSet), false, null, 0, 1);
		assertTest(Filters.equalAny(Sets.ofAll(1, 2)), false, null, 0);
		assertTest(Filters.equalAny(Sets.ofAll(1, 2)), true, 1, 2);
	}

	@Test
	public void testEqualPrimitive() throws Exception {
		assertTest(Filters.equal(1), true, 1);
		assertTest(Filters.equal(1), false, -1, 0);
		assertTest(Filters.equal(1L), true, 1);
		assertTest(Filters.equal(1L), false, -1, 0);
		assertTest(Filters.equal(1.0), true, 1);
		assertTest(Filters.equal(1.0), false, -1, 0);
	}

	@Test
	public void testInstance() throws Exception {
		assertTest(Filters.instance(null), true, nullStr);
		assertTest(Filters.instance(null), true, "");
		assertTest(Filters.instance(Object.class), false, nullStr);
		assertTest(Filters.instance(Object.class), true, "", 1);
		assertTest(Filters.instance(String.class), false, null, 1);
		assertTest(Filters.instance(String.class), true, "");
	}

	@Test
	public void testOfNullPredicate() throws Exception {
		assertTest(Filters.of(null), false, null, 1, "");
		assertTest(Filters.of(Filters.Nulls.yes, null), true, nullStr);
		assertTest(Filters.of(Filters.Nulls.yes, null), true, nullStr);
		assertTest(Filters.of(Filters.Nulls.yes, null), false, "", 1);
		assertTest(Filters.of(Filters.Nulls.no, null), false, null, "", 1);
		assertTest(Filters.of(Filters.Nulls.none, null), false, null, "", 1);
		Assert.nullPointer(() -> Filters.of(Filters.Nulls.fail, null).test(null));
		assertTest(Filters.of(Filters.Nulls.fail, null), false, "", 1);
	}

	@Test
	public void testOfPredicate() {
		assertTest(Filters.of(Filters.YES), false, nullStr);
		assertTest(Filters.of(Filters.YES), true, 1, "");
		assertTest(Filters.of(Filters.Nulls.yes, Filters.YES), true, null, "", 1);
		assertTest(Filters.of(Filters.Nulls.no, Filters.YES), false, nullStr);
		assertTest(Filters.of(Filters.Nulls.no, Filters.YES), true, "", 1);
		assertTest(Filters.of(Filters.Nulls.none, Filters.YES), true, null, "", 1);
		Assert.nullPointer(() -> Filters.of(Filters.Nulls.fail, Filters.YES).test(null));
		assertTest(Filters.of(Filters.Nulls.fail, Filters.YES), true, "", 1);
		assertTest(Filters.ofEx(Filters.YES.castEx()), false, nullStr);
		assertTest(Filters.ofEx(Filters.YES.castEx()), true, 1, "");
	}

	@Test
	public void testSafe() throws Exception {
		assertTest(Filters.safe(null), false, null, 1, "");
		assertTest(Filters.safe(Filters.YES), true, null, 1, "");
		assertTest(Filters.safeEx(null), false, null, 1, "");
		assertTest(Filters.safeEx(Filters.YES.castEx()), true, null, 1, "");
	}

	@Test
	public void testNot() throws Exception {
		Assert.equal(Filters.not(Filters.YES).test(null), false);
		var filter = Filters.not(Filters.equal((Integer) 0));
		var filterEx = filter.<IOException, Integer>castEx();
		assertTest(filter, true, 1, -1);
		assertTest(filter, false, 0);
		Assert.equal(Filters.not(null).test(true), false);
		assertTest(filterEx, true, 1, -1);
		assertTest(filterEx, false, 0);
		Assert.equal(Filters.notEx(null).test(true), false);
	}

	@Test
	public void testAs() throws Exception {
		assertTest(Filters.as(null, Filters.YES), false, nullStr, "", 1);
		assertTest(Filters.as(String::valueOf, null), false, nullStr, "", 1);
		assertTest(Filters.as(String::length, Filters.gt(3)), false, nullStr);
		assertTest(Filters.as(String::length, Filters.gt(3)), true, "test");
	}

	@Test
	public void testAsInt() throws IOException {
		Functions.ToIntFunction<String> accessor = String::length;
		Excepts.ToIntFunction<IOException, String> accessorEx = String::length;
		Functions.IntPredicate gt0 = i -> i > 0;
		Excepts.IntPredicate<IOException> gt0Ex = Filters.castIntEx(gt0);
		assertTest(Filters.asInt(null, _ -> true), false, null, "");
		assertTest(Filters.asInt(accessor, null), false, null, "");
		assertTest(Filters.asInt(accessor, gt0), false, null, "");
		assertTest(Filters.asInt(accessor, gt0), true, "test");
		assertTest(Filters.asIntEx(null, _ -> true), false, null, "");
		assertTest(Filters.asIntEx(accessorEx, null), false, null, "");
		assertTest(Filters.asIntEx(accessorEx, gt0Ex), false, null, "");
		assertTest(Filters.asIntEx(accessorEx, gt0Ex), true, "test");
	}

	@Test
	public void testAsLong() throws IOException {
		Functions.ToLongFunction<String> accessor = String::length;
		Excepts.ToLongFunction<IOException, String> accessorEx = String::length;
		Functions.LongPredicate gt0 = i -> i > 0;
		Excepts.LongPredicate<IOException> gt0Ex = Filters.castLongEx(gt0);
		assertTest(Filters.asLong(null, _ -> true), false, null, "");
		assertTest(Filters.asLong(accessor, null), false, null, "");
		assertTest(Filters.asLong(accessor, gt0), false, null, "");
		assertTest(Filters.asLong(accessor, gt0), true, "test");
		assertTest(Filters.asLongEx(null, _ -> true), false, null, "");
		assertTest(Filters.asLongEx(accessorEx, null), false, null, "");
		assertTest(Filters.asLongEx(accessorEx, gt0Ex), false, null, "");
		assertTest(Filters.asLongEx(accessorEx, gt0Ex), true, "test");
	}

	@Test
	public void testAsDouble() throws IOException {
		Functions.ToDoubleFunction<String> accessor = String::length;
		Excepts.ToDoubleFunction<IOException, String> accessorEx = String::length;
		Functions.DoublePredicate gt0 = i -> i > 0;
		Excepts.DoublePredicate<IOException> gt0Ex = Filters.castDoubleEx(gt0);
		assertTest(Filters.asDouble(null, _ -> true), false, null, "");
		assertTest(Filters.asDouble(accessor, null), false, null, "");
		assertTest(Filters.asDouble(accessor, gt0), false, null, "");
		assertTest(Filters.asDouble(accessor, gt0), true, "test");
		assertTest(Filters.asDoubleEx(null, _ -> true), false, null, "");
		assertTest(Filters.asDoubleEx(accessorEx, null), false, null, "");
		assertTest(Filters.asDoubleEx(accessorEx, gt0Ex), false, null, "");
		assertTest(Filters.asDoubleEx(accessorEx, gt0Ex), true, "test");
	}

	@Test
	public void testBiAs() {
		Functions.Function<String, Integer> a1 = String::length;
		Functions.Function<String, Integer> a2 = s -> Strings.trim(s).length();
		Functions.BinPredicate<Integer> bp = (i, j) -> i - j == 0;
		assertTest(Filters.biAs(null, a2, bp), false, null, "", " ", "a");
		assertTest(Filters.biAs(a1, null, bp), false, null, "", " ", "a");
		assertTest(Filters.biAs(a1, a2, null), false, null, "", " ", "a");
		assertTest(Filters.biAs(a1, a2, bp), false, null, " ", "a ");
		assertTest(Filters.biAs(a1, a2, bp), true, "", "a b");
		assertTest(Filters.biAs(Filters.Nulls.yes, a1, a2, bp), false, " ", "a ");
		assertTest(Filters.biAs(Filters.Nulls.yes, a1, a2, bp), true, null, "", "a b");
		assertTest(Filters.biAs(Filters.Nulls.none, a1, a2, bp), false, " ", "a ");
		assertTest(Filters.biAs(Filters.Nulls.none, a1, a2, bp), true, "", "a b");
		Assert.thrown(() -> Filters.biAs(Filters.Nulls.none, a1, a2, bp).test(null));
		assertTest(Filters.biAs(Filters.Nulls.fail, a1, a2, bp), false, " ", "a ");
		assertTest(Filters.biAs(Filters.Nulls.fail, a1, a2, bp), true, "", "a b");
		Assert.nullPointer(() -> Filters.biAs(Filters.Nulls.fail, a1, a2, bp).test(null));
	}

	@Test
	public void testAnd() throws Exception {
		Functions.Predicate<Double> filter1 = Filters.equal((Double) 1.0);
		Excepts.Predicate<IOException, Double> filter1Ex = filter1.castEx();
		Functions.Predicate<Object> filter2 = Filters.NON_NULL;
		Excepts.Predicate<IOException, Object> filter2Ex = filter2.castEx();
		assertTest(Filters.andOf(), true, nullStr);
		assertTest(Filters.andOf(), true, "");
		assertTest(Filters.andOf(null, filter2), false, 1.0);
		assertTest(Filters.andOf(filter1, filter2), true, 1.0);
		assertTest(Filters.andOf(filter1, filter2), false, null, Double.NaN);
		assertTest(Filters.andOfEx(), true, nullStr);
		assertTest(Filters.andOfEx(), true, "");
		assertTest(Filters.andOfEx(null, filter2Ex), false, 1.0);
		assertTest(Filters.andOfEx(filter1Ex, filter2Ex), true, 1.0);
		assertTest(Filters.andOfEx(filter1Ex, filter2Ex), false, null, Double.NaN);
	}

	@Test
	public void testOr() throws Exception {
		Functions.Predicate<Double> filter1 = Filters.equal((Double) 1.0);
		Excepts.Predicate<IOException, Double> filter1Ex = filter1.castEx();
		Functions.Predicate<Object> filter2 = Filters.IS_NULL;
		Excepts.Predicate<IOException, Object> filter2Ex = filter2.castEx();
		assertTest(Filters.orOf(), false, nullStr);
		assertTest(Filters.orOf(), false, "");
		assertTest(Filters.orOf(null, filter2), false, 1.0);
		assertTest(Filters.orOf(filter1, filter2), true, 1.0);
		assertTest(Filters.orOf(filter1, filter2), false, null, Double.NaN);
		assertTest(Filters.orOfEx(), false, nullStr);
		assertTest(Filters.orOfEx(), false, "");
		assertTest(Filters.orOfEx(null, filter2Ex), false, 1.0);
		assertTest(Filters.orOfEx(filter1Ex, filter2Ex), true, 1.0);
		assertTest(Filters.orOfEx(filter1Ex, filter2Ex), false, null, Double.NaN);
	}

	@Test
	public void testGt() throws Exception {
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
	public void testLt() throws Exception {
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
			Assert.equal(predicate.test(value), result, "testing %s", value);
	}

	private static <E extends Exception> void assertTest(Excepts.IntPredicate<E> predicate,
		boolean result, int... values) throws E {
		for (var value : values)
			Assert.equal(predicate.test(value), result, "testing %s", value);
	}

	private static <E extends Exception> void assertTest(Excepts.LongPredicate<E> predicate,
		boolean result, long... values) throws E {
		for (var value : values)
			Assert.equal(predicate.test(value), result, "testing %s", value);
	}

	private static <E extends Exception> void assertTest(Excepts.DoublePredicate<E> predicate,
		boolean result, double... values) throws E {
		for (var value : values)
			Assert.equal(predicate.test(value), result, "testing %s", value);
	}
}
