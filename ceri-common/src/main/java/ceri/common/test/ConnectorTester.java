package ceri.common.test;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import ceri.common.io.Connector;

/**
 * A connector implementation for tests, using piped streams.
 */
public class ConnectorTester {

	private ConnectorTester() {}

	/**
	 * Run ManualTester for a list of connectors.
	 */
	public static void test(Connector... connectors) throws IOException {
		test(Arrays.asList(connectors));
	}

	/**
	 * Run ManualTester for a list of connectors.
	 */
	public static void test(List<? extends Connector> connectors) throws IOException {
		manual(connectors).build().run();
	}

	/**
	 * Initialize a ManualTester builder for a list of connectors.
	 */
	public static ManualTester.Builder manual(Connector... connectors) throws IOException {
		return manual(Arrays.asList(connectors));
	}

	/**
	 * Initialize a ManualTester builder for a list of connectors. Fixable connectors will be
	 * opened, and listeners
	 */
	public static ManualTester.Builder manual(List<? extends Connector> connectors)
		throws IOException {
		var events = ManualTester.eventCatcher();
		initConnectors(connectors, events);
		var b = ManualTester.builderList(connectors, Connector::name);
		b.preProcessor(Connector.class, (con, t) -> t.readBytes(con.in()));
		b.preProcessor(events);
		b.command(Connector.class, "o(.*)", (m, s, t) -> t.writeAscii(s.out(), m.group(1)),
			"o... = write literal char bytes to output (e.g. \\xff for 0xff)");
		b.command(Connector.class, "C", (m, s, t) -> s.close(),
			"C = close the connector");
		b.command(Connector.Fixable.class, "z", (m, s, t) -> s.broken(),
			"z = mark connector as broken");
		b.command(TestConnector.class, "Z", (m, s, t) -> s.fixed(), "Z = fix the connector");
		return b;
	}

	private static void initConnectors(List<? extends Connector> connectors,
		ManualTester.EventCatcher events) throws IOException {
		for (var connector : connectors) {
			if (connector instanceof Connector.Fixable fixable) {
				fixable.listeners().listen(e -> events.add(fixable.name() + " => " + e));
				fixable.open();
			}
		}
	}

}