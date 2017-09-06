package ceri.misc.parser.expression;

import java.util.regex.Pattern;
import ceri.common.text.ToStringHelper;
import ceri.common.util.EqualsUtil;
import ceri.common.util.HashCoder;

public class Concat implements Expression {
	private final Expression lhs;
	private final Expression rhs;
	private final int hashCode;
	private final Pattern pattern;

	public Concat(Expression lhs, Expression rhs) {
		this.lhs = lhs;
		this.rhs = rhs;
		hashCode = HashCoder.hash(lhs, rhs);
		pattern = Pattern.compile(lhs.asRegex() + rhs.asRegex());
	}

	@Override
	public boolean matches(String str) {
		return pattern.matcher(str).find();
	}
	
	@Override
	public String asRegex() {
		return lhs.asRegex() + rhs.asRegex();
	}

	@Override
	public String asString() {
		return lhs.asString() + rhs.asString();
	}

	@Override
	public String toString() {
		return ToStringHelper.createByClass(this, lhs, rhs).toString();
	}

	@Override
	public int hashCode() {
		return hashCode;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof Concat)) return false;
		Concat exp = (Concat) obj;
		if (!EqualsUtil.equals(lhs, exp.lhs)) return false;
		return EqualsUtil.equals(rhs, exp.rhs);
	}
	
}
