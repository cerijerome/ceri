package ceri.serial.libusb;

import java.io.Closeable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ceri.log.util.LogUtil;
import ceri.serial.libusb.jna.LibUsb;
import ceri.serial.libusb.jna.LibUsb.libusb_config_descriptor;

public class UsbConfig implements Closeable {
	private static final Logger logger = LogManager.getLogger();
	private libusb_config_descriptor config;

	UsbConfig(libusb_config_descriptor config) {
		this.config = config;
	}

	public libusb_config_descriptor descriptor() {
		return config();
	}

	@Override
	public void close() {
		LogUtil.execute(logger, () -> LibUsb.libusb_free_config_descriptor(config));
		config = null;
	}

	private libusb_config_descriptor config() {
		if (config != null) return config;
		throw new IllegalStateException("Config has been closed");
	}

}
