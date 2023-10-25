package ceri.serial.libusb.test;

import static ceri.common.test.AssertUtil.assertPrivateConstructor;
import org.junit.Test;

public class LibUsbSampleDataTest {

	@Test
	public void testConstructorIsPrivate() {
		assertPrivateConstructor(LibUsbSampleData.class);
	}

}
