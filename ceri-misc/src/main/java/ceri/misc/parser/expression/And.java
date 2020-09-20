package ceri.misc.parser.expression;

import java.util.Objects;
import ceri.common.text.ToString;

public class And implements Expression {
	private final Expression lhs;
	private final Expression rhs;

	public And(Expression lhs, Expression rhs) {
		if (lhs == null || rhs == null)
			throw new IllegalArgumentException("lhs or rhs is null: " + lhs + ", " + rhs);
		this.lhs = lhs;
		this.rhs = rhs;
	}

	@Override
	public boolean matches(String str) {
		return lhs.matches(str) && rhs.matches(str);
	}

	@Override
	public String asRegex() {
		String lhsRegex = lhs.asRegex();
		String rhsRegex = rhs.asRegex();
		// return "(?:(?:" + lhsRegex + ".*?" + rhsRegex + ")|(?:" + rhsRegex + ".*?" + lhsRegex +
		// "))";
		return "(?:(?:" + lhsRegex + rhsRegex + ")|(?:" + rhsRegex + lhsRegex + "))";
	}

	@Override
	public String asString() {
		return lhs.asString() + " " + rhs.asString();
	}

	@Override
	public String toString() {
		return ToString.forClass(this, lhs, rhs);
	}

	@Override
	public int hashCode() {
		return Objects.hash(lhs, rhs);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof And)) return false;
		And exp = (And) obj;
		if (!Objects.equals(lhs, exp.lhs)) return false;
		return Objects.equals(rhs, exp.rhs);
	}

}
