package ceri.serial.libusb.jna;

import static ceri.common.math.MathUtil.ubyte;
import static ceri.common.math.MathUtil.ushort;
import static ceri.common.test.AssertUtil.assertArray;
import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.serial.libusb.jna.LibUsb.libusb_bos_type.LIBUSB_BT_CONTAINER_ID;
import static ceri.serial.libusb.jna.LibUsb.libusb_bos_type.LIBUSB_BT_SS_USB_DEVICE_CAPABILITY;
import static ceri.serial.libusb.jna.LibUsb.libusb_bos_type.LIBUSB_BT_USB_2_0_EXTENSION;
import static ceri.serial.libusb.jna.LibUsb.libusb_descriptor_type.LIBUSB_DT_DEVICE_CAPABILITY;
import static ceri.serial.libusb.jna.LibUsb.libusb_error.LIBUSB_ERROR_ACCESS;
import static ceri.serial.libusb.jna.LibUsb.libusb_error.LIBUSB_ERROR_BUSY;
import static ceri.serial.libusb.jna.LibUsb.libusb_error.LIBUSB_ERROR_IO;
import static ceri.serial.libusb.jna.LibUsb.libusb_error.LIBUSB_ERROR_NOT_FOUND;
import static ceri.serial.libusb.jna.LibUsb.libusb_error.LIBUSB_ERROR_NOT_SUPPORTED;
import static ceri.serial.libusb.jna.LibUsb.libusb_error.LIBUSB_ERROR_NO_DEVICE;
import static ceri.serial.libusb.jna.LibUsb.libusb_error.LIBUSB_ERROR_OVERFLOW;
import static ceri.serial.libusb.jna.LibUsb.libusb_error.LIBUSB_ERROR_PIPE;
import static ceri.serial.libusb.jna.LibUsb.libusb_error.LIBUSB_SUCCESS;
import static ceri.serial.libusb.jna.LibUsb.libusb_option.LIBUSB_OPTION_LOG_LEVEL;
import static ceri.serial.libusb.jna.LibUsb.libusb_option.LIBUSB_OPTION_USE_USBDK;
import static ceri.serial.libusb.jna.LibUsb.libusb_option.LIBUSB_OPTION_WEAK_AUTHORITY;
import static ceri.serial.libusb.jna.LibUsb.libusb_transfer_status.LIBUSB_TRANSFER_CANCELLED;
import static ceri.serial.libusb.jna.LibUsb.libusb_transfer_status.LIBUSB_TRANSFER_COMPLETED;
import static java.nio.charset.StandardCharsets.ISO_8859_1;
import java.nio.ByteBuffer;
import java.time.Duration;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.sun.jna.LastErrorException;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.PointerByReference;
import ceri.common.data.ByteProvider;
import ceri.common.data.ByteUtil;
import ceri.common.test.CallSync;
import ceri.common.util.Enclosed;
import ceri.serial.clib.jna.CCaller;
import ceri.serial.clib.jna.CTime.timeval;
import ceri.serial.jna.JnaUtil;
import ceri.serial.jna.PointerUtil;
import ceri.serial.jna.Struct;
import ceri.serial.libusb.UsbEvents.PollFd;
import ceri.serial.libusb.jna.LibUsb.libusb_bos_dev_capability_descriptor;
import ceri.serial.libusb.jna.LibUsb.libusb_bos_type;
import ceri.serial.libusb.jna.LibUsb.libusb_context;
import ceri.serial.libusb.jna.LibUsb.libusb_device;
import ceri.serial.libusb.jna.LibUsb.libusb_device_handle;
import ceri.serial.libusb.jna.LibUsb.libusb_endpoint_direction;
import ceri.serial.libusb.jna.LibUsb.libusb_error;
import ceri.serial.libusb.jna.LibUsb.libusb_hotplug_callback_fn;
import ceri.serial.libusb.jna.LibUsb.libusb_hotplug_event;
import ceri.serial.libusb.jna.LibUsb.libusb_log_cb;
import ceri.serial.libusb.jna.LibUsb.libusb_option;
import ceri.serial.libusb.jna.LibUsb.libusb_pollfd;
import ceri.serial.libusb.jna.LibUsb.libusb_pollfd_added_cb;
import ceri.serial.libusb.jna.LibUsb.libusb_pollfd_removed_cb;
import ceri.serial.libusb.jna.LibUsb.libusb_request_recipient;
import ceri.serial.libusb.jna.LibUsb.libusb_request_type;
import ceri.serial.libusb.jna.LibUsb.libusb_transfer_status;
import ceri.serial.libusb.jna.LibUsb.libusb_transfer_type;
import ceri.serial.libusb.jna.LibUsb.libusb_version;
import ceri.serial.libusb.jna.LibUsbTestData.Context;
import ceri.serial.libusb.jna.LibUsbTestData.DeviceHandle;
import ceri.serial.libusb.jna.LibUsbTestData.HotPlug;
import ceri.serial.libusb.jna.LibUsbTestData.Transfer;

public class TestLibUsbNative implements LibUsbNative {
	private static final Logger logger = LogManager.getFormatterLogger();
	public final LibUsbTestData data = new LibUsbTestData();
	// For general int values
	public final CallSync.Get<Integer> generalSync = CallSync.supplier(0);
	// List<?> = (int reqType, int req, int value, int index, int length)
	public final CallSync.Apply<List<?>, ByteProvider> controlTransferIn =
		CallSync.function(null, ByteProvider.empty());
	// List<?> = (int reqType, int req, int value, int index, ByteProvider data)
	public final CallSync.Apply<List<?>, Integer> controlTransferOut = CallSync.function(null, 0);
	// List<?> = (int endpoint, int length)
	public final CallSync.Apply<List<?>, ByteProvider> bulkTransferIn =
		CallSync.function(null, ByteProvider.empty());
	// List<?> = (int endpoint, ByteProvider data)
	public final CallSync.Apply<List<?>, Integer> bulkTransferOut = CallSync.function(null, 0);
	public final CallSync.Apply<Transfer, libusb_error> submitTransfer =
		CallSync.function(null, LIBUSB_SUCCESS);
	public final CallSync.Apply<TransferEvent, libusb_transfer_status> handleTransferEvent =
		CallSync.<TransferEvent, libusb_transfer_status>function(null)
			.autoResponse(t -> handleTransferFull(t));
	public final CallSync.Apply<HotPlug, HotPlugEvent> handleHotPlugEvent =
		CallSync.function(null, new HotPlugEvent(null, null));
	public final CallSync.Get<PollFdEvent> handlePollFdEvent =
		CallSync.supplier((PollFdEvent) null);
	public final CallSync.Get<List<PollFd>> pollFds = CallSync.supplier((List<PollFd>) null);

	public static record TransferEvent(int endPoint, libusb_transfer_type type,
		ByteBuffer buffer) {}

	public static record HotPlugEvent(libusb_device device, libusb_hotplug_event event) {}

	public static record PollFdEvent(int fd, int events, boolean added) {}

	public static void assertTransferEvent(TransferEvent t, int endPoint, libusb_transfer_type type,
		int... bytes) {
		assertEquals(t.endPoint, endPoint, "endPoint");
		assertEquals(t.type, type, "type");
		assertArray(JnaUtil.bytes(t.buffer), bytes);
	}

	public static LastErrorException lastError(LibUsb.libusb_error error) {
		return new LastErrorException(error.value);
	}

	public static Enclosed<RuntimeException, TestLibUsbNative> register() {
		return register(of());
	}

	public static <T extends LibUsbNative> Enclosed<RuntimeException, T> register(T lib) {
		return LibUsb.library.enclosed(lib);
	}

	public static TestLibUsbNative of() {
		return new TestLibUsbNative();
	}

	protected TestLibUsbNative() {
		reset();
	}

	public void reset() {
		data.reset();
	}

	@Override
	public int libusb_init(PointerByReference ctxRef) {
		int result = generalSync.get();
		if (result != 0) return result;
		var context = ctxRef == null ? data.createContextDef() : data.createContext();
		if (ctxRef != null) ctxRef.setValue(context.p);
		return 0;
	}

	@Override
	public void libusb_exit(libusb_context ctx) {
		data.removeContext(PointerUtil.pointer(ctx));
	}

	@Override
	public int libusb_set_option(libusb_context ctx, int option, Object... args) {
		var context = context(ctx);
		var opt = libusb_option.xcoder.decode(option);
		int value = (Integer) args[0];
		if (opt == LIBUSB_OPTION_LOG_LEVEL) context.debugLevel = value;
		else if (opt == LIBUSB_OPTION_USE_USBDK) context.usbDk = value != 0;
		else if (opt == LIBUSB_OPTION_WEAK_AUTHORITY) context.weakAuth = value != 0;
		else return LIBUSB_ERROR_NOT_SUPPORTED.value;
		return 0;
	}

	@Override
	public void libusb_set_log_cb(libusb_context ctx, libusb_log_cb cb, int mode) {
		throw new UnsupportedOperationException();
	}

	@Override
	public libusb_version libusb_get_version() {
		return data.version;
	}

	@Override
	public int libusb_has_capability(int capability) {
		return data.capabilities & capability;
	}

	@Override
	public String libusb_error_name(int errcode) {
		libusb_error error = libusb_error.xcoder.decode(errcode);
		return error != null ? error.name() : "error-" + errcode;
	}

	@Override
	public int libusb_setlocale(String locale) {
		data.locale = locale;
		return 0;
	}

	@Override
	public String libusb_strerror(int errcode) {
		return libusb_error_name(errcode);
	}

	@Override
	public int libusb_get_device_list(libusb_context ctx, PointerByReference list) {
		var deviceList = data.createDeviceList(context(ctx));
		list.setValue(deviceList.p);
		return deviceList.size;
	}

	@Override
	public void libusb_free_device_list(Pointer list, int unref_devices) {
		var deviceList = data.deviceList(list);
		data.removeDeviceList(deviceList, unref_devices != 0);
	}

	@Override
	public libusb_device libusb_ref_device(libusb_device dev) {
		data.refDevice(data.device(PointerUtil.pointer(dev)), 1);
		return dev;
	}

	@Override
	public void libusb_unref_device(libusb_device dev) {
		data.refDevice(data.device(PointerUtil.pointer(dev)), -1);
	}

	@Override
	public int libusb_get_configuration(libusb_device_handle dev, IntByReference config) {
		config.setValue(handle(dev).configuration);
		return 0;
	}

	@Override
	public int libusb_get_device_descriptor(libusb_device dev, Pointer p) {
		var desc = Struct.write(data.device(PointerUtil.pointer(dev)).config.desc);
		JnaUtil.memcpy(p, 0, desc.getPointer(), 0, desc.size());
		return 0;
	}

	@Override
	public int libusb_get_active_config_descriptor(libusb_device dev, PointerByReference config) {
		return libusb_get_config_descriptor(dev, (byte) 0, config);
	}

	@Override
	public int libusb_get_config_descriptor(libusb_device dev, byte config_index,
		PointerByReference config) {
		var desc =
			data.device(PointerUtil.pointer(dev)).config.configDescriptor(ubyte(config_index));
		if (desc == null) return LIBUSB_ERROR_NOT_FOUND.value;
		config.setValue(desc.getPointer()); // don't copy descriptor
		return 0;
	}

	@Override
	public int libusb_get_config_descriptor_by_value(libusb_device dev, byte bConfigurationValue,
		PointerByReference config) {
		var desc = data.device(PointerUtil.pointer(dev)).config
			.configDescriptorByValue(bConfigurationValue);
		if (desc == null) return LIBUSB_ERROR_NOT_FOUND.value;
		config.setValue(desc.getPointer()); // don't copy descriptor
		return 0;
	}

	@Override
	public void libusb_free_config_descriptor(Pointer config) {
		// do nothing
	}

	@Override
	public int libusb_get_ss_endpoint_companion_descriptor(libusb_context ctx, Pointer endpoint,
		PointerByReference ep_comp) {
		var desc = data.ssEpCompDesc(endpoint);
		if (desc == null) return LIBUSB_ERROR_NOT_FOUND.value;
		ep_comp.setValue(desc.getPointer());
		return 0;
	}

	@Override
	public void libusb_free_ss_endpoint_companion_descriptor(Pointer ep_comp) {
		// do nothing
	}

	@Override
	public int libusb_get_bos_descriptor(libusb_device_handle handle, PointerByReference bos) {
		var desc = handle(handle).device.config.bos;
		if (desc == null) return LIBUSB_ERROR_PIPE.value;
		bos.setValue(desc.getPointer()); // don't copy descriptor
		return 0;
	}

	@Override
	public void libusb_free_bos_descriptor(Pointer bos) {
		// do nothing
	}

	@Override
	public int libusb_get_usb_2_0_extension_descriptor(libusb_context ctx, Pointer dev_cap,
		PointerByReference usb_2_0_extension) {
		return bosDevCap(dev_cap, LIBUSB_BT_USB_2_0_EXTENSION, usb_2_0_extension);
	}

	@Override
	public void libusb_free_usb_2_0_extension_descriptor(Pointer usb_2_0_extension) {
		// do nothing
	}

	@Override
	public int libusb_get_ss_usb_device_capability_descriptor(libusb_context ctx, Pointer dev_cap,
		PointerByReference ss_usb_device_cap) {
		return bosDevCap(dev_cap, LIBUSB_BT_SS_USB_DEVICE_CAPABILITY, ss_usb_device_cap);
	}

	@Override
	public void libusb_free_ss_usb_device_capability_descriptor(Pointer ss_usb_device_cap) {
		// do nothing
	}

	@Override
	public int libusb_get_container_id_descriptor(libusb_context ctx, Pointer dev_cap,
		PointerByReference container_id) {
		return bosDevCap(dev_cap, LIBUSB_BT_CONTAINER_ID, container_id);
	}

	@Override
	public void libusb_free_container_id_descriptor(Pointer container_id) {
		// do nothing
	}

	@Override
	public byte libusb_get_bus_number(libusb_device dev) {
		return (byte) data.device(PointerUtil.pointer(dev)).config.busNumber;
	}

	@Override
	public byte libusb_get_port_number(libusb_device dev) {
		return (byte) data.device(PointerUtil.pointer(dev)).config.portNumber();
	}

	@Override
	public int libusb_get_port_numbers(libusb_device dev, Pointer port_numbers,
		int port_numbers_len) {
		byte[] portNumbers = data.device(PointerUtil.pointer(dev)).config.portNumbers;
		if (portNumbers.length > port_numbers_len) return LIBUSB_ERROR_OVERFLOW.value;
		JnaUtil.write(port_numbers, portNumbers);
		return portNumbers.length;
	}

	@Override
	public libusb_device libusb_get_parent(libusb_device dev) {
		var device = data.device(PointerUtil.pointer(dev));
		var parent = data.parentDevice(device);
		return parent == null ? null : PointerUtil.set(new libusb_device(), parent.p);
	}

	@Override
	public byte libusb_get_device_address(libusb_device dev) {
		return (byte) data.device(PointerUtil.pointer(dev)).config.address;
	}

	@Override
	public int libusb_get_device_speed(libusb_device dev) {
		return data.device(PointerUtil.pointer(dev)).config.speed.value;
	}

	@Override
	public int libusb_get_max_packet_size(libusb_device dev, byte endpoint) {
		throw new UnsupportedOperationException();
	}

	@Override
	public int libusb_get_max_iso_packet_size(libusb_device dev, byte endpoint) {
		throw new UnsupportedOperationException();
	}

	@Override
	public int libusb_wrap_sys_device(libusb_context ctx, int sys_dev, PointerByReference handle) {
		throw new UnsupportedOperationException();
	}

	@Override
	public int libusb_open(libusb_device dev, PointerByReference handle_ref) {
		var device = data.device(PointerUtil.pointer(dev));
		var handle = data.createDeviceHandle(device);
		handle_ref.setValue(handle.p);
		return 0;
	}

	@Override
	public void libusb_close(libusb_device_handle dev_handle) {
		data.removeDeviceHandle(handle(dev_handle));
	}

	@Override
	public libusb_device libusb_get_device(libusb_device_handle dev_handle) {
		return PointerUtil.set(new libusb_device(), handle(dev_handle).device.p);
	}

	@Override
	public int libusb_set_configuration(libusb_device_handle dev, int configuration) {
		handle(dev).configuration = configuration;
		return 0;
	}

	@Override
	public int libusb_claim_interface(libusb_device_handle dev, int interface_number) {
		var handle = handle(dev);
		handle.resetInterface();
		handle.claimedInterface = interface_number;
		return 0;
	}

	@Override
	public int libusb_release_interface(libusb_device_handle dev, int interface_number) {
		var handle = handle(dev);
		if (handle.claimedInterface != interface_number) return LIBUSB_ERROR_NOT_FOUND.value;
		handle.resetInterface();
		return 0;
	}

	@Override
	public libusb_device_handle libusb_open_device_with_vid_pid(libusb_context ctx, short vendor_id,
		short product_id) {
		var deviceList = data.createDeviceList(context(ctx));
		try {
			var device = data.device(d -> d.config.desc.idVendor == vendor_id //
				&& d.config.desc.idProduct == product_id);
			if (device == null) return null;
			var handle = data.createDeviceHandle(device);
			return PointerUtil.set(new libusb_device_handle(), handle.p);
		} finally {
			data.removeDeviceList(deviceList, true);
		}
	}

	@Override
	public int libusb_set_interface_alt_setting(libusb_device_handle dev, int interface_number,
		int alternate_setting) {
		var handle = handle(dev);
		if (handle.claimedInterface != interface_number) return LIBUSB_ERROR_NOT_FOUND.value;
		handle.altSetting = alternate_setting;
		return 0;
	}

	@Override
	public int libusb_clear_halt(libusb_device_handle dev, byte endpoint) {
		throw new UnsupportedOperationException();
	}

	@Override
	public int libusb_reset_device(libusb_device_handle dev) {
		handle(dev).reset();
		return 0;
	}

	@Override
	public int libusb_alloc_streams(libusb_device_handle dev, int num_streams, Pointer endpoints,
		int num_endpoints) {
		var handle = handle(dev);
		if (handle.streamIds != 0) return LIBUSB_ERROR_NO_DEVICE.value;
		handle.streamIds = num_streams;
		handle.endPoints = ByteProvider.of(endpoints.getByteArray(0, num_endpoints));
		return num_streams;
	}

	@Override
	public int libusb_free_streams(libusb_device_handle dev, Pointer endpoints, int num_endpoints) {
		var handle = handle(dev);
		handle.streamIds = 0;
		handle.endPoints = ByteProvider.empty();
		return 0;
	}

	@Override
	public Pointer libusb_dev_mem_alloc(libusb_device_handle dev, int length) {
		throw new UnsupportedOperationException();
	}

	@Override
	public int libusb_dev_mem_free(libusb_device_handle dev, Pointer buffer, int length) {
		throw new UnsupportedOperationException();
	}

	@Override
	public int libusb_kernel_driver_active(libusb_device_handle dev, int interface_number) {
		throw new UnsupportedOperationException();
	}

	@Override
	public int libusb_detach_kernel_driver(libusb_device_handle dev, int interface_number) {
		handle(dev);
		return 0;
	}

	@Override
	public int libusb_attach_kernel_driver(libusb_device_handle dev, int interface_number) {
		handle(dev);
		return 0;
	}

	@Override
	public int libusb_set_auto_detach_kernel_driver(libusb_device_handle dev, int enable) {
		handle(dev);
		return 0;
	}

	@Override
	public Pointer libusb_alloc_transfer(int isoPackets) {
		return data.createTransfer(isoPackets).p;
	}

	@Override
	public int libusb_submit_transfer(Pointer p) {
		var transfer = data.transfer(p);
		deviceHandle(transfer); // verify device handle is valid
		if (transfer.submitted) return LIBUSB_ERROR_BUSY.value;
		var result = submitTransfer.apply(transfer);
		if (result == LIBUSB_SUCCESS) transfer.submitted = true;
		return result.value;
	}

	@Override
	public int libusb_cancel_transfer(Pointer p) {
		var transfer = data.transfer(p);
		if (!transfer.submitted) return LIBUSB_ERROR_NOT_FOUND.value;
		var t = transfer.transfer();
		if (t.status() == LIBUSB_TRANSFER_CANCELLED) return LIBUSB_ERROR_NOT_FOUND.value;
		t.status = LIBUSB_TRANSFER_CANCELLED.value;
		Struct.write(t, "status");
		return 0;
	}

	@Override
	public void libusb_free_transfer(Pointer p) {
		var transfer = data.transfer(p);
		if (transfer.submitted) throw lastError(LIBUSB_ERROR_BUSY);
		data.removeTransfer(data.transfer(p));
	}

	@Override
	public void libusb_transfer_set_stream_id(Pointer p, int stream_id) {
		for (var transfer : data.transfers())
			if (transfer.streamId == stream_id) throw lastError(LIBUSB_ERROR_IO);
		data.transfer(p).streamId = stream_id;
	}

	@Override
	public int libusb_transfer_get_stream_id(Pointer p) {
		return data.transfer(p).streamId;
	}

	public static int libusb_request_type_value(libusb_request_recipient recipient,
		libusb_request_type type, libusb_endpoint_direction endpoint_direction) {
		return (recipient.value | type.value | endpoint_direction.value) & 0xff;
	}

	@Override
	public int libusb_control_transfer(libusb_device_handle dev_handle, byte request_type,
		byte bRequest, short wValue, short wIndex, ByteBuffer data, short wLength, int timeout) {
		handle(dev_handle);
		if ((request_type & libusb_endpoint_direction.LIBUSB_ENDPOINT_IN.value) != 0)
			return controlTransferIn(ubyte(request_type), ubyte(bRequest), ushort(wValue),
				ushort(wIndex), data, ushort(wLength));
		return controlTransferOut(ubyte(request_type), ubyte(bRequest), ushort(wValue),
			ushort(wIndex), data, ushort(wLength));
	}

	@Override
	public int libusb_bulk_transfer(libusb_device_handle dev_handle, byte endpoint, ByteBuffer data,
		int length, IntByReference actual_length, int timeout) {
		handle(dev_handle);
		if ((endpoint & libusb_endpoint_direction.LIBUSB_ENDPOINT_IN.value) != 0)
			return bulkTransferIn(ubyte(endpoint), data, length, actual_length);
		return bulkTransferOut(ubyte(endpoint), data, length, actual_length);

	}

	@Override
	public int libusb_interrupt_transfer(libusb_device_handle dev_handle, byte endpoint,
		ByteBuffer data, int length, IntByReference actual_length, int timeout) {
		throw new UnsupportedOperationException();
	}

	@Override
	public int libusb_get_string_descriptor_ascii(libusb_device_handle dev, byte desc_index,
		ByteBuffer data, int length) {
		String s = handle(dev).device.config.descriptorString(ubyte(desc_index));
		byte[] bytes = s.getBytes(ISO_8859_1);
		data.put(bytes);
		return bytes.length;
	}

	@Override
	public int libusb_try_lock_events(libusb_context ctx) {
		return context(ctx).eventLock.tryLock() ? 0 : 1;
	}

	@Override
	public void libusb_lock_events(libusb_context ctx) {
		context(ctx).eventLock.lock();
	}

	@Override
	public void libusb_unlock_events(libusb_context ctx) {
		context(ctx).eventLock.unlock();
	}

	@Override
	public int libusb_event_handling_ok(libusb_context ctx) {
		return context(ctx).eventHandling ? 1 : 0;
	}

	@Override
	public int libusb_event_handler_active(libusb_context ctx) {
		return context(ctx).eventLock.isLocked() ? 1 : 0;
	}

	@Override
	public void libusb_lock_event_waiters(libusb_context ctx) {
		context(ctx).eventWaiterLock.lock();
	}

	@Override
	public void libusb_unlock_event_waiters(libusb_context ctx) {
		context(ctx).eventWaiterLock.unlock();
	}

	@Override
	public int libusb_wait_for_event(libusb_context ctx, Pointer tv) {
		context(ctx);
		return 0;
	}

	@Override
	public void libusb_interrupt_event_handler(libusb_context ctx) {
		context(ctx);
	}

	@Override
	public int libusb_handle_events_timeout(libusb_context ctx, Pointer tv) {
		return libusb_handle_events_timeout_completed(ctx, tv, null);
	}

	@Override
	public int libusb_handle_events_timeout_completed(libusb_context ctx, Pointer tv,
		IntByReference completed) {
		try {
			var context = context(ctx);
			if (completed == null) completed = new IntByReference();
			if (completed.getValue() != 0) return 0;
			handleTransferEvents(context);
			handleHotPlugEvents(context);
			handlePollFdEvents(context);
			return 0;
		} catch (LastErrorException e) {
			return e.getErrorCode();
		} catch (AssertionError e) {
			logger.catching(e); // to provide more detail
			throw e;
		}
	}

	@Override
	public int libusb_handle_events(libusb_context ctx) {
		return libusb_handle_events_completed(ctx, null);
	}

	@Override
	public int libusb_handle_events_completed(libusb_context ctx, IntByReference completed) {
		return libusb_handle_events_timeout_completed(ctx, null, completed);
	}

	@Override
	public int libusb_handle_events_locked(libusb_context ctx, Pointer tv) {
		if (!context(ctx).eventLock.isHeldByCurrentThread()) return LIBUSB_ERROR_ACCESS.value;
		return libusb_handle_events_timeout(ctx, tv);

	}

	@Override
	public int libusb_pollfds_handle_timeouts(libusb_context ctx) {
		return generalSync.get();
	}

	@Override
	public int libusb_get_next_timeout(libusb_context ctx, Pointer tv) {
		return CCaller
			.capture(() -> Struct.write(new timeval(tv).set(Duration.ofMillis(generalSync.get()))));
	}

	@Override
	public Pointer libusb_get_pollfds(libusb_context ctx) {
		context(ctx);
		var list = this.pollFds.get();
		if (list == null) return null;
		var pollFds = new libusb_pollfd[list.size()];
		var pointers = PointerUtil.callocArray(list.size() + 1);
		for (int i = 0; i < pollFds.length; i++) {
			pollFds[i] = new libusb_pollfd(null);
			pollFds[i].fd = list.get(i).fd();
			pollFds[i].events = (short) list.get(i).events();
			pointers[i].setPointer(0, Struct.write(pollFds[i]).getPointer());
		}
		return pointers[0];
	}

	@Override
	public void libusb_free_pollfds(Pointer pollfds) throws LastErrorException {
		// do nothing
	}

	@Override
	public void libusb_set_pollfd_notifiers(libusb_context ctx, libusb_pollfd_added_cb added_cb,
		libusb_pollfd_removed_cb removed_cb, Pointer user_data) {
		var context = context(ctx);
		context.pollFdAddedCb = added_cb;
		context.pollFdRemovedCb = removed_cb;
	}

	@Override
	public int libusb_hotplug_register_callback(libusb_context ctx, int events, int flags,
		int vendor_id, int product_id, int dev_class, libusb_hotplug_callback_fn cb_fn,
		Pointer user_data, IntByReference handle) {
		return CCaller.capture(() -> {
			var hotPlug = data.createHotPlug(context(ctx));
			hotPlug.events = events;
			hotPlug.flags = flags;
			hotPlug.vendorId = vendor_id;
			hotPlug.productId = product_id;
			hotPlug.devClass = dev_class;
			hotPlug.callback = cb_fn;
			hotPlug.userData = user_data;
			handle.setValue(hotPlug.handle);
		});
	}

	@Override
	public void libusb_hotplug_deregister_callback(libusb_context ctx, int handle) {
		data.removeHotPlug(handle);
	}

	@Override
	public Pointer libusb_hotplug_get_user_data(libusb_context ctx, int callback_handle)
		throws LastErrorException {
		var hotPlug = data.hotPlug(callback_handle);
		if (hotPlug.context == context(ctx)) return hotPlug.userData;
		throw lastError(LIBUSB_ERROR_NOT_FOUND);
	}

	/**
	 * Auto-response function for handleEvent.
	 */
	public static libusb_transfer_status handleTransferFull(TransferEvent event) {
		return handleTransferN(event, event.buffer.capacity());
	}

	/**
	 * Auto-response function for handleEvent.
	 */
	public static libusb_transfer_status handleTransferN(TransferEvent event, int n) {
		event.buffer.position(n);
		return LIBUSB_TRANSFER_COMPLETED;
	}

	public void assertTransferEvent(int endPoint, libusb_transfer_type type,
		int... bytes) {
		assertTransferEvent(handleTransferEvent.value(), endPoint, type, bytes);
	}

	private Context context(libusb_context ctx) {
		return data.context(PointerUtil.pointer(ctx));
	}

	private int bosDevCap(Pointer dev_cap, libusb_bos_type type, PointerByReference ref) {
		if (dev_cap == null) return LIBUSB_ERROR_NOT_FOUND.value;
		var desc = Struct.read(new libusb_bos_dev_capability_descriptor(dev_cap));
		if (ubyte(desc.bDescriptorType) != LIBUSB_DT_DEVICE_CAPABILITY.value ||
			ubyte(desc.bDevCapabilityType) != type.value) return LIBUSB_ERROR_NOT_FOUND.value;
		ref.setValue(dev_cap);
		return 0;
	}

	private int controlTransferIn(int reqType, int req, int value, int index, ByteBuffer buffer,
		int length) {
		ByteProvider bytes = controlTransferIn.apply(List.of(reqType, req, value, index, length));
		if (bytes == null || bytes.length() == 0) return 0;
		ByteUtil.writeTo(buffer, 0, bytes.copy(0));
		return bytes.length();
	}

	private int controlTransferOut(int reqType, int req, int value, int index, ByteBuffer buffer,
		int length) {
		byte[] bytes = new byte[length];
		if (buffer != null) ByteUtil.readFrom(buffer, 0, bytes);
		return controlTransferOut
			.apply(List.of(reqType, req, value, index, ByteProvider.of(bytes)));
	}

	private int bulkTransferIn(int endpoint, ByteBuffer buffer, int length,
		IntByReference actualLength) {
		ByteProvider bytes = bulkTransferIn.apply(List.of(endpoint, length));
		if (bytes != null) {
			ByteUtil.writeTo(buffer, 0, bytes.copy(0));
			if (actualLength != null) actualLength.setValue(bytes.length());
		}
		return 0;
	}

	private int bulkTransferOut(int endpoint, ByteBuffer buffer, int length,
		IntByReference actualLength) {
		byte[] bytes = new byte[length];
		if (buffer != null) ByteUtil.readFrom(buffer, 0, bytes);
		Integer result = bulkTransferOut.apply(List.of(endpoint, ByteProvider.of(bytes)));
		if (actualLength != null) actualLength.setValue(result == null ? 0 : length);
		return result == null ? 0 : result;
	}

	private DeviceHandle deviceHandle(Transfer transfer) {
		try {
			return handle(transfer.transfer().dev_handle);
		} catch (LastErrorException e) {
			throw lastError(LIBUSB_ERROR_NO_DEVICE);
		}
	}

	public DeviceHandle handle(libusb_device_handle handle) {
		return data.deviceHandle(PointerUtil.pointer(handle));
	}

	private void handlePollFdEvents(Context context) {
		var event = handlePollFdEvent.get();
		if (event == null) return;
		if (event.added() && context.pollFdAddedCb != null)
			context.pollFdAddedCb.invoke(event.fd(), (short) event.events(), null);
		if (!event.added() && context.pollFdRemovedCb != null)
			context.pollFdRemovedCb.invoke(event.fd(), null);
	}

	private void handleHotPlugEvents(Context context) {
		for (var hotPlug : data.hotPlugs()) {
			if (hotPlug.context != context) continue;
			var event = handleHotPlugEvent.apply(hotPlug);
			if (event.device == null) continue;
			var ctx = PointerUtil.set(new libusb_context(), context.p);
			int result =
				hotPlug.callback.invoke(ctx, event.device, event.event.value, hotPlug.userData);
			if (result != 0) data.removeHotPlug(hotPlug.handle);
		}
	}

	private void handleTransferEvents(Context context) {
		for (var transfer : data.transfers()) {
			if (!transfer.submitted) continue;
			var t = transfer.transfer();
			if (handle(t.dev_handle).device.deviceList.context != context) continue;
			if (t.status == 0) {
				ByteBuffer buffer = JnaUtil.buffer(t.buffer, 0, t.length);
				var status = handleTransferEvent
					.apply(new TransferEvent(ubyte(t.endpoint), t.type(), buffer));
				if (status == null) continue; // no event
				t.status = status.value;
				t.actual_length = buffer.position();
				Struct.write(t);
			}
			transfer.submitted = false;
			t.callback.invoke(transfer.p);
		}
	}

}
