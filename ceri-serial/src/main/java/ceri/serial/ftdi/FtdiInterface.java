package ceri.serial.ftdi;

import static ceri.serial.libusb.jna.LibUsb.libusb_endpoint_address;
import static ceri.serial.libusb.jna.LibUsb.libusb_endpoint_direction.LIBUSB_ENDPOINT_IN;
import static ceri.serial.libusb.jna.LibUsb.libusb_endpoint_direction.LIBUSB_ENDPOINT_OUT;
import ceri.common.data.TypeTranscoder;

public enum FtdiInterface {
	INTERFACE_A(0), // 0, 1, 0x02, 0x81
	INTERFACE_B(1), // 1, 2, 0x04, 0x83
	INTERFACE_C(2), // 2, 3, 0x06, 0x85
	INTERFACE_D(3); // 3, 4, 0x08, 0x87

	public static final TypeTranscoder.Single<FtdiInterface> xcoder =
		TypeTranscoder.single(t -> t.iface, FtdiInterface.class);
	public final int iface;
	public final int index;
	public final byte inEp;
	public final byte outEp;

	private static class Address {
		static int count = 1;
	}

	private FtdiInterface(int iface) {
		this.iface = iface;
		this.index = iface + 1;
		outEp = libusb_endpoint_address(Address.count++, LIBUSB_ENDPOINT_OUT);
		inEp = libusb_endpoint_address(Address.count++, LIBUSB_ENDPOINT_IN);
	}

}