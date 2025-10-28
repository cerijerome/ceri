package ceri.serial.comm.util;

import java.io.IOException;
import org.junit.Test;
import ceri.common.test.Assert;
import ceri.common.test.TestProcess;
import ceri.common.test.Testing;
import ceri.jna.util.JnaOs;
import ceri.process.ioreg.Ioreg;

public class MacUsbLocatorBehavior {
	private static final String IOREG_XML = Testing.resource("ioreg.xml");

	@Test
	public void shouldProvidePortSupplierForMacOnly() {
		JnaOs.linux.accept(_ -> {
			var locator = MacUsbLocator.of(); // no error on creation
			Assert.thrown(() -> locator.portSupplier(0x123));
		});
		JnaOs.mac.accept(_ -> {
			Assert.string(MacUsbLocator.of().portSupplier(0x123), "locationId:0x123");
		});
	}

	@Test
	public void shouldProvidePortSupplier() throws IOException {
		JnaOs.mac.accept(_ -> {
			var locator = MacUsbLocator.of(Ioreg.of(TestProcess.processor(IOREG_XML)));
			var supplier = locator.portSupplier(18087936);
			Assert.equal(supplier.get(), "/dev/tty.usbserial-1140");
		});
	}

	@Test
	public void shouldFindPorts() throws IOException {
		var locator = MacUsbLocator.of(Ioreg.of(TestProcess.processor(IOREG_XML)));
		Assert
			.map(locator.ports(), //
			17891328, "/dev/tty.usbserial-1110", //
			18087936, "/dev/tty.usbserial-1140");
	}

	@Test
	public void shouldFindPortByLocationId() throws IOException {
		var locator = MacUsbLocator.of(Ioreg.of(TestProcess.processor(IOREG_XML)));
		Assert.equal(locator.port(17891328), "/dev/tty.usbserial-1110");
		Assert.equal(locator.port(18087936), "/dev/tty.usbserial-1140");
		Assert.thrown(() -> locator.port(0)); // 0 is ignored
		Assert.thrown(() -> locator.port(12345678)); // empty IODialinDevice value
	}

	// @Test
	public void shouldListUsbDevices() throws IOException {
		System.out.println(MacUsbLocator.of().ports());
	}
}
