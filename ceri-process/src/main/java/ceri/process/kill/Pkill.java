package ceri.process.kill;

import java.io.IOException;
import ceri.process.util.Parameters;
import ceri.process.util.Processor;

public class Pkill {
	private static final String PKILL = "pkill";
	private static final String FULL_OPTION = "-f";
	private final Processor processor;
	private boolean full = false;
	private Signal signal = null;
	
	public Pkill() {
		this(Processor.DEFAULT);
	}
	
	public Pkill(Processor processor) {
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
		Parameters params = new Parameters();
		if (full) params.add(FULL_OPTION);
		if (signal != null) params.add("-" + signal.number);
		return exec(params.add(pattern));
	}
	
	private String exec(Parameters params) throws IOException {
		return processor.exec(Parameters.of(PKILL).add(params));
	}
	
}
