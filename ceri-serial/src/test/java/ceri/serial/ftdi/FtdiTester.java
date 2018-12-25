package ceri.serial.ftdi;

import static ceri.serial.ftdi.jna.LibFtdi.FTDI_VENDOR_ID;
import static ceri.serial.ftdi.jna.LibFtdi.ftdi_mpsse_mode.BITMODE_BITBANG;
import static ceri.serial.libusb.jna.LibUsbFinder.libusb_find_criteria;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ceri.common.util.BasicUtil;
import ceri.log.util.LogUtil;
import ceri.serial.libusb.jna.LibUsbException;
import ceri.serial.libusb.jna.LibUsbFinder.libusb_device_criteria;

public class FtdiTester {
	private static final Logger logger = LogManager.getLogger();

	public static void main(String[] args) {
		libusb_device_criteria criteria = libusb_find_criteria().vendor(FTDI_VENDOR_ID);
		try (SelfHealingFtdi ftdi =
			SelfHealingFtdi.builder(criteria).bitmode(BITMODE_BITBANG).baud(9600).build()) {
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
