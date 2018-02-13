package ceri.common.math;

import static java.lang.Math.addExact;
import static java.lang.Math.subtractExact;

public class IntervalUtil {

	private IntervalUtil() {}

	public static <T extends Number & Comparable<T>> double midPoint(Interval<T> interval) {
		if (interval.isUnbound()) return 0.0;
		if (interval.lower.isUnbound()) return Double.NEGATIVE_INFINITY;
		if (interval.upper.isUnbound()) return Double.POSITIVE_INFINITY;
		return (interval.lower.value.doubleValue() + interval.upper.value.doubleValue()) / 2;
	}

	public static <T extends Number & Comparable<T>> double width(Interval<T> interval) {
		if (interval.isInfinite()) return Double.POSITIVE_INFINITY;
		return interval.upper.value.doubleValue() - interval.lower.value.doubleValue();
	}

	public static Long longMidPoint(Interval<Long> interval) {
		if (interval.isUnbound()) return 0L;
		if (interval.isInfinite()) return null;
		return addExact(interval.lower.value, interval.upper.value) / 2;
	}

	public static <T extends Number & Comparable<T>> Long longWidth(Interval<Long> interval) {
		if (interval.isInfinite()) return null;
		return subtractExact(interval.upper.value, interval.lower.value);
	}

	public static Integer intMidPoint(Interval<Integer> interval) {
		if (interval.isUnbound()) return 0;
		if (interval.isInfinite()) return null;
		return addExact(interval.lower.value, interval.upper.value) / 2;
	}

	public static <T extends Number & Comparable<T>> Integer intWidth(Interval<Integer> interval) {
		if (interval.isInfinite()) return null;
		return subtractExact(interval.upper.value, interval.lower.value);
	}

}
