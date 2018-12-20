package ceri.serial.ftdi;

import java.io.Closeable;
import java.io.IOException;
import java.util.List;
import static ceri.serial.ftdi.jna.LibFtdi.*;
import ceri.serial.ftdi.jna.LibFtdi;
import ceri.serial.ftdi.jna.LibFtdi.*;
import ceri.serial.libusb.jna.LibUsb;
import ceri.serial.libusb.jna.LibUsb.*;

public class FtdiList implements Closeable {
	private List<libusb_device> devices;
	
	@Override
	public void close() {
		ftdi_list_free(devices);
		devices.clear();
	}
	
}
