package ceri.common.color;

import static ceri.common.color.ColorTestUtil.assertColor;
import static ceri.common.color.ColorTestUtil.assertColorx;
import static ceri.common.color.ColorUtil.color;
import static ceri.common.test.AssertUtil.assertAllNotEqual;
import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.TestUtil.exerciseEquals;
import org.junit.Test;

public class ColorxBehavior {

	@Test
	public void shouldNotBreachEqualsContract() {
		Colorx t = Colorx.of(0x44332211ff886644L);
		Colorx eq0 = Colorx.of(0x44332211ff886644L);
		Colorx eq1 = Colorx.of(0xff886644, 0x11, 0x22, 0x33, 0x44);
		Colorx eq2 = Colorx.of(color(0xff886644), 0x11, 0x22, 0x33, 0x44);
		Colorx ne0 = Colorx.of(0x4433221100886644L);
		Colorx ne1 = Colorx.of(color(0x886644), 0x11, 0x22, 0x33, 0x44);
		Colorx ne2 = Colorx.of(color(0xff886644), 0x11, 0x22, 0x33);
		Colorx ne3 = Colorx.of(color(0xff886644), 0x11, 0x22, 0x33, 0x55);
		exerciseEquals(t, eq0, eq1, eq2);
		assertAllNotEqual(t, ne0, ne1, ne2, ne3);
	}

	@Test
	public void shouldCreateFromDenormalization() {
		assertColorx(Colorx.from(0xff806048, 0x404000, 0x802000, 0x4040), 0x4080ffff000038L);
		assertColorx(Colorx.from( //
			color(0xff806048), color(0x404000), color(0x802000), color(0x4040)), 0x4080ffff000038L);
	}

	@Test
	public void shouldDetermineIfAnyXIsSet() {
		assertEquals(Colorx.of(0L).hasX(), false);
		assertEquals(Colorx.of(0xffffffffL).hasX(), false);
		assertEquals(Colorx.of(-1L).hasX(), true);
		assertEquals(Colorx.of(0x0100000000L).hasX(), true);
		assertEquals(Colorx.of(0x010000000000L).hasX(), true);
		assertEquals(Colorx.of(0x01000000000000L).hasX(), true);
		assertEquals(Colorx.of(0x0100000000000000L).hasX(), true);
	}

	@Test
	public void shouldAccessXComponent() {
		assertEquals(Colorx.of(0x33221100886644L).x(0), 0x11);
		assertEquals(Colorx.of(0x33221100886644L).x(1), 0x22);
		assertEquals(Colorx.of(0x33221100886644L).x(2), 0x33);
		assertEquals(Colorx.of(0x33221100886644L).x(3), 0x0);
	}

	@Test
	public void shouldExtractRgb() {
		assertEquals(Colorx.of(0x33221100886644L).rgb(), 0x886644);
	}

	@Test
	public void shouldNormalizeToColor() {
		assertColor(Colorx.of(0x4080ffff000038L).normalize(), 0xff000038);
		assertColor(Colorx.of(0x4080ffff000038L).normalize( //
			color(0x404000), color(0x802000), color(0x4040)), 0xff806048);
		assertColor(Colorx.of(0x4080ffff000038L).normalize( //
			color(0x404000), color(0x802000), color(0x4040), color(0xffffff)), 0xff806048);
	}

	@Test
	public void shouldNormalizeToArgb() {
		assertEquals(Colorx.of(0x4080ffff000038L).normalizeArgb(), 0xff000038);
		assertEquals(Colorx.of(0x4080ffff000038L).normalizeArgb(0x404000, 0x802000, 0x4040),
			0xff806048);
		assertEquals(Colorx.of(0x4080ffff000038L).normalizeArgb(0x404000, 0x802000, 0x4040, 0xffff),
			0xff806048);
	}

}
