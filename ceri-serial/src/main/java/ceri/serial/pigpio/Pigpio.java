package ceri.serial.pigpio;

import java.io.Closeable;
import java.io.IOException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ceri.log.util.LogUtil;
import ceri.serial.pigpio.jna.LibPigpio;

public class Pigpio implements Closeable {
	private static final Logger logger = LogManager.getLogger();

	public static Pigpio of() throws IOException {
		LibPigpio.gpioInitialise();
		return new Pigpio();
	}

	private Pigpio() {}

	public PigpioSpi spiOpen(PigpioSpiChannel channel, int baudHz, PigpioSpiFlags flags)
		throws IOException {
		return PigpioSpi.open(channel, baudHz, flags);
	}

	@Override
	public void close() {
		LogUtil.execute(logger, LibPigpio::gpioTerminate);
	}
}
