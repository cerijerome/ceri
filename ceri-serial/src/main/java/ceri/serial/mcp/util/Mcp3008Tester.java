package ceri.serial.mcp.util;

import java.io.IOException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ceri.common.data.ByteUtil;
import ceri.common.util.BasicUtil;
import ceri.common.util.OsUtil;
import ceri.common.util.StartupValues;
import ceri.log.util.LogUtil;
import ceri.serial.mcp.Mcp3008;
import ceri.serial.spi.Spi;
import ceri.serial.spi.Spi.Direction;
import ceri.serial.spi.SpiDevice;
import ceri.serial.spi.SpiMode;
import ceri.serial.spi.SpiTransfer;
import ceri.serial.spi.util.SpiEmulator;

public class Mcp3008Tester {
	private static final Logger logger = LogManager.getLogger();

	@SuppressWarnings("resource")
	public static void main(String[] args) throws IOException {
		StartupValues v = LogUtil.startupValues(args);
		int value = v.next("value").asInt(0x8001); // SGL, CH0
		int speedHz = v.next("speedHz").asInt(100000);
		int mode = v.next("mode").asInt(0);
		int delay = v.next("delay").asInt(0);
		int repeat = v.next("repeat").asInt(20);
		int repeatDelayMs = v.next("repeatDelayMs").asInt(500);
		Direction direction = v.next("direction").apply(Direction::valueOf, Direction.duplex);
		int size = v.next("size").asInt(3);
		int bus = v.next("bus").asInt(0);
		int chip = v.next("chip").asInt(0);

		byte[] send = ByteUtil.toLsb(value, Mcp3008.DATA_SIZE);
		boolean emulator = OsUtil.IS_MAC;

		logger.info("Opening /dev/spidev{}.{}", bus, chip);
		try (Spi spi = openSpi(emulator, bus, chip, direction)) {
			spi.mode(SpiMode.of(mode));
			spi.maxSpeedHz(speedHz);
			logger.info("Sending: {}", ByteUtil.toHex(send, "-"));
			SpiTransfer xfer = spi.transfer(size).delayMicros(delay);
			xfer.write(send);
			long t0 = System.currentTimeMillis();
			for (int i = 0; i < repeat; i++) {
				byte[] response = xfer.execute().read();
				logger.info("Response: {}", ByteUtil.toHex(response, "-"));
				BasicUtil.delay(repeatDelayMs);
			}
			long t1 = System.currentTimeMillis();
			logger.info(String.format("Time taken: %.2fs%n", (t1 - t0) / 1000.0));
		}
	}

	private static Spi openSpi(boolean emulator, int bus, int chip, Direction direction)
		throws IOException {
		if (!emulator) return SpiDevice.open(bus, chip, direction);
		logger.info("[Emulating spi device]");
		return SpiEmulator.echo(bus, chip, direction);
	}

}
