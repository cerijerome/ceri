package ceri.common.color;

import static ceri.common.color.ColorTestUtil.assertXyb;
import static ceri.common.color.ColorTestUtil.assertXyz;
import static ceri.common.test.AssertUtil.assertAllNotEqual;
import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertThrown;
import static ceri.common.test.TestUtil.exerciseEquals;
import org.junit.Test;
import ceri.common.geom.Point2d;

public class XybColorBehavior {

	@Test
	public void shouldNotBreachEqualsContract() {
		XybColor c0 = XybColor.of(0.4, 0.1, 0.2, 0.3);
		XybColor c1 = XybColor.of(0.4, Point2d.of(0.1, 0.2), 0.3);
		XybColor c2 = XybColor.of(0.4, 0.01, 0.2, 0.3);
		XybColor c3 = XybColor.of(0.4, 0.1, 0.02, 0.3);
		XybColor c4 = XybColor.of(0.4, 0.1, 0.2, 0.03);
		XybColor c5 = XybColor.of(0.04, 0.1, 0.2, 0.3);
		XybColor c6 = XybColor.of(0.0, 0.1, 0.2, 0.3);
		XybColor c7 = XybColor.of(1.0, 0.1, 0.2, 0.3);
		XybColor c8 = XybColor.full(Point2d.of(0.1, 0.2));
		XybColor c9 = XybColor.of(Point2d.of(0.1, 0.2), 0.3);
		exerciseEquals(c0, c1);
		assertAllNotEqual(c0, c2, c3, c4, c5, c6, c7, c8, c9);
		XybColor c10 = XybColor.of(0.1, 0.2, 1.0);
		XybColor c11 = XybColor.of(Point2d.of(0.1, 0.2), 1.0);
		exerciseEquals(c10, c11);
	}

	@Test
	public void shouldConvertToXyz() {
		assertXyz(XybColor.of(0.8, 0.5, 0.0, 0.2).toXyz(), 0, 0, 0, 0.8);
		assertXyz(XybColor.of(0.4, 0.2, 0.8).toXyz(), 1.6, 0.8, 1.6);
		assertXyz(XybColor.of(0.4, 0.1, 0.2, 0.3).toXyz(), 0.15, 0.3, 1.05, 0.4);
	}

	@Test
	public void shouldDim() {
		XybColor c = XybColor.of(0.5, 0.6, 0.8, 0.4);
		assertEquals(c.dim(1), c);
		assertXyb(c.dim(0.5), 0.6, 0.8, 0.2, 0.5);
		assertXyb(c.dim(0), 0.6, 0.8, 0.0, 0.5);
	}

	@Test
	public void shouldNormalizeValues() {
		assertXyb(XybColor.of(0.667, 0.667, 0.8).normalize(), 0.667, 0.667, 0.8);
		assertXyb(XybColor.of(1.667, 1.0, 0.8).normalize(), 1.0, 0.667, 0.8);
		assertXyb(XybColor.of(-0.333, 1.0, 0.8).normalize(), 0.0, 0.667, 0.8);
		assertXyb(XybColor.of(-0.2, 2.0, 0.8).normalize(), 0.12, 1.0, 0.8);
		assertXyb(XybColor.of(1.0 / 3, -0.333, 0.8).normalize(), 0.333, 0.0, 0.8);
		assertXyb(XybColor.of(-0.333, 0.333, 0.8).normalize(), 0.0, 0.333, 0.8);
		assertXyb(XybColor.of(0.6, 0.4, 1.2).normalize(), 0.6, 0.4, 1.0);
		assertXyb(XybColor.of(-0.1, 0.6, 0.4, 0.2).normalize(), 0.6, 0.4, 0.2, 0.0);
	}

	@Test
	public void shouldLimitValues() {
		assertXyb(XybColor.of(0.2, 0.5, 0.6, 0.3).limit(), 0.5, 0.6, 0.3, 0.2);
		assertXyb(XybColor.of(0.2, 1.1, 0.6, 0.3).limit(), 1.0, 0.6, 0.3, 0.2);
		assertXyb(XybColor.of(0.2, 0.5, 1.1, 0.3).limit(), 0.5, 1.0, 0.3, 0.2);
		assertXyb(XybColor.of(0.2, 0.5, 0.6, -0.1).limit(), 0.5, 0.6, 0.0, 0.2);
		assertXyb(XybColor.of(5.0, 0.5, 0.6, 0.3).limit(), 0.5, 0.6, 0.3, 1.0);
	}

	@Test
	public void shouldVerifyValues() {
		XybColor.of(0.2, 0.5, 0.6, 0.3).verify();
		assertThrown(() -> XybColor.of(0.2, 1.1, 0.6, 0.3).verify());
		assertThrown(() -> XybColor.of(0.2, 0.5, 1.1, 0.3).verify());
		assertThrown(() -> XybColor.of(0.2, 0.5, 0.6, -0.1).verify());
		assertThrown(() -> XybColor.of(5.0, 0.5, 0.6, 0.3).verify());
	}

}
