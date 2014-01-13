package ceri.parser.expression;

import java.util.regex.Pattern;
import ceri.common.util.EqualsUtil;
import ceri.common.util.HashCoder;
import ceri.common.util.ToStringHelper;

public class Exclude implements Expression {
	private final Expression expression;
	private final int hashCode;

	public Exclude(Expression expression) {
		this.expression = expression;
		hashCode = HashCoder.hash(expression);
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
		return ToStringHelper.createByClass(this, expression).toString();
	}

	@Override
	public int hashCode() {
		return hashCode;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof Exclude)) return false;
		Exclude exp = (Exclude) obj;
		return EqualsUtil.equals(expression, exp.expression);
	}
	
}
