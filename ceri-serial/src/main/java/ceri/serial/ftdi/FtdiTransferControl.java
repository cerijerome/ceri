package ceri.serial.ftdi;

import static ceri.serial.ftdi.jna.LibFtdi.ftdi_transfer_data_cancel;
import static ceri.serial.ftdi.jna.LibFtdi.ftdi_transfer_data_done;
import java.time.Duration;
import ceri.serial.ftdi.jna.LibFtdi.ftdi_transfer_control;
import ceri.serial.jna.clib.Time;
import ceri.serial.libusb.jna.LibUsbException;

public class FtdiTransferControl {
	private final ftdi_transfer_control control;

	FtdiTransferControl(ftdi_transfer_control control) {
		this.control = control;
	}

	public int dataDone() throws LibUsbException {
		return ftdi_transfer_data_done(control());
	}

	public void dataCancel(Duration d) throws LibUsbException {
		ftdi_transfer_data_cancel(control(), Time.Util.timeval(d));
	}

	private ftdi_transfer_control control() {
		if (control != null) return control;
		throw new IllegalStateException("Transfer control has been closed");
	}

}
