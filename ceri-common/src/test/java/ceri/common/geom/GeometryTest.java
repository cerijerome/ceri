package ceri.common.geom;

import static ceri.common.test.AssertUtil.assertApprox;
import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertPrivateConstructor;
import static java.lang.Double.MAX_VALUE;
import static java.lang.Double.NaN;
import org.junit.Test;
import ceri.common.math.Maths;

public class GeometryTest {
	private final Size2d d0 = Size2d.of(400, 100);
	private final Size2d d1 = Size2d.of(200, 200);
	private final Size2d d2 = Size2d.of(100, 400);
	private final Size2d d3 = Size2d.of(50, 40);
	private final Size2d d4 = Size2d.of(0, 0);
	private final Size2d d5 = Size2d.of(MAX_VALUE, 0);
	private final Size2d d6 = Size2d.of(0, MAX_VALUE);
	private final Size2d d7 = Size2d.of(MAX_VALUE, MAX_VALUE);
	private final Ratio2d r0 = Ratio2d.of(1, 1);
	private final Ratio2d r1 = Ratio2d.of(1, 0.2);
	private final Ratio2d r2 = Ratio2d.of(0.2, 0.5);
	private final Ratio2d r3 = Ratio2d.of(0, 0);
	private final Ratio2d r4 = Ratio2d.of(1, 0);
	private final Rectangle rt0 = Rectangle.of(0, 0, 200, 100);
	private final Rectangle rt1 = Rectangle.of(-50, 50, 100, 40);
	private final Rectangle rt2 = Rectangle.of(-40, -100, 0, 400);
	private final Rectangle rt3 = Rectangle.of(-20, -200, 100, 100);

	@Test
	public void testConstructorIsPrivate() {
		assertPrivateConstructor(Geometry.class);
	}

	@Test
	public void testAngleFromGradient() {
		assertApprox(Geometry.angleFromGradient(NaN), NaN);
		assertApprox(Geometry.angleFromGradient(0), 0);
		assertApprox(Geometry.angleFromGradient(Double.POSITIVE_INFINITY), Maths.PI_BY_2);
		assertApprox(Geometry.angleFromGradient(Double.NEGATIVE_INFINITY), -Maths.PI_BY_2);
		assertApprox(Geometry.angleFromGradient(1), Math.PI / 4);
		assertApprox(Geometry.angleFromGradient(-1), -Math.PI / 4);
	}

	@Test
	public void testOverlap() {
		assertRect(Geometry.overlap(rt0, rt0), 0, 0, 200, 100);
		assertRect(Geometry.overlap(rt0, rt1), 0, 50, 50, 40);
		assertRect(Geometry.overlap(rt1, rt0), 0, 50, 50, 40);
		assertRect(Geometry.overlap(rt1, rt2), -40, 50, 0, 40);
		assertRect(Geometry.overlap(rt2, rt1), -40, 50, 0, 40);
		assertRect(Geometry.overlap(rt3, rt1), 0, 0, 0, 0);
	}

	@Test
	public void testCrop() {
		assertRect(Geometry.crop(d1, d0, r1), 0, 20, 200, 100);
		assertRect(Geometry.crop(d2, d3, r2), 10, 180, 50, 40);
		assertRect(Geometry.crop(d2, d3, r0), 50, 360, 50, 40);
		assertRect(Geometry.crop(d2, d3, r3), 0, 0, 50, 40);
		assertRect(Geometry.crop(d2, d3, r4), 50, 0, 50, 40);
	}

	@Test
	public void testResizeToMax() {
		assertDim(Geometry.resizeToMax(d1, d0), 100, 100);
		assertDim(Geometry.resizeToMax(d3, d1), 200, 160);
		assertDim(Geometry.resizeToMax(d1, d1), 200, 200);
		assertDim(Geometry.resizeToMax(d1, d4), 0, 0);
		assertDim(Geometry.resizeToMax(d4, d2), 0, 0);
		assertDim(Geometry.resizeToMax(d5, d3), 50, 0);
		assertDim(Geometry.resizeToMax(d6, d3), 0, 40);
		assertDim(Geometry.resizeToMax(d7, d3), 40, 40);
	}

	@Test
	public void testResizeToMin() {
		assertDim(Geometry.resizeToMin(d1, d0), 400, 400);
		assertDim(Geometry.resizeToMin(d3, d1), 250, 200);
		assertDim(Geometry.resizeToMin(d1, d1), 200, 200);
		assertDim(Geometry.resizeToMin(d1, d4), 0, 0);
		assertDim(Geometry.resizeToMin(d4, d2), 0, 0);
		assertDim(Geometry.resizeToMin(d5, d3), 50, 0);
		assertDim(Geometry.resizeToMin(d6, d3), 0, 40);
		assertDim(Geometry.resizeToMin(d7, d3), 50, 50);
	}

	private void assertDim(Size2d dim, double w, double h) {
		assertEquals(dim.w(), w);
		assertEquals(dim.h(), h);
	}

	private void assertRect(Rectangle rect, double x, double y, double w, double h) {
		assertEquals(rect.x(), x);
		assertEquals(rect.y(), y);
		assertEquals(rect.w(), w);
		assertEquals(rect.h(), h);
	}
}
