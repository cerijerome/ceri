package ceri.misc.parser.expression;

import java.util.Objects;
import ceri.common.text.ToString;

public class Wildcard implements Expression {

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
		return ToString.forClass(this);
	}

	@Override
	public int hashCode() {
		return Objects.hash();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		return obj instanceof Wildcard;
	}

}
