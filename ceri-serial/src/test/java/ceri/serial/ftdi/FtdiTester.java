package ceri.serial.ftdi;

import static ceri.serial.ftdi.jna.LibFtdi.FTDI_VENDOR_ID;
import static ceri.serial.libusb.jna.LibUsbFinder.libusb_find_criteria;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ceri.common.util.BasicUtil;
import ceri.log.util.LogUtil;
import ceri.serial.libusb.jna.LibUsbException;
import ceri.serial.libusb.jna.LibUsbFinder.libusb_device_criteria;

public class FtdiTester {
	private static final Logger logger = LogManager.getLogger();

	public static void main(String[] args) throws LibUsbException {
		try (Ftdi ftdi = Ftdi.create()) {
			libusb_device_criteria criteria = libusb_find_criteria().vendor(FTDI_VENDOR_ID);
			ftdi.open(criteria);
			ftdi.bitbang(true);
			process(ftdi);
		}
	}

	private static void process(Ftdi ftdi) throws LibUsbException {
		int delayMs = 250;
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

	private static void read(Ftdi ftdi) throws LibUsbException {
		logger.info("Reading");
		int value = ftdi.readPins();
		logger.info("Read: 0x{}", LogUtil.toHex(value));
	}

	private static void write(Ftdi ftdi, int value) throws LibUsbException {
		logger.info("Writing");
		int n = ftdi.write(value);
		logger.info("Write: {} byte(s)", n);
	}
}
