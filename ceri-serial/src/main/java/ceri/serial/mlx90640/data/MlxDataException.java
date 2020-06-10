package ceri.serial.mlx90640.data;

import java.io.IOException;
import ceri.common.text.StringUtil;

public class MlxDataException extends IOException {
	private static final long serialVersionUID = -7816363832884693846L;

	public static MlxDataException of(String format, Object... args) {
		return of(null, format, args);
	}

	public static MlxDataException of(Throwable cause, String format, Object... args) {
		return new MlxDataException(StringUtil.format(format, args), cause);
	}

	private MlxDataException(String message, Throwable cause) {
		super(message, cause);
	}
}
