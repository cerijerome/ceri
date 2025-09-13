package ceri.serial.libusb.jna;

import java.util.List;
import java.util.Objects;
import ceri.common.collection.Immutable;
import ceri.common.collection.Lists;
import ceri.common.function.Excepts;
import ceri.common.function.Functions;
import ceri.common.math.Maths;
import ceri.common.text.Dsv;
import ceri.common.text.Numbers;
import ceri.common.text.Strings;
import ceri.common.util.Counter;
import ceri.jna.type.ArrayPointer;

public class LibUsbFinder {
	private static final List<Functions.Predicate<LibUsbFinder>> predicates = predicates();
	public static final LibUsbFinder FIRST = builder().build();
	public final int vendor;
	public final int product;
	public final int bus;
	public final int address;
	public final String description;
	public final String serial;
	public final int index;

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
	public static LibUsbFinder from(String descriptor) {
		var b = new Builder();
		var items = Dsv.split(descriptor, ':');
		int size = items.size();
		int i = 0;
		if (i < size) b.vendor(Numbers.Decode.toInt(items.get(i++), 0));
		if (i < size) b.product(Numbers.Decode.toInt(items.get(i++), 0));
		if (i < size) b.bus(Numbers.Decode.toInt(items.get(i++), 0));
		if (i < size) b.address(Numbers.Decode.toInt(items.get(i++), 0));
		if (i < size) b.description(items.get(i++));
		if (i < size) b.serial(items.get(i++));
		if (i < size) b.index(Numbers.Parse.toInt(items.get(i++), 0));
		return b.build();
	}

	/**
	 * Create a finder instance for vendor and product. Specify 0 for any match.
	 */
	public static LibUsbFinder of(int vendor, int product) {
		return builder().vendor(vendor).product(product).build();
	}

	public static class Builder {
		int vendor;
		int product;
		int bus;
		int address;
		String description = "";
		String serial = "";
		int index;

		Builder() {}

		public Builder vendor(int vendor) {
			this.vendor = Maths.ushort(vendor);
			return this;
		}

		public Builder product(int product) {
			this.product = Maths.ushort(product);
			return this;
		}

		public Builder bus(int bus) {
			this.bus = Maths.ubyte(bus);
			return this;
		}

		public Builder address(int address) {
			this.address = Maths.ubyte(address);
			return this;
		}

		public Builder description(String description) {
			this.description = description;
			return this;
		}

		public Builder serial(String serial) {
			this.serial = serial;
			return this;
		}

		public Builder index(int index) {
			this.index = index;
			return this;
		}

		public LibUsbFinder build() {
			return new LibUsbFinder(this);
		}
	}

	public static Builder builder() {
		return new Builder();
	}

	public static Builder builder(LibUsbFinder finder) {
		return builder().vendor(finder.vendor).product(finder.product).bus(finder.bus)
			.address(finder.address).description(finder.description).serial(finder.serial)
			.index(finder.index);
	}

	LibUsbFinder(Builder builder) {
		vendor = builder.vendor;
		product = builder.product;
		bus = builder.bus;
		address = builder.address;
		description = builder.description;
		serial = builder.serial;
		index = builder.index;
	}

	/**
	 * Returns true if the finder matches a device, initializing a new context. Does not open or
	 * reference any matching device.
	 */
	public boolean matches() throws LibUsbException {
		var ctx = LibUsb.libusb_init();
		try {
			return matches(ctx);
		} finally {
			LibUsb.libusb_exit(ctx);
		}
	}

	/**
	 * Returns true if the finder matches a device, using the given context. Does not open or
	 * reference any matching device.
	 */
	public boolean matches(LibUsb.libusb_context ctx) throws LibUsbException {
		return findWithCallback(ctx, _ -> true);
	}

	/**
	 * Returns the number of matching device, initializing a new context. Does not open or reference
	 * any matching device.
	 */
	public int matchCount() throws LibUsbException {
		var ctx = LibUsb.libusb_init();
		try {
			return matchCount(ctx);
		} finally {
			LibUsb.libusb_exit(ctx);
		}
	}

	/**
	 * Returns the number of matching devices, using the given context. Does not open or reference
	 * any matching device.
	 */
	public int matchCount(LibUsb.libusb_context ctx) throws LibUsbException {
		var counter = Counter.of(0);
		findWithCallback(ctx, _ -> {
			counter.inc(1);
			return false;
		});
		return counter.get();
	}

	/**
	 * Finds devices matching criteria and calls the callback method. The callback should return
	 * true if the device is accepted, to stop further iteration. This method returns true if a
	 * match is found and the callback is made.
	 */
	public boolean findWithCallback(ArrayPointer<LibUsb.libusb_device> devs,
		Excepts.Predicate<LibUsbException, LibUsb.libusb_device> callback) throws LibUsbException {
		LibUsbUtil.require(devs, "Device list");
		LibUsbUtil.require(callback, "Callback");
		int index = this.index;
		for (var dev : devs.get()) {
			if (!matchesBusNumber(dev, bus)) continue;
			if (!matchesDeviceAddress(dev, address)) continue;
			if (!matchesDescriptor(dev)) continue;
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
	public boolean findWithCallback(LibUsb.libusb_context ctx,
		Excepts.Predicate<LibUsbException, LibUsb.libusb_device> callback) throws LibUsbException {
		LibUsbUtil.require(callback, "Callback");
		var devs = LibUsb.libusb_get_device_list(ctx);
		try {
			return findWithCallback(devs, callback);
		} finally {
			LibUsb.libusb_free_device_list(devs);
		}
	}

	/**
	 * Finds a device based on criteria. The device should be unreferenced after use. Throws
	 * exception if not found.
	 */
	public LibUsb.libusb_device findAndRef(LibUsb.libusb_context ctx) throws LibUsbException {
		var devs = new LibUsb.libusb_device[1];
		if (!findWithCallback(ctx, dev -> {
			LibUsb.libusb_ref_device(dev);
			devs[0] = dev;
			return true;
		})) throw LibUsbException.of(LibUsb.libusb_error.LIBUSB_ERROR_INVALID_PARAM,
			"Device not found: " + this);
		return devs[0];
	}

	/**
	 * Finds a device and opens it, based on criteria. Throws exception if not found.
	 */
	public LibUsb.libusb_device_handle findAndOpen(LibUsb.libusb_context ctx)
		throws LibUsbException {
		var handles = new LibUsb.libusb_device_handle[1];
		if (!findWithCallback(ctx, dev -> {
			handles[0] = LibUsb.libusb_open(dev);
			return true;
		})) throw LibUsbException.of(LibUsb.libusb_error.LIBUSB_ERROR_INVALID_PARAM,
			"Device not found: " + this);
		return handles[0];
	}

	/**
	 * Finds devices based on criteria, starting from criteria index up to the specified max number.
	 * The devices should be unreferenced after use. Use max of 0 for unlimited devices.
	 */
	public List<LibUsb.libusb_device> findAndRef(LibUsb.libusb_context ctx, int max)
		throws LibUsbException {
		var devs = Lists.<LibUsb.libusb_device>of();
		try {
			findWithCallback(ctx, dev -> {
				LibUsb.libusb_ref_device(dev);
				devs.add(dev);
				return max != 0 && devs.size() >= max;
			});
			return devs;
		} catch (LibUsbException | RuntimeException e) {
			LibUsb.libusb_unref_devices(devs);
			throw e;
		}
	}

	public String asDescriptor() {
		int n = lastIndex();
		int i = 0;
		var b = new StringBuilder();
		if (i++ <= n) b.append(desc("0x%04x", vendor));
		if (i++ <= n) b.append(":").append(desc("0x%04x", product));
		if (i++ <= n) b.append(":").append(desc("0x%02x", bus));
		if (i++ <= n) b.append(":").append(desc("0x%02x", address));
		if (i++ <= n) b.append(":").append(description);
		if (i++ <= n) b.append(":").append(serial);
		if (i++ <= n) b.append(":").append(index);
		return b.toString();
	}

	@Override
	public int hashCode() {
		return Objects.hash(vendor, product, bus, address, description, serial, index);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof LibUsbFinder other)) return false;
		if (vendor != other.vendor) return false;
		if (product != other.product) return false;
		if (bus != other.bus) return false;
		if (address != other.address) return false;
		if (!Objects.equals(description, other.description)) return false;
		if (!Objects.equals(serial, other.serial)) return false;
		if (index != other.index) return false;
		return true;
	}

	@Override
	public String toString() {
		return String.format(
			"{vendor=%s, product=%s, bus=%s, address=%s, description=%s, serial=%s, index=%d}",
			str("0x%04x", vendor), str("0x%04x", product), str("0x%02x", bus),
			str("0x%02x", address), str(description), str(serial), index);
	}

	private int lastIndex() {
		for (int i = predicates.size(); i > 0; i--)
			if (predicates.get(i - 1).test(this)) return i;
		return 0;
	}

	private static boolean matchesBusNumber(LibUsb.libusb_device dev, int bus)
		throws LibUsbException {
		return bus == 0 || bus == Maths.ubyte(LibUsb.libusb_get_bus_number(dev));
	}

	private static boolean matchesDeviceAddress(LibUsb.libusb_device dev, int address)
		throws LibUsbException {
		return address == 0 || address == Maths.ubyte(LibUsb.libusb_get_device_address(dev));
	}

	private boolean matchesDescriptor(LibUsb.libusb_device dev) throws LibUsbException {
		if (!needsDescriptor()) return true;
		var desc = LibUsb.libusb_get_device_descriptor(dev);
		if (!matches(vendor, Maths.ushort(desc.idVendor))) return false;
		if (!matches(product, Maths.ushort(desc.idProduct))) return false;
		return matchesOpenDescriptor(dev, desc);
	}

	private boolean matchesOpenDescriptor(LibUsb.libusb_device dev,
		LibUsb.libusb_device_descriptor desc) throws LibUsbException {
		if (!needsOpen()) return true;
		var usb_dev = LibUsb.libusb_open(dev);
		try {
			if (!matches(usb_dev, description, desc.iProduct)) return false;
			if (!matches(usb_dev, serial, desc.iSerialNumber)) return false;
			return true;
		} finally {
			LibUsb.libusb_close(usb_dev);
		}
	}

	private boolean needsDescriptor() {
		return vendor > 0 || product > 0 || needsOpen();
	}

	private boolean needsOpen() {
		return !Strings.isBlank(description) || !Strings.isBlank(serial);
	}

	private static boolean matches(int expected, int value) {
		return expected == 0 || expected == value;
	}

	private static boolean matches(LibUsb.libusb_device_handle usb_dev, String expected,
		int desc_index) throws LibUsbException {
		if (Strings.isEmpty(expected)) return true;
		var descriptor = LibUsb.libusb_get_string_descriptor_ascii(usb_dev, desc_index);
		return expected.equals(descriptor);
	}

	private static String desc(String format, int value) {
		return value == 0 ? "0" : Strings.format(format, value);
	}

	private static String str(String format, int value) {
		return value == 0 ? "any" : Strings.format(format, value);
	}

	private static String str(String value) {
		return value.length() == 0 ? "any" : "\"" + value + "\"";
	}

	private static List<Functions.Predicate<LibUsbFinder>> predicates() {
		return Immutable.listOf(finder -> finder.product != 0, finder -> finder.bus != 0,
			finder -> finder.address != 0, finder -> !finder.description.isBlank(),
			finder -> !finder.serial.isBlank(), finder -> finder.index != 0);
	}
}
