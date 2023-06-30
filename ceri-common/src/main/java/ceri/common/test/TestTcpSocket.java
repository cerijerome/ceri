package ceri.common.test;

import java.io.IOException;
import java.util.List;
import ceri.common.net.HostPort;
import ceri.common.net.TcpSocket;
import ceri.common.net.TcpSocketOption;
import ceri.common.reflect.ReflectUtil;
import ceri.common.util.BasicUtil;

/**
 * A connector for testing logic against serial connectors.
 */
public class TestTcpSocket extends TestConnector implements TcpSocket.Fixable {
	private static final String NAME = ReflectUtil.name(TestTcpSocket.class);
	public final CallSync.Supplier<HostPort> hostPort = CallSync.supplier(HostPort.NULL);
	public final CallSync.Supplier<Integer> localPort = CallSync.supplier(0);
	// List<?> = TcpSocketOption<T>, T
	public final CallSync.Consumer<List<?>> optionSet = CallSync.consumer(List.of(), true);
	public final CallSync.Function<TcpSocketOption<Object>, Object> optionGet =
		CallSync.function(null, (Object) null);

	/**
	 * Provide a test socket that echoes output to input.
	 */
	@SuppressWarnings("resource")
	public static TestTcpSocket ofEcho() {
		return TestConnector.echoOn(new TestTcpSocket(NAME + ":echo"));
	}

	/**
	 * Provide a pair of test sockets that write to each other.
	 */
	@SuppressWarnings("resource")
	public static TestTcpSocket[] pairOf() {
		return TestConnector.chain(new TestTcpSocket(NAME + "[0->1]"),
			new TestTcpSocket(NAME + "[1->0]"));
	}

	public static TestTcpSocket of() {
		return new TestTcpSocket(null);
	}

	private TestTcpSocket(String name) {
		super(name);
	}

	@Override
	public void reset() {
		super.reset();
		CallSync.resetAll(hostPort);
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
		optionSet.accept(List.of(option, value));
	}

	@Override
	public <T> T option(TcpSocketOption<T> option) throws IOException {
		return BasicUtil.uncheckedCast(optionGet.apply(BasicUtil.uncheckedCast(option)));
	}
}
