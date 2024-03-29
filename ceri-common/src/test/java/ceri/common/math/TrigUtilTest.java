package ceri.common.math;

import static ceri.common.math.MathUtil.PI_BY_2;
import static ceri.common.test.AssertUtil.assertApprox;
import static ceri.common.test.AssertUtil.assertFalse;
import static ceri.common.test.AssertUtil.assertNaN;
import static ceri.common.test.AssertUtil.assertPrivateConstructor;
import static ceri.common.test.AssertUtil.assertThrown;
import static ceri.common.test.AssertUtil.assertTrue;
import static java.lang.Math.PI;
import static java.lang.Math.TAU;
import org.junit.Test;

public class TrigUtilTest {
	private static final double ROOT2 = Math.sqrt(2.0);
	private static final double PI_BY_6 = PI / 6.0;
	private static final double PI_BY_3 = PI / 3.0;

	@Test
	public void testConstructorIsPrivate() {
		assertPrivateConstructor(TrigUtil.class);
	}

	@Test
	public void testNormalize() {
		assertApprox(TrigUtil.normalize(PI_BY_6 * 37), PI_BY_6);
		assertApprox(TrigUtil.normalize(PI_BY_3 * -9), PI);
		assertApprox(TrigUtil.normalize(PI_BY_3 * -18), 0);
	}

	@Test
	public void testWithinNormalizedBounds() {
		assertFalse(TrigUtil.withinNormalizedBounds(PI_BY_6 * -8, PI_BY_6 * 37, PI_BY_3));
		assertTrue(TrigUtil.withinNormalizedBounds(PI_BY_6 * -10, PI_BY_6 * 37, PI_BY_3));
		assertTrue(TrigUtil.withinNormalizedBounds(PI_BY_6 * -11, PI_BY_6 * 37, PI_BY_3));
		assertFalse(TrigUtil.withinNormalizedBounds(PI_BY_6 * -12, PI_BY_6 * 37, PI_BY_3));
	}

	@Test
	public void testTangentAngle() {
		assertThrown(() -> TrigUtil.tangentAngle(1, 0.5));
		assertNaN(TrigUtil.tangentAngle(0, 0));
		assertApprox(TrigUtil.tangentAngle(0, 1), 0);
		assertApprox(TrigUtil.tangentAngle(1, 1), PI_BY_2);
		assertApprox(TrigUtil.tangentAngle(1, 2), PI_BY_6);
	}

	@Test
	public void testSegmentArea() {
		assertThrown(() -> TrigUtil.segmentArea(1, -1));
		assertThrown(() -> TrigUtil.segmentArea(1, TAU + 0.00001));
		assertApprox(TrigUtil.segmentArea(2, TAU), 2 * 2 * PI);
		assertApprox(TrigUtil.segmentArea(2, PI), 2 * PI);
		assertApprox(TrigUtil.segmentArea(2, PI_BY_2), PI - 2);
	}

	@Test
	public void testIntersectionSegmentAngle() {
		assertThrown(() -> TrigUtil.intersectionSegmentAngle(1, 0.5, 0));
		assertApprox(TrigUtil.intersectionSegmentAngle(1, 2, -PI_BY_6 - 0.1), 0);
		assertApprox(TrigUtil.intersectionSegmentAngle(1, 2, -PI_BY_6), 0);
		assertApprox(TrigUtil.intersectionSegmentAngle(1, 2, 0), PI);
		assertApprox(TrigUtil.intersectionSegmentAngle(1, 2, PI_BY_6), TAU);
		assertApprox(TrigUtil.intersectionSegmentAngle(1, 2, PI_BY_6 + 0.1), TAU);
		double angle = Math.asin(1 / (2 * ROOT2));
		assertApprox(TrigUtil.intersectionSegmentAngle(1, 2, -angle), PI_BY_2);
		assertApprox(TrigUtil.intersectionSegmentAngle(1, 2, angle), 3 * PI_BY_2);
	}

}
