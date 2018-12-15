package ceri.serial.ftdi.jna;

import ceri.serial.libusb.jna.LibUsb.libusb_error;
import ceri.serial.libusb.jna.LibUsbException;

public class LibFtdiNotFoundException extends LibUsbException {
	private static final long serialVersionUID = -5799078147060682597L;

	public LibFtdiNotFoundException(String message) {
		super(message, libusb_error.LIBUSB_ERROR_NOT_FOUND);
	}

}
