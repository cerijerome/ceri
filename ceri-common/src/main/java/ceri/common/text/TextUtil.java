package ceri.common.text;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import ceri.common.collection.Collectable;
import ceri.common.math.MathUtil;

/**
 * Multi-line and word-based formatting utilities.
 */
public class TextUtil {
	private static final Pattern CAPITALIZED_WORD_PATTERN = Pattern.compile("^[A-Z]$|^[A-Z][a-z]");
	private static final Pattern WORD_SPLIT_PATTERN =
		Pattern.compile("(?<![A-Z])(?=[A-Z0-9])|(?<=[A-Z0-9])(?=[A-Z][a-z])|[\\s_]+");
	private static final Pattern WORD_BOUNDARY_PATTERN = Pattern.compile("([^a-zA-Z])([a-z])");
	private static final Pattern CASE_BOUNDARY_PATTERN = Pattern.compile("([a-z0-9])([A-Z])");
	private static final Pattern UPPER_CASE_WORD_PATTERN = Pattern.compile("([A-Z])([A-Z0-9]*)");
	private static final Pattern LETTER_PATTERN = Pattern.compile("(?i)([A-Z])");
	private static final Pattern UNDERSCORE_WORD_SEPARATOR_PATTERN =
		Pattern.compile("(?i)([a-z0-9])_([a-z])");
	private static final Pattern DOT_PATTERN = Pattern.compile("\\.");

	private TextUtil() {}

	public static void main(String[] args) {
		var s = "abc\ndef\r\nghi\rjkl";
		System.out.println(prefixLines("> ", s));
	}

	/**
	 * Prefixes each line of the text with given prefix.
	 */
	public static String prefixLines(CharSequence prefix, CharSequence text) {
		if (Strings.isEmpty(text)) return "";
		if (Strings.isEmpty(prefix)) return text.toString();
		return Patterns.findAllAppend(new StringBuilder(prefix), Patterns.Split.LINE.pattern, text,
			(b, m) -> b.append(m.group()).append(prefix)).toString();
	}

	/**
	 * Replaces spaces with tabs for each line, keeping column alignment.
	 */
	public static String spacesToTabs(int tabSize, String text) {
		return spacesToTabs(tabSize, Patterns.Split.LINE.list(text));
	}

	/**
	 * Replaces spaces with tabs for each line, keeping column alignment, and joining the lines.
	 */
	public static String spacesToTabs(int tabSize, Iterable<String> lines) {
		return Joiner.EOL.join(line -> lineSpacesToTabs(tabSize, line), lines);
	}

	/**
	 * Replace spaces with tabs for a line, keeping column alignment.
	 */
	public static String lineSpacesToTabs(int tabSize, String line) {
		if (Strings.isEmpty(line)) return "";
		if (tabSize <= 0) return line;
		StringBuilder b = null;
		int spaces = 0;
		int start = 0;
		for (int i = 0;; i++) {
			if (i - start == tabSize) {
				if (spaces > 1) b = b(b, line, i - spaces).append(Chars.TAB);
				else if (b != null) StringBuilders.repeat(b, ' ', spaces);
				spaces = 0;
				start = i;
			}
			if (i >= line.length()) break;
			char c = line.charAt(i);
			if (c == Chars.TAB) {
				if (spaces > 0 || b != null) b = b(b, line, i - spaces).append(Chars.TAB);
				start = i + 1;
			} else if (c != ' ' && b != null) StringBuilders.repeat(b, ' ', spaces).append(c);
			if (c == ' ') spaces++;
			else spaces = 0;
		}
		if (b == null) return line;
		return StringBuilders.repeat(b, ' ', spaces).toString();
	}

	/**
	 * Replaces tabs with spaces for each line, keeping column alignment.
	 */
	public static String tabsToSpaces(int tabSize, String text) {
		return tabsToSpaces(tabSize, Patterns.Split.LINE.list(text));
	}

	/**
	 * Replaces tabs with spaces for each line, keeping column alignment, and joining the lines.
	 */
	public static String tabsToSpaces(int tabSize, Iterable<String> lines) {
		return Joiner.EOL.join(line -> lineTabsToSpaces(tabSize, line), lines);
	}

	/**
	 * Replaces tabs with spaces for a line, keeping column alignment.
	 */
	public static String lineTabsToSpaces(int tabSize, String line) {
		if (Strings.isEmpty(line)) return "";
		if (tabSize <= 0) return line;
		int start = 0;
		StringBuilder b = null;
		while (true) {
			int pos = line.indexOf(Chars.TAB, start);
			if (pos < 0) break;
			b = b(b, line, start);
			StringBuilders.append(b, line, start, pos - start);
			StringBuilders.repeat(b, ' ', tabSpaces(b.length(), tabSize));
			start = pos + 1;
		}
		if (b == null) return line;
		return StringBuilders.append(b, line, start).toString();
	}

	/**
	 * Adds a line number to the start of each line.
	 */
	public static String addLineNumbers(String text) {
		return addLineNumbers(Patterns.Split.LINE.list(text));
	}

	/**
	 * Adds a line number to the start of each line, and joins the lines.
	 */
	public static String addLineNumbers(String[] lines) {
		return addLineNumbers(Arrays.asList(lines));
	}

	/**
	 * Adds a line number to the start of each line, and joins the lines.
	 */
	public static String addLineNumbers(List<String> lines) {
		if (Collectable.isEmpty(lines)) return "";
		var fmt = "%" + MathUtil.decimalDigits(lines.size() + 1) + "d";
		var b = new StringBuilder();
		for (int i = 0; i < lines.size(); i++) {
			if (i > 0) b.append(Strings.EOL);
			b.append(String.format(fmt, i + 1)).append(": ").append(lines.get(i));
		}
		return b.toString();
	}

	/**
	 * Wraps a multi-line string as a javadoc block.
	 */
	public static String multilineJavadoc(String s) {
		return multilineComment("/**", " * ", " */", s);
	}

	/**
	 * Wraps a multi-line string as a comment block.
	 */
	public static String multilineComment(String s) {
		return multilineComment("/*", " * ", " */", s);
	}

	/**
	 * Splits a string into words, based on word boundary of non-letter followed by letter,
	 * underscores, or whitespace.
	 */
	public static List<String> toWords(String str) {
		if (str == null) return List.of();
		List<String> words = new ArrayList<>();
		for (String word : WORD_SPLIT_PATTERN.split(str)) {
			word = word.trim();
			if (word.length() > 0) words.add(word);
		}
		return words;
	}

	/**
	 * Splits a string into words, based on word boundary of non-letter followed by letter,
	 * underscores, or whitespace. Then lower-cases each word, returning a single-space separated
	 * sequence of the words. Sequences of capital letters are preserved. Useful for converting
	 * method/class names to a phrase.
	 */
	public static String toPhrase(String str) {
		if (str == null || str.isEmpty()) return str;
		StringBuilder b = new StringBuilder();
		boolean first = true;
		for (String word : toWords(str)) {
			if (CAPITALIZED_WORD_PATTERN.matcher(word).find()) word = firstToLower(word);
			if (!first) b.append(' ');
			b.append(word);
			first = false;
		}
		return b.toString();
	}

	/**
	 * Splits a string into words, based on word boundary of non-letter followed by letter,
	 * underscores, or whitespace. Then upper-cases the first letter of each word, returning a
	 * single-space separated sequence of the words.
	 */
	public static String toCapitalizedPhrase(String str) {
		if (str == null || str.isEmpty()) return str;
		StringBuilder b = new StringBuilder();
		boolean first = true;
		for (String word : toWords(str)) {
			word = firstToUpper(word);
			if (!first) b.append(' ');
			b.append(word);
			first = false;
		}
		return b.toString();
	}

	/**
	 * Changes camel case to lower-case hyphenated e.g. _helloThereABC_ => _hello-there-abc_
	 */
	public static String camelToHyphenated(String str) {
		if (str == null || str.isEmpty()) return str;
		return CASE_BOUNDARY_PATTERN.matcher(str).replaceAll("$1-$2").toLowerCase();
	}

	/**
	 * Changes camel case to Pascal case. e.g. _helloThereABC_ => _HelloThereABC_
	 */
	public static String camelToPascal(String str) {
		if (str == null || str.isEmpty()) return str;
		Matcher m = WORD_BOUNDARY_PATTERN.matcher(str);
		StringBuilder sb = new StringBuilder();
		int last = 0;
		while (m.find()) {
			sb.append(str, last, m.start());
			sb.append(m.group(1));
			sb.append(m.group(2).toUpperCase());
			last = m.end();
		}
		sb.append(str.substring(last));
		sb.setCharAt(0, Character.toUpperCase(sb.charAt(0)));
		return sb.toString();
	}

	/**
	 * Makes the first character upper case.
	 */
	public static String firstToUpper(String str) {
		if (str == null || str.isEmpty()) return str;
		char first = str.charAt(0);
		char upper = Character.toUpperCase(first);
		if (first == upper) return str;
		return upper + str.substring(1);
	}

	/**
	 * Makes the first character lower case.
	 */
	public static String firstToLower(String str) {
		if (str == null || str.isEmpty()) return str;
		char first = str.charAt(0);
		char lower = Character.toLowerCase(first);
		if (first == lower) return str;
		return lower + str.substring(1);
	}

	/**
	 * Makes the first found letter upper case.
	 */
	public static String firstLetterToUpper(String str) {
		if (str == null || str.isEmpty()) return str;
		return LETTER_PATTERN.matcher(str).replaceFirst(m -> m.group(0).toUpperCase());
	}

	/**
	 * Makes the first found letter lower case.
	 */
	public static String firstLetterToLower(String str) {
		if (str == null || str.isEmpty()) return str;
		return LETTER_PATTERN.matcher(str).replaceFirst(m -> m.group(0).toLowerCase());
	}

	/**
	 * Changes Pascal case to underscore-separated upper case. Sequential capital letters are not
	 * separated. e.g. _HelloThereABC_ => _HELLO_THERE_ABC_
	 */
	public static String pascalToUnderscore(String str) {
		if (str == null || str.isEmpty()) return str;
		return CASE_BOUNDARY_PATTERN.matcher(str).replaceAll("$1_$2").toUpperCase();
	}

	/**
	 * Changes Pascal case to property name style. e.g. HelloThereABC => hello.there.abc
	 */
	public static String pascalToProperty(String str) {
		return underscoreToProperty(pascalToUnderscore(str));
	}

	/**
	 * Changes upper case sequential chars to capitalized sequence. e.g. _HELLO_THERE_ABC_ =>
	 * _Hello_There_Abc_
	 */
	public static String upperToCapitalized(String str) {
		if (str == null || str.isEmpty()) return str;
		Matcher m = UPPER_CASE_WORD_PATTERN.matcher(str);
		StringBuilder sb = new StringBuilder();
		int last = 0;
		while (m.find()) {
			sb.append(str, last, m.start());
			sb.append(m.group(1));
			sb.append(m.group(2).toLowerCase());
			last = m.end();
		}
		sb.append(str.substring(last));
		return sb.toString();
	}

	/**
	 * Changes underscore-separated upper case to Pascal case. Only single underscores surrounded by
	 * letters are removed. e.g. _HELLO_THERE_ABC_ => _HelloThereAbc_
	 */
	public static String underscoreToPascal(String str) {
		if (str == null || str.isEmpty()) return str;
		return UNDERSCORE_WORD_SEPARATOR_PATTERN.matcher(upperToCapitalized(str))
			.replaceAll("$1$2");
	}

	/**
	 * Changes underscore-separated upper case to camel case. Only single underscores surrounded by
	 * letters are removed. e.g. _HELLO_THERE_ABC_ => _helloThereAbc_
	 */
	public static String underscoreToCamel(String str) {
		if (str == null || str.isEmpty()) return str;
		return firstLetterToLower(underscoreToPascal(str));
	}

	/**
	 * Changes underscore-separated upper case to property name style. Only single underscores
	 * surrounded by letters are converted. e.g. HELLO_THERE_ABC => hello.there.abc
	 */
	public static String underscoreToProperty(String str) {
		if (str == null || str.isEmpty()) return str;
		return UNDERSCORE_WORD_SEPARATOR_PATTERN.matcher(str).replaceAll("$1.$2").toLowerCase();
	}

	/**
	 * Changes underscore-separated upper case to property name style, e.g. hello.there.abc =>
	 * HELLO_THERE_ABC
	 */
	public static String propertyToUnderscore(String str) {
		if (str == null || str.isEmpty()) return str;
		return DOT_PATTERN.matcher(str.toUpperCase()).replaceAll("_");
	}

	// support

	private static StringBuilder b(StringBuilder b, CharSequence s, int i) {
		if (b != null) return b;
		return new StringBuilder().append(s, 0, i);
	}

	private static int tabSpaces(int pos, int tabSize) {
		return ((pos + tabSize) / tabSize) * tabSize - pos;
	}

	private static String multilineComment(String start, String prefix, String end, String str) {
		if (Strings.isEmpty(str)) return str;
		return start + Strings.EOL + prefixLines(prefix, str) + Strings.EOL + end;
	}
}
