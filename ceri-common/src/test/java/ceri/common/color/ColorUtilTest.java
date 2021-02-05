package ceri.common.color;

import static ceri.common.collection.StreamUtil.toList;
import static ceri.common.color.ColorTestUtil.assertColor;
import static ceri.common.color.ColorTestUtil.assertHsb;
import static ceri.common.test.AssertUtil.assertApprox;
import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertFalse;
import static ceri.common.test.AssertUtil.assertIterable;
import static ceri.common.test.AssertUtil.assertNotNull;
import static ceri.common.test.AssertUtil.assertNull;
import static ceri.common.test.AssertUtil.assertPrivateConstructor;
import static ceri.common.test.AssertUtil.assertThrown;
import static ceri.common.test.AssertUtil.assertTrue;
import static ceri.common.test.TestUtil.exerciseEnum;
import static java.awt.Color.black;
import static java.awt.Color.cyan;
import static java.awt.Color.green;
import static java.awt.Color.red;
import static java.awt.Color.white;
import static java.awt.Color.yellow;
import java.awt.Color;
import java.util.List;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import java.util.stream.IntStream;
import org.junit.Test;
import ceri.common.color.ColorUtil.Fn.ChannelAdjuster;
import ceri.common.function.BinaryFunction;

public class ColorUtilTest {

	@Test
	public void testConstructorIsPrivate() {
		assertPrivateConstructor(ColorUtil.class);
		assertPrivateConstructor(ColorUtil.Fn.class);
	}

	@Test
	public void codeCoverage() {
		exerciseEnum(ColorPreset.class);
	}

	@Test
	public void testChannelAdjuster() {
		ChannelAdjuster adj = (x, r) -> (int) (x * r * r);
		assertEquals(adj.applyAsInt(100, 0.5), 25);
		adj = adj.bias(r -> r - 0.1);
		assertEquals(adj.applyAsInt(100, 0.5), 16);
	}

	@Test
	public void testFadeHsbFunction() {
		assertIterable(ColorUtil.Fn.fadeHsb(2).apply(red, green), yellow, green);
	}

	@Test
	public void testScaleHsbFunction() {
		assertColor(ColorUtil.Fn.scaleHsb(0.5).apply(red, green), yellow);
	}

	@Test
	public void testTransformRgbxUnaryToListFunction() {
		assertIterable(ColorUtil.Fn.transform(ColorxUtil.Fn.rotateHuex(2)).apply(red), cyan, red);
		assertNull(ColorUtil.Fn.transform(ColorxUtil.Fn.rotateHuex(2)).apply(null));
		assertIterable(ColorUtil.Fn.transform((Function<Colorx, List<Colorx>>) null).apply(red));
	}

	@Test
	public void testTransformRgbxBinaryToListFunction() {
		assertColors(ColorUtil.Fn.transform(ColorxUtil.Fn.fade(2)) //
			.apply(white, black), 0x808080, 0);
		assertNull(ColorUtil.Fn.transform(ColorxUtil.Fn.fade(2)).apply(null, black));
		assertNull(ColorUtil.Fn.transform(ColorxUtil.Fn.fade(2)).apply(white, null));
		assertColors(ColorUtil.Fn.transform((BinaryFunction<Colorx, List<Colorx>>) null) //
			.apply(white, black));
	}

	@Test
	public void testTransformRgbxBinaryOperator() {
		assertColor(ColorUtil.Fn.transform(ColorxUtil.Fn.scale(0.5)).apply(white, black), 0x808080);
		assertColor(ColorUtil.Fn.transform(ColorxUtil.Fn.scale(0.5)).apply(null, black), black);
		assertColor(ColorUtil.Fn.transform(ColorxUtil.Fn.scale(0.5)).apply(white, null), white);
		assertColor(ColorUtil.Fn.transform((BinaryOperator<Colorx>) null) //
			.apply(white, black), white);
	}

	@Test
	public void testTransformRgbxUnaryOperator() {
		assertColor(ColorUtil.Fn.transform(ColorxUtil.Fn.dim(0.5)).apply(white), 0x808080);
		assertNull(ColorUtil.Fn.transform(ColorxUtil.Fn.dim(0.5)).apply(null));
		assertColor(ColorUtil.Fn.transform((UnaryOperator<Colorx>) null).apply(white), white);
	}

	@Test
	public void testApplyUnaryFunction() {
		assertColor(ColorUtil.Fn.apply(white, ColorUtil.Fn.dim(0.5)), 0x808080);
		assertColor(ColorUtil.Fn.apply(white, (UnaryOperator<Color>) null), white);
		assertNull(ColorUtil.Fn.apply(null, ColorUtil.Fn.dim(0.5)));
	}

	@Test
	public void testApplyBinaryToListFunction() {
		assertColors(ColorUtil.Fn.apply(white, black, ColorUtil.Fn.fade(2)), 0x808080, 0x000000);
		assertNull(ColorUtil.Fn.apply(null, black, ColorUtil.Fn.fade(2)));
		assertNull(ColorUtil.Fn.apply(white, null, ColorUtil.Fn.fade(2)));
		assertColors(ColorUtil.Fn.apply(white, black, (BinaryFunction<Color, List<Color>>) null));
	}

	@Test
	public void testApplyBinaryOperator() {
		assertColor(ColorUtil.Fn.apply(white, black, ColorUtil.Fn.scale(0.5)), 0x808080);
		assertColor(ColorUtil.Fn.apply(null, black, ColorUtil.Fn.scale(0.5)), black);
		assertColor(ColorUtil.Fn.apply(white, null, ColorUtil.Fn.scale(0.5)), white);
		assertColor(ColorUtil.Fn.apply(white, black, (BinaryOperator<Color>) null), white);
	}

	@Test
	public void testApplyUnaryToListFunction() {
		assertIterable(ColorUtil.Fn.apply(red, ColorUtil.Fn.rotateHue(2)), cyan, red);
		assertNull(ColorUtil.Fn.apply(null, ColorUtil.Fn.rotateHue(2)));
		assertIterable(ColorUtil.Fn.apply(red, (Function<Color, List<Color>>) null));
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
		assertEquals(colors.size(), 3);
		assertHsb(colors.get(0), 0, 0, 0.251);
		assertHsb(colors.get(1), 0, 0, 0.251);
		assertHsb(colors.get(2), 0, 0, 0.251);
		colors = ColorUtil.rotateHue(0x408080, 4, Biases.HALF_SINE);
		assertEquals(colors.size(), 4);
		assertHsb(colors.get(0), 0.646, 0.5, 0.502);
		assertHsb(colors.get(1), 0.0, 0.5, 0.502);
		assertHsb(colors.get(2), 0.354, 0.5, 0.502);
		assertHsb(colors.get(3), 0.5, 0.5, 0.502);
	}

	@Test
	public void testRotateHue() {
		List<Color> colors = ColorUtil.rotateHue(0x800000, 2);
		assertEquals(colors.size(), 2);
		assertHsb(colors.get(0), 0.5, 1.0, 0.502);
		assertHsb(colors.get(1), 0.0, 1.0, 0.502);
		colors = ColorUtil.rotateHue(0x404040, 3);
		assertEquals(colors.size(), 3);
		assertHsb(colors.get(0), 0, 0, 0.251);
		assertHsb(colors.get(1), 0, 0, 0.251);
		assertHsb(colors.get(2), 0, 0, 0.251);
		colors = ColorUtil.rotateHue(new Color(0x408080), 4);
		assertEquals(colors.size(), 4);
		assertHsb(colors.get(0), 0.75, 0.5, 0.502);
		assertHsb(colors.get(1), 0.0, 0.5, 0.502);
		assertHsb(colors.get(2), 0.25, 0.5, 0.502);
		assertHsb(colors.get(3), 0.5, 0.5, 0.502);
	}

	@Test
	public void testScale() {
		assertColor(ColorUtil.scale(null, Color.white, 0.5), Color.white);
		assertColor(ColorUtil.scale(Color.white, null, 0.5), Color.white);
		assertColor(ColorUtil.scale(Color.black, Color.gray, 2), Color.gray);
		assertEquals(ColorUtil.scaleRgba(0x102030, 0x405060, -2), 0x102030);
	}

	@Test
	public void testScaleHsb() {
		assertColor(ColorUtil.scaleHsb(null, Color.white, 0.5), Color.white);
		assertColor(ColorUtil.scaleHsb(Color.white, null, 0.5), Color.white);
		assertColor(ColorUtil.scaleHsb(Color.black, Color.white, 1.1), Color.white);
		assertColor(ColorUtil.scaleHsb(Color.black, Color.white, -1), Color.black);
		assertColor(ColorUtil.scaleHsb(Color.black, Color.white, 0.5), Color.gray);
		assertColor(ColorUtil.scaleHsb(Color.yellow, Color.magenta, 0.5), Color.red);
		assertColor(ColorUtil.scaleHsb(Color.magenta, Color.yellow, 0.5), Color.red);
		assertColor(ColorUtil.scaleHsb(Color.cyan, Color.red, 0.33333), Color.green);
	}

	@Test
	public void testScaleHue() {
		assertApprox(ColorUtil.scaleHue(0.2, 0.8, 0), 0.2);
		assertApprox(ColorUtil.scaleHue(0.2, 0.8, -1), 0.2);
		assertApprox(ColorUtil.scaleHue(0.2, 0.8, 0.5), 0.0);
		assertApprox(ColorUtil.scaleHue(0.2, 0.8, 0.25), 0.1);
		assertApprox(ColorUtil.scaleHue(0.2, 0.8, 1), 0.8);
		assertApprox(ColorUtil.scaleHue(0.2, 0.8, 2), 0.8);
		assertApprox(ColorUtil.scaleHue(0.8, 0.2, 0.5), 1.0);
		assertApprox(ColorUtil.scaleHue(0.8, 0.2, 0.25), 0.9);
		assertApprox(ColorUtil.scaleHue(0.5, 1, 0.8), 0.9);
		assertApprox(ColorUtil.scaleHue(1, 0.5, 0.8), 0.6);
	}

	@Test
	public void testScaleRatio() {
		assertApprox(ColorUtil.scaleRatio(0.2, 0.7, 0), 0.2);
		assertApprox(ColorUtil.scaleRatio(0.2, 0.7, -1), 0.2);
		assertApprox(ColorUtil.scaleRatio(0.2, 0.7, 1), 0.7);
		assertApprox(ColorUtil.scaleRatio(0.2, 0.7, 2), 0.7);
		assertApprox(ColorUtil.scaleRatio(0.7, 0.2, 0), 0.7);
		assertApprox(ColorUtil.scaleRatio(0.7, 0.2, -1), 0.7);
		assertApprox(ColorUtil.scaleRatio(0.7, 0.2, 1), 0.2);
		assertApprox(ColorUtil.scaleRatio(0.7, 0.2, 2), 0.2);
		assertApprox(ColorUtil.scaleRatio(0.2, 0.7, 0.4), 0.4);
		assertApprox(ColorUtil.scaleRatio(0.7, 0.2, 0.4), 0.5);
	}

	@Test
	public void testScaleChannel() {
		assertEquals(ColorUtil.scaleChannel(50, 150, 0), 50);
		assertEquals(ColorUtil.scaleChannel(50, 150, -1), 50);
		assertEquals(ColorUtil.scaleChannel(50, 150, 1), 150);
		assertEquals(ColorUtil.scaleChannel(50, 150, 1.1), 150);
	}

	@Test
	public void testAlphaColor() {
		assertColor(ColorUtil.alphaColor(Color.magenta, 100), 255, 0, 255, 100);
		assertColor(ColorUtil.alphaColor(Color.yellow, 0), 255, 255, 0, 0);
		assertColor(ColorUtil.alphaColor(Color.cyan, 255), 0, 255, 255, 255);
	}

	@Test
	public void testGrayColor() {
		assertColor(ColorUtil.grayColor(-1), 255, 255, 255, 255);
		assertColor(ColorUtil.grayColor(0), 0, 0, 0, 255);
		assertColor(ColorUtil.grayColor(100), 100, 100, 100, 255);
		assertColor(ColorUtil.grayColor(255), 255, 255, 255, 255);
		assertColor(ColorUtil.grayColor(256), 0, 0, 0, 255);
		assertColor(ColorUtil.grayColor(257), 1, 1, 1, 255);
	}

	@Test
	public void testRandom() {
		assertNotNull(ColorUtil.random());
	}

	@Test
	public void testColors() {
		assertIterable(ColorUtil.colors("black", "white", "cyan"), Color.black, Color.white,
			Color.cyan);
	}

	@Test
	public void testValidColor() {
		assertThrown(() -> ColorUtil.validColor(null));
		assertThrown(() -> ColorUtil.validColor("\0white"));
		assertEquals(ColorUtil.validColor("white"), Color.white);
	}

	@Test
	public void testColorFromString() {
		assertColor(ColorUtil.color("black"), Color.black);
		assertColor(ColorUtil.color("teal"), X11Color.teal.color);
		assertColor(ColorUtil.color("0"), Color.black);
		assertColor(ColorUtil.color("#0"), Color.black);
		assertColor(ColorUtil.color("0x0"), Color.black);
		assertColor(ColorUtil.color("0x000"), Color.black);
		assertColor(ColorUtil.color("0x0000"), Color.black);
		assertColor(ColorUtil.color("0xffffff"), Color.white);
		assertColor(ColorUtil.color("0xfff"), Color.white);
		assertColor(ColorUtil.color("#fff"), Color.white);
		assertColor(ColorUtil.color("fff"), Color.white);
		assertColor(ColorUtil.color("ffff"), Color.cyan);
		assertNull(ColorUtil.color("xxx"));
	}

	@Test
	public void testColorFromName() {
		assertColor(ColorUtil.colorFromName("black"), Color.black);
		assertColor(ColorUtil.colorFromName("teal"), X11Color.teal.color);
		assertNull(ColorUtil.colorFromName(""));
		assertNull(ColorUtil.colorFromName("xxx"));
	}

	@Test
	public void testFade() {
		List<Color> colors = ColorUtil.fade(Color.gray, X11Color.crimson.color, 5);
		assertEquals(colors.size(), 5);
		assertColor(colors.get(0), 146, 106, 114);
		assertColor(colors.get(1), 165, 85, 101);
		assertColor(colors.get(2), 183, 63, 87);
		assertColor(colors.get(3), 202, 42, 74);
		assertEquals(colors.get(4), X11Color.crimson.color);
		colors = ColorUtil.fade(0xffafaf, 0xffafaf, 2); // pink
		assertEquals(colors.size(), 2);
		assertEquals(colors.get(0), Color.pink);
		assertEquals(colors.get(1), Color.pink);
		colors = ColorUtil.fade(X11Color.gold.color, Color.black, 1);
		assertEquals(colors.size(), 1);
		assertEquals(colors.get(0), Color.black);
	}

	@Test
	public void testFadeHsb() {
		assertIterable(ColorUtil.fadeHsb(Color.red, Color.blue, 2), //
			new Color(255, 0, 255), new Color(0, 0, 255));
		assertIterable(ColorUtil.fadeHsb(Color.red.getRGB(), Color.green.getRGB(), 2), //
			new Color(255, 255, 0), new Color(0, 255, 0));
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
	public void testToStrings() {
		assertIterable(ColorUtil.toStrings(Color.pink, Color.orange), "pink", "orange");
	}

	@Test
	public void testToString() {
		assertNull(ColorUtil.toString(null));
		assertEquals(ColorUtil.toString(Color.cyan), "cyan");
		assertEquals(ColorUtil.toString(Color.black), "black");
		assertEquals(ColorUtil.toString(0), "black");
		assertEquals(ColorUtil.toString(X11Color.beige.color), "beige");
		assertEquals(ColorUtil.toString(0xaabbcc), "#aabbcc");
		assertEquals(ColorUtil.toString(255, 127, 80), "coral");
	}

	@Test
	public void testToHex() {
		assertNull(ColorUtil.toHex(null));
		assertEquals(ColorUtil.toHex(X11Color.beige.color), "#f5f5dc");
		assertEquals(ColorUtil.toHex(255, 255, 0), "#ffff00");
	}

	@Test
	public void testToName() {
		assertNull(ColorUtil.toName(null));
		assertEquals(ColorUtil.toName(X11Color.beige.color), "beige");
		assertEquals(ColorUtil.toName(255, 0, 255), "magenta");
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
	public void testRgba() {
		assertEquals(ColorUtil.rgba(0, 0, 0, 0), 0);
		assertEquals(ColorUtil.rgba(1, 127, 128, 0), 0x00017f80);
		assertEquals(ColorUtil.rgba(255, 255, 255, 255), 0xffffffff);
		assertEquals(ColorUtil.rgba(-1, 257, -128, -2), 0xfeff0180);
	}

	@Test
	public void testRgb() {
		assertEquals(ColorUtil.rgb(new Color(55, 111, 222)), 0x376fde);
		assertEquals(ColorUtil.rgb(Color.pink), 0xffafaf);
		assertEquals(ColorUtil.rgb(Color.white), 0xffffff);
	}

	private void assertColors(Iterable<Color> colors, int... colorValues) {
		assertIterable(colors, toList(IntStream.of(colorValues).mapToObj(Color::new)));
	}

}
