package ceri.misc.parser.expression;

import ceri.common.text.ToStringHelper;
import ceri.common.util.HashCoder;


public class Wildcard implements Expression {
	private final int hashCode = HashCoder.hash();
	
	@Override
	public boolean matches(String str) {
		return true;
	}
	
	@Override
	public String asRegex() {
		return ".*?";
	}
	
	@Override
	public String asString() {
		return "*";
	}

	@Override
	public String toString() {
		return ToStringHelper.createByClass(this).toString();
	}

	@Override
	public int hashCode() {
		return hashCode;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		return obj instanceof Wildcard;
	}
	
}