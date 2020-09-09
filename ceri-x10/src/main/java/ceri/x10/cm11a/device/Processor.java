package ceri.x10.cm11a.device;

import static ceri.common.io.IoUtil.ioExceptionf;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ceri.common.concurrent.ConcurrentUtil;
import ceri.common.concurrent.RuntimeInterruptedException;
import ceri.common.data.ByteProvider;
import ceri.common.data.ByteStream;
import ceri.common.io.PollingInputStream;
import ceri.log.concurrent.LoopingExecutor;
import ceri.log.util.LogUtil;
import ceri.x10.cm11a.protocol.Data;
import ceri.x10.cm11a.protocol.InputBuffer;
import ceri.x10.cm11a.protocol.Protocol;
import ceri.x10.cm11a.protocol.WriteStatus;
import ceri.x10.command.BaseCommand;

/**
 * Handles all communication with the device. Reads commands from the input queue, and dispatches
 * notifications to the output queue.
 */
public class Processor extends LoopingExecutor {
	private static final Logger logger = LogManager.getLogger();
	// private final Cm11aDeviceConfig config;
	private final int pollTimeoutMs;
	private final int maxSendAttempts;
	private final BlockingQueue<? extends BaseCommand<?>> inQueue;
	private final Collection<? super BaseCommand<?>> outQueue;
	private final ByteStream.Reader in;
	private final ByteStream.Writer out;
	private final EntryDispatcher dispatcher;

	@SuppressWarnings("resource")
	Processor(Cm11aDeviceConfig config, Cm11aConnector connector,
		BlockingQueue<? extends BaseCommand<?>> inQueue,
		Collection<? super BaseCommand<?>> outQueue) {
		try {
			pollTimeoutMs = config.pollTimeoutMs;
			maxSendAttempts = config.maxSendAttempts;
			PollingInputStream pollIn = new PollingInputStream(
				new BufferedInputStream(connector.in()), config.readPollMs, config.readTimeoutMs);
			in = ByteStream.reader(pollIn);
			out = ByteStream.writer(new BufferedOutputStream(connector.out()));
			this.inQueue = inQueue;
			this.outQueue = outQueue;
			dispatcher = new EntryDispatcher(outQueue);
			start();
		} catch (RuntimeException e) {
			close();
			throw e;
		}
	}

	@Override
	protected void loop() throws InterruptedException {
		try {
			ConcurrentUtil.checkInterrupted();
			if (in.available() > 0) {
				processInput(in.readUbyte());
			} else {
				BaseCommand<?> command = inQueue.poll(pollTimeoutMs, TimeUnit.MILLISECONDS);
				if (command == null) return;
				sendCommand(command, maxSendAttempts);
				outQueue.add(command);
			}
		} catch (RuntimeInterruptedException | InterruptedException e) {
			throw e;
		} catch (RuntimeException | IOException e) {
			logger.catching(e);
		}
	}

	/**
	 * Processes input from the device based on the protocol type.
	 */
	private void processInput(int next) throws IOException {
		Protocol protocol = Protocol.from(next); // exception if no match
		logger.debug(protocol);
		switch (protocol) {
		case TIME_POLL:
			sendStatus();
			break;
		case DATA_POLL:
			processInputData();
			break;
		default:
			logger.warn("Ignoring {}", protocol.name());
		}
	}

	/**
	 * Reads input from the device and dispatches the result to the command listener.
	 */
	@SuppressWarnings("resource")
	private void processInputData() throws IOException {
		out.writeByte(Protocol.PC_READY.value);
		out.flush();
		InputBuffer buffer = InputBuffer.decode(in);
		dispatcher.dispatch(buffer.entries);
	}

	/**
	 * Sends status response to the device.
	 */
	private void sendStatus() throws IOException {
		WriteStatus.DEFAULT.encode(out);
		out.flush();
	}

	/**
	 * Sends a command by breaking it into entries and sending each entry.
	 */
	private void sendCommand(BaseCommand<?> command, int maxSendAttempts) {
		for (Entry entry : EntryDispatcher.toEntries(command))
			sendEntry(entry, maxSendAttempts);
	}

	/**
	 * Sends a single entry to the device. Waits for checksum response, checks the value then sends
	 * acknowledgment.
	 */
	@SuppressWarnings("resource")
	private void sendEntry(Entry entry, int maxSendAttempts) {
		logger.debug("Sending: {}", entry);
		ByteProvider data = Data.write.encode(entry);
		for (int i = 0; i < maxSendAttempts; i++) {
			try {
				out.writeFrom(data);
				out.flush();
				await(Data.checksum(data), maxSendAttempts);
				out.writeByte(Protocol.OK.value);
				out.flush();
				await(Protocol.READY.value, maxSendAttempts);
				return;
			} catch (RuntimeInterruptedException e) {
				throw e;
			} catch (RuntimeException | IOException e) {
				Level level = i < maxSendAttempts - 1 ? Level.WARN : Level.ERROR;
				logger.catching(level, e);
			}
		}
	}

	/**
	 * Waits for a specific byte from the device. If the byte is a protocol input value, process
	 * that then wait again. Only retry the given maximum number of times.
	 */
	private void await(int expected, int maxAttempts) throws IOException {
		logger.debug("Waiting for byte 0x{}", LogUtil.toHex(expected));
		for (int i = 0; i < maxAttempts; i++) {
			int actual = in.readUbyte();
			if (actual == expected) return;
			processInput(actual);
		}
		throw ioExceptionf("Failed to receive byte in %d attempts: 0x%x", expected, maxAttempts);
	}

}
