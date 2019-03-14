package ceri.serial.ftdi;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ceri.common.util.BasicUtil;
import ceri.log.util.LogUtil;
import ceri.serial.ftdi.util.SelfHealingFtdi;
import ceri.serial.ftdi.util.SelfHealingFtdiConfig;
import ceri.serial.libusb.jna.LibUsbException;

public class FtdiTester {
	private static final Logger logger = LogManager.getLogger();

	public static void main(String[] args) {
		SelfHealingFtdiConfig config = SelfHealingFtdiConfig.of();
		try (SelfHealingFtdi ftdi = SelfHealingFtdi.of(config)) {
			ftdi.openQuietly();
			while (true) {
				try {
					process(ftdi);
				} catch (LibUsbException | RuntimeException e) {
					logger.error(e.getMessage());
					BasicUtil.delay(3000);
				}
			}
		}
	}

	private static void process(SelfHealingFtdi ftdi) throws LibUsbException {
		int delayMs = 500;
		read(ftdi);
		BasicUtil.delay(delayMs);
		for (int i = 0; i < 16; i++) {
			write(ftdi, i);
			BasicUtil.delay(delayMs);
			read(ftdi);
			BasicUtil.delay(delayMs);
		}
		logger.info("Done");
	}

	private static void read(SelfHealingFtdi ftdi) throws LibUsbException {
		logger.info("Reading");
		int value = ftdi.readPins();
		logger.info("Read: 0x{}", LogUtil.toHex(value));
	}

	private static void write(SelfHealingFtdi ftdi, int value) throws LibUsbException {
		logger.info("Writing");
		int n = ftdi.write(value);
		logger.info("Write: {} byte(s)", n);
	}
}
