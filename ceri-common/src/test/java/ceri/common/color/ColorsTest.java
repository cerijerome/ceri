package ceri.common.color;

import static ceri.common.color.ColorTestUtil.assertColor;
import static ceri.common.color.ColorTestUtil.assertHsb;
import java.awt.Color;
import java.util.Comparator;
import org.junit.Test;
import ceri.common.stream.Streams;
import ceri.common.test.Assert;
import ceri.common.test.Testing;

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
		Assert.equal(Colors.Compare.HUE.compare(null, null), 0);
		Assert.equal(Colors.Compare.HUE.compare(Color.white, null), 1);
		Assert.equal(Colors.Compare.HUE.compare(null, Color.black), -1);
		Assert.equal(Colors.Compare.HUE.compare(Color.black, Color.black), 0);
		Assert.equal(Colors.Compare.HUE.compare(Color.black, Color.white), 0);
		Assert.equal(Colors.Compare.HUE.compare(Color.red, Color.green), -1);
		Assert.equal(Colors.Compare.HUE.compare(Color.green, Color.blue), -1);
		Assert.equal(Colors.Compare.HUE.compare(Color.blue, Color.red), 1);
		Assert.equal(Colors.Compare.HUE.compare(Color.red, Color.pink), 0);
		Assert.equal(Colors.Compare.HUE.compare(Color.red, Color.magenta), -1);
	}

	@Test
	public void testCompareSaturation() {
		Assert.equal(Colors.Compare.SATURATION.compare(null, null), 0);
		Assert.equal(Colors.Compare.SATURATION.compare(Color.white, null), 1);
		Assert.equal(Colors.Compare.SATURATION.compare(null, Color.black), -1);
		Assert.equal(Colors.Compare.SATURATION.compare(Color.black, Color.black), 0);
		Assert.equal(Colors.Compare.SATURATION.compare(Color.black, Color.white), 0);
		Assert.equal(Colors.Compare.SATURATION.compare(Color.red, Color.green), 0);
		Assert.equal(Colors.Compare.SATURATION.compare(Color.green, Color.blue), 0);
		Assert.equal(Colors.Compare.SATURATION.compare(Color.blue, Color.red), 0);
		Assert.equal(Colors.Compare.SATURATION.compare(Color.red, Color.pink), 1);
		Assert.equal(Colors.Compare.SATURATION.compare(Color.red, Color.magenta), 0);
	}

	@Test
	public void testCompareBrightness() {
		Assert.equal(Colors.Compare.BRIGHTNESS.compare(null, null), 0);
		Assert.equal(Colors.Compare.BRIGHTNESS.compare(Color.white, null), 1);
		Assert.equal(Colors.Compare.BRIGHTNESS.compare(null, Color.black), -1);
		Assert.equal(Colors.Compare.BRIGHTNESS.compare(Color.black, Color.black), 0);
		Assert.equal(Colors.Compare.BRIGHTNESS.compare(Color.black, Color.white), -1);
		Assert.equal(Colors.Compare.BRIGHTNESS.compare(Color.red, Color.green), 0);
		Assert.equal(Colors.Compare.BRIGHTNESS.compare(Color.green, Color.blue), 0);
		Assert.equal(Colors.Compare.BRIGHTNESS.compare(Color.blue, Color.red), 0);
		Assert.equal(Colors.Compare.BRIGHTNESS.compare(Color.red, Color.pink), 0);
		Assert.equal(Colors.Compare.BRIGHTNESS.compare(Color.red, Color.magenta), 0);
	}

	@Test
	public void testCompareHsb() {
		Assert.equal(Colors.Compare.HSB.compare(null, null), 0);
		Assert.equal(Colors.Compare.HSB.compare(Color.white, null), 1);
		Assert.equal(Colors.Compare.HSB.compare(null, Color.black), -1);
		Assert.equal(Colors.Compare.HSB.compare(Color.black, Color.black), 0);
		Assert.equal(Colors.Compare.HSB.compare(Color.black, Color.white), -1);
		Assert.equal(Colors.Compare.HSB.compare(Color.red, Color.green), -1);
		Assert.equal(Colors.Compare.HSB.compare(Color.green, Color.blue), -1);
		Assert.equal(Colors.Compare.HSB.compare(Color.blue, Color.red), 1);
		Assert.equal(Colors.Compare.HSB.compare(Color.red, Color.pink), 1);
		Assert.equal(Colors.Compare.HSB.compare(Color.red, Color.magenta), -1);
	}

	@Test
	public void testFlattenArgb() {
		Assert.equal(Colors.flattenArgb(0x00802040), 0xff000000);
		Assert.equal(Colors.flattenArgb(0x80802040), 0xff401020);
		Assert.equal(Colors.flattenArgb(0xff802040), 0xff802040);
	}

	@Test
	public void testRgb() {
		Assert.equal(Colors.rgb(0x98765432), 0x765432);
		Assert.equal(Colors.rgb(Colors.color(0x98765432)), 0x765432);
	}

	@Test
	public void testArgb() {
		Assert.equal(Colors.argb(0x11, 0x22, 0x33), 0xff112233);
	}

	@Test
	public void testArgbFromText() {
		Assert.equal(Colors.argb("test"), null);
		Assert.equal(Colors.argb("clear"), 0);
		Assert.equal(Colors.argb("aquamarine"), Coloring.aquamarine.argb);
		Assert.equal(Colors.argb("#fed"), 0xffffeedd);
		Assert.equal(Colors.argb("0xfed"), 0xff000fed);
		Assert.equal(Colors.argb("#fedcba"), 0xfffedcba);
		Assert.equal(Colors.argb("0xfedcba"), 0xfffedcba);
		Assert.equal(Colors.argb("#fedcba98"), 0xfedcba98);
		Assert.equal(Colors.argb("0xfedcba98"), 0xfedcba98);
	}

	@Test
	public void testValidArgbFromText() {
		Assert.thrown(() -> Colors.validArgb("test"));
		Assert.equal(Colors.validArgb("aquamarine"), Coloring.aquamarine.argb);
	}

	@Test
	public void testGrayArgb() {
		Assert.equal(Colors.grayArgb(0), 0xff000000);
		Assert.equal(Colors.grayArgb(0xab), 0xffababab);
	}

	@Test
	public void testMaxArgb() {
		Assert.equal(Colors.maxArgb(0x80408020), 0x8080ff40);
		Assert.equal(Colors.maxArgb(0x8040ff20), 0x8040ff20);
		Assert.equal(Colors.maxArgb(0x80000000), 0x80000000);
	}

	@Test
	public void testRandomRgb() {
		Assert.equal(Colors.randomRgb() & 0xff000000, 0xff000000);
	}

	@Test
	public void testRandomArgb() {
		Colors.randomArgb();
	}

	@Test
	public void testBlendArgbs() {
		Assert.equal(Colors.blendArgbs(), 0);
		Assert.equal(Colors.blendArgbs(0x80aabbcc), 0x80aabbcc);
		Assert.equal(Colors.blendArgbs(0xffaabbcc, 0x40112233), 0xffaabbcc);
		Assert.equal(Colors.blendArgbs(0x80aabbcc, 0x112233), 0x80aabbcc);
		Assert.equal(Colors.blendArgbs(0xaabbcc, 0x40112233), 0x40112233);
		Assert.equal(Colors.blendArgbs(0x80aabbcc, 0x40112233), 0xa08b9cad);
		Assert.equal(Colors.blendArgbs(0x40112233, 0x80aabbcc), 0xa06d7e8f);
	}

	@Test
	public void testDimArgb() {
		Assert.equal(Colors.dimArgb(0xff664422, 0.0), 0xff000000);
		Assert.equal(Colors.dimArgb(0xff664422, 1.0), 0xff664422);
		Assert.equal(Colors.dimArgb(0xff664422, 0.5), 0xff332211);
		Assert.equal(Colors.dimArgb(0xff664422, 0.125), 0xff0d0904);
		Assert.equal(Colors.dimArgb(0x88664422, 0.5), 0x88332211);
		Assert.equal(Colors.dimArgb(0x88000000, 0.5), 0x88000000);
	}

	@Test
	public void testScaleArgb() {
		Assert.equal(Colors.scaleArgb(0xff224466, 0xff446622, 0), 0xff224466);
		Assert.equal(Colors.scaleArgb(0xff224466, 0xff446622, 1), 0xff446622);
		Assert.equal(Colors.scaleArgb(0xff224466, 0xff446622, 0.5), 0xff335544);
	}

	@Test
	public void testScaleHsbArgb() {
		Assert.equal(Colors.scaleHsbArgb(0xff804020, 0xff208040, 0), 0xff804020);
		Assert.equal(Colors.scaleHsbArgb(0xff804020, 0xff208040, 0.25), 0xff807020);
		Assert.equal(Colors.scaleHsbArgb(0xff804020, 0xff208040, 0.5), 0xff608020);
		Assert.equal(Colors.scaleHsbArgb(0xff804020, 0xff208040, 0.75), 0xff308020);
		Assert.equal(Colors.scaleHsbArgb(0xff804020, 0xff208040, 1), 0xff208040);
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
		Assert.equal(Colors.a(0x88776655, 0xaa), 0xaa776655);
		Assert.equal(Colors.r(0x88776655, 0xaa), 0x88aa6655);
		Assert.equal(Colors.g(0x88776655, 0xaa), 0x8877aa55);
		Assert.equal(Colors.b(0x88776655, 0xaa), 0x887766aa);
	}

	@Test
	public void testSetComponent() {
		var c = Colors.color(0x88776655);
		assertColor(Colors.a(c, 0xaa), 0xaa776655);
		assertColor(Colors.r(c, 0xaa), 0x88aa6655);
		assertColor(Colors.g(c, 0xaa), 0x8877aa55);
		assertColor(Colors.b(c, 0xaa), 0x887766aa);
		Assert.same(Colors.a(c, 0x88), c);
		Assert.same(Colors.r(c, 0x77), c);
		Assert.same(Colors.g(c, 0x66), c);
		Assert.same(Colors.b(c, 0x55), c);
	}

	@Test
	public void testValidColorFromText() {
		Assert.thrown(() -> Colors.validColor("test"));
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
		Assert.equal(Colors.random().getRGB() & 0xff000000, 0xff000000);
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
		Assert.equal(Colors.a(Colors.color(0x98765432)), 0x98);
		Assert.equal(Colors.r(Colors.color(0x98765432)), 0x76);
		Assert.equal(Colors.g(Colors.color(0x98765432)), 0x54);
		Assert.equal(Colors.b(Colors.color(0x98765432)), 0x32);
	}

	@Test
	public void testValidValue() {
		Assert.equal(Colors.validValue(0), 0);
		Assert.equal(Colors.validValue(255), 255);
		Assert.illegalArg(() -> Colors.validValue(-1));
		Assert.illegalArg(() -> Colors.validValue(256));
	}

	@Test
	public void testValidRatio() {
		Assert.equal(Colors.validRatio(0.0), 0.0);
		Assert.equal(Colors.validRatio(1.0), 1.0);
		Assert.illegalArg(() -> Colors.validRatio(-0.1));
		Assert.illegalArg(() -> Colors.validRatio(1.1));
	}

	@Test
	public void testScaleHue() {
		Assert.approx(Colors.scaleHue(0.9, 0.3, 0.0), 0.9);
		Assert.approx(Colors.scaleHue(0.9, 0.3, 0.5), 0.1);
		Assert.approx(Colors.scaleHue(0.9, 0.3, 1.0), 0.3);
	}

	@Test
	public void testScaleValue() {
		Assert.equal(Colors.scaleValue(0xde, 0x51, 0.0), 0xde);
		Assert.equal(Colors.scaleValue(0xde, 0x51, 0.5), 0x98);
		Assert.equal(Colors.scaleValue(0xde, 0x51, 1.0), 0x51);
	}

	@Test
	public void testScaleRatio() {
		Assert.approx(Colors.scaleRatio(0.9, 0.3, 0.0), 0.9);
		Assert.approx(Colors.scaleRatio(0.9, 0.3, 0.5), 0.6);
		Assert.approx(Colors.scaleRatio(0.9, 0.3, 1.0), 0.3);
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
		Assert.equal(Colors.toString(Colors.clear), "#00000000(clear)");
		Assert.equal(Colors.toString(Coloring.aquamarine.color()), "#7fffd4(aquamarine)");
		Assert.equal(Colors.toString(Coloring.aquamarine.argb), "#7fffd4(aquamarine)");
		Assert.equal(Colors.toString(0xffffeedd), "#ffeedd");
	}

	@Test
	public void testName() {
		Assert.equal(Colors.name(Colors.clear), "clear");
		Assert.equal(Colors.name(Coloring.aquamarine.color()), "aquamarine");
		Assert.equal(Colors.name(Colors.color(0x123456)), null);
	}

	@Test
	public void testHex() {
		Assert.equal(Colors.hex(Colors.color(0x123456)), "#00123456");
		Assert.equal(Colors.hex(Colors.color(0xff123456)), "#123456");
	}

	@Test
	public void testArgbsFromText() {
		Assert.array(Colors.argbs("clear", "#def", "0x12345678"), 0, 0xffddeeff, 0x12345678);
		Assert.thrown(() -> Colors.argbs("test"));
	}

	@Test
	public void testColorsFromText() {
		Assert.array(Colors.colors("clear", "#def", "0x12345678"), Colors.clear,
			Colors.color(0xffddeeff), Colors.color(0x12345678));
		Assert.thrown(() -> Colors.colors("test"));
	}

	@Test
	public void testArgbList() {
		Assert.ordered(Colors.argbList(Streams.ints(0, 0x12345678)), 0, 0x12345678);
	}

	@Test
	public void testColorList() {
		Assert.ordered(Colors.colorList(Streams.ints(0, 0x12345678)), Colors.clear,
			Colors.color(0x12345678));
	}

	@Test
	public void testArgbStream() {
		Assert.stream(Colors.rgbStream(0, 0x12345678), 0xff000000, 0xff345678);
	}

	@Test
	public void testFadeStream() {
		Assert.stream(
			Colors.fadeStream(Colors.color(0x204060), Colors.color(0x406020), 4, Bias.NONE),
			0x284850, 0x305040, 0x385830, 0x406020);
	}

	@Test
	public void testFadeHsbStream() {
		Assert.stream(
			Colors.fadeHsbStream(Colors.color(0x204060), Colors.color(0x406020), 4, Bias.NONE),
			0xff206060, 0xff206040, 0xff206020, 0xff406020);
	}

	@Test
	public void testRotateHueStream() {
		Assert.stream(Colors.rotateHueStream(Coloring.aquamarine.color(), 4, Bias.NONE), 0xff947fff,
			0xffff7faa, 0xffeaff7f, 0xff7fffd4);
	}

	private static void exerciseCompare(Comparator<Color> comparator, int c, int lt, int eq,
		int gt) {
		Testing.exerciseCompare(comparator, Colors.color(c), Colors.color(lt), Colors.color(eq),
			Colors.color(gt));
	}
}
