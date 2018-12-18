package ceri.serial.libusb.jna;

import static ceri.common.util.BasicUtil.isEmpty;
import static ceri.common.util.PrimitiveUtil.decode;
import static ceri.common.util.PrimitiveUtil.valueOf;
import static ceri.serial.jna.JnaUtil.ubyte;
import static ceri.serial.jna.JnaUtil.ushort;
import static ceri.serial.libusb.jna.LibUsb.libusb_close;
import static ceri.serial.libusb.jna.LibUsb.libusb_free_device_list;
import static ceri.serial.libusb.jna.LibUsb.libusb_get_bus_number;
import static ceri.serial.libusb.jna.LibUsb.libusb_get_device_address;
import static ceri.serial.libusb.jna.LibUsb.libusb_get_device_descriptor;
import static ceri.serial.libusb.jna.LibUsb.libusb_get_device_list;
import static ceri.serial.libusb.jna.LibUsb.libusb_get_string_descriptor_ascii;
import static ceri.serial.libusb.jna.LibUsb.libusb_open;
import static ceri.serial.libusb.jna.LibUsb.libusb_ref_device;
import static ceri.serial.libusb.jna.LibUsb.libusb_unref_devices;
import static ceri.serial.libusb.jna.LibUsb.require;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import ceri.common.function.ExceptionPredicate;
import ceri.common.text.DsvParser;
import ceri.common.text.StringUtil;
import ceri.common.util.EqualsUtil;
import ceri.common.util.HashCoder;
import ceri.serial.libusb.jna.LibUsb.libusb_context;
import ceri.serial.libusb.jna.LibUsb.libusb_device;
import ceri.serial.libusb.jna.LibUsb.libusb_device_descriptor;
import ceri.serial.libusb.jna.LibUsb.libusb_device_handle;

public class LibUsbFinder {

	public static class libusb_device_criteria {
		int vendor;
		int product;
		int busNumber;
		int deviceAddress;
		String description;
		String serial;
		int index;

		libusb_device_criteria() {}

		public libusb_device_criteria vendor(int vendor) {
			this.vendor = vendor & 0xffff;
			return this;
		}

		public int vendor() {
			return vendor;
		}

		public libusb_device_criteria product(int product) {
			this.product = product & 0xffff;
			return this;
		}

		public int product() {
			return product;
		}

		public libusb_device_criteria busNumber(int busNumber) {
			this.busNumber = busNumber & 0xff;
			return this;
		}

		public int busNumber() {
			return busNumber;
		}

		public libusb_device_criteria deviceAddress(int deviceAddress) {
			this.deviceAddress = deviceAddress & 0xff;
			return this;
		}

		public int deviceAddress() {
			return deviceAddress;
		}

		public libusb_device_criteria description(String description) {
			this.description = description;
			return this;
		}

		public String description() {
			return description;
		}

		public libusb_device_criteria serial(String serial) {
			this.serial = serial;
			return this;
		}

		public String serial() {
			return serial;
		}

		public libusb_device_criteria index(int index) {
			this.index = index;
			return this;
		}

		public int index() {
			return index;
		}

		boolean needsDescriptor() {
			return vendor > 0 || product > 0;
		}

		boolean needsOpen() {
			return !isEmpty(description) || !isEmpty(serial);
		}

		@Override
		public int hashCode() {
			return HashCoder.hash(vendor, product, busNumber, deviceAddress, description, serial,
				index);
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) return true;
			if (!(obj instanceof libusb_device_criteria)) return false;
			libusb_device_criteria other = (libusb_device_criteria) obj;
			if (vendor != other.vendor) return false;
			if (product != other.product) return false;
			if (busNumber != other.busNumber) return false;
			if (deviceAddress != other.deviceAddress) return false;
			if (!EqualsUtil.equals(description, other.description)) return false;
			if (!EqualsUtil.equals(serial, other.serial)) return false;
			if (index != other.index) return false;
			return true;
		}

		@Override
		public String toString() {
			StringBuilder b = new StringBuilder();
			try (PrintStream out = StringUtil.asPrintStream(b)) {
				if (vendor > 0) out.printf(" vendor=0x%04x", vendor);
				if (product > 0) out.printf(" product=0x%04x", product);
				if (busNumber > 0) out.printf(" bus_number=0x%02x", busNumber);
				if (deviceAddress > 0) out.printf(" device_address=0x%02x", deviceAddress);
				if (description != null) out.printf(" description='%s'", description);
				if (serial != null) out.printf(" serial='%s'", serial);
				if (index > 0) out.printf(" index=%d", index);
				if (b.length() == 0) out.printf(" index=0");
			}
			b.insert(0, "criteria:");
			return b.toString();
		}
	}

	/**
	 * Creates an empty criteria object.
	 */
	public static libusb_device_criteria libusb_find_criteria() {
		return new libusb_device_criteria();
	}

	/**
	 * Creates a criteria object from a string. String syntax:
	 * 
	 * <pre>
	 * vendor:product:bus-number:device-address:description:serial:index
	 * </pre>
	 * 
	 * Vendor and product are 16-bit unsigned integers, and can be blank, or specified as decimal,
	 * octal (leading 0), or hex (leading 0x). Bus-number and device-address are 8-bit unsigned
	 * integers, and can be blank or specified as decimal, octal (leading 0), or hex (leading 0x).
	 * Description and serial are text identifiers and may be blank, quoted, or unquoted. Index is a
	 * decimal field.
	 */
	public static libusb_device_criteria libusb_find_criteria_string(String descriptor) {
		libusb_device_criteria criteria = libusb_find_criteria();
		List<String> items = DsvParser.split(descriptor, ':');
		int size = items.size();
		int i = 0;
		if (i < size) criteria.vendor(decode(items.get(i++), 0));
		if (i < size) criteria.product(decode(items.get(i++), 0));
		if (i < size) criteria.busNumber(decode(items.get(i++), 0));
		if (i < size) criteria.deviceAddress(decode(items.get(i++), 0));
		if (i < size) criteria.description(items.get(i++));
		if (i < size) criteria.serial(items.get(i++));
		if (i < size) criteria.index(valueOf(items.get(i++), 0));
		return criteria;
	}

	/**
	 * Finds devices matching criteria and calls the callback method. The callback should return
	 * true if the device is accepted, to stop further iteration. This method returns true if a
	 * match is found and the callback is made.
	 */
	public static boolean libusb_find_device_callback(libusb_device.ByReference devs,
		libusb_device_criteria criteria, ExceptionPredicate<LibUsbException, libusb_device> callback)
		throws LibUsbException {
		require(devs, "Device list");
		require(criteria, "Criteria");
		require(callback, "Callback");
		int index = criteria.index;
		for (libusb_device dev : devs.typedArray()) {
			if (!matches(criteria.busNumber, ubyte(libusb_get_bus_number(dev)))) continue;
			if (!matches(criteria.deviceAddress, ubyte(libusb_get_device_address(dev)))) continue;
			if (criteria.needsDescriptor() && !matchesDescriptor(dev, criteria)) continue;
			if (index-- > 0) continue;
			if (callback.test(dev)) return true;
		}
		return false;
	}

	/**
	 * Finds devices matching criteria and calls the callback method. Devices should be referenced
	 * or opened in the callback to avoid being invalidated. The callback should return true if the
	 * device is accepted, to stop further iteration. This method returns true if a match is found
	 * and the callback is made.
	 */
	public static boolean libusb_find_device_callback(libusb_context ctx,
		libusb_device_criteria criteria, ExceptionPredicate<LibUsbException, libusb_device> callback)
		throws LibUsbException {
		require(ctx, "Context");
		require(criteria, "Criteria");
		require(callback, "Callback");
		libusb_device.ByReference devs = libusb_get_device_list(ctx);
		try {
			return libusb_find_device_callback(devs, criteria, callback);
		} finally {
			libusb_free_device_list(devs);
		}
	}

	/**
	 * Finds a device based on criteria. The device should be unreferenced after use.
	 */
	public static libusb_device libusb_find_device_ref(libusb_context ctx,
		libusb_device_criteria criteria) throws LibUsbException {
		libusb_device[] devs = new libusb_device[1];
		libusb_find_device_callback(ctx, criteria, dev -> {
			libusb_ref_device(dev);
			devs[0] = dev;
			return true;
		});
		return devs[0];
	}

	/**
	 * Finds devices based on criteria, starting from criteria index up to the specified max number.
	 * The devices should be unreferenced after use.
	 */
	public static List<libusb_device> libusb_find_devices_ref(libusb_context ctx,
		libusb_device_criteria criteria, int max) throws LibUsbException {
		if (max <= 0) return List.of();
		List<libusb_device> devs = new ArrayList<>();
		try {
			libusb_find_device_callback(ctx, criteria, dev -> {
				libusb_ref_device(dev);
				devs.add(dev);
				return devs.size() >= max;
			});
			return devs;
		} catch (LibUsbException | RuntimeException e) {
			libusb_unref_devices(devs);
			throw e;
		}
	}

	private static boolean matchesDescriptor(libusb_device dev, libusb_device_criteria criteria)
		throws LibUsbException {
		libusb_device_descriptor desc = libusb_get_device_descriptor(dev);
		if (!matches(criteria.vendor, ushort(desc.idVendor))) return false;
		if (!matches(criteria.product, ushort(desc.idProduct))) return false;

		if (criteria.needsOpen()) {
			libusb_device_handle usb_dev = libusb_open(dev);
			try {
				if (!matches(usb_dev, criteria.description, desc.iProduct)) return false;
				if (!matches(usb_dev, criteria.serial, desc.iSerialNumber)) return false;
			} finally {
				libusb_close(usb_dev);
			}
		}
		return true;
	}

	private static boolean matches(int expected, int value) {
		return expected == 0 || expected == value;
	}

	private static boolean matches(libusb_device_handle usb_dev, String expected, int desc_index)
		throws LibUsbException {
		if (expected == null || expected.isEmpty()) return true;
		String descriptor = libusb_get_string_descriptor_ascii(usb_dev, desc_index);
		return expected.equals(descriptor);
	}

}
