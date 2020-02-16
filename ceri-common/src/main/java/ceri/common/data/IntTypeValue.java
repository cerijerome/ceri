package ceri.common.data;

import static ceri.common.text.StringUtil.BYTE_HEX_DIGITS;
import static ceri.common.text.StringUtil.INT_HEX_DIGITS;
import static ceri.common.text.StringUtil.SHORT_HEX_DIGITS;
import static ceri.common.util.BasicUtil.defaultValue;
import java.util.function.IntFunction;
import ceri.common.text.StringUtil;
import ceri.common.util.EqualsUtil;
import ceri.common.util.HashCoder;

/**
 * Used to hold values that may have an type value, such as an enum. If no type maps to the value,
 * then a name can be used, such as "unknown". A sub-value is also available for types that map to a
 * range of values.
 */
public class IntTypeValue<T> {
	private final T type;
	private final int value;
	private final Integer subValue;
	private final String name;
	private transient final IntFunction<String> valueFormatter;

	/**
	 * Formatting for the integer value on toString(). Useful to match up with type definition.
	 */
	public static enum Format implements IntFunction<String> {
		decimal(String::valueOf),
		hex(i -> "0x" + Integer.toHexString(i)),
		hexByte(hex(BYTE_HEX_DIGITS)),
		hexShort(hex(SHORT_HEX_DIGITS)),
		hexInt(hex(INT_HEX_DIGITS));

		private final IntFunction<String> formatter;

		public static IntFunction<String> hex(int digits) {
			return i -> "0x" + StringUtil.toHex(i, digits);
		}

		private Format(IntFunction<String> formatter) {
			this.formatter = formatter;
		}

		@Override
		public String apply(int value) {
			return formatter.apply(value);
		}
	}

	public static <T> IntTypeValue<T> of(int value, T type, String name) {
		return of(value, type, name, null, Format.decimal);
	}

	public static <T> IntTypeValue<T> of(int value, T type, String name, int subValue) {
		return of(value, type, name, subValue, Format.decimal);
	}

	public static <T> IntTypeValue<T> ofByte(int value, T type, String name) {
		return of((byte) value, type, name, null, Format.hexByte);
	}

	public static <T> IntTypeValue<T> ofByte(int value, T type, String name, int subValue) {
		return of((byte) value, type, name, subValue, Format.hexByte);
	}

	public static <T> IntTypeValue<T> ofShort(int value, T type, String name) {
		return of((short) value, type, name, null, Format.hexShort);
	}

	public static <T> IntTypeValue<T> ofShort(int value, T type, String name, int subValue) {
		return of((short) value, type, name, subValue, Format.hexShort);
	}

	public static <T> IntTypeValue<T> ofInt(int value, T type, String name) {
		return of(value, type, name, null, Format.hexInt);
	}

	public static <T> IntTypeValue<T> ofInt(int value, T type, String name, int subValue) {
		return of(value, type, name, subValue, Format.hexInt);
	}

	public static <T> IntTypeValue<T> of(int value, T type, String name, Integer subValue,
		IntFunction<String> valueFormatter) {
		return new IntTypeValue<>(value, type, name, subValue, valueFormatter);
	}

	protected IntTypeValue(int value, T type, String name, Integer subValue,
		IntFunction<String> valueFormatter) {
		this.type = type;
		this.value = value;
		this.subValue = subValue;
		this.name = type != null ? null : name; // ignore if has type
		this.valueFormatter = defaultValue(valueFormatter, Format.decimal);
	}

	public T type() {
		return type;
	}

	public boolean hasType() {
		return type != null;
	}

	public String name() {
		if (type != null) return type.toString();
		return name;
	}

	public int value() {
		return value;
	}

	public short shortValue() {
		return (short) value;
	}

	public byte byteValue() {
		return (byte) value;
	}

	public int subValue() {
		return subValue == null ? 0 : subValue;
	}

	public boolean hasSubValue() {
		return subValue != null;
	}

	@Override
	public int hashCode() {
		return HashCoder.hash(type, name, value, subValue);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof IntTypeValue)) return false;
		IntTypeValue<?> other = (IntTypeValue<?>) obj;
		if (value != other.value) return false;
		if (!EqualsUtil.equals(type, other.type)) return false;
		if (!EqualsUtil.equals(subValue, other.subValue)) return false;
		if (!EqualsUtil.equals(name, other.name)) return false;
		return true;
	}

	public String compact() {
		if (!hasType()) return toString();
		if (!hasSubValue()) return type().toString();
		return String.format("%s(%s)", type, valueFormatter.apply(subValue));
	}

	@Override
	public String toString() {
		if (subValue == null) return String.format("%s(%s)", name(), valueFormatter.apply(value));
		return String.format("%s(%s:%s)", name(), valueFormatter.apply(value),
			valueFormatter.apply(subValue));
	}

}
