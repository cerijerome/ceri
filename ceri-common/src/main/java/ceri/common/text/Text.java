package ceri.common.text;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import ceri.common.array.ArrayUtil;
import ceri.common.collect.Collectable;
import ceri.common.collect.Immutable;
import ceri.common.collect.Lists;
import ceri.common.math.Maths;

/**
 * Multi-line and word-based formatting utilities.
 */
public class Text {
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

	private Text() {}

	/**
	 * Prefixes each line of the text with given prefix.
	 */
	public static String prefixLines(CharSequence prefix, CharSequence text) {
		if (Strings.isEmpty(text)) return "";
		if (Strings.isEmpty(prefix)) return text.toString();
		return Regex.appendAll(new StringBuilder(prefix), Regex.EOL, text,
			(b, m) -> b.append(m.group()).append(prefix)).toString();
	}

	/**
	 * Replaces spaces with tabs for each line, keeping column alignment.
	 */
	public static String spacesToTabs(int tabSize, String text) {
		return spacesToTabs(tabSize, Regex.Split.LINE.list(text));
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
		return tabsToSpaces(tabSize, Regex.Split.LINE.list(text));
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
		return addLineNumbers(Regex.Split.LINE.list(text));
	}

	/**
	 * Adds a line number to the start of each line, and joins the lines.
	 */
	public static String addLineNumbers(String[] lines) {
		if (ArrayUtil.isEmpty(lines)) return "";
		return addLineNumbers(Arrays.asList(lines));
	}

	/**
	 * Adds a line number to the start of each line, and joins the lines.
	 */
	public static String addLineNumbers(List<String> lines) {
		return addLineNumbers(lines, 1);
	}

	/**
	 * Adds a line number to the start of each line, and joins the lines.
	 */
	public static String addLineNumbers(List<String> lines, int startLine) {
		if (Collectable.isEmpty(lines)) return "";
		var fmt = "%" + Maths.decimalDigits(lines.size() + startLine) + "d";
		var b = new StringBuilder();
		for (int i = 0; i < lines.size(); i++) {
			if (i > 0) b.append(Strings.EOL);
			var line = Chars.safe(lines.get(i));
			b.append(String.format(fmt, i + startLine)).append(": ").append(line);
		}
		return b.toString();
	}

	/**
	 * Wraps a multi-line string as a javadoc block.
	 */
	public static String multilineJavadoc(CharSequence s) {
		return multilineComment("/**", " * ", " */", s);
	}

	/**
	 * Wraps a multi-line string as a comment block.
	 */
	public static String multilineComment(CharSequence s) {
		return multilineComment("/*", " * ", " */", s);
	}

	/**
	 * Splits a string into words, based on word boundary of non-letter followed by letter,
	 * underscores, or whitespace.
	 */
	public static List<String> toWords(CharSequence s) {
		if (Strings.isEmpty(s)) return Immutable.list();
		var words = Lists.<String>of();
		for (String word : WORD_SPLIT_PATTERN.split(s)) {
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
	public static String toPhrase(CharSequence s) {
		if (Strings.isEmpty(s)) return "";
		var b = new StringBuilder();
		boolean first = true;
		for (String word : toWords(s)) {
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
	public static String toCapitalizedPhrase(CharSequence s) {
		if (Strings.isEmpty(s)) return "";
		var b = new StringBuilder();
		boolean first = true;
		for (String word : toWords(s)) {
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
	public static String camelToHyphenated(CharSequence s) {
		if (Strings.isEmpty(s)) return "";
		return CASE_BOUNDARY_PATTERN.matcher(s).replaceAll("$1-$2").toLowerCase();
	}

	/**
	 * Changes camel case to Pascal case. e.g. _helloThereABC_ => _HelloThereABC_
	 */
	public static String camelToPascal(CharSequence s) {
		if (Strings.isEmpty(s)) return "";
		var m = WORD_BOUNDARY_PATTERN.matcher(s);
		var sb = new StringBuilder();
		int last = 0;
		while (m.find()) {
			sb.append(s, last, m.start()).append(m.group(1)).append(m.group(2).toUpperCase());
			last = m.end();
		}
		sb.append(s, last, s.length()).setCharAt(0, Character.toUpperCase(sb.charAt(0)));
		return sb.toString();
	}

	/**
	 * Makes the first character upper case.
	 */
	public static String firstToUpper(CharSequence s) {
		if (Strings.isEmpty(s)) return "";
		char first = s.charAt(0);
		char upper = Character.toUpperCase(first);
		if (first == upper) return s.toString();
		return new StringBuilder(s.length()).append(upper).append(s, 1, s.length()).toString();
	}

	/**
	 * Makes the first character lower case.
	 */
	public static String firstToLower(CharSequence s) {
		if (Strings.isEmpty(s)) return "";
		char first = s.charAt(0);
		char lower = Character.toLowerCase(first);
		if (first == lower) return s.toString();
		return new StringBuilder(s.length()).append(lower).append(s, 1, s.length()).toString();
	}

	/**
	 * Makes the first found letter upper case.
	 */
	public static String firstLetterToUpper(CharSequence s) {
		if (Strings.isEmpty(s)) return "";
		return LETTER_PATTERN.matcher(s).replaceFirst(m -> m.group(0).toUpperCase());
	}

	/**
	 * Makes the first found letter lower case.
	 */
	public static String firstLetterToLower(CharSequence s) {
		if (Strings.isEmpty(s)) return "";
		return LETTER_PATTERN.matcher(s).replaceFirst(m -> m.group(0).toLowerCase());
	}

	/**
	 * Changes Pascal case to underscore-separated upper case. Sequential capital letters are not
	 * separated. e.g. _HelloThereABC_ => _HELLO_THERE_ABC_
	 */
	public static String pascalToUnderscore(CharSequence s) {
		if (Strings.isEmpty(s)) return "";
		return CASE_BOUNDARY_PATTERN.matcher(s).replaceAll("$1_$2").toUpperCase();
	}

	/**
	 * Changes Pascal case to property name style. e.g. HelloThereABC => hello.there.abc
	 */
	public static String pascalToProperty(CharSequence s) {
		return underscoreToProperty(pascalToUnderscore(s));
	}

	/**
	 * Changes upper case sequential chars to capitalized sequence. e.g. _HELLO_THERE_ABC_ =>
	 * _Hello_There_Abc_
	 */
	public static String upperToCapitalized(CharSequence s) {
		if (Strings.isEmpty(s)) return "";
		var m = UPPER_CASE_WORD_PATTERN.matcher(s);
		var sb = new StringBuilder();
		int last = 0;
		while (m.find()) {
			sb.append(s, last, m.start()).append(m.group(1)).append(m.group(2).toLowerCase());
			last = m.end();
		}
		return sb.append(s.subSequence(last, s.length())).toString();
	}

	/**
	 * Changes underscore-separated upper case to Pascal case. Only single underscores surrounded by
	 * letters are removed. e.g. _HELLO_THERE_ABC_ => _HelloThereAbc_
	 */
	public static String underscoreToPascal(CharSequence s) {
		if (Strings.isEmpty(s)) return "";
		return UNDERSCORE_WORD_SEPARATOR_PATTERN.matcher(upperToCapitalized(s)).replaceAll("$1$2");
	}

	/**
	 * Changes underscore-separated upper case to camel case. Only single underscores surrounded by
	 * letters are removed. e.g. _HELLO_THERE_ABC_ => _helloThereAbc_
	 */
	public static String underscoreToCamel(CharSequence s) {
		if (Strings.isEmpty(s)) return "";
		return firstLetterToLower(underscoreToPascal(s));
	}

	/**
	 * Changes underscore-separated upper case to property name style. Only single underscores
	 * surrounded by letters are converted. e.g. HELLO_THERE_ABC => hello.there.abc
	 */
	public static String underscoreToProperty(CharSequence s) {
		if (Strings.isEmpty(s)) return "";
		return UNDERSCORE_WORD_SEPARATOR_PATTERN.matcher(s).replaceAll("$1.$2").toLowerCase();
	}

	/**
	 * Changes underscore-separated upper case to property name style, e.g. hello.there.abc =>
	 * HELLO_THERE_ABC
	 */
	public static String propertyToUnderscore(String s) {
		if (Strings.isEmpty(s)) return "";
		return DOT_PATTERN.matcher(s.toUpperCase()).replaceAll("_");
	}

	// support

	private static StringBuilder b(StringBuilder b, CharSequence s, int i) {
		if (b != null) return b;
		return new StringBuilder().append(s, 0, i);
	}

	private static int tabSpaces(int pos, int tabSize) {
		return ((pos + tabSize) / tabSize) * tabSize - pos;
	}

	private static String multilineComment(CharSequence start, CharSequence prefix,
		CharSequence end, CharSequence s) {
		if (Strings.isEmpty(s)) return "";
		return start + Strings.EOL + prefixLines(prefix, s) + Strings.EOL + end;
	}
}
