package ceri.common.text;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.regex.Pattern;
import ceri.common.array.RawArray;
import ceri.common.collect.Maps;
import ceri.common.data.ByteArray;
import ceri.common.data.ByteProvider;
import ceri.common.io.Buffers;
import ceri.common.math.Radix;
import ceri.common.util.Validate;

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
	 * Charset information.
	 */
	public record Info(ByteProvider bom, ByteProvider term, float bytesPerChar) {
		private static final Map<Charset, Info> cache = Maps.syncWeak();

		public static Info of(Charset charset) {
			return cache.computeIfAbsent(safe(charset), Info::from);
		}

		static Info from(Charset charset) {
			var bytes = "\0".getBytes(charset);
			int n = 0; // number of zeroes
			for (int i = 0; i < bytes.length; i++)
				if (bytes[i] == 0) n++;
			var bom = bom(bytes, n);
			var term = term(bytes, n);
			var bytesPerChar = charset.newEncoder().averageBytesPerChar();
			return new Info(bom, term, bytesPerChar);
		}

		private static ByteProvider bom(byte[] bytes, int n) {
			// Pragmatic BOM finder
			if (n == bytes.length || n == 0) return ByteProvider.empty(); // no BOM
			if (n == 1) return ByteArray.Immutable.wrap(bytes, 0, bytes.length - 1); // UTF8 BOM
			return ByteArray.Immutable.wrap(bytes, 0, bytes.length >> 1); // UTF16/32 BOM
		}

		private static ByteProvider term(byte[] bytes, int n) {
			// Pragmatic nul-terminator finder
			if (n == bytes.length) return ByteProvider.of(bytes); // no BOM
			if (n <= 1) return ByteProvider.of(0); // UTF8 with BOM or non-standard charset
			return ByteArray.Immutable.wrap(bytes, bytes.length >> 1); // UTF16/32 with BOM
		}
	}

	/**
	 * Operates on a char and returns a char.
	 */
	@FunctionalInterface
	public interface Operator<E extends Exception> {
		char applyAsChar(char c) throws E;
	}

	/**
	 * Char escape support.
	 */
	public static class Escape {
		public static Format.OfLong UTF16 = Format.ofLong("\\u", Radix.HEX.n, 4, 4);
		public static Format.OfLong OCT = Format.ofLong("\\", Radix.OCT.n, 1, 3);
		public static Format.OfLong HEX = Format.ofLong("\\x", Radix.HEX.n, 2, 2); // not standard
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
	 * Finds the named charset.
	 */
	public static Charset charset(String name) {
		return Strings.nonEmpty(name) ? Charset.forName(name) : Charset.defaultCharset();
	}

	/**
	 * Returns a compact name for the charset.
	 */
	public static String compactName(Charset charset) {
		if (charset == null) return "";
		var b = new StringBuilder();
		for (char c : charset.name().toCharArray())
			if (Character.isLetterOrDigit(c)) b.append(Character.toLowerCase(c));
		return b.toString();
	}

	/**
	 * Returns default charset if null.
	 */
	public static Charset safe(Charset charset) {
		return charset == null ? Charset.defaultCharset() : charset;
	}

	/**
	 * Returns empty string if null.
	 */
	public static CharSequence safe(CharSequence s) {
		return s == null ? "" : s;
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
	 * Returns the last char, or null if empty.
	 */
	public static Character last(CharSequence s) {
		if (s == null) return null;
		return at(s, s.length() - 1);
	}

	/**
	 * Returns the last char, or default if empty.
	 */
	public static char last(CharSequence s, char def) {
		if (s == null) return def;
		return at(s, s.length() - 1, def);
	}

	/**
	 * Converts each char to lower case.
	 */
	public static CharSequence lower(CharSequence s) {
		var b = StringBuilders.State.of(s);
		for (int i = 0; i < b.length(); i++)
			b.append(Character.toLowerCase(b.at(i)));
		return b.get();
	}

	/**
	 * Converts each char to upper case.
	 */
	public static CharSequence upper(CharSequence s) {
		var b = StringBuilders.State.of(s);
		for (int i = 0; i < b.length(); i++)
			b.append(Character.toUpperCase(b.at(i)));
		return b.get();
	}

	/**
	 * Returns true if the chars at each index are in range and the same value.
	 */
	public static boolean equals(CharSequence ls, int li, CharSequence rs, int ri) {
		if (ls == null || rs == null || li < 0 || li >= ls.length() || ri < 0 || ri >= rs.length())
			return false;
		return ls.charAt(li) == rs.charAt(ri);
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
		return appendEscape(StringBuilders.State.of(""), 0, c).toString();
	}

	/**
	 * Returns the string with each non-printable char replaced by its escaped char sequence.
	 */
	public static String escape(CharSequence s) {
		if (s == null) return "";
		var b = StringBuilders.State.of(s);
		for (int i = 0; i < b.length(); i++)
			appendEscape(b, i, b.at(i));
		return b.toString();
	}

	/**
	 * Returns the string with each escaped char sequence replaced by its unescaped char.
	 */
	public static String unescape(CharSequence s) {
		if (Strings.isEmpty(s)) return "";
		return Regex.appendAll(StringBuilders.State.of(s), Escape.REGEX,
			(b, m) -> appendUnescape(b, m.start(), m.group())).toString();
	}

	/**
	 * Encodes the string to byte array within bounds. Returns the number of bytes written. Encoding
	 * stops if there are no more chars to encode, the array slice has no space for the next full
	 * char, or the char sequence is malformed.
	 */
	public static int encode(CharSequence s, Charset charset, byte[] bytes) {
		return encode(s, charset, bytes, 0);
	}

	/**
	 * Encodes the string to byte array within bounds. Returns the number of bytes written. Encoding
	 * stops if there are no more chars to encode, the array slice has no space for the next full
	 * char, or the char sequence is malformed.
	 */
	public static int encode(CharSequence s, Charset charset, byte[] bytes, int index) {
		return encode(s, charset, bytes, index, Integer.MAX_VALUE);
	}

	/**
	 * Encodes the string to byte array within bounds. Returns the number of bytes written. Encoding
	 * stops if there are no more chars to encode, the array slice has no space for the next full
	 * char, or the char sequence is malformed.
	 */
	public static int encode(CharSequence s, Charset charset, byte[] bytes, int index, int len) {
		return encode(s, charset, Buffers.BYTE.of(bytes, index, len));
	}

	/**
	 * Encodes the string to the buffer within bounds. Returns the number of bytes written. Encoding
	 * stops if there are no more chars to encode, the buffer has no space for the next full char,
	 * or the char sequence is malformed.
	 */
	public static int encode(CharSequence s, Charset charset, ByteBuffer buffer) {
		if (s == null || buffer == null || buffer.remaining() == 0) return 0;
		int i = buffer.position();
		safe(charset).newEncoder().encode(CharBuffer.wrap(s), buffer, true);
		return buffer.position() - i;
	}

	/**
	 * Decodes the byte array to a string, replacing malformed chars, and dropping any trailing
	 * partial char encoding.
	 */
	public static String decode(Charset charset, byte[] bytes) {
		return decode(charset, bytes, 0);
	}

	/**
	 * Decodes the byte array slice to a string, replacing malformed chars, and dropping any
	 * trailing partial char encoding.
	 */
	public static String decode(Charset charset, byte[] bytes, int index) {
		return decode(charset, bytes, index, Integer.MAX_VALUE);
	}

	/**
	 * Decodes the byte array slice to a string, replacing malformed chars, and dropping any
	 * trailing partial char encoding.
	 */
	public static String decode(Charset charset, byte[] bytes, int index, int len) {
		if (bytes == null) return "";
		return RawArray.applySlice(bytes, index, len,
			(i, l) -> decode(charset, ByteBuffer.wrap(bytes, i, l)));
	}

	/**
	 * Decodes the buffer to a string, replacing malformed chars, and dropping any trailing partial
	 * char encoding.
	 */
	public static String decode(Charset charset, ByteBuffer buffer) {
		if (buffer == null || !buffer.hasRemaining()) return "";
		return safe(charset).decode(buffer).toString();
	}

	// support

	private static StringBuilders.State appendEscape(StringBuilders.State b, int i, char c) {
		switch (c) {
			case NUL -> b.ensure(i).append(Escape.NUL);
			case BS -> b.ensure(i).append(Escape.BS);
			case TAB -> b.ensure(i).append(Escape.TAB);
			case NL -> b.ensure(i).append(Escape.NL);
			case FF -> b.ensure(i).append(Escape.FF);
			case CR -> b.ensure(i).append(Escape.CR);
			case ESC -> b.ensure(i).append(Escape.ESC);
			case BSLASH -> b.ensure(i).append(Escape.BSLASH);
			default -> {
				if (Chars.isPrintable(c)) b.append(i, c);
				else b.append(i, Escape.UTF16.apply(c));
			}
		}
		return b;
	}

	private static StringBuilders.State appendUnescape(StringBuilders.State b, int i,
		String escapedChar) {
		switch (escapedChar) {
			case Escape.BSLASH -> b.ensure(i).append(BSLASH);
			case Escape.BS -> b.ensure(i).append(BS);
			case Escape.ESC -> b.ensure(i).append(ESC);
			case Escape.FF -> b.ensure(i).append(FF);
			case Escape.NL -> b.ensure(i).append(NL);
			case Escape.CR -> b.ensure(i).append(CR);
			case Escape.TAB -> b.ensure(i).append(TAB);
			case Escape.NUL -> b.ensure(i).append(NUL);
			default -> b.append(i, unescape(escapedChar));
		}
		return b;
	}

	private static char unescape(String escapedChar) {
		int c = unescape(escapedChar, Escape.UTF16);
		if (c == -1) c = unescape(escapedChar, Escape.HEX);
		if (c == -1) c = unescape(escapedChar, Escape.OCT);
		Validate.min(c, 0, "Escaped char");
		return (char) c;
	}

	private static int unescape(String escapedChar, Format.OfLong format) {
		if (!escapedChar.startsWith(format.prefix())) return -1;
		return Integer.parseUnsignedInt(escapedChar, format.prefix().length(), escapedChar.length(),
			format.radix());
	}
}
