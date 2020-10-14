package ceri.common.color;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNull;
import static ceri.common.test.TestUtil.assertThat;
import java.awt.Color;
import org.junit.Test;

public class ColorableTest {

	@Test
	public void testMultiSetColorForEmptyCollection() {
		Colorable c = Colorable.multi();
		c.color(X11Color.chartreuse);
		c.color(Color.white);
	}

	@Test
	public void testMultiSetColor() {
		TestColorable c0 = new TestColorable();
		TestColorable c1 = new TestColorable();
		TestColorable c2 = new TestColorable();
		Colorable c = Colorable.multi(c0, c1, c2);
		c.color(X11Color.chartreuse);
		assertThat(c0.color, is(X11Color.chartreuse.color));
		assertThat(c1.color, is(X11Color.chartreuse.color));
		assertThat(c2.color, is(X11Color.chartreuse.color));
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
		assertThat(c.color(), is(Color.red));
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
