package ceri.serial.libusb.test;

import org.junit.Test;
import ceri.common.test.Assert;

public class LibUsbSampleDataTest {

	@Test
	public void testConstructorIsPrivate() {
		Assert.privateConstructor(LibUsbSampleData.class);
	}

}
