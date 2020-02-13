package ceri.serial.mcp;

import java.io.IOException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ceri.common.collection.ArrayUtil;
import ceri.common.data.ByteUtil;
import ceri.log.util.LogUtil;
import ceri.serial.spi.Spi;
import ceri.serial.spi.Spi.Direction;
import ceri.serial.spi.SpiDevice;
import ceri.serial.spi.SpiTransfer;

public class Mcp3008Device implements Mcp3008 {
	private static final Logger logger = LogManager.getLogger();
	private static final byte START = 0x01;
	private static final int READ_MASK = MAX_VALUE;
	private final Spi spi;
	private final SpiTransfer xfer;

	public static Mcp3008Device open(int bus, int chip, int speedHz) throws IOException {
		return new Mcp3008Device(bus, chip, speedHz);
	}

	private Mcp3008Device(int bus, int chip, int speedHz) throws IOException {
		try {
			spi = SpiDevice.open(bus, chip, Direction.duplex);
			spi.maxSpeedHz(speedHz);
			xfer = spi.transfer(DATA_SIZE);
		} catch (IOException | RuntimeException e) {
			close();
			throw e;
		}
	}

	@Override
	public int value(Mcp3008Input input) throws IOException {
		xfer.write(ArrayUtil.bytes(START, input.encode(), 0));
		byte[] result = xfer.execute().read();
		if (result.length != DATA_SIZE) return 0;
		return (int) ByteUtil.fromLittleEndian(result, 1) & READ_MASK;
	}

	@Override
	public void close() {
		LogUtil.close(logger, spi);
	}

}
