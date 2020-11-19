package ceri.serial.ftdi;

import static ceri.common.test.AssertUtil.*;
import static ceri.common.test.ErrorGen.*;
import static ceri.common.test.TestUtil.*;
import static ceri.serial.libusb.jna.LibUsb.libusb_speed.*;
import static ceri.serial.libusb.jna.LibUsbFinder.libusb_find_criteria_string;
import static org.hamcrest.CoreMatchers.*;
import static org.mockito.Mockito.*;
import java.io.IOException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import ceri.common.data.IntArray;
import ceri.common.data.IntProvider;
import ceri.common.test.CallSync;
import ceri.common.util.Enclosed;
import ceri.serial.ftdi.util.SelfHealingFtdiConfig;
import ceri.serial.ftdi.util.SelfHealingFtdiConnector;
import ceri.serial.libusb.jna.LibUsbException;
import ceri.serial.libusb.jna.LibUsbFinder;
import ceri.serial.libusb.jna.LibUsbTestUtil;
import ceri.serial.libusb.jna.TestLibUsbNative;
import ceri.serial.libusb.jna.LibUsb;
import ceri.serial.libusb.jna.LibUsb.libusb_speed;
import ceri.serial.libusb.jna.LibUsbExampleData;

public class FtdiBehavior {
	private Enclosed<TestLibUsbNative> enc;
	private TestLibUsbNative lib;
	
	@Before
	public void before() {
		enc = TestLibUsbNative.register();
		lib = enc.subject;
		LibUsbExampleData.populate(lib.data);
	}
	
	@After
	public void after() {
		enc.close();
	}
	
	@Test
	public void should() throws LibUsbException {
		try (Ftdi ftdi = Ftdi.of()) {
			var finder = libusb_find_criteria_string("0x403");
			ftdi.open(finder);
		}
	}

}
