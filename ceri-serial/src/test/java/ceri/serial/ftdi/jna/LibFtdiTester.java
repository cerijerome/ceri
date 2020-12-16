package ceri.serial.ftdi.jna;

import static ceri.serial.ftdi.jna.LibFtdi.FTDI_VENDOR_ID;
import static ceri.serial.ftdi.jna.LibFtdi.ftdi_enable_bitbang;
import static ceri.serial.ftdi.jna.LibFtdi.ftdi_free;
import static ceri.serial.ftdi.jna.LibFtdi.ftdi_new;
import static ceri.serial.ftdi.jna.LibFtdi.ftdi_read_pins;
import static ceri.serial.ftdi.jna.LibFtdi.*;
import static ceri.serial.ftdi.jna.LibFtdi.ftdi_write_data;
import java.nio.ByteBuffer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ceri.common.collection.ArrayUtil;
import ceri.common.concurrent.ConcurrentUtil;
import ceri.log.util.LogUtil;
import ceri.serial.ftdi.jna.LibFtdi.ftdi_context;
import ceri.serial.libusb.jna.LibUsbException;
import ceri.serial.libusb.jna.LibUsbFinder;

public class LibFtdiTester {
	private static final Logger logger = LogManager.getLogger();

	public static void main(String[] args) throws LibUsbException {
		ftdi_context ftdi = ftdi_new();
		try {
			LibUsbFinder finder = LibUsbFinder.builder().vendor(FTDI_VENDOR_ID).build();
			ftdi_usb_open_find(ftdi, finder);
			ftdi_enable_bitbang(ftdi);
			process(ftdi);
		} finally {
			ftdi_free(ftdi);
		}
	}

	private static void process(ftdi_context ftdi) throws LibUsbException {
		int delayMs = 1000;
		read(ftdi);
		ConcurrentUtil.delay(delayMs);
		for (int i = 0; i < 16; i++) {
			write(ftdi, i);
			ConcurrentUtil.delay(delayMs);
			read(ftdi);
			ConcurrentUtil.delay(delayMs);
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
		int n = ftdi_write_data(ftdi, ByteBuffer.wrap(ArrayUtil.bytes(value)), 1);
		logger.info("Write: {} byte(s)", n);
	}
}
