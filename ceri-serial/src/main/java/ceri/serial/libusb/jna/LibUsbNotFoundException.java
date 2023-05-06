package ceri.serial.libusb.jna;

import static ceri.serial.libusb.jna.LibUsb.libusb_error.LIBUSB_ERROR_NOT_FOUND;

/**
 * Specific exception for when a usb device is not found. The separate exception allows for
 * detection apart from I/O errors.
 */
@SuppressWarnings("serial")
public class LibUsbNotFoundException extends LibUsbException {

	/**
	 * Create exception without adding the error code to the message.
	 */
	public static LibUsbNotFoundException of(String format, Object... args) {
		return new LibUsbNotFoundException(String.format(format, args));
	}

	private LibUsbNotFoundException(String message) {
		super(message, LIBUSB_ERROR_NOT_FOUND.value, LIBUSB_ERROR_NOT_FOUND);
	}

}
