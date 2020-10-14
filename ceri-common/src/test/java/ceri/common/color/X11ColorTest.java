package ceri.common.color;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static ceri.common.test.TestUtil.assertThat;
import java.awt.Color;
import org.junit.Test;

public class X11ColorTest {

	@Test
	public void testFromColor() {
		Color color = new Color(0xdc143c);
		assertThat(X11Color.from(color), is(X11Color.crimson));
	}

	@Test
	public void testFromValue() {
		assertThat(X11Color.valueOf("crimson"), is(X11Color.crimson));
	}

	@Test
	public void testRandom() {
		assertNotNull(X11Color.random());
	}

}
