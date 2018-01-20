package ceri.common.code;

import java.util.Arrays;
import java.util.Collection;
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
		imports(Arrays.asList(classes));
	}

	public void imports(Collection<Class<?>> classes) {
		imports.addAll(classes);
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
				int result = Integer.compare(importScore(lhs), importScore(rhs));
				if (result != 0) return result;
				return Comparators.STRING.compare(lhs, rhs);
			}
			
			private int importScore(String s) {
				if (s.startsWith("java.")) return 1;
				if (s.startsWith("javax.")) return 2;
				if (s.startsWith("org.")) return 3;
				if (s.startsWith("com.")) return 4;
				return 5;
			}
		};
	}
	

}