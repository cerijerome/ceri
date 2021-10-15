package ceri.serial.ftdi;

import java.time.Duration;
import ceri.serial.clib.jna.CTime.timeval;
import ceri.serial.ftdi.jna.LibFtdi;
import ceri.serial.ftdi.jna.LibFtdi.ftdi_transfer_control;
import ceri.serial.jna.Struct;
import ceri.serial.libusb.jna.LibUsbException;

public class FtdiTransferControl {
	private final ftdi_transfer_control control;

	FtdiTransferControl(ftdi_transfer_control control) {
		this.control = control;
	}

	/**
	 * Waits for transfer to complete. Returns the number of bytes transferred.
	 */
	public int dataDone() throws LibUsbException {
		return LibFtdi.ftdi_transfer_data_done(control);
	}

	/**
	 * Cancels the transfer.
	 */
	public void dataCancel(Duration d) throws LibUsbException {
		LibFtdi.ftdi_transfer_data_cancel(control, Struct.write(timeval.from(d)));
	}
}
