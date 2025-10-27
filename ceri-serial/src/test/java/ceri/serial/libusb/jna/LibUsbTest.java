package ceri.serial.libusb.jna;

import static ceri.common.test.TestUtil.exerciseEnum;
import static ceri.jna.test.JnaTestUtil.assertPointer;
import static ceri.jna.test.JnaTestUtil.mem;
import org.junit.After;
import org.junit.Test;
import com.sun.jna.Pointer;
import ceri.common.function.Enclosure;
import ceri.common.test.Assert;
import ceri.jna.type.ArrayPointer;
import ceri.jna.util.JnaUtil;
import ceri.jna.util.PointerUtil;
import ceri.serial.libusb.jna.LibUsb.libusb_config_descriptor;
import ceri.serial.libusb.jna.LibUsb.libusb_device;
import ceri.serial.libusb.jna.LibUsb.libusb_device_handle;
import ceri.serial.libusb.jna.LibUsb.libusb_endpoint_descriptor;
import ceri.serial.libusb.jna.LibUsb.libusb_endpoint_transfer_type;
import ceri.serial.libusb.jna.LibUsb.libusb_error;
import ceri.serial.libusb.jna.LibUsb.libusb_interface;
import ceri.serial.libusb.jna.LibUsb.libusb_interface_descriptor;
import ceri.serial.libusb.jna.LibUsb.libusb_log_cb_mode;
import ceri.serial.libusb.jna.LibUsb.libusb_poll_event;
import ceri.serial.libusb.jna.LibUsb.libusb_transfer_status;
import ceri.serial.libusb.test.LibUsbSampleData;
import ceri.serial.libusb.test.TestLibUsbNative;

public class LibUsbTest {
	private TestLibUsbNative lib;
	private Enclosure<TestLibUsbNative> enc;

	@After
	public void after() {
		lib = null;
		if (enc != null) enc.close();
		enc = null;
	}

	@Test
	public void testEnumStringRepresentation() {
		exerciseEnum(libusb_endpoint_transfer_type.class);
		exerciseEnum(libusb_log_cb_mode.class);
	}

	@Test
	public void testEmptyConfig() {
		Assert.array(new libusb_config_descriptor(null).interfaces());
		Assert.array(new libusb_interface().altsettings());
		Assert.array(new libusb_interface_descriptor().endpoints());
	}

	@Test
	public void testIsoPacketStatus() {
		var t = new LibUsb.libusb_iso_packet_descriptor(null);
		t.status = libusb_transfer_status.LIBUSB_TRANSFER_TIMED_OUT.value;
		Assert.equal(t.status(), libusb_transfer_status.LIBUSB_TRANSFER_TIMED_OUT);
	}

	@Test
	public void testControlSetup() {
		var transfer = new LibUsb.libusb_transfer(null);
		var m = mem(0, 0, 0, 0, 0, 0, 0, 0, 7, 8, 9);
		var setup = LibUsb.libusb_fill_control_setup(m.m, 0x80, 3, 4, 5, 3);
		LibUsb.libusb_fill_control_transfer(transfer, null, setup, null, null, 0);
		setup = LibUsb.libusb_control_transfer_get_setup(transfer);
		Assert.equal(setup.bmRequestType, (byte) 0x80);
		Assert.equal(setup.bRequest, (byte) 3);
		Assert.equal(setup.wValue, (short) 4);
		Assert.equal(setup.wIndex, (short) 5);
		Assert.equal(setup.wLength, (short) 3);
		Pointer p = LibUsb.libusb_control_transfer_get_data(transfer);
		assertPointer(p, 0, 7, 8, 9);
	}

	@Test
	public void testIsoPacketSimpleAccess() {
		var transfer = transfer(4);
		transfer.buffer = JnaUtil.mallocBytes(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12);
		LibUsb.libusb_set_iso_packet_lengths(transfer, 3);
		assertPointer(LibUsb.libusb_get_iso_packet_buffer_simple(transfer, 2), 0, 7, 8, 9);
		Assert.isNull(LibUsb.libusb_get_iso_packet_buffer_simple(transfer, Short.MAX_VALUE + 1));
		Assert.isNull(LibUsb.libusb_get_iso_packet_buffer_simple(transfer, 4));
	}

	@Test
	public void testIsoPacketAccess() {
		var transfer = transfer(4);
		transfer.buffer = JnaUtil.mallocBytes(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12);
		LibUsb.libusb_set_iso_packet_lengths(transfer, 3);
		assertPointer(LibUsb.libusb_get_iso_packet_buffer(transfer, 2), 0, 7, 8, 9);
		Assert.isNull(LibUsb.libusb_get_iso_packet_buffer(transfer, Short.MAX_VALUE + 1));
		Assert.isNull(LibUsb.libusb_get_iso_packet_buffer(transfer, 4));
	}

	@Test
	public void testPollfdEvents() {
		var pollfd = new LibUsb.libusb_pollfd(null);
		pollfd.events = 0x05;
		Assert.unordered(pollfd.events(), libusb_poll_event.POLLIN, libusb_poll_event.POLLOUT);
	}

	@Test
	public void testPollfdsTimeouts() throws LibUsbException {
		initLib();
		Assert.equal(LibUsb.libusb_pollfds_handle_timeouts(null), false);
		lib.generalSync.autoResponses(1);
		Assert.equal(LibUsb.libusb_pollfds_handle_timeouts(null), true);
	}

	@Test
	public void testErrorAndLog() throws LibUsbException {
		initLib();
		LibUsb.libusb_set_log_cb(null, (_, _, _) -> 0,
			LibUsb.libusb_log_cb_mode.LIBUSB_LOG_CB_GLOBAL);
		Assert.notNull(LibUsb.libusb_error_name(libusb_error.LIBUSB_ERROR_BUSY));
		LibUsb.libusb_strerror(libusb_error.LIBUSB_ERROR_BUSY); // may be null?
	}

	@Test
	public void testDeviceRefs() throws LibUsbException {
		LibUsb.libusb_unref_device(null);
		LibUsb.libusb_free_device_list(null);
		LibUsb.libusb_free_device_list(deviceList(null));
	}

	@Test
	public void testBosDescriptors() throws LibUsbException {
		initLib();
		lib.data.addConfig(LibUsbSampleData.internalHubConfig());
		var handle = LibUsb.libusb_open_device_with_vid_pid(null, 0xa5c, 0x4500);
		Assert.equal(LibUsb.libusb_get_bos_descriptor(handle), null);
		lib.generalSync.autoResponses(libusb_error.LIBUSB_ERROR_ACCESS.value,
			libusb_error.LIBUSB_ERROR_NOT_FOUND.value, libusb_error.LIBUSB_ERROR_BUSY.value);
		Assert.thrown(() -> LibUsb.libusb_get_ss_endpoint_companion_descriptor(null,
			new libusb_endpoint_descriptor()));
		Assert.equal(LibUsb.libusb_get_bos_descriptor(new libusb_device_handle()), null);
		Assert.thrown(() -> LibUsb.libusb_get_bos_descriptor(new libusb_device_handle()));
	}

	@Test
	public void testGetDevice() throws LibUsbException {
		Assert.equal(LibUsb.libusb_get_device(null), null);
	}

	@Test
	public void testReleaseInterface() throws LibUsbException {
		LibUsb.libusb_release_interface(null, 0);
	}

	@Test
	public void testStreams() throws LibUsbException {
		initLib();
		lib.data.addConfig(LibUsbSampleData.internalHubConfig());
		var handle = LibUsb.libusb_open_device_with_vid_pid(null, 0xa5c, 0x4500);
		Assert.equal(LibUsb.libusb_alloc_streams(handle, 3, 0x81), 3);
		var transfer = LibUsb.libusb_alloc_transfer(0);
		LibUsb.libusb_transfer_set_stream_id(transfer, 2);
		Assert.equal(LibUsb.libusb_transfer_get_stream_id(transfer), 2);
		LibUsb.libusb_free_streams(handle, 0x81);
		LibUsb.libusb_free_streams(handle);
		LibUsb.libusb_free_streams(null);
	}

	@Test
	public void testCancelTransfer() throws LibUsbException {
		initLib();
		var transfer = LibUsb.libusb_alloc_transfer(0);
		LibUsb.libusb_cancel_transfer(transfer);
		lib.generalSync.autoResponses(libusb_error.LIBUSB_ERROR_BUSY.value);
		Assert.thrown(() -> LibUsb.libusb_cancel_transfer(transfer));
	}

	@Test
	public void testBulkZeroTransfer() throws LibUsbException {
		initLib();
		lib.data.addConfig(LibUsbSampleData.sdReaderConfig());
		var handle = LibUsb.libusb_open_device_with_vid_pid(null, 0x5ac, 0x8406);
		Assert.equal(LibUsb.libusb_bulk_transfer(handle, 0x02, null, 0, 0), 0);
	}

	@Test
	public void testHotPlugDeregister() throws LibUsbException {
		LibUsb.libusb_hotplug_deregister_callback(null, null);
	}

	private static ArrayPointer<libusb_device> deviceList(Pointer p) {
		return ArrayPointer.byRef(p, PointerUtil.adapt(libusb_device::new), libusb_device[]::new);
	}

	private static LibUsb.libusb_transfer transfer(int packets) {
		var transfer = new LibUsb.libusb_transfer(null);
		transfer.iso_packet_desc = new LibUsb.libusb_iso_packet_descriptor[packets];
		transfer.num_iso_packets = transfer.iso_packet_desc.length;
		for (int i = 0; i < transfer.iso_packet_desc.length; i++)
			transfer.iso_packet_desc[i] = new LibUsb.libusb_iso_packet_descriptor(null);
		return transfer;
	}

	private void initLib() throws LibUsbException {
		enc = TestLibUsbNative.register();
		lib = enc.ref;
		LibUsb.libusb_init_default();
	}
}
