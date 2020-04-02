package ceri.common.validation;

import static ceri.common.collection.CollectionUtil.first;
import java.util.Arrays;
import java.util.Collection;
import java.util.function.LongFunction;
import ceri.common.text.StringUtil;

public enum DisplayLong {
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
		if (flags.isEmpty()) return DisplayLong.dec.format(value);
		if (flags.size() == 1) return first(flags).format(value);
		return StringUtil.join(", ", "(", ")", flag -> flag.format(value), flags);
	}

	private DisplayLong(LongFunction<String> formatter) {
		this.formatter = formatter;
	}

	public String format(long value) {
		return formatter.apply(value);
	}

}
