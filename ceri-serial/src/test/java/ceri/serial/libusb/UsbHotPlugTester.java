package ceri.serial.libusb;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ceri.common.concurrent.ConcurrentUtil;
import ceri.common.io.IoUtil;
import ceri.common.test.TestUtil;
import ceri.jna.clib.jna.CSignal;
import ceri.log.test.LogModifier;
import ceri.log.util.LogUtil;
import ceri.serial.libusb.UsbHotPlug.Callback;
import ceri.serial.libusb.jna.LibUsb.libusb_hotplug_event;
import ceri.serial.libusb.jna.LibUsbException;

public class UsbHotPlugTester {
	private static final Logger logger = LogManager.getFormatterLogger();
	private static final int POLL_MS = 200;

	private static record Event(UsbDevice device, libusb_hotplug_event event, String message) {}

	@SuppressWarnings("resource")
	public static void main(String[] args) throws IOException {
		LogModifier.of(Level.INFO, UsbHotPlugTester.class);
		init();
		try (Usb usb = Usb.of()) {
			List<UsbHotPlug> hotPlugs = new ArrayList<>();
			try {
				Deque<Event> events = new ArrayDeque<>();
				registerHotPlugs(usb, events, hotPlugs);
				logger.info("Processing events...");
				while (IoUtil.availableChar() == 0) {
					showEvents(events);
					usb.events().handleTimeoutCompleted(Duration.ZERO, null);
					ConcurrentUtil.delay(POLL_MS);
					showEvents(events);
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

	private static void init() throws IOException {
		logger.info("Trying to prevent SIGABRT");
		CSignal.signal(CSignal.SIGABRT, 1);
		CSignal.raise(CSignal.SIGABRT);
		var version = Usb.version();
		logger.info("Version: %s rc=%s desc=%s", version, version.rcSuffix(), version.describe());
		logger.info("Hotplug capability: %s", UsbHotPlug.hasCapability());
	}

	@SuppressWarnings("resource")
	private static void registerHotPlugs(Usb usb, Deque<Event> events, List<UsbHotPlug> hotPlugs)
		throws LibUsbException {
		logger.info("Registering callbacks");
		hotPlugs.add(
			usb.hotPlug(callback(events, "ARRIVE+enumerate")).arrived().enumerate().register());
		hotPlugs.add(usb.hotPlug(callback(events, "LEFT")).left().register());
		hotPlugs.add(usb.hotPlug(callback(events, "ALL")).left().arrived().register());
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
			if (event.event == libusb_hotplug_event.LIBUSB_HOTPLUG_EVENT_DEVICE_ARRIVED) {
				try (UsbDeviceHandle handle = device.open()) {
					fields.put("manufacturer", desc.manufacturer(handle));
					fields.put("product", desc.product(handle));
				}
			}
		} catch (LibUsbException e) {
			logger.error(e.getMessage());
		}
		return fields;
	}

	private static Callback callback(Deque<Event> events, String message) {
		return (device, event) -> {
			events.offer(new Event(device, event, message));
			return false;
		};
	}
}
