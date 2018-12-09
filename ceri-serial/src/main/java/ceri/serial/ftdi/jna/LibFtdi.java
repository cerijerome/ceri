package ceri.serial.ftdi.jna;

import static ceri.common.validation.ValidationUtil.validateRange;
import static ceri.serial.libusb.jna.LibUsb.libusb_endpoint_direction.LIBUSB_ENDPOINT_IN;
import static ceri.serial.libusb.jna.LibUsb.libusb_endpoint_direction.LIBUSB_ENDPOINT_OUT;
import static ceri.serial.libusb.jna.LibUsb.libusb_request_recipient.LIBUSB_RECIPIENT_DEVICE;
import static ceri.serial.libusb.jna.LibUsb.libusb_request_type.LIBUSB_REQUEST_TYPE_VENDOR;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.sun.jna.Memory;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import ceri.common.data.BooleanAccessor;
import ceri.common.data.FieldTranscoder;
import ceri.common.data.IntAccessor;
import ceri.common.data.TypeTranscoder;
import ceri.log.util.LogUtil;
import ceri.serial.ftdi.FtdiBitMode;
import ceri.serial.ftdi.FtdiBreakType;
import ceri.serial.ftdi.FtdiChipType;
import ceri.serial.ftdi.FtdiDataBits;
import ceri.serial.ftdi.FtdiFlowControl;
import ceri.serial.ftdi.FtdiModemStatus;
import ceri.serial.ftdi.FtdiParity;
import ceri.serial.ftdi.FtdiStopBitsType;
import ceri.serial.ftdi.RequestType;
import ceri.serial.jna.JnaUtil;
import ceri.serial.jna.Struct;
import ceri.serial.libusb.jna.LibUsb;
import ceri.serial.libusb.jna.LibUsb.libusb_context;
import ceri.serial.libusb.jna.LibUsb.libusb_device_handle;
import ceri.serial.libusb.jna.LibUsb.libusb_error;
import ceri.serial.libusb.jna.LibUsb.libusb_transfer;
import ceri.serial.libusb.jna.LibUsb.timeval;
import ceri.serial.libusb.jna.LibUsbException;

public class LibFtdi {
	private static final Logger logger = LogManager.getLogger();
	private static final byte FTDI_DEVICE_OUT_REQTYPE = LibUsb.libusb_request_type(
		LIBUSB_RECIPIENT_DEVICE, LIBUSB_REQUEST_TYPE_VENDOR, LIBUSB_ENDPOINT_OUT);
	private static final byte FTDI_DEVICE_IN_REQTYPE = LibUsb.libusb_request_type(
		LIBUSB_RECIPIENT_DEVICE, LIBUSB_REQUEST_TYPE_VENDOR, LIBUSB_ENDPOINT_IN);
	private static final int READ_STATUS_BYTES = 2;
		
	private static final int SIO_SET_DTR_MASK = 0x1;
	private static final int SIO_SET_DTR_HIGH = 1 | (SIO_SET_DTR_MASK << 8);
	private static final int SIO_SET_DTR_LOW = 0 | (SIO_SET_DTR_MASK << 8);
	private static final int SIO_SET_RTS_MASK = 0x2;
	private static final int SIO_SET_RTS_HIGH = 2 | (SIO_SET_RTS_MASK << 8);
	private static final int SIO_SET_RTS_LOW = 0 | (SIO_SET_RTS_MASK << 8);

	private static final int SIO_RESET_SIO = 0;
	private static final int SIO_RESET_PURGE_RX = 1;
	private static final int SIO_RESET_PURGE_TX = 2;
	
	private static final int SIO_RESET_REQUEST = 0x00;
	private static final int SIO_SET_MODEM_CTRL_REQUEST = 0x01;
	private static final int SIO_SET_FLOW_CTRL_REQUEST = 0x02;
	private static final int SIO_SET_BAUDRATE_REQUEST = 0x03;
	private static final int SIO_SET_DATA_REQUEST = 0x04;
	private static final int SIO_POLL_MODEM_STATUS_REQUEST = 0x05;
	private static final int SIO_SET_EVENT_CHAR_REQUEST = 0x06;
	private static final int SIO_SET_ERROR_CHAR_REQUEST = 0x07;
	private static final int SIO_SET_LATENCY_TIMER_REQUEST = 0x09;
	private static final int SIO_GET_LATENCY_TIMER_REQUEST = 0x0A;
	private static final int SIO_SET_BITMODE_REQUEST = 0x0B;
	private static final int SIO_READ_PINS_REQUEST = 0x0C;
	private static final int SIO_READ_EEPROM_REQUEST = 0x90;
	private static final int SIO_WRITE_EEPROM_REQUEST = 0x91;
	private static final int SIO_ERASE_EEPROM_REQUEST = 0x92;
	
	/**
	 * MPSSE bitbang modes
	 */
	public static enum ftdi_mpsse_mode {
		/** switch off bitbang mode, back to regular serial/FIFO */
		BITMODE_RESET(0x00),
		/** classical asynchronous bitbang mode, introduced with B-type chips */
		BITMODE_BITBANG(0x01),
		/** MPSSE mode, available on 2232x chips */
		BITMODE_MPSSE(0x02),
		/** synchronous bitbang mode, available on 2232x and R-type chips */
		BITMODE_SYNCBB(0x04),
		/** MCU Host Bus Emulation mode, available on 2232x chips */
		BITMODE_MCU(0x08),

		// CPU-style fifo mode gets set via EEPROM
		/** Fast Opto-Isolated Serial Interface Mode, available on 2232x chips */
		BITMODE_OPTO(0x10),
		/** Bitbang on CBUS pins of R-type chips, configure in EEPROM before */
		BITMODE_CBUS(0x20),
		/** Single Channel Synchronous FIFO mode, available on 2232H chips */
		BITMODE_SYNCFF(0x40),
		/** FT1284 mode, available on 232H chips */
		BITMODE_FT1284(0x80);

		public static final TypeTranscoder.Single<FtdiBitMode> xcoder =
			TypeTranscoder.single(t -> t.value, FtdiBitMode.class);
		public final int value;

		private ftdi_mpsse_mode(int value) {
			this.value = value;
		}
	}

	/** Number of bits for ftdi_set_line_property() */
	public static enum ftdi_data_bits_type {
		BITS_7(7),
		BITS_8(8);

		public static final TypeTranscoder.Single<ftdi_data_bits_type> xcoder =
			TypeTranscoder.single(t -> t.value, ftdi_data_bits_type.class);
		public final int value;

		private ftdi_data_bits_type(int value) {
			this.value = value;
		}
	}
	
	/** Number of stop bits for ftdi_set_line_property() */
	public enum ftdi_stop_bits_type {
		STOP_BIT_1(0),
		STOP_BIT_15(1),
		STOP_BIT_2(2);

		public static final TypeTranscoder.Single<ftdi_stop_bits_type> xcoder =
			TypeTranscoder.single(t -> t.value, ftdi_stop_bits_type.class);
		public final int value;

		private ftdi_stop_bits_type(int value) {
			this.value = value;
		}
	}
	
	/** Parity mode for ftdi_set_line_property() */
	public enum ftdi_parity_type {
		NONE(0),
		ODD(1),
		EVEN(2),
		MARK(3),
		SPACE(4);

		public static final TypeTranscoder.Single<ftdi_parity_type> xcoder =
			TypeTranscoder.single(t -> t.value, ftdi_parity_type.class);
		public final int value;

		private ftdi_parity_type(int value) {
			this.value = value;
		}
	}
	
	/** Break type for ftdi_set_line_property() */
	public static enum ftdi_break_type {
		BREAK_OFF(0),
		BREAK_ON(1);

		public static final TypeTranscoder.Single<ftdi_break_type> xcoder =
			TypeTranscoder.single(t -> t.value, ftdi_break_type.class);
		public final int value;

		private ftdi_break_type(int value) {
			this.value = value;
		}
	}
	
	public static class ftdi_context extends Struct {
		private static final List<String> FIELDS = List.of( //
			"usb_ctx", "usb_dev", "usb_read_timeout", "usb_write_timeout", "type", "baudrate",
			"bitbang_enabled", "readbuffer", "readbuffer_offset", "readbuffer_remaining",
			"readbuffer_chunksize", "writebuffer_chunksize", "max_packet_size", "iface", "index",
			"in_ep", "out_ep", "bitbang_mode", "eeprom", "error_str", "module_detach_mode");
		private static final BooleanAccessor.Typed<ftdi_context> bitbang_enabled_accessor =
			BooleanAccessor.typedByte(t -> t.bitbang_enabled, (t, b) -> t.bitbang_enabled = b);
		private static final IntAccessor.Typed<ftdi_context> type_accessor =
			IntAccessor.typed(t -> t.type, (t, i) -> t.type = i);

		public static class ByValue extends ftdi_context //
			implements Structure.ByValue {}

		public static class ByReference extends ftdi_context //
			implements Structure.ByReference {}

		public libusb_context usb_ctx;
		public libusb_device_handle usb_dev;
		public int usb_read_timeout;
		public int usb_write_timeout;
		public int type; // ftdi_chip_type
		public int baudrate;
		public byte bitbang_enabled; // boolean
		public Pointer readbuffer;
		public int readbuffer_offset;
		public int readbuffer_remaining;
		public int readbuffer_chunksize;
		public int writebuffer_chunksize;
		public int max_packet_size;
		public int iface;
		public int index;
		public int in_ep;
		public int out_ep;
		public byte bitbang_mode; // enum bit mode
		public ftdi_eeprom.ByReference eeprom;
		public String error_str;
		public int module_detach_mode; // ftdi_module_detach_mode

		public ftdi_context() {}

		public ftdi_context(Pointer p) {
			super(p);
		}

		public FieldTranscoder.Single<FtdiChipType> type() {
			return FtdiChipType.xcoder.field(type_accessor.from(this));
		}

		public BooleanAccessor bitbang_enabled() {
			return bitbang_enabled_accessor.from(this);
		}

		@Override
		protected List<String> getFieldOrder() {
			return FIELDS;
		}

	}

	public static class ftdi_transfer_control extends Struct {
		private static final List<String> FIELDS = List.of( //
			"completed", "buf", "size", "offset", "ftdi", "transfer");

		public static class ByValue extends ftdi_transfer_control //
			implements Structure.ByValue {}

		public static class ByReference extends ftdi_transfer_control //
			implements Structure.ByReference {}

		int completed;
		Pointer buf;
		int size;
		int offset;
		ftdi_context.ByReference ftdi;
		libusb_transfer.ByReference transfer;

		public ftdi_transfer_control() {}

		public ftdi_transfer_control(Pointer p) {
			super(p);
		}

		public byte[] buf() {
			return JnaUtil.byteArray(buf, offset, size);
		}

		@Override
		protected List<String> getFieldOrder() {
			return FIELDS;
		}
	}

	public static class ftdi_progress_info extends Struct {
		private static final List<String> FIELDS = List.of( //
			"first", "prev", "current", "totalTime", "totalRate", "currentRate");

		public static class ByValue extends ftdi_progress_info //
			implements Structure.ByValue {}

		public static class ByReference extends ftdi_progress_info //
			implements Structure.ByReference {}

		public size_and_time first;
		public size_and_time prev;
		public size_and_time current;
		public double totalTime;
		public double totalRate;
		public double currentRate;

		public ftdi_progress_info() {}

		public ftdi_progress_info(Pointer p) {
			super(p);
		}

		@Override
		protected List<String> getFieldOrder() {
			return FIELDS;
		}
	}

	public static class ftdi_version_info extends Structure {
		private static final List<String> FIELDS = List.of( //
			"major", "minor", "micro", "version_str", "snapshot_str");

		public static class ByValue extends ftdi_version_info //
			implements Structure.ByValue {}

		public static class ByReference extends ftdi_version_info //
			implements Structure.ByReference {}

		public int major;
		public int minor;
		public int micro;
		public String version_str;
		public String snapshot_str;

		public ftdi_version_info() {}

		public ftdi_version_info(Pointer p) {
			super(p);
		}

		@Override
		protected List<String> getFieldOrder() {
			return FIELDS;
		}
	}

	static final int FTDI_MAX_EEPROM_SIZE = 256;
	static final int MAX_POWER_MILLIAMP_PER_UNIT = 2;

	static class ftdi_eeprom extends Structure {
		private static final List<String> FIELDS = List.of( //
			"vendor_id", "product_id", "initialized_for_connected_device", "self_powered",
			"remote_wakeup", "is_not_pnp", "suspend_dbus7", "in_is_isochronous",
			"out_is_isochronous", "suspend_pull_downs", "use_serial", "usb_version",
			"use_usb_version", "max_power", "manufacturer", "product", "serial", "channel_a_type",
			"channel_b_type", "channel_a_driver", "channel_b_driver", "channel_c_driver",
			"channel_d_driver", "channel_a_rs485enable", "channel_b_rs485enable",
			"channel_c_rs485enable", "channel_d_rs485enable", "cbus_function", "high_current",
			"high_current_a", "high_current_b", "invert", "external_oscillator", "group0_drive",
			"group0_schmitt", "group0_slew", "group1_drive", "group1_schmitt", "group1_slew",
			"group2_drive", "group2_schmitt", "group2_slew", "group3_drive", "group3_schmitt",
			"group3_slew", "powersave", "clock_polarity", "data_order", "flow_control",
			"user_data_addr", "user_data_size", "user_data", "size", "chip", "buf",
			"release_number");

		public static class ByValue extends ftdi_eeprom //
			implements Structure.ByValue {}

		public static class ByReference extends ftdi_eeprom //
			implements Structure.ByReference {}

		public int vendor_id;
		public int product_id;
		public int initialized_for_connected_device;
		public int self_powered;
		public int remote_wakeup;
		public int is_not_pnp;
		public int suspend_dbus7;
		public int in_is_isochronous;
		public int out_is_isochronous;
		public int suspend_pull_downs;
		public int use_serial;
		public int usb_version;
		public int use_usb_version;
		public int max_power;
		public String manufacturer;
		public String product;
		public String serial;
		public int channel_a_type;
		public int channel_b_type;
		public int channel_a_driver;
		public int channel_b_driver;
		public int channel_c_driver;
		public int channel_d_driver;
		public int channel_a_rs485enable;
		public int channel_b_rs485enable;
		public int channel_c_rs485enable;
		public int channel_d_rs485enable;
		public int[] cbus_function = new int[10];
		public int high_current;
		public int high_current_a;
		public int high_current_b;
		public int invert;
		public int external_oscillator;
		public int group0_drive;
		public int group0_schmitt;
		public int group0_slew;
		public int group1_drive;
		public int group1_schmitt;
		public int group1_slew;
		public int group2_drive;
		public int group2_schmitt;
		public int group2_slew;
		public int group3_drive;
		public int group3_schmitt;
		public int group3_slew;
		public int powersave;
		public int clock_polarity;
		public int data_order;
		public int flow_control;
		public int user_data_addr;
		public int user_data_size;
		public String user_data;
		public int size;
		public int chip;
		public byte[] buf = new byte[FTDI_MAX_EEPROM_SIZE];
		public int release_number;

		public ftdi_eeprom() {}

		public ftdi_eeprom(Pointer p) {
			super(p);
		}

		@Override
		protected List<String> getFieldOrder() {
			return FIELDS;
		}
	}

	public static class size_and_time extends Struct {
		private static final List<String> FIELDS = List.of( //
			"totalBytes", "time");

		public static class ByValue extends size_and_time //
			implements Structure.ByValue {}

		public static class ByReference extends size_and_time //
			implements Structure.ByReference {}

		public long totalBytes;
		public timeval time;

		public size_and_time() {}

		public size_and_time(Pointer p) {
			super(p);
		}

		@Override
		protected List<String> getFieldOrder() {
			return FIELDS;
		}

	}


	public static FtdiContext ftdi_init(ftdi_context ftdi) throws LibUsbException {
		FtdiContext ftdi = new FtdiContext();
		ftdi.usb_ctx = LibUsbContext.init();
		return ftdi;
	}

	public static void ftdi_setInterface(ftdi_context ftdi, FtdiInterface iface) throws FtdiException {
		validateNotNull(iface);
		if (ftdi.usb_dev == null) this.iface = iface;
		else if (this.iface != iface)
			throw new FtdiException(-3, "Interface can not be changed on an already open device");
	}

	public static void ftdi_set_usb_dev(ftdi_context ftdi, LibUsb.libusb_device_handle handle) {
		ftdi.usb_dev = handle;
	}

	public static void ftdi_reset(ftdi_context ftdi) throws LibUsbException {
		requireDev(ftdi);
		controlTransferOut(ftdi, SIO_RESET_REQUEST, SIO_RESET_SIO, ftdi.index);
		clearReadBuffer(ftdi);
	}

	public static void ftdi_purge_buffers(ftdi_context ftdi) throws LibUsbException {
		ftdi_purge_rx_buffer(ftdi);
		ftdi_purge_tx_buffer(ftdi);
	}

	public static void ftdi_purge_rx_buffer(ftdi_context ftdi) throws LibUsbException {
		requireDev(ftdi);
		controlTransferOut(ftdi, SIO_RESET_REQUEST, SIO_RESET_PURGE_RX, ftdi.index);
		clearReadBuffer(ftdi);
	}

	public static void ftdi_purge_tx_buffer(ftdi_context ftdi) throws LibUsbException {
		requireDev(ftdi);
		controlTransferOut(ftdi, SIO_RESET_REQUEST, SIO_RESET_PURGE_TX, ftdi.index);
	}

	public static void ftdi_set_line_property(ftdi_context ftdi, ftdi_data_bits_type bits, ftdi_stop_bits_type sbit, ftdi_parity_type parity)
		throws LibUsbException {
		ftdi_set_line_property(ftdi, bits, sbit, parity, ftdi_break_type.BREAK_OFF);
	}

	public static void ftdi_set_line_property(ftdi_context ftdi, ftdi_data_bits_type bits, ftdi_stop_bits_type stopBits, ftdi_parity_type parity,
		ftdi_break_type breakType) throws LibUsbException {
		requireDev(ftdi);
		int value = breakType.value << 14 | stopBits.value << 11 | parity.value << 8 | bits.value;
		controlTransferOut(ftdi, SIO_SET_DATA_REQUEST, value, ftdi.index);
	}

	public static int ftdi_write_data(ftdi_context ftdi, byte...data) throws LibUsbException {
		return ftdi_write_data(ftdi, data, 0, data.length);
	}
	
	public static int ftdi_write_data(ftdi_context ftdi, byte[] data, int offset) throws LibUsbException {
		return ftdi_write_data(ftdi, data, offset, data.length - offset);
	}
	
	public static int ftdi_write_data(ftdi_context ftdi, byte[] data, int offset, int len) throws LibUsbException {
		return ftdi_write_data(ftdi, ByteBuffer.wrap(data, offset, len));
	}
	
	public static int ftdi_write_data(ftdi_context ftdi, ByteBuffer buffer) throws LibUsbException {
		requireDev(ftdi);
		int offset = 0;
		while (buffer.hasRemaining()) {
			int len = Math.min(ftdi.writebuffer_chunksize, buffer.remaining());
			int n = LibUsb.libusb_bulk_transfer(ftdi.usb_dev, ftdi.in_ep, buffer, len,
				ftdi.usb_write_timeout);
			offset += n;
		}
		return offset;
	}

	public static int ftdi_read_data(ftdi_context ftdi) throws LibUsbException {
		byte[] data = ftdi_read_data(ftdi, 1);
		if (data.length == 0) return -1; 
		return JnaUtil.ubyte(data[0]);
	}
	
	public static byte[] ftdi_read_data(ftdi_context ftdi, int size) throws LibUsbException {
		byte[] buffer = new byte[size];
		int n = ftdi_read_data(ftdi, ByteBuffer.wrap(buffer), size);
		return n == size ? buffer : Arrays.copyOf(buffer, n);
	}
	
	public static int ftdi_read_data(ftdi_context ftdi, ByteBuffer buffer) throws LibUsbException {
		return ftdi_read_data(ftdi, buffer, buffer.remaining());
	}
	
	public static int ftdi_read_data(ftdi_context ftdi, ByteBuffer buffer, int size) throws LibUsbException {
		requireDev(ftdi);
		int remaining = size;
		ByteBuffer readBuffer = ByteBuffer.allocate(ftdi.readbuffer_chunksize);
		while (remaining > 0) {
			int len = readLen(ftdi, remaining);
			readBuffer.clear();
			int n = LibUsb.libusb_bulk_transfer(ftdi.usb_dev, ftdi.out_ep, readBuffer, len, ftdi.usb_read_timeout);
			if (n <= READ_STATUS_BYTES) break;
			int packets = (n + ftdi.max_packet_size - 1) / ftdi.max_packet_size;
			for (int i = 0; i < packets; i++) {
				int position = (i * ftdi.max_packet_size) + READ_STATUS_BYTES;
				int limit = Math.min(n, position + ftdi.max_packet_size - READ_STATUS_BYTES);
				if (limit <= position) break;
				buffer.put(readBuffer.limit(limit).position(position));
				remaining -= (limit - position);
			}
		}
		return size - remaining;
	}

	private static int readLen(ftdi_context ftdi, int size) {
		int packets = size / (ftdi.max_packet_size - READ_STATUS_BYTES);
		int rem = size % (ftdi.max_packet_size - READ_STATUS_BYTES);
		int total = (packets * ftdi.max_packet_size) + (rem > 0 ? READ_STATUS_BYTES + rem : 0);
		return Math.min(total, ftdi.readbuffer_chunksize);
	}

	public static void ftdi_read_data_set_chunk_size(ftdi_context ftdi, int chunkSize) {
		clearReadBuffer(ftdi);
		ftdi.readbuffer = new Memory(chunkSize); // not used?
		ftdi.readbuffer_chunksize = chunkSize;
	}

	public static void ftdi_setBitMode(ftdi_context ftdi, int bitmask, ftdi_mpsse_mode mode) throws LibUsbException {
		requireDev(ftdi);
		int value = mode.value << 8 | bitmask;
		controlTransferOut(ftdi, SIO_SET_BITMODE_REQUEST, value, ftdi.index);
		ftdi.bitbang_mode = (byte) mode.value;
		ftdi.bitbang_enabled = (byte)(mode != ftdi_mpsse_mode.BITMODE_RESET ? 1 : 0);
	}

	public static void ftdi_disableBitbang(ftdi_context ftdi) throws LibUsbException {
		requireDev(ftdi);
		controlTransferOut(ftdi, SIO_SET_BITMODE_REQUEST, 0, ftdi.index);
		ftdi.bitbang_enabled = 1;
	}

	public static int ftdi_read_pins(ftdi_context ftdi) throws LibUsbException {
		requireDev(ftdi);
		byte[] data = controlTransferIn(ftdi, SIO_READ_PINS_REQUEST, 0, ftdi.index, 1);
		return JnaUtil.ubyte(data[0]);
	}

	public static void ftdi_set_latency_timer(ftdi_context ftdi, int latency) throws LibUsbException {
		validateRange(latency, 1, 255, "latency");
		requireDev(ftdi);
		controlTransferOut(ftdi, SIO_SET_LATENCY_TIMER_REQUEST, latency, ftdi.index);
	}

	public static int ftdi_get_latency_timer(ftdi_context ftdi) throws LibUsbException {
		requireDev(ftdi);
		byte[] data = controlTransferIn(ftdi, SIO_GET_LATENCY_TIMER_REQUEST, 0, ftdi.index, 1);
		return JnaUtil.ubyte(data[0]);
	}

	public static int ftdi_poll_modem_status(ftdi_context ftdi) throws LibUsbException {
		requireDev(ftdi);
		byte[] data = controlTransferIn(ftdi, SIO_POLL_MODEM_STATUS_REQUEST, 0, ftdi.index, 2);
		return data[1] << 8 | data[0];
	}

	public static void ftdi_set_flow_ctrl(ftdi_context ftdi, FtdiFlowControl flowCtrl)
		throws LibUsbException {
		requireDev(ftdi);
		controlTransferOut(ftdi, SIO_SET_FLOW_CTRL_REQUEST, 0, flowCtrl.value | ftdi.index);
	}

	public static void ftdi_set_dtr(ftdi_context ftdi, boolean state) throws LibUsbException {
		requireDev(ftdi);
		int val = (state ? SIO_SET_DTR_HIGH : SIO_SET_DTR_LOW);
		controlTransferOut(ftdi, SIO_SET_MODEM_CTRL_REQUEST, val, ftdi.index);
	}

	public static void ftdi_set_rts(ftdi_context ftdi, boolean state) throws LibUsbException {
		requireDev(ftdi);
		int val = (state ? SIO_SET_RTS_HIGH : SIO_SET_RTS_LOW);
		controlTransferOut(ftdi, SIO_SET_MODEM_CTRL_REQUEST, val, ftdi.index);
	}

	public static void ftdi_set_dtr_rts(ftdi_context ftdi, boolean dtr, boolean rts) throws LibUsbException {
		requireDev(ftdi);
		int val = (dtr ? SIO_SET_DTR_HIGH : SIO_SET_DTR_LOW) | //
			(rts ? SIO_SET_RTS_HIGH : SIO_SET_RTS_LOW);
		controlTransferOut(ftdi, SIO_SET_MODEM_CTRL_REQUEST, val, ftdi.index);
	}

	public static void ftdi_set_event_char(ftdi_context ftdi, char eventch, boolean enable) throws LibUsbException {
		requireDev(ftdi);
		int val = eventch & 0xff;
		if (enable) val |= 1 << 8;
		controlTransferOut(ftdi, SIO_SET_EVENT_CHAR_REQUEST, val, ftdi.index);
	}

	public static void ftdi_set_error_char(ftdi_context ftdi, char errorch, boolean enable) throws LibUsbException {
		requireDev(ftdi);
		int val = errorch & 0xff;
		if (enable) val |= 1 << 8;
		controlTransferOut(ftdi, SIO_SET_ERROR_CHAR_REQUEST, val, ftdi.index);
	}

	public static void close(ftdi_context ftdi) {
		if (ftdi == null) return;
		LogUtil.close(logger, ftdi.usb_dev, dev ->
			LibUsb.libusb_release_interface(dev, ftdi.iface));
		LogUtil.close(logger, ftdi.usb_dev, dev -> LibUsb.libusb_close(dev));
		// TODO: eeprom.close()?
		if (ftdi.eeprom != null) ftdi.eeprom.initialized_for_connected_device = 0;
		LogUtil.close(logger, ftdi.usb_ctx, ctx -> LibUsb.libusb_exit(ctx));
	}

	public static String ftdi_get_error_string (ftdi_context ftdi) {
	    return ftdi == null ? null : ftdi.error_str;
	}
	
	private static boolean controlTransferOut(ftdi_context ftdi, int request, int value, int index)
		throws LibUsbException {
		if (ftdi.usb_dev == null) return false;
		LibUsb.libusb_control_transfer(ftdi.usb_dev, FTDI_DEVICE_OUT_REQTYPE,
			request, value, index, ftdi.usb_write_timeout);
		return true;
	}

	private static byte[] controlTransferIn(ftdi_context ftdi, int request, int value, int index, int length)
		throws LibUsbException {
		if (ftdi.usb_dev == null) return null;
		return LibUsb.libusb_control_transfer(ftdi.usb_dev,
			FTDI_DEVICE_IN_REQTYPE, request, value, index, length,
			ftdi.usb_read_timeout);
	}

	private static void clearReadBuffer(ftdi_context ctx) {
		// TODO: not needed?
		ctx.readbuffer_offset = 0;
		ctx.readbuffer_remaining = 0;
	}

	private static void requireDev(ftdi_context ftdi) throws LibFtdiException {
		require(ftdi);
		require(ftdi.usb_ctx, "USB context");
		require(ftdi.usb_dev, "USB device");
	}

	private static void require(ftdi_context ftdi) throws LibFtdiException {
		require(ftdi, "Ftdi context");
	}

	private static void require(Object obj, String name) throws LibFtdiException {
		if (obj != null) return;
		throw new LibFtdiException(name + " unavailable",
			libusb_error.LIBUSB_ERROR_INVALID_PARAM.value);
	}

}
