package ceri.parser.expression;

import ceri.common.text.ToStringHelper;
import ceri.common.util.EqualsUtil;
import ceri.common.util.HashCoder;

public class And implements Expression {
	private final Expression lhs;
	private final Expression rhs;
	private final int hashCode;

	public And(Expression lhs, Expression rhs) {
		if (lhs == null || rhs == null) throw new IllegalArgumentException("lhs or rhs is null: " +
			lhs + ", " + rhs);
		this.lhs = lhs;
		this.rhs = rhs;
		hashCode = HashCoder.hash(lhs, rhs);
	}

	@Override
	public boolean matches(String str) {
		return lhs.matches(str) && rhs.matches(str);
	}

	@Override
	public String asRegex() {
		String lhsRegex = lhs.asRegex();
		String rhsRegex = rhs.asRegex();
		//return "(?:(?:" + lhsRegex + ".*?" + rhsRegex + ")|(?:" + rhsRegex + ".*?" + lhsRegex +
		//	"))";
		return "(?:(?:" + lhsRegex + rhsRegex + ")|(?:" + rhsRegex + lhsRegex + "))";
	}

	@Override
	public String asString() {
		return lhs.asString() + " " + rhs.asString();
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
		if (!(obj instanceof And)) return false;
		And exp = (And) obj;
		if (!EqualsUtil.equals(lhs, exp.lhs)) return false;
		return EqualsUtil.equals(rhs, exp.rhs);
	}

}
