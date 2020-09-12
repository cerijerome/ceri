package ceri.x10.cm11a.device;

import static ceri.common.io.IoUtil.ioExceptionf;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.concurrent.BlockingQueue;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ceri.common.concurrent.ConcurrentUtil;
import ceri.common.concurrent.RuntimeInterruptedException;
import ceri.common.data.ByteProvider;
import ceri.common.data.ByteStream;
import ceri.common.data.ByteUtil;
import ceri.common.io.PollingInputStream;
import ceri.common.util.ExceptionTracker;
import ceri.log.concurrent.LoopingExecutor;
import ceri.x10.cm11a.entry.Clock;
import ceri.x10.cm11a.entry.Data;
import ceri.x10.cm11a.entry.Entry;
import ceri.x10.cm11a.entry.EntryBuffer;
import ceri.x10.cm11a.entry.EntryCollector;
import ceri.x10.cm11a.entry.Protocol;
import ceri.x10.cm11a.entry.Transmit;
import ceri.x10.command.Command;

/**
 * Handles all communication with the device. Reads commands from the input queue, and dispatches
 * notifications to the output queue.
 */
public class Processor extends LoopingExecutor {
	private static final Logger logger = LogManager.getFormatterLogger();
	private final Cm11aDeviceConfig config;
	private final BlockingQueue<Command> inQueue;
	private final Collection<Command> outQueue;
	private final ByteStream.Reader in;
	private final ByteStream.Writer out;
	private final EntryCollector collector;
	private final ExceptionTracker exceptions = ExceptionTracker.of();

	@SuppressWarnings("resource")
	Processor(Cm11aDeviceConfig config, Cm11aConnector connector, BlockingQueue<Command> inQueue,
		Collection<Command> outQueue) {
		try {
			this.config = config;
			PollingInputStream pollIn = new PollingInputStream(
				new BufferedInputStream(connector.in()), config.readPollMs, config.readTimeoutMs);
			in = ByteStream.reader(pollIn);
			out = ByteStream.writer(new BufferedOutputStream(connector.out()));
			this.inQueue = inQueue;
			this.outQueue = outQueue;
			collector = new EntryCollector(outQueue);
			start();
		} catch (RuntimeException e) {
			close();
			throw e;
		}
	}

	@Override
	public void close() {
		super.close();
	}

	@Override
	protected void loop() throws InterruptedException {
		try {
			ConcurrentUtil.checkInterrupted();
			if (in.available() > 0) processInput(in.readUbyte());
			else processQueue();
			exceptions.clear();
		} catch (RuntimeInterruptedException | InterruptedException e) {
			throw e;
		} catch (RuntimeException | IOException e) {
			if (exceptions.add(e)) logger.catching(e);
		}
	}

	private void processQueue() throws InterruptedException, IOException {
		Command command = inQueue.poll(config.pollTimeoutMs, MILLISECONDS);
		if (command == null) return;
		sendCommand(command, config.maxSendAttempts);
		outQueue.add(command);
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
	 * Reads data from the device and dispatches the result to the command listener.
	 */
	@SuppressWarnings("resource")
	private void receiveData() throws IOException {
		out.writeByte(Protocol.PC_READY.value);
		out.flush();
		EntryBuffer buffer = EntryBuffer.decode(in);
		collector.collectAll(buffer.entries);
	}

	/**
	 * Sends status response to the device.
	 */
	private void sendClock(Clock clock) throws IOException {
		logger.debug("Sending: %s", clock);
		ByteProvider data = clock.encode();
		send(data, config.maxSendAttempts);
	}

	/**
	 * Sends a command by breaking it into entries and sending each entry.
	 * 
	 * @throws IOException
	 */
	private void sendCommand(Command command, int maxSendAttempts) throws IOException {
		for (Entry entry : Entry.allFrom(command))
			sendEntry(entry, maxSendAttempts);
	}

	/**
	 * Sends a single entry to the device. Waits for checksum response, checks the value then sends
	 * acknowledgment.
	 * 
	 * @throws IOException
	 */
	private void sendEntry(Entry entry, int maxSendAttempts) throws IOException {
		logger.debug("Sending: %s", entry);
		ByteProvider data = Transmit.encode(entry);
		send(data, maxSendAttempts);
	}

	/**
	 * Sends a single entry to the device. Waits for checksum response, checks the value then sends
	 * acknowledgment.
	 */
	@SuppressWarnings("resource")
	private void send(ByteProvider data, int maxSendAttempts) throws IOException {
		int checksum = Data.checksum(data);
		ExceptionTracker exceptions = ExceptionTracker.of();
		for (int i = 0; i < maxSendAttempts; i++) {
			try {
				out.writeFrom(data);
				out.flush();
				await(checksum, maxSendAttempts);
				out.writeByte(Protocol.OK.value);
				out.flush();
				await(Protocol.READY.value, maxSendAttempts);
				return;
			} catch (RuntimeInterruptedException e) {
				throw e;
			} catch (RuntimeException | IOException e) {
				if (exceptions.add(e)) logger.catching(Level.WARN, e);
			}
		}
		throw ioExceptionf("Failed to send data in %d attempts: %s", maxSendAttempts,
			ByteUtil.toHex(data.ustream(0), "-"));
	}

	/**
	 * Waits for a specific byte from the device. If the byte is a protocol input value, process
	 * that then wait again. Only retry the given maximum number of times.
	 */
	private void await(int expected, int maxAttempts) throws IOException {
		logger.debug("Waiting for byte 0x%02x", expected);
		for (int i = 0; i < maxAttempts; i++) {
			int actual = in.readUbyte();
			if (actual == expected) return;
			processInput(actual);
		}
		throw ioExceptionf("Failed to receive 0x%02x in %d attempts", expected, maxAttempts);
	}

}
