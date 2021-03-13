package ceri.common.color;

import static ceri.common.color.ColorUtil.color;
import static ceri.common.test.AssertUtil.assertApprox;
import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertNotNull;
import java.awt.Color;
import org.junit.Test;

public class ColorsTest {

	@Test
	public void testColor() {
		assertEquals(Colors.color("test"), null);
		assertEquals(Colors.color("aquamarine"), Colors.aquamarine.color());
	}

	@Test
	public void testName() {
		assertEquals(Colors.name(color(0x123456)), null);
		assertEquals(Colors.name(Colors.aquamarine.color()), "aquamarine");
	}

	@Test
	public void testFromName() {
		assertEquals(Colors.from("unknown"), null);
		assertEquals(Colors.from("aquamarine"), Colors.aquamarine);
	}

	@Test
	public void testFromColor() {
		Color color = new Color(0xdc143c);
		assertEquals(Colors.from(color), Colors.crimson);
	}

	@Test
	public void testFromValue() {
		assertEquals(Colors.valueOf("crimson"), Colors.crimson);
	}

	@Test
	public void testRandom() {
		assertNotNull(Colors.random());
	}

	@Test
	public void shouldProvideXargb() {
		assertEquals(Colors.gainsboro.xargb(), 0xffdcdcdcL);
	}

	@Test
	public void shouldProvideLightnessValue() {
		assertEquals(Colors.black.lightness(), 0.0);
		assertApprox(Colors.blue.lightness(), 0.323);
		assertApprox(Colors.chocolate.lightness(), 0.560);
		assertApprox(Colors.cyan.lightness(), 0.911);
		assertApprox(Colors.white.lightness(), 1.0);
	}

}
