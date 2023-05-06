package ceri.serial.ftdi.jna;

import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertThrown;
import static ceri.serial.ftdi.jna.LibFtdi.FTDI_VENDOR_ID;
import static ceri.serial.libusb.jna.LibUsb.libusb_error.LIBUSB_ERROR_INTERRUPTED;
import static ceri.serial.libusb.jna.LibUsb.libusb_error.LIBUSB_ERROR_IO;
import static ceri.serial.libusb.jna.LibUsb.libusb_error.LIBUSB_ERROR_NO_MEM;
import static ceri.serial.libusb.jna.TestLibUsbNative.lastError;
import java.util.List;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import ceri.common.data.ByteProvider;
import ceri.common.util.Enclosed;
import ceri.jna.clib.jna.CTime.timeval;
import ceri.jna.util.GcMemory;
import ceri.jna.util.Struct;
import ceri.serial.ftdi.jna.LibFtdi.ftdi_context;
import ceri.serial.ftdi.jna.LibFtdi.ftdi_interface;
import ceri.serial.ftdi.jna.LibFtdi.ftdi_module_detach_mode;
import ceri.serial.libusb.jna.LibUsbException;
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
		lib.generalSync.autoResponses(LIBUSB_ERROR_NO_MEM.value);
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
		ftdi.module_detach_mode = ftdi_module_detach_mode.DONT_DETACH_SIO_MODULE;
		LibFtdi.ftdi_set_usb_dev(ftdi, null);
		LibFtdi.ftdi_usb_close(ftdi);
		assertThrown(() -> LibFtdi.ftdi_usb_open(ftdi, FTDI_VENDOR_ID, 1));
		LibFtdi.ftdi_usb_open(ftdi, FTDI_VENDOR_ID, 0);
		LibFtdi.ftdi_usb_open_bus_addr(ftdi, 0x14, 0x05);
		LibFtdi.ftdi_usb_open_string(ftdi, "d:20/5");
		LibFtdi.ftdi_usb_open_string(ftdi, "i:0x403:0x6001");
		LibFtdi.ftdi_usb_open_string(ftdi, "s:0x403:0x6001:A7047D8V");
		assertThrown(() -> LibFtdi.ftdi_usb_open_string(ftdi, ""));
		LibFtdi.ftdi_usb_close(ftdi);
	}

	@Test
	public void testGetStrings() throws LibUsbException {
		ftdi = LibFtdi.ftdi_new();
		var list = LibFtdi.ftdi_usb_find_all(ftdi, 0, 0);
		LibFtdi.ftdi_usb_get_strings(ftdi, list.get(0));
		LibFtdi.ftdi_list_free(list);
	}

	@Test
	public void testInitialization() throws LibUsbException {
		var config = lib.data.deviceConfigs.get(0);
		config.configDescriptors[0].interfaces()[0].altsettings()[0].bNumEndpoints = 0;
		Struct.write(config.configDescriptors[0].interfaces()[0].altsettings()[0], "bNumEndpoints");
		config.configuration = 0;
		ftdi = openFtdi();
		assertEquals(ftdi.max_packet_size, 64);
		config.configDescriptors[0].interfaces()[0].num_altsetting = 0;
		Struct.write(config.configDescriptors[0].interfaces()[0], "num_altsetting");
		ftdi = openFtdi();
		assertEquals(ftdi.max_packet_size, 64);
		config.configDescriptors[0].bNumInterfaces = 0;
		Struct.write(config.configDescriptors[0], "bNumInterfaces");
		ftdi = openFtdi();
		assertEquals(ftdi.max_packet_size, 64);
		config.desc.bNumConfigurations = 0;
		ftdi = openFtdi();
		assertEquals(ftdi.max_packet_size, 64);
	}

	@Test
	public void testFailedAsyncTransfers() throws LibUsbException {
		ftdi = openFtdi();
		var m = GcMemory.malloc(5);
		lib.submitTransfer.error.set(lastError(LIBUSB_ERROR_NO_MEM));
		assertThrown(() -> LibFtdi.ftdi_write_data_submit(ftdi, m.m, 5));
		assertThrown(() -> LibFtdi.ftdi_read_data_submit(ftdi, m.m, 5));
		lib.submitTransfer.error.clear();
		var tc = LibFtdi.ftdi_write_data_submit(ftdi, m.m, 5);
		lib.handleTransferEvent.error.set(lastError(LIBUSB_ERROR_INTERRUPTED),
			lastError(LIBUSB_ERROR_IO));
		assertEquals(LibFtdi.ftdi_transfer_data_done(null), 0);
		assertThrown(() -> LibFtdi.ftdi_transfer_data_done(tc));
	}

	@Test
	public void testAsyncCancel() throws LibUsbException {
		ftdi = openFtdi();
		var m = GcMemory.malloc(5);
		var tc = LibFtdi.ftdi_write_data_submit(ftdi, m.m, 5);
		LibFtdi.ftdi_transfer_data_cancel(null, new timeval());
		LibFtdi.ftdi_transfer_data_cancel(tc, new timeval());
		LibFtdi.ftdi_transfer_data_cancel(tc, new timeval());
		tc.completed.setValue(0);
		LibFtdi.ftdi_transfer_data_cancel(tc, new timeval());
	}

	@Test
	public void testSetChar() throws LibUsbException {
		ftdi = openFtdi();
		LibFtdi.ftdi_set_event_char(ftdi, 'x', true);
		LibFtdi.ftdi_set_event_char(ftdi, 'x', false);
		LibFtdi.ftdi_set_error_char(ftdi, 'x', true);
		LibFtdi.ftdi_set_error_char(ftdi, 'x', false);
		lib.syncTransferOut.assertValues( //
			List.of(0x40, 0x06, 0x0178, 1, ByteProvider.empty()),
			List.of(0x40, 0x06, 0x0078, 1, ByteProvider.empty()),
			List.of(0x40, 0x07, 0x0178, 1, ByteProvider.empty()),
			List.of(0x40, 0x07, 0x0078, 1, ByteProvider.empty()));

	}

	private ftdi_context openFtdi() throws LibUsbException {
		var ftdi = LibFtdi.ftdi_new();
		LibFtdi.ftdi_usb_open_find(ftdi, LibFtdiUtil.FINDER);
		lib.syncTransferOut.reset(); // clear original open()
		return ftdi;
	}

}
