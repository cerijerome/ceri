package ceri.serial;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import ceri.common.test.TestUtil;
import ceri.serial.jna.JnaUtilTest;
import ceri.serial.libusb.jna.LibUsbFinderTest;

/**
 * Generated test suite for ceri-serial
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
	// jna
	JnaUtilTest.class, //
	// libusb.jna
	LibUsbFinderTest.class, //
})
public class _Tests {
	public static void main(String... args) {
		TestUtil.exec(_Tests.class);
	}
}
