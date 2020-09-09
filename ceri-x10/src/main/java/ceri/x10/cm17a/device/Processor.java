package ceri.x10.cm17a.device;

import static ceri.x10.cm17a.device.Data.transmission;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ceri.common.concurrent.RuntimeInterruptedException;
import ceri.common.data.ByteProvider;
import ceri.common.data.ByteUtil;
import ceri.common.util.BasicUtil;
import ceri.common.util.EqualsUtil;
import ceri.common.util.ExceptionTracker;
import ceri.log.concurrent.LoopingExecutor;
import ceri.x10.command.BaseCommand;
import ceri.x10.command.BaseUnitCommand;
import ceri.x10.command.DimCommand;
import ceri.x10.command.UnitCommand;
import ceri.x10.type.Address;
import ceri.x10.type.FunctionGroup;
import ceri.x10.type.FunctionType;

/**
 * Handles all communication with the device. Reads commands from the input queue, and dispatches
 * notifications to the output queue.
 */
public class Processor extends LoopingExecutor {
	private static final Logger logger = LogManager.getLogger();
	private static final List<FunctionType> supportedFns =
		List.of(FunctionType.off, FunctionType.on, FunctionType.dim, FunctionType.bright);
	private static final int DIM_PERCENT_PER_SEND = 5;
	private final Cm17aDeviceConfig config;
	private final BlockingQueue<? extends BaseCommand<?>> inQueue;
	private final Collection<? super BaseCommand<?>> outQueue;
	private final Cm17aConnector connector;
	private final ExceptionTracker exceptions = ExceptionTracker.of();

	Processor(Cm17aDeviceConfig config, Cm17aConnector connector,
		BlockingQueue<? extends BaseCommand<?>> inQueue,
		Collection<? super BaseCommand<?>> outQueue) {
		this.config = config;
		this.connector = connector;
		this.inQueue = inQueue;
		this.outQueue = outQueue;
		start();
	}

	/**
	 * Returns true if the function type is supported.
	 */
	public static boolean supported(FunctionType type) {
		return supportedFns.contains(type);
	}

	@Override
	protected void loop() throws IOException, InterruptedException {
		try {
			sendReset();
			Address lastAddress = null;
			while (true) {
				BaseCommand<?> command = inQueue.poll(config.pollTimeoutMs, TimeUnit.MILLISECONDS);
				if (command == null) continue;
				sendCommand(lastAddress, command);
				lastAddress = getAddress(lastAddress, command);
				outQueue.add(command);
				BasicUtil.delay(config.commandIntervalMs);
			}
		} catch (RuntimeInterruptedException e) {
			throw e;
		} catch (RuntimeException | IOException e) {
			if (exceptions.add(e)) logger.catching(Level.WARN, e);
			BasicUtil.delay(config.errorDelayMs);
		}
	}

	/**
	 * Determines the address based on command type and last address used.
	 */
	private Address getAddress(Address lastAddress, BaseCommand<?> command) {
		if (command.type.group != FunctionGroup.unit) return lastAddress;
		BaseUnitCommand<?> unitCommand = (BaseUnitCommand<?>) command;
		return unitCommand.address();
	}

	/**
	 * Sends a command to the device. Some commands require multiple transmissions.
	 */
	private void sendCommand(Address lastAddress, BaseCommand<?> command) throws IOException {
		Collection<ByteProvider> transmissions = transmissions(lastAddress, command);
		for (ByteProvider transmission : transmissions)
			send(transmission);
	}

	/**
	 * Converts a command into transmissions.
	 */
	private List<ByteProvider> transmissions(Address lastAddress, BaseCommand<?> command) {
		if (command.type.group == FunctionGroup.unit)
			return unitTransmissions((UnitCommand) command);
		if (command.type.group == FunctionGroup.dim)
			return dimTransmissions(lastAddress, (DimCommand) command);
		switch (command.type) {
		case off:
		case on:
			UnitCommand unitCommand = (UnitCommand) command;
			return List.of(Data.transmission(command.house, unitCommand.unit, command.type));
		case dim:
		case bright:
			return dimTransmissions(lastAddress, (DimCommand) command);
		case extended:
		case allUnitsOff:
		case allLightsOff:
		case allLightsOn:
		default:
			logger.warn("Not supported: {}", command);
			return Collections.emptyList();
		}
	}

	private List<ByteProvider> unitTransmissions(UnitCommand command) {
		return List.of(transmission(command.house, command.unit, command.type));
	}

	/**
	 * Converts a dim command into multiple transmissions. One transmission is added to set the
	 * address using an ON command if necessary. Subsequent transmissions send a DIM/BRIGHT in 5%
	 * steps.
	 */
	private List<ByteProvider> dimTransmissions(Address lastAddress, DimCommand command) {
		if (command.percent == 0) return Collections.emptyList();
		List<ByteProvider> transmissions = new ArrayList<>();
		if (!EqualsUtil.equals(lastAddress, command.address()))
			transmissions.add(transmission(command.house, command.unit, FunctionType.on));
		ByteProvider dim = transmission(command.house, command.type);
		int count = command.percent / DIM_PERCENT_PER_SEND;
		while (count-- > 0)
			transmissions.add(dim);
		return transmissions;
	}

	/**
	 * Sends a binary transmission to the device by toggling RTS(0) and DTS(1).
	 */
	private void send(ByteProvider transmission) throws IOException {
		logger.debug("Sending: {}", transmission);
		for (int i = 0; i < transmission.length(); i++) {
			int b = transmission.getUbyte(i);
			for (int j = Byte.SIZE - 1; j >= 0; j--) {
				sendBit(ByteUtil.bit(b, j));
			}
		}
	}

	/**
	 * Sends a bit to the device by toggling DTR(1) and RTS(0).
	 */
	private void sendBit(boolean bit) throws IOException {
		if (bit) {
			connector.setDtr(false);
			BasicUtil.delay(config.waitIntervalMs);
			connector.setDtr(true);
			BasicUtil.delay(config.waitIntervalMs);
		} else {
			connector.setRts(false);
			BasicUtil.delay(config.waitIntervalMs);
			connector.setRts(true);
			BasicUtil.delay(config.waitIntervalMs);
		}
	}

	/**
	 * Resets the device by powering it off then on.
	 */
	private void sendReset() throws IOException {
		logger.debug("Sending reset");
		connector.setDtr(false);
		connector.setRts(false);
		BasicUtil.delay(config.resetIntervalMs);
		connector.setDtr(true);
		connector.setRts(true);
		BasicUtil.delay(config.resetIntervalMs);
	}

}
