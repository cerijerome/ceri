package ceri.common.color;

import static ceri.common.color.ColorTestUtil.assertColorx;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class ColorxableTest {
	@Mock
	Colorxable c0;
	@Mock
	Colorxable c1;
	@Mock
	Colorxable c2;

	@Before
	public void init() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void testMultiSetColor() {
		Colorxable c = Colorxable.multi();
		c.colorx(0xee, 0x77, 0x33, 0xaa);
		c.colorx(Colorx.full);
		c = Colorxable.multi(c0, c1, c2);
		c.colorx(0x10203040);
		verify(c0).colorx(0x10, 0x20, 0x30, 0x40);
		verify(c1).colorx(0x10, 0x20, 0x30, 0x40);
		verify(c2).colorx(0x10, 0x20, 0x30, 0x40);
	}

	@Test
	public void testMultiGetColor() {
		Colorxable c = Colorxable.multi();
		assertNull(c.colorx());
		c = Colorxable.multi(c0, c1, c2);
		when(c0.colorx()).thenReturn(Colorx.of(0xff000010));
		when(c1.colorx()).thenReturn(Colorx.of(0x00ff0020));
		when(c2.colorx()).thenReturn(Colorx.of(0x0000ff30));
		assertColorx(c.colorx(), 0xff000010);
	}

}
