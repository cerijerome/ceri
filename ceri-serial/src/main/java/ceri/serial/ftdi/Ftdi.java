package ceri.serial.ftdi;

import static ceri.serial.ftdi.jna.LibFtdi.*;
import java.io.Closeable;
import java.io.IOException;
import ceri.serial.ftdi.jna.LibFtdi;
import ceri.serial.ftdi.jna.LibFtdi.*;
import ceri.serial.libusb.jna.LibUsbException;
import ceri.serial.libusb.jna.LibUsbFinder.libusb_device_criteria;

public class Ftdi implements Closeable {
	private LibFtdi.ftdi_context ftdi = null;
	
	public void init() throws LibUsbException {
		close();
		ensureCtx();
	}
	
	public Ftdi setInterface(ftdi_interface iface) throws LibUsbException {
		ensureCtx();
		ftdi_set_interface(ftdi, iface);
		return this;
	}
	
	public FtdiDevice open(libusb_device_criteria criteria) throws LibUsbException {
		ensureCtx();
		ftdi_usb_open_criteria(ftdi, criteria);
		return createDevice();
	}
	
	@Override
	public void close() {
		ftdi_free(ftdi);
		ftdi = null;
	}
	
	private FtdiDevice createDevice() {
		// Creates device and hands off ftdi_context to the device
		if (ftdi == null || ftdi.usb_ctx == null || ftdi.usb_dev == null) return null;
		FtdiDevice dev = new FtdiDevice(ftdi);
		ftdi = null;
		return dev;
	}
	
	private Ftdi ensureCtx() throws LibUsbException {
		if (ftdi == null) ftdi = ftdi_new();
		return this;
	}
	
}
