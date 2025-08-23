package ceri.common.color;

import static ceri.common.color.Colors.color;
import static ceri.common.test.AssertUtil.assertEquals;
import java.awt.Color;
import org.junit.Test;

public class ColorableTest {

	@Test
	public void testNullInstance() {
		Colorable.NULL.argb(0x123456);
		assertEquals(Colorable.NULL.argb(), 0);
	}

	@Test
	public void testRgb() {
		Colorable c = new TestColorable();
		c.rgb(0x12345678);
		assertEquals(c.argb(), 0xff345678);
		assertEquals(c.rgb(), 0x345678);
	}

	@Test
	public void testFromColorxable() {
		Colorxable cx = new ColorxableTest.TestColorxable();
		Colorable c = Colorable.from(cx, color(0x800000), color(0x8000), color(0x80));
		cx.xargb(0x808080ff223344L);
		assertEquals(c.argb(), 0xff627384);
		c.argb(0xffaabbcc);
		assertEquals(cx.xargb(), 0xffffffff2a3b4cL);
	}

	@Test
	public void testMultiSetColorForEmptyCollection() {
		Colorable c = Colorable.multi();
		c.color(Coloring.chartreuse.color());
		c.color(Color.white);
	}

	@Test
	public void testMultiSetColor() {
		Colorable c0 = new TestColorable();
		Colorable c1 = new TestColorable();
		Colorable c2 = new TestColorable();
		Colorable c = Colorable.multi(c0, c1, c2);
		c.argb(Coloring.chartreuse.argb);
		assertEquals(c0.color(), Coloring.chartreuse.color());
		assertEquals(c1.color(), Coloring.chartreuse.color());
		assertEquals(c2.color(), Coloring.chartreuse.color());
	}

	@Test
	public void testMultiGetColorForEmptyCollection() {
		Colorable c = Colorable.multi();
		assertEquals(c.argb(), 0);
	}

	@Test
	public void testMultiGetColor() {
		Colorable c0 = new TestColorable();
		Colorable c1 = new TestColorable();
		Colorable c2 = new TestColorable();
		Colorable c = Colorable.multi(c0, c1, c2);
		c0.color(Color.red);
		c1.color(Color.cyan);
		c2.argb(Color.magenta.getRGB());
		assertEquals(c.color(), Color.red);
	}

	static class TestColorable implements Colorable {
		public int argb;

		@Override
		public int argb() {
			return argb;
		}

		@Override
		public void argb(int argb) {
			this.argb = argb;
		}
	}
}
