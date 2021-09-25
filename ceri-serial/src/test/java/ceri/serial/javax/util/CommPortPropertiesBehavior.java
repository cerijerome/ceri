package ceri.serial.javax.util;

import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertNotEquals;
import java.io.IOException;
import org.junit.Test;
import ceri.common.test.TestUtil;

public class CommPortPropertiesBehavior {

	@Test
	public void shouldProvideCommPort() throws IOException {
		assertEquals(properties("serial0.usb").supplier().get(), "com0");
	}

	@Test
	public void shouldProvideLocator() {
		assertNotEquals(properties("serial1.usb").supplier(), null);
	}

	@Test
	public void shouldProvideNullForMissingProperties() {
		assertEquals(properties("serialN.usb").supplier(), null);
	}

	private static CommPortProperties properties(String group) {
		return new CommPortProperties(TestUtil.baseProperties("serial"), group);
	}
}
