package ceri.serial.libusb;

import static ceri.serial.libusb.jna.LibUsb.libusb_free_bos_descriptor;
import static ceri.serial.libusb.jna.LibUsb.libusb_free_container_id_descriptor;
import static ceri.serial.libusb.jna.LibUsb.libusb_free_ss_endpoint_companion_descriptor;
import static ceri.serial.libusb.jna.LibUsb.libusb_free_ss_usb_device_capability_descriptor;
import static ceri.serial.libusb.jna.LibUsb.libusb_free_usb_2_0_extension_descriptor;
import java.io.Closeable;
import ceri.serial.libusb.jna.LibUsb.libusb_bos_descriptor;
import ceri.serial.libusb.jna.LibUsb.libusb_container_id_descriptor;
import ceri.serial.libusb.jna.LibUsb.libusb_ss_endpoint_companion_descriptor;
import ceri.serial.libusb.jna.LibUsb.libusb_ss_usb_device_capability_descriptor;
import ceri.serial.libusb.jna.LibUsb.libusb_usb_2_0_extension_descriptor;

public class UsbDescriptor {

	public static class SsEndpointCompanion implements Closeable {
		private libusb_ss_endpoint_companion_descriptor descriptor;

		SsEndpointCompanion(libusb_ss_endpoint_companion_descriptor descriptor) {
			this.descriptor = descriptor;
		}

		public libusb_ss_endpoint_companion_descriptor descriptor() {
			if (descriptor != null) return descriptor;
			throw new IllegalStateException("Descriptor has been closed");
		}

		@Override
		public void close() {
			libusb_free_ss_endpoint_companion_descriptor(descriptor);
			descriptor = null;
		}
	}

	public static class Bos implements Closeable {
		private libusb_bos_descriptor descriptor;

		Bos(libusb_bos_descriptor descriptor) {
			this.descriptor = descriptor;
		}

		public libusb_bos_descriptor descriptor() {
			if (descriptor != null) return descriptor;
			throw new IllegalStateException("Descriptor has been closed");
		}

		@Override
		public void close() {
			libusb_free_bos_descriptor(descriptor);
			descriptor = null;
		}
	}

	public static class Usb20Extension implements Closeable {
		private libusb_usb_2_0_extension_descriptor descriptor;

		Usb20Extension(libusb_usb_2_0_extension_descriptor descriptor) {
			this.descriptor = descriptor;
		}

		public libusb_usb_2_0_extension_descriptor descriptor() {
			if (descriptor != null) return descriptor;
			throw new IllegalStateException("Descriptor has been closed");
		}

		@Override
		public void close() {
			libusb_free_usb_2_0_extension_descriptor(descriptor);
			descriptor = null;
		}
	}

	public static class SsUsbDeviceCapability implements Closeable {
		private libusb_ss_usb_device_capability_descriptor descriptor;

		SsUsbDeviceCapability(libusb_ss_usb_device_capability_descriptor descriptor) {
			this.descriptor = descriptor;
		}

		public libusb_ss_usb_device_capability_descriptor descriptor() {
			if (descriptor != null) return descriptor;
			throw new IllegalStateException("Descriptor has been closed");
		}

		@Override
		public void close() {
			libusb_free_ss_usb_device_capability_descriptor(descriptor);
			descriptor = null;
		}
	}

	public static class ContainerId implements Closeable {
		private libusb_container_id_descriptor descriptor;

		ContainerId(libusb_container_id_descriptor descriptor) {
			this.descriptor = descriptor;
		}

		public libusb_container_id_descriptor descriptor() {
			if (descriptor != null) return descriptor;
			throw new IllegalStateException("Descriptor has been closed");
		}

		@Override
		public void close() {
			libusb_free_container_id_descriptor(descriptor);
			descriptor = null;
		}
	}

	private UsbDescriptor() {}

}