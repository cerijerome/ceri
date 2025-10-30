package ceri.x10.cm17a.device;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ceri.common.event.Listenable;
import ceri.common.function.Enclosure;
import ceri.common.io.StateChange;
import ceri.common.text.ToString;
import ceri.log.concurrent.Dispatcher;
import ceri.log.util.Logs;
import ceri.x10.command.Command;
import ceri.x10.command.FunctionGroup;
import ceri.x10.util.X10Controller;

public class Cm17aDevice implements Cm17a {
	private static final Logger logger = LogManager.getLogger();
	static final List<FunctionGroup> supportedGroups =
		List.of(FunctionGroup.unit, FunctionGroup.dim);
	private final Cm17aConnector connector;
	private final Processor processor;
	private final Dispatcher<Command.Listener, Command> dispatcher;

	public static class Config {
		public static final Config NULL = builder().commandIntervalMicros(0).resetIntervalMicros(0)
			.waitIntervalMicros(0).queuePollTimeoutMs(0).errorDelayMs(0).build();
		public static final Config DEFAULT = builder().build();
		public final int queuePollTimeoutMs;
		public final int waitIntervalMicros;
		public final int resetIntervalMicros;
		public final int commandIntervalMicros;
		public final int errorDelayMs;
		public final int queueSize;

		public static class Builder {
			int queuePollTimeoutMs = 10000;
			int waitIntervalMicros = 800;
			int resetIntervalMicros = 10000;
			int commandIntervalMicros = 1000;
			int errorDelayMs = 1000;
			int queueSize = 100;

			Builder() {}

			public Builder queuePollTimeoutMs(int queuePollTimeoutMs) {
				this.queuePollTimeoutMs = queuePollTimeoutMs;
				return this;
			}

			public Builder waitIntervalMicros(int waitIntervalMicros) {
				this.waitIntervalMicros = waitIntervalMicros;
				return this;
			}

			public Builder resetIntervalMicros(int resetIntervalMicros) {
				this.resetIntervalMicros = resetIntervalMicros;
				return this;
			}

			public Builder commandIntervalMicros(int commandIntervalMicros) {
				this.commandIntervalMicros = commandIntervalMicros;
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
			queuePollTimeoutMs = builder.queuePollTimeoutMs;
			waitIntervalMicros = builder.waitIntervalMicros;
			resetIntervalMicros = builder.resetIntervalMicros;
			commandIntervalMicros = builder.commandIntervalMicros;
			errorDelayMs = builder.errorDelayMs;
			queueSize = builder.queueSize;
		}

		@Override
		public int hashCode() {
			return Objects.hash(queuePollTimeoutMs, waitIntervalMicros, resetIntervalMicros,
				commandIntervalMicros, errorDelayMs, queueSize);
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) return true;
			if (!(obj instanceof Config other)) return false;
			if (queuePollTimeoutMs != other.queuePollTimeoutMs) return false;
			if (waitIntervalMicros != other.waitIntervalMicros) return false;
			if (resetIntervalMicros != other.resetIntervalMicros) return false;
			if (commandIntervalMicros != other.commandIntervalMicros) return false;
			if (errorDelayMs != other.errorDelayMs) return false;
			if (queueSize != other.queueSize) return false;
			return true;
		}

		@Override
		public String toString() {
			return ToString.forClass(this, queuePollTimeoutMs, waitIntervalMicros,
				resetIntervalMicros, commandIntervalMicros, errorDelayMs, queueSize);
		}
	}

	public static Cm17aDevice of(Config config, Cm17aConnector connector) {
		return new Cm17aDevice(config, connector);
	}

	private Cm17aDevice(Config config, Cm17aConnector connector) {
		this.connector = connector;
		dispatcher = Dispatcher.of(config.queuePollTimeoutMs, Command.Listener::dispatcher);
		processor = new Processor(config, connector);
	}

	@Override
	public Listenable<StateChange> listeners() {
		return connector.listeners();
	}

	@Override
	public Enclosure<Command.Listener> listen(Command.Listener listener) {
		return dispatcher.listen(listener);
	}

	@Override
	public boolean supports(Command command) {
		return supportsCommand(command);
	}

	@Override
	public void command(Command command) throws IOException {
		X10Controller.verifySupported(this, command);
		logger.info("Command: {}", command);
		processor.command(command);
		dispatcher.dispatch(command);
	}

	@Override
	public void close() {
		Logs.close(processor, dispatcher);
	}

	static boolean supportsCommand(Command command) {
		return supportedGroups.contains(command.group());
	}
}
