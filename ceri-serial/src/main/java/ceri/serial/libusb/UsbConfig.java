package ceri.serial.libusb;

import static ceri.serial.libusb.jna.LibUsb.libusb_free_config_descriptor;
import java.io.Closeable;
import ceri.serial.libusb.jna.LibUsb.libusb_config_descriptor;

public class UsbConfig implements Closeable {
	private libusb_config_descriptor config;

	UsbConfig(libusb_config_descriptor config) {
		this.config = config;
	}

	public libusb_config_descriptor descriptor() {
		return config();
	}

	@Override
	public void close() {
		libusb_free_config_descriptor(config);
		config = null;
	}

	private libusb_config_descriptor config() {
		if (config != null) return config;
		throw new IllegalStateException("Config has been closed");
	}

}
