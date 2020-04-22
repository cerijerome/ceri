package ceri.serial.libusb.jna;

import java.io.PrintStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.function.Predicate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ceri.common.collection.ImmutableUtil;
import ceri.common.data.ByteUtil;
import ceri.common.text.StringUtil;
import ceri.serial.libusb.jna.LibUsb.libusb_config_descriptor;
import ceri.serial.libusb.jna.LibUsb.libusb_context;
import ceri.serial.libusb.jna.LibUsb.libusb_device;
import ceri.serial.libusb.jna.LibUsb.libusb_device_descriptor;
import ceri.serial.libusb.jna.LibUsb.libusb_device_handle;
import ceri.serial.libusb.jna.LibUsb.libusb_endpoint_descriptor;
import ceri.serial.libusb.jna.LibUsb.libusb_interface;
import ceri.serial.libusb.jna.LibUsb.libusb_interface_descriptor;
import ceri.serial.libusb.jna.LibUsb.libusb_log_level;
import ceri.serial.libusb.jna.LibUsb.libusb_version;

/**
 * Iterates over usb devices and prints configuration info.
 */
public class LibUsbPrinter {
	private static final Logger logger = LogManager.getLogger();
	private final List<Predicate<libusb_device_descriptor>> skips;
	private final PrintStream out;
	private final libusb_log_level logLevel;

	public static void main(String[] args) {
		// Skips devices that cause seg fault
		builder().skip(0x05ac, 0x8007).skip(0x05ac, 0x8006).build().print();
		// builder().build().print();
	}

	public static class Builder {
		final Collection<Predicate<libusb_device_descriptor>> skips = new LinkedHashSet<>();
		PrintStream out = System.out;
		libusb_log_level logLevel = null;

		Builder() {}

		public Builder logLevel(libusb_log_level logLevel) {
			this.logLevel = logLevel;
			return this;
		}

		public Builder out(PrintStream out) {
			this.out = out;
			return this;
		}

		public final Builder skip(int vendorId, int productId) {
			return skip(desc -> (vendorId == 0 || desc.idVendor == (short) vendorId) &&
				(productId == 0 || desc.idProduct == (short) productId));
		}

		@SafeVarargs
		public final Builder skip(Predicate<libusb_device_descriptor>... skips) {
			return skip(Arrays.asList(skips));
		}

		public Builder skip(Collection<Predicate<libusb_device_descriptor>> skips) {
			this.skips.addAll(skips);
			return this;
		}

		public LibUsbPrinter build() {
			return new LibUsbPrinter(this);
		}
	}

	public static Builder builder() {
		return new Builder();
	}

	LibUsbPrinter(Builder builder) {
		skips = ImmutableUtil.copyAsList(builder.skips);
		out = builder.out;
		logLevel = builder.logLevel;
	}

	public void print() {
		String pre = "";
		try {
			libusb_context ctx = LibUsb.libusb_init();
			if (logLevel != null) LibUsb.libusb_set_debug(ctx, logLevel);
			version(pre);
			devices(pre, ctx);
			LibUsb.libusb_exit(ctx);
		} catch (Exception e) {
			logger.catching(e);
		}
	}

	private boolean skip(libusb_device_descriptor desc) {
		return skips.stream().anyMatch(skip -> skip.test(desc));
	}

	private void version(String pre) throws LibUsbException {
		out.printf("%s: [libusb_version]%n", pre);
		libusb_version v = LibUsb.libusb_get_version();
		out.printf("%s: version=%04x-%04x-%04x-%04x%n", pre, v.major, v.minor, v.micro, v.nano);
		out.printf("%s: describe=%s%n", pre, v.describe);
		out.printf("%s: rc=%s%n", pre, v.rc);
		out.println();
	}

	private void devices(String pre0, libusb_context ctx) throws Exception {
		libusb_device.ByReference list = LibUsb.libusb_get_device_list(ctx);
		libusb_device[] devices = list.typedArray();
		out.printf("#devices=%d%n", devices.length);
		for (int i = 0; i < devices.length; i++) {
			String pre = pre0 + i;
			out.printf("%s:----------------------------------------%n", pre);
			out.printf("%s: [libusb_device #%d]%n", pre, i);
			libusb_device device = devices[i];
			libusb_device_descriptor desc = LibUsb.libusb_get_device_descriptor(device);
			libusb_device_handle handle = LibUsb.libusb_open(device);

			desc(pre, handle, desc);
			other(pre, device);
			if (skip(desc)) out.printf("%s: <configuration skipped>", pre);
			else configs(pre, device, handle, desc.bNumConfigurations);

			LibUsb.libusb_close(handle);
			out.println();
		}
		LibUsb.libusb_free_device_list(list);
	}

	private void desc(String pre, libusb_device_handle handle, libusb_device_descriptor desc)
		throws Exception {
		out.printf("%s: [libusb_device_descriptor]%n", pre);
		out.printf("%s: bLength=%d%n", pre, desc.bLength);
		out.printf("%s: bDescriptorType=%s%n", pre, desc.bDescriptorType().get());
		out.printf("%s: bcdUSB=0x%04x%n", pre, desc.bcdUSB);
		out.printf("%s: bDeviceClass=%s%n", pre, desc.bDeviceClass().get());
		out.printf("%s: bDeviceSubClass=0x%02x%n", pre, desc.bDeviceSubClass);
		out.printf("%s: bDeviceProtocol=0x%02x%n", pre, desc.bDeviceProtocol);
		out.printf("%s: bMaxPacketSize0=%d%n", pre, desc.bMaxPacketSize0);
		out.printf("%s: idVendor=0x%04x%n", pre, desc.idVendor);
		out.printf("%s: idProduct=0x%04x%n", pre, desc.idProduct);
		out.printf("%s: bcdDevice=0x%04x%n", pre, desc.bcdDevice);
		out.printf("%s: iManufacturer=%d%n", pre, desc.iManufacturer);
		out.printf("%s:   ascii=%s%n", pre, descriptor(handle, desc.iManufacturer));
		out.printf("%s: iProduct=%d%n", pre, desc.iProduct);
		out.printf("%s:   ascii=%s%n", pre, descriptor(handle, desc.iProduct));
		out.printf("%s: iSerialNumber=%d%n", pre, desc.iSerialNumber);
		out.printf("%s:   ascii=%s%n", pre, descriptor(handle, desc.iSerialNumber));
		out.printf("%s: bNumConfigurations=%d%n", pre, desc.bNumConfigurations);
	}

	private void other(String pre, libusb_device device) throws Exception {

		out.printf("%s: bus_number()=0x%02x%n", pre, LibUsb.libusb_get_bus_number(device));
		out.printf("%s: port_number()=0x%02x%n", pre, LibUsb.libusb_get_port_number(device));
		out.printf("%s: port_numbers()=0x%s%n", pre,
			StringUtil.toHex(LibUsb.libusb_get_port_numbers(device)));
		out.printf("%s: device_address()=0x%02x%n", pre, LibUsb.libusb_get_device_address(device));
		out.printf("%s: device_speed()=%s%n", pre, LibUsb.libusb_get_device_speed(device));
	}

	private void configs(String pre, libusb_device device, libusb_device_handle handle, int configs)
		throws Exception {
		out.printf("%s: configuration()=0x%04x%n", pre, LibUsb.libusb_get_configuration(handle));
		for (byte i = 0; i < configs; i++) {
			libusb_config_descriptor config = LibUsb.libusb_get_config_descriptor(device, i);
			config(pre + "." + i, handle, config);
			LibUsb.libusb_free_config_descriptor(config);
		}
	}

	private void config(String pre, libusb_device_handle handle, libusb_config_descriptor config)
		throws Exception {
		out.printf("%s: [libusb_config_descriptor #%s]%n", pre, pre);
		out.printf("%s: bLength=%d%n", pre, config.bLength);
		out.printf("%s: bDescriptorType=%s%n", pre, config.bDescriptorType().get());
		out.printf("%s: wTotalLength=%d%n", pre, config.wTotalLength);
		out.printf("%s: bNumInterfaces=%d%n", pre, config.bNumInterfaces);
		out.printf("%s: bConfigurationValue=0x%02x%n", pre, config.bConfigurationValue);
		out.printf("%s: iConfiguration=%d%n", pre, config.iConfiguration);
		out.printf("%s:   ascii=%s%n", pre, descriptor(handle, config.iConfiguration));
		out.printf("%s: bmAttributes=0x%02x%n", pre, config.bmAttributes);
		out.printf("%s: MaxPower=%d%n", pre, config.MaxPower);
		out.printf("%s: extra[]=%s%n", pre, hex(config.extra()));
		libusb_interface[] interfaces = config.interfaces();
		out.printf("%s: #interfaces=%d%n", pre, interfaces.length);
		for (int i = 0; i < interfaces.length; i++)
			iface(pre + "." + i, handle, interfaces[i]);
	}

	private void iface(String pre, libusb_device_handle handle, libusb_interface iface)
		throws Exception {
		libusb_interface_descriptor[] altsettings = iface.altsettings();
		out.printf("%s: [libusb_interface #%s]%n", pre, pre);
		out.printf("%s: #altsettings=%d%n", pre, altsettings.length);
		for (int i = 0; i < altsettings.length; i++)
			altsetting(pre + "." + i, handle, altsettings[i]);
	}

	private void altsetting(String pre, libusb_device_handle handle,
		libusb_interface_descriptor alt) throws Exception {
		out.printf("%s: [libusb_interface_descriptor #%s]%n", pre, pre);
		out.printf("%s: bLength=%d%n", pre, alt.bLength);
		out.printf("%s: bDescriptorType=%s%n", pre, alt.bDescriptorType().get());
		out.printf("%s: bInterfaceNumber=%d%n", pre, alt.bInterfaceNumber);
		out.printf("%s: bAlternateSetting=%d%n", pre, alt.bAlternateSetting);
		out.printf("%s: bNumEndpoints=%d%n", pre, alt.bNumEndpoints);
		out.printf("%s: bInterfaceClass=%s%n", pre, alt.bInterfaceClass().get());
		out.printf("%s: bInterfaceSubClass=%d%n", pre, alt.bInterfaceSubClass);
		out.printf("%s: bInterfaceProtocol=%d%n", pre, alt.bInterfaceProtocol);
		out.printf("%s: iInterface=%d%n", pre, alt.iInterface);
		out.printf("%s:   ascii=%s%n", pre, descriptor(handle, alt.iInterface));
		out.printf("%s: extra[]=%s%n", pre, hex(alt.extra()));
		libusb_endpoint_descriptor[] endpoints = alt.endpoints();
		out.printf("%s: #endpoints=%d%n", pre, endpoints.length);
		for (int i = 0; i < endpoints.length; i++)
			endpoint(pre + "." + i, endpoints[i]);
	}

	private void endpoint(String pre, libusb_endpoint_descriptor ep) {
		out.printf("%s: [libusb_endpoint_descriptor #%s]%n", pre, pre);
		out.printf("%s: bLength=%d%n", pre, ep.bLength);
		out.printf("%s: bDescriptorType=%s%n", pre, ep.bDescriptorType().get());
		out.printf("%s: bEndpointAddress=0x%02x%n", pre, ep.bEndpointAddress);
		out.printf("%s:   Number=%d%n", pre, ep.bEndpointNumber().get());
		out.printf("%s:   Direction=%s%n", pre, ep.bEndpointDirection().get());
		out.printf("%s: bmAttributes=0x%02x%n", pre, ep.bmAttributes);
		out.printf("%s:   TransferType=%s%n", pre, ep.bmAttributesTransferType().get());
		out.printf("%s:   IsoSyncType=%s%n", pre, ep.bmAttributesIsoSyncType().get());
		out.printf("%s:   IsoUsageType=%s%n", pre, ep.bmAttributesIsoUsageType().get());
		out.printf("%s: wMaxPacketSize=%d%n", pre, ep.wMaxPacketSize);
		out.printf("%s: bInterval=%d%n", pre, ep.bInterval);
		out.printf("%s: bRefresh=%d%n", pre, ep.bRefresh);
		out.printf("%s: bSynchAddress=%d%n", pre, ep.bSynchAddress);
		out.printf("%s: extra[]=%s%n", pre, hex(ep.extra()));
	}

	private String hex(byte[] bytes) {
		if (bytes == null) return null;
		return "[" + ByteUtil.toHex(bytes, " ") + "]";
	}

	private String descriptor(libusb_device_handle handle, byte desc_index) throws LibUsbException {
		return LibUsb.libusb_get_string_descriptor_ascii(handle, desc_index);
	}

}
