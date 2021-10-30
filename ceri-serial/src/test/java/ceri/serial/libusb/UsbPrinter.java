package ceri.serial.libusb;

import static ceri.serial.libusb.jna.LibUsb.libusb_bos_type.LIBUSB_BT_CONTAINER_ID;
import static ceri.serial.libusb.jna.LibUsb.libusb_bos_type.LIBUSB_BT_SS_USB_DEVICE_CAPABILITY;
import static ceri.serial.libusb.jna.LibUsb.libusb_bos_type.LIBUSB_BT_USB_2_0_EXTENSION;
import java.io.PrintStream;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ceri.common.data.ByteProvider;
import ceri.common.data.IntProvider;
import ceri.common.function.ExceptionSupplier;
import ceri.common.reflect.ReflectUtil;
import ceri.serial.libusb.jna.LibUsbException;
import ceri.serial.libusb.jna.LibUsbSampleData;
import ceri.serial.libusb.jna.TestLibUsbNative;

/**
 * Iterates over usb devices and prints configuration info.
 */
public class UsbPrinter {
	private static final Logger logger = LogManager.getLogger();
	private final PrintStream out;
	private final Level logLevel;

	public static void main(String[] args) {
		var printer = new UsbPrinter(System.out, null);
		run(printer);
		runTest(printer);
	}

	public static void runTest(UsbPrinter printer) {
		TestLibUsbNative lib = TestLibUsbNative.of();
		try (var enc = TestLibUsbNative.register(lib)) {
			LibUsbSampleData.populate(lib.data);
			run(printer);
		}
	}

	public static void run(UsbPrinter printer) {
		printer.print();
	}

	private UsbPrinter(PrintStream out, Level logLevel) {
		this.out = out;
		this.logLevel = logLevel;
	}

	public void print() {
		String pre = "";
		try (Usb usb = Usb.of()) {
			if (logLevel != null) usb.debug(logLevel);
			version(pre);
			devices(pre, usb);
		} catch (Exception e) {
			logger.catching(e);
		}
	}

	private void version(String pre) throws LibUsbException {
		var v = Usb.version();
		out.printf("%s: [%s]%n", pre, name(v));
		out.printf("%s: toString()=%s%n", pre, v);
		out.printf("%s: describe()=%s%n", pre, v.describe());
		out.printf("%s: rcSuffix()=%s%n", pre, v.rcSuffix());
		out.println();
	}

	private void devices(String pre0, Usb usb) throws Exception {
		try (var list = usb.deviceList()) {
			var devices = list.devices();
			out.printf("#devices=%d%n", devices.size());
			for (int i = 0; i < devices.size(); i++) {
				String pre = pre0 + i;
				out.printf("%s:----------------------------------------%n", pre);
				try (var device = devices.get(i)) {
					device(pre, device, i);
					var desc = device.descriptor();
					try (var handle = device.open()) {
						desc(pre, handle, desc);
						configs(pre, device, handle, desc.configurationCount());
						try (var bos = handle.bosDescriptor()) {
							bos(pre, bos);
						}
					}
				}
				out.println();
			}
		}
	}

	private void device(String pre, UsbDevice device, int index) throws Exception {
		out.printf("%s: [%s #%d]%n", pre, name(device), index);
		out.printf("%s: busNumber()=0x%02x%n", pre, device.busNumber());
		out.printf("%s: portNumber()=0x%02x%n", pre, device.portNumber());
		out.printf("%s: portNumbers()=%s%n", pre, hex(device.portNumbers()));
		out.printf("%s: deviceAddress()=0x%02x%n", pre, device.address());
		out.printf("%s: deviceSpeed()=%s%n", pre, device.speed());
	}

	private void desc(String pre, UsbDeviceHandle handle, UsbDescriptors.Device desc)
		throws Exception {
		out.printf("%s: [%s]%n", pre, name(desc));
		out.printf("%s: usbVersion()=0x%04x%n", pre, desc.usbVersion());
		out.printf("%s: deviceClass()=%s%n", pre, desc.classCode());
		out.printf("%s: deviceSubClass()=0x%02x%n", pre, desc.subClass());
		out.printf("%s: deviceProtocol()=0x%02x%n", pre, desc.protocol());
		out.printf("%s: maxPacketSize0()=%d%n", pre, desc.maxPacketSize0());
		out.printf("%s: vendorId()=0x%04x%n", pre, desc.vendorId());
		out.printf("%s: productId()=0x%04x%n", pre, desc.productId());
		out.printf("%s: deviceVersion()=0x%04x%n", pre, desc.deviceVersion());
		out.printf("%s: manufacturer()=%s%n", pre, desc.manufacturer(handle));
		out.printf("%s: product()=%s%n", pre, desc.product(handle));
		out.printf("%s: serialNumber()=%s%n", pre, desc.serialNumber(handle));
		out.printf("%s: configurationCount()=%d%n", pre, desc.configurationCount());
	}

	private void configs(String pre, UsbDevice device, UsbDeviceHandle handle, int configs)
		throws Exception {
		int currentConfig = handle.configuration();
		out.printf("%s: configuration()=%d%n", pre, currentConfig);
		for (byte i = 0; i < configs; i++) {
			try (var config = device.config(i)) {
				config(pre + "." + i, device, handle, config, currentConfig);
			}
		}
	}

	private void config(String pre, UsbDevice device, UsbDeviceHandle handle,
		UsbDescriptors.Config config, int current) throws Exception {
		boolean active = config.value() == current;
		out.printf("%s: [%s%s #%s]%n", pre, active ? "*" : "", name(config), pre);
		out.printf("%s: value()=%d%n", pre, config.value());
		out.printf("%s: description()=%s%n", pre, config.description(handle));
		out.printf("%s: attributes()=%s%n", pre, config.attributes());
		out.printf("%s: maxPower()=%d%n", pre, config.maxPower());
		out.printf("%s: extra()=%s%n", pre, hex(config.extra()));
		out.printf("%s: interfaceCount()=%s%n", pre, config.interfaceCount());
		var interfaces = config.interfaces();
		for (int i = 0; i < interfaces.size(); i++)
			iface(pre + "." + i, device, handle, interfaces.get(i));
	}

	private void iface(String pre, UsbDevice device, UsbDeviceHandle handle,
		UsbDescriptors.Interface iface) throws Exception {
		var altSettings = iface.altSettings();
		out.printf("%s: [%s #%s]%n", pre, name(iface), pre);
		out.printf("%s: altSettingCount()=%d%n", pre, iface.altSettingCount());
		for (int i = 0; i < altSettings.size(); i++)
			altsetting(pre + "." + i, device, handle, altSettings.get(i));
	}

	private void altsetting(String pre, UsbDevice device, UsbDeviceHandle handle,
		UsbDescriptors.AltSetting alt) throws Exception {
		out.printf("%s: [%s #%s]%n", pre, name(alt), pre);
		out.printf("%s: number()=%d%n", pre, alt.number());
		out.printf("%s: altSetting()=%d%n", pre, alt.altSetting());
		out.printf("%s: classCode()=%s%n", pre, alt.classCode());
		out.printf("%s: subClass()=0x%02x%n", pre, alt.subClass());
		out.printf("%s: protocol()=0x%02x%n", pre, alt.protocol());
		out.printf("%s: description()=%s%n", pre, alt.description(handle));
		out.printf("%s: extra()=%s%n", pre, hex(alt.extra()));
		out.printf("%s: endpointCount()=%d%n", pre, alt.endPointCount());
		var endPoints = alt.endPoints();
		for (int i = 0; i < endPoints.size(); i++)
			endPoint(pre + "." + i, device, endPoints.get(i));
	}

	private void endPoint(String pre, UsbDevice device, UsbDescriptors.EndPoint ep)
		throws LibUsbException {
		out.printf("%s: [%s #%s]%n", pre, name(ep), pre);
		out.printf("%s: endPointAddress()=0x%02x%n", pre, ep.address());
		out.printf("%s:   endPointNumber()=%d%n", pre, ep.number());
		out.printf("%s:   endPointDirection()=%s%n", pre, ep.direction());
		out.printf("%s: attributes()=0x%02x%n", pre, ep.attributes());
		out.printf("%s:   transferType()=%s%n", pre, ep.transferType());
		out.printf("%s:   isoSyncType()=%s%n", pre, ep.isoSyncType());
		out.printf("%s:   isoUsageType()=%s%n", pre, ep.isoUsageType());
		out.printf("%s: maxPacketSize()=%d%n", pre, ep.maxPacketSize());
		out.printf("%s: pollInterval()=0x%02x%n", pre, ep.pollInterval());
		out.printf("%s: audioRefreshRate()=0x%02x%n", pre, ep.audioRefreshRate());
		out.printf("%s: audioSyncAddress()=0x%02x%n", pre, ep.audioSyncAddress());
		out.printf("%s: extra()=%s%n", pre, hex(ep.extra()));
		out.printf("%s: maxPacketSize(0x%02x)=%s%n", pre, ep.address(),
			get(() -> device.maxPacketSize(ep.address())));
		out.printf("%s: maxIsoPacketSize(0x%02x)=%s%n", pre, ep.address(),
			get(() -> device.maxIsoPacketSize(ep.address())));
		try (var ss = ep.ssEndPointCompanion()) {
			ssEndPoint(pre, ss);
		}
	}

	private void ssEndPoint(String pre, UsbDescriptors.SsEndPointCompanion ss) {
		if (ss == null) return;
		out.printf("%s: [%s]%n", pre, name(ss));
		out.printf("%s: maxBurstPackets()=%d%n", pre, ss.maxBurstPackets());
		out.printf("%s: attributes()=0x%02x%n", pre, ss.attributes());
		out.printf("%s:   maxBulkStreams()=%d%n", pre, ss.maxBulkStreams());
		out.printf("%s:   isoMult()=%d%n", pre, ss.isoMult());
		out.printf("%s: bytesPerInterval()=%d%n", pre, ss.bytesPerInterval());
	}

	private void bos(String pre, UsbDescriptors.Bos bos) throws LibUsbException {
		if (bos == null) return;
		out.printf("%s: [%s]%n", pre, name(bos));
		out.printf("%s: capabilityCount()=%d%n", pre, bos.capabilityCount());
		var capabilities = bos.capabilities();
		for (int i = 0; i < capabilities.size(); i++) {
			var bdc = capabilities.get(i);
			var bosType = bdc.bosType();
			var pre0 = pre + "." + i;
			if (bosType == LIBUSB_BT_USB_2_0_EXTENSION) bosUsb20Ext(pre0, bdc);
			else if (bosType == LIBUSB_BT_SS_USB_DEVICE_CAPABILITY) bosSsUsbDevCap(pre0, bdc);
			else if (bosType == LIBUSB_BT_CONTAINER_ID) bosContainerId(pre0, bdc);
			else bosDevCapability(pre0, bdc);
		}
	}

	private void bosDevCapability(String pre, UsbDescriptors.BosDevCapability bdc) {
		if (bdc == null) return;
		out.printf("%s: [%s #%s]%n", pre, name(bdc), pre);
		out.printf("%s: bosType()=%s%n", pre, bdc.bosType());
		out.printf("%s: capabilityData()=%s%n", pre, hex(bdc.capabilityData()));
	}

	private void bosUsb20Ext(String pre, UsbDescriptors.BosDevCapability bdc)
		throws LibUsbException {
		try (var usb = bdc.usb20Extension()) {
			if (usb == null) return;
			out.printf("%s: [%s #%s]%n", pre, name(usb), pre);
			out.printf("%s: attributes()=%s%n", pre, usb.attributes());
		}
	}

	private void bosSsUsbDevCap(String pre, UsbDescriptors.BosDevCapability bdc)
		throws LibUsbException {
		try (var ss = bdc.ssUsbDeviceCapability()) {
			if (ss == null) return;
			out.printf("%s: [%s #%s]%n", pre, name(ss), pre);
			out.printf("%s: attributes()=%s%n", pre, ss.attributes());
			out.printf("%s: supportedSpeeds()=%s%n", pre, ss.supportedSpeeds());
			out.printf("%s: functionalitySupport()=0x%02x%n", pre, ss.functionalitySupport());
			out.printf("%s: u1DeviceExitLatency()=0x%02x%n", pre, ss.u1DeviceExitLatency());
			out.printf("%s: u2DeviceExitLatency()=0x%04x%n", pre, ss.u2DeviceExitLatency());
		}
	}

	private void bosContainerId(String pre, UsbDescriptors.BosDevCapability bdc)
		throws LibUsbException {
		try (var con = bdc.containerId()) {
			if (con == null) return;
			out.printf("%s: [%s #%s]%n", pre, name(con), pre);
			out.printf("%s: uuid()=%s%n", pre, hex(con.uuid()));
		}
	}

	private static String name(Object obj) {
		return ReflectUtil.name(obj.getClass());
	}

	private static String hex(ByteProvider bytes) {
		return ByteProvider.toHex(bytes);
	}

	private static String hex(IntProvider ints) {
		return IntProvider.toHex(ints);
	}

	private static <T> T get(ExceptionSupplier<LibUsbException, T> supplier) {
		try {
			return supplier.get();
		} catch (LibUsbException e) {
			return null;
		}
	}
}
