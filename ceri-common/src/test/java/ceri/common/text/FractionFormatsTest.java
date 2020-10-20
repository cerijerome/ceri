package ceri.common.text;

import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertNull;
import static ceri.common.test.AssertUtil.assertPrivateConstructor;
import static ceri.common.test.TestUtil.exerciseEnum;
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
		assertEquals(FractionFormats.parse("-777/-1"), Fraction.of(777, 1));
		assertEquals(FractionFormats.parse("\u2154"), Fraction.of(2, 3));
		assertEquals(FractionFormats.parse("-\u00b3\u215f\u208b\u2083"), Fraction.of(31, 3));
		assertNull(FractionFormats.parse("-\u00b31/10"));
	}

	@Test
	public void testFormat() {
		assertEquals(FractionFormats.format(Fraction.of(-7, 1)), "\u207b\u2077/\u2081");
		assertEquals(FractionFormats.format(Fraction.of(1, 99)), "\u215f\u2089\u2089");
		assertEquals(FractionFormats.format(Fraction.of(7, 8)), "\u215e");
	}

	@Test
	public void shouldParseGlyphs() {
		assertNull(Glyph.of(null));
		assertEquals(Glyph.from('\u2155'), Glyph.oneFifth);
	}

	@Test
	public void shouldFormatSuperscripts() {
		assertEquals(Superscript.format(-309), "\u207b\u00b3\u2070\u2079");
		assertEquals(Superscript.format(44), "\u2074\u2074");
	}

	@Test
	public void shouldFormatSubscripts() {
		assertEquals(Subscript.format(-309), "\u208b\u2083\u2080\u2089");
		assertEquals(Subscript.format(44), "\u2084\u2084");
	}

}
