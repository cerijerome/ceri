package ceri.serial.jna.libusb;

import static ceri.serial.jna.JnaUtil.verify;
import static com.sun.jna.Pointer.NULL;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import com.sun.jna.Callback;
import com.sun.jna.Library;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import ceri.common.collection.ImmutableUtil;
import ceri.serial.jna.JnaUtil;
import ceri.serial.jna.TypedPointer;

public class LibUsb {
	private static final ILibUsb LIBUSB = JnaUtil.loadLibrary("usb-1.0.0", ILibUsb.class);
	public static final int LIBUSB_API_VERSION = 0x01000104;
	public static final int LIBUSBX_API_VERSION = LIBUSB_API_VERSION;

	public static short libusb_cpu_to_le16(short x) {
		return (short) (((x & 0xff) << 8) | ((x & 0xff00) >> 8));
	}

	public static short libusb_le16_to_cpu(short x) {
		return libusb_cpu_to_le16(x);
	}

	public static enum libusb_class_code {
		LIBUSB_CLASS_PER_INTERFACE(0),
		LIBUSB_CLASS_AUDIO(1),
		LIBUSB_CLASS_COMM(2),
		LIBUSB_CLASS_HID(3),
		LIBUSB_CLASS_PHYSICAL(5),
		LIBUSB_CLASS_PRINTER(7),
		LIBUSB_CLASS_PTP(6),
		LIBUSB_CLASS_IMAGE(6),
		LIBUSB_CLASS_MASS_STORAGE(8),
		LIBUSB_CLASS_HUB(9),
		LIBUSB_CLASS_DATA(10),
		LIBUSB_CLASS_SMART_CARD(0x0b),
		LIBUSB_CLASS_CONTENT_SECURITY(0x0d),
		LIBUSB_CLASS_VIDEO(0x0e),
		LIBUSB_CLASS_PERSONAL_HEALTHCARE(0x0f),
		LIBUSB_CLASS_DIAGNOSTIC_DEVICE(0xdc),
		LIBUSB_CLASS_WIRELESS(0xe0),
		LIBUSB_CLASS_APPLICATION(0xfe),
		LIBUSB_CLASS_VENDOR_SPEC(0xff);

		private static final Map<Integer, libusb_class_code> lookup =
			ImmutableUtil.enumMap(t -> t.value, libusb_class_code.class);
		public final int value;

		private libusb_class_code(int value) {
			this.value = value;
		}

		public static libusb_class_code from(int value) {
			return lookup.get(value);
		}
	}

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

		private static final Map<Integer, libusb_descriptor_type> lookup =
			ImmutableUtil.enumMap(t -> t.value, libusb_descriptor_type.class);
		public final int value;

		private libusb_descriptor_type(int value) {
			this.value = value;
		}

		public static libusb_descriptor_type from(int value) {
			return lookup.get(value);
		}
	}

	//
	public static final int LIBUSB_DT_DEVICE_SIZE = 18;
	public static final int LIBUSB_DT_CONFIG_SIZE = 9;
	public static final int LIBUSB_DT_INTERFACE_SIZE = 9;
	public static final int LIBUSB_DT_ENDPOINT_SIZE = 7;
	public static final int LIBUSB_DT_ENDPOINT_AUDIO_SIZE = 9;
	public static final int LIBUSB_DT_HUB_NONVAR_SIZE = 7;
	public static final int LIBUSB_DT_SS_ENDPOINT_COMPANION_SIZE = 6;
	public static final int LIBUSB_DT_BOS_SIZE = 5;
	public static final int LIBUSB_DT_DEVICE_CAPABILITY_SIZE = 3;
	//
	public static final int LIBUSB_BT_USB_2_0_EXTENSION_SIZE = 7;
	public static final int LIBUSB_BT_SS_USB_DEVICE_CAPABILITY_SIZE = 10;
	public static final int LIBUSB_BT_CONTAINER_ID_SIZE = 20;
	public static final int LIBUSB_DT_BOS_MAX_SIZE =
		LIBUSB_DT_BOS_SIZE + LIBUSB_BT_USB_2_0_EXTENSION_SIZE +
			LIBUSB_BT_SS_USB_DEVICE_CAPABILITY_SIZE + LIBUSB_BT_CONTAINER_ID_SIZE;
	//
	public static final int LIBUSB_ENDPOINT_ADDRESS_MASK = 0x0f;
	public static final int LIBUSB_ENDPOINT_DIR_MASK = 0x80;

	public static enum libusb_endpoint_direction {
		LIBUSB_ENDPOINT_IN(0x80),
		LIBUSB_ENDPOINT_OUT(0x00);

		private static final Map<Integer, libusb_endpoint_direction> lookup =
			ImmutableUtil.enumMap(t -> (int) t.value, libusb_endpoint_direction.class);
		public final byte value;

		private libusb_endpoint_direction(int value) {
			this.value = (byte) value;
		}

		public static libusb_endpoint_direction from(int value) {
			return lookup.get(value);
		}
	}

	public static final int LIBUSB_TRANSFER_TYPE_MASK = 0x03;

	public static enum libusb_transfer_type {
		LIBUSB_TRANSFER_TYPE_CONTROL(0),
		LIBUSB_TRANSFER_TYPE_ISOCHRONOUS(1),
		LIBUSB_TRANSFER_TYPE_BULK(2),
		LIBUSB_TRANSFER_TYPE_INTERRUPT(3),
		LIBUSB_TRANSFER_TYPE_BULK_STREAM(4);

		private static final Map<Integer, libusb_transfer_type> lookup =
			ImmutableUtil.enumMap(t -> (int) t.value, libusb_transfer_type.class);
		public final byte value;

		private libusb_transfer_type(int value) {
			this.value = (byte) value;
		}

		public static libusb_transfer_type from(int value) {
			return lookup.get(value);
		}
	}

	public static enum libusb_standard_request {
		LIBUSB_REQUEST_GET_STATUS(0x00),
		LIBUSB_REQUEST_CLEAR_FEATURE(0x01),
		LIBUSB_REQUEST_SET_FEATURE(0x03),
		LIBUSB_REQUEST_SET_ADDRESS(0x05),
		LIBUSB_REQUEST_GET_DESCRIPTOR(0x06),
		LIBUSB_REQUEST_SET_DESCRIPTOR(0x07),
		LIBUSB_REQUEST_GET_CONFIGURATION(0x08),
		LIBUSB_REQUEST_SET_CONFIGURATION(0x09),
		LIBUSB_REQUEST_GET_INTERFACE(0x0A),
		LIBUSB_REQUEST_SET_INTERFACE(0x0B),
		LIBUSB_REQUEST_SYNCH_FRAME(0x0C),
		LIBUSB_REQUEST_SET_SEL(0x30),
		LIBUSB_SET_ISOCH_DELAY(0x31);

		private static final Map<Integer, libusb_standard_request> lookup =
			ImmutableUtil.enumMap(t -> (int) t.value, libusb_standard_request.class);
		public final byte value;

		private libusb_standard_request(int value) {
			this.value = (byte) value;
		}

		public static libusb_standard_request from(int value) {
			return lookup.get(value);
		}
	}

	public static enum libusb_request_type {
		LIBUSB_REQUEST_TYPE_STANDARD((0x00 << 5)),
		LIBUSB_REQUEST_TYPE_CLASS((0x01 << 5)),
		LIBUSB_REQUEST_TYPE_VENDOR((0x02 << 5)),
		LIBUSB_REQUEST_TYPE_RESERVED((0x03 << 5));

		private static final Map<Integer, libusb_request_type> lookup =
			ImmutableUtil.enumMap(t -> t.value, libusb_request_type.class);
		public final int value;

		private libusb_request_type(int value) {
			this.value = value;
		}

		public static libusb_request_type from(int value) {
			return lookup.get(value);
		}
	}

	public static enum libusb_request_recipient {
		LIBUSB_RECIPIENT_DEVICE(0x00),
		LIBUSB_RECIPIENT_INTERFACE(0x01),
		LIBUSB_RECIPIENT_ENDPOINT(0x02),
		LIBUSB_RECIPIENT_OTHER(0x03);

		private static final Map<Integer, libusb_request_recipient> lookup =
			ImmutableUtil.enumMap(t -> t.value, libusb_request_recipient.class);
		public final int value;

		private libusb_request_recipient(int value) {
			this.value = value;
		}

		public static libusb_request_recipient from(int value) {
			return lookup.get(value);
		}
	}

	public static final int LIBUSB_ISO_SYNC_TYPE_MASK = 0x0C;

	public static enum libusb_iso_sync_type {
		LIBUSB_ISO_SYNC_TYPE_NONE(0),
		LIBUSB_ISO_SYNC_TYPE_ASYNC(1),
		LIBUSB_ISO_SYNC_TYPE_ADAPTIVE(2),
		LIBUSB_ISO_SYNC_TYPE_SYNC(3);

		private static final Map<Integer, libusb_iso_sync_type> lookup =
			ImmutableUtil.enumMap(t -> t.value, libusb_iso_sync_type.class);
		public final int value;

		private libusb_iso_sync_type(int value) {
			this.value = value;
		}

		public static libusb_iso_sync_type from(int value) {
			return lookup.get(value);
		}
	}

	public static final int LIBUSB_ISO_USAGE_TYPE_MASK = 0x30;

	public static enum libusb_iso_usage_type {
		LIBUSB_ISO_USAGE_TYPE_DATA(0),
		LIBUSB_ISO_USAGE_TYPE_FEEDBACK(1),
		LIBUSB_ISO_USAGE_TYPE_IMPLICIT(2);

		private static final Map<Integer, libusb_iso_usage_type> lookup =
			ImmutableUtil.enumMap(t -> t.value, libusb_iso_usage_type.class);
		public final int value;

		private libusb_iso_usage_type(int value) {
			this.value = value;
		}

		public static libusb_iso_usage_type from(int value) {
			return lookup.get(value);
		}
	}

	public static class libusb_device_descriptor extends Structure {
		private static final List<String> FIELDS = List.of( //
			"bLength", "bDescriptorType", "bcdUSB", "bDeviceClass", "bDeviceSubClass",
			"bDeviceProtocol", "bMaxPacketSize0", "idVendor", "idProduct", "bcdDevice",
			"iManufacturer", "iProduct", "iSerialNumber", "bNumConfigurations");

		public static class ByValue extends libusb_device_descriptor //
			implements Structure.ByValue {}

		public static class ByReference extends libusb_device_descriptor //
			implements Structure.ByReference {}

		public byte bLength;
		public byte bDescriptorType;
		public short bcdUSB;
		public byte bDeviceClass;
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

		public libusb_device_descriptor() {}

		public libusb_device_descriptor(Pointer p) {
			super(p);
		}

		@Override
		protected List<String> getFieldOrder() {
			return FIELDS;
		}
	}

	public static class libusb_endpoint_descriptor extends Structure {
		private static final List<String> FIELDS = List.of( //
			"bLength", "bDescriptorType", "bEndpointAddress", "bmAttributes", "wMaxPacketSize",
			"bInterval", "bRefresh", "bSynchAddress");

		public static class ByValue extends libusb_endpoint_descriptor //
			implements Structure.ByValue {}

		public static class ByReference extends libusb_endpoint_descriptor //
			implements Structure.ByReference {}

		public byte bLength;
		public byte bDescriptorType;
		public byte bEndpointAddress;
		public byte bmAttributes;
		public short wMaxPacketSize;
		public byte bInterval;
		public byte bRefresh;
		public byte bSynchAddress;

		public libusb_endpoint_descriptor() {}

		public libusb_endpoint_descriptor(Pointer p) {
			super(p);
		}

		@Override
		protected List<String> getFieldOrder() {
			return FIELDS;
		}
	}

	public static class libusb_interface_descriptor extends Structure {
		private static final List<String> FIELDS = List.of( //
			"bLength", "bDescriptorType", "bInterfaceNumber", "bAlternateSetting", "bNumEndpoints",
			"bInterfaceClass", "bInterfaceSubClass", "bInterfaceProtocol", "iInterface", "endpoint",
			"extra", "extra_length");

		public static class ByValue extends libusb_interface_descriptor //
			implements Structure.ByValue {}

		public static class ByReference extends libusb_interface_descriptor //
			implements Structure.ByReference {}

		public byte bLength;
		public byte bDescriptorType;
		public byte bInterfaceNumber;
		public byte bAlternateSetting;
		public byte bNumEndpoints;
		public byte bInterfaceClass;
		public byte bInterfaceSubClass;
		public byte bInterfaceProtocol;
		public byte iInterface;
		public libusb_endpoint_descriptor.ByReference endpoint;
		public Pointer extra;
		public int extra_length;

		public libusb_interface_descriptor() {}

		public libusb_interface_descriptor(Pointer p) {
			super(p);
		}

		@Override
		protected List<String> getFieldOrder() {
			return FIELDS;
		}
	}

	public static class libusb_interface extends Structure {
		private static final List<String> FIELDS = List.of( //
			"altsetting", "num_altsetting");

		public static class ByValue extends libusb_interface //
			implements Structure.ByValue {}

		public static class ByReference extends libusb_interface //
			implements Structure.ByReference {}

		public libusb_interface_descriptor.ByReference altsetting;
		public int num_altsetting;

		public libusb_interface() {}

		public libusb_interface(Pointer p) {
			super(p);
		}

		@Override
		protected List<String> getFieldOrder() {
			return FIELDS;
		}
	}

	public static class libusb_config_descriptor extends Structure {
		private static final List<String> FIELDS = List.of( //
			"bLength", "bDescriptorType", "wTotalLength", "bNumInterfaces", "bConfigurationValue",
			"iConfiguration", "bmAttributes", "MaxPower", "_interface", "extra", "extra_length");

		public static class ByValue extends libusb_config_descriptor //
			implements Structure.ByValue {}

		public static class ByReference extends libusb_config_descriptor //
			implements Structure.ByReference {}

		public byte bLength;
		public byte bDescriptorType;
		public short wTotalLength;
		public byte bNumInterfaces;
		public byte bConfigurationValue;
		public byte iConfiguration;
		public byte bmAttributes;
		public byte MaxPower;
		public libusb_interface.ByReference _interface;
		public Pointer extra;
		public int extra_length;

		public libusb_config_descriptor() {}

		public libusb_config_descriptor(Pointer p) {
			super(p);
		}

		@Override
		protected List<String> getFieldOrder() {
			return FIELDS;
		}
	}

	public static class libusb_ss_endpoint_companion_descriptor extends Structure {
		private static final List<String> FIELDS = List.of( //
			"bLength", "bDescriptorType", "bMaxBurst", "bmAttributes", "wBytesPerInterval");

		public static class ByValue extends libusb_ss_endpoint_companion_descriptor //
			implements Structure.ByValue {}

		public static class ByReference extends libusb_ss_endpoint_companion_descriptor //
			implements Structure.ByReference {}

		public byte bLength;
		public byte bDescriptorType;
		public byte bMaxBurst;
		public byte bmAttributes;
		public short wBytesPerInterval;

		public libusb_ss_endpoint_companion_descriptor() {}

		public libusb_ss_endpoint_companion_descriptor(Pointer p) {
			super(p);
		}

		@Override
		protected List<String> getFieldOrder() {
			return FIELDS;
		}
	}

	public static class libusb_bos_dev_capability_descriptor extends Structure {
		private static final List<String> FIELDS = List.of( //
			"bLength", "bDescriptorType", "bDevCapabilityType", "dev_capability_data");

		public static class ByValue extends libusb_bos_dev_capability_descriptor //
			implements Structure.ByValue {}

		public static class ByReference extends libusb_bos_dev_capability_descriptor //
			implements Structure.ByReference {}

		public byte bLength;
		public byte bDescriptorType;
		public byte bDevCapabilityType;
		public Pointer dev_capability_data;

		public libusb_bos_dev_capability_descriptor() {}

		public libusb_bos_dev_capability_descriptor(Pointer p) {
			super(p);
		}

		@Override
		protected List<String> getFieldOrder() {
			return FIELDS;
		}
	}

	public static class libusb_bos_descriptor extends Structure {
		private static final List<String> FIELDS = List.of( //
			"bLength", "bDescriptorType", "wTotalLength", "bNumDeviceCaps", "dev_capability");

		public static class ByValue extends libusb_bos_descriptor //
			implements Structure.ByValue {}

		public static class ByReference extends libusb_bos_descriptor //
			implements Structure.ByReference {}

		public byte bLength;
		public byte bDescriptorType;
		public short wTotalLength;
		public byte bNumDeviceCaps;
		public libusb_bos_dev_capability_descriptor.ByReference dev_capability;

		public libusb_bos_descriptor() {}

		public libusb_bos_descriptor(Pointer p) {
			super(p);
		}

		@Override
		protected List<String> getFieldOrder() {
			return FIELDS;
		}

	}

	public static class libusb_usb_2_0_extension_descriptor extends Structure {
		private static final List<String> FIELDS = List.of( //
			"bLength", "bDescriptorType", "bDevCapabilityType", "bmAttributes");

		public static class ByValue extends libusb_usb_2_0_extension_descriptor //
			implements Structure.ByValue {}

		public static class ByReference extends libusb_usb_2_0_extension_descriptor //
			implements Structure.ByReference {}

		public byte bLength;
		public byte bDescriptorType;
		public byte bDevCapabilityType;
		public int bmAttributes;

		public libusb_usb_2_0_extension_descriptor() {}

		public libusb_usb_2_0_extension_descriptor(Pointer p) {
			super(p);
		}

		@Override
		protected List<String> getFieldOrder() {
			return FIELDS;
		}
	}

	public static class libusb_ss_usb_device_capability_descriptor extends Structure {
		private static final List<String> FIELDS = List.of( //
			"bLength", "bDescriptorType", "bDevCapabilityType", "bmAttributes", "wSpeedSupported",
			"bFunctionalitySupport", "bU1DevExitLat", "bU2DevExitLat");

		public static class ByValue extends libusb_ss_usb_device_capability_descriptor //
			implements Structure.ByValue {}

		public static class ByReference extends libusb_ss_usb_device_capability_descriptor //
			implements Structure.ByReference {}

		public byte bLength;
		public byte bDescriptorType;
		public byte bDevCapabilityType;
		public byte bmAttributes;
		public short wSpeedSupported;
		public byte bFunctionalitySupport;
		public byte bU1DevExitLat;
		public short bU2DevExitLat;

		public libusb_ss_usb_device_capability_descriptor() {}

		public libusb_ss_usb_device_capability_descriptor(Pointer p) {
			super(p);
		}

		@Override
		protected List<String> getFieldOrder() {
			return FIELDS;
		}
	}

	public static class libusb_container_id_descriptor extends Structure {
		private static final List<String> FIELDS = List.of( //
			"bLength", "bDescriptorType", "bDevCapabilityType", "bReserved", "ContainerID");

		public static class ByValue extends libusb_container_id_descriptor //
			implements Structure.ByValue {}

		public static class ByReference extends libusb_container_id_descriptor //
			implements Structure.ByReference {}

		public byte bLength;
		public byte bDescriptorType;
		public byte bDevCapabilityType;
		public byte bReserved;
		public byte[] ContainerID = new byte[16];

		public libusb_container_id_descriptor() {}

		public libusb_container_id_descriptor(Pointer p) {
			super(p);
		}

		@Override
		protected List<String> getFieldOrder() {
			return FIELDS;
		}
	}

	public static class libusb_control_setup extends Structure {
		private static final List<String> FIELDS = List.of( //
			"bmRequestType", "bRequest", "wValue", "wIndex", "wLength");

		public static class ByValue extends libusb_control_setup //
			implements Structure.ByValue {}

		public static class ByReference extends libusb_control_setup //
			implements Structure.ByReference {}

		public byte bmRequestType;
		public byte bRequest;
		public short wValue;
		public short wIndex;
		public short wLength;

		public libusb_control_setup() {}

		public libusb_control_setup(Pointer p) {
			super(p);
		}

		@Override
		protected List<String> getFieldOrder() {
			return FIELDS;
		}
	}

	public static final int LIBUSB_CONTROL_SETUP_SIZE = new libusb_control_setup().size();

	public static class libusb_version extends Structure {
		private static final List<String> FIELDS = List.of( //
			"major", "minor", "micro", "nano", "rc", "describe");

		public static class ByValue extends libusb_version //
			implements Structure.ByValue {}

		public static class ByReference extends libusb_version //
			implements Structure.ByReference {}

		public short major;
		public short minor;
		public short micro;
		public short nano;
		public String rc;
		public String describe;

		public libusb_version() {}

		public libusb_version(Pointer p) {
			super(p);
		}

		@Override
		protected List<String> getFieldOrder() {
			return FIELDS;
		}
	}

	public static enum libusb_speed {
		LIBUSB_SPEED_UNKNOWN(0),
		LIBUSB_SPEED_LOW(1),
		LIBUSB_SPEED_FULL(2),
		LIBUSB_SPEED_HIGH(3),
		LIBUSB_SPEED_SUPER(4);

		private static final Map<Integer, libusb_speed> lookup =
			ImmutableUtil.enumMap(t -> t.value, libusb_speed.class);
		public final int value;

		private libusb_speed(int value) {
			this.value = value;
		}

		public static libusb_speed from(int value) {
			return lookup.get(value);
		}
	}

	public static enum libusb_supported_speed {
		LIBUSB_LOW_SPEED_OPERATION(1),
		LIBUSB_FULL_SPEED_OPERATION(2),
		LIBUSB_HIGH_SPEED_OPERATION(4),
		LIBUSB_SUPER_SPEED_OPERATION(8);

		private static final Map<Integer, libusb_supported_speed> lookup =
			ImmutableUtil.enumMap(t -> t.value, libusb_supported_speed.class);
		public final int value;

		private libusb_supported_speed(int value) {
			this.value = value;
		}

		public static libusb_supported_speed from(int value) {
			return lookup.get(value);
		}
	}

	public static enum libusb_usb_2_0_extension_attributes {
		LIBUSB_BM_LPM_SUPPORT(2);

		private static final Map<Integer, libusb_usb_2_0_extension_attributes> lookup =
			ImmutableUtil.enumMap(t -> t.value, libusb_usb_2_0_extension_attributes.class);
		public final int value;

		private libusb_usb_2_0_extension_attributes(int value) {
			this.value = value;
		}

		public static libusb_usb_2_0_extension_attributes from(int value) {
			return lookup.get(value);
		}
	}

	public static enum libusb_ss_usb_device_capability_attributes {
		LIBUSB_BM_LTM_SUPPORT(2);

		private static final Map<Integer, libusb_ss_usb_device_capability_attributes> lookup =
			ImmutableUtil.enumMap(t -> t.value, libusb_ss_usb_device_capability_attributes.class);
		public final int value;

		private libusb_ss_usb_device_capability_attributes(int value) {
			this.value = value;
		}

		public static libusb_ss_usb_device_capability_attributes from(int value) {
			return lookup.get(value);
		}
	}

	public enum libusb_bos_type {
		LIBUSB_BT_WIRELESS_USB_DEVICE_CAPABILITY(1),
		LIBUSB_BT_USB_2_0_EXTENSION(2),
		LIBUSB_BT_SS_USB_DEVICE_CAPABILITY(3),
		LIBUSB_BT_CONTAINER_ID(4);

		private static final Map<Integer, libusb_bos_type> lookup =
			ImmutableUtil.enumMap(t -> t.value, libusb_bos_type.class);
		public final int value;

		private libusb_bos_type(int value) {
			this.value = value;
		}

		public static libusb_bos_type from(int value) {
			return lookup.get(value);
		}
	}

	public enum libusb_error {
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

		private static final Map<Integer, libusb_error> lookup =
			ImmutableUtil.enumMap(t -> t.value, libusb_error.class);
		public final int value;

		private libusb_error(int value) {
			this.value = value;
		}

		public static libusb_error from(int value) {
			return lookup.get(value);
		}
	}

	public static final int LIBUSB_ERROR_COUNT = 14;

	public static enum libusb_transfer_status {
		LIBUSB_TRANSFER_COMPLETED(0),
		LIBUSB_TRANSFER_ERROR(1),
		LIBUSB_TRANSFER_TIMED_OUT(2),
		LIBUSB_TRANSFER_CANCELLED(3),
		LIBUSB_TRANSFER_STALL(4),
		LIBUSB_TRANSFER_NO_DEVICE(5),
		LIBUSB_TRANSFER_OVERFLOW(6);

		private static final Map<Integer, libusb_transfer_status> lookup =
			ImmutableUtil.enumMap(t -> t.value, libusb_transfer_status.class);
		public final int value;

		private libusb_transfer_status(int value) {
			this.value = value;
		}

		public static libusb_transfer_status from(int value) {
			return lookup.get(value);
		}
	}

	public static enum libusb_transfer_flags {
		LIBUSB_TRANSFER_SHORT_NOT_OK(1 << 0),
		LIBUSB_TRANSFER_FREE_BUFFER(1 << 1),
		LIBUSB_TRANSFER_FREE_TRANSFER(1 << 2),
		LIBUSB_TRANSFER_ADD_ZERO_PACKET(1 << 3);

		private static final Map<Integer, libusb_transfer_flags> lookup =
			ImmutableUtil.enumMap(t -> t.value, libusb_transfer_flags.class);
		public final int value;

		private libusb_transfer_flags(int value) {
			this.value = value;
		}

		public static libusb_transfer_flags from(int value) {
			return lookup.get(value);
		}
	}

	public static class libusb_iso_packet_descriptor extends Structure {
		private static final List<String> FIELDS = List.of( //
			"length", "actual_length", "status");

		public static class ByValue extends libusb_iso_packet_descriptor //
			implements Structure.ByValue {}

		public static class ByReference extends libusb_iso_packet_descriptor //
			implements Structure.ByReference {}

		public int length;
		public int actual_length;
		public int status; // libusb_transfer_status

		public libusb_iso_packet_descriptor() {}

		public libusb_iso_packet_descriptor(Pointer p) {
			super(p);
		}

		@Override
		protected List<String> getFieldOrder() {
			return FIELDS;
		}
	}

	public static interface libusb_transfer_cb_fn extends Callback {
		public void invoke(libusb_transfer transfer);
	}

	public static class libusb_transfer extends Structure {
		private static final List<String> FIELDS = List.of( //
			"dev_handle", "flags", "endpoint", "type", "timeout", "status", "length",
			"actual_length", "callback", "user_data", "buffer", "num_iso_packets",
			"iso_packet_desc");

		public static class ByValue extends libusb_transfer //
			implements Structure.ByValue {}

		public static class ByReference extends libusb_transfer //
			implements Structure.ByReference {}

		public Pointer dev_handle;
		public byte flags;
		public byte endpoint;
		public byte type;
		public int timeout;
		public int status; // libusb_transfer_status
		public int length;
		public int actual_length;
		public libusb_transfer_cb_fn callback;
		public Pointer user_data;
		public Pointer buffer;
		public int num_iso_packets;
		// TODO: is correct?
		public libusb_iso_packet_descriptor.ByReference[] iso_packet_desc;

		public libusb_transfer() {}

		public libusb_transfer(Pointer p) {
			super(p);
		}

		@Override
		protected List<String> getFieldOrder() {
			return FIELDS;
		}
	}

	public static enum libusb_capability {
		LIBUSB_CAP_HAS_CAPABILITY(0x0000),
		LIBUSB_CAP_HAS_HOTPLUG(0x0001),
		LIBUSB_CAP_HAS_HID_ACCESS(0x0100),
		LIBUSB_CAP_SUPPORTS_DETACH_KERNEL_DRIVER(0x0101);

		private static final Map<Integer, libusb_capability> lookup =
			ImmutableUtil.enumMap(t -> t.value, libusb_capability.class);
		public final int value;

		private libusb_capability(int value) {
			this.value = value;
		}

		public static libusb_capability from(int value) {
			return lookup.get(value);
		}
	}

	public enum libusb_log_level {
		LIBUSB_LOG_LEVEL_NONE(0),
		LIBUSB_LOG_LEVEL_ERROR(1),
		LIBUSB_LOG_LEVEL_WARNING(2),
		LIBUSB_LOG_LEVEL_INFO(3),
		LIBUSB_LOG_LEVEL_DEBUG(4);

		private static final Map<Integer, libusb_log_level> lookup =
			ImmutableUtil.enumMap(t -> t.value, libusb_log_level.class);
		public final int value;

		private libusb_log_level(int value) {
			this.value = value;
		}

		public static libusb_log_level from(int value) {
			return lookup.get(value);
		}
	}

	public static Pointer libusb_control_transfer_get_data(libusb_transfer transfer) {
		return transfer.buffer.share(LIBUSB_CONTROL_SETUP_SIZE);
	}

	public static Pointer libusb_control_transfer_get_setup(libusb_transfer transfer) {
		return transfer.buffer;
	}

	public static void libusb_fill_control_setup(Pointer buffer, byte bmRequestType, byte bRequest,
		short wValue, short wIndex, short wLength) {
		libusb_control_setup setup = new libusb_control_setup(buffer);
		setup.bmRequestType = bmRequestType;
		setup.bRequest = bRequest;
		setup.wValue = libusb_cpu_to_le16(wValue);
		setup.wIndex = libusb_cpu_to_le16(wIndex);
		setup.wLength = libusb_cpu_to_le16(wLength);
	}

	public static void libusb_fill_control_transfer(libusb_transfer transfer, Pointer dev_handle,
		Pointer buffer, libusb_transfer_cb_fn callback, Pointer user_data, int timeout) {
		libusb_control_setup setup = new libusb_control_setup(buffer);
		transfer.dev_handle = dev_handle;
		transfer.endpoint = 0;
		transfer.type = libusb_transfer_type.LIBUSB_TRANSFER_TYPE_CONTROL.value;
		transfer.timeout = timeout;
		transfer.buffer = buffer;
		if (JnaUtil.isNonZero(setup))
			transfer.length = LIBUSB_CONTROL_SETUP_SIZE + libusb_le16_to_cpu(setup.wLength);
		transfer.user_data = user_data;
		transfer.callback = callback;
	}

	public static void libusb_fill_bulk_transfer(libusb_transfer transfer, Pointer dev_handle,
		byte endpoint, Pointer buffer, int length, libusb_transfer_cb_fn callback,
		Pointer user_data, int timeout) {
		transfer.dev_handle = dev_handle;
		transfer.endpoint = endpoint;
		transfer.type = libusb_transfer_type.LIBUSB_TRANSFER_TYPE_BULK.value;
		transfer.timeout = timeout;
		transfer.buffer = buffer;
		transfer.length = length;
		transfer.user_data = user_data;
		transfer.callback = callback;
	}

	public static void libusb_fill_bulk_stream_transfer(libusb_transfer transfer,
		Pointer dev_handle, byte endpoint, int stream_id, Pointer buffer, int length,
		libusb_transfer_cb_fn callback, Pointer user_data, int timeout) {
		libusb_fill_bulk_transfer(transfer, dev_handle, endpoint, buffer, length, callback,
			user_data, timeout);
		transfer.type = libusb_transfer_type.LIBUSB_TRANSFER_TYPE_BULK_STREAM.value;
		transfer.write(); // TODO: is needed?
		LIBUSB.libusb_transfer_set_stream_id(transfer, stream_id);
	}

	public static void libusb_fill_interrupt_transfer(libusb_transfer transfer, Pointer dev_handle,
		byte endpoint, Pointer buffer, int length, libusb_transfer_cb_fn callback,
		Pointer user_data, int timeout) {
		transfer.dev_handle = dev_handle;
		transfer.endpoint = endpoint;
		transfer.type = libusb_transfer_type.LIBUSB_TRANSFER_TYPE_INTERRUPT.value;
		transfer.timeout = timeout;
		transfer.buffer = buffer;
		transfer.length = length;
		transfer.user_data = user_data;
		transfer.callback = callback;
	}

	public static void libusb_fill_iso_transfer(libusb_transfer transfer, Pointer dev_handle,
		byte endpoint, Pointer buffer, int length, int num_iso_packets,
		libusb_transfer_cb_fn callback, Pointer user_data, int timeout) {
		transfer.dev_handle = dev_handle;
		transfer.endpoint = endpoint;
		transfer.type = libusb_transfer_type.LIBUSB_TRANSFER_TYPE_ISOCHRONOUS.value;
		transfer.timeout = timeout;
		transfer.buffer = buffer;
		transfer.length = length;
		transfer.num_iso_packets = num_iso_packets;
		transfer.user_data = user_data;
		transfer.callback = callback;
	}

	public static void libusb_set_iso_packet_lengths(libusb_transfer transfer, int length) {
		for (int i = 0; i < transfer.num_iso_packets; i++)
			transfer.iso_packet_desc[i].length = length;
	}

	public static Pointer libusb_get_iso_packet_buffer(libusb_transfer transfer, int packet) {
		int offset = 0;
		if (packet > Short.MAX_VALUE) return NULL;
		if (packet >= transfer.num_iso_packets) return NULL;
		for (int i = 0; i < packet; i++)
			offset += transfer.iso_packet_desc[i].length;
		return transfer.buffer.share(offset);
	}

	public static Pointer libusb_get_iso_packet_buffer_simple(libusb_transfer transfer,
		int packet) {
		if (packet > Short.MAX_VALUE) return NULL;
		if (packet >= transfer.num_iso_packets) return NULL;
		return transfer.buffer.share(transfer.iso_packet_desc[0].length * packet);
	}

	public static int libusb_get_descriptor(Pointer dev, byte desc_type, byte desc_index,
		Pointer data, int length) {
		return LIBUSB.libusb_control_transfer(dev,
			libusb_endpoint_direction.LIBUSB_ENDPOINT_IN.value,
			libusb_standard_request.LIBUSB_REQUEST_GET_DESCRIPTOR.value,
			(short) ((desc_type << 8) | desc_index), (short) 0, data, (short) length, 1000);
	}

	public static int libusb_get_string_descriptor(Pointer dev, byte desc_index, short langid,
		Pointer data, int length) {
		return LIBUSB.libusb_control_transfer(dev,
			libusb_endpoint_direction.LIBUSB_ENDPOINT_IN.value,
			libusb_standard_request.LIBUSB_REQUEST_GET_DESCRIPTOR.value,
			(short) ((libusb_descriptor_type.LIBUSB_DT_STRING.value << 8) | desc_index), langid,
			data, (short) length, 1000);
	}

	public static class libusb_pollfd extends Structure {
		private static final List<String> FIELDS = List.of( //
			"fd", "events");

		public static class ByValue extends libusb_pollfd //
			implements Structure.ByValue {}

		public static class ByReference extends libusb_pollfd //
			implements Structure.ByReference {}

		public int fd;
		public short events;

		public libusb_pollfd() {}

		public libusb_pollfd(Pointer p) {
			super(p);
		}

		@Override
		protected List<String> getFieldOrder() {
			return FIELDS;
		}
	}

	public static interface libusb_pollfd_added_cb extends Callback {
		public void invoke(int fd, short events, Pointer user_data);
	}

	public static interface libusb_pollfd_removed_cb extends Callback {
		public void invoke(int fd, Pointer user_data);
	}

	public static enum libusb_hotplug_flag {
		LIBUSB_HOTPLUG_NO_FLAGS(0),
		LIBUSB_HOTPLUG_ENUMERATE(1 << 0);

		private static final Map<Integer, libusb_hotplug_flag> lookup =
			ImmutableUtil.enumMap(t -> t.value, libusb_hotplug_flag.class);
		public final int value;

		private libusb_hotplug_flag(int value) {
			this.value = value;
		}

		public static libusb_hotplug_flag from(int value) {
			return lookup.get(value);
		}
	}

	public static enum libusb_hotplug_event {
		LIBUSB_HOTPLUG_EVENT_DEVICE_ARRIVED(0x01),
		LIBUSB_HOTPLUG_EVENT_DEVICE_LEFT(0x02);

		private static final Map<Integer, libusb_hotplug_event> lookup =
			ImmutableUtil.enumMap(t -> t.value, libusb_hotplug_event.class);
		public final int value;

		private libusb_hotplug_event(int value) {
			this.value = value;
		}

		public static libusb_hotplug_event from(int value) {
			return lookup.get(value);
		}
	}

	public static final Object LIBUSB_HOTPLUG_MATCH_ANY = -1;

	public static interface libusb_hotplug_callback_fn extends Callback {
		public void invoke(Pointer ctx, Pointer device, int event, Pointer user_data);
	}

	// struct libusb_context;
	public static class libusb_context extends TypedPointer {
		public static class ByReference extends TypedPointer.ByReference<libusb_context> {
			public ByReference() {
				super(libusb_context::new);
			}
		}
	}

	// struct libusb_device;
	public static class libusb_device extends TypedPointer {
		public static class ByReference extends TypedPointer.ByReference<libusb_device> {
			public ByReference() {
				super(libusb_device::new);
			}
		}
		public static class ArrayReference extends TypedPointer.ArrayReference<libusb_device> {
			public ArrayReference() {
				super(libusb_device::new, libusb_device[]::new);
			}
		}
	}

	// struct libusb_device_handle;
	public static class libusb_device_handle extends TypedPointer {
		public static class ByReference extends TypedPointer.ByReference<libusb_device_handle> {
			public ByReference() {
				super(libusb_device_handle::new);
			}
		}
	}

	public static void main(String[] args) throws IOException {
		JnaUtil.setProtected();
		ILibUsb usb = LibUsb.LIBUSB;

		libusb_context.ByReference ctxPtr = new libusb_context.ByReference();
		verify(usb.libusb_init(ctxPtr), "init");
		libusb_context ctx = ctxPtr.getValue();

		libusb_version version = usb.libusb_get_version();
		System.out.println(version);

		libusb_device.ArrayReference listRef = new libusb_device.ArrayReference();
		int size = verify(usb.libusb_get_device_list(ctx, listRef), "get_device_list");
		libusb_device[] devices = listRef.toArray(size);
		System.out.printf("%d items", size);
		System.out.println(Arrays.toString(devices));
		
		usb.libusb_free_device_list(devices, size);
		usb.libusb_exit(ctx);
	}

	/**
	 * Native functions mapped to library
	 */
	private static interface ILibUsb extends Library {
		// int LIBUSB_CALL libusb_init(libusb_context **ctx);
		int libusb_init(libusb_context.ByReference ctx);

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

		// ssize_t LIBUSB_CALL libusb_get_device_list(libusb_context *ctx, libusb_device ***list);
		int libusb_get_device_list(libusb_context ctx, libusb_device.ArrayReference list);
		//int libusb_get_device_list(libusb_context ctx, PointerByReference list);

		// void LIBUSB_CALL libusb_free_device_list(libusb_device **list, int unref_devices);
		void libusb_free_device_list(libusb_device[] list, int unref_devices);

		// libusb_device * LIBUSB_CALL libusb_ref_device(libusb_device *dev);
		libusb_device libusb_ref_device(libusb_device dev);

		// void LIBUSB_CALL libusb_unref_device(libusb_device *dev);
		void libusb_unref_device(libusb_device dev);

		// int libusb_get_configuration(Pointer dev, IntByReference config);
		// int libusb_get_device_descriptor(Pointer dev, libusb_device_descriptor desc);
		// int libusb_get_active_config_descriptor(Pointer dev, PointerByReference config);
		// int libusb_get_config_descriptor(Pointer dev, byte config_index, PointerByReference
		// config);
		// int libusb_get_config_descriptor_by_value(Pointer dev,
		// byte bConfigurationValue, struct libusb_config_descriptor **config);
		// void libusb_free_config_descriptor(struct libusb_config_descriptor *config);
		// int libusb_get_ss_endpoint_companion_descriptor(
		// struct Pointer ctx,
		// const struct libusb_endpoint_descriptor *endpoint,
		// struct libusb_ss_endpoint_companion_descriptor **ep_comp);
		// void libusb_free_ss_endpoint_companion_descriptor(
		// struct libusb_ss_endpoint_companion_descriptor *ep_comp);
		// int libusb_get_bos_descriptor(Pointer handle,
		// struct libusb_bos_descriptor **bos);
		// void libusb_free_bos_descriptor(struct libusb_bos_descriptor *bos);
		// int libusb_get_usb_2_0_extension_descriptor(
		// struct Pointer ctx,
		// struct libusb_bos_dev_capability_descriptor *dev_cap,
		// struct libusb_usb_2_0_extension_descriptor **usb_2_0_extension);
		// void libusb_free_usb_2_0_extension_descriptor(
		// struct libusb_usb_2_0_extension_descriptor *usb_2_0_extension);
		// int libusb_get_ss_usb_device_capability_descriptor(
		// struct Pointer ctx,
		// struct libusb_bos_dev_capability_descriptor *dev_cap,
		// struct libusb_ss_usb_device_capability_descriptor **ss_usb_device_cap);
		// void libusb_free_ss_usb_device_capability_descriptor(
		// struct libusb_ss_usb_device_capability_descriptor *ss_usb_device_cap);
		// int libusb_get_container_id_descriptor(struct Pointer ctx,
		// struct libusb_bos_dev_capability_descriptor *dev_cap,
		// struct libusb_container_id_descriptor **container_id);
		// void libusb_free_container_id_descriptor(
		// struct libusb_container_id_descriptor *container_id);
		// byte libusb_get_bus_number(Pointer dev);
		// byte libusb_get_port_number(Pointer dev);
		// int libusb_get_port_numbers(Pointer dev, byte* port_numbers, int
		// port_numbers_len);
		// int libusb_get_port_path(Pointer ctx, Pointer dev, byte* path, byte
		// path_length);
		// Pointer libusb_get_parent(Pointer dev);
		// byte libusb_get_device_address(Pointer dev);
		// int libusb_get_device_speed(Pointer dev);
		// int libusb_get_max_packet_size(Pointer dev,
		// unsigned char endpoint);
		// int libusb_get_max_iso_packet_size(Pointer dev,
		// unsigned char endpoint);
		//
		// int libusb_open(Pointer dev, PointerByReference handle);
		// void libusb_close(Pointer dev_handle);
		// Pointer libusb_get_device(Pointer dev_handle);
		//
		// int libusb_set_configuration(Pointer dev,
		// int configuration);
		// int libusb_claim_interface(Pointer dev,
		// int interface_number);
		// int libusb_release_interface(Pointer dev,
		// int interface_number);
		//
		// Pointer libusb_open_device_with_vid_pid(
		// Pointer ctx, short vendor_id, short product_id);
		//
		// int libusb_set_interface_alt_setting(Pointer dev,
		// int interface_number, int alternate_setting);
		// int libusb_clear_halt(Pointer dev,
		// unsigned char endpoint);
		// int libusb_reset_device(Pointer dev);
		//
		// int libusb_alloc_streams(Pointer dev,
		// int num_streams, unsigned char *endpoints, int num_endpoints);
		// int libusb_free_streams(Pointer dev,
		// unsigned char *endpoints, int num_endpoints);
		//
		// int libusb_kernel_driver_active(Pointer dev,
		// int interface_number);
		// int libusb_detach_kernel_driver(Pointer dev,
		// int interface_number);
		// int libusb_attach_kernel_driver(Pointer dev,
		// int interface_number);
		// int libusb_set_auto_detach_kernel_driver(
		// Pointer dev, int enable);

		// int libusb_control_transfer(Pointer dev_handle, byte request_type, byte
		// bRequest,
		// short wValue, short wIndex, unsigned char *data, short wLength, unsigned int timeout);
		int libusb_control_transfer(Pointer dev_handle, byte request_type, byte bRequest,
			short wValue, short wIndex, Pointer data, short wLength, int timeout);
		//
		// int libusb_bulk_transfer(Pointer dev_handle,
		// unsigned char endpoint, unsigned char *data, int length,
		// int *actual_length, unsigned int timeout);
		//
		// int libusb_interrupt_transfer(Pointer dev_handle,
		// unsigned char endpoint, unsigned char *data, int length,
		// int *actual_length, unsigned int timeout);

		// struct libusb_transfer * libusb_alloc_transfer(int iso_packets);
		// int libusb_submit_transfer(struct libusb_transfer *transfer);
		// int libusb_cancel_transfer(struct libusb_transfer *transfer);
		// void libusb_free_transfer(struct libusb_transfer *transfer);
		// void libusb_transfer_set_stream_id(struct libusb_transfer *transfer, int stream_id);
		void libusb_transfer_set_stream_id(libusb_transfer transfer, int stream_id);
		// int libusb_transfer_get_stream_id(struct libusb_transfer *transfer);

		// int libusb_get_string_descriptor_ascii(Pointer dev,
		// byte desc_index, unsigned char *data, int length);
		//
		// int libusb_try_lock_events(Pointer ctx);
		// void libusb_lock_events(Pointer ctx);
		// void libusb_unlock_events(Pointer ctx);
		// int libusb_event_handling_ok(Pointer ctx);
		// int libusb_event_handler_active(Pointer ctx);
		// void libusb_lock_event_waiters(Pointer ctx);
		// void libusb_unlock_event_waiters(Pointer ctx);
		// int libusb_wait_for_event(Pointer ctx, struct timeval *tv);
		//
		// int libusb_handle_events_timeout(Pointer ctx,
		// struct timeval *tv);
		// int libusb_handle_events_timeout_completed(Pointer ctx,
		// struct timeval *tv, int *completed);
		// int libusb_handle_events(Pointer ctx);
		// int libusb_handle_events_completed(Pointer ctx, int *completed);
		// int libusb_handle_events_locked(Pointer ctx,
		// struct timeval *tv);
		// int libusb_pollfds_handle_timeouts(Pointer ctx);
		// int libusb_get_next_timeout(Pointer ctx,
		// struct timeval *tv);

		// const struct libusb_pollfd ** libusb_get_pollfds(Pointer ctx);
		// void libusb_free_pollfds(const struct libusb_pollfd **pollfds);
		// void libusb_set_pollfd_notifiers(Pointer ctx,
		// libusb_pollfd_added_cb added_cb, libusb_pollfd_removed_cb removed_cb,
		// void *user_data);
		// typedef int libusb_hotplug_callback_handle;

		// int libusb_hotplug_register_callback(Pointer ctx,
		// libusb_hotplug_event events,
		// libusb_hotplug_flag flags,
		// int vendor_id, int product_id,
		// int dev_class,
		// libusb_hotplug_callback_fn cb_fn,
		// void *user_data,
		// libusb_hotplug_callback_handle *handle);
		//
		// void libusb_hotplug_deregister_callback(Pointer ctx,
		// libusb_hotplug_callback_handle handle);

	}

}
