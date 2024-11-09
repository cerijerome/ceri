package ceri.serial.libusb;

import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.fail;
import static ceri.common.test.ErrorGen.IOX;
import static ceri.serial.libusb.jna.LibUsb.libusb_class_code.LIBUSB_CLASS_HID;
import org.apache.logging.log4j.Level;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import ceri.common.test.CallSync;
import ceri.common.util.Enclosed;
import ceri.log.test.LogModifier;
import ceri.serial.libusb.jna.LibUsb.libusb_capability;
import ceri.serial.libusb.jna.LibUsb.libusb_hotplug_event;
import ceri.serial.libusb.test.LibUsbSampleData;
import ceri.serial.libusb.test.TestLibUsbNative;
import ceri.serial.libusb.test.TestLibUsbNative.HotPlugEvent;
import ceri.serial.libusb.jna.LibUsbException;

public class UsbHotPlugBehavior {
	private TestLibUsbNative lib;
	private Enclosed<RuntimeException, TestLibUsbNative> enc;
	private Usb usb;
	private CallSync.Function<CallbackArgs, Boolean> callback;
	private UsbHotPlug.Builder builder;

	private static record CallbackArgs(UsbDevice device, libusb_hotplug_event event) {}

	@Before
	public void before() throws LibUsbException {
		enc = TestLibUsbNative.register();
		lib = enc.ref;
		usb = Usb.of();
		callback = CallSync.function(null, false);
		builder = usb.hotPlug((device, event) -> callback.apply(new CallbackArgs(device, event)));
	}

	@After
	public void after() {
		usb.close();
		enc.close();
	}

	@Test
	public void shouldDetermineHotPlugCapability() throws LibUsbException {
		assertEquals(UsbHotPlug.hasCapability(), false);
		lib.data.capabilities(libusb_capability.LIBUSB_CAP_HAS_HOTPLUG.value);
		assertEquals(UsbHotPlug.hasCapability(), true);
	}

	@Test
	public void shouldReceiveEventCallback() throws LibUsbException {
		lib.data.addConfig(LibUsbSampleData.mouseConfig());
		try (var hotPlug = builder.arrived().left().vendor(123).product(0).enumerate()
			.deviceClass(LIBUSB_CLASS_HID).register(); var mouse = device(hotPlug, 0)) {
			lib.handleHotPlugEvent.autoResponses(
				new HotPlugEvent(mouse.device(),
					libusb_hotplug_event.LIBUSB_HOTPLUG_EVENT_DEVICE_ARRIVED),
				new HotPlugEvent(mouse.device(),
					libusb_hotplug_event.LIBUSB_HOTPLUG_EVENT_DEVICE_LEFT));
			callback.autoResponses(false, true, false);
			usb.events().handle(); // arrived
			assertCallback(callback.awaitAuto(), 0x14, 0x08,
				libusb_hotplug_event.LIBUSB_HOTPLUG_EVENT_DEVICE_ARRIVED);
			usb.events().handle(); // left + deregister
			assertCallback(callback.awaitAuto(), 0x14, 0x08,
				libusb_hotplug_event.LIBUSB_HOTPLUG_EVENT_DEVICE_LEFT);
			callback.reset();
			usb.events().handle(); // none
			callback.assertCalls(0);
		}
	}

	@Test
	public void shouldContinueOnCallbackError() throws LibUsbException {
		lib.data.addConfig(LibUsbSampleData.sdReaderConfig());
		LogModifier.run(() -> {
			try (var hotPlug = builder.arrived().register(); var device = device(hotPlug, 0)) {
				lib.handleHotPlugEvent.autoResponses(new HotPlugEvent(device.device(),
					libusb_hotplug_event.LIBUSB_HOTPLUG_EVENT_DEVICE_ARRIVED));
				callback.error.setFrom(IOX, null);
				usb.events().handle(); // arrived + error
				assertCallback(callback.awaitAuto(), 0x14, 0x0b,
					libusb_hotplug_event.LIBUSB_HOTPLUG_EVENT_DEVICE_ARRIVED);
				usb.events().handle(); // arrived
				assertCallback(callback.awaitAuto(), 0x14, 0x0b,
					libusb_hotplug_event.LIBUSB_HOTPLUG_EVENT_DEVICE_ARRIVED);
			}
		}, Level.OFF, UsbHotPlug.class);
	}

	@SuppressWarnings("resource")
	private static UsbDevice device(UsbHotPlug hotPlug, int index) throws LibUsbException {
		try (var devices = hotPlug.usb().deviceList()) {
			var device = devices.devices().get(index);
			device.ref();
			return device;
		}
	}

	private static boolean assertCallback(CallbackArgs args, int busNumber, int address,
		libusb_hotplug_event event) {
		try {
			assertEquals(args.device.busNumber(), busNumber);
			assertEquals(args.device.address(), address);
			assertEquals(args.event, event);
		} catch (LibUsbException e) {
			fail(e);
		}
		return false;
	}

}
