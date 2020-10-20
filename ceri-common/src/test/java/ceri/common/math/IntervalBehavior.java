package ceri.common.math;

import static ceri.common.math.Bound.exclusive;
import static ceri.common.math.Bound.inclusive;
import static ceri.common.test.AssertUtil.assertAllNotEqual;
import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertFalse;
import static ceri.common.test.AssertUtil.assertTrue;
import static ceri.common.test.TestUtil.exerciseEquals;
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
		assertEquals(IntervalUtil.width(i), 0.0);
		assertEquals(IntervalUtil.midPoint(i), -1.0);
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
		assertEquals(IntervalUtil.width(i), Double.POSITIVE_INFINITY);
		assertEquals(IntervalUtil.midPoint(i), 0.0);
		assertTrue(i.contains(0.0));
		assertTrue(i.contains(Double.MAX_VALUE));
		assertTrue(i.contains(-Double.MAX_VALUE));
	}

	@Test
	public void shouldEncapsulateLowerBoundedIntervals() {
		Interval<Double> i = Interval.lower(exclusive(1.0));
		assertFalse(i.isUnbound());
		assertFalse(i.isEmpty());
		assertEquals(IntervalUtil.width(i), Double.POSITIVE_INFINITY);
		assertEquals(IntervalUtil.midPoint(i), Double.POSITIVE_INFINITY);
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
		assertEquals(IntervalUtil.width(i), Double.POSITIVE_INFINITY);
		assertEquals(IntervalUtil.midPoint(i), Double.NEGATIVE_INFINITY);
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
		assertEquals(IntervalUtil.width(i), 8.5);
		assertEquals(IntervalUtil.midPoint(i), -5.75);
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
		assertEquals(IntervalUtil.width(i), 8.5);
		assertEquals(IntervalUtil.midPoint(i), -5.75);
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
		assertTrue(Interval.point("xxx", comparator).contains("aaa"));
		assertFalse(Interval.point("xxx", comparator).contains("xx"));
		assertFalse(Interval.point("xxx", comparator).contains("xxxx"));
		assertTrue(Interval.exclusive("xxx", "xxxxx", comparator).contains("xxxx"));
		assertFalse(Interval.exclusive("xxx", "xxxxx", comparator).contains("xxx"));
		assertFalse(Interval.exclusive("xxx", "xxxxx", comparator).contains("xxxxx"));
	}

	@Test
	public void shouldProvideStringRepresentation() {
		assertEquals(Interval.unbound().toString(), "(\u221e)"); // (infinity);
		assertEquals(Interval.point(3.33).toString(), "[3.33]");
		assertEquals(Interval.lower(Bound.inclusive(3.33)).toString(), "[3.33, \u221e)");
		assertEquals(Interval.lower(Bound.exclusive(3.33)).toString(), "(3.33, \u221e)");
		assertEquals(Interval.upper(Bound.inclusive(3.33)).toString(), "(-\u221e, 3.33]");
		assertEquals(Interval.upper(Bound.exclusive(3.33)).toString(), "(-\u221e, 3.33)");
		assertEquals(Interval.inclusive(-2, -3).toString(), "[-2, -3]");
		assertEquals(Interval.exclusive(-2, -3).toString(), "(-2, -3)");
	}

}
