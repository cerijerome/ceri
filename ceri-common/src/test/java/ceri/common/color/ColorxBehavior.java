package ceri.common.color;

import static ceri.common.color.ColorTestUtil.assertColor;
import static ceri.common.color.ColorTestUtil.assertColorx;
import static ceri.common.test.AssertUtil.assertAllNotEqual;
import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.TestUtil.exerciseEquals;
import java.awt.Color;
import org.junit.Test;

public class ColorxBehavior {

	@Test
	public void shouldNotBreachEqualsContract() {
		Colorx c0 = Colorx.of(0x10, 0x20, 0x40, 0x80, 0xff);
		Colorx c1 = Colorx.of(0x10, 0x20, 0x40, 0x80);
		Colorx c2 = Colorx.of(0x10204080);
		Colorx n0 = Colorx.of(0x11, 0x20, 0x40, 0x80, 0xff);
		Colorx n1 = Colorx.of(0x10, 0x21, 0x40, 0x80, 0xff);
		Colorx n2 = Colorx.of(0x10, 0x20, 0x41, 0x80, 0xff);
		Colorx n3 = Colorx.of(0x10, 0x20, 0x40, 0x81, 0xff);
		Colorx n4 = Colorx.of(0x10, 0x20, 0x40, 0x80, 0);
		exerciseEquals(c0, c1, c2);
		assertAllNotEqual(c0, n0, n1, n2, n3, n4);
	}

	@Test
	public void shouldExtractXComponentFromRgb() {
		Color c = new Color(0xff, 0xaa, 0x55);
		Color x = new Color(0x55, 0x33, 0x11);
		assertColorx(Colorx.from(c, x), 0xaa, 0x77, 0x44, 0xff, 0xff);
		assertColorx(Colorx.from(0xff, 0xaa, 0x55, x), 0xaa, 0x77, 0x44, 0xff, 0xff);
		assertColorx(Colorx.from(Color.black, x), 0, 0, 0, 0, 0xff);
		assertColorx(Colorx.from(x, x), 0, 0, 0, 0xff, 0xff);
		assertColorx(Colorx.from(Color.magenta, Color.green), 0xff, 0, 0xff, 0, 0xff);
	}

	@Test
	public void shouldConvertToRgbxInteger() {
		assertEquals(Colorx.of(255, 127, 63, 31).rgbx(), 0xff7f3f1f);
	}

	@Test
	public void shouldNormalizeForXColor() {
		Color x = new Color(0x55, 0x33, 0x11);
		Colorx cx = Colorx.of(0xaa, 0x77, 0x44, 0xff);
		assertColor(cx.normalizeFor(x), 0xff, 0xaa, 0x55);
		assertColor(cx.normalizeFor(null), 0xaa, 0x77, 0x44);
	}

	@Test
	public void shouldAccessComponents() {
		Colorx cx = Colorx.of(0xaa, 0x77, 0x44, 0x11, 0xf0);
		assertEquals(cx.r(), 0xaa);
		assertEquals(cx.g(), 0x77);
		assertEquals(cx.b(), 0x44);
		assertEquals(cx.x(), 0x11);
		assertEquals(cx.a(), 0xf0);
	}

}
