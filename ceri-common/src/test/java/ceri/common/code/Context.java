package ceri.common.code;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import ceri.common.comparator.Comparators;

public class Context {
	private static final Comparator<String> importComparator = importComparator();
	public final StringBuilder b = new StringBuilder();
	public final Set<Class<?>> imports = new HashSet<>();

	public void print(String line) {
		b.append(line);
	}

	public void println() {
		b.append("\n");
	}

	public void println(String line) {
		print(line);
		println();
	}

	public void printf(String format, Object... objs) {
		b.append(String.format(format, objs));
	}

	public void imports(Class<?>... classes) {
		Collections.addAll(imports, classes);
	}

	@Override
	public String toString() {
		return importsToString() + b.toString();
	}

	private String importsToString() {
		StringBuilder b = new StringBuilder();
		imports.stream().map(Class::getName).sorted(importComparator)
			.forEach(s -> b.append(String.format("import %s;%n", s)));
		if (b.length() > 0) b.append("\n");
		return b.toString();
	}

	private static Comparator<String> importComparator() {
		return new Comparator<String>() {
			@Override
			public int compare(String lhs, String rhs) {
				boolean lhsJava = lhs.startsWith("java");
				boolean rhsJava = rhs.startsWith("java");
				if (lhsJava == rhsJava) return Comparators.STRING.compare(lhs, rhs);
				return lhsJava ? -1 : 1;
			}
		};
	}
	
}