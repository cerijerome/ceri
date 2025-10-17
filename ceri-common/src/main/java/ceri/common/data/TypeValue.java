package ceri.common.data;

import java.util.Objects;
import ceri.common.text.Format;
import ceri.common.util.Basics;
import ceri.common.util.Validate;

/**
 * Used to hold values that may have a type value, such as an enum. If no type maps to the value,
 * then a name can be used, such as "unknown". A sub-value is also available for types that map to a
 * range of values.
 */
public class TypeValue<T> {
	private final T type;
	private final long value;
	private final Long sub;
	private final String name;
	private transient final Format.LongFunction formatter;

	/**
	 * Validates value to make sure type is set.
	 */
	public static <T, V extends TypeValue<T>> V validate(V value) {
		return valid(value, (String) null);
	}

	/**
	 * Validates value to make sure type is set.
	 */
	public static <T, V extends TypeValue<T>> V valid(V value, String name) {
		return validExcept(value, null, name);
	}

	/**
	 * Validates value to make sure type is set and not equals to the invalid value.
	 */
	public static <T, V extends TypeValue<T>> V validExcept(V value, T invalid) {
		return validExcept(value, invalid, null);
	}

	/**
	 * Validates value to make sure type is set and not equals to the invalid value.
	 */
	public static <T, V extends TypeValue<T>> V validExcept(V value, T invalid, String name) {
		Validate.nonNull(value, name);
		Validate.nonNull(value.type(), name);
		if (invalid != null) Validate.notEqual(value.type(), invalid, name);
		return value;
	}

	/**
	 * Create type with optional sub-value and decimal formatter.
	 */
	public static <T> TypeValue<T> of(long value, T type, String name) {
		return of(value, type, name, null);
	}

	/**
	 * Create type with optional sub-value and decimal formatter.
	 */
	public static <T> TypeValue<T> of(long value, T type, String name, long sub) {
		return of(value, type, name, sub, null);
	}

	/**
	 * Create type with optional sub-value and custom formatter.
	 */
	public static <T> TypeValue<T> of(long value, T type, String name, long sub,
		Format.LongFunction formatter) {
		return new TypeValue<>(value, type, name, sub, formatter);
	}

	/**
	 * Create type with optional sub-value and custom formatter.
	 */
	public static <T> TypeValue<T> of(long value, T type, String name,
		Format.LongFunction formatter) {
		return new TypeValue<>(value, type, name, null, formatter);
	}

	protected TypeValue(long value, T type, String name, Long sub, Format.LongFunction formatter) {
		this.type = type;
		this.value = value;
		this.sub = sub;
		this.name = type != null ? null : name; // ignore if has type
		this.formatter = Basics.def(formatter, Format.DEC);
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

	public long value() {
		return value;
	}

	public int intValue() {
		return Math.toIntExact(value());
	}

	public long sub() {
		return sub == null ? 0L : sub;
	}

	public int intSub() {
		return Math.toIntExact(sub());
	}

	public boolean hasSub() {
		return sub != null;
	}

	@Override
	public int hashCode() {
		return Objects.hash(type, name, value, sub);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof TypeValue other)) return false;
		if (value != other.value) return false;
		if (!Objects.equals(type, other.type)) return false;
		if (!Objects.equals(sub, other.sub)) return false;
		if (!Objects.equals(name, other.name)) return false;
		return true;
	}

	public String compact() {
		if (!hasType()) return toString();
		if (!hasSub()) return type().toString();
		return String.format("%s(%s)", type, formatter.apply(sub));
	}

	@Override
	public String toString() {
		if (sub == null) return String.format("%s(%s)", name(), formatter.apply(value));
		return String.format("%s(%s:%s)", name(), formatter.apply(value), formatter.apply(sub));
	}
}
