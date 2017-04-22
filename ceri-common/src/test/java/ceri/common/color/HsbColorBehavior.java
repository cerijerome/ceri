package ceri.common.color;

import static ceri.common.color.ColorUtil.alphaColor;
import static ceri.common.test.TestUtil.assertAllNotEqual;
import static ceri.common.test.TestUtil.exerciseEquals;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import java.awt.Color;
import org.junit.Test;

public class HsbColorBehavior {
	private static final double MARGIN_OF_ERROR = 0.00001;

	@Test
	public void shouldConvertFromColor() {
		assertColor(HsbColor.from(Color.black), 0.0, 0.0, 0.0);
		assertColor(HsbColor.from(Color.white), 0.0, 0.0, 1.0);
		assertColor(HsbColor.from(Color.red), 0.0, 1.0, 1.0);
		assertColor(HsbColor.from(Color.green), 0.33333, 1.0, 1.0);
		assertColor(HsbColor.from(Color.blue), 0.66667, 1.0, 1.0);
		assertColor(HsbColor.from(Color.yellow), 0.16667, 1.0, 1.0);
		assertColor(HsbColor.from(Color.cyan), 0.5, 1.0, 1.0);
		assertColor(HsbColor.from(Color.magenta), 0.83333, 1.0, 1.0);
	}

	@Test
	public void shouldConvertFromRgb() {
		assertColor(HsbColor.from(Color.black.getRGB()), 0.0, 0.0, 0.0);
		assertColor(HsbColor.from(Color.white.getRGB()), 0.0, 0.0, 1.0);
		assertColor(HsbColor.from(Color.red.getRGB()), 0.0, 1.0, 1.0);
		assertColor(HsbColor.from(Color.green.getRGB()), 0.33333, 1.0, 1.0);
		assertColor(HsbColor.from(Color.blue.getRGB()), 0.66667, 1.0, 1.0);
		assertColor(HsbColor.from(Color.yellow.getRGB()), 0.16667, 1.0, 1.0);
		assertColor(HsbColor.from(Color.cyan.getRGB()), 0.5, 1.0, 1.0);
		assertColor(HsbColor.from(Color.magenta.getRGB()), 0.83333, 1.0, 1.0);
	}

	@Test
	public void shouldConvertFromColorWithAlphaChannel() {
		assertColor(HsbColor.from(alphaColor(Color.black, 0)), 0.0, 0.0, 0.0, 0.0);
		assertColor(HsbColor.from(alphaColor(Color.black, 255)), 0.0, 0.0, 0.0, 1.0);
		assertColor(HsbColor.from(alphaColor(Color.white, 0)), 0.0, 0.0, 1.0, 0.0);
		assertColor(HsbColor.from(alphaColor(Color.white, 255)), 0.0, 0.0, 1.0, 1.0);
		assertColor(HsbColor.from(alphaColor(Color.yellow, 100)), 0.16667, 1.0, 1.0, 100 / 255);
	}

	@Test
	public void shouldNotBreachEqualsContract() {
		HsbColor c0 = new HsbColor(0.1, 0.2, 0.3, 0.4);
		HsbColor c1 = new HsbColor(0.1, 0.2, 0.3, 0.4);
		HsbColor c2 = new HsbColor(0.01, 0.2, 0.3, 0.4);
		HsbColor c3 = new HsbColor(0.1, 0.02, 0.3, 0.4);
		HsbColor c4 = new HsbColor(0.1, 0.2, 0.03, 0.4);
		HsbColor c5 = new HsbColor(0.1, 0.2, 0.3, 0.04);
		HsbColor c6 = new HsbColor(0.1, 0.2, 0.3, 0.0);
		HsbColor c7 = new HsbColor(0.1, 0.2, 0.3, 1.0);
		exerciseEquals(c0, c1);
		assertAllNotEqual(c0, c2, c3, c4, c5, c6, c7);
		HsbColor c8 = new HsbColor(0.9, 0.8, 0.7, 1.0);
		HsbColor c9 = new HsbColor(0.9, 0.8, 0.7, 1.0);
		exerciseEquals(c8, c9);
	}

	@Test
	public void shouldConvertToColor() {
		assertThat(new HsbColor(0.0, 0.0, 1.0).asColor(), is(Color.white));
		assertThat(new HsbColor(0.5, 0.0, 1.0).asColor(), is(Color.white));
		assertThat(new HsbColor(1.0, 0.0, 1.0).asColor(), is(Color.white));
		assertThat(new HsbColor(0.0, 0.0, 0.0).asColor(), is(Color.black));
		assertThat(new HsbColor(0.5, 0.0, 0.0).asColor(), is(Color.black));
		assertThat(new HsbColor(1.0, 0.0, 0.0).asColor(), is(Color.black));
		assertThat(new HsbColor(0.0, 1.0, 1.0).asColor(), is(Color.red));
		assertThat(new HsbColor(0.33333, 1.0, 1.0).asColor(), is(Color.green));
		assertThat(new HsbColor(0.66667, 1.0, 1.0).asColor(), is(Color.blue));
		assertThat(new HsbColor(0.16667, 1.0, 1.0).asColor(), is(Color.yellow));
		assertThat(new HsbColor(0.5, 1.0, 1.0).asColor(), is(Color.cyan));
		assertThat(new HsbColor(0.83333, 1.0, 1.0).asColor(), is(Color.magenta));
	}

	@Test
	public void shouldConvertToColorWithAlphaChannel() {
		assertColor(new HsbColor(0.0, 0.0, 0.0, 0.0).asColor(), Color.black, 0);
		assertColor(new HsbColor(0.0, 0.0, 0.0, 1.0).asColor(), Color.black, 255);
		assertColor(new HsbColor(0.0, 0.0, 1.0, 0.0).asColor(), Color.white, 0);
		assertColor(new HsbColor(0.0, 0.0, 1.0, 1.0).asColor(), Color.white, 255);
		assertColor(new HsbColor(0.16667, 1.0, 1.0, 0.5).asColor(), Color.yellow, 127);
	}

	private void assertColor(HsbColor color, double h, double s, double b) {
		assertColor(color, h, s, b, 1.0);
	}

	private void assertColor(HsbColor color, double h, double s, double b, double a) {
		assertEquals(color.h, h, MARGIN_OF_ERROR);
		assertEquals(color.s, s, MARGIN_OF_ERROR);
		assertEquals(color.b, b, MARGIN_OF_ERROR);
		assertEquals(color.a, a, MARGIN_OF_ERROR);
	}

	private void assertColor(Color c0, Color c, int alpha) {
		assertThat(c0, is(new Color(c.getRed(), c.getGreen(), c.getBlue(), alpha)));
	}

}
