package ceri.common.math;

import static ceri.common.test.AssertUtil.assertAllNotEqual;
import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertFalse;
import static ceri.common.test.AssertUtil.assertNull;
import static ceri.common.test.AssertUtil.assertThrown;
import static ceri.common.test.AssertUtil.assertTrue;
import java.util.Comparator;
import org.junit.Test;
import ceri.common.test.TestUtil;

public class IntervalBehavior {
	private final Interval<Long> maxLong = Interval.inclusive(Long.MIN_VALUE, Long.MAX_VALUE);
	private final Interval<Integer> maxInt =
		Interval.inclusive(Integer.MIN_VALUE, Integer.MAX_VALUE);

	@Test
	public void testLongMidPoint() {
		assertEquals(Interval.longMidPoint(Interval.unbound()), 0L);
		assertNull(Interval.longMidPoint(Interval.lower(Bound.exclusive(1L))));
		assertEquals(Interval.longMidPoint(Interval.inclusive(1L, 4L)), 3L);
		assertEquals(Interval.longMidPoint(maxLong), 0L);
	}

	@Test
	public void testLongWidth() {
		assertNull(Interval.longWidth(Interval.unbound()));
		assertNull(Interval.longWidth(Interval.lower(Bound.exclusive(1L))));
		assertEquals(Interval.longWidth(Interval.inclusive(1L, 4L)), 3L);
		assertThrown(() -> Interval.longWidth(maxLong));
	}

	@Test
	public void testIntMidPoint() {
		assertEquals(Interval.intMidPoint(Interval.unbound()), 0);
		assertNull(Interval.intMidPoint(Interval.lower(Bound.exclusive(1))));
		assertEquals(Interval.intMidPoint(Interval.inclusive(1, 4)), 3);
		assertEquals(Interval.intMidPoint(maxInt), 0);
	}

	@Test
	public void testIntWidth() {
		assertNull(Interval.intWidth(Interval.unbound()));
		assertNull(Interval.intWidth(Interval.lower(Bound.exclusive(1))));
		assertEquals(Interval.intWidth(Interval.inclusive(1, 4)), 3);
		assertThrown(() -> Interval.intWidth(maxInt));
	}
	
	@Test
	public void shouldNotBreachEqualsContract() {
		Interval<Double> i = Interval.of(Bound.exclusive(-10.0), Bound.inclusive(-1.5));
		Interval<Double> eq0 = Interval.of(Bound.exclusive(-10.0), Bound.inclusive(-1.5));
		Interval<Double> ne0 = Interval.of(Bound.inclusive(-10.0), Bound.inclusive(-1.5));
		Interval<Double> ne1 = Interval.of(Bound.exclusive(-10.0), Bound.exclusive(-1.5));
		Interval<Double> ne2 = Interval.of(Bound.inclusive(-10.0), Bound.exclusive(-1.5));
		Interval<Double> ne3 = Interval.of(Bound.exclusive(-11.0), Bound.inclusive(-1.5));
		Interval<Double> ne4 = Interval.of(Bound.exclusive(-10.0), Bound.inclusive(-1.0));
		TestUtil.exerciseEquals(i, eq0);
		assertAllNotEqual(i, ne0, ne1, ne2, ne3, ne4);
	}

	@Test
	public void shouldDetermineInvertedBoundsAreEmpty() {
		assertTrue(Interval.of(Bound.inclusive(1), Bound.inclusive(-1)).isEmpty());
	}

	@Test
	public void shouldEncapsulateAPoint() {
		Interval<Double> i = Interval.point(-1.0);
		assertFalse(i.isUnbound());
		assertFalse(i.isEmpty());
		assertEquals(Interval.width(i), 0.0);
		assertEquals(Interval.midPoint(i), -1.0);
		assertFalse(i.contains(-1.001));
		assertTrue(i.contains(-1.0));
		assertFalse(i.contains(-0.999));
		assertTrue(Interval.of(Bound.inclusive(0), Bound.exclusive(0)).isEmpty());
		assertTrue(Interval.of(Bound.exclusive(0), Bound.inclusive(0)).isEmpty());
		assertTrue(Interval.of(Bound.exclusive(0), Bound.exclusive(0)).isEmpty());
	}

	@Test
	public void shouldEncapsulateUnboundInterval() {
		Interval<Double> i = Interval.unbound();
		assertTrue(i.isUnbound());
		assertFalse(i.isEmpty());
		assertEquals(Interval.width(i), Double.POSITIVE_INFINITY);
		assertEquals(Interval.midPoint(i), 0.0);
		assertTrue(i.contains(0.0));
		assertTrue(i.contains(Double.MAX_VALUE));
		assertTrue(i.contains(-Double.MAX_VALUE));
	}

	@Test
	public void shouldEncapsulateLowerBoundedIntervals() {
		Interval<Double> i = Interval.lower(Bound.exclusive(1.0));
		assertFalse(i.isUnbound());
		assertFalse(i.isEmpty());
		assertEquals(Interval.width(i), Double.POSITIVE_INFINITY);
		assertEquals(Interval.midPoint(i), Double.POSITIVE_INFINITY);
		assertFalse(i.contains(0.0));
		assertFalse(i.contains(1.0));
		assertTrue(i.contains(1.001));
		assertTrue(i.contains(Double.MAX_VALUE));
		assertFalse(i.contains(-Double.MAX_VALUE));
	}

	@Test
	public void shouldEncapsulateUpperBoundedIntervals() {
		Interval<Double> i = Interval.upper(Bound.inclusive(1.0));
		assertFalse(i.isUnbound());
		assertFalse(i.isEmpty());
		assertEquals(Interval.width(i), Double.POSITIVE_INFINITY);
		assertEquals(Interval.midPoint(i), Double.NEGATIVE_INFINITY);
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
		assertEquals(Interval.width(i), 8.5);
		assertEquals(Interval.midPoint(i), -5.75);
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
		assertEquals(Interval.width(i), 8.5);
		assertEquals(Interval.midPoint(i), -5.75);
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
