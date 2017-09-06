package ceri.misc.parser.expression;

import ceri.common.text.ToStringHelper;
import ceri.common.util.EqualsUtil;
import ceri.common.util.HashCoder;

public class Parentheses implements Expression {
	private final Expression expression;
	private final int hashCode;

	public Parentheses(Expression expression) {
		this.expression = expression;
		hashCode = HashCoder.hash(expression);
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
		return ToStringHelper.createByClass(this, expression).toString();
	}

	@Override
	public int hashCode() {
		return hashCode;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof Parentheses)) return false;
		Parentheses exp = (Parentheses) obj;
		return EqualsUtil.equals(expression, exp.expression);
	}
	
}
