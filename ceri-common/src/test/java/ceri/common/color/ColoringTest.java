package ceri.common.color;

import static ceri.common.color.Colors.color;
import static ceri.common.test.Assert.assertEquals;
import java.awt.Color;
import org.junit.Test;
import ceri.common.test.Assert;

public class ColoringTest {

	@Test
	public void testColor() {
		assertEquals(Coloring.color("test"), null);
		assertEquals(Coloring.color("aquamarine"), Coloring.aquamarine.color());
	}

	@Test
	public void testName() {
		assertEquals(Coloring.name(color(0x123456)), null);
		assertEquals(Coloring.name(Coloring.aquamarine.color()), "aquamarine");
	}

	@Test
	public void testFromName() {
		assertEquals(Coloring.from("unknown"), null);
		assertEquals(Coloring.from("aquamarine"), Coloring.aquamarine);
	}

	@Test
	public void testFromColor() {
		Color color = new Color(0xdc143c);
		assertEquals(Coloring.from(color), Coloring.crimson);
	}

	@Test
	public void testFromValue() {
		assertEquals(Coloring.valueOf("crimson"), Coloring.crimson);
	}

	@Test
	public void testRandom() {
		Assert.notNull(Coloring.random());
	}

	@Test
	public void shouldProvideXargb() {
		assertEquals(Coloring.gainsboro.xargb(), 0xffdcdcdcL);
	}

	@Test
	public void shouldProvideLightnessValue() {
		assertEquals(Coloring.black.lightness(), 0.0);
		Assert.approx(Coloring.blue.lightness(), 0.323);
		Assert.approx(Coloring.chocolate.lightness(), 0.560);
		Assert.approx(Coloring.cyan.lightness(), 0.911);
		Assert.approx(Coloring.white.lightness(), 1.0);
	}
}
