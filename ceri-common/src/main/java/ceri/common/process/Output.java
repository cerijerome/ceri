package ceri.common.process;

import java.util.function.Function;

/**
 * Captures raw text output from process, and parsed type.
 */
public class Output<T> {
	public final String out;
	private final Function<String, T> parser;
	
	public static <T> Output<T> of(String out, Function<String, T> parser) {
		return new Output<>(out, parser);
	}
	
	private Output(String out, Function<String, T> parser) {
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
