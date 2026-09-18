package ceri.common.net;

import java.io.IOException;
import org.junit.Test;
import ceri.common.test.Assert;
import ceri.common.test.TestIo;

public class ReplaceableTcpSocketBehavior {

	@SuppressWarnings("resource")
	@Test
	public void shouldApplySocketOption() throws IOException {
		var s = TestIo.tcpSocket();
		var r = ReplaceableTcpSocket.of();
		r.set(s);
		r.option(TcpSocketOption.soReuseAddr, true);
		Assert.equal(r.option(TcpSocketOption.soReuseAddr), true);
	}
}
