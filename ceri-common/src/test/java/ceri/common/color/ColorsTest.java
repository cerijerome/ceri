package ceri.common.color;

import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertNotNull;
import java.awt.Color;
import org.junit.Test;

public class ColorsTest {

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

}
