package ceri.common.math;

import static ceri.common.math.Bound.exclusive;
import static ceri.common.math.Bound.inclusive;
import static ceri.common.test.TestUtil.assertAllNotEqual;
import static ceri.common.test.TestUtil.exerciseEquals;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static ceri.common.test.TestUtil.assertThat;
import static org.junit.Assert.assertTrue;
import java.util.Comparator;
import org.junit.Test;

public class IntervalBehavior {

	@Test
	public void shouldNotBreachEqualsContract() {
		Interval<Double> i = Interval.of(exclusive(-10.0), inclusive(-1.5));
		Interval<Double> eq0 = Interval.of(exclusive(-10.0), inclusive(-1.5));
		Interval<Double> ne0 = Interval.of(inclusive(-10.0), inclusive(-1.5));
		Interval<Double> ne1 = Interval.of(exclusive(-10.0), exclusive(-1.5));
		Interval<Double> ne2 = Interval.of(inclusive(-10.0), exclusive(-1.5));
		Interval<Double> ne3 = Interval.of(exclusive(-11.0), inclusive(-1.5));
		Interval<Double> ne4 = Interval.of(exclusive(-10.0), inclusive(-1.0));
		exerciseEquals(i, eq0);
		assertAllNotEqual(i, ne0, ne1, ne2, ne3, ne4);
	}

	@Test
	public void shouldDetermineInvertedBoundsAreEmpty() {
		assertTrue(Interval.of(inclusive(1), inclusive(-1)).isEmpty());
	}

	@Test
	public void shouldEncapsulateAPoint() {
		Interval<Double> i = Interval.point(-1.0);
		assertFalse(i.isUnbound());
		assertFalse(i.isEmpty());
		assertThat(IntervalUtil.width(i), is(0.0));
		assertThat(IntervalUtil.midPoint(i), is(-1.0));
		assertFalse(i.contains(-1.001));
		assertTrue(i.contains(-1.0));
		assertFalse(i.contains(-0.999));
		assertTrue(Interval.of(inclusive(0), exclusive(0)).isEmpty());
		assertTrue(Interval.of(exclusive(0), inclusive(0)).isEmpty());
		assertTrue(Interval.of(exclusive(0), exclusive(0)).isEmpty());
	}

	@Test
	public void shouldEncapsulateUnboundInterval() {
		Interval<Double> i = Interval.unbound();
		assertTrue(i.isUnbound());
		assertFalse(i.isEmpty());
		assertThat(IntervalUtil.width(i), is(Double.POSITIVE_INFINITY));
		assertThat(IntervalUtil.midPoint(i), is(0.0));
		assertTrue(i.contains(0.0));
		assertTrue(i.contains(Double.MAX_VALUE));
		assertTrue(i.contains(-Double.MAX_VALUE));
	}

	@Test
	public void shouldEncapsulateLowerBoundedIntervals() {
		Interval<Double> i = Interval.lower(exclusive(1.0));
		assertFalse(i.isUnbound());
		assertFalse(i.isEmpty());
		assertThat(IntervalUtil.width(i), is(Double.POSITIVE_INFINITY));
		assertThat(IntervalUtil.midPoint(i), is(Double.POSITIVE_INFINITY));
		assertFalse(i.contains(0.0));
		assertFalse(i.contains(1.0));
		assertTrue(i.contains(1.001));
		assertTrue(i.contains(Double.MAX_VALUE));
		assertFalse(i.contains(-Double.MAX_VALUE));
	}

	@Test
	public void shouldEncapsulateUpperBoundedIntervals() {
		Interval<Double> i = Interval.upper(inclusive(1.0));
		assertFalse(i.isUnbound());
		assertFalse(i.isEmpty());
		assertThat(IntervalUtil.width(i), is(Double.POSITIVE_INFINITY));
		assertThat(IntervalUtil.midPoint(i), is(Double.NEGATIVE_INFINITY));
		assertTrue(i.contains(0.0));
		assertTrue(i.contains(1.0));
		assertFalse(i.contains(1.001));
		assertFalse(i.contains(Double.MAX_VALUE));
		assertTrue(i.contains(-Double.MAX_VALUE));
	}

	@Test
	public void shouldEncapsulateInclusiveBoundedIntervals() {
		Interval<Double> i = Interval.inclusive(-10.0, -1.5);
		assertFalse(i.isUnbound());
		assertFalse(i.isEmpty());
		assertThat(IntervalUtil.width(i), is(8.5));
		assertThat(IntervalUtil.midPoint(i), is(-5.75));
		assertTrue(i.contains(-9.999));
		assertTrue(i.contains(-10.0));
		assertFalse(i.contains(-10.001));
		assertTrue(i.contains(-1.501));
		assertTrue(i.contains(-1.5));
		assertFalse(i.contains(-1.499));
	}

	@Test
	public void shouldEncapsulateExclusiveBoundedIntervals() {
		Interval<Double> i = Interval.exclusive(-10.0, -1.5);
		assertFalse(i.isUnbound());
		assertFalse(i.isEmpty());
		assertThat(IntervalUtil.width(i), is(8.5));
		assertThat(IntervalUtil.midPoint(i), is(-5.75));
		assertTrue(i.contains(-9.999));
		assertFalse(i.contains(-10.0));
		assertFalse(i.contains(-10.001));
		assertTrue(i.contains(-1.501));
		assertFalse(i.contains(-1.5));
		assertFalse(i.contains(-1.499));
	}

	@Test
	public void shouldAllowCustomComparators() {
		Comparator<String> comparator = Comparator.comparing(s -> s.length());
		assertThat(Interval.point("xxx", comparator).contains("aaa"), is(true));
		assertThat(Interval.point("xxx", comparator).contains("xx"), is(false));
		assertThat(Interval.point("xxx", comparator).contains("xxxx"), is(false));
		assertThat(Interval.exclusive("xxx", "xxxxx", comparator).contains("xxxx"), is(true));
		assertThat(Interval.exclusive("xxx", "xxxxx", comparator).contains("xxx"), is(false));
		assertThat(Interval.exclusive("xxx", "xxxxx", comparator).contains("xxxxx"), is(false));
	}

	@Test
	public void shouldProvideStringRepresentation() {
		assertThat(Interval.unbound().toString(), is("(\u221e)")); // (infinity)
		assertThat(Interval.point(3.33).toString(), is("[3.33]"));
		assertThat(Interval.lower(Bound.inclusive(3.33)).toString(), is("[3.33, \u221e)"));
		assertThat(Interval.lower(Bound.exclusive(3.33)).toString(), is("(3.33, \u221e)"));
		assertThat(Interval.upper(Bound.inclusive(3.33)).toString(), is("(-\u221e, 3.33]"));
		assertThat(Interval.upper(Bound.exclusive(3.33)).toString(), is("(-\u221e, 3.33)"));
		assertThat(Interval.inclusive(-2, -3).toString(), is("[-2, -3]"));
		assertThat(Interval.exclusive(-2, -3).toString(), is("(-2, -3)"));
	}

}
