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
		return new LibUsbException(format(message, code, error), code, error);
	}

	protected LibUsbException(String message, int code, libusb_error error) {
		super(message, code, null);
		this.error = error;
	}

	private static libusb_error error(int code) {
		return LibUsb.libusb_error.xcoder.decode(code);
	}

	private static int code(libusb_error error) {
		return error != null ? error.value : libusb_error.LIBUSB_ERROR_OTHER.value;
	}

	private static String format(String message, int code, libusb_error error) {
		return String.format("%s: %s", message, error == null ? code : error);
	}
	
	private static LibUsbException adapt(Throwable e) {
		var error = libusb_error.LIBUSB_ERROR_OTHER;
		String message = e.getMessage();
		if (message == null) message = error.toString();
		else message += ": " + error;
		return ExceptionUtil.initCause(of(error, message), e);
	}
}
