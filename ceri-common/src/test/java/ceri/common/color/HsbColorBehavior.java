package ceri.common.color;

import static ceri.common.color.ColorTestUtil.assertColor;
import static ceri.common.color.ColorTestUtil.assertHsb;
import static ceri.common.color.ColorTestUtil.assertRgb;
import static ceri.common.color.ColorUtil.alphaColor;
import static ceri.common.test.TestUtil.assertAllNotEqual;
import static ceri.common.test.TestUtil.assertException;
import static ceri.common.test.TestUtil.exerciseEquals;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import java.awt.Color;
import org.junit.Test;

public class HsbColorBehavior {

	@Test
	public void shouldNotBreachEqualsContract() {
		HsbColor c0 = HsbColor.of(0.1, 0.2, 0.3, 0.4);
		HsbColor c1 = HsbColor.of(0.1, 0.2, 0.3, 0.4);
		HsbColor c2 = HsbColor.of(0.01, 0.2, 0.3, 0.4);
		HsbColor c3 = HsbColor.of(0.1, 0.02, 0.3, 0.4);
		HsbColor c4 = HsbColor.of(0.1, 0.2, 0.03, 0.4);
		HsbColor c5 = HsbColor.of(0.1, 0.2, 0.3, 0.04);
		HsbColor c6 = HsbColor.of(0.1, 0.2, 0.3, 0.0);
		HsbColor c7 = HsbColor.of(0.1, 0.2, 0.3, 1.0);
		exerciseEquals(c0, c1);
		assertAllNotEqual(c0, c2, c3, c4, c5, c6, c7);
		HsbColor c8 = HsbColor.of(0.9, 0.8, 0.7, 1.0);
		HsbColor c9 = HsbColor.of(0.9, 0.8, 0.7, 1.0);
		exerciseEquals(c8, c9);
	}

	@Test
	public void shouldCreateWithMaxSaturationAndBrightness() {
		assertHsb(HsbColor.max(0.0), 0.0, 1.0, 1.0);
		assertHsb(HsbColor.max(0.333), 0.333, 1.0, 1.0);
	}

	@Test
	public void shouldConvertFromColor() {
		assertHsb(HsbColor.from(Color.black), 0.0, 0.0, 0.0);
		assertHsb(HsbColor.from(Color.white), 0.0, 0.0, 1.0);
		assertHsb(HsbColor.from(Color.red), 0.0, 1.0, 1.0);
		assertHsb(HsbColor.from(Color.green), 0.33333, 1.0, 1.0);
		assertHsb(HsbColor.from(Color.blue), 0.66667, 1.0, 1.0);
		assertHsb(HsbColor.from(Color.yellow), 0.16667, 1.0, 1.0);
		assertHsb(HsbColor.from(Color.cyan), 0.5, 1.0, 1.0);
		assertHsb(HsbColor.from(Color.magenta), 0.83333, 1.0, 1.0);
	}

	@Test
	public void shouldConvertFromRgbInt() {
		assertHsb(HsbColor.from(Color.black.getRGB()), 0.0, 0.0, 0.0);
		assertHsb(HsbColor.from(Color.white.getRGB()), 0.0, 0.0, 1.0);
		assertHsb(HsbColor.from(Color.red.getRGB()), 0.0, 1.0, 1.0);
		assertHsb(HsbColor.from(Color.green.getRGB()), 0.33333, 1.0, 1.0);
		assertHsb(HsbColor.from(Color.blue.getRGB()), 0.66667, 1.0, 1.0);
		assertHsb(HsbColor.from(Color.yellow.getRGB()), 0.16667, 1.0, 1.0);
		assertHsb(HsbColor.from(Color.cyan.getRGB()), 0.5, 1.0, 1.0);
		assertHsb(HsbColor.from(Color.magenta.getRGB()), 0.83333, 1.0, 1.0);
	}

	@Test
	public void shouldConvertFromRgb() {
		assertHsb(HsbColor.from(RgbColor.BLACK), 0.0, 0.0, 0.0);
		assertHsb(HsbColor.from(RgbColor.WHITE), 0.0, 0.0, 1.0);
		assertHsb(HsbColor.from(RgbColor.from(Color.red)), 0.0, 1.0, 1.0);
	}

	@Test
	public void shouldConvertFromColorWithAlphaChannel() {
		assertHsb(HsbColor.from(alphaColor(Color.black, 0)), 0.0, 0.0, 0.0, 0.0);
		assertHsb(HsbColor.from(alphaColor(Color.black, 255)), 0.0, 0.0, 0.0, 1.0);
		assertHsb(HsbColor.from(alphaColor(Color.white, 0)), 0.0, 0.0, 1.0, 0.0);
		assertHsb(HsbColor.from(alphaColor(Color.white, 255)), 0.0, 0.0, 1.0, 1.0);
		assertHsb(HsbColor.from(alphaColor(Color.yellow, 100)), 0.16667, 1.0, 1.0, 100.0 / 255);
	}

	@Test
	public void shouldConvertToColor() {
		assertColor(HsbColor.of(0.0, 0.0, 1.0).asColor(), Color.white);
		assertColor(HsbColor.of(0.5, 0.0, 1.0).asColor(), Color.white);
		assertColor(HsbColor.of(1.0, 0.0, 1.0).asColor(), Color.white);
		assertColor(HsbColor.of(0.0, 0.0, 0.0).asColor(), Color.black);
		assertColor(HsbColor.of(0.5, 0.0, 0.0).asColor(), Color.black);
		assertColor(HsbColor.of(1.0, 0.0, 0.0).asColor(), Color.black);
		assertColor(HsbColor.of(0.0, 1.0, 1.0).asColor(), Color.red);
		assertColor(HsbColor.of(0.33333, 1.0, 1.0).asColor(), Color.green);
		assertColor(HsbColor.of(0.66667, 1.0, 1.0).asColor(), Color.blue);
		assertColor(HsbColor.of(0.16667, 1.0, 1.0).asColor(), Color.yellow);
		assertColor(HsbColor.of(0.5, 1.0, 1.0).asColor(), Color.cyan);
		assertColor(HsbColor.of(0.83333, 1.0, 1.0).asColor(), Color.magenta);
	}

	@Test
	public void shouldConvertToColorWithAlphaChannel() {
		assertColor(HsbColor.of(0.0, 0.0, 0.0, 0.0).asColor(), Color.black, 0);
		assertColor(HsbColor.of(0.0, 0.0, 0.0, 1.0).asColor(), Color.black, 255);
		assertColor(HsbColor.of(0.0, 0.0, 1.0, 0.0).asColor(), Color.white, 0);
		assertColor(HsbColor.of(0.0, 0.0, 1.0, 1.0).asColor(), Color.white, 255);
		assertColor(HsbColor.of(0.16667, 1.0, 1.0, 0.5).asColor(), Color.yellow, 127);
	}

	@Test
	public void shouldConvertToRgb() {
		assertRgb(HsbColor.of(0.0, 1.0, 1.0, 0.5).asRgb(), 1.0, 0, 0, 0.5);
	}

	@Test
	public void shouldDetermineIfBlack() {
		assertTrue(HsbColor.BLACK.isBlack());
		assertTrue(HsbColor.of(1.0, 1.0, 0.0).isBlack());
		assertTrue(HsbColor.of(0.0, 1.0, 0.0).isBlack());
		assertTrue(HsbColor.of(1.0, 0.0, 0.0).isBlack());
		assertFalse(HsbColor.of(0.0, 0.0, 0.1).isBlack());
	}

	@Test
	public void shouldDetermineIfWhite() {
		assertTrue(HsbColor.WHITE.isWhite());
		assertTrue(HsbColor.of(0.0, 0.0, 1.0).isWhite());
		assertTrue(HsbColor.of(1.0, 0.0, 1.0).isWhite());
		assertFalse(HsbColor.of(0.0, 0.1, 1.0).isWhite());
		assertFalse(HsbColor.of(0.0, 0.0, 0.9).isWhite());
	}

	@Test
	public void shouldDim() {
		HsbColor c = HsbColor.of(0.6, 0.8, 0.4, 0.5);
		assertThat(c.dim(1), is(c));
		assertHsb(c.dim(0.5), 0.6, 0.8, 0.2, 0.5);
		assertHsb(c.dim(0), 0.6, 0.8, 0.0, 0.5);
	}

	@Test
	public void shouldNormalizeValues() {
		assertHsb(HsbColor.of(0.5, 0.6, 0.3, 0.2).normalize(), 0.5, 0.6, 0.3, 0.2);
		assertHsb(HsbColor.of(1.1, 0.6, 0.3, 0.2).normalize(), 0.1, 0.6, 0.3, 0.2);
		assertHsb(HsbColor.of(0.5, 1.1, 0.3, 0.2).normalize(), 0.5, 1.0, 0.3, 0.2);
		assertHsb(HsbColor.of(0.5, 0.6, -0.1, 0.2).normalize(), 0.5, 0.6, 0.0, 0.2);
		assertHsb(HsbColor.of(0.5, 0.6, 0.3, 5.0).normalize(), 0.5, 0.6, 0.3, 1.0);
	}

	@Test
	public void shouldLimitValues() {
		assertHsb(HsbColor.of(0.5, 0.6, 0.3, 0.2).limit(), 0.5, 0.6, 0.3, 0.2);
		assertHsb(HsbColor.of(1.1, 0.6, 0.3, 0.2).limit(), 1.0, 0.6, 0.3, 0.2);
		assertHsb(HsbColor.of(0.5, 1.1, 0.3, 0.2).limit(), 0.5, 1.0, 0.3, 0.2);
		assertHsb(HsbColor.of(0.5, 0.6, -0.1, 0.2).limit(), 0.5, 0.6, 0.0, 0.2);
		assertHsb(HsbColor.of(0.5, 0.6, 0.3, 5.0).limit(), 0.5, 0.6, 0.3, 1.0);
	}

	@Test
	public void shouldVerifyValues() {
		HsbColor.of(0.5, 0.6, 0.3, 0.2).verify();
		assertException(() -> HsbColor.of(1.1, 0.6, 0.3, 0.2).verify());
		assertException(() -> HsbColor.of(0.5, 1.1, 0.3, 0.2).verify());
		assertException(() -> HsbColor.of(0.5, 0.6, -0.1, 0.2).verify());
		assertException(() -> HsbColor.of(0.5, 0.6, 0.3, 5.0).verify());
	}

}
