package ceri.serial.libusb;

import static ceri.common.test.AssertUtil.assertArray;
import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertMatch;
import static ceri.common.test.AssertUtil.assertNotNull;
import static ceri.common.test.AssertUtil.assertThrown;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import ceri.common.util.Enclosed;
import ceri.serial.libusb.jna.LibUsb.libusb_speed;
import ceri.serial.libusb.jna.LibUsbException;
import ceri.serial.libusb.jna.LibUsbSampleData;
import ceri.serial.libusb.jna.TestLibUsbNative;

public class UsbDeviceBehavior {
	private TestLibUsbNative lib;
	private Enclosed<RuntimeException, TestLibUsbNative> enc;
	private Usb usb;

	@Before
	public void before() throws LibUsbException {
		enc = TestLibUsbNative.register();
		lib = enc.subject;
		usb = Usb.of();
	}

	@After
	public void after() {
		usb.close();
		enc.close();
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldProvideConfiguration() throws LibUsbException {
		lib.data.deviceConfigs.add(LibUsbSampleData.externalUsb3HubConfig());
		lib.data.deviceConfigs.add(LibUsbSampleData.mouseConfig());
		lib.data.deviceConfigs.get(1).parent = lib.data.deviceConfigs.get(0);
		try (var list = usb.deviceList(); var dev = list.devices().get(1)) {
			assertEquals(dev.parent().device(), list.devices().get(0).device());
			assertEquals(dev.portNumber(), 1);
			assertArray(dev.portNumbers(), 1);
			assertEquals(dev.speed(), libusb_speed.LIBUSB_SPEED_LOW);
			assertEquals(dev.maxPacketSize(0x81), 4);
			assertEquals(dev.maxIsoPacketSize(0x81), 4);
			assertNotNull(dev.config());
			assertNotNull(dev.configByValue(1));
			assertMatch(dev.toString(), "%s\\(native@.*\\)", dev.getClass().getSimpleName());
			dev.unref();
		}
	}

	@Test
	public void shouldProvideStringRepresentation() throws LibUsbException {
		lib.data.deviceConfigs.add(LibUsbSampleData.mouseConfig());
		try (var list = usb.deviceList(); var dev = list.devices().get(0)) {
			assertMatch(dev.toString(), "%s\\(native@.*\\,0\\)", dev.getClass().getSimpleName());
			dev.unref();
			dev.close();
			assertThrown(() -> dev.config());
			assertMatch(dev.toString(), "%s\\(null,0\\)", dev.getClass().getSimpleName());
		}
	}

}
