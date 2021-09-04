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

	/**
	 * Create exception without adding the error code to the message.
	 */
	public static LibUsbException of(libusb_error error, String format, Object... args) {
		return new LibUsbException(StringUtil.format(format, args), code(error), error);
	}

	/**
	 * Create exception adding the error code to the message.
	 */
	public static LibUsbException full(String message, int code) {
		return full(message, code, error(code));
	}

	/**
	 * Create exception adding the error code to the message.
	 */
	public static LibUsbException full(String message, libusb_error error) {
		return full(message, code(error), error);
	}

	private static LibUsbException full(String message, int code, libusb_error error) {
		String full =
			String.format("%s: %d (%s)", message, code, error == null ? null : error.name());
		return new LibUsbException(full, code, error);
	}

	protected LibUsbException(String message, int code, libusb_error error) {
		super(message, code);
		this.error = error;
	}

	private static libusb_error error(int code) {
		return LibUsb.libusb_error.xcoder.decode(code);
	}

	private static int code(libusb_error error) {
		return error != null ? error.value : libusb_error.LIBUSB_ERROR_OTHER.value;
	}
}
