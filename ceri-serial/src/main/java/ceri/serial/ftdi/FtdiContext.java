package ceri.serial.ftdi;

import static ceri.serial.ftdi.jna.LibFtdi.*;
import java.io.Closeable;
import ceri.serial.ftdi.jna.LibFtdi.*;
import ceri.serial.ftdi.jna.LibFtdi.ftdi_context;
import ceri.serial.libusb.jna.LibUsbException;

public class FtdiContext implements Closeable {
	private final ftdi_context ftdi;
	
	public static FtdiContext create() throws LibUsbException {
		return new FtdiContext(ftdi_new());
	}
	
	private FtdiContext(ftdi_context ftdi) {
		this.ftdi = ftdi;
	}

	public void enableBitbang() throws LibUsbException {
		ftdi_enable_bitbang(ftdi);
	}
	
	public void disableBitbang() throws LibUsbException {
		ftdi_disable_bitbang(ftdi);
	}
	
	@Override
	public void close() {
		ftdi_free(ftdi);
	}
	
}
