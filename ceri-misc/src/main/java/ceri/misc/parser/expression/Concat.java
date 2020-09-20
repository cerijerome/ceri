package ceri.misc.parser.expression;

import java.util.Objects;
import java.util.regex.Pattern;
import ceri.common.text.ToString;

public class Concat implements Expression {
	private final Expression lhs;
	private final Expression rhs;
	private final Pattern pattern;

	public Concat(Expression lhs, Expression rhs) {
		this.lhs = lhs;
		this.rhs = rhs;
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
		return ToString.forClass(this, lhs, rhs);
	}

	@Override
	public int hashCode() {
		return Objects.hash(lhs, rhs);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof Concat)) return false;
		Concat exp = (Concat) obj;
		if (!Objects.equals(lhs, exp.lhs)) return false;
		return Objects.equals(rhs, exp.rhs);
	}

}
