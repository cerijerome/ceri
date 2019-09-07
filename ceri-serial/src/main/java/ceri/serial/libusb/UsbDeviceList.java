package ceri.serial.libusb;

import static ceri.common.collection.ImmutableUtil.convertAsList;
import static ceri.serial.libusb.jna.LibUsb.libusb_free_device_list;
import java.io.Closeable;
import java.util.List;
import java.util.function.Supplier;
import ceri.serial.libusb.jna.LibUsb.libusb_context;
import ceri.serial.libusb.jna.LibUsb.libusb_device;

public class UsbDeviceList implements Closeable {
	private libusb_device.ByReference list;
	public final List<UsbDevice> devices;

	UsbDeviceList(Supplier<libusb_context> contextSupplier, libusb_device.ByReference list) {
		this.list = list;
		this.devices = convertAsList(d -> new UsbDevice(contextSupplier, d), list.typedArray());
	}

	public List<UsbDevice> devices() {
		return List.copyOf(devices);
	}

	@Override
	public void close() {
		libusb_free_device_list(list);
		list = null;
		devices.clear();
	}

}
