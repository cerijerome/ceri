package ceri.common.color;

import static ceri.common.test.AssertUtil.*;
import static ceri.common.test.ErrorGen.*;
import static ceri.common.test.TestUtil.*;
import static org.hamcrest.CoreMatchers.*;
import static org.mockito.Mockito.*;
import org.junit.Test;
import ceri.common.test.CallSync;

public class XybColorBehavior {

	@Test
	public void shouldNotBreachEqualsContract() {
		XybColor t = XybColor.of(1.0, 0.7, 0.5, 0.8);
		XybColor eq0 = XybColor.of(1.0, 0.7, 0.5, 0.8);
		XybColor ne0 = XybColor.of(0.9, 0.7, 0.5, 0.8);
		XybColor ne1 = XybColor.of(1.0, 0.6, 0.5, 0.8);
		XybColor ne2 = XybColor.of(1.0, 0.7, 0.6, 0.8);
		XybColor ne3 = XybColor.of(1.0, 0.7, 0.5, 0.7);
		exerciseEquals(t, eq0);
		assertAllNotEqual(t, ne0, ne1, ne2, ne3);
	}

	@Test
	public void should() {
		//XybColor.
	}

}
