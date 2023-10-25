package ceri.serial.comm.util;

import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertMap;
import static ceri.common.test.AssertUtil.assertString;
import static ceri.common.test.AssertUtil.assertThrown;
import java.io.IOException;
import org.junit.Test;
import ceri.common.test.TestProcess;
import ceri.common.test.TestUtil;
import ceri.jna.test.JnaTestUtil;
import ceri.process.ioreg.Ioreg;

public class MacUsbLocatorBehavior {
	private static final String IOREG_XML = TestUtil.resource("ioreg.xml");

	@Test
	public void shouldProvidePortSupplierForMacOnly() {
		JnaTestUtil.testAsOs(JnaTestUtil.LINUX_OS, () -> {
			var locator = MacUsbLocator.of();
			assertThrown(() -> locator.portSupplier(0x123));
		});
		JnaTestUtil.testAsOs(JnaTestUtil.MAC_OS, () -> {
			assertString(MacUsbLocator.of().portSupplier(0x123), "locationId:0x123");
		});
	}

	@Test
	public void shouldProvidePortSupplier() throws IOException {
		JnaTestUtil.testAsOs(JnaTestUtil.MAC_OS, () -> {
			var locator = MacUsbLocator.of(Ioreg.of(TestProcess.processor(IOREG_XML)));
			var supplier = locator.portSupplier(18087936);
			assertEquals(supplier.get(), "/dev/tty.usbserial-1140");
		});
	}

	@Test
	public void shouldFindPorts() throws IOException {
		var locator = MacUsbLocator.of(Ioreg.of(TestProcess.processor(IOREG_XML)));
		assertMap(locator.ports(), //
			17891328, "/dev/tty.usbserial-1110", //
			18087936, "/dev/tty.usbserial-1140");
	}

	@Test
	public void shouldFindPortByLocationId() throws IOException {
		var locator = MacUsbLocator.of(Ioreg.of(TestProcess.processor(IOREG_XML)));
		assertEquals(locator.port(17891328), "/dev/tty.usbserial-1110");
		assertEquals(locator.port(18087936), "/dev/tty.usbserial-1140");
		assertThrown(() -> locator.port(0)); // 0 is ignored
		assertThrown(() -> locator.port(12345678)); // empty IODialinDevice value
	}

	// @Test
	public void shouldListUsbDevices() throws IOException {
		System.out.println(MacUsbLocator.of().ports());
	}

}
