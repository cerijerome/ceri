package ceri.misc.parser.expression;

import java.util.Objects;
import ceri.common.text.ToString;

public class Or implements Expression {
	private final Expression lhs;
	private final Expression rhs;

	public Or(Expression lhs, Expression rhs) {
		if (lhs == null || rhs == null)
			throw new IllegalArgumentException("lhs or rhs is null: " + lhs + ", " + rhs);
		this.lhs = lhs;
		this.rhs = rhs;
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
		return ToString.forClass(this, lhs, rhs);
	}

	@Override
	public int hashCode() {
		return Objects.hash(lhs, rhs);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof Or)) return false;
		Or exp = (Or) obj;
		if (!Objects.equals(lhs, exp.lhs)) return false;
		return Objects.equals(rhs, exp.rhs);
	}

}
