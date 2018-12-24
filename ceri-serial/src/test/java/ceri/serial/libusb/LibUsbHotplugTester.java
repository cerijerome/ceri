package ceri.serial.libusb;

import static ceri.serial.libusb.LibUsbContext.hasCapability;
import static ceri.serial.libusb.LibUsbHotplug.registration;
import static ceri.serial.libusb.jna.LibUsb.libusb_capability.LIBUSB_CAP_HAS_HOTPLUG;
import static ceri.serial.libusb.jna.LibUsb.libusb_hotplug_event.LIBUSB_HOTPLUG_EVENT_DEVICE_ARRIVED;
import static ceri.serial.libusb.jna.LibUsb.libusb_hotplug_event.LIBUSB_HOTPLUG_EVENT_DEVICE_LEFT;
import static ceri.serial.libusb.jna.LibUsb.libusb_hotplug_flag.LIBUSB_HOTPLUG_ENUMERATE;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ceri.common.util.BasicUtil;
import ceri.serial.libusb.jna.LibUsb.libusb_device_descriptor;
import ceri.serial.libusb.jna.LibUsbException;

public class LibUsbHotplugTester {
	private static final Logger logger = LogManager.getLogger();
	private static final int WAIT_MS = 60 * 1000;
	private static final int DELAY_MS = 100;

	public static void main(String[] args) throws LibUsbException {
		logger.info("Hotplug capability: {}", hasCapability(LIBUSB_CAP_HAS_HOTPLUG));
		try (LibUsbContext ctx = LibUsbContext.init()) {
			logger.info("Registering callback");
			ctx.hotplug().registerCallback(registration( //
				(context, device, event, userData) -> arrived(device, userData), "hello!")
					.events(LIBUSB_HOTPLUG_EVENT_DEVICE_ARRIVED).flags(LIBUSB_HOTPLUG_ENUMERATE));
			ctx.hotplug().registerCallback(registration( //
				(context, device, event, userData) -> left(device, userData), "goodbye!")
					.events(LIBUSB_HOTPLUG_EVENT_DEVICE_LEFT));
			ctx.hotplug().registerCallback(registration( //
				(context, device, event, userData) -> all(device)).allEvents());
			System.gc();
			long t = System.currentTimeMillis();
			while (System.currentTimeMillis() - t < WAIT_MS) {
				ctx.handleEventsCompleted();
				BasicUtil.delay(DELAY_MS);
				System.gc();
			}
			logger.info("Done");
		}
	}

	private static boolean arrived(LibUsbDevice device, String userData) throws LibUsbException {
		logger.info("Arrived: {} {}", userData, identifier(device));
		libusb_device_descriptor desc = device.descriptor();
		try (LibUsbDeviceHandle handle = device.open()) {
			String manu = handle.stringDescriptorAscii(desc.iManufacturer);
			String prod = handle.stringDescriptorAscii(desc.iProduct);
			logger.info("Device: {}/{}", manu, prod);
			return false;
		}
	}

	private static boolean left(LibUsbDevice device, String userData) throws LibUsbException {
		logger.info("Left: {} {}", userData, identifier(device));
		return false;
	}

	private static boolean all(LibUsbDevice device) throws LibUsbException {
		logger.info("All: {}", identifier(device));
		return false;
	}

	private static String identifier(LibUsbDevice device) throws LibUsbException {
		if (device == null) return "";
		libusb_device_descriptor desc = device.descriptor();
		return String.format("vendor=0x%04x product=0x%04x bus_number=0x%02x device_address=0x%02x",
			desc.idVendor, desc.idProduct, device.busNumber(), device.address());
	}

}
