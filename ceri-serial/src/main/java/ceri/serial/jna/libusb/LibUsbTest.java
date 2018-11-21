package ceri.serial.jna.libusb;

import static ceri.serial.jna.JnaUtil.verify;
import java.io.IOException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.sun.jna.ptr.ByteByReference;
import ceri.common.math.MathUtil;
import ceri.common.util.BasicUtil;
import ceri.serial.jna.libusb.LibUsb.libusb_context;
import ceri.serial.jna.libusb.LibUsb.libusb_device;
import ceri.serial.jna.libusb.LibUsb.libusb_device_descriptor;
import ceri.serial.jna.libusb.LibUsb.libusb_device_handle;

public class LibUsbTest {
	private static final Logger logger = LogManager.getLogger();

	public static void main(String[] args) throws IOException {
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
			//System.out.printf("Device: 0x%04x 0x%04x%n", descriptor.idVendor,
			//descriptor.idProduct);
			//if (descriptor.idVendor == 0x0403 && descriptor.idProduct == 0x6001)
			//	process(usb, ctx, device, descriptor);
		}

		LibUsb.libusb_free_device_list(list);
		LibUsb.libusb_exit(ctx);
	}

	private static void process(LibUsbNative usb, libusb_context ctx, libusb_device device,
		libusb_device_descriptor descriptor) throws IOException {
		
		libusb_device_handle.ByReference handleRef = new libusb_device_handle.ByReference();
		verify(usb.libusb_open(device, handleRef), "open");
		libusb_device_handle handle = handleRef.typedValue();

		logger.info("Claim interface");
		verify(usb.libusb_claim_interface(handle, 0), "claim_interface");

		// Enable bit-bang
		logger.info("Enable bit-bang");
		verify(usb.libusb_control_transfer(handle, (byte) 0x40, (byte) 0x0b, (short) 0x01ff,
			(short) 0x0001, null, (short) 0, 500), "control_transfer");

		ByteByReference buffer = new ByteByReference();
		verify(usb.libusb_control_transfer(handle, (byte) 0xc0, (byte) 0x0c, (short) 0x0000,
			(short) 0x0001, buffer, (short) 1, 500), "control_transfer");
		logger.info("Status: {}", buffer.getValue());

		for (int i = 0; i < 10; i++) {
			buffer.setValue((byte) 0x0f);
			logger.info("Switch: {}", buffer.getValue());
			verify(usb.libusb_control_transfer(handle, (byte) 0xc0, (byte) 0x0c, (short) 0x0000,
				(short) 0x0001, buffer, (short) 1, 500), "control_transfer");

			BasicUtil.delay(1000);

			//buffer.setValue((byte) 0x00);
			buffer.setValue((byte) MathUtil.randomInt(0, 255));
			
			logger.info("Switch: {}", buffer.getValue());
			verify(usb.libusb_control_transfer(handle, (byte) 0xc0, (byte) 0x0c, (short) 0x0000,
				(short) 0x0001, buffer, (short) 1, 500), "control_transfer");

			BasicUtil.delay(1000);
		}

		// Disable bit-bang
		verify(usb.libusb_control_transfer(handle, (byte) 0x40, (byte) 0x0b, (short) 0x0000,
			(short) 0x0001, null, (short) 0, 500), "control_transfer");

		logger.info("Release interface");
		verify(usb.libusb_release_interface(handle, 0), "release_interface");

		usb.libusb_close(handle);
	}

}
