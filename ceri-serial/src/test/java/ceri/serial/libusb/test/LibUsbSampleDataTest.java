package ceri.serial.libusb.test;

import org.junit.jupiter.api.Test;
import ceri.common.test.Assert;

public class LibUsbSampleDataTest {

	@Test
	public void testConstructorIsPrivate() {
		Assert.privateConstructor(LibUsbSampleData.class);
	}

}
