package ceri.serial.pigpio.jna;

import java.nio.ByteBuffer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ceri.common.text.StringUtil;
import ceri.serial.jna.JnaUtil;

public class LibPigpio {
	private static final Logger logger = LogManager.getLogger();
	private static LibPigpioNative PIGPIO = loadLibrary("pigpio");

	public static void gpioInitialise() throws PigpioException {
		verify(PIGPIO.gpioInitialise(), "gpioInitialise");
	}

	public static void gpioTerminate() {
		PIGPIO.gpioTerminate();
	}

	public static int spiOpen(int channel, int baud, int flags) throws PigpioException {
		return verify(PIGPIO.spiOpen(channel, baud, flags), "spiOpen", channel, baud, flags);
	}

	public static void spiClose(int handle) throws PigpioException {
		verify(PIGPIO.spiClose(handle), "spiClose");
	}

	public static int spiRead(int handle, ByteBuffer buffer, int count) throws PigpioException {
		return verify(PIGPIO.spiRead(handle, buffer, count), "spiRead");
	}

	public static int spiWrite(int handle, ByteBuffer buffer, int count) throws PigpioException {
		return verify(PIGPIO.spiWrite(handle, buffer, count), "spiWrite");
	}

	public static int spiXfer(int handle, ByteBuffer txBuffer, ByteBuffer rxBuffer, int count)
		throws PigpioException {
		return verify(PIGPIO.spiXfer(handle, txBuffer, rxBuffer, count), "spiXfer");
	}

	private static int verify(int result, String name, Object... objs) throws PigpioException {
		if (result >= 0) return result;
		String message = StringUtil.toString("libusb_" + name + "(", ") failed", ", ", objs);
		throw PigpioException.fullMessage(message, result);
	}

	private static LibPigpioNative loadLibrary(String name) {
		logger.info("Loading {} started", name);
		LibPigpioNative lib = JnaUtil.loadLibrary(name, LibPigpioNative.class);
		logger.info("Loading {} complete", name);
		return lib;
	}

}
