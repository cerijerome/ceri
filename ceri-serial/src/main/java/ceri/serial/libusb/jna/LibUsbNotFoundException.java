package ceri.serial.libusb.jna;

import static ceri.serial.libusb.jna.LibUsb.libusb_error.LIBUSB_ERROR_NOT_FOUND;
import ceri.serial.libusb.jna.LibUsb.libusb_error;

/**
 * Specific exception for when a usb device is not found. The separate exception allows for
 * detection apart from I/O errors.
 */
public class LibUsbNotFoundException extends LibUsbException {
	private static final long serialVersionUID = -5799078147060682597L;
	private static final libusb_error ERROR = LIBUSB_ERROR_NOT_FOUND;

	public static LibUsbNotFoundException byFormat(String format, Object... args) {
		return new LibUsbNotFoundException(String.format(format, args));
	}

	public LibUsbNotFoundException(String message) {
		super(message, ERROR);
	}

}
