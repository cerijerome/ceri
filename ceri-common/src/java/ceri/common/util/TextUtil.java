package ceri.common.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import ceri.common.collection.ArrayUtil;

/**
 * Word-based formatting utilities. Not intended for UI purposes; tailored for
 * ASCII in default Locale only.
 */
public class TextUtil {
	private static final Pattern UPPER_PATTERN = Pattern.compile("^[^a-z]*$");
	private static final Pattern WORD_SPLIT_PATTERN = Pattern.compile("(?<![A-Z])(?=[A-Z])");
	private static final Pattern WORD_BOUNDARY_PATTERN = Pattern.compile("([^a-zA-Z])([a-z])");
	private static final Pattern CASE_BOUNDARY_PATTERN = Pattern.compile("([a-z0-9])([A-Z])");
	private static final Pattern UPPER_CASE_WORD_PATTERN = Pattern.compile("([A-Z])([A-Z0-9]*)");
	private static final Pattern UNDERSCORE_WORD_SEPARATOR_PATTERN = Pattern
		.compile("(?i)([a-z0-9])_([a-z])");
	private static final Pattern DOT_PATTERN = Pattern.compile("\\.");

	private TextUtil() {}

	/**
	 * Split a string into words based on word boundary of non-letter followed by letter.
	 */
	public static String[] toWords(String str) {
		if (str.isEmpty()) return ArrayUtil.EMPTY_STRING;
		String[] words = WORD_SPLIT_PATTERN.split(str);
		return words;
	}

	/**
	 * Changes camel/Pascal case to words e.g. helloThereABC => hello there ABC.
	 * Useful for converting method/class names to a phrase. 
	 */
	public static String toPhrase(String str) {
		String[] words = toWords(str);
		StringBuilder b = new StringBuilder();
		boolean first = true;
		for (String word : words) {
			if (!UPPER_PATTERN.matcher(word).find()) word = firstToLower(word);
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
		return Character.toUpperCase(str.charAt(0)) + str.substring(1);
	}
	
	/**
	 * Make the first character lower case. 
	 */
	public static String firstToLower(String str) {
		if (str.isEmpty()) return str;
		return Character.toLowerCase(str.charAt(0)) + str.substring(1);
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
