package ceri.log.net;

import java.io.IOException;
import ceri.common.net.HostPort;
import ceri.common.net.ReplaceableTcpSocket;
import ceri.common.net.TcpServerSocket;
import ceri.common.test.ManualTester;
import ceri.common.test.TcpSocketTester;

public class SelfHealingTcpSocketTester {

	public static void main(String[] args) throws IOException {
		testPair();
	}

	public static void testEcho() throws IOException {
		var events = ManualTester.eventCatcher(true);
		try (var ss = TcpServerSocket.of()) {
			TcpSocketTester.listenAndEcho(ss, events);
			var config = SelfHealingTcpSocket.Config.of(HostPort.localhost(ss.port()));
			try (var s = SelfHealingTcpSocket.of(config);
				var m = TcpSocketTester.manual(s).preProcessor(events).build()) {
				m.run();
			}
		}
	}

	public static void testPair() throws IOException {
		try (var pair1 = ReplaceableTcpSocket.of()) {
			try (var ss = TcpServerSocket.of()) {
				ss.listen(pair1::replace);
				var config = SelfHealingTcpSocket.Config.of(HostPort.localhost(ss.port()));
				try (var pair0 = SelfHealingTcpSocket.of(config)) {
					TcpSocketTester.test(pair0, pair1);
				}
			}
		}
	}

}
