package ceri.serial.jna.libusb;

import java.io.Closeable;
import java.io.IOException;
import java.util.List;
import ceri.serial.jna.JnaUtil;
import ceri.serial.jna.libusb.LibUsb.libusb_context;
import ceri.serial.jna.libusb.LibUsb.libusb_device;
import ceri.serial.jna.libusb.LibUsb.libusb_device_descriptor;

public class UsbController implements Closeable {
	private final libusb_context ctx;

	public static UsbController create() throws IOException {
		return new UsbController();
	}

	private UsbController() throws IOException {
		ctx = LibUsb.libusb_init();
	}

	public UsbDeviceFinder finder() throws IOException {
		return UsbDeviceFinder.of(ctx);
	}

	@Override
	public void close() {
		LibUsb.libusb_exit(ctx);
	}

	public static void main(String[] args) throws IOException {
		JnaUtil.setProtected();
		try (UsbController usb = create()) {
			try (UsbDeviceFinder finder = usb.finder()) {
				List<libusb_device> devices = finder.devices();

				System.out.printf("%d items", devices.size());
				for (libusb_device device : devices) {
					libusb_device_descriptor descriptor =
						LibUsb.libusb_get_device_descriptor(device);
					System.out.println(descriptor);
				}
			}
		}
	}

}
