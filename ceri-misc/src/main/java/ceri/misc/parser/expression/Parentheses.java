package ceri.misc.parser.expression;

import java.util.Objects;
import ceri.common.text.ToString;

public class Parentheses implements Expression {
	private final Expression expression;

	public Parentheses(Expression expression) {
		this.expression = expression;
	}

	@Override
	public boolean matches(String str) {
		return expression.matches(str);
	}

	@Override
	public String asRegex() {
		return expression.asRegex(); // No parentheses?
	}

	@Override
	public String asString() {
		return "(" + expression.asString() + ")";
	}

	@Override
	public String toString() {
		return ToString.forClass(this, expression);
	}

	@Override
	public int hashCode() {
		return Objects.hash(expression);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof Parentheses)) return false;
		Parentheses exp = (Parentheses) obj;
		return Objects.equals(expression, exp.expression);
	}

}
