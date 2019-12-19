package ceri.process.nmcli;

import java.io.IOException;
import java.util.List;
import ceri.common.process.Output;
import ceri.common.process.Parameters;
import ceri.common.process.Processor;

public class Nmcli {
	private static final String NMCLI = "nmcli";
	public final Con con;

	public static Nmcli of() {
		return of(Processor.DEFAULT);
	}

	public static Nmcli of(Processor processor) {
		return new Nmcli(processor);
	}

	private Nmcli(Processor processor) {
		con = new Con(processor);
	}

	public static class Con {
		private static final String OBJECT = "con";
		private static final String SHOW_COMMAND = "show";
		private static final String UP_COMMAND = "up";
		private static final String DOWN_COMMAND = "down";
		private static final String ID_PARAM = "id";
		private static final String WAIT_OPTION = "--wait";
		private final Processor processor;

		Con(Processor processor) {
			this.processor = processor;
		}

		/**
		 * Call <b>nmcli show</b> without id. Example output:
		 * 
		 * <pre>
		 * NAME  UUID                                  TYPE      DEVICE
		 * eth1  01fa0bf4-b6bd-484f-a9a3-2b10ff701dcd  ethernet  eth1
		 * eth0  2e9f0cdd-ea2f-4b63-b146-3b9a897c9e45  ethernet  eth0
		 * eth2  186053d4-9369-4a4e-87b8-d1f9a419f985  ethernet  eth2
		 * </pre>
		 */
		public Output<List<ConShowItem>> show() throws IOException {
			return Output.of(exec(Parameters.of(SHOW_COMMAND)), ConShowItem::fromOutput);
		}

		/**
		 * Call <b>nmcli show id [id]</b>. Example output:
		 * 
		 * <pre>
		 * connection.id:                          eth2
		 * connection.uuid:                        186053d4-9369-4a4e-87b8-d1f9a419f985
		 * connection.stable-id:                   --
		 * connection.type:                        802-3-ethernet
		 * connection.interface-name:              eth2
		 * connection.autoconnect:                 yes
		 * </pre>
		 */
		public Output<ConShowIdResult> show(String id) throws IOException {
			return Output.of(exec(Parameters.of(SHOW_COMMAND, ID_PARAM, id)),
				ConShowIdResult::fromOutput);
		}

		public String up(String id) throws IOException {
			return up(id, null);
		}

		public String up(String id, Integer waitSec) throws IOException {
			Parameters params = Parameters.of(UP_COMMAND, ID_PARAM, id);
			if (waitSec != null) params.add(WAIT_OPTION, String.valueOf(waitSec));
			return exec(params);
		}

		public String down(String id) throws IOException {
			return exec(Parameters.of(DOWN_COMMAND, ID_PARAM, id));
		}

		private String exec(Parameters params) throws IOException {
			return processor.exec(Parameters.of(NMCLI, OBJECT).addAll(params));
		}

	}

}
