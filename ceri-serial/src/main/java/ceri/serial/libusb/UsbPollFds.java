package ceri.serial.libusb;

import static ceri.serial.libusb.jna.LibUsb.libusb_free_pollfds;
import java.io.Closeable;
import java.util.List;
import ceri.serial.jna.JnaUtil;
import ceri.serial.libusb.jna.LibUsb.libusb_pollfd;

public class UsbPollFds implements Closeable {
	private libusb_pollfd.ByReference ref;

	UsbPollFds(libusb_pollfd.ByReference ref) {
		this.ref = ref;
	}

	public List<libusb_pollfd> list() {
		if (ref == null) return List.of();
		libusb_pollfd[] array = 
			JnaUtil.arrayByRef(ref.getPointer(), libusb_pollfd::new, libusb_pollfd[]::new);
		return List.of(array);
	}

	@Override
	public void close() {
		libusb_free_pollfds(ref);
		ref = null;
	}
}
