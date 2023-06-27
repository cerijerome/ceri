package ceri.common.test;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
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

	public static void testEcho() throws IOException {
		try (var ss = TcpServerSocket.of()) {
			ss.listen(Connector::echo);
			try (var s = TcpSocket.connect(ss.hostPort())) {
				test(s);
			}
		}
	}

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

	public static void test(TcpSocket... sockets) throws IOException {
		test(Arrays.asList(sockets));
	}

	public static void test(List<? extends TcpSocket> sockets) throws IOException {
		TestConnector.manual(sockets, TcpSocketTester::buildCommands);
	}

	private static void buildCommands(ManualTester.Builder b) {
		b.command(TcpSocket.class, "Oti(\\d*)",
			(m, s, t) -> option(s, t, TcpSocketOption.soTimeout, i(m)),
			"Oti[N] = SO_TIMEOUT: timeout in milliseconds");
		b.command(TcpSocket.class, "Oli(\\-1|\\d*)",
			(m, s, t) -> option(s, t, TcpSocketOption.soLinger, i(m)),
			"Oli[N] = SO_LINGER: linger-on-close in seconds, -1 to disable");
		b.command(TcpSocket.class, "Otc(\\d*)",
			(m, s, t) -> option(s, t, TcpSocketOption.ipTos, i(m)),
			"Otc[N] = IP_TOS: traffic class");
		b.command(TcpSocket.class, "Osb(\\d*)",
			(m, s, t) -> option(s, t, TcpSocketOption.soSndBuf, i(m)),
			"Osb[N] = SO_SNDBUF: send buffer size");
		b.command(TcpSocket.class, "Orb(\\d*)",
			(m, s, t) -> option(s, t, TcpSocketOption.soRcvBuf, i(m)),
			"Orb[N] = SO_RCVBUF: receive buffer size");
		b.command(TcpSocket.class, "Oka(0|1|)",
			(m, s, t) -> option(s, t, TcpSocketOption.soKeepAlive, b(m)),
			"Oka[0|1] = SO_KEEPALIVE: keep-alive off/on");
		b.command(TcpSocket.class, "Ora(0|1|)",
			(m, s, t) -> option(s, t, TcpSocketOption.soReuseAddr, b(m)),
			"Ora[0|1] = SO_REUSEADDR: re-use address off/on");
		b.command(TcpSocket.class, "Ooi(0|1|)",
			(m, s, t) -> option(s, t, TcpSocketOption.soOobInline, b(m)),
			"Ooi[0|1] = SO_OOBINLINE: inline urgent data inline off/on");
		b.command(TcpSocket.class, "Ond(0|1|)",
			(m, s, t) -> option(s, t, TcpSocketOption.tcpNoDelay, b(m)),
			"Ond[0|1] = TCP_NODELAY: no-delay off/on");
	}

	private static <T> void option(TcpSocket socket, ManualTester tester, TcpSocketOption<T> option,
		T value) throws IOException {
		if (value != null) socket.option(option, value);
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
