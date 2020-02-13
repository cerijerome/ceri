package ceri.common.data;

import ceri.common.text.StringUtil;
import ceri.common.util.EqualsUtil;
import ceri.common.util.HashCoder;

/**
 * Used to hold values that may or may not have an enum value.
 */
public class ShortTypeValue<T extends Enum<T>> {
	public final T type;
	public final short value;
	private final String name;

	protected ShortTypeValue(T type, String name, short value) {
		this.type = type;
		this.value = value;
		this.name = name;
	}

	@Override
	public int hashCode() {
		return HashCoder.hash(type, value, name);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof ShortTypeValue)) return false;
		ShortTypeValue<?> other = (ShortTypeValue<?>) obj;
		if (!EqualsUtil.equals(type, other.type)) return false;
		if (value != other.value) return false;
		if (!EqualsUtil.equals(name, other.name)) return false;
		return true;
	}

	@Override
	public String toString() {
		return name() + "(0x" + StringUtil.toHex(value) + ")";
	}

	public String name() {
		if (type != null) return type.name();
		if (name != null) return name;
		return getClass().getSimpleName();
	}

}
