package ceri.log.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ceri.common.concurrent.BooleanCondition;
import ceri.common.concurrent.ConcurrentUtil;
import ceri.common.concurrent.RuntimeInterruptedException;
import ceri.common.event.Listenable;
import ceri.common.event.Listeners;
import ceri.common.exception.ExceptionTracker;
import ceri.common.io.ReplaceableInputStream;
import ceri.common.io.ReplaceableOutputStream;
import ceri.common.io.StateChange;
import ceri.common.net.HostPort;
import ceri.common.text.ToString;
import ceri.log.concurrent.LoopingExecutor;
import ceri.log.util.LogUtil;

/**
 * A self-healing TCP socket connector. It will automatically reconnect if the connection is broken.
 */
public class SelfHealingSocketConnector extends LoopingExecutor implements SocketConnector {
	private static final Logger logger = LogManager.getLogger();
	private final SelfHealingSocketConfig config;
	private final Listeners<StateChange> listeners = Listeners.of();
	private final ReplaceableInputStream in = new ReplaceableInputStream();
	private final ReplaceableOutputStream out = new ReplaceableOutputStream();
	private final BooleanCondition sync = BooleanCondition.of();
	private volatile Socket socket;

	public static SelfHealingSocketConnector of(SelfHealingSocketConfig config) {
		return new SelfHealingSocketConnector(config);
	}

	private SelfHealingSocketConnector(SelfHealingSocketConfig config) {
		this.config = config;
		in.listeners().listen(this::checkIfBroken);
		out.listeners().listen(this::checkIfBroken);
		start();
	}

	/**
	 * Manually notify the connector it is broken. Useful if the connector cannot determine it is
	 * broken from IOExceptions alone.
	 */
	@Override
	public void broken() {
		setBroken();
	}

	@Override
	public HostPort hostPort() {
		return config.hostPort;
	}
	
	@Override
	public void connect() throws IOException {
		try {
			initSocket();
		} catch (RuntimeException | IOException e) {
			broken();
			throw e;
		}
	}

	@Override
	public Listenable<StateChange> listeners() {
		return listeners;
	}

	@Override
	public InputStream in() {
		return in;
	}

	@Override
	public OutputStream out() {
		return out;
	}

	@Override
	public void close() {
		super.close();
		LogUtil.close(logger, socket);
	}

	@Override
	public String toString() {
		return ToString.forClass(this, config);
	}

	@Override
	protected void loop() throws InterruptedException {
		sync.awaitPeek();
		logger.info("Connection is broken - attempting to fix");
		fixSocket();
		logger.info("Connection is now fixed");
		// wait for streams to recover before clearing
		ConcurrentUtil.delay(config.recoveryDelayMs);
		sync.clear();
		notifyListeners(StateChange.fixed);
	}

	private void fixSocket() {
		ExceptionTracker exceptions = ExceptionTracker.of();
		while (true) {
			try {
				initSocket();
				break;
			} catch (IOException e) {
				if (exceptions.add(e)) logger.error("Failed to fix connection, retrying: {}", e);
				ConcurrentUtil.delay(config.fixRetryDelayMs);
			}
		}
	}

	private void notifyListeners(StateChange state) {
		try {
			listeners.accept(state);
		} catch (RuntimeInterruptedException e) {
			throw e;
		} catch (RuntimeException e) {
			logger.catching(e);
		}
	}

	private void checkIfBroken(Exception e) {
		if (!config.brokenPredicate.test(e)) return;
		if (sync.isSet()) return;
		setBroken();
	}

	private void setBroken() {
		sync.signal();
		notifyListeners(StateChange.broken);
	}

	@SuppressWarnings("resource")
	private void initSocket() throws IOException {
		LogUtil.close(logger, socket);
		socket = openSocket();
		logger.debug("Connected to {}", config.hostPort);
		in.setInputStream(socket.getInputStream());
		out.setOutputStream(socket.getOutputStream());
	}

	private Socket openSocket() throws IOException {
		Socket socket = null;
		try {
			socket = config.openSocket();
			config.params.applyTo(socket);
			return socket;
		} catch (RuntimeException | IOException e) {
			LogUtil.close(logger, socket);
			throw e;
		}
	}

}
