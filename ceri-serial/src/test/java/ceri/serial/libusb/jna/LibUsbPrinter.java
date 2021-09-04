package ceri.serial.libusb.jna;

import static ceri.serial.libusb.jna.LibUsb.libusb_bos_type.LIBUSB_BT_CONTAINER_ID;
import static ceri.serial.libusb.jna.LibUsb.libusb_bos_type.LIBUSB_BT_SS_USB_DEVICE_CAPABILITY;
import static ceri.serial.libusb.jna.LibUsb.libusb_bos_type.LIBUSB_BT_USB_2_0_EXTENSION;
import static ceri.serial.libusb.jna.LibUsb.libusb_option.LIBUSB_OPTION_LOG_LEVEL;
import java.io.PrintStream;
import java.util.regex.Pattern;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ceri.common.collection.ArrayUtil;
import ceri.common.reflect.ReflectUtil;
import ceri.common.text.RegexUtil;
import ceri.serial.libusb.jna.LibUsb.libusb_bos_dev_capability_descriptor;
import ceri.serial.libusb.jna.LibUsb.libusb_config_descriptor;
import ceri.serial.libusb.jna.LibUsb.libusb_context;
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
	private static final Pattern NAME_REGEX = Pattern.compile("\\.(\\w+)");
	private final PrintStream out;
	private final libusb_log_level logLevel;

	public static void main(String[] args) throws LibUsbException {
		System.setProperty("jna.debug_load", "true");
		printenv();
		var printer = new LibUsbPrinter(System.out, null);
		run(printer);
		runTest(printer);
	}

	private static void printenv() {
		System.getProperties().forEach((k, v) -> System.out.println(k + " = " + v));
		System.out.println();
		System.getenv().forEach((k, v) -> System.out.println(k + " = " + v));
	}
	
	public static void runTest(LibUsbPrinter printer) throws LibUsbException {
		TestLibUsbNative lib = TestLibUsbNative.of();
		try (var enc = TestLibUsbNative.register(lib)) {
			LibUsbSampleData.populate(lib.data);
			run(printer);
		}
	}

	public static void run(LibUsbPrinter printer) throws LibUsbException {
		printer.print();
	}

	private LibUsbPrinter(PrintStream out, libusb_log_level logLevel) {
		this.out = out;
		this.logLevel = logLevel;
	}

	public void print() throws LibUsbException {
		String pre = "";
		libusb_context ctx = null;
		try {
			ctx = LibUsb.libusb_init();
			if (logLevel != null) LibUsb.libusb_set_option(ctx, LIBUSB_OPTION_LOG_LEVEL, logLevel);
			version(pre);
			devices(pre, ctx);
		} catch (Exception e) {
			logger.catching(e);
		} finally {
			printenv();
			if (ctx != null) LibUsb.libusb_exit(ctx);
		}
	}

	private void version(String pre) throws LibUsbException {
		libusb_version v = LibUsb.libusb_get_version();
		out.printf("%s: [%s]%n", pre, name(v));
		out.printf("%s: version=%d.%d.%d.%d%n", pre, v.major, v.minor, v.micro, v.nano);
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
		out.printf("%s: [%s]%n", pre, name(device));
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
		out.printf("%s: port_numbers()=%s%n", pre, hex(LibUsb.libusb_get_port_numbers(device)));
		out.printf("%s: device_address()=0x%02x%n", pre, LibUsb.libusb_get_device_address(device));
		out.printf("%s: device_speed()=%s%n", pre, LibUsb.libusb_get_device_speed(device));
	}

	private void desc(String pre, libusb_device_handle handle, libusb_device_descriptor desc)
		throws LibUsbException {
		out.printf("%s: [%s]%n", pre, name(desc));
		out.printf("%s: bLength=%d%n", pre, desc.bLength);
		out.printf("%s: bDescriptorType=0x%02x %s%n", pre, desc.bDescriptorType,
			desc.bDescriptorType());
		out.printf("%s: bcdUSB=0x%04x%n", pre, desc.bcdUSB);
		out.printf("%s: bDeviceClass=0x%02x %s%n", pre, desc.bDeviceClass, desc.bDeviceClass());
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
		out.printf("%s: [%s #%s]%n", pre, name(config), pre);
		out.printf("%s: bLength=%d%n", pre, config.bLength);
		out.printf("%s: bDescriptorType=0x%02x %s%n", pre, config.bDescriptorType,
			config.bDescriptorType());
		out.printf("%s: wTotalLength=%d%n", pre, config.wTotalLength);
		out.printf("%s: bNumInterfaces=%d%n", pre, config.bNumInterfaces);
		out.printf("%s: bConfigurationValue=%d%n", pre, config.bConfigurationValue);
		out.printf("%s: iConfiguration=%d \"%s\"%n", pre, config.iConfiguration,
			descriptor(handle, config.iConfiguration));
		out.printf("%s: bmAttributes=0x%02x %s%n", pre, config.bmAttributes, config.bmAttributes());
		out.printf("%s: bMaxPower=%d%n", pre, config.bMaxPower);
		out.printf("%s: extra=%s%n", pre, hex(config.extra()));
		libusb_interface[] interfaces = config.interfaces();
		out.printf("%s: #interfaces=%d%n", pre, interfaces.length);
		for (int i = 0; i < interfaces.length; i++)
			iface(pre + "." + i, ctx, handle, interfaces[i]);
	}

	private void iface(String pre, libusb_context ctx, libusb_device_handle handle,
		libusb_interface iface) throws LibUsbException {
		libusb_interface_descriptor[] altsettings = iface.altsettings();
		out.printf("%s: [%s #%s]%n", pre, name(iface), pre);
		out.printf("%s: #altsettings=%d%n", pre, altsettings.length);
		for (int i = 0; i < altsettings.length; i++)
			altsetting(pre + "." + i, ctx, handle, altsettings[i]);
	}

	private void altsetting(String pre, libusb_context ctx, libusb_device_handle handle,
		libusb_interface_descriptor alt) throws LibUsbException {
		out.printf("%s: [%s #%s]%n", pre, name(alt), pre);
		out.printf("%s: bLength=%d%n", pre, alt.bLength);
		out.printf("%s: bDescriptorType=0x%02x %s%n", pre, alt.bDescriptorType,
			alt.bDescriptorType());
		out.printf("%s: bInterfaceNumber=%d%n", pre, alt.bInterfaceNumber);
		out.printf("%s: bAlternateSetting=%d%n", pre, alt.bAlternateSetting);
		out.printf("%s: bNumEndpoints=%d%n", pre, alt.bNumEndpoints);
		out.printf("%s: bInterfaceClass=0x%02x %s%n", pre, alt.bInterfaceClass,
			alt.bInterfaceClass());
		out.printf("%s: bInterfaceSubClass=0x%02x%n", pre, alt.bInterfaceSubClass);
		out.printf("%s: bInterfaceProtocol=0x%02x%n", pre, alt.bInterfaceProtocol);
		out.printf("%s: iInterface=%d \"%s\"%n", pre, alt.iInterface,
			descriptor(handle, alt.iInterface));
		out.printf("%s: extra=%s%n", pre, hex(alt.extra()));
		libusb_endpoint_descriptor[] endpoints = alt.endpoints();
		out.printf("%s: #endpoints=%d%n", pre, endpoints.length);
		for (int i = 0; i < endpoints.length; i++)
			endpoint(pre + "." + i, ctx, endpoints[i]);
	}

	private void endpoint(String pre, libusb_context ctx, libusb_endpoint_descriptor ep)
		throws LibUsbException {
		out.printf("%s: [%s #%s]%n", pre, name(ep), pre);
		out.printf("%s: bLength=%d%n", pre, ep.bLength);
		out.printf("%s: bDescriptorType=0x%02x %s%n", pre, ep.bDescriptorType,
			ep.bDescriptorType());
		out.printf("%s: bEndpointAddress=0x%02x%n", pre, ep.bEndpointAddress);
		out.printf("%s:   Number=%d%n", pre, ep.bEndpointNumber());
		out.printf("%s:   Direction=%s%n", pre, ep.bEndpointDirection());
		out.printf("%s: bmAttributes=0x%02x%n", pre, ep.bmAttributes);
		out.printf("%s:   TransferType=%s%n", pre, ep.bmAttributesTransferType());
		out.printf("%s:   IsoSyncType=%s%n", pre, ep.bmAttributesIsoSyncType());
		out.printf("%s:   IsoUsageType=%s%n", pre, ep.bmAttributesIsoUsageType());
		out.printf("%s: wMaxPacketSize=%d%n", pre, ep.wMaxPacketSize);
		out.printf("%s: bInterval=0x%02x%n", pre, ep.bInterval);
		out.printf("%s: bRefresh=%d%n", pre, ep.bRefresh);
		out.printf("%s: bSynchAddress=%d%n", pre, ep.bSynchAddress);
		out.printf("%s: extra=%s%n", pre, hex(ep.extra()));
		var ss = LibUsb.libusb_get_ss_endpoint_companion_descriptor(ctx, ep);
		try {
			ssEndPoint(pre, ss);
		} finally {
			LibUsb.libusb_free_ss_endpoint_companion_descriptor(ss);
		}
	}

	private void ssEndPoint(String pre, libusb_ss_endpoint_companion_descriptor ss) {
		if (ss == null) return;
		out.printf("%s: [%s]%n", pre, name(ss));
		out.printf("%s: bLength=%d%n", pre, ss.bLength);
		out.printf("%s: bDescriptorType=0x%02x %s%n", pre, ss.bDescriptorType,
			ss.bDescriptorType());
		out.printf("%s: bMaxBurst=%d%n", pre, ss.bMaxBurst);
		out.printf("%s: bmAttributes=0x%02x%n", pre, ss.bmAttributes);
		out.printf("%s:   BulkMaxStreams=%d%n", pre, ss.bmAttributesBulkMaxStreams());
		out.printf("%s:   IsoMult=%d%n", pre, ss.bmAttributesIsoMult());
		out.printf("%s: wBytesPerInterval=%d%n", pre, ss.wBytesPerInterval);
	}

	private void bos(String pre, libusb_context ctx, libusb_device_handle handle)
		throws LibUsbException {
		var bos = LibUsb.libusb_get_bos_descriptor(handle);
		if (bos == null) return;
		try {
			out.printf("%s: [%s]%n", pre, name(bos));
			out.printf("%s: bLength=%d%n", pre, bos.bLength);
			out.printf("%s: bDescriptorType=0x%02x %s%n", pre, bos.bDescriptorType,
				bos.bDescriptorType());
			out.printf("%s: wTotalLength=%d%n", pre, bos.wTotalLength);
			out.printf("%s: bNumDeviceCaps=%d%n", pre, bos.bNumDeviceCaps);
			for (var bdc : bos.dev_capability) {
				var type = bdc.bDevCapabilityType();
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
		out.printf("%s: [%s]%n", pre, name(bdc));
		out.printf("%s: bLength=%d%n", pre, bdc.bLength);
		out.printf("%s: bDescriptorType=0x%02x %s%n", pre, bdc.bDescriptorType,
			bdc.bDescriptorType());
		out.printf("%s: bDevCapabilityType=0x%02x %s%n", pre, bdc.bDevCapabilityType,
			bdc.bDevCapabilityType());
		out.printf("%s: dev_capability_data[]=%s%n", pre, hex(bdc.dev_capability_data));
	}

	private void bosUsb20Ext(String pre, libusb_context ctx,
		libusb_bos_dev_capability_descriptor bdc) throws LibUsbException {
		var usb = LibUsb.libusb_get_usb_2_0_extension_descriptor(ctx, bdc);
		try {
			out.printf("%s: [%s]%n", pre, name(usb));
			out.printf("%s: bLength=%d%n", pre, usb.bLength);
			out.printf("%s: bDescriptorType=0x%02x %s%n", pre, usb.bDescriptorType,
				usb.bDescriptorType());
			out.printf("%s: bDevCapabilityType=0x%02x %s%n", pre, usb.bDevCapabilityType,
				usb.bDevCapabilityType());
			out.printf("%s: bmAttributes=0x%08x %s%n", pre, usb.bmAttributes, usb.bmAttributes());
		} finally {
			LibUsb.libusb_free_usb_2_0_extension_descriptor(usb);
		}
	}

	private void bosSsUsbDevCap(String pre, libusb_context ctx,
		libusb_bos_dev_capability_descriptor bdc) throws LibUsbException {
		var ss = LibUsb.libusb_get_ss_usb_device_capability_descriptor(ctx, bdc);
		try {
			out.printf("%s: [%s]%n", pre, name(ss));
			out.printf("%s: bLength=%d%n", pre, ss.bLength);
			out.printf("%s: bDescriptorType=0x%02x %s%n", pre, ss.bDescriptorType,
				ss.bDescriptorType());
			out.printf("%s: bDevCapabilityType=0x%02x %s%n", pre, ss.bDevCapabilityType,
				ss.bDevCapabilityType());
			out.printf("%s: bmAttributes=0x%02x %s%n", pre, ss.bmAttributes, ss.bmAttributes());
			out.printf("%s: wSpeedSupported=0x%04x %s%n", pre, ss.wSpeedSupported,
				ss.wSpeedSupported());
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
			out.printf("%s: [%s]%n", pre, name(con));
			out.printf("%s: bLength=%d%n", pre, con.bLength);
			out.printf("%s: bDescriptorType=0x%02x %s%n", pre, con.bDescriptorType,
				con.bDescriptorType());
			out.printf("%s: bDevCapabilityType=0x%02x %s%n", pre, con.bDevCapabilityType,
				con.bDevCapabilityType());
			out.printf("%s: ContainerID[]=%s%n", pre, hex(con.ContainerID));
		} finally {
			LibUsb.libusb_free_container_id_descriptor(con);
		}
	}

	private static String name(Object obj) {
		return RegexUtil.find(NAME_REGEX, ReflectUtil.name(obj.getClass()));
	}

	private static String descriptor(libusb_device_handle handle, byte desc_index)
		throws LibUsbException {
		return LibUsb.libusb_get_string_descriptor_ascii(handle, desc_index);
	}

	private static String hex(byte[] bytes) {
		return ArrayUtil.toHex(bytes);
	}
}
