package ceri.common.color;

import static ceri.common.color.Bias.NONE;
import static ceri.common.color.ColorTestUtil.assertColor;
import static ceri.common.color.ColorTestUtil.assertColorx;
import static ceri.common.test.AssertUtil.assertArray;
import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertIterable;
import static ceri.common.test.AssertUtil.assertPrivateConstructor;
import static ceri.common.test.AssertUtil.assertStream;
import static ceri.common.test.AssertUtil.assertThrown;
import java.awt.Color;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import org.junit.Test;

public class ColorxUtilTest {

	@Test
	public void testConstructorIsPrivate() {
		assertPrivateConstructor(ColorxUtil.class);
	}

	@Test
	public void testApplyAlphaXargb() {
		assertEquals(ColorxUtil.applyAlphaXargb(0x804000804020L), 0x0000ff000000L);
		assertEquals(ColorxUtil.applyAlphaXargb(0x804080804020L), 0x4020ff402010L);
		assertEquals(ColorxUtil.applyAlphaXargb(0x8040ff804020L), 0x8040ff804020L);
	}

	@Test
	public void testArgb() {
		assertEquals(ColorxUtil.argb(0x0L), 0);
		assertEquals(ColorxUtil.argb(-1L), -1);
		assertEquals(ColorxUtil.argb(0xfedcba9876543210L), 0x76543210);
	}

	@Test
	public void testXrgb() {
		assertEquals(ColorxUtil.xrgb(0), 0L);
		assertEquals(ColorxUtil.xrgb(0xfedcba9876543210L), 0xfedcba9800543210L);
		assertEquals(ColorxUtil.xrgb(-1L), 0xffffffff00ffffffL);
	}

	@Test
	public void testXargb() {
		assertEquals(ColorxUtil.xargb(0), 0L);
		assertEquals(ColorxUtil.xargb(0xfedcba98), 0xfedcba98L);
		assertEquals(ColorxUtil.xargb(0, 0x89, 0xab, 0xcd, 0xef), 0xefcdab8900000000L);
		assertEquals(ColorxUtil.xargb(Color.magenta, 0x89, 0xab, 0xcd, 0xef), 0xefcdab89ffff00ffL);
	}

	@Test
	public void testXargbFromText() {
		assertEquals(ColorxUtil.xargb("test"), null);
		assertEquals(ColorxUtil.xargb("clear"), Colorx.clear.xargb);
		assertEquals(ColorxUtil.xargb("full"), Colorx.full.xargb);
		assertEquals(ColorxUtil.xargb("#fed"), 0xffffeeddL);
		assertEquals(ColorxUtil.xargb("0xfed"), 0xff000fedL);
		assertEquals(ColorxUtil.xargb("#abcdfedcba987654"), 0xabcdfedcba987654L);
	}

	@Test
	public void testValidXargbFromText() {
		assertThrown(() -> ColorxUtil.validXargb("test"));
		assertEquals(ColorxUtil.validXargb("full"), Colorx.full.xargb);
	}

	@Test
	public void testMaxXargb() {
		assertEquals(ColorxUtil.maxXargb(0), 0L);
		assertEquals(ColorxUtil.maxXargb(0x102030407f504030L), 0x336699cc7fffcc99L);
		assertEquals(ColorxUtil.maxXargb(0x100030407f004030L), 0x4000bfff7f00ffbfL);
		assertEquals(ColorxUtil.maxXargb(0x7f004030L), 0x7f00ffbfL);
		assertEquals(ColorxUtil.maxXargb(0xff7f004030L), 0xff7f004030L);
	}

	@Test
	public void testRandomXargb() {
		assertEquals(ColorxUtil.randomXargb() & 0xff000000L, 0xff000000L);
		assertEquals(ColorxUtil.randomXargb(0) & 0xffffffffff000000L, 0xff000000L);
		assertEquals(ColorxUtil.randomXargb(2) & 0xffff0000ff000000L, 0xff000000L);
	}

	@Test
	public void testDimXargb() {
		assertEquals(ColorxUtil.dimXargb(0xeeccaa88ff664422L, 0.0), 0xff000000L);
		assertEquals(ColorxUtil.dimXargb(0xeeccaa88ff664422L, 1.0), 0xeeccaa88ff664422L);
		assertEquals(ColorxUtil.dimXargb(0xeeccaa88ff664422L, 0.5), 0x77665544ff332211L);
		assertEquals(ColorxUtil.dimXargb(0xeeccaa88ff664422L, 0.125), 0x1e1a1511ff0d0904L);
		assertEquals(ColorxUtil.dimXargb(0x88664422L, 0.5), 0x88332211L);
		assertEquals(ColorxUtil.dimXargb(0x88000000L, 0.5), 0x88000000L);
	}

	@Test
	public void testScaleXargb() {
		assertEquals(ColorxUtil.scaleXargb(0xffddbb99ff224466L, 0x99bbddffff446622L, 0),
			0xffddbb99ff224466L);
		assertEquals(ColorxUtil.scaleXargb(0xffddbb99ff224466L, 0x99bbddffff446622L, 1),
			0x99bbddffff446622L);
		assertEquals(ColorxUtil.scaleXargb(0xffddbb99ff224466L, 0x99bbddffff446622L, 0.5),
			0xccccccccff335544L);
	}

	@Test
	public void testApplyAlpha() {
		assertColorx(ColorxUtil.applyAlpha(Colorx.of(0x804000804020L)), 0x0000ff000000L);
		assertColorx(ColorxUtil.applyAlpha(Colorx.of(0x804080804020L)), 0x4020ff402010L);
		assertColorx(ColorxUtil.applyAlpha(Colorx.of(0x8040ff804020L)), 0x8040ff804020L);
	}

	@Test
	public void testColorxFromText() {
		assertColorx(ColorxUtil.colorx("test"), null);
		assertColorx(ColorxUtil.colorx("full"), Colorx.full);
	}

	@Test
	public void testValidColorxFromText() {
		assertThrown(() -> ColorxUtil.validColorx("test"));
		assertColorx(ColorxUtil.validColorx("full"), Colorx.full);
	}

	@Test
	public void testMax() {
		assertColorx(ColorxUtil.max(Colorx.clear), 0L);
		assertColorx(ColorxUtil.max(Colorx.full), Colorx.full);
		assertColorx(ColorxUtil.max(Colorx.of(0x101ff010101L)), Colorx.fullX01);
	}

	@Test
	public void testRandom() {
		assertEquals(ColorxUtil.random().xargb & 0xff000000L, 0xff000000L);
		assertEquals(ColorxUtil.random(0).xargb & 0xffffffffff000000L, 0xff000000L);
		assertEquals(ColorxUtil.random(2).xargb & 0xffff0000ff000000L, 0xff000000L);
	}

	@Test
	public void testDim() {
		assertColorx(ColorxUtil.dim(Colorx.of(0xeeccaa88ff664422L), 0.0), 0xff000000L);
		assertColorx(ColorxUtil.dim(Colorx.of(0xeeccaa88ff664422L), 1.0), 0xeeccaa88ff664422L);
		assertColorx(ColorxUtil.dim(Colorx.of(0xeeccaa88ff664422L), 0.5), 0x77665544ff332211L);
	}

	@Test
	public void testScale() {
		assertColorx(ColorxUtil.scale(Colorx.of(0xffddbb99ff224466L), //
			Colorx.of(0x99bbddffff446622L), 0), 0xffddbb99ff224466L);
		assertColorx(ColorxUtil.scale(Colorx.of(0xffddbb99ff224466L), //
			Colorx.of(0x99bbddffff446622L), 1), 0x99bbddffff446622L);
		assertColorx(ColorxUtil.scale(Colorx.of(0xffddbb99ff224466L), //
			Colorx.of(0x99bbddffff446622L), 0.5), 0xccccccccff335544L);
	}

	@Test
	public void testToString() {
		assertEquals(ColorxUtil.toString(0xff000000L), "#000000(black)");
		assertEquals(ColorxUtil.toString(0xff123456L), "#123456");
		assertEquals(ColorxUtil.toString(Colorx.clear), "#00000000(clear)");
		assertEquals(ColorxUtil.toString(0x12345678L), "#12345678");
		assertEquals(ColorxUtil.toString(0x123456789aL), "#123456789a");
		assertEquals(ColorxUtil.toString(Colorx.fullX012), "#ffffffffffffff(fullX012)");
	}

	@Test
	public void testName() {
		assertEquals(ColorxUtil.name(Colorx.fullX012), "fullX012");
		assertEquals(ColorxUtil.name(Colorx.of(0x123456L)), null);
	}

	@Test
	public void testHex() {
		assertEquals(ColorxUtil.hex(Colorx.black), "#000000");
		assertEquals(ColorxUtil.hex(Colorx.of(0x123456789aL)), "#123456789a");
	}

	@Test
	public void testXargbsFromColorxs() {
		assertArray(ColorxUtil.xargbs(Colorx.fullX0, Colorx.of(0x123456789aL)), 0xffffffffffL,
			0x123456789aL);
	}

	@Test
	public void testXargbsFromText() {
		assertArray(ColorxUtil.xargbs("fullX0", "#123456789a"), 0xffffffffffL, 0x123456789aL);
		assertThrown(() -> ColorxUtil.xargbs("fullX0", "#"));
	}

	@Test
	public void testColorxsFromText() {
		assertArray(ColorxUtil.colorxs("fullX0", "#123456789a"), Colorx.of(0xffffffffffL),
			Colorx.of(0x123456789aL));
		assertThrown(() -> ColorxUtil.colorxs("fullX0", "#"));
	}

	@Test
	public void testXargbList() {
		assertIterable(ColorxUtil.xargbList(LongStream.of(0xffffffffffL, 0x123456789aL)),
			0xffffffffffL, 0x123456789aL);
	}

	@Test
	public void testColorxList() {
		assertIterable(ColorxUtil.colorxList(LongStream.of(0xffffffffffL, 0x123456789aL)),
			Colorx.of(0xffffffffffL), Colorx.of(0x123456789aL));
	}

	@Test
	public void testArgbStream() {
		assertStream(ColorxUtil.argbStream(Color.cyan.getRGB(), Color.yellow.getRGB()), 0xff00ffffL,
			0xffffff00L);
	}

	@Test
	public void testStreamArgbsAsXargbs() {
		assertStream(ColorxUtil.stream(IntStream.of(0x12345678, 0x87654321), 0x9a, 0xbc, 0xde),
			0xdebc9a12345678L, 0xdebc9a87654321L);
	}

	@Test
	public void testDenormalizeXargb() {
		assertEquals(ColorxUtil.denormalizeXargb(0xfedcba98), 0xfedcba98L);
		assertEquals(ColorxUtil.denormalizeXargb(0xfedcba98, 0), 0xfedcba98L);
		assertEquals(ColorxUtil.denormalizeXargb(0xfedcba98, 0, 0, 0, 0), 0xfedcba98L);
		assertEquals(ColorxUtil.denormalizeXargb(0x0000ff, 0xff), 0xff00000000L);
		assertEquals(ColorxUtil.denormalizeXargb(0x0000ff, 0x7f), 0xff00000080L);
		assertEquals(ColorxUtil.denormalizeXargb(0x0000ff, 0x7f, 0x3f), 0xffff00000041L);
		assertEquals(ColorxUtil.denormalizeXargb(0x0000ff, 0x7f, 0x7f), 0xffff00000001L);
		assertEquals(ColorxUtil.denormalizeXargb(0x0000ff, 0x7f, 0xff), 0x80ff00000000L);
		assertEquals(ColorxUtil.denormalizeXargb(0x00007f, 0x3f, 0xff), 0x40ff00000000L);
		assertEquals(ColorxUtil.denormalizeXargb(0x804000, 0xff0000, 0x00ff00), 0x408000000000L);
		assertEquals(ColorxUtil.denormalizeXargb(0x804000, 0x400000, 0x004000), 0xffff00400000L);
		assertEquals(ColorxUtil.denormalizeXargb(0x804000, 0x408000, 0x404000), 0x008000600000L);
		assertEquals(ColorxUtil.denormalizeXargb(0x804000, 0x603000, 0x404000), 0x40ff00100000L);
		assertEquals(ColorxUtil.denormalizeXargb(0xffffff, 0x800000, 0x8000, 0x80, 0x808080),
			0xfdffffff00000000L);
		assertEquals(ColorxUtil.denormalizeXargb(0xffffff, 0x800000, 0x8000, 0x80, 0x404040),
			0xffffffff003f3f3fL);
		assertEquals(ColorxUtil.denormalizeXargb(0xffbfdf, 0xff0000, 0xff00, 0xff, 0xffffff),
			0xdfbfff00000000L);
	}

	@Test
	public void testDenormalize() {
		assertColorx(
			ColorxUtil.denormalize(Color.white, Color.gray, Color.red, Color.green, Color.blue),
			0x7f7f7fffff000000L);
	}

	@Test
	public void testNormalizeArgb() {
		assertEquals(ColorxUtil.normalizeArgb(0x12345678fedcba98L), 0xfedcba98);
		assertEquals(ColorxUtil.normalizeArgb(0x12345678fedcba98L, 0), 0xfedcba98);
		assertEquals(ColorxUtil.normalizeArgb(0x12345678fedcba98L, 0, 0, 0, 0), 0xfedcba98);
		assertEquals(ColorxUtil.normalizeArgb(0x000000ff00000000L, 0xff), 0xff);
		assertEquals(ColorxUtil.normalizeArgb(0x000000ff00000080L, 0x7f), 0xff);
		assertEquals(ColorxUtil.normalizeArgb(0x0000ffff00000041L, 0x7f, 0x3f), 0xff);
		assertEquals(ColorxUtil.normalizeArgb(0x0000ffff00000001L, 0x7f, 0x7f), 0xff);
		assertEquals(ColorxUtil.normalizeArgb(0x000080ff00000000L, 0x7f, 0xff), 0xff);
		assertEquals(ColorxUtil.normalizeArgb(0x000040ff00000000L, 0x3f, 0xff), 0x7f);
		assertEquals(ColorxUtil.normalizeArgb(0x0000408000000000L, 0xff0000, 0x00ff00), 0x804000);
		assertEquals(ColorxUtil.normalizeArgb(0x0000ffff00400000L, 0x400000, 0x004000), 0x804000);
		assertEquals(ColorxUtil.normalizeArgb(0x0000008000600000L, 0x408000, 0x404000), 0x804000);
		assertEquals(ColorxUtil.normalizeArgb(0x000040ff00100000L, 0x603000, 0x404000), 0x804000);
		assertEquals(
			ColorxUtil.normalizeArgb(0xfdffffff00000000L, 0x800000, 0x8000, 0x80, 0x808080),
			0xffffff);
		assertEquals(
			ColorxUtil.normalizeArgb(0xffffffff003f3f3fL, 0x800000, 0x8000, 0x80, 0x404040),
			0xffffff);
		assertEquals(
			ColorxUtil.normalizeArgb(0x8060408000000000L, 0xff0000, 0xff00, 0xff, 0xffffff),
			0xffbfdf);
	}

	@Test
	public void testNormalize() {
		assertColor(ColorxUtil.normalize(Colorx.of(0x7f7f7fffff000000L), Color.gray, Color.red,
			Color.green, Color.blue), Color.white);
	}

	@Test
	public void testDenormalizeStream() {
		assertStream(
			ColorxUtil.denormalize(IntStream.of(0xffffffff, 0xff806040), new Color(0x808080)),
			0xffff7f7f7fL, 0x80ff402000L);
	}

	@Test
	public void testNormalizeStream() {
		assertStream(
			ColorxUtil.normalize(LongStream.of(0xffff7f7f7fL, 0x80ff402000L), new Color(0x808080)),
			0xffffffff, 0xff806040);
	}

	@Test
	public void testApplyArgbFunctionToStream() {
		assertStream(ColorxUtil.applyArgb(LongStream.of(0x12345678abcdefL, 0L, -1L), c -> ~c),
			0x12345687543210L, 0xffffffffL, 0xffffffff00000000L);
	}

	@Test
	public void testFadeStream() {
		assertStream(
			ColorxUtil.fadeStream(Colorx.of(0xff000000L), Colorx.of(0x804020ff204080L), 4, NONE),
			0x201008ff081020L, 0x402010ff102040L, 0x603018ff183060L, 0x804020ff204080L);
	}

	@Test
	public void testApplyColorFunction() {
		assertColorx(ColorxUtil.apply(Colorx.of(0x8040ffff00ffL), Color::darker), 0x8040ffb200b2L);
	}

}
