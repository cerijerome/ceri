package ceri.common.validation;

import java.util.Arrays;
import java.util.Collection;
import java.util.function.IntFunction;
import java.util.function.LongFunction;
import ceri.common.collection.Iterables;
import ceri.common.text.Joiner;
import ceri.common.text.StringUtil;

public enum DisplayLong implements LongFunction<String>, IntFunction<String> {
	dec(String::valueOf), // decimal
	udec(Long::toUnsignedString), // unsigned decimal
	bin(l -> "0b" + Long.toBinaryString(l)), // binary, no leading 0s
	hex(l -> "0x" + Long.toHexString(l)), // no leading 0s
	hex2(l -> "0x" + StringUtil.toHex(l, 2)), // exactly 2 digits
	hex4(l -> "0x" + StringUtil.toHex(l, 4)), // exactly 4 digits
	hex8(l -> "0x" + StringUtil.toHex(l, 8)), // exactly 8 digits
	hex16(l -> "0x" + StringUtil.toHex(l, 16)); // exactly 16 digits

	private final LongFunction<String> formatter;

	public static String format(long value, DisplayLong... flags) {
		return format(value, Arrays.asList(flags));
	}

	public static String format(long value, Collection<DisplayLong> flags) {
		if (flags.isEmpty()) return DisplayLong.dec.apply(value);
		if (flags.size() == 1) return Iterables.first(flags).apply(value);
		return Joiner.PARAM.join(f -> f.apply(value), flags);
	}

	private DisplayLong(LongFunction<String> formatter) {
		this.formatter = formatter;
	}

	@Override
	public String apply(long value) {
		return formatter.apply(value);
	}

	@Override
	public String apply(int value) {
		return apply((long) value);
	}
}
