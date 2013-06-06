package ceri.parser.expression;

public interface Expression {
	boolean matches(String str);
	String asRegex();
	String asString();
}
