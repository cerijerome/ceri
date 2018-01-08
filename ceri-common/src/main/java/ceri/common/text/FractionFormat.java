package ceri.common.text;

import static ceri.common.validation.ValidationUtil.validateRange;
import java.util.stream.Stream;
import ceri.common.collection.StreamUtil;
import ceri.common.math.Fraction;

public class FractionFormat {

	private FractionFormat() {}

	public static enum Glyph {
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

		public static char ONE_OVER = '\u215f';
		public final int numerator;
		public final int denominator;
		public final double value;
		public final char code;

		private Glyph(int numerator, int denominator, char code) {
			this.numerator = numerator;
			this.denominator = denominator;
			this.code = code;
			value = (double) numerator / denominator;
		}

		public static Glyph find(String s) {
			s = StringUtil.trim(s);
			if (s == null || s.length() != 1) return null;
			return find(s.charAt(0));
		}

		public static Glyph find(char glyph) {
			return StreamUtil.first(Stream.of(Glyph.values()).filter(g -> g.code == glyph));
		}

		public static Glyph of(Fraction fraction) {
			if (fraction == null) return null;
			return of(fraction.numerator, fraction.denominator);
		}

		public static Glyph of(long numerator, long denominator) {
			return StreamUtil.first(Stream.of(Glyph.values()).filter( //
				g -> g.matches(numerator, denominator)));
		}

		private boolean matches(long numerator, long denominator) {
			return this.numerator == numerator && this.denominator == denominator;
		}

	}

	public static enum Slash {
		solidus('\u002f'),
		fractionSlash('\u2044'),
		divisionSlash('\u2215'),
		fullwidthSolidus('\uff0f');

		public final char code;

		private Slash(char code) {
			this.code = code;
		}
	}

	public static class Superscript {
		public static char PLUS = '\u207a';
		public static char MINUS = '\u207b';

		private Superscript() {}

		public static String toString(long number) {
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
	}

	public static class Subscript {
		public static char PLUS = '\u208a';
		public static char MINUS = '\u208b';

		private Subscript() {}

		public static String toString(long number) {
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
	}

	public static Fraction fromString(String value) {
		return null;
	}

	public static String toString(Fraction fraction) {
		Glyph glyph = Glyph.of(fraction);
		if (glyph != null) return String.valueOf(glyph.code);
		if (fraction.numerator == 1) return toOneOverString(fraction.denominator);
		return toSuperSlashSubString(fraction);
	}

	private static String toSuperSlashSubString(Fraction fraction) {
		return Superscript.toString(fraction.numerator) + Slash.solidus.code +
			Subscript.toString(fraction.denominator);
	}

	private static String toOneOverString(long denominator) {
		return Subscript.append(new StringBuilder().append(Glyph.ONE_OVER), denominator).toString();
	}

}
