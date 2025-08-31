package ceri.common.validation;

import java.util.Arrays;
import java.util.Collection;
import java.util.function.IntFunction;
import java.util.function.LongFunction;
import ceri.common.collection.Iterables;
import ceri.common.text.Format;
import ceri.common.text.Joiner;

// Use Format
@Deprecated
public enum DisplayLong implements LongFunction<String>, IntFunction<String> {
	dec(String::valueOf), // decimal
	udec(Long::toUnsignedString), // unsigned decimal
	bin(Format.BIN::apply), // binary, no leading 0s
	hex(Format.HEX::apply), // no leading 0s
	hex2(Format.HEX2::ubyte), // exactly 2 digits
	hex4(Format.HEX4::ushort), // exactly 4 digits
	hex8(Format.HEX8::uint), // exactly 8 digits
	hex16(Format.HEX8::apply); // exactly 16 digits

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
