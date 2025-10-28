package ceri.log.io.test;

import static ceri.common.test.ErrorGen.IOX;
import java.io.IOException;
import org.junit.After;
import org.junit.Test;
import ceri.common.data.ByteStream;
import ceri.common.net.HostPort;
import ceri.common.test.Assert;
import ceri.common.test.TestTcpSocket;
import ceri.common.test.Testing;

public class TestTcpSocketBehavior {
	private TestTcpSocket s;

	@After
	public void after() {
		s = Testing.close(s);
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldEcho() throws IOException {
		s = TestTcpSocket.ofEcho();
		s.open();
		var in = ByteStream.reader(s.in());
		var out = ByteStream.writer(s.out());
		out.writeAscii("test");
		Assert.ascii(in, "test");
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldProvideConnector() {
		s = TestTcpSocket.of();
		Assert.equal(s.hostPort(), HostPort.NULL);
		s.in.read.error.setFrom(IOX);
		s.in.to.writeBytes(0);
		Assert.io(s.in()::read);
	}
}
