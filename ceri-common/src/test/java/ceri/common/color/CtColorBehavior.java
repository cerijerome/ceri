package ceri.common.color;

import static ceri.common.test.TestUtil.assertAllNotEqual;
import static ceri.common.test.TestUtil.assertApprox;
import static ceri.common.test.TestUtil.exerciseEquals;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import org.junit.Test;

public class CtColorBehavior {

	@Test
	public void shouldNotBreachEqualsContract() {
		CtColor c0 = CtColor.of(6500);
		CtColor c1 = CtColor.of(6500);
		CtColor c2 = CtColor.of(6600);
		CtColor c3 = CtColor.of(6500, 0.8);
		CtColor c4 = CtColor.of(6500, 1.0, 0.9);
		exerciseEquals(c0, c1);
		assertAllNotEqual(c0, c2, c3, c4);
	}

	@Test
	public void shouldConvertBetweenMiredAndKelvin() {
		assertApprox(CtColor.of(4500).mired(), 222.222);
		assertThat(CtColor.miredToKelvin(222.222), is(4500));
	}

}
