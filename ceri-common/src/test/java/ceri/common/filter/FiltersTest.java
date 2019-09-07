package ceri.common.filter;

import static ceri.common.test.TestUtil.assertPrivateConstructor;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;
import org.junit.Test;
import ceri.common.collection.ArrayUtil;

public class FiltersTest {

	@Test
	public void testConstructorIsPrivate() {
		assertPrivateConstructor(Filters.class);
	}

	@Test
	public void testTransform() {
		Filter<String> filter = Filters.transform(Filters.gt(3), String::length);
		assertFalse(filter.filter(null));
		assertTrue(filter.filter("test"));
	}

	@Test
	public void testLower() {
		assertTrue(Filters.lower(null).filter("aaa"));
		Filter<String> filter = Filters.lower(Filters.eqAny("a"));
		assertTrue(filter.filter("A"));
		assertTrue(filter.filter("a"));
		assertFalse(filter.filter(null));
	}

	@Test
	public void testTrue() {
		assertSame(Filters._true(), Filters._true()); // same instance
		assertTrue(Filters._true().filter(null));
		assertTrue(Filters._true().filter(false));
		assertTrue(Filters._true().filter(true));
		assertTrue(Filters._true().filter(Double.NaN));
		assertTrue(Filters._true().filter(Double.NEGATIVE_INFINITY));
		assertTrue(Filters._true().filter(Long.MIN_VALUE));
	}

	@Test
	public void testFalse() {
		assertSame(Filters._false(), Filters._false()); // same instance
		assertFalse(Filters._false().filter(null));
		assertFalse(Filters._false().filter(false));
		assertFalse(Filters._false().filter(true));
		assertFalse(Filters.nonNull(null).filter(true));
	}

	@Test
	public void testNull() {
		assertTrue(Filters._null().filter(null));
		assertFalse(Filters._null().filter(""));
	}

	@Test
	public void testEqIgnoreCase() {
		assertTrue(Filters.eqIgnoreCase(null).filter(null));
		assertFalse(Filters.eqIgnoreCase("").filter(null));
		assertFalse(Filters.eqIgnoreCase(null).filter(""));
		Filter<String> filter = Filters.eqIgnoreCase("aBc");
		assertTrue(filter.filter("aBc"));
		assertTrue(filter.filter("abc"));
		assertTrue(filter.filter("ABC"));
		assertFalse(filter.filter("aBcd"));
	}

	@Test
	public void testEq() {
		Filter<Integer> filter = Filters.eqAny(-1, 1);
		assertTrue(filter.filter(1));
		assertTrue(filter.filter(-1));
		assertFalse(filter.filter(0));
		assertFalse(filter.filter(null));
	}

	@Test
	public void testNot() {
		assertFalse(Filters.not(Filters._true()).filter(null));
		Filter<Integer> filter = Filters.not(Filters.eq(0));
		assertTrue(filter.filter(1));
		assertFalse(filter.filter(0));
		assertTrue(filter.filter(-1));
		assertFalse(Filters.not(null).filter(true));
	}

	@Test
	public void testAll() {
		assertTrue(Filters.all().filter(true));
		Filter<Double> filter1 = Filters.eq(1.0);
		Filter<Double> filter2 = Filters.not(Filters.eq(null));
		Collection<Filter<Double>> filters = Arrays.asList(filter1, filter2);
		Filter<Double> fAll0 = Filters.all(filters);
		Filter<Double> fAll1 = Filters.all(filter1, filter2);
		assertTrue(fAll0.filter(1.0));
		assertFalse(fAll0.filter(null));
		assertFalse(fAll0.filter(Double.NaN));
		assertTrue(fAll1.filter(1.0));
		assertFalse(fAll1.filter(null));
		assertFalse(fAll1.filter(Double.NaN));
	}

	@Test
	public void testAny() {
		assertTrue(Filters.any().filter(false));
		Filter<Double> filter1 = Filters.eq(1.0);
		Filter<Double> filter2 = Filters.eq(null);
		Collection<Filter<Double>> filters = Arrays.asList(filter1, filter2);
		Filter<Double> fAll0 = Filters.any(filters);
		Filter<Double> fAll1 = Filters.any(filter1, filter2);
		assertTrue(fAll0.filter(1.0));
		assertFalse(fAll0.filter(null));
		assertFalse(fAll0.filter(Double.NaN));
		assertTrue(fAll1.filter(1.0));
		assertFalse(fAll1.filter(null));
		assertFalse(fAll1.filter(Double.NaN));
	}

	@Test
	public void testContains() {
		assertTrue(Filters.contains(null).filter("aaa"));
		assertTrue(Filters.contains("").filter("aaa"));
		assertTrue(Filters.containsIgnoreCase(null).filter("aaa"));
		assertTrue(Filters.containsIgnoreCase("").filter("aaa"));
		Filter<String> filter = Filters.containsIgnoreCase("hElLo");
		assertTrue(filter.filter("...hello there..."));
		assertTrue(filter.filter("HELLO"));
		assertFalse(filter.filter("H ELLO"));
		assertFalse(filter.filter(null));
		filter = Filters.contains("hElLo");
		assertTrue(filter.filter("hElLo ThErE"));
		assertFalse(filter.filter("hello"));
	}

	@Test
	public void testGt() {
		assertTrue(Filters.<Long>gt(null).filter(0L));
		Filter<Long> filter = Filters.gt(0L);
		assertTrue(filter.filter(Long.MAX_VALUE));
		assertFalse(filter.filter(0L));
		assertFalse(filter.filter(Long.MIN_VALUE));
	}

	@Test
	public void testGte() {
		assertTrue(Filters.<String>gte(null).filter("aaa"));
		Filter<String> filter = Filters.gte("N");
		assertTrue(filter.filter("N"));
		assertTrue(filter.filter("N "));
		assertTrue(filter.filter("NNN"));
		assertFalse(filter.filter("MZZZZZZZZZ"));
		assertFalse(filter.filter(""));
		assertFalse(filter.filter(null));
	}

	@Test
	public void testLt() {
		assertTrue(Filters.<Long>lt(null).filter(0L));
		Filter<Long> filter = Filters.lt(0L);
		assertTrue(filter.filter(Long.MIN_VALUE));
		assertFalse(filter.filter(0L));
		assertFalse(filter.filter(Long.MAX_VALUE));
	}

	@Test
	public void testLte() {
		assertTrue(Filters.<String>lte(null).filter("aaa"));
		Filter<String> filter = Filters.lte("N");
		assertTrue(filter.filter("N"));
		assertFalse(filter.filter("N "));
		assertFalse(filter.filter("NNN"));
		assertTrue(filter.filter("MZZZZZZZZZ"));
		assertTrue(filter.filter(""));
		assertFalse(filter.filter(null));
	}

	@Test
	public void testRange() {
		Filter<Integer> filter = Filters.range(null, null);
		assertTrue(filter.filter(null));
		assertTrue(filter.filter(0));
		assertTrue(filter.filter(Integer.MAX_VALUE));
		assertTrue(filter.filter(Integer.MIN_VALUE));
		filter = Filters.range(null, -10);
		assertFalse(filter.filter(null));
		assertFalse(filter.filter(0));
		assertFalse(filter.filter(Integer.MAX_VALUE));
		assertTrue(filter.filter(-10));
		assertTrue(filter.filter(Integer.MIN_VALUE));
		filter = Filters.range(10, null);
		assertFalse(filter.filter(null));
		assertFalse(filter.filter(0));
		assertTrue(filter.filter(Integer.MAX_VALUE));
		assertTrue(filter.filter(10));
		assertFalse(filter.filter(Integer.MIN_VALUE));
		filter = Filters.range(-1, 1);
		assertFalse(filter.filter(null));
		assertTrue(filter.filter(-1));
		assertTrue(filter.filter(0));
		assertTrue(filter.filter(1));
		assertFalse(filter.filter(Integer.MAX_VALUE));
		assertFalse(filter.filter(Integer.MIN_VALUE));
	}

	@Test
	public void testPattern() {
		assertTrue(Filters.pattern("").filter("aaa"));
		assertTrue(Filters.pattern((Pattern) null).filter("aaa"));
		Filter<String> filter = Filters.pattern("(?i)^a.*z$");
		assertTrue(filter.filter("ABC..XYZ"));
		assertTrue(filter.filter("aAzZ"));
		assertFalse(filter.filter("zaza"));
		assertFalse(filter.filter(""));
		assertFalse(filter.filter(null));
	}

	@Test
	public void testRemove() {
		Filter<Character> filter = Filters.eq('A');
		List<Character> items = ArrayUtil.asList('A', null, 'B', 'a', null, 'A', 'b');
		Filters.remove(items, filter);
		assertThat(items, is(Arrays.asList('A', 'A')));
	}

	@Test
	public void testPredicate() {
		assertTrue(Filters.contains("ABC").test("xyzABCdef"));
		assertFalse(Filters.contains("ABC").test("xyzA BCdef"));
	}

}
