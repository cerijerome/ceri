package ceri.serial.ftdi;

import static ceri.serial.libusb.jna.LibUsb.libusb_error.LIBUSB_ERROR_NOT_FOUND;
import static ceri.serial.libusb.jna.LibUsb.libusb_error.LIBUSB_ERROR_NO_DEVICE;
import static ceri.serial.libusb.jna.LibUsb.libusb_error.LIBUSB_ERROR_NO_MEM;
import java.io.IOException;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import com.sun.jna.Memory;
import com.sun.jna.Pointer;
import ceri.common.concurrent.Lazy;
import ceri.jna.io.JnaInputStream;
import ceri.jna.io.JnaOutputStream;
import ceri.jna.util.JnaUtil;
import ceri.log.util.LogUtil;
import ceri.serial.ftdi.jna.LibFtdi;
import ceri.serial.ftdi.jna.LibFtdi.ftdi_context;
import ceri.serial.ftdi.jna.LibFtdi.ftdi_interface;
import ceri.serial.ftdi.jna.LibFtdi.ftdi_usb_strings;
import ceri.serial.ftdi.jna.LibFtdiStream;
import ceri.serial.ftdi.jna.LibFtdiStream.FTDIProgressInfo;
import ceri.serial.ftdi.jna.LibFtdiStream.FTDIStreamCallback;
import ceri.serial.ftdi.jna.LibFtdiUtil;
import ceri.serial.libusb.jna.LibUsb;
import ceri.serial.libusb.jna.LibUsb.libusb_device;
import ceri.serial.libusb.jna.LibUsb.libusb_error;
import ceri.serial.libusb.jna.LibUsbException;
import ceri.serial.libusb.jna.LibUsbFinder;

/**
 * Encapsulates ftdi_context and LibFtdi calls. Allow one usb device open at any time.
 */
public class FtdiDevice implements Ftdi {
	private static final Set<libusb_error> FATAL_USB_ERRORS =
		Set.of(LIBUSB_ERROR_NO_DEVICE, LIBUSB_ERROR_NOT_FOUND, LIBUSB_ERROR_NO_MEM);
	private final ftdi_context ftdi;
	private final JnaInputStream in;
	private final JnaOutputStream out;
	private final Lazy.Supplier<LibUsbException, ftdi_usb_strings> descriptor =
		Lazy.unsafe(this::deviceDescriptor);
	private final AtomicBoolean closed = new AtomicBoolean(false);

	public static boolean isFatal(Exception e) {
		if (!(e instanceof LibUsbException le)) return false;
		return FATAL_USB_ERRORS.contains(le.error);
	}

	public static FtdiDevice open() throws LibUsbException {
		return open(LibFtdiUtil.FINDER);
	}

	public static FtdiDevice open(LibUsbFinder finder) throws LibUsbException {
		return open(finder, ftdi_interface.INTERFACE_ANY);
	}

	public static FtdiDevice open(LibUsbFinder finder, ftdi_interface iface)
		throws LibUsbException {
		ftdi_context ftdi = LibFtdi.ftdi_new();
		try {
			if (iface != ftdi_interface.INTERFACE_ANY) LibFtdi.ftdi_set_interface(ftdi, iface);
			LibFtdi.ftdi_usb_open_find(ftdi, finder);
			return new FtdiDevice(ftdi);
		} catch (LibUsbException | RuntimeException e) {
			LogUtil.close(ftdi, LibFtdi::ftdi_free);
			throw e;
		}
	}

	FtdiDevice(ftdi_context ftdi) {
		this.ftdi = ftdi;
		in = createIn();
		out = createOut();
	}

	@Override
	public ftdi_usb_strings descriptor() throws LibUsbException {
		return descriptor.get();
	}

	@Override
	public void usbReset() throws LibUsbException {
		LibFtdi.ftdi_usb_reset(ftdi());
	}

	@Override
	public void bitMode(FtdiBitMode bitMode) throws LibUsbException {
		LibFtdi.ftdi_set_bitmode(ftdi(), bitMode.mask(), bitMode.mode());
	}

	@Override
	public void baud(int baud) throws LibUsbException {
		LibFtdi.ftdi_set_baudrate(ftdi(), baud);
	}

	@Override
	public void line(FtdiLineParams properties) throws LibUsbException {
		LibFtdi.ftdi_set_line_property(ftdi(), properties.dataBits(), properties.stopBits(),
			properties.parity(), properties.breakType());
	}

	@Override
	public void flowControl(FtdiFlowControl flowControl) throws LibUsbException {
		LibFtdi.ftdi_set_flow_ctrl(ftdi(), flowControl.value);
	}

	@Override
	public void dtr(boolean state) throws LibUsbException {
		LibFtdi.ftdi_set_dtr(ftdi(), state);
	}

	@Override
	public void rts(boolean state) throws LibUsbException {
		LibFtdi.ftdi_set_rts(ftdi(), state);
	}

	@Override
	public int readPins() throws LibUsbException {
		return LibFtdi.ftdi_read_pins(ftdi());
	}

	@Override
	public int pollModemStatus() throws LibUsbException {
		return LibFtdi.ftdi_poll_modem_status(ftdi());
	}

	@Override
	public void latencyTimer(int latency) throws LibUsbException {
		LibFtdi.ftdi_set_latency_timer(ftdi(), latency);
	}

	@Override
	public int latencyTimer() throws LibUsbException {
		return LibFtdi.ftdi_get_latency_timer(ftdi());
	}

	@Override
	public void readChunkSize(int size) throws LibUsbException {
		LibFtdi.ftdi_read_data_set_chunksize(ftdi(), size);
	}

	@Override
	public int readChunkSize() throws LibUsbException {
		return LibFtdi.ftdi_read_data_get_chunksize(ftdi());
	}

	@Override
	public void writeChunkSize(int size) throws LibUsbException {
		LibFtdi.ftdi_write_data_set_chunksize(ftdi(), size);
	}

	@Override
	public int writeChunkSize() throws LibUsbException {
		return LibFtdi.ftdi_write_data_get_chunksize(ftdi());
	}

	@Override
	public void purgeReadBuffer() throws LibUsbException {
		LibFtdi.ftdi_usb_purge_rx_buffer(ftdi());
	}

	@Override
	public void purgeWriteBuffer() throws LibUsbException {
		LibFtdi.ftdi_usb_purge_tx_buffer(ftdi());
	}

	@Override
	public JnaInputStream in() {
		return in;
	}

	@Override
	public JnaOutputStream out() {
		return out;
	}

	@Override
	public FtdiTransferControl readSubmit(Pointer buf, int size) throws LibUsbException {
		var control = LibFtdi.ftdi_read_data_submit(ftdi(), buf, size);
		return FtdiTransferControl.from(control);
	}

	@Override
	public FtdiTransferControl writeSubmit(Pointer buf, int size) throws LibUsbException {
		var control = LibFtdi.ftdi_write_data_submit(ftdi(), buf, size);
		return FtdiTransferControl.from(control);
	}

	@Override
	public void readStream(StreamCallback callback, int packetsPerTransfer, int numTransfers,
		double progressIntervalSec) throws LibUsbException {
		FTDIStreamCallback<?> ftdiCb = (buffer, length, progress,
			_) -> streamCallback(buffer, length, progress, callback);
		LibFtdiStream.ftdi_readstream(ftdi(), ftdiCb, null, packetsPerTransfer, numTransfers,
			progressIntervalSec);
	}

	@Override
	public void close() {
		if (closed.getAndSet(true)) return;
		LogUtil.close(ftdi, LibFtdi::ftdi_free);
		LogUtil.close(in, out);
	}

	ftdi_context ftdi() throws LibUsbException {
		if (!closed.get()) return ftdi;
		throw LibUsbException.of(LIBUSB_ERROR_NO_DEVICE, "Device is closed");
	}

	private ftdi_usb_strings deviceDescriptor() throws LibUsbException {
		var ftdi = ftdi();
		libusb_device dev = LibUsb.libusb_get_device(ftdi.usb_dev);
		return LibFtdi.ftdi_usb_get_strings(ftdi, dev);
	}

	private boolean streamCallback(Pointer buffer, int length, FTDIProgressInfo progress,
		StreamCallback callback) {
		return callback.invoke(FtdiProgressInfo.of(progress),
			buffer == null ? null : buffer.getByteBuffer(0, length));
	}

	private JnaInputStream createIn() {
		return new JnaInputStream() {
			@Override
			protected int read(Memory buffer, int len) throws IOException {
				return LibFtdi.ftdi_read_data(ftdi(), JnaUtil.buffer(buffer), len);
			}
		};
	}

	private JnaOutputStream createOut() {
		return new JnaOutputStream() {
			@Override
			protected int write(Memory buffer, int len) throws IOException {
				return LibFtdi.ftdi_write_data(ftdi(), JnaUtil.buffer(buffer), len);
			}
		};
	}

}
