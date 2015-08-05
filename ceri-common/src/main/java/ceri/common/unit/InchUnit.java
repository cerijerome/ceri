package ceri.common.unit;

import java.util.EnumSet;
import java.util.Set;
import java.util.regex.Matcher;
import ceri.common.collection.ImmutableUtil;
import ceri.common.text.StringUtil;
import ceri.common.util.MultiPattern;
import ceri.common.util.PrimitiveUtil;

/**
 * Example of Unit covering distance/height in inches. Only handles values in integral number of
 * inches.
 */
public enum InchUnit implements Unit {
	inch(1, "\""),
	foot(12, "'", "ft"),
	yard(foot.inches * 3, "yds", "yd"),
	mile(yard.inches * 1760);

	private static final EnumSet<InchUnit> HEIGHT_UNITS = EnumSet.of(foot, inch);
	private static final MultiPattern HEIGHT_PATTERNS = MultiPattern.builder().pattern(
		"(\\d+)" + foot.regex + "(\\d+)\"?", "(\\d+)" + foot.regex, "(\\d+)\"", "(\\d+)").build();
	public final Set<String> aliases;
	public final long inches;
	private final String regex;

	private InchUnit(long inches, String... aliases) {
		this.aliases = ImmutableUtil.asSet(aliases);
		this.inches = inches;
		regex = StringUtil.toString("(?:\\Q", "\\E)", "\\E|\\Q", aliases);
	}

	@Override
	public long units() {
		return inches;
	}

	/**
	 * Normalizes inches into miles, yards, feet and inches.
	 */
	public static NormalizedValue<InchUnit> normalize(long inches) {
		return NormalizedValue.create(inches, InchUnit.class);
	}

	/**
	 * Normalizes inches into feet and inches.
	 */
	public static NormalizedValue<InchUnit> normalizeHeight(long inches) {
		return NormalizedValue.create(inches, HEIGHT_UNITS);
	}

	/**
	 * Converts height string into a normalized value. Allowed formats are of the form: 10ft,
	 * 10ft11", 10ft11, 10', 10'11", 10'11, 11", 11
	 */
	public static NormalizedValue<InchUnit> heightFromString(String s) {
		Matcher m = HEIGHT_PATTERNS.find(s);
		if (m == null) throw new IllegalArgumentException("Unable to parse string: " + s);
		NormalizedValue.Builder<InchUnit> builder = NormalizedValue.builder(HEIGHT_UNITS);
		long g1 = PrimitiveUtil.valueOf(m.group(1), 0);
		if (m.groupCount() > 1) {
			long g2 = PrimitiveUtil.valueOf(m.group(2), 0);
			return builder.value(g1, foot).value(g2).build();
		} else if (m.pattern().pattern().endsWith(foot.regex)) return builder.value(g1, foot)
			.build();
		else return builder.value(g1).build();
	}

}
