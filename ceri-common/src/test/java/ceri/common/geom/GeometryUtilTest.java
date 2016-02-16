package ceri.common.geom;

import static ceri.common.test.TestUtil.assertPrivateConstructor;
import static java.lang.Double.MAX_VALUE;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import org.junit.Test;
import ceri.common.geom.Dimension2d;
import ceri.common.geom.GeometryUtil;
import ceri.common.geom.Ratio2d;
import ceri.common.geom.Rectangle;

public class GeometryUtilTest {
	private final Dimension2d d0 = new Dimension2d(400, 100);
	private final Dimension2d d1 = new Dimension2d(200, 200);
	private final Dimension2d d2 = new Dimension2d(100, 400);
	private final Dimension2d d3 = new Dimension2d(50, 40);
	private final Dimension2d d4 = new Dimension2d(0, 0);
	private final Dimension2d d5 = new Dimension2d(MAX_VALUE, 0);
	private final Dimension2d d6 = new Dimension2d(0, MAX_VALUE);
	private final Dimension2d d7 = new Dimension2d(MAX_VALUE, MAX_VALUE);
	private final Ratio2d r0 = new Ratio2d(1, 1);
	private final Ratio2d r1 = new Ratio2d(1, 0.2);
	private final Ratio2d r2 = new Ratio2d(0.2, 0.5);
	private final Ratio2d r3 = new Ratio2d(0, 0);
	private final Ratio2d r4 = new Ratio2d(1, 0);
	private final Rectangle rt0 = new Rectangle(0, 0, 200, 100);
	private final Rectangle rt1 = new Rectangle(-50, 50, 100, 40);
	private final Rectangle rt2 = new Rectangle(-40, -100, 0, 400);
	private final Rectangle rt3 = new Rectangle(-20, -200, 100, 100);

	@Test
	public void testConstructorIsPrivate() {
		assertPrivateConstructor(GeometryUtil.class);
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
		assertThat(dim.w, is(w));
		assertThat(dim.h, is(h));
	}

	private void assertRect(Rectangle rect, double x, double y, double w, double h) {
		assertThat(rect.x, is(x));
		assertThat(rect.y, is(y));
		assertThat(rect.w, is(w));
		assertThat(rect.h, is(h));
	}

}
