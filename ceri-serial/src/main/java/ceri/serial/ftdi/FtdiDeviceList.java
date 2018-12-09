package ceri.serial.ftdi;

import ceri.serial.libusb.jna.LibUsb.libusb_device;

/**
 * \brief list of usb devices created by ftdi_usb_find_all()
 */
public class FtdiDeviceList {
	FtdiDeviceList next;
	libusb_device dev;
}