package ceri.serial.ftdi;

import ceri.common.data.TypeTranscoder;

public enum FtdiInterface {
	INTERFACE_A(0, 1, 0x02, 0x81),
	INTERFACE_B(1, 2, 0x04, 0x83),
	INTERFACE_C(2, 3, 0x06, 0x85),
	INTERFACE_D(3, 4, 0x08, 0x87);

	public static final TypeTranscoder.Single<FtdiInterface> xcoder =
		TypeTranscoder.single(t -> t.iface, FtdiInterface.class);
	public final int iface;
	public final int index;
	public final int inEndpoint;
	public final int outEndpoint;
	
	private FtdiInterface(int iface, int index, int inEndpoint, int outEndpoint) {
		this.iface = iface;
		this.index = index;
		this.inEndpoint = inEndpoint;
		this.outEndpoint = outEndpoint;
	}
}