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
		assertThat(1.00001f, isApprox(1.0f, 0.00002f));
	}

	@Test
	public void shouldFailIfNotWithinDelta() {
		assertAssertion(() -> assertThat(1.00001, isApprox(1.0, 0.00001)));
		assertAssertion(() -> assertThat(1.00001f, isApprox(1.0f, 0.00001f)));
	}

	@Test
	public void shouldSucceedIfWithinDecimalPlaces() {
		assertThat(1.00001, isRounded(1.0, 4));
		assertThat(1.00001f, isRounded(1.0f, 4));
	}

	@Test
	public void shouldFailIfNotWithinDecimalPlaces() {
		assertAssertion(() -> assertThat(1.00001, isRounded(1.0, 8)));
		assertAssertion(() -> assertThat(1.00001f, isRounded(1.0f, 8)));
	}

	@Test
	public void shouldMatchInfiniteDoubles() {
		assertThat(Double.POSITIVE_INFINITY, isRounded(Double.POSITIVE_INFINITY, 1));
		assertThat(Double.NEGATIVE_INFINITY, isRounded(Double.NEGATIVE_INFINITY, 1));
		assertAssertion(() -> assertThat(0.0, isRounded(Double.NEGATIVE_INFINITY, 1)));
		assertAssertion(() -> assertThat(Double.NEGATIVE_INFINITY, isRounded(0.0, 1)));
		assertAssertion(() -> assertThat(Double.POSITIVE_INFINITY,
			isRounded(Double.NEGATIVE_INFINITY, 1)));
		assertAssertion(() -> assertThat(Double.NEGATIVE_INFINITY,
			isRounded(Double.POSITIVE_INFINITY, 1)));
	}
	
	@Test
	public void shouldMatchInfiniteFloats() {
		assertThat(Float.POSITIVE_INFINITY, isRounded(Float.POSITIVE_INFINITY, 1));
		assertThat(Float.NEGATIVE_INFINITY, isRounded(Float.NEGATIVE_INFINITY, 1));
		assertAssertion(() -> assertThat(0.0f, isRounded(Float.NEGATIVE_INFINITY, 1)));
		assertAssertion(() -> assertThat(Float.NEGATIVE_INFINITY, isRounded(0.0f, 1)));
		assertAssertion(() -> assertThat(Float.POSITIVE_INFINITY,
			isRounded(Float.NEGATIVE_INFINITY, 1)));
		assertAssertion(() -> assertThat(Float.NEGATIVE_INFINITY,
			isRounded(Float.POSITIVE_INFINITY, 1)));
	}
	
	@Test
	public void shouldFailForNull() {
		assertAssertion(() -> assertThat(null, isRounded(1.0, 8)));
		assertAssertion(() -> assertThat(null, isRounded(1.0f, 8)));
	}
	
}
