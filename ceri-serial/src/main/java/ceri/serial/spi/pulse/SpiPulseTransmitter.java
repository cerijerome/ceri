package ceri.serial.spi.pulse;

import java.io.IOException;
import java.io.InputStream;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ceri.common.concurrent.BooleanCondition;
import ceri.common.concurrent.ConcurrentUtil;
import ceri.common.concurrent.RuntimeInterruptedException;
import ceri.common.concurrent.SafeReadWrite;
import ceri.common.data.ByteProvider;
import ceri.common.data.ByteReceiver;
import ceri.common.exception.ExceptionTracker;
import ceri.log.concurrent.LoopingExecutor;
import ceri.serial.spi.Spi;
import ceri.serial.spi.Spi.Direction;
import ceri.serial.spi.SpiTransfer;

/**
 * Provides a ByteReceiver interface for writing pulse data to an Spi device.
 */
public class SpiPulseTransmitter extends LoopingExecutor implements ByteReceiver {
	private static final Logger logger = LogManager.getLogger();
	private final SafeReadWrite safe = SafeReadWrite.of();
	private final BooleanCondition sync = BooleanCondition.of(safe.conditionLock());
	private final SpiPulseConfig config;
	private final int id;
	private final PulseBuffer buffer;
	private final SpiTransfer xfer;
	private final ExceptionTracker exceptions = ExceptionTracker.of();

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
		return safe.writeWithReturn(() -> buffer.setByte(pos, b));
	}

	@Override
	public int copyFrom(int pos, byte[] array, int offset, int length) {
		return safe.writeWithReturn(() -> buffer.copyFrom(pos, array, offset, length));
	}

	@Override
	public int copyFrom(int pos, ByteProvider provider, int offset, int length) {
		return safe.writeWithReturn(() -> buffer.copyFrom(pos, provider, offset, length));
	}

	@Override
	public int fill(int pos, int length, int value) {
		return safe.writeWithReturn(() -> buffer.fill(pos, length, value));
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
		safe.write(() -> {
			sync.await();
			buffer.writePulseTo(xfer.out());
		});
	}

	private static String logName(int id, SpiPulseConfig config) {
		return String.format("%s(%d:%d:%s)", SpiPulseTransmitter.class.getSimpleName(), id,
			config.size, config.cycle);
	}

}
