package ceri.common.data.old;

import java.util.function.LongFunction;
import ceri.common.text.StringUtil;

@Deprecated
public enum UnsignedOctetType {
	_general(0, Long::toHexString),
	_long(8, StringUtil::toHex),
	_int(4, l -> StringUtil.toHex((int) l)),
	_short(2, l -> StringUtil.toHex((short) l)),
	_byte(1, l -> StringUtil.toHex((byte) l));

	public final int octets;
	private final LongFunction<String> formatter;

	UnsignedOctetType(int octets, LongFunction<String> formatter) {
		this.octets = octets;
		this.formatter = formatter;
	}

	public String format(long value) {
		return "0x" + formatter.apply(value);
	}

}