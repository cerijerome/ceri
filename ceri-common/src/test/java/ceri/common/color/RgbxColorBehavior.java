package ceri.common.color;

import static ceri.common.color.ColorTestUtil.assertColorx;
import static ceri.common.color.ColorTestUtil.assertRgbx;
import static ceri.common.test.TestUtil.assertAllNotEqual;
import static ceri.common.test.TestUtil.exerciseEquals;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotEquals;
import static ceri.common.test.TestUtil.assertThat;
import org.junit.Test;
import ceri.common.test.TestUtil;

public class RgbxColorBehavior {

	@Test
	public void shouldNotBreachEqualsContract() {
		RgbxColor c0 = RgbxColor.of(0.4, 0.8, 0.6, 0.5);
		RgbxColor c1 = RgbxColor.of(0.4, 0.8, 0.6, 0.5);
		RgbxColor c2 = RgbxColor.of(0.5, 0.8, 0.6, 0.5);
		RgbxColor c3 = RgbxColor.of(0.4, 0.7, 0.6, 0.5);
		RgbxColor c4 = RgbxColor.of(0.4, 0.8, 0.5, 0.5);
		RgbxColor c5 = RgbxColor.of(0.4, 0.8, 0.6, 0.6);
		RgbxColor c6 = RgbxColor.of(0.4, 0.8, 0.6, 0.5, 0.9);
		exerciseEquals(c0, c1);
		assertAllNotEqual(c0, c2, c3, c4, c5, c6);
		assertNotEquals(c6.toString(), c0.toString());
	}

	@Test
	public void shouldCreateFromRgbxValues() {
		assertRgbx(RgbxColor.from(Colorx.of(0xffff0080)), 1.0, 1.0, 0.0, 0.502);
		assertRgbx(RgbxColor.from(0xff, 0, 0, 0xff), 1.0, 0.0, 0.0, 1.0);
	}

	@Test
	public void shouldCreateColorFromRatios() {
		assertColorx(RgbxColor.toColorx(1.0, 0.0, 1.0, 0.5), 0xff00ff80);
		assertColorx(RgbxColor.toColorx(0.0, 1.0, 1.0, 0.0, 0.5), 0x00ffff00, 128);
	}

	@Test
	public void shouldDim() {
		RgbxColor c = RgbxColor.of(0.6, 0.8, 0.4, 0.5, 0.8);
		assertThat(c.dim(1), is(c));
		assertRgbx(c.dim(0.5), 0.3, 0.4, 0.2, 0.25, 0.8);
		assertRgbx(c.dim(0), 0.0, 0.0, 0.0, 0.0, 0.8);
	}

	@Test
	public void shouldNormalizeValues() {
		assertRgbx(RgbxColor.of(0.5, 0.6, 0.3, 0.2, 0.8).normalize(), 0.5, 0.6, 0.3, 0.2, 0.8);
		assertRgbx(RgbxColor.of(-0.2, 0.6, 0.3, 0.2, 0.8).normalize(), 0.0, 0.8, 0.5, 0.4, 0.8);
		assertRgbx(RgbxColor.of(2.0, 0.0, 0.0, 0.0, 0.8).normalize(), 1.0, 0.0, 0.0, 0.0, 0.8);
		assertRgbx(RgbxColor.of(0.0, 2.0, 0.0, 0.0, 0.8).normalize(), 0.0, 1.0, 0.0, 0.0, 0.8);
		assertRgbx(RgbxColor.of(0.0, 0.0, 2.0, 0.0, 0.8).normalize(), 0.0, 0.0, 1.0, 0.0, 0.8);
		assertRgbx(RgbxColor.of(0.0, 0.0, 0.0, 2.0, 0.8).normalize(), 0.0, 0.0, 0.0, 1.0, 0.8);
		assertRgbx(RgbxColor.of(0.5, 0.6, 0.3, 0.2, 1.2).normalize(), 0.5, 0.6, 0.3, 0.2, 1.0);
	}

	@Test
	public void shouldLimitValues() {
		assertRgbx(RgbxColor.of(0.5, 0.6, 0.3, 0.2, 0.8).limit(), 0.5, 0.6, 0.3, 0.2, 0.8);
		assertRgbx(RgbxColor.of(1.1, 0.6, 0.3, 0.2, 0.8).limit(), 1.0, 0.6, 0.3, 0.2, 0.8);
		assertRgbx(RgbxColor.of(0.5, 1.1, 0.3, 0.2, 0.8).limit(), 0.5, 1.0, 0.3, 0.2, 0.8);
		assertRgbx(RgbxColor.of(0.5, 0.6, -0.1, 0.2, 0.8).limit(), 0.5, 0.6, 0.0, 0.2, 0.8);
		assertRgbx(RgbxColor.of(0.5, 0.6, 0.3, 5.0, 0.8).limit(), 0.5, 0.6, 0.3, 1.0, 0.8);
		assertRgbx(RgbxColor.of(0.5, 0.6, 0.3, 0.2, -5.0).limit(), 0.5, 0.6, 0.3, 0.2, 0.0);
	}

	@Test
	public void shouldVerifyValues() {
		RgbxColor.of(0.5, 0.6, 0.3, 0.2).verify();
		TestUtil.assertThrown(() -> RgbxColor.of(1.1, 0.6, 0.3, 0.2, 0.8).verify());
		TestUtil.assertThrown(() -> RgbxColor.of(0.5, 1.1, 0.3, 0.2, 0.8).verify());
		TestUtil.assertThrown(() -> RgbxColor.of(0.5, 0.6, -0.1, 0.2, 0.8).verify());
		TestUtil.assertThrown(() -> RgbxColor.of(0.5, 0.6, 0.3, 5.0, 0.8).verify());
		TestUtil.assertThrown(() -> RgbxColor.of(0.5, 0.6, 0.3, 0.2, -5).verify());
	}

}
