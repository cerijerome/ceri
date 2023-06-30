package ceri.common.test;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Future;
import java.util.regex.Matcher;
import ceri.common.io.Connector;
import ceri.common.net.ReplaceableTcpSocket;
import ceri.common.net.TcpServerSocket;
import ceri.common.net.TcpSocket;
import ceri.common.net.TcpSocketOption;

/**
 * Utilities to manually test serial ports.
 */
public class TcpSocketTester {

	private TcpSocketTester() {}

	public static void main(String[] args) throws IOException {
		testEcho();
	}

	public static void testEcho() throws IOException {
		var events = ManualTester.eventCatcher(true);
		try (var ss = TcpServerSocket.of()) {
			listenAndEcho(ss, events);
			try (var s = TcpSocket.connect(ss.hostPort())) {
				manual(s).preProcessor(events).build().run();
			}
		}
	}

	public static void testPair() throws IOException {
		try (var pair1 = ReplaceableTcpSocket.of()) {
			try (var ss = TcpServerSocket.of()) {
				ss.listen(pair1::replace);
				try (var pair0 = TcpSocket.connect(ss.hostPort())) {
					manual(pair0, pair1).build().run();
				}
			}
		}
	}

	/**
	 * Start the server socket listener, and echo input to output for each new socket. Any exception
	 * will stop the socket listener, and will be available on the returned future.
	 */
	public static Future<?> listenAndEcho(TcpServerSocket ss) {
		return ss.listenAndClose(Connector::echo);
	}

	/**
	 * Start the server socket listener, and echo input to output for each new socket. Events are
	 * captured for open, close and exceptions, for use with ManualTester.
	 */
	public static void listenAndEcho(TcpServerSocket ss, ManualTester.EventCatcher events) {
		ss.listenAndClose(socket -> {
			events.add(socket.name() + " open");
			events.execute(() -> Connector.echo(socket));
			events.add(socket.name() + " closed");
		});
	}

	public static void test(TcpSocket... sockets) throws IOException {
		test(Arrays.asList(sockets));
	}

	public static void test(List<? extends TcpSocket> sockets) throws IOException {
		manual(sockets).build().run();
	}

	/**
	 * Initialize a ManualTester builder for a list of connectors.
	 */
	public static ManualTester.Builder manual(TcpSocket... sockets) throws IOException {
		return manual(Arrays.asList(sockets));
	}

	/**
	 * Initialize a ManualTester builder for a list of connectors.
	 */
	public static ManualTester.Builder manual(List<? extends TcpSocket> sockets)
		throws IOException {
		var b = ConnectorTester.manual(sockets);
		buildCommands(b);
		return b;
	}

	private static void buildCommands(ManualTester.Builder b) {
		b.command(TcpSocket.class, "O", (m, s, t) -> options(s, t), "O = show all options");
		b.command(TcpSocket.class, "Ot(\\d*)",
			(m, s, t) -> option(s, t, TcpSocketOption.soTimeout, i(m)),
			"Ot[N] = SO_TIMEOUT: timeout in milliseconds");
		b.command(TcpSocket.class, "Ol(\\-1|\\d*)",
			(m, s, t) -> option(s, t, TcpSocketOption.soLinger, i(m)),
			"Ol[N] = SO_LINGER: linger-on-close in seconds; -1 to disable");
		b.command(TcpSocket.class, "Oc(\\d*)",
			(m, s, t) -> option(s, t, TcpSocketOption.ipTos, i(m)),
			"Oc[N] = IP_TOS: traffic class");
		b.command(TcpSocket.class, "Os(\\d*)",
			(m, s, t) -> option(s, t, TcpSocketOption.soSndBuf, i(m)),
			"Os[N] = SO_SNDBUF: send buffer size");
		b.command(TcpSocket.class, "Or(\\d*)",
			(m, s, t) -> option(s, t, TcpSocketOption.soRcvBuf, i(m)),
			"Or[N] = SO_RCVBUF: receive buffer size");
		b.command(TcpSocket.class, "Ok(0|1|)",
			(m, s, t) -> option(s, t, TcpSocketOption.soKeepAlive, b(m)),
			"Ok[0|1] = SO_KEEPALIVE: keep-alive off/on");
		b.command(TcpSocket.class, "Oa(0|1|)",
			(m, s, t) -> option(s, t, TcpSocketOption.soReuseAddr, b(m)),
			"Oa[0|1] = SO_REUSEADDR: re-use address off/on");
		b.command(TcpSocket.class, "Ou(0|1|)",
			(m, s, t) -> option(s, t, TcpSocketOption.soOobInline, b(m)),
			"Ou[0|1] = SO_OOBINLINE: inline urgent data off/on");
		b.command(TcpSocket.class, "Od(0|1|)",
			(m, s, t) -> option(s, t, TcpSocketOption.tcpNoDelay, b(m)),
			"Od[0|1] = TCP_NODELAY: no-delay off/on");
	}

	private static <T> void option(TcpSocket socket, ManualTester tester, TcpSocketOption<T> option,
		T value) throws IOException {
		if (value != null) socket.option(option, value);
		tester.out(option + " = " + socket.option(option));
	}

	private static void options(TcpSocket socket, ManualTester tester) throws IOException {
		for (var option : TcpSocketOption.all)
			tester.out(option + " = " + socket.option(option));
	}

	private static Boolean b(Matcher m) {
		String s = m.group(1);
		if (s.isEmpty()) return null;
		return s.charAt(0) == '1';
	}

	private static Integer i(Matcher m) {
		String s = m.group(1);
		if (s.isEmpty()) return null;
		return Integer.parseInt(s);
	}

}
