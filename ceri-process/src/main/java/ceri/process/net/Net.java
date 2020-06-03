package ceri.process.net;

import java.io.IOException;
import ceri.common.process.Output;
import ceri.common.process.Parameters;
import ceri.common.process.Processor;

/**
 * Windows command 'net'.
 */
public class Net {
	private static final String NET = "net";
	public final Stats stats;

	public static Net of() {
		return of(Processor.DEFAULT);
	}

	public static Net of(Processor processor) {
		return new Net(processor);
	}

	private Net(Processor processor) {
		stats = new Stats(processor);
	}

	public static class Stats {
		private static final String STATS = "stats";
		private static final String SERVER = "srv";
		private final Processor processor;

		Stats(Processor processor) {
			this.processor = processor;
		}

		public Output<ServerStats> server() throws IOException {
			return Output.of(exec(Parameters.of(SERVER)), ServerStats::from);
		}

		private String exec(Parameters params) throws IOException {
			return processor.exec(Parameters.of(NET, STATS).addAll(params));
		}

	}

}
