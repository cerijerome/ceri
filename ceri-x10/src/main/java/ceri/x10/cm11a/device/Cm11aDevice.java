package ceri.x10.cm11a.device;

import static ceri.x10.util.X10Controller.verifySupported;
import java.io.IOException;
import java.util.Objects;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ceri.common.event.Listenable;
import ceri.common.io.Connector;
import ceri.common.io.StateChange;
import ceri.common.text.ToString;
import ceri.common.util.Enclosed;
import ceri.log.concurrent.Dispatcher;
import ceri.log.util.LogUtil;
import ceri.x10.cm11a.protocol.Status;
import ceri.x10.command.Command;
import ceri.x10.command.CommandListener;

public class Cm11aDevice implements Cm11a {
	private static final Logger logger = LogManager.getLogger();
	private final Connector.Fixable connector;
	private final Processor processor;
	private final Dispatcher<CommandListener, Command> dispatcher;

	public static class Config {
		public static final Config DEFAULT = builder().build();
		public final int maxSendAttempts;
		public final int queuePollTimeoutMs;
		public final int readPollMs;
		public final int readTimeoutMs;
		public final int errorDelayMs;
		public final int queueSize;

		public static class Builder {
			int maxSendAttempts = 3;
			int queuePollTimeoutMs = 50;
			int readPollMs = 20;
			int readTimeoutMs = 3000;
			int errorDelayMs = 1000;
			int queueSize = 100;

			Builder() {}

			public Builder maxSendAttempts(int maxSendAttempts) {
				this.maxSendAttempts = maxSendAttempts;
				return this;
			}

			public Builder queuePollTimeoutMs(int queuePollTimeoutMs) {
				this.queuePollTimeoutMs = queuePollTimeoutMs;
				return this;
			}

			public Builder readPollMs(int readPollMs) {
				this.readPollMs = readPollMs;
				return this;
			}

			public Builder readTimeoutMs(int readTimeoutMs) {
				this.readTimeoutMs = readTimeoutMs;
				return this;
			}

			public Builder errorDelayMs(int errorDelayMs) {
				this.errorDelayMs = errorDelayMs;
				return this;
			}

			public Builder queueSize(int queueSize) {
				this.queueSize = queueSize;
				return this;
			}

			public Config build() {
				return new Config(this);
			}
		}

		public static Builder builder() {
			return new Builder();
		}

		Config(Builder builder) {
			maxSendAttempts = builder.maxSendAttempts;
			queuePollTimeoutMs = builder.queuePollTimeoutMs;
			readPollMs = builder.readPollMs;
			readTimeoutMs = builder.readTimeoutMs;
			errorDelayMs = builder.errorDelayMs;
			queueSize = builder.queueSize;
		}

		@Override
		public int hashCode() {
			return Objects.hash(maxSendAttempts, queuePollTimeoutMs, readPollMs, readTimeoutMs,
				errorDelayMs, queueSize);
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) return true;
			if (!(obj instanceof Config other)) return false;
			if (maxSendAttempts != other.maxSendAttempts) return false;
			if (queuePollTimeoutMs != other.queuePollTimeoutMs) return false;
			if (readPollMs != other.readPollMs) return false;
			if (readTimeoutMs != other.readTimeoutMs) return false;
			if (errorDelayMs != other.errorDelayMs) return false;
			if (queueSize != other.queueSize) return false;
			return true;
		}

		@Override
		public String toString() {
			return ToString.forClass(this, maxSendAttempts, queuePollTimeoutMs, readPollMs,
				readTimeoutMs, errorDelayMs, queueSize);
		}
	}

	public static Cm11aDevice of(Config config, Connector.Fixable connector) {
		return new Cm11aDevice(config, connector);
	}

	private Cm11aDevice(Config config, Connector.Fixable connector) {
		this.connector = connector;
		dispatcher = Dispatcher.of(config.queuePollTimeoutMs, CommandListener::dispatcher);
		processor = new Processor(config, connector, dispatcher::dispatch);
	}

	@Override
	public Listenable<StateChange> listeners() {
		return connector.listeners();
	}

	@Override
	public void command(Command command) throws IOException {
		verifySupported(this, command);
		logger.info("Command: {}", command);
		processor.command(command);
	}

	public Status requestStatus() throws IOException {
		logger.info("Request: status");
		return processor.requestStatus();
	}

	@Override
	public Enclosed<RuntimeException, CommandListener> listen(CommandListener listener) {
		return dispatcher.listen(listener);
	}

	@Override
	public void close() {
		LogUtil.close(processor, dispatcher);
	}

}
