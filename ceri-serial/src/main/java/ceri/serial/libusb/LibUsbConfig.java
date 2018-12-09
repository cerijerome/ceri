package ceri.serial.libusb;

import java.io.Closeable;
import ceri.serial.libusb.jna.LibUsb;
import ceri.serial.libusb.jna.LibUsb.libusb_config_descriptor;

public class LibUsbConfig implements Closeable {
	public final LibUsbDevice device;
	public final libusb_config_descriptor config;

	LibUsbConfig(LibUsbDevice device, libusb_config_descriptor config) {
		this.device = device;
		this.config = config;
	}

	@Override
	public void close() {
		LibUsb.libusb_free_config_descriptor(config);
	}

}
