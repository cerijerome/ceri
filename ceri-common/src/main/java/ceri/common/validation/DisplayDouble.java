package ceri.common.validation;

import static ceri.common.collection.CollectionUtil.first;
import java.util.Arrays;
import java.util.Collection;
import java.util.function.DoubleFunction;
import ceri.common.math.MathUtil;
import ceri.common.text.StringUtil;

public enum DisplayDouble {
	std(String::valueOf), // standard view
	round(d -> String.valueOf(Math.round(d))), // rounded to long
	round1(d -> String.valueOf(MathUtil.round(d, 1))), // 1 decimal place
	round2(d -> String.valueOf(MathUtil.round(d, 2))), // 2 decimal places
	round3(d -> String.valueOf(MathUtil.round(d, 3))); // 3 decimal places

	private final DoubleFunction<String> formatter;

	public static String format(double value, DisplayDouble... flags) {
		return format(value, Arrays.asList(flags));
	}

	public static String format(double value, Collection<DisplayDouble> flags) {
		if (flags.isEmpty()) return DisplayDouble.std.format(value);
		if (flags.size() == 1) return first(flags).format(value);
		return StringUtil.join(", ", "(", ")", flag -> flag.format(value), flags);
	}

	private DisplayDouble(DoubleFunction<String> formatter) {
		this.formatter = formatter;
	}

	public String format(double value) {
		return formatter.apply(value);
	}

}
