package ceri.common.color;

import static ceri.common.color.ColorTestUtil.assertColor;
import static ceri.common.color.ColorTestUtil.assertHsb;
import static ceri.common.test.AssertUtil.assertApprox;
import static ceri.common.test.AssertUtil.assertArray;
import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertIllegalArg;
import static ceri.common.test.AssertUtil.assertOrdered;
import static ceri.common.test.AssertUtil.assertSame;
import static ceri.common.test.AssertUtil.assertStream;
import static ceri.common.test.AssertUtil.assertThrown;
import java.awt.Color;
import java.util.Comparator;
import org.junit.Test;
import ceri.common.stream.Streams;
import ceri.common.test.TestUtil;

public class ColorsTest {

	@Test
	public void testCompareArgb() {
		exerciseCompare(Colors.Compare.ARGB, 0xff204060, 0xff203060, 0xff204060, 0xff204070);
	}

	@Test
	public void testCompareAlpha() {
		exerciseCompare(Colors.Compare.A, 0xf0204060, 0xef204060, 0xf0204060, 0xff204060);
	}

	@Test
	public void testCompareRed() {
		exerciseCompare(Colors.Compare.R, 0xff204060, 0xff104060, 0xff204060, 0xff304060);
	}

	@Test
	public void testCompareGreen() {
		exerciseCompare(Colors.Compare.G, 0xff204060, 0xff203060, 0xff204060, 0xff205060);
	}

	@Test
	public void testCompareBlue() {
		exerciseCompare(Colors.Compare.B, 0xff204060, 0xff204050, 0xff204060, 0xff204070);
	}

	@Test
	public void testCompareHue() {
		assertEquals(Colors.Compare.HUE.compare(null, null), 0);
		assertEquals(Colors.Compare.HUE.compare(Color.white, null), 1);
		assertEquals(Colors.Compare.HUE.compare(null, Color.black), -1);
		assertEquals(Colors.Compare.HUE.compare(Color.black, Color.black), 0);
		assertEquals(Colors.Compare.HUE.compare(Color.black, Color.white), 0);
		assertEquals(Colors.Compare.HUE.compare(Color.red, Color.green), -1);
		assertEquals(Colors.Compare.HUE.compare(Color.green, Color.blue), -1);
		assertEquals(Colors.Compare.HUE.compare(Color.blue, Color.red), 1);
		assertEquals(Colors.Compare.HUE.compare(Color.red, Color.pink), 0);
		assertEquals(Colors.Compare.HUE.compare(Color.red, Color.magenta), -1);
	}

	@Test
	public void testCompareSaturation() {
		assertEquals(Colors.Compare.SATURATION.compare(null, null), 0);
		assertEquals(Colors.Compare.SATURATION.compare(Color.white, null), 1);
		assertEquals(Colors.Compare.SATURATION.compare(null, Color.black), -1);
		assertEquals(Colors.Compare.SATURATION.compare(Color.black, Color.black), 0);
		assertEquals(Colors.Compare.SATURATION.compare(Color.black, Color.white), 0);
		assertEquals(Colors.Compare.SATURATION.compare(Color.red, Color.green), 0);
		assertEquals(Colors.Compare.SATURATION.compare(Color.green, Color.blue), 0);
		assertEquals(Colors.Compare.SATURATION.compare(Color.blue, Color.red), 0);
		assertEquals(Colors.Compare.SATURATION.compare(Color.red, Color.pink), 1);
		assertEquals(Colors.Compare.SATURATION.compare(Color.red, Color.magenta), 0);
	}

	@Test
	public void testCompareBrightness() {
		assertEquals(Colors.Compare.BRIGHTNESS.compare(null, null), 0);
		assertEquals(Colors.Compare.BRIGHTNESS.compare(Color.white, null), 1);
		assertEquals(Colors.Compare.BRIGHTNESS.compare(null, Color.black), -1);
		assertEquals(Colors.Compare.BRIGHTNESS.compare(Color.black, Color.black), 0);
		assertEquals(Colors.Compare.BRIGHTNESS.compare(Color.black, Color.white), -1);
		assertEquals(Colors.Compare.BRIGHTNESS.compare(Color.red, Color.green), 0);
		assertEquals(Colors.Compare.BRIGHTNESS.compare(Color.green, Color.blue), 0);
		assertEquals(Colors.Compare.BRIGHTNESS.compare(Color.blue, Color.red), 0);
		assertEquals(Colors.Compare.BRIGHTNESS.compare(Color.red, Color.pink), 0);
		assertEquals(Colors.Compare.BRIGHTNESS.compare(Color.red, Color.magenta), 0);
	}

	@Test
	public void testCompareHsb() {
		assertEquals(Colors.Compare.HSB.compare(null, null), 0);
		assertEquals(Colors.Compare.HSB.compare(Color.white, null), 1);
		assertEquals(Colors.Compare.HSB.compare(null, Color.black), -1);
		assertEquals(Colors.Compare.HSB.compare(Color.black, Color.black), 0);
		assertEquals(Colors.Compare.HSB.compare(Color.black, Color.white), -1);
		assertEquals(Colors.Compare.HSB.compare(Color.red, Color.green), -1);
		assertEquals(Colors.Compare.HSB.compare(Color.green, Color.blue), -1);
		assertEquals(Colors.Compare.HSB.compare(Color.blue, Color.red), 1);
		assertEquals(Colors.Compare.HSB.compare(Color.red, Color.pink), 1);
		assertEquals(Colors.Compare.HSB.compare(Color.red, Color.magenta), -1);
	}

	@Test
	public void testFlattenArgb() {
		assertEquals(Colors.flattenArgb(0x00802040), 0xff000000);
		assertEquals(Colors.flattenArgb(0x80802040), 0xff401020);
		assertEquals(Colors.flattenArgb(0xff802040), 0xff802040);
	}

	@Test
	public void testRgb() {
		assertEquals(Colors.rgb(0x98765432), 0x765432);
		assertEquals(Colors.rgb(Colors.color(0x98765432)), 0x765432);
	}

	@Test
	public void testArgb() {
		assertEquals(Colors.argb(0x11, 0x22, 0x33), 0xff112233);
	}

	@Test
	public void testArgbFromText() {
		assertEquals(Colors.argb("test"), null);
		assertEquals(Colors.argb("clear"), 0);
		assertEquals(Colors.argb("aquamarine"), Coloring.aquamarine.argb);
		assertEquals(Colors.argb("#fed"), 0xffffeedd);
		assertEquals(Colors.argb("0xfed"), 0xff000fed);
		assertEquals(Colors.argb("#fedcba"), 0xfffedcba);
		assertEquals(Colors.argb("0xfedcba"), 0xfffedcba);
		assertEquals(Colors.argb("#fedcba98"), 0xfedcba98);
		assertEquals(Colors.argb("0xfedcba98"), 0xfedcba98);
	}

	@Test
	public void testValidArgbFromText() {
		assertThrown(() -> Colors.validArgb("test"));
		assertEquals(Colors.validArgb("aquamarine"), Coloring.aquamarine.argb);
	}

	@Test
	public void testGrayArgb() {
		assertEquals(Colors.grayArgb(0), 0xff000000);
		assertEquals(Colors.grayArgb(0xab), 0xffababab);
	}

	@Test
	public void testMaxArgb() {
		assertEquals(Colors.maxArgb(0x80408020), 0x8080ff40);
		assertEquals(Colors.maxArgb(0x8040ff20), 0x8040ff20);
		assertEquals(Colors.maxArgb(0x80000000), 0x80000000);
	}

	@Test
	public void testRandomRgb() {
		assertEquals(Colors.randomRgb() & 0xff000000, 0xff000000);
	}

	@Test
	public void testRandomArgb() {
		Colors.randomArgb();
	}

	@Test
	public void testBlendArgbs() {
		assertEquals(Colors.blendArgbs(), 0);
		assertEquals(Colors.blendArgbs(0x80aabbcc), 0x80aabbcc);
		assertEquals(Colors.blendArgbs(0xffaabbcc, 0x40112233), 0xffaabbcc);
		assertEquals(Colors.blendArgbs(0x80aabbcc, 0x112233), 0x80aabbcc);
		assertEquals(Colors.blendArgbs(0xaabbcc, 0x40112233), 0x40112233);
		assertEquals(Colors.blendArgbs(0x80aabbcc, 0x40112233), 0xa08b9cad);
		assertEquals(Colors.blendArgbs(0x40112233, 0x80aabbcc), 0xa06d7e8f);
	}

	@Test
	public void testDimArgb() {
		assertEquals(Colors.dimArgb(0xff664422, 0.0), 0xff000000);
		assertEquals(Colors.dimArgb(0xff664422, 1.0), 0xff664422);
		assertEquals(Colors.dimArgb(0xff664422, 0.5), 0xff332211);
		assertEquals(Colors.dimArgb(0xff664422, 0.125), 0xff0d0904);
		assertEquals(Colors.dimArgb(0x88664422, 0.5), 0x88332211);
		assertEquals(Colors.dimArgb(0x88000000, 0.5), 0x88000000);
	}

	@Test
	public void testScaleArgb() {
		assertEquals(Colors.scaleArgb(0xff224466, 0xff446622, 0), 0xff224466);
		assertEquals(Colors.scaleArgb(0xff224466, 0xff446622, 1), 0xff446622);
		assertEquals(Colors.scaleArgb(0xff224466, 0xff446622, 0.5), 0xff335544);
	}

	@Test
	public void testScaleHsbArgb() {
		assertEquals(Colors.scaleHsbArgb(0xff804020, 0xff208040, 0), 0xff804020);
		assertEquals(Colors.scaleHsbArgb(0xff804020, 0xff208040, 0.25), 0xff807020);
		assertEquals(Colors.scaleHsbArgb(0xff804020, 0xff208040, 0.5), 0xff608020);
		assertEquals(Colors.scaleHsbArgb(0xff804020, 0xff208040, 0.75), 0xff308020);
		assertEquals(Colors.scaleHsbArgb(0xff804020, 0xff208040, 1), 0xff208040);
	}

	@Test
	public void testFlatten() {
		assertColor(Colors.flatten(Colors.color(0x00802040)), 0xff000000);
		assertColor(Colors.flatten(Colors.color(0x80802040)), 0xff401020);
		assertColor(Colors.flatten(Colors.color(0xff802040)), 0xff802040);
	}

	@Test
	public void testColorFromComponents() {
		assertColor(Colors.color(0x80, 0x70, 0xff, 0), 0x8070ff00);
	}

	@Test
	public void testColorFromText() {
		assertColor(Colors.color("test"), null);
		assertColor(Colors.color("clear"), 0);
		assertColor(Colors.color("aquamarine"), Coloring.aquamarine.argb);
		assertColor(Colors.color("#fed"), 0xffffeedd);
		assertColor(Colors.color("0xfed"), 0xff000fed);
		assertColor(Colors.color("#fedcba98"), 0xfedcba98);
	}

	@Test
	public void testSetArgbComponent() {
		assertEquals(Colors.a(0x88776655, 0xaa), 0xaa776655);
		assertEquals(Colors.r(0x88776655, 0xaa), 0x88aa6655);
		assertEquals(Colors.g(0x88776655, 0xaa), 0x8877aa55);
		assertEquals(Colors.b(0x88776655, 0xaa), 0x887766aa);
	}

	@Test
	public void testSetComponent() {
		var c = Colors.color(0x88776655);
		assertColor(Colors.a(c, 0xaa), 0xaa776655);
		assertColor(Colors.r(c, 0xaa), 0x88aa6655);
		assertColor(Colors.g(c, 0xaa), 0x8877aa55);
		assertColor(Colors.b(c, 0xaa), 0x887766aa);
		assertSame(Colors.a(c, 0x88), c);
		assertSame(Colors.r(c, 0x77), c);
		assertSame(Colors.g(c, 0x66), c);
		assertSame(Colors.b(c, 0x55), c);
	}

	@Test
	public void testValidColorFromText() {
		assertThrown(() -> Colors.validColor("test"));
		assertColor(Colors.validColor("aquamarine"), Coloring.aquamarine.argb);
	}

	@Test
	public void testGray() {
		assertColor(Colors.gray(0), 0xff000000);
		assertColor(Colors.gray(0xab), 0xffababab);
	}

	@Test
	public void testMax() {
		assertColor(Colors.max(Colors.color(0x80408020)), 0x8080ff40);
		assertColor(Colors.max(Colors.color(0x8040ff20)), 0x8040ff20);
		assertColor(Colors.max(Colors.color(0x80000000)), 0x80000000);
	}

	@Test
	public void testRandom() {
		assertEquals(Colors.random().getRGB() & 0xff000000, 0xff000000);
	}

	@Test
	public void testDim() {
		assertColor(Colors.dim(Colors.color(0xff664422), 0.0), 0xff000000);
		assertColor(Colors.dim(Colors.color(0xff664422), 1.0), 0xff664422);
		assertColor(Colors.dim(Colors.color(0xff664422), 0.5), 0xff332211);
		assertColor(Colors.dim(Colors.color(0xff664422), 0.125), 0xff0d0904);
		assertColor(Colors.dim(Colors.color(0x88664422), 0.5), 0x88332211);
		assertColor(Colors.dim(Colors.color(0x88000000), 0.5), 0x88000000);
	}

	@Test
	public void testScale() {
		assertColor(Colors.scale(Colors.color(0xff224466), Colors.color(0xff446622), 0),
			0xff224466);
		assertColor(Colors.scale(Colors.color(0xff224466), Colors.color(0xff446622), 1),
			0xff446622);
		assertColor(Colors.scale(Colors.color(0xff224466), Colors.color(0xff446622), 0.5),
			0xff335544);
	}

	@Test
	public void testScaleHsb() {
		assertColor(Colors.scaleHsb(Colors.color(0xff804020), Colors.color(0xff208040), 0),
			0xff804020);
		assertColor(Colors.scaleHsb(Colors.color(0xff804020), Colors.color(0xff208040), 0.25),
			0xff807020);
		assertColor(Colors.scaleHsb(Colors.color(0xff804020), Colors.color(0xff208040), 0.5),
			0xff608020);
		assertColor(Colors.scaleHsb(Colors.color(0xff804020), Colors.color(0xff208040), 0.75),
			0xff308020);
		assertColor(Colors.scaleHsb(Colors.color(0xff804020), Colors.color(0xff208040), 1),
			0xff208040);
	}

	@Test
	public void testScaleHsbColors() {
		assertHsb(Colors.scaleHsb(Hsb.of(0.5, 0.8, 0.4), Hsb.of(0.9, 0.4, 0.6), 0.0), 0.5, 0.8,
			0.4);
		assertHsb(Colors.scaleHsb(Hsb.of(0.5, 0.8, 0.4), Hsb.of(0.9, 0.4, 0.6), 0.5), 0.7, 0.6,
			0.5);
		assertHsb(Colors.scaleHsb(Hsb.of(0.5, 0.8, 0.4), Hsb.of(0.9, 0.4, 0.6), 1.0), 0.9, 0.4,
			0.6);
	}

	@Test
	public void testComponentAccess() {
		assertEquals(Colors.a(Colors.color(0x98765432)), 0x98);
		assertEquals(Colors.r(Colors.color(0x98765432)), 0x76);
		assertEquals(Colors.g(Colors.color(0x98765432)), 0x54);
		assertEquals(Colors.b(Colors.color(0x98765432)), 0x32);
	}

	@Test
	public void testValidValue() {
		assertEquals(Colors.validValue(0), 0);
		assertEquals(Colors.validValue(255), 255);
		assertIllegalArg(() -> Colors.validValue(-1));
		assertIllegalArg(() -> Colors.validValue(256));
	}

	@Test
	public void testValidRatio() {
		assertEquals(Colors.validRatio(0.0), 0.0);
		assertEquals(Colors.validRatio(1.0), 1.0);
		assertIllegalArg(() -> Colors.validRatio(-0.1));
		assertIllegalArg(() -> Colors.validRatio(1.1));
	}

	@Test
	public void testScaleHue() {
		assertApprox(Colors.scaleHue(0.9, 0.3, 0.0), 0.9);
		assertApprox(Colors.scaleHue(0.9, 0.3, 0.5), 0.1);
		assertApprox(Colors.scaleHue(0.9, 0.3, 1.0), 0.3);
	}

	@Test
	public void testScaleValue() {
		assertEquals(Colors.scaleValue(0xde, 0x51, 0.0), 0xde);
		assertEquals(Colors.scaleValue(0xde, 0x51, 0.5), 0x98);
		assertEquals(Colors.scaleValue(0xde, 0x51, 1.0), 0x51);
	}

	@Test
	public void testScaleRatio() {
		assertApprox(Colors.scaleRatio(0.9, 0.3, 0.0), 0.9);
		assertApprox(Colors.scaleRatio(0.9, 0.3, 0.5), 0.6);
		assertApprox(Colors.scaleRatio(0.9, 0.3, 1.0), 0.3);
	}

	@Test
	public void testBlendAssociativity() {
		var c0 = Colors.a(Coloring.aquamarine.argb, 0x80);
		var c1 = Colors.a(Coloring.blueViolet.argb, 0x40);
		var c2 = Colors.a(Coloring.coral.argb, 0x20);
		var c21 = Colors.blendArgbs(c2, c1);
		var c10 = Colors.blendArgbs(c1, c0);
		var c21_0 = Colors.blendArgbs(c21, c0);
		var c2_10 = Colors.blendArgbs(c2, c10);
		ColorTestUtil.assertArgbDiff(c21_0, c2_10, 1);
	}

	@Test
	public void testToString() {
		assertEquals(Colors.toString(Colors.clear), "#00000000(clear)");
		assertEquals(Colors.toString(Coloring.aquamarine.color()), "#7fffd4(aquamarine)");
		assertEquals(Colors.toString(Coloring.aquamarine.argb), "#7fffd4(aquamarine)");
		assertEquals(Colors.toString(0xffffeedd), "#ffeedd");
	}

	@Test
	public void testName() {
		assertEquals(Colors.name(Colors.clear), "clear");
		assertEquals(Colors.name(Coloring.aquamarine.color()), "aquamarine");
		assertEquals(Colors.name(Colors.color(0x123456)), null);
	}

	@Test
	public void testHex() {
		assertEquals(Colors.hex(Colors.color(0x123456)), "#00123456");
		assertEquals(Colors.hex(Colors.color(0xff123456)), "#123456");
	}

	@Test
	public void testArgbsFromText() {
		assertArray(Colors.argbs("clear", "#def", "0x12345678"), 0, 0xffddeeff, 0x12345678);
		assertThrown(() -> Colors.argbs("test"));
	}

	@Test
	public void testColorsFromText() {
		assertArray(Colors.colors("clear", "#def", "0x12345678"), Colors.clear,
			Colors.color(0xffddeeff), Colors.color(0x12345678));
		assertThrown(() -> Colors.colors("test"));
	}

	@Test
	public void testArgbList() {
		assertOrdered(Colors.argbList(Streams.ints(0, 0x12345678)), 0, 0x12345678);
	}

	@Test
	public void testColorList() {
		assertOrdered(Colors.colorList(Streams.ints(0, 0x12345678)), Colors.clear,
			Colors.color(0x12345678));
	}

	@Test
	public void testArgbStream() {
		assertStream(Colors.rgbStream(0, 0x12345678), 0xff000000, 0xff345678);
	}

	@Test
	public void testFadeStream() {
		assertStream(
			Colors.fadeStream(Colors.color(0x204060), Colors.color(0x406020), 4, Bias.NONE),
			0x284850, 0x305040, 0x385830, 0x406020);
	}

	@Test
	public void testFadeHsbStream() {
		assertStream(
			Colors.fadeHsbStream(Colors.color(0x204060), Colors.color(0x406020), 4, Bias.NONE),
			0xff206060, 0xff206040, 0xff206020, 0xff406020);
	}

	@Test
	public void testRotateHueStream() {
		assertStream(Colors.rotateHueStream(Coloring.aquamarine.color(), 4, Bias.NONE), 0xff947fff,
			0xffff7faa, 0xffeaff7f, 0xff7fffd4);
	}

	private static void exerciseCompare(Comparator<Color> comparator, int c, int lt, int eq,
		int gt) {
		TestUtil.exerciseCompare(comparator, Colors.color(c), Colors.color(lt), Colors.color(eq),
			Colors.color(gt));
	}
}
