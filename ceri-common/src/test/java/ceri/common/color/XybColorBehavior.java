package ceri.common.color;

import static ceri.common.color.ColorTestUtil.assertXyb;
import static ceri.common.color.ColorTestUtil.assertXyz;
import static ceri.common.test.TestUtil.assertAllNotEqual;
import static ceri.common.test.TestUtil.exerciseEquals;
import static org.hamcrest.CoreMatchers.is;
import static ceri.common.test.TestUtil.assertThat;
import org.junit.Test;
import ceri.common.geom.Point2d;
import ceri.common.test.TestUtil;

public class XybColorBehavior {

	@Test
	public void shouldNotBreachEqualsContract() {
		XybColor c0 = XybColor.of(0.1, 0.2, 0.3, 0.4);
		XybColor c1 = XybColor.of(Point2d.of(0.1, 0.2), 0.3, 0.4);
		XybColor c2 = XybColor.of(0.01, 0.2, 0.3, 0.4);
		XybColor c3 = XybColor.of(0.1, 0.02, 0.3, 0.4);
		XybColor c4 = XybColor.of(0.1, 0.2, 0.03, 0.4);
		XybColor c5 = XybColor.of(0.1, 0.2, 0.3, 0.04);
		XybColor c6 = XybColor.of(0.1, 0.2, 0.3, 0.0);
		XybColor c7 = XybColor.of(0.1, 0.2, 0.3, 1.0);
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
		assertXyz(XybColor.of(0.5, 0.0, 0.2, 0.8).toXyz(), 0, 0, 0, 0.8);
		assertXyz(XybColor.of(0.4, 0.2, 0.8).toXyz(), 1.6, 0.8, 1.6);
		assertXyz(XybColor.of(0.1, 0.2, 0.3, 0.4).toXyz(), 0.15, 0.3, 1.05, 0.4);
	}

	@Test
	public void shouldDim() {
		XybColor c = XybColor.of(0.6, 0.8, 0.4, 0.5);
		assertThat(c.dim(1), is(c));
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
		assertXyb(XybColor.of(0.6, 0.4, 0.2, -0.1).normalize(), 0.6, 0.4, 0.2, 0.0);
	}

	@Test
	public void shouldLimitValues() {
		assertXyb(XybColor.of(0.5, 0.6, 0.3, 0.2).limit(), 0.5, 0.6, 0.3, 0.2);
		assertXyb(XybColor.of(1.1, 0.6, 0.3, 0.2).limit(), 1.0, 0.6, 0.3, 0.2);
		assertXyb(XybColor.of(0.5, 1.1, 0.3, 0.2).limit(), 0.5, 1.0, 0.3, 0.2);
		assertXyb(XybColor.of(0.5, 0.6, -0.1, 0.2).limit(), 0.5, 0.6, 0.0, 0.2);
		assertXyb(XybColor.of(0.5, 0.6, 0.3, 5.0).limit(), 0.5, 0.6, 0.3, 1.0);
	}

	@Test
	public void shouldVerifyValues() {
		XybColor.of(0.5, 0.6, 0.3, 0.2).verify();
		TestUtil.assertThrown(() -> XybColor.of(1.1, 0.6, 0.3, 0.2).verify());
		TestUtil.assertThrown(() -> XybColor.of(0.5, 1.1, 0.3, 0.2).verify());
		TestUtil.assertThrown(() -> XybColor.of(0.5, 0.6, -0.1, 0.2).verify());
		TestUtil.assertThrown(() -> XybColor.of(0.5, 0.6, 0.3, 5.0).verify());
	}

}
