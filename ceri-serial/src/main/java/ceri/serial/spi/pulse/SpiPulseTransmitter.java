package ceri.serial.spi.pulse;

import java.io.IOException;
import java.io.InputStream;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ceri.common.concurrent.BooleanCondition;
import ceri.common.concurrent.ConcurrentUtil;
import ceri.common.concurrent.Locker;
import ceri.common.concurrent.RuntimeInterruptedException;
import ceri.common.data.ByteProvider;
import ceri.common.data.ByteReceiver;
import ceri.common.exception.ExceptionTracker;
import ceri.common.io.Direction;
import ceri.log.concurrent.LoopingExecutor;
import ceri.serial.spi.Spi;
import ceri.serial.spi.SpiTransfer;

/**
 * Provides a ByteReceiver interface for writing pulse data to an SPI device.
 */
public class SpiPulseTransmitter extends LoopingExecutor implements ByteReceiver {
	private static final Logger logger = LogManager.getLogger();
	private final Locker locker = Locker.of();
	private final BooleanCondition sync = BooleanCondition.of(locker.lock);
	private final SpiPulseConfig config;
	private final int id;
	private final PulseBuffer buffer;
	private final SpiTransfer xfer;
	private final ExceptionTracker exceptions = ExceptionTracker.of();
	private boolean changed = false; // need to copy data before sending?

	public static SpiPulseTransmitter of(int id, Spi spi, SpiPulseConfig config) {
		return new SpiPulseTransmitter(id, spi, config);
	}

	private SpiPulseTransmitter(int id, Spi spi, SpiPulseConfig config) {
		super(logName(id, config));
		this.id = id;
		this.config = config;
		buffer = config.buffer();
		xfer = spi.transfer(Direction.out, buffer.storageSize());
		xfer.delayMicros(config.delayMicros);
		start();
	}

	public int id() {
		return id;
	}

	public PulseCycle cycle() {
		return buffer.cycle;
	}

	@Override
	public int length() {
		return buffer.length();
	}

	@Override
	public int setByte(int pos, int b) {
		try (var locked = locker.lock()) {
			changed = true;
			return buffer.setByte(pos, b);
		}
	}

	@Override
	public int copyFrom(int pos, byte[] array, int offset, int length) {
		try (var locked = locker.lock()) {
			if (length > 0) changed = true;
			return buffer.copyFrom(pos, array, offset, length);
		}
	}

	@Override
	public int copyFrom(int pos, ByteProvider provider, int offset, int length) {
		try (var locked = locker.lock()) {
			if (length > 0) changed = true;
			return buffer.copyFrom(pos, provider, offset, length);
		}
	}

	@Override
	public int fill(int pos, int length, int value) {
		try (var locked = locker.lock()) {
			if (length > 0) changed = true;
			return buffer.fill(pos, length, value);
		}
	}

	@Override
	public int readFrom(int index, InputStream in, int length) throws IOException {
		return ByteReceiver.readBufferFrom(this, index, in, length);
	}

	public void send() {
		sync.signal();
	}

	@Override
	protected void loop() throws InterruptedException {
		try {
			syncData();
			xfer.execute();
			exceptions.clear();
		} catch (InterruptedException | RuntimeInterruptedException e) {
			throw e;
		} catch (Exception e) {
			if (exceptions.add(e)) logger.catching(e);
			ConcurrentUtil.delay(config.resetDelayMs);
		}
	}

	private void syncData() throws InterruptedException {
		try (var locked = locker.lock()) {
			sync.await();
			if (changed) buffer.writePulseTo(xfer.out());
			changed = false;
		}
	}

	private static String logName(int id, SpiPulseConfig config) {
		return String.format("%s(%d:%d:%s)", SpiPulseTransmitter.class.getSimpleName(), id,
			config.size, config.cycle);
	}
}
