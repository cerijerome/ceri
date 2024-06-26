package ceri.common.net;

import static ceri.common.test.AssertUtil.assertEquals;
import java.io.IOException;
import org.junit.Test;
import ceri.common.test.TestTcpSocket;

public class ReplaceableTcpSocketBehavior {

	@SuppressWarnings("resource")
	@Test
	public void shouldApplySocketOption() throws IOException {
		var s = TestTcpSocket.of();
		var r = ReplaceableTcpSocket.of();
		r.set(s);
		r.option(TcpSocketOption.soReuseAddr, true);
		assertEquals(r.option(TcpSocketOption.soReuseAddr), true);
	}

}
