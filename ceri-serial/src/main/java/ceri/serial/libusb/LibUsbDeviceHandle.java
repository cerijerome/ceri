package ceri.serial.libusb;

import static ceri.common.data.ByteUtil.bytes;
import static ceri.common.data.ByteUtil.toByteArray;
import java.io.Closeable;
import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.List;
import ceri.serial.libusb.jna.LibUsb;
import ceri.serial.libusb.jna.LibUsb.libusb_device;
import ceri.serial.libusb.jna.LibUsb.libusb_device_handle;
import ceri.serial.libusb.jna.LibUsbException;

public class LibUsbDeviceHandle implements Closeable {
	public final LibUsbContext ctx;
	private final libusb_device_handle handle;

	LibUsbDeviceHandle(LibUsbContext ctx, libusb_device_handle handle) {
		this.ctx = ctx;
		this.handle = handle;
	}

	public boolean kernelDriverActive(int interfaceNumber) throws LibUsbException {
		return LibUsb.libusb_kernel_driver_active(handle, interfaceNumber);
	}

	public void detachKernelDriver(int interfaceNumber) throws LibUsbException {
		LibUsb.libusb_detach_kernel_driver(handle, interfaceNumber);
	}

	public void attachKernelDriver(int interfaceNumber) throws LibUsbException {
		LibUsb.libusb_attach_kernel_driver(handle, interfaceNumber);
	}

	public void setAutoDetachKernelDriver(boolean enable) throws LibUsbException {
		LibUsb.libusb_set_auto_detach_kernel_driver(handle, enable);
	}

	public int configuration() throws LibUsbException {
		return LibUsb.libusb_get_configuration(handle);
	}

	public void setConfiguration(int configuration) throws LibUsbException {
		LibUsb.libusb_set_configuration(handle, configuration);
	}

	public String stringDescriptorAscii(int index) throws LibUsbException {
		return LibUsb.libusb_get_string_descriptor_ascii(handle, (byte) index);
	}

	public LibUsbDevice device() throws LibUsbException {
		libusb_device device = LibUsb.libusb_get_device(handle);
		return new LibUsbDevice(ctx, device);
	}

	public void claimInterface(int interfaceNumber) throws LibUsbException {
		LibUsb.libusb_claim_interface(handle, interfaceNumber);
	}

	public void releaseInterface(int interfaceNumber) throws LibUsbException {
		LibUsb.libusb_release_interface(handle, interfaceNumber);
	}

	public void setInterfaceAltSetting(int interfaceNumber, int alternateSetting)
		throws LibUsbException {
		LibUsb.libusb_set_interface_alt_setting(handle, interfaceNumber, alternateSetting);
	}

	public void clearHalt(byte endpoint) throws LibUsbException {
		LibUsb.libusb_clear_halt(handle, endpoint);
	}

	public void resetDevice() throws LibUsbException {
		LibUsb.libusb_reset_device(handle);
	}

	public int allocStreams(int streams, int... endPoints) throws LibUsbException {
		return LibUsb.libusb_alloc_streams(handle, (byte) streams, bytes(endPoints));
	}

	public int allocStreams(int streams, List<Integer> endPoints) throws LibUsbException {
		return LibUsb.libusb_alloc_streams(handle, (byte) streams, toByteArray(endPoints));
	}

	public void freeStreams(int... endPoints) throws LibUsbException {
		LibUsb.libusb_free_streams(handle, bytes(endPoints));
	}

	public void freeStreams(Collection<Integer> endPoints) throws LibUsbException {
		LibUsb.libusb_free_streams(handle, toByteArray(endPoints));
	}

	public int controlTransfer(int requestType, int request, int value, int index, int timeout)
		throws LibUsbException {
		return LibUsb.libusb_control_transfer(handle, (byte) requestType, (byte) request,
			(short) value, (short) index, null, (short) 0, timeout);
	}

	public int controlTransfer(int requestType, int request, int value, int index, byte[] data,
		int timeout) throws LibUsbException {
		return LibUsb.libusb_control_transfer(handle, (byte) requestType, (byte) request,
			(short) value, (short) index, data, timeout);
	}

	public byte[] controlTransfer(int requestType, int request, int value, int index, int length,
		int timeout) throws LibUsbException {
		return LibUsb.libusb_control_transfer(handle, (byte) requestType, (byte) request,
			(short) value, (short) index, (short) length, timeout);
	}

	public int controlTransfer(int requestType, int request, int value, int index,
		ByteBuffer buffer, int length, int timeout) throws LibUsbException {
		return LibUsb.libusb_control_transfer(handle, (byte) requestType, (byte) request,
			(short) value, (short) index, buffer, (short) length, timeout);
	}

	public int bulkTransfer(int endpoint, byte[] data, int timeout) throws LibUsbException {
		return LibUsb.libusb_bulk_transfer(handle, (byte) endpoint, data, timeout);
	}

	public int bulkTransfer(int endpoint, ByteBuffer data, int length, int timeout)
		throws LibUsbException {
		return LibUsb.libusb_bulk_transfer(handle, (byte) endpoint, data, length, timeout);
	}

	public int interruptTransfer(int endpoint, byte[] data, int timeout) throws LibUsbException {
		return LibUsb.libusb_interrupt_transfer(handle, (byte) endpoint, data, timeout);
	}

	public byte[] interruptTransfer(int endpoint, int length, int timeout) throws LibUsbException {
		return LibUsb.libusb_interrupt_transfer(handle, (byte) endpoint, length, timeout);
	}

	public int interruptTransfer(int endpoint, ByteBuffer data, int length, int timeout)
		throws LibUsbException {
		return LibUsb.libusb_interrupt_transfer(handle, (byte) endpoint, data, length, timeout);
	}

	@Override
	public void close() {
		LibUsb.libusb_close(handle);
	}

}
