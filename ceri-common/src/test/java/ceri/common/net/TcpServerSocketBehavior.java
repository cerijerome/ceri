package ceri.common.net;

import static ceri.common.test.AssertUtil.assertThrown;
import static ceri.common.test.ErrorGen.IOX;
import org.junit.Test;
import ceri.common.concurrent.ConcurrentUtil;
import ceri.common.test.CallSync;

public class TcpServerSocketBehavior {

	@Test
	public void shouldStopListeningOnErrors() throws Exception {
		var c = CallSync.consumer(null, true);
		c.error.setFrom(IOX);
		try (var ss = TcpServerSocket.of()) {
			var future = ss.listen(c::accept);
			try (var socket = TcpSocket.connect(HostPort.localhost(ss.port()))) {
				assertThrown(future::get);
			}
		}
	}

	@Test
	public void shouldStopListeningOnInterrupt() throws Exception {
		try (var ss = TcpServerSocket.of()) {
			ConcurrentUtil.interrupt();
			ss.listenAndClose(s -> {});
		}
	}

}
