package ceri.serial.ftdi.jna;

import static ceri.serial.ftdi.jna.LibFtdi.FTDI_VENDOR_ID;
import static ceri.serial.libusb.jna.LibUsbFinder.libusb_find_criteria;
import ceri.serial.ftdi.jna.LibFtdi.ftdi_context;
import ceri.serial.libusb.jna.LibUsbException;
import ceri.serial.libusb.jna.LibUsbFinder.libusb_device_criteria;
import ceri.serial.libusb.jna.LibUsbUtil;

public class LibFtdiUtil {
	public static final int READ_STATUS_BYTES = 2;

	private LibFtdiUtil() {}

	public static libusb_device_criteria finder() {
		return libusb_find_criteria().vendor(FTDI_VENDOR_ID);
	}

	public static void requireDev(ftdi_context ftdi) throws LibUsbException {
		requireCtx(ftdi);
		LibUsbUtil.require(ftdi.usb_dev);
	}

	public static void requireCtx(ftdi_context ftdi) throws LibUsbException {
		require(ftdi);
		LibUsbUtil.require(ftdi.usb_ctx);
	}

	public static void require(ftdi_context ftdi) throws LibUsbException {
		LibUsbUtil.require(ftdi, "Ftdi context");
	}

}
