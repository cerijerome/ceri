package ceri.serial.ftdi.jna;

import ceri.serial.ftdi.jna.LibFtdi.ftdi_context;
import ceri.serial.libusb.jna.LibUsbException;
import ceri.serial.libusb.jna.LibUsbUtil;

public class LibFtdiUtil {
	public static final int READ_STATUS_BYTES = 2;

	private LibFtdiUtil() {}

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
