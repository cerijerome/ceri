package ceri.parser.expression;

import ceri.common.text.ToStringHelper;
import ceri.common.util.EqualsUtil;
import ceri.common.util.HashCoder;

public class Quote implements Expression {
	public final String value;
	private final int hashCode;

	public Quote(String value) {
		this.value = value;
		hashCode = HashCoder.hash(value); 
	}

	@Override
	public boolean matches(String str) {
		return str.contains(value);
	}
	
	@Override
	public String asRegex() {
		return "\\Q" + value + "\\E";
	}

	@Override
	public String asString() {
		return "\"" + value + "\"";
	}

	@Override
	public String toString() {
		return ToStringHelper.createByClass(this, "\"" + value + "\"").toString();
	}

	@Override
	public int hashCode() {
		return hashCode;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof Quote)) return false;
		Quote exp = (Quote) obj;
		return EqualsUtil.equals(value, exp.value);
	}
	
}
