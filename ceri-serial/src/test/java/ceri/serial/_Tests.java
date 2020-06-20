package ceri.serial;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import ceri.common.test.TestUtil;
import ceri.serial.clib.FileDescriptorBehavior;
import ceri.serial.clib.ModeBehavior;
import ceri.serial.clib.OpenFlagBehavior;
import ceri.serial.clib.SeekBehavior;
import ceri.serial.clib.jna.CLibBehavior;
import ceri.serial.clib.util.ResponseFdBehavior;
import ceri.serial.jna.JnaUtilTest;
import ceri.serial.jna.test.JnaTestUtilTest;
import ceri.serial.libusb.jna.LibUsbFinderTest;

/**
 * Generated test suite for ceri-serial
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
	// clib
	FileDescriptorBehavior.class, //
	ModeBehavior.class, //
	OpenFlagBehavior.class, //
	SeekBehavior.class, //
	// clib.jna
	CLibBehavior.class, //
	// clib.util
	ResponseFdBehavior.class, //
	// jna
	JnaUtilTest.class, //
	// jna.test
	JnaTestUtilTest.class, //
	// libusb.jna
	LibUsbFinderTest.class, //
})
public class _Tests {
	public static void main(String... args) {
		TestUtil.exec(_Tests.class);
	}
}
