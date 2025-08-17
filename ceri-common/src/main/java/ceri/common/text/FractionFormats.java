package ceri.common.text;

import static ceri.common.validation.ValidationUtil.validateRange;
import static ceri.common.validation.ValidationUtil.validatef;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import ceri.common.collection.Enums;
import ceri.common.collection.Immutable;
import ceri.common.math.Fraction;
import ceri.common.stream.IntStream;
import ceri.common.stream.Streams;

/**
 * Provides parsing and formatting of fractions.
 */
public class FractionFormats {

	private FractionFormats() {}

	public static Fraction parse(String value) {
		return Parser.parse(value);
	}

	public static String format(Fraction fraction) {
		return Formatter.format(fraction);
	}

	public enum Glyph {
		oneQuarter(1, 4, '\u00BC'),
		oneHalf(1, 2, '\u00BD'),
		threeQuarters(3, 4, '\u00BE'),
		oneSeventh(1, 7, '\u2150'),
		oneNinth(1, 9, '\u2151'),
		oneTenth(1, 10, '\u2152'),
		oneThird(1, 3, '\u2153'),
		twoThirds(2, 3, '\u2154'),
		oneFifth(1, 5, '\u2155'),
		twoFifths(2, 5, '\u2156'),
		threeFifths(3, 5, '\u2157'),
		fourFifths(4, 5, '\u2158'),
		oneSixth(1, 6, '\u2159'),
		fiveSixths(5, 6, '\u215A'),
		oneEighth(1, 8, '\u215B'),
		threeEighths(3, 8, '\u215C'),
		fiveEighths(5, 8, '\u215D'),
		sevenEighths(7, 8, '\u215E');

		private static final Map<Fraction, Glyph> map = Enums.map(g -> g.fraction, Glyph.class);
		public final Fraction fraction;
		public final double value;
		public final char code;

		Glyph(int numerator, int denominator, char code) {
			fraction = Fraction.of(numerator, denominator);
			this.code = code;
			value = (double) numerator / denominator;
		}

		public static Glyph from(char code) {
			return Enums.stream(Glyph.class).filter(g -> g.code == code).next();
		}

		public static Glyph of(Fraction fraction) {
			return map.get(fraction);
		}

		public static Glyph of(long numerator, long denominator) {
			return of(Fraction.of(numerator, denominator));
		}

		static String all() {
			return Enums.stream(Glyph.class).mapToInt(g -> g.code).collect(IntStream.Collect.chars);
		}
	}

	public enum Slash {
		solidus('\u002f'),
		fractionSlash('\u2044'),
		divisionSlash('\u2215'),
		fullwidthSolidus('\uff0f');

		public final char code;

		Slash(char code) {
			this.code = code;
		}

		static String all() {
			return Enums.stream(Slash.class).mapToInt(g -> g.code).collect(IntStream.Collect.chars);
		}
	}

	public static class Superscript {
		public static final char PLUS = '\u207a';
		public static final char MINUS = '\u207b';
		public static final char ONE_OVER = '\u215f';

		private Superscript() {}

		public static String format(long number) {
			return append(new StringBuilder(), number).toString();
		}

		static StringBuilder append(StringBuilder b, long number) {
			if (number < 0) b.append(MINUS);
			String.valueOf(Math.abs(number)).chars().forEach(c -> b.append(toChar(c - '0')));
			return b;
		}

		public static char toChar(long digit) {
			validateRange(digit, 0, 9);
			if (digit == 1) return '\u00b9';
			if (digit == 2) return '\u00b2';
			if (digit == 3) return '\u00b3';
			return (char) ('\u2070' + digit);
		}

		static String digits() {
			return Streams.range(0, 10).map(Superscript::toChar).collect(IntStream.Collect.chars);
		}

	}

	public static class Subscript {
		public static final char PLUS = '\u208a';
		public static final char MINUS = '\u208b';

		private Subscript() {}

		public static String format(long number) {
			return append(new StringBuilder(), number).toString();
		}

		static StringBuilder append(StringBuilder b, long number) {
			if (number < 0) b.append(MINUS);
			String.valueOf(Math.abs(number)).chars().forEach(c -> b.append(toChar(c - '0')));
			return b;
		}

		public static char toChar(long digit) {
			validateRange(digit, 0, 9);
			return (char) ('\u2080' + digit);
		}

		static String digits() {
			return Streams.range(0, 10).map(Subscript::toChar)
				.collect(ceri.common.stream.IntStream.Collect.chars);
		}
	}

	static class Parser {
		private static final Map<Character, String> EXPANSIONS = expansions();
		private static final Pattern EXPAND_REGEX = RegexUtil.compile("[%s]", expandables());
		private static final String INTEGERS = "[+-]?[0-9]+";
		private static final String GLYPHS =
			String.format("[+-%s%s]?[%s]", Superscript.PLUS, Superscript.MINUS, Glyph.all());
		private static final String SUPERSCRIPTS = String.format("[+-%s%s]?[%s]+", Superscript.PLUS,
			Superscript.MINUS, Superscript.digits());
		private static final String SUPERSCRIPTS_OVER = String.format("[+-%s%s]?[%s]*%s",
			Superscript.PLUS, Superscript.MINUS, Superscript.digits(), Superscript.ONE_OVER);
		private static final String SLASHES = String.format("[%s]", Slash.all());
		private static final String SUBSCRIPTS =
			String.format("[+-%s%s]?[%s]+", Subscript.PLUS, Subscript.MINUS, Subscript.digits());
		private static final Pattern PATTERN = Pattern.compile(pattern());
		private static final Pattern EXPANDED_REGEX =
			RegexUtil.compile("(%s)/(%s)", INTEGERS, INTEGERS);

		private Parser() {}

		public static Fraction parse(String s) {
			if (!PATTERN.matcher(s).matches()) return null;
			String expanded = expand(s);
			Matcher m = EXPANDED_REGEX.matcher(expanded);
			validatef(m.matches(), "Expansion failed: %s", expanded);
			return Fraction.of(Integer.parseInt(m.group(1)), Integer.parseInt(m.group(2)));
		}

		public static String expand(String s) {
			return EXPAND_REGEX.matcher(s).replaceAll(m -> EXPANSIONS.get(m.group().charAt(0)));
		}

		private static String pattern() {
			// (glyphs|(ints|superscripts)/(ints|subscripts)|superscripts-over(ints|subscripts))
			return String.format("(?:%2$s|(?:%1$s|%3$s)%5$s(?:%1$s|%6$s)|%4$s(?:%1$s|%6$s))",
				INTEGERS, GLYPHS, SUPERSCRIPTS, SUPERSCRIPTS_OVER, SLASHES, SUBSCRIPTS);
		}

		private static String expandables() {
			return Streams.from(EXPANSIONS.keySet()).mapToInt(c -> (int) c)
				.collect(IntStream.Collect.chars);
		}

		private static Map<Character, String> expansions() {
			var expansions = new LinkedHashMap<Character, String>();
			for (Glyph g : Glyph.values())
				expansions.put(g.code, g.fraction.toString());
			expansions.put(Superscript.PLUS, "+");
			expansions.put(Superscript.MINUS, "-");
			expansions.put(Superscript.ONE_OVER, "1/");
			for (int i = 0; i < 10; i++)
				expansions.put(Superscript.toChar(i), String.valueOf(i));
			expansions.put(Subscript.PLUS, "+");
			expansions.put(Subscript.MINUS, "-");
			for (int i = 0; i < 10; i++)
				expansions.put(Subscript.toChar(i), String.valueOf(i));
			expansions.put(Slash.fractionSlash.code, "/");
			expansions.put(Slash.divisionSlash.code, "/");
			expansions.put(Slash.fullwidthSolidus.code, "/");
			return Immutable.wrap(expansions);
		}
	}

	static class Formatter {
		private Formatter() {}

		public static String format(Fraction fraction) {
			Glyph glyph = Glyph.of(fraction);
			if (glyph != null) return String.valueOf(glyph.code);
			if (fraction.numerator() == 1) return toOneOverString(fraction.denominator());
			return toSuperSlashSubString(fraction);
		}

		private static String toSuperSlashSubString(Fraction fraction) {
			return Superscript.format(fraction.numerator()) + Slash.solidus.code
				+ Subscript.format(fraction.denominator());
		}

		private static String toOneOverString(long denominator) {
			return Subscript.append(new StringBuilder().append(Superscript.ONE_OVER), denominator)
				.toString();
		}
	}
}
