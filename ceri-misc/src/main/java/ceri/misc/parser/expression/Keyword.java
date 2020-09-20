package ceri.misc.parser.expression;

import java.util.Objects;
import ceri.common.text.ToString;

public class Keyword implements Expression {
	private final String value;

	public Keyword(String value) {
		this.value = value;
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
		return value;
	}

	@Override
	public String toString() {
		return ToString.forClass(this, value);
	}

	@Override
	public int hashCode() {
		return Objects.hash(value);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof Keyword)) return false;
		Keyword exp = (Keyword) obj;
		return Objects.equals(value, exp.value);
	}

}
