package ceri.common.code;

import java.util.regex.Pattern;
import ceri.common.function.FunctionUtil;

public class Generics {
	private static final Pattern CLEAN_GENERICS_REGEX =
		Pattern.compile("(<[^<>]+>|&|extends|super|(?<=\\w+)\\s+\\w+)");
	public String cls;
	public String simple;
	public String stat; // static
	public String empty;
	public String wildcard;

	public static Generics of(String classGenerics) {
		return new Generics(classGenerics);
	}
	
	private Generics(String cls) {
		this.cls = cls;
		simple = simplify(cls);
		stat = stat(cls);
		empty = empty(cls);
		wildcard = wildcard(cls, simple);
	}

	private String stat(String classGenerics) {
		return classGenerics.isEmpty() ? "" : classGenerics + " ";
	}

	private String empty(String classGenerics) {
		if (classGenerics.isEmpty()) return "";
		return "<>";
	}

	private String wildcard(String classGenerics, String simpleGenerics) {
		if (classGenerics.isEmpty()) return "";
		return simpleGenerics.replaceAll("\\w+", "?");
	}

	public static String simplify(String generics) {
		if (generics.isEmpty()) return "";
		String str = FunctionUtil.recurse(generics.substring(1, generics.length() - 1),
			s -> CLEAN_GENERICS_REGEX.matcher(s).replaceAll(""));
		return "<" + str + ">";
	}

}
