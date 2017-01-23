package ceri.common.color;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.awt.Color;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import ceri.common.color.Colorable;
import ceri.common.color.X11Color;

public class ColorableTest {
	@Mock Colorable c0;
	@Mock Colorable c1;
	@Mock Colorable c2;

	@Before
	public void init() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void testMultiSetColor() {
		Colorable c = Colorable.multi();
		c.color(X11Color.chartreuse);
		c.color(Color.white);
		c = Colorable.multi(c0, c1, c2);
		c.color(0x102030);
		verify(c0).color(0x10, 0x20, 0x30);
		verify(c1).color(0x10, 0x20, 0x30);
		verify(c2).color(0x10, 0x20, 0x30);
	}

	@Test
	public void testMultiGetColor() {
		Colorable c = Colorable.multi();
		assertNull(c.color());
		c = Colorable.multi(c0, c1, c2);
		when(c0.color()).thenReturn(Color.red);
		when(c1.color()).thenReturn(Color.cyan);
		when(c2.color()).thenReturn(Color.magenta);
		assertThat(c.color(), is(Color.red));
	}

}
