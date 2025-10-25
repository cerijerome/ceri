package ceri.common.math;

import static ceri.common.test.Assert.assertEquals;
import static ceri.common.test.Assert.assertFalse;
import static ceri.common.test.Assert.assertPrivateConstructor;
import static ceri.common.test.Assert.assertTrue;
import org.junit.Test;
import ceri.common.test.Assert;

public class TrigTest {
	private static final double ROOT2 = Math.sqrt(2.0);
	private static final double PI_BY_6 = Math.PI / 6.0;
	private static final double PI_BY_3 = Math.PI / 3.0;

	@Test
	public void testConstructorIsPrivate() {
		assertPrivateConstructor(Trig.class);
	}

	@Test
	public void testNormalize() {
		Assert.approx(Trig.normalize(PI_BY_6 * 37), PI_BY_6);
		Assert.approx(Trig.normalize(PI_BY_3 * -9), Math.PI);
		Assert.approx(Trig.normalize(PI_BY_3 * -18), 0);
	}

	@Test
	public void testWithinNormalizedBounds() {
		assertFalse(Trig.withinNormalizedBounds(PI_BY_6 * -8, PI_BY_6 * 37, PI_BY_3));
		assertTrue(Trig.withinNormalizedBounds(PI_BY_6 * -10, PI_BY_6 * 37, PI_BY_3));
		assertTrue(Trig.withinNormalizedBounds(PI_BY_6 * -11, PI_BY_6 * 37, PI_BY_3));
		assertFalse(Trig.withinNormalizedBounds(PI_BY_6 * -12, PI_BY_6 * 37, PI_BY_3));
	}

	@Test
	public void testTangentAngle() {
		Assert.thrown(() -> Trig.tangentAngle(1, 0.5));
		assertEquals(Trig.tangentAngle(0, 0), Double.NaN);
		Assert.approx(Trig.tangentAngle(0, 1), 0);
		Assert.approx(Trig.tangentAngle(1, 1), Maths.PI_BY_2);
		Assert.approx(Trig.tangentAngle(1, 2), PI_BY_6);
	}

	@Test
	public void testSegmentArea() {
		Assert.approx(Trig.segmentArea(1, -Math.PI), Math.PI / 2);
		Assert.approx(Trig.segmentArea(1, Math.TAU + 0.00001), 0.0);
		Assert.approx(Trig.segmentArea(2, Math.TAU), 2 * 2 * Math.PI);
		Assert.approx(Trig.segmentArea(2, Math.PI), 2 * Math.PI);
		Assert.approx(Trig.segmentArea(2, Maths.PI_BY_2), Math.PI - 2);
	}

	@Test
	public void testIntersectionSegmentAngle() {
		Assert.thrown(() -> Trig.intersectionSegmentAngle(1, 0.5, 0));
		Assert.approx(Trig.intersectionSegmentAngle(1, 2, -PI_BY_6 - 0.1), 0);
		Assert.approx(Trig.intersectionSegmentAngle(1, 2, -PI_BY_6), 0);
		Assert.approx(Trig.intersectionSegmentAngle(1, 2, 0), Math.PI);
		Assert.approx(Trig.intersectionSegmentAngle(1, 2, PI_BY_6), Math.TAU);
		Assert.approx(Trig.intersectionSegmentAngle(1, 2, PI_BY_6 + 0.1), Math.TAU);
		double angle = Math.asin(1 / (2 * ROOT2));
		Assert.approx(Trig.intersectionSegmentAngle(1, 2, -angle), Maths.PI_BY_2);
		Assert.approx(Trig.intersectionSegmentAngle(1, 2, angle), 3 * Maths.PI_BY_2);
	}
}
