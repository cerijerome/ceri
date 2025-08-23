package ceri.common.color;

import static ceri.common.color.Colors.color;
import static ceri.common.test.AssertUtil.assertApprox;
import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertNotNull;
import java.awt.Color;
import org.junit.Test;

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
		assertNotNull(Coloring.random());
	}

	@Test
	public void shouldProvideXargb() {
		assertEquals(Coloring.gainsboro.xargb(), 0xffdcdcdcL);
	}

	@Test
	public void shouldProvideLightnessValue() {
		assertEquals(Coloring.black.lightness(), 0.0);
		assertApprox(Coloring.blue.lightness(), 0.323);
		assertApprox(Coloring.chocolate.lightness(), 0.560);
		assertApprox(Coloring.cyan.lightness(), 0.911);
		assertApprox(Coloring.white.lightness(), 1.0);
	}
}
