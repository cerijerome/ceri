package ceri.serial.ftdi;

import ceri.serial.ftdi.jna.LibFtdi.ftdi_string_descriptors;

public class FtdiDescriptor {
	public static final FtdiDescriptor NULL = of("", "", "");
	public final String manufacturer;
	public final String description;
	public final String serial;

	public static FtdiDescriptor from(ftdi_string_descriptors descriptors) {
		return of(descriptors.manufacturer, descriptors.description, descriptors.serial);
	}

	public static FtdiDescriptor of(String manufacturer, String description, String serial) {
		return new FtdiDescriptor(manufacturer, description, serial);
	}

	FtdiDescriptor(String manufacturer, String description, String serial) {
		this.manufacturer = manufacturer;
		this.description = description;
		this.serial = serial;
	}
}
