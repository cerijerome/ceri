package ceri.serial.comm.util;

import java.io.IOException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.sun.jna.Memory;
import ceri.common.concurrent.SimpleExecutor;
import ceri.common.function.Excepts.Consumer;
import ceri.common.function.Excepts.IntConsumer;
import ceri.common.test.TestUtil;
import ceri.jna.clib.jna.CIoctl;
import ceri.jna.clib.jna.CUnistd;
import ceri.serial.comm.SerialParams;
import ceri.serial.comm.SerialPort;
import ceri.serial.comm.jna.CSerial;

/**
 * Support for serial testing.
 */
public class SerialTestUtil {
	private static final Logger logger = LogManager.getFormatterLogger();

	private SerialTestUtil() {}

	public static String usbPort() throws IOException {
		return usbPorts(1)[0];
	}

	public static String[] usbPorts(int min) throws IOException {
		var paths = PortSupplier.locator(null).usbPorts();
		if (paths.size() >= min) return paths.toArray(String[]::new);
		throw new IOException(min + " serial port(s) required: " + paths);
	}

	public static void clear(int fd) throws IOException {
		int n = CIoctl.fionread(fd);
		if (n > 0) try (var m = new Memory(n)) {
			n = CUnistd.read(fd, m, n);
			logger.info("Cleared = " + n);
		}
	}

	public static void applySelfHealing(String path, Integer baud,
		Consumer<IOException, SelfHealingSerial> consumer) throws IOException {
		try (var serial = SelfHealingSerial.of(SelfHealingSerial.Config.of(path))) {
			serial.open();
			if (baud != null) serial.params(SerialParams.of(baud));
			consumer.accept(serial);
		}
	}

	public static void applySerial(String path, Integer baud,
		Consumer<IOException, SerialPort> consumer) throws IOException {
		try (var serial = SerialPort.open(path)) {
			if (baud != null) serial.params(SerialParams.of(baud));
			consumer.accept(serial);
		}
	}

	public static void applyFd(String path, int baud, IntConsumer<IOException> consumer)
		throws IOException {
		int fd = CSerial.open(path);
		try {
			CSerial.setParams(fd, baud, 8, 1, 0);
			CSerial.setFlowControl(fd, CSerial.FLOWCONTROL_NONE);
			consumer.accept(fd);
		} finally {
			CUnistd.closeSilently(fd);
		}
	}

	public static SimpleExecutor<RuntimeException, ?> execSelfHealing(String path, Integer baud,
		Consumer<IOException, SelfHealingSerial> consumer) {
		return TestUtil.threadRun(() -> applySelfHealing(path, baud, consumer));
	}

	public static SimpleExecutor<RuntimeException, ?> execSerial(String path, Integer baud,
		Consumer<IOException, SerialPort> consumer) {
		return TestUtil.threadRun(() -> applySerial(path, baud, consumer));
	}

	public static SimpleExecutor<RuntimeException, ?> execFd(String path, int baud,
		IntConsumer<IOException> consumer) {
		return TestUtil.threadRun(() -> applyFd(path, baud, consumer));
	}

}
