package ceri.serial.ftdi;

import java.io.IOException;

public class FtdiException extends IOException {
	private static final long serialVersionUID = -7274377830953987618L;
	//public final libusb_error error;
	public final int code;

	//public FtdiException(String message, libusb_error error, int code) {
	public FtdiException(int code, String message, Throwable t) {
		super(message, t);
		//this.error = error;
		this.code = code;
	}

	public FtdiException(int code, String message) {
		super(message);
		//this.error = error;
		this.code = code;
	}

}
