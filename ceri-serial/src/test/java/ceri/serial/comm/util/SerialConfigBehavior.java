package ceri.serial.comm.util;

import static ceri.common.test.AssertUtil.assertAllNotEqual;
import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertNotNull;
import java.io.IOException;
import java.util.Properties;
import org.junit.After;
import org.junit.Test;
import ceri.common.property.TypedProperties;
import ceri.common.test.FileTestHelper;
import ceri.common.test.TestUtil;
import ceri.common.util.CloseableUtil;
import ceri.serial.comm.DataBits;
import ceri.serial.comm.FlowControl;
import ceri.serial.comm.Parity;
import ceri.serial.comm.SerialParams;
import ceri.serial.comm.StopBits;

public class SerialConfigBehavior {
	private FileTestHelper helper;

	@After
	public void after() {
		CloseableUtil.close(helper);
		helper = null;
	}

	@Test
	public void shouldNotBreachEqualsContract() {
		var params = SerialParams.of(1200, DataBits._7, StopBits._1, Parity.odd);
		var t = SerialConfig.builder().params(params).flowControl(FlowControl.rtsCtsIn)
			.inBufferSize(111).outBufferSize(222).build();
		var eq0 = SerialConfig.builder().params(params).flowControl(FlowControl.rtsCtsIn)
			.inBufferSize(111).outBufferSize(222).build();
		var ne0 = SerialConfig.DEFAULT;
		var ne1 = SerialConfig.of(1200);
		var ne2 = SerialConfig.of(SerialParams.of(1200, DataBits._7, StopBits._1, Parity.odd));
		var ne3 = SerialConfig.builder().params(params).flowControl(FlowControl.NONE)
			.inBufferSize(111).outBufferSize(222).build();
		var ne4 = SerialConfig.builder().params(params).flowControl(FlowControl.rtsCtsIn)
			.inBufferSize(112).outBufferSize(222).build();
		var ne5 = SerialConfig.builder().params(params).flowControl(FlowControl.rtsCtsIn)
			.inBufferSize(111).outBufferSize(223).build();
		TestUtil.exerciseEquals(t, eq0);
		assertAllNotEqual(t, ne0, ne1, ne2, ne3, ne4, ne5);
	}

	@Test
	public void shouldReplaceSerialParams() {
		assertEquals(SerialConfig.DEFAULT.replace(null).params, SerialParams.DEFAULT);
		assertEquals(SerialConfig.DEFAULT.replace(SerialParams.DEFAULT).params,
			SerialParams.DEFAULT);
		assertEquals(SerialConfig.DEFAULT.replace(SerialParams.NULL).params, SerialParams.NULL);
	}

	@Test
	public void shouldReturnNullWithNoPortLocator() throws IOException {
		var p = new Properties();
		var port = new SerialConfig.Properties(TypedProperties.from(p), "device").portSupplier();
		assertEquals(port.get(), null);
	}

	@Test
	public void shouldCreateDefaultPathPatternPortLocator() {
		var p = new Properties();
		p.setProperty("device.port.locator.pattern", "regex:.*\\d+");
		var port = new SerialConfig.Properties(TypedProperties.from(p), "device").portSupplier();
		assertNotNull(port);
	}

	@Test
	public void shouldCreatePatternPortLocator() throws IOException {
		var path = initPorts("port0", "port1");
		var p = new Properties();
		p.setProperty("device.port.locator.path", path);
		p.setProperty("device.port.locator.pattern", "regex:port\\d+");
		var port = new SerialConfig.Properties(TypedProperties.from(p), "device").portSupplier();
		assertEquals(port.get(), path + "/port0");
	}

	@Test
	public void shouldCreatePatternPortIndexLocator() throws IOException {
		var path = initPorts("port0", "port1");
		var p = new Properties();
		p.setProperty("device.port.locator.path", path);
		p.setProperty("device.port.locator.pattern", "regex:port\\d+");
		p.setProperty("device.port.locator.index", "1");
		var port = new SerialConfig.Properties(TypedProperties.from(p), "device").portSupplier();
		assertEquals(port.get(), path + "/port1");
	}

	@Test
	public void shouldCreateUsbPortIndexLocator() throws IOException {
		var path = initPorts("tty.usb0", "tty.usb1");
		var p = new Properties();
		p.setProperty("device.port.locator.path", path);
		p.setProperty("device.port.locator.index", "1");
		var port = new SerialConfig.Properties(TypedProperties.from(p), "device").portSupplier();
		assertEquals(port.get(), path + "/tty.usb1");
	}

	@Test
	public void shouldCreateLocationIdPortLocator() {
		var p = new Properties();
		p.setProperty("device.port.locator.id", "0x1234");
		var port = new SerialConfig.Properties(TypedProperties.from(p), "device").portSupplier();
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
