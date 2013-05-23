package ceri.common.filter;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import org.junit.Test;
import ceri.common.collection.ArrayUtil;

public class FiltersTest {

	@Test
	public void testNul() {
		assertTrue(Filters.nul() == Filters.nul()); // same instance
		assertTrue(Filters.nul().filter(null));
		assertTrue(Filters.nul().filter(false));
		assertTrue(Filters.nul().filter(Double.NaN));
		assertTrue(Filters.nul().filter(Double.NEGATIVE_INFINITY));
		assertTrue(Filters.nul().filter(Long.MIN_VALUE));
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
		assertFalse(Filters.not(Filters.nul()).filter(null));
		Filter<Integer> filter = Filters.not(Filters.eq(0));
		assertTrue(filter.filter(1));
		assertFalse(filter.filter(0));
		assertTrue(filter.filter(-1));
	}

	@Test
	public void testAll() {
		Filter<Double> filter1 = Filters.eq(1.0);
		Filter<Comparable<?>> filter2 = Filters.not(Filters.eq(null));
		Collection<Filter<? super Double>> filters =
			Arrays.<Filter<? super Double>>asList(filter1, filter2);
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
		Filter<Double> filter1 = Filters.eq(1.0);
		Filter<Comparable<?>> filter2 = Filters.eq(null);
		Collection<Filter<? super Double>> filters =
			Arrays.<Filter<? super Double>>asList(filter1, filter2);
		Filter<Double> fAll0 = Filters.any(filters);
		Filter<Double> fAll1 = Filters.any(filter1, filter2);
		assertTrue(fAll0.filter(1.0));
		assertTrue(fAll0.filter(null));
		assertFalse(fAll0.filter(Double.NaN));
		assertTrue(fAll1.filter(1.0));
		assertTrue(fAll1.filter(null));
		assertFalse(fAll1.filter(Double.NaN));
	}

	@Test
	public void testContains() {
		Filter<String> filter = Filters.contains("hElLo", true);
		assertTrue(filter.filter("...hello there..."));
		assertTrue(filter.filter("HELLO"));
		assertFalse(filter.filter("H ELLO"));
		assertFalse(filter.filter(null));
		filter = Filters.contains("hElLo", false);
		assertTrue(filter.filter("hElLo ThErE"));
		assertFalse(filter.filter("hello"));
	}

	@Test
	public void testMin() {
		Filter<String> filter = Filters.min("N");
		assertTrue(filter.filter("N"));
		assertTrue(filter.filter("N "));
		assertTrue(filter.filter("NNN"));
		assertFalse(filter.filter("MZZZZZZZZZ"));
		assertFalse(filter.filter(""));
		assertFalse(filter.filter(null));
	}

	@Test
	public void testMax() {
		Filter<String> filter = Filters.max("N");
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
		Filter<String> filter = Filters.pattern("(?i)^a.*z$");
		assertTrue(filter.filter("ABC..XYZ"));
		assertTrue(filter.filter("aAzZ"));
		assertFalse(filter.filter("zaza"));
		assertFalse(filter.filter(""));
		assertFalse(filter.filter(null));
	}

	@Test
	public void testFilter() {
		Filter<Character> filter = Filters.eq('A');
		List<Character> items = ArrayUtil.asList('A', 'B', 'a', 'A', 'b');
		Filters.filter(items, filter);
		assertThat(items, is(Arrays.asList('A', 'A')));
	}

}
