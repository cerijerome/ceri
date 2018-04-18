package ceri.process.util.parse;

import ceri.common.util.BasicUtil;
import ceri.common.util.EqualsUtil;
import ceri.common.util.HashCoder;
import ceri.common.util.PrimitiveUtil;

public class NameValue implements Named {
	public static NameValue NULL = new NameValue(null, null);
	public final String name;
	public final String value;

	public NameValue(String name, String value) {
		this.name = name;
		this.value = value;
	}

	@Override
	public String name() {
		return name;
	}

	public static NameValue as(Value value) {
		if (value == null) return NULL;
		NameValue nameValue = BasicUtil.castOrNull(NameValue.class, value);
		if (nameValue != null) return nameValue;
		Named named = BasicUtil.castOrNull(Named.class, value);
		if (named != null) return new NameValue(named.name(), null);
		return NULL;
	}
	
	public Boolean asBoolean() {
		return PrimitiveUtil.valueOf(value, (Boolean) null);
	}

	public Integer asInteger() {
		return PrimitiveUtil.valueOf(value, (Integer) null);
	}

	public Double asDouble() {
		return PrimitiveUtil.valueOf(value, (Double) null);
	}

	@Override
	public int hashCode() {
		return HashCoder.hash(name, value);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof NameValue)) return false;
		NameValue other = (NameValue) obj;
		if (!EqualsUtil.equals(name, other.name)) return false;
		if (!EqualsUtil.equals(value, other.value)) return false;
		return true;
	}

	@Override
	public String toString() {
		return name + " : " + value;
	}

}
