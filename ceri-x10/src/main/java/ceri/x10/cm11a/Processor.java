package ceri.x10.cm11a;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.concurrent.BlockingQueue;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ceri.common.concurrent.ConcurrentUtil;
import ceri.common.concurrent.RuntimeInterruptedException;
import ceri.common.io.IoTimeoutException;
import ceri.common.io.PollingInputStream;
import ceri.x10.cm11a.protocol.Data;
import ceri.x10.cm11a.protocol.InputBuffer;
import ceri.x10.cm11a.protocol.Protocol;
import ceri.x10.cm11a.protocol.ProtocolState;
import ceri.x10.cm11a.protocol.WriteStatus;
import ceri.x10.command.BaseCommand;
import ceri.x10.command.CommandState;
import ceri.x10.util.UnexpectedByteException;

/**
 * Handles all communication with the device.
 * Reads commands from the input queue, and dispatches notifications to the output queue.
 */
public class Processor implements Closeable {
	private static final Logger logger = LogManager.getFormatterLogger(Processor.class);
	private final int maxSendAttempts;
	private final int queuePollTimeoutMs;
	private final BlockingQueue<? extends BaseCommand<?>> inQueue;
	private final Collection<? super BaseCommand<?>> outQueue;
	private final DataInputStream in;
	private final DataOutputStream out;
	private final EntryDispatcher dispatcher;
	private final Thread thread;

	public static class Builder {
		final Cm11aConnector connector;
		final BlockingQueue<? extends BaseCommand<?>> inQueue;
		final Collection<? super BaseCommand<?>> outQueue;
		int maxSendAttempts = 3;
		int queuePollTimeoutMs = 100;
		int readPollMs = 50;
		int readTimeoutMs = 5000;

		Builder(Cm11aConnector connector, BlockingQueue<? extends BaseCommand<?>> inQueue,
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
		
		public Builder readPollMs(int readPollMs) {
			this.readPollMs = readPollMs;
			return this;
		}
		
		public Builder readTimeoutMs(int readTimeoutMs) {
			this.readTimeoutMs = readTimeoutMs;
			return this;
		}
		
		public Processor build() {
			return new Processor(this);
		}
	}

	public static Builder
		builder(Cm11aConnector connector, BlockingQueue<? extends BaseCommand<?>> inQueue,
			Collection<? super BaseCommand<?>> outQueue) {
		return new Builder(connector, inQueue, outQueue);
	}

	Processor(Builder builder) {
		PollingInputStream pollIn =
			new PollingInputStream(new BufferedInputStream(builder.connector.in()),
				builder.readPollMs, builder.readTimeoutMs);
		in = new DataInputStream(pollIn);
		out = new DataOutputStream(new BufferedOutputStream(builder.connector.out()));
		inQueue = builder.inQueue;
		outQueue = builder.outQueue;
		queuePollTimeoutMs = builder.queuePollTimeoutMs;
		maxSendAttempts = builder.maxSendAttempts;
		dispatcher = new EntryDispatcher(outQueue);
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
		ProtocolState protocolState = new ProtocolState(in);
		CommandState commandState = new CommandState(inQueue, maxSendAttempts, queuePollTimeoutMs);
		while (true) {
			try {
				ConcurrentUtil.checkInterrupted();
				Protocol protocol = protocolState.protocol();
				if (protocol != null) {
					processInput(protocol);
				} else {
					BaseCommand<?> command = commandState.command();
					if (command != null) {
						sendCommand(command);
						commandState.success();
						outQueue.add(command);
					}
				}
			} catch (UnexpectedByteException e) {
				logger.catching(Level.WARN, e);
				protocolState.unexpected(e.actual);
			} catch (IOException | IoTimeoutException e) {
				logger.catching(e);
			}
		}
	}

	/**
	 * Waits for a specific byte from the device.
	 */
	private void await(byte expected) throws IOException {
		logger.debug("Waiting for 0x%x", expected);
		int actual = in.read();
		if (actual != expected) throw new UnexpectedByteException(expected, actual);
	}

	/**
	 * Processes input from the device based on the protocol type.
	 */
	private void processInput(Protocol protocol) throws IOException {
		logger.debug(protocol);
		switch (protocol) {
		case TIME_POLL:
			sendStatus();
			break;
		case DATA_POLL:
			processInputData();
			break;
		default:
			logger.warn("Ignoring %s", protocol.name());
		}
	}

	/**
	 * Reads input from the device and dispatches the result to the command listener.
	 */
	private void processInputData() throws IOException {
		out.write(Protocol.PC_READY.value);
		out.flush();
		InputBuffer buffer = InputBuffer.readFrom(in);
		dispatcher.dispatch(buffer.entries);
	}

	/**
	 * Sends status response to the device.
	 */
	private void sendStatus() throws IOException {
		WriteStatus status = new WriteStatus.Builder().build();
		status.writeTo(out);
		out.flush();
	}

	/**
	 * Sends a command by breaking it into entries and sending each entry.
	 */
	private void sendCommand(BaseCommand<?> command) throws IOException {
		for (Entry entry : EntryDispatcher.toEntries(command))
			sendEntry(entry);
	}

	/**
	 * Sends a single entry to the device. Waits for checksum response, checks the value then sends
	 * acknowledgment.
	 */
	private void sendEntry(Entry entry) throws IOException {
		logger.debug("Sending: %s", entry);
		byte[] data = Data.write.fromEntry(entry);
		out.write(data);
		out.flush();
		await(Data.checksum(data));
		out.write(Protocol.OK.value);
		out.flush();
		await(Protocol.READY.value);
	}

}
