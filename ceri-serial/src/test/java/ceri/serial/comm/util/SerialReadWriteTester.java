package ceri.serial.comm.util;

import java.io.IOException;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ceri.common.concurrent.Concurrent;
import ceri.common.io.Io;
import ceri.common.math.Maths;
import ceri.common.test.Testing;
import ceri.log.test.LogModifier;
import ceri.serial.comm.Serial;
import ceri.serial.comm.SerialParams;

/**
 * Continually reads and writes between 2 serial ports, with a baud change halfway.
 */
public class SerialReadWriteTester {
	private static final Logger logger = LogManager.getFormatterLogger();
	private static final int TOTAL_TIME_MS = 5000;
	private static final int CYCLE_DELAY_MS = 50;
	private static final int CYCLES = TOTAL_TIME_MS / CYCLE_DELAY_MS;
	private static final int CYCLE_DIFF_MS = CYCLE_DELAY_MS * 10;
	private static final int STRING_MAX = 16;
	private static final int CHAR_MIN = 'A';
	private static final int CHAR_MAX = 'Z';
	private static final int BAUD1 = 9600;
	private static final int BAUD2 = 250000;

	static {
		LogModifier.set(Level.INFO, SerialReadWriteTester.class);
	}

	public static void main(String[] args) throws Exception {
		var ports = SerialTesting.usbPorts(2);
		try (var x1 = SerialTesting.execSelfHealing(ports[0], BAUD1, s -> readWrite(s))) {
			Concurrent.delay(CYCLE_DIFF_MS);
			try (var x2 = SerialTesting.execSelfHealing(ports[1], BAUD1, s -> readWrite(s))) {
				x2.get();
				x1.get();
			}
		}
	}

	private static void readWrite(SelfHealingSerial serial) {
		for (int i = 0; i < CYCLES; i++) {
			try {
				if (i == CYCLES / 2) changeBaud(serial, BAUD2);
				writeString(serial);
				readString(serial);
				Concurrent.delay(CYCLE_DELAY_MS);
			} catch (IOException e) {
				logger.warn(e);
				Concurrent.delay(CYCLE_DELAY_MS);
			}
		}
	}

	@SuppressWarnings("resource")
	private static void writeString(Serial serial) throws IOException {
		String s = Testing.randomString(Maths.random(1, STRING_MAX), CHAR_MIN, CHAR_MAX);
		serial.out().write(s.getBytes());
		serial.out().flush();
	}

	@SuppressWarnings("resource")
	private static void readString(Serial serial) throws IOException {
		String s = Io.availableString(serial.in());
		if (s.isEmpty()) return;
		if (s.codePoints().filter(i -> i < CHAR_MIN || i > CHAR_MAX).findAny().isEmpty())
			logger.info("%s: %s", serial.port(), s);
		else logger.warn("%s: invalid read ************", serial.port());
	}

	private static void changeBaud(Serial serial, int baud) throws IOException {
		logger.info("%s: setting baud %d ********************", serial.port(), baud);
		serial.params(SerialParams.of(baud));
	}
}
