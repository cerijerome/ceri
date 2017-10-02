package ceri.common.data;

import ceri.common.text.StringUtil;
import ceri.common.util.EqualsUtil;
import ceri.common.util.HashCoder;

public class IntTypeValue<T extends Enum<T>> {
	public final T type;
	public final int value;
	private final String name;

	protected IntTypeValue(T type, String name, int value) {
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
		if (!(obj instanceof IntTypeValue)) return false;
		IntTypeValue<?> other = (IntTypeValue<?>) obj;
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
