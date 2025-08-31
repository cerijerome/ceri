package ceri.common.text;

import static ceri.common.validation.ValidationUtil.validateMin;
import java.util.regex.Pattern;
import ceri.common.math.Radix;

public class Chars {
	public static final char NUL = '\0';
	public static final char BS = '\b';
	public static final char TAB = '\t'; // HT
	public static final char NL = '\n'; // LF
	public static final char FF = '\f';
	public static final char CR = '\r';
	public static final char ESC = '\u001b';
	public static final char DEL = '\u007f';
	public static final char QUOT = '"';
	public static final char SQUOT = '\'';
	public static final char BSLASH = '\\';

	private Chars() {}

	/**
	 * Char escape support.
	 */
	public static class Escape {
		public static Format UTF16 = Format.of(Radix.HEX.n, "\\u", 4, 4);
		public static Format OCT = Format.of(Radix.OCT.n, "\\", 1, 3);
		public static Format HEX = Format.of(Radix.HEX.n, "\\x", 2, 2); // not standard
		private static final Pattern REGEX = // java literal + regex compile => 4:1 backslashes
			Pattern.compile("\\\\\\\\|\\\\b|\\\\t|\\\\n|\\\\f|\\\\r|\\\\e"
				+ "|\\\\[0-3]?[0-7]?[0-7]|\\\\x[0-9a-fA-F]{2}|\\\\u[0-9a-fA-F]{4}");
		public static final String NUL = "\\0";
		public static final String BS = "\\b";
		public static final String TAB = "\\t";
		public static final String NL = "\\n";
		public static final String FF = "\\f";
		public static final String CR = "\\r";
		public static final String ESC = "\\e"; // not standard
		public static final String BSLASH = "\\\\";

		private Escape() {}
	}

	/**
	 * Returns the char at index, or null if out of range.
	 */
	public static Character at(CharSequence s, int index) {
		if (s == null || index < 0 || index >= s.length()) return null;
		return s.charAt(index);
	}

	/**
	 * Returns the char at index, or default if out of range.
	 */
	public static char at(CharSequence s, int index, char def) {
		if (s == null || index < 0 || index >= s.length()) return def;
		return s.charAt(index);
	}

	/**
	 * Simple (non-exhaustive) char printability check.
	 */
	public static boolean isPrintable(CharSequence s, int index) {
		return isPrintable(at(s, index, NUL));
	}
	
	/**
	 * Simple (non-exhaustive) char printability check.
	 */
	public static boolean isPrintable(char c) {
		return !Character.isISOControl(c)
			&& Character.UnicodeBlock.of(c) != Character.UnicodeBlock.SPECIALS;
	}

	/**
	 * Returns true if the chars mark a change to upper-case, or between letter and symbol.
	 */
	public static boolean isNameBoundary(char l, char r) {
		if (Character.isLetter(l) != Character.isLetter(r)) return true;
		if (Character.isDigit(l) != Character.isDigit(r)) return true;
		if (Character.isLowerCase(l) && Character.isUpperCase(r)) return true;
		return false;
	}

	/**
	 * Returns the char as a string if printable, or as an escaped string if non-printable.
	 */
	public static String escape(char c) {
		return appendEscape(new StringBuilder(), c).toString();
	}

	/**
	 * Returns the string with each non-printable char replaced by its escaped char sequence.
	 */
	public static String escape(CharSequence s) {
		if (s == null) return "";
		var b = new StringBuilder();
		for (int i = 0; i < s.length(); i++)
			appendEscape(b, s.charAt(i));
		return b.toString();
	}

	/**
	 * Returns the string with each escaped char sequence replaced by its unescaped char.
	 */
	public static String unescape(CharSequence s) {
		if (Strings.isEmpty(s)) return "";
		return Patterns.findAllAccept(Escape.REGEX, s, (b, m) -> appendUnescape(b, m.group()));
	}

	// support

	private static StringBuilder appendEscape(StringBuilder b, char c) {
		return switch (c) {
			case NUL -> b.append(Escape.NUL);
			case BS -> b.append(Escape.BS);
			case TAB -> b.append(Escape.TAB);
			case NL -> b.append(Escape.NL);
			case FF -> b.append(Escape.FF);
			case CR -> b.append(Escape.CR);
			case ESC -> b.append(Escape.ESC);
			case BSLASH -> b.append(Escape.BSLASH);
			default -> Chars.isPrintable(c) ? b.append(c) : Escape.UTF16.append(b, c);
		};
	}

	private static void appendUnescape(StringBuilder b, String escapedChar) {
		switch (escapedChar) {
			case Escape.BSLASH -> b.append(BSLASH);
			case Escape.BS -> b.append(BS);
			case Escape.ESC -> b.append(ESC);
			case Escape.FF -> b.append(FF);
			case Escape.NL -> b.append(NL);
			case Escape.CR -> b.append(CR);
			case Escape.TAB -> b.append(TAB);
			case Escape.NUL -> b.append(NUL);
			default -> b.append(decode(escapedChar));
		}
	}

	private static char decode(String escapedChar) {
		int c = decode(escapedChar, Escape.UTF16);
		if (c == -1) c = decode(escapedChar, Escape.HEX);
		if (c == -1) c = decode(escapedChar, Escape.OCT);
		validateMin(c, 0, "Escaped char");
		return (char) c;
	}

	private static int decode(String escapedChar, Format format) {
		if (!escapedChar.startsWith(format.prefix())) return -1;
		return Integer.parseUnsignedInt(escapedChar, format.prefix().length(), escapedChar.length(),
			format.radix());
	}
}
