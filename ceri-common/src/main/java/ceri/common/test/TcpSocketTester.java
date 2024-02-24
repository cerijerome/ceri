package ceri.common.test;

import static ceri.common.test.ManualTester.Parse.b;
import static ceri.common.test.ManualTester.Parse.i;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
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

	/**
	 * Test a socket whose peer echoes back all output.
	 */
	public static void testEcho() throws IOException {
		var events = ManualTester.EventCatcher.of(true);
		try (var ss = TcpServerSocket.of()) {
			listenAndEcho(ss, events);
			try (var s = TcpSocket.connect(ss.hostPort());
				var m = manual(s).preProcessor(events).build()) {
				m.run();
			}
		}
	}

	/**
	 * Test a socket and its current peer.
	 */
	public static void testPair() throws IOException {
		try (var pair1 = ReplaceableTcpSocket.of()) {
			try (var ss = TcpServerSocket.of()) {
				ss.listen(pair1::replace);
				try (var pair0 = TcpSocket.connect(ss.hostPort())) {
					test(pair0, pair1);
				}
			}
		}
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

	/**
	 * Manually test a list of sockets.
	 */
	public static void test(TcpSocket... sockets) throws IOException {
		test(Arrays.asList(sockets));
	}

	/**
	 * Manually test a list of sockets.
	 */
	public static void test(List<? extends TcpSocket> sockets) throws IOException {
		try (var m = manual(sockets).build()) {
			m.run();
		}
	}

	/**
	 * Initialize a ManualTester builder for a list of sockets.
	 */
	public static ManualTester.Builder manual(TcpSocket... sockets) throws IOException {
		return manual(Arrays.asList(sockets));
	}

	/**
	 * Initialize a ManualTester builder for a list of sockets.
	 */
	public static ManualTester.Builder manual(List<? extends TcpSocket> sockets)
		throws IOException {
		var b = ConnectorTester.manual(sockets);
		buildCommands(b);
		return b;
	}

	private static void buildCommands(ManualTester.Builder b) {
		b.command(TcpSocket.class, "O", (t, m, s) -> options(s, t), "O = show all options");
		b.command(TcpSocket.class, "Ot(\\d*)",
			(t, m, s) -> option(s, t, TcpSocketOption.soTimeout, i(m)),
			"Ot[N] = SO_TIMEOUT: timeout in milliseconds");
		b.command(TcpSocket.class, "Ol(\\-1|\\d*)",
			(t, m, s) -> option(s, t, TcpSocketOption.soLinger, i(m)),
			"Ol[N] = SO_LINGER: linger-on-close in seconds; -1 to disable");
		b.command(TcpSocket.class, "Oc(\\d*)",
			(t, m, s) -> option(s, t, TcpSocketOption.ipTos, i(m)),
			"Oc[N] = IP_TOS: traffic class");
		b.command(TcpSocket.class, "Os(\\d*)",
			(t, m, s) -> option(s, t, TcpSocketOption.soSndBuf, i(m)),
			"Os[N] = SO_SNDBUF: send buffer size");
		b.command(TcpSocket.class, "Or(\\d*)",
			(t, m, s) -> option(s, t, TcpSocketOption.soRcvBuf, i(m)),
			"Or[N] = SO_RCVBUF: receive buffer size");
		b.command(TcpSocket.class, "Ok(0|1|)",
			(t, m, s) -> option(s, t, TcpSocketOption.soKeepAlive, b(m)),
			"Ok[0|1] = SO_KEEPALIVE: keep-alive off/on");
		b.command(TcpSocket.class, "Oa(0|1|)",
			(t, m, s) -> option(s, t, TcpSocketOption.soReuseAddr, b(m)),
			"Oa[0|1] = SO_REUSEADDR: re-use address off/on");
		b.command(TcpSocket.class, "Ou(0|1|)",
			(t, m, s) -> option(s, t, TcpSocketOption.soOobInline, b(m)),
			"Ou[0|1] = SO_OOBINLINE: inline urgent data off/on");
		b.command(TcpSocket.class, "Od(0|1|)",
			(t, m, s) -> option(s, t, TcpSocketOption.tcpNoDelay, b(m)),
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
}
