package ceri.serial.libusb.jna;

import static ceri.common.math.MathUtil.ubyte;
import static ceri.common.math.MathUtil.ushort;
import static ceri.serial.libusb.jna.LibUsb.libusb_bos_type.LIBUSB_BT_CONTAINER_ID;
import static ceri.serial.libusb.jna.LibUsb.libusb_bos_type.LIBUSB_BT_SS_USB_DEVICE_CAPABILITY;
import static ceri.serial.libusb.jna.LibUsb.libusb_bos_type.LIBUSB_BT_USB_2_0_EXTENSION;
import static ceri.serial.libusb.jna.LibUsb.libusb_descriptor_type.LIBUSB_DT_DEVICE_CAPABILITY;
import static ceri.serial.libusb.jna.LibUsb.libusb_error.LIBUSB_ERROR_NOT_FOUND;
import static ceri.serial.libusb.jna.LibUsb.libusb_error.LIBUSB_ERROR_NOT_SUPPORTED;
import static ceri.serial.libusb.jna.LibUsb.libusb_error.LIBUSB_ERROR_OVERFLOW;
import static ceri.serial.libusb.jna.LibUsb.libusb_error.LIBUSB_ERROR_PIPE;
import static ceri.serial.libusb.jna.LibUsb.libusb_option.LIBUSB_OPTION_LOG_LEVEL;
import static ceri.serial.libusb.jna.LibUsb.libusb_option.LIBUSB_OPTION_USE_USBDK;
import static ceri.serial.libusb.jna.LibUsb.libusb_option.LIBUSB_OPTION_WEAK_AUTHORITY;
import static java.nio.charset.StandardCharsets.ISO_8859_1;
import java.nio.ByteBuffer;
import java.util.List;
import com.sun.jna.LastErrorException;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.PointerByReference;
import ceri.common.data.ByteProvider;
import ceri.common.data.ByteUtil;
import ceri.common.test.CallSync;
import ceri.common.util.Enclosed;
import ceri.serial.clib.jna.CUtil;
import ceri.serial.jna.JnaUtil;
import ceri.serial.jna.PointerUtil;
import ceri.serial.jna.Struct;
import ceri.serial.libusb.jna.LibUsb.libusb_bos_dev_capability_descriptor;
import ceri.serial.libusb.jna.LibUsb.libusb_bos_type;
import ceri.serial.libusb.jna.LibUsb.libusb_context;
import ceri.serial.libusb.jna.LibUsb.libusb_device;
import ceri.serial.libusb.jna.LibUsb.libusb_device_handle;
import ceri.serial.libusb.jna.LibUsb.libusb_endpoint_direction;
import ceri.serial.libusb.jna.LibUsb.libusb_error;
import ceri.serial.libusb.jna.LibUsb.libusb_hotplug_callback_fn;
import ceri.serial.libusb.jna.LibUsb.libusb_iso_packet_descriptor;
import ceri.serial.libusb.jna.LibUsb.libusb_log_cb;
import ceri.serial.libusb.jna.LibUsb.libusb_option;
import ceri.serial.libusb.jna.LibUsb.libusb_pollfd_added_cb;
import ceri.serial.libusb.jna.LibUsb.libusb_pollfd_removed_cb;
import ceri.serial.libusb.jna.LibUsb.libusb_request_recipient;
import ceri.serial.libusb.jna.LibUsb.libusb_request_type;
import ceri.serial.libusb.jna.LibUsb.libusb_transfer;
import ceri.serial.libusb.jna.LibUsb.libusb_version;

public class TestLibUsbNative implements LibUsbNative {
	public final LibUsbTestData data = new LibUsbTestData();
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

	public static Enclosed<RuntimeException, TestLibUsbNative> register() {
		return register(of());
	}

	public static <T extends LibUsbNative> Enclosed<RuntimeException, T> register(T lib) {
		return LibUsb.library.enclosed(lib);
	}

	// TODO:
	// - extract audio descriptors from test data
	// - endPoint -> endpoint
	// - summarize libusb
	// - async transfers
	// - polling
	// - abstract async io
	// - transfer type
	// - fill
	// - callback logic
	// - breakdown by completion/timeout/error/cancel?
	// - check libusb 1.0.24 examples

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
		var context = data.createContext(ctxRef == null);
		if (ctxRef != null) ctxRef.setValue(context.ptr);
		return 0;
	}

	@Override
	public void libusb_exit(libusb_context ctx) {
		data.removeContext(data.context(PointerUtil.pointer(ctx)));
	}

	@Override
	public int libusb_set_option(libusb_context ctx, int option, Object... args) {
		var context = data.context(PointerUtil.pointer(ctx));
		var opt = libusb_option.xcoder.decode(option);
		int value = (int) args[0];
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
		var context = data.context(PointerUtil.pointer(ctx));
		var deviceList = data.createDeviceList(context);
		list.setValue(deviceList.ptr);
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
		config.setValue(data.deviceHandle(PointerUtil.pointer(dev)).configuration);
		return 0;
	}

	@Override
	public int libusb_get_device_descriptor(libusb_device dev, Pointer p) {
		var desc = Struct.write(data.device(PointerUtil.pointer(dev)).config.desc);
		CUtil.memmove(p, 0, desc.getPointer(), 0, desc.size());
		return 0;
	}

	@Override
	public int libusb_get_active_config_descriptor(libusb_device dev, PointerByReference config) {
		throw new UnsupportedOperationException();
	}

	@Override
	public int libusb_get_config_descriptor(libusb_device dev, byte config_index,
		PointerByReference config) {
		var desc = data.device(PointerUtil.pointer(dev)).config.configDescriptor(ubyte(config_index));
		if (desc == null) return LIBUSB_ERROR_NOT_FOUND.value;
		config.setValue(desc.getPointer()); // don't copy descriptor
		return 0;
	}

	@Override
	public int libusb_get_config_descriptor_by_value(libusb_device dev, byte bConfigurationValue,
		PointerByReference config) {
		var desc = data.device(PointerUtil.pointer(dev)).config.configDescriptorByValue(bConfigurationValue);
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
		var desc = data.deviceHandle(PointerUtil.pointer(handle)).device.config.bos;
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
		return parent == null ? null : new libusb_device(parent.ptr);
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
		handle_ref.setValue(handle.ptr);
		return 0;
	}

	@Override
	public void libusb_close(libusb_device_handle dev_handle) {
		var handle = data.deviceHandle(PointerUtil.pointer(dev_handle));
		data.removeDeviceHandle(handle);
	}

	@Override
	public libusb_device libusb_get_device(libusb_device_handle dev_handle) {
		var device = data.deviceHandle(PointerUtil.pointer(dev_handle)).device;
		return new libusb_device(device.ptr);
	}

	@Override
	public int libusb_set_configuration(libusb_device_handle dev, int configuration) {
		data.deviceHandle(PointerUtil.pointer(dev)).configuration = configuration;
		return 0;
	}

	@Override
	public int libusb_claim_interface(libusb_device_handle dev, int interface_number) {
		var handle = data.deviceHandle(PointerUtil.pointer(dev));
		handle.resetInterface();
		handle.claimedInterface = interface_number;
		return 0;
	}

	@Override
	public int libusb_release_interface(libusb_device_handle dev, int interface_number) {
		var handle = data.deviceHandle(PointerUtil.pointer(dev));
		if (handle.claimedInterface != interface_number) return LIBUSB_ERROR_NOT_FOUND.value;
		handle.resetInterface();
		return 0;
	}

	@Override
	public libusb_device_handle libusb_open_device_with_vid_pid(libusb_context ctx, short vendor_id,
		short product_id) {
		var context = data.context(PointerUtil.pointer(ctx));
		var deviceList = data.createDeviceList(context);
		try {
			var device = data.device(d -> d.config.desc.idVendor == vendor_id //
				&& d.config.desc.idProduct == product_id);
			if (device == null) return null;
			var handle = data.createDeviceHandle(device);
			return PointerUtil.set(new libusb_device_handle(), handle.ptr);
		} finally {
			data.removeDeviceList(deviceList, true);
		}
	}

	@Override
	public int libusb_set_interface_alt_setting(libusb_device_handle dev, int interface_number,
		int alternate_setting) {
		var handle = data.deviceHandle(PointerUtil.pointer(dev));
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
		var handle = data.deviceHandle(PointerUtil.pointer(dev));
		handle.reset();
		return 0;
	}

	@Override
	public int libusb_alloc_streams(libusb_device_handle dev, int num_streams, Pointer endpoints,
		int num_endpoints) {
		throw new UnsupportedOperationException();
	}

	@Override
	public int libusb_free_streams(libusb_device_handle dev, Pointer endpoints, int num_endpoints) {
		throw new UnsupportedOperationException();
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
		data.deviceHandle(PointerUtil.pointer(dev));
		return 0;
	}

	@Override
	public int libusb_attach_kernel_driver(libusb_device_handle dev, int interface_number) {
		data.deviceHandle(PointerUtil.pointer(dev));
		return 0;
	}

	@Override
	public int libusb_set_auto_detach_kernel_driver(libusb_device_handle dev, int enable) {
		data.deviceHandle(PointerUtil.pointer(dev));
		return 0;
	}

	@Override
	public Pointer libusb_alloc_transfer(int isoPackets) {
		libusb_transfer xfer = new libusb_transfer(null);
		xfer.num_iso_packets = isoPackets;
		xfer.iso_packet_desc = new libusb_iso_packet_descriptor[isoPackets];
		for (int i = 0; i < isoPackets; i++)
			xfer.iso_packet_desc[i] = new libusb_iso_packet_descriptor(null);
		return Struct.write(xfer).getPointer();
	}

	@Override
	public int libusb_submit_transfer(Pointer transfer) {
		return 0;
	}

	@Override
	public int libusb_cancel_transfer(Pointer transfer) {
		return 0;
	}

	@Override
	public void libusb_free_transfer(Pointer transfer) {}

	@Override
	public void libusb_transfer_set_stream_id(Pointer transfer, int stream_id) {
		throw new UnsupportedOperationException();
	}

	@Override
	public int libusb_transfer_get_stream_id(Pointer transfer) {
		throw new UnsupportedOperationException();
	}

	public static int libusb_request_type_value(libusb_request_recipient recipient,
		libusb_request_type type, libusb_endpoint_direction endpoint_direction) {
		return (recipient.value | type.value | endpoint_direction.value) & 0xff;
	}

	@Override
	public int libusb_control_transfer(libusb_device_handle dev_handle, byte request_type,
		byte bRequest, short wValue, short wIndex, ByteBuffer data, short wLength, int timeout) {
		this.data.deviceHandle(PointerUtil.pointer(dev_handle));
		if ((request_type & libusb_endpoint_direction.LIBUSB_ENDPOINT_IN.value) != 0)
			return controlTransferIn(ubyte(request_type), ubyte(bRequest), ushort(wValue),
				ushort(wIndex), data, ushort(wLength));
		return controlTransferOut(ubyte(request_type), ubyte(bRequest), ushort(wValue),
			ushort(wIndex), data, ushort(wLength));
	}

	@Override
	public int libusb_bulk_transfer(libusb_device_handle dev_handle, byte endpoint, ByteBuffer data,
		int length, IntByReference actual_length, int timeout) {
		this.data.deviceHandle(PointerUtil.pointer(dev_handle));
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
		var handle = this.data.deviceHandle(PointerUtil.pointer(dev));
		String s = handle.device.config.descriptorString(ubyte(desc_index));
		byte[] bytes = s.getBytes(ISO_8859_1);
		data.put(bytes);
		return bytes.length;
	}

	@Override
	public int libusb_try_lock_events(libusb_context ctx) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void libusb_lock_events(libusb_context ctx) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void libusb_unlock_events(libusb_context ctx) {
		throw new UnsupportedOperationException();
	}

	@Override
	public int libusb_event_handling_ok(libusb_context ctx) {
		throw new UnsupportedOperationException();
	}

	@Override
	public int libusb_event_handler_active(libusb_context ctx) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void libusb_lock_event_waiters(libusb_context ctx) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void libusb_unlock_event_waiters(libusb_context ctx) {
		throw new UnsupportedOperationException();
	}

	@Override
	public int libusb_wait_for_event(libusb_context ctx, Pointer tv) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void libusb_interrupt_event_handler(libusb_context ctx) {
		throw new UnsupportedOperationException();
	}

	@Override
	public int libusb_handle_events_timeout(libusb_context ctx, Pointer tv) {
		throw new UnsupportedOperationException();
	}

	@Override
	public int libusb_handle_events_timeout_completed(libusb_context ctx, Pointer tv,
		IntByReference completed) {
		throw new UnsupportedOperationException();
	}

	@Override
	public int libusb_handle_events(libusb_context ctx) {
		throw new UnsupportedOperationException();
	}

	@Override
	public int libusb_handle_events_completed(libusb_context ctx, IntByReference completed) {
		throw new UnsupportedOperationException();
	}

	@Override
	public int libusb_handle_events_locked(libusb_context ctx, Pointer tv) {
		throw new UnsupportedOperationException();
	}

	@Override
	public int libusb_pollfds_handle_timeouts(libusb_context ctx) {
		throw new UnsupportedOperationException();
	}

	@Override
	public int libusb_get_next_timeout(libusb_context ctx, Pointer tv) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Pointer libusb_get_pollfds(libusb_context ctx) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void libusb_free_pollfds(Pointer pollfds) throws LastErrorException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void libusb_set_pollfd_notifiers(libusb_context ctx, libusb_pollfd_added_cb added_cb,
		libusb_pollfd_removed_cb removed_cb, Pointer user_data) {
		throw new UnsupportedOperationException();
	}

	@Override
	public int libusb_hotplug_register_callback(libusb_context ctx, int events, int flags,
		int vendor_id, int product_id, int dev_class, libusb_hotplug_callback_fn cb_fn,
		Pointer user_data, IntByReference handle) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void libusb_hotplug_deregister_callback(libusb_context ctx, int handle) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Pointer libusb_hotplug_get_user_data(libusb_context ctx, int callback_handle)
		throws LastErrorException {
		throw new UnsupportedOperationException();
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
		int result = bulkTransferOut.apply(List.of(endpoint, ByteProvider.of(bytes)));
		if (actualLength != null) actualLength.setValue(length);
		return result;
	}

}
