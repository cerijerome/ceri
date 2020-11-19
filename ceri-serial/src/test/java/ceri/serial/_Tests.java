package ceri.serial;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import ceri.common.test.TestUtil;

/**
 * Generated test suite for ceri-serial
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
	// clib
	ceri.serial.clib.CFileDescriptorBehavior.class, //
	ceri.serial.clib.FileDescriptorBehavior.class, //
	ceri.serial.clib.ModeBehavior.class, //
	ceri.serial.clib.OpenFlagBehavior.class, //
	ceri.serial.clib.SeekBehavior.class, //
	// clib.jna
	ceri.serial.clib.jna.CLibBehavior.class, //
	// clib.util
	ceri.serial.clib.util.ResponseFdBehavior.class, //
	// ftdi
	ceri.serial.ftdi.FtdiBehavior.class, //
	// ftdi.util
	ceri.serial.ftdi.util.SelfHealingFtdiConnectorBehavior.class, //
	// javax.util
	ceri.serial.javax.util.CommPortSupplierBehavior.class, //
	ceri.serial.javax.util.ConnectorNotSetExceptionBehavior.class, //
	ceri.serial.javax.util.ReplaceableSerialConnectorBehavior.class, //
	ceri.serial.javax.util.SelfHealingSerialConfigBehavior.class, //
	ceri.serial.javax.util.SelfHealingSerialConnectorBehavior.class, //
	// jna
	ceri.serial.jna.JnaUtilTest.class, //
	// jna.test
	ceri.serial.jna.test.JnaTestUtilTest.class, //
	// libusb
	ceri.serial.libusb.UsbBehavior.class, //
	// libusb.jna
	ceri.serial.libusb.jna.LibUsbFinderTest.class, //
})
public class _Tests {
	public static void main(String... args) {
		TestUtil.exec(_Tests.class);
	}
}
