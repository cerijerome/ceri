package ceri.serial.javax.util;

import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertMap;
import static ceri.common.test.AssertUtil.assertThrown;
import java.io.IOException;
import org.junit.Test;
import ceri.common.test.TestProcess;
import ceri.common.test.TestUtil;
import ceri.common.util.OsUtil;
import ceri.process.ioreg.Ioreg;

public class MacUsbLocatorBehavior {
	private static final String IOREG_XML = TestUtil.resource("ioreg.xml");

	@Test
	public void shouldCreateDefault() {
		MacUsbLocator.of();
	}

	@Test
	public void shouldSupplyCommPort() throws IOException {
		if (!OsUtil.IS_MAC) return;
		var locator = MacUsbLocator.of(Ioreg.of(TestProcess.processor(IOREG_XML)));
		assertEquals(locator.deviceByLocationId(0x14200000).get(), "/dev/tty.usbserial-00000000");
		assertThrown(locator.deviceByLocationId(0x14100000)::get);
	}

	@Test
	public void shouldProvideMapOfLocationIds() throws IOException {
		var locator = MacUsbLocator.of(Ioreg.of(TestProcess.processor(IOREG_XML)));
		assertMap(locator.devices(), 0x14110000, "/dev/tty.usbserial-A7047D8V", 0x14140000,
			"/dev/tty.usbserial-15", 0x14200000, "/dev/tty.usbserial-00000000");
	}

	@Test
	public void shouldFindDeviceFromLocationId() throws IOException {
		var locator = MacUsbLocator.of(Ioreg.of(TestProcess.processor(IOREG_XML)));
		assertEquals(locator.device(0x14140000), "/dev/tty.usbserial-15");
		assertThrown(() -> locator.device(0x14130000));
	}

	@Test
	public void shouldIgnoreEmptyDevices() throws IOException {
		String badIoregXml = IOREG_XML.replace("/dev/tty.usbserial-A7047D8V", "");
		badIoregXml = badIoregXml.replace("337641472", ""); // 0x14200000
		var locator = MacUsbLocator.of(Ioreg.of(TestProcess.processor(badIoregXml)));
		assertMap(locator.devices(), 0x14140000, "/dev/tty.usbserial-15");
		assertEquals(locator.device(0x14140000), "/dev/tty.usbserial-15");
		assertThrown(() -> locator.device(0x14110000));
		assertThrown(() -> locator.device(0x14200000));
	}

}
