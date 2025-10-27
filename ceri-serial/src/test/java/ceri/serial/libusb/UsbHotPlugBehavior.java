package ceri.serial.libusb;

import org.apache.logging.log4j.Level;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import ceri.common.function.Enclosure;
import ceri.common.test.Assert;
import ceri.common.test.CallSync;
import ceri.common.test.ErrorGen;
import ceri.log.test.LogModifier;
import ceri.serial.libusb.jna.LibUsb.libusb_capability;
import ceri.serial.libusb.jna.LibUsb.libusb_class_code;
import ceri.serial.libusb.jna.LibUsb.libusb_hotplug_event;
import ceri.serial.libusb.jna.LibUsbException;
import ceri.serial.libusb.test.LibUsbSampleData;
import ceri.serial.libusb.test.TestLibUsbNative;
import ceri.serial.libusb.test.TestLibUsbNative.HotPlugEvent;

public class UsbHotPlugBehavior {
	private TestLibUsbNative lib;
	private Enclosure<TestLibUsbNative> enc;
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
		Assert.equal(UsbHotPlug.hasCapability(), false);
		lib.data.capabilities(libusb_capability.LIBUSB_CAP_HAS_HOTPLUG.value);
		Assert.equal(UsbHotPlug.hasCapability(), true);
	}

	@Test
	public void shouldReceiveEventCallback() throws LibUsbException {
		lib.data.addConfig(LibUsbSampleData.mouseConfig());
		try (
			var hotPlug = builder.arrived().left().vendor(123).product(0).enumerate()
				.deviceClass(libusb_class_code.LIBUSB_CLASS_HID).register();
			var mouse = device(hotPlug, 0)) {
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
				callback.error.setFrom(ErrorGen.IOX, null);
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
			Assert.equal(args.device.busNumber(), busNumber);
			Assert.equal(args.device.address(), address);
			Assert.equal(args.event, event);
		} catch (LibUsbException e) {
			Assert.fail(e);
		}
		return false;
	}
}
