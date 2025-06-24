package ceri.serial.libusb;

import static ceri.common.math.MathUtil.ubyte;
import java.util.List;
import ceri.common.data.IntArray;
import ceri.common.data.IntProvider;
import ceri.common.function.RuntimeCloseable;
import ceri.common.text.ToString;
import ceri.jna.type.ArrayPointer;
import ceri.jna.util.PointerUtil;
import ceri.log.util.LogUtil;
import ceri.serial.libusb.jna.LibUsb;
import ceri.serial.libusb.jna.LibUsb.libusb_config_descriptor;
import ceri.serial.libusb.jna.LibUsb.libusb_device;
import ceri.serial.libusb.jna.LibUsb.libusb_device_handle;
import ceri.serial.libusb.jna.LibUsb.libusb_speed;
import ceri.serial.libusb.jna.LibUsbException;

public class UsbDevice implements RuntimeCloseable {
	private final Usb usb;
	private libusb_device device;
	private int refs;

	public static class Devices implements RuntimeCloseable {
		private ArrayPointer<libusb_device> list;
		private final List<UsbDevice> devices;

		Devices(ArrayPointer<libusb_device> list, List<UsbDevice> devices) {
			this.list = list;
			this.devices = devices;
		}

		public List<UsbDevice> devices() {
			return devices;
		}

		@Override
		public void close() {
			LogUtil.close(() -> LibUsb.libusb_free_device_list(list));
			list = null;
		}
	}

	UsbDevice(Usb usb, libusb_device device) {
		this.usb = usb;
		this.device = device;
	}

	public Usb usb() {
		return usb;
	}

	public void ref() throws LibUsbException {
		LibUsb.libusb_ref_device(device());
		refs++;
	}

	public void unref() throws LibUsbException {
		if (refs <= 0) return;
		LibUsb.libusb_unref_device(device());
		refs--;
	}

	public UsbDevice parent() throws LibUsbException {
		libusb_device parent = LibUsb.libusb_get_parent(device());
		return parent == null ? null : new UsbDevice(usb, parent);
	}

	public UsbDeviceHandle open() throws LibUsbException {
		libusb_device_handle handle = LibUsb.libusb_open(device());
		return new UsbDeviceHandle(usb, handle);
	}

	public int busNumber() throws LibUsbException {
		return ubyte(LibUsb.libusb_get_bus_number(device()));
	}

	public int portNumber() throws LibUsbException {
		return ubyte(LibUsb.libusb_get_port_number(device()));
	}

	public IntProvider portNumbers() throws LibUsbException {
		byte[] portNumbers = LibUsb.libusb_get_port_numbers(device());
		int[] ints = new int[portNumbers.length];
		for (int i = 0; i < ints.length; i++)
			ints[i] = ubyte(portNumbers[i]);
		return IntArray.Immutable.wrap(ints);
	}

	public int address() throws LibUsbException {
		return ubyte(LibUsb.libusb_get_device_address(device()));
	}

	public libusb_speed speed() throws LibUsbException {
		return LibUsb.libusb_get_device_speed(device());
	}

	public int maxPacketSize(int endpoint) throws LibUsbException {
		return LibUsb.libusb_get_max_packet_size(device(), (byte) endpoint);
	}

	public int maxIsoPacketSize(int endpoint) throws LibUsbException {
		return LibUsb.libusb_get_max_iso_packet_size(device(), (byte) endpoint);
	}

	public UsbDescriptors.Config activeConfig() throws LibUsbException {
		libusb_config_descriptor config = LibUsb.libusb_get_active_config_descriptor(device());
		return new UsbDescriptors.Config(this, config);
	}

	/**
	 * Gets first configuration.
	 */
	public UsbDescriptors.Config config() throws LibUsbException {
		return config(0);
	}

	/**
	 * Gets configuration on 0-based index.
	 */
	public UsbDescriptors.Config config(int index) throws LibUsbException {
		libusb_config_descriptor config =
			LibUsb.libusb_get_config_descriptor(device(), (byte) index);
		return new UsbDescriptors.Config(this, config);
	}

	/**
	 * Gets configuration from value in descriptor (usually 1-based index?)
	 */
	public UsbDescriptors.Config configByValue(int value) throws LibUsbException {
		libusb_config_descriptor config =
			LibUsb.libusb_get_config_descriptor_by_value(device(), value);
		return new UsbDescriptors.Config(this, config);
	}

	public UsbDescriptors.Device descriptor() throws LibUsbException {
		var desc = LibUsb.libusb_get_device_descriptor(device());
		return new UsbDescriptors.Device(desc);
	}

	@Override
	public void close() {
		LogUtil.close(() -> {
			while (refs > 0)
				unref();
		});
		device = null;
	}

	@Override
	public String toString() {
		return ToString.forClass(this, PointerUtil.pointer(device), refs);
	}

	libusb_device device() {
		if (device != null) return device;
		throw new IllegalStateException("Device has been closed");
	}
}
