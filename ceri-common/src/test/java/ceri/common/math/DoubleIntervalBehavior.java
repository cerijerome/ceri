package ceri.common.math;

import static ceri.common.math.DoubleBound.exclusive;
import static ceri.common.math.DoubleBound.inclusive;
import static ceri.common.test.TestUtil.assertAllNotEqual;
import static ceri.common.test.TestUtil.exerciseEquals;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

public class DoubleIntervalBehavior {

	@Test
	public void shouldNotBreachEqualsContract() {
		DoubleInterval i = DoubleInterval.create(exclusive(-10), inclusive(-1.5));
		DoubleInterval eq0 = DoubleInterval.create(exclusive(-10), inclusive(-1.5));
		DoubleInterval ne0 = DoubleInterval.create(inclusive(-10), inclusive(-1.5));
		DoubleInterval ne1 = DoubleInterval.create(exclusive(-10), exclusive(-1.5));
		DoubleInterval ne2 = DoubleInterval.create(inclusive(-10), exclusive(-1.5));
		DoubleInterval ne3 = DoubleInterval.create(exclusive(-11), inclusive(-1.5));
		DoubleInterval ne4 = DoubleInterval.create(exclusive(-10), inclusive(-1));
		exerciseEquals(i, eq0);
		assertAllNotEqual(i, ne0, ne1, ne2, ne3, ne4);
	}

	@Test
	public void shouldDetermineInvertedBoundsAreEmpty() {
		assertTrue(DoubleInterval.create(inclusive(1), inclusive(-1)).empty());
	}

	@Test
	public void shouldEncapsulateAPoint() {
		DoubleInterval i = DoubleInterval.point(-1);
		assertFalse(i.unbound());
		assertFalse(i.empty());
		assertThat(i.width(), is(0.0));
		assertThat(i.midPoint(), is(-1.0));
		assertFalse(i.contains(-1.001));
		assertTrue(i.contains(-1));
		assertFalse(i.contains(-0.999));
		assertTrue(DoubleInterval.create(inclusive(0), exclusive(0)).empty());
		assertTrue(DoubleInterval.create(exclusive(0), inclusive(0)).empty());
		assertTrue(DoubleInterval.create(exclusive(0), exclusive(0)).empty());
	}

	@Test
	public void shouldEncapsulateUnboundInterval() {
		DoubleInterval i = DoubleInterval.UNBOUND;
		assertTrue(i.unbound());
		assertFalse(i.empty());
		assertThat(i.width(), is(Double.POSITIVE_INFINITY));
		assertThat(i.midPoint(), is(0.0));
		assertTrue(i.contains(0));
		assertTrue(i.contains(Double.MAX_VALUE));
		assertTrue(i.contains(-Double.MAX_VALUE));
	}

	@Test
	public void shouldEncapsulateLowerBoundedIntervals() {
		DoubleInterval i = DoubleInterval.lower(exclusive(1));
		assertFalse(i.unbound());
		assertFalse(i.empty());
		assertThat(i.width(), is(Double.POSITIVE_INFINITY));
		assertThat(i.midPoint(), is(Double.POSITIVE_INFINITY));
		assertFalse(i.contains(0));
		assertFalse(i.contains(1));
		assertTrue(i.contains(1.001));
		assertTrue(i.contains(Double.MAX_VALUE));
		assertFalse(i.contains(-Double.MAX_VALUE));
	}

	@Test
	public void shouldEncapsulateUpperBoundedIntervals() {
		DoubleInterval i = DoubleInterval.upper(inclusive(1));
		assertFalse(i.unbound());
		assertFalse(i.empty());
		assertThat(i.width(), is(Double.POSITIVE_INFINITY));
		assertThat(i.midPoint(), is(Double.NEGATIVE_INFINITY));
		assertTrue(i.contains(0));
		assertTrue(i.contains(1));
		assertFalse(i.contains(1.001));
		assertFalse(i.contains(Double.MAX_VALUE));
		assertTrue(i.contains(-Double.MAX_VALUE));
	}

	@Test
	public void shouldEncapsulateInclusiveBoundedIntervals() {
		DoubleInterval i = DoubleInterval.inclusive(-10, -1.5);
		assertFalse(i.unbound());
		assertFalse(i.empty());
		assertThat(i.width(), is(8.5));
		assertThat(i.midPoint(), is(-5.75));
		assertTrue(i.contains(-9.999));
		assertTrue(i.contains(-10));
		assertFalse(i.contains(-10.001));
		assertTrue(i.contains(-1.501));
		assertTrue(i.contains(-1.5));
		assertFalse(i.contains(-1.499));
	}

	@Test
	public void shouldEncapsulateExclusiveBoundedIntervals() {
		DoubleInterval i = DoubleInterval.exclusive(-10, -1.5);
		assertFalse(i.unbound());
		assertFalse(i.empty());
		assertThat(i.width(), is(8.5));
		assertThat(i.midPoint(), is(-5.75));
		assertTrue(i.contains(-9.999));
		assertFalse(i.contains(-10));
		assertFalse(i.contains(-10.001));
		assertTrue(i.contains(-1.501));
		assertFalse(i.contains(-1.5));
		assertFalse(i.contains(-1.499));
	}

}
