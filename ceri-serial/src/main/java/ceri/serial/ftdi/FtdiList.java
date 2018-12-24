package ceri.serial.ftdi;

import java.io.Closeable;
import java.util.List;
import ceri.serial.libusb.UsbDevice;

public class FtdiList implements Closeable {
	private final List<UsbDevice> devices;

	FtdiList(List<UsbDevice> devices) {
		this.devices = devices;
	}

	public List<UsbDevice> devices() {
		return List.copyOf(devices);
	}

	@Override
	public void close() {
		devices.forEach(UsbDevice::close);
		devices.clear();
	}

}
