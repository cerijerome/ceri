package ceri.common.test;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import ceri.common.exception.ExceptionAdapter;
import ceri.common.net.HostPort;
import ceri.common.net.TcpSocket;
import ceri.common.net.TcpSocketOption;
import ceri.common.net.TcpSocketOptions;
import ceri.common.reflect.ReflectUtil;

/**
 * A connector for testing logic against serial connectors.
 */
public class TestTcpSocket extends TestConnector implements TcpSocket.Fixable {
	private static final String NAME = ReflectUtil.name(TestTcpSocket.class);
	public final CallSync.Supplier<HostPort> hostPort;
	public final CallSync.Supplier<Integer> localPort;
	public final CallSync.Runnable optionSync = CallSync.runnable(true);
	public final TcpSocketOptions.Mutable options = TcpSocketOptions.of(ConcurrentHashMap::new);

	/**
	 * Provide a test socket that echoes output to input.
	 */
	@SuppressWarnings("resource")
	public static TestTcpSocket ofEcho() {
		return TestConnector.echoOn(new TestTcpSocket(NAME + ":echo", HostPort.NULL, 0));
	}

	/**
	 * Provide a pair of test sockets that write to each other.
	 */
	@SuppressWarnings("resource")
	public static TestTcpSocket[] pairOf() {
		return TestConnector.chain(new TestTcpSocket(NAME + "[0->1]", HostPort.NULL, 0),
			new TestTcpSocket(NAME + "[1->0]", HostPort.NULL, 1));
	}

	public static TestTcpSocket of() {
		return of(HostPort.NULL, 0);
	}

	public static TestTcpSocket of(HostPort hostPort, int localPort) {
		return new TestTcpSocket(null, hostPort, localPort);
	}

	private TestTcpSocket(String name, HostPort hostPort, int localPort) {
		super(name);
		this.hostPort = CallSync.supplier(hostPort);
		this.localPort = CallSync.supplier(localPort);
	}

	@Override
	public void reset() {
		super.reset();
		CallSync.resetAll(hostPort, localPort, optionSync);
	}

	@Override
	public HostPort hostPort() {
		return hostPort.get();
	}

	@Override
	public int localPort() {
		return localPort.get();
	}

	@Override
	public <T> void option(TcpSocketOption<T> option, T value) throws IOException {
		options.set(option, value);
		optionSync.run(ExceptionAdapter.io);
	}

	@Override
	public <T> T option(TcpSocketOption<T> option) throws IOException {
		optionSync.run(ExceptionAdapter.io);
		return options.get(option);
	}
}
