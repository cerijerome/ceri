package ceri.common.color;

import static ceri.common.collection.StreamUtil.toList;
import static ceri.common.test.TestUtil.assertIterable;
import static ceri.common.test.TestUtil.assertPrivateConstructor;
import static ceri.common.test.TestUtil.exerciseEnum;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import java.awt.Color;
import java.util.List;
import java.util.stream.IntStream;
import org.junit.Test;
import ceri.common.data.ByteUtil;
import ceri.common.math.MathUtil;

public class ColorUtilTest {

	@Test
	public void testConstructorIsPrivate() {
		assertPrivateConstructor(ColorUtil.class);
	}

	@Test
	public void codeCoverage() {
		exerciseEnum(ColorPreset.class);
	}

	@Test
	public void testMax() {
		ColorUtil.max(Color.black);
		assertColor(ColorUtil.max(Color.black), 0xffffff);
		assertColor(ColorUtil.max(X11Color.coral.color), 0xff7f50);
	}

	@Test
	public void testRotateBiasedHue() {
		List<Color> colors = ColorUtil.rotateHue(0x404040, 3, Biases.HALF_SINE);
		assertThat(colors.size(), is(3));
		assertApproximateHsb(colors.get(0), 0, 0, 0.25);
		assertApproximateHsb(colors.get(1), 0, 0, 0.25);
		assertApproximateHsb(colors.get(2), 0, 0, 0.25);
		colors = ColorUtil.rotateHue(0x408080, 4, Biases.HALF_SINE);
		assertThat(colors.size(), is(4));
		assertApproximateHsb(colors.get(0), 0.65, 0.5, 0.5);
		assertApproximateHsb(colors.get(1), 0.0, 0.5, 0.5);
		assertApproximateHsb(colors.get(2), 0.35, 0.5, 0.5);
		assertApproximateHsb(colors.get(3), 0.5, 0.5, 0.5);
	}

	@Test
	public void testRotateHue() {
		List<Color> colors = ColorUtil.rotateHue(0x800000, 2);
		assertThat(colors.size(), is(2));
		assertApproximateHsb(colors.get(0), 0.5, 1.0, 0.5);
		assertApproximateHsb(colors.get(1), 0.0, 1.0, 0.5);
		colors = ColorUtil.rotateHue(0x404040, 3);
		assertThat(colors.size(), is(3));
		assertApproximateHsb(colors.get(0), 0, 0, 0.25);
		assertApproximateHsb(colors.get(1), 0, 0, 0.25);
		assertApproximateHsb(colors.get(2), 0, 0, 0.25);
		colors = ColorUtil.rotateHue(new Color(0x408080), 4);
		assertThat(colors.size(), is(4));
		assertApproximateHsb(colors.get(0), 0.75, 0.5, 0.5);
		assertApproximateHsb(colors.get(1), 0.0, 0.5, 0.5);
		assertApproximateHsb(colors.get(2), 0.25, 0.5, 0.5);
		assertApproximateHsb(colors.get(3), 0.5, 0.5, 0.5);
	}

	@Test
	public void testScale() {
		assertThat(ColorUtil.scale(Color.black, Color.gray, 2), is(Color.gray));
		assertThat(ColorUtil.scale(0x102030, 0x405060, -2), is(new Color(0x102030)));
	}

	@Test
	public void testScaleChannel() {
		assertThat(ColorUtil.scaleChannel(50, 150, 0), is(50));
		assertThat(ColorUtil.scaleChannel(50, 150, -1), is(50));
		assertThat(ColorUtil.scaleChannel(50, 150, 1), is(150));
		assertThat(ColorUtil.scaleChannel(50, 150, 1.1), is(150));
	}

	@Test
	public void testAlphaColor() {
		assertColor(ColorUtil.alphaColor(Color.magenta, 100), 255, 0, 255, 100);
		assertColor(ColorUtil.alphaColor(Color.yellow, 0), 255, 255, 0, 0);
		assertColor(ColorUtil.alphaColor(Color.cyan, 255), 0, 255, 255, 255);
	}

	@Test
	public void testColors() {
		assertIterable(ColorUtil.colors("black", "white", "cyan"), Color.black, Color.white,
			Color.cyan);
	}

	@Test
	public void testColorFromString() {
		assertThat(ColorUtil.color("black"), is(Color.black));
		assertThat(ColorUtil.color("teal"), is(X11Color.teal.color));
		assertThat(ColorUtil.color("0"), is(Color.black));
		assertThat(ColorUtil.color("#0"), is(Color.black));
		assertThat(ColorUtil.color("0x0"), is(Color.black));
		assertThat(ColorUtil.color("0x000"), is(Color.black));
		assertThat(ColorUtil.color("0x0000"), is(Color.black));
		assertThat(ColorUtil.color("0xffffff"), is(Color.white));
		assertThat(ColorUtil.color("0xfff"), is(Color.white));
		assertThat(ColorUtil.color("#fff"), is(Color.white));
		assertThat(ColorUtil.color("fff"), is(Color.white));
		assertThat(ColorUtil.color("ffff"), is(Color.cyan));
		assertNull(ColorUtil.color("xxx"));
	}

	@Test
	public void testColorFromName() {
		assertThat(ColorUtil.colorFromName("black"), is(Color.black));
		assertThat(ColorUtil.colorFromName("teal"), is(X11Color.teal.color));
		assertNull(ColorUtil.colorFromName(""));
		assertNull(ColorUtil.colorFromName("xxx"));
	}

	@Test
	public void testFade() {
		List<Color> colors = ColorUtil.fade(Color.gray, X11Color.crimson.color, 5);
		assertThat(colors.size(), is(5));
		assertColor(colors.get(0), 146, 106, 114);
		assertColor(colors.get(1), 165, 85, 101);
		assertColor(colors.get(2), 183, 63, 87);
		assertColor(colors.get(3), 202, 42, 74);
		assertThat(colors.get(4), is(X11Color.crimson.color));
		colors = ColorUtil.fade(0xffafaf, 0xffafaf, 2); // pink
		assertThat(colors.size(), is(2));
		assertThat(colors.get(0), is(Color.pink));
		assertThat(colors.get(1), is(Color.pink));
		colors = ColorUtil.fade(X11Color.gold.color, Color.black, 1);
		assertThat(colors.size(), is(1));
		assertThat(colors.get(0), is(Color.black));
	}

	@Test
	public void testDim() {
		assertColor(ColorUtil.dim(new Color(255, 153, 51), 0), 0, 0, 0);
		assertColor(ColorUtil.dim(new Color(255, 153, 51), .7), 179, 107, 36);
		assertColor(ColorUtil.dim(new Color(255, 153, 51), 1), 255, 153, 51);
		assertColor(ColorUtil.dim(0x0, 0), 0, 0, 0);
		assertColor(ColorUtil.dim(new Color(0, 0, 0), 1), 0, 0, 0);
		assertColor(ColorUtil.dim(new Color(255, 255, 255), 0), 0, 0, 0);
		assertColor(ColorUtil.dim(0xffffff, 1), 255, 255, 255);
	}

	@Test
	public void testDimAll() {
		assertColors(ColorUtil.dimAll(0.5, Color.black, Color.white, Color.yellow, Color.cyan,
			Color.magenta), 0, 0x808080, 0x808000, 0x008080, 0x800080);
		assertColors(ColorUtil.dimAll(0.5, 0, 0xffffff, 0x806040), 0, 0x808080, 0x403020);
	}

	@Test
	public void testToString() {
		assertThat(ColorUtil.toString(Color.cyan), is("cyan"));
		assertThat(ColorUtil.toString(Color.black), is("black"));
		assertThat(ColorUtil.toString(0), is("black"));
		assertThat(ColorUtil.toString(X11Color.beige.color), is("beige"));
		assertThat(ColorUtil.toString(0xaabbcc), is("#aabbcc"));
		assertThat(ColorUtil.toString(255, 127, 80), is("coral"));
	}

	@Test
	public void testIsNamedColor() {
		assertTrue(ColorUtil.isNamedAwtColor(0));
		assertTrue(ColorUtil.isNamedAwtColor(Color.orange));
		assertTrue(ColorUtil.isNamedAwtColor(new Color(255, 175, 175)));
		assertFalse(ColorUtil.isNamedAwtColor(new Color(255, 255, 1)));
		assertFalse(ColorUtil.isNamedAwtColor(1));
	}

	@Test
	public void testRgb() {
		assertThat(ColorUtil.rgb(0, 0, 0), is(0x000000));
		assertThat(ColorUtil.rgb(1, 127, 128), is(0x017f80));
		assertThat(ColorUtil.rgb(255, 255, 255), is(0xffffff));
		assertThat(ColorUtil.rgb(new Color(55, 111, 222)), is(0x376fde));
		assertThat(ColorUtil.rgb(Color.pink), is(0xffafaf));
		assertThat(ColorUtil.rgb(Color.white), is(0xffffff));
		assertThat(ColorUtil.rgb(-1, 257, -128), is(0xff0180));
	}

	private void assertApproximateHsb(Color color, double h, double s, double b) {
		HsbColor hsb = HsbColor.from(color);
		assertThat(MathUtil.simpleRound(hsb.h, 2), is(h));
		assertThat(MathUtil.simpleRound(hsb.s, 2), is(s));
		assertThat(MathUtil.simpleRound(hsb.b, 2), is(b));
	}

	private void assertColors(Iterable<Color> colors, int... colorValues) {
		assertIterable(colors, toList(IntStream.of(colorValues).mapToObj(Color::new)));
	}

	private void assertColor(Color color, int rgb) {
		assertColor(color, byteAt(rgb, 2), byteAt(rgb, 1), byteAt(rgb, 0));
	}

	private int byteAt(int color, int i) {
		return ByteUtil.byteAt(color, i) & 0xff;
	}

	private void assertColor(Color color, int r, int g, int b) {
		assertThat(color, is(new Color(r, g, b)));
	}

	private void assertColor(Color color, int r, int g, int b, int a) {
		assertThat(color, is(new Color(r, g, b, a)));
	}

}
