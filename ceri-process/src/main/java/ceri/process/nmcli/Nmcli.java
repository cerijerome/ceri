package ceri.process.nmcli;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import ceri.common.collect.Immutable;
import ceri.common.collect.Maps;
import ceri.common.process.Columns;
import ceri.common.process.Output;
import ceri.common.process.Parameters;
import ceri.common.process.Processor;
import ceri.common.property.Parser;
import ceri.common.text.Regex;

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

		public record Item(String name, String uuid, String type, String device) {

			public static final Item NULL = new Item(null, null, null, null);
			private static final String NAME_COLUMN = "NAME";
			private static final String UUID_COLUMN = "UUID";
			private static final String TYPE_COLUMN = "TYPE";
			private static final String DEVICE_COLUMN = "DEVICE";

			public static List<Item> fromOutput(String output) {
				var lines = Regex.Split.LINE.list(output);
				if (lines.size() <= 1) return List.of();
				var columns = Columns.fromFixedWidthHeader(lines.get(0));
				lines = lines.subList(1, lines.size());
				return Immutable.adaptList(line -> fromNameValues(columns.parseAsMap(line)), lines);
			}

			private static Item fromNameValues(Map<String, String> map) {
				String name = map.get(NAME_COLUMN);
				String uuid = map.get(UUID_COLUMN);
				String type = map.get(TYPE_COLUMN);
				String device = map.get(DEVICE_COLUMN);
				return new Item(name, uuid, type, device);
			}

			public boolean isNull() {
				return name == null && uuid == null && type == null && device == null;
			}
		}

		public record IdResult(Map<String, String> values) {
			public static final IdResult NULL = new IdResult(Immutable.map());
			private static final Pattern NAME_VALUE_SPLIT = Pattern.compile("(.*?):\\s*(.*)");
			private static final String NONE = "--";

			public enum Key {
				conId("connection.id"),
				conUuid("connection.uuid"),
				conType("connection.type"),
				conIfaceName("connection.interface-name"),
				genState("GENERAL.STATE"),
				genVpn("GENERAL.VPN");

				public final String value;

				private Key(String value) {
					this.value = value;
				}
			}

			public static IdResult fromOutput(String output) {
				var map = Maps.<String, String>link();
				for (var line : Regex.Split.LINE.array(output)) {
					var m = Regex.match(NAME_VALUE_SPLIT, line);
					if (m.hasMatch()) map.put(m.group(1), m.group(2));
				}
				return new IdResult(Immutable.wrap(map));
			}

			public boolean isNull() {
				return Maps.isEmpty(values());
			}

			public String get(Key key) {
				return key == null ? "" : value(key.value);
			}

			public Parser.String parse(Key key) {
				return Parser.string(get(key));
			}

			public String value(String key) {
				var value = Maps.get(values(), key, "");
				return NONE.equals(value) ? "" : value;
			}
		}

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
		public Output<List<Con.Item>> show() throws IOException {
			return Output.of(exec(Parameters.of(SHOW_COMMAND)), Con.Item::fromOutput);
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
		public Output<Con.IdResult> show(String id) throws IOException {
			return Output.of(exec(Parameters.of(SHOW_COMMAND, ID_PARAM, id)),
				Con.IdResult::fromOutput);
		}

		public String up(String id) throws IOException {
			return up(id, null);
		}

		public String up(String id, Integer waitSec) throws IOException {
			var params = Parameters.of(UP_COMMAND, ID_PARAM, id);
			if (waitSec != null) params.add(WAIT_OPTION, waitSec);
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
