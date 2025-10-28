package ceri.common.net;

import java.io.IOException;
import org.junit.Test;
import ceri.common.test.Assert;
import ceri.common.test.TestTcpSocket;
import ceri.common.test.Testing;

public class TcpSocketOptionsBehavior {

	@Test
	public void shouldNotBreachEqualsContract() {
		var t = TcpSocketOptions.of().set(TcpSocketOption.ipTos, 123)
			.set(TcpSocketOption.soReuseAddr, false);
		var eq0 = TcpSocketOptions.of().set(TcpSocketOption.ipTos, 123)
			.set(TcpSocketOption.soReuseAddr, false);
		var eq1 = t.immutable();
		TcpSocketOptions ne0 = TcpSocketOptions.of().set(TcpSocketOption.ipTos, 123);
		Testing.exerciseEquals(t);
		Assert.equal(t, eq0);
		Assert.equal(t, eq1);
		Assert.notEqualAll(t, ne0);
	}

	@Test
	public void shouldCreateFromSocket() throws IOException {
		try (TestTcpSocket s = TestTcpSocket.of()) {
			s.option(TcpSocketOption.soKeepAlive, true);
			s.option(TcpSocketOption.soLinger, 123);
			var options = TcpSocketOptions.from(s);
			Assert.unordered(options.options(), TcpSocketOption.soKeepAlive,
				TcpSocketOption.soLinger);
			Assert.yes(options.has(TcpSocketOption.soKeepAlive));
			Assert.yes(options.has(TcpSocketOption.soLinger));
			Assert.no(options.has(TcpSocketOption.soTimeout));
			Assert.equal(options.get(TcpSocketOption.soKeepAlive), true);
			Assert.equal(options.get(TcpSocketOption.soLinger), 123);
		}
	}

	@Test
	public void shouldApplyOptions() throws IOException {
		try (TestTcpSocket s = TestTcpSocket.of()) {
			var options = TcpSocketOptions.of().set(TcpSocketOption.ipTos, 123)
				.set(TcpSocketOptions.of().set(TcpSocketOption.soReuseAddr, false)).immutable();
			options.applyAll(s);
			options.apply(TcpSocketOption.tcpNoDelay, s); // no value
			Assert.equal(s.options(), options);
		}
	}

}
