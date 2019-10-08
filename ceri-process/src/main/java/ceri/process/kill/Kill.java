package ceri.process.kill;

import java.io.IOException;
import java.util.stream.IntStream;
import ceri.log.process.Parameters;
import ceri.log.process.Processor;

public class Kill {
	private static final String KILL = "kill";
	private final Processor processor;
	private Signal signal = null;
	
	public Kill() {
		this(Processor.DEFAULT);
	}
	
	public Kill(Processor processor) {
		this.processor = processor;
	}
	
	public Kill signal(Signal signal) {
		this.signal = signal;
		return this;
	}
	
	public String kill(int...pids) throws IOException {
		Parameters params = new Parameters();
		if (signal != null) params.add("-" + signal.number);
		IntStream.of(pids).forEach(pid -> params.add(String.valueOf(pid)));
		return exec(params);
	}
	
	private String exec(Parameters params) throws IOException {
		return processor.exec(Parameters.of(KILL).add(params));
	}
	
}
