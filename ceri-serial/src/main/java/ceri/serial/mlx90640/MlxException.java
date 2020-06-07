package ceri.serial.mlx90640;

import ceri.common.text.StringUtil;
import ceri.serial.clib.jna.CException;

/**
 * Exception for MLX90640 errors. Holds ErrorCode (may be null) and code.
 */
@Deprecated
public class MlxException extends CException {
	private static final long serialVersionUID = 3696913945167490798L;
	public final MlxError error;

	public static MlxException of(MlxError error, String format, Object... args) {
		return new MlxException(error.code, error, StringUtil.format(format, args));
	}

	public static MlxException of(int code, String format, Object... args) {
		MlxError error = MlxError.from(code);
		return new MlxException(code, error, StringUtil.format(format, args));
	}

	protected MlxException(int code, MlxError error, String message) {
		super(code, message);
		this.error = error;
	}

}
