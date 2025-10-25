package ceri.common.color;

import static ceri.common.color.ColorTestUtil.assertColor;
import static ceri.common.color.ColorTestUtil.assertColorx;
import static ceri.common.test.Assert.assertArray;
import static ceri.common.test.Assert.assertEquals;
import static ceri.common.test.Assert.assertOrdered;
import static ceri.common.test.Assert.assertStream;
import java.awt.Color;
import java.util.Comparator;
import org.junit.Test;
import ceri.common.stream.Streams;
import ceri.common.test.Assert;
import ceri.common.test.TestUtil;

public class ColorxsTest {

	@Test
	public void testCompareXargb() {
		exerciseCompare(Colorxs.Compare.XARGB, 0x8000ff204060L, 0x40ff204060L, 0x008000ff204060L,
			0x100000ff204060L);
	}

	@Test
	public void testCompareXrgb() {
		exerciseCompare(Colorxs.Compare.XRGB, 0x8000ff204060L, 0x40ff204060L, 0x0080007f204060L,
			0x100000ff204060L);
	}

	@Test
	public void testCompareAlpha() {
		exerciseCompare(Colorxs.Compare.A, 0x800080204060L, 0xffff40204060L, 0x80ffffffL,
			0xcc000000L);
	}

	@Test
	public void testCompareRed() {
		exerciseCompare(Colorxs.Compare.R, 0x800080204060L, 0xffff40104060L, 0x8020ffffL,
			0xcc400000L);
	}

	@Test
	public void testCompareGreen() {
		exerciseCompare(Colorxs.Compare.G, 0x800080204060L, 0xffff40202060L, 0x80ff40ffL,
			0xcc005000L);
	}

	@Test
	public void testCompareBlue() {
		exerciseCompare(Colorxs.Compare.B, 0x800080204060L, 0xffff40204050L, 0x80ffff60L,
			0xcc0000ffL);
	}

	@Test
	public void testCompareX() {
		exerciseCompare(Colorxs.Compare.x(0), 0x807080204060L, 0xff6040204050L, 0x7080ffff60L,
			0x80cc0000ffL);
	}

	@Test
	public void testCompareColor() {
		exerciseCompare(Colorxs.Compare.color(Colors.Compare.HUE), 0xffff808060L, 0xffffffff800000L,
			0xffffffffff00L, 0xffff0060ffL);
	}

	@Test
	public void testArgb() {
		assertEquals(Colorxs.argb(0x0L), 0);
		assertEquals(Colorxs.argb(-1L), -1);
		assertEquals(Colorxs.argb(0xfedcba9876543210L), 0x76543210);
	}

	@Test
	public void testXrgb() {
		assertEquals(Colorxs.xrgb(0), 0L);
		assertEquals(Colorxs.xrgb(0xfedcba9876543210L), 0xfedcba9800543210L);
		assertEquals(Colorxs.xrgb(-1L), 0xffffffff00ffffffL);
	}

	@Test
	public void testXargb() {
		assertEquals(Colorxs.xargb(0), 0L);
		assertEquals(Colorxs.xargb(0xfedcba98), 0xfedcba98L);
		assertEquals(Colorxs.xargb(0, 0x89, 0xab, 0xcd, 0xef), 0xefcdab8900000000L);
		assertEquals(Colorxs.xargb(Color.magenta, 0x89, 0xab, 0xcd, 0xef), 0xefcdab89ffff00ffL);
	}

	@Test
	public void testXargbFromText() {
		assertEquals(Colorxs.xargb("test"), null);
		assertEquals(Colorxs.xargb("clear"), Colorx.clear.xargb());
		assertEquals(Colorxs.xargb("full"), Colorx.full.xargb());
		assertEquals(Colorxs.xargb("#fed"), 0xffffeeddL);
		assertEquals(Colorxs.xargb("0xfed"), 0xff000fedL);
		assertEquals(Colorxs.xargb("#abcdfedcba987654"), 0xabcdfedcba987654L);
	}

	@Test
	public void testValidXargbFromText() {
		Assert.thrown(() -> Colorxs.validXargb("test"));
		assertEquals(Colorxs.validXargb("full"), Colorx.full.xargb());
	}

	@Test
	public void testMaxXargb() {
		assertEquals(Colorxs.maxXargb(0), 0L);
		assertEquals(Colorxs.maxXargb(0x102030407f504030L), 0x336699cc7fffcc99L);
		assertEquals(Colorxs.maxXargb(0x100030407f004030L), 0x4000bfff7f00ffbfL);
		assertEquals(Colorxs.maxXargb(0x7f004030L), 0x7f00ffbfL);
		assertEquals(Colorxs.maxXargb(0xff7f004030L), 0xff7f004030L);
	}

	@Test
	public void testRandomXargb() {
		assertEquals(Colorxs.randomXargb() & 0xff000000L, 0xff000000L);
		assertEquals(Colorxs.randomXargb(0) & 0xffffffffff000000L, 0xff000000L);
		assertEquals(Colorxs.randomXargb(2) & 0xffff0000ff000000L, 0xff000000L);
	}

	@Test
	public void testDimXargb() {
		assertEquals(Colorxs.dimXargb(0xeeccaa88ff664422L, 0.0), 0xff000000L);
		assertEquals(Colorxs.dimXargb(0xeeccaa88ff664422L, 1.0), 0xeeccaa88ff664422L);
		assertEquals(Colorxs.dimXargb(0xeeccaa88ff664422L, 0.5), 0x77665544ff332211L);
		assertEquals(Colorxs.dimXargb(0xeeccaa88ff664422L, 0.125), 0x1e1a1511ff0d0904L);
		assertEquals(Colorxs.dimXargb(0x88664422L, 0.5), 0x88332211L);
		assertEquals(Colorxs.dimXargb(0x88000000L, 0.5), 0x88000000L);
	}

	@Test
	public void testScaleXargb() {
		assertEquals(Colorxs.scaleXargb(0xffddbb99ff224466L, 0x99bbddffff446622L, 0),
			0xffddbb99ff224466L);
		assertEquals(Colorxs.scaleXargb(0xffddbb99ff224466L, 0x99bbddffff446622L, 1),
			0x99bbddffff446622L);
		assertEquals(Colorxs.scaleXargb(0xffddbb99ff224466L, 0x99bbddffff446622L, 0.5),
			0xccccccccff335544L);
	}

	@Test
	public void testBlendAssociativity() {
		var c0 = Colorx.of(0x332211807fffd4L);
		var c1 = Colorx.of(0x330077408a2be2L);
		var c2 = Colorx.of(0xaa22220020ff7f50L);
		var c21 = Colorxs.blend(c2, c1);
		var c10 = Colorxs.blend(c1, c0);
		var c21_0 = Colorxs.blend(c21, c0);
		var c2_10 = Colorxs.blend(c2, c10);
		ColorTestUtil.assertXargbDiff(c21_0.xargb(), c2_10.xargb(), 1);
	}

	@Test
	public void testBlendXargbAssociativity() {
		var c0 = 0xaa22220020ff7f50L;
		var c1 = 0x332211807fffd4L;
		var c2 = 0x330077408a2be2L;
		var c21 = Colorxs.blendXargbs(c2, c1);
		var c10 = Colorxs.blendXargbs(c1, c0);
		var c21_0 = Colorxs.blendXargbs(c21, c0);
		var c2_10 = Colorxs.blendXargbs(c2, c10);
		ColorTestUtil.assertXargbDiff(c21_0, c2_10, 1);
	}

	@Test
	public void testBlendXargbs() {
		assertEquals(Colorxs.blendXargbs(), 0L);
		assertEquals(Colorxs.blendXargbs(0xaa22220020ff7f50L), 0xaa22220020ff7f50L);
		assertEquals(Colorxs.blendXargbs(0x332211ff7fffd4L, 0xaa22220020ff7f50L),
			0x332211ff7fffd4L);
		assertEquals(Colorxs.blendXargbs(0x332211007fffd4L, 0xaa22220020ff7f50L),
			0xaa22220020ff7f50L);
		assertEquals(Colorxs.blendXargbs(0x332211807fffd4L, 0xaa22220000ff7f50L),
			0x332211807fffd4L);
		assertEquals(Colorxs.blendXargbs(0x332211807fffd4L, 0xaa22220020ff7f50L),
			0x1331220f908df1c5L);
		assertEquals(Colorxs.blendXargbs(0xaa22220020ff7f50L, 0x332211807fffd4L),
			0x262f220d909be2b7L);
	}

	@Test
	public void testColorxFromText() {
		assertColorx(Colorxs.colorx("test"), null);
		assertColorx(Colorxs.colorx("full"), Colorx.full);
	}

	@Test
	public void testValidColorxFromText() {
		Assert.thrown(() -> Colorxs.validColorx("test"));
		assertColorx(Colorxs.validColorx("full"), Colorx.full);
	}

	@Test
	public void testMax() {
		assertColorx(Colorxs.max(Colorx.clear), 0L);
		assertColorx(Colorxs.max(Colorx.full), Colorx.full);
		assertColorx(Colorxs.max(Colorx.of(0x101ff010101L)), Colorx.fullX01);
	}

	@Test
	public void testRandom() {
		assertEquals(Colorxs.random().xargb() & 0xff000000L, 0xff000000L);
		assertEquals(Colorxs.random(0).xargb() & 0xffffffffff000000L, 0xff000000L);
		assertEquals(Colorxs.random(2).xargb() & 0xffff0000ff000000L, 0xff000000L);
	}

	@Test
	public void testDim() {
		assertColorx(Colorxs.dim(Colorx.of(0xeeccaa88ff664422L), 0.0), 0xff000000L);
		assertColorx(Colorxs.dim(Colorx.of(0xeeccaa88ff664422L), 1.0), 0xeeccaa88ff664422L);
		assertColorx(Colorxs.dim(Colorx.of(0xeeccaa88ff664422L), 0.5), 0x77665544ff332211L);
	}

	@Test
	public void testScale() {
		assertColorx(Colorxs.scale(Colorx.of(0xffddbb99ff224466L), //
			Colorx.of(0x99bbddffff446622L), 0), 0xffddbb99ff224466L);
		assertColorx(Colorxs.scale(Colorx.of(0xffddbb99ff224466L), //
			Colorx.of(0x99bbddffff446622L), 1), 0x99bbddffff446622L);
		assertColorx(Colorxs.scale(Colorx.of(0xffddbb99ff224466L), //
			Colorx.of(0x99bbddffff446622L), 0.5), 0xccccccccff335544L);
	}

	@Test
	public void testToString() {
		assertEquals(Colorxs.toString(0xff000000L), "#000000(black)");
		assertEquals(Colorxs.toString(0xff123456L), "#123456");
		assertEquals(Colorxs.toString(Colorx.clear), "#00000000(clear)");
		assertEquals(Colorxs.toString(0x12345678L), "#12345678");
		assertEquals(Colorxs.toString(0x123456789aL), "#123456789a");
		assertEquals(Colorxs.toString(0xff77ffffffL), "#ff77ffffff(fullX0)");
		assertEquals(Colorxs.toString(0xffff77ffffffL), "#ffff77ffffff(fullX01)");
		assertEquals(Colorxs.toString(Colorx.fullX012), "#ffffffffffffff(fullX012)");
	}

	@Test
	public void testName() {
		assertEquals(Colorxs.name(Colorx.fullX012), "fullX012");
		assertEquals(Colorxs.name(Colorx.of(0x123456L)), null);
	}

	@Test
	public void testHex() {
		assertEquals(Colorxs.hex(Colorx.black), "#000000");
		assertEquals(Colorxs.hex(Colorx.of(0x123456789aL)), "#123456789a");
	}

	@Test
	public void testXargbsFromColorxs() {
		assertArray(Colorxs.xargbs(Colorx.fullX0, Colorx.of(0x123456789aL)), 0xffffffffffL,
			0x123456789aL);
	}

	@Test
	public void testXargbsFromText() {
		assertArray(Colorxs.xargbs("fullX0", "#123456789a"), 0xffffffffffL, 0x123456789aL);
		Assert.thrown(() -> Colorxs.xargbs("fullX0", "#"));
	}

	@Test
	public void testColorxsFromText() {
		assertArray(Colorxs.colorxs("fullX0", "#123456789a"), Colorx.of(0xffffffffffL),
			Colorx.of(0x123456789aL));
		Assert.thrown(() -> Colorxs.colorxs("fullX0", "#"));
	}

	@Test
	public void testXargbList() {
		assertOrdered(Colorxs.xargbList(Streams.longs(0xffffffffffL, 0x123456789aL)), 0xffffffffffL,
			0x123456789aL);
	}

	@Test
	public void testColorxList() {
		assertOrdered(Colorxs.colorxList(Streams.longs(0xffffffffffL, 0x123456789aL)),
			Colorx.of(0xffffffffffL), Colorx.of(0x123456789aL));
	}

	@Test
	public void testArgbStream() {
		assertStream(Colorxs.argbStream(Color.cyan.getRGB(), Color.yellow.getRGB()), 0xff00ffffL,
			0xffffff00L);
	}

	@Test
	public void testStreamArgbsAsXargbs() {
		assertStream(Colorxs.stream(Streams.ints(0x12345678, 0x87654321), 0x9a, 0xbc, 0xde),
			0xdebc9a12345678L, 0xdebc9a87654321L);
	}

	@Test
	public void testDenormalizeXargb() {
		assertEquals(Colorxs.denormalizeXargb(0xfedcba98), 0xfedcba98L);
		assertEquals(Colorxs.denormalizeXargb(0xfedcba98, 0), 0xfedcba98L);
		assertEquals(Colorxs.denormalizeXargb(0xfedcba98, 0, 0, 0, 0), 0xfedcba98L);
		assertEquals(Colorxs.denormalizeXargb(0x0000ff, 0xff), 0xff00000000L);
		assertEquals(Colorxs.denormalizeXargb(0x0000ff, 0x7f), 0xff00000080L);
		assertEquals(Colorxs.denormalizeXargb(0x0000ff, 0x7f, 0x3f), 0xffff00000041L);
		assertEquals(Colorxs.denormalizeXargb(0x0000ff, 0x7f, 0x7f), 0xffff00000001L);
		assertEquals(Colorxs.denormalizeXargb(0x0000ff, 0x7f, 0xff), 0x80ff00000000L);
		assertEquals(Colorxs.denormalizeXargb(0x00007f, 0x3f, 0xff), 0x40ff00000000L);
		assertEquals(Colorxs.denormalizeXargb(0x804000, 0xff0000, 0x00ff00), 0x408000000000L);
		assertEquals(Colorxs.denormalizeXargb(0x804000, 0x400000, 0x004000), 0xffff00400000L);
		assertEquals(Colorxs.denormalizeXargb(0x804000, 0x408000, 0x404000), 0x008000600000L);
		assertEquals(Colorxs.denormalizeXargb(0x804000, 0x603000, 0x404000), 0x40ff00100000L);
		assertEquals(Colorxs.denormalizeXargb(0xffffff, 0x800000, 0x8000, 0x80, 0x808080),
			0xfdffffff00000000L);
		assertEquals(Colorxs.denormalizeXargb(0xffffff, 0x800000, 0x8000, 0x80, 0x404040),
			0xffffffff003f3f3fL);
		assertEquals(Colorxs.denormalizeXargb(0xffbfdf, 0xff0000, 0xff00, 0xff, 0xffffff),
			0xdfbfff00000000L);
	}

	@Test
	public void testDenormalize() {
		assertColorx(
			Colorxs.denormalize(Color.white, Color.gray, Color.red, Color.green, Color.blue),
			0x7f7f7fffff000000L);
	}

	@Test
	public void testNormalizeArgb() {
		assertEquals(Colorxs.normalizeArgb(0x12345678fedcba98L), 0xfedcba98);
		assertEquals(Colorxs.normalizeArgb(0x12345678fedcba98L, 0), 0xfedcba98);
		assertEquals(Colorxs.normalizeArgb(0x12345678fedcba98L, 0, 0, 0, 0), 0xfedcba98);
		assertEquals(Colorxs.normalizeArgb(0x000000ff00000000L, 0xff), 0xff);
		assertEquals(Colorxs.normalizeArgb(0x000000ff00000080L, 0x7f), 0xff);
		assertEquals(Colorxs.normalizeArgb(0x0000ffff00000041L, 0x7f, 0x3f), 0xff);
		assertEquals(Colorxs.normalizeArgb(0x0000ffff00000001L, 0x7f, 0x7f), 0xff);
		assertEquals(Colorxs.normalizeArgb(0x000080ff00000000L, 0x7f, 0xff), 0xff);
		assertEquals(Colorxs.normalizeArgb(0x000040ff00000000L, 0x3f, 0xff), 0x7f);
		assertEquals(Colorxs.normalizeArgb(0x0000408000000000L, 0xff0000, 0x00ff00), 0x804000);
		assertEquals(Colorxs.normalizeArgb(0x0000ffff00400000L, 0x400000, 0x004000), 0x804000);
		assertEquals(Colorxs.normalizeArgb(0x0000008000600000L, 0x408000, 0x404000), 0x804000);
		assertEquals(Colorxs.normalizeArgb(0x000040ff00100000L, 0x603000, 0x404000), 0x804000);
		assertEquals(Colorxs.normalizeArgb(0xfdffffff00000000L, 0x800000, 0x8000, 0x80, 0x808080),
			0xffffff);
		assertEquals(Colorxs.normalizeArgb(0xffffffff003f3f3fL, 0x800000, 0x8000, 0x80, 0x404040),
			0xffffff);
		assertEquals(Colorxs.normalizeArgb(0x8060408000000000L, 0xff0000, 0xff00, 0xff, 0xffffff),
			0xffbfdf);
	}

	@Test
	public void testNormalize() {
		assertColor(Colorxs.normalize(Colorx.of(0x7f7f7fffff000000L), Color.gray, Color.red,
			Color.green, Color.blue), Color.white);
	}

	@Test
	public void testDenormalizeStream() {
		assertStream(Colorxs.denormalize(Streams.ints(0xffffffff, 0xff806040), new Color(0x808080)),
			0xffff7f7f7fL, 0x80ff402000L);
	}

	@Test
	public void testNormalizeStream() {
		assertStream(
			Colorxs.normalize(Streams.longs(0xffff7f7f7fL, 0x80ff402000L), new Color(0x808080)),
			0xffffffff, 0xff806040);
	}

	@Test
	public void testApplyArgbFunctionToStream() {
		assertStream(Colorxs.applyArgb(Streams.longs(0x12345678abcdefL, 0L, -1L), c -> ~c),
			0x12345687543210L, 0xffffffffL, 0xffffffff00000000L);
	}

	@Test
	public void testFadeStream() {
		assertStream(
			Colorxs.fadeStream(Colorx.of(0xff000000L), Colorx.of(0x804020ff204080L), 4, Bias.NONE),
			0x201008ff081020L, 0x402010ff102040L, 0x603018ff183060L, 0x804020ff204080L);
	}

	@Test
	public void testApplyColorFunction() {
		assertColorx(Colorxs.apply(Colorx.of(0x8040ffff00ffL), Color::darker), 0x8040ffb200b2L);
	}

	private static void exerciseCompare(Comparator<Colorx> comparator, long cx, long lt, long eq,
		long gt) {
		TestUtil.exerciseCompare(comparator, Colorx.of(cx), Colorx.of(lt), Colorx.of(eq),
			Colorx.of(gt));
	}
}
