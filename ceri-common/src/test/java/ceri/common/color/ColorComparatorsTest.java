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
		assertEquals(ColorComparators.HSB.compare(null, null), 0);
		assertEquals(ColorComparators.HSB.compare(white, null), 1);
		assertEquals(ColorComparators.HSB.compare(null, black), -1);
		assertEquals(ColorComparators.HSB.compare(black, black), 0);
		assertEquals(ColorComparators.HSB.compare(black, white), -1);
		assertEquals(ColorComparators.HSB.compare(red, green), -1);
		assertEquals(ColorComparators.HSB.compare(green, blue), -1);
		assertEquals(ColorComparators.HSB.compare(blue, red), 1);
		assertEquals(ColorComparators.HSB.compare(red, pink), 1);
		assertEquals(ColorComparators.HSB.compare(red, magenta), -1);
	}

	@Test
	public void testHue() {
		assertEquals(ColorComparators.HUE.compare(null, null), 0);
		assertEquals(ColorComparators.HUE.compare(white, null), 1);
		assertEquals(ColorComparators.HUE.compare(null, black), -1);
		assertEquals(ColorComparators.HUE.compare(black, black), 0);
		assertEquals(ColorComparators.HUE.compare(black, white), 0);
		assertEquals(ColorComparators.HUE.compare(red, green), -1);
		assertEquals(ColorComparators.HUE.compare(green, blue), -1);
		assertEquals(ColorComparators.HUE.compare(blue, red), 1);
		assertEquals(ColorComparators.HUE.compare(red, pink), 0);
		assertEquals(ColorComparators.HUE.compare(red, magenta), -1);
	}

	@Test
	public void testSaturation() {
		assertEquals(ColorComparators.SATURATION.compare(null, null), 0);
		assertEquals(ColorComparators.SATURATION.compare(white, null), 1);
		assertEquals(ColorComparators.SATURATION.compare(null, black), -1);
		assertEquals(ColorComparators.SATURATION.compare(black, black), 0);
		assertEquals(ColorComparators.SATURATION.compare(black, white), 0);
		assertEquals(ColorComparators.SATURATION.compare(red, green), 0);
		assertEquals(ColorComparators.SATURATION.compare(green, blue), 0);
		assertEquals(ColorComparators.SATURATION.compare(blue, red), 0);
		assertEquals(ColorComparators.SATURATION.compare(red, pink), 1);
		assertEquals(ColorComparators.SATURATION.compare(red, magenta), 0);
	}

	@Test
	public void testBrightness() {
		assertEquals(ColorComparators.BRIGHTNESS.compare(null, null), 0);
		assertEquals(ColorComparators.BRIGHTNESS.compare(white, null), 1);
		assertEquals(ColorComparators.BRIGHTNESS.compare(null, black), -1);
		assertEquals(ColorComparators.BRIGHTNESS.compare(black, black), 0);
		assertEquals(ColorComparators.BRIGHTNESS.compare(black, white), -1);
		assertEquals(ColorComparators.BRIGHTNESS.compare(red, green), 0);
		assertEquals(ColorComparators.BRIGHTNESS.compare(green, blue), 0);
		assertEquals(ColorComparators.BRIGHTNESS.compare(blue, red), 0);
		assertEquals(ColorComparators.BRIGHTNESS.compare(red, pink), 0);
		assertEquals(ColorComparators.BRIGHTNESS.compare(red, magenta), 0);
	}

	@Test
	public void testAlpha() {
		Color a255 = new Color(200, 100, 0, 255);
		Color a254 = new Color(220, 0, 110, 254);
		Color a1 = new Color(220, 0, 110, 1);
		Color a0 = new Color(200, 100, 0, 0);
		assertEquals(ColorComparators.ALPHA.compare(a255, a254), 1);
		assertEquals(ColorComparators.ALPHA.compare(a1, a254), -1);
		assertEquals(ColorComparators.ALPHA.compare(a0, a1), -1);
		assertEquals(ColorComparators.ALPHA.compare(a254, a254), 0);
	}

	@Test
	public void testArgb() {
		assertEquals(ColorComparators.ARGB.compare(null, null), 0);
		assertEquals(ColorComparators.ARGB.compare(white, null), 1);
		assertEquals(ColorComparators.ARGB.compare(null, black), -1);
		assertEquals(ColorComparators.ARGB.compare(black, black), 0);
		assertEquals(ColorComparators.ARGB.compare(black, white), -1);
		assertEquals(ColorComparators.ARGB.compare(red, green), 1);
		assertEquals(ColorComparators.ARGB.compare(green, blue), 1);
		assertEquals(ColorComparators.ARGB.compare(blue, red), -1);
	}

	@Test
	public void testRed() {
		assertEquals(ColorComparators.RED.compare(white, red), 0);
		assertEquals(ColorComparators.RED.compare(red, red), 0);
		assertEquals(ColorComparators.RED.compare(black, red), -1);
		assertEquals(ColorComparators.RED.compare(red, lightGray), 1);
	}

	@Test
	public void testGreen() {
		assertEquals(ColorComparators.GREEN.compare(white, green), 0);
		assertEquals(ColorComparators.GREEN.compare(green, green), 0);
		assertEquals(ColorComparators.GREEN.compare(black, green), -1);
		assertEquals(ColorComparators.GREEN.compare(green, lightGray), 1);
	}

	@Test
	public void testBlue() {
		assertEquals(ColorComparators.BLUE.compare(white, blue), 0);
		assertEquals(ColorComparators.BLUE.compare(blue, blue), 0);
		assertEquals(ColorComparators.BLUE.compare(black, blue), -1);
		assertEquals(ColorComparators.BLUE.compare(blue, lightGray), 1);
	}

}
