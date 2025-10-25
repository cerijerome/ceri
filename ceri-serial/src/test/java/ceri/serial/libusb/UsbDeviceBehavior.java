package ceri.serial.libusb;

import static ceri.common.test.Assert.assertArray;
import static ceri.common.test.Assert.assertEquals;
import static ceri.common.test.Assert.assertMatch;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import ceri.common.function.Enclosure;
import ceri.common.test.Assert;
import ceri.serial.libusb.jna.LibUsb.libusb_speed;
import ceri.serial.libusb.jna.LibUsbException;
import ceri.serial.libusb.test.LibUsbSampleData;
import ceri.serial.libusb.test.TestLibUsbNative;

public class UsbDeviceBehavior {
	private TestLibUsbNative lib;
	private Enclosure<TestLibUsbNative> enc;
	private Usb usb;

	@Before
	public void before() throws LibUsbException {
		enc = TestLibUsbNative.register();
		lib = enc.ref;
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
		var hubConfig = LibUsbSampleData.externalUsb3HubConfig();
		var mouseConfig = LibUsbSampleData.mouseConfig();
		mouseConfig.parent = hubConfig;
		lib.data.addConfig(hubConfig, mouseConfig);
		try (var list = usb.deviceList(); var dev = list.devices().get(1)) {
			assertEquals(dev.parent().device(), list.devices().get(0).device());
			assertEquals(dev.parent().parent(), null);
			assertEquals(dev.portNumber(), 1);
			assertArray(dev.portNumbers(), 1);
			assertEquals(dev.speed(), libusb_speed.LIBUSB_SPEED_LOW);
			assertEquals(dev.maxPacketSize(0x81), 4);
			assertEquals(dev.maxIsoPacketSize(0x81), 4);
			Assert.notNull(dev.config());
			Assert.notNull(dev.configByValue(1));
			assertMatch(dev.toString(), "%s\\(native@.*\\)", dev.getClass().getSimpleName());
			dev.unref();
		}
	}

	@Test
	public void shouldProvideStringRepresentation() throws LibUsbException {
		lib.data.addConfig(LibUsbSampleData.mouseConfig());
		try (var list = usb.deviceList(); var dev = list.devices().get(0)) {
			assertMatch(dev.toString(), "%s\\(native@.*\\,0\\)", dev.getClass().getSimpleName());
			dev.unref();
			dev.close();
			Assert.thrown(() -> dev.config());
			assertMatch(dev.toString(), "%s\\(null,0\\)", dev.getClass().getSimpleName());
		}
	}
}
