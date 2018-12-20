package ceri.serial.libusb;

import static ceri.serial.libusb.jna.LibUsb.libusb_cancel_transfer;
import static ceri.serial.libusb.jna.LibUsb.libusb_free_transfer;
import static ceri.serial.libusb.jna.LibUsb.libusb_submit_transfer;
import static ceri.serial.libusb.jna.LibUsb.libusb_transfer_get_stream_id;
import static ceri.serial.libusb.jna.LibUsb.libusb_transfer_set_stream_id;
import java.io.Closeable;
import java.util.function.Supplier;
import com.sun.jna.Pointer;
import ceri.serial.libusb.jna.LibUsb;
import ceri.serial.libusb.jna.LibUsb.libusb_control_setup;
import ceri.serial.libusb.jna.LibUsb.libusb_device_handle;
import ceri.serial.libusb.jna.LibUsb.libusb_transfer;
import ceri.serial.libusb.jna.LibUsb.libusb_transfer_cb_fn;
import ceri.serial.libusb.jna.LibUsbException;

public class LibUsbTransfer implements Closeable {
	private final Supplier<libusb_device_handle> handleSupplier;
	private libusb_transfer transfer;

	LibUsbTransfer(Supplier<libusb_device_handle> handleSupplier, libusb_transfer transfer) {
		this.handleSupplier = handleSupplier;
		this.transfer = transfer;
	}

	public void submit() throws LibUsbException {
		libusb_submit_transfer(transfer());
	}

	public void cancel() throws LibUsbException {
		libusb_cancel_transfer(transfer());
	}

	public void setStreamId(int stream_id) throws LibUsbException {
		libusb_transfer_set_stream_id(transfer(), stream_id);
	}

	public int streamId() throws LibUsbException {
		return libusb_transfer_get_stream_id(transfer());
	}

	public Pointer controlGetData() {
		return LibUsb.libusb_control_transfer_get_data(transfer());
	}

	public libusb_control_setup controlTransferGetSetup() {
		return LibUsb.libusb_control_transfer_get_setup(transfer());
	}

	public void fillControlTransfer(Pointer buffer, libusb_transfer_cb_fn callback,
		Pointer userData, int timeout) {
		LibUsb.libusb_fill_control_transfer(transfer(), handle(), buffer, callback, userData,
			timeout);
	}

	public void fillBulkTransfer(int endpoint, Pointer buffer, int length,
		libusb_transfer_cb_fn callback, Pointer userData, int timeout) {
		LibUsb.libusb_fill_bulk_transfer(transfer(), handle(), endpoint, buffer, length, callback,
			userData, timeout);
	}

	public void fillBulkStreamTransfer(int endpoint, int streamId, Pointer buffer, int length,
		libusb_transfer_cb_fn callback, Pointer userData, int timeout) {
		LibUsb.libusb_fill_bulk_stream_transfer(transfer(), handle(), endpoint, streamId, buffer,
			length, callback, userData, timeout);
	}

	public void fillInterruptTransfer(int endpoint, Pointer buffer, int length,
		libusb_transfer_cb_fn callback, Pointer userData, int timeout) {
		LibUsb.libusb_fill_interrupt_transfer(transfer(), handle(), endpoint, buffer, length,
			callback, userData, timeout);
	}

	public void fillIsoTransfer(int endpoint, Pointer buffer, int length, int numIsoPackets,
		libusb_transfer_cb_fn callback, Pointer userData, int timeout) {
		LibUsb.libusb_fill_iso_transfer(transfer(), handle(), endpoint, buffer, length,
			numIsoPackets, callback, userData, timeout);
	}

	public void setIsoPacketLengths(int length) {
		LibUsb.libusb_set_iso_packet_lengths(transfer(), length);
	}

	public Pointer isoPacketBuffer(int packet) {
		return LibUsb.libusb_get_iso_packet_buffer(transfer(), packet);
	}

	public Pointer isoPacketBufferSimple(int packet) {
		return LibUsb.libusb_get_iso_packet_buffer_simple(transfer(), packet);
	}

	@Override
	public void close() {
		libusb_free_transfer(transfer);
		transfer = null;
	}

	private libusb_transfer transfer() {
		if (transfer != null) return transfer;
		throw new IllegalStateException("Transfer has been closed");
	}

	private libusb_device_handle handle() {
		return handleSupplier.get();
	}

}
