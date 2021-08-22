package ceri.serial.ftdi;

import static ceri.serial.ftdi.jna.LibFtdi.ftdi_transfer_data_cancel;
import static ceri.serial.ftdi.jna.LibFtdi.ftdi_transfer_data_done;
import java.time.Duration;
import ceri.serial.clib.jna.Time;
import ceri.serial.ftdi.jna.LibFtdi.ftdi_transfer_control;
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
		return ftdi_transfer_data_done(control());
	}

	/**
	 * Cancels the transfer.
	 */
	public void dataCancel(Duration d) throws LibUsbException {
		ftdi_transfer_data_cancel(control(), Time.timeval(d));
	}

	private ftdi_transfer_control control() {
		if (control != null) return control;
		throw new IllegalStateException("Transfer control has been closed");
	}

}
