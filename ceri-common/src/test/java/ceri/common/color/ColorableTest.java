package ceri.common.color;

import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertNull;
import java.awt.Color;
import org.junit.Test;

public class ColorableTest {

	@Test
	public void testMultiSetColorForEmptyCollection() {
		Colorable c = Colorable.multi();
		c.color(Colors.chartreuse);
		c.color(Color.white);
	}

	@Test
	public void testMultiSetColor() {
		TestColorable c0 = new TestColorable();
		TestColorable c1 = new TestColorable();
		TestColorable c2 = new TestColorable();
		Colorable c = Colorable.multi(c0, c1, c2);
		c.color(Colors.chartreuse);
		assertEquals(c0.color, Colors.chartreuse.color);
		assertEquals(c1.color, Colors.chartreuse.color);
		assertEquals(c2.color, Colors.chartreuse.color);
	}

	@Test
	public void testMultiGetColorForEmptyCollection() {
		Colorable c = Colorable.multi();
		assertNull(c.color());
	}

	@Test
	public void testMultiGetColor() {
		TestColorable c0 = new TestColorable();
		TestColorable c1 = new TestColorable();
		TestColorable c2 = new TestColorable();
		Colorable c = Colorable.multi(c0, c1, c2);
		c0.color(Color.red);
		c1.color(Color.cyan);
		c2.color(Color.magenta.getRGB());
		assertEquals(c.color(), Color.red);
	}

	private static class TestColorable implements Colorable {
		public Color color;

		@Override
		public Color color() {
			return color;
		}

		@Override
		public void color(int r, int g, int b) {
			color = new Color(r, g, b);
		}
	}
}
