package ceri.ci.x10;

import static ceri.common.test.AssertUtil.assertCollection;
import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertTrue;
import java.util.Properties;
import org.junit.BeforeClass;
import org.junit.Test;
import ceri.common.property.BaseProperties;
import ceri.x10.util.X10ControllerType;

public class X10PropertiesBehavior {
	private static Properties props = new Properties();
	private static BaseProperties baseProps = new BaseProperties(props) {};

	@BeforeClass
	public static void init() {
		props.put("x.enabled", "true");
		props.put("x.comm.port", "COM1");
		props.put("x.controller", "cm11a");
		props.put("x.address.A", "D9");
		props.put("x.address.B", "F1");
		props.put("y.controller", "xxx");
	}

	@Test
	public void shouldReadValuesWithPrefix() {
		X10Properties x10 = new X10Properties(baseProps, "x");
		assertTrue(x10.enabled());
		assertEquals(x10.commPort(), "COM1");
		assertEquals(x10.controllerType(), X10ControllerType.cm11a);
		assertEquals(x10.address("A"), "D9");
		assertEquals(x10.address("B"), "F1");
		assertCollection(x10.names(), "A", "B");
	}

	@Test(expected = IllegalArgumentException.class)
	public void shouldFailForInvalidControllerType() {
		X10Properties x10 = new X10Properties(baseProps, "y");
		x10.controllerType();
	}

}
