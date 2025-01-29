package ceri.common.net;

import static ceri.common.test.AssertUtil.assertThrown;
import static ceri.common.test.ErrorGen.IOX;
import java.io.IOException;
import org.junit.Test;
import ceri.common.concurrent.ConcurrentUtil;
import ceri.common.test.CallSync;

public class TcpServerSocketBehavior {

	@Test
	public void shouldStopListeningOnErrors() throws IOException {
		var c = CallSync.consumer(null, true);
		c.error.setFrom(IOX);
		try (var ss = TcpServerSocket.of()) {
			var future = ss.listen(c::accept);
			try (var _ = TcpSocket.connect(HostPort.localhost(ss.port()))) {
				assertThrown(future::get);
			}
		}
	}

	@Test
	public void shouldStopListeningOnInterrupt() throws IOException {
		try (var ss = TcpServerSocket.of()) {
			ConcurrentUtil.interrupt();
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
