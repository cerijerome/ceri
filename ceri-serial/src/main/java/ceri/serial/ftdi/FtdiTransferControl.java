package ceri.serial.ftdi;

import java.time.Duration;
import ceri.jna.clib.jna.CTime.timeval;
import ceri.jna.util.Struct;
import ceri.serial.ftdi.jna.LibFtdi;
import ceri.serial.ftdi.jna.LibFtdi.ftdi_transfer_control;
import ceri.serial.libusb.jna.LibUsbException;

public interface FtdiTransferControl {
	/** A no-op, stateless instance */
	FtdiTransferControl NULL = new Null() {};

	/**
	 * Waits for transfer to complete. Returns the number of bytes transferred.
	 */
	int dataDone() throws LibUsbException;

	/**
	 * Cancels the transfer.
	 */
	void dataCancel(Duration d) throws LibUsbException;

	/**
	 * An implementation that wraps ftdi_transfer_control.
	 */
	static FtdiTransferControl from(ftdi_transfer_control control) {
		return new FtdiTransferControl() {
			@Override
			public int dataDone() throws LibUsbException {
				return LibFtdi.ftdi_transfer_data_done(control);
			}

			@Override
			public void dataCancel(Duration d) throws LibUsbException {
				LibFtdi.ftdi_transfer_data_cancel(control, Struct.write(timeval.from(d)));
			}
		};
	}

	/**
	 * A no-op, stateless implementation.
	 */
	interface Null extends FtdiTransferControl {
		@Override
		default int dataDone() throws LibUsbException {
			return 0;
		}

		@Override
		default void dataCancel(Duration d) throws LibUsbException {}
	}
}
