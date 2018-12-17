package ceri.serial.ftdi.jna;

import static ceri.common.collection.ImmutableUtil.enumSet;
import static ceri.common.validation.ValidationUtil.validateRange;
import static ceri.serial.ftdi.jna.LibFtdi.ftdi_chip_type.TYPE_2232C;
import static ceri.serial.ftdi.jna.LibFtdi.ftdi_chip_type.TYPE_2232H;
import static ceri.serial.ftdi.jna.LibFtdi.ftdi_chip_type.TYPE_230X;
import static ceri.serial.ftdi.jna.LibFtdi.ftdi_chip_type.TYPE_232H;
import static ceri.serial.ftdi.jna.LibFtdi.ftdi_chip_type.TYPE_4232H;
import static ceri.serial.ftdi.jna.LibFtdi.ftdi_chip_type.TYPE_AM;
import static ceri.serial.ftdi.jna.LibFtdi.ftdi_chip_type.TYPE_BM;
import static ceri.serial.ftdi.jna.LibFtdi.ftdi_chip_type.TYPE_R;
import static ceri.serial.ftdi.jna.LibFtdi.ftdi_interface.INTERFACE_A;
import static ceri.serial.ftdi.jna.LibFtdi.ftdi_interface.INTERFACE_ANY;
import static ceri.serial.ftdi.jna.LibFtdi.ftdi_interface.INTERFACE_B;
import static ceri.serial.ftdi.jna.LibFtdi.ftdi_interface.INTERFACE_C;
import static ceri.serial.ftdi.jna.LibFtdi.ftdi_interface.INTERFACE_D;
import static ceri.serial.ftdi.jna.LibFtdi.ftdi_module_detach_mode.AUTO_DETACH_SIO_MODULE;
import static ceri.serial.ftdi.jna.LibFtdi.ftdi_mpsse_mode.BITMODE_BITBANG;
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
import static ceri.serial.jna.CException.capture;
import static ceri.serial.jna.JnaUtil.ubyte;
import static ceri.serial.jna.JnaUtil.ushort;
import static ceri.serial.libusb.jna.LibUsb.libusb_alloc_transfer;
import static ceri.serial.libusb.jna.LibUsb.libusb_bulk_transfer;
import static ceri.serial.libusb.jna.LibUsb.libusb_cancel_transfer;
import static ceri.serial.libusb.jna.LibUsb.libusb_claim_interface;
import static ceri.serial.libusb.jna.LibUsb.libusb_close;
import static ceri.serial.libusb.jna.LibUsb.libusb_control_transfer;
import static ceri.serial.libusb.jna.LibUsb.libusb_detach_kernel_driver;
import static ceri.serial.libusb.jna.LibUsb.libusb_endpoint_address;
import static ceri.serial.libusb.jna.LibUsb.libusb_exit;
import static ceri.serial.libusb.jna.LibUsb.libusb_fill_bulk_transfer;
import static ceri.serial.libusb.jna.LibUsb.libusb_free_config_descriptor;
import static ceri.serial.libusb.jna.LibUsb.libusb_free_device_list;
import static ceri.serial.libusb.jna.LibUsb.libusb_free_transfer;
import static ceri.serial.libusb.jna.LibUsb.libusb_get_bus_number;
import static ceri.serial.libusb.jna.LibUsb.libusb_get_config_descriptor;
import static ceri.serial.libusb.jna.LibUsb.libusb_get_configuration;
import static ceri.serial.libusb.jna.LibUsb.libusb_get_device_address;
import static ceri.serial.libusb.jna.LibUsb.libusb_get_device_descriptor;
import static ceri.serial.libusb.jna.LibUsb.libusb_get_device_list;
import static ceri.serial.libusb.jna.LibUsb.libusb_get_string_descriptor_ascii;
import static ceri.serial.libusb.jna.LibUsb.libusb_handle_events_timeout_completed;
import static ceri.serial.libusb.jna.LibUsb.libusb_init;
import static ceri.serial.libusb.jna.LibUsb.libusb_open;
import static ceri.serial.libusb.jna.LibUsb.libusb_ref_device;
import static ceri.serial.libusb.jna.LibUsb.libusb_request_type;
import static ceri.serial.libusb.jna.LibUsb.libusb_set_configuration;
import static ceri.serial.libusb.jna.LibUsb.libusb_submit_transfer;
import static ceri.serial.libusb.jna.LibUsb.libusb_unref_device;
import static ceri.serial.libusb.jna.LibUsb.libusb_endpoint_direction.LIBUSB_ENDPOINT_IN;
import static ceri.serial.libusb.jna.LibUsb.libusb_endpoint_direction.LIBUSB_ENDPOINT_OUT;
import static ceri.serial.libusb.jna.LibUsb.libusb_error.LIBUSB_ERROR_INTERRUPTED;
import static ceri.serial.libusb.jna.LibUsb.libusb_error.LIBUSB_ERROR_INVALID_PARAM;
import static ceri.serial.libusb.jna.LibUsb.libusb_error.LIBUSB_ERROR_NOT_SUPPORTED;
import static ceri.serial.libusb.jna.LibUsb.libusb_request_recipient.LIBUSB_RECIPIENT_DEVICE;
import static ceri.serial.libusb.jna.LibUsb.libusb_request_type.LIBUSB_REQUEST_TYPE_VENDOR;
import static ceri.serial.libusb.jna.LibUsb.libusb_transfer_status.LIBUSB_TRANSFER_CANCELLED;
import static java.lang.String.format;
import static java.util.regex.Pattern.compile;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.sun.jna.Memory;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import ceri.common.data.BooleanAccessor;
import ceri.common.data.FieldTranscoder;
import ceri.common.data.IntAccessor;
import ceri.common.data.TypeTranscoder;
import ceri.common.text.RegexUtil;
import ceri.common.util.PrimitiveUtil;
import ceri.serial.jna.JnaUtil;
import ceri.serial.jna.Struct;
import ceri.serial.jna.Time.timeval;
import ceri.serial.libusb.jna.LibUsb.libusb_config_descriptor;
import ceri.serial.libusb.jna.LibUsb.libusb_context;
import ceri.serial.libusb.jna.LibUsb.libusb_device;
import ceri.serial.libusb.jna.LibUsb.libusb_device_descriptor;
import ceri.serial.libusb.jna.LibUsb.libusb_device_handle;
import ceri.serial.libusb.jna.LibUsb.libusb_interface;
import ceri.serial.libusb.jna.LibUsb.libusb_interface_descriptor;
import ceri.serial.libusb.jna.LibUsb.libusb_transfer;
import ceri.serial.libusb.jna.LibUsb.libusb_transfer_cb_fn;
import ceri.serial.libusb.jna.LibUsb.libusb_transfer_status;
import ceri.serial.libusb.jna.LibUsbException;

/**
 * Implementation of libftdi.
 * 
 * TODO: testing, including async transfers; implement eeprom functionality
 */
public class LibFtdi {
	private static final Logger logger = LogManager.getLogger();
	private static final Pattern FIND_BY_DEVICE_NODE = compile("d:(\\d+)/(\\d+)");
	private static final Pattern FIND_BY_VENDOR_INDEX = compile("i:(\\w+):(\\w+)(?::(\\d+))?");
	private static final Pattern FIND_BY_VENDOR_SERIAL = compile("s:(\\w+):(\\w+):(\\w+)");
	private static final byte FTDI_DEVICE_OUT_REQTYPE = libusb_request_type( //
		LIBUSB_RECIPIENT_DEVICE, LIBUSB_REQUEST_TYPE_VENDOR, LIBUSB_ENDPOINT_OUT);
	private static final byte FTDI_DEVICE_IN_REQTYPE = libusb_request_type( //
		LIBUSB_RECIPIENT_DEVICE, LIBUSB_REQUEST_TYPE_VENDOR, LIBUSB_ENDPOINT_IN);
	private static final libusb_transfer_cb_fn ftdi_write_data_cb = LibFtdi::ftdi_write_data_cb;
	private static final libusb_transfer_cb_fn ftdi_read_data_cb = LibFtdi::ftdi_read_data_cb;
	static final int READ_STATUS_BYTES = 2;
	private static final int H_CLK = 120000000;
	private static final int C_CLK = 48000000;
	private static final int FTDI_MAX_EEPROM_SIZE = 256;
	// eeprom not yet supported
	// private static final int MAX_POWER_MILLIAMP_PER_UNIT = 2;

	public static final int FTDI_VENDOR_ID = 0x403;

	// SIO_RESET_REQUEST values
	private static final int SIO_RESET_SIO = 0;
	private static final int SIO_RESET_PURGE_RX = 1;
	private static final int SIO_RESET_PURGE_TX = 2;
	// SIO_SET_MODEM_CTRL_REQUEST values
	private static final int SIO_SET_DTR_HIGH = 0x101;
	private static final int SIO_SET_DTR_LOW = 0x100;
	private static final int SIO_SET_RTS_HIGH = 0x202;
	private static final int SIO_SET_RTS_LOW = 0x200;

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

		public static final TypeTranscoder.Single<ftdi_request_type> xcoder =
			TypeTranscoder.single(t -> t.value, ftdi_request_type.class);
		public final int value;

		private ftdi_request_type(int value) {
			this.value = value;
		}
	}

	public static enum ftdi_chip_type {
		TYPE_AM(0),
		TYPE_BM(1),
		TYPE_2232C(2),
		TYPE_R(3),
		TYPE_2232H(4),
		TYPE_4232H(5),
		TYPE_232H(6),
		TYPE_230X(7);

		public static final TypeTranscoder.Single<ftdi_chip_type> xcoder =
			TypeTranscoder.single(t -> t.value, ftdi_chip_type.class);
		public static final Set<ftdi_chip_type> H_TYPES =
			enumSet(TYPE_2232H, TYPE_4232H, TYPE_232H);
		public static final Set<ftdi_chip_type> SYNC_FIFO_TYPES = enumSet(TYPE_2232H, TYPE_232H);
		public final int value;

		private ftdi_chip_type(int value) {
			this.value = value;
		}

		public boolean isHType() {
			return H_TYPES.contains(this);
		}

		public boolean isSyncFifoType() {
			return SYNC_FIFO_TYPES.contains(this);
		}
	}

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

		public static final TypeTranscoder.Single<ftdi_mpsse_mode> xcoder =
			TypeTranscoder.single(t -> t.value, ftdi_mpsse_mode.class);
		public final int value;

		private ftdi_mpsse_mode(int value) {
			this.value = value;
		}
	}

	public static enum ftdi_interface {
		INTERFACE_ANY(0),
		INTERFACE_A(1), // 0, 1, 0x02, 0x81
		INTERFACE_B(2), // 1, 2, 0x04, 0x83
		INTERFACE_C(3), // 2, 3, 0x06, 0x85
		INTERFACE_D(4); // 3, 4, 0x08, 0x87

		public static final TypeTranscoder.Single<ftdi_interface> xcoder =
			TypeTranscoder.single(t -> t.value, ftdi_interface.class);
		public final int value;

		private ftdi_interface(int value) {
			this.value = value;
		}
	}

	public enum ftdi_module_detach_mode {
		AUTO_DETACH_SIO_MODULE(0),
		DONT_DETACH_SIO_MODULE(1),
		AUTO_DETACH_REATACH_SIO_MODULE(2);

		public static final TypeTranscoder.Single<ftdi_module_detach_mode> xcoder =
			TypeTranscoder.single(t -> t.value, ftdi_module_detach_mode.class);
		public final int value;

		private ftdi_module_detach_mode(int value) {
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

	public static enum ftdi_flow_control {
		SIO_DISABLE_FLOW_CTRL(0x0000),
		SIO_RTS_CTS_HS(0x0100),
		SIO_DTR_DSR_HS(0x0200),
		SIO_XON_XOFF_HS(0x0400);

		public static final TypeTranscoder.Single<ftdi_flow_control> xcoder =
			TypeTranscoder.single(t -> t.value, ftdi_flow_control.class);
		public final int value;

		private ftdi_flow_control(int value) {
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
			IntAccessor.typedByte(t -> t.bitbang_mode, (t, i) -> t.bitbang_mode = i);
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
		// public int readbuffer_offset; // not required
		// public int readbuffer_remaining; // not required
		public int readbuffer_chunksize;
		public int writebuffer_chunksize;
		public int max_packet_size;
		public int iface;
		public int index; // ftdi_interface (confusing name!)
		public int in_ep;
		public int out_ep;
		public byte bitbang_mode; // ftdi_bit_mode
		public ftdi_eeprom.ByReference eeprom;
		// TODO: remove
		public String error_str;
		public int module_detach_mode; // ftdi_module_detach_mode

		public ftdi_context() {}

		public ftdi_context(Pointer p) {
			super(p);
		}

		public FieldTranscoder.Single<ftdi_chip_type> type() {
			return ftdi_chip_type.xcoder.field(type_accessor.from(this));
		}

		public BooleanAccessor bitbang_enabled() {
			return bitbang_enabled_accessor.from(this);
		}

		public FieldTranscoder.Single<ftdi_interface> index() {
			return ftdi_interface.xcoder.field(index_accessor.from(this));
		}

		public FieldTranscoder.Single<ftdi_mpsse_mode> bitbang_mode() {
			return ftdi_mpsse_mode.xcoder.field(bitbang_mode_accessor.from(this));
		}

		public FieldTranscoder.Single<ftdi_module_detach_mode> module_detach_mode() {
			return ftdi_module_detach_mode.xcoder.field(module_detach_mode_accessor.from(this));
		}

		@Override
		protected List<String> getFieldOrder() {
			return FIELDS;
		}

	}

	public static class ftdi_transfer_control extends Struct {
		private static final List<String> FIELDS = List.of( //
			"completed", "buf", "size", "offset", "ftdi", "transfer");
		private static final IntAccessor.Typed<ftdi_transfer_control> completed_accessor =
			IntAccessor.typed(t -> t.completed, (t, i) -> t.completed = i);

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

		public FieldTranscoder.Single<libusb_transfer_status> completed() {
			return libusb_transfer_status.xcoder.field(completed_accessor.from(this));
		}

		public byte[] buf() {
			return JnaUtil.byteArray(buf, offset, size);
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

	public static class ftdi_device_list {
		public ftdi_device_list next;
		public libusb_device dev;
	}

	private static void ftdi_usb_close_internal(ftdi_context ftdi) {
		if (ftdi == null) return;
		libusb_close(ftdi.usb_dev);
		ftdi.usb_dev = null;
		if (ftdi.eeprom != null) ftdi.eeprom.initialized_for_connected_device = 0;
	}

	public static void ftdi_init(ftdi_context ftdi) throws LibUsbException {
		try {
			ftdi.usb_ctx = null;
			ftdi.usb_dev = null;
			ftdi.usb_read_timeout = 5000;
			ftdi.usb_write_timeout = 5000;

			ftdi.type().set(TYPE_BM);
			ftdi.baudrate = -1;
			ftdi.bitbang_enabled().set(false);

			ftdi.readbuffer = null;
			ftdi.writebuffer_chunksize = 4096;
			ftdi.max_packet_size = 0;
			ftdi.error_str = null;
			ftdi.module_detach_mode().set(AUTO_DETACH_SIO_MODULE);

			ftdi.usb_ctx = libusb_init();
			ftdi_set_interface(ftdi, INTERFACE_ANY);
			ftdi.bitbang_mode().set(BITMODE_BITBANG);
			ftdi.eeprom = new ftdi_eeprom.ByReference();
			ftdi_read_data_set_chunk_size(ftdi, 4096);
		} catch (LibUsbException | RuntimeException e) {
			ftdi_free(ftdi);
			throw e;
		}
	}

	public static ftdi_context ftdi_new() throws LibUsbException {
		ftdi_context ftdi = new ftdi_context();
		ftdi_init(ftdi);
		return ftdi;
	}

	public static void ftdi_set_interface(ftdi_context ftdi, ftdi_interface iface)
		throws LibUsbException {
		require(ftdi);
		if (iface == INTERFACE_ANY) iface = INTERFACE_A;
		if (ftdi.usb_dev != null && ftdi.index != iface.value)
			throw new LibUsbException("Interface cannot be changed on an open device",
				LIBUSB_ERROR_NOT_SUPPORTED);

		switch (iface) {
		case INTERFACE_ANY:
		case INTERFACE_A:
			ftdi.iface = 0;
			ftdi.index().set(INTERFACE_A);
			ftdi.in_ep = libusb_endpoint_address(2, LIBUSB_ENDPOINT_IN); // 0x02
			ftdi.out_ep = libusb_endpoint_address(1, LIBUSB_ENDPOINT_OUT); // 0x81
			break;
		case INTERFACE_B:
			ftdi.iface = 1;
			ftdi.index().set(INTERFACE_B);
			ftdi.in_ep = libusb_endpoint_address(4, LIBUSB_ENDPOINT_IN); // 0x04
			ftdi.out_ep = libusb_endpoint_address(3, LIBUSB_ENDPOINT_OUT); // 0x83
			break;
		case INTERFACE_C:
			ftdi.iface = 2;
			ftdi.index().set(INTERFACE_C);
			ftdi.in_ep = libusb_endpoint_address(6, LIBUSB_ENDPOINT_IN); // 0x06
			ftdi.out_ep = libusb_endpoint_address(5, LIBUSB_ENDPOINT_OUT); // 0x85
			break;
		case INTERFACE_D:
			ftdi.iface = 3;
			ftdi.index().set(INTERFACE_D);
			ftdi.in_ep = libusb_endpoint_address(8, LIBUSB_ENDPOINT_IN); // 0x08
			ftdi.out_ep = libusb_endpoint_address(7, LIBUSB_ENDPOINT_OUT); // 0x87
			break;
		default:
			throw new LibUsbException("Unknown interface: " + iface, LIBUSB_ERROR_INVALID_PARAM);
		}
	}

	public static void ftdi_free(ftdi_context ftdi) {
		if (ftdi == null) return;
		ftdi_usb_close_internal(ftdi);
		ftdi.readbuffer = null;
		ftdi.eeprom = null;
		libusb_exit(ftdi.usb_ctx);
		ftdi.usb_ctx = null;
	}

	public static void ftdi_set_usb_dev(ftdi_context ftdi, libusb_device_handle usb) {
		if (ftdi != null) ftdi.usb_dev = usb;
	}

	/**
	 * Finds all ftdi devices with given VID:PID on the usb bus. Creates a new ftdi_device_list
	 * which needs to be deallocated by ftdi_list_free() after use. With VID:PID 0:0, search for the
	 * default devices with vendor id 0x403
	 */
	public static List<libusb_device> ftdi_usb_find_all(ftdi_context ftdi, int vendor, int product)
		throws LibUsbException {
		List<libusb_device> devs = new ArrayList<>();

		libusb_device.ByReference list = libusb_get_device_list(ftdi.usb_ctx);
		try {
			for (libusb_device dev : list.typedArray()) {
				libusb_device_descriptor desc = libusb_get_device_descriptor(dev);
				if (vendor != 0 && vendor != ushort(desc.idVendor)) continue;
				if (product != 0 && product != ushort(desc.idProduct)) continue;
				if (vendor == 0 && FTDI_VENDOR_ID != ushort(desc.idVendor)) continue;
				// Originally only checks product for 0x6001, 0x6010, 0x6011, 0x6014, 0x6015
				libusb_ref_device(dev);
				devs.add(dev);
			}
			return devs;
		} catch (LibUsbException | RuntimeException e) {
			ftdi_list_free(devs);
			throw e;
		} finally {
			libusb_free_device_list(list); // ,1?
		}
	}

	public static void ftdi_list_free(List<libusb_device> devlist) {
		for (libusb_device dev : devlist)
			libusb_unref_device(dev);
	}

	/**
	 * Return device ID strings from the usb device. Use this function only in combination with
	 * ftdi_usb_find_all() as it closes the internal "usb_dev" after use.
	 */
	public static ftdi_string_descriptors ftdi_usb_get_strings(ftdi_context ftdi, libusb_device dev)
		throws LibUsbException {
		require(ftdi);
		require(dev, "USB device");
		if (ftdi.usb_dev == null) ftdi.usb_dev = libusb_open(dev);
		try {
			ftdi_string_descriptors descriptors = ftdi_usb_get_strings2(ftdi, dev);
			return descriptors;
		} finally {
			ftdi_usb_close_internal(ftdi);
		}
	}

	/**
	 * Return device ID strings from the usb device. The old function ftdi_usb_get_strings() always
	 * closes the device. This version only closes the device if it was opened by it.
	 */
	public static ftdi_string_descriptors ftdi_usb_get_strings2(ftdi_context ftdi,
		libusb_device dev) throws LibUsbException {
		require(ftdi);
		require(dev, "USB device");

		boolean need_open = ftdi.usb_dev == null;
		if (need_open) ftdi.usb_dev = libusb_open(dev);
		try {
			libusb_device_descriptor desc = libusb_get_device_descriptor(dev);
			String manufacturer =
				libusb_get_string_descriptor_ascii(ftdi.usb_dev, desc.iManufacturer);
			String description = libusb_get_string_descriptor_ascii(ftdi.usb_dev, desc.iProduct);
			String serial = libusb_get_string_descriptor_ascii(ftdi.usb_dev, desc.iSerialNumber);
			return new ftdi_string_descriptors(manufacturer, description, serial);
		} finally {
			if (need_open) ftdi_usb_close_internal(ftdi);
		}
	}

	private static int _ftdi_determine_max_packet_size(ftdi_context ftdi, libusb_device dev)
		throws LibUsbException {
		if (ftdi == null || dev == null) return 64;
		int packet_size = ftdi.type().get().isHType() ? 512 : 64;

		libusb_device_descriptor desc = libusb_get_device_descriptor(dev);
		libusb_config_descriptor config0 = libusb_get_config_descriptor(dev, 0);
		try {
			if (desc.bNumConfigurations == 0 && ftdi.iface < config0.bNumInterfaces) {
				libusb_interface iface = config0.interfaces()[ftdi.iface];
				if (iface.num_altsetting > 0) {
					libusb_interface_descriptor descriptor = iface.altsettings()[0];
					if (descriptor.bNumEndpoints > 0)
						packet_size = descriptor.endpoints()[0].wMaxPacketSize;
				}
			}
		} finally {
			libusb_free_config_descriptor(config0);
		}
		return packet_size;
	}

	public static void ftdi_usb_open_dev(ftdi_context ftdi, libusb_device dev)
		throws LibUsbException {
		require(ftdi);
		try {
			ftdi.usb_dev = libusb_open(dev);
			libusb_device_descriptor desc = libusb_get_device_descriptor(dev);
			libusb_config_descriptor config0 = libusb_get_config_descriptor(dev, 0);
			int cfg0 = config0.bConfigurationValue;
			libusb_free_config_descriptor(config0);

			if (ftdi.module_detach_mode().get() == AUTO_DETACH_SIO_MODULE)
				capture(() -> libusb_detach_kernel_driver(ftdi.usb_dev, ftdi.iface));

			int cfg = libusb_get_configuration(ftdi.usb_dev);
			if (desc.bNumConfigurations > 0 && cfg != cfg0)
				libusb_set_configuration(ftdi.usb_dev, cfg0);
			libusb_claim_interface(ftdi.usb_dev, ftdi.iface);
			ftdi_usb_reset(ftdi);
			ftdi.type().set(guessChipType(desc.bcdDevice, desc.iSerialNumber));
			ftdi.max_packet_size = _ftdi_determine_max_packet_size(ftdi, dev);
			ftdi_set_baudrate(ftdi, 9600);
		} catch (LibUsbException | RuntimeException e) {
			ftdi_usb_close_internal(ftdi);
			throw e;
		}
	}

	private static ftdi_chip_type guessChipType(int device, int serial) {
		switch (device & 0xffff) {
		case 0x0200:
			return serial == 0 ? TYPE_BM : TYPE_AM;
		case 0x0400:
			return TYPE_BM;
		case 0x0500:
			return TYPE_2232C;
		case 0x0600:
			return TYPE_R;
		case 0x0700:
			return TYPE_2232H;
		case 0x0800:
			return TYPE_4232H;
		case 0x0900:
			return TYPE_232H;
		case 0x1000:
			return TYPE_230X;
		default:
			return TYPE_BM;
		}
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
		requireCtx(ftdi);
		libusb_device.ByReference devs = libusb_get_device_list(ftdi.usb_ctx);
		try {
			for (libusb_device dev : devs.typedArray()) {
				libusb_device_descriptor desc = libusb_get_device_descriptor(dev);
				if (ushort(desc.idVendor) != vendor) continue;
				if (ushort(desc.idProduct) != product) continue;
				libusb_device_handle usb_dev = libusb_open(dev);
				try {
					if (!matchesDesc(usb_dev, description, desc.iProduct)) continue;
					if (!matchesDesc(usb_dev, serial, desc.iSerialNumber)) continue;
					if (index-- > 0) continue;
				} finally {
					libusb_close(usb_dev);
				}
				ftdi_usb_open_dev(ftdi, dev);
				return;
			}
		} finally {
			libusb_free_device_list(devs); // ,1 ?
		}
		throw new LibFtdiNotFoundException(String.format(
			"Device not found: vendor=0x%04x product=0x%04x description=%s serial=%s index=%d",
			vendor, product, description, serial, index));
	}

	static boolean matchesDesc(libusb_device_handle usb_dev, String expected, int desc_index)
		throws LibUsbException {
		if (expected == null || expected.isEmpty()) return true;
		String descriptor = libusb_get_string_descriptor_ascii(usb_dev, desc_index);
		return expected.equals(descriptor);
	}

	/**
	 * Opens the device at a given USB bus and device address.
	 */
	public static void ftdi_usb_open_bus_addr(ftdi_context ftdi, int bus, int addr)
		throws LibUsbException {
		requireCtx(ftdi);
		libusb_device.ByReference devs = libusb_get_device_list(ftdi.usb_ctx);
		try {
			for (libusb_device dev : devs.typedArray()) {
				if (bus != ubyte(libusb_get_bus_number(dev))) continue;
				if (addr != ubyte(libusb_get_device_address(dev))) continue;
				ftdi_usb_open_dev(ftdi, dev);
				return;
			}
		} finally {
			libusb_free_device_list(devs); // ,1 ?
		}
		throw new LibFtdiNotFoundException(
			String.format("Device not found: bus_number=0x%02x address=0x%02x", bus, addr));
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
		Matcher m = RegexUtil.matched(FIND_BY_DEVICE_NODE, description);
		if (m != null) {
			int bus = Integer.parseInt(m.group(1));
			int addr = Integer.parseInt(m.group(2));
			ftdi_usb_open_bus_addr(ftdi, bus, addr);
			return;
		}
		m = RegexUtil.matched(FIND_BY_VENDOR_INDEX, description);
		if (m != null) {
			int vendor = Integer.decode(m.group(1));
			int product = Integer.decode(m.group(2));
			int index = PrimitiveUtil.valueOf(m.group(3), 0);
			ftdi_usb_open_desc_index(ftdi, vendor, product, null, null, index);
			return;
		}
		m = RegexUtil.matched(FIND_BY_VENDOR_SERIAL, description);
		if (m != null) {
			int vendor = Integer.decode(m.group(1));
			int product = Integer.decode(m.group(2));
			String serial = m.group(3);
			ftdi_usb_open_desc(ftdi, vendor, product, null, serial);
			return;
		}
		throw new LibUsbException("Invalid description format: " + description,
			LIBUSB_ERROR_INVALID_PARAM);
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

	private static int ftdi_to_clkbits_AM(int baudrate, long[] encoded_divisor_return) {
		byte[] frac_code = { 0, 3, 2, 4, 1, 5, 6, 7 };
		byte[] am_adjust_up = { 0, 0, 0, 1, 0, 3, 2, 1 };
		byte[] am_adjust_dn = { 0, 0, 0, 1, 0, 1, 2, 3 };
		int divisor = 24000000 / baudrate;
		divisor -= am_adjust_dn[divisor & 7];

		int best_divisor = 0;
		int best_baud = 0;
		int best_baud_diff = 0;
		for (int i = 0; i < 2; i++) {
			int try_divisor = divisor + i;
			int baud_estimate;
			int baud_diff;

			if (try_divisor <= 8) try_divisor = 8;
			else if (divisor < 16) try_divisor = 16;
			else {
				try_divisor += am_adjust_up[try_divisor & 7];
				if (try_divisor > 0x1FFF8) try_divisor = 0x1FFF8;
			}
			baud_estimate = (24000000 + (try_divisor / 2)) / try_divisor;
			if (baud_estimate < baudrate) baud_diff = baudrate - baud_estimate;
			else baud_diff = baud_estimate - baudrate;
			if (i == 0 || baud_diff < best_baud_diff) {
				best_divisor = try_divisor;
				best_baud = baud_estimate;
				best_baud_diff = baud_diff;
				if (baud_diff == 0) break;
			}
		}
		long encoded_divisor = (best_divisor >> 3) | (frac_code[best_divisor & 7] << 14);
		if (encoded_divisor == 1) encoded_divisor = 0; // 3000000 baud
		else if (encoded_divisor == 0x4001) encoded_divisor = 1; // 2000000 baud (BM only)
		encoded_divisor_return[0] = encoded_divisor;
		return best_baud;
	}

	private static int ftdi_to_clkbits(int baudrate, int clk, int clk_div, long[] encoded_divisor) {
		byte[] frac_code = { 0, 3, 2, 4, 1, 5, 6, 7 };
		int best_baud = 0;
		if (baudrate >= clk / clk_div) {
			encoded_divisor[0] = 0;
			best_baud = clk / clk_div;
		} else if (baudrate >= clk / (clk_div + clk_div / 2)) {
			encoded_divisor[0] = 1;
			best_baud = clk / (clk_div + clk_div / 2);
		} else if (baudrate >= clk / (2 * clk_div)) {
			encoded_divisor[0] = 2;
			best_baud = clk / (2 * clk_div);
		} else {
			int divisor = clk * 16 / clk_div / baudrate;
			int best_divisor;
			if ((divisor & 1) != 0) best_divisor = divisor / 2 + 1;
			else best_divisor = divisor / 2;
			if (best_divisor > 0x20000) best_divisor = 0x1ffff;
			best_baud = clk * 16 / clk_div / best_divisor;
			if ((best_baud & 1) != 0) best_baud = best_baud / 2 + 1;
			else best_baud = best_baud / 2;
			encoded_divisor[0] = (best_divisor >> 3) | (frac_code[best_divisor & 0x7] << 14);
		}
		return best_baud;
	}

	private static int ftdi_convert_baudrate(int baudrate, ftdi_context ftdi, short[] value,
		short[] index) {
		int best_baud;
		long[] encoded_divisor = new long[1];
		ftdi_chip_type type = ftdi.type().get();

		if (baudrate <= 0) return -1;
		if (type != null && type.isHType()) {
			if (baudrate * 10 > H_CLK / 0x3fff) {
				best_baud = ftdi_to_clkbits(baudrate, H_CLK, 10, encoded_divisor);
				encoded_divisor[0] |= 0x20000; /* switch on CLK/10 */
			} else best_baud = ftdi_to_clkbits(baudrate, C_CLK, 16, encoded_divisor);
		} else if (type != null && type != TYPE_AM)
			best_baud = ftdi_to_clkbits(baudrate, C_CLK, 16, encoded_divisor);
		else best_baud = ftdi_to_clkbits_AM(baudrate, encoded_divisor);

		value[0] = (short) (encoded_divisor[0] & 0xFFFF);
		if (type != null && type.isHType()) {
			index[0] = (short) (encoded_divisor[0] >> 8);
			index[0] &= 0xFF00;
			index[0] |= ftdi.index;
		} else index[0] = (short) (encoded_divisor[0] >> 16);
		return best_baud;
	}

	public static void ftdi_set_baudrate(ftdi_context ftdi, int baudrate) throws LibUsbException {
		requireDev(ftdi);
		short[] value = new short[1];
		short[] index = new short[1];

		if (ftdi.bitbang_enabled().get()) baudrate = baudrate * 4;
		int actual_baudrate = ftdi_convert_baudrate(baudrate, ftdi, value, index);
		if (actual_baudrate <= 0) throw new LibUsbException("Baudrate <= 0: " + actual_baudrate,
			LIBUSB_ERROR_INVALID_PARAM);

		if ((actual_baudrate * 2 < baudrate) || ((actual_baudrate < baudrate) ?
			(actual_baudrate * 21 < baudrate * 20) : (baudrate * 21 < actual_baudrate * 20)))
			throw new LibUsbException(
				format("Unsupported baudrate: %d (%d)", baudrate, actual_baudrate),
				LIBUSB_ERROR_INVALID_PARAM);

		controlTransferOut(ftdi, SIO_SET_BAUDRATE_REQUEST, value[0], index[0]);
		ftdi.baudrate = baudrate;
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

	public static int ftdi_write_data(ftdi_context ftdi, byte... data) throws LibUsbException {
		return ftdi_write_data(ftdi, data, 0, data.length);
	}

	public static int ftdi_write_data(ftdi_context ftdi, byte[] data, int offset)
		throws LibUsbException {
		return ftdi_write_data(ftdi, data, offset, data.length - offset);
	}

	public static int ftdi_write_data(ftdi_context ftdi, byte[] data, int offset, int len)
		throws LibUsbException {
		return ftdi_write_data(ftdi, ByteBuffer.wrap(data, offset, len));
	}

	public static int ftdi_write_data(ftdi_context ftdi, ByteBuffer buffer) throws LibUsbException {
		requireDev(ftdi);
		int offset = 0;
		while (buffer.hasRemaining()) {
			int len = Math.min(ftdi.writebuffer_chunksize, buffer.remaining());
			int n =
				libusb_bulk_transfer(ftdi.usb_dev, ftdi.in_ep, buffer, len, ftdi.usb_write_timeout);
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

	public static int ftdi_read_data(ftdi_context ftdi, ByteBuffer buffer, int size)
		throws LibUsbException {
		requireDev(ftdi);
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

	public static ftdi_transfer_control ftdi_write_data_submit(ftdi_context ftdi, Pointer buf,
		int size) throws LibUsbException {
		requireDev(ftdi);
		libusb_transfer transfer = libusb_alloc_transfer(0);
		try {
			ftdi_transfer_control tc = new ftdi_transfer_control();
			tc.ftdi = new ftdi_context.ByReference(ftdi.getPointer());
			tc.completed = 0;
			tc.buf = buf;
			tc.size = size;
			tc.offset = 0;
			tc.transfer = new libusb_transfer.ByReference(transfer.getPointer());

			int write_size = Math.min(size, ftdi.writebuffer_chunksize);
			libusb_fill_bulk_transfer(transfer, ftdi.usb_dev, ftdi.in_ep, buf, write_size,
				ftdi_write_data_cb, tc.getPointer(), ftdi.usb_write_timeout);
			libusb_submit_transfer(transfer);
			return tc;
		} catch (LibUsbException | RuntimeException e) {
			libusb_free_transfer(transfer);
			throw e;
		}
	}

	private static void ftdi_write_data_cb(libusb_transfer transfer) {
		ftdi_transfer_control tc = new ftdi_transfer_control(transfer.user_data);
		ftdi_context ftdi = tc.ftdi;
		tc.offset += transfer.actual_length;

		if (tc.offset >= tc.size) tc.completed = 1; // same code as error!
		else if (transfer.status().get() == LIBUSB_TRANSFER_CANCELLED)
			tc.completed().set(LIBUSB_TRANSFER_CANCELLED);
		else try {
			transfer.length = Math.min(ftdi.writebuffer_chunksize, tc.size - tc.offset);
			transfer.buffer = tc.buf.share(tc.offset);
			libusb_submit_transfer(transfer);
		} catch (LibUsbException | RuntimeException e) {
			logger.catching(e);
			tc.completed = 1;
		}
	}

	public static ftdi_transfer_control ftdi_read_data_submit(ftdi_context ftdi, Pointer buf,
		int size) throws LibUsbException {
		requireDev(ftdi);
		libusb_transfer transfer = libusb_alloc_transfer(0);
		try {
			ftdi_transfer_control tc = new ftdi_transfer_control();
			tc.ftdi = new ftdi_context.ByReference(ftdi.getPointer());
			tc.completed = 0;
			tc.buf = buf;
			tc.size = size;
			tc.offset = 0;
			tc.transfer = new libusb_transfer.ByReference(transfer.getPointer());

			int read_size = readLen(ftdi, size);
			libusb_fill_bulk_transfer(transfer, ftdi.usb_dev, ftdi.out_ep, ftdi.readbuffer,
				read_size, ftdi_read_data_cb, tc.getPointer(), ftdi.usb_read_timeout);
			libusb_submit_transfer(transfer);
			return tc;
		} catch (LibUsbException | RuntimeException e) {
			libusb_free_transfer(transfer);
			throw e;
		}
	}

	private static void ftdi_read_data_cb(libusb_transfer transfer) {
		ftdi_transfer_control tc = new ftdi_transfer_control(transfer.user_data);
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

		if (tc.offset >= tc.size) tc.completed = 1; // same code as error!
		else if (transfer.status().get() == LIBUSB_TRANSFER_CANCELLED)
			tc.completed().set(LIBUSB_TRANSFER_CANCELLED);
		else try {
			transfer.length =
				Math.min(ftdi.readbuffer_chunksize, readLen(ftdi, tc.size - tc.offset));
			libusb_submit_transfer(transfer);
		} catch (LibUsbException | RuntimeException e) {
			logger.catching(e);
			tc.completed = 1;
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
			libusb_free_transfer(tc.transfer);
			tc.transfer = null;
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
			libusb_free_transfer(tc.transfer);
			tc.transfer = null;
		}
	}

	private static void waitForCompletion(ftdi_transfer_control tc, timeval to)
		throws LibUsbException {
		if (tc == null || tc.ftdi == null || tc.ftdi.usb_ctx == null) return;
		while (tc.completed == 0)
			tc.completed = libusb_handle_events_timeout_completed(tc.ftdi.usb_ctx, to);
	}

	public static void ftdi_read_data_set_chunk_size(ftdi_context ftdi, int chunkSize) {
		ftdi.readbuffer = new Memory(chunkSize);
		ftdi.readbuffer_chunksize = chunkSize;
	}

	public static void ftdi_set_bitmode(ftdi_context ftdi, int bitmask, ftdi_mpsse_mode mode)
		throws LibUsbException {
		requireDev(ftdi);
		int value = mode.value << 8 | bitmask;
		controlTransferOut(ftdi, SIO_SET_BITMODE_REQUEST, value, ftdi.index);
		ftdi.bitbang_mode = (byte) mode.value;
		ftdi.bitbang_enabled = (byte) (mode != ftdi_mpsse_mode.BITMODE_RESET ? 1 : 0);
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

	public static void ftdi_set_latency_timer(ftdi_context ftdi, int latency)
		throws LibUsbException {
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

	public static String ftdi_get_error_string(ftdi_context ftdi) {
		return ftdi == null ? null : ftdi.error_str;
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

	static void requireDev(ftdi_context ftdi) throws LibUsbException {
		requireCtx(ftdi);
		require(ftdi.usb_dev, "USB device");
	}

	static void requireCtx(ftdi_context ftdi) throws LibUsbException {
		require(ftdi);
		require(ftdi.usb_ctx, "USB context");
	}

	static void require(ftdi_context ftdi) throws LibUsbException {
		require(ftdi, "Ftdi context");
	}

	static void require(Object obj, String name) throws LibUsbException {
		if (obj != null) return;
		throw new LibUsbException(name + " unavailable", LIBUSB_ERROR_INVALID_PARAM);
	}

}
