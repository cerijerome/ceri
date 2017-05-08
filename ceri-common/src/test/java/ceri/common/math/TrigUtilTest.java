package ceri.common.math;

import static ceri.common.test.TestUtil.assertApprox;
import static ceri.common.test.TestUtil.assertException;
import static ceri.common.test.TestUtil.assertNaN;
import static ceri.common.test.TestUtil.assertPrivateConstructor;
import org.junit.Test;

public class TrigUtilTest {
	private static final double ROOT2 = Math.sqrt(2.0);
	private static final double PI_BY_6 = Math.PI / 6.0;

	@Test
	public void testConstructorIsPrivate() {
		assertPrivateConstructor(TrigUtil.class);
	}

	@Test
	public void testTangentAngle() {
		assertException(() -> TrigUtil.tangentAngle(1, 0.5));
		assertNaN(TrigUtil.tangentAngle(0, 0));
		assertApprox(TrigUtil.tangentAngle(0, 1), 0);
		assertApprox(TrigUtil.tangentAngle(1, 1), MathUtil.PI_BY_2);
		assertApprox(TrigUtil.tangentAngle(1, 2), PI_BY_6);
	}

	@Test
	public void testSegmentArea() {
		assertException(() -> TrigUtil.segmentArea(1, -1));
		assertException(() -> TrigUtil.segmentArea(1, MathUtil.PIx2 + 0.00001));
		assertApprox(TrigUtil.segmentArea(2, MathUtil.PIx2), 2 * 2 * Math.PI);
		assertApprox(TrigUtil.segmentArea(2, Math.PI), 2 * Math.PI);
		assertApprox(TrigUtil.segmentArea(2, MathUtil.PI_BY_2), Math.PI - 2);
	}

	@Test
	public void testIntersectionSegmentAngle() {
		assertException(() -> TrigUtil.intersectionSegmentAngle(1, 0.5, 0));
		assertApprox(TrigUtil.intersectionSegmentAngle(1, 2, -PI_BY_6), 0);
		assertApprox(TrigUtil.intersectionSegmentAngle(1, 2, 0), Math.PI);
		assertApprox(TrigUtil.intersectionSegmentAngle(1, 2, PI_BY_6), MathUtil.PIx2);
		double angle = Math.asin(1 / (2 * ROOT2));
		assertApprox(TrigUtil.intersectionSegmentAngle(1, 2, -angle), MathUtil.PI_BY_2);
		assertApprox(TrigUtil.intersectionSegmentAngle(1, 2, 0), Math.PI);
		assertApprox(TrigUtil.intersectionSegmentAngle(1, 2, angle), 3 * MathUtil.PI_BY_2);
	}

}
