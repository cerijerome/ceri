package ceri.common.io;

import ceri.common.text.StringUtil;

/**
 * Runtime end-of-file exception.
 */
public class RuntimeEofException extends RuntimeIoException {
	private static final long serialVersionUID = 1926088442969855303L;

	public static RuntimeEofException of() {
		return new RuntimeEofException(null, null);
	}

	public static RuntimeEofException of(String format, Object... args) {
		return of(null, format, args);
	}

	public static RuntimeEofException of(Throwable t, String format, Object... args) {
		return new RuntimeEofException(StringUtil.format(format, args), t);
	}

	private RuntimeEofException(String message, Throwable e) {
		super(message, e);
	}

}
