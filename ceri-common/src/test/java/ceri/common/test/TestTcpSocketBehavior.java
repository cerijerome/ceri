package ceri.common.test;

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
		Assert.read(s.in(), 1, 2, 3);
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldProvideSocketPair() throws IOException {
		var ss = TestTcpSocket.pairOf();
		ss[0].open();
		ss[1].open();
		ss[0].out().write(ArrayUtil.bytes.of(1, 2, 3));
		ss[1].out().write(ArrayUtil.bytes.of(4, 5, 6));
		Assert.read(ss[0].in(), 4, 5, 6);
		Assert.read(ss[1].in(), 1, 2, 3);
	}

	@Test
	public void shouldProvideHostPort() throws IOException {
		try (var s = TestTcpSocket.of(HostPort.of("test", 123), 456)) {
			Assert.equal(s.hostPort(), HostPort.of("test", 123));
			s.hostPort.autoResponses(HostPort.LOCALHOST);
			Assert.equal(s.hostPort(), HostPort.LOCALHOST);
			s.reset();
			Assert.equal(s.hostPort(), HostPort.of("test", 123));
		}
	}

	@Test
	public void shouldProvideLocalPort() throws IOException {
		try (var s = TestTcpSocket.of(HostPort.of("test", 123), 456)) {
			Assert.equal(s.localPort(), 456);
			s.localPort.autoResponses(123);
			Assert.equal(s.localPort(), 123);
			s.reset();
			Assert.equal(s.localPort(), 456);
		}
	}

}
