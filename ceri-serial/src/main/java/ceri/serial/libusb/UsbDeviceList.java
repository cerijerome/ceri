package ceri.serial.libusb;

import static ceri.common.collection.ImmutableUtil.convertAsList;
import java.io.Closeable;
import java.util.List;
import java.util.function.Supplier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ceri.log.util.LogUtil;
import ceri.serial.libusb.jna.LibUsb;
import ceri.serial.libusb.jna.LibUsb.libusb_context;
import ceri.serial.libusb.jna.LibUsb.libusb_device;

public class UsbDeviceList implements Closeable {
	private static final Logger logger = LogManager.getLogger();
	private libusb_device.ByReference list;
	public final List<UsbDevice> devices;

	UsbDeviceList(Supplier<libusb_context> contextSupplier, libusb_device.ByReference list) {
		this.list = list;
		this.devices = convertAsList(d -> new UsbDevice(contextSupplier, d), list.typedArray());
	}

	public List<UsbDevice> devices() {
		return list == null ? List.of() : devices;
	}

	@Override
	public void close() {
		LogUtil.execute(logger, () -> LibUsb.libusb_free_device_list(list));
		list = null;
	}

}
