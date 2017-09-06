package ceri.misc.parser.expression;

import ceri.common.text.ToStringHelper;
import ceri.common.util.EqualsUtil;
import ceri.common.util.HashCoder;

public class Or implements Expression {
	private final Expression lhs;
	private final Expression rhs;
	private final int hashCode;

	public Or(Expression lhs, Expression rhs) {
		if (lhs == null || rhs == null) throw new IllegalArgumentException("lhs or rhs is null: " +
			lhs + ", " + rhs);
		this.lhs = lhs;
		this.rhs = rhs;
		hashCode = HashCoder.hash(lhs, rhs);
	}

	@Override
	public boolean matches(String str) {
		return lhs.matches(str) || rhs.matches(str);
	}
	
	@Override
	public String asRegex() {
		return "(?:" + lhs.asRegex() + "|" + rhs.asRegex() + ")";
	}

	@Override
	public String asString() {
		return lhs.asString() + " OR " + rhs.asString();
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
		if (!(obj instanceof Or)) return false;
		Or exp = (Or) obj;
		if (!EqualsUtil.equals(lhs, exp.lhs)) return false;
		return EqualsUtil.equals(rhs, exp.rhs);
	}
	
}
