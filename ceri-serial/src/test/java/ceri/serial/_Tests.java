package ceri.serial;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import ceri.common.test.TestUtil;
import ceri.serial.clib.FileDescriptorBehavior;
import ceri.serial.clib.FileReaderBehavior;
import ceri.serial.clib.FileWriterBehavior;
import ceri.serial.clib.ModeBehavior;
import ceri.serial.clib.OpenFlagBehavior;
import ceri.serial.clib.SeekBehavior;
import ceri.serial.clib.jna.CLibBehavior;
import ceri.serial.jna.JnaUtilTest;
import ceri.serial.libusb.jna.LibUsbFinderTest;
import ceri.serial.mlx90640.EepromDataBehavior;
import ceri.serial.mlx90640.FrameDataBehavior;

/**
 * Generated test suite for ceri-serial
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
	// clib
	FileDescriptorBehavior.class, //
	FileReaderBehavior.class, //
	FileWriterBehavior.class, //
	ModeBehavior.class, //
	OpenFlagBehavior.class, //
	SeekBehavior.class, //
	// clib.jna
	CLibBehavior.class, //
	// jna
	JnaUtilTest.class, //
	// libusb.jna
	LibUsbFinderTest.class, //
	// mlx90640
	EepromDataBehavior.class, //
	FrameDataBehavior.class, //
})
public class _Tests {
	public static void main(String... args) {
		TestUtil.exec(_Tests.class);
	}
}
