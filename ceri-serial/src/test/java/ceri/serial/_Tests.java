package ceri.serial;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import ceri.common.test.TestUtil;
import ceri.serial.ftdi.jna.LibFtdiFinderTest;
import ceri.serial.jna.JnaUtilTest;
import ceri.serial.jna.RefStoreBehavior;

/**
 * Generated test suite for ceri-serial
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
	// ftdi.jna
	LibFtdiFinderTest.class, //
	// jna
	JnaUtilTest.class, //
	RefStoreBehavior.class, //
})
public class _Tests {
	public static void main(String... args) {
		TestUtil.exec(_Tests.class);
	}
}
