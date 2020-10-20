package ceri.common.color;

import static ceri.common.collection.StreamUtil.toList;
import static ceri.common.color.ColorTestUtil.assertColorx;
import static ceri.common.color.Colorx.black;
import static ceri.common.color.Colorx.full;
import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertIterable;
import static ceri.common.test.AssertUtil.assertNotNull;
import static ceri.common.test.AssertUtil.assertNull;
import static ceri.common.test.AssertUtil.assertPrivateConstructor;
import static ceri.common.test.AssertUtil.assertThrown;
import static java.awt.Color.green;
import static java.awt.Color.red;
import java.awt.Color;
import java.util.List;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import java.util.stream.IntStream;
import org.junit.Test;
import ceri.common.color.ColorUtil.Fn.ChannelAdjuster;
import ceri.common.color.ColorUtil.Fn.ChannelScaler;
import ceri.common.function.BinaryFunction;

public class ColorxUtilTest {

	@Test
	public void testConstructorIsPrivate() {
		assertPrivateConstructor(ColorxUtil.class);
		assertPrivateConstructor(ColorxUtil.Fn.class);
	}

	@Test
	public void testFadeHsbxFunction() {
		assertColorxs(ColorxUtil.Fn.fadeHsbx(2).apply( //
			Colorx.of(red, 0x44), Colorx.of(green, 0x66)), 0xffff0055, 0x00ff0066);
	}

	@Test
	public void testScaleHsbxFunction() {
		assertColorx(ColorxUtil.Fn.scaleHsbx(0.5).apply( //
			Colorx.of(red, 0x44), Colorx.of(green, 0x66)), 0xffff0055);
	}

	@Test
	public void testTransformRgbUnaryToListFunction() {
		assertColorxs(ColorxUtil.Fn.transform(ColorUtil.Fn.rotateHue(2)) //
			.apply(Colorx.of(0xff000055)), 0x00ffff55, 0xff000055);
		ChannelAdjuster adj = (x, r) -> (int) (x * r);
		assertColorxs(ColorxUtil.Fn.transform(ColorUtil.Fn.rotateHue(2), adj) //
			.apply(Colorx.of(0xff000066)), 0x00ffff33, 0xff000066);

		assertNull(ColorxUtil.Fn.transform(ColorUtil.Fn.rotateHue(2)).apply(null));
		assertColorxs(ColorxUtil.Fn.transform((Function<Color, List<Color>>) null)
			.apply(Colorx.of(0xff000055)));
		assertColorxs(ColorxUtil.Fn.transform(ColorUtil.Fn.rotateHue(2), null) //
			.apply(Colorx.of(0xff000066)), 0x00ffff00, 0xff000000);
	}

	@Test
	public void testTransformRgbBinaryToListFunction() {
		assertColorxs(ColorxUtil.Fn.transform(ColorUtil.Fn.fade(2)) //
			.apply(full, black), 0x80808080, 0);
		assertColorxs(ColorxUtil.Fn.transform(ColorUtil.Fn.fade(2), ChannelScaler.of()) //
			.apply(full, black), 0x80808080, 0);
		assertColorxs(ColorxUtil.Fn.transform(ColorUtil.Fn.fade(2), r -> 0.0) //
			.apply(full, black), 0x808080ff, 0x000000ff);

		assertNull(ColorxUtil.Fn.transform(ColorUtil.Fn.fade(2)).apply(null, black));
		assertNull(ColorxUtil.Fn.transform(ColorUtil.Fn.fade(2)).apply(full, null));
		assertColorxs(ColorxUtil.Fn.transform((BinaryFunction<Color, List<Color>>) null) //
			.apply(full, black));
		assertColorxs(ColorxUtil.Fn.transform(ColorUtil.Fn.fade(2), (ChannelScaler) null) //
			.apply(full, black), 0x80808000, 0);
	}

	@Test
	public void testTransformRgbBinaryOperator() {
		assertColorx(ColorxUtil.Fn.transform(ColorUtil.Fn.scale(0.5)) //
			.apply(full, black), 0x80808080);
		assertColorx(ColorxUtil.Fn.transform(ColorUtil.Fn.scale(0.5), Math::max).apply(full, black),
			0x808080ff);
		assertColorx(ColorxUtil.Fn.transform(ColorUtil.Fn.scale(0.5)).apply(null, black), black);
		assertColorx(ColorxUtil.Fn.transform(ColorUtil.Fn.scale(0.5)).apply(full, null), full);
		assertColorx(ColorxUtil.Fn.transform((BinaryOperator<Color>) null) //
			.apply(full, black), full);
	}

	@Test
	public void testTransformRgbUnaryOperator() {
		assertColorx(ColorxUtil.Fn.transform(ColorUtil.Fn.dim(0.5)).apply(full), 0x808080ff);
		assertColorx(ColorxUtil.Fn.transform(ColorUtil.Fn.dim(0.5), x -> x / 4) //
			.apply(full), 0x8080803f);
		assertNull(ColorxUtil.Fn.transform(ColorUtil.Fn.dim(0.5)).apply(null));
		assertColorx(ColorxUtil.Fn.transform((UnaryOperator<Color>) null).apply(full), full);
	}

	@Test
	public void testApplyUnaryFunction() {
		assertColorx(ColorxUtil.Fn.apply(full, ColorxUtil.Fn.dim(0.5)), 0x80808080);
		assertColorx(ColorxUtil.Fn.apply(full, (UnaryOperator<Colorx>) null), full);
		assertNull(ColorxUtil.Fn.apply(null, ColorxUtil.Fn.dim(0.5)));
	}

	@Test
	public void testApplyBinaryToListFunction() {
		assertColorxs(ColorxUtil.Fn.apply(full, black, ColorxUtil.Fn.fade(2)), 0x80808080, 0);
		assertNull(ColorxUtil.Fn.apply(null, black, ColorxUtil.Fn.fade(2)));
		assertNull(ColorxUtil.Fn.apply(full, null, ColorxUtil.Fn.fade(2)));
		assertColorxs(
			ColorxUtil.Fn.apply(full, black, (BinaryFunction<Colorx, List<Colorx>>) null));
	}

	@Test
	public void testApplyBinaryOperator() {
		assertColorx(ColorxUtil.Fn.apply(full, black, ColorxUtil.Fn.scale(0.5)), 0x80808080);
		assertColorx(ColorxUtil.Fn.apply(null, black, ColorxUtil.Fn.scale(0.5)), black);
		assertColorx(ColorxUtil.Fn.apply(full, null, ColorxUtil.Fn.scale(0.5)), full);
		assertColorx(ColorxUtil.Fn.apply(full, black, (BinaryOperator<Colorx>) null), full);
	}

	@Test
	public void testApplyUnaryToListFunction() {
		assertColorxs(ColorxUtil.Fn.apply(Colorx.of(0xff000055), ColorxUtil.Fn.rotateHuex(2)),
			0x00ffff55, 0xff000055);
		assertNull(ColorxUtil.Fn.apply(null, ColorxUtil.Fn.rotateHuex(2)));
		assertIterable(
			ColorxUtil.Fn.apply(Colorx.of(0xff000055), (Function<Colorx, List<Colorx>>) null));
	}

	@Test
	public void testMax() {
		assertColorx(ColorxUtil.max(black), 0xffffffff);
		assertColorx(ColorxUtil.max(Colorx.of(0x80402010)), 0xff7f3f1f);
	}

	@Test
	public void testValidColor() {
		assertThrown(() -> ColorxUtil.validColor(null));
		assertThrown(() -> ColorxUtil.validColor("\0black"));
		assertColorx(ColorxUtil.validColor("black"), black);
	}

	@Test
	public void testColor() {
		assertNull(ColorxUtil.color(null));
		assertColorx(ColorxUtil.color("#4321"), 0x44332211);
		assertColorx(ColorxUtil.color("0xfedcba98"), 0xfedcba98);
		assertColorx(ColorxUtil.color("#fedcba98"), 0xfedcba98);
		assertColorx(ColorxUtil.color("magenta"), Color.magenta, 0);
		assertColorx(ColorxUtil.color("#432"), 0x44332200);
		assertColorx(ColorxUtil.color("full"), 0xffffffff);
	}

	@Test
	public void testColorFromName() {
		assertNull(ColorxUtil.colorFromName(null));
		assertColorx(ColorxUtil.colorFromName("full"), full);
		assertColorx(ColorxUtil.colorFromName("black"), black);
		assertColorx(ColorxUtil.colorFromName("magenta"), Color.magenta, 0);
		assertNull(ColorxUtil.colorFromName("#aabbccdd"));
		assertNull(ColorxUtil.colorFromName("#aabbcc"));
	}

	@Test
	public void testColors() {
		assertColorxs(ColorxUtil.colors("#4321", "0xfedcba98"), 0x44332211, 0xfedcba98);
		assertColorxs(ColorxUtil.colors(0x44332211, 0xfedcba98, 0), 0x44332211, 0xfedcba98, 0);
	}

	@Test
	public void testAlphaColor() {
		assertColorx(ColorxUtil.alphaColor(Colorx.of(0x12345678), 0x9a), 0x12, 0x34, 0x56, 0x78,
			0x9a);
	}

	@Test
	public void testToStrings() {
		assertIterable(ColorxUtil.toStrings(full, Colorx.of(Color.pink, 0)), "full", "pink");
	}

	@Test
	public void testToString() {
		assertNull(ColorxUtil.toString(null));
		assertEquals(ColorxUtil.toString(full), "full");
		assertEquals(ColorxUtil.toString(0xff, 0xff, 0, 0), "yellow");
		assertEquals(ColorxUtil.toString(0xff, 0xff, 0, 0xff), "#ffff00ff");
	}

	@Test
	public void testToName() {
		assertNull(ColorxUtil.toName(null));
		assertNull(ColorxUtil.toName(0x12345678));
		assertEquals(ColorxUtil.toName(full), "full");
		assertEquals(ColorxUtil.toName(black), "black");
		assertEquals(ColorxUtil.toName(0xff, 0, 0xff, 0), "magenta");
	}

	@Test
	public void testToHex() {
		assertNull(ColorxUtil.toHex(null));
		assertEquals(ColorxUtil.toHex(black), "#00000000");
		assertEquals(ColorxUtil.toHex(full), "#ffffffff");
		assertEquals(ColorxUtil.toHex(0x88664422), "#88664422");
		assertEquals(ColorxUtil.toHex(16, 32, 64, 128), "#10204080");
	}

	@Test
	public void testFade() {
		assertColorxs(ColorxUtil.fade(full, black, 4), 0xbfbfbfbf, 0x80808080, 0x40404040, 0);
		assertColorxs(ColorxUtil.fade(0x80604020, 0x20406080, 4), 0x68584838, 0x50505050,
			0x38485868, 0x20406080);
	}

	@Test
	public void testFadeHsbx() {
		assertColorxs(ColorxUtil.fadeHsbx(0xff000077, 0x00ff0033, 2), 0xffff0055, 0x00ff0033);
		assertColorxs(ColorxUtil.fadeHsbx(Colorx.of(0xff000077), Colorx.of(0x00ff0033), 2),
			0xffff0055, 0x00ff0033);
	}

	@Test
	public void testScaleHsbx() {
		assertColorx(ColorxUtil.scaleHsbx(0xff000033, 0x00ff0055, 0.5), 0xffff0044);
		assertColorx(ColorxUtil.scaleHsbx(null, Colorx.of(0x00ff0055), 0.5), 0x00ff0055);
		assertColorx(ColorxUtil.scaleHsbx(Colorx.of(0xff000033), null, 0.5), 0xff000033);
		assertColorx(ColorxUtil.scaleHsbx(0xff000033, 0x00ff0055, 0.0), 0xff000033);
		assertColorx(ColorxUtil.scaleHsbx(0xff000033, 0x00ff0055, 1.0), 0x00ff0055);
	}

	@Test
	public void testScale() {
		assertColorx(ColorxUtil.scale(0, 0x80402010, 0), 0);
		assertColorx(ColorxUtil.scale(0, 0x80402010, 0.25), 0x20100804);
	}

	@Test
	public void testRotateHuex() {
		assertColorxs(ColorxUtil.rotateHuex(0xff000077, 2), 0x00ffff77, 0xff000077);
		assertColorxs(ColorxUtil.rotateHuex(Colorx.of(red, 0x77), 2), 0x00ffff77, 0xff000077);
	}

	@Test
	public void testDimAll() {
		assertColorxs(ColorxUtil.dimAll(0.5, 0, 0x80402010, 0xffffffff), 0, 0x40201008, 0x80808080);
		assertColorxs(ColorxUtil.dimAll(0.5, black, Colorx.of(0x80402010), full), 0, 0x40201008,
			0x80808080);
	}

	@Test
	public void testDim() {
		assertColorx(ColorxUtil.dim(0x10204080, 0.75), 0x0c183060);
	}

	@Test
	public void testRandom() {
		assertNotNull(ColorxUtil.random());
	}

	private void assertColorxs(Iterable<Colorx> colorxs, int... rgbxs) {
		assertIterable(colorxs, toList(IntStream.of(rgbxs).mapToObj(Colorx::of)));
	}

}
