package ceri.common.color;

import static ceri.common.color.Colorx.black;
import static ceri.common.color.Colorx.full;
import static ceri.common.test.TestUtil.assertPrivateConstructor;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
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
		assertThat(ColorxComparators.BY_ALPHA.compare(a255, a254), is(1));
		assertThat(ColorxComparators.BY_ALPHA.compare(a1, a254), is(-1));
		assertThat(ColorxComparators.BY_ALPHA.compare(a0, a1), is(-1));
		assertThat(ColorxComparators.BY_ALPHA.compare(a254, a254), is(0));
	}

	@Test
	public void testRgbx() {
		assertThat(ColorxComparators.BY_RGBX.compare(null, null), is(0));
		assertThat(ColorxComparators.BY_RGBX.compare(full, null), is(1));
		assertThat(ColorxComparators.BY_RGBX.compare(null, black), is(-1));
		assertThat(ColorxComparators.BY_RGBX.compare(black, black), is(0));
		assertThat(ColorxComparators.BY_RGBX.compare(black, full), is(-1));
		assertThat(ColorxComparators.BY_RGBX.compare(red, green), is(1));
	}

	@Test
	public void testRed() {
		assertThat(ColorxComparators.BY_RED.compare(full, red), is(0));
		assertThat(ColorxComparators.BY_RED.compare(red, red), is(0));
		assertThat(ColorxComparators.BY_RED.compare(black, red), is(-1));
		assertThat(ColorxComparators.BY_RED.compare(red, lightGray), is(1));
	}

	@Test
	public void testGreen() {
		assertThat(ColorxComparators.BY_GREEN.compare(full, green), is(0));
		assertThat(ColorxComparators.BY_GREEN.compare(green, green), is(0));
		assertThat(ColorxComparators.BY_GREEN.compare(black, green), is(-1));
		assertThat(ColorxComparators.BY_GREEN.compare(green, lightGray), is(1));
	}

	@Test
	public void testBlue() {
		assertThat(ColorxComparators.BY_BLUE.compare(full, blue), is(0));
		assertThat(ColorxComparators.BY_BLUE.compare(blue, blue), is(0));
		assertThat(ColorxComparators.BY_BLUE.compare(black, blue), is(-1));
		assertThat(ColorxComparators.BY_BLUE.compare(blue, lightGray), is(1));
	}

}
