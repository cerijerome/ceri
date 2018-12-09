package ceri.serial.libusb;

import java.io.Closeable;
import java.util.List;
import ceri.common.collection.ImmutableUtil;
import ceri.serial.libusb.jna.LibUsb;
import ceri.serial.libusb.jna.LibUsb.libusb_device;

public class LibUsbDeviceList implements Closeable {
	public final LibUsbContext ctx;
	private final libusb_device.ByReference list;
	public final List<LibUsbDevice> devices;

	LibUsbDeviceList(LibUsbContext ctx, libusb_device.ByReference list) {
		this.ctx = ctx;
		this.list = list;
		this.devices =
			ImmutableUtil.convertAsList(d -> new LibUsbDevice(ctx, d), list.typedArray());
	}

	@Override
	public void close() {
		LibUsb.libusb_free_device_list(list);
	}

}
