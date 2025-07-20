package ceri.common.test;

import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertRead;
import java.io.IOException;
import org.junit.Test;
import ceri.common.array.ArrayUtil;
import ceri.common.net.HostPort;

public class TestTcpSocketBehavior {

	@SuppressWarnings("resource")
	@Test
	public void shouldProvideAnEchoSocket() throws IOException {
		var s = TestTcpSocket.ofEcho();
		s.open();
		s.out().write(ArrayUtil.bytes.of(1, 2, 3));
		assertRead(s.in(), 1, 2, 3);
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldProvideSocketPair() throws IOException {
		var ss = TestTcpSocket.pairOf();
		ss[0].open();
		ss[1].open();
		ss[0].out().write(ArrayUtil.bytes.of(1, 2, 3));
		ss[1].out().write(ArrayUtil.bytes.of(4, 5, 6));
		assertRead(ss[0].in(), 4, 5, 6);
		assertRead(ss[1].in(), 1, 2, 3);
	}

	@Test
	public void shouldProvideHostPort() throws IOException {
		try (var s = TestTcpSocket.of(HostPort.of("test", 123), 456)) {
			assertEquals(s.hostPort(), HostPort.of("test", 123));
			s.hostPort.autoResponses(HostPort.LOCALHOST);
			assertEquals(s.hostPort(), HostPort.LOCALHOST);
			s.reset();
			assertEquals(s.hostPort(), HostPort.of("test", 123));
		}
	}

	@Test
	public void shouldProvideLocalPort() throws IOException {
		try (var s = TestTcpSocket.of(HostPort.of("test", 123), 456)) {
			assertEquals(s.localPort(), 456);
			s.localPort.autoResponses(123);
			assertEquals(s.localPort(), 123);
			s.reset();
			assertEquals(s.localPort(), 456);
		}
	}

}
