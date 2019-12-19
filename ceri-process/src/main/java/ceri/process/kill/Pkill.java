package ceri.process.kill;

import java.io.IOException;
import ceri.common.process.Parameters;
import ceri.common.process.Processor;

public class Pkill {
	private static final String PKILL = "pkill";
	private static final String FULL_OPTION = "-f";
	private final Processor processor;
	private boolean full = false;
	private Signal signal = null;

	public static Pkill of() {
		return of(Processor.DEFAULT);
	}

	public static Pkill of(Processor processor) {
		return new Pkill(processor);
	}

	private Pkill(Processor processor) {
		this.processor = processor;
	}

	public Pkill full(boolean enabled) {
		full = enabled;
		return this;
	}

	public Pkill signal(Signal signal) {
		this.signal = signal;
		return this;
	}

	public String pkill(String pattern) throws IOException {
		Parameters params = Parameters.of();
		if (full) params.add(FULL_OPTION);
		if (signal != null) params.add("-" + signal.number);
		return exec(params.add(pattern));
	}

	private String exec(Parameters params) throws IOException {
		return processor.exec(Parameters.of(PKILL).add(params));
	}

}
