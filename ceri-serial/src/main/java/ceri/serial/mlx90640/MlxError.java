package ceri.serial.mlx90640;

import ceri.common.data.TypeTranscoder;

@Deprecated
public enum MlxError {
	i2cError(-1),
	i2cWriteFailed(-2),
	tooManyBrokenPixels(-3),
	tooManyOutlierPixels(-4),
	tooManyBadPixels(-5),
	badAdjacentBadPixels(-6),
	badData(-8),
	failedToStart(-9);

	private static final TypeTranscoder<MlxError> xcoder =
		TypeTranscoder.of(t -> t.code, MlxError.class);
	public final int code;

	public static MlxError from(int code) {
		return xcoder.decode(code);
	}

	private MlxError(int code) {
		this.code = code;
	}
	
	public MlxException exception(String format, Object...args) {
		return MlxException.of(this, format, args);
	}
}
