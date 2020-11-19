package ceri.serial.libusb;

import static ceri.serial.libusb.jna.LibUsb.libusb_alloc_streams;
import static ceri.serial.libusb.jna.LibUsb.libusb_alloc_transfer;
import static ceri.serial.libusb.jna.LibUsb.libusb_attach_kernel_driver;
import static ceri.serial.libusb.jna.LibUsb.libusb_bulk_transfer;
import static ceri.serial.libusb.jna.LibUsb.libusb_claim_interface;
import static ceri.serial.libusb.jna.LibUsb.libusb_clear_halt;
import static ceri.serial.libusb.jna.LibUsb.libusb_control_transfer;
import static ceri.serial.libusb.jna.LibUsb.libusb_detach_kernel_driver;
import static ceri.serial.libusb.jna.LibUsb.libusb_endpoint_address;
import static ceri.serial.libusb.jna.LibUsb.libusb_fill_control_setup;
import static ceri.serial.libusb.jna.LibUsb.libusb_free_streams;
import static ceri.serial.libusb.jna.LibUsb.libusb_get_bos_descriptor;
import static ceri.serial.libusb.jna.LibUsb.libusb_get_configuration;
import static ceri.serial.libusb.jna.LibUsb.libusb_get_descriptor;
import static ceri.serial.libusb.jna.LibUsb.libusb_get_device;
import static ceri.serial.libusb.jna.LibUsb.libusb_get_string_descriptor;
import static ceri.serial.libusb.jna.LibUsb.libusb_get_string_descriptor_ascii;
import static ceri.serial.libusb.jna.LibUsb.libusb_interrupt_transfer;
import static ceri.serial.libusb.jna.LibUsb.libusb_kernel_driver_active;
import static ceri.serial.libusb.jna.LibUsb.libusb_release_interface;
import static ceri.serial.libusb.jna.LibUsb.libusb_request_type_value;
import static ceri.serial.libusb.jna.LibUsb.libusb_reset_device;
import static ceri.serial.libusb.jna.LibUsb.libusb_set_auto_detach_kernel_driver;
import static ceri.serial.libusb.jna.LibUsb.libusb_set_configuration;
import static ceri.serial.libusb.jna.LibUsb.libusb_set_interface_alt_setting;
import static ceri.serial.libusb.jna.LibUsb.libusb_capability.LIBUSB_CAP_SUPPORTS_DETACH_KERNEL_DRIVER;
import java.io.Closeable;
import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.sun.jna.Pointer;
import ceri.common.collection.ArrayUtil;
import ceri.log.util.LogUtil;
import ceri.serial.libusb.jna.LibUsb;
import ceri.serial.libusb.jna.LibUsb.libusb_context;
import ceri.serial.libusb.jna.LibUsb.libusb_descriptor_type;
import ceri.serial.libusb.jna.LibUsb.libusb_device;
import ceri.serial.libusb.jna.LibUsb.libusb_device_handle;
import ceri.serial.libusb.jna.LibUsb.libusb_endpoint_direction;
import ceri.serial.libusb.jna.LibUsb.libusb_request_recipient;
import ceri.serial.libusb.jna.LibUsb.libusb_request_type;
import ceri.serial.libusb.jna.LibUsbException;

public class UsbDeviceHandle implements Closeable {
	private static final Logger logger = LogManager.getLogger();
	private final Supplier<libusb_context> contextSupplier;
	private libusb_device_handle handle;

	public static int requestTypeValue(libusb_request_recipient recipient, libusb_request_type type,
		libusb_endpoint_direction endpointDirection) {
		return libusb_request_type_value(recipient, type, endpointDirection);
	}

	public static int endpointAddress(int value, libusb_endpoint_direction direction) {
		return libusb_endpoint_address(value, direction);
	}

	public static void fillControlSetup(Pointer buffer, int bmRequestType, int bRequest, int wValue,
		int wIndex, int wLength) {
		libusb_fill_control_setup(buffer, bmRequestType, bRequest, wValue, wIndex, wLength);
	}

	public static boolean canDetachKernelDriver() {
		return Usb.hasCapability(LIBUSB_CAP_SUPPORTS_DETACH_KERNEL_DRIVER);
	}

	UsbDeviceHandle(Supplier<libusb_context> contextSupplier, libusb_device_handle handle) {
		this.contextSupplier = contextSupplier;
		this.handle = handle;
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

	public UsbDevice device() throws LibUsbException {
		libusb_device device = libusb_get_device(handle());
		return new UsbDevice(contextSupplier, device);
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

	public int allocStreams(int streams, int... endPoints) throws LibUsbException {
		return libusb_alloc_streams(handle(), (byte) streams, ArrayUtil.bytes(endPoints));
	}

	public int allocStreams(int streams, List<Integer> endPoints) throws LibUsbException {
		return libusb_alloc_streams(handle(), (byte) streams, ArrayUtil.bytes(endPoints));
	}

	public void freeStreams(int... endPoints) throws LibUsbException {
		libusb_free_streams(handle(), ArrayUtil.bytes(endPoints));
	}

	public void freeStreams(Collection<Integer> endPoints) throws LibUsbException {
		libusb_free_streams(handle(), ArrayUtil.bytes(endPoints));
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

	public UsbDescriptor.Bos bosDescriptor() throws LibUsbException {
		return new UsbDescriptor.Bos(libusb_get_bos_descriptor(handle()));
	}

	@Override
	public void close() {
		LogUtil.execute(logger, () -> LibUsb.libusb_close(handle));
		handle = null;
	}

	public libusb_device_handle handle() {
		if (handle != null) return handle;
		throw new IllegalStateException("Handle has been closed");
	}

	public libusb_context context() {
		return contextSupplier.get();
	}

}
