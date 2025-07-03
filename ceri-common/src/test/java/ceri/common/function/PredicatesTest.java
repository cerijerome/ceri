package ceri.common.function;

import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertFalse;
import static ceri.common.test.AssertUtil.assertPrivateConstructor;
import static ceri.common.test.AssertUtil.assertSame;
import static ceri.common.test.AssertUtil.assertTrue;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import org.junit.Test;
import ceri.common.collection.ArrayUtil;

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
	public void testByName() {
		assertFalse(Predicates.<T>name("ab").test(null));
		assertFalse(Predicates.<T>name("ab").test(T.abc));
		assertTrue(Predicates.<T>name("ab").test(T.ab));
	}

	@Test
	public void testTesting() {
		Predicate<String> filter = Predicates.testing(String::length, Predicates.gt(3));
		assertFalse(filter.test(null));
		assertTrue(filter.test("test"));
	}

	@Test
	public void testTestingInt() {
		Predicate<String> filter = Predicates.testingInt(String::length, i -> i > 0);
		assertFalse(filter.test(null));
		assertFalse(filter.test(""));
		assertTrue(filter.test("test"));
	}

	@Test
	public void testTestingLong() {
		Predicate<String> filter = Predicates.testingLong(String::length, l -> l > 0);
		assertFalse(filter.test(null));
		assertFalse(filter.test(""));
		assertTrue(filter.test("test"));
	}

	@Test
	public void testTestingDouble() {
		Predicate<String> filter = Predicates.testingDouble(String::length, d -> d > 0.0);
		assertFalse(filter.test(null));
		assertFalse(filter.test(""));
		assertTrue(filter.test("test"));
	}

	@Test
	public void testLower() {
		assertTrue(Predicates.lower(null).test("aaa"));
		Predicate<String> filter = Predicates.lower(Predicates.eqAny("a"));
		assertTrue(filter.test("A"));
		assertTrue(filter.test("a"));
		assertFalse(filter.test(null));
	}

	@Test
	public void testTrue() {
		assertSame(Predicates.yes(), Predicates.yes()); // same instance
		assertTrue(Predicates.yes().test(null));
		assertTrue(Predicates.yes().test(false));
		assertTrue(Predicates.yes().test(true));
		assertTrue(Predicates.yes().test(Double.NaN));
		assertTrue(Predicates.yes().test(Double.NEGATIVE_INFINITY));
		assertTrue(Predicates.yes().test(Long.MIN_VALUE));
	}

	@Test
	public void testFalse() {
		assertSame(Predicates.no(), Predicates.no()); // same instance
		assertFalse(Predicates.no().test(null));
		assertFalse(Predicates.no().test(false));
		assertFalse(Predicates.no().test(true));
		assertFalse(Predicates.nonNull(null).test(true));
	}

	@Test
	public void testNull() {
		assertTrue(Predicates.isNull().test(null));
		assertFalse(Predicates.isNull().test(""));
	}

	@Test
	public void testEqIgnoreCase() {
		assertTrue(Predicates.eqIgnoreCase(null).test(null));
		assertFalse(Predicates.eqIgnoreCase("").test(null));
		assertFalse(Predicates.eqIgnoreCase(null).test(""));
		Predicate<String> filter = Predicates.eqIgnoreCase("aBc");
		assertTrue(filter.test("aBc"));
		assertTrue(filter.test("abc"));
		assertTrue(filter.test("ABC"));
		assertFalse(filter.test("aBcd"));
	}

	@Test
	public void testEq() {
		Predicate<Integer> filter = Predicates.eqAny(-1, 1);
		assertTrue(filter.test(1));
		assertTrue(filter.test(-1));
		assertFalse(filter.test(0));
		assertFalse(filter.test(null));
	}

	@Test
	public void testNot() {
		assertFalse(Predicates.not(Predicates.yes()).test(null));
		Predicate<Integer> filter = Predicates.not(Predicates.eq(0));
		assertTrue(filter.test(1));
		assertFalse(filter.test(0));
		assertTrue(filter.test(-1));
		assertFalse(Predicates.not(null).test(true));
	}

	@Test
	public void testAll() {
		assertTrue(Predicates.all().test(true));
		Predicate<Double> filter1 = Predicates.eq(1.0);
		Predicate<Double> filter2 = Predicates.not(Predicates.eq(null));
		Collection<Predicate<Double>> filters = Arrays.asList(filter1, filter2);
		Predicate<Double> fAll0 = Predicates.all(filters);
		Predicate<Double> fAll1 = Predicates.all(filter1, filter2);
		assertTrue(fAll0.test(1.0));
		assertFalse(fAll0.test(null));
		assertFalse(fAll0.test(Double.NaN));
		assertTrue(fAll1.test(1.0));
		assertFalse(fAll1.test(null));
		assertFalse(fAll1.test(Double.NaN));
	}

	@Test
	public void testAny() {
		assertFalse(Predicates.any().test(false));
		Predicate<Double> filter1 = Predicates.eq(1.0);
		Predicate<Double> filter2 = Predicates.eq(null);
		Collection<Predicate<Double>> filters = Arrays.asList(filter1, filter2);
		Predicate<Double> fAll0 = Predicates.any(filters);
		Predicate<Double> fAll1 = Predicates.any(filter1, filter2);
		assertTrue(fAll0.test(1.0));
		assertFalse(fAll0.test(null));
		assertFalse(fAll0.test(Double.NaN));
		assertTrue(fAll1.test(1.0));
		assertFalse(fAll1.test(null));
		assertFalse(fAll1.test(Double.NaN));
	}

	@Test
	public void testContains() {
		assertTrue(Predicates.contains(null).test("aaa"));
		assertTrue(Predicates.contains("").test("aaa"));
		assertTrue(Predicates.containsIgnoreCase(null).test("aaa"));
		assertTrue(Predicates.containsIgnoreCase("").test("aaa"));
		Predicate<String> filter = Predicates.containsIgnoreCase("hElLo");
		assertTrue(filter.test("...hello there..."));
		assertTrue(filter.test("HELLO"));
		assertFalse(filter.test("H ELLO"));
		assertFalse(filter.test(null));
		filter = Predicates.contains("hElLo");
		assertTrue(filter.test("hElLo ThErE"));
		assertFalse(filter.test("hello"));
	}

	@Test
	public void testGt() {
		assertTrue(Predicates.<Long>gt(null).test(0L));
		Predicate<Long> filter = Predicates.gt(0L);
		assertTrue(filter.test(Long.MAX_VALUE));
		assertFalse(filter.test(0L));
		assertFalse(filter.test(Long.MIN_VALUE));
	}

	@Test
	public void testGte() {
		assertTrue(Predicates.<String>gte(null).test("aaa"));
		Predicate<String> filter = Predicates.gte("N");
		assertTrue(filter.test("N"));
		assertTrue(filter.test("N "));
		assertTrue(filter.test("NNN"));
		assertFalse(filter.test("MZZZZZZZZZ"));
		assertFalse(filter.test(""));
		assertFalse(filter.test(null));
	}

	@Test
	public void testLt() {
		assertTrue(Predicates.<Long>lt(null).test(0L));
		Predicate<Long> filter = Predicates.lt(0L);
		assertTrue(filter.test(Long.MIN_VALUE));
		assertFalse(filter.test(0L));
		assertFalse(filter.test(Long.MAX_VALUE));
	}

	@Test
	public void testLte() {
		assertTrue(Predicates.<String>lte(null).test("aaa"));
		Predicate<String> filter = Predicates.lte("N");
		assertTrue(filter.test("N"));
		assertFalse(filter.test("N "));
		assertFalse(filter.test("NNN"));
		assertTrue(filter.test("MZZZZZZZZZ"));
		assertTrue(filter.test(""));
		assertFalse(filter.test(null));
	}

	@Test
	public void testRange() {
		Predicate<Integer> filter = Predicates.range(null, null);
		assertTrue(filter.test(null));
		assertTrue(filter.test(0));
		assertTrue(filter.test(Integer.MAX_VALUE));
		assertTrue(filter.test(Integer.MIN_VALUE));
		filter = Predicates.range(null, -10);
		assertFalse(filter.test(null));
		assertFalse(filter.test(0));
		assertFalse(filter.test(Integer.MAX_VALUE));
		assertTrue(filter.test(-10));
		assertTrue(filter.test(Integer.MIN_VALUE));
		filter = Predicates.range(10, null);
		assertFalse(filter.test(null));
		assertFalse(filter.test(0));
		assertTrue(filter.test(Integer.MAX_VALUE));
		assertTrue(filter.test(10));
		assertFalse(filter.test(Integer.MIN_VALUE));
		filter = Predicates.range(-1, 1);
		assertFalse(filter.test(null));
		assertTrue(filter.test(-1));
		assertTrue(filter.test(0));
		assertTrue(filter.test(1));
		assertFalse(filter.test(Integer.MAX_VALUE));
		assertFalse(filter.test(Integer.MIN_VALUE));
	}

	@Test
	public void testPattern() {
		assertTrue(Predicates.pattern("").test("aaa"));
		assertTrue(Predicates.pattern((Pattern) null).test("aaa"));
		Predicate<String> filter = Predicates.pattern("(?i)^a.*z$");
		assertTrue(filter.test("ABC..XYZ"));
		assertTrue(filter.test("aAzZ"));
		assertFalse(filter.test("zaza"));
		assertFalse(filter.test(""));
		assertFalse(filter.test(null));
	}

	@Test
	public void testRemove() {
		Predicate<Character> filter = Predicates.eq('A');
		List<Character> items = ArrayUtil.asList('A', null, 'B', 'a', null, 'A', 'b');
		Predicates.remove(items, filter);
		assertEquals(items, Arrays.asList('A', 'A'));
	}

	@Test
	public void testPredicate() {
		assertTrue(Predicates.contains("ABC").test("xyzABCdef"));
		assertFalse(Predicates.contains("ABC").test("xyzA BCdef"));
	}

	@Test
	public void testForSize() {
		Predicate<Collection<Integer>> filter = Predicates.forSize(Predicates.eq(1));
		assertFalse(filter.test(Collections.emptyList()));
		assertTrue(filter.test(Collections.singleton(0)));
		assertFalse(filter.test(Arrays.asList(-1, 1)));
		assertFalse(filter.test(null));
	}

	@Test
	public void testForIndex() {
		Predicate<List<String>> filter = Predicates.forIndex(1, Predicates.lte("B"));
		assertTrue(filter.test(Arrays.asList("C", "A", "C")));
		assertFalse(filter.test(Arrays.asList("A", "C", "A")));
		filter = Predicates.forIndex(2, Predicates.gte("B"));
		assertFalse(filter.test(Arrays.asList("C", "A")));
		assertFalse(filter.test(Arrays.asList("A", "C")));
	}

	@Test
	public void testForAll() {
		Predicate<Collection<Boolean>> filter = Predicates.forAll(Predicates.eq(true));
		assertTrue(filter.test(Collections.emptyList()));
		assertFalse(filter.test(Arrays.asList(false)));
		assertTrue(filter.test(Arrays.asList(true)));
		assertFalse(filter.test(Arrays.asList(false, false)));
		assertFalse(filter.test(Arrays.asList(true, false)));
		assertFalse(filter.test(Arrays.asList(false, true)));
		assertTrue(filter.test(Arrays.asList(true, true)));
	}

	@Test
	public void testForAny() {
		Predicate<Collection<Boolean>> filter = Predicates.forAny(Predicates.eq(true));
		assertFalse(filter.test(Collections.emptyList()));
		assertFalse(filter.test(Arrays.asList(false)));
		assertTrue(filter.test(Arrays.asList(true)));
		assertFalse(filter.test(Arrays.asList(false, false)));
		assertTrue(filter.test(Arrays.asList(true, false)));
		assertTrue(filter.test(Arrays.asList(false, true)));
		assertTrue(filter.test(Arrays.asList(true, true)));
	}

}
