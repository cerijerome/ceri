package ceri.serial.ftdi;

import static ceri.common.collection.ArrayUtil.bytes;
import static ceri.common.math.MathUtil.ubyte;
import static ceri.serial.libusb.jna.LibUsb.libusb_error.LIBUSB_ERROR_NOT_FOUND;
import static ceri.serial.libusb.jna.LibUsb.libusb_error.LIBUSB_ERROR_NO_DEVICE;
import static ceri.serial.libusb.jna.LibUsb.libusb_error.LIBUSB_ERROR_NO_MEM;
import java.io.Closeable;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Set;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.sun.jna.Pointer;
import ceri.common.collection.ArrayUtil;
import ceri.log.util.LogUtil;
import ceri.serial.ftdi.jna.LibFtdi;
import ceri.serial.ftdi.jna.LibFtdi.ftdi_context;
import ceri.serial.ftdi.jna.LibFtdi.ftdi_interface;
import ceri.serial.ftdi.jna.LibFtdi.ftdi_string_descriptors;
import ceri.serial.ftdi.jna.LibFtdiStream;
import ceri.serial.ftdi.jna.LibFtdiStream.FTDIProgressInfo;
import ceri.serial.ftdi.jna.LibFtdiStream.FTDIStreamCallback;
import ceri.serial.ftdi.jna.LibFtdiUtil;
import ceri.serial.jna.JnaUtil;
import ceri.serial.libusb.jna.LibUsb;
import ceri.serial.libusb.jna.LibUsb.libusb_device;
import ceri.serial.libusb.jna.LibUsb.libusb_error;
import ceri.serial.libusb.jna.LibUsbException;
import ceri.serial.libusb.jna.LibUsbFinder;

/**
 * Encapsulates ftdi_context and LibFtdi calls. Allow one usb device open at any time.
 */
public class Ftdi implements Closeable {
	private static final Logger logger = LogManager.getLogger();
	private static final Set<libusb_error> FATAL_USB_ERRORS =
		Set.of(LIBUSB_ERROR_NO_DEVICE, LIBUSB_ERROR_NOT_FOUND, LIBUSB_ERROR_NO_MEM);
	private final ftdi_context ftdi;
	private ftdi_string_descriptors descriptors = null;
	private boolean closed = false;

	/**
	 * Callback to register for streaming events.
	 */
	public static interface StreamCallback {
		boolean invoke(FtdiProgressInfo progress, ByteBuffer buffer);
	}

	public static boolean isFatal(LibUsbException e) {
		if (e == null) return false;
		return FATAL_USB_ERRORS.contains(e.error);
	}

	public static Ftdi open() throws LibUsbException {
		return open(LibFtdiUtil.FINDER);
	}

	public static Ftdi open(LibUsbFinder finder) throws LibUsbException {
		return open(finder, ftdi_interface.INTERFACE_ANY);
	}

	public static Ftdi open(LibUsbFinder finder, ftdi_interface iface) throws LibUsbException {
		ftdi_context ftdi = LibFtdi.ftdi_new();
		try {
			LibFtdi.ftdi_set_interface(ftdi, iface);
			LibFtdi.ftdi_usb_open_find(ftdi, finder);
			return new Ftdi(ftdi);
		} catch (LibUsbException | RuntimeException e) {
			LogUtil.close(logger, ftdi, LibFtdi::ftdi_free);
			throw e;
		}
	}

	Ftdi(ftdi_context ftdi) {
		this.ftdi = ftdi;
	}

	/**
	 * Send reset request to device.
	 */
	public void usbReset() throws LibUsbException {
		LibFtdi.ftdi_usb_reset(ftdi());
	}

	public void bitMode(FtdiBitMode bitMode) throws LibUsbException {
		LibFtdi.ftdi_set_bitmode(ftdi(), bitMode.mask, bitMode.mode);
	}

	public void bitBang(boolean on) throws LibUsbException {
		if (on) LibFtdi.ftdi_enable_bitbang(ftdi());
		else LibFtdi.ftdi_disable_bitbang(ftdi());
	}

	public void baudRate(int baudRate) throws LibUsbException {
		LibFtdi.ftdi_set_baudrate(ftdi(), baudRate);
	}

	/**
	 * Sets data bits, stop bits, parity, and break.
	 */
	public void lineParams(FtdiLineParams properties) throws LibUsbException {
		LibFtdi.ftdi_set_line_property(ftdi(), properties.dataBits, properties.stopBits,
			properties.parity, properties.breakType);
	}

	public void flowControl(FtdiFlowControl flowControl) throws LibUsbException {
		LibFtdi.ftdi_set_flow_ctrl(ftdi(), flowControl.value);
	}

	public void dtr(boolean state) throws LibUsbException {
		LibFtdi.ftdi_set_dtr(ftdi(), state);
	}

	public void rts(boolean state) throws LibUsbException {
		LibFtdi.ftdi_set_rts(ftdi(), state);
	}

	/**
	 * Sets DTR and RTS at the same time.
	 */
	public void dtrRts(boolean dtr, boolean rts) throws LibUsbException {
		LibFtdi.ftdi_set_dtr_rts(ftdi(), dtr, rts);
	}

	public int write(int... data) throws LibUsbException {
		return write(bytes(data));
	}

	public int write(byte[] data) throws LibUsbException {
		return write(data, 0);
	}

	public int write(byte[] data, int offset) throws LibUsbException {
		return write(data, offset, data.length - offset);
	}

	public int write(byte[] data, int offset, int len) throws LibUsbException {
		return write(ByteBuffer.wrap(data, offset, len), len);
	}

	public int write(ByteBuffer buffer) throws LibUsbException {
		return write(buffer, buffer.remaining());
	}

	public int write(ByteBuffer buffer, int len) throws LibUsbException {
		return LibFtdi.ftdi_write_data(ftdi(), buffer, len);
	}

	public FtdiTransferControl writeSubmit(int... bytes) throws LibUsbException {
		return writeSubmit(bytes(bytes));
	}

	public FtdiTransferControl writeSubmit(byte[] data) throws LibUsbException {
		return writeSubmit(data, 0);
	}

	public FtdiTransferControl writeSubmit(byte[] data, int offset) throws LibUsbException {
		return writeSubmit(data, offset, data.length - offset);
	}

	public FtdiTransferControl writeSubmit(byte[] data, int offset, int len)
		throws LibUsbException {
		ArrayUtil.validateSlice(data.length, offset, len);
		Pointer p = JnaUtil.mallocBytes(data, offset, len);
		return writeSubmit(p, len);
	}

	public FtdiTransferControl writeSubmit(Pointer buf, int size) throws LibUsbException {
		return new FtdiTransferControl(LibFtdi.ftdi_write_data_submit(ftdi(), buf, size));
	}

	/**
	 * Reads 1 byte. Returns -1 if no byte available.
	 */
	public int read() throws LibUsbException {
		byte[] data = read(1);
		if (data.length == 0) return -1;
		return ubyte(data[0]);
	}

	/**
	 * Reads bytes and returns array.
	 */
	public byte[] read(int size) throws LibUsbException {
		byte[] buffer = new byte[size];
		int n = read(ByteBuffer.wrap(buffer), size);
		return n >= size ? buffer : Arrays.copyOf(buffer, n);
	}

	/**
	 * Reads bytes into array.
	 */
	public int read(byte[] buffer, int offset, int length) throws LibUsbException {
		return read(ByteBuffer.wrap(buffer, offset, length), length);
	}

	/**
	 * Reads bytes into buffer.
	 */
	public int read(ByteBuffer buffer) throws LibUsbException {
		return read(buffer, buffer.remaining());
	}

	/**
	 * Reads bytes into buffer.
	 */
	public int read(ByteBuffer buffer, int size) throws LibUsbException {
		return LibFtdi.ftdi_read_data(ftdi(), buffer, size);
	}

	/**
	 * Reads 8-bit pin status.
	 */
	public int readPins() throws LibUsbException {
		return LibFtdi.ftdi_read_pins(ftdi());
	}

	/**
	 * Submits
	 */
	public FtdiTransferControl readSubmit(Pointer buf, int size) throws LibUsbException {
		return new FtdiTransferControl(LibFtdi.ftdi_read_data_submit(ftdi(), buf, size));
	}

	public void readStream(StreamCallback callback, int packetsPerTransfer, int numTransfers)
		throws LibUsbException {
		FTDIStreamCallback<?> ftdiCb = (buffer, length, progress,
			user_data) -> streamCallback(buffer, length, progress, callback);
		LibFtdiStream.ftdi_readstream(ftdi(), ftdiCb, null, packetsPerTransfer, numTransfers);
	}

	public void readChunkSize(int chunkSize) throws LibUsbException {
		LibFtdi.ftdi_read_data_set_chunk_size(ftdi(), chunkSize);
	}

	public void purgeRxBuffer() throws LibUsbException {
		LibFtdi.ftdi_usb_purge_rx_buffer(ftdi());
	}

	public void purgeTxBuffer() throws LibUsbException {
		LibFtdi.ftdi_usb_purge_tx_buffer(ftdi());
	}

	public void purgeBuffers() throws LibUsbException {
		purgeRxBuffer();
		purgeTxBuffer();
	}

	public void latencyTimer(int latency) throws LibUsbException {
		LibFtdi.ftdi_set_latency_timer(ftdi(), latency);
	}

	public int latencyTimer() throws LibUsbException {
		return LibFtdi.ftdi_get_latency_timer(ftdi());
	}

	public int pollModemStatus() throws LibUsbException {
		return LibFtdi.ftdi_poll_modem_status(ftdi());
	}

	/**
	 * Return the descriptor text.
	 */
	public String manufacturer() throws LibUsbException {
		return ensureDescriptors().manufacturer;
	}

	/**
	 * Return the descriptor text.
	 */
	public String description() throws LibUsbException {
		return ensureDescriptors().description;
	}

	/**
	 * Return the descriptor text.
	 */
	public String serial() throws LibUsbException {
		return ensureDescriptors().serial;
	}

	@Override
	public void close() {
		if (closed) return;
		closed = true;
		LogUtil.close(logger, ftdi, LibFtdi::ftdi_free);
	}

	ftdi_context ftdi() throws LibUsbException {
		if (!closed) return ftdi;
		throw LibUsbException.of(LIBUSB_ERROR_NO_DEVICE, "Device is closed");
	}

	private boolean streamCallback(Pointer buffer, int length, FTDIProgressInfo progress,
		StreamCallback callback) {
		return callback.invoke(FtdiProgressInfo.of(progress),
			buffer == null ? null : buffer.getByteBuffer(0, length));
	}

	private ftdi_string_descriptors ensureDescriptors() throws LibUsbException {
		if (descriptors != null) return descriptors;
		libusb_device dev = LibUsb.libusb_get_device(ftdi().usb_dev);
		descriptors = LibFtdi.ftdi_usb_get_strings(ftdi(), dev);
		return descriptors;
	}

}
