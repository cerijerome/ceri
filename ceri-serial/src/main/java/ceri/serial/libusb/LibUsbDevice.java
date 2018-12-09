package ceri.serial.libusb;

import static ceri.serial.jna.JnaUtil.ubyte;
import java.util.List;
import ceri.common.collection.ImmutableUtil;
import ceri.common.data.ByteUtil;
import ceri.serial.libusb.jna.LibUsb;
import ceri.serial.libusb.jna.LibUsb.libusb_config_descriptor;
import ceri.serial.libusb.jna.LibUsb.libusb_device;
import ceri.serial.libusb.jna.LibUsb.libusb_device_descriptor;
import ceri.serial.libusb.jna.LibUsb.libusb_device_handle;
import ceri.serial.libusb.jna.LibUsb.libusb_speed;
import ceri.serial.libusb.jna.LibUsbException;

public class LibUsbDevice {
	public final LibUsbContext ctx;
	private final libusb_device device;

	LibUsbDevice(LibUsbContext ctx, libusb_device device) {
		this.ctx = ctx;
		this.device = device;
	}

	public void ref() throws LibUsbException {
		// TODO: assign returned device?
		LibUsb.libusb_ref_device(device);
	}

	public void unref() {
		LibUsb.libusb_unref_device(device);
	}

	public LibUsbDevice parent() throws LibUsbException {
		libusb_device parent = LibUsb.libusb_get_parent(device);
		return new LibUsbDevice(ctx, parent);
	}

	public LibUsbDeviceHandle open() throws LibUsbException {
		libusb_device_handle handle = LibUsb.libusb_open(device);
		return new LibUsbDeviceHandle(ctx, handle);
	}

	public libusb_device_descriptor descriptor() throws LibUsbException {
		return LibUsb.libusb_get_device_descriptor(device);
	}

	public int busNumber() throws LibUsbException {
		return ubyte(LibUsb.libusb_get_bus_number(device));
	}

	public int portNumber() throws LibUsbException {
		return ubyte(LibUsb.libusb_get_port_number(device));
	}

	public List<Integer> portNumbers() throws LibUsbException {
		byte[] portNumbers = LibUsb.libusb_get_port_numbers(device);
		return ImmutableUtil.collectAsList(ByteUtil.streamOf(portNumbers).boxed());
	}

	public int address() throws LibUsbException {
		return ubyte(LibUsb.libusb_get_device_address(device));
	}

	public libusb_speed speed() throws LibUsbException {
		return LibUsb.libusb_get_device_speed(device);
	}

	public int maxPacketSize(int endpoint) throws LibUsbException {
		return LibUsb.libusb_get_max_packet_size(device, (byte) endpoint);
	}

	public int maxIsoPacketSize(int endpoint) throws LibUsbException {
		return LibUsb.libusb_get_max_iso_packet_size(device, (byte) endpoint);
	}

	public LibUsbConfig activeConfigDescriptor() throws LibUsbException {
		libusb_config_descriptor config = LibUsb.libusb_get_active_config_descriptor(device);
		return new LibUsbConfig(this, config);
	}

	/**
	 * Gets first configuration.
	 */
	public LibUsbConfig configDescriptor() throws LibUsbException {
		return configDescriptor(0);
	}

	/**
	 * Gets configuration on 0-based index.
	 */
	public LibUsbConfig configDescriptor(int index) throws LibUsbException {
		libusb_config_descriptor config = LibUsb.libusb_get_config_descriptor(device, (byte) index);
		return new LibUsbConfig(this, config);
	}

	/**
	 * Gets configuration from value in descriptor (usually 1-based index?)
	 */
	public LibUsbConfig configDescriptorByValue(int value) throws LibUsbException {
		libusb_config_descriptor config =
			LibUsb.libusb_get_config_descriptor_by_value(device, (byte) value);
		return new LibUsbConfig(this, config);
	}

}
