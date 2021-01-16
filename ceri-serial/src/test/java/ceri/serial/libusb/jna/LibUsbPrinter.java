package ceri.serial.libusb.jna;

import static ceri.common.collection.ArrayUtil.toHex;
import static ceri.common.math.MathUtil.ubyte;
import static ceri.serial.libusb.jna.LibUsb.libusb_bos_type.LIBUSB_BT_CONTAINER_ID;
import static ceri.serial.libusb.jna.LibUsb.libusb_bos_type.LIBUSB_BT_SS_USB_DEVICE_CAPABILITY;
import static ceri.serial.libusb.jna.LibUsb.libusb_bos_type.LIBUSB_BT_USB_2_0_EXTENSION;
import static ceri.serial.libusb.jna.LibUsb.libusb_config_attributes.LIBUSB_CA_RESERVED1;
import java.io.PrintStream;
import java.util.Set;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ceri.serial.libusb.jna.LibUsb.libusb_bos_dev_capability_descriptor;
import ceri.serial.libusb.jna.LibUsb.libusb_bos_type;
import ceri.serial.libusb.jna.LibUsb.libusb_class_code;
import ceri.serial.libusb.jna.LibUsb.libusb_config_attributes;
import ceri.serial.libusb.jna.LibUsb.libusb_config_descriptor;
import ceri.serial.libusb.jna.LibUsb.libusb_context;
import ceri.serial.libusb.jna.LibUsb.libusb_descriptor_type;
import ceri.serial.libusb.jna.LibUsb.libusb_device;
import ceri.serial.libusb.jna.LibUsb.libusb_device_descriptor;
import ceri.serial.libusb.jna.LibUsb.libusb_device_handle;
import ceri.serial.libusb.jna.LibUsb.libusb_endpoint_descriptor;
import ceri.serial.libusb.jna.LibUsb.libusb_interface;
import ceri.serial.libusb.jna.LibUsb.libusb_interface_descriptor;
import ceri.serial.libusb.jna.LibUsb.libusb_log_level;
import ceri.serial.libusb.jna.LibUsb.libusb_ss_endpoint_companion_descriptor;
import ceri.serial.libusb.jna.LibUsb.libusb_version;

/**
 * Iterates over usb devices and prints configuration info.
 */
public class LibUsbPrinter {
	private static final Logger logger = LogManager.getLogger();
	private final PrintStream out;
	private final libusb_log_level logLevel;

	// TODO:
	// - add bos types to UsbPrinter
	// - remove unused struct constructors
	// - remove struct type accessors (put in UsbXxx classes)
	// - remove struct initializer values

	public static void main(String[] args) {
		var printer = new LibUsbPrinter(System.out, null);
		run(printer);
		runTest(printer);
	}

	public static void runTest(LibUsbPrinter printer) {
		TestLibUsbNative lib = TestLibUsbNative.of();
		try (var enc = TestLibUsbNative.register(lib)) {
			LibUsbSampleData.populate(lib.data);
			run(printer);
		}
	}

	public static void run(LibUsbPrinter printer) {
		printer.print();
	}

	private LibUsbPrinter(PrintStream out, libusb_log_level logLevel) {
		this.out = out;
		this.logLevel = logLevel;
	}

	public void print() {
		String pre = "";
		libusb_context ctx = null;
		try {
			ctx = LibUsb.libusb_init();
			if (logLevel != null) LibUsb.libusb_set_debug(ctx, logLevel);
			version(pre);
			devices(pre, ctx);
		} catch (Exception e) {
			logger.catching(e);
		} finally {
			if (ctx != null) LibUsb.libusb_exit(ctx);
		}
	}

	private void version(String pre) {
		out.printf("%s: [libusb_version]%n", pre);
		libusb_version v = LibUsb.libusb_get_version();
		out.printf("%s: version=%04x-%04x-%04x-%04x%n", pre, v.major, v.minor, v.micro, v.nano);
		out.printf("%s: describe=%s%n", pre, v.describe);
		out.printf("%s: rc=%s%n", pre, v.rc);
		out.println();
	}

	private void devices(String pre0, libusb_context ctx) throws Exception {
		var list = LibUsb.libusb_get_device_list(ctx);
		try {
			libusb_device[] devices = list.array();
			out.printf("#devices=%d%n", devices.length);
			for (int i = 0; i < devices.length; i++)
				device(pre0 + i, ctx, devices[i]);
		} finally {
			LibUsb.libusb_free_device_list(list);
		}
	}

	private void device(String pre, libusb_context ctx, libusb_device device)
		throws LibUsbException {
		out.printf("%s:----------------------------------------%n", pre);
		out.printf("%s: [libusb_device]%n", pre);
		libusb_device_descriptor desc = LibUsb.libusb_get_device_descriptor(device);
		libusb_device_handle handle = LibUsb.libusb_open(device);
		try {
			other(pre, device);
			desc(pre, handle, desc);
			configs(pre, ctx, device, handle, desc.bNumConfigurations);
			bos(pre, ctx, handle);
		} finally {
			LibUsb.libusb_close(handle);
		}
		out.println();
	}

	private void other(String pre, libusb_device device) throws LibUsbException {
		out.printf("%s: bus_number()=0x%02x%n", pre, LibUsb.libusb_get_bus_number(device));
		out.printf("%s: port_number()=0x%02x%n", pre, LibUsb.libusb_get_port_number(device));
		out.printf("%s: port_numbers()=%s%n", pre, toHex(LibUsb.libusb_get_port_numbers(device)));
		out.printf("%s: device_address()=0x%02x%n", pre, LibUsb.libusb_get_device_address(device));
		out.printf("%s: device_speed()=%s%n", pre, LibUsb.libusb_get_device_speed(device));
	}

	private void desc(String pre, libusb_device_handle handle, libusb_device_descriptor desc)
		throws LibUsbException {
		out.printf("%s: [libusb_device_descriptor]%n", pre);
		out.printf("%s: bLength=%d%n", pre, desc.bLength);
		out.printf("%s: bDescriptorType=0x%02x %s%n", pre, desc.bDescriptorType,
			type(desc.bDescriptorType));
		out.printf("%s: bcdUSB=0x%04x%n", pre, desc.bcdUSB);
		out.printf("%s: bDeviceClass=0x%02x %s%n", pre, desc.bDeviceClass,
			devClass(desc.bDeviceClass));
		out.printf("%s: bDeviceSubClass=0x%02x%n", pre, desc.bDeviceSubClass);
		out.printf("%s: bDeviceProtocol=0x%02x%n", pre, desc.bDeviceProtocol);
		out.printf("%s: bMaxPacketSize0=%d%n", pre, desc.bMaxPacketSize0);
		out.printf("%s: idVendor=0x%04x%n", pre, desc.idVendor);
		out.printf("%s: idProduct=0x%04x%n", pre, desc.idProduct);
		out.printf("%s: bcdDevice=0x%04x%n", pre, desc.bcdDevice);
		out.printf("%s: iManufacturer=%d \"%s\"%n", pre, desc.iManufacturer,
			descriptor(handle, desc.iManufacturer));
		out.printf("%s: iProduct=%d \"%s\"%n", pre, desc.iProduct,
			descriptor(handle, desc.iProduct));
		out.printf("%s: iSerialNumber=%d \"%s\"%n", pre, desc.iSerialNumber,
			descriptor(handle, desc.iSerialNumber));
		out.printf("%s: bNumConfigurations=%d%n", pre, desc.bNumConfigurations);
	}

	private void configs(String pre, libusb_context ctx, libusb_device device,
		libusb_device_handle handle, int configs) throws LibUsbException {
		out.printf("%s: configuration()=%d%n", pre, LibUsb.libusb_get_configuration(handle));
		for (byte i = 0; i < configs; i++) {
			libusb_config_descriptor config = LibUsb.libusb_get_config_descriptor(device, i);
			try {
				config(pre + "." + i, ctx, handle, config);
			} finally {
				LibUsb.libusb_free_config_descriptor(config);
			}
		}
	}

	private void config(String pre, libusb_context ctx, libusb_device_handle handle,
		libusb_config_descriptor config) throws LibUsbException {
		out.printf("%s: [libusb_config_descriptor #%s]%n", pre, pre);
		out.printf("%s: bLength=%d%n", pre, config.bLength);
		out.printf("%s: bDescriptorType=0x%02x %s%n", pre, config.bDescriptorType,
			type(config.bDescriptorType));
		out.printf("%s: wTotalLength=%d%n", pre, config.wTotalLength);
		out.printf("%s: bNumInterfaces=%d%n", pre, config.bNumInterfaces);
		out.printf("%s: bConfigurationValue=%d%n", pre, config.bConfigurationValue);
		out.printf("%s: iConfiguration=%d \"%s\"%n", pre, config.iConfiguration,
			descriptor(handle, config.iConfiguration));
		out.printf("%s: bmAttributes=0x%02x %s%n", pre, config.bmAttributes,
			attrs(config.bmAttributes));
		out.printf("%s: bMaxPower=%d%n", pre, config.bMaxPower);
		out.printf("%s: extra=%s%n", pre, toHex(config.extra()));
		libusb_interface[] interfaces = config.interfaces();
		out.printf("%s: #interfaces=%d%n", pre, interfaces.length);
		for (int i = 0; i < interfaces.length; i++)
			iface(pre + "." + i, ctx, handle, interfaces[i]);
	}

	private void iface(String pre, libusb_context ctx, libusb_device_handle handle,
		libusb_interface iface) throws LibUsbException {
		libusb_interface_descriptor[] altsettings = iface.altsettings();
		out.printf("%s: [libusb_interface #%s]%n", pre, pre);
		out.printf("%s: #altsettings=%d%n", pre, altsettings.length);
		for (int i = 0; i < altsettings.length; i++)
			altsetting(pre + "." + i, ctx, handle, altsettings[i]);
	}

	private void altsetting(String pre, libusb_context ctx, libusb_device_handle handle,
		libusb_interface_descriptor alt) throws LibUsbException {
		out.printf("%s: [libusb_interface_descriptor #%s]%n", pre, pre);
		out.printf("%s: bLength=%d%n", pre, alt.bLength);
		out.printf("%s: bDescriptorType=0x%02x %s%n", pre, alt.bDescriptorType,
			type(alt.bDescriptorType));
		out.printf("%s: bInterfaceNumber=%d%n", pre, alt.bInterfaceNumber);
		out.printf("%s: bAlternateSetting=%d%n", pre, alt.bAlternateSetting);
		out.printf("%s: bNumEndpoints=%d%n", pre, alt.bNumEndpoints);
		out.printf("%s: bInterfaceClass=0x%02x %s%n", pre, alt.bInterfaceClass,
			devClass(alt.bInterfaceClass));
		out.printf("%s: bInterfaceSubClass=0x%02x%n", pre, alt.bInterfaceSubClass);
		out.printf("%s: bInterfaceProtocol=0x%02x%n", pre, alt.bInterfaceProtocol);
		out.printf("%s: iInterface=%d \"%s\"%n", pre, alt.iInterface,
			descriptor(handle, alt.iInterface));
		out.printf("%s: extra=%s%n", pre, toHex(alt.extra()));
		libusb_endpoint_descriptor[] endpoints = alt.endpoints();
		out.printf("%s: #endpoints=%d%n", pre, endpoints.length);
		for (int i = 0; i < endpoints.length; i++)
			endpoint(pre + "." + i, ctx, endpoints[i]);
	}

	private void endpoint(String pre, libusb_context ctx, libusb_endpoint_descriptor ep)
		throws LibUsbException {
		out.printf("%s: [libusb_endpoint_descriptor #%s]%n", pre, pre);
		out.printf("%s: bLength=%d%n", pre, ep.bLength);
		out.printf("%s: bDescriptorType=0x%02x %s%n", pre, ep.bDescriptorType,
			type(ep.bDescriptorType));
		out.printf("%s: bEndpointAddress=0x%02x%n", pre, ep.bEndpointAddress);
		out.printf("%s:   Number=%d%n", pre, ep.bEndpointNumber().get());
		out.printf("%s:   Direction=%s%n", pre, ep.bEndpointDirection().get());
		out.printf("%s: bmAttributes=0x%02x%n", pre, ep.bmAttributes);
		out.printf("%s:   TransferType=%s%n", pre, ep.bmAttributesTransferType().get());
		out.printf("%s:   IsoSyncType=%s%n", pre, ep.bmAttributesIsoSyncType().get());
		out.printf("%s:   IsoUsageType=%s%n", pre, ep.bmAttributesIsoUsageType().get());
		out.printf("%s: wMaxPacketSize=%d%n", pre, ep.wMaxPacketSize);
		out.printf("%s: bInterval=0x%02x%n", pre, ep.bInterval);
		out.printf("%s: bRefresh=%d%n", pre, ep.bRefresh);
		out.printf("%s: bSynchAddress=%d%n", pre, ep.bSynchAddress);
		out.printf("%s: extra=%s%n", pre, toHex(ep.extra()));
		var ss = LibUsb.libusb_get_ss_endpoint_companion_descriptor(ctx, ep);
		try {
			ssEndPoint(pre, ss);
		} finally {
			LibUsb.libusb_free_ss_endpoint_companion_descriptor(ss);
		}
	}

	private void ssEndPoint(String pre, libusb_ss_endpoint_companion_descriptor ss) {
		if (ss == null) return;
		out.printf("%s: [libusb_ss_endpoint_companion_descriptor]%n", pre);
		out.printf("%s: bLength=%d%n", pre, ss.bLength);
		out.printf("%s: bDescriptorType=0x%02x %s%n", pre, ss.bDescriptorType,
			type(ss.bDescriptorType));
		out.printf("%s: bMaxBurst=%d%n", pre, ss.bMaxBurst);
		out.printf("%s: bmAttributes=0x%02x%n", pre, ss.bmAttributes);
		out.printf("%s:   BulkMaxStreams=%d%n", pre, ss.bmAttributesBulkMaxStreams().get());
		out.printf("%s:   IsoMult=%d%n", pre, ss.bmAttributesIsoMult().get());
		out.printf("%s: wBytesPerInterval=%d%n", pre, ss.wBytesPerInterval);
	}

	private void bos(String pre, libusb_context ctx, libusb_device_handle handle)
		throws LibUsbException {
		var bos = LibUsb.libusb_get_bos_descriptor(handle);
		if (bos == null) return;
		try {
			out.printf("%s: [libusb_bos_descriptor]%n", pre);
			out.printf("%s: bLength=%d%n", pre, bos.bLength);
			out.printf("%s: bDescriptorType=0x%02x %s%n", pre, bos.bDescriptorType,
				type(bos.bDescriptorType));
			out.printf("%s: wTotalLength=%d%n", pre, bos.wTotalLength);
			out.printf("%s: bNumDeviceCaps=%d%n", pre, bos.bNumDeviceCaps);
			for (var bdc : bos.dev_capability) {
				var type = bosType(bdc.bDevCapabilityType);
				if (type == LIBUSB_BT_USB_2_0_EXTENSION) bosUsb20Ext(pre, ctx, bdc);
				else if (type == LIBUSB_BT_SS_USB_DEVICE_CAPABILITY) bosSsUsbDevCap(pre, ctx, bdc);
				else if (type == LIBUSB_BT_CONTAINER_ID) bosContainerId(pre, ctx, bdc);
				else bosDevCap(pre, bdc);
			}
		} finally {
			LibUsb.libusb_free_bos_descriptor(bos);
		}
	}

	private void bosDevCap(String pre, libusb_bos_dev_capability_descriptor bdc) {
		out.printf("%s: [libusb_bos_dev_capability_descriptor]%n", pre);
		out.printf("%s: bLength=%d%n", pre, bdc.bLength);
		out.printf("%s: bDescriptorType=0x%02x %s%n", pre, bdc.bDescriptorType,
			type(bdc.bDescriptorType));
		out.printf("%s: bDevCapabilityType=0x%02x %s%n", pre, bdc.bDevCapabilityType,
			bosType(bdc.bDevCapabilityType));
		out.printf("%s: dev_capability_data[]=%s%n", pre, toHex(bdc.dev_capability_data));
	}

	private void bosUsb20Ext(String pre, libusb_context ctx,
		libusb_bos_dev_capability_descriptor bdc) throws LibUsbException {
		var usb = LibUsb.libusb_get_usb_2_0_extension_descriptor(ctx, bdc);
		try {
			out.printf("%s: [libusb_usb_2_0_extension_descriptor]%n", pre);
			out.printf("%s: bLength=%d%n", pre, usb.bLength);
			out.printf("%s: bDescriptorType=0x%02x %s%n", pre, usb.bDescriptorType,
				type(usb.bDescriptorType));
			out.printf("%s: bDevCapabilityType=0x%02x %s%n", pre, usb.bDevCapabilityType,
				bosType(usb.bDevCapabilityType));
			out.printf("%s: bmAttributes=0x%08x%n", pre, usb.bmAttributes);
			out.printf("%s:   bmAttributes()=%s%n", pre, usb.bmAttributes().getAll());
		} finally {
			LibUsb.libusb_free_usb_2_0_extension_descriptor(usb);
		}
	}

	private void bosSsUsbDevCap(String pre, libusb_context ctx,
		libusb_bos_dev_capability_descriptor bdc) throws LibUsbException {
		var ss = LibUsb.libusb_get_ss_usb_device_capability_descriptor(ctx, bdc);
		try {
			out.printf("%s: [libusb_ss_usb_device_capability_descriptor]%n", pre);
			out.printf("%s: bLength=%d%n", pre, ss.bLength);
			out.printf("%s: bDescriptorType=0x%02x %s%n", pre, ss.bDescriptorType,
				type(ss.bDescriptorType));
			out.printf("%s: bDevCapabilityType=0x%02x %s%n", pre, ss.bDevCapabilityType,
				bosType(ss.bDevCapabilityType));
			out.printf("%s: bmAttributes=0x%02x%n", pre, ss.bmAttributes);
			out.printf("%s:   bmAttributes()=%s%n", pre, ss.bmAttributes().getAll());
			out.printf("%s: wSpeedSupported=0x%04x%n", pre, ss.wSpeedSupported);
			out.printf("%s:   wSpeedSupported()=%s%n", pre, ss.wSpeedSupported().getAll());
			out.printf("%s: bFunctionalitySupport=0x%02x%n", pre, ss.bFunctionalitySupport);
			out.printf("%s: bU1DevExitLat=0x%02x%n", pre, ss.bU1DevExitLat);
			out.printf("%s: wU2DevExitLat=0x%04x%n", pre, ss.wU2DevExitLat);
		} finally {
			LibUsb.libusb_free_ss_usb_device_capability_descriptor(ss);
		}
	}

	private void bosContainerId(String pre, libusb_context ctx,
		libusb_bos_dev_capability_descriptor bdc) throws LibUsbException {
		var con = LibUsb.libusb_get_container_id_descriptor(ctx, bdc);
		try {
			out.printf("%s: [libusb_container_id_descriptor]%n", pre);
			out.printf("%s: bLength=%d%n", pre, con.bLength);
			out.printf("%s: bDescriptorType=0x%02x %s%n", pre, con.bDescriptorType,
				type(con.bDescriptorType));
			out.printf("%s: bDevCapabilityType=0x%02x %s%n", pre, con.bDevCapabilityType,
				bosType(con.bDevCapabilityType));
			out.printf("%s: ContainerID[]=%s%n", pre, toHex(con.ContainerID));
		} finally {
			LibUsb.libusb_free_container_id_descriptor(con);
		}
	}

	private String descriptor(libusb_device_handle handle, byte desc_index) throws LibUsbException {
		return LibUsb.libusb_get_string_descriptor_ascii(handle, desc_index);
	}

	private static libusb_descriptor_type type(byte bDescriptorType) {
		return libusb_descriptor_type.xcoder.decode(ubyte(bDescriptorType));
	}

	private static libusb_class_code devClass(byte bDeviceClass) {
		return libusb_class_code.xcoder.decode(ubyte(bDeviceClass));
	}

	private static Set<libusb_config_attributes> attrs(byte bmAttributes) {
		return libusb_config_attributes.xcoder
			.decodeAll(ubyte(bmAttributes) & ~LIBUSB_CA_RESERVED1.value);
	}

	private static libusb_bos_type bosType(byte bDevCapabilityType) {
		return libusb_bos_type.xcoder.decode(ubyte(bDevCapabilityType));
	}
}
