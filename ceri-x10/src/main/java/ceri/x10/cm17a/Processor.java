package ceri.x10.cm17a;

import static ceri.common.io.BitIterator.Start.high;
import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.BlockingQueue;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ceri.common.concurrent.RuntimeInterruptedException;
import ceri.common.io.BitIterator;
import ceri.common.util.BasicUtil;
import ceri.common.util.EqualsUtil;
import ceri.x10.command.BaseCommand;
import ceri.x10.command.BaseUnitCommand;
import ceri.x10.command.CommandFactory;
import ceri.x10.command.CommandState;
import ceri.x10.command.DimCommand;
import ceri.x10.command.UnitCommand;
import ceri.x10.type.Address;

/**
 * Handles all communication with the device. Reads commands from the input queue, and dispatches
 * notifications to the output queue.
 */
public class Processor implements Closeable {
	private static final Logger logger = LogManager.getLogger();
	private static final int DIM_PERCENT_PER_SEND = 5;
	private final int maxSendAttempts;
	private final int queuePollTimeoutMs;
	private final int waitIntervalMs;
	private final int resetIntervalMs;
	private final int commandIntervalMs;
	private final Commands commands;
	private final BlockingQueue<? extends BaseCommand<?>> inQueue;
	private final Collection<? super BaseCommand<?>> outQueue;
	private final Cm17aConnector connector;
	private final Thread thread;

	public static class Builder {
		final Cm17aConnector connector;
		final BlockingQueue<? extends BaseCommand<?>> inQueue;
		final Collection<? super BaseCommand<?>> outQueue;
		int maxSendAttempts = 3;
		int queuePollTimeoutMs = 10000;
		int waitIntervalMs = 1;
		int resetIntervalMs = 10;
		int commandIntervalMs = 1000;

		Builder(Cm17aConnector connector, BlockingQueue<? extends BaseCommand<?>> inQueue,
			Collection<? super BaseCommand<?>> outQueue) {
			this.connector = connector;
			this.inQueue = inQueue;
			this.outQueue = outQueue;
		}

		public Builder maxSendAttempts(int maxSendAttempts) {
			this.maxSendAttempts = maxSendAttempts;
			return this;
		}

		public Builder queuePollTimeoutMs(int queuePollTimeoutMs) {
			this.queuePollTimeoutMs = queuePollTimeoutMs;
			return this;
		}

		public Builder waitIntervalMs(int waitIntervalMs) {
			this.waitIntervalMs = waitIntervalMs;
			return this;
		}

		public Builder resetIntervalMs(int resetIntervalMs) {
			this.resetIntervalMs = resetIntervalMs;
			return this;
		}

		public Builder commandIntervalMs(int commandIntervalMs) {
			this.commandIntervalMs = commandIntervalMs;
			return this;
		}

		public Processor build() throws IOException {
			return new Processor(this);
		}
	}

	public static Builder
		builder(Cm17aConnector connector, BlockingQueue<? extends BaseCommand<?>> inQueue,
			Collection<? super BaseCommand<?>> outQueue) {
		return new Builder(connector, inQueue, outQueue);
	}

	Processor(Builder builder) throws IOException {
		connector = builder.connector;
		inQueue = builder.inQueue;
		outQueue = builder.outQueue;
		commands = new Commands();
		maxSendAttempts = builder.maxSendAttempts;
		queuePollTimeoutMs = builder.queuePollTimeoutMs;
		waitIntervalMs = builder.waitIntervalMs;
		resetIntervalMs = builder.resetIntervalMs;
		commandIntervalMs = builder.commandIntervalMs;
		thread = new Thread(new Runnable() {
			@Override
			public void run() {
				Processor.this.run();
			}
		});
		thread.start();
	}

	@Override
	public void close() {
		thread.interrupt();
		try {
			thread.join();
		} catch (InterruptedException e) {
			logger.catching(Level.WARN, e);
		}
	}

	void run() {
		logger.info("Processor thread started");
		try {
			process();
		} catch (InterruptedException | RuntimeInterruptedException e) {
			logger.info("Processor thread interrupted");
		} catch (RuntimeException e) {
			logger.catching(e);
		} finally {
			logger.info("Processor thread stopped");
		}
	}

	/**
	 * Processing loop.
	 */
	private void process() throws InterruptedException {
		CommandState commandState = new CommandState(inQueue, maxSendAttempts, queuePollTimeoutMs);
		sendReset();
		Address lastAddress = null;
		while (true) {
			BaseCommand<?> command = commandState.command();
			if (command != null) {
				sendCommand(lastAddress, command);
				commandState.success();
				lastAddress = getAddress(lastAddress, command);
				outQueue.add(command);
				BasicUtil.delay(commandIntervalMs);
			}
		}
	}

	/**
	 * Determines the address based on command type and last address used.
	 */
	private Address getAddress(Address lastAddress, BaseCommand<?> command) {
		switch (command.type.group) {
		case unit:
			BaseUnitCommand<?> unitCommand = (BaseUnitCommand<?>) command;
			return unitCommand.address();
		default:
			return lastAddress;
		}
	}

	/**
	 * Sends a command to the device. Some commands require multiple transmissions.
	 */
	private void sendCommand(Address lastAddress, BaseCommand<?> command) {
		Collection<byte[]> transmissions = transmissions(lastAddress, command);
		for (byte[] transmission : transmissions)
			send(transmission);
	}

	/**
	 * Converts a command into transmissions.
	 */
	private Collection<byte[]> transmissions(Address lastAddress, BaseCommand<?> command) {
		switch (command.type) {
		case OFF:
		case ON:
			return Collections.singleton(commands.unit((UnitCommand) command));
		case DIM:
		case BRIGHT:
			return dimTransmissions(lastAddress, (DimCommand) command);
		case EXTENDED:
		case ALL_UNITS_OFF:
		case ALL_LIGHTS_OFF:
		case ALL_LIGHTS_ON:
		default:
			return null;
		}
	}

	/**
	 * Converts a dim command into multiple transmissions. One transmission is added to set the
	 * address using an ON command if necessary. Subsequent transmissions send a DIM/BRIGHT in 5%
	 * steps.
	 */
	private Collection<byte[]> dimTransmissions(Address lastAddress, DimCommand command) {
		if (command.percent == 0) return Collections.emptyList();
		Collection<byte[]> transmissions = new ArrayList<>();
		if (!EqualsUtil.equals(lastAddress, new Address(command.house, command.unit))) {
			UnitCommand onCommand = CommandFactory.on(command.house, command.unit);
			transmissions.add(commands.unit(onCommand));
		}
		byte[] dim = commands.dim(command.function());
		int count = command.percent / DIM_PERCENT_PER_SEND;
		while (count-- > 0)
			transmissions.add(dim);
		return transmissions;
	}

	/**
	 * Sends a binary transmission to the device by toggling RTS(0) and DTS(1).
	 */
	private void send(byte[] transmission) {
		logger.debug("Sending: " + transmission);
		for (Boolean bit : BasicUtil.forEach(new BitIterator(high, transmission))) {
			if (bit.booleanValue()) {
				connector.setDtr(false);
				BasicUtil.delay(waitIntervalMs);
				connector.setDtr(true);
				BasicUtil.delay(waitIntervalMs);
			} else {
				connector.setRts(false);
				BasicUtil.delay(waitIntervalMs);
				connector.setRts(true);
				BasicUtil.delay(waitIntervalMs);
			}
		}
	}

	/**
	 * Resets the device by powering it off then on.
	 */
	private void sendReset() {
		logger.debug("Sending reset");
		connector.setDtr(false);
		connector.setRts(false);
		BasicUtil.delay(resetIntervalMs);
		connector.setDtr(true);
		connector.setRts(true);
		BasicUtil.delay(resetIntervalMs);
	}

}
