package ceri.common.color;

import static ceri.common.collection.StreamUtil.toList;
import static ceri.common.color.ColorTestUtil.assertColorx;
import static ceri.common.test.TestUtil.assertException;
import static ceri.common.test.TestUtil.assertIterable;
import static ceri.common.test.TestUtil.assertPrivateConstructor;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import java.awt.Color;
import java.util.stream.IntStream;
import org.junit.Test;

public class ColorxUtilTest {

	@Test
	public void testConstructorIsPrivate() {
		assertPrivateConstructor(ColorxUtil.class);
	}

	@Test
	public void testApplyRgb() {
		Colorx colorx = Colorx.of(0x80402010);
		assertColorx(ColorxUtil.applyRgb(colorx, null), colorx);
		assertColorx(ColorxUtil.applyRgb(colorx, c -> ColorUtil.dim(c, 0.5)), 0x40201010);
		assertColorx(ColorxUtil.applyRgb(colorx, c -> ColorUtil.dim(c, 1.0)), colorx);
	}

	@Test
	public void testMax() {
		assertColorx(ColorxUtil.max(Colorx.black), 0xffffffff);
		assertColorx(ColorxUtil.max(Colorx.of(0x80402010)), 0xff7f3f1f);
	}

	@Test
	public void testValidColor() {
		assertException(() -> ColorxUtil.validColor(null));
		assertException(() -> ColorxUtil.validColor("\0black"));
		assertColorx(ColorxUtil.validColor("black"), Colorx.black);
	}

	@Test
	public void testColor() {
		assertNull(ColorxUtil.color(null));
		assertColorx(ColorxUtil.color("#4321"), 0x44332211);
		assertColorx(ColorxUtil.color("0xfedcba98"), 0xfedcba98);
		assertColorx(ColorxUtil.color("#fedcba98"), 0xfedcba98);
		assertColorx(ColorxUtil.color("magenta"), Color.magenta, 0);
		assertColorx(ColorxUtil.color("#432"), 0x00000432);
		assertColorx(ColorxUtil.color("full"), 0xffffffff);
	}

	@Test
	public void testColorFromName() {
		assertNull(ColorxUtil.colorFromName(null));
		assertColorx(ColorxUtil.colorFromName("full"), Colorx.full);
		assertColorx(ColorxUtil.colorFromName("black"), Colorx.black);
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
	public void testToHex() {
		assertNull(ColorxUtil.toHex(null));
		assertThat(ColorxUtil.toHex(Colorx.black), is("#00000000"));
		assertThat(ColorxUtil.toHex(Colorx.full), is("#ffffffff"));
		assertThat(ColorxUtil.toHex(0x88664422), is("#88664422"));
		assertThat(ColorxUtil.toHex(16, 32, 64, 128), is("#10204080"));
	}

	@Test
	public void testFade() {
		assertColorxs(ColorxUtil.fade(Colorx.full, Colorx.black, 4), 0xbfbfbfbf, 0x80808080,
			0x40404040, 0);
		assertColorxs(ColorxUtil.fade(0x80604020, 0x20406080, 4), 0x68584838, 0x50505050,
			0x38485868, 0x20406080);
	}

	@Test
	public void testScale() {
		assertColorx(ColorxUtil.scale(0, 0x80402010, 0), 0);
		assertColorx(ColorxUtil.scale(0, 0x80402010, 0.25), 0x20100804);
	}

	@Test
	public void testDimAll() {
		assertColorxs(ColorxUtil.dimAll(0.5, 0, 0x80402010, 0xffffffff), 0, 0x40201008, 0x80808080);
		assertColorxs(ColorxUtil.dimAll(0.5, Colorx.black, Colorx.of(0x80402010), Colorx.full), 0,
			0x40201008, 0x80808080);
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
