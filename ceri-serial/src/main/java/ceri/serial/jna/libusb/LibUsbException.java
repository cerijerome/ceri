package ceri.serial.jna.libusb;

import java.io.IOException;
import ceri.serial.jna.libusb.LibUsb.libusb_error;

public class LibUsbException extends IOException {
	private static final long serialVersionUID = -7274377830953987618L;
	public final libusb_error error;
	public final int code;

	public LibUsbException(String message, libusb_error error, int code) {
		super(message);
		this.error = error;
		this.code = code;
	}

}
