package ceri.process.scutil;

import java.io.IOException;
import java.util.List;
import ceri.log.process.Output;
import ceri.log.process.Parameters;
import ceri.log.process.Processor;

public class Scutil {
	private static final String SCUTIL = "scutil";
	public final Nc nc;

	public Scutil() {
		this(Processor.DEFAULT);
	}

	public Scutil(Processor processor) {
		nc = new Nc(processor);
	}

	public static class Nc {
		private static final String OPTION = "--nc";
		private static final String LIST = "list";
		private static final String STATUS_COMMAND = "status";
		private static final String SHOW_COMMAND = "show";
		private static final String STATISTICS_COMMAND = "statistics";
		private static final String START_COMMAND = "start";
		private static final String USER_PARAM = "--user";
		private static final String PASSWORD_PARAM = "--password";
		private static final String SECRET_PARAM = "--secret";
		private static final String STOP_COMMAND = "stop";
		private final Processor processor;

		Nc(Processor processor) {
			this.processor = processor;
		}

		public Output<List<NcListItem>> list() throws IOException {
			return new Output<>(exec(Parameters.of(LIST)), NcListItem::fromList);
		}

		public Output<NcStatus> status(String service) throws IOException {
			return new Output<>(exec(Parameters.of(STATUS_COMMAND, service)), NcStatus::from);
		}

		public Output<NcShow> show(String service) throws IOException {
			return new Output<>(exec(Parameters.of(SHOW_COMMAND, service)), NcShow::from);
		}

		public Output<NcStatistics> statistics(String service) throws IOException {
			return new Output<>(exec(Parameters.of(STATISTICS_COMMAND, service)),
				NcStatistics::from);
		}

		public String start(String service, String user, String password, String secret)
			throws IOException {
			Parameters params = Parameters.of(START_COMMAND, service);
			if (user != null) params.add(USER_PARAM, user);
			if (password != null) params.add(PASSWORD_PARAM, password);
			if (secret != null) params.add(SECRET_PARAM, secret);
			return exec(params);
		}

		public String stop(String service) throws IOException {
			return exec(Parameters.of(STOP_COMMAND, service));
		}

		private String exec(Parameters params) throws IOException {
			return processor.exec(Parameters.of(SCUTIL, OPTION).add(params));
		}

	}

}
