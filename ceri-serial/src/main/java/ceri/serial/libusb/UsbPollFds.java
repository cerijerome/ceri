package ceri.serial.libusb;

import java.io.Closeable;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ceri.log.util.LogUtil;
import ceri.serial.jna.Struct;
import ceri.serial.libusb.jna.LibUsb;
import ceri.serial.libusb.jna.LibUsb.libusb_pollfd;

public class UsbPollFds implements Closeable {
	private static final Logger logger = LogManager.getLogger();
	private libusb_pollfd.ByReference ref;

	UsbPollFds(libusb_pollfd.ByReference ref) {
		this.ref = ref;
	}

	public List<libusb_pollfd> list() {
		if (ref == null) return List.of();
		libusb_pollfd[] array =
			Struct.arrayByRef(ref.getPointer(), libusb_pollfd::new, libusb_pollfd[]::new);
		return List.of(array);
	}

	@Override
	public void close() {
		LogUtil.execute(logger, () -> LibUsb.libusb_free_pollfds(ref));
		ref = null;
	}
}
