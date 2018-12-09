package ceri.serial.libusb;

import java.io.Closeable;
import ceri.serial.libusb.jna.LibUsb;
import ceri.serial.libusb.jna.LibUsb.libusb_transfer;
import ceri.serial.libusb.jna.LibUsbException;

public class LibUsbTransfer implements Closeable {
	private final libusb_transfer transfer;

	LibUsbTransfer(libusb_transfer transfer) {
		this.transfer = transfer;
	}

	public void submit() throws LibUsbException {
		LibUsb.libusb_submit_transfer(transfer);
	}

	public void cancel() throws LibUsbException {
		LibUsb.libusb_cancel_transfer(transfer);
	}

	public void setStreamId(int stream_id) throws LibUsbException {
		LibUsb.libusb_transfer_set_stream_id(transfer, stream_id);
	}

	public int streamId() throws LibUsbException {
		return LibUsb.libusb_transfer_get_stream_id(transfer);
	}

	@Override
	public void close() {
		LibUsb.libusb_free_transfer(transfer);
	}

}
