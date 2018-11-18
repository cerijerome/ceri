package ceri.serial.jna.libusb;

import static ceri.serial.jna.JnaUtil.verify;
import java.io.IOException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ceri.serial.jna.libusb.LibUsb.LibUsbNative;
import ceri.serial.jna.libusb.LibUsb.libusb_context;
import ceri.serial.jna.libusb.LibUsb.libusb_device;
import ceri.serial.jna.libusb.LibUsb.libusb_device_descriptor;
import ceri.serial.jna.libusb.LibUsb.libusb_version;

public class LibUsbTest {
	private static final Logger logger = LogManager.getLogger();

	public static void main(String[] args) throws IOException {
		logger.info("Started");
		// JnaUtil.setProtected();
		LibUsbNative usb = LibUsb.LIBUSB;

		libusb_context.ByReference ctxPtr = new libusb_context.ByReference();
		verify(usb.libusb_init(ctxPtr), "init");
		libusb_context ctx = ctxPtr.typedValue();

		libusb_version version = usb.libusb_get_version();
		System.out.println(version);

		libusb_device.ArrayRef.ByRef listRef = new libusb_device.ArrayRef.ByRef();
		int size = verify(usb.libusb_get_device_list(ctx, listRef), "get_device_list");
		libusb_device[] devices = listRef.typedValue().typedArray(size);
		System.out.printf("%d items%n", size);
		for (libusb_device device : devices) {
			libusb_device_descriptor descriptor = new libusb_device_descriptor();
			verify(usb.libusb_get_device_descriptor(device, descriptor), "get_device_descriptor");
			System.out.println(descriptor);
		}

		usb.libusb_free_device_list(listRef.typedValue(), size);
		usb.libusb_exit(ctx);
	}

}
