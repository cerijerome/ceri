package ceri.common.color;

import static ceri.common.test.AssertUtil.*;
import static ceri.common.test.ErrorGen.*;
import static ceri.common.test.TestUtil.*;
import static org.hamcrest.CoreMatchers.*;
import static org.mockito.Mockito.*;
import org.junit.Test;
import ceri.common.test.CallSync;

public class LuvColorBehavior {

	@Test
	public void shouldNotBreachEqualsContract() {
		LuvColor t = LuvColor.of(1.0, 0.5, 0.4, 0.3);
		LuvColor eq0 = LuvColor.of(1.0, 0.5, 0.4, 0.3);
		LuvColor ne0 = LuvColor.of(0.9, 0.5, 0.4, 0.3);
		LuvColor ne1 = LuvColor.of(1.0, 0.4, 0.4, 0.3);
		LuvColor ne2 = LuvColor.of(1.0, 0.5, 0.5, 0.3);
		LuvColor ne3 = LuvColor.of(1.0, 0.5, 0.4, 0.4);
		exerciseEquals(t, eq0);
		assertAllNotEqual(t, ne0, ne1, ne2, ne3);
	}

	@Test
	public void should() {
		//LuvColor.
	}

}
