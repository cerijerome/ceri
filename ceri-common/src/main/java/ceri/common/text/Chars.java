package ceri.common.text;

import java.awt.event.KeyEvent;
import java.util.regex.Pattern;
import ceri.common.function.Functions;

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
	 * 
	 */
	public enum Radix {
		hex(16, 2, 4, 8, 16),
		dec(10, 3, 5, 10, 19),
		oct(8, 3, 6, 11, 21),
		bin(2, 8, 16, 32, 64);
		
		public final int n;
		public final int byteDigits;
		public final int shortDigits;
		public final int intDigits;
		public final int longDigits;
		
		private Radix(int n, int... digits) {
			this.n = n;
			int i = 0;
			byteDigits = digits[i++];
			shortDigits = digits[i++];
			intDigits = digits[i++];
			longDigits = digits[i];
		}
	}
	
	public static class Escape {
		private static final Pattern REGEX = 
			Pattern.compile("\\\\\\\\|\\\\b|\\\\e|\\\\t|\\\\f|\\\\r|\\\\n|\\\\0[0-3][0-7]{2}"
				+ "|\\\\0[0-7]{2}|\\\\0[0-7]|\\\\0|\\\\x[0-9a-fA-F]{2}|\\\\u[0-9a-fA-F]{4}");
		private static final String NUL = "\\0";
		private static final String BS = "\\b";
		private static final String TAB = "\\t";
		private static final String NL = "\\n";
		private static final String FF = "\\f";
		private static final String CR = "\\r";
		private static final String ESC = "\\e";
		private static final String BSLASH = "\\\\";
		private static final String OCTAL = "\\0";
		private static final String HEX = "\\x";
		private static final String UTF16 = "\\u";

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
	 * Tries to match a character from its expanded escaped string.
	 */
	public static Character unEscape(String escapedChar) {
		if (escapedChar == null) return null;
		return switch (escapedChar) {
			case Escape.BSLASH -> BSLASH;
			case Escape.NUL -> NUL;
			case Escape.BS -> BS;
			case Escape.TAB -> TAB;
			case Escape.NL -> NL;
			case Escape.FF -> FF;
			case Escape.CR -> CR;
			case Escape.ESC -> ESC;
			default -> escapedCode(escapedChar);
		};
	}
	
	/**
	 * Checks if a char is printable
	 */
	public static boolean isPrintable(char c) {
		if (Character.isISOControl(c) || c == KeyEvent.CHAR_UNDEFINED) return false;
		return Character.UnicodeBlock.of(c) != Character.UnicodeBlock.SPECIALS;
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
	
	// support
	
	private static Character escapedCode(String escapedChar) {
		var c = escapedCode(escapedChar, Escape.OCTAL, Radix.oct.n);
		if (c == null) c = escapedCode(escapedChar, Escape.HEX, Radix.hex.n);
		if (c == null) c = escapedCode(escapedChar, Escape.UTF16, Radix.hex.n);
		return c;
	}
	
	private static Character escapedCode(String escapedChar, String prefix, int radix) {
		if (!escapedChar.startsWith(prefix)) return null;
		return (char) Integer.parseInt(escapedChar.substring(prefix.length()), radix);
	}
}
