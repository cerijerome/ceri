package ceri.common.math;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import org.junit.Test;

public class NumberPatternsBehavior {
	private static final NumberPatterns patterns = NumberPatterns.DEFAULT;

	@Test
	public void shouldRemoveSeparators() {
		assertNull(patterns.removeSeparators(null));
		assertThat(patterns.removeSeparators(""), is(""));
		assertThat(patterns.removeSeparators("10,000"), is("10000"));
		assertThat(patterns.removeSeparators("1,0000"), is("10000"));
		assertThat(patterns.removeSeparators(",10000"), is(",10000"));
		assertThat(patterns.removeSeparators("10000,"), is("10000,"));
		assertThat(patterns.removeSeparators("1000,0"), is("10000"));
		assertThat(patterns.removeSeparators("100,00"), is("10000"));
		assertThat(patterns.removeSeparators("10,,000"), is("10,,000"));
	}

	@Test
	public void shouldMatchIntegersWithoutSeparators() {
		assertNull(patterns.integral.parse(null));
		assertNull(patterns.integral.parse(""));
		assertNull(patterns.integral.parse("0.9"));
		assertNull(patterns.integral.parse("0.999.999"));
		assertNull(patterns.integral.parse("0.999,999"));
		assertNull(patterns.integral.parse(".9"));
		assertNull(patterns.integral.parse("100."));
		assertNull(patterns.integral.parse("10,000"));
		assertNull(patterns.integral.parse("1,0000"));
		assertNull(patterns.integral.parse(",10000"));
		assertNull(patterns.integral.parse("10000,"));
		assertNull(patterns.integral.parse("1000,0"));
		assertNull(patterns.integral.parse("100,00"));
		assertNull(patterns.integral.parse("10,,000"));
		assertThat(patterns.integral.parse("0"), is(0.0));
		assertThat(patterns.integral.parse("999999999"), is(999999999.0));
		assertNull(patterns.integral.parse("999,999,999"));
	}

	@Test
	public void shouldMatchIntegersWithStrictSeparators() {
		assertNull(patterns.integralStrictSeparator.parse(null));
		assertNull(patterns.integralStrictSeparator.parse(""));
		assertNull(patterns.integralStrictSeparator.parse("0.9"));
		assertNull(patterns.integralStrictSeparator.parse("0.999.999"));
		assertNull(patterns.integralStrictSeparator.parse("0.999,999"));
		assertNull(patterns.integralStrictSeparator.parse(".9"));
		assertNull(patterns.integralStrictSeparator.parse("100."));
		assertThat(patterns.integralStrictSeparator.parse("10,000"), is(10000.0));
		assertNull(patterns.integralStrictSeparator.parse("1,0000"));
		assertNull(patterns.integralStrictSeparator.parse(",10000"));
		assertNull(patterns.integralStrictSeparator.parse("10000,"));
		assertNull(patterns.integralStrictSeparator.parse("1000,0"));
		assertNull(patterns.integralStrictSeparator.parse("100,00"));
		assertNull(patterns.integralStrictSeparator.parse("10,,000"));
		assertThat(patterns.integralStrictSeparator.parse("0"), is(0.0));
		assertNull(patterns.integralStrictSeparator.parse("999999999"));
		assertThat(patterns.integralStrictSeparator.parse("999,999,999"), is(999999999.0));
	}

	@Test
	public void shouldMatchIntegersWithLooseSeparators() {
		assertNull(patterns.integralLooseSeparator.parse(null));
		assertNull(patterns.integralLooseSeparator.parse(""));
		assertNull(patterns.integralLooseSeparator.parse("0.9"));
		assertNull(patterns.integralLooseSeparator.parse(".9"));
		assertNull(patterns.integralLooseSeparator.parse("0.999.999"));
		assertNull(patterns.integralLooseSeparator.parse("0.999,999"));
		assertNull(patterns.integralLooseSeparator.parse("100."));
		assertThat(patterns.integralLooseSeparator.parse("10,000"), is(10000.0));
		assertThat(patterns.integralLooseSeparator.parse("1,0000"), is(10000.0));
		assertNull(patterns.integralLooseSeparator.parse(",10000"));
		assertNull(patterns.integralLooseSeparator.parse("10000,"));
		assertThat(patterns.integralLooseSeparator.parse("1000,0"), is(10000.0));
		assertThat(patterns.integralLooseSeparator.parse("100,00"), is(10000.0));
		assertNull(patterns.integralLooseSeparator.parse("10,,000"));
		assertThat(patterns.integralLooseSeparator.parse("0"), is(0.0));
		assertThat(patterns.integralLooseSeparator.parse("999999999"), is(999999999.0));
		assertThat(patterns.integralLooseSeparator.parse("999,999,999"), is(999999999.0));
	}

	@Test
	public void shouldMatchDoublesWithoutSeparators() {
		assertNull(patterns.floatingPoint.parse(null));
		assertNull(patterns.floatingPoint.parse(""));
		assertThat(patterns.floatingPoint.parse("0.9"), is(0.9));
		assertNull(patterns.floatingPoint.parse("0.999.999"));
		assertNull(patterns.floatingPoint.parse("0.999,999"));
		assertThat(patterns.floatingPoint.parse(".9"), is(0.9));
		assertNull(patterns.floatingPoint.parse("100."));
		assertNull(patterns.floatingPoint.parse("10,000"));
		assertNull(patterns.floatingPoint.parse("1,0000"));
		assertNull(patterns.floatingPoint.parse(",10000"));
		assertNull(patterns.floatingPoint.parse("10000,"));
		assertNull(patterns.floatingPoint.parse("1000,0"));
		assertNull(patterns.floatingPoint.parse("100,00"));
		assertNull(patterns.floatingPoint.parse("10,,000"));
		assertThat(patterns.floatingPoint.parse("0"), is(0.0));
		assertThat(patterns.floatingPoint.parse("999999999"), is(999999999.0));
		assertNull(patterns.floatingPoint.parse("999,999,999"));
		assertThat(patterns.floatingPoint.parse("999999.999"), is(999999.999));
		assertNull(patterns.floatingPoint.parse("999.999.999"));
		assertNull(patterns.floatingPoint.parse("999.999,999"));
		assertNull(patterns.floatingPoint.parse("999,999.999"));
		assertNull(patterns.floatingPoint.parse(",999,999.999"));
	}

	@Test
	public void shouldMatchDoublesWithLooseSeparators() {
		assertNull(patterns.floatingPointLooseSeparator.parse(null));
		assertNull(patterns.floatingPointLooseSeparator.parse(""));
		assertThat(patterns.floatingPointLooseSeparator.parse("0.9"), is(0.9));
		assertNull(patterns.floatingPointLooseSeparator.parse("0.999.999"));
		assertNull(patterns.floatingPointLooseSeparator.parse("0.999,999"));
		assertThat(patterns.floatingPointLooseSeparator.parse(".9"), is(0.9));
		assertNull(patterns.floatingPointLooseSeparator.parse("100."));
		assertThat(patterns.floatingPointLooseSeparator.parse("10,000"), is(10000.0));
		assertThat(patterns.floatingPointLooseSeparator.parse("1,0000"), is(10000.0));
		assertNull(patterns.floatingPointLooseSeparator.parse(",10000"));
		assertNull(patterns.floatingPointLooseSeparator.parse("10000,"));
		assertThat(patterns.floatingPointLooseSeparator.parse("1000,0"), is(10000.0));
		assertThat(patterns.floatingPointLooseSeparator.parse("100,00"), is(10000.0));
		assertNull(patterns.floatingPointLooseSeparator.parse("10,,000"));
		assertThat(patterns.floatingPointLooseSeparator.parse("0"), is(0.0));
		assertThat(patterns.floatingPointLooseSeparator.parse("999999999"), is(999999999.0));
		assertThat(patterns.floatingPointLooseSeparator.parse("999,999,999"), is(999999999.0));
		assertThat(patterns.floatingPointLooseSeparator.parse("999999.999"), is(999999.999));
		assertNull(patterns.floatingPointLooseSeparator.parse("999.999.999"));
		assertNull(patterns.floatingPointLooseSeparator.parse("999.999,999"));
		assertThat(patterns.floatingPointLooseSeparator.parse("999,999.999"), is(999999.999));
		assertNull(patterns.floatingPointLooseSeparator.parse(",999,999.999"));
	}

	@Test
	public void shouldMatchDoublesWithStrictSeparators() {
		assertNull(patterns.floatingPointStrictSeparator.parse(null));
		assertNull(patterns.floatingPointStrictSeparator.parse(""));
		assertThat(patterns.floatingPointStrictSeparator.parse("0.9"), is(0.9));
		assertNull(patterns.floatingPointStrictSeparator.parse("0.999.999"));
		assertNull(patterns.floatingPointStrictSeparator.parse("0.999,999"));
		assertThat(patterns.floatingPointStrictSeparator.parse(".9"), is(0.9));
		assertNull(patterns.floatingPointStrictSeparator.parse("100."));
		assertThat(patterns.floatingPointStrictSeparator.parse("10,000"), is(10000.0));
		assertNull(patterns.floatingPointStrictSeparator.parse("1,0000"));
		assertNull(patterns.floatingPointStrictSeparator.parse(",10000"));
		assertNull(patterns.floatingPointStrictSeparator.parse("10000,"));
		assertNull(patterns.floatingPointStrictSeparator.parse("1000,0"));
		assertNull(patterns.floatingPointStrictSeparator.parse("100,00"));
		assertNull(patterns.floatingPointStrictSeparator.parse("10,,000"));
		assertThat(patterns.floatingPointStrictSeparator.parse("0"), is(0.0));
		assertNull(patterns.floatingPointStrictSeparator.parse("999999999"));
		assertThat(patterns.floatingPointStrictSeparator.parse("999,999,999"), is(999999999.0));
		assertNull(patterns.floatingPointStrictSeparator.parse("999999.999"));
		assertNull(patterns.floatingPointStrictSeparator.parse("999.999.999"));
		assertNull(patterns.floatingPointStrictSeparator.parse("999.999,999"));
		assertThat(patterns.floatingPointStrictSeparator.parse("999,999.999"), is(999999.999));
		assertNull(patterns.floatingPointStrictSeparator.parse(",999,999.999"));
	}

}
