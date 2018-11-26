package ceri.serial.jna.libusb;

import static ceri.serial.jna.JnaUtil.verify;
import static ceri.serial.jna.libusb.LibUsb.*;
import static ceri.serial.jna.libusb.LibUsb.libusb_endpoint_direction.LIBUSB_ENDPOINT_IN;
import static ceri.serial.jna.libusb.LibUsb.libusb_endpoint_direction.LIBUSB_ENDPOINT_OUT;
import static ceri.serial.jna.libusb.LibUsb.libusb_request_recipient.LIBUSB_RECIPIENT_DEVICE;
import static ceri.serial.jna.libusb.LibUsb.libusb_request_type.LIBUSB_REQUEST_TYPE_VENDOR;
import java.io.IOException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.sun.jna.ptr.ByteByReference;
import ceri.common.data.FieldTranscoder.Flag;
import ceri.common.data.FieldTranscoder.Single;
import ceri.common.math.MathUtil;
import ceri.common.util.BasicUtil;
import ceri.serial.jna.libusb.LibUsb.libusb_context;
import ceri.serial.jna.libusb.LibUsb.libusb_device;
import ceri.serial.jna.libusb.LibUsb.libusb_device_descriptor;
import ceri.serial.jna.libusb.LibUsb.libusb_device_handle;

public class LibUsbTest {
	private static final Logger logger = LogManager.getLogger();
	private static final byte FTDI_DEVICE_OUT_REQTYPE = LibUsb.libusb_request_type(
		LIBUSB_RECIPIENT_DEVICE, LIBUSB_REQUEST_TYPE_VENDOR, LIBUSB_ENDPOINT_OUT);
	private static final byte FTDI_DEVICE_IN_REQTYPE = LibUsb.libusb_request_type(
		LIBUSB_RECIPIENT_DEVICE, LIBUSB_REQUEST_TYPE_VENDOR, LIBUSB_ENDPOINT_IN);
	private static final byte SIO_SET_BITMODE_REQUEST = 0x0b;
	private static final byte SIO_READ_PINS_REQUEST = 0x0c;

	public static void main(String[] args) {
		libusb_endpoint_descriptor ed = new libusb_endpoint_descriptor();
		System.out.printf("%d %s(%d)%n", ed.bDescriptorType, ed.bDescriptorType().get(),
			ed.bDescriptorType().accessor().get());
		ed.bDescriptorType().set(libusb_descriptor_type.LIBUSB_DT_SUPERSPEED_HUB);
		System.out.printf("%d %s(%d)%n", ed.bDescriptorType, ed.bDescriptorType().get(),
			ed.bDescriptorType().accessor().get());

		System.out.printf("0x%02x 0x%02x %s(0x%02x)%n", ed.bEndpointAddress, ed.bEndpointNumber().get(),
			ed.bEndpointDirection().get(),
			ed.bEndpointDirection().get() == null ? 0 : ed.bEndpointDirection().get().value);
		ed.bEndpointDirection().set(LIBUSB_ENDPOINT_IN);
		System.out.printf("0x%02x 0x%02x %s(0x%02x)%n", ed.bEndpointAddress, ed.bEndpointNumber().get(),
			ed.bEndpointDirection().get(),
			ed.bEndpointDirection().get() == null ? 0 : ed.bEndpointDirection().get().value);
		ed.bEndpointNumber().set(19);
		System.out.printf("0x%02x 0x%02x %s(0x%02x)%n", ed.bEndpointAddress, ed.bEndpointNumber().get(),
			ed.bEndpointDirection().get(),
			ed.bEndpointDirection().get() == null ? 0 : ed.bEndpointDirection().get().value);
		ed.bEndpointAddress = (byte) 0xff;
		System.out.printf("0x%02x 0x%02x %s(0x%02x)%n", ed.bEndpointAddress, ed.bEndpointNumber().get(),
			ed.bEndpointDirection().get(),
			ed.bEndpointDirection().get() == null ? 0 : ed.bEndpointDirection().get().value);

		// bits 0:1 libusb_transfer_type, 2:3 libusb_iso_sync_type (iso only)
				// 4:5 libusb_iso_usage_type (both iso only) 6:7 reserved
		System.out.printf("0x%02x %s %s %s%n", ed.bmAttributes,
			value(ed.bmAttributesTransferType()),
			value(ed.bmAttributesIsoSyncType()),
			value(ed.bmAttributesIsoUsageType()));
		ed.bmAttributesTransferType().set(libusb_transfer_type.LIBUSB_TRANSFER_TYPE_BULK);
		ed.bmAttributesIsoSyncType().set(libusb_iso_sync_type.LIBUSB_ISO_SYNC_TYPE_ADAPTIVE);
		ed.bmAttributesIsoUsageType().set(libusb_iso_usage_type.LIBUSB_ISO_USAGE_TYPE_FEEDBACK);
		System.out.printf("0x%02x %s %s %s%n", ed.bmAttributes,
			value(ed.bmAttributesTransferType()),
			value(ed.bmAttributesIsoSyncType()),
			value(ed.bmAttributesIsoUsageType()));
	}
	
	private static String value(Single<?> t) {
		Object obj = t.get();
		return obj != null ? String.valueOf(obj) : String.format("null(0x%02x)", t.accessor().get());
	}
	
	private static String value(Flag<?> t) {
		Object obj = t.get();
		return obj != null ? String.valueOf(obj) : String.format("null(0x%02x)", t.accessor().get());
	}
	
	public static void main0(String[] args) throws IOException {
		System.out.printf("FTDI_DEVICE_OUT_REQTYPE=0x%02x%n", FTDI_DEVICE_OUT_REQTYPE);
		System.out.printf("FTDI_DEVICE_IN_REQTYPE=0x%02x%n", FTDI_DEVICE_IN_REQTYPE);
		if (true) return;
		logger.info("Started");
		// JnaUtil.setProtected();

		libusb_context ctx = LibUsb.libusb_init();

		// libusb_version version = usb.libusb_get_version();
		// System.out.println(version);
		libusb_device.ArrayRef list = LibUsb.libusb_get_device_list(ctx);
		libusb_device[] devices = list.typedArray();
		System.out.printf("%d items%n", devices.length);
		for (libusb_device device : devices) {
			libusb_device_descriptor descriptor = LibUsb.libusb_get_device_descriptor(device);
			System.out.println(descriptor);
			System.out.println(descriptor.bDescriptorType().get());
			System.out.println(descriptor.bDeviceClass().get());
			System.out.println();
			// System.out.printf("Device: 0x%04x 0x%04x%n", descriptor.idVendor,
			// descriptor.idProduct);
			// if (descriptor.idVendor == 0x0403 && descriptor.idProduct == 0x6001)
			// process(usb, ctx, device, descriptor);
		}

		LibUsb.libusb_free_device_list(list);
		LibUsb.libusb_exit(ctx);
	}

	// private static void process(libusb_context ctx, libusb_device device,
	// libusb_device_descriptor descriptor) throws IOException {
	//
	// libusb_device_handle handle = LibUsb.libusb_open(device);
	// logger.info("Claim interface");
	// LibUsb.libusb_claim_interface(handle, 0);
	//
	// // Enable bit-bang
	// logger.info("Enable bit-bang");
	// LibUsb.libusb_control_transfer(handle, (byte) 0x40, (byte) 0x0b, (short) 0x01ff,
	// (short) 0x0001, null, (short) 0, 500);
	//
	// ByteByReference buffer = new ByteByReference();
	// verify(LibUsb.libusb_control_transfer(handle, (byte) 0xc0, (byte) 0x0c, (short) 0x0000,
	// (short) 0x0001, buffer, (short) 1, 500), "control_transfer");
	// logger.info("Status: {}", buffer.getValue());
	//
	// for (int i = 0; i < 10; i++) {
	// buffer.setValue((byte) 0x0f);
	// logger.info("Switch: {}", buffer.getValue());
	// verify(LibUsb.libusb_control_transfer(handle, (byte) 0xc0, (byte) 0x0c, (short) 0x0000,
	// (short) 0x0001, buffer, (short) 1, 500), "control_transfer");
	//
	// BasicUtil.delay(1000);
	//
	// // buffer.setValue((byte) 0x00);
	// buffer.setValue((byte) MathUtil.randomInt(0, 255));
	//
	// logger.info("Switch: {}", buffer.getValue());
	// verify(LibUsb.libusb_control_transfer(handle, (byte) 0xc0, (byte) 0x0c, (short) 0x0000,
	// (short) 0x0001, buffer, (short) 1, 500), "control_transfer");
	//
	// BasicUtil.delay(1000);
	// }
	//
	// // Disable bit-bang
	// verify(LibUsb.libusb_control_transfer(handle, (byte) 0x40, (byte) 0x0b, (short) 0x0000,
	// (short) 0x0001, null, (short) 0, 500), "control_transfer");
	//
	// logger.info("Release interface");
	// verify(LibUsb.libusb_release_interface(handle, 0), "release_interface");
	//
	// LibUsb.libusb_close(handle);
	// }

}
