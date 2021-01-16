package ceri.serial.libusb;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ceri.common.concurrent.ConcurrentUtil;
import ceri.serial.libusb.jna.LibUsb.libusb_hotplug_event;
import ceri.serial.libusb.jna.LibUsb.libusb_hotplug_flag;
import ceri.serial.libusb.jna.LibUsbException;

public class UsbHotplugTester {
	private static final Logger logger = LogManager.getLogger();
	private static final int WAIT_MS = 30 * 1000;
	private static final int DELAY_MS = 100;

	@SuppressWarnings("resource")
	public static void main(String[] args) throws LibUsbException {
		logger.info("Hotplug capability: {}", UsbHotplug.hasCapability());
		try (Usb ctx = Usb.of()) {
			logger.info("Registering callback");
			ctx.hotplug().registration( //
				(context, device, event, userData) -> arrived(device, userData), "hello!")
				.events(libusb_hotplug_event.LIBUSB_HOTPLUG_EVENT_DEVICE_ARRIVED)
				.flags(libusb_hotplug_flag.LIBUSB_HOTPLUG_ENUMERATE).register();
			ctx.hotplug().registration( //
				(context, device, event, userData) -> left(device, userData), "goodbye!")
				.events(libusb_hotplug_event.LIBUSB_HOTPLUG_EVENT_DEVICE_LEFT).register();
			ctx.hotplug().registration( //
				(context, device, event, userData) -> all(device)).allEvents().register();
			System.gc();
			logger.info("Repeating System.gc() over {} ms...", WAIT_MS);
			long t = System.currentTimeMillis();
			while (System.currentTimeMillis() - t < WAIT_MS) {
				ctx.events().handleCompleted();
				ConcurrentUtil.delay(DELAY_MS);
				System.gc();
			}
			logger.info("Done");
		}
	}

	private static boolean arrived(UsbDevice device, String userData) throws LibUsbException {
		logger.info("Arrived: {} {}", userData, identifier(device));
		var desc = device.descriptor();
		try (UsbDeviceHandle handle = device.open()) {
			String manu = desc.manufacturer(handle);
			String prod = desc.product(handle);
			logger.info("Device: {}/{}", manu, prod);
			return false;
		}
	}

	private static boolean left(UsbDevice device, String userData) throws LibUsbException {
		logger.info("Left: {} {}", userData, identifier(device));
		return false;
	}

	private static boolean all(UsbDevice device) throws LibUsbException {
		logger.info("All: {}", identifier(device));
		return false;
	}

	private static String identifier(UsbDevice device) throws LibUsbException {
		if (device == null) return "";
		var desc = device.descriptor();
		return String.format("vendor=0x%04x product=0x%04x bus_number=0x%02x device_address=0x%02x",
			desc.vendorId(), desc.productId(), device.busNumber(), device.address());
	}

}
