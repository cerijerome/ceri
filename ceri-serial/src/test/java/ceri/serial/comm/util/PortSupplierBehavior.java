package ceri.serial.comm.util;

import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertThrown;
import static ceri.common.test.AssertUtil.assertUnordered;
import java.io.IOException;
import java.nio.file.Path;
import org.junit.After;
import org.junit.Test;
import ceri.common.test.FileTestHelper;
import ceri.common.util.CloseableUtil;

public class PortSupplierBehavior {
	private FileTestHelper helper;
	private PortSupplier.Locator locator;

	@After
	public void after() {
		CloseableUtil.close(helper);
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
		assertThrown(() -> locator.usbPort(2));
	}

	@Test
	public void shouldFailWithNoPorts() throws IOException {
		initLocator();
		assertUnordered(locator.ports("glob:*"));
		assertThrown(() -> locator.port("glob:*", 0));
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
