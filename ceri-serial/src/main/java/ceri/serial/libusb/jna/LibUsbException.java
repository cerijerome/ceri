package ceri.serial.libusb.jna;

import ceri.common.exception.ExceptionAdapter;
import ceri.common.exception.ExceptionUtil;
import ceri.common.text.StringUtil;
import ceri.jna.clib.jna.CException;
import ceri.serial.libusb.jna.LibUsb.libusb_error;

/**
 * Exception for libusb errors. Holds libusb_error (may be null) and code.
 */
@SuppressWarnings("serial")
public class LibUsbException extends CException {
	public static final ExceptionAdapter<LibUsbException> ADAPTER =
		ExceptionAdapter.of(LibUsbException.class, LibUsbException::adapt);
	public final libusb_error error;

	/**
	 * Create exception without adding the error code to the message.
	 */
	public static LibUsbException of(libusb_error error, String format, Object... args) {
		return new LibUsbException(error, code(error), StringUtil.format(format, args));
	}

	/**
	 * Create exception adding the error code to the message.
	 */
	public static LibUsbException full(int code, String format, Object... args) {
		var error = LibUsb.libusb_error.xcoder.decode(code);
		return new LibUsbException(error, code, format(error, code, format, args));
	}

	/**
	 * Create exception adding the error code to the message.
	 */
	public static LibUsbException full(libusb_error error, String format, Object... args) {
		var code = code(error);
		return new LibUsbException(error, code, format(error, code, format, args));
	}

	protected LibUsbException(libusb_error error, int code, String message) {
		super(code, message);
		this.error = error;
	}

	private static int code(libusb_error error) {
		return error != null ? error.value : libusb_error.LIBUSB_ERROR_OTHER.value;
	}

	private static String format(libusb_error error, int code, String message, Object... args) {
		return "[" + (error == null ? code : error) + "] " + StringUtil.format(message, args);
	}

	private static LibUsbException adapt(Throwable e) {
		var error = libusb_error.LIBUSB_ERROR_OTHER;
		String message = e.getMessage();
		if (message == null) message = error.toString();
		else message += ": " + error;
		return ExceptionUtil.initCause(of(error, message), e);
	}
}
