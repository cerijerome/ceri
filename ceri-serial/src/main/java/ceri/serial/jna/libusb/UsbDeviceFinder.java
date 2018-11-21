package ceri.serial.jna.libusb;

import java.io.Closeable;
import java.io.IOException;
import java.util.List;
import ceri.common.collection.ImmutableUtil;
import ceri.serial.jna.libusb.LibUsb.libusb_context;
import ceri.serial.jna.libusb.LibUsb.libusb_device;

public class UsbDeviceFinder implements Closeable {
	private final libusb_device.ArrayRef list;

	public static UsbDeviceFinder of(libusb_context ctx) throws IOException {
		return new UsbDeviceFinder(LibUsb.libusb_get_device_list(ctx));
	}

	private UsbDeviceFinder(libusb_device.ArrayRef list) {
		this.list = list;
	}

	public List<libusb_device> devices() {
		return ImmutableUtil.asList(list.typedArray());
	}

	@Override
	public void close() {
		LibUsb.libusb_free_device_list(list);
	}

}
