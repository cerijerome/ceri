package ceri.process.util;

import java.util.function.Function;

public class Output<T> {
	public final String out;
	private final Function<String, T> parser;
	
	public Output(String out, Function<String, T> parser) {
		this.out = out;
		this.parser = parser;
	}
	
	public T parse() {
		if (out == null || parser == null) return null;
		return parser.apply(out);
	}
	
	@Override
	public String toString() {
		return out;
	}
	
}
