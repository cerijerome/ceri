package ceri.common.net;

import java.io.IOException;
import org.junit.Test;
import ceri.common.concurrent.Concurrent;
import ceri.common.test.Assert;
import ceri.common.test.CallSync;
import ceri.common.test.ErrorGen;

public class TcpServerSocketBehavior {

	@Test
	public void shouldStopListeningOnErrors() throws IOException {
		var c = CallSync.consumer(null, true);
		c.error.setFrom(ErrorGen.IOX);
		try (var ss = TcpServerSocket.of()) {
			var future = ss.listen(c::accept);
			try (var _ = TcpSocket.connect(HostPort.localhost(ss.port()))) {
				Assert.thrown(future::get);
			}
		}
	}

	@Test
	public void shouldStopListeningOnInterrupt() throws IOException {
		try (var ss = TcpServerSocket.of()) {
			Concurrent.interrupt();
			ss.listenAndClose(_ -> {});
		}
	}

	@Test
	public void shouldNotListenIfClosed() throws IOException {
		try (var ss = TcpServerSocket.of()) {
			ss.close();
			ss.listenAndClose(_ -> {});
		}
	}
}
