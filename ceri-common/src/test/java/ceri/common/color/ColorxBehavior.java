package ceri.common.color;

import static ceri.common.color.ColorTestUtil.assertColor;
import static ceri.common.color.ColorTestUtil.assertColorx;
import static ceri.common.color.Colors.color;
import org.junit.Test;
import ceri.common.test.Assert;
import ceri.common.test.Testing;

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
		Testing.exerciseEquals(t, eq0, eq1, eq2);
		Assert.notEqualAll(t, ne0, ne1, ne2, ne3);
	}

	@Test
	public void shouldCreateFromDenormalization() {
		assertColorx(Colorx.from(0xff806048, 0x404000, 0x802000, 0x4040), 0x4080ffff000038L);
		assertColorx(Colorx.from( //
			color(0xff806048), color(0x404000), color(0x802000), color(0x4040)), 0x4080ffff000038L);
	}

	@Test
	public void shouldDetermineIfAnyXIsSet() {
		Assert.equal(Colorx.of(0L).hasX(), false);
		Assert.equal(Colorx.of(0xffffffffL).hasX(), false);
		Assert.equal(Colorx.of(-1L).hasX(), true);
		Assert.equal(Colorx.of(0x0100000000L).hasX(), true);
		Assert.equal(Colorx.of(0x010000000000L).hasX(), true);
		Assert.equal(Colorx.of(0x01000000000000L).hasX(), true);
		Assert.equal(Colorx.of(0x0100000000000000L).hasX(), true);
	}

	@Test
	public void shouldAccessXComponent() {
		Assert.equal(Colorx.of(0x33221100886644L).x(0), 0x11);
		Assert.equal(Colorx.of(0x33221100886644L).x(1), 0x22);
		Assert.equal(Colorx.of(0x33221100886644L).x(2), 0x33);
		Assert.equal(Colorx.of(0x33221100886644L).x(3), 0x0);
	}

	@Test
	public void shouldNotSetUnchangedComponent() {
		var cx = Colorx.of(0x33221177886644L);
		Assert.same(cx.a(0x77), cx);
		Assert.same(cx.r(0x88), cx);
		Assert.same(cx.g(0x66), cx);
		Assert.same(cx.b(0x44), cx);
		Assert.same(cx.x(1, 0x22), cx);
		Assert.same(cx.x(3, 0), cx);
	}

	@Test
	public void shouldSetComponent() {
		var cx = Colorx.of(0x33221177886644L);
		assertColorx(cx.a(0xaa), 0x332211aa886644L);
		assertColorx(cx.r(0xaa), 0x33221177aa6644L);
		assertColorx(cx.g(0xaa), 0x3322117788aa44L);
		assertColorx(cx.b(0xaa), 0x332211778866aaL);
		assertColorx(cx.x(1, 0xaa), 0x33aa1177886644L);
		assertColorx(cx.x(3, 0xaa), 0xaa33221177886644L);
	}

	@Test
	public void shouldExtractRgb() {
		Assert.equal(Colorx.of(0x33221100886644L).rgb(), 0x886644);
	}

	@Test
	public void shouldNotFlattenAlphaIfOpaque() {
		var cx = Colorx.of(0x332211ff886644L);
		Assert.same(cx.flatten(), cx);
	}

	@Test
	public void shouldFlattenAlpha() {
		var cx = Colorx.of(0x33221180886644L);
		assertColorx(cx.flatten(), 0x1a1109ff443322L);
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
		Assert.equal(Colorx.of(0x4080ffff000038L).normalizeArgb(), 0xff000038);
		Assert.equal(Colorx.of(0x4080ffff000038L).normalizeArgb(0x404000, 0x802000, 0x4040),
			0xff806048);
		Assert.equal(Colorx.of(0x4080ffff000038L).normalizeArgb(0x404000, 0x802000, 0x4040, 0xffff),
			0xff806048);
	}

}
