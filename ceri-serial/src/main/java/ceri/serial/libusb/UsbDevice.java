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
import java.util.function.Supplier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.sun.jna.Pointer;
import ceri.common.collection.ImmutableUtil;
import ceri.common.data.ByteUtil;
import ceri.common.function.FunctionUtil;
import ceri.common.text.ToString;
import ceri.log.util.LogUtil;
import ceri.serial.libusb.jna.LibUsb.libusb_config_descriptor;
import ceri.serial.libusb.jna.LibUsb.libusb_context;
import ceri.serial.libusb.jna.LibUsb.libusb_device;
import ceri.serial.libusb.jna.LibUsb.libusb_device_descriptor;
import ceri.serial.libusb.jna.LibUsb.libusb_device_handle;
import ceri.serial.libusb.jna.LibUsb.libusb_speed;
import ceri.serial.libusb.jna.LibUsbException;

public class UsbDevice implements Closeable {
	private static final Logger logger = LogManager.getLogger();
	private final Supplier<libusb_context> contextSupplier;
	private libusb_device device;
	private int refs;

	UsbDevice(Supplier<libusb_context> contextSupplier, libusb_device device) {
		this(contextSupplier, device, 0);
	}

	UsbDevice(Supplier<libusb_context> contextSupplier, libusb_device device, int refs) {
		this.contextSupplier = contextSupplier;
		this.device = device;
		this.refs = refs;
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
		return new UsbDevice(contextSupplier, parent);
	}

	public UsbDeviceHandle open() throws LibUsbException {
		libusb_device_handle handle = libusb_open(device());
		return new UsbDeviceHandle(contextSupplier, handle);
	}

	public int busNumber() throws LibUsbException {
		return ubyte(libusb_get_bus_number(device()));
	}

	public int portNumber() throws LibUsbException {
		return ubyte(libusb_get_port_number(device()));
	}

	public List<Integer> portNumbers() throws LibUsbException {
		byte[] portNumbers = libusb_get_port_numbers(device());
		return ImmutableUtil.collectAsList(ByteUtil.ustream(portNumbers).boxed());
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

	public UsbConfig activeConfig() throws LibUsbException {
		libusb_config_descriptor config = libusb_get_active_config_descriptor(device());
		return new UsbConfig(config);
	}

	/**
	 * Gets first configuration.
	 */
	public UsbConfig config() throws LibUsbException {
		return config(0);
	}

	/**
	 * Gets configuration on 0-based index.
	 */
	public UsbConfig config(int index) throws LibUsbException {
		libusb_config_descriptor config = libusb_get_config_descriptor(device(), (byte) index);
		return new UsbConfig(config);
	}

	/**
	 * Gets configuration from value in descriptor (usually 1-based index?)
	 */
	public UsbConfig configByValue(int value) throws LibUsbException {
		libusb_config_descriptor config =
			libusb_get_config_descriptor_by_value(device(), (byte) value);
		return new UsbConfig(config);
	}

	public libusb_device_descriptor descriptor() throws LibUsbException {
		return libusb_get_device_descriptor(device());
	}

	public libusb_device device() {
		if (device != null) return device;
		throw new IllegalStateException("Device has been closed");
	}

	public libusb_context context() {
		return contextSupplier.get();
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

}
