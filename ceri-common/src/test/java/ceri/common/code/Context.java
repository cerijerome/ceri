package ceri.common.code;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class Context {
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
		imports.stream().map(Class::getName).sorted()
			.forEach(s -> b.append(String.format("import %s;%n", s)));
		if (b.length() > 0) b.append("\n");
		return b.toString();
	}

}