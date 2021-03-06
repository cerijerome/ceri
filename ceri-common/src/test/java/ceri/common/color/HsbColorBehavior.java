package ceri.common.color;

import static ceri.common.test.AssertUtil.*;
import static ceri.common.test.ErrorGen.*;
import static ceri.common.test.TestUtil.*;
import static org.hamcrest.CoreMatchers.*;
import static org.mockito.Mockito.*;
import org.junit.Test;
import ceri.common.test.CallSync;

public class HsbColorBehavior {

	@Test
	public void shouldNotBreachEqualsContract() {
		HsbColor t = HsbColor.of(1.0, 0.5, 0.8, 0.6);
		HsbColor eq0 = HsbColor.of(1.0, 0.5, 0.8, 0.6);
		HsbColor ne0 = HsbColor.of(0.9, 0.5, 0.8, 0.6);
		HsbColor ne1 = HsbColor.of(1.0, 0.6, 0.8, 0.6);
		HsbColor ne2 = HsbColor.of(1.0, 0.5, 0.9, 0.6);
		HsbColor ne3 = HsbColor.of(1.0, 0.5, 0.8, 0.8);
		exerciseEquals(t, eq0);
		assertAllNotEqual(t, ne0, ne1, ne2, ne3);
	}

	@Test
	public void should() {
		//HsbColor.
	}

}
