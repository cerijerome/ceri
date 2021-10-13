package ceri.serial.ftdi.jna;

import static ceri.common.test.AssertUtil.*;
import static ceri.common.test.ErrorGen.*;
import static ceri.common.test.TestUtil.*;
import static org.hamcrest.CoreMatchers.*;
import static org.mockito.Mockito.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import ceri.common.test.CallSync;
import ceri.common.util.Enclosed;
import ceri.log.util.LogUtil;
import ceri.serial.ftdi.Ftdi;
import ceri.serial.ftdi.jna.LibFtdi.ftdi_context;
import ceri.serial.ftdi.jna.LibFtdi.ftdi_interface;
import ceri.serial.libusb.jna.LibUsbException;
import ceri.serial.libusb.jna.LibUsbFinder;
import ceri.serial.libusb.jna.LibUsbSampleData;
import ceri.serial.libusb.jna.TestLibUsbNative;

public class LibFtdiTest {
	private TestLibUsbNative lib;
	private Enclosed<RuntimeException, TestLibUsbNative> enc;
	private ftdi_context ftdi;

	@Before
	public void before() {
		enc = TestLibUsbNative.register();
		lib = enc.subject;
		lib.data.deviceConfigs.add(LibUsbSampleData.ftdiConfig());
	}

	@After
	public void after() {
		LibFtdi.ftdi_free(ftdi);
		ftdi = null;
		enc.close();
	}

	@Test
	public void test() throws LibUsbException {
		ftdi = openFtdi();
	}

	private static ftdi_context openFtdi() throws LibUsbException {
		var ftdi = LibFtdi.ftdi_new();
		LibFtdi.ftdi_set_interface(ftdi, ftdi_interface.INTERFACE_ANY);
		LibFtdi.ftdi_usb_open_find(ftdi, LibFtdiUtil.FINDER);
		ftdi.max_packet_size = 5;
		return ftdi;
	}

}
