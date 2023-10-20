/***************************************************************************
                          ftdi.h  -  description
                             -------------------
    begin                : Fri Apr 4 2003
    copyright            : (C) 2003-2017 by Intra2net AG and the libftdi developers
    email                : opensource@intra2net.com
 ***************************************************************************/

/***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU Lesser General Public License           *
 *   version 2.1 as published by the Free Software Foundation;             *
 *                                                                         *
 ***************************************************************************/

#ifndef __baud_h__
#define __baud_h__

#include <stdint.h>
#ifndef _WIN32
#include <sys/time.h>
#endif

/* 'interface' might be defined as a macro on Windows, so we need to
 * undefine it so as not to break the current libftdi API, because
 * struct ftdi_context has an 'interface' member
 * As this can be problematic if you include windows.h after ftdi.h
 * in your sources, we force windows.h to be included first. */
#if defined(_WIN32) || defined(__CYGWIN__) || defined(_WIN32_WCE)
#include <windows.h>
#if defined(interface)
#undef interface
#endif
#endif

/** FTDI chip type */
enum ftdi_chip_type
{
    TYPE_AM=0,
    TYPE_BM=1,
    TYPE_2232C=2,
    TYPE_R=3,
    TYPE_2232H=4,
    TYPE_4232H=5,
    TYPE_232H=6,
    TYPE_230X=7,
};
/** Parity mode for ftdi_set_line_property() */
enum ftdi_parity_type { NONE=0, ODD=1, EVEN=2, MARK=3, SPACE=4 };
/** Number of stop bits for ftdi_set_line_property() */
enum ftdi_stopbits_type { STOP_BIT_1=0, STOP_BIT_15=1, STOP_BIT_2=2 };
/** Number of bits for ftdi_set_line_property() */
enum ftdi_bits_type { BITS_7=7, BITS_8=8 };
/** Break type for ftdi_set_line_property2() */
enum ftdi_break_type { BREAK_OFF=0, BREAK_ON=1 };

/** MPSSE bitbang modes */
enum ftdi_mpsse_mode
{
    BITMODE_RESET  = 0x00,    /**< switch off bitbang mode, back to regular serial/FIFO */
    BITMODE_BITBANG= 0x01,    /**< classical asynchronous bitbang mode, introduced with B-type chips */
    BITMODE_MPSSE  = 0x02,    /**< MPSSE mode, available on 2232x chips */
    BITMODE_SYNCBB = 0x04,    /**< synchronous bitbang mode, available on 2232x and R-type chips  */
    BITMODE_MCU    = 0x08,    /**< MCU Host Bus Emulation mode, available on 2232x chips */
    /* CPU-style fifo mode gets set via EEPROM */
    BITMODE_OPTO   = 0x10,    /**< Fast Opto-Isolated Serial Interface Mode, available on 2232x chips  */
    BITMODE_CBUS   = 0x20,    /**< Bitbang on CBUS pins of R-type chips, configure in EEPROM before */
    BITMODE_SYNCFF = 0x40,    /**< Single Channel Synchronous FIFO mode, available on 2232H chips */
    BITMODE_FT1284 = 0x80,    /**< FT1284 mode, available on 232H chips */
};

/** Port interface for chips with multiple interfaces */
enum ftdi_interface
{
    INTERFACE_ANY = 0,
    INTERFACE_A   = 1,
    INTERFACE_B   = 2,
    INTERFACE_C   = 3,
    INTERFACE_D   = 4
};

/** Automatic loading / unloading of kernel modules */
enum ftdi_module_detach_mode
{
    AUTO_DETACH_SIO_MODULE = 0,
    DONT_DETACH_SIO_MODULE = 1
};

/* Shifting commands IN MPSSE Mode*/
#define MPSSE_WRITE_NEG 0x01   /* Write TDI/DO on negative TCK/SK edge*/
#define MPSSE_BITMODE   0x02   /* Write bits, not bytes */
#define MPSSE_READ_NEG  0x04   /* Sample TDO/DI on negative TCK/SK edge */
#define MPSSE_LSB       0x08   /* LSB first */
#define MPSSE_DO_WRITE  0x10   /* Write TDI/DO */
#define MPSSE_DO_READ   0x20   /* Read TDO/DI */
#define MPSSE_WRITE_TMS 0x40   /* Write TMS/CS */

/* FTDI MPSSE commands */
#define SET_BITS_LOW   0x80
/*BYTE DATA*/
/*BYTE Direction*/
#define SET_BITS_HIGH  0x82
/*BYTE DATA*/
/*BYTE Direction*/
#define GET_BITS_LOW   0x81
#define GET_BITS_HIGH  0x83
#define LOOPBACK_START 0x84
#define LOOPBACK_END   0x85
#define TCK_DIVISOR    0x86
/* H Type specific commands */
#define DIS_DIV_5       0x8a
#define EN_DIV_5        0x8b
#define EN_3_PHASE      0x8c
#define DIS_3_PHASE     0x8d
#define CLK_BITS        0x8e
#define CLK_BYTES       0x8f
#define CLK_WAIT_HIGH   0x94
#define CLK_WAIT_LOW    0x95
#define EN_ADAPTIVE     0x96
#define DIS_ADAPTIVE    0x97
#define CLK_BYTES_OR_HIGH 0x9c
#define CLK_BYTES_OR_LOW  0x9d
/*FT232H specific commands */
#define DRIVE_OPEN_COLLECTOR 0x9e
/* Value Low */
/* Value HIGH */ /*rate is 12000000/((1+value)*2) */
#define DIV_VALUE(rate) (rate > 6000000)?0:((6000000/rate -1) > 0xffff)? 0xffff: (6000000/rate -1)

/* Commands in MPSSE and Host Emulation Mode */
#define SEND_IMMEDIATE 0x87
#define WAIT_ON_HIGH   0x88
#define WAIT_ON_LOW    0x89

/* Commands in Host Emulation Mode */
#define READ_SHORT     0x90
/* Address_Low */
#define READ_EXTENDED  0x91
/* Address High */
/* Address Low  */
#define WRITE_SHORT    0x92
/* Address_Low */
#define WRITE_EXTENDED 0x93
/* Address High */
/* Address Low  */

/* Definitions for flow control */
#define SIO_RESET          0 /* Reset the port */
#define SIO_MODEM_CTRL     1 /* Set the modem control register */
#define SIO_SET_FLOW_CTRL  2 /* Set flow control register */
#define SIO_SET_BAUD_RATE  3 /* Set baud rate */
#define SIO_SET_DATA       4 /* Set the data characteristics of the port */

#define FTDI_DEVICE_OUT_REQTYPE (LIBUSB_REQUEST_TYPE_VENDOR | LIBUSB_RECIPIENT_DEVICE | LIBUSB_ENDPOINT_OUT)
#define FTDI_DEVICE_IN_REQTYPE (LIBUSB_REQUEST_TYPE_VENDOR | LIBUSB_RECIPIENT_DEVICE | LIBUSB_ENDPOINT_IN)

/* Requests */
#define SIO_RESET_REQUEST             SIO_RESET
#define SIO_SET_BAUDRATE_REQUEST      SIO_SET_BAUD_RATE
#define SIO_SET_DATA_REQUEST          SIO_SET_DATA
#define SIO_SET_FLOW_CTRL_REQUEST     SIO_SET_FLOW_CTRL
#define SIO_SET_MODEM_CTRL_REQUEST    SIO_MODEM_CTRL
#define SIO_POLL_MODEM_STATUS_REQUEST 0x05
#define SIO_SET_EVENT_CHAR_REQUEST    0x06
#define SIO_SET_ERROR_CHAR_REQUEST    0x07
#define SIO_SET_LATENCY_TIMER_REQUEST 0x09
#define SIO_GET_LATENCY_TIMER_REQUEST 0x0A
#define SIO_SET_BITMODE_REQUEST       0x0B
#define SIO_READ_PINS_REQUEST         0x0C
#define SIO_READ_EEPROM_REQUEST       0x90
#define SIO_WRITE_EEPROM_REQUEST      0x91
#define SIO_ERASE_EEPROM_REQUEST      0x92


#define SIO_RESET_SIO 0
#define SIO_RESET_PURGE_RX 1
#define SIO_RESET_PURGE_TX 2

#define SIO_DISABLE_FLOW_CTRL 0x0
#define SIO_RTS_CTS_HS (0x1 << 8)
#define SIO_DTR_DSR_HS (0x2 << 8)
#define SIO_XON_XOFF_HS (0x4 << 8)

#define SIO_SET_DTR_MASK 0x1
#define SIO_SET_DTR_HIGH ( 1 | ( SIO_SET_DTR_MASK  << 8))
#define SIO_SET_DTR_LOW  ( 0 | ( SIO_SET_DTR_MASK  << 8))
#define SIO_SET_RTS_MASK 0x2
#define SIO_SET_RTS_HIGH ( 2 | ( SIO_SET_RTS_MASK << 8 ))
#define SIO_SET_RTS_LOW ( 0 | ( SIO_SET_RTS_MASK << 8 ))

#define SIO_RTS_CTS_HS (0x1 << 8)

/* marker for unused usb urb structures
   (taken from libusb) */
#define FTDI_URB_USERCONTEXT_COOKIE ((void *)0x1)

#ifdef __GNUC__
#define DEPRECATED(func) func __attribute__ ((deprecated))
#elif defined(_MSC_VER)
#define DEPRECATED(func) __declspec(deprecated) func
#else
#pragma message("WARNING: You need to implement DEPRECATED for this compiler")
#define DEPRECATED(func) func
#endif

/**
    \brief Main context structure for all libftdi functions.

    Do not access directly if possible.
*/
struct ftdi_context
{
    /* USB specific */
    /** libusb's context */
    //struct libusb_context *usb_ctx;
    void *usb_ctx;
    /** libusb's usb_dev_handle */
    //struct libusb_device_handle *usb_dev;
    void *usb_dev;
    /** usb read timeout */
    int usb_read_timeout;
    /** usb write timeout */
    int usb_write_timeout;

    /* FTDI specific */
    /** FTDI chip type */
    enum ftdi_chip_type type;
    /** baudrate */
    int baudrate;
    /** bitbang mode state */
    unsigned char bitbang_enabled;
    /** pointer to read buffer for ftdi_read_data */
    unsigned char *readbuffer;
    /** read buffer offset */
    unsigned int readbuffer_offset;
    /** number of remaining data in internal read buffer */
    unsigned int readbuffer_remaining;
    /** read buffer chunk size */
    unsigned int readbuffer_chunksize;
    /** write buffer chunk size */
    unsigned int writebuffer_chunksize;
    /** maximum packet size. Needed for filtering modem status bytes every n packets. */
    unsigned int max_packet_size;

    /* FTDI FT2232C requirecments */
    /** FT2232C interface number: 0 or 1 */
    int interface;   /* 0 or 1 */
    /** FT2232C index number: 1 or 2 */
    int index;       /* 1 or 2 */
    /* Endpoints */
    /** FT2232C end points: 1 or 2 */
    int in_ep;
    int out_ep;      /* 1 or 2 */

    /** Bitbang mode. 1: (default) Normal bitbang mode, 2: FT2232C SPI bitbang mode */
    unsigned char bitbang_mode;

    /** Decoded eeprom structure */
    void *eeprom;

    /** String representation of last error */
    const char *error_str;

    /** Defines behavior in case a kernel module is already attached to the device */
    enum ftdi_module_detach_mode module_detach_mode;
};

#define FT1284_CLK_IDLE_STATE 0x01
#define FT1284_DATA_LSB       0x02 /* DS_FT232H 1.3 amd ftd2xx.h 1.0.4 disagree here*/
#define FT1284_FLOW_CONTROL   0x04
#define POWER_SAVE_DISABLE_H 0x80

#define USE_SERIAL_NUM 0x08
enum ftdi_cbus_func
{
    CBUS_TXDEN = 0, CBUS_PWREN = 1, CBUS_RXLED = 2, CBUS_TXLED = 3, CBUS_TXRXLED = 4,
    CBUS_SLEEP = 5, CBUS_CLK48 = 6, CBUS_CLK24 = 7, CBUS_CLK12 = 8, CBUS_CLK6 =  9,
    CBUS_IOMODE = 0xa, CBUS_BB_WR = 0xb, CBUS_BB_RD = 0xc
};

enum ftdi_cbush_func
{
    CBUSH_TRISTATE = 0, CBUSH_TXLED = 1, CBUSH_RXLED = 2, CBUSH_TXRXLED = 3, CBUSH_PWREN = 4,
    CBUSH_SLEEP = 5, CBUSH_DRIVE_0 = 6, CBUSH_DRIVE1 = 7, CBUSH_IOMODE = 8, CBUSH_TXDEN =  9,
    CBUSH_CLK30 = 10, CBUSH_CLK15 = 11, CBUSH_CLK7_5 = 12
};

enum ftdi_cbusx_func
{
    CBUSX_TRISTATE = 0, CBUSX_TXLED = 1, CBUSX_RXLED = 2, CBUSX_TXRXLED = 3, CBUSX_PWREN = 4,
    CBUSX_SLEEP = 5, CBUSX_DRIVE_0 = 6, CBUSX_DRIVE1 = 7, CBUSX_IOMODE = 8, CBUSX_TXDEN =  9,
    CBUSX_CLK24 = 10, CBUSX_CLK12 = 11, CBUSX_CLK6 = 12, CBUSX_BAT_DETECT = 13,
    CBUSX_BAT_DETECT_NEG = 14, CBUSX_I2C_TXE = 15, CBUSX_I2C_RXF = 16, CBUSX_VBUS_SENSE = 17,
    CBUSX_BB_WR = 18, CBUSX_BB_RD = 19, CBUSX_TIME_STAMP = 20, CBUSX_AWAKE = 21
};

/** Invert TXD# */
#define INVERT_TXD 0x01
/** Invert RXD# */
#define INVERT_RXD 0x02
/** Invert RTS# */
#define INVERT_RTS 0x04
/** Invert CTS# */
#define INVERT_CTS 0x08
/** Invert DTR# */
#define INVERT_DTR 0x10
/** Invert DSR# */
#define INVERT_DSR 0x20
/** Invert DCD# */
#define INVERT_DCD 0x40
/** Invert RI# */
#define INVERT_RI  0x80

/** Interface Mode. */
#define CHANNEL_IS_UART 0x0
#define CHANNEL_IS_FIFO 0x1
#define CHANNEL_IS_OPTO 0x2
#define CHANNEL_IS_CPU  0x4
#define CHANNEL_IS_FT1284 0x8

#define CHANNEL_IS_RS485 0x10

#define DRIVE_4MA  0
#define DRIVE_8MA  1
#define DRIVE_12MA 2
#define DRIVE_16MA 3
#define SLOW_SLEW  4
#define IS_SCHMITT 8

/** Driver Type. */
#define DRIVER_VCP 0x08
#define DRIVER_VCPH 0x10 /* FT232H has moved the VCP bit */

#define USE_USB_VERSION_BIT 0x10

#define SUSPEND_DBUS7_BIT 0x80

/** High current drive. */
#define HIGH_CURRENT_DRIVE   0x10
#define HIGH_CURRENT_DRIVE_R 0x04


#ifdef __cplusplus
extern "C"
{
#endif

    int ftdi_init(struct ftdi_context *ftdi);
    struct ftdi_context *ftdi_new(void);
    int ftdi_set_interface(struct ftdi_context *ftdi, enum ftdi_interface interface);

    int ftdi_set_baudrate(struct ftdi_context *ftdi, int baudrate);

    int ftdi_set_bitmode(struct ftdi_context *ftdi, unsigned char bitmask, unsigned char mode);
    int ftdi_disable_bitbang(struct ftdi_context *ftdi);

#ifdef __cplusplus
}
#endif

#endif /* __baud_h__ */
