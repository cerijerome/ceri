package ceri.serial.ftdi;

import com.sun.jna.Pointer;
import ceri.serial.jna.libusb.LibUsb.libusb_context;
import ceri.serial.jna.libusb.LibUsb.libusb_device_handle;

public class FtdiContext {

	/* USB specific */
	public libusb_context usbCtx = null;
	public libusb_device_handle usbDev = null;
	public int usbReadTimeout = 5000;
	public int usbWriteTimeout = 5000;
	/* FTDI specific */
	FtdiChipType type = FtdiChipType.TYPE_BM;
	int baudRate = -1;
	boolean bitbangEnabled = false;
	/** pointer to read buffer for ftdi_read_data */
	Pointer readBuffer = null;
	int readBufferOffset = 0;
	int readBufferRemaining = 0;
	int readBufferChunkSize;
	int writeBufferChunkSize = 4096;
	int maxPacketSize = 0;

	FtdiInterface iface;

	/** Bitbang mode. 1: (default) Normal bitbang mode, 2: FT2232C SPI bitbang mode */
	public FtdiMpsseMode bitbangMode = FtdiMpsseMode.BITMODE_BITBANG;
	/** Decoded eeprom structure */
	FtdiEeprom eeprom;
	/** String representation of last error */
	String errorStr = null;
	/** Defines behavior in case a kernel module is already attached to the device */
	FtdiModuleDetachMode moduleDetachMode = FtdiModuleDetachMode.AUTO_DETACH_SIO_MODULE;

}
