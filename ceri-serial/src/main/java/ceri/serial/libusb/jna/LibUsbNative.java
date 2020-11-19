package ceri.serial.libusb.jna;

import java.nio.ByteBuffer;
import com.sun.jna.LastErrorException;
import com.sun.jna.Library;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.PointerByReference;
import ceri.serial.clib.jna.Time.timeval;
import ceri.serial.libusb.jna.LibUsb.libusb_bos_dev_capability_descriptor;
import ceri.serial.libusb.jna.LibUsb.libusb_context;
import ceri.serial.libusb.jna.LibUsb.libusb_device;
import ceri.serial.libusb.jna.LibUsb.libusb_device_descriptor;
import ceri.serial.libusb.jna.LibUsb.libusb_device_handle;
import ceri.serial.libusb.jna.LibUsb.libusb_endpoint_descriptor;
import ceri.serial.libusb.jna.LibUsb.libusb_hotplug_callback_fn;
import ceri.serial.libusb.jna.LibUsb.libusb_pollfd;
import ceri.serial.libusb.jna.LibUsb.libusb_pollfd_added_cb;
import ceri.serial.libusb.jna.LibUsb.libusb_pollfd_removed_cb;
import ceri.serial.libusb.jna.LibUsb.libusb_transfer;
import ceri.serial.libusb.jna.LibUsb.libusb_version;

interface LibUsbNative extends Library {

	// int LIBUSB_CALL libusb_init(libusb_context **ctx);
	int libusb_init(PointerByReference ctx);

	// void LIBUSB_CALL libusb_exit(libusb_context *ctx);
	void libusb_exit(libusb_context ctx);

	// void LIBUSB_CALL libusb_set_debug(libusb_context *ctx, int level);
	void libusb_set_debug(libusb_context ctx, int level);

	// const struct libusb_version * LIBUSB_CALL libusb_get_version(void);
	libusb_version libusb_get_version();

	// int LIBUSB_CALL libusb_has_capability(uint32_t capability);
	int libusb_has_capability(int capability);

	// const char * LIBUSB_CALL libusb_error_name(int errcode);
	String libusb_error_name(int errcode);

	// int LIBUSB_CALL libusb_setlocale(const char *locale);
	int libusb_setlocale(String locale);

	// const char * LIBUSB_CALL libusb_strerror(enum libusb_error errcode);
	String libusb_strerror(int errcode);

	//

	// ssize_t LIBUSB_CALL libusb_get_device_list(libusb_context *ctx, libusb_device ***list);
	int libusb_get_device_list(libusb_context ctx, PointerByReference list)
		throws LastErrorException;

	// void LIBUSB_CALL libusb_free_device_list(libusb_device **list, int unref_devices);
	void libusb_free_device_list(Pointer list, int unref_devices) throws LastErrorException;

	// libusb_device * LIBUSB_CALL libusb_ref_device(libusb_device *dev);
	libusb_device libusb_ref_device(libusb_device dev) throws LastErrorException;

	// void LIBUSB_CALL libusb_unref_device(libusb_device *dev);
	void libusb_unref_device(libusb_device dev) throws LastErrorException;

	//

	// int LIBUSB_CALL libusb_get_configuration(libusb_device_handle *dev, int *config);
	int libusb_get_configuration(libusb_device_handle dev, IntByReference config)
		throws LastErrorException;

	// int LIBUSB_CALL libusb_get_device_descriptor(libusb_device *dev,
	// struct libusb_device_descriptor *desc);
	int libusb_get_device_descriptor(libusb_device dev, libusb_device_descriptor desc)
		throws LastErrorException;

	// int LIBUSB_CALL libusb_get_active_config_descriptor(libusb_device *dev,
	// struct libusb_config_descriptor **config);
	int libusb_get_active_config_descriptor(libusb_device dev, PointerByReference config)
		throws LastErrorException;

	// int LIBUSB_CALL libusb_get_config_descriptor(libusb_device *dev, uint8_t config_index,
	// struct libusb_config_descriptor **config);
	int libusb_get_config_descriptor(libusb_device dev, byte config_index,
		PointerByReference config) throws LastErrorException;

	// int LIBUSB_CALL libusb_get_config_descriptor_by_value(libusb_device *dev,
	// uint8_t bConfigurationValue, struct libusb_config_descriptor **config);
	int libusb_get_config_descriptor_by_value(libusb_device dev, byte bConfigurationValue,
		PointerByReference config) throws LastErrorException;

	// void LIBUSB_CALL libusb_free_config_descriptor(struct libusb_config_descriptor *config);
	void libusb_free_config_descriptor(Pointer config) throws LastErrorException;

	// int LIBUSB_CALL libusb_get_ss_endpoint_companion_descriptor(struct libusb_context *ctx,
	// const struct libusb_endpoint_descriptor *endpoint,
	// struct libusb_ss_endpoint_companion_descriptor **ep_comp);
	int libusb_get_ss_endpoint_companion_descriptor(libusb_context ctx,
		libusb_endpoint_descriptor endpoint, PointerByReference ep_comp) throws LastErrorException;

	// void LIBUSB_CALL libusb_free_ss_endpoint_companion_descriptor(
	// struct libusb_ss_endpoint_companion_descriptor *ep_comp);
	void libusb_free_ss_endpoint_companion_descriptor(Pointer ep_comp) throws LastErrorException;

	// int LIBUSB_CALL libusb_get_bos_descriptor(libusb_device_handle *handle,
	// struct libusb_bos_descriptor **bos);
	int libusb_get_bos_descriptor(libusb_device_handle handle, PointerByReference bos)
		throws LastErrorException;

	// void LIBUSB_CALL libusb_free_bos_descriptor(struct libusb_bos_descriptor *bos);
	void libusb_free_bos_descriptor(Pointer bos) throws LastErrorException;

	// int LIBUSB_CALL libusb_get_usb_2_0_extension_descriptor(struct libusb_context *ctx,
	// struct libusb_bos_dev_capability_descriptor *dev_cap,
	// struct libusb_usb_2_0_extension_descriptor **usb_2_0_extension);
	int libusb_get_usb_2_0_extension_descriptor(libusb_context ctx,
		libusb_bos_dev_capability_descriptor dev_cap, PointerByReference usb_2_0_extension)
		throws LastErrorException;

	// void LIBUSB_CALL libusb_free_usb_2_0_extension_descriptor(
	// struct libusb_usb_2_0_extension_descriptor *usb_2_0_extension);
	void libusb_free_usb_2_0_extension_descriptor(Pointer usb_2_0_extension)
		throws LastErrorException;

	// int LIBUSB_CALL libusb_get_ss_usb_device_capability_descriptor(struct libusb_context *ctx,
	// struct libusb_bos_dev_capability_descriptor *dev_cap,
	// struct libusb_ss_usb_device_capability_descriptor **ss_usb_device_cap);
	int libusb_get_ss_usb_device_capability_descriptor(libusb_context ctx,
		libusb_bos_dev_capability_descriptor dev_cap, PointerByReference ss_usb_device_cap)
		throws LastErrorException;

	// void LIBUSB_CALL libusb_free_ss_usb_device_capability_descriptor(
	// struct libusb_ss_usb_device_capability_descriptor *ss_usb_device_cap);
	void libusb_free_ss_usb_device_capability_descriptor(Pointer ss_usb_device_cap)
		throws LastErrorException;

	// int LIBUSB_CALL libusb_get_container_id_descriptor(struct libusb_context *ctx,
	// struct libusb_bos_dev_capability_descriptor *dev_cap,
	// struct libusb_container_id_descriptor **container_id);
	int libusb_get_container_id_descriptor(libusb_context ctx,
		libusb_bos_dev_capability_descriptor dev_cap, PointerByReference container_id)
		throws LastErrorException;

	// void LIBUSB_CALL libusb_free_container_id_descriptor(
	// struct libusb_container_id_descriptor *container_id);
	void libusb_free_container_id_descriptor(Pointer container_id) throws LastErrorException;

	// uint8_t LIBUSB_CALL libusb_get_bus_number(libusb_device *dev);
	byte libusb_get_bus_number(libusb_device dev) throws LastErrorException;

	// uint8_t LIBUSB_CALL libusb_get_port_number(libusb_device *dev);
	byte libusb_get_port_number(libusb_device dev) throws LastErrorException;

	// int LIBUSB_CALL libusb_get_port_numbers(libusb_device *dev, uint8_t* port_numbers,
	// int port_numbers_len);
	int libusb_get_port_numbers(libusb_device dev, Pointer port_numbers, int port_numbers_len)
		throws LastErrorException;

	// int LIBUSB_CALL libusb_get_port_path(libusb_context *ctx, libusb_device *dev,
	// uint8_t* path, uint8_t path_length);
	int libusb_get_port_path(libusb_context ctx, libusb_device dev, Pointer path, byte path_length)
		throws LastErrorException;

	// libusb_device * LIBUSB_CALL libusb_get_parent(libusb_device *dev);
	libusb_device libusb_get_parent(libusb_device dev) throws LastErrorException;

	// uint8_t LIBUSB_CALL libusb_get_device_address(libusb_device *dev);
	byte libusb_get_device_address(libusb_device dev) throws LastErrorException;

	// int LIBUSB_CALL libusb_get_device_speed(libusb_device *dev);
	int libusb_get_device_speed(libusb_device dev) throws LastErrorException;

	// int LIBUSB_CALL libusb_get_max_packet_size(libusb_device *dev, unsigned char endpoint);
	int libusb_get_max_packet_size(libusb_device dev, byte endpoint) throws LastErrorException;

	// int LIBUSB_CALL libusb_get_max_iso_packet_size(libusb_device *dev, unsigned char endpoint);
	int libusb_get_max_iso_packet_size(libusb_device dev, byte endpoint) throws LastErrorException;

	//

	// int LIBUSB_CALL libusb_open(libusb_device *dev, libusb_device_handle **handle);
	int libusb_open(libusb_device dev, PointerByReference handle) throws LastErrorException;

	// void LIBUSB_CALL libusb_close(libusb_device_handle *dev_handle);
	void libusb_close(libusb_device_handle dev_handle) throws LastErrorException;

	// libusb_device * LIBUSB_CALL libusb_get_device(libusb_device_handle *dev_handle);
	libusb_device libusb_get_device(libusb_device_handle dev_handle) throws LastErrorException;

	//

	// int LIBUSB_CALL libusb_set_configuration(libusb_device_handle *dev, int configuration);
	int libusb_set_configuration(libusb_device_handle dev, int configuration)
		throws LastErrorException;

	// int LIBUSB_CALL libusb_claim_interface(libusb_device_handle *dev, int interface_number);
	int libusb_claim_interface(libusb_device_handle dev, int interface_number)
		throws LastErrorException;

	// int LIBUSB_CALL libusb_release_interface(libusb_device_handle *dev, int interface_number);
	int libusb_release_interface(libusb_device_handle dev, int interface_number)
		throws LastErrorException;

	//

	// libusb_device_handle * LIBUSB_CALL libusb_open_device_with_vid_pid(
	// libusb_context *ctx, uint16_t vendor_id, uint16_t product_id);
	libusb_device_handle libusb_open_device_with_vid_pid(libusb_context ctx, short vendor_id,
		short product_id) throws LastErrorException;

	//

	// int LIBUSB_CALL libusb_set_interface_alt_setting(libusb_device_handle *dev,
	// int interface_number, int alternate_setting);
	int libusb_set_interface_alt_setting(libusb_device_handle dev, int interface_number,
		int alternate_setting) throws LastErrorException;

	// int LIBUSB_CALL libusb_clear_halt(libusb_device_handle *dev, unsigned char endpoint);
	int libusb_clear_halt(libusb_device_handle dev, byte endpoint) throws LastErrorException;

	// int LIBUSB_CALL libusb_reset_device(libusb_device_handle *dev);
	int libusb_reset_device(libusb_device_handle dev) throws LastErrorException;

	//

	// int LIBUSB_CALL libusb_alloc_streams(libusb_device_handle *dev,
	// uint32_t num_streams, unsigned char *endpoints, int num_endpoints);
	int libusb_alloc_streams(libusb_device_handle dev, int num_streams, Pointer endpoints,
		int num_endpoints) throws LastErrorException;

	// int LIBUSB_CALL libusb_free_streams(libusb_device_handle *dev,
	// unsigned char *endpoints, int num_endpoints);
	int libusb_free_streams(libusb_device_handle dev, Pointer endpoints, int num_endpoints)
		throws LastErrorException;

	//

	// int LIBUSB_CALL libusb_kernel_driver_active(libusb_device_handle *dev, int interface_number);
	int libusb_kernel_driver_active(libusb_device_handle dev, int interface_number)
		throws LastErrorException;

	// int LIBUSB_CALL libusb_detach_kernel_driver(libusb_device_handle *dev, int interface_number);
	int libusb_detach_kernel_driver(libusb_device_handle dev, int interface_number)
		throws LastErrorException;

	// int LIBUSB_CALL libusb_attach_kernel_driver(libusb_device_handle *dev, int interface_number);
	int libusb_attach_kernel_driver(libusb_device_handle dev, int interface_number)
		throws LastErrorException;

	// int LIBUSB_CALL libusb_set_auto_detach_kernel_driver(libusb_device_handle *dev, int enable);
	int libusb_set_auto_detach_kernel_driver(libusb_device_handle dev, int enable)
		throws LastErrorException;

	/* async I/O */

	// struct libusb_transfer * LIBUSB_CALL libusb_alloc_transfer(int iso_packets);
	libusb_transfer libusb_alloc_transfer(int iso_packets) throws LastErrorException;

	// int LIBUSB_CALL libusb_submit_transfer(struct libusb_transfer *transfer);
	int libusb_submit_transfer(libusb_transfer transfer) throws LastErrorException;

	// int LIBUSB_CALL libusb_cancel_transfer(struct libusb_transfer *transfer);
	int libusb_cancel_transfer(libusb_transfer transfer) throws LastErrorException;

	// void LIBUSB_CALL libusb_free_transfer(struct libusb_transfer *transfer);
	void libusb_free_transfer(libusb_transfer transfer) throws LastErrorException;

	// void LIBUSB_CALL libusb_transfer_set_stream_id(struct libusb_transfer *transfer, uint32_t
	// stream_id);
	void libusb_transfer_set_stream_id(libusb_transfer transfer, int stream_id)
		throws LastErrorException;

	// uint32_t LIBUSB_CALL libusb_transfer_get_stream_id(struct libusb_transfer *transfer);
	int libusb_transfer_get_stream_id(libusb_transfer transfer) throws LastErrorException;

	/* sync I/O */

	// int LIBUSB_CALL libusb_control_transfer(libusb_device_handle *dev_handle,
	// uint8_t request_type, uint8_t bRequest, uint16_t wValue, uint16_t wIndex,
	// unsigned char *data, uint16_t wLength, unsigned int timeout);
	int libusb_control_transfer(libusb_device_handle dev_handle, byte request_type, byte bRequest,
		short wValue, short wIndex, ByteBuffer data, short wLength, int timeout)
		throws LastErrorException;

	//

	// int LIBUSB_CALL libusb_bulk_transfer(libusb_device_handle *dev_handle,
	// unsigned char endpoint, unsigned char *data, int length, int *actual_length,
	// unsigned int timeout);
	int libusb_bulk_transfer(libusb_device_handle dev_handle, byte endpoint, ByteBuffer data,
		int length, IntByReference actual_length, int timeout) throws LastErrorException;

	//

	// int LIBUSB_CALL libusb_interrupt_transfer(libusb_device_handle *dev_handle,
	// unsigned char endpoint, unsigned char *data, int length, int *actual_length,
	// unsigned int timeout);
	int libusb_interrupt_transfer(libusb_device_handle dev_handle, byte endpoint, ByteBuffer data,
		int length, IntByReference actual_length, int timeout) throws LastErrorException;

	//

	// int LIBUSB_CALL libusb_get_string_descriptor_ascii(libusb_device_handle *dev,
	// uint8_t desc_index, unsigned char *data, int length);
	int libusb_get_string_descriptor_ascii(libusb_device_handle dev, byte desc_index,
		ByteBuffer data, int length) throws LastErrorException;

	/* polling and timeouts */

	// int LIBUSB_CALL libusb_try_lock_events(libusb_context *ctx);
	int libusb_try_lock_events(libusb_context ctx) throws LastErrorException;

	// void LIBUSB_CALL libusb_lock_events(libusb_context *ctx);
	void libusb_lock_events(libusb_context ctx) throws LastErrorException;

	// void LIBUSB_CALL libusb_unlock_events(libusb_context *ctx);
	void libusb_unlock_events(libusb_context ctx) throws LastErrorException;

	// int LIBUSB_CALL libusb_event_handling_ok(libusb_context *ctx);
	int libusb_event_handling_ok(libusb_context ctx) throws LastErrorException;

	// int LIBUSB_CALL libusb_event_handler_active(libusb_context *ctx);
	int libusb_event_handler_active(libusb_context ctx) throws LastErrorException;

	// void LIBUSB_CALL libusb_lock_event_waiters(libusb_context *ctx);
	void libusb_lock_event_waiters(libusb_context ctx) throws LastErrorException;

	// void LIBUSB_CALL libusb_unlock_event_waiters(libusb_context *ctx);
	void libusb_unlock_event_waiters(libusb_context ctx) throws LastErrorException;

	// int LIBUSB_CALL libusb_wait_for_event(libusb_context *ctx, struct timeval *tv);
	int libusb_wait_for_event(libusb_context ctx, timeval tv) throws LastErrorException;

	//

	// int LIBUSB_CALL libusb_handle_events_timeout(libusb_context *ctx, struct timeval *tv);
	int libusb_handle_events_timeout(libusb_context ctx, timeval tv) throws LastErrorException;

	// int LIBUSB_CALL libusb_handle_events_timeout_completed(libusb_context *ctx, struct timeval
	// *tv, int *completed);
	int libusb_handle_events_timeout_completed(libusb_context ctx, timeval tv,
		IntByReference completed) throws LastErrorException;

	// int LIBUSB_CALL libusb_handle_events(libusb_context *ctx);
	int libusb_handle_events(libusb_context ctx) throws LastErrorException;

	// int LIBUSB_CALL libusb_handle_events_completed(libusb_context *ctx, int *completed);
	int libusb_handle_events_completed(libusb_context ctx, IntByReference completed)
		throws LastErrorException;

	// int LIBUSB_CALL libusb_handle_events_locked(libusb_context *ctx, struct timeval *tv);
	int libusb_handle_events_locked(libusb_context ctx, timeval tv) throws LastErrorException;

	// int LIBUSB_CALL libusb_pollfds_handle_timeouts(libusb_context *ctx);
	int libusb_pollfds_handle_timeouts(libusb_context ctx) throws LastErrorException;

	// int LIBUSB_CALL libusb_get_next_timeout(libusb_context *ctx, struct timeval *tv);
	int libusb_get_next_timeout(libusb_context ctx, timeval tv) throws LastErrorException;

	//

	// const struct libusb_pollfd ** LIBUSB_CALL libusb_get_pollfds(libusb_context *ctx);
	libusb_pollfd.ByReference libusb_get_pollfds(libusb_context ctx) throws LastErrorException;

	// void LIBUSB_CALL libusb_free_pollfds(const struct libusb_pollfd **pollfds);
	void libusb_free_pollfds(libusb_pollfd.ByReference pollfds) throws LastErrorException;

	// void LIBUSB_CALL libusb_set_pollfd_notifiers(libusb_context *ctx,
	// libusb_pollfd_added_cb added_cb, libusb_pollfd_removed_cb removed_cb, void *user_data);
	void libusb_set_pollfd_notifiers(libusb_context ctx, libusb_pollfd_added_cb added_cb,
		libusb_pollfd_removed_cb removed_cb, Pointer user_data) throws LastErrorException;

	//

	// typedef int libusb_hotplug_callback_handle;

	// int LIBUSB_CALL libusb_hotplug_register_callback(libusb_context *ctx,
	// libusb_hotplug_event events, libusb_hotplug_flag flags, int vendor_id, int product_id,
	// int dev_class, libusb_hotplug_callback_fn cb_fn, void *user_data,
	// libusb_hotplug_callback_handle *handle);
	int libusb_hotplug_register_callback(libusb_context ctx, int events, int flags, int vendor_id,
		int product_id, int dev_class, libusb_hotplug_callback_fn cb_fn, Pointer user_data,
		IntByReference handle) throws LastErrorException;

	// void LIBUSB_CALL libusb_hotplug_deregister_callback(libusb_context *ctx,
	// libusb_hotplug_callback_handle handle);
	void libusb_hotplug_deregister_callback(libusb_context ctx, int handle)
		throws LastErrorException;

}
