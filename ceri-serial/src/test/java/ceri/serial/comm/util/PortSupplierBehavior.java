package ceri.serial.comm.util;

import static ceri.common.test.Assert.assertEquals;
import static ceri.common.test.Assert.assertUnordered;
import java.io.IOException;
import java.nio.file.Path;
import org.junit.After;
import org.junit.Test;
import ceri.common.function.Closeables;
import ceri.common.test.Assert;
import ceri.common.test.FileTestHelper;

public class PortSupplierBehavior {
	private FileTestHelper helper;
	private PortSupplier.Locator locator;

	@After
	public void after() {
		Closeables.close(helper);
		helper = null;
		locator = null;
	}

	@Test
	public void testFixed() throws IOException {
		assertEquals(PortSupplier.fixed("test").get(), "test");
		assertEquals(PortSupplier.fixed(null), null);
	}

	@Test
	public void shouldLocateUsbPorts() throws IOException {
		var path = initLocator("tty.xxx", "tty.usb0", "tty.USB1");
		assertUnordered(locator.usbPorts(), path + "/tty.usb0", path + "/tty.USB1");
		assertEquals(locator.usbPort(1), path + "/tty.usb0"); // USB before usb
		Assert.thrown(() -> locator.usbPort(2));
	}

	@Test
	public void shouldFailWithNoPorts() throws IOException {
		initLocator();
		assertUnordered(locator.ports("glob:*"));
		Assert.thrown(() -> locator.port("glob:*", 0));
	}

	/**
	 * Create dummy ports, locator, and return root path.
	 */
	private Path initLocator(String... ports) throws IOException {
		var b = FileTestHelper.builder();
		for (var port : ports)
			b.file(port, "");
		helper = b.build();
		locator = PortSupplier.locator(helper.root);
		return helper.root;
	}
}
