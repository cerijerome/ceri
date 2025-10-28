package ceri.serial.libusb;

import static ceri.serial.libusb.jna.LibUsb.libusb_hotplug_event.LIBUSB_HOTPLUG_EVENT_DEVICE_LEFT;
import java.io.IOException;
import java.time.Duration;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ceri.common.collect.Lists;
import ceri.common.collect.Maps;
import ceri.common.concurrent.Concurrent;
import ceri.common.io.IoUtil;
import ceri.common.test.Testing;
import ceri.common.text.Strings;
import ceri.jna.clib.jna.CSignal;
import ceri.log.test.LogModifier;
import ceri.log.util.LogUtil;
import ceri.serial.libusb.UsbHotPlug.Callback;
import ceri.serial.libusb.jna.LibUsb.libusb_hotplug_event;
import ceri.serial.libusb.jna.LibUsbException;

public class UsbHotPlugTester {
	private static final Logger logger = LogManager.getFormatterLogger();
	private static final int POLL_MS = 200;

	private static record Event(UsbDevice device, libusb_hotplug_event event, String message,
		Map<String, Object> fields) {}

	@SuppressWarnings("resource")
	public static void main(String[] args) throws IOException {
		LogModifier.of(Level.INFO, UsbHotPlugTester.class);
		init();
		try (Usb usb = Usb.of()) {
			//usb.debug(Level.INFO);
			testHotPlugs(usb);
		} catch (Exception e) {
			logger.catching(e);
		}
	}

	private static void testHotPlugs(Usb usb) throws LibUsbException {
		var hotPlugs = Lists.<UsbHotPlug>of();
		var events = new ArrayDeque<Event>();
		try {
			registerHotPlugs(usb, events, hotPlugs);
			logger.info("Processing events...");
			while (IoUtil.availableChar() == 0) {
				showEvents(events);
				usb.events().handleTimeoutCompleted(Duration.ZERO, null);
				Concurrent.delay(POLL_MS);
				showEvents(events);
				Testing.gc();
			}
			logger.info("Done");
		} finally {
			LogUtil.close(hotPlugs);
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
		hotPlugs.add(usb.hotPlug(callback(events, "ARR+")).arrived().enumerate().register());
		hotPlugs.add(usb.hotPlug(callback(events, "LEFT")).left().register());
		hotPlugs.add(usb.hotPlug(callback(events, "ALL")).left().arrived().register());
		Testing.gc();
	}

	private static void showEvents(Deque<Event> events) {
		while (!events.isEmpty()) {
			var event = events.poll();
			logger.info("%-4s %d %s", event.message, event.event.value, event.fields);
			welcome(event);
		}
	}

	private static void welcome(Event event) {
		if (event.event == LIBUSB_HOTPLUG_EVENT_DEVICE_LEFT) return;
		try {
			var desc = event.device.descriptor();
			try (UsbDeviceHandle handle = event.device.open()) {
				logger.info(" => %s : %s", Strings.trim(desc.manufacturer(handle)),
					Strings.trim(desc.product(handle)));
			}
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
	}

	private static Callback callback(Deque<Event> events, String message) {
		return (device, event) -> {
			events.offer(new Event(device, event, message, fields(device)));
			if (event == LIBUSB_HOTPLUG_EVENT_DEVICE_LEFT) device.close();
			return false;
		};
	}

	private static Map<String, Object> fields(UsbDevice device) {
		var fields = Maps.<String, Object>link();
		try {
			var desc = device.descriptor();
			fields.put("vendor", String.format("0x%04x", desc.vendorId()));
			fields.put("product", String.format("0x%04x", desc.productId()));
			fields.put("bus", String.format("0x%02x", device.busNumber()));
			fields.put("addr", String.format("0x%02x", device.address()));
		} catch (LibUsbException e) {
			logger.error(e.getMessage());
		}
		return fields;
	}

}
