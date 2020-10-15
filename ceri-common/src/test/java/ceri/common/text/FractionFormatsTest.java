package ceri.common.text;

import static ceri.common.test.TestUtil.assertNull;
import static ceri.common.test.TestUtil.assertPrivateConstructor;
import static ceri.common.test.TestUtil.assertThat;
import static ceri.common.test.TestUtil.exerciseEnum;
import static org.hamcrest.CoreMatchers.is;
import org.junit.Test;
import ceri.common.math.Fraction;
import ceri.common.text.FractionFormats.Formatter;
import ceri.common.text.FractionFormats.Glyph;
import ceri.common.text.FractionFormats.Parser;
import ceri.common.text.FractionFormats.Slash;
import ceri.common.text.FractionFormats.Subscript;
import ceri.common.text.FractionFormats.Superscript;

public class FractionFormatsTest {

	@Test
	public void testConstructorIsPrivate() {
		assertPrivateConstructor(FractionFormats.class);
		assertPrivateConstructor(Superscript.class);
		assertPrivateConstructor(Subscript.class);
		assertPrivateConstructor(Parser.class);
		assertPrivateConstructor(Formatter.class);
		exerciseEnum(Glyph.class);
		exerciseEnum(Slash.class);
	}

	@Test
	public void testParse() {
		assertThat(FractionFormats.parse("-777/-1"), is(Fraction.of(777, 1)));
		assertThat(FractionFormats.parse("\u2154"), is(Fraction.of(2, 3)));
		assertThat(FractionFormats.parse("-\u00b3\u215f\u208b\u2083"), is(Fraction.of(31, 3)));
		assertNull(FractionFormats.parse("-\u00b31/10"));
	}

	@Test
	public void testFormat() {
		assertThat(FractionFormats.format(Fraction.of(-7, 1)), is("\u207b\u2077/\u2081"));
		assertThat(FractionFormats.format(Fraction.of(1, 99)), is("\u215f\u2089\u2089"));
		assertThat(FractionFormats.format(Fraction.of(7, 8)), is("\u215e"));
	}

	@Test
	public void shouldParseGlyphs() {
		assertNull(Glyph.of(null));
		assertThat(Glyph.from('\u2155'), is(Glyph.oneFifth));
	}

	@Test
	public void shouldFormatSuperscripts() {
		assertThat(Superscript.format(-309), is("\u207b\u00b3\u2070\u2079"));
		assertThat(Superscript.format(44), is("\u2074\u2074"));
	}

	@Test
	public void shouldFormatSubscripts() {
		assertThat(Subscript.format(-309), is("\u208b\u2083\u2080\u2089"));
		assertThat(Subscript.format(44), is("\u2084\u2084"));
	}

}
