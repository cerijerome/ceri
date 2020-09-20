package ceri.misc.parser.expression;

import java.util.Objects;
import java.util.regex.Pattern;
import ceri.common.text.ToString;

public class Exclude implements Expression {
	private final Expression expression;

	public Exclude(Expression expression) {
		this.expression = expression;
	}

	public static void main(String[] args) {
		match("a(bc){0}d", "ad");
		match("(?!bc)", "abc");
		match("^((?!defh).)*$", "abcdefghijklm");
		match("((?!ghi).)*$", "abcdefghijklm");
		match("bd|(^((?!bc).)*$)", "abcde");
		match("bc|^((?!bc).)*$", "abcde");
		match("(?:(?:(?!\\Qa\\E).*?\\Qb\\E)|(?:\\Qb\\E.*?(?!\\Qa\\E)))", "ba");
	}

	private static void match(String regex, String s) {
		Pattern p = Pattern.compile(regex);
		boolean result = p.matcher(s).find();
		System.out.println(regex + " => \"" + s + "\" = " + result);
	}

	@Override
	public boolean matches(String str) {
		return !expression.matches(str);
	}

	@Override
	public String asRegex() {
		// Only use for concat expressions
		String regex = expression.asRegex();
		return "(?!" + regex + ")";
	}

	@Override
	public String asString() {
		return "-" + expression.asString();
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
		if (!(obj instanceof Exclude)) return false;
		Exclude exp = (Exclude) obj;
		return Objects.equals(expression, exp.expression);
	}

}
