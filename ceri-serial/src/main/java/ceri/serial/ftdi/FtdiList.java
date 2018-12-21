package ceri.serial.ftdi;

import java.io.Closeable;
import java.util.List;
import ceri.serial.libusb.LibUsbDevice;

public class FtdiList implements Closeable {
	private final List<LibUsbDevice> devices;

	FtdiList(List<LibUsbDevice> devices) {
		this.devices = devices;
	}

	public List<LibUsbDevice> devices() {
		return List.copyOf(devices);
	}

	@Override
	public void close() {
		devices.forEach(LibUsbDevice::close);
		devices.clear();
	}

}
