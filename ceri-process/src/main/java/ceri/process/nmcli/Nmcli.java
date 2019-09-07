package ceri.process.nmcli;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import ceri.process.util.Output;
import ceri.process.util.Parameters;
import ceri.process.util.Processor;
import ceri.process.util.parse.ParseUtil;

public class Nmcli {
	private static final String NMCLI = "nmcli";
	public final Con con;

	public static void main(String[] args) throws Exception {
		System.out.println(new Nmcli().con.list());
		System.out.println(new Nmcli().con.status());
	}

	public Nmcli() {
		this(Processor.DEFAULT);
	}

	public Nmcli(Processor processor) {
		con = new Con(processor);
	}

	public static class Con {
		private static final String NAME_VALUE_SPLIT = ":\\s+";
		private static final String OBJECT = "con";
		private static final String LIST_COMMAND = "list";
		private static final String STATUS_COMMAND = "status";
		private static final String UP_COMMAND = "up";
		private static final String DOWN_COMMAND = "down";
		private static final String ID_PARAM = "id";
		private static final String NO_WAIT_OPTION = "--nowait";
		private static final String TIMEOUT_OPTION = "--timeout";
		private final Processor processor;

		public enum Wait {
			wait,
			noWait
		}

		Con(Processor processor) {
			this.processor = processor;
		}

		public Output<List<ConListItem>> list() throws IOException {
			return new Output<>(exec(Parameters.of(LIST_COMMAND)), ConListItem::fromOutput);
		}

		public Output<Map<String, String>> list(String id) throws IOException {
			return new Output<>(exec(Parameters.of(LIST_COMMAND, ID_PARAM, id)), this::nameValues);
		}

		public Output<List<ConStatusItem>> status() throws IOException {
			return new Output<>(exec(Parameters.of(STATUS_COMMAND)), ConStatusItem::fromOutput);
		}

		public Output<Map<String, String>> status(String id) throws IOException {
			return new Output<>(exec(Parameters.of(STATUS_COMMAND, ID_PARAM, id)), this::nameValues);
		}

		public String up(String id) throws IOException {
			return up(id, null, null);
		}

		public String up(String id, Wait wait, Integer timeoutSec) throws IOException {
			Parameters params = Parameters.of(UP_COMMAND, ID_PARAM, id);
			if (wait == Wait.noWait) params.add(NO_WAIT_OPTION);
			if (timeoutSec != null) params.add(TIMEOUT_OPTION, String.valueOf(timeoutSec));
			return exec(params);
		}

		public String down(String id) throws IOException {
			return exec(Parameters.of(DOWN_COMMAND, ID_PARAM, id));
		}

		private String exec(Parameters params) throws IOException {
			return processor.exec(Parameters.of(NMCLI, OBJECT).add(params));
		}

		private Map<String, String> nameValues(String output) {
			return ParseUtil.toMap(ParseUtil.parseNameValues(NAME_VALUE_SPLIT, output));
		}

	}

}
