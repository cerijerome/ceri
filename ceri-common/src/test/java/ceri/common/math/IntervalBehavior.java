package ceri.common.math;

import java.util.Comparator;
import org.junit.Test;
import ceri.common.test.Assert;
import ceri.common.test.TestUtil;

public class IntervalBehavior {
	private final Interval<Long> maxLong = Interval.inclusive(Long.MIN_VALUE, Long.MAX_VALUE);
	private final Interval<Integer> maxInt =
		Interval.inclusive(Integer.MIN_VALUE, Integer.MAX_VALUE);

	@Test
	public void testLongMidPoint() {
		Assert.equal(Interval.longMidPoint(Interval.unbound()), 0L);
		Assert.isNull(Interval.longMidPoint(Interval.lower(Bound.exclusive(1L))));
		Assert.equal(Interval.longMidPoint(Interval.inclusive(1L, 4L)), 3L);
		Assert.equal(Interval.longMidPoint(maxLong), 0L);
	}

	@Test
	public void testLongWidth() {
		Assert.isNull(Interval.longWidth(Interval.unbound()));
		Assert.isNull(Interval.longWidth(Interval.lower(Bound.exclusive(1L))));
		Assert.equal(Interval.longWidth(Interval.inclusive(1L, 4L)), 3L);
		Assert.thrown(() -> Interval.longWidth(maxLong));
	}

	@Test
	public void testIntMidPoint() {
		Assert.equal(Interval.intMidPoint(Interval.unbound()), 0);
		Assert.isNull(Interval.intMidPoint(Interval.lower(Bound.exclusive(1))));
		Assert.equal(Interval.intMidPoint(Interval.inclusive(1, 4)), 3);
		Assert.equal(Interval.intMidPoint(maxInt), 0);
	}

	@Test
	public void testIntWidth() {
		Assert.isNull(Interval.intWidth(Interval.unbound()));
		Assert.isNull(Interval.intWidth(Interval.lower(Bound.exclusive(1))));
		Assert.equal(Interval.intWidth(Interval.inclusive(1, 4)), 3);
		Assert.thrown(() -> Interval.intWidth(maxInt));
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
		Assert.notEqualAll(i, ne0, ne1, ne2, ne3, ne4);
	}

	@Test
	public void shouldDetermineInvertedBoundsAreEmpty() {
		Assert.yes(Interval.of(Bound.inclusive(1), Bound.inclusive(-1)).isEmpty());
	}

	@Test
	public void shouldEncapsulateAPoint() {
		Interval<Double> i = Interval.point(-1.0);
		Assert.no(i.isUnbound());
		Assert.no(i.isEmpty());
		Assert.equal(Interval.width(i), 0.0);
		Assert.equal(Interval.midPoint(i), -1.0);
		Assert.no(i.contains(-1.001));
		Assert.yes(i.contains(-1.0));
		Assert.no(i.contains(-0.999));
		Assert.yes(Interval.of(Bound.inclusive(0), Bound.exclusive(0)).isEmpty());
		Assert.yes(Interval.of(Bound.exclusive(0), Bound.inclusive(0)).isEmpty());
		Assert.yes(Interval.of(Bound.exclusive(0), Bound.exclusive(0)).isEmpty());
	}

	@Test
	public void shouldEncapsulateUnboundInterval() {
		Interval<Double> i = Interval.unbound();
		Assert.yes(i.isUnbound());
		Assert.no(i.isEmpty());
		Assert.equal(Interval.width(i), Double.POSITIVE_INFINITY);
		Assert.equal(Interval.midPoint(i), 0.0);
		Assert.yes(i.contains(0.0));
		Assert.yes(i.contains(Double.MAX_VALUE));
		Assert.yes(i.contains(-Double.MAX_VALUE));
	}

	@Test
	public void shouldEncapsulateLowerBoundedIntervals() {
		Interval<Double> i = Interval.lower(Bound.exclusive(1.0));
		Assert.no(i.isUnbound());
		Assert.no(i.isEmpty());
		Assert.equal(Interval.width(i), Double.POSITIVE_INFINITY);
		Assert.equal(Interval.midPoint(i), Double.POSITIVE_INFINITY);
		Assert.no(i.contains(0.0));
		Assert.no(i.contains(1.0));
		Assert.yes(i.contains(1.001));
		Assert.yes(i.contains(Double.MAX_VALUE));
		Assert.no(i.contains(-Double.MAX_VALUE));
	}

	@Test
	public void shouldEncapsulateUpperBoundedIntervals() {
		Interval<Double> i = Interval.upper(Bound.inclusive(1.0));
		Assert.no(i.isUnbound());
		Assert.no(i.isEmpty());
		Assert.equal(Interval.width(i), Double.POSITIVE_INFINITY);
		Assert.equal(Interval.midPoint(i), Double.NEGATIVE_INFINITY);
		Assert.yes(i.contains(0.0));
		Assert.yes(i.contains(1.0));
		Assert.no(i.contains(1.001));
		Assert.no(i.contains(Double.MAX_VALUE));
		Assert.yes(i.contains(-Double.MAX_VALUE));
	}

	@Test
	public void shouldEncapsulateInclusiveBoundedIntervals() {
		Interval<Double> i = Interval.inclusive(-10.0, -1.5);
		Assert.no(i.isUnbound());
		Assert.no(i.isEmpty());
		Assert.equal(Interval.width(i), 8.5);
		Assert.equal(Interval.midPoint(i), -5.75);
		Assert.yes(i.contains(-9.999));
		Assert.yes(i.contains(-10.0));
		Assert.no(i.contains(-10.001));
		Assert.yes(i.contains(-1.501));
		Assert.yes(i.contains(-1.5));
		Assert.no(i.contains(-1.499));
	}

	@Test
	public void shouldEncapsulateExclusiveBoundedIntervals() {
		Interval<Double> i = Interval.exclusive(-10.0, -1.5);
		Assert.no(i.isUnbound());
		Assert.no(i.isEmpty());
		Assert.equal(Interval.width(i), 8.5);
		Assert.equal(Interval.midPoint(i), -5.75);
		Assert.yes(i.contains(-9.999));
		Assert.no(i.contains(-10.0));
		Assert.no(i.contains(-10.001));
		Assert.yes(i.contains(-1.501));
		Assert.no(i.contains(-1.5));
		Assert.no(i.contains(-1.499));
	}

	@Test
	public void shouldAllowCustomComparators() {
		Comparator<String> comparator = Comparator.comparing(s -> s.length());
		Assert.yes(Interval.point("xxx", comparator).contains("aaa"));
		Assert.no(Interval.point("xxx", comparator).contains("xx"));
		Assert.no(Interval.point("xxx", comparator).contains("xxxx"));
		Assert.yes(Interval.exclusive("xxx", "xxxxx", comparator).contains("xxxx"));
		Assert.no(Interval.exclusive("xxx", "xxxxx", comparator).contains("xxx"));
		Assert.no(Interval.exclusive("xxx", "xxxxx", comparator).contains("xxxxx"));
	}

	@Test
	public void shouldProvideStringRepresentation() {
		Assert.equal(Interval.unbound().toString(), "(\u221e)"); // (infinity);
		Assert.equal(Interval.point(3.33).toString(), "[3.33]");
		Assert.equal(Interval.lower(Bound.inclusive(3.33)).toString(), "[3.33, \u221e)");
		Assert.equal(Interval.lower(Bound.exclusive(3.33)).toString(), "(3.33, \u221e)");
		Assert.equal(Interval.upper(Bound.inclusive(3.33)).toString(), "(-\u221e, 3.33]");
		Assert.equal(Interval.upper(Bound.exclusive(3.33)).toString(), "(-\u221e, 3.33)");
		Assert.equal(Interval.inclusive(-2, -3).toString(), "[-2, -3]");
		Assert.equal(Interval.exclusive(-2, -3).toString(), "(-2, -3)");
	}
}
