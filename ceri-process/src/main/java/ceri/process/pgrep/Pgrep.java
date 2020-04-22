package ceri.process.pgrep;

import java.io.IOException;
import java.util.Arrays;
import java.util.function.Predicate;
import ceri.common.process.Output;
import ceri.common.process.Parameters;
import ceri.common.process.Processor;
import ceri.common.text.StringUtil;

public class Pgrep {
	private static final String PGREP = "pgrep";
	private static final String FULL_OPTION = "-f"; // make into enum, share with pkill
	private final Processor processor;
	private boolean full = false;

	private static int[] pids(String output) {
		String[] pidStrs = StringUtil.WHITE_SPACE_REGEX.split(output);
		Predicate<String> filter = String::isEmpty;
		return Arrays.stream(pidStrs).filter(filter.negate()).mapToInt(Integer::parseInt).toArray();
	}

	public static Pgrep of() {
		return of(Processor.DEFAULT);
	}

	public static Pgrep of(Processor processor) {
		return new Pgrep(processor);
	}

	private Pgrep(Processor processor) {
		this.processor = processor;
	}

	public Pgrep full(boolean enabled) {
		full = enabled;
		return this;
	}

	public Output<int[]> pgrep(String pattern) throws IOException {
		Parameters params = Parameters.of();
		if (full) params.add(FULL_OPTION);
		params.add(pattern);
		return Output.of(exec(params), Pgrep::pids);
	}

	private String exec(Parameters params) throws IOException {
		return processor.exec(Parameters.of(PGREP).addAll(params));
	}

}
