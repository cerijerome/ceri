package ceri.common.math;

import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import ceri.common.text.RegexUtil;

/**
 * Regular expressions for parsing numbers. Needs refactoring to handle non-english locales.
 */
public class NumberPatterns {
	public static final NumberPatterns DEFAULT = new NumberPatterns();
	private static final String decimal = "[.][0-9]+";
	private final Pattern separator;
	public final Parser integer;
	public final Parser integerStrictSeparator;
	public final Parser integerLooseSeparator;
	public final Parser number;
	public final Parser numberStrictSeparator;
	public final Parser numberLooseSeparator;

	public static class Parser {
		private final Pattern pattern;
		private final Function<String, Double> parser;

		Parser(Pattern pattern, Function<String, Double> parser) {
			this.pattern = pattern;
			this.parser = parser;
		}

		public Pattern pattern() {
			return pattern;
		}

		public String format() {
			return pattern.pattern();
		}

		public Double parse(String number) {
			return parser.apply(number);
		}
	}

	private NumberPatterns() {
		separator = Pattern.compile("(?<=[0-9])[,](?=[0-9])");
		integer = parser("[0-9]+");
		integerStrictSeparator = parser("[0-9]{1,3}(?:[,][0-9]{3})*");
		integerLooseSeparator = parser("[0-9]+(?:[,][0-9]+)*");
		number = floatParser(integer);
		numberStrictSeparator = floatParser(integerStrictSeparator);
		numberLooseSeparator = floatParser(integerLooseSeparator);
	}

	private Parser parser(String pattern) {
		Pattern p = Pattern.compile(pattern);
		return new Parser(p, s -> parseNumber(p, s));
	}

	private Parser floatParser(Parser parser) {
		Pattern p = floatingPoint(parser.pattern());
		return new Parser(p, s -> parseNumber(p, s));
	}

	/**
	 * Removes separators loosely from integral values.
	 */
	public String removeSeparators(String number) {
		if (number == null || number.isEmpty()) return number;
		return separator.matcher(number).replaceAll("");
	}

	private Double parseNumber(Pattern pattern, String number) {
		if (number == null) return null;
		Matcher m = pattern.matcher(number);
		if (!m.matches()) return null;
		return Double.parseDouble(removeSeparators(m.group()));
	}

	private static Pattern floatingPoint(Pattern integral) {
		return RegexUtil.compile("(?:%s(?:%s)?|%2$s)", integral.pattern(), decimal);
	}

}
