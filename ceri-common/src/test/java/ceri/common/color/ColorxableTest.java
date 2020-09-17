package ceri.common.color;

import static ceri.common.color.ColorTestUtil.assertColorx;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import org.junit.Test;

public class ColorxableTest {

	@Test
	public void testMultiSetColorWithEmptyCollection() {
		Colorxable c = Colorxable.multi();
		c.colorx(0xee, 0x77, 0x33, 0xaa);
		c.colorx(Colorx.full);
	}

	@Test
	public void testMultiSetColor() {
		TestColorxable c0 = new TestColorxable();
		TestColorxable c1 = new TestColorxable();
		TestColorxable c2 = new TestColorxable();
		Colorxable c = Colorxable.multi(c0, c1, c2);
		c.colorx(0x10203040);
		assertThat(c0.colorx.rgbx(), is(0x10203040));
		assertThat(c1.colorx.rgbx(), is(0x10203040));
		assertThat(c2.colorx.rgbx(), is(0x10203040));
	}

	@Test
	public void testMultiGetColorWithEmptyCollection() {
		Colorxable c = Colorxable.multi();
		assertNull(c.colorx());
	}

	@Test
	public void testMultiGetColor() {
		TestColorxable c0 = new TestColorxable();
		TestColorxable c1 = new TestColorxable();
		TestColorxable c2 = new TestColorxable();
		Colorxable c = Colorxable.multi(c0, c1, c2);
		c0.colorx(0xff000010);
		c1.colorx(0x00ff0020);
		c2.colorx(0x0000ff30);
		assertColorx(c.colorx(), 0xff000010);
	}

	private static class TestColorxable implements Colorxable {
		public Colorx colorx;

		@Override
		public Colorx colorx() {
			return colorx;
		}

		@Override
		public void colorx(int r, int g, int b, int x) {
			colorx = Colorx.of(r, g, b, x);
		}
	}
}
