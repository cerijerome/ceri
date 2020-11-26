package ceri.serial.ftdi;

import static ceri.common.test.AssertUtil.*;
import static ceri.common.test.ErrorGen.*;
import static ceri.common.test.TestUtil.*;
import static ceri.serial.libusb.jna.LibUsb.libusb_speed.*;
import static ceri.serial.libusb.jna.LibUsbFinder.libusb_find_criteria_string;
import static org.hamcrest.CoreMatchers.*;
import static org.mockito.Mockito.*;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import ceri.common.data.ByteArray;
import ceri.common.data.ByteProvider;
import ceri.common.data.ByteUtil;
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
import ceri.serial.libusb.jna.LibUsbTestData.DeviceHandle;
import ceri.serial.libusb.jna.LibUsbExampleData;

public class FtdiBehavior {
	private TestLibUsbNative lib;
	private Enclosed<TestLibUsbNative> enc;
	private Ftdi ftdi;

	@Before
	public void before() throws LibUsbException {
		enc = TestLibUsbNative.register();
		lib = enc.subject;
		LibUsbExampleData.populate(lib.data);
		ftdi = Ftdi.of();
	}

	@After
	public void after() {
		ftdi.close();
		enc.close();
	}

	@Test
	public void shouldOpenDevice() throws LibUsbException {
		var finder = libusb_find_criteria_string("0x403");
		ftdi.open(finder);
		//ftdi.bitbang(true);
		//ftdi.reset();
		assertIterable(lib.controlTransferOut.values(),
			List.of(0x40, 0, 0x0000, 1, ByteProvider.empty()), // ftdi_usb_reset
			List.of(0x40, 3, 0x4138, 0, ByteProvider.empty())); // ftdi_set_baudrate
		lib.controlTransferIn.assertNoCall();
	}

}
