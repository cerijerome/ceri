package ceri.common.test;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import ceri.common.math.MathUtil;
import ceri.common.util.BasicUtil;
import ceri.common.util.EqualsUtil;

/**
 * Matcher for double, approximating to decimal places or by a delta value.
 */
public class ApproxMatcher extends BaseMatcher<Double> {
	private final Integer places;
	private final double delta;
	private final double expected;

	/**
	 * Creates a matcher for decimal places.
	 */
	public static ApproxMatcher round(double expected, int places) {
		return new ApproxMatcher(expected, places, Double.NaN);
	}

	/**
	 * Creates a matcher with a delta value.
	 */
	public static ApproxMatcher delta(double expected, double delta) {
		return new ApproxMatcher(expected, null, delta);
	}

	private ApproxMatcher(double expected, Integer places, double delta) {
		this.expected = expected;
		this.places = places;
		this.delta = delta;
	}

	@Override
	public void describeTo(Description description) {
		description.appendValue(expected)
			.appendText(hasPlaces() ? " within " + places + " decimal places" : " within " + delta);
	}

	@Override
	public boolean matches(Object item) {
		Double d = BasicUtil.castOrNull(Double.class, item);
		if (d != null) return match(d);
		Float f = BasicUtil.castOrNull(Float.class, item);
		if (f != null) return match(f);
		return false;
	}

	private boolean hasPlaces() {
		return places != null;
	}

	private boolean match(double actual) {
		return hasPlaces() ? matchRound(actual) : matchDelta(actual);
	}

	private boolean matchRound(double actual) {
		if (!Double.isFinite(actual) || !Double.isFinite(expected))
			return EqualsUtil.equals(actual, expected);
		double approxActual = MathUtil.round(places, actual);
		double approxExpected = MathUtil.round(places, expected);
		return EqualsUtil.equals(approxActual, approxExpected);
	}

	private boolean matchDelta(double actual) {
		return MathUtil.approxEqual(actual, expected, delta);
	}

}
