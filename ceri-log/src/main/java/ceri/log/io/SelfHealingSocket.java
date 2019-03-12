package ceri.log.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ceri.common.concurrent.BooleanCondition;
import ceri.common.concurrent.RuntimeInterruptedException;
import ceri.common.event.Listenable;
import ceri.common.event.Listeners;
import ceri.common.io.ReplaceableInputStream;
import ceri.common.io.ReplaceableOutputStream;
import ceri.common.io.StateChange;
import ceri.common.text.ToStringHelper;
import ceri.common.util.BasicUtil;
import ceri.log.concurrent.LoopingExecutor;
import ceri.log.util.LogUtil;

/**
 * A self-healing socket. It will automatically reconnect if the socket is broken.
 */
public class SelfHealingSocket extends LoopingExecutor {
	private static final Logger logger = LogManager.getLogger();
	private final String host;
	private final int port;
	private final Integer receiveBufferSize;
	private final Integer sendBufferSize;
	private final Boolean tcpNoDelay;
	private final Integer soTimeoutSeconds;
	private final Boolean soLingerEnabled;
	private final Integer soLingerSeconds;
	private final Boolean keepAlive;
	private final int fixRetryDelayMs;
	private final int recoveryDelayMs;
	private final Listeners<StateChange> listeners = new Listeners<>();
	private final ReplaceableInputStream in = new ReplaceableInputStream();
	private final ReplaceableOutputStream out = new ReplaceableOutputStream();
	private final BooleanCondition sync = BooleanCondition.create();
	private volatile Socket socket = null;

	public static class Builder {
		final String host;
		final int port;
		Integer receiveBufferSize;
		Integer sendBufferSize;
		Boolean tcpNoDelay;
		Integer soTimeoutSeconds;
		Boolean soLingerEnabled;
		Integer soLingerSeconds;
		Boolean keepAlive;
		int fixRetryDelayMs = 2000;
		int recoveryDelayMs = fixRetryDelayMs / 2;

		Builder(String host, int port) {
			this.host = host;
			this.port = port;
		}

		public Builder receiveBufferSize(int receiveBufferSize) {
			this.receiveBufferSize = receiveBufferSize;
			return this;
		}

		public Builder sendBufferSize(int sendBufferSize) {
			this.sendBufferSize = sendBufferSize;
			return this;
		}

		public Builder soTimeout(int soTimeoutSeconds) {
			this.soTimeoutSeconds = soTimeoutSeconds;
			return this;
		}

		public Builder soLingerOff() {
			soLingerEnabled = Boolean.FALSE;
			soLingerSeconds = 0;
			return this;
		}

		public Builder soLinger(int lingerSeconds) {
			soLingerEnabled = Boolean.FALSE;
			soLingerSeconds = lingerSeconds;
			return this;
		}

		public Builder tcpNoDelay(boolean enabled) {
			tcpNoDelay = enabled;
			return this;
		}

		public Builder keepAlive(boolean enabled) {
			keepAlive = enabled;
			return this;
		}

		public Builder fixRetryDelayMs(int fixRetryDelayMs) {
			this.fixRetryDelayMs = fixRetryDelayMs;
			return this;
		}

		public Builder recoveryDelayMs(int recoveryDelayMs) {
			this.recoveryDelayMs = recoveryDelayMs;
			return this;
		}

		public SelfHealingSocket build() {
			return new SelfHealingSocket(this);
		}
	}

	public static Builder builder(String host, int port) {
		return new Builder(host, port);
	}

	SelfHealingSocket(Builder builder) {
		host = builder.host;
		port = builder.port;
		receiveBufferSize = builder.receiveBufferSize;
		sendBufferSize = builder.sendBufferSize;
		tcpNoDelay = builder.tcpNoDelay;
		soTimeoutSeconds = builder.soTimeoutSeconds;
		soLingerEnabled = builder.soLingerEnabled;
		soLingerSeconds = builder.soLingerSeconds;
		keepAlive = builder.keepAlive;
		fixRetryDelayMs = builder.fixRetryDelayMs;
		recoveryDelayMs = builder.recoveryDelayMs;
		in.listeners().listen(e -> checkIfBroken(e));
		out.listeners().listen(e -> checkIfBroken(e));
		start();
	}

	/**
	 * Manually notify the connector it is broken. Useful if the socket cannot determine it is
	 * broken from IOExceptions alone.
	 */
	public void broken() {
		setBroken();
	}

	public void connect() throws IOException {
		initSocket();
	}

	public Listenable<StateChange> listeners() {
		return listeners;
	}

	public InputStream in() {
		return in;
	}

	public OutputStream out() {
		return out;
	}

	@Override
	public void close() {
		super.close();
		LogUtil.close(logger, socket);
	}

	@Override
	protected void loop() throws InterruptedException {
		sync.awaitPeek();
		logger.warn("Connection is broken - attempting to fix");
		String lastErrorMsg = null;
		while (true) {
			try {
				initSocket();
				break;
			} catch (IOException e) {
				String errorMsg = e.getMessage();
				if (lastErrorMsg == null || !lastErrorMsg.equals(errorMsg))
					logger.debug("Failed to fix connection, retrying: {}", errorMsg);
				lastErrorMsg = errorMsg;
				BasicUtil.delay(fixRetryDelayMs);
			}
		}
		logger.info("Connection is now fixed");
		BasicUtil.delay(recoveryDelayMs); // wait for streams to recover before clearing
		sync.clear();
		notifyListeners(StateChange.fixed);
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
		BasicUtil.unused(e); // may use later
		if (sync.isSet()) return;
		setBroken();
	}

	private void setBroken() {
		sync.signal();
		notifyListeners(StateChange.broken);
	}

	private void initSocket() throws IOException {
		LogUtil.close(logger, socket);
		socket = createSocket();
		in.setInputStream(socket.getInputStream());
		out.setOutputStream(socket.getOutputStream());
	}

	private Socket createSocket() throws IOException {
		Socket socket = new Socket(this.host, this.port);
		if (receiveBufferSize != null) socket.setReceiveBufferSize(receiveBufferSize);
		if (sendBufferSize != null) socket.setSendBufferSize(sendBufferSize);
		if (keepAlive != null) socket.setKeepAlive(keepAlive);
		if (tcpNoDelay != null) socket.setTcpNoDelay(tcpNoDelay);
		if (soTimeoutSeconds != null) socket.setSoTimeout(soLingerSeconds);
		if (soLingerEnabled != null) socket.setSoLinger(soLingerEnabled, soLingerSeconds);
		return socket;
	}

	@Override
	public String toString() {
		return ToStringHelper.createByClass(this, host, port, receiveBufferSize, sendBufferSize,
			tcpNoDelay, soTimeoutSeconds, soLingerEnabled, soLingerSeconds, keepAlive,
			fixRetryDelayMs, recoveryDelayMs).toString();
	}

}
