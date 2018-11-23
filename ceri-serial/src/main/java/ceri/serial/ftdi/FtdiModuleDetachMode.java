package ceri.serial.ftdi;

import ceri.common.data.TypeTranscoder;

public enum FtdiModuleDetachMode {
	AUTO_DETACH_SIO_MODULE(0),
	DONT_DETACH_SIO_MODULE(1),
	AUTO_DETACH_REATACH_SIO_MODULE(2);

	public static final TypeTranscoder.Single<FtdiModuleDetachMode> xcoder =
		TypeTranscoder.single(t -> t.value, FtdiModuleDetachMode.class);
	public final int value;

	private FtdiModuleDetachMode(int value) {
		this.value = value;
	}
}