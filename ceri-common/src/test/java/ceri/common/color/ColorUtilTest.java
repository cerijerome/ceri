package ceri.common.color;

import static ceri.common.color.ColorTestUtil.assertColor;
import static ceri.common.color.ColorTestUtil.assertHsb;
import static ceri.common.color.ColorUtil.color;
import static ceri.common.test.AssertUtil.assertApprox;
import static ceri.common.test.AssertUtil.assertArray;
import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertOrdered;
import static ceri.common.test.AssertUtil.assertSame;
import static ceri.common.test.AssertUtil.assertStream;
import static ceri.common.test.AssertUtil.assertThrown;
import java.util.stream.IntStream;
import org.junit.Test;

public class ColorUtilTest {

	@Test
	public void testFlattenArgb() {
		assertEquals(ColorUtil.flattenArgb(0x00802040), 0xff000000);
		assertEquals(ColorUtil.flattenArgb(0x80802040), 0xff401020);
		assertEquals(ColorUtil.flattenArgb(0xff802040), 0xff802040);
	}

	@Test
	public void testRgb() {
		assertEquals(ColorUtil.rgb(0x98765432), 0x765432);
		assertEquals(ColorUtil.rgb(color(0x98765432)), 0x765432);
	}

	@Test
	public void testArgb() {
		assertEquals(ColorUtil.argb(0x11, 0x22, 0x33), 0xff112233);
	}

	@Test
	public void testArgbFromText() {
		assertEquals(ColorUtil.argb("test"), null);
		assertEquals(ColorUtil.argb("clear"), 0);
		assertEquals(ColorUtil.argb("aquamarine"), Colors.aquamarine.argb);
		assertEquals(ColorUtil.argb("#fed"), 0xffffeedd);
		assertEquals(ColorUtil.argb("0xfed"), 0xff000fed);
		assertEquals(ColorUtil.argb("#fedcba"), 0xfffedcba);
		assertEquals(ColorUtil.argb("0xfedcba"), 0xfffedcba);
		assertEquals(ColorUtil.argb("#fedcba98"), 0xfedcba98);
		assertEquals(ColorUtil.argb("0xfedcba98"), 0xfedcba98);
	}

	@Test
	public void testValidArgbFromText() {
		assertThrown(() -> ColorUtil.validArgb("test"));
		assertEquals(ColorUtil.validArgb("aquamarine"), Colors.aquamarine.argb);
	}

	@Test
	public void testGrayArgb() {
		assertEquals(ColorUtil.grayArgb(0), 0xff000000);
		assertEquals(ColorUtil.grayArgb(0xab), 0xffababab);
	}

	@Test
	public void testMaxArgb() {
		assertEquals(ColorUtil.maxArgb(0x80408020), 0x8080ff40);
		assertEquals(ColorUtil.maxArgb(0x8040ff20), 0x8040ff20);
		assertEquals(ColorUtil.maxArgb(0x80000000), 0x80000000);
	}

	@Test
	public void testRandomRgb() {
		assertEquals(ColorUtil.randomRgb() & 0xff000000, 0xff000000);
	}

	@Test
	public void testRandomArgb() {
		ColorUtil.randomArgb();
	}

	@Test
	public void testBlendArgbs() {
		assertEquals(ColorUtil.blendArgbs(), 0);
		assertEquals(ColorUtil.blendArgbs(0x80aabbcc), 0x80aabbcc);
		assertEquals(ColorUtil.blendArgbs(0xffaabbcc, 0x40112233), 0xffaabbcc);
		assertEquals(ColorUtil.blendArgbs(0x80aabbcc, 0x112233), 0x80aabbcc);
		assertEquals(ColorUtil.blendArgbs(0xaabbcc, 0x40112233), 0x40112233);
		assertEquals(ColorUtil.blendArgbs(0x80aabbcc, 0x40112233), 0xa08b9cad);
		assertEquals(ColorUtil.blendArgbs(0x40112233, 0x80aabbcc), 0xa06d7e8f);
	}

	@Test
	public void testDimArgb() {
		assertEquals(ColorUtil.dimArgb(0xff664422, 0.0), 0xff000000);
		assertEquals(ColorUtil.dimArgb(0xff664422, 1.0), 0xff664422);
		assertEquals(ColorUtil.dimArgb(0xff664422, 0.5), 0xff332211);
		assertEquals(ColorUtil.dimArgb(0xff664422, 0.125), 0xff0d0904);
		assertEquals(ColorUtil.dimArgb(0x88664422, 0.5), 0x88332211);
		assertEquals(ColorUtil.dimArgb(0x88000000, 0.5), 0x88000000);
	}

	@Test
	public void testScaleArgb() {
		assertEquals(ColorUtil.scaleArgb(0xff224466, 0xff446622, 0), 0xff224466);
		assertEquals(ColorUtil.scaleArgb(0xff224466, 0xff446622, 1), 0xff446622);
		assertEquals(ColorUtil.scaleArgb(0xff224466, 0xff446622, 0.5), 0xff335544);
	}

	@Test
	public void testScaleHsbArgb() {
		assertEquals(ColorUtil.scaleHsbArgb(0xff804020, 0xff208040, 0), 0xff804020);
		assertEquals(ColorUtil.scaleHsbArgb(0xff804020, 0xff208040, 0.25), 0xff807020);
		assertEquals(ColorUtil.scaleHsbArgb(0xff804020, 0xff208040, 0.5), 0xff608020);
		assertEquals(ColorUtil.scaleHsbArgb(0xff804020, 0xff208040, 0.75), 0xff308020);
		assertEquals(ColorUtil.scaleHsbArgb(0xff804020, 0xff208040, 1), 0xff208040);
	}

	@Test
	public void testFlatten() {
		assertColor(ColorUtil.flatten(color(0x00802040)), 0xff000000);
		assertColor(ColorUtil.flatten(color(0x80802040)), 0xff401020);
		assertColor(ColorUtil.flatten(color(0xff802040)), 0xff802040);
	}

	@Test
	public void testColorFromComponents() {
		assertColor(color(0x80, 0x70, 0xff, 0), 0x8070ff00);
	}

	@Test
	public void testColorFromText() {
		assertColor(color("test"), null);
		assertColor(color("clear"), 0);
		assertColor(color("aquamarine"), Colors.aquamarine.argb);
		assertColor(color("#fed"), 0xffffeedd);
		assertColor(color("0xfed"), 0xff000fed);
		assertColor(color("#fedcba98"), 0xfedcba98);
	}

	@Test
	public void testSetArgbComponent() {
		assertEquals(ColorUtil.a(0x88776655, 0xaa), 0xaa776655);
		assertEquals(ColorUtil.r(0x88776655, 0xaa), 0x88aa6655);
		assertEquals(ColorUtil.g(0x88776655, 0xaa), 0x8877aa55);
		assertEquals(ColorUtil.b(0x88776655, 0xaa), 0x887766aa);
	}

	@Test
	public void testSetComponent() {
		var c = color(0x88776655);
		assertColor(ColorUtil.a(c, 0xaa), 0xaa776655);
		assertColor(ColorUtil.r(c, 0xaa), 0x88aa6655);
		assertColor(ColorUtil.g(c, 0xaa), 0x8877aa55);
		assertColor(ColorUtil.b(c, 0xaa), 0x887766aa);
		assertSame(ColorUtil.a(c, 0x88), c);
		assertSame(ColorUtil.r(c, 0x77), c);
		assertSame(ColorUtil.g(c, 0x66), c);
		assertSame(ColorUtil.b(c, 0x55), c);
	}

	@Test
	public void testValidColorFromText() {
		assertThrown(() -> ColorUtil.validColor("test"));
		assertColor(ColorUtil.validColor("aquamarine"), Colors.aquamarine.argb);
	}

	@Test
	public void testGray() {
		assertColor(ColorUtil.gray(0), 0xff000000);
		assertColor(ColorUtil.gray(0xab), 0xffababab);
	}

	@Test
	public void testMax() {
		assertColor(ColorUtil.max(color(0x80408020)), 0x8080ff40);
		assertColor(ColorUtil.max(color(0x8040ff20)), 0x8040ff20);
		assertColor(ColorUtil.max(color(0x80000000)), 0x80000000);
	}

	@Test
	public void testRandom() {
		assertEquals(ColorUtil.random().getRGB() & 0xff000000, 0xff000000);
	}

	@Test
	public void testDim() {
		assertColor(ColorUtil.dim(color(0xff664422), 0.0), 0xff000000);
		assertColor(ColorUtil.dim(color(0xff664422), 1.0), 0xff664422);
		assertColor(ColorUtil.dim(color(0xff664422), 0.5), 0xff332211);
		assertColor(ColorUtil.dim(color(0xff664422), 0.125), 0xff0d0904);
		assertColor(ColorUtil.dim(color(0x88664422), 0.5), 0x88332211);
		assertColor(ColorUtil.dim(color(0x88000000), 0.5), 0x88000000);
	}

	@Test
	public void testScale() {
		assertColor(ColorUtil.scale(color(0xff224466), color(0xff446622), 0), 0xff224466);
		assertColor(ColorUtil.scale(color(0xff224466), color(0xff446622), 1), 0xff446622);
		assertColor(ColorUtil.scale(color(0xff224466), color(0xff446622), 0.5), 0xff335544);
	}

	@Test
	public void testScaleHsb() {
		assertColor(ColorUtil.scaleHsb(color(0xff804020), color(0xff208040), 0), 0xff804020);
		assertColor(ColorUtil.scaleHsb(color(0xff804020), color(0xff208040), 0.25), 0xff807020);
		assertColor(ColorUtil.scaleHsb(color(0xff804020), color(0xff208040), 0.5), 0xff608020);
		assertColor(ColorUtil.scaleHsb(color(0xff804020), color(0xff208040), 0.75), 0xff308020);
		assertColor(ColorUtil.scaleHsb(color(0xff804020), color(0xff208040), 1), 0xff208040);
	}

	@Test
	public void testScaleHsbColors() {
		assertHsb(ColorUtil.scaleHsb(HsbColor.of(0.5, 0.8, 0.4), HsbColor.of(0.9, 0.4, 0.6), 0.0),
			0.5, 0.8, 0.4);
		assertHsb(ColorUtil.scaleHsb(HsbColor.of(0.5, 0.8, 0.4), HsbColor.of(0.9, 0.4, 0.6), 0.5),
			0.7, 0.6, 0.5);
		assertHsb(ColorUtil.scaleHsb(HsbColor.of(0.5, 0.8, 0.4), HsbColor.of(0.9, 0.4, 0.6), 1.0),
			0.9, 0.4, 0.6);
	}

	@Test
	public void testComponentAccess() {
		assertEquals(ColorUtil.a(color(0x98765432)), 0x98);
		assertEquals(ColorUtil.r(color(0x98765432)), 0x76);
		assertEquals(ColorUtil.g(color(0x98765432)), 0x54);
		assertEquals(ColorUtil.b(color(0x98765432)), 0x32);
	}

	@Test
	public void testScaleHue() {
		assertApprox(ColorUtil.scaleHue(0.9, 0.3, 0.0), 0.9);
		assertApprox(ColorUtil.scaleHue(0.9, 0.3, 0.5), 0.1);
		assertApprox(ColorUtil.scaleHue(0.9, 0.3, 1.0), 0.3);
	}

	@Test
	public void testScaleValue() {
		assertEquals(ColorUtil.scaleValue(0xde, 0x51, 0.0), 0xde);
		assertEquals(ColorUtil.scaleValue(0xde, 0x51, 0.5), 0x98);
		assertEquals(ColorUtil.scaleValue(0xde, 0x51, 1.0), 0x51);
	}

	@Test
	public void testScaleRatio() {
		assertApprox(ColorUtil.scaleRatio(0.9, 0.3, 0.0), 0.9);
		assertApprox(ColorUtil.scaleRatio(0.9, 0.3, 0.5), 0.6);
		assertApprox(ColorUtil.scaleRatio(0.9, 0.3, 1.0), 0.3);
	}

	@Test
	public void testBlendAssociativity() {
		var c0 = ColorUtil.a(Colors.aquamarine.argb, 0x80);
		var c1 = ColorUtil.a(Colors.blueViolet.argb, 0x40);
		var c2 = ColorUtil.a(Colors.coral.argb, 0x20);
		var c21 = ColorUtil.blendArgbs(c2, c1);
		var c10 = ColorUtil.blendArgbs(c1, c0);
		var c21_0 = ColorUtil.blendArgbs(c21, c0);
		var c2_10 = ColorUtil.blendArgbs(c2, c10);
		ColorTestUtil.assertArgbDiff(c21_0, c2_10, 1);
	}

	@Test
	public void testToString() {
		assertEquals(ColorUtil.toString(ColorUtil.clear), "#00000000(clear)");
		assertEquals(ColorUtil.toString(Colors.aquamarine.color()), "#7fffd4(aquamarine)");
		assertEquals(ColorUtil.toString(Colors.aquamarine.argb), "#7fffd4(aquamarine)");
		assertEquals(ColorUtil.toString(0xffffeedd), "#ffeedd");
	}

	@Test
	public void testName() {
		assertEquals(ColorUtil.name(ColorUtil.clear), "clear");
		assertEquals(ColorUtil.name(Colors.aquamarine.color()), "aquamarine");
		assertEquals(ColorUtil.name(color(0x123456)), null);
	}

	@Test
	public void testHex() {
		assertEquals(ColorUtil.hex(color(0x123456)), "#00123456");
		assertEquals(ColorUtil.hex(color(0xff123456)), "#123456");
	}

	@Test
	public void testArgbsFromText() {
		assertArray(ColorUtil.argbs("clear", "#def", "0x12345678"), 0, 0xffddeeff, 0x12345678);
		assertThrown(() -> ColorUtil.argbs("test"));
	}

	@Test
	public void testColorsFromText() {
		assertArray(ColorUtil.colors("clear", "#def", "0x12345678"), ColorUtil.clear,
			color(0xffddeeff), color(0x12345678));
		assertThrown(() -> ColorUtil.colors("test"));
	}

	@Test
	public void testArgbList() {
		assertOrdered(ColorUtil.argbList(IntStream.of(0, 0x12345678)), 0, 0x12345678);
	}

	@Test
	public void testColorList() {
		assertOrdered(ColorUtil.colorList(IntStream.of(0, 0x12345678)), ColorUtil.clear,
			color(0x12345678));
	}

	@Test
	public void testArgbStream() {
		assertStream(ColorUtil.rgbStream(0, 0x12345678), 0xff000000, 0xff345678);
	}

	@Test
	public void testFadeStream() {
		assertStream(ColorUtil.fadeStream(color(0x204060), color(0x406020), 4, Bias.NONE), 0x284850,
			0x305040, 0x385830, 0x406020);
	}

	@Test
	public void testFadeHsbStream() {
		assertStream(ColorUtil.fadeHsbStream(color(0x204060), color(0x406020), 4, Bias.NONE),
			0xff206060, 0xff206040, 0xff206020, 0xff406020);
	}

	@Test
	public void testRotateHueStream() {
		assertStream(ColorUtil.rotateHueStream(Colors.aquamarine.color(), 4, Bias.NONE), 0xff947fff,
			0xffff7faa, 0xffeaff7f, 0xff7fffd4);
	}

}
