package ceri.serial.ftdi;

import static ceri.common.validation.ValidationUtil.validateNotNull;
import static ceri.common.validation.ValidationUtil.validateRange;
import static ceri.serial.ftdi.Ftdi.FTDI_DEVICE_IN_REQTYPE;
import static ceri.serial.ftdi.Ftdi.FTDI_DEVICE_OUT_REQTYPE;
import static ceri.serial.ftdi.Ftdi.SIO_SET_DTR_HIGH;
import static ceri.serial.ftdi.Ftdi.SIO_SET_DTR_LOW;
import static ceri.serial.ftdi.Ftdi.SIO_SET_RTS_HIGH;
import static ceri.serial.ftdi.Ftdi.SIO_SET_RTS_LOW;
import static ceri.serial.ftdi.RequestType.SIO_GET_LATENCY_TIMER_REQUEST;
import static ceri.serial.ftdi.RequestType.SIO_POLL_MODEM_STATUS_REQUEST;
import static ceri.serial.ftdi.RequestType.SIO_READ_PINS_REQUEST;
import static ceri.serial.ftdi.RequestType.SIO_RESET_REQUEST;
import static ceri.serial.ftdi.RequestType.SIO_SET_BITMODE_REQUEST;
import static ceri.serial.ftdi.RequestType.SIO_SET_ERROR_CHAR_REQUEST;
import static ceri.serial.ftdi.RequestType.SIO_SET_EVENT_CHAR_REQUEST;
import static ceri.serial.ftdi.RequestType.SIO_SET_FLOW_CTRL_REQUEST;
import static ceri.serial.ftdi.RequestType.SIO_SET_LATENCY_TIMER_REQUEST;
import static ceri.serial.ftdi.RequestType.SIO_SET_MODEM_CTRL_REQUEST;
import static ceri.serial.jna.JnaUtil.ubyte;
import java.nio.ByteBuffer;
import java.util.Arrays;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ceri.log.util.LogUtil;
import ceri.serial.jna.JnaUtil;
import ceri.serial.libusb.LibUsbContext;
import ceri.serial.libusb.LibUsbDeviceHandle;
import ceri.serial.libusb.jna.LibUsbException;

public class FtdiContext {
	private static final Logger logger = LogManager.getLogger();
	private static final int READ_STATUS_BYTES = 2;


	/* USB specific */
	public LibUsbContext usbCtx = null;
	public LibUsbDeviceHandle usbDev = null;
	public int usbReadTimeout = 5000;
	public int usbWriteTimeout = 5000;
	/* FTDI specific */
	FtdiChipType type = FtdiChipType.TYPE_BM;
	int baudRate = -1;
	boolean bitbangEnabled = false;
	/** pointer to read buffer for ftdi_read_data */
	ByteBuffer readBuffer = null;
	int readBufferOffset = 0;
	int readBufferRemaining = 0;
	int readBufferChunkSize;
	int writeBufferChunkSize = 4096;
	int maxPacketSize = 0;

	FtdiInterface iface; // contains interface, index, in_ep, out_ep fields

	/** Bitbang mode. 1: (default) Normal bitbang mode, 2: FT2232C SPI bitbang mode */
	public FtdiMpsseMode bitbangMode = FtdiMpsseMode.BITMODE_BITBANG;
	/** Decoded eeprom structure */
	FtdiEeprom eeprom;
	/** String representation of last error */
	String errorStr = null;
	/** Defines behavior in case a kernel module is already attached to the device */
	FtdiModuleDetachMode moduleDetachMode = FtdiModuleDetachMode.AUTO_DETACH_SIO_MODULE;

	public static FtdiContext init() throws LibUsbException {
		FtdiContext ftdi = new FtdiContext();
		ftdi.usbCtx = LibUsbContext.init();
		return ftdi;
	}

	public void setInterface(FtdiInterface iface) throws FtdiException {
		validateNotNull(iface);
		if (usbDev == null) this.iface = iface;
		else if (this.iface != iface)
			throw new FtdiException(-3, "Interface can not be changed on an already open device");
	}

	public void setUsbDev(LibUsbDeviceHandle handle) {
		this.usbDev = handle;
	}

	public void reset() throws LibUsbException {
		controlTransferOut(SIO_RESET_REQUEST, Ftdi.SIO_RESET_SIO, iface.index);
		clearReadBuffer();
	}

	public void purgeBuffers() throws LibUsbException {
		purgeRxBuffer();
		purgeTxBuffer();
	}

	public void purgeRxBuffer() throws LibUsbException {
		controlTransferOut(SIO_RESET_REQUEST, Ftdi.SIO_RESET_PURGE_RX, iface.index);
		clearReadBuffer();
	}

	public void purgeTxBuffer() throws LibUsbException {
		controlTransferOut(SIO_RESET_REQUEST, Ftdi.SIO_RESET_PURGE_TX, iface.index);
	}

	public void setLineProperty(FtdiBitsType bits, FtdiStopBitsType sbit, FtdiParityType parity)
		throws LibUsbException {
		setLineProperty(bits, sbit, parity, FtdiBreakType.BREAK_OFF);
	}

	public void setLineProperty(FtdiBitsType bits, FtdiStopBitsType stopBits, FtdiParityType parity,
		FtdiBreakType breakType) throws LibUsbException {
		int value = breakType.value << 14 | stopBits.value << 11 | parity.value << 8 | bits.value;
		if (usbDev != null) usbDev.controlTransfer(FTDI_DEVICE_OUT_REQTYPE,
			RequestType.SIO_SET_DATA_REQUEST.value, value, iface.index, null, 0, usbWriteTimeout);
	}

	public int writeData(byte...data) throws LibUsbException {
		return writeData(data, 0, data.length);
	}
	
	public int writeData(byte[] data, int offset) throws LibUsbException {
		return writeData(data, offset, data.length - offset);
	}
	
	public int writeData(byte[] data, int offset, int len) throws LibUsbException {
		return writeData(ByteBuffer.wrap(data, offset, len));
	}
	
	public int writeData(ByteBuffer buffer) throws LibUsbException {
		int offset = 0;
		while (buffer.hasRemaining()) {
			int len = Math.min(writeBufferChunkSize, buffer.remaining());
			int n = usbDev.bulkTransfer(iface.inEp, buffer, len, usbWriteTimeout);
			offset += n;
		}
		return offset;
	}

	public int readData() throws LibUsbException {
		byte[] data = readData(1);
		if (data.length == 0) return -1; 
		return ubyte(data[0]);
	}
	
	public byte[] readData(int size) throws LibUsbException {
		byte[] buffer = new byte[size];
		int n = readData(ByteBuffer.wrap(buffer), size);
		return n == size ? buffer : Arrays.copyOf(buffer, n);
	}
	
	public int readData(ByteBuffer buffer) throws LibUsbException {
		return readData(buffer, buffer.remaining());
	}
	
	public int readData(ByteBuffer buffer, int size) throws LibUsbException {
		int remaining = size;
		while (remaining > 0) {
			int len = readLen(remaining);
			readBuffer.clear();
			int n = usbDev.bulkTransfer(iface.outEp, readBuffer, len, usbReadTimeout);
			if (n <= READ_STATUS_BYTES) break;
			int packets = (n + maxPacketSize - 1) / maxPacketSize;
			for (int i = 0; i < packets; i++) {
				int position = (i * maxPacketSize) + READ_STATUS_BYTES;
				int limit = Math.min(n, position + maxPacketSize - READ_STATUS_BYTES);
				if (limit <= position) break;
				buffer.put(readBuffer.limit(limit).position(position));
				remaining -= (limit - position);
			}
		}
		return size - remaining;
	}

	private int readLen(int size) {
		int packets = size / (maxPacketSize - READ_STATUS_BYTES);
		int rem = size % (maxPacketSize - READ_STATUS_BYTES);
		int total = (packets * maxPacketSize) + (rem > 0 ? READ_STATUS_BYTES + rem : 0);
		return Math.min(total, readBufferChunkSize);
	}

	public void readDataSetChunkSize(int chunkSize) {
		clearReadBuffer();
		readBuffer = ByteBuffer.allocate(chunkSize);
		readBufferChunkSize = chunkSize;
	}

	public void setBitMode(int bitmask, FtdiMpsseMode mode) throws LibUsbException {
		int value = mode.value << 8 | bitmask;
		controlTransferOut(SIO_SET_BITMODE_REQUEST, value, iface.index);
		bitbangMode = mode;
		bitbangEnabled = (mode == FtdiMpsseMode.BITMODE_RESET);
	}

	public void disableBitbang() throws LibUsbException {
		controlTransferOut(SIO_SET_BITMODE_REQUEST, 0, iface.index);
		bitbangEnabled = true;
	}

	public int readPins() throws LibUsbException {
		byte[] data = controlTransferIn(SIO_READ_PINS_REQUEST, 0, iface.index, 1);
		return ubyte(data[0]);
	}

	public void setLatencyTimer(int latency) throws LibUsbException {
		validateRange(latency, 1, 255, "latency");
		controlTransferOut(SIO_SET_LATENCY_TIMER_REQUEST, latency, iface.index);
	}

	public int getLatencyTimer() throws LibUsbException {
		byte[] data = controlTransferIn(SIO_GET_LATENCY_TIMER_REQUEST, 0, iface.index, 1);
		return JnaUtil.ubyte(data[0]);
	}

	public int pollModemStatus() throws LibUsbException {
		byte[] data = controlTransferIn(SIO_POLL_MODEM_STATUS_REQUEST, 0, iface.index, 2);
		return data[1] << 8 | data[0];
	}

	public void setFloCtrl(FlowControl flowCtrl) throws LibUsbException {
		controlTransferOut(SIO_SET_FLOW_CTRL_REQUEST, 0, flowCtrl.value | iface.index);
	}

	public void setDtr(boolean state) throws LibUsbException {
		int val = (state ? SIO_SET_DTR_HIGH : SIO_SET_DTR_LOW);
		controlTransferOut(SIO_SET_MODEM_CTRL_REQUEST, val, iface.index);
	}

	public void setRts(boolean state) throws LibUsbException {
		int val = (state ? SIO_SET_RTS_HIGH : SIO_SET_RTS_LOW);
		controlTransferOut(SIO_SET_MODEM_CTRL_REQUEST, val, iface.index);
	}

	public void setDtrRts(boolean dtr, boolean rts) throws LibUsbException {
		int val = (dtr ? SIO_SET_DTR_HIGH : SIO_SET_DTR_LOW) | //
			(rts ? SIO_SET_RTS_HIGH : SIO_SET_RTS_LOW);
		controlTransferOut(SIO_SET_MODEM_CTRL_REQUEST, val, iface.index);
	}

	public void setEventChar(char eventch, boolean enable) throws LibUsbException {
		int val = eventch & 0xff;
		if (enable) val |= 1 << 8;
		controlTransferOut(SIO_SET_EVENT_CHAR_REQUEST, val, iface.index);
	}

	public void setErrorChar(char errorch, boolean enable) throws LibUsbException {
		int val = errorch & 0xff;
		if (enable) val |= 1 << 8;
		controlTransferOut(SIO_SET_ERROR_CHAR_REQUEST, val, iface.index);
	}

	public void close() {
		LogUtil.close(logger, usbDev, usbDev -> usbDev.releaseInterface(iface.iface));
		LogUtil.close(logger, usbDev);
		// TODO: eeprom.close()?
		if (eeprom != null) eeprom.initialized_for_connected_device = 0;
		LogUtil.close(logger, usbCtx);
	}

	private boolean controlTransferOut(RequestType request, int value, int index)
		throws LibUsbException {
		if (usbDev == null) return false;
		usbDev.controlTransfer(FTDI_DEVICE_OUT_REQTYPE, request.value, value, index,
			usbWriteTimeout);
		return true;
	}

	public byte[] controlTransferIn(RequestType request, int value, int index, int length)
		throws LibUsbException {
		if (usbDev == null) return null;
		return usbDev.controlTransfer(FTDI_DEVICE_IN_REQTYPE, request.value, value, index, length,
			usbReadTimeout);
	}

	private void clearReadBuffer() {
		// TODO: not needed?
		readBufferOffset = 0;
		readBufferRemaining = 0;
	}

}
