package ceri.serial.ftdi;

import static ceri.common.collection.StreamUtil.toList;
import static ceri.serial.ftdi.jna.LibFtdi.ftdi_disable_bitbang;
import static ceri.serial.ftdi.jna.LibFtdi.ftdi_enable_bitbang;
import static ceri.serial.ftdi.jna.LibFtdi.ftdi_free;
import static ceri.serial.ftdi.jna.LibFtdi.ftdi_get_latency_timer;
import static ceri.serial.ftdi.jna.LibFtdi.ftdi_list_free;
import static ceri.serial.ftdi.jna.LibFtdi.ftdi_new;
import static ceri.serial.ftdi.jna.LibFtdi.ftdi_poll_modem_status;
import static ceri.serial.ftdi.jna.LibFtdi.ftdi_read_data;
import static ceri.serial.ftdi.jna.LibFtdi.ftdi_read_data_set_chunk_size;
import static ceri.serial.ftdi.jna.LibFtdi.ftdi_read_data_submit;
import static ceri.serial.ftdi.jna.LibFtdi.ftdi_read_pins;
import static ceri.serial.ftdi.jna.LibFtdi.ftdi_set_baudrate;
import static ceri.serial.ftdi.jna.LibFtdi.ftdi_set_bitmode;
import static ceri.serial.ftdi.jna.LibFtdi.ftdi_set_dtr;
import static ceri.serial.ftdi.jna.LibFtdi.ftdi_set_dtr_rts;
import static ceri.serial.ftdi.jna.LibFtdi.ftdi_set_error_char;
import static ceri.serial.ftdi.jna.LibFtdi.ftdi_set_event_char;
import static ceri.serial.ftdi.jna.LibFtdi.ftdi_set_flow_ctrl;
import static ceri.serial.ftdi.jna.LibFtdi.ftdi_set_interface;
import static ceri.serial.ftdi.jna.LibFtdi.ftdi_set_latency_timer;
import static ceri.serial.ftdi.jna.LibFtdi.ftdi_set_line_property;
import static ceri.serial.ftdi.jna.LibFtdi.ftdi_set_rts;
import static ceri.serial.ftdi.jna.LibFtdi.ftdi_usb_find_all;
import static ceri.serial.ftdi.jna.LibFtdi.ftdi_usb_get_strings;
import static ceri.serial.ftdi.jna.LibFtdi.ftdi_usb_open;
import static ceri.serial.ftdi.jna.LibFtdi.ftdi_usb_open_criteria;
import static ceri.serial.ftdi.jna.LibFtdi.ftdi_usb_open_dev;
import static ceri.serial.ftdi.jna.LibFtdi.ftdi_usb_open_string;
import static ceri.serial.ftdi.jna.LibFtdi.ftdi_usb_purge_buffers;
import static ceri.serial.ftdi.jna.LibFtdi.ftdi_usb_purge_rx_buffer;
import static ceri.serial.ftdi.jna.LibFtdi.ftdi_usb_purge_tx_buffer;
import static ceri.serial.ftdi.jna.LibFtdi.ftdi_usb_reset;
import static ceri.serial.ftdi.jna.LibFtdi.ftdi_write_data;
import static ceri.serial.ftdi.jna.LibFtdi.ftdi_write_data_submit;
import static ceri.serial.ftdi.jna.LibFtdiStream.ftdi_read_stream;
import java.io.Closeable;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.sun.jna.Pointer;
import ceri.serial.ftdi.jna.LibFtdi.ftdi_break_type;
import ceri.serial.ftdi.jna.LibFtdi.ftdi_context;
import ceri.serial.ftdi.jna.LibFtdi.ftdi_data_bits_type;
import ceri.serial.ftdi.jna.LibFtdi.ftdi_flow_control;
import ceri.serial.ftdi.jna.LibFtdi.ftdi_interface;
import ceri.serial.ftdi.jna.LibFtdi.ftdi_mpsse_mode;
import ceri.serial.ftdi.jna.LibFtdi.ftdi_parity_type;
import ceri.serial.ftdi.jna.LibFtdi.ftdi_stop_bits_type;
import ceri.serial.ftdi.jna.LibFtdi.ftdi_string_descriptors;
import ceri.serial.ftdi.jna.LibFtdiStream.ftdi_progress_info;
import ceri.serial.ftdi.jna.LibFtdiStream.ftdi_stream_cb;
import ceri.serial.libusb.LibUsbContext;
import ceri.serial.libusb.LibUsbDevice;
import ceri.serial.libusb.jna.LibUsb.libusb_context;
import ceri.serial.libusb.jna.LibUsb.libusb_device;
import ceri.serial.libusb.jna.LibUsbException;
import ceri.serial.libusb.jna.LibUsbFinder.libusb_device_criteria;

public class Ftdi implements Closeable {
	private static final Logger logger = LogManager.getLogger();
	private ftdi_context ftdi;
	// Temporarily stores callbacks to make sure they are not removed by GC
	// Assigns a generated id, and tracks per callback
	private final Map<Integer, ftdi_stream_cb> streamCallbacks = new ConcurrentHashMap<>();
	private AtomicInteger streamCallbackId = new AtomicInteger();

	/**
	 * Return true if finished reading from stream.
	 */
	public static interface StreamCallback<T> {
		public boolean event(ByteBuffer buffer, int length, ftdi_progress_info progress, T userData)
			throws IOException;
	}

	public static Ftdi create() throws LibUsbException {
		return new Ftdi(ftdi_new());
	}

	static Ftdi from(Supplier<ftdi_context> supplier) {
		return new Ftdi(supplier.get());
	}

	Ftdi(ftdi_context ftdi) {
		this.ftdi = ftdi;
	}

	public void setInterface(ftdi_interface iface) throws LibUsbException {
		ftdi_set_interface(ftdi(), iface);
	}

	public FtdiList findAll(int vendor, int product) throws LibUsbException {
		return wrap(ftdi_usb_find_all(ftdi(), vendor, product));
	}

	public FtdiList findAll(libusb_device_criteria criteria) throws LibUsbException {
		return wrap(ftdi_usb_find_all(ftdi(), criteria));
	}

	public ftdi_string_descriptors usbStrings(LibUsbDevice dev) throws LibUsbException {
		return ftdi_usb_get_strings(ftdi(), dev.device());
	}

	public void open(LibUsbDevice dev) throws LibUsbException {
		if (ftdi().usb_ctx != dev.context())
			throw new IllegalArgumentException("Device context does not match");
		ftdi_usb_open_dev(ftdi(), dev.device());
	}

	public void open(libusb_device_criteria criteria) throws LibUsbException {
		ftdi_usb_open_criteria(ftdi(), criteria);
	}

	public void open(int vendor, int product) throws LibUsbException {
		ftdi_usb_open(ftdi(), vendor, product);
	}

	public void open(String description) throws LibUsbException {
		ftdi_usb_open_string(ftdi(), description);
	}

	public void reset() throws LibUsbException {
		ftdi_usb_reset(ftdi());
	}

	public void purgeRxBuffer() throws LibUsbException {
		ftdi_usb_purge_rx_buffer(ftdi());
	}

	public void purgeTxBuffer() throws LibUsbException {
		ftdi_usb_purge_tx_buffer(ftdi());
	}

	public void purgeBuffers() throws LibUsbException {
		ftdi_usb_purge_buffers(ftdi());
	}

	public void baudrate(int baudrate) throws LibUsbException {
		ftdi_set_baudrate(ftdi(), baudrate);
	}

	public void lineProperty(ftdi_data_bits_type bits, ftdi_stop_bits_type sbit,
		ftdi_parity_type parity) throws LibUsbException {
		ftdi_set_line_property(ftdi(), bits, sbit, parity);
	}

	public void lineProperty(ftdi_data_bits_type bits, ftdi_stop_bits_type stopBits,
		ftdi_parity_type parity, ftdi_break_type breakType) throws LibUsbException {
		ftdi_set_line_property(ftdi(), bits, stopBits, parity, breakType);
	}

	public int write(int... data) throws LibUsbException {
		return ftdi_write_data(ftdi(), data);
	}

	public int write(byte[] data, int offset) throws LibUsbException {
		return ftdi_write_data(ftdi(), data, offset);
	}

	public int write(byte[] data, int offset, int len) throws LibUsbException {
		return ftdi_write_data(ftdi(), data, offset, len);
	}

	public int write(ByteBuffer buffer) throws LibUsbException {
		return ftdi_write_data(ftdi(), buffer);
	}

	public int write(ByteBuffer buffer, int len) throws LibUsbException {
		return ftdi_write_data(ftdi(), buffer, len);
	}

	public int read() throws LibUsbException {
		return ftdi_read_data(ftdi());
	}

	public byte[] read(int size) throws LibUsbException {
		return ftdi_read_data(ftdi(), size);
	}

	public int read(ByteBuffer buffer) throws LibUsbException {
		return ftdi_read_data(ftdi(), buffer);
	}

	public int read(ByteBuffer buffer, int size) throws LibUsbException {
		return ftdi_read_data(ftdi(), buffer, size);
	}

	public FtdiTransferControl writeSubmit(Pointer buf, int size) throws LibUsbException {
		return new FtdiTransferControl(ftdi_write_data_submit(ftdi(), buf, size));
	}

	public FtdiTransferControl readSubmit(Pointer buf, int size) throws LibUsbException {
		return new FtdiTransferControl(ftdi_read_data_submit(ftdi(), buf, size));
	}

	public <T> void readStream(StreamCallback<T> callback, T userData, int packetsPerTransfer,
		int numTransfers) throws LibUsbException {
		int callbackId = streamCallbackId.incrementAndGet();
		ftdi_stream_cb jnaCallback = (buffer, length, progress, user_data) -> streamCallback(buffer,
			length, progress, callbackId, callback, userData);
		ftdi_read_stream(ftdi(), jnaCallback, null, packetsPerTransfer, numTransfers);
		streamCallbacks.put(callbackId, jnaCallback);
	}

	private <T> int streamCallback(Pointer buffer, int length, ftdi_progress_info progress,
		int callbackId, StreamCallback<T> callback, T userData) {
		try {
			ByteBuffer b = buffer.getByteBuffer(0, length);
			boolean result = callback.event(b, length, progress, userData);
			if (result) streamCallbacks.remove(callbackId);
			return result ? 1 : 0;
		} catch (IOException | RuntimeException e) {
			logger.catching(e);
			return 0;
		}
	}

	public void readChunkSize(int chunkSize) {
		ftdi_read_data_set_chunk_size(ftdi(), chunkSize);
	}

	public void bitmode(int bitmask, ftdi_mpsse_mode mode) throws LibUsbException {
		ftdi_set_bitmode(ftdi(), bitmask, mode);
	}

	public void bitbang(boolean on) throws LibUsbException {
		if (on) ftdi_enable_bitbang(ftdi());
		else ftdi_disable_bitbang(ftdi());
	}

	public int readPins() throws LibUsbException {
		return ftdi_read_pins(ftdi());
	}

	public void latencyTimer(int latency) throws LibUsbException {
		ftdi_set_latency_timer(ftdi(), latency);
	}

	public int latencyTimer() throws LibUsbException {
		return ftdi_get_latency_timer(ftdi());
	}

	public int pollModemStatus() throws LibUsbException {
		return ftdi_poll_modem_status(ftdi());
	}

	public void flowCtrl(ftdi_flow_control flowCtrl) throws LibUsbException {
		ftdi_set_flow_ctrl(ftdi(), flowCtrl);
	}

	public void dtr(boolean state) throws LibUsbException {
		ftdi_set_dtr(ftdi(), state);
	}

	public void rts(boolean state) throws LibUsbException {
		ftdi_set_rts(ftdi(), state);
	}

	public void dtrRts(boolean dtr, boolean rts) throws LibUsbException {
		ftdi_set_dtr_rts(ftdi(), dtr, rts);
	}

	public void eventChar(char eventch, boolean enable) throws LibUsbException {
		ftdi_set_event_char(ftdi(), eventch, enable);
	}

	public void errorChar(char errorch, boolean enable) throws LibUsbException {
		ftdi_set_error_char(ftdi(), errorch, enable);
	}

	@Override
	public void close() {
		streamCallbacks.clear();
		ftdi_free(ftdi);
		ftdi = null;
	}

	private ftdi_context ftdi() {
		if (ftdi != null) return ftdi;
		throw new IllegalStateException("Ftdi context has been closed");
	}

	private LibUsbContext context() {
		libusb_context context = ftdi().usb_ctx;
		if (context != null) return LibUsbContext.from(context);
		throw new IllegalStateException("Context is not available");
	}

	@SuppressWarnings("resource")
	private FtdiList wrap(List<libusb_device> devs) {
		try {
			LibUsbContext context = context();
			List<LibUsbDevice> devices = toList(devs.stream().map(dev -> context.wrap(dev, 1)));
			return new FtdiList(devices);
		} catch (RuntimeException e) {
			ftdi_list_free(devs);
			throw e;
		}
	}

}
