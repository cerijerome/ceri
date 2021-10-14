package ceri.serial.ftdi.jna;

import static ceri.common.test.AssertUtil.*;
import static ceri.common.test.ErrorGen.*;
import static ceri.common.test.TestUtil.*;
import static ceri.serial.ftdi.jna.LibFtdi.FTDI_VENDOR_ID;
import static ceri.serial.libusb.jna.LibUsb.libusb_error.LIBUSB_ERROR_NO_MEM;
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
import ceri.serial.libusb.jna.LibUsb;
import ceri.serial.libusb.jna.LibUsb.libusb_error;
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
	public void testInitFailure() {
		lib.init.autoResponses(LIBUSB_ERROR_NO_MEM.value);
		assertThrown(() -> LibFtdi.ftdi_new());
	}

	@Test
	public void testSetInterface() throws LibUsbException {
		var ftdi = openFtdi();
		LibFtdi.ftdi_set_interface(ftdi, ftdi_interface.INTERFACE_A);
		assertThrown(() -> LibFtdi.ftdi_set_interface(ftdi, ftdi_interface.INTERFACE_B));
	}

	@Test
	public void testOpenDevice() throws LibUsbException {
		LibFtdi.ftdi_set_usb_dev(ftdi, null);
		ftdi = LibFtdi.ftdi_new();
		LibFtdi.ftdi_set_usb_dev(ftdi, null);
		LibFtdi.ftdi_usb_close(ftdi);
		var list = LibFtdi.ftdi_usb_find_all(ftdi, 0, 0);
		LibFtdi.ftdi_usb_open_dev(ftdi, list.get(0));
		LibFtdi.ftdi_list_free(list);
		assertThrown(() -> LibFtdi.ftdi_usb_open(ftdi, FTDI_VENDOR_ID, 1));
		LibFtdi.ftdi_usb_open(ftdi, FTDI_VENDOR_ID, 0);
		LibFtdi.ftdi_usb_open_bus_addr(ftdi, 0x14, 0x05);
		LibFtdi.ftdi_usb_open_string(ftdi, "d:20/5");
		LibFtdi.ftdi_usb_open_string(ftdi, "i:0x403:0x6001");
		LibFtdi.ftdi_usb_open_string(ftdi, "s:0x403:0x6001:A7047D8V");
		assertThrown(() -> LibFtdi.ftdi_usb_open_string(ftdi, ""));
		LibFtdi.ftdi_usb_close(ftdi);
	}

	private static ftdi_context openFtdi() throws LibUsbException {
		var ftdi = LibFtdi.ftdi_new();
		LibFtdi.ftdi_usb_open_find(ftdi, LibFtdiUtil.FINDER);
		ftdi.max_packet_size = 5;
		return ftdi;
	}

}
