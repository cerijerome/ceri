package ceri.serial.pigpio.jna;

import ceri.serial.jna.clib.CException;

/**
 * Exception for Pigpio errors. Holds PigpioError (may be null) and code.
 */
public class PigpioException extends CException {
	private static final long serialVersionUID = -1591502698175476184L;
	public final PigpioError error;

	public static PigpioException fullMessage(String message, PigpioError error) {
		return new PigpioException(fullMessageText(message, error), error);
	}

	public static PigpioException fullMessage(String message, int code) {
		PigpioError error = PigpioError.xcoder.decode(code);
		return new PigpioException(fullMessageText(message, error, code), error, code);
	}

	public PigpioException(String message, PigpioError error) {
		this(message, error, error.value);
	}

	public PigpioException(String message, int code) {
		this(message, PigpioError.xcoder.decode(code), code);
	}

	public PigpioException(String message, PigpioError error, int code) {
		super(message, code);
		this.error = error;
	}

	protected static String fullMessageText(String message, PigpioError error) {
		return fullMessageText(message, error, error.value);
	}

	protected static String fullMessageText(String message, int code) {
		PigpioError error = PigpioError.xcoder.decode(code);
		return fullMessageText(message, error, code);
	}

	private static String fullMessageText(String message, PigpioError error, int code) {
		return String.format("%s: %d (%s)", message, code, error == null ? null : error.name());
	}

}
