package ceri.serial.spi.pulse;

import java.io.IOException;
import java.io.InputStream;
import java.util.function.IntSupplier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ceri.common.collection.ArrayUtil;
import ceri.common.concurrent.BooleanCondition;
import ceri.common.concurrent.RuntimeInterruptedException;
import ceri.common.concurrent.SafeReadWrite;
import ceri.common.data.ByteProvider;
import ceri.common.data.ByteReceiver;
import ceri.common.util.BasicUtil;
import ceri.log.concurrent.LoopingExecutor;
import ceri.serial.spi.Spi;
import ceri.serial.spi.SpiTransfer;

/**
 * Controls transmission of spi pulse data.
 */
public class SpiPulseTransmitter extends LoopingExecutor implements ByteReceiver {
	private static final Logger logger = LogManager.getLogger();
	private final SafeReadWrite safe = SafeReadWrite.of();
	private final BooleanCondition sync = BooleanCondition.of(safe.conditionLock());
	private final SpiPulseConfig config;
	private final PulseBuffer buffer;
	private final SpiTransfer xfer;
	private final ByteReceiver wrapper;
	private final byte[] data;

	public static SpiPulseTransmitter of(Spi spi, SpiPulseConfig config) {
		return new SpiPulseTransmitter(spi, config);
	}

	private SpiPulseTransmitter(Spi spi, SpiPulseConfig config) {
		this.config = config;
		buffer = config.buffer();
		xfer = spi.transfer(buffer.storageSize()).delayMicros(config.delayMicros);
		data = new byte[buffer.length()];
		wrapper = ByteReceiver.wrap(data);
		start();
	}

	public PulseCycle cycle() {
		return buffer.cycle;
	}

	@Override
	public int length() {
		return buffer.length();
	}

	@Override
	public void set(int pos, int b) {
		copyFrom(pos, ArrayUtil.bytes(b));
	}

	@Override
	public int copyFrom(int pos, byte[] array, int offset, int length) {
		return signal(() -> wrapper.copyFrom(pos, array, offset, length));
	}

	@Override
	public int copyFrom(int pos, ByteProvider provider, int offset, int length) {
		return signal(() -> wrapper.copyFrom(pos, provider, offset, length));
	}

	@Override
	public int fill(int value, int pos, int length) {
		return signal(() -> wrapper.fill(value, pos, length));
	}

	@Override
	public int readFrom(InputStream in, int offset, int length) throws IOException {
		return ByteReceiver.readBufferFrom(this, in, offset, length);
	}

	@Override
	protected void loop() throws InterruptedException {
		try {
			syncData();
			buffer.writePulseTo(xfer.out());
			xfer.execute();
		} catch (InterruptedException | RuntimeInterruptedException e) {
			throw e;
		} catch (Exception e) {
			logger.catching(e);
			BasicUtil.delay(config.resetDelayMs);
		}
	}

	private void syncData() throws InterruptedException {
		safe.write(() -> {
			sync.await();
			buffer.copyFrom(data);
		});
	}

	private int signal(IntSupplier action) {
		return safe.writeWithReturn(() -> {
			int result = action.getAsInt();
			sync.signal();
			return result;
		});
	}

}
