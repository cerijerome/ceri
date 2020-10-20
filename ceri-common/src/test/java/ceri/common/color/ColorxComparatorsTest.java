package ceri.common.color;

import static ceri.common.color.Colorx.black;
import static ceri.common.color.Colorx.full;
import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertPrivateConstructor;
import java.awt.Color;
import org.junit.Test;

public class ColorxComparatorsTest {
	private static final Colorx red = Colorx.of(Color.red, 0);
	private static final Colorx lightGray = Colorx.of(Color.lightGray, 0);
	private static final Colorx blue = Colorx.of(Color.blue, 0);
	private static final Colorx green = Colorx.of(Color.green, 0);

	@Test
	public void testConstructorIsPrivate() {
		assertPrivateConstructor(ColorxComparators.class);
	}

	@Test
	public void testAlpha() {
		Colorx a255 = Colorx.of(200, 100, 0, 50, 255);
		Colorx a254 = Colorx.of(220, 0, 110, 10, 254);
		Colorx a1 = Colorx.of(220, 0, 110, 77, 1);
		Colorx a0 = Colorx.of(200, 100, 0, 99, 0);
		assertEquals(ColorxComparators.BY_ALPHA.compare(a255, a254), 1);
		assertEquals(ColorxComparators.BY_ALPHA.compare(a1, a254), -1);
		assertEquals(ColorxComparators.BY_ALPHA.compare(a0, a1), -1);
		assertEquals(ColorxComparators.BY_ALPHA.compare(a254, a254), 0);
	}

	@Test
	public void testRgbx() {
		assertEquals(ColorxComparators.BY_RGBX.compare(null, null), 0);
		assertEquals(ColorxComparators.BY_RGBX.compare(full, null), 1);
		assertEquals(ColorxComparators.BY_RGBX.compare(null, black), -1);
		assertEquals(ColorxComparators.BY_RGBX.compare(black, black), 0);
		assertEquals(ColorxComparators.BY_RGBX.compare(black, full), -1);
		assertEquals(ColorxComparators.BY_RGBX.compare(red, green), 1);
	}

	@Test
	public void testRed() {
		assertEquals(ColorxComparators.BY_RED.compare(full, red), 0);
		assertEquals(ColorxComparators.BY_RED.compare(red, red), 0);
		assertEquals(ColorxComparators.BY_RED.compare(black, red), -1);
		assertEquals(ColorxComparators.BY_RED.compare(red, lightGray), 1);
	}

	@Test
	public void testGreen() {
		assertEquals(ColorxComparators.BY_GREEN.compare(full, green), 0);
		assertEquals(ColorxComparators.BY_GREEN.compare(green, green), 0);
		assertEquals(ColorxComparators.BY_GREEN.compare(black, green), -1);
		assertEquals(ColorxComparators.BY_GREEN.compare(green, lightGray), 1);
	}

	@Test
	public void testBlue() {
		assertEquals(ColorxComparators.BY_BLUE.compare(full, blue), 0);
		assertEquals(ColorxComparators.BY_BLUE.compare(blue, blue), 0);
		assertEquals(ColorxComparators.BY_BLUE.compare(black, blue), -1);
		assertEquals(ColorxComparators.BY_BLUE.compare(blue, lightGray), 1);
	}

}
