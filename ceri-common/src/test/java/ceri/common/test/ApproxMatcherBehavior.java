package ceri.common.test;

import static ceri.common.test.TestUtil.assertAssertion;
import static ceri.common.test.TestUtil.isApprox;
import static ceri.common.test.TestUtil.isRounded;
import static org.junit.Assert.assertThat;
import org.junit.Test;

public class ApproxMatcherBehavior {

	@Test
	public void shouldSucceedIfWithinDelta() {
		assertThat(1.00001, isApprox(1.0, 0.00002));
	}

	@Test
	public void shouldFailIfNotWithinDelta() {
		assertAssertion(() -> assertThat(1.00001, isApprox(1.0, 0.00001)));
	}

	@Test
	public void shouldSucceedIfWithinDecimalPlaces() {
		assertThat(1.00001, isRounded(1.0, 4));
	}

	@Test
	public void shouldFailIfNotWithinDecimalPlaces() {
		assertAssertion(() -> assertThat(1.00001, isRounded(1.0, 8)));
	}

}
