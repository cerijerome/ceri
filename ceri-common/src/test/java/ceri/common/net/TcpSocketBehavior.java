package ceri.common.net;

import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import ceri.common.test.Assert;
import ceri.common.test.TestIo;
import ceri.common.test.Testing;

public class TcpSocketBehavior {
	private TestIo.Socket jdkSocket;
	private TestIo.TcpSocket socket;

	@AfterEach
	public void after() {
		jdkSocket = Testing.close(jdkSocket);
		socket = Testing.close(socket);
	}

	@Test
	public void shouldDisableSocketOption() throws IOException {
		jdkSocket = TestIo.socket();
		TcpSocket.Option.soLinger.disable(jdkSocket);
		Assert.equal(jdkSocket.getSoLinger(), -1);
		TcpSocket.Option.soSndBuf.disable(jdkSocket); // does nothing, not supported
		Assert.notEqual(jdkSocket.getSendBufferSize(), 0);
	}

	@Test
	public void shouldNotBreachOptionsEqualsContract() {
		var t = TcpSocket.Options.of().set(TcpSocket.Option.ipTos, 123)
			.set(TcpSocket.Option.soReuseAddr, false);
		var eq0 = TcpSocket.Options.of().set(TcpSocket.Option.ipTos, 123)
			.set(TcpSocket.Option.soReuseAddr, false);
		var eq1 = t.immutable();
		var ne0 = TcpSocket.Options.of().set(TcpSocket.Option.ipTos, 123);
		Testing.exerciseEquals(t);
		Assert.equal(t, eq0);
		Assert.equal(t, eq1);
		Assert.notEqualAll(t, ne0);
	}

	@Test
	public void shouldCreateFromSocket() throws IOException {
		socket = TestIo.tcpSocket();
		socket.option(TcpSocket.Option.soKeepAlive, true);
		socket.option(TcpSocket.Option.soLinger, 123);
		var options = TcpSocket.Options.of().add(socket);
		Assert.unordered(options.options(), TcpSocket.Option.soKeepAlive, TcpSocket.Option.soLinger);
		Assert.yes(options.has(TcpSocket.Option.soKeepAlive));
		Assert.yes(options.has(TcpSocket.Option.soLinger));
		Assert.no(options.has(TcpSocket.Option.soTimeout));
		Assert.equal(options.get(TcpSocket.Option.soKeepAlive), true);
		Assert.equal(options.get(TcpSocket.Option.soLinger), 123);
	}

	@Test
	public void shouldApplyOptions() throws IOException {
		socket = TestIo.tcpSocket();
		var options = TcpSocket.Options.of().set(TcpSocket.Option.ipTos, 123)
			.set(TcpSocket.Options.of().set(TcpSocket.Option.soReuseAddr, false)).immutable();
		options.applyAll(socket);
		options.apply(TcpSocket.Option.tcpNoDelay, socket); // no value
		Assert.equal(socket.options(), options);
	}
	
	@Test
	public void shouldProvideNoOpImplementation() throws IOException {
		Assert.equal(TcpSocket.NULL.hostPort(), HostPort.NULL);
		Assert.equal(TcpSocket.NULL.localPort(), 0);
		TcpSocket.NULL.option(TcpSocket.Option.soLinger, 100);
		Assert.equal(TcpSocket.NULL.option(TcpSocket.Option.soLinger), -1);
		Assert.equal(TcpSocket.NULL.option(TcpSocket.Option.soSndBuf), 0);
	}

	@Test
	public void shouldFailToWrapBadSocket() throws IOException {
		try (Socket s = new Socket()) {
			Assert.thrown(() -> TcpSocket.wrap(s)); // not connected
		}
		Assert.thrown(() -> TcpSocket.connect(HostPort.LOCALHOST)); // port out of range -1
	}

	@Test
	public void shouldDetermineIfSocketIsBroken() {
		Assert.no(TcpSocket.isBroken(null));
		Assert.no(TcpSocket.isBroken(new IOException()));
		Assert.yes(TcpSocket.isBroken(new SocketException()));
	}

	@Test
	public void shouldApplyOptionsFromSocket() throws IOException {
		try (var s = TestIo.tcpSocket()) {
			s.options(TcpSocket.Options.of().set(TcpSocket.Option.tcpNoDelay, true));
			Assert.equal(s.option(TcpSocket.Option.tcpNoDelay), true);
		}
	}
	
	@SuppressWarnings("resource")
	@Test
	public void shouldApplySocketOption() throws IOException {
		var s = TestIo.tcpSocket();
		var r = TcpSocket.replaceable();
		r.set(s);
		r.option(TcpSocket.Option.soReuseAddr, true);
		Assert.equal(r.option(TcpSocket.Option.soReuseAddr), true);
	}
}
