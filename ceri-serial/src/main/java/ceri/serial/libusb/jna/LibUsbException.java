package ceri.serial.libusb.jna;

import ceri.serial.clib.jna.CException;
import ceri.serial.libusb.jna.LibUsb.libusb_error;

/**
 * Exception for libusb errors. Holds libusb_error (may be null) and code.
 */
public class LibUsbException extends CException {
	private static final long serialVersionUID = 3696913945167490798L;
	public final libusb_error error;

	public static LibUsbException fullMessage(String message, libusb_error error) {
		return new LibUsbException(fullMessageText(message, error), error);
	}

	public static LibUsbException fullMessage(String message, int code) {
		libusb_error error = LibUsb.libusb_error.xcoder.decode(code);
		return new LibUsbException(fullMessageText(message, error, code), error, code);
	}

	public LibUsbException(String message, libusb_error error) {
		this(message, error, error.value);
	}

	public LibUsbException(String message, int code) {
		this(message, LibUsb.libusb_error.xcoder.decode(code), code);
	}

	public LibUsbException(String message, libusb_error error, int code) {
		super(message, code);
		this.error = error;
	}

	protected static String fullMessageText(String message, libusb_error error) {
		return fullMessageText(message, error, error.value);
	}

	protected static String fullMessageText(String message, int code) {
		libusb_error error = LibUsb.libusb_error.xcoder.decode(code);
		return fullMessageText(message, error, code);
	}

	private static String fullMessageText(String message, libusb_error error, int code) {
		return String.format("%s: %d (%s)", message, code, error == null ? null : error.name());
	}

}
