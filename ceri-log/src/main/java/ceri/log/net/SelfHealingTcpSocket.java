package ceri.log.io;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import ceri.common.collection.CollectionUtil;
import ceri.common.function.FunctionUtil;
import ceri.common.io.Connector;
import ceri.common.net.HostPort;
import ceri.common.net.TcpServerSocket;
import ceri.common.net.TcpSocket;
import ceri.common.net.TcpSocketOption;
import ceri.common.test.TcpSocketTester;
import ceri.common.text.ToString;
import ceri.log.util.LogUtil;

/**
 * A self-healing TCP socket connector. It will automatically reconnect if the connection is broken.
 */
public class SelfHealingTcpSocket extends SelfHealingConnector<TcpSocket>
	implements TcpSocket.Fixable {
	private final SelfHealingTcpSocketConfig config;
	private final Map<TcpSocketOption<Object>, Object> options = new ConcurrentHashMap<>();

	public static void main(String[] args) throws IOException {
		try (var ss = TcpServerSocket.of()) {
			ss.listen(Connector::echo);
			var config = SelfHealingTcpSocketConfig.of("localhost", ss.port());
			try (var s = SelfHealingTcpSocket.of(config)) {
				TcpSocketTester.test(s);
			}
		}
	}
	
	public static SelfHealingTcpSocket of(SelfHealingTcpSocketConfig config) {
		return new SelfHealingTcpSocket(config);
	}

	private SelfHealingTcpSocket(SelfHealingTcpSocketConfig config) {
		super(config.selfHealing);
		this.config = config;
		options.putAll(config.options);
	}

	@Override
	public HostPort hostPort() {
		return config.hostPort;
	}

	@SuppressWarnings("resource")
	@Override
	public int localPort() {
		return FunctionUtil.safeApply(connector(), TcpSocket::localPort, -1);
	}

	@Override
	public <T> void option(TcpSocketOption<T> option, T value) throws IOException {
		acceptConnector(socket -> socket.option(option, value));
	}

	@Override
	public <T> T option(TcpSocketOption<T> option) throws IOException {
		return applyConnector(socket -> socket.option(option));
	}

	@Override
	public String toString() {
		return ToString.forClass(this, config.hostPort, localPort(), options, config.selfHealing);
	}

	@Override
	protected TcpSocket openConnector() throws IOException {
		TcpSocket socket = null;
		try {
			socket = config.openSocket();
			CollectionUtil.forEach(options, socket::option);
			return socket;
		} catch (RuntimeException | IOException e) {
			LogUtil.close(logger, socket);
			throw e;
		}
	}

}
