package ceri.x10.cm17a.device;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import java.io.IOException;
import java.util.Collection;
import java.util.concurrent.BlockingQueue;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ceri.common.concurrent.RuntimeInterruptedException;
import ceri.common.data.ByteUtil;
import ceri.common.util.BasicUtil;
import ceri.common.util.ExceptionTracker;
import ceri.log.concurrent.LoopingExecutor;
import ceri.x10.command.Address;
import ceri.x10.command.Command;
import ceri.x10.command.FunctionGroup;
import ceri.x10.command.FunctionType;

/**
 * Handles all communication with the device. Reads commands from the input queue, and dispatches
 * notifications to the output queue.
 */
public class Processor extends LoopingExecutor {
	private static final Logger logger = LogManager.getFormatterLogger();
	private final Cm17aDeviceConfig config;
	private final BlockingQueue<Command> inQueue;
	private final Collection<Command> outQueue;
	private final Cm17aConnector connector;
	private final ExceptionTracker exceptions = ExceptionTracker.of();
	private Address lastOn = null;

	Processor(Cm17aDeviceConfig config, Cm17aConnector connector, BlockingQueue<Command> inQueue,
		Collection<Command> outQueue) {
		this.config = config;
		this.connector = connector;
		this.inQueue = inQueue;
		this.outQueue = outQueue;
		start();
	}

	@Override
	protected void loop() throws IOException, InterruptedException {
		try {
			sendReset();
			lastOn = null;
			while (true) {
				Command command = inQueue.poll(config.pollTimeoutMs, MILLISECONDS);
				if (command == null) continue;
				sendCommand(command);
				outQueue.add(command);
				BasicUtil.delayMicros(config.commandIntervalMicros);
			}
		} catch (RuntimeInterruptedException e) {
			throw e;
		} catch (RuntimeException | IOException e) {
			if (exceptions.add(e)) logger.catching(Level.WARN, e);
			BasicUtil.delay(config.errorDelayMs);
		}
	}

	private void sendCommand(Command command) throws IOException {
		if (command.group() == FunctionGroup.dim) sendDimCommand((Command.Dim) command);
		else sendUnitCommand(command);
	}

	private void sendUnitCommand(Command command) throws IOException {
		for (Address address : command.addresses()) {
			if (command.type() == FunctionType.on) sendOn(address);
			else sendOff(address);
		}
	}

	private void sendDimCommand(Command.Dim command) throws IOException {
		int count = Math.min(1, Data.toDimCount(command.percent()));
		int dimCode = Data.code(command.house(), command.type());
		for (Address address : command.addresses()) {
			sendOn(address);
			for (int i = 0; i < count; i++)
				send(dimCode);
		}
	}

	private void sendOff(Address address) throws IOException {
		int code = Data.code(address.house, address.unit, FunctionType.off);
		send(code);
		lastOn = null;
	}

	private void sendOn(Address address) throws IOException {
		if (address.equals(lastOn)) return; // already on
		int code = Data.code(address.house, address.unit, FunctionType.on);
		send(code);
		lastOn = address;
	}

	/**
	 * Sends a binary transmission to the device by toggling RTS(0) and DTS(1).
	 */
	private void send(int code) throws IOException {
		logger.debug("Sending: 0x%02x", code);
		sendByte(Data.HEADER1);
		sendByte(Data.HEADER2);
		sendByte(Data.HEADER2);
		sendByte(ByteUtil.ubyteAt(code, 1));
		sendByte(ByteUtil.ubyteAt(code, 0));
		sendByte(Data.FOOTER);
	}

	/**
	 * Sends bits to the device by toggling DTR(1) and RTS(0).
	 */
	private void sendByte(int b) throws IOException {
		for (int i = Byte.SIZE - 1; i >= 0; i--) {
			if (ByteUtil.bit(b, i)) {
				connector.setDtr(false);
				BasicUtil.delayMicros(config.waitIntervalMicros);
				connector.setDtr(true);
				BasicUtil.delayMicros(config.waitIntervalMicros);
			} else {
				connector.setRts(false);
				BasicUtil.delayMicros(config.waitIntervalMicros);
				connector.setRts(true);
				BasicUtil.delayMicros(config.waitIntervalMicros);
			}
		}
	}

	/**
	 * Resets the device by powering it off then on.
	 */
	private void sendReset() throws IOException {
		logger.debug("Sending reset");
		connector.setDtr(false);
		connector.setRts(false);
		BasicUtil.delayMicros(config.resetIntervalMicros);
		connector.setDtr(true);
		connector.setRts(true);
		BasicUtil.delayMicros(config.resetIntervalMicros);
	}

}
