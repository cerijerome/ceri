package ceri.serial.ftdi;

import com.sun.jna.Pointer;
import ceri.serial.jna.libusb.LibUsb.libusb_transfer;

public class FtdiTransferControl {
	int completed;
	Pointer buf;
	int size;
	int offset;
	FtdiContext ftdi;
	libusb_transfer transfer;
}
