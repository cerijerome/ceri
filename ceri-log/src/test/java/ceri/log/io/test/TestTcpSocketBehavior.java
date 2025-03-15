package ceri.log.io.test;

import static ceri.common.test.AssertUtil.assertAscii;
import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertIoe;
import static ceri.common.test.ErrorGen.IOX;
import java.io.IOException;
import org.junit.Test;
import ceri.common.data.ByteStream;
import ceri.common.net.HostPort;
import ceri.common.test.TestTcpSocket;

public class TestTcpSocketBehavior {

	@SuppressWarnings("resource")
	@Test
	public void shouldEcho() throws IOException {
		try (var s = TestTcpSocket.ofEcho()) {
			s.open();
			var in = ByteStream.reader(s.in());
			var out = ByteStream.writer(s.out());
			out.writeAscii("test");
			assertAscii(in, "test");
		}
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldProvideConnector() throws IOException {
		try (var s = TestTcpSocket.of()) {
			assertEquals(s.hostPort(), HostPort.NULL);
			s.in.read.error.setFrom(IOX);
			s.in.to.writeBytes(0);
			assertIoe(s.in()::read);
		}
	}

}
