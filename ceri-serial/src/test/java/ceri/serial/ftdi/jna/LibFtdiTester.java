package ceri.serial.ftdi.jna;

import static ceri.serial.ftdi.jna.LibFtdi.FTDI_VENDOR_ID;
import static ceri.serial.ftdi.jna.LibFtdi.ftdi_enable_bitbang;
import static ceri.serial.ftdi.jna.LibFtdi.ftdi_free;
import static ceri.serial.ftdi.jna.LibFtdi.ftdi_new;
import static ceri.serial.ftdi.jna.LibFtdi.ftdi_read_pins;
import static ceri.serial.ftdi.jna.LibFtdi.ftdi_usb_open_criteria;
import static ceri.serial.ftdi.jna.LibFtdi.ftdi_write_data;
import static ceri.serial.libusb.jna.LibUsbFinder.libusb_find_criteria;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ceri.common.util.BasicUtil;
import ceri.log.util.LogUtil;
import ceri.serial.ftdi.jna.LibFtdi.ftdi_context;
import ceri.serial.libusb.jna.LibUsbException;
import ceri.serial.libusb.jna.LibUsbFinder.libusb_device_criteria;

public class LibFtdiTester {
	private static final Logger logger = LogManager.getLogger();

	public static void main(String[] args) throws LibUsbException {
		ftdi_context ftdi = ftdi_new();
		try {
			libusb_device_criteria criteria = libusb_find_criteria().vendor(FTDI_VENDOR_ID);
			ftdi_usb_open_criteria(ftdi, criteria);
			ftdi_enable_bitbang(ftdi);
			process(ftdi);
		} finally {
			ftdi_free(ftdi);
		}
	}

	private static void process(ftdi_context ftdi) throws LibUsbException {
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

	private static void read(ftdi_context ftdi) throws LibUsbException {
		logger.info("Reading");
		int value = ftdi_read_pins(ftdi);
		logger.info("Read: 0x{}", LogUtil.toHex(value));
	}

	private static void write(ftdi_context ftdi, int value) throws LibUsbException {
		logger.info("Writing");
		int n = ftdi_write_data(ftdi, value);
		logger.info("Write: {} byte(s)", n);
	}
}
