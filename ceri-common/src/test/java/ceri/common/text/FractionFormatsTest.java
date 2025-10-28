package ceri.common.text;

import static ceri.common.test.Testing.exerciseEnum;
import org.junit.Test;
import ceri.common.math.Fraction;
import ceri.common.test.Assert;
import ceri.common.text.FractionFormats.Formatter;
import ceri.common.text.FractionFormats.Glyph;
import ceri.common.text.FractionFormats.Parser;
import ceri.common.text.FractionFormats.Slash;
import ceri.common.text.FractionFormats.Subscript;
import ceri.common.text.FractionFormats.Superscript;

public class FractionFormatsTest {

	@Test
	public void testConstructorIsPrivate() {
		Assert.privateConstructor(FractionFormats.class);
		Assert.privateConstructor(Superscript.class);
		Assert.privateConstructor(Subscript.class);
		Assert.privateConstructor(Parser.class);
		Assert.privateConstructor(Formatter.class);
		exerciseEnum(Glyph.class);
		exerciseEnum(Slash.class);
	}

	@Test
	public void testParse() {
		Assert.equal(FractionFormats.parse("-777/-1"), Fraction.of(777, 1));
		Assert.equal(FractionFormats.parse("\u2154"), Fraction.of(2, 3));
		Assert.equal(FractionFormats.parse("-\u00b3\u215f\u208b\u2083"), Fraction.of(31, 3));
		Assert.isNull(FractionFormats.parse("-\u00b31/10"));
	}

	@Test
	public void testFormat() {
		Assert.equal(FractionFormats.format(Fraction.of(-7, 1)), "\u207b\u2077/\u2081");
		Assert.equal(FractionFormats.format(Fraction.of(1, 99)), "\u215f\u2089\u2089");
		Assert.equal(FractionFormats.format(Fraction.of(7, 8)), "\u215e");
	}

	@Test
	public void shouldParseGlyphs() {
		Assert.isNull(Glyph.of(null));
		Assert.equal(Glyph.of(3, 4), Glyph.threeQuarters);
		Assert.equal(Glyph.from('\u2155'), Glyph.oneFifth);
	}

	@Test
	public void shouldFormatSuperscripts() {
		Assert.equal(Superscript.format(-309), "\u207b\u00b3\u2070\u2079");
		Assert.equal(Superscript.format(44), "\u2074\u2074");
	}

	@Test
	public void shouldFormatSubscripts() {
		Assert.equal(Subscript.format(-309), "\u208b\u2083\u2080\u2089");
		Assert.equal(Subscript.format(44), "\u2084\u2084");
	}
}
