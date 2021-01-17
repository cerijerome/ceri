package ceri.serial.libusb;

import static ceri.common.math.MathUtil.ubyte;
import static ceri.serial.libusb.jna.LibUsb.libusb_get_active_config_descriptor;
import static ceri.serial.libusb.jna.LibUsb.libusb_get_bus_number;
import static ceri.serial.libusb.jna.LibUsb.libusb_get_config_descriptor;
import static ceri.serial.libusb.jna.LibUsb.libusb_get_config_descriptor_by_value;
import static ceri.serial.libusb.jna.LibUsb.libusb_get_device_address;
import static ceri.serial.libusb.jna.LibUsb.libusb_get_device_descriptor;
import static ceri.serial.libusb.jna.LibUsb.libusb_get_device_speed;
import static ceri.serial.libusb.jna.LibUsb.libusb_get_max_iso_packet_size;
import static ceri.serial.libusb.jna.LibUsb.libusb_get_max_packet_size;
import static ceri.serial.libusb.jna.LibUsb.libusb_get_parent;
import static ceri.serial.libusb.jna.LibUsb.libusb_get_port_number;
import static ceri.serial.libusb.jna.LibUsb.libusb_get_port_numbers;
import static ceri.serial.libusb.jna.LibUsb.libusb_open;
import static ceri.serial.libusb.jna.LibUsb.libusb_ref_device;
import static ceri.serial.libusb.jna.LibUsb.libusb_unref_device;
import java.io.Closeable;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.sun.jna.Pointer;
import ceri.common.data.IntArray;
import ceri.common.data.IntProvider;
import ceri.common.function.FunctionUtil;
import ceri.common.text.ToString;
import ceri.log.util.LogUtil;
import ceri.serial.jna.PointerRef;
import ceri.serial.libusb.jna.LibUsb;
import ceri.serial.libusb.jna.LibUsb.libusb_config_descriptor;
import ceri.serial.libusb.jna.LibUsb.libusb_device;
import ceri.serial.libusb.jna.LibUsb.libusb_device_handle;
import ceri.serial.libusb.jna.LibUsb.libusb_speed;
import ceri.serial.libusb.jna.LibUsbException;

public class UsbDevice implements Closeable {
	private static final Logger logger = LogManager.getLogger();
	private final Usb usb;
	private libusb_device device;
	private int refs;

	public static class Devices implements Closeable {
		private static final Logger logger = LogManager.getLogger();
		private PointerRef<libusb_device> list;
		private final List<UsbDevice> devices;

		Devices(PointerRef<libusb_device> list, List<UsbDevice> devices) {
			this.list = list;
			this.devices = devices;
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

	UsbDevice(Usb usb, libusb_device device) {
		this(usb, device, 0);
	}

	UsbDevice(Usb usb, libusb_device device, int refs) {
		this.usb = usb;
		this.device = device;
		this.refs = refs;
	}

	public Usb usb() {
		return usb;
	}

	public void ref() throws LibUsbException {
		libusb_ref_device(device());
		refs++;
	}

	public void unref() throws LibUsbException {
		if (refs <= 0) return;
		libusb_unref_device(device());
		refs--;
	}

	public UsbDevice parent() throws LibUsbException {
		libusb_device parent = libusb_get_parent(device());
		return new UsbDevice(usb, parent);
	}

	public UsbDeviceHandle open() throws LibUsbException {
		libusb_device_handle handle = libusb_open(device());
		return new UsbDeviceHandle(usb, handle);
	}

	public int busNumber() throws LibUsbException {
		return ubyte(libusb_get_bus_number(device()));
	}

	public int portNumber() throws LibUsbException {
		return ubyte(libusb_get_port_number(device()));
	}

	public IntProvider portNumbers() throws LibUsbException {
		byte[] portNumbers = libusb_get_port_numbers(device());
		int[] ints = new int[portNumbers.length];
		for (int i = 0; i < ints.length; i++)
			ints[i] = ubyte(portNumbers[i]);
		return IntArray.Immutable.wrap(ints);
	}

	public int address() throws LibUsbException {
		return ubyte(libusb_get_device_address(device()));
	}

	public libusb_speed speed() throws LibUsbException {
		return libusb_get_device_speed(device());
	}

	public int maxPacketSize(int endpoint) throws LibUsbException {
		return libusb_get_max_packet_size(device(), (byte) endpoint);
	}

	public int maxIsoPacketSize(int endpoint) throws LibUsbException {
		return libusb_get_max_iso_packet_size(device(), (byte) endpoint);
	}

	public UsbDescriptors.Config activeConfig() throws LibUsbException {
		libusb_config_descriptor config = libusb_get_active_config_descriptor(device());
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
		libusb_config_descriptor config = libusb_get_config_descriptor(device(), (byte) index);
		return new UsbDescriptors.Config(this, config);
	}

	/**
	 * Gets configuration from value in descriptor (usually 1-based index?)
	 */
	public UsbDescriptors.Config configByValue(int value) throws LibUsbException {
		libusb_config_descriptor config = libusb_get_config_descriptor_by_value(device(), value);
		return new UsbDescriptors.Config(this, config);
	}

	public UsbDescriptors.Device descriptor() throws LibUsbException {
		var desc = libusb_get_device_descriptor(device());
		return new UsbDescriptors.Device(desc);
	}

	@Override
	public void close() {
		LogUtil.execute(logger, () -> {
			while (refs > 0)
				unref();
		});
		device = null;
	}

	@Override
	public String toString() {
		Pointer devicePtr = FunctionUtil.safeApply(device, libusb_device::getPointer);
		return ToString.forClass(this, devicePtr, refs);
	}

	private libusb_device device() {
		if (device != null) return device;
		throw new IllegalStateException("Device has been closed");
	}

}
