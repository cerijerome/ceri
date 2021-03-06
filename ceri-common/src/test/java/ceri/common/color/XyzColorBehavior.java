package ceri.common.color;

import static ceri.common.test.AssertUtil.*;
import static ceri.common.test.ErrorGen.*;
import static ceri.common.test.TestUtil.*;
import static org.hamcrest.CoreMatchers.*;
import static org.mockito.Mockito.*;
import org.junit.Test;
import ceri.common.test.CallSync;

public class XyzColorBehavior {

	@Test
	public void shouldNotBreachEqualsContract() {
		XyzColor t = XyzColor.of(1.0, 0.5, 0.8, 0.6);
		XyzColor eq0 = XyzColor.of(1.0, 0.5, 0.8, 0.6);
		XyzColor ne0 = XyzColor.of(0.9, 0.5, 0.8, 0.6);
		XyzColor ne1 = XyzColor.of(1.0, 0.6, 0.8, 0.6);
		XyzColor ne2 = XyzColor.of(1.0, 0.5, 0.9, 0.6);
		XyzColor ne3 = XyzColor.of(1.0, 0.5, 0.8, 0.7);
		exerciseEquals(t, eq0);
		assertAllNotEqual(t, ne0, ne1, ne2, ne3);
	}

	@Test
	public void should() {
		//XyzColor.
	}

}
