package ceri.serial.ftdi.jna;

import ceri.serial.libusb.jna.LibUsbException;

public class LibFtdiException extends LibUsbException {
	private static final long serialVersionUID = -7274377830953987618L;

	public LibFtdiException(String message, int code) {
		super(message, code);
	}

	public LibFtdiException(String message, int code, Throwable t) {
		super(message, code);
		initCause(t);
	}

}
