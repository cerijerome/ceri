package ceri.serial.libusb;

import static ceri.common.test.AssertUtil.*;
import static ceri.common.test.ErrorGen.*;
import static ceri.common.test.TestUtil.*;
import static org.hamcrest.CoreMatchers.*;
import static org.mockito.Mockito.*;
import java.io.IOException;
import java.util.Locale;
import org.apache.logging.log4j.Level;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import ceri.common.test.CallSync;
import ceri.common.util.Enclosed;
import ceri.serial.ftdi.Ftdi;
import ceri.serial.libusb.jna.LibUsbFinder;
import ceri.serial.libusb.jna.LibUsbSampleData;
import ceri.serial.libusb.jna.TestLibUsbNative;

public class UsbBehavior {
	private TestLibUsbNative lib;
	private Enclosed<RuntimeException, TestLibUsbNative> enc;

	@Before
	public void before() {
		enc = TestLibUsbNative.register();
		lib = enc.subject;
		// LibUsbSampleData.populate(lib.data);
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
		assertEquals(lib.data.locale, "en_US");
	}

	@Test
	public void shouldCreateDefault() throws IOException {
		try (var usb = Usb.ofDefault()) {
			usb.debug(Level.WARN);
			usb.useUsbDk(false);
			usb.weakAuthority(true);
		}
	}

	@Test
	public void shouldCreateInstance() throws IOException {
		@SuppressWarnings("resource")
		var usb = Usb.of();
		usb.useUsbDk(true);
		usb.weakAuthority(false);
		usb.close();
		assertThrown(() -> usb.useUsbDk(true)); // already closed
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldOpenDeviceFromList() throws IOException {
		lib.data.deviceConfigs.add(LibUsbSampleData.kbConfig());
		lib.data.deviceConfigs.add(LibUsbSampleData.sdReaderConfig());
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
		lib.data.deviceConfigs.add(LibUsbSampleData.kbConfig());
		lib.data.deviceConfigs.add(LibUsbSampleData.sdReaderConfig());
		try (var usb = Usb.of()) {
			try (var deviceHandle = usb.open(LibUsbFinder.of(0, 0x8406))) {
				assertNotNull(deviceHandle);
			}
		}
	}

}
