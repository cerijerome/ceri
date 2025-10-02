package ceri.serial.libusb.jna;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Set;
import com.sun.jna.Callback;
import com.sun.jna.Memory;
import com.sun.jna.Platform;
import com.sun.jna.Pointer;
import com.sun.jna.PointerType;
import com.sun.jna.Structure;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.PointerByReference;
import ceri.common.array.ArrayUtil;
import ceri.common.data.ByteUtil;
import ceri.common.data.Xcoder;
import ceri.common.math.Maths;
import ceri.common.util.Basics;
import ceri.jna.clib.jna.CTime;
import ceri.jna.type.ArrayPointer;
import ceri.jna.type.Struct;
import ceri.jna.type.Struct.Fields;
import ceri.jna.type.VarStruct;
import ceri.jna.util.Caller;
import ceri.jna.util.JnaLibrary;
import ceri.jna.util.JnaUtil;
import ceri.jna.util.PointerUtil;

/**
 * Provides types and static function calls. Updated to libusb 1.0.24.
 */
public class LibUsb {
	public static final JnaLibrary<LibUsbNative> library =
		JnaLibrary.of("usb-1.0", LibUsbNative.class);
	public static final Caller<LibUsbException> caller = Caller.of(LibUsbException::full);
	private static final int DEFAULT_TIMEOUT = 1000;
	private static final int MAX_DESCRIPTOR_SIZE = 255;
	private static final int MAX_PORT_NUMBERS = 7;
	// Public constants
	public static final int LIBUSB_API_VERSION = 0x01000108;
	public static final int LIBUSB_ERROR_COUNT = 14;
	public static final int LIBUSB_HOTPLUG_NO_FLAGS = 0;
	public static final int LIBUSB_HOTPLUG_MATCH_ANY = -1;
	// Descriptor sizes per descriptor type
	public static final int LIBUSB_DT_DEVICE_SIZE = 18;
	public static final int LIBUSB_DT_CONFIG_SIZE = 9;
	public static final int LIBUSB_DT_INTERFACE_SIZE = 9;
	public static final int LIBUSB_DT_ENDPOINT_SIZE = 7;
	public static final int LIBUSB_DT_ENDPOINT_AUDIO_SIZE = 9;
	public static final int LIBUSB_DT_HUB_NONVAR_SIZE = 7;
	public static final int LIBUSB_DT_SS_ENDPOINT_COMPANION_SIZE = 6;
	public static final int LIBUSB_DT_BOS_SIZE = 5;
	public static final int LIBUSB_DT_DEVICE_CAPABILITY_SIZE = 3;
	public static final int LIBUSB_CONTROL_SETUP_SIZE = new libusb_control_setup(null).size();
	// BOS descriptor sizes
	public static final int LIBUSB_BT_USB_2_0_EXTENSION_SIZE = 7; // 8 with padding?
	public static final int LIBUSB_BT_SS_USB_DEVICE_CAPABILITY_SIZE = 10;
	public static final int LIBUSB_BT_CONTAINER_ID_SIZE = 20;
	public static final int LIBUSB_DT_BOS_MAX_SIZE =
		LIBUSB_DT_BOS_SIZE + LIBUSB_BT_USB_2_0_EXTENSION_SIZE
			+ LIBUSB_BT_SS_USB_DEVICE_CAPABILITY_SIZE + LIBUSB_BT_CONTAINER_ID_SIZE;
	// Field masks
	public static final int LIBUSB_ENDPOINT_ADDRESS_MASK = 0x0f;
	public static final int LIBUSB_ENDPOINT_DIR_MASK = 0x80;
	public static final int LIBUSB_TRANSFER_TYPE_MASK = 0x03;
	public static final int LIBUSB_BULK_MAX_STREAMS_MASK = 0x1f;
	public static final int LIBUSB_ISO_MULT_MASK = 0x03;
	public static final int LIBUSB_ISO_SYNC_TYPE_MASK = 0x0c;
	public static final int LIBUSB_ISO_USAGE_TYPE_MASK = 0x30;

	private LibUsb() {}

	/**
	 * Device and/or Interface Class codes
	 */
	public static enum libusb_class_code {
		LIBUSB_CLASS_PER_INTERFACE(0x00),
		LIBUSB_CLASS_AUDIO(0x01),
		LIBUSB_CLASS_COMM(0x02),
		LIBUSB_CLASS_HID(0x03),
		LIBUSB_CLASS_PHYSICAL(0x05),
		// LIBUSB_CLASS_PTP(0x06), // legacy name from libusb-0.1
		LIBUSB_CLASS_IMAGE(0x06),
		LIBUSB_CLASS_PRINTER(0x07),
		LIBUSB_CLASS_MASS_STORAGE(0x08),
		LIBUSB_CLASS_HUB(0x09),
		LIBUSB_CLASS_DATA(0x0a),
		LIBUSB_CLASS_SMART_CARD(0x0b),
		LIBUSB_CLASS_CONTENT_SECURITY(0x0d),
		LIBUSB_CLASS_VIDEO(0x0e),
		LIBUSB_CLASS_PERSONAL_HEALTHCARE(0x0f),
		LIBUSB_CLASS_DIAGNOSTIC_DEVICE(0xdc),
		LIBUSB_CLASS_WIRELESS(0xe0),
		LIBUSB_CLASS_MISCELLANEOUS(0xef),
		LIBUSB_CLASS_APPLICATION(0xfe),
		LIBUSB_CLASS_VENDOR_SPEC(0xff);

		public static final Xcoder.Type<libusb_class_code> xcoder =
			Xcoder.type(libusb_class_code.class);
		public final int value;

		libusb_class_code(int value) {
			this.value = value;
		}
	}

	/**
	 * Descriptor types as defined by the USB specification.
	 */
	public static enum libusb_descriptor_type {
		LIBUSB_DT_DEVICE(0x01),
		LIBUSB_DT_CONFIG(0x02),
		LIBUSB_DT_STRING(0x03),
		LIBUSB_DT_INTERFACE(0x04),
		LIBUSB_DT_ENDPOINT(0x05),
		LIBUSB_DT_BOS(0x0f),
		LIBUSB_DT_DEVICE_CAPABILITY(0x10),
		LIBUSB_DT_HID(0x21),
		LIBUSB_DT_REPORT(0x22),
		LIBUSB_DT_PHYSICAL(0x23),
		LIBUSB_DT_HUB(0x29),
		LIBUSB_DT_SUPERSPEED_HUB(0x2a),
		LIBUSB_DT_SS_ENDPOINT_COMPANION(0x30);

		public static final Xcoder.Type<libusb_descriptor_type> xcoder =
			Xcoder.type(libusb_descriptor_type.class);
		public final int value;

		libusb_descriptor_type(int value) {
			this.value = value;
		}
	}

	/**
	 * Endpoint direction. Values for bit 7 of the libusb_endpoint_descriptor.bEndpointAddress
	 * "endpoint address" scheme.
	 */
	public static enum libusb_endpoint_direction {
		/** host-to-device */
		LIBUSB_ENDPOINT_OUT(0x00),
		/** device-to-host */
		LIBUSB_ENDPOINT_IN(0x80);

		public static final Xcoder.Type<libusb_endpoint_direction> xcoder =
			Xcoder.type(libusb_endpoint_direction.class);
		public final int value;

		libusb_endpoint_direction(int value) {
			this.value = value;
		}
	}

	/**
	 * Endpoint transfer type. Values for bits 0:1 of the libusb_endpoint_descriptor.bmAttributes
	 * "endpoint attributes" field.
	 */
	public static enum libusb_endpoint_transfer_type {
		LIBUSB_ENDPOINT_TRANSFER_TYPE_CONTROL(0),
		LIBUSB_ENDPOINT_TRANSFER_TYPE_ISOCHRONOUS(1),
		LIBUSB_ENDPOINT_TRANSFER_TYPE_BULK(2),
		LIBUSB_ENDPOINT_TRANSFER_TYPE_INTERRUPT(3);

		public static final Xcoder.Type<libusb_endpoint_transfer_type> xcoder =
			Xcoder.type(libusb_endpoint_transfer_type.class);
		public final int value;

		libusb_endpoint_transfer_type(int value) {
			this.value = value;
		}
	}

	/**
	 * Standard requests, as defined in table 9-5 of the USB 3.0 specifications
	 */
	public static enum libusb_standard_request {
		LIBUSB_REQUEST_GET_STATUS(0x00),
		LIBUSB_REQUEST_CLEAR_FEATURE(0x01),
		// 0x02 is reserved
		LIBUSB_REQUEST_SET_FEATURE(0x03),
		// 0x04 is reserved
		LIBUSB_REQUEST_SET_ADDRESS(0x05),
		LIBUSB_REQUEST_GET_DESCRIPTOR(0x06),
		LIBUSB_REQUEST_SET_DESCRIPTOR(0x07),
		LIBUSB_REQUEST_GET_CONFIGURATION(0x08),
		LIBUSB_REQUEST_SET_CONFIGURATION(0x09),
		LIBUSB_REQUEST_GET_INTERFACE(0x0a),
		LIBUSB_REQUEST_SET_INTERFACE(0x0b),
		LIBUSB_REQUEST_SYNCH_FRAME(0x0c),
		LIBUSB_REQUEST_SET_SEL(0x30),
		LIBUSB_REQUEST_SET_ISOCH_DELAY(0x31); // LIBUSB_SET_ISOCH_DELAY

		public static final Xcoder.Type<libusb_standard_request> xcoder =
			Xcoder.type(libusb_standard_request.class);
		public final int value;

		libusb_standard_request(int value) {
			this.value = value;
		}
	}

	/**
	 * Request type bits of the libusb_control_setup.bmRequestType "bmRequestType" field in control
	 * transfers.
	 */
	public static enum libusb_request_type {
		LIBUSB_REQUEST_TYPE_STANDARD(0x00 << 5),
		LIBUSB_REQUEST_TYPE_CLASS(0x01 << 5),
		LIBUSB_REQUEST_TYPE_VENDOR(0x02 << 5),
		LIBUSB_REQUEST_TYPE_RESERVED(0x03 << 5);

		public static final Xcoder.Type<libusb_request_type> xcoder =
			Xcoder.type(libusb_request_type.class);
		public final int value;

		libusb_request_type(int value) {
			this.value = value;
		}
	}

	/**
	 * Recipient bits of the libusb_control_setup.bmRequestType "bmRequestType" field in control
	 * transfers. Values 4 through 31 are reserved.
	 */
	public static enum libusb_request_recipient {
		LIBUSB_RECIPIENT_DEVICE(0x00),
		LIBUSB_RECIPIENT_INTERFACE(0x01),
		LIBUSB_RECIPIENT_ENDPOINT(0x02),
		LIBUSB_RECIPIENT_OTHER(0x03);

		public static final Xcoder.Type<libusb_request_recipient> xcoder =
			Xcoder.type(libusb_request_recipient.class);
		public final int value;

		libusb_request_recipient(int value) {
			this.value = value;
		}
	}

	/**
	 * Synchronization type for isochronous endpoints. Values for bits 2:3 of the
	 * libusb_endpoint_descriptor.bmAttributes "bmAttributes" field in libusb_endpoint_descriptor.
	 */
	public static enum libusb_iso_sync_type {
		LIBUSB_ISO_SYNC_TYPE_NONE(0),
		LIBUSB_ISO_SYNC_TYPE_ASYNC(1),
		LIBUSB_ISO_SYNC_TYPE_ADAPTIVE(2),
		LIBUSB_ISO_SYNC_TYPE_SYNC(3);

		public static final Xcoder.Type<libusb_iso_sync_type> xcoder =
			Xcoder.type(libusb_iso_sync_type.class);
		public final int value;

		libusb_iso_sync_type(int value) {
			this.value = value;
		}
	}

	/**
	 * Usage type for isochronous endpoints. Values for bits 4:5 of the
	 * libusb_endpoint_descriptor.bmAttributes "bmAttributes" field in libusb_endpoint_descriptor.
	 */
	public static enum libusb_iso_usage_type {
		LIBUSB_ISO_USAGE_TYPE_DATA(0),
		LIBUSB_ISO_USAGE_TYPE_FEEDBACK(1),
		LIBUSB_ISO_USAGE_TYPE_IMPLICIT(2);

		public static final Xcoder.Type<libusb_iso_usage_type> xcoder =
			Xcoder.type(libusb_iso_usage_type.class);
		public final int value;

		libusb_iso_usage_type(int value) {
			this.value = value;
		}
	}

	/**
	 * Supported speeds (wSpeedSupported) bitfield. Indicates what speeds the device supports.
	 */
	public static enum libusb_supported_speed {
		LIBUSB_LOW_SPEED_OPERATION(1 << 0), // 1.5 Mbit/s
		LIBUSB_FULL_SPEED_OPERATION(1 << 1), // 12 Mbit/s
		LIBUSB_HIGH_SPEED_OPERATION(1 << 2), // 480 Mbit/s
		LIBUSB_SUPER_SPEED_OPERATION(1 << 3); // 5000 Mbit/s

		public static final Xcoder.Types<libusb_supported_speed> xcoder =
			Xcoder.types(libusb_supported_speed.class);
		public final int value;

		libusb_supported_speed(int value) {
			this.value = value;
		}
	}

	/**
	 * Masks for the bits of the libusb_usb_2_0_extension_descriptor.bmAttributes "bmAttributes"
	 * field of the USB 2.0 Extension descriptor.
	 */
	public static enum libusb_usb_2_0_extension_attributes {
		LIBUSB_BM_LPM_SUPPORT(1 << 1);

		public static final Xcoder.Types<libusb_usb_2_0_extension_attributes> xcoder =
			Xcoder.types(libusb_usb_2_0_extension_attributes.class);
		public final int value;

		libusb_usb_2_0_extension_attributes(int value) {
			this.value = value;
		}
	}

	/**
	 * Masks for the bits of the libusb_ss_usb_device_capability_descriptor.bmAttributes
	 * "bmAttributes" field field of the SuperSpeed USB Device Capability descriptor.
	 */
	public static enum libusb_ss_usb_device_capability_attributes {
		LIBUSB_BM_LTM_SUPPORT(1 << 1);

		public static final Xcoder.Types<libusb_ss_usb_device_capability_attributes> xcoder =
			Xcoder.types(libusb_ss_usb_device_capability_attributes.class);
		public final int value;

		libusb_ss_usb_device_capability_attributes(int value) {
			this.value = value;
		}
	}

	/**
	 * USB capability types
	 */
	public static enum libusb_bos_type {
		LIBUSB_BT_WIRELESS_USB_DEVICE_CAPABILITY(1),
		LIBUSB_BT_USB_2_0_EXTENSION(2),
		LIBUSB_BT_SS_USB_DEVICE_CAPABILITY(3),
		LIBUSB_BT_CONTAINER_ID(4);

		public static final Xcoder.Type<libusb_bos_type> xcoder =
			Xcoder.type(libusb_bos_type.class);
		public final int value;

		libusb_bos_type(int value) {
			this.value = value;
		}
	}

	/**
	 * Speed codes. Indicates the speed at which the device is operating.
	 */
	public static enum libusb_speed {
		LIBUSB_SPEED_UNKNOWN(0),
		LIBUSB_SPEED_LOW(1), // 1.5 Mbit/s
		LIBUSB_SPEED_FULL(2), // 12 Mbit/s
		LIBUSB_SPEED_HIGH(3), // 480 Mbit/s
		LIBUSB_SPEED_SUPER(4), // 5000 Mbit/s
		LIBUSB_SPEED_SUPER_PLUS(5); // 10000 Mbit/s

		public static final Xcoder.Type<libusb_speed> xcoder = Xcoder.type(libusb_speed.class);
		public final int value;

		libusb_speed(int value) {
			this.value = value;
		}
	}

	/**
	 * Error codes. Most libusb functions return 0 on success or one of these codes on failure. You
	 * can call libusb_error_name() to retrieve a string representation of an error code or
	 * libusb_strerror() to get an end-user suitable description of an error code.
	 */
	public static enum libusb_error {
		LIBUSB_SUCCESS(0),
		LIBUSB_ERROR_IO(-1),
		LIBUSB_ERROR_INVALID_PARAM(-2),
		LIBUSB_ERROR_ACCESS(-3),
		LIBUSB_ERROR_NO_DEVICE(-4),
		LIBUSB_ERROR_NOT_FOUND(-5),
		LIBUSB_ERROR_BUSY(-6),
		LIBUSB_ERROR_TIMEOUT(-7),
		LIBUSB_ERROR_OVERFLOW(-8),
		LIBUSB_ERROR_PIPE(-9),
		LIBUSB_ERROR_INTERRUPTED(-10),
		LIBUSB_ERROR_NO_MEM(-11),
		LIBUSB_ERROR_NOT_SUPPORTED(-12),
		LIBUSB_ERROR_OTHER(-99);

		public static final Xcoder.Type<libusb_error> xcoder = Xcoder.type(libusb_error.class);
		public final int value;

		libusb_error(int value) {
			this.value = value;
		}
	}

	/**
	 * Transfer type
	 */
	public static enum libusb_transfer_type {
		LIBUSB_TRANSFER_TYPE_CONTROL(0),
		LIBUSB_TRANSFER_TYPE_ISOCHRONOUS(1),
		LIBUSB_TRANSFER_TYPE_BULK(2),
		LIBUSB_TRANSFER_TYPE_INTERRUPT(3),
		LIBUSB_TRANSFER_TYPE_BULK_STREAM(4);

		public static final Xcoder.Type<libusb_transfer_type> xcoder =
			Xcoder.type(libusb_transfer_type.class);
		public final int value;

		libusb_transfer_type(int value) {
			this.value = value;
		}
	}

	/**
	 * Transfer status codes
	 */
	public static enum libusb_transfer_status {
		LIBUSB_TRANSFER_COMPLETED(0),
		LIBUSB_TRANSFER_ERROR(1),
		LIBUSB_TRANSFER_TIMED_OUT(2),
		LIBUSB_TRANSFER_CANCELLED(3),
		LIBUSB_TRANSFER_STALL(4),
		LIBUSB_TRANSFER_NO_DEVICE(5),
		LIBUSB_TRANSFER_OVERFLOW(6);

		public static final Xcoder.Type<libusb_transfer_status> xcoder =
			Xcoder.type(libusb_transfer_status.class);
		public final int value;

		libusb_transfer_status(int value) {
			this.value = value;
		}
	}

	/**
	 * Transfer flag values
	 */
	public static enum libusb_transfer_flags {
		LIBUSB_TRANSFER_SHORT_NOT_OK(1),
		LIBUSB_TRANSFER_FREE_BUFFER(1 << 1),
		LIBUSB_TRANSFER_FREE_TRANSFER(1 << 2),
		LIBUSB_TRANSFER_ADD_ZERO_PACKET(1 << 3);

		public static final Xcoder.Types<libusb_transfer_flags> xcoder =
			Xcoder.types(libusb_transfer_flags.class);
		public final int value;

		libusb_transfer_flags(int value) {
			this.value = value;
		}
	}

	/**
	 * Capabilities supported by an instance of libusb on the current running platform. Test if the
	 * loaded library supports a given capability by calling libusb_has_capability().
	 */
	public static enum libusb_capability {
		LIBUSB_CAP_HAS_CAPABILITY(0x0000),
		LIBUSB_CAP_HAS_HOTPLUG(0x0001),
		LIBUSB_CAP_HAS_HID_ACCESS(0x0100),
		LIBUSB_CAP_SUPPORTS_DETACH_KERNEL_DRIVER(0x0101);

		public static final Xcoder.Types<libusb_capability> xcoder =
			Xcoder.types(libusb_capability.class);
		public final int value;

		libusb_capability(int value) {
			this.value = value;
		}
	}

	/**
	 * Log message levels.
	 */
	public static enum libusb_log_level {
		LIBUSB_LOG_LEVEL_NONE(0),
		LIBUSB_LOG_LEVEL_ERROR(1),
		LIBUSB_LOG_LEVEL_WARNING(2),
		LIBUSB_LOG_LEVEL_INFO(3),
		LIBUSB_LOG_LEVEL_DEBUG(4);

		public static final Xcoder.Type<libusb_log_level> xcoder =
			Xcoder.type(libusb_log_level.class);
		public final int value;

		libusb_log_level(int value) {
			this.value = value;
		}
	}

	/**
	 * libusb_lib Log callback mode for libusb_set_log_cb().
	 */
	public static enum libusb_log_cb_mode {
		/** Callback function handling all log mesages. */
		LIBUSB_LOG_CB_GLOBAL(1 << 0),
		/** Callback function handling context related log mesages. */
		LIBUSB_LOG_CB_CONTEXT(1 << 1);

		public static final Xcoder.Types<libusb_log_cb_mode> xcoder =
			Xcoder.types(libusb_log_cb_mode.class);
		public final int value;

		libusb_log_cb_mode(int value) {
			this.value = value;
		}
	}

	/**
	 * Since version 1.0.16, LIBUSB_API_VERSION >= 0x01000102 Hotplug events
	 */
	public static enum libusb_hotplug_event {
		LIBUSB_HOTPLUG_EVENT_DEVICE_ARRIVED(1 << 0),
		LIBUSB_HOTPLUG_EVENT_DEVICE_LEFT(1 << 1);

		public static final Xcoder.Types<libusb_hotplug_event> xcoder =
			Xcoder.types(libusb_hotplug_event.class);
		public final int value;

		libusb_hotplug_event(int value) {
			this.value = value;
		}
	}

	/**
	 * Since version 1.0.16, LIBUSB_API_VERSION >= 0x01000102 Flags for hotplug events
	 */
	public static enum libusb_hotplug_flag {
		LIBUSB_HOTPLUG_ENUMERATE(1 << 0);

		public static final Xcoder.Types<libusb_hotplug_flag> xcoder =
			Xcoder.types(libusb_hotplug_flag.class);
		public final int value;

		libusb_hotplug_flag(int value) {
			this.value = value;
		}
	}

	/**
	 * Available option values for libusb_set_option().
	 */
	public static enum libusb_option {
		/** Set log message verbosity; pass libusb_log_level */
		LIBUSB_OPTION_LOG_LEVEL(0),
		/** Use UsbDk backend; Windows only */
		LIBUSB_OPTION_USE_USBDK(1),
		/** Skip scan devices in libusb_init; valid for Linux/Android */
		LIBUSB_OPTION_WEAK_AUTHORITY(2);

		public static final Xcoder.Type<libusb_option> xcoder = Xcoder.type(libusb_option.class);
		public final int value;

		libusb_option(int value) {
			this.value = value;
		}
	}

	/**
	 * Configuration descriptor attributes (not in libusb.h).
	 */
	public static enum libusb_config_attributes {
		LIBUSB_CA_REMOTE_WAKEUP(0x20),
		LIBUSB_CA_SELF_POWERED(0x40),
		LIBUSB_CA_RESERVED1(0x80);

		public static final Xcoder.Types<libusb_config_attributes> xcoder =
			Xcoder.types(libusb_config_attributes.class);
		public final int value;

		libusb_config_attributes(int value) {
			this.value = value;
		}
	}

	/**
	 * A structure representing the standard USB device descriptor. This descriptor is documented in
	 * section 9.6.1 of the USB 3.0 specification. All multiple-byte fields are represented in
	 * host-endian format.
	 */
	@Fields({ "bLength", "bDescriptorType", "bcdUSB", "bDeviceClass", "bDeviceSubClass",
		"bDeviceProtocol", "bMaxPacketSize0", "idVendor", "idProduct", "bcdDevice", "iManufacturer",
		"iProduct", "iSerialNumber", "bNumConfigurations" })
	public static class libusb_device_descriptor extends Struct {
		public static final libusb_descriptor_type TYPE = libusb_descriptor_type.LIBUSB_DT_DEVICE;
		public byte bLength = LIBUSB_DT_DEVICE_SIZE;
		public byte bDescriptorType = (byte) TYPE.value;
		public short bcdUSB;
		public byte bDeviceClass; // libusb_class_code
		public byte bDeviceSubClass;
		public byte bDeviceProtocol;
		public byte bMaxPacketSize0;
		public short idVendor;
		public short idProduct;
		public short bcdDevice;
		public byte iManufacturer;
		public byte iProduct;
		public byte iSerialNumber;
		public byte bNumConfigurations;

		public libusb_device_descriptor(Pointer p) {
			super(p);
		}

		public libusb_class_code bDeviceClass() {
			return libusb_class_code.xcoder.decode(Maths.ubyte(bDeviceClass));
		}
	}

	/**
	 * A structure representing the standard USB endpoint descriptor. This descriptor is documented
	 * in section 9.6.6 of the USB 3.0 specification. All multiple-byte fields are represented in
	 * host-endian format.
	 */
	@Fields({ "bLength", "bDescriptorType", "bEndpointAddress", "bmAttributes", "wMaxPacketSize",
		"bInterval", "bRefresh", "bSynchAddress", "extra", "extra_length" })
	public static class libusb_endpoint_descriptor extends Struct {
		public static final libusb_descriptor_type TYPE = libusb_descriptor_type.LIBUSB_DT_ENDPOINT;
		public byte bLength = LIBUSB_DT_ENDPOINT_SIZE; // or LIBUSB_DT_ENDPOINT_AUDIO_SIZE
		public byte bDescriptorType = (byte) TYPE.value;
		// bits 0:3 endpoint number, 4:6 reserved, 7 direction (libusb_endpoint_direction)
		public byte bEndpointAddress;
		// bits 0:1 libusb_transfer_type, 2:3 libusb_iso_sync_type (iso only)
		// 4:5 libusb_iso_usage_type (both iso only) 6:7 reserved
		public byte bmAttributes;
		public short wMaxPacketSize;
		public byte bInterval;
		public byte bRefresh; // audio only
		public byte bSynchAddress; // audio only
		public Pointer extra;
		public int extra_length;

		public static class ByRef extends libusb_endpoint_descriptor
			implements Structure.ByReference {}

		public int bEndpointNumber() {
			return bEndpointAddress & LIBUSB_ENDPOINT_ADDRESS_MASK;
		}

		public libusb_endpoint_direction bEndpointDirection() {
			return libusb_endpoint_direction.xcoder
				.decode(bEndpointAddress & LIBUSB_ENDPOINT_DIR_MASK);
		}

		public libusb_transfer_type bmAttributesTransferType() {
			return libusb_transfer_type.xcoder.decode(bmAttributes & LIBUSB_TRANSFER_TYPE_MASK);
		}

		public libusb_iso_sync_type bmAttributesIsoSyncType() {
			return libusb_iso_sync_type.xcoder
				.decode((bmAttributes & LIBUSB_ISO_SYNC_TYPE_MASK) >>> 2);
		}

		public libusb_iso_usage_type bmAttributesIsoUsageType() {
			return libusb_iso_usage_type.xcoder
				.decode((bmAttributes & LIBUSB_ISO_USAGE_TYPE_MASK) >>> 4);
		}

		public byte[] extra() {
			return JnaUtil.bytes(extra, 0, extra_length);
		}
	}

	/**
	 * A structure representing the standard USB interface descriptor. This descriptor is documented
	 * in section 9.6.5 of the USB 3.0 specification. All multiple-byte fields are represented in
	 * host-endian format.
	 */
	@Fields({ "bLength", "bDescriptorType", "bInterfaceNumber", "bAlternateSetting",
		"bNumEndpoints", "bInterfaceClass", "bInterfaceSubClass", "bInterfaceProtocol",
		"iInterface", "endpoint", "extra", "extra_length" })
	public static class libusb_interface_descriptor extends Struct {
		public static final libusb_descriptor_type TYPE =
			libusb_descriptor_type.LIBUSB_DT_INTERFACE;
		public byte bLength = LIBUSB_DT_INTERFACE_SIZE;
		public byte bDescriptorType = (byte) TYPE.value;
		public byte bInterfaceNumber;
		public byte bAlternateSetting;
		public byte bNumEndpoints;
		public byte bInterfaceClass; // libusb_class_code
		public byte bInterfaceSubClass;
		public byte bInterfaceProtocol;
		public byte iInterface;
		public libusb_endpoint_descriptor.ByRef endpoint;
		public Pointer extra;
		public int extra_length;

		public static class ByRef extends libusb_interface_descriptor
			implements Structure.ByReference {}

		public libusb_class_code bInterfaceClass() {
			return libusb_class_code.xcoder.decode(Maths.ubyte(bInterfaceClass));
		}

		public libusb_endpoint_descriptor[] endpoints() {
			return arrayOfVal(endpoint, libusb_endpoint_descriptor[]::new,
				Maths.ubyte(bNumEndpoints));
		}

		public byte[] extra() {
			return JnaUtil.bytes(extra, 0, extra_length);
		}
	}

	/**
	 * A collection of alternate settings for a particular USB interface.
	 */
	@Fields({ "altsetting", "num_altsetting" })
	public static class libusb_interface extends Struct {
		public libusb_interface_descriptor.ByRef altsetting;
		public int num_altsetting;

		public static class ByRef extends libusb_interface implements Structure.ByReference {}

		public libusb_interface_descriptor[] altsettings() {
			return arrayOfVal(altsetting, libusb_interface_descriptor[]::new, num_altsetting);
		}
	}

	/**
	 * A structure representing the standard USB configuration descriptor. This descriptor is
	 * documented in section 9.6.3 of the USB 3.0 specification. All multiple-byte fields are
	 * represented in host-endian format.
	 */
	@Fields({ "bLength", "bDescriptorType", "wTotalLength", "bNumInterfaces", "bConfigurationValue",
		"iConfiguration", "bmAttributes", "bMaxPower", "interfaces", "extra", "extra_length" })
	public static class libusb_config_descriptor extends Struct {
		public static final libusb_descriptor_type TYPE = libusb_descriptor_type.LIBUSB_DT_CONFIG;
		public byte bLength = LIBUSB_DT_CONFIG_SIZE;
		public byte bDescriptorType = (byte) TYPE.value;
		public short wTotalLength;
		public byte bNumInterfaces;
		public byte bConfigurationValue;
		public byte iConfiguration;
		public byte bmAttributes; // libusb_config_attributes
		public byte bMaxPower;
		public libusb_interface.ByRef interfaces;
		public Pointer extra;
		public int extra_length;

		public libusb_config_descriptor(Pointer p) {
			super(p);
		}

		public Set<libusb_config_attributes> bmAttributes() {
			return libusb_config_attributes.xcoder.decodeAll(
				Maths.ubyte(bmAttributes) & ~libusb_config_attributes.LIBUSB_CA_RESERVED1.value);
		}

		public libusb_interface[] interfaces() {
			return arrayOfVal(interfaces, libusb_interface[]::new, Maths.ubyte(bNumInterfaces));
		}

		public byte[] extra() {
			return JnaUtil.bytes(extra, 0, extra_length);
		}
	}

	/**
	 * A structure representing the superspeed endpoint companion descriptor. This descriptor is
	 * documented in section 9.6.7 of the USB 3.0 specification. All multiple-byte fields are
	 * represented in host-endian format.
	 */
	@Fields({ "bLength", "bDescriptorType", "bMaxBurst", "bmAttributes", "wBytesPerInterval" })
	public static class libusb_ss_endpoint_companion_descriptor extends Struct {
		public static final libusb_descriptor_type TYPE =
			libusb_descriptor_type.LIBUSB_DT_SS_ENDPOINT_COMPANION;
		public byte bLength = LIBUSB_DT_SS_ENDPOINT_COMPANION_SIZE;
		public byte bDescriptorType = (byte) TYPE.value;
		public byte bMaxBurst;
		public byte bmAttributes; // bulk: bits 0:4 max number of streams, iso: bits 0:1 Mult
		public short wBytesPerInterval;

		public libusb_ss_endpoint_companion_descriptor(Pointer p) {
			super(p);
		}

		public int bmAttributesBulkMaxStreams() {
			return bmAttributes & LIBUSB_BULK_MAX_STREAMS_MASK;
		}

		public int bmAttributesIsoMult() {
			return bmAttributes & LIBUSB_ISO_MULT_MASK;
		}
	}

	/**
	 * A generic representation of a BOS Device Capability descriptor. It is advised to check
	 * bDevCapabilityType and call the matching libusb_get_*_descriptor function to get a structure
	 * fully matching the type.
	 */
	@Fields({ "bLength", "bDescriptorType", "bDevCapabilityType", "dev_capability_data" })
	public static class libusb_bos_dev_capability_descriptor extends VarStruct {
		public static final libusb_descriptor_type TYPE =
			libusb_descriptor_type.LIBUSB_DT_DEVICE_CAPABILITY;
		public byte bLength = LIBUSB_DT_DEVICE_CAPABILITY_SIZE;
		public byte bDescriptorType = (byte) TYPE.value;
		public byte bDevCapabilityType; // libusb_bos_type
		public byte[] dev_capability_data = new byte[0];

		public static class ByRef extends libusb_bos_dev_capability_descriptor
			implements Structure.ByReference {
			public ByRef(Pointer p) {
				super(p);
			}
		}

		public libusb_bos_dev_capability_descriptor(Pointer p) {
			super(p);
		}

		public libusb_bos_type bDevCapabilityType() {
			return libusb_bos_type.xcoder.decode(Maths.ubyte(bDevCapabilityType));
		}

		@Override
		protected void setVarArray(int count) {
			dev_capability_data = new byte[count];
		}

		@Override
		protected int varCount() {
			return Maths.ubyte(bLength) - LIBUSB_DT_DEVICE_CAPABILITY_SIZE;
		}
	}

	/**
	 * A structure representing the Binary Device Object Store (BOS) descriptor. This descriptor is
	 * documented in section 9.6.2 of the USB 3.0 specification. All multiple-byte fields are
	 * represented in host-endian format.
	 */
	@Fields({ "bLength", "bDescriptorType", "wTotalLength", "bNumDeviceCaps", "dev_capability" })
	public static class libusb_bos_descriptor extends VarStruct {
		public static final libusb_descriptor_type TYPE = libusb_descriptor_type.LIBUSB_DT_BOS;
		public byte bLength = (byte) LIBUSB_DT_BOS_SIZE;
		public byte bDescriptorType = (byte) TYPE.value;
		public short wTotalLength;
		public byte bNumDeviceCaps;
		public libusb_bos_dev_capability_descriptor.ByRef[] dev_capability =
			new libusb_bos_dev_capability_descriptor.ByRef[0];

		public libusb_bos_descriptor(Pointer p) {
			super(p);
		}

		@Override
		protected void setVarArray(int count) {
			dev_capability = new libusb_bos_dev_capability_descriptor.ByRef[count];
		}

		@Override
		protected int varCount() {
			return Maths.ubyte(bNumDeviceCaps);
		}
	}

	/**
	 * A structure representing the USB 2.0 Extension descriptor This descriptor is documented in
	 * section 9.6.2.1 of the USB 3.0 specification. All multiple-byte fields are represented in
	 * host-endian format.
	 */
	@Fields({ "bLength", "bDescriptorType", "bDevCapabilityType", "bmAttributes" })
	public static class libusb_usb_2_0_extension_descriptor extends Struct {
		public static final libusb_descriptor_type TYPE =
			libusb_descriptor_type.LIBUSB_DT_DEVICE_CAPABILITY;
		public static final libusb_bos_type BOS_TYPE = libusb_bos_type.LIBUSB_BT_USB_2_0_EXTENSION;
		public byte bLength = LIBUSB_BT_USB_2_0_EXTENSION_SIZE;
		public byte bDescriptorType = (byte) TYPE.value;
		public byte bDevCapabilityType = (byte) BOS_TYPE.value;
		public int bmAttributes; // libusb_usb_2_0_extension_attributes

		public libusb_usb_2_0_extension_descriptor(Pointer p) {
			super(p);
		}

		public Set<libusb_usb_2_0_extension_attributes> bmAttributes() {
			return libusb_usb_2_0_extension_attributes.xcoder.decodeAll(bmAttributes);
		}
	}

	/**
	 * A structure representing the SuperSpeed USB Device Capability descriptor This descriptor is
	 * documented in section 9.6.2.2 of the USB 3.0 specification. All multiple-byte fields are
	 * represented in host-endian format.
	 */
	@Fields({ "bLength", "bDescriptorType", "bDevCapabilityType", "bmAttributes", "wSpeedSupported",
		"bFunctionalitySupport", "bU1DevExitLat", "wU2DevExitLat" })
	public static class libusb_ss_usb_device_capability_descriptor extends Struct {
		public static final libusb_descriptor_type TYPE =
			libusb_descriptor_type.LIBUSB_DT_DEVICE_CAPABILITY;
		public static final libusb_bos_type BOS_TYPE =
			libusb_bos_type.LIBUSB_BT_SS_USB_DEVICE_CAPABILITY;
		public byte bLength = LIBUSB_BT_SS_USB_DEVICE_CAPABILITY_SIZE;
		public byte bDescriptorType = (byte) TYPE.value;
		public byte bDevCapabilityType = (byte) BOS_TYPE.value;
		public byte bmAttributes; // libusb_ss_usb_device_capability_attributes
		public short wSpeedSupported; // libusb_supported_speed
		public byte bFunctionalitySupport;
		public byte bU1DevExitLat;
		public short wU2DevExitLat;

		public libusb_ss_usb_device_capability_descriptor(Pointer p) {
			super(p);
		}

		public Set<libusb_ss_usb_device_capability_attributes> bmAttributes() {
			return libusb_ss_usb_device_capability_attributes.xcoder
				.decodeAll(Maths.ubyte(bmAttributes));
		}

		public Set<libusb_supported_speed> wSpeedSupported() {
			return libusb_supported_speed.xcoder.decodeAll(Maths.ushort(wSpeedSupported));
		}
	}

	/**
	 * A structure representing the Container ID descriptor. This descriptor is documented in
	 * section 9.6.2.3 of the USB 3.0 specification. All multiple-byte fields, except UUIDs, are
	 * represented in host-endian format.
	 */
	@Fields({ "bLength", "bDescriptorType", "bDevCapabilityType", "bReserved", "ContainerID" })
	public static class libusb_container_id_descriptor extends Struct {
		public static final libusb_descriptor_type TYPE =
			libusb_descriptor_type.LIBUSB_DT_DEVICE_CAPABILITY;
		public static final libusb_bos_type BOS_TYPE = libusb_bos_type.LIBUSB_BT_CONTAINER_ID;
		public byte bLength = LIBUSB_BT_CONTAINER_ID_SIZE;
		public byte bDescriptorType = (byte) TYPE.value;
		public byte bDevCapabilityType = (byte) BOS_TYPE.value;
		public byte bReserved;
		public byte[] ContainerID = new byte[16]; // UUID

		public libusb_container_id_descriptor(Pointer p) {
			super(p);
		}
	}

	/**
	 * Setup packet for control transfers.
	 */
	@Fields({ "bmRequestType", "bRequest", "wValue", "wIndex", "wLength" })
	public static class libusb_control_setup extends Struct {
		// bits 0:4 libusb_request_recipient
		// bits 5:6 libusb_request_type
		// bit 7 libusb_endpoint_direction
		public byte bmRequestType;
		// if libusb_request_type.LIBUSB_REQUEST_TYPE_STANDARD,
		// libusb_standard_request, otherwise app-specific
		public byte bRequest;
		public short wValue; // always little-endian, use libusb_cpu_to_le16/libusb_le16_to_cpu
		public short wIndex; // always little-endian, use libusb_cpu_to_le16/libusb_le16_to_cpu
		public short wLength; // always little-endian, use libusb_cpu_to_le16/libusb_le16_to_cpu

		public libusb_control_setup(Pointer p) {
			super(p, Basics.ternary(Platform.isWindows(), Align.none, Align.platform));
		}
	}

	/**
	 * Structure providing the version of the libusb runtime.
	 */
	@Fields({ "major", "minor", "micro", "nano", "rc", "describe" })
	public static class libusb_version extends Struct {
		public short major;
		public short minor;
		public short micro;
		public short nano;
		/** Release candidate suffix */
		public String rc;
		/** For ABI compatibility */
		public String describe;
	}

	/**
	 * Structure representing a libusb session. The concept of individual libusb sessions allows for
	 * your program to use two libraries (or dynamically load two modules) which both independently
	 * use libusb. This will prevent interference between the individual libusb users - for example
	 * libusb_set_debug() will not affect the other user of the library, and libusb_exit() will not
	 * destroy resources that the other user is still using. Sessions are created by libusb_init()
	 * and destroyed through libusb_exit(). If your application is guaranteed to only ever include a
	 * single libusb user (i.e. you), you do not have to worry about contexts: pass NULL in every
	 * function call where a context is required. The default context will be used.
	 */
	// typedef struct libusb_context libusb_context;
	public static class libusb_context extends PointerType {}

	/**
	 * Structure representing a USB device detected on the system. This is an opaque type for which
	 * you are only ever provided with a pointer, usually originating from libusb_get_device_list().
	 * Certain operations can be performed on a device, but in order to do any I/O you will have to
	 * first obtain a device handle using libusb_open(). Devices are reference counted with
	 * libusb_ref_device() and libusb_unref_device(), and are freed when the reference count reaches
	 * 0. New devices presented by libusb_get_device_list() have a reference count of 1, and
	 * libusb_free_device_list() can optionally decrease the reference count on all devices in the
	 * list. libusb_open() adds another reference which is later destroyed by libusb_close().
	 */
	// typedef struct libusb_device libusb_device;
	public static class libusb_device extends PointerType {}

	/**
	 * Structure representing a handle on a USB device. This is an opaque type for which you are
	 * only ever provided with a pointer, usually originating from libusb_open(). A device handle is
	 * used to perform I/O and other operations. When finished with a device handle, you should call
	 * libusb_close().
	 */
	// typedef struct libusb_device_handle libusb_device_handle;
	public static class libusb_device_handle extends PointerType {}

	/**
	 * Isochronous packet descriptor.
	 */
	@Fields({ "length", "actual_length", "status" })
	public static class libusb_iso_packet_descriptor extends Struct {
		public int length;
		public int actual_length;
		public int status; // libusb_transfer_status

		public libusb_iso_packet_descriptor(Pointer p) {
			super(p);
		}

		public libusb_transfer_status status() {
			return libusb_transfer_status.xcoder.decode(status);
		}
	}

	/**
	 * Asynchronous transfer callback function type. When submitting asynchronous transfers, you
	 * pass a pointer to a callback function of this type via the libusb_transfer.callback
	 * "callback" member of the libusb_transfer structure. libusb will call this function later,
	 * when the transfer has completed or failed.
	 */
	// typedef void (LIBUSB_CALL *libusb_transfer_cb_fn)(struct libusb_transfer *transfer);
	public interface libusb_transfer_cb_fn extends Callback {
		void invoke(Pointer transfer);

		/**
		 * Call to read modified fields on callback.
		 */
		static libusb_transfer read(libusb_transfer transfer) {
			for (int i = 0; i < transfer.num_iso_packets; i++)
				Struct.read(transfer.iso_packet_desc[i], "status", "actual_length");
			return Struct.read(transfer, "status", "actual_length");
		}
	}

	/**
	 * The generic USB transfer structure. The user populates this structure and then submits it in
	 * order to request a transfer. After the transfer has completed, the library populates the
	 * transfer with the results and passes it back to the user.
	 */
	@Fields({ "dev_handle", "flags", "endpoint", "type", "timeout", "status", "length",
		"actual_length", "callback", "user_data", "buffer", "num_iso_packets", "iso_packet_desc" })
	public static class libusb_transfer extends VarStruct {
		public libusb_device_handle dev_handle;
		public byte flags; // libusb_transfer_flags
		public byte endpoint;
		public byte type; // libusb_transfer_type
		public int timeout;
		public int status; // libusb_transfer_status; read-only, set before callback
		public int length;
		public int actual_length; // read-only, set before callback
		public libusb_transfer_cb_fn callback;
		public Pointer user_data;
		public Pointer buffer;
		public int num_iso_packets;
		public libusb_iso_packet_descriptor[] iso_packet_desc = new libusb_iso_packet_descriptor[0];

		public libusb_transfer(Pointer p) {
			super(p);
		}

		public Set<libusb_transfer_flags> flags() {
			return libusb_transfer_flags.xcoder.decodeAll(Maths.ubyte(flags));
		}

		public libusb_transfer_type type() {
			return libusb_transfer_type.xcoder.decode(Maths.ubyte(type));
		}

		public libusb_transfer_status status() {
			return libusb_transfer_status.xcoder.decode(status);
		}

		@Override
		protected void setVarArray(int count) {
			iso_packet_desc = new libusb_iso_packet_descriptor[count];
		}

		@Override
		protected int varCount() {
			return num_iso_packets;
		}
	}

	/**
	 * libusb_lib callback function for handling log messages.
	 */
	// typedef void (LIBUSB_CALL *libusb_log_cb)(libusb_context *ctx, enum libusb_log_level level,
	// const char *str);
	public interface libusb_log_cb extends Callback {
		int invoke(libusb_context ctx, int level, String str);
	}

	/* async I/O */

	/**
	 * Returns the pointer offset to the start of the data buffer for a control transfer.
	 */
	public static Pointer libusb_control_transfer_get_data(libusb_transfer transfer) {
		return transfer.buffer.share(LIBUSB_CONTROL_SETUP_SIZE);
	}

	/**
	 * Returns the control setup structure read from the start of the transfer buffer.
	 */
	public static libusb_control_setup libusb_control_transfer_get_setup(libusb_transfer transfer) {
		return Struct.read(new libusb_control_setup(transfer.buffer));
	}

	/**
	 * Populates and returns the setup structure for a control transfer.
	 */
	public static libusb_control_setup libusb_fill_control_setup(Pointer buffer, int bmRequestType,
		int bRequest, int wValue, int wIndex, int wLength) {
		libusb_control_setup setup = new libusb_control_setup(buffer);
		setup.bmRequestType = (byte) bmRequestType;
		setup.bRequest = (byte) bRequest;
		setup.wValue = libusb_cpu_to_le16((short) wValue);
		setup.wIndex = libusb_cpu_to_le16((short) wIndex);
		setup.wLength = libusb_cpu_to_le16((short) wLength);
		Struct.write(setup);
		return setup;
	}

	/**
	 * Helper function to populate the required libusb_transfer fields for a control transfer. If
	 * you pass a transfer buffer to this function, the first 8 bytes will be interpreted as a
	 * control setup packet, and the wLength field will be used to automatically populate
	 * libusb_transfer.length. Therefore the recommended approach is:
	 * <ul>
	 * <li>Allocate a suitably sized data buffer (including space for control setup)</li>
	 * <li>Call libusb_fill_control_setup()</li>
	 * <li>If this is a host-to-device transfer with a data stage, put the data in place after the
	 * setup packet</li>
	 * <li>Call this function</li>
	 * <li>Call libusb_submit_transfer()</li>
	 * </ul>
	 * It is also legal to pass a NULL buffer to this function, in which case this function will not
	 * attempt to populate the length field. Remember that you must then populate the buffer and
	 * length fields later.
	 * @param transfer
	 *            the transfer to populate
	 * @param dev_handle
	 *            handle of the device that will handle the transfer
	 * @param buffer
	 *            data buffer. If provided, this function will interpret the first 8 bytes as a
	 *            setup packet and infer the transfer length from that. This pointer must be aligned
	 *            to at least 2 bytes boundary.
	 * @param callback
	 *            callback function to be invoked on transfer completion
	 * @param user_data
	 *            user data to pass to callback function
	 * @param timeout
	 *            timeout for the transfer in milliseconds
	 */
	// public static void libusb_fill_control_transfer(libusb_transfer transfer,
	// libusb_device_handle dev_handle, Pointer buffer, libusb_transfer_cb_fn callback,
	// Pointer user_data, int timeoutMs) {
	public static void libusb_fill_control_transfer(libusb_transfer transfer,
		libusb_device_handle dev_handle, libusb_control_setup setup, libusb_transfer_cb_fn callback,
		Pointer user_data, int timeoutMs) {
		transfer.dev_handle = dev_handle;
		transfer.endpoint = 0;
		transfer.type = (byte) libusb_transfer_type.LIBUSB_TRANSFER_TYPE_CONTROL.value;
		transfer.timeout = timeoutMs;
		transfer.buffer = setup == null ? null : setup.getPointer();
		transfer.length =
			setup == null ? 0 : LIBUSB_CONTROL_SETUP_SIZE + libusb_le16_to_cpu(setup.wLength);
		transfer.user_data = user_data;
		transfer.callback = callback;
	}

	/**
	 * Helper function to populate the required libusb_transfer fields for a bulk transfer.
	 * @param transfer
	 *            the transfer to populate
	 * @param dev_handle
	 *            handle of the device that will handle the transfer
	 * @param endpoint
	 *            address of the endpoint where this transfer will be sent
	 * @param buffer
	 *            data buffer
	 * @param length
	 *            length of data buffer
	 * @param callback
	 *            callback function to be invoked on transfer completion
	 * @param user_data
	 *            user data to pass to callback function
	 * @param timeout
	 *            timeout for the transfer in milliseconds
	 */
	public static void libusb_fill_bulk_transfer(libusb_transfer transfer,
		libusb_device_handle dev_handle, int endpoint, Pointer buffer, int length,
		libusb_transfer_cb_fn callback, Pointer user_data, int timeout) {
		transfer.dev_handle = dev_handle;
		transfer.endpoint = (byte) endpoint;
		transfer.type = (byte) libusb_transfer_type.LIBUSB_TRANSFER_TYPE_BULK.value;
		transfer.timeout = timeout;
		transfer.buffer = buffer;
		transfer.length = length;
		transfer.user_data = user_data;
		transfer.callback = callback;
	}

	/**
	 * Helper function to populate the required libusb_transfer fields for a bulk transfer using
	 * bulk streams. Since version 1.0.19, LIBUSB_API_VERSION >= 0x01000103
	 * @param transfer
	 *            the transfer to populate
	 * @param dev_handle
	 *            handle of the device that will handle the transfer
	 * @param endpoint
	 *            address of the endpoint where this transfer will be sent
	 * @param stream_id
	 *            bulk stream id for this transfer
	 * @param buffer
	 *            data buffer
	 * @param length
	 *            length of data buffer
	 * @param callback
	 *            callback function to be invoked on transfer completion
	 * @param user_data
	 *            user data to pass to callback function
	 * @param timeout
	 *            timeout for the transfer in milliseconds
	 */
	public static void libusb_fill_bulk_stream_transfer(libusb_transfer transfer,
		libusb_device_handle dev_handle, int endpoint, int stream_id, Pointer buffer, int length,
		libusb_transfer_cb_fn callback, Pointer user_data, int timeout) throws LibUsbException {
		libusb_fill_bulk_transfer(transfer, dev_handle, endpoint, buffer, length, callback,
			user_data, timeout);
		transfer.type = (byte) libusb_transfer_type.LIBUSB_TRANSFER_TYPE_BULK_STREAM.value;
		libusb_transfer_set_stream_id(transfer, stream_id);
	}

	/**
	 * Helper function to populate the required libusb_transfer fields for an interrupt transfer.
	 * @param transfer
	 *            the transfer to populate
	 * @param dev_handle
	 *            handle of the device that will handle the transfer
	 * @param endpoint
	 *            address of the endpoint where this transfer will be sent
	 * @param buffer
	 *            data buffer
	 * @param length
	 *            length of data buffer
	 * @param callback
	 *            callback function to be invoked on transfer completion
	 * @param user_data
	 *            user data to pass to callback function
	 * @param timeout
	 *            timeout for the transfer in milliseconds
	 */
	public static void libusb_fill_interrupt_transfer(libusb_transfer transfer,
		libusb_device_handle dev_handle, int endpoint, Pointer buffer, int length,
		libusb_transfer_cb_fn callback, Pointer user_data, int timeout) {
		transfer.dev_handle = dev_handle;
		transfer.endpoint = (byte) endpoint;
		transfer.type = (byte) libusb_transfer_type.LIBUSB_TRANSFER_TYPE_INTERRUPT.value;
		transfer.timeout = timeout;
		transfer.buffer = buffer;
		transfer.length = length;
		transfer.user_data = user_data;
		transfer.callback = callback;
	}

	/**
	 * Helper function to populate the required libusb_transfer fields for an isochronous transfer.
	 * @param transfer
	 *            the transfer to populate
	 * @param dev_handle
	 *            handle of the device that will handle the transfer
	 * @param endpoint
	 *            address of the endpoint where this transfer will be sent
	 * @param buffer
	 *            data buffer
	 * @param length
	 *            length of data buffer
	 * @param num_iso_packets
	 *            the number of isochronous packets
	 * @param callback
	 *            callback function to be invoked on transfer completion
	 * @param user_data
	 *            user data to pass to callback function
	 * @param timeout
	 *            timeout for the transfer in milliseconds
	 */
	public static void libusb_fill_iso_transfer(libusb_transfer transfer,
		libusb_device_handle dev_handle, int endpoint, Pointer buffer, int length,
		int num_iso_packets, libusb_transfer_cb_fn callback, Pointer user_data, int timeout) {
		transfer.dev_handle = dev_handle;
		transfer.endpoint = (byte) endpoint;
		transfer.type = (byte) libusb_transfer_type.LIBUSB_TRANSFER_TYPE_ISOCHRONOUS.value;
		transfer.timeout = timeout;
		transfer.buffer = buffer;
		transfer.length = length;
		transfer.num_iso_packets = num_iso_packets;
		transfer.user_data = user_data;
		transfer.callback = callback;
	}

	/**
	 * Convenience function to set the length of all packets in an isochronous transfer, based on
	 * the num_iso_packets field in the transfer structure.
	 * @param transfer
	 *            a transfer
	 * @param length
	 *            the length to set in each isochronous packet descriptor
	 *            libusb_get_max_packet_size()
	 */
	public static void libusb_set_iso_packet_lengths(libusb_transfer transfer, int length) {
		for (libusb_iso_packet_descriptor iso_packet_desc : transfer.iso_packet_desc)
			iso_packet_desc.length = length;
	}

	/**
	 * Convenience function to locate the position of an isochronous packet within the buffer of an
	 * isochronous transfer. This is a thorough function which loops through all preceding packets,
	 * accumulating their lengths to find the position of the specified packet. Typically you will
	 * assign equal lengths to each packet in the transfer, and hence the above method is
	 * sub-optimal. You may wish to use libusb_get_iso_packet_buffer_simple() instead.
	 * @param transfer
	 *            a transfer
	 * @param packet
	 *            the packet to return the address of
	 * @return the base address of the packet buffer inside the transfer buffer, or NULL if the
	 *         packet does not exist. See libusb_get_iso_packet_buffer_simple()
	 */
	public static Pointer libusb_get_iso_packet_buffer(libusb_transfer transfer, int packet) {
		if (packet > Short.MAX_VALUE) return null;
		if (packet >= transfer.num_iso_packets) return null;
		int offset = 0;
		for (int i = 0; i < packet; i++)
			offset += transfer.iso_packet_desc[i].length;
		return transfer.buffer.share(offset);
	}

	/**
	 * Convenience function to locate the position of an isochronous packet within the buffer of an
	 * isochronous transfer, for transfers where each packet is of identical size. This function
	 * relies on the assumption that every packet within the transfer is of identical size to the
	 * first packet. Calculating the location of the packet buffer is then just a simple
	 * calculation:
	 *
	 * <pre>
	 * buffer + (packet_size * packet)
	 * </pre>
	 *
	 * Do not use this function on transfers other than those that have identical packet lengths for
	 * each packet.
	 * @param transfer
	 *            a transfer
	 * @param packet
	 *            the packet to return the address of
	 * @return the base address of the packet buffer inside the transfer buffer, or NULL if the
	 *         packet does not exist. See libusb_get_iso_packet_buffer()
	 */
	public static Pointer libusb_get_iso_packet_buffer_simple(libusb_transfer transfer,
		int packet) {
		if (packet > Short.MAX_VALUE) return null;
		if (packet >= transfer.num_iso_packets) return null;
		return transfer.buffer.share(transfer.iso_packet_desc[0].length * packet);
	}

	/* sync I/O */

	/**
	 * Retrieve a descriptor from the default control pipe. This is a convenience function which
	 * formulates the appropriate control message to retrieve the descriptor.
	 */
	public static byte[] libusb_get_descriptor(libusb_device_handle dev,
		libusb_descriptor_type desc_type, int desc_index) throws LibUsbException {
		return libusb_control_transfer(dev, //
			libusb_endpoint_direction.LIBUSB_ENDPOINT_IN.value,
			libusb_standard_request.LIBUSB_REQUEST_GET_DESCRIPTOR.value,
			(desc_type.value << 8) | desc_index, 0, MAX_DESCRIPTOR_SIZE, DEFAULT_TIMEOUT);
	}

	/**
	 * Retrieve a descriptor from a device. This is a convenience function which formulates the
	 * appropriate control message to retrieve the descriptor. The string returned is Unicode, as
	 * detailed in the USB specifications.
	 */
	public static String libusb_get_string_descriptor(libusb_device_handle dev, int desc_index,
		int langid) throws LibUsbException {
		ByteBuffer buffer = ByteBuffer.allocate(MAX_DESCRIPTOR_SIZE);
		int size = libusb_control_transfer(dev, //
			libusb_endpoint_direction.LIBUSB_ENDPOINT_IN.value,
			libusb_standard_request.LIBUSB_REQUEST_GET_DESCRIPTOR.value,
			(libusb_descriptor_type.LIBUSB_DT_STRING.value << 8) | desc_index, langid, buffer,
			buffer.limit(), DEFAULT_TIMEOUT);
		return JnaUtil.string(StandardCharsets.UTF_16, buffer, 0, size);
	}

	/* polling and timeouts */

	/**
	 * Subset of poll_event from &lt;poll.h&gt;. POLLIN indicates that you should monitor this file
	 * descriptor for becoming ready to read from, and POLLOUT indicates that you should monitor
	 * this file descriptor for nonblocking write readiness.
	 */
	public static enum libusb_poll_event {
		POLLIN(0x0001),
		POLLOUT(0x0004);

		public static final Xcoder.Types<libusb_poll_event> xcoder =
			Xcoder.types(libusb_poll_event.class);
		public final int value;

		private libusb_poll_event(int value) {
			this.value = value;
		}
	}

	/**
	 * File descriptor for polling
	 */
	@Fields({ "fd", "events" })
	public static class libusb_pollfd extends Struct {
		public int fd;
		public short events;

		public libusb_pollfd(Pointer p) {
			super(p);
		}

		public Set<libusb_poll_event> events() {
			return libusb_poll_event.xcoder.decodeAll(Maths.ushort(events));
		}
	}

	/**
	 * Callback function, invoked when a new file descriptor should be added to the set of file
	 * descriptors monitored for events.
	 */
	// typedef void (LIBUSB_CALL *libusb_pollfd_added_cb)(int fd, short events, void *user_data);
	public interface libusb_pollfd_added_cb extends Callback {
		void invoke(int fd, short events, Pointer user_data);
	}

	/**
	 * Callback function, invoked when a file descriptor should be removed from the set of file
	 * descriptors being monitored for events. After returning from this callback, do not use that
	 * file descriptor again.
	 */
	// typedef void (LIBUSB_CALL *libusb_pollfd_removed_cb)(int fd, void *user_data);
	public interface libusb_pollfd_removed_cb extends Callback {
		void invoke(int fd, Pointer user_data);
	}

	/**
	 * Callback handle. Callbacks handles are generated by libusb_hotplug_register_callback() and
	 * can be used to deregister callbacks. Callback handles are unique per libusb_context and it is
	 * safe to call libusb_hotplug_deregister_callback() on an already deregistered callback. Since
	 * version 1.0.16, LIBUSB_API_VERSION >= 0x01000102 For more information, see hotplug.
	 */
	// typedef int libusb_hotplug_callback_handle;
	public static class libusb_hotplug_callback_handle {
		public final int value;

		libusb_hotplug_callback_handle(int value) {
			this.value = value;
		}
	}

	/**
	 * Hotplug callback function type. When requesting hotplug event notifications, you pass a
	 * pointer to a callback function of this type. This callback may be called by an internal event
	 * thread and as such it is recommended the callback do minimal processing before returning.
	 * libusb will call this function later, when a matching event had happened on a matching
	 * device. See hotplug for more information. It is safe to call either
	 * libusb_hotplug_register_callback() or libusb_hotplug_deregister_callback() from within a
	 * callback function. Since version 1.0.16, LIBUSB_API_VERSION >= 0x01000102
	 * <p/>
	 * Notes:
	 * <ul>
	 * <li>libusb_get_device_descriptor() is ok for any event within the callback</li>
	 * <li>calls such as libusb_get_string_descriptor_ascii will fail inside the callback</li>
	 * <li>for LIBUSB_HOTPLUG_EVENT_DEVICE_LEFT, calls after the callback will cause SIGSEGV</li>
	 * </ul>
	 */
	// typedef int (LIBUSB_CALL *libusb_hotplug_callback_fn)(libusb_context *ctx,
	// libusb_device *device, libusb_hotplug_event event, void *user_data);
	public interface libusb_hotplug_callback_fn extends Callback {
		/** Return 1 to indicate finished processing event, and callback will be unregistered. */
		int invoke(libusb_context ctx, libusb_device device, int event, Pointer user_data);
	}

	/**
	 * Initializes a new context.
	 */
	public static libusb_context libusb_init() throws LibUsbException {
		PointerByReference ref = new PointerByReference();
		caller.verify(() -> lib().libusb_init(ref), "libusb_init");
		return PointerUtil.set(new libusb_context(), ref.getValue());
	}

	/**
	 * Initializes default context if first time, otherwise reuses default context. Returns null for
	 * context, which can be used for other calls requiring context.
	 */
	public static libusb_context libusb_init_default() throws LibUsbException {
		caller.verify(() -> lib().libusb_init(null), "libusb_init_default");
		return null;
	}

	public static void libusb_exit(libusb_context ctx) throws LibUsbException {
		// Don't close default context or libusb_init_default will fail
		if (ctx != null) caller.call(() -> lib().libusb_exit(ctx), "libusb_exit", ctx);
	}

	public static void libusb_set_option(libusb_context ctx, libusb_option option, Object... args)
		throws LibUsbException {
		caller.verify(() -> lib().libusb_set_option(ctx, option.value, args), "libusb_set_option",
			option, args);
	}

	public static void libusb_set_log_cb(libusb_context ctx, libusb_log_cb cb,
		libusb_log_cb_mode mode) throws LibUsbException {
		caller.call(() -> lib().libusb_set_log_cb(ctx, cb, mode.value), "libusb_set_log_cb", ctx,
			cb, mode);
	}

	public static libusb_version libusb_get_version() throws LibUsbException {
		return caller.callType(() -> lib().libusb_get_version(), "libusb_get_version");
	}

	public static boolean libusb_has_capability(libusb_capability capability)
		throws LibUsbException {
		return caller.callInt(() -> lib().libusb_has_capability(capability.value),
			"libusb_has_capability", capability) != 0;
	}

	public static String libusb_error_name(libusb_error errcode) throws LibUsbException {
		return caller.callType(() -> lib().libusb_error_name(errcode.value), "libusb_error_name",
			errcode);
	}

	public static void libusb_setlocale(String locale) throws LibUsbException {
		caller.verify(() -> lib().libusb_setlocale(locale), "libusb_setlocale", locale);
	}

	public static String libusb_strerror(libusb_error errcode) throws LibUsbException {
		return caller.callType(() -> lib().libusb_strerror(errcode.value), "libusb_strerror",
			errcode);
	}

	public static ArrayPointer<libusb_device> libusb_get_device_list(libusb_context ctx)
		throws LibUsbException {
		PointerByReference ref = new PointerByReference();
		int size = caller.verifyInt(() -> lib().libusb_get_device_list(ctx, ref),
			"libusb_get_device_list", ctx);
		return ArrayPointer.byRef(ref.getValue(), PointerUtil.adapt(libusb_device::new),
			libusb_device[]::new, size);
	}

	public static void libusb_free_device_list(ArrayPointer<libusb_device> list)
		throws LibUsbException {
		libusb_free_device_list(list, 1);
	}

	public static void libusb_free_device_list(ArrayPointer<libusb_device> list, int unref)
		throws LibUsbException {
		if (list == null) return;
		Pointer p = list.getPointer();
		if (p != null) caller.call(() -> lib().libusb_free_device_list(p, unref),
			"libusb_free_device_list", p, unref);
	}

	public static libusb_device libusb_ref_device(libusb_device dev) throws LibUsbException {
		LibUsbUtil.require(dev);
		return caller.verifyType(() -> lib().libusb_ref_device(dev),
			libusb_error.LIBUSB_ERROR_NOT_FOUND.value, "libusb_ref_device", dev);
	}

	public static void libusb_unref_device(libusb_device dev) throws LibUsbException {
		if (dev != null)
			caller.call(() -> lib().libusb_unref_device(dev), "libusb_unref_device", dev);
	}

	public static void libusb_unref_devices(Collection<libusb_device> devs) throws LibUsbException {
		LibUsbUtil.require(devs, "Devices");
		for (var dev : devs)
			libusb_unref_device(dev);
	}

	public static int libusb_get_configuration(libusb_device_handle dev) throws LibUsbException {
		LibUsbUtil.require(dev);
		IntByReference config = new IntByReference();
		caller.verify(() -> lib().libusb_get_configuration(dev, config), "libusb_get_configuration",
			dev, config);
		return config.getValue();
	}

	public static libusb_device_descriptor libusb_get_device_descriptor(libusb_device dev)
		throws LibUsbException {
		LibUsbUtil.require(dev);
		libusb_device_descriptor descriptor = new libusb_device_descriptor(null);
		Pointer p = descriptor.getPointer();
		caller.verify(() -> lib().libusb_get_device_descriptor(dev, p),
			"libusb_get_device_descriptor", dev, p);
		return Struct.read(descriptor);
	}

	public static libusb_config_descriptor libusb_get_active_config_descriptor(libusb_device dev)
		throws LibUsbException {
		LibUsbUtil.require(dev);
		PointerByReference config = new PointerByReference();
		caller.verify(() -> lib().libusb_get_active_config_descriptor(dev, config),
			"libusb_get_active_config_descriptor", dev, config);
		return Struct.read(new libusb_config_descriptor(config.getValue()));
	}

	public static libusb_config_descriptor libusb_get_config_descriptor(libusb_device dev,
		int config_index) throws LibUsbException {
		LibUsbUtil.require(dev);
		PointerByReference config = new PointerByReference();
		caller.verify(() -> lib().libusb_get_config_descriptor(dev, (byte) config_index, config),
			"libusb_get_config_descriptor", dev, config_index, config);
		return Struct.read(new libusb_config_descriptor(config.getValue()));
	}

	public static libusb_config_descriptor libusb_get_config_descriptor_by_value(libusb_device dev,
		int bConfigurationValue) throws LibUsbException {
		LibUsbUtil.require(dev);
		PointerByReference config = new PointerByReference();
		caller
			.verify(
				() -> lib().libusb_get_config_descriptor_by_value(dev, (byte) bConfigurationValue,
					config),
				"libusb_get_config_descriptor_by_value", dev, bConfigurationValue, config);
		return Struct.read(new libusb_config_descriptor(config.getValue()));
	}

	public static void libusb_free_config_descriptor(libusb_config_descriptor config)
		throws LibUsbException {
		Pointer p = Struct.pointer(config);
		if (p != null) caller.call(() -> lib().libusb_free_config_descriptor(p),
			"libusb_free_config_descriptor", p);
	}

	public static libusb_ss_endpoint_companion_descriptor
		libusb_get_ss_endpoint_companion_descriptor(libusb_context ctx,
			libusb_endpoint_descriptor endpoint) throws LibUsbException {
		LibUsbUtil.require(endpoint, "Endpoint");
		Pointer p = endpoint.getPointer();
		PointerByReference ep_comp = new PointerByReference();
		int result = caller.verifyInt(
			() -> lib().libusb_get_ss_endpoint_companion_descriptor(ctx, p, ep_comp),
			r -> r >= 0 || r == libusb_error.LIBUSB_ERROR_NOT_FOUND.value,
			"libusb_get_ss_endpoint_companion_descriptor", ctx, p, ep_comp);
		return result < 0 ? null :
			Struct.read(new libusb_ss_endpoint_companion_descriptor(ep_comp.getValue()));
	}

	public static void libusb_free_ss_endpoint_companion_descriptor(
		libusb_ss_endpoint_companion_descriptor ep_comp) throws LibUsbException {
		Pointer p = Struct.pointer(ep_comp);
		if (p != null) caller.call(() -> lib().libusb_free_ss_endpoint_companion_descriptor(p),
			"libusb_free_ss_endpoint_companion_descriptor", p);
	}

	public static libusb_bos_descriptor libusb_get_bos_descriptor(libusb_device_handle handle)
		throws LibUsbException {
		LibUsbUtil.require(handle);
		PointerByReference bos = new PointerByReference();
		int result = caller.verifyInt(() -> lib().libusb_get_bos_descriptor(handle, bos),
			r -> r >= 0 || r == libusb_error.LIBUSB_ERROR_NOT_FOUND.value
				|| r == libusb_error.LIBUSB_ERROR_PIPE.value,
			"libusb_get_bos_descriptor", handle, bos);
		return result < 0 ? null : Struct.read(new libusb_bos_descriptor(bos.getValue()));
	}

	public static void libusb_free_bos_descriptor(libusb_bos_descriptor bos)
		throws LibUsbException {
		Pointer p = Struct.pointer(bos);
		if (p != null)
			caller.call(() -> lib().libusb_free_bos_descriptor(p), "libusb_free_bos_descriptor", p);
	}

	public static libusb_usb_2_0_extension_descriptor libusb_get_usb_2_0_extension_descriptor(
		libusb_context ctx, libusb_bos_dev_capability_descriptor dev_cap) throws LibUsbException {
		LibUsbUtil.require(dev_cap, "Descriptor");
		Pointer p = dev_cap.getPointer();
		PointerByReference usb_2_0_extension = new PointerByReference();
		caller.verify(
			() -> lib().libusb_get_usb_2_0_extension_descriptor(ctx, p, usb_2_0_extension),
			"libusb_get_usb_2_0_extension_descriptor", ctx, p, usb_2_0_extension);
		return Struct.read(new libusb_usb_2_0_extension_descriptor(usb_2_0_extension.getValue()));
	}

	public static void libusb_free_usb_2_0_extension_descriptor(
		libusb_usb_2_0_extension_descriptor usb_2_0_extension) throws LibUsbException {
		Pointer p = Struct.pointer(usb_2_0_extension);
		if (p != null) caller.call(() -> lib().libusb_free_usb_2_0_extension_descriptor(p),
			"libusb_free_usb_2_0_extension_descriptor", p);
	}

	public static libusb_ss_usb_device_capability_descriptor
		libusb_get_ss_usb_device_capability_descriptor(libusb_context ctx,
			libusb_bos_dev_capability_descriptor dev_cap) throws LibUsbException {
		LibUsbUtil.require(dev_cap, "Descriptor");
		Pointer p = dev_cap.getPointer();
		PointerByReference ss_usb_device_cap = new PointerByReference();
		caller.verify(
			() -> lib().libusb_get_ss_usb_device_capability_descriptor(ctx, p, ss_usb_device_cap),
			"libusb_get_ss_usb_device_capability_descriptor", ctx, p, ss_usb_device_cap);
		return Struct
			.read(new libusb_ss_usb_device_capability_descriptor(ss_usb_device_cap.getValue()));
	}

	public static void libusb_free_ss_usb_device_capability_descriptor(
		libusb_ss_usb_device_capability_descriptor ss_usb_device_cap) throws LibUsbException {
		Pointer p = Struct.pointer(ss_usb_device_cap);
		if (p != null) caller.call(() -> lib().libusb_free_ss_usb_device_capability_descriptor(p),
			"libusb_free_ss_usb_device_capability_descriptor", p);
	}

	public static libusb_container_id_descriptor libusb_get_container_id_descriptor(
		libusb_context ctx, libusb_bos_dev_capability_descriptor dev_cap) throws LibUsbException {
		LibUsbUtil.require(dev_cap, "Descriptor");
		Pointer p = dev_cap.getPointer();
		PointerByReference container_id = new PointerByReference();
		caller.verify(() -> lib().libusb_get_container_id_descriptor(ctx, p, container_id),
			"libusb_get_container_id_descriptor", ctx, p, container_id);
		return Struct.read(new libusb_container_id_descriptor(container_id.getValue()));
	}

	public static void libusb_free_container_id_descriptor(
		libusb_container_id_descriptor container_id) throws LibUsbException {
		Pointer p = Struct.pointer(container_id);
		if (p != null) caller.call(() -> lib().libusb_free_container_id_descriptor(p),
			"libusb_free_container_id_descriptor", p);
	}

	public static int libusb_get_bus_number(libusb_device dev) throws LibUsbException {
		LibUsbUtil.require(dev);
		return caller.callInt(() -> lib().libusb_get_bus_number(dev), "libusb_get_bus_number", dev)
			& 0xff;
	}

	public static int libusb_get_port_number(libusb_device dev) throws LibUsbException {
		LibUsbUtil.require(dev);
		return caller.callInt(() -> lib().libusb_get_port_number(dev), "libusb_get_port_number",
			dev) & 0xff;
	}

	public static byte[] libusb_get_port_numbers(libusb_device dev) throws LibUsbException {
		LibUsbUtil.require(dev);
		try (Memory m = new Memory(MAX_PORT_NUMBERS)) {
			int size = caller.verifyInt(() -> lib().libusb_get_port_numbers(dev, m, (int) m.size()),
				"libusb_get_port_numbers", dev, m, m.size());
			return m.getByteArray(0, size);
		}
	}

	public static libusb_device libusb_get_parent(libusb_device dev) throws LibUsbException {
		LibUsbUtil.require(dev);
		return caller.callType(() -> lib().libusb_get_parent(dev), "libusb_get_parent", dev);
	}

	public static byte libusb_get_device_address(libusb_device dev) throws LibUsbException {
		LibUsbUtil.require(dev);
		return caller.callType(() -> lib().libusb_get_device_address(dev),
			"libusb_get_device_address", dev);
	}

	public static libusb_speed libusb_get_device_speed(libusb_device dev) throws LibUsbException {
		LibUsbUtil.require(dev);
		int speed = caller.verifyInt(() -> lib().libusb_get_device_speed(dev),
			"libusb_get_device_speed", dev);
		return libusb_speed.xcoder.decode(speed);
	}

	public static int libusb_get_max_packet_size(libusb_device dev, int endpoint)
		throws LibUsbException {
		LibUsbUtil.require(dev);
		return caller.verifyInt(() -> lib().libusb_get_max_packet_size(dev, (byte) endpoint),
			"libusb_get_max_packet_size", dev, endpoint);
	}

	public static int libusb_get_max_iso_packet_size(libusb_device dev, int endpoint)
		throws LibUsbException {
		LibUsbUtil.require(dev);
		return caller.verifyInt(() -> lib().libusb_get_max_iso_packet_size(dev, (byte) endpoint),
			"libusb_get_max_iso_packet_size", dev, endpoint);
	}

	public static libusb_device_handle libusb_open(libusb_device dev) throws LibUsbException {
		LibUsbUtil.require(dev);
		PointerByReference handle = new PointerByReference();
		caller.verify(() -> lib().libusb_open(dev, handle), "libusb_open", dev, handle);
		return PointerUtil.set(new libusb_device_handle(), handle.getValue());
	}

	public static void libusb_close(libusb_device_handle dev_handle) throws LibUsbException {
		if (dev_handle != null)
			caller.call(() -> lib().libusb_close(dev_handle), "libusb_close", dev_handle);
	}

	public static libusb_device libusb_get_device(libusb_device_handle dev_handle)
		throws LibUsbException {
		if (dev_handle == null) return null;
		return caller.verifyType(() -> lib().libusb_get_device(dev_handle),
			libusb_error.LIBUSB_ERROR_NOT_FOUND.value, "libusb_get_device", dev_handle);
	}

	public static void libusb_set_configuration(libusb_device_handle dev, int configuration)
		throws LibUsbException {
		LibUsbUtil.require(dev);
		caller.verify(() -> lib().libusb_set_configuration(dev, configuration),
			"libusb_set_configuration", dev, configuration);
	}

	public static void libusb_claim_interface(libusb_device_handle dev, int interface_number)
		throws LibUsbException {
		LibUsbUtil.require(dev);
		caller.verify(() -> lib().libusb_claim_interface(dev, interface_number),
			"libusb_claim_interface", dev, interface_number);
	}

	public static void libusb_release_interface(libusb_device_handle dev, int interface_number)
		throws LibUsbException {
		if (dev != null) caller.verify(() -> lib().libusb_release_interface(dev, interface_number),
			"libusb_release_interface", dev, interface_number);
	}

	public static libusb_device_handle libusb_open_device_with_vid_pid(libusb_context ctx,
		int vendor_id, int product_id) throws LibUsbException {
		return caller.verifyType(
			() -> lib().libusb_open_device_with_vid_pid(ctx, (short) vendor_id, (short) product_id),
			libusb_error.LIBUSB_ERROR_NOT_FOUND.value, "libusb_open_device_with_vid_pid", ctx,
			vendor_id, product_id);
	}

	public static void libusb_set_interface_alt_setting(libusb_device_handle dev,
		int interface_number, int alternate_setting) throws LibUsbException {
		LibUsbUtil.require(dev);
		caller.verify(
			() -> lib().libusb_set_interface_alt_setting(dev, interface_number, alternate_setting),
			"libusb_set_interface_alt_setting", dev, interface_number, alternate_setting);
	}

	public static void libusb_clear_halt(libusb_device_handle dev, int endpoint)
		throws LibUsbException {
		LibUsbUtil.require(dev);
		caller.verify(() -> lib().libusb_clear_halt(dev, (byte) endpoint), "libusb_clear_halt", dev,
			endpoint);
	}

	public static void libusb_reset_device(libusb_device_handle dev) throws LibUsbException {
		LibUsbUtil.require(dev);
		caller.verify(() -> lib().libusb_reset_device(dev), "libusb_reset_device", dev);
	}

	public static int libusb_alloc_streams(libusb_device_handle dev, int num_streams,
		int... endpoints) throws LibUsbException {
		return libusb_alloc_streams(dev, num_streams, ArrayUtil.bytes.of(endpoints));
	}

	public static int libusb_alloc_streams(libusb_device_handle dev, int num_streams,
		byte[] endpoints) throws LibUsbException {
		LibUsbUtil.require(dev);
		try (Memory m = JnaUtil.mallocBytes(endpoints)) {
			return caller.verifyInt(
				() -> lib().libusb_alloc_streams(dev, num_streams, m, endpoints.length),
				"libusb_alloc_streams", dev, num_streams, m, endpoints.length);
		}
	}

	public static void libusb_free_streams(libusb_device_handle dev, int... endpoints)
		throws LibUsbException {
		libusb_free_streams(dev, ArrayUtil.bytes.of(endpoints));
	}

	public static void libusb_free_streams(libusb_device_handle dev, byte[] endpoints)
		throws LibUsbException {
		if (dev == null || endpoints.length == 0) return;
		try (Memory m = JnaUtil.mallocBytes(endpoints)) {
			caller.verify(() -> lib().libusb_free_streams(dev, m, endpoints.length),
				"libusb_free_streams", dev, m, endpoints.length);
		}
	}

	public static boolean libusb_kernel_driver_active(libusb_device_handle dev,
		int interface_number) throws LibUsbException {
		LibUsbUtil.require(dev);
		return caller.verifyInt(() -> lib().libusb_kernel_driver_active(dev, interface_number),
			"libusb_kernel_driver_active", dev, interface_number) != 0;
	}

	public static void libusb_detach_kernel_driver(libusb_device_handle dev, int interface_number)
		throws LibUsbException {
		LibUsbUtil.require(dev);
		caller.verify(() -> lib().libusb_detach_kernel_driver(dev, interface_number),
			"libusb_detach_kernel_driver", dev, interface_number);
	}

	public static void libusb_attach_kernel_driver(libusb_device_handle dev, int interface_number)
		throws LibUsbException {
		LibUsbUtil.require(dev);
		caller.verify(() -> lib().libusb_attach_kernel_driver(dev, interface_number),
			"libusb_attach_kernel_driver", dev, interface_number);
	}

	public static void libusb_set_auto_detach_kernel_driver(libusb_device_handle dev,
		boolean enable) throws LibUsbException {
		LibUsbUtil.require(dev);
		caller.verify(() -> lib().libusb_set_auto_detach_kernel_driver(dev, enable ? 1 : 0),
			"libusb_set_auto_detach_kernel_driver", dev, enable ? 1 : 0);
	}

	public static libusb_transfer libusb_alloc_transfer(int iso_packets) throws LibUsbException {
		Pointer p = caller.verifyType(() -> lib().libusb_alloc_transfer(iso_packets),
			libusb_error.LIBUSB_ERROR_NO_MEM.value, "libusb_alloc_transfer", iso_packets);
		var xfer = new libusb_transfer(p);
		xfer.num_iso_packets = iso_packets;
		xfer.iso_packet_desc = new libusb_iso_packet_descriptor[iso_packets];
		for (int i = 0; i < xfer.iso_packet_desc.length; i++)
			xfer.iso_packet_desc[i] = new libusb_iso_packet_descriptor(null);
		return xfer;
	}

	/**
	 * Submits asynchronous transfer. Caller must sync transfer struct fields before calling.
	 */
	public static void libusb_submit_transfer(libusb_transfer transfer) throws LibUsbException {
		LibUsbUtil.require(transfer);
		Pointer p = Struct.pointer(transfer);
		caller.verify(() -> lib().libusb_submit_transfer(p), "libusb_submit_transfer", p);
	}

	public static boolean libusb_cancel_transfer(libusb_transfer transfer) throws LibUsbException {
		Pointer p = Struct.pointer(transfer);
		if (p == null) return false;
		return caller.verifyInt(() -> lib().libusb_cancel_transfer(p),
			i -> i == 0 || i == libusb_error.LIBUSB_ERROR_NOT_FOUND.value, "libusb_cancel_transfer",
			p) == 0;
	}

	public static void libusb_free_transfer(libusb_transfer transfer) throws LibUsbException {
		Pointer p = Struct.pointer(transfer);
		if (p != null) caller.call(() -> lib().libusb_free_transfer(p), "libusb_free_transfer", p);
	}

	public static void libusb_transfer_set_stream_id(libusb_transfer transfer, int stream_id)
		throws LibUsbException {
		LibUsbUtil.require(transfer);
		Pointer p = Struct.pointer(transfer);
		caller.call(() -> lib().libusb_transfer_set_stream_id(p, stream_id),
			"libusb_transfer_set_stream_id", p, stream_id);
	}

	public static int libusb_transfer_get_stream_id(libusb_transfer transfer)
		throws LibUsbException {
		LibUsbUtil.require(transfer);
		Pointer p = Struct.pointer(transfer);
		return caller.verifyInt(() -> lib().libusb_transfer_get_stream_id(p),
			"libusb_transfer_get_stream_id", p);
	}

	public static int libusb_control_transfer(libusb_device_handle dev_handle, int request_type,
		int bRequest, int wValue, int wIndex, byte[] data, int timeout) throws LibUsbException {
		ByteBuffer buffer = ByteBuffer.wrap(data);
		return libusb_control_transfer(dev_handle, request_type, bRequest, wValue, wIndex, buffer,
			data.length, timeout);
	}

	public static byte[] libusb_control_transfer(libusb_device_handle dev_handle, int request_type,
		int bRequest, int wValue, int wIndex, int wLength, int timeout) throws LibUsbException {
		ByteBuffer buffer = ByteBuffer.allocate(wLength);
		int count = libusb_control_transfer(dev_handle, request_type, bRequest, wValue, wIndex,
			buffer, wLength, timeout);
		return JnaUtil.bytes(buffer, 0, count);
	}

	public static int libusb_control_transfer(libusb_device_handle dev_handle, int request_type,
		int bRequest, int wValue, int wIndex, ByteBuffer data, int timeout) throws LibUsbException {
		return libusb_control_transfer(dev_handle, request_type, bRequest, wValue, wIndex, data,
			data.remaining(), timeout);
	}

	public static int libusb_control_transfer(libusb_device_handle dev_handle, int request_type,
		int bRequest, int wValue, int wIndex, ByteBuffer data, int wLength, int timeout)
		throws LibUsbException {
		LibUsbUtil.require(dev_handle);
		LibUsbUtil.require(data, wLength);
		int n = caller.verifyInt(
			() -> lib().libusb_control_transfer(dev_handle, (byte) request_type, (byte) bRequest,
				(short) wValue, (short) wIndex, data, (short) wLength, timeout),
			"libusb_control_transfer", dev_handle, request_type, bRequest, wValue, wIndex, data,
			wLength, timeout);
		return updatePosition(data, n);
	}

	public static int libusb_bulk_transfer(libusb_device_handle dev_handle, int endpoint,
		byte[] data, int timeout) throws LibUsbException {
		ByteBuffer buffer = ByteBuffer.wrap(data);
		return libusb_bulk_transfer(dev_handle, endpoint, buffer, buffer.limit(), timeout);
	}

	public static byte[] libusb_bulk_transfer(libusb_device_handle dev_handle, int endpoint,
		int length, int timeout) throws LibUsbException {
		ByteBuffer buffer = ByteBuffer.allocate(length);
		int count = libusb_bulk_transfer(dev_handle, endpoint, buffer, length, timeout);
		return JnaUtil.bytes(buffer, 0, count);
	}

	public static int libusb_bulk_transfer(libusb_device_handle dev_handle, int endpoint,
		ByteBuffer data, int timeout) throws LibUsbException {
		return libusb_bulk_transfer(dev_handle, endpoint, data, data.remaining(), timeout);
	}

	public static int libusb_bulk_transfer(libusb_device_handle dev_handle, int endpoint,
		ByteBuffer data, int length, int timeout) throws LibUsbException {
		LibUsbUtil.require(dev_handle);
		LibUsbUtil.require(data, length);
		IntByReference actual_length = new IntByReference();
		caller.verify(
			() -> lib().libusb_bulk_transfer(dev_handle, (byte) endpoint, data, length,
				actual_length, timeout),
			"libusb_bulk_transfer", dev_handle, endpoint, data, length, actual_length, timeout);
		return updatePosition(data, actual_length.getValue());
	}

	public static int libusb_interrupt_transfer(libusb_device_handle dev_handle, int endpoint,
		byte[] data, int timeout) throws LibUsbException {
		ByteBuffer buffer = ByteBuffer.wrap(data);
		return libusb_interrupt_transfer(dev_handle, endpoint, buffer, buffer.limit(), timeout);
	}

	public static byte[] libusb_interrupt_transfer(libusb_device_handle dev_handle, int endpoint,
		int length, int timeout) throws LibUsbException {
		ByteBuffer buffer = ByteBuffer.allocate(length);
		int len = libusb_interrupt_transfer(dev_handle, endpoint, buffer, length, timeout);
		return JnaUtil.bytes(buffer, 0, len);
	}

	public static int libusb_interrupt_transfer(libusb_device_handle dev_handle, int endpoint,
		ByteBuffer data, int timeout) throws LibUsbException {
		return libusb_interrupt_transfer(dev_handle, endpoint, data, data.remaining(), timeout);
	}

	public static int libusb_interrupt_transfer(libusb_device_handle dev_handle, int endpoint,
		ByteBuffer data, int length, int timeout) throws LibUsbException {
		LibUsbUtil.require(dev_handle);
		LibUsbUtil.require(data, length);
		IntByReference actual_length = new IntByReference();
		caller.verify(
			() -> lib().libusb_interrupt_transfer(dev_handle, (byte) endpoint, data, length,
				actual_length, timeout),
			"libusb_interrupt_transfer", dev_handle, endpoint, data, length, actual_length,
			timeout);
		return updatePosition(data, actual_length.getValue());
	}

	public static String libusb_get_string_descriptor_ascii(libusb_device_handle dev,
		int desc_index) throws LibUsbException {
		LibUsbUtil.require(dev);
		if (desc_index == 0) return null;
		ByteBuffer buffer = ByteBuffer.allocate(MAX_DESCRIPTOR_SIZE);
		int size = caller.verifyInt(
			() -> lib().libusb_get_string_descriptor_ascii(dev, (byte) desc_index, buffer,
				buffer.capacity()),
			"libusb_get_string_descriptor_ascii", dev, desc_index, buffer, buffer.capacity());
		return JnaUtil.string(StandardCharsets.ISO_8859_1, buffer, 0, size);
	}

	/* polling and timeouts */

	public static boolean libusb_try_lock_events(libusb_context ctx) throws LibUsbException {
		return caller.verifyInt(() -> lib().libusb_try_lock_events(ctx), "libusb_try_lock_events",
			ctx) == 0;
	}

	public static void libusb_lock_events(libusb_context ctx) throws LibUsbException {
		caller.call(() -> lib().libusb_lock_events(ctx), "libusb_lock_events", ctx);
	}

	public static void libusb_unlock_events(libusb_context ctx) throws LibUsbException {
		caller.call(() -> lib().libusb_unlock_events(ctx), "libusb_unlock_events", ctx);
	}

	public static boolean libusb_event_handling_ok(libusb_context ctx) throws LibUsbException {
		return caller.verifyInt(() -> lib().libusb_event_handling_ok(ctx),
			"libusb_event_handling_ok", ctx) != 0;
	}

	public static boolean libusb_event_handler_active(libusb_context ctx) throws LibUsbException {
		return caller.verifyInt(() -> lib().libusb_event_handler_active(ctx),
			"libusb_event_handler_active", ctx) != 0;
	}

	public static void libusb_lock_event_waiters(libusb_context ctx) throws LibUsbException {
		caller.call(() -> lib().libusb_lock_event_waiters(ctx), "libusb_lock_event_waiters", ctx);
	}

	public static void libusb_unlock_event_waiters(libusb_context ctx) throws LibUsbException {
		caller.call(() -> lib().libusb_unlock_event_waiters(ctx), "libusb_unlock_event_waiters",
			ctx);
	}

	public static void libusb_wait_for_event(libusb_context ctx, CTime.timeval tv)
		throws LibUsbException {
		caller.verify(() -> lib().libusb_wait_for_event(ctx, tv.getPointer()),
			"libusb_wait_for_event", ctx, tv);
	}

	public static void libusb_interrupt_event_handler(libusb_context ctx) throws LibUsbException {
		caller.call(() -> lib().libusb_interrupt_event_handler(ctx),
			"libusb_interrupt_event_handler", ctx);
	}

	public static void libusb_handle_events_timeout(libusb_context ctx, CTime.timeval tv)
		throws LibUsbException {
		caller.verify(() -> lib().libusb_handle_events_timeout(ctx, tv.getPointer()),
			"libusb_handle_events_timeout", ctx, tv);
	}

	public static int libusb_handle_events_timeout_completed(libusb_context ctx, CTime.timeval tv,
		IntByReference completed) throws LibUsbException {
		caller.verify(
			() -> lib().libusb_handle_events_timeout_completed(ctx, tv.getPointer(), completed),
			"libusb_handle_events_timeout_completed", ctx, tv, completed);
		return completed == null ? 0 : completed.getValue();
	}

	public static void libusb_handle_events(libusb_context ctx) throws LibUsbException {
		caller.verify(() -> lib().libusb_handle_events(ctx), "libusb_handle_events", ctx);
	}

	public static int libusb_handle_events_completed(libusb_context ctx, IntByReference completed)
		throws LibUsbException {
		caller.verify(() -> lib().libusb_handle_events_completed(ctx, completed),
			"libusb_handle_events_completed", ctx, completed);
		return completed == null ? 0 : completed.getValue();
	}

	public static void libusb_handle_events_locked(libusb_context ctx, CTime.timeval tv)
		throws LibUsbException {
		caller.verify(() -> lib().libusb_handle_events_locked(ctx, tv.getPointer()),
			"libusb_handle_events_locked", ctx, tv);
	}

	public static boolean libusb_pollfds_handle_timeouts(libusb_context ctx)
		throws LibUsbException {
		return caller.verifyInt(() -> lib().libusb_pollfds_handle_timeouts(ctx),
			"libusb_pollfds_handle_timeouts", ctx) != 0;
	}

	public static CTime.timeval libusb_get_next_timeout(libusb_context ctx) throws LibUsbException {
		var tv = new CTime.timeval();
		caller.verify(() -> lib().libusb_get_next_timeout(ctx, tv.getPointer()),
			"libusb_get_next_timeout", ctx, tv);
		return Struct.read(tv);
	}

	public static ArrayPointer<libusb_pollfd> libusb_get_pollfds(libusb_context ctx)
		throws LibUsbException {
		Pointer p = caller.callType(() -> lib().libusb_get_pollfds(ctx), "libusb_get_pollfds", ctx);
		return ArrayPointer.byRef(p, libusb_pollfd::new, libusb_pollfd[]::new);
	}

	public static void libusb_free_pollfds(ArrayPointer<libusb_pollfd> pollFds)
		throws LibUsbException {
		Pointer p = PointerUtil.pointer(pollFds);
		if (p != null) caller.call(() -> lib().libusb_free_pollfds(p), "libusb_free_pollfds", p);
	}

	public static void libusb_set_pollfd_notifiers(libusb_context ctx,
		libusb_pollfd_added_cb added_cb, libusb_pollfd_removed_cb removed_cb, Pointer user_data)
		throws LibUsbException {
		caller.call(() -> lib().libusb_set_pollfd_notifiers(ctx, added_cb, removed_cb, user_data),
			"libusb_set_pollfd_notifiers", ctx, added_cb, removed_cb, user_data);
	}

	public static libusb_hotplug_callback_handle libusb_hotplug_register_callback(
		libusb_context ctx, /* libusb_hotplug_event */ int events,
		/* libusb_hotplug_flag */ int flags, int vendor_id, int product_id,
		/* libusb_class_code|-1 */ int dev_class, libusb_hotplug_callback_fn cb_fn,
		Pointer user_data) throws LibUsbException {
		IntByReference handle = new IntByReference();
		caller.verify(
			() -> lib().libusb_hotplug_register_callback(ctx, events, flags, vendor_id, product_id,
				dev_class, cb_fn, user_data, handle),
			"libusb_hotplug_register_callback", ctx, events, flags, vendor_id, product_id,
			dev_class, cb_fn, user_data, handle);
		return new libusb_hotplug_callback_handle(handle.getValue());
	}

	public static void libusb_hotplug_deregister_callback(libusb_context ctx,
		libusb_hotplug_callback_handle handle) throws LibUsbException {
		if (handle != null)
			caller.call(() -> lib().libusb_hotplug_deregister_callback(ctx, handle.value),
				"libusb_hotplug_deregister_callback", ctx, handle.value);
	}

	public static short libusb_cpu_to_le16(short x) {
		return (short) Basics.ternaryInt(ByteUtil.IS_BIG_ENDIAN, Short.reverseBytes(x), x);
	}

	public static short libusb_le16_to_cpu(short x) {
		return libusb_cpu_to_le16(x);
	}

	private static int updatePosition(ByteBuffer buffer, int inc) {
		if (buffer != null && inc != 0)
			buffer.position(Math.min(buffer.position() + inc, buffer.limit()));
		return inc;
	}

	private static LibUsbNative lib() {
		return library.get();
	}
}
