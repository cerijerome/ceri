package ceri.serial.ftdi.jna;

import static ceri.common.collection.ImmutableUtil.enumSet;
import static ceri.common.math.MathUtil.ubyte;
import static ceri.common.validation.ValidationUtil.validateRange;
import static ceri.serial.clib.jna.CException.capture;
import static ceri.serial.ftdi.jna.LibFtdi.ftdi_chip_type.TYPE_BM;
import static ceri.serial.ftdi.jna.LibFtdi.ftdi_interface.INTERFACE_A;
import static ceri.serial.ftdi.jna.LibFtdi.ftdi_interface.INTERFACE_ANY;
import static ceri.serial.ftdi.jna.LibFtdi.ftdi_module_detach_mode.AUTO_DETACH_SIO_MODULE;
import static ceri.serial.ftdi.jna.LibFtdi.ftdi_mpsse_mode.BITMODE_BITBANG;
import static ceri.serial.ftdi.jna.LibFtdi.ftdi_mpsse_mode.BITMODE_RESET;
import static ceri.serial.ftdi.jna.LibFtdi.ftdi_request_type.SIO_GET_LATENCY_TIMER_REQUEST;
import static ceri.serial.ftdi.jna.LibFtdi.ftdi_request_type.SIO_POLL_MODEM_STATUS_REQUEST;
import static ceri.serial.ftdi.jna.LibFtdi.ftdi_request_type.SIO_READ_PINS_REQUEST;
import static ceri.serial.ftdi.jna.LibFtdi.ftdi_request_type.SIO_RESET_REQUEST;
import static ceri.serial.ftdi.jna.LibFtdi.ftdi_request_type.SIO_SET_BAUDRATE_REQUEST;
import static ceri.serial.ftdi.jna.LibFtdi.ftdi_request_type.SIO_SET_BITMODE_REQUEST;
import static ceri.serial.ftdi.jna.LibFtdi.ftdi_request_type.SIO_SET_DATA_REQUEST;
import static ceri.serial.ftdi.jna.LibFtdi.ftdi_request_type.SIO_SET_ERROR_CHAR_REQUEST;
import static ceri.serial.ftdi.jna.LibFtdi.ftdi_request_type.SIO_SET_EVENT_CHAR_REQUEST;
import static ceri.serial.ftdi.jna.LibFtdi.ftdi_request_type.SIO_SET_FLOW_CTRL_REQUEST;
import static ceri.serial.ftdi.jna.LibFtdi.ftdi_request_type.SIO_SET_LATENCY_TIMER_REQUEST;
import static ceri.serial.ftdi.jna.LibFtdi.ftdi_request_type.SIO_SET_MODEM_CTRL_REQUEST;
import static ceri.serial.ftdi.jna.LibFtdiUtil.guessChipType;
import static ceri.serial.ftdi.jna.LibFtdiUtil.require;
import static ceri.serial.ftdi.jna.LibFtdiUtil.requireCtx;
import static ceri.serial.ftdi.jna.LibFtdiUtil.requireDev;
import static ceri.serial.ftdi.jna.LibFtdiUtil.vendor;
import static ceri.serial.libusb.jna.LibUsb.libusb_alloc_transfer;
import static ceri.serial.libusb.jna.LibUsb.libusb_bulk_transfer;
import static ceri.serial.libusb.jna.LibUsb.libusb_cancel_transfer;
import static ceri.serial.libusb.jna.LibUsb.libusb_claim_interface;
import static ceri.serial.libusb.jna.LibUsb.libusb_close;
import static ceri.serial.libusb.jna.LibUsb.libusb_control_transfer;
import static ceri.serial.libusb.jna.LibUsb.libusb_detach_kernel_driver;
import static ceri.serial.libusb.jna.LibUsb.libusb_exit;
import static ceri.serial.libusb.jna.LibUsb.libusb_fill_bulk_transfer;
import static ceri.serial.libusb.jna.LibUsb.libusb_free_config_descriptor;
import static ceri.serial.libusb.jna.LibUsb.libusb_free_transfer;
import static ceri.serial.libusb.jna.LibUsb.libusb_get_config_descriptor;
import static ceri.serial.libusb.jna.LibUsb.libusb_get_configuration;
import static ceri.serial.libusb.jna.LibUsb.libusb_get_device_descriptor;
import static ceri.serial.libusb.jna.LibUsb.libusb_get_string_descriptor_ascii;
import static ceri.serial.libusb.jna.LibUsb.libusb_handle_events_timeout_completed;
import static ceri.serial.libusb.jna.LibUsb.libusb_init;
import static ceri.serial.libusb.jna.LibUsb.libusb_open;
import static ceri.serial.libusb.jna.LibUsb.libusb_set_configuration;
import static ceri.serial.libusb.jna.LibUsb.libusb_submit_transfer;
import static ceri.serial.libusb.jna.LibUsb.libusb_unref_devices;
import static ceri.serial.libusb.jna.LibUsb.libusb_endpoint_direction.LIBUSB_ENDPOINT_IN;
import static ceri.serial.libusb.jna.LibUsb.libusb_endpoint_direction.LIBUSB_ENDPOINT_OUT;
import static ceri.serial.libusb.jna.LibUsb.libusb_error.LIBUSB_ERROR_INTERRUPTED;
import static ceri.serial.libusb.jna.LibUsb.libusb_error.LIBUSB_ERROR_NOT_SUPPORTED;
import static ceri.serial.libusb.jna.LibUsb.libusb_request_recipient.LIBUSB_RECIPIENT_DEVICE;
import static ceri.serial.libusb.jna.LibUsb.libusb_request_type.LIBUSB_REQUEST_TYPE_VENDOR;
import static ceri.serial.libusb.jna.LibUsb.libusb_transfer_status.LIBUSB_TRANSFER_CANCELLED;
import java.nio.ByteBuffer;
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
import ceri.serial.clib.jna.Time.timeval;
import ceri.serial.jna.JnaUtil;
import ceri.serial.jna.Struct;
import ceri.serial.libusb.jna.LibUsb.libusb_config_descriptor;
import ceri.serial.libusb.jna.LibUsb.libusb_context;
import ceri.serial.libusb.jna.LibUsb.libusb_device;
import ceri.serial.libusb.jna.LibUsb.libusb_device_descriptor;
import ceri.serial.libusb.jna.LibUsb.libusb_device_handle;
import ceri.serial.libusb.jna.LibUsb.libusb_interface;
import ceri.serial.libusb.jna.LibUsb.libusb_interface_descriptor;
import ceri.serial.libusb.jna.LibUsb.libusb_transfer;
import ceri.serial.libusb.jna.LibUsb.libusb_transfer_status;
import ceri.serial.libusb.jna.LibUsbException;
import ceri.serial.libusb.jna.LibUsbFinder;
import ceri.serial.libusb.jna.LibUsbNotFoundException;
import ceri.serial.libusb.jna.LibUsbUtil;

/**
 * Implementation of libftdi, built on top of libusb JNA code.
 * <p/>
 * TODO: async transfer testing, implement eeprom functionality.
 */
public class LibFtdi {
	private static final Logger logger = LogManager.getLogger();
	private static final int FTDI_DEVICE_OUT_REQTYPE = LibUsbUtil.requestTypeValue( // 0x40
		LIBUSB_RECIPIENT_DEVICE, LIBUSB_REQUEST_TYPE_VENDOR, LIBUSB_ENDPOINT_OUT);
	private static final int FTDI_DEVICE_IN_REQTYPE = LibUsbUtil.requestTypeValue( // 0xc0
		LIBUSB_RECIPIENT_DEVICE, LIBUSB_REQUEST_TYPE_VENDOR, LIBUSB_ENDPOINT_IN);
	// private static final libusb_transfer_cb_fn ftdi_write_data_cb = LibFtdi::ftdi_write_data_cb;
	// private static final libusb_transfer_cb_fn ftdi_read_data_cb = LibFtdi::ftdi_read_data_cb;
	private static final int CHUNKSIZE_DEF = 4096;
	private static final int TIMEOUT_MS_DEF = 5000;
	private static final int FTDI_MAX_EEPROM_SIZE = 256;
	private static final int BAUD_RATE = 9600;
	private static final int READ_STATUS_BYTES = 2;
	public static final int FTDI_VENDOR_ID = 0x403;
	// eeprom not yet supported
	// private static final int MAX_POWER_MILLIAMP_PER_UNIT = 2;
	// SIO_RESET_REQUEST values
	private static final int SIO_RESET_SIO = 0;
	private static final int SIO_RESET_PURGE_RX = 1;
	private static final int SIO_RESET_PURGE_TX = 2;
	// SIO_SET_MODEM_CTRL_REQUEST values
	private static final int SIO_SET_DTR_HIGH = 0x101;
	private static final int SIO_SET_DTR_LOW = 0x100;
	private static final int SIO_SET_RTS_HIGH = 0x202;
	private static final int SIO_SET_RTS_LOW = 0x200;

	private LibFtdi() {}

	static enum ftdi_request_type {
		SIO_RESET_REQUEST(0x00),
		SIO_SET_MODEM_CTRL_REQUEST(0x01),
		SIO_SET_FLOW_CTRL_REQUEST(0x02),
		SIO_SET_BAUDRATE_REQUEST(0x03),
		SIO_SET_DATA_REQUEST(0x04),
		SIO_POLL_MODEM_STATUS_REQUEST(0x05),
		SIO_SET_EVENT_CHAR_REQUEST(0x06),
		SIO_SET_ERROR_CHAR_REQUEST(0x07),
		SIO_SET_LATENCY_TIMER_REQUEST(0x09),
		SIO_GET_LATENCY_TIMER_REQUEST(0x0A),
		SIO_SET_BITMODE_REQUEST(0x0B),
		SIO_READ_PINS_REQUEST(0x0C),
		SIO_READ_EEPROM_REQUEST(0x90),
		SIO_WRITE_EEPROM_REQUEST(0x91),
		SIO_ERASE_EEPROM_REQUEST(0x92);

		public static final TypeTranscoder<ftdi_request_type> xcoder =
			TypeTranscoder.of(t -> t.value, ftdi_request_type.class);
		public final int value;

		ftdi_request_type(int value) {
			this.value = value;
		}
	}

	public enum ftdi_chip_type {
		TYPE_AM(0),
		TYPE_BM(1),
		TYPE_2232C(2),
		TYPE_R(3),
		TYPE_2232H(4),
		TYPE_4232H(5),
		TYPE_232H(6),
		TYPE_230X(7);

		public static final TypeTranscoder<ftdi_chip_type> xcoder =
			TypeTranscoder.of(t -> t.value, ftdi_chip_type.class);
		public static final Set<ftdi_chip_type> H_TYPES =
			enumSet(TYPE_2232H, TYPE_4232H, TYPE_232H);
		public static final Set<ftdi_chip_type> SYNC_FIFO_TYPES = enumSet(TYPE_2232H, TYPE_232H);
		public final int value;

		public static boolean isHType(ftdi_chip_type type) {
			return H_TYPES.contains(type);
		}

		public static boolean isAmType(ftdi_chip_type type) {
			return TYPE_AM == type;
		}

		public static boolean isSyncFifoType(ftdi_chip_type type) {
			return SYNC_FIFO_TYPES.contains(type);
		}

		ftdi_chip_type(int value) {
			this.value = value;
		}

		@Deprecated
		public boolean isHType() {
			return H_TYPES.contains(this);
		}

		@Deprecated
		public boolean isSyncFifoType() {
			return SYNC_FIFO_TYPES.contains(this);
		}
	}

	/**
	 * MPSSE bitbang modes
	 */
	public enum ftdi_mpsse_mode {
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

		public static final TypeTranscoder<ftdi_mpsse_mode> xcoder =
			TypeTranscoder.of(t -> t.value, ftdi_mpsse_mode.class);
		public final int value;

		ftdi_mpsse_mode(int value) {
			this.value = value;
		}
	}

	public enum ftdi_interface {
		INTERFACE_ANY(0, 0, 1, 2), // same as INTERFACE_A
		INTERFACE_A(1, 0, 1, 2), // 0, 1, 0x81, 0x02
		INTERFACE_B(2, 1, 3, 4), // 1, 2, 0x83, 0x04
		INTERFACE_C(3, 2, 5, 6), // 2, 3, 0x85, 0x06
		INTERFACE_D(4, 3, 7, 8); // 3, 4, 0x87, 0x08

		public static final TypeTranscoder<ftdi_interface> xcoder =
			TypeTranscoder.of(t -> t.index, ftdi_interface.class);
		public final int index;
		public final int iface;
		public final int outAddress;
		public final int inAddress;

		ftdi_interface(int index, int iface, int outAddress, int inAddress) {
			this.index = index;
			this.iface = iface;
			this.outAddress = outAddress;
			this.inAddress = inAddress;
		}
	}

	public enum ftdi_module_detach_mode {
		AUTO_DETACH_SIO_MODULE(0),
		DONT_DETACH_SIO_MODULE(1),
		AUTO_DETACH_REATACH_SIO_MODULE(2);

		public static final TypeTranscoder<ftdi_module_detach_mode> xcoder =
			TypeTranscoder.of(t -> t.value, ftdi_module_detach_mode.class);
		public final int value;

		ftdi_module_detach_mode(int value) {
			this.value = value;
		}
	}

	/** Number of bits for ftdi_set_line_property() */
	public enum ftdi_data_bits_type {
		BITS_7(7),
		BITS_8(8);

		public static final TypeTranscoder<ftdi_data_bits_type> xcoder =
			TypeTranscoder.of(t -> t.value, ftdi_data_bits_type.class);
		public final int value;

		ftdi_data_bits_type(int value) {
			this.value = value;
		}
	}

	/** Number of stop bits for ftdi_set_line_property() */
	public enum ftdi_stop_bits_type {
		STOP_BIT_1(0),
		STOP_BIT_15(1),
		STOP_BIT_2(2);

		public static final TypeTranscoder<ftdi_stop_bits_type> xcoder =
			TypeTranscoder.of(t -> t.value, ftdi_stop_bits_type.class);
		public final int value;

		ftdi_stop_bits_type(int value) {
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

		public static final TypeTranscoder<ftdi_parity_type> xcoder =
			TypeTranscoder.of(t -> t.value, ftdi_parity_type.class);
		public final int value;

		ftdi_parity_type(int value) {
			this.value = value;
		}
	}

	/** Break type for ftdi_set_line_property() */
	public enum ftdi_break_type {
		BREAK_OFF(0),
		BREAK_ON(1);

		public static final TypeTranscoder<ftdi_break_type> xcoder =
			TypeTranscoder.of(t -> t.value, ftdi_break_type.class);
		public final int value;

		ftdi_break_type(int value) {
			this.value = value;
		}
	}

	public enum ftdi_flow_control {
		SIO_DISABLE_FLOW_CTRL(0x0000),
		SIO_RTS_CTS_HS(0x0100),
		SIO_DTR_DSR_HS(0x0200),
		SIO_XON_XOFF_HS(0x0400);

		public static final TypeTranscoder<ftdi_flow_control> xcoder =
			TypeTranscoder.of(t -> t.value, ftdi_flow_control.class);
		public final int value;

		ftdi_flow_control(int value) {
			this.value = value;
		}
	}

	public static class ftdi_context extends Struct {
		private static final List<String> FIELDS = List.of( //
			"usb_ctx", "usb_dev", "usb_read_timeout", "usb_write_timeout", "type", "baudrate",
			"bitbang_enabled", "readbuffer", "readbuffer_offset", "readbuffer_remaining",
			"readbuffer_chunksize", "writebuffer_chunksize", "max_packet_size", "iface", "index",
			"in_ep", "out_ep", "bitbang_mode", "eeprom", "error_str", "module_detach_mode");
		private static final IntAccessor.Typed<ftdi_context> type_accessor =
			IntAccessor.typed(t -> t.type, (t, i) -> t.type = i);
		private static final BooleanAccessor.Typed<ftdi_context> bitbang_enabled_accessor =
			BooleanAccessor.typedByte(t -> t.bitbang_enabled, (t, b) -> t.bitbang_enabled = b);
		private static final IntAccessor.Typed<ftdi_context> index_accessor =
			IntAccessor.typed(t -> t.index, (t, i) -> t.index = i);
		private static final IntAccessor.Typed<ftdi_context> bitbang_mode_accessor =
			IntAccessor.typedUbyte(t -> t.bitbang_mode, (t, i) -> t.bitbang_mode = i);
		private static final IntAccessor.Typed<ftdi_context> module_detach_mode_accessor =
			IntAccessor.typed(t -> t.module_detach_mode, (t, i) -> t.module_detach_mode = i);

		public static class ByValue extends ftdi_context //
			implements Structure.ByValue {}

		public static class ByReference extends ftdi_context implements Structure.ByReference {
			public ByReference() {}

			public ByReference(Pointer p) {
				super(p);
			}
		}

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
		public int index; // ftdi_interface (confusing name!)
		public int in_ep;
		public int out_ep;
		public byte bitbang_mode; // ftdi_bit_mode
		public ftdi_eeprom.ByReference eeprom;
		public Pointer error_str; // const char*
		public int module_detach_mode; // ftdi_module_detach_mode

		public ftdi_context() {}

		public ftdi_context(Pointer p) {
			super(p);
		}

		public FieldTranscoder<ftdi_chip_type> type() {
			return ftdi_chip_type.xcoder.field(type_accessor.from(this));
		}

		public BooleanAccessor bitbang_enabled() {
			return bitbang_enabled_accessor.from(this);
		}

		public FieldTranscoder<ftdi_interface> index() {
			return ftdi_interface.xcoder.field(index_accessor.from(this));
		}

		public FieldTranscoder<ftdi_mpsse_mode> bitbang_mode() {
			return ftdi_mpsse_mode.xcoder.field(bitbang_mode_accessor.from(this));
		}

		public FieldTranscoder<ftdi_module_detach_mode> module_detach_mode() {
			return ftdi_module_detach_mode.xcoder.field(module_detach_mode_accessor.from(this));
		}

		@Override
		protected List<String> getFieldOrder() {
			return FIELDS;
		}

	}

	public static class ftdi_transfer_control {
		private static final IntAccessor.Typed<ftdi_transfer_control> completed_accessor =
			IntAccessor.typed(t -> t.completed, (t, i) -> t.completed = i);
		public int completed;
		public Pointer buf;
		public int size;
		public int offset;
		public ftdi_context ftdi; // ftdi_context.ByReference
		// Use setAutoSynch(false) to only use pointer (no reads/writes)
		public libusb_transfer transfer; // libusb_transfer.ByReference

		public FieldTranscoder<libusb_transfer_status> completed() {
			return libusb_transfer_status.xcoder.field(completed_accessor.from(this));
		}

		public byte[] buf() {
			return JnaUtil.byteArray(buf, offset, size);
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

	public static class ftdi_eeprom extends Structure {
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

	public static class ftdi_string_descriptors {
		public final String manufacturer;
		public final String description;
		public final String serial;

		ftdi_string_descriptors(String manufacturer, String description, String serial) {
			this.manufacturer = manufacturer;
			this.description = description;
			this.serial = serial;
		}
	}

	/**
	 * Create a new FTDI context, and initialize it.
	 */
	public static ftdi_context ftdi_new() throws LibUsbException {
		ftdi_context ftdi = new ftdi_context();
		ftdi_init(ftdi);
		return ftdi;
	}

	/**
	 * Initialize FTDI context. This overwrites any current values, so caller must ensure opened
	 * resources are closed.
	 */
	public static void ftdi_init(ftdi_context ftdi) throws LibUsbException {
		try {
			ftdi.usb_ctx = null;
			ftdi.usb_dev = null;
			ftdi.usb_read_timeout = TIMEOUT_MS_DEF;
			ftdi.usb_write_timeout = TIMEOUT_MS_DEF;
			ftdi.type().set(TYPE_BM);
			ftdi.baudrate = -1;
			ftdi.bitbang_enabled().set(false);
			ftdi.readbuffer = null;
			ftdi.writebuffer_chunksize = CHUNKSIZE_DEF;
			ftdi.max_packet_size = 0;
			ftdi.module_detach_mode().set(AUTO_DETACH_SIO_MODULE);
			ftdi.usb_ctx = libusb_init();
			ftdi_set_interface(ftdi, INTERFACE_ANY);
			ftdi.bitbang_mode().set(BITMODE_BITBANG);
			ftdi.eeprom = new ftdi_eeprom.ByReference();
			ftdi_read_data_set_chunk_size(ftdi, CHUNKSIZE_DEF);
		} catch (LibUsbException | RuntimeException e) {
			ftdi_free(ftdi);
			throw e;
		}
	}

	/**
	 * Frees the ftdi context, and closes the usb device handle if set.
	 */
	public static void ftdi_free(ftdi_context ftdi) throws LibUsbException {
		if (ftdi == null) return;
		closeDevice(ftdi);
		ftdi.readbuffer = null;
		ftdi.eeprom = null;
		libusb_exit(ftdi.usb_ctx);
		ftdi.usb_ctx = null;
	}

	/**
	 * Sets the usb interface type. Must be called before opening a device.
	 */
	public static void ftdi_set_interface(ftdi_context ftdi, ftdi_interface iface)
		throws LibUsbException {
		require(ftdi);
		if (iface == INTERFACE_ANY) iface = INTERFACE_A;
		if (ftdi.usb_dev != null && ftdi.index != iface.index) throw LibUsbException
			.of(LIBUSB_ERROR_NOT_SUPPORTED, "Interface cannot be changed on an open device");
		ftdi.iface = iface.iface;
		ftdi.index = iface.index;
		ftdi.out_ep = LibUsbUtil.endpointAddress(iface.outAddress, LIBUSB_ENDPOINT_IN);
		ftdi.in_ep = LibUsbUtil.endpointAddress(iface.inAddress, LIBUSB_ENDPOINT_OUT);
	}

	/**
	 * Sets the usb device handle. Caller is responsible for freeing any existing handle.
	 */
	public static void ftdi_set_usb_dev(ftdi_context ftdi, libusb_device_handle usb) {
		if (ftdi != null) ftdi.usb_dev = usb;
	}

	/**
	 * Finds all ftdi devices with given VID:PID on the usb bus. Creates a new device list which
	 * needs to be deallocated by ftdi_list_free() after use. With VID:PID 0:0, search for the
	 * default devices with vendor id 0x403.
	 */
	public static List<libusb_device> ftdi_usb_find_all(ftdi_context ftdi, int vendor, int product)
		throws LibUsbException {
		LibUsbFinder finder =
			LibUsbFinder.builder().vendor(vendor(vendor)).product(product).build();
		return ftdi_usb_find_all(ftdi, finder);
	}

	/**
	 * Finds all matching ftdi devices. Creates a new device list which needs to be deallocated by
	 * ftdi_list_free() after use.
	 */
	public static List<libusb_device> ftdi_usb_find_all(ftdi_context ftdi, LibUsbFinder finder)
		throws LibUsbException {
		requireCtx(ftdi);
		return finder.findAndRef(ftdi.usb_ctx, 0);
	}

	/**
	 * Must be called to free device list returned from ftdi_usb_find_all methods.
	 */
	public static void ftdi_list_free(List<libusb_device> devlist) throws LibUsbException {
		libusb_unref_devices(devlist);
	}

	/**
	 * Return device ID strings from the usb device. This method opens and closes the device if not
	 * already open.
	 */
	public static ftdi_string_descriptors ftdi_usb_get_strings(ftdi_context ftdi, libusb_device dev)
		throws LibUsbException {
		require(ftdi);
		LibUsbUtil.require(dev);
		boolean need_open = ftdi.usb_dev == null;
		if (need_open) ftdi.usb_dev = libusb_open(dev);
		try {
			libusb_device_descriptor desc = libusb_get_device_descriptor(dev);
			String mfr = libusb_get_string_descriptor_ascii(ftdi.usb_dev, desc.iManufacturer);
			String description = libusb_get_string_descriptor_ascii(ftdi.usb_dev, desc.iProduct);
			String serial = libusb_get_string_descriptor_ascii(ftdi.usb_dev, desc.iSerialNumber);
			return new ftdi_string_descriptors(mfr, description, serial);
		} finally {
			if (need_open) closeDevice(ftdi);
		}
	}

	public static void ftdi_usb_open_dev(ftdi_context ftdi, libusb_device dev)
		throws LibUsbException {
		require(ftdi);
		try {
			ftdi.usb_dev = libusb_open(dev);
			configureFtdi(ftdi, dev);
			libusb_claim_interface(ftdi.usb_dev, ftdi.iface);
			ftdi_usb_reset(ftdi);
			ftdi_set_baudrate(ftdi, BAUD_RATE);
		} catch (LibUsbException | RuntimeException e) {
			closeDevice(ftdi);
			throw e;
		}
	}

	/**
	 * Opens the first device matching the given criteria. Throws LibFtdiNotFoundException if not
	 * found.
	 */
	public static void ftdi_usb_open_find(ftdi_context ftdi, LibUsbFinder finder)
		throws LibUsbException {
		requireCtx(ftdi);
		if (!finder.findWithCallback(ftdi.usb_ctx, dev -> {
			ftdi_usb_open_dev(ftdi, dev);
			return true;
		})) throw LibUsbNotFoundException.of("Device not found, " + finder);
	}

	/**
	 * Opens the first device with a given, vendor id, product id. Throws LibFtdiNotFoundException
	 * if not found.
	 */
	public static void ftdi_usb_open(ftdi_context ftdi, int vendor, int product)
		throws LibUsbException {
		ftdi_usb_open_desc(ftdi, vendor, product, null, null);
	}

	/**
	 * Opens the first device with a given, vendor id, product id, description and serial. Throws
	 * LibFtdiNotFoundException if not found.
	 */
	public static void ftdi_usb_open_desc(ftdi_context ftdi, int vendor, int product,
		String description, String serial) throws LibUsbException {
		ftdi_usb_open_desc_index(ftdi, vendor, product, description, serial, 0);
	}

	/**
	 * Opens the device at given index with vendor id, product id, description and serial. Throws
	 * LibFtdiNotFoundException if not found.
	 */
	public static void ftdi_usb_open_desc_index(ftdi_context ftdi, int vendor, int product,
		String description, String serial, int index) throws LibUsbException {
		LibUsbFinder finder = LibUsbFinder.builder().vendor(vendor(vendor)).product(product)
			.description(description).serial(serial).index(index).build();
		ftdi_usb_open_find(ftdi, finder);
	}

	/**
	 * Opens the device at a given USB bus and device address.
	 */
	public static void ftdi_usb_open_bus_addr(ftdi_context ftdi, int bus, int addr)
		throws LibUsbException {
		LibUsbFinder finder = LibUsbFinder.builder().bus(bus).address(addr).build();
		ftdi_usb_open_find(ftdi, finder);
	}

	/**
	 * Opens the device based on descriptor formats:
	 *
	 * <pre>
	 * d:bus/addr
	 * i:vendor:product
	 * i:vendor:product:index
	 * s:vendor:product:serial
	 * </pre>
	 *
	 * Vendor and product ids may be specified in hex/octal/decimal
	 */
	public static void ftdi_usb_open_string(ftdi_context ftdi, String description)
		throws LibUsbException {
		requireCtx(ftdi);
		LibUsbFinder finder = LibFtdiUtil.finder(description);
		ftdi_usb_open_find(ftdi, finder);
	}

	public static void ftdi_usb_reset(ftdi_context ftdi) throws LibUsbException {
		requireDev(ftdi);
		controlTransferOut(ftdi, SIO_RESET_REQUEST, SIO_RESET_SIO, ftdi.index);
	}

	public static void ftdi_usb_purge_rx_buffer(ftdi_context ftdi) throws LibUsbException {
		requireDev(ftdi);
		controlTransferOut(ftdi, SIO_RESET_REQUEST, SIO_RESET_PURGE_RX, ftdi.index);
	}

	public static void ftdi_usb_purge_tx_buffer(ftdi_context ftdi) throws LibUsbException {
		requireDev(ftdi);
		controlTransferOut(ftdi, SIO_RESET_REQUEST, SIO_RESET_PURGE_TX, ftdi.index);
	}

	public static void ftdi_usb_purge_buffers(ftdi_context ftdi) throws LibUsbException {
		ftdi_usb_purge_rx_buffer(ftdi);
		ftdi_usb_purge_tx_buffer(ftdi);
	}

	public static void ftdi_set_baudrate(ftdi_context ftdi, int baudrate) throws LibUsbException {
		var baud = LibFtdiBaud.from(ftdi, baudrate);
		controlTransferOut(ftdi, SIO_SET_BAUDRATE_REQUEST, baud.value(), baud.index());
		ftdi.baudrate = baud.actualRate();
	}

	public static void ftdi_set_line_property(ftdi_context ftdi, ftdi_data_bits_type bits,
		ftdi_stop_bits_type sbit, ftdi_parity_type parity) throws LibUsbException {
		ftdi_set_line_property(ftdi, bits, sbit, parity, ftdi_break_type.BREAK_OFF);
	}

	public static void ftdi_set_line_property(ftdi_context ftdi, ftdi_data_bits_type bits,
		ftdi_stop_bits_type stopBits, ftdi_parity_type parity, ftdi_break_type breakType)
		throws LibUsbException {
		requireDev(ftdi);
		int value = breakType.value << 14 | stopBits.value << 11 | parity.value << 8 | bits.value;
		controlTransferOut(ftdi, SIO_SET_DATA_REQUEST, value, ftdi.index);
	}

	public static int ftdi_write_data(ftdi_context ftdi, ByteBuffer buffer, int len)
		throws LibUsbException {
		requireDev(ftdi);
		LibUsbUtil.require(buffer, len);
		int remaining = len;
		while (remaining > 0) {
			int size = Math.min(remaining, ftdi.writebuffer_chunksize);
			int n = libusb_bulk_transfer(ftdi.usb_dev, ftdi.in_ep, buffer, size,
				ftdi.usb_write_timeout);
			if (n <= 0) break;
			remaining -= n;
		}
		return len - remaining;
	}

	public static int ftdi_read_data(ftdi_context ftdi, ByteBuffer buffer, int size)
		throws LibUsbException {
		requireDev(ftdi);
		LibUsbUtil.require(buffer, size);
		int remaining = size;
		ByteBuffer readBuffer = ByteBuffer.allocate(readLen(ftdi, size));
		while (remaining > 0) {
			int readLen = readLen(ftdi, remaining);
			readBuffer.clear();
			readLen = libusb_bulk_transfer(ftdi.usb_dev, ftdi.out_ep, readBuffer, readLen,
				ftdi.usb_read_timeout);
			if (readLen <= READ_STATUS_BYTES) break;
			for (int i = 0;; i++) {
				int position = (i * ftdi.max_packet_size) + READ_STATUS_BYTES;
				int len = Math.min(readLen - position, ftdi.max_packet_size - READ_STATUS_BYTES);
				if (len <= 0) break;
				buffer.put(readBuffer.limit(position + len).position(position));
				remaining -= len;
			}
		}
		return size - remaining;
	}

	public static ftdi_transfer_control ftdi_write_data_submit(ftdi_context ftdi, Pointer buf,
		int size) throws LibUsbException {
		requireDev(ftdi);
		libusb_transfer transfer = libusb_alloc_transfer(0);
		try {
			ftdi_transfer_control tc = transferControl(ftdi, buf, size, transfer);
			int write_size = Math.min(size, ftdi.writebuffer_chunksize);
			libusb_fill_bulk_transfer(transfer, ftdi.usb_dev, ftdi.in_ep, buf, write_size,
				xfer -> ftdi_write_data_cb(xfer, tc), null, ftdi.usb_write_timeout);
			libusb_submit_transfer(transfer);
			tc.transfer.setAutoSynch(false); // don't read/write from now on
			return tc;
		} catch (LibUsbException | RuntimeException e) {
			libusb_free_transfer(transfer);
			throw e;
		}
	}

	public static ftdi_transfer_control ftdi_read_data_submit(ftdi_context ftdi, Pointer buf,
		int size) throws LibUsbException {
		requireDev(ftdi);
		libusb_transfer transfer = libusb_alloc_transfer(0);
		try {
			ftdi_transfer_control tc = transferControl(ftdi, buf, size, transfer);
			int read_size = readLen(ftdi, size);
			libusb_fill_bulk_transfer(transfer, ftdi.usb_dev, ftdi.out_ep, ftdi.readbuffer,
				read_size, xfer -> ftdi_read_data_cb(xfer, tc), null, ftdi.usb_read_timeout);
			libusb_submit_transfer(transfer);
			tc.transfer.setAutoSynch(false); // don't read/write from now on
			return tc;
		} catch (LibUsbException | RuntimeException e) {
			libusb_free_transfer(transfer);
			throw e;
		}
	}

	public static int ftdi_transfer_data_done(ftdi_transfer_control tc) throws LibUsbException {
		if (tc == null) return 0;
		timeval to = new timeval(0, 0);
		try {
			while (tc.completed != 0) {
				try {
					tc.completed = libusb_handle_events_timeout_completed(tc.ftdi.usb_ctx, to);
				} catch (LibUsbException | RuntimeException e) {
					if (e instanceof LibUsbException &&
						((LibUsbException) e).error == LIBUSB_ERROR_INTERRUPTED) continue;
					ftdi_transfer_data_cancel(tc, to);
					throw e;
				}
			}
			return tc.offset;
		} finally {
			freeTransfer(tc);
		}
	}

	public static void ftdi_transfer_data_cancel(ftdi_transfer_control tc, timeval to)
		throws LibUsbException {
		if (tc == null) return;
		try {
			if (tc.completed != 0 || tc.transfer == null) return;
			libusb_cancel_transfer(tc.transfer);
			waitForCompletion(tc, to);
		} finally {
			freeTransfer(tc);
		}
	}

	public static void ftdi_read_data_set_chunk_size(ftdi_context ftdi, int chunkSize) {
		ftdi.readbuffer = new Memory(chunkSize);
		ftdi.readbuffer_chunksize = chunkSize;
	}

	/**
	 * Set pin bitmode. Bitmask specified lines 1 for output, 0 for input.
	 */
	public static void ftdi_set_bitmode(ftdi_context ftdi, int bitmask, ftdi_mpsse_mode mode)
		throws LibUsbException {
		requireDev(ftdi);
		int value = mode.value << 8 | (bitmask & 0xff);
		controlTransferOut(ftdi, SIO_SET_BITMODE_REQUEST, value, ftdi.index);
		ftdi.bitbang_mode().set(mode);
		ftdi.bitbang_enabled().set(mode != BITMODE_RESET);
	}

	public static void ftdi_enable_bitbang(ftdi_context ftdi) throws LibUsbException {
		ftdi_set_bitmode(ftdi, 0xff, BITMODE_BITBANG);
	}

	public static void ftdi_disable_bitbang(ftdi_context ftdi) throws LibUsbException {
		ftdi_set_bitmode(ftdi, 0x00, BITMODE_RESET);
	}

	public static int ftdi_read_pins(ftdi_context ftdi) throws LibUsbException {
		requireDev(ftdi);
		byte[] data = controlTransferIn(ftdi, SIO_READ_PINS_REQUEST, 0, ftdi.index, 1);
		return ubyte(data[0]);
	}

	public static void ftdi_set_latency_timer(ftdi_context ftdi, int latency)
		throws LibUsbException {
		validateRange(latency, 1, 255, "latency");
		requireDev(ftdi);
		controlTransferOut(ftdi, SIO_SET_LATENCY_TIMER_REQUEST, latency, ftdi.index);
	}

	public static int ftdi_get_latency_timer(ftdi_context ftdi) throws LibUsbException {
		requireDev(ftdi);
		byte[] data = controlTransferIn(ftdi, SIO_GET_LATENCY_TIMER_REQUEST, 0, ftdi.index, 1);
		return ubyte(data[0]);
	}

	public static int ftdi_poll_modem_status(ftdi_context ftdi) throws LibUsbException {
		requireDev(ftdi);
		byte[] data = controlTransferIn(ftdi, SIO_POLL_MODEM_STATUS_REQUEST, 0, ftdi.index, 2);
		return data[1] << 8 | data[0];
	}

	public static void ftdi_set_flow_ctrl(ftdi_context ftdi, ftdi_flow_control flowCtrl)
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

	public static void ftdi_set_dtr_rts(ftdi_context ftdi, boolean dtr, boolean rts)
		throws LibUsbException {
		requireDev(ftdi);
		int val = (dtr ? SIO_SET_DTR_HIGH : SIO_SET_DTR_LOW) | //
			(rts ? SIO_SET_RTS_HIGH : SIO_SET_RTS_LOW);
		controlTransferOut(ftdi, SIO_SET_MODEM_CTRL_REQUEST, val, ftdi.index);
	}

	public static void ftdi_set_event_char(ftdi_context ftdi, char eventch, boolean enable)
		throws LibUsbException {
		requireDev(ftdi);
		int val = eventch & 0xff;
		if (enable) val |= 1 << 8;
		controlTransferOut(ftdi, SIO_SET_EVENT_CHAR_REQUEST, val, ftdi.index);
	}

	public static void ftdi_set_error_char(ftdi_context ftdi, char errorch, boolean enable)
		throws LibUsbException {
		requireDev(ftdi);
		int val = errorch & 0xff;
		if (enable) val |= 1 << 8;
		controlTransferOut(ftdi, SIO_SET_ERROR_CHAR_REQUEST, val, ftdi.index);
	}

	private static void configureFtdi(ftdi_context ftdi, libusb_device dev) throws LibUsbException {
		libusb_device_descriptor desc = libusb_get_device_descriptor(dev);
		ftdi.type().set(guessChipType(desc.bcdDevice, desc.iSerialNumber));
		libusb_config_descriptor config0 = libusb_get_config_descriptor(dev, 0);
		int cfg0 = config0.bConfigurationValue;
		ftdi.max_packet_size = maxPacketSize(ftdi, config0, desc);
		libusb_free_config_descriptor(config0);
		autoDetach(ftdi);
		setConfig(desc, ftdi.usb_dev, cfg0);
	}

	private static int maxPacketSize(ftdi_context ftdi, libusb_config_descriptor config,
		libusb_device_descriptor desc) {
		int packet_size = ftdi_chip_type.isHType(ftdi.type().get()) ? 512 : 64;
		if (desc.bNumConfigurations == 0 || ftdi.iface >= config.bNumInterfaces) return packet_size;
		libusb_interface iface = config.interfaces()[ftdi.iface];
		if (iface.num_altsetting == 0) return packet_size;
		libusb_interface_descriptor descriptor = iface.altsettings()[0];
		if (descriptor.bNumEndpoints == 0) return packet_size;
		return descriptor.endpoints()[0].wMaxPacketSize;
	}

	private static int autoDetach(ftdi_context ftdi) {
		if (ftdi.module_detach_mode().get() != AUTO_DETACH_SIO_MODULE) return 0;
		return capture(() -> libusb_detach_kernel_driver(ftdi.usb_dev, ftdi.iface));
	}

	private static void setConfig(libusb_device_descriptor desc, libusb_device_handle dev, int cfg)
		throws LibUsbException {
		int current = libusb_get_configuration(dev);
		if (desc.bNumConfigurations > 0 && current != cfg) libusb_set_configuration(dev, cfg);
	}

	private static void closeDevice(ftdi_context ftdi) throws LibUsbException {
		if (ftdi == null) return;
		libusb_close(ftdi.usb_dev);
		ftdi.usb_dev = null;
		if (ftdi.eeprom != null) ftdi.eeprom.initialized_for_connected_device = 0;
	}

	private static ftdi_transfer_control transferControl(ftdi_context ftdi, Pointer buf, int size,
		libusb_transfer transfer) {
		ftdi_transfer_control tc = new ftdi_transfer_control();
		tc.ftdi = ftdi;
		tc.completed = 0;
		tc.buf = buf;
		tc.size = size;
		tc.offset = 0;
		tc.transfer = transfer;
		return tc;
	}

	/**
	 * Callback function for ftdi_write_data_submit. Checks if transfer is complete.
	 */
	private static void ftdi_write_data_cb(libusb_transfer transfer, ftdi_transfer_control tc) {
		tc.offset += transfer.actual_length;
		if (tc.offset >= tc.size) tc.completed = 1;
		else if (transfer.status() == LIBUSB_TRANSFER_CANCELLED)
			tc.completed().set(LIBUSB_TRANSFER_CANCELLED);
		else writeMoreData(transfer, tc);
	}

	private static void writeMoreData(libusb_transfer transfer, ftdi_transfer_control tc) {
		ftdi_context ftdi = tc.ftdi;
		try {
			transfer.length = Math.min(ftdi.writebuffer_chunksize, tc.size - tc.offset);
			transfer.buffer = tc.buf.share(tc.offset);
			libusb_submit_transfer(transfer);
		} catch (LibUsbException | RuntimeException e) {
			logger.catching(e);
			tc.completed = 1;
		}
	}

	/**
	 * Callback function for ftdi_read_data_submit. Copies to buffer and checks if transfer is
	 * complete. If not, a new transfer is submitted.
	 */
	private static void ftdi_read_data_cb(libusb_transfer transfer, ftdi_transfer_control tc) {
		readData(transfer, tc);
		if (tc.offset >= tc.size) tc.completed = 1;
		else if (transfer.status() == LIBUSB_TRANSFER_CANCELLED)
			tc.completed().set(LIBUSB_TRANSFER_CANCELLED);
		else readMoreData(transfer, tc);
	}

	private static void readData(libusb_transfer transfer, ftdi_transfer_control tc) {
		ftdi_context ftdi = tc.ftdi;
		int dataLen = Math.min(dataLen(ftdi, transfer.actual_length), tc.size - tc.offset);
		byte[] buffer = ftdi.readbuffer.getByteArray(0, transfer.actual_length);
		for (int i = 0;; i++) {
			int toOffset = i * (ftdi.max_packet_size - READ_STATUS_BYTES);
			int fromOffset = (i * ftdi.max_packet_size) + READ_STATUS_BYTES;
			int len = Math.min(dataLen - toOffset, ftdi.max_packet_size - READ_STATUS_BYTES);
			if (len <= 0) break;
			System.arraycopy(buffer, fromOffset, buffer, toOffset, len);
		}
		tc.buf.write(tc.offset, buffer, 0, dataLen);
		tc.offset += dataLen;
	}

	/**
	 * Continue reading with a new transfer. If the transfer fails to start, an error is logged and
	 * the transfer is marked as complete.
	 */
	private static void readMoreData(libusb_transfer transfer, ftdi_transfer_control tc) {
		ftdi_context ftdi = tc.ftdi;
		try {
			transfer.length =
				Math.min(ftdi.readbuffer_chunksize, readLen(ftdi, tc.size - tc.offset));
			libusb_submit_transfer(transfer);
		} catch (LibUsbException | RuntimeException e) {
			logger.catching(e);
			tc.completed = 1;
		}
	}

	/**
	 * Free libusb_transfer reference without writing fields to memory.
	 */
	private static void freeTransfer(ftdi_transfer_control tc) throws LibUsbException {
		if (tc == null || tc.transfer == null) return;
		libusb_free_transfer(tc.transfer);
		tc.transfer = null;
	}

	/**
	 * Calculates the number of read bytes needed to extract size bytes, removing packet headers, up
	 * to maximum chunk size
	 */
	private static int readLen(ftdi_context ftdi, int dataSize) {
		int packets = dataSize / (ftdi.max_packet_size - READ_STATUS_BYTES);
		int rem = dataSize % (ftdi.max_packet_size - READ_STATUS_BYTES);
		int total = (packets * ftdi.max_packet_size) + (rem > 0 ? READ_STATUS_BYTES + rem : 0);
		return Math.min(total, ftdi.readbuffer_chunksize);
	}

	/**
	 * Calculates the number of actual bytes that can be extracted from read bytes, removing packet
	 * headers
	 */
	private static int dataLen(ftdi_context ftdi, int readSize) {
		int packets = readSize / ftdi.max_packet_size;
		int rem = readSize % ftdi.max_packet_size;
		return (packets * (ftdi.max_packet_size - READ_STATUS_BYTES)) +
			Math.max(0, rem - READ_STATUS_BYTES);
	}

	private static void waitForCompletion(ftdi_transfer_control tc, timeval to)
		throws LibUsbException {
		if (tc == null || tc.ftdi == null || tc.ftdi.usb_ctx == null) return;
		while (tc.completed == 0)
			tc.completed = libusb_handle_events_timeout_completed(tc.ftdi.usb_ctx, to);
	}

	private static boolean controlTransferOut(ftdi_context ftdi, ftdi_request_type request,
		int value, int index) throws LibUsbException {
		if (ftdi.usb_dev == null) return false;
		libusb_control_transfer(ftdi.usb_dev, FTDI_DEVICE_OUT_REQTYPE, request.value, value, index,
			ftdi.usb_write_timeout);
		return true;
	}

	private static byte[] controlTransferIn(ftdi_context ftdi, ftdi_request_type request, int value,
		int index, int length) throws LibUsbException {
		if (ftdi.usb_dev == null) return null;
		return libusb_control_transfer(ftdi.usb_dev, FTDI_DEVICE_IN_REQTYPE, request.value, value,
			index, length, ftdi.usb_read_timeout);
	}

}
