package ceri.common.color;

import static ceri.common.test.TestUtil.assertPrivateConstructor;
import static java.awt.Color.black;
import static java.awt.Color.blue;
import static java.awt.Color.green;
import static java.awt.Color.lightGray;
import static java.awt.Color.magenta;
import static java.awt.Color.pink;
import static java.awt.Color.red;
import static java.awt.Color.white;
import static org.hamcrest.CoreMatchers.is;
import static ceri.common.test.TestUtil.assertThat;
import java.awt.Color;
import org.junit.Test;

public class ColorComparatorsTest {

	@Test
	public void testConstructorIsPrivate() {
		assertPrivateConstructor(ColorComparators.class);
	}

	@Test
	public void testHsb() {
		assertThat(ColorComparators.BY_HSB.compare(null, null), is(0));
		assertThat(ColorComparators.BY_HSB.compare(white, null), is(1));
		assertThat(ColorComparators.BY_HSB.compare(null, black), is(-1));
		assertThat(ColorComparators.BY_HSB.compare(black, black), is(0));
		assertThat(ColorComparators.BY_HSB.compare(black, white), is(-1));
		assertThat(ColorComparators.BY_HSB.compare(red, green), is(-1));
		assertThat(ColorComparators.BY_HSB.compare(green, blue), is(-1));
		assertThat(ColorComparators.BY_HSB.compare(blue, red), is(1));
		assertThat(ColorComparators.BY_HSB.compare(red, pink), is(1));
		assertThat(ColorComparators.BY_HSB.compare(red, magenta), is(-1));
	}

	@Test
	public void testHue() {
		assertThat(ColorComparators.BY_HUE.compare(null, null), is(0));
		assertThat(ColorComparators.BY_HUE.compare(white, null), is(1));
		assertThat(ColorComparators.BY_HUE.compare(null, black), is(-1));
		assertThat(ColorComparators.BY_HUE.compare(black, black), is(0));
		assertThat(ColorComparators.BY_HUE.compare(black, white), is(0));
		assertThat(ColorComparators.BY_HUE.compare(red, green), is(-1));
		assertThat(ColorComparators.BY_HUE.compare(green, blue), is(-1));
		assertThat(ColorComparators.BY_HUE.compare(blue, red), is(1));
		assertThat(ColorComparators.BY_HUE.compare(red, pink), is(0));
		assertThat(ColorComparators.BY_HUE.compare(red, magenta), is(-1));
	}

	@Test
	public void testSaturation() {
		assertThat(ColorComparators.BY_SATURATION.compare(null, null), is(0));
		assertThat(ColorComparators.BY_SATURATION.compare(white, null), is(1));
		assertThat(ColorComparators.BY_SATURATION.compare(null, black), is(-1));
		assertThat(ColorComparators.BY_SATURATION.compare(black, black), is(0));
		assertThat(ColorComparators.BY_SATURATION.compare(black, white), is(0));
		assertThat(ColorComparators.BY_SATURATION.compare(red, green), is(0));
		assertThat(ColorComparators.BY_SATURATION.compare(green, blue), is(0));
		assertThat(ColorComparators.BY_SATURATION.compare(blue, red), is(0));
		assertThat(ColorComparators.BY_SATURATION.compare(red, pink), is(1));
		assertThat(ColorComparators.BY_SATURATION.compare(red, magenta), is(0));
	}

	@Test
	public void testBrightness() {
		assertThat(ColorComparators.BY_BRIGHTNESS.compare(null, null), is(0));
		assertThat(ColorComparators.BY_BRIGHTNESS.compare(white, null), is(1));
		assertThat(ColorComparators.BY_BRIGHTNESS.compare(null, black), is(-1));
		assertThat(ColorComparators.BY_BRIGHTNESS.compare(black, black), is(0));
		assertThat(ColorComparators.BY_BRIGHTNESS.compare(black, white), is(-1));
		assertThat(ColorComparators.BY_BRIGHTNESS.compare(red, green), is(0));
		assertThat(ColorComparators.BY_BRIGHTNESS.compare(green, blue), is(0));
		assertThat(ColorComparators.BY_BRIGHTNESS.compare(blue, red), is(0));
		assertThat(ColorComparators.BY_BRIGHTNESS.compare(red, pink), is(0));
		assertThat(ColorComparators.BY_BRIGHTNESS.compare(red, magenta), is(0));
	}

	@Test
	public void testAlpha() {
		Color a255 = new Color(200, 100, 0, 255);
		Color a254 = new Color(220, 0, 110, 254);
		Color a1 = new Color(220, 0, 110, 1);
		Color a0 = new Color(200, 100, 0, 0);
		assertThat(ColorComparators.BY_ALPHA.compare(a255, a254), is(1));
		assertThat(ColorComparators.BY_ALPHA.compare(a1, a254), is(-1));
		assertThat(ColorComparators.BY_ALPHA.compare(a0, a1), is(-1));
		assertThat(ColorComparators.BY_ALPHA.compare(a254, a254), is(0));
	}

	@Test
	public void testRgb() {
		assertThat(ColorComparators.BY_RGB.compare(null, null), is(0));
		assertThat(ColorComparators.BY_RGB.compare(white, null), is(1));
		assertThat(ColorComparators.BY_RGB.compare(null, black), is(-1));
		assertThat(ColorComparators.BY_RGB.compare(black, black), is(0));
		assertThat(ColorComparators.BY_RGB.compare(black, white), is(-1));
		assertThat(ColorComparators.BY_RGB.compare(red, green), is(1));
		assertThat(ColorComparators.BY_RGB.compare(green, blue), is(1));
		assertThat(ColorComparators.BY_RGB.compare(blue, red), is(-1));
	}

	@Test
	public void testRed() {
		assertThat(ColorComparators.BY_RED.compare(white, red), is(0));
		assertThat(ColorComparators.BY_RED.compare(red, red), is(0));
		assertThat(ColorComparators.BY_RED.compare(black, red), is(-1));
		assertThat(ColorComparators.BY_RED.compare(red, lightGray), is(1));
	}

	@Test
	public void testGreen() {
		assertThat(ColorComparators.BY_GREEN.compare(white, green), is(0));
		assertThat(ColorComparators.BY_GREEN.compare(green, green), is(0));
		assertThat(ColorComparators.BY_GREEN.compare(black, green), is(-1));
		assertThat(ColorComparators.BY_GREEN.compare(green, lightGray), is(1));
	}

	@Test
	public void testBlue() {
		assertThat(ColorComparators.BY_BLUE.compare(white, blue), is(0));
		assertThat(ColorComparators.BY_BLUE.compare(blue, blue), is(0));
		assertThat(ColorComparators.BY_BLUE.compare(black, blue), is(-1));
		assertThat(ColorComparators.BY_BLUE.compare(blue, lightGray), is(1));
	}

}
