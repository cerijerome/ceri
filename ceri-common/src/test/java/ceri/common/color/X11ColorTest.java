package ceri.common.color;

import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertNotNull;
import java.awt.Color;
import org.junit.Test;

public class X11ColorTest {

	@Test
	public void testFromColor() {
		Color color = new Color(0xdc143c);
		assertEquals(X11Color.from(color), X11Color.crimson);
	}

	@Test
	public void testFromValue() {
		assertEquals(X11Color.valueOf("crimson"), X11Color.crimson);
	}

	@Test
	public void testRandom() {
		assertNotNull(X11Color.random());
	}

}
