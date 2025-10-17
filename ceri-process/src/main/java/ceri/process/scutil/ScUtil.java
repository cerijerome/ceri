package ceri.process.scutil;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import ceri.common.collect.Immutable;
import ceri.common.collect.Maps;
import ceri.common.collect.Node;
import ceri.common.process.Output;
import ceri.common.process.Parameters;
import ceri.common.process.Processor;
import ceri.common.text.Regex;
import ceri.common.text.Strings;
import ceri.common.util.Basics;

/**
 * Mac command scutil.
 */
public class ScUtil {
	private static final String SCUTIL = "scutil";
	public final Nc nc;

	public static ScUtil of() {
		return of(Processor.DEFAULT);
	}

	public static ScUtil of(Processor processor) {
		return new ScUtil(processor);
	}

	private ScUtil(Processor processor) {
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

		public enum State {
			unknown("Unknown"),
			connecting("Connecting"),
			connected("Connected"),
			disconnected("Disconnected"),
			noService("No service");

			public final String state;

			public static State from(String state) {
				if (state != null) for (var en : State.values())
					if (Strings.equals(false, en.state, state)) return en;
				return State.unknown;
			}

			private State(String state) {
				this.state = state;
			}

			@Override
			public String toString() {
				return "(" + state + ")";
			}
		}

		public record Status(State state, Node<Void> data) {
			public static Status from(String output) {
				var split = Regex.Split.LINE.array(output, 2);
				var state = State.from(split[0]);
				if (split.length == 1) return new Status(state, Node.of());
				return new Status(state, Parser.parse(split[1]));
			}

			@Override
			public String toString() {
				return state + ": " + data;
			}
		}

		/**
		 * Wrapper for the results of {@code scutil --nc statistics <service-name>}:
		 *
		 * <pre>
		 * 	{@code <dictionary>} {
		 * 		PPP : {@code <dictionary>} {
		 * 			BytesIn : 20337
		 * 			BytesOut : 16517
		 * 			ErrorsIn : 0
		 * 			ErrorsOut : 0
		 * 			PacketsIn : 77
		 * 			PacketsOut : 118
		 * 		}
		 * 	}
		 * </pre>
		 */
		public record Stats(Map<String, Integer> values) {
			public static final Stats NULL = new Stats(Immutable.map());

			public enum Key {
				bytesIn("BytesIn"),
				bytesOut("BytesOut"),
				errorsIn("ErrorsIn"),
				errorsOut("ErrorsOut"),
				packetsIn("PacketsIn"),
				packetsOut("PacketsOut");

				public final String value;

				private Key(String value) {
					this.value = value;
				}
			}

			public static Stats from(String output) {
				var values = Maps.<String, Integer>of();
				var node = Parser.parse(output).child(0, 0);
				node.namedChildren()
					.forEach((name, n) -> n.parse().asInt().accept(i -> values.put(name, i)));
				return new Stats(Immutable.wrap(values));
			}

			public int value(Key key) {
				return value(key == null ? "" : key.value, 0);
			}

			public int value(String name, int def) {
				return values.getOrDefault(name, def);
			}

			public double packetErrorRateIn() {
				int packetsIn = value(Key.packetsIn);
				return packetsIn == 0 ? 0.0 : (double) value(Key.errorsIn) / packetsIn;
			}

			public double packetErrorRateOut() {
				int packetsOut = value(Key.packetsOut);
				return packetsOut == 0 ? 0.0 : (double) value(Key.errorsOut) / packetsOut;
			}
		}

		/**
		 * One line from scutil --nc list.
		 *
		 * <pre>
		 * * (state) id protocol --> device "name" [protocol:type]
		 * </pre>
		 */
		public record Item(boolean enabled, State state, String passwordHash, String protocol,
			String device, String name, String type) {

			public static final Item NULL = new Item(false, State.noService, "", "", "", "", "");
			private static final Pattern DECODE_REGEX = Pattern.compile(
				"(.) \\((.*)\\)\\s+(\\S+) (\\w+) --> (.*)\\s+\"(.*)\"\\s+\\[\\w+:(\\w+)\\]");
			private static final String ENABLED = "*";

			public static List<Item> fromList(String output) {
				return Regex.Split.LINE.stream(output).map(Item::from).nonNull().toList();
			}

			public static Item from(String line) {
				var m = Regex.find(DECODE_REGEX, line);
				if (!Regex.hasMatch(m)) return null;
				int i = 1;
				return new Item(ENABLED.equals(m.group(i++)), State.from(m.group(i++)),
					m.group(i++), m.group(i++), m.group(i++), m.group(i++), m.group(i++));
			}

			@Override
			public State state() {
				return Basics.def(state, State.unknown);
			}

			@Override
			public String toString() {
				if (state() == State.noService) return state().toString();
				return String.format("%s %-16s %s %s --> %-10s %-32s [%s:%s]",
					enabled() ? ENABLED : " ", state(), passwordHash(), protocol(), device(),
					"\"" + name() + "\"", protocol(), type());
			}
		}

		public record Show(Item item, Node<Void> data) {
			public static Show from(String output) {
				var split = Regex.Split.LINE.array(output, 2);
				var item = Item.from(split[0]);
				if (split.length == 1) return new Show(item, Node.of());
				return new Show(item, Parser.parse(split[1]));
			}

			@Override
			public String toString() {
				return item + Strings.EOL + data;
			}
		}

		Nc(Processor processor) {
			this.processor = processor;
		}

		public Output<List<Item>> list() throws IOException {
			return Output.of(exec(Parameters.of(LIST)), Item::fromList);
		}

		public Output<Status> status(String service) throws IOException {
			return Output.of(exec(Parameters.of(STATUS_COMMAND, service)), Status::from);
		}

		public Output<Show> show(String service) throws IOException {
			return Output.of(exec(Parameters.of(SHOW_COMMAND, service)), Show::from);
		}

		public Output<Stats> statistics(String service) throws IOException {
			return Output.of(exec(Parameters.of(STATISTICS_COMMAND, service)), Stats::from);
		}

		public String start(String service, String user, String password, String secret)
			throws IOException {
			var params = Parameters.of(START_COMMAND, service);
			if (user != null) params.add(USER_PARAM, user);
			if (password != null) params.add(PASSWORD_PARAM, password);
			if (secret != null) params.add(SECRET_PARAM, secret);
			return exec(params);
		}

		public String stop(String service) throws IOException {
			return exec(Parameters.of(STOP_COMMAND, service));
		}

		private String exec(Parameters params) throws IOException {
			return processor.exec(Parameters.of(SCUTIL, OPTION).addAll(params));
		}
	}
}
