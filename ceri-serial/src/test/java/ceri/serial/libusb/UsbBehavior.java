package ceri.serial.libusb;

import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertMatch;
import static ceri.common.test.AssertUtil.assertNotNull;
import static ceri.common.test.AssertUtil.assertThrown;
import java.io.IOException;
import java.util.Locale;
import org.apache.logging.log4j.Level;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import ceri.common.util.Enclosure;
import ceri.serial.libusb.jna.LibUsbFinder;
import ceri.serial.libusb.test.LibUsbSampleData;
import ceri.serial.libusb.test.TestLibUsbNative;

public class UsbBehavior {
	private TestLibUsbNative lib;
	private Enclosure<TestLibUsbNative> enc;

	@Before
	public void before() {
		enc = TestLibUsbNative.register();
		lib = enc.ref;
	}

	@After
	public void after() {
		enc.close();
	}

	@Test
	public void shouldProvideVersion() throws IOException {
		var version = Usb.version();
		assertMatch(version, "\\d+\\.\\d+\\.\\d+\\.\\d+");
		assertNotNull(version.describe());
		assertNotNull(version.rcSuffix());
	}

	@Test
	public void shouldSetLocale() throws IOException {
		Usb.setLocale(Locale.US);
		assertEquals(lib.data.locale(), "en_US");
	}

	@Test
	public void shouldCreateInstance() throws IOException {
		try (var usb = Usb.of()) {
			usb.debug(Level.WARN);
			usb.useUsbDk(true);
			usb.useUsbDk(false);
			usb.weakAuthority(true);
			usb.weakAuthority(false);
			usb.close();
			assertThrown(() -> usb.useUsbDk(true)); // already closed
		}
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldOpenDeviceFromList() throws IOException {
		lib.data.addConfig(LibUsbSampleData.kbConfig());
		lib.data.addConfig(LibUsbSampleData.sdReaderConfig());
		try (var usb = Usb.of()) {
			UsbDeviceHandle deviceHandle = null;
			try (var list = usb.deviceList()) {
				for (var device : list.devices()) {
					if (device.descriptor().productId() == 0x8406) deviceHandle = device.open();
				}
			}
			assertNotNull(deviceHandle);
			if (deviceHandle != null) deviceHandle.close();
		}
	}

	@Test
	public void shouldOpenFromFinder() throws IOException {
		lib.data.addConfig(LibUsbSampleData.kbConfig());
		lib.data.addConfig(LibUsbSampleData.sdReaderConfig());
		try (var usb = Usb.of()) {
			try (var deviceHandle = usb.open(LibUsbFinder.of(0, 0x8406))) {
				assertNotNull(deviceHandle);
			}
		}
	}

}
