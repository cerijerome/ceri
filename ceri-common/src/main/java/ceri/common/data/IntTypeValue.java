package ceri.common.data;

import static ceri.common.math.MathUtil.ubyte;
import static ceri.common.math.MathUtil.ushort;
import static ceri.common.util.BasicUtil.defaultValue;
import static ceri.common.validation.DisplayLong.dec;
import static ceri.common.validation.DisplayLong.hex;
import static ceri.common.validation.DisplayLong.hex2;
import static ceri.common.validation.DisplayLong.hex4;
import static ceri.common.validation.ValidationUtil.validateNotEqualObj;
import static ceri.common.validation.ValidationUtil.validateNotNull;
import java.util.Objects;
import java.util.function.IntFunction;

/**
 * Used to hold values that may have a type value, such as an enum. If no type maps to the value,
 * then a name can be used, such as "unknown". A sub-value is also available for types that map to a
 * range of values.
 */
public class IntTypeValue<T> {
	private final T type;
	private final int value;
	private final Integer subValue;
	private final String name;
	private transient final IntFunction<String> formatter;

	/**
	 * Create decimal type.
	 */
	public static <T> IntTypeValue<T> of(int value, T type, String name) {
		return of(value, type, name, null, dec::format);
	}

	/**
	 * Create decimal type with sub-value.
	 */
	public static <T> IntTypeValue<T> of(int value, T type, String name, int subValue) {
		return of(value, type, name, subValue, dec::format);
	}

	/**
	 * Create unsigned byte type.
	 */
	public static <T> IntTypeValue<T> ofUbyte(int value, T type, String name) {
		return of(ubyte(value), type, name, null, hex2::format);
	}

	/**
	 * Create unsigned byte type with sub-value.
	 */
	public static <T> IntTypeValue<T> ofUbyte(int value, T type, String name, int subValue) {
		return of(ubyte(value), type, name, subValue, hex2::format);
	}

	/**
	 * Create unsigned short type.
	 */
	public static <T> IntTypeValue<T> ofUshort(int value, T type, String name) {
		return of(ushort(value), type, name, null, hex4::format);
	}

	/**
	 * Create unsigned short type with sub-value.
	 */
	public static <T> IntTypeValue<T> ofUshort(int value, T type, String name, int subValue) {
		return of(ushort(value), type, name, subValue, hex4::format);
	}

	/**
	 * Create unsigned int type.
	 */
	public static <T> IntTypeValue<T> ofUint(int value, T type, String name) {
		return of(value, type, name, null, hex::format);
	}

	/**
	 * Create unsigned int type with sub-value.
	 */
	public static <T> IntTypeValue<T> ofUint(int value, T type, String name, int subValue) {
		return of(value, type, name, subValue, hex::format);
	}

	/**
	 * Create type with optional sub-value and custom formatter.
	 */
	public static <T> IntTypeValue<T> of(int value, T type, String name, Integer subValue,
		IntFunction<String> formatter) {
		return new IntTypeValue<>(value, type, name, subValue, formatter);
	}

	/**
	 * Validates value to make sure type is set.
	 */
	public static void validate(IntTypeValue<?> value) {
		validate(value, (String) null);
	}

	/**
	 * Validates value to make sure type is set.
	 */
	public static void validate(IntTypeValue<?> value, String name) {
		validateExcept(value, null, name);
	}

	/**
	 * Validates value to make sure type is set and not equals to the invalid value.
	 */
	public static <T> void validateExcept(IntTypeValue<T> value, T invalid) {
		validateExcept(value, invalid, null);
	}

	/**
	 * Validates value to make sure type is set and not equals to the invalid value.
	 */
	public static <T> void validateExcept(IntTypeValue<T> value, T invalid, String name) {
		validateNotNull(value, name);
		validateNotNull(value.type(), name);
		if (invalid != null) validateNotEqualObj(value.type(), invalid, name);
	}

	protected IntTypeValue(int value, T type, String name, Integer subValue,
		IntFunction<String> formatter) {
		this.type = type;
		this.value = value;
		this.subValue = subValue;
		this.name = type != null ? null : name; // ignore if has type
		this.formatter = defaultValue(formatter, dec::format);
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
		return Objects.hash(type, name, value, subValue);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof IntTypeValue)) return false;
		IntTypeValue<?> other = (IntTypeValue<?>) obj;
		if (value != other.value) return false;
		if (!Objects.equals(type, other.type)) return false;
		if (!Objects.equals(subValue, other.subValue)) return false;
		if (!Objects.equals(name, other.name)) return false;
		return true;
	}

	public String compact() {
		if (!hasType()) return toString();
		if (!hasSubValue()) return type().toString();
		return String.format("%s(%s)", type, formatter.apply(subValue));
	}

	@Override
	public String toString() {
		if (subValue == null) return String.format("%s(%s)", name(), formatter.apply(value));
		return String.format("%s(%s:%s)", name(), formatter.apply(value),
			formatter.apply(subValue));
	}

}
