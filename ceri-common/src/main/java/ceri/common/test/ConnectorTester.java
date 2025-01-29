package ceri.common.test;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import ceri.common.io.Connector;
import ceri.common.io.Fixable;
import ceri.common.reflect.ReflectUtil;

/**
 * Provides manual commands to test connectors.
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
		try (var m = manual(connectors).build()) {
			m.run();
		}
	}

	/**
	 * Initialize a ManualTester builder for a list of connectors. Fixable connectors will be
	 * opened, and listeners
	 */
	public static ManualTester.Builder manual(List<? extends Connector> connectors)
		throws IOException {
		ReflectUtil.acceptInstances(Fixable.class, Fixable::open, connectors);
		var b = ManualTester.builderList(connectors, Connector::name);
		b.preProcessor(Connector.class, (t, con) -> t.readBytes(con.in()));
		b.listen(connectors);
		b.command(Fixable.class, "O(s?)", (_, m, s) -> open(m, s),
			"O[s] = open the connector (s = silently)");
		b.command(Connector.class, "o(?s)(.*)", (t, m, s) -> t.writeAscii(s.out(), m.group(1)),
			"o... = write literal char bytes to output (e.g. \\xff for 0xff)");
		b.command(Connector.class, "C", (_, _, s) -> s.close(), "C = close the connector");
		b.command(Fixable.class, "z", (_, _, s) -> s.broken(), "z = mark connector as broken");
		b.command(TestConnector.class, "Z", (_, _, s) -> s.fixed(), "Z = fix the connector");
		return b;
	}

	private static void open(Matcher m, Fixable fixable) throws IOException {
		if (m.group(1) == "") fixable.open();
		else fixable.openSilently();
	}

}
