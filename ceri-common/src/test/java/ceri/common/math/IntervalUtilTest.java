package ceri.common.math;

import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertNull;
import static ceri.common.test.AssertUtil.assertPrivateConstructor;
import static ceri.common.test.AssertUtil.assertThrown;
import org.junit.Test;

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
		assertEquals(IntervalUtil.longMidPoint(Interval.unbound()), 0L);
		assertNull(IntervalUtil.longMidPoint(Interval.lower(Bound.exclusive(1L))));
		assertEquals(IntervalUtil.longMidPoint(Interval.inclusive(1L, 4L)), 3L);
		assertEquals(IntervalUtil.longMidPoint(maxLong), 0L);
	}

	@Test
	public void testLongWidth() {
		assertNull(IntervalUtil.longWidth(Interval.unbound()));
		assertNull(IntervalUtil.longWidth(Interval.lower(Bound.exclusive(1L))));
		assertEquals(IntervalUtil.longWidth(Interval.inclusive(1L, 4L)), 3L);
		assertThrown(() -> IntervalUtil.longWidth(maxLong));
	}

	@Test
	public void testIntMidPoint() {
		assertEquals(IntervalUtil.intMidPoint(Interval.unbound()), 0);
		assertNull(IntervalUtil.intMidPoint(Interval.lower(Bound.exclusive(1))));
		assertEquals(IntervalUtil.intMidPoint(Interval.inclusive(1, 4)), 3);
		assertEquals(IntervalUtil.intMidPoint(maxInt), 0);
	}

	@Test
	public void testIntWidth() {
		assertNull(IntervalUtil.intWidth(Interval.unbound()));
		assertNull(IntervalUtil.intWidth(Interval.lower(Bound.exclusive(1))));
		assertEquals(IntervalUtil.intWidth(Interval.inclusive(1, 4)), 3);
		assertThrown(() -> IntervalUtil.intWidth(maxInt));
	}

}
