package ceri.serial.libusb;

import java.nio.ByteBuffer;
import java.util.function.Consumer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ceri.common.collection.ArrayUtil;
import ceri.common.data.ByteProvider;
import ceri.common.function.RuntimeCloseable;
import ceri.log.util.LogUtil;
import ceri.serial.libusb.UsbTransfer.BulkStreams;
import ceri.serial.libusb.jna.LibUsb;
import ceri.serial.libusb.jna.LibUsb.libusb_capability;
import ceri.serial.libusb.jna.LibUsb.libusb_context;
import ceri.serial.libusb.jna.LibUsb.libusb_descriptor_type;
import ceri.serial.libusb.jna.LibUsb.libusb_device_handle;
import ceri.serial.libusb.jna.LibUsbException;

public class UsbDeviceHandle implements RuntimeCloseable {
	private static final Logger logger = LogManager.getLogger();
	private final Usb usb;
	private UsbDevice device = null;
	private libusb_device_handle handle;

	public static boolean canDetachKernelDriver() throws LibUsbException {
		return LibUsb
			.libusb_has_capability(libusb_capability.LIBUSB_CAP_SUPPORTS_DETACH_KERNEL_DRIVER);
	}

	UsbDeviceHandle(Usb usb, libusb_device_handle handle) {
		this.usb = usb;
		this.handle = handle;
	}

	public Usb usb() {
		return usb;
	}

	public UsbDevice device() throws LibUsbException {
		if (device == null) device = new UsbDevice(usb, LibUsb.libusb_get_device(handle()));
		return device;
	}

	public boolean kernelDriverActive(int interfaceNumber) throws LibUsbException {
		return LibUsb.libusb_kernel_driver_active(handle(), interfaceNumber);
	}

	public void detachKernelDriver(int interfaceNumber) throws LibUsbException {
		LibUsb.libusb_detach_kernel_driver(handle(), interfaceNumber);
	}

	public void attachKernelDriver(int interfaceNumber) throws LibUsbException {
		LibUsb.libusb_attach_kernel_driver(handle(), interfaceNumber);
	}

	public void autoDetachKernelDriver(boolean enable) throws LibUsbException {
		LibUsb.libusb_set_auto_detach_kernel_driver(handle(), enable);
	}

	public int configuration() throws LibUsbException {
		return LibUsb.libusb_get_configuration(handle());
	}

	public void configuration(int configuration) throws LibUsbException {
		LibUsb.libusb_set_configuration(handle(), configuration);
	}

	public ByteProvider descriptor(libusb_descriptor_type descType, int descIndex)
		throws LibUsbException {
		return ByteProvider.of(LibUsb.libusb_get_descriptor(handle(), descType, descIndex));
	}

	public String stringDescriptor(int descIndex, int langid) throws LibUsbException {
		return LibUsb.libusb_get_string_descriptor(handle(), descIndex, langid);
	}

	public String stringDescriptorAscii(int index) throws LibUsbException {
		return LibUsb.libusb_get_string_descriptor_ascii(handle(), (byte) index);
	}

	public void claimInterface(int interfaceNumber) throws LibUsbException {
		LibUsb.libusb_claim_interface(handle(), interfaceNumber);
	}

	public void releaseInterface(int interfaceNumber) throws LibUsbException {
		LibUsb.libusb_release_interface(handle(), interfaceNumber);
	}

	public void altSetting(int interfaceNumber, int alternateSetting) throws LibUsbException {
		LibUsb.libusb_set_interface_alt_setting(handle(), interfaceNumber, alternateSetting);
	}

	public void clearHalt(int endpoint) throws LibUsbException {
		LibUsb.libusb_clear_halt(handle(), endpoint);
	}

	public void resetDevice() throws LibUsbException {
		LibUsb.libusb_reset_device(handle());
	}

	/**
	 * Holder for allocated streams used for bulk transfers.
	 */
	public UsbTransfer.BulkStreams bulkStreams(int count, int... endPoints) throws LibUsbException {
		byte[] eps = ArrayUtil.bytes(endPoints);
		int n = LibUsb.libusb_alloc_streams(handle(), count, eps);
		return new BulkStreams(this, n, eps);
	}

	/**
	 * Allocates an async control transfer.
	 */
	public UsbTransfer.Control controlTransfer(Consumer<? super UsbTransfer.Control> callback)
		throws LibUsbException {
		return UsbTransfer.Control.alloc(this, callback);
	}

	/**
	 * Allocates an async bulk transfer.
	 */
	public UsbTransfer.Bulk bulkTransfer(Consumer<? super UsbTransfer.Bulk> callback)
		throws LibUsbException {
		return UsbTransfer.Bulk.alloc(this, callback);
	}

	/**
	 * Allocates an async interrupt transfer.
	 */
	public UsbTransfer.Interrupt interruptTransfer(Consumer<? super UsbTransfer.Interrupt> callback)
		throws LibUsbException {
		return UsbTransfer.Interrupt.alloc(this, callback);
	}

	/**
	 * Allocates an async isochronous transfer.
	 */
	public UsbTransfer.Iso isoTransfer(int packets, Consumer<? super UsbTransfer.Iso> callback)
		throws LibUsbException {
		return UsbTransfer.Iso.alloc(this, packets, callback);
	}

	/**
	 * Executes a synchronous control transfer without data.
	 */
	public void controlTransfer(int requestType, int request, int value, int index, int timeoutMs)
		throws LibUsbException {
		controlTransfer(requestType, request, value, index, 0, timeoutMs);
	}

	/**
	 * Executes a synchronous control transfer sending data. Returns the number of bytes
	 * transferred.
	 */
	public int controlTransfer(int requestType, int request, int value, int index, byte[] data,
		int timeoutMs) throws LibUsbException {
		return LibUsb.libusb_control_transfer(handle(), requestType, request, value, index, data,
			timeoutMs);
	}

	/**
	 * Executes a synchronous control transfer receiving data.
	 */
	public byte[] controlTransfer(int requestType, int request, int value, int index, int length,
		int timeoutMs) throws LibUsbException {
		return LibUsb.libusb_control_transfer(handle(), requestType, request, value, index, length,
			timeoutMs);
	}

	/**
	 * Executes a synchronous control transfer sending and/or receiving data. Returns the number of
	 * bytes transferred.
	 */
	public int controlTransfer(int requestType, int request, int value, int index, ByteBuffer data,
		int timeoutMs) throws LibUsbException {
		return LibUsb.libusb_control_transfer(handle(), requestType, request, value, index, data,
			timeoutMs);
	}

	/**
	 * Executes a synchronous bulk transfer sending data.
	 * Returns the number of bytes transferred.
	 */
	public int bulkTransfer(int endpoint, byte[] data, int timeoutMs) throws LibUsbException {
		return LibUsb.libusb_bulk_transfer(handle(), (byte) endpoint, data, timeoutMs);
	}

	/**
	 * Executes a synchronous bulk transfer receiving data.
	 */
	public byte[] bulkTransfer(int endpoint, int length, int timeoutMs) throws LibUsbException {
		return LibUsb.libusb_bulk_transfer(handle(), endpoint, length, timeoutMs);
	}

	/**
	 * Executes a synchronous bulk transfer sending and/or receiving data.
	 * Returns the number of bytes transferred.
	 */
	public int bulkTransfer(int endpoint, ByteBuffer data, int timeoutMs) throws LibUsbException {
		return LibUsb.libusb_bulk_transfer(handle(), endpoint, data, timeoutMs);
	}

	public int interruptTransfer(int endpoint, byte[] data, int timeoutMs) throws LibUsbException {
		return LibUsb.libusb_interrupt_transfer(handle(), (byte) endpoint, data, timeoutMs);
	}

	public byte[] interruptTransfer(int endpoint, int length, int timeoutMs)
		throws LibUsbException {
		return LibUsb.libusb_interrupt_transfer(handle(), (byte) endpoint, length, timeoutMs);
	}

	public int interruptTransfer(int endpoint, ByteBuffer data, int timeoutMs)
		throws LibUsbException {
		return LibUsb.libusb_interrupt_transfer(handle(), endpoint, data, timeoutMs);
	}

	/**
	 * Gets Binary Object Store descriptor via blocking device call. Returns null if unsupported.
	 */
	public UsbDescriptors.Bos bosDescriptor() throws LibUsbException {
		var desc = LibUsb.libusb_get_bos_descriptor(handle());
		return desc == null ? null : new UsbDescriptors.Bos(this, desc);
	}

	@Override
	public void close() {
		LogUtil.close(logger, device, () -> LibUsb.libusb_close(handle));
		handle = null;
	}

	libusb_device_handle handle() {
		if (handle != null) return handle;
		throw new IllegalStateException("Handle has been closed");
	}

	libusb_context context() {
		return usb.context();
	}
}
