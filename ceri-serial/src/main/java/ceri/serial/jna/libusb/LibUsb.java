package ceri.serial.jna.libusb;

import static ceri.serial.jna.JnaUtil.ubyte;
import static com.sun.jna.Pointer.NULL;
import java.nio.ByteOrder;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.sun.jna.Callback;
import com.sun.jna.Memory;
import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.PointerByReference;
import ceri.common.data.FieldTranscoder;
import ceri.common.data.IntAccessor;
import ceri.common.data.MaskAccessor;
import ceri.common.data.TypeTranscoder;
import ceri.serial.jna.JnaUtil;
//import ceri.serial.jna.JnaUtil;
import ceri.serial.jna.Struct;
import ceri.serial.jna.TypedPointer;

public class LibUsb {
	private static final Logger logger = LogManager.getLogger();
	private static LibUsbNative LIBUSB = loadLibrary("usb-1.0.0");

	private static short libusb_cpu_to_le16(short x) {
		if (ByteOrder.LITTLE_ENDIAN.equals(ByteOrder.nativeOrder())) return x;
		return Short.reverseBytes(x);
	}

	private static short libusb_le16_to_cpu(short x) {
		return libusb_cpu_to_le16(x);
	}

	/**
	 * Device and/or Interface Class codes
	 */
	public static enum libusb_class_code {
		LIBUSB_CLASS_PER_INTERFACE(0),
		LIBUSB_CLASS_AUDIO(1),
		LIBUSB_CLASS_COMM(2),
		LIBUSB_CLASS_HID(3),
		LIBUSB_CLASS_PHYSICAL(5),
		LIBUSB_CLASS_PRINTER(7),
		// LIBUSB_CLASS_PTP(6), // legacy name from libusb-0.1
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

		public static final TypeTranscoder.Single<libusb_class_code> xcoder =
			TypeTranscoder.single(t -> t.value, libusb_class_code.class);
		public final int value;

		private libusb_class_code(int value) {
			this.value = value;
		}

		@Override
		public String toString() {
			return String.format("%s(0x%02x)", name(), value);
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

		public static final TypeTranscoder.Single<libusb_descriptor_type> xcoder =
			TypeTranscoder.single(t -> t.value, libusb_descriptor_type.class);
		public final int value;

		private libusb_descriptor_type(int value) {
			this.value = value;
		}

		@Override
		public String toString() {
			return String.format("%s(0x%02x)", name(), value);
		}
	}

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
	// BOS descriptor sizes
	public static final int LIBUSB_BT_USB_2_0_EXTENSION_SIZE = 7;
	public static final int LIBUSB_BT_SS_USB_DEVICE_CAPABILITY_SIZE = 10;
	public static final int LIBUSB_BT_CONTAINER_ID_SIZE = 20;
	public static final int LIBUSB_DT_BOS_MAX_SIZE =
		LIBUSB_DT_BOS_SIZE + LIBUSB_BT_USB_2_0_EXTENSION_SIZE +
			LIBUSB_BT_SS_USB_DEVICE_CAPABILITY_SIZE + LIBUSB_BT_CONTAINER_ID_SIZE;
	// Endpoint masks
	public static final int LIBUSB_ENDPOINT_ADDRESS_MASK = 0x0f;
	public static final int LIBUSB_ENDPOINT_DIR_MASK = 0x80;

	/**
	 * Endpoint direction. Values for bit 7 of the libusb_endpoint_descriptor.bEndpointAddress
	 * "endpoint address" scheme.
	 */
	public static enum libusb_endpoint_direction {
		LIBUSB_ENDPOINT_IN(0x80),
		LIBUSB_ENDPOINT_OUT(0x00);

		public static final TypeTranscoder.Single<libusb_endpoint_direction> xcoder =
			TypeTranscoder.single(t -> t.value, libusb_endpoint_direction.class);
		public final int value;

		private libusb_endpoint_direction(int value) {
			this.value = value;
		}

		@Override
		public String toString() {
			return String.format("%s(0x%02x)", name(), value);
		}
	}

	public static final int LIBUSB_TRANSFER_TYPE_MASK = 0x03;

	/**
	 * Endpoint transfer type. Values for bits 0:1 of the libusb_endpoint_descriptor.bmAttributes
	 * "endpoint attributes" field.
	 */
	public static enum libusb_transfer_type {
		LIBUSB_TRANSFER_TYPE_CONTROL(0),
		LIBUSB_TRANSFER_TYPE_ISOCHRONOUS(1),
		LIBUSB_TRANSFER_TYPE_BULK(2),
		LIBUSB_TRANSFER_TYPE_INTERRUPT(3),
		LIBUSB_TRANSFER_TYPE_BULK_STREAM(4);

		public static final TypeTranscoder.Single<libusb_transfer_type> xcoder =
			TypeTranscoder.single(t -> t.value, libusb_transfer_type.class);
		public final int value;

		private libusb_transfer_type(int value) {
			this.value = value;
		}

		@Override
		public String toString() {
			return String.format("%s(%d)", name(), value);
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
		LIBUSB_REQUEST_GET_INTERFACE(0x0A),
		LIBUSB_REQUEST_SET_INTERFACE(0x0B),
		LIBUSB_REQUEST_SYNCH_FRAME(0x0C),
		LIBUSB_REQUEST_SET_SEL(0x30),
		LIBUSB_SET_ISOCH_DELAY(0x31);

		public static final TypeTranscoder.Single<libusb_standard_request> xcoder =
			TypeTranscoder.single(t -> t.value, libusb_standard_request.class);
		public final int value;

		private libusb_standard_request(int value) {
			this.value = value;
		}

		@Override
		public String toString() {
			return String.format("%s(0x%02x)", name(), value);
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

		public static final TypeTranscoder.Single<libusb_request_type> xcoder =
			TypeTranscoder.single(t -> t.value, libusb_request_type.class);
		public final int value;

		private libusb_request_type(int value) {
			this.value = value;
		}

		@Override
		public String toString() {
			return String.format("%s(0x%02x)", name(), value);
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

		public static final TypeTranscoder.Single<libusb_request_recipient> xcoder =
			TypeTranscoder.single(t -> t.value, libusb_request_recipient.class);
		public final int value;

		private libusb_request_recipient(int value) {
			this.value = value;
		}

		@Override
		public String toString() {
			return String.format("%s(0x%02x)", name(), value);
		}
	}

	public static byte libusb_request_type(libusb_request_recipient recipient,
		libusb_request_type type, libusb_endpoint_direction endpoint_direction) {
		return (byte) (recipient.value | type.value | endpoint_direction.value);
	}

	public static final int LIBUSB_ISO_SYNC_TYPE_MASK = 0x0C;

	/**
	 * Synchronization type for isochronous endpoints. Values for bits 2:3 of the
	 * libusb_endpoint_descriptor.bmAttributes "bmAttributes" field in libusb_endpoint_descriptor.
	 */
	public static enum libusb_iso_sync_type {
		LIBUSB_ISO_SYNC_TYPE_NONE(0),
		LIBUSB_ISO_SYNC_TYPE_ASYNC(1),
		LIBUSB_ISO_SYNC_TYPE_ADAPTIVE(2),
		LIBUSB_ISO_SYNC_TYPE_SYNC(3);

		public static final TypeTranscoder.Single<libusb_iso_sync_type> xcoder =
			TypeTranscoder.single(t -> t.value, libusb_iso_sync_type.class);
		public final int value;

		private libusb_iso_sync_type(int value) {
			this.value = value;
		}

		@Override
		public String toString() {
			return String.format("%s(%d)", name(), value);
		}
	}

	public static final int LIBUSB_ISO_USAGE_TYPE_MASK = 0x30;

	/**
	 * Usage type for isochronous endpoints. Values for bits 4:5 of the
	 * libusb_endpoint_descriptor.bmAttributes "bmAttributes" field in libusb_endpoint_descriptor.
	 */
	public static enum libusb_iso_usage_type {
		LIBUSB_ISO_USAGE_TYPE_DATA(0),
		LIBUSB_ISO_USAGE_TYPE_FEEDBACK(1),
		LIBUSB_ISO_USAGE_TYPE_IMPLICIT(2);

		public static final TypeTranscoder.Single<libusb_iso_usage_type> xcoder =
			TypeTranscoder.single(t -> t.value, libusb_iso_usage_type.class);
		public final int value;

		private libusb_iso_usage_type(int value) {
			this.value = value;
		}

		@Override
		public String toString() {
			return String.format("%s(%d)", name(), value);
		}
	}

	/**
	 * A structure representing the standard USB device descriptor. This descriptor is documented in
	 * section 9.6.1 of the USB 3.0 specification. All multiple-byte fields are represented in
	 * host-endian format.
	 */
	public static class libusb_device_descriptor extends Struct {
		private static final List<String> FIELDS = List.of( //
			"bLength", "bDescriptorType", "bcdUSB", "bDeviceClass", "bDeviceSubClass",
			"bDeviceProtocol", "bMaxPacketSize0", "idVendor", "idProduct", "bcdDevice",
			"iManufacturer", "iProduct", "iSerialNumber", "bNumConfigurations");
		private static final IntAccessor.Typed<libusb_device_descriptor> bDescriptorTypeAccessor =
			IntAccessor.typedByte(t -> t.bDescriptorType, (t, b) -> t.bDescriptorType = b);
		private static final IntAccessor.Typed<libusb_device_descriptor> bDeviceClassAccessor =
			IntAccessor.typedByte(t -> t.bDeviceClass, (t, b) -> t.bDeviceClass = b);

		public static class ByValue extends libusb_device_descriptor //
			implements Structure.ByValue {}

		public static class ByReference extends libusb_device_descriptor //
			implements Structure.ByReference {}

		public byte bLength;
		public byte bDescriptorType; // libusb_descriptor_type
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

		public libusb_device_descriptor() {}

		public libusb_device_descriptor(Pointer p) {
			super(p);
		}

		public FieldTranscoder.Single<libusb_descriptor_type> bDescriptorType() {
			return libusb_descriptor_type.xcoder.field(bDescriptorTypeAccessor.from(this));
		}

		public FieldTranscoder.Single<libusb_class_code> bDeviceClass() {
			return libusb_class_code.xcoder.field(bDeviceClassAccessor.from(this));
		}

		@Override
		protected List<String> getFieldOrder() {
			return FIELDS;
		}
	}

	/**
	 * A structure representing the standard USB endpoint descriptor. This descriptor is documented
	 * in section 9.6.6 of the USB 3.0 specification. All multiple-byte fields are represented in
	 * host-endian format.
	 */
	public static class libusb_endpoint_descriptor extends Struct {
		private static final List<String> FIELDS = List.of( //
			"bLength", "bDescriptorType", "bEndpointAddress", "bmAttributes", "wMaxPacketSize",
			"bInterval", "bRefresh", "bSynchAddress", "extra", "extra_length");
		private static final IntAccessor.Typed<libusb_endpoint_descriptor> bDescriptorTypeAccessor =
			IntAccessor.typedByte(t -> t.bDescriptorType, (t, b) -> t.bDescriptorType = b);
		private static final IntAccessor.Typed<libusb_endpoint_descriptor> bEndpointAddressAccessor =
			IntAccessor.typedByte(t -> t.bEndpointAddress, (t, b) -> t.bEndpointAddress = b);
		private static final IntAccessor.Typed<libusb_endpoint_descriptor> bmAttributesAccessor =
			IntAccessor.typedByte(t -> t.bmAttributes, (t, b) -> t.bmAttributes = b);

		public static class ByValue extends libusb_endpoint_descriptor //
			implements Structure.ByValue {}

		public static class ByReference extends libusb_endpoint_descriptor //
			implements Structure.ByReference {}

		public byte bLength;
		public byte bDescriptorType; // libusb_descriptor_type.LIBUSB_DT_ENDPOINT
		// bits 0:3 endpoint number, 4:6 reserved, 7 direction (libusb_endpoint_direction)
		public byte bEndpointAddress;
		// bits 0:1 libusb_transfer_type, 2:3 libusb_iso_sync_type (iso only)
		// 4:5 libusb_iso_usage_type (both iso only) 6:7 reserved
		public byte bmAttributes;
		public short wMaxPacketSize;
		public byte bInterval;
		public byte bRefresh;
		public byte bSynchAddress;
		public Pointer extra;
		public int extra_length;

		public libusb_endpoint_descriptor() {}

		public libusb_endpoint_descriptor(Pointer p) {
			super(p);
		}

		public FieldTranscoder.Single<libusb_descriptor_type> bDescriptorType() {
			return libusb_descriptor_type.xcoder.field(bDescriptorTypeAccessor.from(this));
		}

		public IntAccessor bEndpointNumber() {
			return MaskAccessor.of(bEndpointAddressAccessor.from(this), 0x0f);
		}

		public FieldTranscoder.Single<libusb_endpoint_direction> bEndpointDirection() {
			return libusb_endpoint_direction.xcoder
				.field(MaskAccessor.of(bEndpointAddressAccessor.from(this), 0x80));
		}

		public FieldTranscoder.Single<libusb_transfer_type> bmAttributesTransferType() {
			return libusb_transfer_type.xcoder
				.field(MaskAccessor.of(bmAttributesAccessor.from(this), 0x03));
		}

		public FieldTranscoder.Single<libusb_iso_sync_type> bmAttributesIsoSyncType() {
			return libusb_iso_sync_type.xcoder
				.field(MaskAccessor.of(bmAttributesAccessor.from(this), 0x0c, 2));
		}

		public FieldTranscoder.Single<libusb_iso_usage_type> bmAttributesIsoUsageType() {
			return libusb_iso_usage_type.xcoder
				.field(MaskAccessor.of(bmAttributesAccessor.from(this), 0x30, 4));
		}

		public byte[] extra() {
			return JnaUtil.byteArray(extra, extra_length);
		}

		@Override
		protected List<String> getFieldOrder() {
			return FIELDS;
		}
	}

	/**
	 * A structure representing the standard USB interface descriptor. This descriptor is documented
	 * in section 9.6.5 of the USB 3.0 specification. All multiple-byte fields are represented in
	 * host-endian format.
	 */
	public static class libusb_interface_descriptor extends Struct {
		private static final List<String> FIELDS = List.of( //
			"bLength", "bDescriptorType", "bInterfaceNumber", "bAlternateSetting", "bNumEndpoints",
			"bInterfaceClass", "bInterfaceSubClass", "bInterfaceProtocol", "iInterface", "endpoint",
			"extra", "extra_length");
		private static final IntAccessor.Typed<libusb_interface_descriptor> bDescriptorTypeAccessor =
			IntAccessor.typedByte(t -> t.bDescriptorType, (t, b) -> t.bDescriptorType = b);
		private static final IntAccessor.Typed<libusb_interface_descriptor> bInterfaceClassAccessor =
			IntAccessor.typedByte(t -> t.bInterfaceClass, (t, b) -> t.bInterfaceClass = b);

		public static class ByValue extends libusb_interface_descriptor //
			implements Structure.ByValue {}

		public static class ByReference extends libusb_interface_descriptor //
			implements Structure.ByReference {}

		public byte bLength;
		public byte bDescriptorType; // libusb_descriptor_type.LIBUSB_DT_INTERFACE
		public byte bInterfaceNumber;
		public byte bAlternateSetting;
		public byte bNumEndpoints;
		public byte bInterfaceClass; // libusb_class_code
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

		public FieldTranscoder.Single<libusb_descriptor_type> bDescriptorType() {
			return libusb_descriptor_type.xcoder.field(bDescriptorTypeAccessor.from(this));
		}

		public FieldTranscoder.Single<libusb_class_code> bInterfaceClass() {
			return libusb_class_code.xcoder.field(bInterfaceClassAccessor.from(this));
		}

		public libusb_endpoint_descriptor[] endpoints() {
			return JnaUtil.array(endpoint, ubyte(bNumEndpoints), libusb_endpoint_descriptor[]::new);
		}

		public byte[] extra() {
			return JnaUtil.byteArray(extra, extra_length);
		}

		@Override
		protected List<String> getFieldOrder() {
			return FIELDS;
		}
	}

	/**
	 * A collection of alternate settings for a particular USB interface.
	 */
	public static class libusb_interface extends Struct {
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

		public libusb_interface_descriptor[] altsettings() {
			return JnaUtil.array(altsetting, num_altsetting, libusb_interface_descriptor[]::new);
		}

		@Override
		protected List<String> getFieldOrder() {
			return FIELDS;
		}
	}

	/**
	 * A structure representing the standard USB configuration descriptor. This descriptor is
	 * documented in section 9.6.3 of the USB 3.0 specification. All multiple-byte fields are
	 * represented in host-endian format.
	 */
	public static class libusb_config_descriptor extends Struct {
		private static final List<String> FIELDS = List.of( //
			"bLength", "bDescriptorType", "wTotalLength", "bNumInterfaces", "bConfigurationValue",
			"iConfiguration", "bmAttributes", "MaxPower", "interfaces", "extra", "extra_length");
		private static final IntAccessor.Typed<libusb_config_descriptor> bDescriptorTypeAccessor =
			IntAccessor.typedByte(t -> t.bDescriptorType, (t, b) -> t.bDescriptorType = b);

		public static class ByValue extends libusb_config_descriptor //
			implements Structure.ByValue {}

		public static class ByReference extends libusb_config_descriptor //
			implements Structure.ByReference {}

		public byte bLength;
		public byte bDescriptorType; // libusb_descriptor_type.LIBUSB_DT_CONFIG
		public short wTotalLength;
		public byte bNumInterfaces;
		public byte bConfigurationValue;
		public byte iConfiguration;
		public byte bmAttributes;
		public byte MaxPower;
		public libusb_interface.ByReference interfaces;
		public Pointer extra;
		public int extra_length;

		public libusb_config_descriptor() {}

		public libusb_config_descriptor(Pointer p) {
			super(p);
		}

		public FieldTranscoder.Single<libusb_descriptor_type> bDescriptorType() {
			return libusb_descriptor_type.xcoder.field(bDescriptorTypeAccessor.from(this));
		}

		public libusb_interface[] interfaces() {
			return JnaUtil.array(interfaces, ubyte(bNumInterfaces), libusb_interface[]::new);
		}

		public byte[] extra() {
			return JnaUtil.byteArray(extra, extra_length);
		}

		@Override
		protected List<String> getFieldOrder() {
			return FIELDS;
		}
	}

	/**
	 * A structure representing the superspeed endpoint companion descriptor. This descriptor is
	 * documented in section 9.6.7 of the USB 3.0 specification. All multiple-byte fields are
	 * represented in host-endian format.
	 */
	public static class libusb_ss_endpoint_companion_descriptor extends Struct {
		private static final List<String> FIELDS = List.of( //
			"bLength", "bDescriptorType", "bMaxBurst", "bmAttributes", "wBytesPerInterval");
		private static final IntAccessor.Typed<libusb_ss_endpoint_companion_descriptor> bDescriptorTypeAccessor =
			IntAccessor.typedByte(t -> t.bDescriptorType, (t, b) -> t.bDescriptorType = b);
		private static final IntAccessor.Typed<libusb_ss_endpoint_companion_descriptor> bmAttributesAccessor =
			IntAccessor.typedByte(t -> t.bmAttributes, (t, b) -> t.bmAttributes = b);

		public static class ByValue extends libusb_ss_endpoint_companion_descriptor //
			implements Structure.ByValue {}

		public static class ByReference extends libusb_ss_endpoint_companion_descriptor //
			implements Structure.ByReference {}

		public byte bLength;
		public byte bDescriptorType; // libusb_descriptor_type.LIBUSB_DT_SS_ENDPOINT_COMPANION
		public byte bMaxBurst;
		// bulk: bits 0:4 max number of streams
		// iso: bits 0:1 Mult
		public byte bmAttributes;
		public short wBytesPerInterval;

		public libusb_ss_endpoint_companion_descriptor() {}

		public libusb_ss_endpoint_companion_descriptor(Pointer p) {
			super(p);
		}

		public FieldTranscoder.Single<libusb_descriptor_type> bDescriptorType() {
			return libusb_descriptor_type.xcoder.field(bDescriptorTypeAccessor.from(this));
		}

		public IntAccessor bmAttributesBulkMaxStreams() {
			return MaskAccessor.of(bmAttributesAccessor.from(this), 0x1f);
		}

		public IntAccessor bmAttributesIsoMult() {
			return MaskAccessor.of(bmAttributesAccessor.from(this), 0x03);
		}

		@Override
		protected List<String> getFieldOrder() {
			return FIELDS;
		}
	}

	/**
	 * A generic representation of a BOS Device Capability descriptor. It is advised to check
	 * bDevCapabilityType and call the matching libusb_get_*_descriptor function to get a structure
	 * fully matching the type.
	 */
	public static class libusb_bos_dev_capability_descriptor extends Struct {
		private static final List<String> FIELDS = List.of( //
			"bLength", "bDescriptorType", "bDevCapabilityType", "dev_capability_data");
		private static final IntAccessor.Typed<libusb_bos_dev_capability_descriptor> bDescriptorTypeAccessor =
			IntAccessor.typedByte(t -> t.bDescriptorType, (t, b) -> t.bDescriptorType = b);

		public static class ByValue extends libusb_bos_dev_capability_descriptor //
			implements Structure.ByValue {}

		public static class ByReference extends libusb_bos_dev_capability_descriptor //
			implements Structure.ByReference {}

		public byte bLength;
		public byte bDescriptorType; // libusb_descriptor_type.LIBUSB_DT_DEVICE_CAPABILITY
		public byte bDevCapabilityType;
		public byte[] dev_capability_data;

		public libusb_bos_dev_capability_descriptor() {}

		public libusb_bos_dev_capability_descriptor(Pointer p) {
			super(p);
		}

		public FieldTranscoder.Single<libusb_descriptor_type> bDescriptorType() {
			return libusb_descriptor_type.xcoder.field(bDescriptorTypeAccessor.from(this));
		}

		public byte[] dev_capability_data() {
			// TODO: init field instead?
			return fieldByteArrayRem("dev_capability_data", ubyte(bLength));
		}

		@Override
		protected List<String> getFieldOrder() {
			return FIELDS;
		}
	}

	/**
	 * A structure representing the Binary Device Object Store (BOS) descriptor. This descriptor is
	 * documented in section 9.6.2 of the USB 3.0 specification. All multiple-byte fields are
	 * represented in host-endian format.
	 */
	public static class libusb_bos_descriptor extends Struct {
		private static final List<String> FIELDS = List.of( //
			"bLength", "bDescriptorType", "wTotalLength", "bNumDeviceCaps", "dev_capability");
		private static final IntAccessor.Typed<libusb_bos_descriptor> bDescriptorTypeAccessor =
			IntAccessor.typedByte(t -> t.bDescriptorType, (t, b) -> t.bDescriptorType = b);

		public static class ByValue extends libusb_bos_descriptor //
			implements Structure.ByValue {}

		public static class ByReference extends libusb_bos_descriptor //
			implements Structure.ByReference {}

		public byte bLength;
		public byte bDescriptorType; // libusb_descriptor_type.LIBUSB_DT_BOS LIBUSB_DT_BOS
		public short wTotalLength;
		public byte bNumDeviceCaps;
		public libusb_bos_dev_capability_descriptor.ByReference[] dev_capability;

		public libusb_bos_descriptor() {}

		public libusb_bos_descriptor(Pointer p) {
			super(p);
		}

		public FieldTranscoder.Single<libusb_descriptor_type> bDescriptorType() {
			return libusb_descriptor_type.xcoder.field(bDescriptorTypeAccessor.from(this));
		}

		public libusb_bos_dev_capability_descriptor[] dev_capability() {
			// TODO: init field instead?
			return fieldArrayByRef("dev_capability", ubyte(bNumDeviceCaps),
				libusb_bos_dev_capability_descriptor::new,
				libusb_bos_dev_capability_descriptor[]::new);
		}

		@Override
		protected List<String> getFieldOrder() {
			return FIELDS;
		}

	}

	/**
	 * A structure representing the USB 2.0 Extension descriptor This descriptor is documented in
	 * section 9.6.2.1 of the USB 3.0 specification. All multiple-byte fields are represented in
	 * host-endian format.
	 */
	public static class libusb_usb_2_0_extension_descriptor extends Struct {
		private static final List<String> FIELDS = List.of( //
			"bLength", "bDescriptorType", "bDevCapabilityType", "bmAttributes");
		private static final IntAccessor.Typed<libusb_usb_2_0_extension_descriptor> bDescriptorTypeAccessor =
			IntAccessor.typedByte(t -> t.bDescriptorType, (t, b) -> t.bDescriptorType = b);
		private static final IntAccessor.Typed<libusb_usb_2_0_extension_descriptor> bDevCapabilityTypeAccessor =
			IntAccessor.typedByte(t -> t.bDevCapabilityType, (t, b) -> t.bDevCapabilityType = b);
		private static final IntAccessor.Typed<libusb_usb_2_0_extension_descriptor> bmAttributesTypeAccessor =
			IntAccessor.typed(t -> t.bmAttributes, (t, i) -> t.bmAttributes = i);

		public static class ByValue extends libusb_usb_2_0_extension_descriptor //
			implements Structure.ByValue {}

		public static class ByReference extends libusb_usb_2_0_extension_descriptor //
			implements Structure.ByReference {}

		public byte bLength;
		public byte bDescriptorType; // libusb_descriptor_type.LIBUSB_DT_DEVICE_CAPABILITY
		public byte bDevCapabilityType; // libusb_bos_type.LIBUSB_BT_USB_2_0_EXTENSION
		public int bmAttributes; // libusb_usb_2_0_extension_attributes

		public libusb_usb_2_0_extension_descriptor() {}

		public libusb_usb_2_0_extension_descriptor(Pointer p) {
			super(p);
		}

		public FieldTranscoder.Single<libusb_descriptor_type> bDescriptorType() {
			return libusb_descriptor_type.xcoder.field(bDescriptorTypeAccessor.from(this));
		}

		public FieldTranscoder.Single<libusb_bos_type> bDevCapabilityType() {
			return libusb_bos_type.xcoder.field(bDevCapabilityTypeAccessor.from(this));
		}

		public FieldTranscoder.Flag<libusb_usb_2_0_extension_attributes> bmAttributes() {
			return libusb_usb_2_0_extension_attributes.xcoder
				.field(bmAttributesTypeAccessor.from(this));
		}

		@Override
		protected List<String> getFieldOrder() {
			return FIELDS;
		}
	}

	/**
	 * A structure representing the SuperSpeed USB Device Capability descriptor This descriptor is
	 * documented in section 9.6.2.2 of the USB 3.0 specification. All multiple-byte fields are
	 * represented in host-endian format.
	 */
	public static class libusb_ss_usb_device_capability_descriptor extends Struct {
		private static final List<String> FIELDS = List.of( //
			"bLength", "bDescriptorType", "bDevCapabilityType", "bmAttributes", "wSpeedSupported",
			"bFunctionalitySupport", "bU1DevExitLat", "bU2DevExitLat");
		private static final IntAccessor.Typed<libusb_ss_usb_device_capability_descriptor> bDescriptorTypeAccessor =
			IntAccessor.typedByte(t -> t.bDescriptorType, (t, b) -> t.bDescriptorType = b);
		private static final IntAccessor.Typed<libusb_ss_usb_device_capability_descriptor> bDevCapabilityTypeAccessor =
			IntAccessor.typedByte(t -> t.bDevCapabilityType, (t, b) -> t.bDevCapabilityType = b);
		private static final IntAccessor.Typed<libusb_ss_usb_device_capability_descriptor> bmAttributesTypeAccessor =
			IntAccessor.typedByte(t -> t.bmAttributes, (t, b) -> t.bmAttributes = b);
		private static final IntAccessor.Typed<libusb_ss_usb_device_capability_descriptor> wSpeedSupportedAccessor =
			IntAccessor.typedShort(t -> t.wSpeedSupported, (t, s) -> t.wSpeedSupported = s);

		public static class ByValue extends libusb_ss_usb_device_capability_descriptor //
			implements Structure.ByValue {}

		public static class ByReference extends libusb_ss_usb_device_capability_descriptor //
			implements Structure.ByReference {}

		public byte bLength;
		public byte bDescriptorType; // libusb_descriptor_type.LIBUSB_DT_DEVICE_CAPABILITY
		public byte bDevCapabilityType;// libusb_bos_type.LIBUSB_BT_SS_USB_DEVICE_CAPABILITY
		public byte bmAttributes; // libusb_ss_usb_device_capability_attributes
		public short wSpeedSupported; // libusb_supported_speed
		public byte bFunctionalitySupport;
		public byte bU1DevExitLat;
		public short bU2DevExitLat;

		public libusb_ss_usb_device_capability_descriptor() {}

		public libusb_ss_usb_device_capability_descriptor(Pointer p) {
			super(p);
		}

		public FieldTranscoder.Single<libusb_descriptor_type> bDescriptorType() {
			return libusb_descriptor_type.xcoder.field(bDescriptorTypeAccessor.from(this));
		}

		public FieldTranscoder.Single<libusb_bos_type> bDevCapabilityType() {
			return libusb_bos_type.xcoder.field(bDevCapabilityTypeAccessor.from(this));
		}

		public FieldTranscoder.Flag<libusb_ss_usb_device_capability_attributes> bmAttributes() {
			return libusb_ss_usb_device_capability_attributes.xcoder
				.field(bmAttributesTypeAccessor.from(this));
		}

		public FieldTranscoder.Flag<libusb_supported_speed> wSpeedSupported() {
			return libusb_supported_speed.xcoder.field(wSpeedSupportedAccessor.from(this));
		}

		@Override
		protected List<String> getFieldOrder() {
			return FIELDS;
		}
	}

	/**
	 * A structure representing the Container ID descriptor. This descriptor is documented in
	 * section 9.6.2.3 of the USB 3.0 specification. All multiple-byte fields, except UUIDs, are
	 * represented in host-endian format.
	 */
	public static class libusb_container_id_descriptor extends Struct {
		private static final List<String> FIELDS = List.of( //
			"bLength", "bDescriptorType", "bDevCapabilityType", "bReserved", "ContainerID");
		private static final IntAccessor.Typed<libusb_container_id_descriptor> bDescriptorTypeAccessor =
			IntAccessor.typedByte(t -> t.bDescriptorType, (t, b) -> t.bDescriptorType = b);
		private static final IntAccessor.Typed<libusb_container_id_descriptor> bDevCapabilityTypeAccessor =
			IntAccessor.typedByte(t -> t.bDevCapabilityType, (t, b) -> t.bDevCapabilityType = b);

		public static class ByValue extends libusb_container_id_descriptor //
			implements Structure.ByValue {}

		public static class ByReference extends libusb_container_id_descriptor //
			implements Structure.ByReference {}

		public byte bLength;
		public byte bDescriptorType; // libusb_descriptor_type.LIBUSB_DT_DEVICE_CAPABILITY
		public byte bDevCapabilityType; // libusb_bos_type.LIBUSB_BT_CONTAINER_ID
		public byte bReserved;
		public byte[] ContainerID = new byte[16];

		public libusb_container_id_descriptor() {}

		public libusb_container_id_descriptor(Pointer p) {
			super(p);
		}

		public FieldTranscoder.Single<libusb_descriptor_type> bDescriptorType() {
			return libusb_descriptor_type.xcoder.field(bDescriptorTypeAccessor.from(this));
		}

		public FieldTranscoder.Single<libusb_bos_type> bDevCapabilityType() {
			return libusb_bos_type.xcoder.field(bDevCapabilityTypeAccessor.from(this));
		}

		@Override
		protected List<String> getFieldOrder() {
			return FIELDS;
		}
	}

	/**
	 * Setup packet for control transfers.
	 */
	public static class libusb_control_setup extends Struct {
		private static final List<String> FIELDS = List.of( //
			"bmRequestType", "bRequest", "wValue", "wIndex", "wLength");
		private static final IntAccessor.Typed<libusb_control_setup> bmRequestTypeAccessor =
			IntAccessor.typedByte(t -> t.bmRequestType, (t, b) -> t.bmRequestType = b);
		private static final IntAccessor.Typed<libusb_control_setup> bRequestAccessor =
			IntAccessor.typedByte(t -> t.bRequest, (t, b) -> t.bRequest = b);

		public static class ByValue extends libusb_control_setup //
			implements Structure.ByValue {}

		public static class ByReference extends libusb_control_setup //
			implements Structure.ByReference {}

		// bits 0:4 libusb_request_recipient
		// bits 5:6 libusb_request_type
		// bit 7 libusb_endpoint_direction
		public byte bmRequestType;
		// if libusb_request_type.LIBUSB_REQUEST_TYPE_STANDARD,
		// libusb_standard_request, otherwise app-specific
		public byte bRequest;
		public short wValue;
		public short wIndex;
		public short wLength;

		public libusb_control_setup() {}

		public libusb_control_setup(Pointer p) {
			super(p);
		}

		public FieldTranscoder.Single<libusb_request_recipient> bmRequestRecipient() {
			return libusb_request_recipient.xcoder
				.field(MaskAccessor.of(bmRequestTypeAccessor.from(this), 0x1f));
		}

		public FieldTranscoder.Single<libusb_request_type> bmRequestType() {
			return libusb_request_type.xcoder
				.field(MaskAccessor.of(bmRequestTypeAccessor.from(this), 0x60));
		}

		public FieldTranscoder.Single<libusb_endpoint_direction> bmRequestDirection() {
			return libusb_endpoint_direction.xcoder
				.field(MaskAccessor.of(bmRequestTypeAccessor.from(this), 0x80));
		}

		public FieldTranscoder.Single<libusb_standard_request> bRequestStandard() {
			return libusb_standard_request.xcoder.field(bRequestAccessor.from(this));
		}

		@Override
		protected List<String> getFieldOrder() {
			return FIELDS;
		}
	}

	public static final int LIBUSB_CONTROL_SETUP_SIZE = new libusb_control_setup().size();

	/* libusb */

	/**
	 * Structure providing the version of the libusb runtime
	 */
	public static class libusb_version extends Struct {
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

	/**
	 * Structure representing a libusb session. The concept of individual libusb sessions allows for
	 * your program to use two libraries (or dynamically load two modules) which both independently
	 * use libusb. This will prevent interference between the individual libusb users - for example
	 * libusb_set_debug() will not affect the other user of the library, and libusb_exit() will not
	 * destroy resources that the other user is still using.
	 *
	 * Sessions are created by libusb_init() and destroyed through libusb_exit(). If your
	 * application is guaranteed to only ever include a single libusb user (i.e. you), you do not
	 * have to worry about contexts: pass NULL in every function call where a context is required.
	 * The default context will be used.
	 */
	// typedef struct libusb_context libusb_context;
	public static class libusb_context extends TypedPointer {
		public static class ByReference extends TypedPointer.ByReference<libusb_context> {
			public ByReference() {
				super(libusb_context::new);
			}
		}
	}

	/**
	 * Structure representing a USB device detected on the system. This is an opaque type for which
	 * you are only ever provided with a pointer, usually originating from libusb_get_device_list().
	 *
	 * Certain operations can be performed on a device, but in order to do any I/O you will have to
	 * first obtain a device handle using libusb_open().
	 *
	 * Devices are reference counted with libusb_ref_device() and libusb_unref_device(), and are
	 * freed when the reference count reaches 0. New devices presented by libusb_get_device_list()
	 * have a reference count of 1, and libusb_free_device_list() can optionally decrease the
	 * reference count on all devices in the list. libusb_open() adds another reference which is
	 * later destroyed by libusb_close().
	 */
	// typedef struct libusb_device libusb_device;
	public static class libusb_device extends TypedPointer {
		public static class ArrayRef extends TypedPointer.ByReference<libusb_device> {
			public static class ByRef extends TypedPointer.ByReference<ArrayRef> {
				public ByRef() {
					super(ArrayRef::new);
				}
			}

			public ArrayRef() {
				super(libusb_device::new, libusb_device[]::new);
			}
		}
	}

	/**
	 * Structure representing a handle on a USB device. This is an opaque type for which you are
	 * only ever provided with a pointer, usually originating from libusb_open().
	 *
	 * A device handle is used to perform I/O and other operations. When finished with a device
	 * handle, you should call libusb_close().
	 */
	// typedef struct libusb_device_handle libusb_device_handle;
	public static class libusb_device_handle extends TypedPointer {
		public static class ByReference extends TypedPointer.ByReference<libusb_device_handle> {
			public ByReference() {
				super(libusb_device_handle::new);
			}
		}
	}

	/**
	 * Speed codes. Indicates the speed at which the device is operating.
	 */
	public static enum libusb_speed {
		LIBUSB_SPEED_UNKNOWN(0),
		LIBUSB_SPEED_LOW(1),
		LIBUSB_SPEED_FULL(2),
		LIBUSB_SPEED_HIGH(3),
		LIBUSB_SPEED_SUPER(4);

		public static final TypeTranscoder.Single<libusb_speed> xcoder =
			TypeTranscoder.single(t -> t.value, libusb_speed.class);
		public final int value;

		private libusb_speed(int value) {
			this.value = value;
		}

		@Override
		public String toString() {
			return String.format("%s(%d)", name(), value);
		}
	}

	/**
	 * Supported speeds (wSpeedSupported) bitfield. Indicates what speeds the device supports.
	 */
	public static enum libusb_supported_speed {
		LIBUSB_LOW_SPEED_OPERATION(1),
		LIBUSB_FULL_SPEED_OPERATION(2),
		LIBUSB_HIGH_SPEED_OPERATION(4),
		LIBUSB_SUPER_SPEED_OPERATION(8);

		public static final TypeTranscoder.Flag<libusb_supported_speed> xcoder =
			TypeTranscoder.flag(t -> t.value, libusb_supported_speed.class);
		public final int value;

		private libusb_supported_speed(int value) {
			this.value = value;
		}

		@Override
		public String toString() {
			return String.format("%s(0x%02x)", name(), value);
		}
	}

	/**
	 * Masks for the bits of the libusb_usb_2_0_extension_descriptor.bmAttributes "bmAttributes"
	 * field of the USB 2.0 Extension descriptor.
	 */
	public static enum libusb_usb_2_0_extension_attributes {
		LIBUSB_BM_LPM_SUPPORT(2);

		public static final TypeTranscoder.Flag<libusb_usb_2_0_extension_attributes> xcoder =
			TypeTranscoder.flag(t -> t.value, libusb_usb_2_0_extension_attributes.class);
		public final int value;

		private libusb_usb_2_0_extension_attributes(int value) {
			this.value = value;
		}

		@Override
		public String toString() {
			return String.format("%s(%d)", name(), value);
		}
	}

	/**
	 * Masks for the bits of the libusb_ss_usb_device_capability_descriptor.bmAttributes
	 * "bmAttributes" field field of the SuperSpeed USB Device Capability descriptor.
	 */
	public static enum libusb_ss_usb_device_capability_attributes {
		LIBUSB_BM_LTM_SUPPORT(2);

		public static final TypeTranscoder.Flag<libusb_ss_usb_device_capability_attributes> xcoder =
			TypeTranscoder.flag(t -> t.value, libusb_ss_usb_device_capability_attributes.class);
		public final int value;

		private libusb_ss_usb_device_capability_attributes(int value) {
			this.value = value;
		}

		@Override
		public String toString() {
			return String.format("%s(%d)", name(), value);
		}
	}

	/**
	 * USB capability types
	 */
	public enum libusb_bos_type {
		LIBUSB_BT_WIRELESS_USB_DEVICE_CAPABILITY(1),
		LIBUSB_BT_USB_2_0_EXTENSION(2),
		LIBUSB_BT_SS_USB_DEVICE_CAPABILITY(3),
		LIBUSB_BT_CONTAINER_ID(4);

		public static final TypeTranscoder.Single<libusb_bos_type> xcoder =
			TypeTranscoder.single(t -> t.value, libusb_bos_type.class);
		public final int value;

		private libusb_bos_type(int value) {
			this.value = value;
		}

		@Override
		public String toString() {
			return String.format("%s(%d)", name(), value);
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

		public static final TypeTranscoder.Single<libusb_error> xcoder =
			TypeTranscoder.single(t -> t.value, libusb_error.class);
		public final int value;

		private libusb_error(int value) {
			this.value = value;
		}

		@Override
		public String toString() {
			return String.format("%s(%d)", name(), value);
		}
	}

	public static final int LIBUSB_ERROR_COUNT = 14;

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

		public static final TypeTranscoder.Single<libusb_transfer_status> xcoder =
			TypeTranscoder.single(t -> t.value, libusb_transfer_status.class);
		public final int value;

		private libusb_transfer_status(int value) {
			this.value = value;
		}

		@Override
		public String toString() {
			return String.format("%s(%d)", name(), value);
		}
	}

	/**
	 * Transfer flag values
	 */
	public static enum libusb_transfer_flags {
		LIBUSB_TRANSFER_SHORT_NOT_OK(1 << 0),
		LIBUSB_TRANSFER_FREE_BUFFER(1 << 1),
		LIBUSB_TRANSFER_FREE_TRANSFER(1 << 2),
		LIBUSB_TRANSFER_ADD_ZERO_PACKET(1 << 3);

		public static final TypeTranscoder.Flag<libusb_transfer_flags> xcoder =
			TypeTranscoder.flag(t -> t.value, libusb_transfer_flags.class);
		public final int value;

		private libusb_transfer_flags(int value) {
			this.value = value;
		}

		@Override
		public String toString() {
			return String.format("%s(0x%02x)", name(), value);
		}
	}

	/**
	 * Isochronous packet descriptor.
	 */
	public static class libusb_iso_packet_descriptor extends Struct {
		private static final List<String> FIELDS = List.of( //
			"length", "actual_length", "status");
		private static final IntAccessor.Typed<libusb_iso_packet_descriptor> statusAccessor =
			IntAccessor.typed(t -> t.status, (t, i) -> t.status = i);

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

		public FieldTranscoder.Single<libusb_transfer_status> status() {
			return libusb_transfer_status.xcoder.field(statusAccessor.from(this));
		}

		@Override
		protected List<String> getFieldOrder() {
			return FIELDS;
		}
	}

	/**
	 * Asynchronous transfer callback function type. When submitting asynchronous transfers, you
	 * pass a pointer to a callback function of this type via the libusb_transfer.callback
	 * "callback" member of the libusb_transfer structure. libusb will call this function later,
	 * when the transfer has completed or failed.
	 * 
	 * @param transfer
	 *            The libusb_transfer struct the callback function is being notified about.
	 */
	// typedef void (LIBUSB_CALL *libusb_transfer_cb_fn)(struct libusb_transfer *transfer);
	public static interface libusb_transfer_cb_fn extends Callback {
		public void invoke(libusb_transfer transfer);
	}

	/**
	 * The generic USB transfer structure. The user populates this structure and then submits it in
	 * order to request a transfer. After the transfer has completed, the library populates the
	 * transfer with the results and passes it back to the user.
	 */
	public static class libusb_transfer extends Struct {
		private static final List<String> FIELDS = List.of( //
			"dev_handle", "flags", "endpoint", "type", "timeout", "status", "length",
			"actual_length", "callback", "user_data", "buffer", "num_iso_packets",
			"iso_packet_desc");
		private static final IntAccessor.Typed<libusb_transfer> flagsAccessor =
			IntAccessor.typedByte(t -> t.flags, (t, b) -> t.flags = b);
		private static final IntAccessor.Typed<libusb_transfer> typeAccessor =
			IntAccessor.typedByte(t -> t.type, (t, b) -> t.type = b);
		private static final IntAccessor.Typed<libusb_transfer> statusAccessor =
			IntAccessor.typed(t -> t.status, (t, i) -> t.status = i);

		public static class ByValue extends libusb_transfer //
			implements Structure.ByValue {}

		public static class ByReference extends libusb_transfer //
			implements Structure.ByReference {}

		public libusb_device_handle dev_handle;
		public byte flags; // libusb_transfer_flags
		public byte endpoint;
		public byte type; // libusb_transfer_type
		public int timeout;
		public int status; // libusb_transfer_status
		public int length;
		public int actual_length;
		public libusb_transfer_cb_fn callback;
		public Pointer user_data;
		public Pointer buffer;
		public int num_iso_packets;
		public libusb_iso_packet_descriptor[] iso_packet_desc;

		public libusb_transfer() {}

		public libusb_transfer(Pointer p) {
			super(p);
		}

		public FieldTranscoder.Flag<libusb_transfer_flags> flags() {
			return libusb_transfer_flags.xcoder.field(flagsAccessor.from(this));
		}

		public FieldTranscoder.Single<libusb_transfer_type> type() {
			return libusb_transfer_type.xcoder.field(typeAccessor.from(this));
		}

		public FieldTranscoder.Single<libusb_transfer_status> status() {
			return libusb_transfer_status.xcoder.field(statusAccessor.from(this));
		}

		public libusb_iso_packet_descriptor[] iso_packet_desc(int count) {
			return fieldArray("iso_packet_desc", count, libusb_iso_packet_descriptor::new,
				libusb_iso_packet_descriptor[]::new);
		}

		public libusb_iso_packet_descriptor[] iso_packet_desc() {
			// TODO: initialize field instead?
			return iso_packet_desc(num_iso_packets);
		}

		@Override
		protected List<String> getFieldOrder() {
			return FIELDS;
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

		public static final TypeTranscoder.Single<libusb_capability> xcoder =
			TypeTranscoder.single(t -> t.value, libusb_capability.class);
		public final int value;

		private libusb_capability(int value) {
			this.value = value;
		}

		@Override
		public String toString() {
			return String.format("%s(0x%04x)", name(), value);
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

		public static final TypeTranscoder.Single<libusb_log_level> xcoder =
			TypeTranscoder.single(t -> t.value, libusb_log_level.class);
		public final int value;

		private libusb_log_level(int value) {
			this.value = value;
		}

		@Override
		public String toString() {
			return String.format("%s(%d)", name(), value);
		}
	}

	/*
	 * Native calls here
	 */

	/* async I/O */

	/**
	 * Get the data section of a control transfer. This convenience function is here to remind you
	 * that the data does not start until 8 bytes into the actual buffer, as the setup packet comes
	 * first.
	 *
	 * Calling this function only makes sense from a transfer callback function, or situations where
	 * you have already allocated a suitably sized buffer at transfer.buffer.
	 *
	 * @param transfer
	 *            a transfer
	 * @return pointer to the first byte of the data section
	 */
	public static Pointer libusb_control_transfer_get_data(libusb_transfer transfer) {
		// TODO: check this is correct way to offset pointer
		return transfer.buffer.share(LIBUSB_CONTROL_SETUP_SIZE);
	}

	/**
	 * Get the control setup packet of a control transfer. This convenience function is here to
	 * remind you that the control setup occupies the first 8 bytes of the transfer data buffer.
	 *
	 * Calling this function only makes sense from a transfer callback function, or situations where
	 * you have already allocated a suitably sized buffer at transfer.buffer.
	 *
	 * @param transfer
	 *            a transfer
	 * @return a casted pointer to the start of the transfer data buffer
	 */
	public static libusb_control_setup libusb_control_transfer_get_setup(libusb_transfer transfer) {
		// TODO: check is correct?
		return new libusb_control_setup(transfer.buffer);
	}

	/**
	 * Helper function to populate the setup packet (first 8 bytes of the data buffer) for a control
	 * transfer. The wIndex, wValue and wLength values should be given in host-endian byte order.
	 *
	 * @param buffer
	 *            buffer to output the setup packet into This pointer must be aligned to at least 2
	 *            bytes boundary.
	 * @param bmRequestType
	 *            see libusb_control_setup.bmRequestType
	 * @param bRequest
	 *            see libusb_control_setup.bRequest
	 * @param wValue
	 *            see libusb_control_setup.wValue
	 * @param wIndex
	 *            see libusb_control_setup.wIndex
	 * @param wLength
	 *            see libusb_control_setup.wLength
	 */
	public static void libusb_fill_control_setup(Pointer buffer, byte bmRequestType, byte bRequest,
		short wValue, short wIndex, short wLength) {
		libusb_control_setup setup = new libusb_control_setup(buffer);
		setup.bmRequestType = bmRequestType;
		setup.bRequest = bRequest;
		setup.wValue = libusb_cpu_to_le16(wValue);
		setup.wIndex = libusb_cpu_to_le16(wIndex);
		setup.wLength = libusb_cpu_to_le16(wLength);
	}

	/*
	 * Native calls here
	 */

	/**
	 * Helper function to populate the required libusb_transfer fields for a control transfer.
	 *
	 * If you pass a transfer buffer to this function, the first 8 bytes will be interpreted as a
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
	 *
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
	public static void libusb_fill_control_transfer(libusb_transfer transfer,
		libusb_device_handle dev_handle, Pointer buffer, libusb_transfer_cb_fn callback,
		Pointer user_data, int timeout) {
		libusb_control_setup setup = new libusb_control_setup(buffer);
		transfer.dev_handle = dev_handle;
		transfer.endpoint = 0;
		transfer.type().set(libusb_transfer_type.LIBUSB_TRANSFER_TYPE_CONTROL);
		transfer.timeout = timeout;
		transfer.buffer = buffer;
		if (buffer != Pointer.NULL)
			transfer.length = LIBUSB_CONTROL_SETUP_SIZE + libusb_le16_to_cpu(setup.wLength);
		transfer.user_data = user_data;
		transfer.callback = callback;
	}

	/**
	 * Helper function to populate the required libusb_transfer fields for a bulk transfer.
	 *
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
		libusb_device_handle dev_handle, byte endpoint, Pointer buffer, int length,
		libusb_transfer_cb_fn callback, Pointer user_data, int timeout) {
		transfer.dev_handle = dev_handle;
		transfer.endpoint = endpoint;
		transfer.type().set(libusb_transfer_type.LIBUSB_TRANSFER_TYPE_BULK);
		transfer.timeout = timeout;
		transfer.buffer = buffer;
		transfer.length = length;
		transfer.user_data = user_data;
		transfer.callback = callback;
	}

	/**
	 * Helper function to populate the required libusb_transfer fields for a bulk transfer using
	 * bulk streams.
	 *
	 * Since version 1.0.19, LIBUSB_API_VERSION >= 0x01000103
	 *
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
		libusb_device_handle dev_handle, byte endpoint, int stream_id, Pointer buffer, int length,
		libusb_transfer_cb_fn callback, Pointer user_data, int timeout) {
		libusb_fill_bulk_transfer(transfer, dev_handle, endpoint, buffer, length, callback,
			user_data, timeout);
		transfer.type().set(libusb_transfer_type.LIBUSB_TRANSFER_TYPE_BULK_STREAM);
		LIBUSB.libusb_transfer_set_stream_id(transfer, stream_id);
	}

	/**
	 * Helper function to populate the required libusb_transfer fields for an interrupt transfer.
	 *
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
		libusb_device_handle dev_handle, byte endpoint, Pointer buffer, int length,
		libusb_transfer_cb_fn callback, Pointer user_data, int timeout) {
		transfer.dev_handle = dev_handle;
		transfer.endpoint = endpoint;
		transfer.type().set(libusb_transfer_type.LIBUSB_TRANSFER_TYPE_INTERRUPT);
		transfer.timeout = timeout;
		transfer.buffer = buffer;
		transfer.length = length;
		transfer.user_data = user_data;
		transfer.callback = callback;
	}

	/**
	 * Helper function to populate the required libusb_transfer fields for an isochronous transfer.
	 *
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
		libusb_device_handle dev_handle, byte endpoint, Pointer buffer, int length,
		int num_iso_packets, libusb_transfer_cb_fn callback, Pointer user_data, int timeout) {
		transfer.dev_handle = dev_handle;
		transfer.endpoint = endpoint;
		transfer.type().set(libusb_transfer_type.LIBUSB_TRANSFER_TYPE_ISOCHRONOUS);
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
	 *
	 * @param transfer
	 *            a transfer
	 * @param length
	 *            the length to set in each isochronous packet descriptor
	 *            libusb_get_max_packet_size()
	 */
	public static void libusb_set_iso_packet_lengths(libusb_transfer transfer, int length) {
		// TODO: write()? better way?
		libusb_iso_packet_descriptor[] iso_packet_descs = transfer.iso_packet_desc();
		for (int i = 0; i < iso_packet_descs.length; i++)
			iso_packet_descs[i].length = length;
	}

	/**
	 * Convenience function to locate the position of an isochronous packet within the buffer of an
	 * isochronous transfer.
	 *
	 * This is a thorough function which loops through all preceding packets, accumulating their
	 * lengths to find the position of the specified packet. Typically you will assign equal lengths
	 * to each packet in the transfer, and hence the above method is sub-optimal. You may wish to
	 * use libusb_get_iso_packet_buffer_simple() instead.
	 *
	 * @param transfer
	 *            a transfer
	 * @param packet
	 *            the packet to return the address of
	 * @return the base address of the packet buffer inside the transfer buffer, or NULL if the
	 *         packet does not exist. See libusb_get_iso_packet_buffer_simple()
	 */
	public static Pointer libusb_get_iso_packet_buffer(libusb_transfer transfer, int packet) {
		int offset = 0;
		if (packet > Short.MAX_VALUE) return NULL;
		if (packet >= transfer.num_iso_packets) return NULL;
		// TODO: write()? better way?
		libusb_iso_packet_descriptor[] iso_packet_descs = transfer.iso_packet_desc(packet);
		for (int i = 0; i < packet; i++)
			offset += iso_packet_descs[i].length;
		// TODO: is correct?
		return transfer.buffer.share(offset);
	}

	/**
	 * Convenience function to locate the position of an isochronous packet within the buffer of an
	 * isochronous transfer, for transfers where each packet is of identical size.
	 *
	 * This function relies on the assumption that every packet within the transfer is of identical
	 * size to the first packet. Calculating the location of the packet buffer is then just a simple
	 * calculation:
	 * 
	 * <pre>
	 * buffer + (packet_size * packet)
	 * </pre>
	 *
	 * Do not use this function on transfers other than those that have identical packet lengths for
	 * each packet.
	 *
	 * @param transfer
	 *            a transfer
	 * @param packet
	 *            the packet to return the address of
	 * @return the base address of the packet buffer inside the transfer buffer, or NULL if the
	 *         packet does not exist. See libusb_get_iso_packet_buffer()
	 */
	public static Pointer libusb_get_iso_packet_buffer_simple(libusb_transfer transfer,
		int packet) {
		if (packet > Short.MAX_VALUE) return NULL;
		if (packet >= transfer.num_iso_packets) return NULL;
		return transfer.buffer.share(transfer.iso_packet_desc.length * packet);
	}

	/* sync I/O */

	/*
	 * Native calls here
	 */

	/**
	 * Retrieve a descriptor from the default control pipe. This is a convenience function which
	 * formulates the appropriate control message to retrieve the descriptor.
	 *
	 * @param dev
	 *            a device handle
	 * @param desc_type
	 *            the descriptor type, see libusb_descriptor_type
	 * @param desc_index
	 *            the index of the descriptor to retrieve
	 * @param data
	 *            output buffer for descriptor
	 * @param length
	 *            size of data buffer
	 * @return number of bytes returned in data, or LIBUSB_ERROR code on failure
	 */
	public static int libusb_get_descriptor(libusb_device_handle dev, byte desc_type,
		byte desc_index, Pointer data, int length) throws LibUsbException {
		return verify(LIBUSB.libusb_control_transfer(dev, //
			(byte) libusb_endpoint_direction.LIBUSB_ENDPOINT_IN.value,
			(byte) libusb_standard_request.LIBUSB_REQUEST_GET_DESCRIPTOR.value,
			(short) ((desc_type << 8) | desc_index), (short) 0, data, (short) length, 1000),
			"control_transfer");
	}

	/**
	 * Retrieve a descriptor from a device. This is a convenience function which formulates the
	 * appropriate control message to retrieve the descriptor. The string returned is Unicode, as
	 * detailed in the USB specifications.
	 *
	 * @param dev
	 *            a device handle
	 * @param desc_index
	 *            the index of the descriptor to retrieve
	 * @param langid
	 *            the language ID for the string descriptor
	 * @param data
	 *            output buffer for descriptor
	 * @param length
	 *            size of data buffer
	 * 
	 * @return number of bytes returned in data, or LIBUSB_ERROR code on failure see
	 *         libusb_get_string_descriptor_ascii()
	 */
	public static int libusb_get_string_descriptor(libusb_device_handle dev, byte desc_index,
		short langid, Pointer data, int length) throws LibUsbException {
		return verify(LIBUSB.libusb_control_transfer(dev,
			(byte) libusb_endpoint_direction.LIBUSB_ENDPOINT_IN.value,
			(byte) libusb_standard_request.LIBUSB_REQUEST_GET_DESCRIPTOR.value,
			(short) ((libusb_descriptor_type.LIBUSB_DT_STRING.value << 8) | desc_index), langid,
			data, (short) length, 1000), "control_transfer");
	}

	private static int LIBUSB_MAX_DESCRIPTOR_SIZE = 255;

	public static String libusb_get_string_descriptor(libusb_device_handle dev, byte desc_index,
		short langid) throws LibUsbException {
		Memory m = new Memory(LIBUSB_MAX_DESCRIPTOR_SIZE);
		int size = libusb_get_string_descriptor(dev, desc_index, langid, m, (int) m.size());
		return m.share(0, size).getString(0);
	}

	/*
	 * Native calls here
	 */

	/* polling and timeouts */

	/*
	 * Native calls here
	 */

	/**
	 * Added from time.h
	 */
	public static class timeval extends Struct {
		private static final List<String> FIELDS = List.of( //
			"tv_sec", "tv_usec");

		public static class ByValue extends libusb_pollfd //
			implements Structure.ByValue {}

		public static class ByReference extends libusb_pollfd //
			implements Structure.ByReference {}

		public NativeLong tv_sec;
		public NativeLong tv_usec;

		public timeval() {}

		public timeval(Pointer p) {
			super(p);
		}

		@Override
		protected List<String> getFieldOrder() {
			return FIELDS;
		}
	}

	/**
	 * File descriptor for polling
	 */
	public static class libusb_pollfd extends Struct {
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

	/**
	 * Callback function, invoked when a new file descriptor should be added to the set of file
	 * descriptors monitored for events. @param fd the new file descriptor @param events events to
	 * monitor for, see libusb_pollfd for a description @param user_data User data pointer specified
	 * in libusb_set_pollfd_notifiers() call see libusb_set_pollfd_notifiers()
	 */
	// typedef void (LIBUSB_CALL *libusb_pollfd_removed_cb)(int fd, void *user_data);
	public static interface libusb_pollfd_added_cb extends Callback {
		public void invoke(int fd, short events, Pointer user_data);
	}

	/**
	 * Callback function, invoked when a file descriptor should be removed from the set of file
	 * descriptors being monitored for events. After returning from this callback, do not use that
	 * file descriptor again. @param fd the file descriptor to stop monitoring @param user_data User
	 * data pointer specified in libusb_set_pollfd_notifiers() call see
	 * libusb_set_pollfd_notifiers()
	 */
	// typedef void (LIBUSB_CALL *libusb_pollfd_removed_cb)(int fd, void *user_data);
	public static interface libusb_pollfd_removed_cb extends Callback {
		public void invoke(int fd, Pointer user_data);
	}

	/*
	 * Native calls here
	 */

	/**
	 * Callback handle.
	 *
	 * Callbacks handles are generated by libusb_hotplug_register_callback() and can be used to
	 * deregister callbacks. Callback handles are unique per libusb_context and it is safe to call
	 * libusb_hotplug_deregister_callback() on an already deregisted callback.
	 *
	 * Since version 1.0.16, LIBUSB_API_VERSION >= 0x01000102
	 *
	 * For more information, see hotplug.
	 */
	// typedef int libusb_hotplug_callback_handle;

	/**
	 * Since version 1.0.16, LIBUSB_API_VERSION >= 0x01000102
	 * 
	 * Flags for hotplug events
	 */
	public static enum libusb_hotplug_flag {
		LIBUSB_HOTPLUG_NO_FLAGS(0),
		LIBUSB_HOTPLUG_ENUMERATE(1 << 0);

		public static final TypeTranscoder.Single<libusb_hotplug_flag> xcoder =
			TypeTranscoder.single(t -> t.value, libusb_hotplug_flag.class);
		public final int value;

		private libusb_hotplug_flag(int value) {
			this.value = value;
		}

		@Override
		public String toString() {
			return String.format("%s(0x%02x)", name(), value);
		}
	}

	/**
	 * Since version 1.0.16, LIBUSB_API_VERSION >= 0x01000102
	 * 
	 * Hotplug events
	 */
	public static enum libusb_hotplug_event {
		LIBUSB_HOTPLUG_EVENT_DEVICE_ARRIVED(0x01),
		LIBUSB_HOTPLUG_EVENT_DEVICE_LEFT(0x02);

		public static final TypeTranscoder.Single<libusb_hotplug_event> xcoder =
			TypeTranscoder.single(t -> t.value, libusb_hotplug_event.class);
		public final int value;

		private libusb_hotplug_event(int value) {
			this.value = value;
		}

		@Override
		public String toString() {
			return String.format("%s(0x%02x)", name(), value);
		}
	}

	public static final int LIBUSB_HOTPLUG_MATCH_ANY = -1;

	/**
	 * Hotplug callback function type. When requesting hotplug event notifications, you pass a
	 * pointer to a callback function of this type.
	 *
	 * This callback may be called by an internal event thread and as such it is recommended the
	 * callback do minimal processing before returning.
	 *
	 * libusb will call this function later, when a matching event had happened on a matching
	 * device. See hotplug for more information.
	 *
	 * It is safe to call either libusb_hotplug_register_callback() or
	 * libusb_hotplug_deregister_callback() from within a callback function.
	 *
	 * Since version 1.0.16, LIBUSB_API_VERSION >= 0x01000102
	 *
	 * @param ctx
	 *            context of this notification
	 * @param device
	 *            libusb_device this event occurred on
	 * @param event
	 *            event that occurred
	 * @param user_data
	 *            user data provided when this callback was registered
	 * @return bool whether this callback is finished processing events. returning 1 will cause this
	 *         callback to be deregistered
	 */
	// typedef int (LIBUSB_CALL *libusb_hotplug_callback_fn)(libusb_context *ctx,
	// libusb_device *device, libusb_hotplug_event event, void *user_data);
	public static interface libusb_hotplug_callback_fn extends Callback {
		public void invoke(libusb_context ctx, Pointer device, int event, Pointer user_data);
	}

	/*
	 * Native calls here
	 */

	/*
	 * Added types / typed calls here
	 */

	public static class libusb_hotplug_callback_handle {
		public int value;
	}

	public static libusb_context libusb_init() throws LibUsbException {
		libusb_context.ByReference ctxPtr = new libusb_context.ByReference();
		verify(LIBUSB.libusb_init(ctxPtr), "init");
		return ctxPtr.typedValue();
	}

	public static void libusb_exit(libusb_context ctx) {
		LIBUSB.libusb_exit(ctx);
	}

	public static void libusb_set_debug(libusb_context ctx, libusb_log_level level) {
		LIBUSB.libusb_set_debug(ctx, level.value);
	}

	public static libusb_version libusb_get_version() {
		return LIBUSB.libusb_get_version();
	}

	public static boolean libusb_has_capability(libusb_capability capability) {
		return LIBUSB.libusb_has_capability(capability.value) != 0;
	}

	public static String libusb_error_name(libusb_error errcode) {
		return LIBUSB.libusb_error_name(errcode.value);
	}

	public static void libusb_setlocale(String locale) throws LibUsbException {
		verify(LIBUSB.libusb_setlocale(locale), "setlocale");
	}

	public static String libusb_strerror(libusb_error errcode) {
		return LIBUSB.libusb_strerror(errcode.value);
	}

	public static libusb_device.ArrayRef libusb_get_device_list(libusb_context ctx)
		throws LibUsbException {
		libusb_device.ArrayRef.ByRef listRef = new libusb_device.ArrayRef.ByRef();
		int size = verify(LIBUSB.libusb_get_device_list(ctx, listRef), "get_device_list");
		libusb_device.ArrayRef list = listRef.typedValue();
		list.setCount(size);
		return list;
	}

	public static void libusb_free_device_list(libusb_device.ArrayRef list) {
		LIBUSB.libusb_free_device_list(list, list.getCount());
	}

	public static libusb_device libusb_ref_device(libusb_device dev) {
		return LIBUSB.libusb_ref_device(dev);
	}

	public static void libusb_unref_device(libusb_device dev) {
		LIBUSB.libusb_unref_device(dev);
	}

	public static int libusb_get_configuration(libusb_device_handle dev) throws LibUsbException {
		IntByReference config = new IntByReference();
		verify(LIBUSB.libusb_get_configuration(dev, config), "get_configuration");
		return config.getValue();
	}

	public static libusb_device_descriptor libusb_get_device_descriptor(libusb_device device)
		throws LibUsbException {
		libusb_device_descriptor descriptor = new libusb_device_descriptor();
		verify(LIBUSB.libusb_get_device_descriptor(device, descriptor), "get_device_descriptor");
		return descriptor;
	}

	public static libusb_config_descriptor libusb_get_active_config_descriptor(libusb_device dev)
		throws LibUsbException {
		PointerByReference config = new PointerByReference();
		verify(LIBUSB.libusb_get_active_config_descriptor(dev, config),
			"get_active_config_descriptor");
		return new libusb_config_descriptor(config.getValue());
	}

	public static libusb_config_descriptor libusb_get_config_descriptor(libusb_device dev)
		throws LibUsbException {
		return libusb_get_config_descriptor(dev, (byte) 0);
	}

	public static libusb_config_descriptor libusb_get_config_descriptor(libusb_device dev,
		byte config_index) throws LibUsbException {
		PointerByReference config = new PointerByReference();
		verify(LIBUSB.libusb_get_config_descriptor(dev, config_index, config),
			"get_config_descriptor");
		return new libusb_config_descriptor(config.getValue());
	}

	public static libusb_config_descriptor libusb_get_config_descriptor_by_value(libusb_device dev,
		byte bConfigurationValue) throws LibUsbException {
		PointerByReference config = new PointerByReference();
		verify(LIBUSB.libusb_get_config_descriptor_by_value(dev, bConfigurationValue, config),
			"get_config_descriptor_by_value");
		return new libusb_config_descriptor(config.getValue());
	}

	public static void libusb_free_config_descriptor(libusb_config_descriptor config) {
		LIBUSB.libusb_free_config_descriptor(config.getPointer());
	}

	public static libusb_ss_endpoint_companion_descriptor
		libusb_get_ss_endpoint_companion_descriptor(libusb_context ctx,
			libusb_endpoint_descriptor endpoint) throws LibUsbException {
		PointerByReference ep_comp = new PointerByReference();
		verify(LIBUSB.libusb_get_ss_endpoint_companion_descriptor(ctx, endpoint, ep_comp),
			"get_config_descriptor_by_value");
		return new libusb_ss_endpoint_companion_descriptor(ep_comp.getValue());
	}

	public static void libusb_free_ss_endpoint_companion_descriptor(
		libusb_ss_endpoint_companion_descriptor ep_comp) {
		LIBUSB.libusb_free_ss_endpoint_companion_descriptor(ep_comp.getPointer());
	}

	public static libusb_bos_descriptor libusb_get_bos_descriptor(libusb_device_handle handle)
		throws LibUsbException {
		PointerByReference bos = new PointerByReference();
		verify(LIBUSB.libusb_get_bos_descriptor(handle, bos), "get_bos_descriptor");
		return new libusb_bos_descriptor(bos.getValue());
	}

	public static void libusb_free_bos_descriptor(libusb_bos_descriptor bos) {
		LIBUSB.libusb_free_bos_descriptor(bos.getPointer());
	}

	public static libusb_usb_2_0_extension_descriptor libusb_get_usb_2_0_extension_descriptor(
		libusb_context ctx, libusb_bos_dev_capability_descriptor dev_cap) throws LibUsbException {
		PointerByReference usb_2_0_extension = new PointerByReference();
		verify(LIBUSB.libusb_get_usb_2_0_extension_descriptor(ctx, dev_cap, usb_2_0_extension),
			"get_usb_2_0_extension_descriptor");
		return new libusb_usb_2_0_extension_descriptor(usb_2_0_extension.getValue());
	}

	public static void libusb_free_usb_2_0_extension_descriptor(
		libusb_usb_2_0_extension_descriptor usb_2_0_extension) {
		LIBUSB.libusb_free_usb_2_0_extension_descriptor(usb_2_0_extension.getPointer());
	}

	public static libusb_ss_usb_device_capability_descriptor
		libusb_get_ss_usb_device_capability_descriptor(libusb_context ctx,
			libusb_bos_dev_capability_descriptor dev_cap) throws LibUsbException {
		PointerByReference ss_usb_device_cap = new PointerByReference();
		verify(LIBUSB.libusb_get_ss_usb_device_capability_descriptor( //
			ctx, dev_cap, ss_usb_device_cap), "get_ss_usb_device_capability_descriptor");
		return new libusb_ss_usb_device_capability_descriptor(ss_usb_device_cap.getValue());
	}

	public static void libusb_free_ss_usb_device_capability_descriptor(
		libusb_ss_usb_device_capability_descriptor ss_usb_device_cap) {
		LIBUSB.libusb_free_ss_usb_device_capability_descriptor(ss_usb_device_cap.getPointer());
	}

	public static libusb_container_id_descriptor libusb_get_container_id_descriptor(
		libusb_context ctx, libusb_bos_dev_capability_descriptor dev_cap) throws LibUsbException {
		PointerByReference container_id = new PointerByReference();
		verify(LIBUSB.libusb_get_container_id_descriptor(ctx, dev_cap, container_id),
			"get_container_id_descriptor");
		return new libusb_container_id_descriptor(container_id.getValue());
	}

	public static void
		libusb_free_container_id_descriptor(libusb_container_id_descriptor container_id) {
		LIBUSB.libusb_free_container_id_descriptor(container_id.getPointer());
	}

	public static byte libusb_get_bus_number(libusb_device dev) {
		return LIBUSB.libusb_get_bus_number(dev);
	}

	public static byte libusb_get_port_number(libusb_device dev) {
		return LIBUSB.libusb_get_port_number(dev);
	}

	private static final int LIBUSB_MAX_PORT_NUMBERS = 7;

	public static byte[] libusb_get_port_numbers(libusb_device dev) throws LibUsbException {
		Memory memory = new Memory(LIBUSB_MAX_PORT_NUMBERS);
		int size = verify(LIBUSB.libusb_get_port_numbers(dev, memory, (int) memory.size()),
			"get_port_numbers");
		return memory.getByteArray(0, size);
	}

	public static libusb_device libusb_get_parent(libusb_device dev) {
		return LIBUSB.libusb_get_parent(dev);
	}

	public static byte libusb_get_device_address(libusb_device dev) {
		return LIBUSB.libusb_get_device_address(dev);
	}

	public static libusb_speed libusb_get_device_speed(libusb_device dev) throws LibUsbException {
		int speed = verify(LIBUSB.libusb_get_device_speed(dev), "get_device_speed");
		return libusb_speed.xcoder.decode(speed);
	}

	public static int libusb_get_max_packet_size(libusb_device dev, byte endpoint)
		throws LibUsbException {
		return verify(LIBUSB.libusb_get_max_packet_size(dev, endpoint), "get_max_packet_size");
	}

	public static int libusb_get_max_iso_packet_size(libusb_device dev, byte endpoint)
		throws LibUsbException {
		return verify(LIBUSB.libusb_get_max_iso_packet_size(dev, endpoint),
			"get_max_iso_packet_size");
	}

	public static libusb_device_handle libusb_open(libusb_device dev) throws LibUsbException {
		libusb_device_handle.ByReference handle = new libusb_device_handle.ByReference();
		verify(LIBUSB.libusb_open(dev, handle), "open");
		return handle.typedValue();
	}

	public static void libusb_close(libusb_device_handle dev_handle) {
		LIBUSB.libusb_close(dev_handle);
	}

	public static libusb_device libusb_get_device(libusb_device_handle dev_handle) {
		return LIBUSB.libusb_get_device(dev_handle);
	}

	public static void libusb_set_configuration(libusb_device_handle dev, int configuration)
		throws LibUsbException {
		verify(LIBUSB.libusb_set_configuration(dev, configuration), "set_configuration");
	}

	public static void libusb_claim_interface(libusb_device_handle dev, int interface_number)
		throws LibUsbException {
		verify(LIBUSB.libusb_claim_interface(dev, interface_number), "claim_interface");
	}

	public static void libusb_release_interface(libusb_device_handle dev, int interface_number)
		throws LibUsbException {
		verify(LIBUSB.libusb_release_interface(dev, interface_number), "release_interface");
	}

	public static libusb_device_handle libusb_open_device_with_vid_pid(libusb_context ctx,
		short vendor_id, short product_id) {
		return LIBUSB.libusb_open_device_with_vid_pid(ctx, vendor_id, product_id);
	}

	public static void libusb_set_interface_alt_setting(libusb_device_handle dev,
		int interface_number, int alternate_setting) throws LibUsbException {
		verify(LIBUSB.libusb_set_interface_alt_setting(dev, interface_number, alternate_setting),
			"set_interface_alt_setting");
	}

	public static void libusb_clear_halt(libusb_device_handle dev, byte endpoint)
		throws LibUsbException {
		verify(LIBUSB.libusb_clear_halt(dev, endpoint), "clear_halt");
	}

	public static void libusb_reset_device(libusb_device_handle dev) throws LibUsbException {
		verify(LIBUSB.libusb_reset_device(dev), "reset_device");
	}

	public static int libusb_alloc_streams(libusb_device_handle dev, int num_streams, byte endpoint)
		throws LibUsbException {
		return libusb_alloc_streams(dev, num_streams, new byte[] { endpoint });
	}

	public static int libusb_alloc_streams(libusb_device_handle dev, int num_streams,
		byte[] endpoints) throws LibUsbException {
		Memory m = JnaUtil.malloc(endpoints);
		return verify(LIBUSB.libusb_alloc_streams(dev, num_streams, m, endpoints.length),
			"alloc_streams");
	}

	public static void libusb_free_streams(libusb_device_handle dev, byte endpoint)
		throws LibUsbException {
		libusb_free_streams(dev, new byte[] { endpoint });
	}

	public static void libusb_free_streams(libusb_device_handle dev, byte[] endpoints)
		throws LibUsbException {
		Memory m = JnaUtil.malloc(endpoints);
		verify(LIBUSB.libusb_free_streams(dev, m, endpoints.length), "free_streams");
	}

	public static boolean libusb_kernel_driver_active(libusb_device_handle dev,
		int interface_number) throws LibUsbException {
		return verify(LIBUSB.libusb_kernel_driver_active(dev, interface_number),
			"kernel_driver_active") != 0;
	}

	public static void libusb_detach_kernel_driver(libusb_device_handle dev, int interface_number)
		throws LibUsbException {
		verify(LIBUSB.libusb_detach_kernel_driver(dev, interface_number), "detach_kernel_driver");
	}

	public static void libusb_attach_kernel_driver(libusb_device_handle dev, int interface_number)
		throws LibUsbException {
		verify(LIBUSB.libusb_attach_kernel_driver(dev, interface_number), "attach_kernel_driver");
	}

	public static void libusb_set_auto_detach_kernel_driver(libusb_device_handle dev,
		boolean enable) throws LibUsbException {
		verify(LIBUSB.libusb_set_auto_detach_kernel_driver(dev, enable ? 1 : 0),
			"set_auto_detach_kernel_driver");
	}

	public static libusb_transfer libusb_alloc_transfer(int iso_packets) {
		return LIBUSB.libusb_alloc_transfer(iso_packets);
	}

	public static void libusb_submit_transfer(libusb_transfer transfer) throws LibUsbException {
		verify(LIBUSB.libusb_submit_transfer(transfer), "submit_transfer");
	}

	public static void libusb_cancel_transfer(libusb_transfer transfer) throws LibUsbException {
		verify(LIBUSB.libusb_cancel_transfer(transfer), "cancel_transfer");
	}

	public static void libusb_free_transfer(libusb_transfer transfer) {
		LIBUSB.libusb_free_transfer(transfer);
	}

	public static void libusb_transfer_set_stream_id(libusb_transfer transfer, int stream_id) {
		LIBUSB.libusb_transfer_set_stream_id(transfer, stream_id);
	}

	public static int libusb_transfer_get_stream_id(libusb_transfer transfer) {
		return LIBUSB.libusb_transfer_get_stream_id(transfer);
	}

	public static int libusb_control_transfer(libusb_device_handle dev_handle, byte request_type,
		byte bRequest, short wValue, short wIndex, int timeout) throws LibUsbException {
		return libusb_control_transfer(dev_handle, request_type, bRequest, wValue, wIndex, null,
			(short) 0, timeout);
	}

	public static int libusb_control_transfer(libusb_device_handle dev_handle, byte request_type,
		byte bRequest, short wValue, short wIndex, byte[] data, int timeout)
		throws LibUsbException {
		Memory m = JnaUtil.malloc(data);
		return libusb_control_transfer(dev_handle, request_type, bRequest, wValue, wIndex, m,
			(short) m.size(), timeout);
	}

	public static int libusb_control_transfer(libusb_device_handle dev_handle, byte request_type,
		byte bRequest, short wValue, short wIndex, Pointer data, short wLength, int timeout)
		throws LibUsbException {
		return verify(LIBUSB.libusb_control_transfer(dev_handle, request_type, bRequest, wValue,
			wIndex, data, wLength, timeout), "control_transfer");
	}

	public static int libusb_bulk_transfer(libusb_device_handle dev_handle, byte endpoint,
		byte[] data, int timeout) throws LibUsbException {
		Memory m = JnaUtil.malloc(data);
		return libusb_bulk_transfer(dev_handle, endpoint, m, (int) m.size(), timeout);
	}

	public static int libusb_bulk_transfer(libusb_device_handle dev_handle, byte endpoint,
		Pointer data, int length, int timeout) throws LibUsbException {
		IntByReference actual_length = new IntByReference();
		verify(LIBUSB.libusb_bulk_transfer( //
			dev_handle, endpoint, data, length, actual_length, timeout), "bulk_transfer");
		return actual_length.getValue();
	}

	public static int libusb_interrupt_transfer(libusb_device_handle dev_handle, byte endpoint,
		byte[] data, int timeout) throws LibUsbException {
		Memory m = JnaUtil.malloc(data);
		return libusb_interrupt_transfer(dev_handle, endpoint, m, (int) m.size(), timeout);
	}

	public static int libusb_interrupt_transfer(libusb_device_handle dev_handle, byte endpoint,
		Pointer data, int length, int timeout) throws LibUsbException {
		IntByReference actual_length = new IntByReference();
		verify(LIBUSB.libusb_interrupt_transfer(dev_handle, endpoint, data, length, actual_length,
			timeout), "interrupt_transfer");
		return actual_length.getValue();
	}

	public static String libusb_get_string_descriptor_ascii(libusb_device_handle dev,
		byte desc_index) throws LibUsbException {
		if (desc_index == 0) return null;
		Memory m = new Memory(LIBUSB_MAX_DESCRIPTOR_SIZE);
		int size = verify(LIBUSB.libusb_get_string_descriptor_ascii( //
			dev, desc_index, m, (int) m.size()), "get_string_descriptor_ascii");
		return m.share(0, size).getString(0);
	}

	private static int verify(int result, String name) throws LibUsbException {
		if (result >= 0) return result;
		libusb_error error = libusb_error.xcoder.decode(result);
		throw new LibUsbException(String.format("libusb_%s failed: %d (%s)", name, result, error),
			error, result);
	}

	private static LibUsbNative loadLibrary(String name) {
		logger.info("Loading {} started", name);
		logger.info("Protected: {}", JnaUtil.setProtected());
		LibUsbNative lib = JnaUtil.loadLibrary(name, LibUsbNative.class);
		logger.info("Loading {} complete", name);
		return lib;
	}

}
