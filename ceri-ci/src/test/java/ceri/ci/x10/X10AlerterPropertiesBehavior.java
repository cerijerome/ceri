package ceri.ci.x10;

import static ceri.common.test.TestUtil.assertCollection;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import java.util.Properties;
import org.junit.BeforeClass;
import org.junit.Test;
import ceri.x10.X10ControllerType;

public class X10AlerterPropertiesBehavior {
	private static Properties props = new Properties();
	
	@BeforeClass
	public static void init() {
		props.put("x.comm.port", "COM1");
		props.put("x.controller", "cm11a");
		props.put("x.address.A", "D9");
		props.put("x.address.B", "F1");
		props.put("comm.port", "123");
		props.put("controller", "?");
		props.put("address.x", "A16");
	}
	
	@Test
	public void shouldReadValuesWithPrefix() {
		X10AlerterProperties x10 = new X10AlerterProperties(props, "x");
		assertThat(x10.commPort(), is("COM1"));
		assertThat(x10.controllerType(), is(X10ControllerType.cm11a));
		assertThat(x10.address("A"), is("D9"));
		assertCollection(x10.names(), "A", "B");
	}

	@Test
	public void shouldReadValuesWithoutPrefix() {
		X10AlerterProperties x10 = new X10AlerterProperties(props);
		assertThat(x10.commPort(), is("123"));
		assertThat(x10.address("x"), is("A16"));
		assertCollection(x10.names(), "x");
	}

	@Test(expected = IllegalArgumentException.class)
	public void shouldFailForInvalidControllerType() {
		X10AlerterProperties x10 = new X10AlerterProperties(props);
		x10.controllerType();
	}
	
}
