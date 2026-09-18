package ceri.common.net;

import java.io.IOException;
import org.junit.After;
import org.junit.Test;
import ceri.common.test.Assert;
import ceri.common.test.TestIo;
import ceri.common.test.Testing;

public class TcpSocketOptionsBehavior {
	private TestIo.TcpSocket socket;

	@After
	public void after() {
		socket = Testing.close(socket);
	}

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
		socket = TestIo.tcpSocket();
		socket.option(TcpSocketOption.soKeepAlive, true);
		socket.option(TcpSocketOption.soLinger, 123);
		var options = TcpSocketOptions.from(socket);
		Assert.unordered(options.options(), TcpSocketOption.soKeepAlive, TcpSocketOption.soLinger);
		Assert.yes(options.has(TcpSocketOption.soKeepAlive));
		Assert.yes(options.has(TcpSocketOption.soLinger));
		Assert.no(options.has(TcpSocketOption.soTimeout));
		Assert.equal(options.get(TcpSocketOption.soKeepAlive), true);
		Assert.equal(options.get(TcpSocketOption.soLinger), 123);
	}

	@Test
	public void shouldApplyOptions() throws IOException {
		socket = TestIo.tcpSocket();
		var options = TcpSocketOptions.of().set(TcpSocketOption.ipTos, 123)
			.set(TcpSocketOptions.of().set(TcpSocketOption.soReuseAddr, false)).immutable();
		options.applyAll(socket);
		options.apply(TcpSocketOption.tcpNoDelay, socket); // no value
		Assert.equal(socket.options(), options);
	}

}
