package ceri.process.kill;

import java.io.IOException;
import java.util.stream.IntStream;
import ceri.common.process.Parameters;
import ceri.common.process.Processor;

public class Kill {
	private static final String KILL = "kill";
	private final Processor processor;
	private Signal signal = null;

	public enum Signal {
		HUP(1), // hang up
		INT(2), // interrupt
		QUIT(3), // quit
		ABRT(6), // abort
		KILL(9), // non-catchable, non-ignorable kill
		ALRM(14), // alarm clock
		TERM(15); // software termination signal (default)

		public final int number;

		Signal(int number) {
			this.number = number;
		}
	
	}
	public static Kill of() {
		return of(Processor.DEFAULT);
	}

	public static Kill of(Processor processor) {
		return new Kill(processor);
	}

	private Kill(Processor processor) {
		this.processor = processor;
	}

	public Kill signal(Signal signal) {
		this.signal = signal;
		return this;
	}

	public String kill(int... pids) throws IOException {
		var params = Parameters.of();
		if (signal != null) params.add("-" + signal.number);
		IntStream.of(pids).forEach(params::add);
		return exec(params);
	}

	private String exec(Parameters params) throws IOException {
		return processor.exec(Parameters.of(KILL).addAll(params));
	}
}
