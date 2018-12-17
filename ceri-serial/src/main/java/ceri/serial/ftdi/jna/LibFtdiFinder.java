package ceri.serial.ftdi.jna;

import static ceri.common.util.BasicUtil.isEmpty;
import static ceri.common.util.PrimitiveUtil.decode;
import static ceri.common.util.PrimitiveUtil.valueOf;
import static ceri.serial.ftdi.jna.LibFtdi.ftdi_usb_open_dev;
import static ceri.serial.ftdi.jna.LibFtdi.matchesDesc;
import static ceri.serial.jna.JnaUtil.ubyte;
import static ceri.serial.jna.JnaUtil.ushort;
import static ceri.serial.libusb.jna.LibUsb.libusb_close;
import static ceri.serial.libusb.jna.LibUsb.libusb_free_device_list;
import static ceri.serial.libusb.jna.LibUsb.libusb_get_bus_number;
import static ceri.serial.libusb.jna.LibUsb.libusb_get_device_address;
import static ceri.serial.libusb.jna.LibUsb.libusb_get_device_descriptor;
import static ceri.serial.libusb.jna.LibUsb.libusb_get_device_list;
import static ceri.serial.libusb.jna.LibUsb.libusb_open;
import java.io.PrintStream;
import java.util.List;
import ceri.common.text.DsvParser;
import ceri.common.text.StringUtil;
import ceri.serial.ftdi.jna.LibFtdi.ftdi_context;
import ceri.serial.libusb.jna.LibUsb.libusb_device;
import ceri.serial.libusb.jna.LibUsb.libusb_device_descriptor;
import ceri.serial.libusb.jna.LibUsb.libusb_device_handle;
import ceri.serial.libusb.jna.LibUsbException;

public class LibFtdiFinder {

	public static class ftdi_find_criteria {
		int vendor = 0;
		int product = 0;
		int busNumber = 0;
		int deviceAddress = 0;
		String description = null;
		String serial = null;
		int index = 0;

		ftdi_find_criteria() {}

		public ftdi_find_criteria vendor(int vendor) {
			this.vendor = vendor & 0xffff;
			return this;
		}

		public ftdi_find_criteria product(int product) {
			this.product = product & 0xffff;
			return this;
		}

		public ftdi_find_criteria busNumber(int busNumber) {
			this.busNumber = busNumber & 0xff;
			return this;
		}

		public ftdi_find_criteria deviceAddress(int deviceAddress) {
			this.deviceAddress = deviceAddress & 0xff;
			return this;
		}

		public ftdi_find_criteria description(String description) {
			this.description = description;
			return this;
		}

		public ftdi_find_criteria serial(String serial) {
			this.serial = serial;
			return this;
		}

		public ftdi_find_criteria index(int index) {
			this.index = index;
			return this;
		}

		boolean needsDescriptor() {
			return vendor > 0 || product > 0;
		}

		boolean needsOpen() {
			return !isEmpty(description) || !isEmpty(serial);
		}
	}

	public static ftdi_find_criteria ftdi_find_criteria() {
		return new ftdi_find_criteria();
	}

	public static ftdi_find_criteria ftdi_find_criteria_string(String descriptor) {
		ftdi_find_criteria criteria = ftdi_find_criteria();
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

	public static void ftdi_usb_open_criteria(ftdi_context ftdi, ftdi_find_criteria criteria)
		throws LibUsbException {
		LibFtdi.requireCtx(ftdi);
		libusb_device.ByReference devs = libusb_get_device_list(ftdi.usb_ctx);
		try {
			int index = criteria.index;
			for (libusb_device dev : devs.typedArray()) {
				if (!matches(criteria.busNumber, ubyte(libusb_get_bus_number(dev)))) continue;
				if (!matches(criteria.deviceAddress, ubyte(libusb_get_device_address(dev))))
					continue;
				if (criteria.needsDescriptor() && !matchesDescriptor(dev, criteria)) continue;
				if (index-- > 0) continue;
				ftdi_usb_open_dev(ftdi, dev);
				return;
			}
		} finally {
			libusb_free_device_list(devs); // ,1 ?
		}
		throw new LibFtdiNotFoundException("Device not found:" + criteriaString(criteria));
	}

	private static boolean matchesDescriptor(libusb_device dev, ftdi_find_criteria criteria)
		throws LibUsbException {
		libusb_device_descriptor desc = libusb_get_device_descriptor(dev);
		if (!matches(criteria.vendor, ushort(desc.idVendor))) return false;
		if (!matches(criteria.product, ushort(desc.idProduct))) return false;

		if (criteria.needsOpen()) {
			libusb_device_handle usb_dev = libusb_open(dev);
			try {
				if (!matchesDesc(usb_dev, criteria.description, desc.iProduct)) return false;
				if (!matchesDesc(usb_dev, criteria.serial, desc.iSerialNumber)) return false;
			} finally {
				libusb_close(usb_dev);
			}
		}
		return true;
	}

	private static boolean matches(int expected, int value) {
		return expected == 0 || expected == value;
	}

	private static String criteriaString(ftdi_find_criteria criteria) {
		StringBuilder b = new StringBuilder();
		try (PrintStream out = StringUtil.asPrintStream(b)) {
			if (criteria.vendor > 0) out.printf(" vendor=0x%04x", criteria.vendor);
			if (criteria.product > 0) out.printf(" product=0x%04x", criteria.product);
			if (criteria.busNumber > 0) out.printf(" bus_number=0x%02x", criteria.busNumber);
			if (criteria.deviceAddress > 0)
				out.printf(" device_address=0x%02x", criteria.deviceAddress);
			if (criteria.description != null) out.printf(" description='%s'", criteria.description);
			if (criteria.serial != null) out.printf(" serial='%s'", criteria.serial);
			if (criteria.index > 0) out.printf(" index=%d", criteria.index);
			if (b.length() == 0) return "index=0";
		}
		return b.toString();
	}

}
