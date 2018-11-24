package ceri.serial.ftdi;

import java.util.Arrays;
import com.sun.jna.Pointer;
import ceri.serial.jna.libusb.LibUsb;
import ceri.serial.jna.libusb.LibUsb.libusb_config_descriptor;
import ceri.serial.jna.libusb.LibUsb.libusb_context;
import ceri.serial.jna.libusb.LibUsb.libusb_device;
import ceri.serial.jna.libusb.LibUsb.libusb_device_descriptor;
import ceri.serial.jna.libusb.LibUsb.libusb_device_handle;
import ceri.serial.jna.libusb.LibUsb.libusb_interface;
import ceri.serial.jna.libusb.LibUsbException;

public class BitbangTest {

	public static void main(String[] args) throws Exception {
		libusb_context ctx = LibUsb.libusb_init();
		findFtdi(ctx);
		LibUsb.libusb_exit(ctx);
	}

	private static void findFtdi(libusb_context ctx) throws LibUsbException {
		libusb_device.ArrayRef list = LibUsb.libusb_get_device_list(ctx);
		for (libusb_device device : list.typedArray()) {
			libusb_device_descriptor desc = LibUsb.libusb_get_device_descriptor(device);
			// System.out.println(desc);
			System.out.printf("%04x:%04x:%04x%n", desc.idVendor, desc.idProduct, desc.bcdDevice);
			System.out.printf("bDescriptorType=%s%n", desc.bDescriptorType().get());
			System.out.printf("bDeviceClass=%s%n", desc.bDeviceClass().get());

			libusb_config_descriptor config = LibUsb.libusb_get_config_descriptor(device);
			System.out.printf("bLength=%02x%n", config.bLength);
			System.out.printf("bDescriptorType=%02x %s%n", config.bDescriptorType, config.bDescriptorType());
			System.out.printf("wTotalLength=%02x%n", config.wTotalLength);
			System.out.printf("bNumInterfaces=%02x%n", config.bNumInterfaces);
			System.out.printf("bConfigurationValue=%02x%n", config.bConfigurationValue);
			System.out.printf("iConfiguration=%02x%n", config.iConfiguration);
			System.out.printf("bmAttributes=%02x%n", config.bmAttributes);
			System.out.printf("MaxPower=%02x%n", config.MaxPower);
			System.out.printf("_interface=%s%n", config._interface);
			System.out.printf("extra=%s%n", config.extra);
			System.out.printf("extra_length=%s%n", config.extra_length);
			System.out.printf("interfaces[]=%s%n", Arrays.toString(config.interfaces()));
			System.out.printf("extra[]=%s%n", Arrays.toString(config.extra()));

			libusb_device_handle handle = LibUsb.libusb_open(device);
			String manu = descriptor(handle, desc.iManufacturer);
			String prod = descriptor(handle, desc.iProduct);
			String seri = descriptor(handle, desc.iSerialNumber);
			System.out.printf("Manufacturer=%s%n", manu);
			System.out.printf("Product=%s%n", prod);
			System.out.printf("Serial=%s%n", seri);

			LibUsb.libusb_close(handle);
			System.out.println();
		}
		LibUsb.libusb_free_device_list(list);
	}

	private static String descriptor(libusb_device_handle handle, byte desc_index)
		throws LibUsbException {
		// System.out.printf("libusb_get_string_descriptor_ascii: %d%n", desc_index);
		return LibUsb.libusb_get_string_descriptor_ascii(handle, desc_index);
	}

}
