package ceri.common.color;

import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertPrivateConstructor;
import static java.awt.Color.black;
import static java.awt.Color.blue;
import static java.awt.Color.green;
import static java.awt.Color.lightGray;
import static java.awt.Color.magenta;
import static java.awt.Color.pink;
import static java.awt.Color.red;
import static java.awt.Color.white;
import java.awt.Color;
import org.junit.Test;

public class ColorComparatorsTest {

	@Test
	public void testConstructorIsPrivate() {
		assertPrivateConstructor(ColorComparators.class);
	}

	@Test
	public void testHsb() {
		assertEquals(ColorComparators.BY_HSB.compare(null, null), 0);
		assertEquals(ColorComparators.BY_HSB.compare(white, null), 1);
		assertEquals(ColorComparators.BY_HSB.compare(null, black), -1);
		assertEquals(ColorComparators.BY_HSB.compare(black, black), 0);
		assertEquals(ColorComparators.BY_HSB.compare(black, white), -1);
		assertEquals(ColorComparators.BY_HSB.compare(red, green), -1);
		assertEquals(ColorComparators.BY_HSB.compare(green, blue), -1);
		assertEquals(ColorComparators.BY_HSB.compare(blue, red), 1);
		assertEquals(ColorComparators.BY_HSB.compare(red, pink), 1);
		assertEquals(ColorComparators.BY_HSB.compare(red, magenta), -1);
	}

	@Test
	public void testHue() {
		assertEquals(ColorComparators.BY_HUE.compare(null, null), 0);
		assertEquals(ColorComparators.BY_HUE.compare(white, null), 1);
		assertEquals(ColorComparators.BY_HUE.compare(null, black), -1);
		assertEquals(ColorComparators.BY_HUE.compare(black, black), 0);
		assertEquals(ColorComparators.BY_HUE.compare(black, white), 0);
		assertEquals(ColorComparators.BY_HUE.compare(red, green), -1);
		assertEquals(ColorComparators.BY_HUE.compare(green, blue), -1);
		assertEquals(ColorComparators.BY_HUE.compare(blue, red), 1);
		assertEquals(ColorComparators.BY_HUE.compare(red, pink), 0);
		assertEquals(ColorComparators.BY_HUE.compare(red, magenta), -1);
	}

	@Test
	public void testSaturation() {
		assertEquals(ColorComparators.BY_SATURATION.compare(null, null), 0);
		assertEquals(ColorComparators.BY_SATURATION.compare(white, null), 1);
		assertEquals(ColorComparators.BY_SATURATION.compare(null, black), -1);
		assertEquals(ColorComparators.BY_SATURATION.compare(black, black), 0);
		assertEquals(ColorComparators.BY_SATURATION.compare(black, white), 0);
		assertEquals(ColorComparators.BY_SATURATION.compare(red, green), 0);
		assertEquals(ColorComparators.BY_SATURATION.compare(green, blue), 0);
		assertEquals(ColorComparators.BY_SATURATION.compare(blue, red), 0);
		assertEquals(ColorComparators.BY_SATURATION.compare(red, pink), 1);
		assertEquals(ColorComparators.BY_SATURATION.compare(red, magenta), 0);
	}

	@Test
	public void testBrightness() {
		assertEquals(ColorComparators.BY_BRIGHTNESS.compare(null, null), 0);
		assertEquals(ColorComparators.BY_BRIGHTNESS.compare(white, null), 1);
		assertEquals(ColorComparators.BY_BRIGHTNESS.compare(null, black), -1);
		assertEquals(ColorComparators.BY_BRIGHTNESS.compare(black, black), 0);
		assertEquals(ColorComparators.BY_BRIGHTNESS.compare(black, white), -1);
		assertEquals(ColorComparators.BY_BRIGHTNESS.compare(red, green), 0);
		assertEquals(ColorComparators.BY_BRIGHTNESS.compare(green, blue), 0);
		assertEquals(ColorComparators.BY_BRIGHTNESS.compare(blue, red), 0);
		assertEquals(ColorComparators.BY_BRIGHTNESS.compare(red, pink), 0);
		assertEquals(ColorComparators.BY_BRIGHTNESS.compare(red, magenta), 0);
	}

	@Test
	public void testAlpha() {
		Color a255 = new Color(200, 100, 0, 255);
		Color a254 = new Color(220, 0, 110, 254);
		Color a1 = new Color(220, 0, 110, 1);
		Color a0 = new Color(200, 100, 0, 0);
		assertEquals(ColorComparators.BY_ALPHA.compare(a255, a254), 1);
		assertEquals(ColorComparators.BY_ALPHA.compare(a1, a254), -1);
		assertEquals(ColorComparators.BY_ALPHA.compare(a0, a1), -1);
		assertEquals(ColorComparators.BY_ALPHA.compare(a254, a254), 0);
	}

	@Test
	public void testRgb() {
		assertEquals(ColorComparators.BY_RGB.compare(null, null), 0);
		assertEquals(ColorComparators.BY_RGB.compare(white, null), 1);
		assertEquals(ColorComparators.BY_RGB.compare(null, black), -1);
		assertEquals(ColorComparators.BY_RGB.compare(black, black), 0);
		assertEquals(ColorComparators.BY_RGB.compare(black, white), -1);
		assertEquals(ColorComparators.BY_RGB.compare(red, green), 1);
		assertEquals(ColorComparators.BY_RGB.compare(green, blue), 1);
		assertEquals(ColorComparators.BY_RGB.compare(blue, red), -1);
	}

	@Test
	public void testRed() {
		assertEquals(ColorComparators.BY_RED.compare(white, red), 0);
		assertEquals(ColorComparators.BY_RED.compare(red, red), 0);
		assertEquals(ColorComparators.BY_RED.compare(black, red), -1);
		assertEquals(ColorComparators.BY_RED.compare(red, lightGray), 1);
	}

	@Test
	public void testGreen() {
		assertEquals(ColorComparators.BY_GREEN.compare(white, green), 0);
		assertEquals(ColorComparators.BY_GREEN.compare(green, green), 0);
		assertEquals(ColorComparators.BY_GREEN.compare(black, green), -1);
		assertEquals(ColorComparators.BY_GREEN.compare(green, lightGray), 1);
	}

	@Test
	public void testBlue() {
		assertEquals(ColorComparators.BY_BLUE.compare(white, blue), 0);
		assertEquals(ColorComparators.BY_BLUE.compare(blue, blue), 0);
		assertEquals(ColorComparators.BY_BLUE.compare(black, blue), -1);
		assertEquals(ColorComparators.BY_BLUE.compare(blue, lightGray), 1);
	}

}
