package ceri.serial.spi.jna;

import static ceri.common.math.Maths.ushort;
import java.util.concurrent.TimeUnit;
import ceri.common.io.Direction;
import ceri.serial.spi.jna.SpiDev.spi_ioc_transfer;

public class SpiDevUtil {

	private SpiDevUtil() {}

	/**
	 * Determines transfer direction from structure values.
	 */
	public static Direction direction(spi_ioc_transfer xfer) {
		if (xfer.tx_buf == 0L && xfer.rx_buf == 0L && xfer.len > 0)
			throw new IllegalArgumentException("Tx and Rx buffers are null");
		if (xfer.rx_buf == 0L) return Direction.out;
		if (xfer.tx_buf == 0L) return Direction.in;
		return Direction.duplex;
	}

	/**
	 * Estimated data transfer time from transfer structure. Duplex transfers are assumed to happen
	 * simultaneously.
	 */
	public static long transferTimeMicros(spi_ioc_transfer xfer) {
		return transferTimeMicros(xfer, 0);
	}

	/**
	 * Estimated data transfer time from transfer structure and SPI default speed. Duplex transfers
	 * are assumed to happen simultaneously.
	 */
	public static long transferTimeMicros(spi_ioc_transfer xfer, int spiMaxSpeedHz) {
		long t = ushort(xfer.delay_usecs);
		int speedHz = xfer.speed_hz;
		if (speedHz == 0) speedHz = spiMaxSpeedHz;
		if (speedHz == 0) return t;
		return t + (xfer.len * Byte.SIZE * TimeUnit.SECONDS.toMicros(1) / speedHz);
	}
}
