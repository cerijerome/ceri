package ceri.log.net;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import ceri.common.net.HostPort;
import ceri.common.net.TcpSocket;
import ceri.common.net.TcpSocketOption;
import ceri.common.net.TcpSocketOptions;
import ceri.common.text.ToString;
import ceri.log.io.SelfHealingConnector;
import ceri.log.util.LogUtil;

/**
 * A self-healing TCP socket connector. It will automatically reconnect if the connection is broken.
 */
public class SelfHealingTcpSocket extends SelfHealingConnector<TcpSocket>
	implements TcpSocket.Fixable {
	private final SelfHealingTcpSocketConfig config;
	private final TcpSocketOptions.Mutable options = TcpSocketOptions.of(ConcurrentHashMap::new);

	public static SelfHealingTcpSocket of(SelfHealingTcpSocketConfig config) {
		return new SelfHealingTcpSocket(config);
	}

	private SelfHealingTcpSocket(SelfHealingTcpSocketConfig config) {
		super(config.selfHealing);
		this.config = config;
		options.set(config.options);
	}

	@Override
	public HostPort hostPort() {
		return config.hostPort;
	}

	@Override
	public int localPort() {
		return device.applyIfSet(TcpSocket::localPort, HostPort.INVALID_PORT);
	}

	@Override
	public void options(TcpSocketOptions options) throws IOException {
		this.options.set(options);
		TcpSocket.Fixable.super.options(options);
	}

	@Override
	public <T> void option(TcpSocketOption<T> option, T value) throws IOException {
		options.set(option, value);
		device.acceptIfSet(socket -> socket.option(option, value));
	}

	@Override
	public <T> T option(TcpSocketOption<T> option) throws IOException {
		return device.applyIfSet(socket -> socket.option(option), null);
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
			options.applyAll(socket);
			return socket;
		} catch (RuntimeException | IOException e) {
			LogUtil.close(socket);
			throw e;
		}
	}
}
