package ceri.serial.libusb;

import static ceri.serial.libusb.jna.LibUsb.libusb_hotplug_event.LIBUSB_HOTPLUG_EVENT_DEVICE_ARRIVED;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ceri.common.concurrent.ConcurrentUtil;
import ceri.common.io.IoUtil;
import ceri.common.test.TestUtil;
import ceri.log.util.LogUtil;
import ceri.serial.libusb.jna.LibUsb.libusb_hotplug_event;
import ceri.serial.libusb.jna.LibUsb.libusb_hotplug_flag;
import ceri.serial.libusb.jna.LibUsbException;

public class UsbHotplugTester {
	private static final Logger logger = LogManager.getFormatterLogger();
	private static final int POLL_MS = 100;

	private static record Event(UsbDevice device, libusb_hotplug_event event, String message) {}

	@SuppressWarnings("resource")
	public static void main(String[] args) throws LibUsbException {
		logger.info("Hotplug capability: %s", UsbHotPlug.hasCapability());
		try (Usb usb = Usb.of()) {
			List<UsbHotPlug> hotPlugs = new ArrayList<>();
			try {
				Deque<Event> events = new ArrayDeque<>();
				registerHotPlugs(usb, events, hotPlugs);
				logger.info("Processing events...");
				while (IoUtil.availableChar() != 0) {
					showEvents(events);
					usb.events().handleCompleted();
					showEvents(events);
					ConcurrentUtil.delay(POLL_MS);
					TestUtil.gc();
				}
				logger.info("Done");
			} finally {
				LogUtil.close(logger, hotPlugs);
			}
		} catch (Exception e) {
			logger.catching(e);
		}
	}

	@SuppressWarnings("resource")
	private static void registerHotPlugs(Usb usb, Deque<Event> events, List<UsbHotPlug> hotPlugs)
		throws LibUsbException {
		logger.info("Registering callbacks");
		hotPlugs.add(usb.hotPlug( //
			(device, event) -> callback(events, device, event, "arrive:enumerate"))
			.events(libusb_hotplug_event.LIBUSB_HOTPLUG_EVENT_DEVICE_ARRIVED)
			.flags(libusb_hotplug_flag.LIBUSB_HOTPLUG_ENUMERATE).register());
		hotPlugs.add(usb.hotPlug( //
			(device, event) -> callback(events, device, event, "left"))
			.events(libusb_hotplug_event.LIBUSB_HOTPLUG_EVENT_DEVICE_LEFT).register());
		hotPlugs.add(usb.hotPlug( //
			(device, event) -> callback(events, device, event, "all")).allEvents().register());
		TestUtil.gc();
	}

	private static void showEvents(Deque<Event> events) {
		while (!events.isEmpty()) {
			var event = events.poll();
			var fields = fields(event);
			logger.info("%s: %s %s", event.message, event.event, fields);
		}
	}

	private static Map<String, Object> fields(Event event) {
		Map<String, Object> fields = new LinkedHashMap<>();
		var device = event.device;
		try {
			var desc = event.device.descriptor();
			fields.put("vendorId", String.format("0x%04x", desc.vendorId()));
			fields.put("productId", String.format("0x%04x", desc.productId()));
			fields.put("busNumber", String.format("0x%02x", device.busNumber()));
			fields.put("address", String.format("0x%02x", device.address()));
			if (event.event != LIBUSB_HOTPLUG_EVENT_DEVICE_ARRIVED) return fields;
			try (UsbDeviceHandle handle = device.open()) {
				fields.put("manufacturer", desc.manufacturer(handle));
				fields.put("product", desc.product(handle));
			}
		} catch (LibUsbException e) {
			logger.catching(e);
		}
		return fields;
	}

	private static boolean callback(Deque<Event> events, UsbDevice device,
		libusb_hotplug_event event, String message) {
		events.offer(new Event(device, event, message));
		return false;
	}
}
