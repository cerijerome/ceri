package ceri.common.color;

import static ceri.common.color.ColorTestUtil.assertXyb;
import static ceri.common.color.ColorTestUtil.assertXyz;
import static ceri.common.test.TestUtil.assertAllNotEqual;
import static ceri.common.test.TestUtil.assertThat;
import static ceri.common.test.TestUtil.exerciseEquals;
import static org.hamcrest.CoreMatchers.is;
import org.junit.Test;
import ceri.common.test.TestUtil;

public class XyzColorBehavior {

	@Test
	public void shouldNotBreachEqualsContract() {
		XyzColor c0 = XyzColor.of(0.1, 0.2, 0.3, 0.4);
		XyzColor c1 = XyzColor.of(0.1, 0.2, 0.3, 0.4);
		XyzColor c2 = XyzColor.of(0.01, 0.2, 0.3, 0.4);
		XyzColor c3 = XyzColor.of(0.1, 0.02, 0.3, 0.4);
		XyzColor c4 = XyzColor.of(0.1, 0.2, 0.03, 0.4);
		XyzColor c5 = XyzColor.of(0.1, 0.2, 0.3, 0.04);
		XyzColor c6 = XyzColor.of(0.1, 0.2, 0.3, 0.0);
		XyzColor c7 = XyzColor.of(0.1, 0.2, 0.3, 1.0);
		exerciseEquals(c0, c1);
		assertAllNotEqual(c0, c2, c3, c4, c5, c6, c7);
		XyzColor c8 = XyzColor.of(0.9, 0.8, 0.7, 1.0);
		XyzColor c9 = XyzColor.of(0.9, 0.8, 0.7, 1.0);
		exerciseEquals(c8, c9);
	}

	@Test
	public void shouldConvertToXyb() {
		assertXyb(XyzColor.of(0, 0, 0, 0.5).toXyb(), 0, 0, 0, 0.5);
		assertXyb(XyzColor.of(0.7, 0.5, 0.8, 0.5).toXyb(), 0.35, 0.25, 0.5, 0.5);
		assertXyb(XyzColor.of(1.4, 1.0, 1.6, 0.5).toXyb(), 0.35, 0.25, 1.0, 0.5);
	}

	@Test
	public void shouldNormalizeValues() {
		assertXyz(XyzColor.of(1, 1, 1).normalize(), 1, 1, 1);
		assertXyz(XyzColor.of(0.5, 0.5, 0.5).normalize(), 0.5, 0.5, 0.5);
		assertXyz(XyzColor.of(1.333, 1, -1.333).normalize(), 1.285, 1, -1);
	}

	@Test
	public void shouldLimitValues() {
		assertThat(XyzColor.of(0, 0, 0).limit(), is(XyzColor.of(0, 0, 0)));
		assertThat(XyzColor.of(0, 0, 0, 0.5).limit(), is(XyzColor.of(0, 0, 0, 0.5)));
		assertThat(XyzColor.of(0, 0, 0, 2.0).limit(), is(XyzColor.of(0, 0, 0, 1.0)));
		assertThat(XyzColor.of(1.4, 1.0, 1.6, 1.2).limit(), is(XyzColor.of(1.4, 1.0, 1.6, 1.0)));
	}

	@Test
	public void shouldVerifyValues() {
		XyzColor.of(0.5, 0.6, 0.3, 0.2).verify();
		TestUtil.assertThrown(() -> XyzColor.of(1.333, 1, -1.333, 0.2).verify());
	}

}
