package ceri.common.text;

import java.util.function.Function;

public class Marshaller<T> {
	private final Function<T, String> to;
	private final Function<String, T> from;

	public Marshaller(Function<T, String> to, Function<String, T> from) {
		this.to = to;
		this.from = from;
	}

	public String to(T unit) {
		if (unit == null) return null;
		return to.apply(unit);
	}

	public T from(String s) {
		if (s == null) return null;
		return from.apply(s);
	}

}
