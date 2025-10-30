package ceri.common.color;

import java.awt.Color;
import org.junit.Test;
import ceri.common.test.Assert;

public class ColoringTest {

	@Test
	public void testColor() {
		Assert.equal(Coloring.color("test"), null);
		Assert.equal(Coloring.color("aquamarine"), Coloring.aquamarine.color());
	}

	@Test
	public void testName() {
		Assert.equal(Coloring.name(Colors.color(0x123456)), null);
		Assert.equal(Coloring.name(Coloring.aquamarine.color()), "aquamarine");
	}

	@Test
	public void testFromName() {
		Assert.equal(Coloring.from("unknown"), null);
		Assert.equal(Coloring.from("aquamarine"), Coloring.aquamarine);
	}

	@Test
	public void testFromColor() {
		Color color = new Color(0xdc143c);
		Assert.equal(Coloring.from(color), Coloring.crimson);
	}

	@Test
	public void testFromValue() {
		Assert.equal(Coloring.valueOf("crimson"), Coloring.crimson);
	}

	@Test
	public void testRandom() {
		Assert.notNull(Coloring.random());
	}

	@Test
	public void shouldProvideXargb() {
		Assert.equal(Coloring.gainsboro.xargb(), 0xffdcdcdcL);
	}

	@Test
	public void shouldProvideLightnessValue() {
		Assert.equal(Coloring.black.lightness(), 0.0);
		Assert.approx(Coloring.blue.lightness(), 0.323);
		Assert.approx(Coloring.chocolate.lightness(), 0.560);
		Assert.approx(Coloring.cyan.lightness(), 0.911);
		Assert.approx(Coloring.white.lightness(), 1.0);
	}
}
