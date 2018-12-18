package ceri.serial.ftdi;

import static ceri.serial.libusb.jna.LibUsb.libusb_endpoint_direction.LIBUSB_ENDPOINT_IN;
import static ceri.serial.libusb.jna.LibUsb.libusb_endpoint_direction.LIBUSB_ENDPOINT_OUT;
import static ceri.serial.libusb.jna.LibUsb.libusb_request_recipient.LIBUSB_RECIPIENT_DEVICE;
import static ceri.serial.libusb.jna.LibUsb.libusb_request_type.LIBUSB_REQUEST_TYPE_VENDOR;
import ceri.serial.libusb.jna.LibUsb;

public class Ftdi {

	/* FTDI MPSSE commands */
	public static final int SET_BITS_LOW = 0x80;
	/* BYTE DATA */
	/* BYTE Direction */
	public static final int SET_BITS_HIGH = 0x82;
	/* BYTE DATA */
	/* BYTE Direction */
	public static final int GET_BITS_LOW = 0x81;
	public static final int GET_BITS_HIGH = 0x83;
	public static final int LOOPBACK_START = 0x84;
	public static final int LOOPBACK_END = 0x85;
	public static final int TCK_DIVISOR = 0x86;
	/* H Type specific commands */
	public static final int DIS_DIV_5 = 0x8a;
	public static final int EN_DIV_5 = 0x8b;
	public static final int EN_3_PHASE = 0x8c;
	public static final int DIS_3_PHASE = 0x8d;
	public static final int CLK_BITS = 0x8e;
	public static final int CLK_BYTES = 0x8f;
	public static final int CLK_WAIT_HIGH = 0x94;
	public static final int CLK_WAIT_LOW = 0x95;
	public static final int EN_ADAPTIVE = 0x96;
	public static final int DIS_ADAPTIVE = 0x97;
	public static final int CLK_BYTES_OR_HIGH = 0x9c;
	public static final int CLK_BYTES_OR_LOW = 0x9d;
	/* FT232H specific commands */
	public static final int DRIVE_OPEN_COLLECTOR = 0x9e;

	/* Value Low */
	/* Value HIGH */ /* rate is 12000000/((1+value)*2) */
	public static int DIV_VALUE(int rate) {
		if (rate > 6000000) return 0;
		int value = (6000000 / rate) - 1;
		return (value > 0xffff) ? 0xffff : value;
	}

	/* Commands in MPSSE and Host Emulation Mode */
	public static final int SEND_IMMEDIATE = 0x87;
	public static final int WAIT_ON_HIGH = 0x88;
	public static final int WAIT_ON_LOW = 0x89;

	/* Commands in Host Emulation Mode */
	public static final int READ_SHORT = 0x90;
	/* Address_Low */
	public static final int READ_EXTENDED = 0x91;
	/* Address High */
	/* Address Low */
	public static final int WRITE_SHORT = 0x92;
	/* Address_Low */
	public static final int WRITE_EXTENDED = 0x93;
	/* Address High */
	/* Address Low */

	/* Definitions for flow control */
	// public static final int SIO_RESET = 0; /* Reset the port */
	// public static final int SIO_MODEM_CTRL = 1; /* Set the modem control register */
	// public static final int SIO_SET_FLOW_CTRL = 2; /* Set flow control register */
	// public static final int SIO_SET_BAUD_RATE = 3; /* Set baud rate */
	// public static final int SIO_SET_DATA = 4; /* Set the data characteristics of the port */

	public static final int FTDI_DEVICE_OUT_REQTYPE = LibUsb.libusb_request_type_value(
		LIBUSB_RECIPIENT_DEVICE, LIBUSB_REQUEST_TYPE_VENDOR, LIBUSB_ENDPOINT_OUT);
	public static final int FTDI_DEVICE_IN_REQTYPE = LibUsb.libusb_request_type_value(
		LIBUSB_RECIPIENT_DEVICE, LIBUSB_REQUEST_TYPE_VENDOR, LIBUSB_ENDPOINT_IN);

	/* Requests */
	// public static final int SIO_RESET_REQUEST = SIO_RESET;
	// public static final int SIO_SET_BAUDRATE_REQUEST = SIO_SET_BAUD_RATE;
	// public static final int SIO_SET_DATA_REQUEST = SIO_SET_DATA;
	// public static final int SIO_SET_FLOW_CTRL_REQUEST = SIO_SET_FLOW_CTRL;
	// public static final int SIO_SET_MODEM_CTRL_REQUEST = SIO_MODEM_CTRL;
	// public static final int SIO_POLL_MODEM_STATUS_REQUEST = 0x05;
	// public static final int SIO_SET_EVENT_CHAR_REQUEST = 0x06;
	// public static final int SIO_SET_ERROR_CHAR_REQUEST = 0x07;
	// public static final int SIO_SET_LATENCY_TIMER_REQUEST = 0x09;
	// public static final int SIO_GET_LATENCY_TIMER_REQUEST = 0x0A;
	// public static final int SIO_SET_BITMODE_REQUEST = 0x0B;
	// public static final int SIO_READ_PINS_REQUEST = 0x0C;
	// public static final int SIO_READ_EEPROM_REQUEST = 0x90;
	// public static final int SIO_WRITE_EEPROM_REQUEST = 0x91;
	// public static final int SIO_ERASE_EEPROM_REQUEST = 0x92;

	public static final int SIO_RESET_SIO = 0;
	public static final int SIO_RESET_PURGE_RX = 1;
	public static final int SIO_RESET_PURGE_TX = 2;

	//	public static final int SIO_DISABLE_FLOW_CTRL = 0x0;
	//	public static final int SIO_RTS_CTS_HS = 0x1 << 8;
	//	public static final int SIO_DTR_DSR_HS = 0x2 << 8;
	//	public static final int SIO_XON_XOFF_HS = 0x4 << 8;

	public static final int SIO_SET_DTR_MASK = 0x1;
	public static final int SIO_SET_DTR_HIGH = 1 | (SIO_SET_DTR_MASK << 8);
	public static final int SIO_SET_DTR_LOW = 0 | (SIO_SET_DTR_MASK << 8);
	public static final int SIO_SET_RTS_MASK = 0x2;
	public static final int SIO_SET_RTS_HIGH = 2 | (SIO_SET_RTS_MASK << 8);
	public static final int SIO_SET_RTS_LOW = 0 | (SIO_SET_RTS_MASK << 8);

	/*
	 * marker for unused usb urb structures (taken from libusb)
	 */
	// #define FTDI_URB_USERCONTEXT_COOKIE ((void *)0x1)

	public static final int FT1284_CLK_IDLE_STATE = 0x01;
	public static final int FT1284_DATA_LSB = 0x02;
	public static final int FT1284_FLOW_CONTROL = 0x04;
	public static final int POWER_SAVE_DISABLE_H = 0x80;

	public static final int USE_SERIAL_NUM = 0x08;

	/** Invert TXD# */
	public static final int INVERT_TXD = 0x01;
	/** Invert RXD# */
	public static final int INVERT_RXD = 0x02;
	/** Invert RTS# */
	public static final int INVERT_RTS = 0x04;
	/** Invert CTS# */
	public static final int INVERT_CTS = 0x08;
	/** Invert DTR# */
	public static final int INVERT_DTR = 0x10;
	/** Invert DSR# */
	public static final int INVERT_DSR = 0x20;
	/** Invert DCD# */
	public static final int INVERT_DCD = 0x40;
	/** Invert RI# */
	public static final int INVERT_RI = 0x80;

	/** Interface Mode. */
	public static final int CHANNEL_IS_UART = 0x0;
	public static final int CHANNEL_IS_FIFO = 0x1;
	public static final int CHANNEL_IS_OPTO = 0x2;
	public static final int CHANNEL_IS_CPU = 0x4;
	public static final int CHANNEL_IS_FT1284 = 0x8;

	public static final int CHANNEL_IS_RS485 = 0x10;

	public static final int DRIVE_4MA = 0;
	public static final int DRIVE_8MA = 1;
	public static final int DRIVE_12MA = 2;
	public static final int DRIVE_16MA = 3;
	public static final int SLOW_SLEW = 4;
	public static final int IS_SCHMITT = 8;

	/** Driver Type. */
	public static final int DRIVER_VCP = 0x08;
	public static final int DRIVER_VCPH = 0x10; /* FT232H has moved the VCP bit */

	public static final int USE_USB_VERSION_BIT = 0x10;

	public static final int SUSPEND_DBUS7_BIT = 0x80;

	/** High current drive. */
	public static final int HIGH_CURRENT_DRIVE = 0x10;
	public static final int HIGH_CURRENT_DRIVE_R = 0x04;

	// typedef int (FTDIStreamCallback)(uint8_t *buffer, int length,
	// FTDIProgressInfo *progress, void *userdata);

//    int ftdi_init(struct ftdi_context *ftdi);
//    struct ftdi_context *ftdi_new(void);
//    int ftdi_set_interface(struct ftdi_context *ftdi, enum ftdi_interface interface);
//
//    void ftdi_deinit(struct ftdi_context *ftdi);
//    void ftdi_free(struct ftdi_context *ftdi);
//    void ftdi_set_usbdev (struct ftdi_context *ftdi, struct libusb_device_handle *usbdev);
//
//    struct ftdi_version_info ftdi_get_library_version(void);
//
//    int ftdi_usb_find_all(struct ftdi_context *ftdi, struct ftdi_device_list **devlist,
//                          int vendor, int product);
//    void ftdi_list_free(struct ftdi_device_list **devlist);
//    void ftdi_list_free2(struct ftdi_device_list *devlist);
//    int ftdi_usb_get_strings(struct ftdi_context *ftdi, struct libusb_device *dev,
//                             char *manufacturer, int mnf_len,
//                             char *description, int desc_len,
//                             char *serial, int serial_len);
//    int ftdi_usb_get_strings2(struct ftdi_context *ftdi, struct libusb_device *dev,
//                              char *manufacturer, int mnf_len,
//                              char *description, int desc_len,
//                              char *serial, int serial_len);
//
//    int ftdi_eeprom_get_strings(struct ftdi_context *ftdi,
//                                char *manufacturer, int mnf_len,
//                                char *product, int prod_len,
//                                char *serial, int serial_len);
//    int ftdi_eeprom_set_strings(struct ftdi_context *ftdi, char * manufacturer,
//                                char * product, char * serial);
//
//    int ftdi_usb_open(struct ftdi_context *ftdi, int vendor, int product);
//    int ftdi_usb_open_desc(struct ftdi_context *ftdi, int vendor, int product,
//                           const char* description, const char* serial);
//    int ftdi_usb_open_desc_index(struct ftdi_context *ftdi, int vendor, int product,
//                                 const char* description, const char* serial, unsigned int index);
//    int ftdi_usb_open_bus_addr(struct ftdi_context *ftdi, uint8_t bus, uint8_t addr);
//    int ftdi_usb_open_dev(struct ftdi_context *ftdi, struct libusb_device *dev);
//    int ftdi_usb_open_string(struct ftdi_context *ftdi, const char* description);
//
//    int ftdi_usb_close(struct ftdi_context *ftdi);
//    int ftdi_usb_reset(struct ftdi_context *ftdi);
//    int ftdi_usb_purge_rx_buffer(struct ftdi_context *ftdi);
//    int ftdi_usb_purge_tx_buffer(struct ftdi_context *ftdi);
//    int ftdi_usb_purge_buffers(struct ftdi_context *ftdi);
//
//    int ftdi_set_baudrate(struct ftdi_context *ftdi, int baudrate);
//    int ftdi_set_line_property(struct ftdi_context *ftdi, enum ftdi_bits_type bits,
//                               enum ftdi_stopbits_type sbit, enum ftdi_parity_type parity);
//    int ftdi_set_line_property2(struct ftdi_context *ftdi, enum ftdi_bits_type bits,
//                                enum ftdi_stopbits_type sbit, enum ftdi_parity_type parity,
//                                enum ftdi_break_type break_type);
//
//    int ftdi_read_data(struct ftdi_context *ftdi, unsigned char *buf, int size);
//    int ftdi_read_data_set_chunksize(struct ftdi_context *ftdi, unsigned int chunksize);
//    int ftdi_read_data_get_chunksize(struct ftdi_context *ftdi, unsigned int *chunksize);
//
//    int ftdi_write_data(struct ftdi_context *ftdi, const unsigned char *buf, int size);
//    int ftdi_write_data_set_chunksize(struct ftdi_context *ftdi, unsigned int chunksize);
//    int ftdi_write_data_get_chunksize(struct ftdi_context *ftdi, unsigned int *chunksize);
//
//    int ftdi_readstream(struct ftdi_context *ftdi, FTDIStreamCallback *callback,
//                        void *userdata, int packetsPerTransfer, int numTransfers);
//    struct ftdi_transfer_control *ftdi_write_data_submit(struct ftdi_context *ftdi, unsigned char *buf, int size);
//
//    struct ftdi_transfer_control *ftdi_read_data_submit(struct ftdi_context *ftdi, unsigned char *buf, int size);
//    int ftdi_transfer_data_done(struct ftdi_transfer_control *tc);
//    void ftdi_transfer_data_cancel(struct ftdi_transfer_control *tc, struct timeval * to);
//
//    int ftdi_set_bitmode(struct ftdi_context *ftdi, unsigned char bitmask, unsigned char mode);
//    int ftdi_disable_bitbang(struct ftdi_context *ftdi);
//    int ftdi_read_pins(struct ftdi_context *ftdi, unsigned char *pins);
//
//    int ftdi_set_latency_timer(struct ftdi_context *ftdi, unsigned char latency);
//    int ftdi_get_latency_timer(struct ftdi_context *ftdi, unsigned char *latency);
//
//    int ftdi_poll_modem_status(struct ftdi_context *ftdi, unsigned short *status);
//
//    /* flow control */
//    int ftdi_setflowctrl(struct ftdi_context *ftdi, int flowctrl);
//    int ftdi_setdtr_rts(struct ftdi_context *ftdi, int dtr, int rts);
//    int ftdi_setdtr(struct ftdi_context *ftdi, int state);
//    int ftdi_setrts(struct ftdi_context *ftdi, int state);
//
//    int ftdi_set_event_char(struct ftdi_context *ftdi, unsigned char eventch, unsigned char enable);
//    int ftdi_set_error_char(struct ftdi_context *ftdi, unsigned char errorch, unsigned char enable);
//
//    /* init eeprom for the given FTDI type */
//    int ftdi_eeprom_initdefaults(struct ftdi_context *ftdi,
//                                 char * manufacturer, char *product,
//                                 char * serial);
//    int ftdi_eeprom_build(struct ftdi_context *ftdi);
//    int ftdi_eeprom_decode(struct ftdi_context *ftdi, int verbose);
//
//    int ftdi_get_eeprom_value(struct ftdi_context *ftdi, enum ftdi_eeprom_value value_name, int* value);
//    int ftdi_set_eeprom_value(struct ftdi_context *ftdi, enum ftdi_eeprom_value value_name, int  value);
//
//    int ftdi_get_eeprom_buf(struct ftdi_context *ftdi, unsigned char * buf, int size);
//    int ftdi_set_eeprom_buf(struct ftdi_context *ftdi, const unsigned char * buf, int size);
//
//    int ftdi_set_eeprom_user_data(struct ftdi_context *ftdi, const char * buf, int size);
//
//    int ftdi_read_eeprom(struct ftdi_context *ftdi);
//    int ftdi_read_chipid(struct ftdi_context *ftdi, unsigned int *chipid);
//    int ftdi_write_eeprom(struct ftdi_context *ftdi);
//    int ftdi_erase_eeprom(struct ftdi_context *ftdi);
//
//    int ftdi_read_eeprom_location (struct ftdi_context *ftdi, int eeprom_addr, unsigned short *eeprom_val);
//    int ftdi_write_eeprom_location(struct ftdi_context *ftdi, int eeprom_addr, unsigned short eeprom_val);
//
//    const char *ftdi_get_error_string(struct ftdi_context *ftdi);

}
