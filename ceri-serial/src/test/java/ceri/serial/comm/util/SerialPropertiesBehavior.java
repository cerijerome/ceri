package ceri.serial.comm.util;

import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertNotNull;
import java.io.IOException;
import java.util.Properties;
import org.junit.After;
import org.junit.Test;
import ceri.common.property.TypedProperties;
import ceri.common.test.FileTestHelper;
import ceri.common.util.CloseableUtil;

public class SerialPropertiesBehavior {
	private FileTestHelper helper;

	@After
	public void after() {
		CloseableUtil.close(helper);
		helper = null;
	}

	@Test
	public void shouldReturnNullWithNoPortLocator() throws IOException {
		var p = new Properties();
		var port = new SerialProperties(TypedProperties.from(p), "device").portSupplier();
		assertEquals(port.get(), null);
	}

	@Test
	public void shouldCreateDefaultPathPatternPortLocator() {
		var p = new Properties();
		p.setProperty("device.port.locator.pattern", "regex:.*\\d+");
		var port = new SerialProperties(TypedProperties.from(p), "device").portSupplier();
		assertNotNull(port);
	}

	@Test
	public void shouldCreatePatternPortLocator() throws IOException {
		var path = initPorts("port0", "port1");
		var p = new Properties();
		p.setProperty("device.port.locator.path", path);
		p.setProperty("device.port.locator.pattern", "regex:port\\d+");
		var port = new SerialProperties(TypedProperties.from(p), "device").portSupplier();
		assertEquals(port.get(), path + "/port0");
	}

	@Test
	public void shouldCreatePatternPortIndexLocator() throws IOException {
		var path = initPorts("port0", "port1");
		var p = new Properties();
		p.setProperty("device.port.locator.path", path);
		p.setProperty("device.port.locator.pattern", "regex:port\\d+");
		p.setProperty("device.port.locator.index", "1");
		var port = new SerialProperties(TypedProperties.from(p), "device").portSupplier();
		assertEquals(port.get(), path + "/port1");
	}

	@Test
	public void shouldCreateUsbPortIndexLocator() throws IOException {
		var path = initPorts("tty.usb0", "tty.usb1");
		var p = new Properties();
		p.setProperty("device.port.locator.path", path);
		p.setProperty("device.port.locator.index", "1");
		var port = new SerialProperties(TypedProperties.from(p), "device").portSupplier();
		assertEquals(port.get(), path + "/tty.usb1");
	}

	@Test
	public void shouldCreateLocationIdPortLocator() {
		var p = new Properties();
		p.setProperty("device.port.locator.id", "0x1234");
		var port = new SerialProperties(TypedProperties.from(p), "device").portSupplier();
		assertNotNull(port);
	}

	/**
	 * Create dummy ports and return root path as a string.
	 */
	private String initPorts(String... ports) throws IOException {
		var b = FileTestHelper.builder();
		for (String port : ports)
			b.file(port, "");
		helper = b.build();
		return helper.root.toString();
	}
}
