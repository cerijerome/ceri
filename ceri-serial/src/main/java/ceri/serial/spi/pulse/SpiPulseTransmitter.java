package ceri.serial.spi.pulse;

import static ceri.common.data.ByteUtil.bytes;
import java.util.Arrays;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ceri.common.collection.ArrayUtil;
import ceri.common.collection.ByteReceiver;
import ceri.common.concurrent.BooleanCondition;
import ceri.common.concurrent.RuntimeInterruptedException;
import ceri.common.concurrent.SafeReadWrite;
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
	private final Spi spi;
	private final SpiTransfer xfer;
	private byte[] data;

	public static SpiPulseTransmitter of(Spi spi, SpiPulseConfig config) {
		return new SpiPulseTransmitter(spi, config);
	}

	private SpiPulseTransmitter(Spi spi, SpiPulseConfig config) {
		this.config = config;
		this.spi = spi;
		buffer = config.buffer();
		xfer = spi.transfer(buffer.storageSize()).delayMicros(config.delayMicros);
		data = new byte[buffer.dataSize()];
		start();
	}

	public PulseCycle cycle() {
		return buffer.cycle;
	}

	@Override
	public int length() {
		return buffer.dataSize();
	}

	@Override
	public void set(int pos, int b) {
		copyFrom(pos, bytes(b));
	}

	@Override
	public int copyFrom(int srcOffset, byte[] data, int offset, int len) {
		ArrayUtil.validateSlice(data.length, srcOffset, len);
		ArrayUtil.validateSlice(this.data.length, offset, len);
		safe.write(() -> {
			System.arraycopy(data, srcOffset, this.data, offset, len);
			sync.signal();
		});
		return srcOffset + len;
	}

	@Override
	public int fill(int value, int pos, int length) {
		ArrayUtil.validateSlice(length(), pos, length);
		byte[] fill = new byte[length];
		Arrays.fill(fill, (byte) value);
		return copyFrom(pos, fill);
	}

	@Override
	protected void loop() throws InterruptedException {
		try {
			syncData();
			buffer.writeTo(xfer.out());
			spi.execute(xfer);
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
			buffer.write(data);
		});
	}

}
