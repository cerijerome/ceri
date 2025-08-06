package ceri.common.validation;

import java.util.Arrays;
import java.util.Collection;
import java.util.function.DoubleFunction;
import ceri.common.collection.Iterables;
import ceri.common.math.MathUtil;
import ceri.common.text.Joiner;

public enum DisplayDouble implements DoubleFunction<String> {
	std(String::valueOf), // standard view
	round(d -> String.valueOf(Math.round(d))), // rounded to long
	round1(d -> String.valueOf(MathUtil.round(1, d))), // 1 decimal place
	round2(d -> String.valueOf(MathUtil.round(2, d))), // 2 decimal places
	round3(d -> String.valueOf(MathUtil.round(3, d))); // 3 decimal places

	private final DoubleFunction<String> formatter;

	public static String format(double value, DisplayDouble... flags) {
		return format(value, Arrays.asList(flags));
	}

	public static String format(double value, Collection<DisplayDouble> flags) {
		if (flags.isEmpty()) return DisplayDouble.std.apply(value);
		if (flags.size() == 1) return Iterables.first(flags).apply(value);
		return Joiner.PARAM.join(f -> f.apply(value), flags);
	}

	private DisplayDouble(DoubleFunction<String> formatter) {
		this.formatter = formatter;
	}

	@Override
	public String apply(double value) {
		return formatter.apply(value);
	}
}
