package ceri.common.util;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Word-based formatting utilities. Not intended for UI purposes; tailored for
 * ASCII in default Locale only.
 */
public class TextUtil {
	private static final Pattern CAPITALIZED_WORD_PATTERN = Pattern.compile("^[A-Z]$|^[A-Z][a-z]");
	private static final Pattern WORD_SPLIT_PATTERN = Pattern
		.compile("(?<![A-Z])(?=[A-Z0-9])|(?<=[A-Z0-9])(?=[A-Z][a-z])|[\\s_]+");
	private static final Pattern WORD_BOUNDARY_PATTERN = Pattern.compile("([^a-zA-Z])([a-z])");
	private static final Pattern CASE_BOUNDARY_PATTERN = Pattern.compile("([a-z0-9])([A-Z])");
	private static final Pattern UPPER_CASE_WORD_PATTERN = Pattern.compile("([A-Z])([A-Z0-9]*)");
	private static final Pattern UNDERSCORE_WORD_SEPARATOR_PATTERN = Pattern
		.compile("(?i)([a-z0-9])_([a-z])");
	private static final Pattern DOT_PATTERN = Pattern.compile("\\.");

	private TextUtil() {}

	/**
	 * Split a string into words, based on word boundary of non-letter followed
	 * by letter, underscores, or whitespace.
	 */
	public static List<String> toWords(String str) {
		List<String> words = new ArrayList<>();
		for (String word : WORD_SPLIT_PATTERN.split(str)) {
			word = word.trim();
			if (word.length() > 0) words.add(word);
		}
		return words;
	}

	/**
	 * Splits a string into words, based on word boundary of non-letter followed
	 * by letter, underscores, or whitespace. Then lower-cases each word,
	 * returning a single-space separated sequence of the words. Sequences of
	 * capital letters are preserved. Useful for converting method/class names
	 * to a phrase.
	 */
	public static String toPhrase(String str) {
		if (str.isEmpty()) return str;
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
	 * Splits a string into words, based on word boundary of non-letter followed
	 * by letter, underscores, or whitespace. Then upper-cases the first letter
	 * of each word, returning a single-space separated sequence of the words.
	 */
	public static String toCapitalizedPhrase(String str) {
		if (str.isEmpty()) return str;
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
	 * Changes camel case to Pascal case. e.g. _helloThereABC_ =>
	 * _HelloThereABC_
	 */
	public static String camelToPascal(String str) {
		if (str.isEmpty()) return str;
		Matcher m = WORD_BOUNDARY_PATTERN.matcher(str);
		StringBuilder sb = new StringBuilder();
		int last = 0;
		while (m.find()) {
			sb.append(str.substring(last, m.start()));
			sb.append(m.group(1));
			sb.append(m.group(2).toUpperCase());
			last = m.end();
		}
		sb.append(str.substring(last));
		sb.setCharAt(0, Character.toUpperCase(sb.charAt(0)));
		return sb.toString();
	}

	/**
	 * Make the first character upper case.
	 */
	public static String firstToUpper(String str) {
		if (str.isEmpty()) return str;
		char first = str.charAt(0);
		char upper = Character.toUpperCase(first);
		if (first == upper) return str;
		return upper + str.substring(1);
	}

	/**
	 * Make the first character lower case.
	 */
	public static String firstToLower(String str) {
		if (str.isEmpty()) return str;
		char first = str.charAt(0);
		char lower = Character.toLowerCase(first);
		if (first == lower) return str;
		return lower + str.substring(1);
	}

	/**
	 * Changes Pascal case to underscore-separated upper case. Sequential
	 * capital letters are not separated. e.g. _HelloThereABC_ =>
	 * _HELLO_THERE_ABC_
	 */
	public static String pascalToUpper(String str) {
		if (str.isEmpty()) return str;
		return CASE_BOUNDARY_PATTERN.matcher(str).replaceAll("$1_$2").toUpperCase();
	}

	/**
	 * Changes Pascal case to property name style. e.g. HelloThereABC =>
	 * hello.there.abc
	 */
	public static String pascalToProperty(String str) {
		return upperToProperty(pascalToUpper(str));
	}

	/**
	 * Changes upper case sequential chars to capitalized sequence. e.g.
	 * _HELLO_THERE_ABC_ => _Hello_There_Abc_
	 */
	public static String upperToCapitalized(String str) {
		if (str.isEmpty()) return str;
		Matcher m = UPPER_CASE_WORD_PATTERN.matcher(str);
		StringBuilder sb = new StringBuilder();
		int last = 0;
		while (m.find()) {
			sb.append(str.substring(last, m.start()));
			sb.append(m.group(1));
			sb.append(m.group(2).toLowerCase());
			last = m.end();
		}
		sb.append(str.substring(last));
		return sb.toString();
	}

	/**
	 * Changes underscore-separated upper case to Pascal case. Only single
	 * underscores surrounded by letters are removed. e.g. _HELLO_THERE_ABC_ =>
	 * _HelloThereAbc_
	 */
	public static String upperToPascal(String str) {
		if (str.isEmpty()) return str;
		return UNDERSCORE_WORD_SEPARATOR_PATTERN.matcher(upperToCapitalized(str))
			.replaceAll("$1$2");
	}

	/**
	 * Changes underscore-separated upper case to property name style. Only
	 * single underscores surrounded by letters are converted. e.g.
	 * HELLO_THERE_ABC => hello.there.abc
	 */
	public static String upperToProperty(String str) {
		if (str.isEmpty()) return str;
		return UNDERSCORE_WORD_SEPARATOR_PATTERN.matcher(str).replaceAll("$1.$2").toLowerCase();
	}

	/**
	 * Changes underscore-separated upper case to property name style, e.g.
	 * hello.there.abc => HELLO_THERE_ABC
	 */
	public static String propertyToUpper(String str) {
		if (str.isEmpty()) return str;
		return DOT_PATTERN.matcher(str.toUpperCase()).replaceAll("_");
	}

}
