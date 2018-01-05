package ceri.common.math;

import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Regular expressions for parsing numbers. Needs refactoring to handle non-english locales.
 */
public class NumberPatterns {
	public static final NumberPatterns DEFAULT = new NumberPatterns();
	private static final String decimal = "[.][0-9]+";
	private final Pattern separator;
	public final Parser integral;
	public final Parser integralStrictSeparator;
	public final Parser integralLooseSeparator;
	public final Parser floatingPoint;
	public final Parser floatingPointStrictSeparator;
	public final Parser floatingPointLooseSeparator;

	public static class Parser {
		public final Pattern pattern;
		private final Function<String, Double> parser;

		Parser(Pattern pattern, Function<String, Double> parser) {
			this.pattern = pattern;
			this.parser = parser;
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
		integral = parser("[0-9]+");
		integralStrictSeparator = parser("[0-9]{1,3}(?:[,][0-9]{3})*");
		integralLooseSeparator = parser("[0-9]+(?:[,][0-9]+)*");
		floatingPoint = floatParser(integral);
		floatingPointStrictSeparator = floatParser(integralStrictSeparator);
		floatingPointLooseSeparator = floatParser(integralLooseSeparator);
	}

	private Parser parser(String pattern) {
		Pattern p = Pattern.compile(pattern);
		return new Parser(p, s -> parseNumber(p, s));
	}
	
	private Parser floatParser(Parser parser) {
		Pattern p = floatingPoint(parser.pattern);
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
		return Pattern.compile(String.format("(?:%s(?:%s)?|%2$s)", integral.pattern(), decimal));
	}

}
