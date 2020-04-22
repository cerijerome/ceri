package ceri.serial.libusb.jna;

import ceri.common.text.StringUtil;
import ceri.serial.clib.jna.CException;
import ceri.serial.libusb.jna.LibUsb.libusb_error;

/**
 * Exception for libusb errors. Holds libusb_error (may be null) and code.
 */
public class LibUsbException extends CException {
	private static final long serialVersionUID = 3696913945167490798L;
	public final libusb_error error;

	public static LibUsbException of(libusb_error error, String format, Object...args) {
		return new LibUsbException(error.value, error, StringUtil.format(format, args));
	}

	public static LibUsbException of(int code, String format, Object...args) {
		libusb_error error = LibUsb.libusb_error.xcoder.decode(code);
		return new LibUsbException(code, error, StringUtil.format(format, args));
	}

	public static LibUsbException full(libusb_error error, String format, Object...args) {
		return new LibUsbException(error.value, error, fullMessage(error.value, error, format, args));
	}

	public static LibUsbException full(int code, String format, Object...args) {
		libusb_error error = LibUsb.libusb_error.xcoder.decode(code);
		return new LibUsbException(code, error, fullMessage(code, error, format, args));
	}
	
	protected LibUsbException(int code, libusb_error error, String message) {
		super(code, message);
		this.error = error;
	}

	private static String fullMessage(int code, libusb_error error, String format, Object...args) {
		String message = StringUtil.format(format, args);
		return String.format("%s: %d (%s)", message, code, error == null ? null : error.name());
	}

}
