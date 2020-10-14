package ceri.common.math;

import static ceri.common.test.TestUtil.assertPrivateConstructor;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNull;
import static ceri.common.test.TestUtil.assertThat;
import org.junit.Test;
import ceri.common.test.TestUtil;

public class IntervalUtilTest {
	private final Interval<Long> maxLong = Interval.inclusive(Long.MIN_VALUE, Long.MAX_VALUE);
	private final Interval<Integer> maxInt =
		Interval.inclusive(Integer.MIN_VALUE, Integer.MAX_VALUE);

	@Test
	public void testConstructorIsPrivate() {
		assertPrivateConstructor(IntervalUtil.class);
	}

	@Test
	public void testLongMidPoint() {
		assertThat(IntervalUtil.longMidPoint(Interval.unbound()), is(0L));
		assertNull(IntervalUtil.longMidPoint(Interval.lower(Bound.exclusive(1L))));
		assertThat(IntervalUtil.longMidPoint(Interval.inclusive(1L, 4L)), is(3L));
		assertThat(IntervalUtil.longMidPoint(maxLong), is(0L));
	}

	@Test
	public void testLongWidth() {
		assertNull(IntervalUtil.longWidth(Interval.unbound()));
		assertNull(IntervalUtil.longWidth(Interval.lower(Bound.exclusive(1L))));
		assertThat(IntervalUtil.longWidth(Interval.inclusive(1L, 4L)), is(3L));
		TestUtil.assertThrown(() -> IntervalUtil.longWidth(maxLong));
	}

	@Test
	public void testIntMidPoint() {
		assertThat(IntervalUtil.intMidPoint(Interval.unbound()), is(0));
		assertNull(IntervalUtil.intMidPoint(Interval.lower(Bound.exclusive(1))));
		assertThat(IntervalUtil.intMidPoint(Interval.inclusive(1, 4)), is(3));
		assertThat(IntervalUtil.intMidPoint(maxInt), is(0));
	}

	@Test
	public void testIntWidth() {
		assertNull(IntervalUtil.intWidth(Interval.unbound()));
		assertNull(IntervalUtil.intWidth(Interval.lower(Bound.exclusive(1))));
		assertThat(IntervalUtil.intWidth(Interval.inclusive(1, 4)), is(3));
		TestUtil.assertThrown(() -> IntervalUtil.intWidth(maxInt));
	}

}
