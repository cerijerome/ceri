package ceri.common.unit;

import java.util.EnumSet;
import java.util.regex.Matcher;
import ceri.common.util.MultiPattern;
import ceri.common.util.PrimitiveUtil;

public enum InchUnit implements Unit {
	inch("\"", 1),
	foot("'", 12),
	yard(foot.inches * 3),
	mile(yard.inches * 1760);

	private static final EnumSet<InchUnit> HEIGHT_UNITS = EnumSet.of(foot, inch);
	private static final MultiPattern HEIGHT_PATTERNS = MultiPattern.builder().pattern(
		"(\\d+)'(\\d+)\"?", "(\\d+)'", "(\\d+)\"", "(\\d+)").build();
	public final String shortName;
	public final long inches;

	private InchUnit(long inches) {
		this(null, inches);
	}
	private InchUnit(String shortName, long inches) {
		this.shortName = shortName == null ? name() : shortName;
		this.inches = inches;
	}

	@Override
	public long units() {
		return inches;
	}

	public static NormalizedValue<InchUnit> normalize(long inches) {
		return NormalizedValue.create(inches, InchUnit.class);
	}

	public static NormalizedValue<InchUnit> normalizeHeight(long inches) {
		return NormalizedValue.create(inches, HEIGHT_UNITS);
	}

	public static NormalizedValue<InchUnit> heightFromString(String s) {
		Matcher m = HEIGHT_PATTERNS.find(s);
		if (m == null) throw new IllegalArgumentException("Unable to parse string: " + s);
		NormalizedValue.Builder<InchUnit> builder = NormalizedValue.builder(HEIGHT_UNITS);
		long g1 = PrimitiveUtil.valueOf(m.group(1), 0);
		if (m.groupCount() > 1) {
			long g2 = PrimitiveUtil.valueOf(m.group(2), 0);
			return builder.value(g1, foot).value(g2).build();
		} else if (m.pattern().pattern().endsWith("'")) return builder.value(g1, foot).build();
		else return builder.value(g1).build();
	}

}
