package ceri.common.math;

import static ceri.common.test.TestUtil.assertNull;
import static ceri.common.test.TestUtil.assertThat;
import static org.hamcrest.CoreMatchers.is;
import org.junit.Test;

public class NumberPatternsBehavior {
	private static final NumberPatterns patterns = NumberPatterns.DEFAULT;

	@Test
	public void shouldExposePatternStrings() {
		assertThat(patterns.integer.format(), is("[0-9]+"));
	}

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
		assertNull(patterns.integer.parse(null));
		assertNull(patterns.integer.parse(""));
		assertNull(patterns.integer.parse("0.9"));
		assertNull(patterns.integer.parse("0.999.999"));
		assertNull(patterns.integer.parse("0.999,999"));
		assertNull(patterns.integer.parse(".9"));
		assertNull(patterns.integer.parse("100."));
		assertNull(patterns.integer.parse("10,000"));
		assertNull(patterns.integer.parse("1,0000"));
		assertNull(patterns.integer.parse(",10000"));
		assertNull(patterns.integer.parse("10000,"));
		assertNull(patterns.integer.parse("1000,0"));
		assertNull(patterns.integer.parse("100,00"));
		assertNull(patterns.integer.parse("10,,000"));
		assertThat(patterns.integer.parse("0"), is(0.0));
		assertThat(patterns.integer.parse("999999999"), is(999999999.0));
		assertNull(patterns.integer.parse("999,999,999"));
	}

	@Test
	public void shouldMatchIntegersWithStrictSeparators() {
		assertNull(patterns.integerStrictSeparator.parse(null));
		assertNull(patterns.integerStrictSeparator.parse(""));
		assertNull(patterns.integerStrictSeparator.parse("0.9"));
		assertNull(patterns.integerStrictSeparator.parse("0.999.999"));
		assertNull(patterns.integerStrictSeparator.parse("0.999,999"));
		assertNull(patterns.integerStrictSeparator.parse(".9"));
		assertNull(patterns.integerStrictSeparator.parse("100."));
		assertThat(patterns.integerStrictSeparator.parse("10,000"), is(10000.0));
		assertNull(patterns.integerStrictSeparator.parse("1,0000"));
		assertNull(patterns.integerStrictSeparator.parse(",10000"));
		assertNull(patterns.integerStrictSeparator.parse("10000,"));
		assertNull(patterns.integerStrictSeparator.parse("1000,0"));
		assertNull(patterns.integerStrictSeparator.parse("100,00"));
		assertNull(patterns.integerStrictSeparator.parse("10,,000"));
		assertThat(patterns.integerStrictSeparator.parse("0"), is(0.0));
		assertNull(patterns.integerStrictSeparator.parse("999999999"));
		assertThat(patterns.integerStrictSeparator.parse("999,999,999"), is(999999999.0));
	}

	@Test
	public void shouldMatchIntegersWithLooseSeparators() {
		assertNull(patterns.integerLooseSeparator.parse(null));
		assertNull(patterns.integerLooseSeparator.parse(""));
		assertNull(patterns.integerLooseSeparator.parse("0.9"));
		assertNull(patterns.integerLooseSeparator.parse(".9"));
		assertNull(patterns.integerLooseSeparator.parse("0.999.999"));
		assertNull(patterns.integerLooseSeparator.parse("0.999,999"));
		assertNull(patterns.integerLooseSeparator.parse("100."));
		assertThat(patterns.integerLooseSeparator.parse("10,000"), is(10000.0));
		assertThat(patterns.integerLooseSeparator.parse("1,0000"), is(10000.0));
		assertNull(patterns.integerLooseSeparator.parse(",10000"));
		assertNull(patterns.integerLooseSeparator.parse("10000,"));
		assertThat(patterns.integerLooseSeparator.parse("1000,0"), is(10000.0));
		assertThat(patterns.integerLooseSeparator.parse("100,00"), is(10000.0));
		assertNull(patterns.integerLooseSeparator.parse("10,,000"));
		assertThat(patterns.integerLooseSeparator.parse("0"), is(0.0));
		assertThat(patterns.integerLooseSeparator.parse("999999999"), is(999999999.0));
		assertThat(patterns.integerLooseSeparator.parse("999,999,999"), is(999999999.0));
	}

	@Test
	public void shouldMatchDoublesWithoutSeparators() {
		assertNull(patterns.number.parse(null));
		assertNull(patterns.number.parse(""));
		assertThat(patterns.number.parse("0.9"), is(0.9));
		assertNull(patterns.number.parse("0.999.999"));
		assertNull(patterns.number.parse("0.999,999"));
		assertThat(patterns.number.parse(".9"), is(0.9));
		assertNull(patterns.number.parse("100."));
		assertNull(patterns.number.parse("10,000"));
		assertNull(patterns.number.parse("1,0000"));
		assertNull(patterns.number.parse(",10000"));
		assertNull(patterns.number.parse("10000,"));
		assertNull(patterns.number.parse("1000,0"));
		assertNull(patterns.number.parse("100,00"));
		assertNull(patterns.number.parse("10,,000"));
		assertThat(patterns.number.parse("0"), is(0.0));
		assertThat(patterns.number.parse("999999999"), is(999999999.0));
		assertNull(patterns.number.parse("999,999,999"));
		assertThat(patterns.number.parse("999999.999"), is(999999.999));
		assertNull(patterns.number.parse("999.999.999"));
		assertNull(patterns.number.parse("999.999,999"));
		assertNull(patterns.number.parse("999,999.999"));
		assertNull(patterns.number.parse(",999,999.999"));
	}

	@Test
	public void shouldMatchDoublesWithLooseSeparators() {
		assertNull(patterns.numberLooseSeparator.parse(null));
		assertNull(patterns.numberLooseSeparator.parse(""));
		assertThat(patterns.numberLooseSeparator.parse("0.9"), is(0.9));
		assertNull(patterns.numberLooseSeparator.parse("0.999.999"));
		assertNull(patterns.numberLooseSeparator.parse("0.999,999"));
		assertThat(patterns.numberLooseSeparator.parse(".9"), is(0.9));
		assertNull(patterns.numberLooseSeparator.parse("100."));
		assertThat(patterns.numberLooseSeparator.parse("10,000"), is(10000.0));
		assertThat(patterns.numberLooseSeparator.parse("1,0000"), is(10000.0));
		assertNull(patterns.numberLooseSeparator.parse(",10000"));
		assertNull(patterns.numberLooseSeparator.parse("10000,"));
		assertThat(patterns.numberLooseSeparator.parse("1000,0"), is(10000.0));
		assertThat(patterns.numberLooseSeparator.parse("100,00"), is(10000.0));
		assertNull(patterns.numberLooseSeparator.parse("10,,000"));
		assertThat(patterns.numberLooseSeparator.parse("0"), is(0.0));
		assertThat(patterns.numberLooseSeparator.parse("999999999"), is(999999999.0));
		assertThat(patterns.numberLooseSeparator.parse("999,999,999"), is(999999999.0));
		assertThat(patterns.numberLooseSeparator.parse("999999.999"), is(999999.999));
		assertNull(patterns.numberLooseSeparator.parse("999.999.999"));
		assertNull(patterns.numberLooseSeparator.parse("999.999,999"));
		assertThat(patterns.numberLooseSeparator.parse("999,999.999"), is(999999.999));
		assertNull(patterns.numberLooseSeparator.parse(",999,999.999"));
	}

	@Test
	public void shouldMatchDoublesWithStrictSeparators() {
		assertNull(patterns.numberStrictSeparator.parse(null));
		assertNull(patterns.numberStrictSeparator.parse(""));
		assertThat(patterns.numberStrictSeparator.parse("0.9"), is(0.9));
		assertNull(patterns.numberStrictSeparator.parse("0.999.999"));
		assertNull(patterns.numberStrictSeparator.parse("0.999,999"));
		assertThat(patterns.numberStrictSeparator.parse(".9"), is(0.9));
		assertNull(patterns.numberStrictSeparator.parse("100."));
		assertThat(patterns.numberStrictSeparator.parse("10,000"), is(10000.0));
		assertNull(patterns.numberStrictSeparator.parse("1,0000"));
		assertNull(patterns.numberStrictSeparator.parse(",10000"));
		assertNull(patterns.numberStrictSeparator.parse("10000,"));
		assertNull(patterns.numberStrictSeparator.parse("1000,0"));
		assertNull(patterns.numberStrictSeparator.parse("100,00"));
		assertNull(patterns.numberStrictSeparator.parse("10,,000"));
		assertThat(patterns.numberStrictSeparator.parse("0"), is(0.0));
		assertNull(patterns.numberStrictSeparator.parse("999999999"));
		assertThat(patterns.numberStrictSeparator.parse("999,999,999"), is(999999999.0));
		assertNull(patterns.numberStrictSeparator.parse("999999.999"));
		assertNull(patterns.numberStrictSeparator.parse("999.999.999"));
		assertNull(patterns.numberStrictSeparator.parse("999.999,999"));
		assertThat(patterns.numberStrictSeparator.parse("999,999.999"), is(999999.999));
		assertNull(patterns.numberStrictSeparator.parse(",999,999.999"));
	}

}
