package ceri.x10.cm11a.device;

import static ceri.common.io.IoUtil.ioExceptionf;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.util.function.Consumer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ceri.common.concurrent.ConcurrentUtil;
import ceri.common.concurrent.RuntimeInterruptedException;
import ceri.common.concurrent.TaskQueue;
import ceri.common.data.ByteProvider;
import ceri.common.data.ByteStream;
import ceri.common.exception.ExceptionTracker;
import ceri.common.io.PollingInputStream;
import ceri.log.concurrent.LoopingExecutor;
import ceri.x10.cm11a.protocol.Clock;
import ceri.x10.cm11a.protocol.Data;
import ceri.x10.cm11a.protocol.Entry;
import ceri.x10.cm11a.protocol.EntryBuffer;
import ceri.x10.cm11a.protocol.EntryCollector;
import ceri.x10.cm11a.protocol.Protocol;
import ceri.x10.cm11a.protocol.Status;
import ceri.x10.cm11a.protocol.Transmit;
import ceri.x10.command.Command;

/**
 * Handles all communication with the device. Reads commands from the input queue, and dispatches
 * notifications to the output queue.
 */
public class Processor extends LoopingExecutor {
	private static final Logger logger = LogManager.getFormatterLogger();
	private final Cm11aDeviceConfig config;
	private final TaskQueue<IOException> taskQueue;
	private final Consumer<Command> dispatcher;
	private final ByteStream.Reader in;
	private final ByteStream.Writer out;
	private final EntryCollector collector;
	private final ExceptionTracker exceptions = ExceptionTracker.of();

	@SuppressWarnings("resource")
	Processor(Cm11aDeviceConfig config, Cm11aConnector connector, Consumer<Command> dispatcher) {
		try {
			this.config = config;
			taskQueue = TaskQueue.of(config.queueSize);
			PollingInputStream pollIn = new PollingInputStream(
				new BufferedInputStream(connector.in()), config.readPollMs, config.readTimeoutMs);
			in = ByteStream.reader(pollIn);
			out = ByteStream.writer(new BufferedOutputStream(connector.out()));
			this.dispatcher = dispatcher;
			collector = new EntryCollector(dispatcher);
			start();
		} catch (RuntimeException e) {
			close();
			throw e;
		}
	}

	public void command(Command command) throws IOException {
		taskQueue.execute(() -> sendCommand(command));
	}

	public Status requestStatus() throws IOException {
		return taskQueue.executeGet(() -> sendStatusRequest());
	}

	@Override
	protected void loop() throws InterruptedException {
		try {
			ConcurrentUtil.checkInterrupted();
			if (in.available() > 0) processInput(in.readUbyte());
			else taskQueue.processNext(config.queuePollTimeoutMs, MILLISECONDS);
			exceptions.clear();
		} catch (RuntimeInterruptedException | InterruptedException e) {
			throw e;
		} catch (RuntimeException | IOException e) {
			if (exceptions.add(e)) logger.catching(e);
		}
	}

	private void sendCommand(Command command) throws IOException {
		for (Entry entry : Entry.allFrom(command))
			sendEntry(entry);
		dispatcher.accept(command);
	}

	/**
	 * Processes input from the device based on the protocol type.
	 */
	private void processInput(int next) throws IOException {
		Protocol protocol = Protocol.from(next); // exception if no match
		logger.debug(protocol);
		switch (protocol) {
		case READY:
			break; // ignore
		case TIME_POLL:
			sendClock(Clock.of());
			break;
		case DATA_POLL:
			receiveData();
			break;
		default:
			logger.warn("Ignoring %s", protocol);
		}
	}

	/**
	 * Sends a single entry to the device, waits for checksum, checks the value then sends
	 * acknowledgment.
	 */
	@SuppressWarnings("resource")
	private void sendEntry(Entry entry) throws IOException {
		logger.debug("Sending: %s", entry);
		ByteProvider data = Transmit.encode(entry);
		int checksum = Data.checksum(data);
		// TODO: add resend logic if checksum fails
		out.writeFrom(data);
		await(checksum);
		out.writeByte(Protocol.OK.value);
		await(Protocol.READY.value);
	}

	@SuppressWarnings("resource")
	private Status sendStatusRequest() throws IOException {
		logger.debug("Request: status");
		out.writeByte(Protocol.STATUS.value).flush();
		return Status.decode(in);
	}

	/**
	 * Reads data from the device and dispatches the result to the command listener.
	 */
	@SuppressWarnings("resource")
	private void receiveData() throws IOException {
		out.writeByte(Protocol.PC_READY.value).flush();
		EntryBuffer buffer = EntryBuffer.decode(in);
		collector.collectAll(buffer.entries);
	}

	/**
	 * Sends status response to the device.
	 *
	 * @throws IOException
	 */
	@SuppressWarnings("resource")
	private void sendClock(Clock clock) throws IOException {
		logger.debug("Sending: %s", clock);
		ByteProvider data = clock.encode();
		out.writeFrom(data).flush();
	}

	/**
	 * Waits for a specific byte from the device. If the byte is a protocol input value, process
	 * that then wait again. Only retry the given maximum number of times.
	 */
	private void await(int expected) throws IOException {
		logger.debug("Waiting for byte 0x%02x", expected);
		out.flush();
		for (int i = 0; i < config.maxSendAttempts; i++) {
			int actual = in.readUbyte();
			if (actual == expected) return;
			processInput(actual);
		}
		throw ioExceptionf("Failed to receive 0x%02x in %d attempts", expected,
			config.maxSendAttempts);
	}

}
