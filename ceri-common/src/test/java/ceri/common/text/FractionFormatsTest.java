package ceri.common.text;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import org.junit.Test;
import ceri.common.math.Fraction;

public class FractionFormatsTest {

	@Test
	public void testParse() {
		assertThat(FractionFormats.parse("-777/-1"), is(Fraction.of(777, 1)));
		assertThat(FractionFormats.parse("-\u00b3\u215f\u208b\u2083"), is(Fraction.of(31, 3)));
		assertNull(FractionFormats.parse("-\u00b31/10"));
	}

	@Test
	public void testFormat() {
		assertThat(FractionFormats.format(Fraction.of(-7, 1)), is("\u207b\u2077/\u2081"));
		assertThat(FractionFormats.format(Fraction.of(1, 99)), is("\u215f\u2089\u2089"));
		assertThat(FractionFormats.format(Fraction.of(7, 8)), is("\u215e"));
	}

}
