package ceri.serial.libusb.jna;

import ceri.serial.jna.CException;
import ceri.serial.libusb.jna.LibUsb.libusb_error;

public class LibUsbException extends CException {
	private static final long serialVersionUID = 3696913945167490798L;
	public final libusb_error error;

	public static LibUsbException fullMessage(String message, libusb_error error) {
		return new LibUsbException(fullMessage(message, error, error.value), error);
	}

	public static LibUsbException fullMessage(String message, int code) {
		libusb_error error = LibUsb.libusb_error.xcoder.decode(code);
		return new LibUsbException(fullMessage(message, error, code), error, code);
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

	private static String fullMessage(String message, libusb_error error, int code) {
		return String.format("%s: %d (%s)", message, code, error);
	}

}
