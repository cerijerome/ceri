package ceri.serial.libusb;

import static ceri.serial.libusb.jna.LibUsb.libusb_alloc_transfer;
import static ceri.serial.libusb.jna.LibUsb.libusb_attach_kernel_driver;
import static ceri.serial.libusb.jna.LibUsb.libusb_bulk_transfer;
import static ceri.serial.libusb.jna.LibUsb.libusb_claim_interface;
import static ceri.serial.libusb.jna.LibUsb.libusb_clear_halt;
import static ceri.serial.libusb.jna.LibUsb.libusb_control_transfer;
import static ceri.serial.libusb.jna.LibUsb.libusb_detach_kernel_driver;
import static ceri.serial.libusb.jna.LibUsb.libusb_fill_control_setup;
import static ceri.serial.libusb.jna.LibUsb.libusb_get_bos_descriptor;
import static ceri.serial.libusb.jna.LibUsb.libusb_get_configuration;
import static ceri.serial.libusb.jna.LibUsb.libusb_get_descriptor;
import static ceri.serial.libusb.jna.LibUsb.libusb_get_device;
import static ceri.serial.libusb.jna.LibUsb.libusb_get_string_descriptor;
import static ceri.serial.libusb.jna.LibUsb.libusb_get_string_descriptor_ascii;
import static ceri.serial.libusb.jna.LibUsb.libusb_interrupt_transfer;
import static ceri.serial.libusb.jna.LibUsb.libusb_kernel_driver_active;
import static ceri.serial.libusb.jna.LibUsb.libusb_release_interface;
import static ceri.serial.libusb.jna.LibUsb.libusb_reset_device;
import static ceri.serial.libusb.jna.LibUsb.libusb_set_auto_detach_kernel_driver;
import static ceri.serial.libusb.jna.LibUsb.libusb_set_configuration;
import static ceri.serial.libusb.jna.LibUsb.libusb_set_interface_alt_setting;
import static ceri.serial.libusb.jna.LibUsb.libusb_capability.LIBUSB_CAP_SUPPORTS_DETACH_KERNEL_DRIVER;
import java.io.Closeable;
import java.nio.ByteBuffer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.sun.jna.Pointer;
import ceri.common.collection.ArrayUtil;
import ceri.log.util.LogUtil;
import ceri.serial.libusb.jna.LibUsb;
import ceri.serial.libusb.jna.LibUsb.libusb_context;
import ceri.serial.libusb.jna.LibUsb.libusb_descriptor_type;
import ceri.serial.libusb.jna.LibUsb.libusb_device_handle;
import ceri.serial.libusb.jna.LibUsbException;

public class UsbDeviceHandle implements Closeable {
	private static final Logger logger = LogManager.getLogger();
	private final Usb usb;
	private UsbDevice device = null;
	private libusb_device_handle handle;

	// TODO: move elsewhere
	public static void fillControlSetup(Pointer buffer, int bmRequestType, int bRequest, int wValue,
		int wIndex, int wLength) {
		libusb_fill_control_setup(buffer, bmRequestType, bRequest, wValue, wIndex, wLength);
	}

	public static boolean canDetachKernelDriver() {
		return LibUsb.libusb_has_capability(LIBUSB_CAP_SUPPORTS_DETACH_KERNEL_DRIVER);
	}

	UsbDeviceHandle(Usb usb, libusb_device_handle handle) {
		this.usb = usb;
		this.handle = handle;
	}

	public Usb usb() {
		return usb;
	}

	public UsbDevice device() throws LibUsbException {
		if (device == null) device = new UsbDevice(usb, libusb_get_device(handle()));
		return device;
	}

	public boolean kernelDriverActive(int interfaceNumber) throws LibUsbException {
		return libusb_kernel_driver_active(handle(), interfaceNumber);
	}

	public void detachKernelDriver(int interfaceNumber) throws LibUsbException {
		libusb_detach_kernel_driver(handle(), interfaceNumber);
	}

	public void attachKernelDriver(int interfaceNumber) throws LibUsbException {
		libusb_attach_kernel_driver(handle(), interfaceNumber);
	}

	public void setAutoDetachKernelDriver(boolean enable) throws LibUsbException {
		libusb_set_auto_detach_kernel_driver(handle(), enable);
	}

	public int configuration() throws LibUsbException {
		return libusb_get_configuration(handle());
	}

	public void setConfiguration(int configuration) throws LibUsbException {
		libusb_set_configuration(handle(), configuration);
	}

	public byte[] descriptor(libusb_descriptor_type descType, int descIndex)
		throws LibUsbException {
		return libusb_get_descriptor(handle(), descType, descIndex);
	}

	public String stringDescriptor(int descIndex, int langid) throws LibUsbException {
		return libusb_get_string_descriptor(handle(), descIndex, langid);
	}

	public String stringDescriptorAscii(int index) throws LibUsbException {
		return libusb_get_string_descriptor_ascii(handle(), (byte) index);
	}

	public void claimInterface(int interfaceNumber) throws LibUsbException {
		libusb_claim_interface(handle(), interfaceNumber);
	}

	public void releaseInterface(int interfaceNumber) throws LibUsbException {
		libusb_release_interface(handle(), interfaceNumber);
	}

	public void setInterfaceAltSetting(int interfaceNumber, int alternateSetting)
		throws LibUsbException {
		libusb_set_interface_alt_setting(handle(), interfaceNumber, alternateSetting);
	}

	public void clearHalt(int endpoint) throws LibUsbException {
		libusb_clear_halt(handle(), endpoint);
	}

	public void resetDevice() throws LibUsbException {
		libusb_reset_device(handle());
	}

	public UsbTransfer allocTransfer(int isoPackets) throws LibUsbException {
		return new UsbTransfer(this::handle, libusb_alloc_transfer(isoPackets));
	}

	public UsbBulkStreams allocateBulkStreams(int count, int... endPoints) throws LibUsbException {
		return UsbBulkStreams.allocate(this::handle, count, ArrayUtil.bytes(endPoints));
	}

	public int controlTransfer(int requestType, int request, int value, int index, int timeout)
		throws LibUsbException {
		return libusb_control_transfer(handle(), (byte) requestType, (byte) request, (short) value,
			(short) index, null, (short) 0, timeout);
	}

	public int controlTransfer(int requestType, int request, int value, int index, byte[] data,
		int timeout) throws LibUsbException {
		return libusb_control_transfer(handle(), (byte) requestType, (byte) request, (short) value,
			(short) index, data, timeout);
	}

	public byte[] controlTransfer(int requestType, int request, int value, int index, int length,
		int timeout) throws LibUsbException {
		return libusb_control_transfer(handle(), (byte) requestType, (byte) request, (short) value,
			(short) index, (short) length, timeout);
	}

	public int controlTransfer(int requestType, int bRequest, int wValue, int wIndex,
		ByteBuffer data, int timeout) throws LibUsbException {
		return libusb_control_transfer(handle(), requestType, bRequest, wValue, wIndex, data,
			timeout);
	}

	public int controlTransfer(int requestType, int request, int value, int index,
		ByteBuffer buffer, int length, int timeout) throws LibUsbException {
		return libusb_control_transfer(handle(), (byte) requestType, (byte) request, (short) value,
			(short) index, buffer, (short) length, timeout);
	}

	public byte[] bulkTransfer(int endpoint, int length, int timeout) throws LibUsbException {
		return libusb_bulk_transfer(handle(), endpoint, length, timeout);
	}

	public int bulkTransfer(int endpoint, byte[] data, int timeout) throws LibUsbException {
		return libusb_bulk_transfer(handle(), (byte) endpoint, data, timeout);
	}

	public int bulkTransfer(int endpoint, ByteBuffer data, int timeout) throws LibUsbException {
		return libusb_bulk_transfer(handle(), endpoint, data, timeout);
	}

	public int bulkTransfer(int endpoint, ByteBuffer data, int length, int timeout)
		throws LibUsbException {
		return libusb_bulk_transfer(handle(), (byte) endpoint, data, length, timeout);
	}

	public int interruptTransfer(int endpoint, byte[] data, int timeout) throws LibUsbException {
		return libusb_interrupt_transfer(handle(), (byte) endpoint, data, timeout);
	}

	public byte[] interruptTransfer(int endpoint, int length, int timeout) throws LibUsbException {
		return libusb_interrupt_transfer(handle(), (byte) endpoint, length, timeout);
	}

	public int interruptTransfer(int endpoint, ByteBuffer data, int timeout)
		throws LibUsbException {
		return libusb_interrupt_transfer(handle(), endpoint, data, timeout);
	}

	public int interruptTransfer(int endpoint, ByteBuffer data, int length, int timeout)
		throws LibUsbException {
		return libusb_interrupt_transfer(handle(), (byte) endpoint, data, length, timeout);
	}

	/**
	 * Gets Binary Object Store descriptor via blocking device call. Returns null if unsupported.
	 */
	public UsbDescriptors.Bos bosDescriptor() throws LibUsbException {
		var desc = libusb_get_bos_descriptor(handle());
		return desc == null ? null : new UsbDescriptors.Bos(this, desc);
	}

	public libusb_device_handle handle() {
		if (handle != null) return handle;
		throw new IllegalStateException("Handle has been closed");
	}

	@Override
	public void close() {
		LogUtil.execute(logger, () -> LibUsb.libusb_close(handle));
		handle = null;
	}

	libusb_context context() {
		return usb.context();
	}

}
