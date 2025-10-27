package ceri.serial.libusb;

import java.io.IOException;
import java.util.Locale;
import org.apache.logging.log4j.Level;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import ceri.common.function.Enclosure;
import ceri.common.test.Assert;
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
		Assert.match(version, "\\d+\\.\\d+\\.\\d+\\.\\d+");
		Assert.notNull(version.describe());
		Assert.notNull(version.rcSuffix());
	}

	@Test
	public void shouldSetLocale() throws IOException {
		Usb.setLocale(Locale.US);
		Assert.equal(lib.data.locale(), "en_US");
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
			Assert.thrown(() -> usb.useUsbDk(true)); // already closed
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
			Assert.notNull(deviceHandle);
			if (deviceHandle != null) deviceHandle.close();
		}
	}

	@Test
	public void shouldOpenFromFinder() throws IOException {
		lib.data.addConfig(LibUsbSampleData.kbConfig());
		lib.data.addConfig(LibUsbSampleData.sdReaderConfig());
		try (var usb = Usb.of()) {
			try (var deviceHandle = usb.open(LibUsbFinder.of(0, 0x8406))) {
				Assert.notNull(deviceHandle);
			}
		}
	}

}
