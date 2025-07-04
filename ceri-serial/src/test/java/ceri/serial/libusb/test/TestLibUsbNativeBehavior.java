package ceri.serial.libusb.test;

import static ceri.common.test.AssertUtil.assertArray;
import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertNotNull;
import static ceri.common.test.AssertUtil.assertNull;
import static ceri.common.test.AssertUtil.assertThrown;
import static ceri.jna.test.JnaTestUtil.assertLastError;
import static ceri.serial.libusb.jna.LibUsb.libusb_error.LIBUSB_ERROR_BUSY;
import static ceri.serial.libusb.jna.LibUsb.libusb_error.LIBUSB_ERROR_NOT_FOUND;
import static ceri.serial.libusb.jna.LibUsb.libusb_error.LIBUSB_ERROR_NOT_SUPPORTED;
import static ceri.serial.libusb.jna.LibUsb.libusb_error.LIBUSB_ERROR_NO_DEVICE;
import static ceri.serial.libusb.jna.LibUsb.libusb_error.LIBUSB_ERROR_OVERFLOW;
import java.nio.ByteBuffer;
import org.junit.After;
import org.junit.Test;
import com.sun.jna.Memory;
import com.sun.jna.ptr.IntByReference;
import ceri.common.data.ByteProvider;
import ceri.common.util.Enclosed;
import ceri.jna.clib.jna.CTime.timeval;
import ceri.jna.type.Struct;
import ceri.log.util.LogUtil;
import ceri.serial.libusb.jna.LibUsb;
import ceri.serial.libusb.jna.LibUsb.libusb_descriptor_type;
import ceri.serial.libusb.jna.LibUsb.libusb_error;
import ceri.serial.libusb.jna.LibUsb.libusb_transfer_status;
import ceri.serial.libusb.jna.LibUsbException;
import ceri.serial.libusb.jna.LibUsbFinder;
import ceri.serial.libusb.test.TestLibUsbNative.PollFdEvent;

public class TestLibUsbNativeBehavior {
	private TestLibUsbNative lib;
	private Enclosed<RuntimeException, TestLibUsbNative> enc;
	private timeval t0 = new timeval();

	@After
	public void after() {
		LogUtil.close(enc);
		lib = null;
		enc = null;
	}

	@Test
	public void shouldNotClearSampleDataOnReset() throws LibUsbException {
		initLib();
		assertThrown(() -> LibUsbFinder.FIRST.findAndOpen(null));
		lib.data.addConfig(LibUsbSampleData.audioConfig());
		LibUsbFinder.FIRST.findAndOpen(null); // no exception
		lib.reset();
		assertThrown(() -> LibUsbFinder.FIRST.findAndOpen(null));
	}

	@Test
	public void shouldFailToSetBadOption() throws LibUsbException {
		initLib();
		assertError(lib.libusb_set_option(null, 777), LIBUSB_ERROR_NOT_SUPPORTED);
	}

	@Test
	public void shouldProvideDefaultErrorName() {
		lib = TestLibUsbNative.of();
		assertEquals(lib.libusb_error_name(0xfedcba98), "UNKNOWN");
	}

	@Test
	public void shouldFailInvalidDeviceConfigurationRequests() throws LibUsbException {
		initLib();
		lib.data.addConfig(LibUsbSampleData.audioConfig());
		var devs = LibUsb.libusb_get_device_list(null);
		var dev = devs.get(0);
		assertError(lib.libusb_get_config_descriptor(dev, (byte) 5, null), LIBUSB_ERROR_NOT_FOUND);
		assertError(lib.libusb_get_config_descriptor_by_value(dev, (byte) 5, null),
			LIBUSB_ERROR_NOT_FOUND);
		assertError(lib.libusb_get_port_numbers(dev, null, 0), LIBUSB_ERROR_OVERFLOW);
		assertNull(lib.libusb_get_parent(dev));
		assertError(lib.libusb_get_max_packet_size(dev, (byte) 0), LIBUSB_ERROR_NOT_FOUND);
		assertError(lib.libusb_get_max_iso_packet_size(dev, (byte) 0), LIBUSB_ERROR_NOT_FOUND);
		LibUsb.libusb_free_device_list(devs, 0);
	}

	@Test
	public void shouldFailForUnsupportedRequests() {
		lib = TestLibUsbNative.of();
		assertThrown(() -> lib.libusb_wrap_sys_device(null, 0, null));
		assertThrown(() -> lib.libusb_dev_mem_alloc(null, 0));
		assertThrown(() -> lib.libusb_dev_mem_free(null, null, 0));
	}

	@Test
	public void shouldFailToOpenInvalidVidPid() throws LibUsbException {
		initLib();
		lib.data.addConfig(LibUsbSampleData.audioConfig());
		assertNotNull(lib.libusb_open_device_with_vid_pid(null, (short) 0xd8c, (short) 0x14));
		assertNull(lib.libusb_open_device_with_vid_pid(null, (short) 0xd8d, (short) 0x14));
		assertNull(lib.libusb_open_device_with_vid_pid(null, (short) 0xd8c, (short) 0x15));
	}

	@Test
	public void shouldFailInvalidDeviceHandleRequests() throws LibUsbException {
		initLib();
		lib.data.addConfig(LibUsbSampleData.audioConfig());
		var h = LibUsbFinder.FIRST.findAndOpen(null);
		assertError(lib.libusb_release_interface(h, 1), LIBUSB_ERROR_NOT_FOUND);
		assertError(lib.libusb_set_interface_alt_setting(h, 1, 0), LIBUSB_ERROR_NOT_FOUND);
		LibUsb.libusb_alloc_streams(h, 3, 2);
		assertError(lib.libusb_alloc_streams(h, 2, null, 0), LIBUSB_ERROR_NO_DEVICE);
	}

	@Test
	public void shouldFailToAllocateTransfer() throws LibUsbException {
		initLib();
		lib.generalSync.autoResponses(1);
		assertNull(lib.libusb_alloc_transfer(1));
	}

	@Test
	public void shouldFailToSubmitTransferTwice() throws LibUsbException {
		initLib();
		lib.data.addConfig(LibUsbSampleData.audioConfig());
		var h = LibUsbFinder.FIRST.findAndOpen(null);
		var xfer = LibUsb.libusb_alloc_transfer(1);
		xfer.dev_handle = h;
		Struct.write(xfer);
		assertEquals(lib.libusb_submit_transfer(xfer.getPointer()), 0);
		assertError(lib.libusb_submit_transfer(xfer.getPointer()), LIBUSB_ERROR_BUSY);
	}

	@Test
	public void shouldFailToCancelTransferTwice() throws LibUsbException {
		initLib();
		lib.data.addConfig(LibUsbSampleData.audioConfig());
		var h = LibUsbFinder.FIRST.findAndOpen(null);
		var xfer = LibUsb.libusb_alloc_transfer(1);
		xfer.dev_handle = h;
		Struct.write(xfer);
		assertEquals(lib.libusb_submit_transfer(xfer.getPointer()), 0);
		assertEquals(lib.libusb_cancel_transfer(xfer.getPointer()), 0);
		assertError(lib.libusb_cancel_transfer(xfer.getPointer()), LIBUSB_ERROR_NOT_FOUND);
	}

	@Test
	public void shouldFailToAllocateStreamTwice() throws LibUsbException {
		initLib();
		lib.data.addConfig(LibUsbSampleData.audioConfig());
		var xfer = LibUsb.libusb_alloc_transfer(1);
		LibUsb.libusb_transfer_set_stream_id(xfer, 3);
		assertLastError(() -> lib.libusb_transfer_set_stream_id(xfer.getPointer(), 3));
	}

	@Test
	public void shouldReturnFromEventHandlerIfComplete() throws LibUsbException {
		initLib();
		var completed = new IntByReference(1);
		assertEquals(lib.libusb_handle_events_timeout_completed(null, null, completed), 0);
	}

	@Test
	public void shouldGetHotPlugUserData() throws LibUsbException {
		try (var m = new Memory(3)) {
			initLib();
			int h = LibUsb.libusb_hotplug_register_callback(null, 3, 0, 0, 0, 0, null, m).value;
			assertEquals(lib.libusb_hotplug_get_user_data(null, h), m);
			assertLastError(() -> lib.libusb_hotplug_get_user_data(LibUsb.libusb_init(), h));
		}
	}

	@Test
	public void shouldHandleCorruptBosDescriptors() throws LibUsbException {
		initLib();
		lib.data.addConfig(LibUsbSampleData.externalUsb3HubConfig());
		var h = LibUsbFinder.FIRST.findAndOpen(null);
		assertError(lib.libusb_get_container_id_descriptor(null, null, null),
			LIBUSB_ERROR_NOT_FOUND);
		var bdc = LibUsb.libusb_get_bos_descriptor(h).dev_capability[0];
		LibUsb.libusb_get_usb_2_0_extension_descriptor(null, bdc);
		assertThrown(() -> LibUsb.libusb_get_container_id_descriptor(null, bdc));
		// make data corrupt
		bdc.bDescriptorType = (byte) libusb_descriptor_type.LIBUSB_DT_ENDPOINT.value;
		Struct.write(bdc);
		assertThrown(() -> LibUsb.libusb_get_usb_2_0_extension_descriptor(null, bdc));
	}

	@Test
	public void shouldHandleNullControlTransfers() throws LibUsbException {
		initLib();
		lib.data.addConfig(LibUsbSampleData.externalUsb3HubConfig());
		var h = LibUsbFinder.FIRST.findAndOpen(null);
		lib.transferIn.autoResponses((ByteProvider) null);
		var buf = (ByteBuffer) null;
		assertArray(LibUsb.libusb_control_transfer(h, 0x80, 0, 0, 0, 0, 0)); // in
		assertEquals(LibUsb.libusb_control_transfer(h, 0, 0, 0, 0, buf, 0, 0), 0); // out
	}

	@Test
	public void shouldHandleNullSyncTransfers() throws LibUsbException {
		initLib();
		lib.data.addConfig(LibUsbSampleData.externalUsb3HubConfig());
		var h = LibUsbFinder.FIRST.findAndOpen(null);
		lib.transferIn.autoResponses((ByteProvider) null, ByteProvider.empty());
		assertArray(LibUsb.libusb_bulk_transfer(h, 0x80, 0, 0)); // in
		var buf = ByteBuffer.allocate(0);
		assertEquals(lib.libusb_bulk_transfer(h, (byte) 0x80, buf, 0, null, 0), 0); // in
		assertEquals(lib.libusb_bulk_transfer(h, (byte) 0, buf, 0, null, 0), 0); // out
	}

	@Test
	public void shouldHandleEmptyPollFdEvents() throws LibUsbException {
		initLib();
		lib.handlePollFdEvent.autoResponses(new PollFdEvent(0, 0, false));
		assertEquals(LibUsb.libusb_handle_events_timeout_completed(null, t0, null), 0);
	}

	@Test
	public void shouldHandleEmptyHotPlugEvents() throws LibUsbException {
		initLib();
		var ctx = LibUsb.libusb_init();
		LibUsb.libusb_hotplug_register_callback(null, 0, 0, 0, 0, 0, null, null);
		LibUsb.libusb_hotplug_register_callback(ctx, 0, 0, 0, 0, 0, null, null);
		assertEquals(LibUsb.libusb_handle_events_timeout_completed(null, t0, null), 0);
	}

	@Test
	public void shouldHandleEmptyTransferEvents() throws LibUsbException {
		initLib();
		lib.data.addConfig(LibUsbSampleData.kbConfig());
		var ctx = LibUsb.libusb_init();
		var h = LibUsbFinder.FIRST.findAndOpen(ctx);
		LibUsb.libusb_alloc_transfer(0);
		var x1 = LibUsb.libusb_alloc_transfer(1);
		LibUsb.libusb_fill_bulk_transfer(x1, h, 0, null, 0, null, null, 0);
		LibUsb.libusb_submit_transfer(Struct.write(x1));
		assertEquals(LibUsb.libusb_handle_events_timeout_completed(null, t0, null), 0);
		lib.handleTransferEvent.autoResponses((libusb_transfer_status) null);
		assertEquals(LibUsb.libusb_handle_events_timeout_completed(ctx, t0, null), 0);
	}

	private void initLib() throws LibUsbException {
		enc = TestLibUsbNative.register();
		lib = enc.ref;
		LibUsb.libusb_init_default();
	}

	private static void assertError(int result, libusb_error error) {
		assertEquals(result, error.value);
	}

}
