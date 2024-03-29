package ceri.common.geom;

import static ceri.common.test.AssertUtil.assertApprox;
import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertPrivateConstructor;
import static java.lang.Double.MAX_VALUE;
import static java.lang.Double.NaN;
import org.junit.Test;
import ceri.common.math.MathUtil;

public class GeometryUtilTest {
	private final Dimension2d d0 = Dimension2d.of(400, 100);
	private final Dimension2d d1 = Dimension2d.of(200, 200);
	private final Dimension2d d2 = Dimension2d.of(100, 400);
	private final Dimension2d d3 = Dimension2d.of(50, 40);
	private final Dimension2d d4 = Dimension2d.of(0, 0);
	private final Dimension2d d5 = Dimension2d.of(MAX_VALUE, 0);
	private final Dimension2d d6 = Dimension2d.of(0, MAX_VALUE);
	private final Dimension2d d7 = Dimension2d.of(MAX_VALUE, MAX_VALUE);
	private final Ratio2d r0 = Ratio2d.of(1, 1);
	private final Ratio2d r1 = Ratio2d.of(1, 0.2);
	private final Ratio2d r2 = Ratio2d.of(0.2, 0.5);
	private final Ratio2d r3 = Ratio2d.of(0, 0);
	private final Ratio2d r4 = Ratio2d.of(1, 0);
	private final Rectangle2d rt0 = Rectangle2d.of(0, 0, 200, 100);
	private final Rectangle2d rt1 = Rectangle2d.of(-50, 50, 100, 40);
	private final Rectangle2d rt2 = Rectangle2d.of(-40, -100, 0, 400);
	private final Rectangle2d rt3 = Rectangle2d.of(-20, -200, 100, 100);

	@Test
	public void testConstructorIsPrivate() {
		assertPrivateConstructor(GeometryUtil.class);
	}

	@Test
	public void testAngleFromGradient() {
		assertApprox(GeometryUtil.angleFromGradient(NaN), NaN);
		assertApprox(GeometryUtil.angleFromGradient(0), 0);
		assertApprox(GeometryUtil.angleFromGradient(Double.POSITIVE_INFINITY), MathUtil.PI_BY_2);
		assertApprox(GeometryUtil.angleFromGradient(Double.NEGATIVE_INFINITY), -MathUtil.PI_BY_2);
		assertApprox(GeometryUtil.angleFromGradient(1), Math.PI / 4);
		assertApprox(GeometryUtil.angleFromGradient(-1), -Math.PI / 4);
	}

	@Test
	public void testOverlap() {
		assertRect(GeometryUtil.overlap(rt0, rt0), 0, 0, 200, 100);
		assertRect(GeometryUtil.overlap(rt0, rt1), 0, 50, 50, 40);
		assertRect(GeometryUtil.overlap(rt1, rt0), 0, 50, 50, 40);
		assertRect(GeometryUtil.overlap(rt1, rt2), -40, 50, 0, 40);
		assertRect(GeometryUtil.overlap(rt2, rt1), -40, 50, 0, 40);
		assertRect(GeometryUtil.overlap(rt3, rt1), 0, 0, 0, 0);
	}

	@Test
	public void testCrop() {
		assertRect(GeometryUtil.crop(d1, d0, r1), 0, 20, 200, 100);
		assertRect(GeometryUtil.crop(d2, d3, r2), 10, 180, 50, 40);
		assertRect(GeometryUtil.crop(d2, d3, r0), 50, 360, 50, 40);
		assertRect(GeometryUtil.crop(d2, d3, r3), 0, 0, 50, 40);
		assertRect(GeometryUtil.crop(d2, d3, r4), 50, 0, 50, 40);
	}

	@Test
	public void testResizeToMax() {
		assertDim(GeometryUtil.resizeToMax(d1, d0), 100, 100);
		assertDim(GeometryUtil.resizeToMax(d3, d1), 200, 160);
		assertDim(GeometryUtil.resizeToMax(d1, d1), 200, 200);
		assertDim(GeometryUtil.resizeToMax(d1, d4), 0, 0);
		assertDim(GeometryUtil.resizeToMax(d4, d2), 0, 0);
		assertDim(GeometryUtil.resizeToMax(d5, d3), 50, 0);
		assertDim(GeometryUtil.resizeToMax(d6, d3), 0, 40);
		assertDim(GeometryUtil.resizeToMax(d7, d3), 40, 40);
	}

	@Test
	public void testResizeToMin() {
		assertDim(GeometryUtil.resizeToMin(d1, d0), 400, 400);
		assertDim(GeometryUtil.resizeToMin(d3, d1), 250, 200);
		assertDim(GeometryUtil.resizeToMin(d1, d1), 200, 200);
		assertDim(GeometryUtil.resizeToMin(d1, d4), 0, 0);
		assertDim(GeometryUtil.resizeToMin(d4, d2), 0, 0);
		assertDim(GeometryUtil.resizeToMin(d5, d3), 50, 0);
		assertDim(GeometryUtil.resizeToMin(d6, d3), 0, 40);
		assertDim(GeometryUtil.resizeToMin(d7, d3), 50, 50);
	}

	private void assertDim(Dimension2d dim, double w, double h) {
		assertEquals(dim.w, w);
		assertEquals(dim.h, h);
	}

	private void assertRect(Rectangle2d rect, double x, double y, double w, double h) {
		assertEquals(rect.x, x);
		assertEquals(rect.y, y);
		assertEquals(rect.w, w);
		assertEquals(rect.h, h);
	}

}
