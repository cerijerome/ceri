package ceri.serial.libusb.jna;

import ceri.serial.jna.CException;
import ceri.serial.libusb.jna.LibUsb.libusb_error;

public class LibUsbException extends CException {
	private static final long serialVersionUID = 3696913945167490798L;
	public final libusb_error error;

	public static int verify(int result, String name) throws LibUsbException {
		if (result >= 0) return result;
		libusb_error error = libusb_error.xcoder.decode(result);
		throw new LibUsbException(String.format("%s failed: %d (%s)", name, result, error), error,
			result);
	}

	public LibUsbException(String message, libusb_error error) {
		this(message, error, error.value);
	}

	public LibUsbException(String message, int code) {
		this(message, null, code);
	}

	public LibUsbException(String message, libusb_error error, int code) {
		super(message, code);
		this.error = error;
	}

}
