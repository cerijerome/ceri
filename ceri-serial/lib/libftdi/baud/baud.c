
#include <string.h>
#include <errno.h>
#include <stdio.h>
#include <stdlib.h>

#include "baud.h"

/*
 * Reduced version of ftdi.c to test baud rates.
 */

#define ftdi_error_return(code, str) do {  \
        if ( ftdi )                        \
            ftdi->error_str = str;         \
        else                               \
            fprintf(stderr, str);          \
        return code;                       \
   } while(0);

/**
    Initializes a ftdi_context.

    \param ftdi pointer to ftdi_context

    \retval  0: all fine
    \retval -1: couldn't allocate read buffer
    \retval -2: couldn't allocate struct  buffer
    \retval -3: libusb_init() failed

    \remark This should be called before all functions
*/
int ftdi_init(struct ftdi_context *ftdi)
{
    ftdi->usb_ctx = NULL;
    ftdi->usb_dev = NULL;
    ftdi->usb_read_timeout = 5000;
    ftdi->usb_write_timeout = 5000;

    ftdi->type = TYPE_BM;    /* chip type */
    ftdi->baudrate = -1;
    ftdi->bitbang_enabled = 0;  /* 0: normal mode 1: any of the bitbang modes enabled */

    ftdi->readbuffer = NULL;
    ftdi->readbuffer_offset = 0;
    ftdi->readbuffer_remaining = 0;
    ftdi->writebuffer_chunksize = 4096;
    ftdi->max_packet_size = 0;
    ftdi->error_str = NULL;
    ftdi->module_detach_mode = AUTO_DETACH_SIO_MODULE;

    ftdi_set_interface(ftdi, INTERFACE_ANY);
    ftdi->bitbang_mode = 1; /* when bitbang is enabled this holds the number of the mode  */

    /* All fine. Now allocate the readbuffer */
    return 0; //ftdi_read_data_set_chunksize(ftdi, 4096);
}

/**
    Allocate and initialize a new ftdi_context

    \return a pointer to a new ftdi_context, or NULL on failure
*/
struct ftdi_context *ftdi_new(void)
{
    struct ftdi_context * ftdi = (struct ftdi_context *)malloc(sizeof(struct ftdi_context));

    if (ftdi == NULL)
    {
        return NULL;
    }

    if (ftdi_init(ftdi) != 0)
    {
        free(ftdi);
        return NULL;
    }

    return ftdi;
}

/**
    Open selected channels on a chip, otherwise use first channel.

    \param ftdi pointer to ftdi_context
    \param interface Interface to use for FT2232C/2232H/4232H chips.

    \retval  0: all fine
    \retval -1: unknown interface
    \retval -2: USB device unavailable
    \retval -3: Device already open, interface can't be set in that state
*/
int ftdi_set_interface(struct ftdi_context *ftdi, enum ftdi_interface interface)
{
    if (ftdi == NULL)
        ftdi_error_return(-2, "USB device unavailable");

    if (ftdi->usb_dev != NULL)
    {
        int check_interface = interface;
        if (check_interface == INTERFACE_ANY)
            check_interface = INTERFACE_A;

        if (ftdi->index != check_interface)
            ftdi_error_return(-3, "Interface can not be changed on an already open device");
    }

    switch (interface)
    {
        case INTERFACE_ANY:
        case INTERFACE_A:
            ftdi->interface = 0;
            ftdi->index     = INTERFACE_A;
            ftdi->in_ep     = 0x02;
            ftdi->out_ep    = 0x81;
            break;
        case INTERFACE_B:
            ftdi->interface = 1;
            ftdi->index     = INTERFACE_B;
            ftdi->in_ep     = 0x04;
            ftdi->out_ep    = 0x83;
            break;
        case INTERFACE_C:
            ftdi->interface = 2;
            ftdi->index     = INTERFACE_C;
            ftdi->in_ep     = 0x06;
            ftdi->out_ep    = 0x85;
            break;
        case INTERFACE_D:
            ftdi->interface = 3;
            ftdi->index     = INTERFACE_D;
            ftdi->in_ep     = 0x08;
            ftdi->out_ep    = 0x87;
            break;
        default:
            ftdi_error_return(-1, "Unknown interface");
    }
    return 0;
}

/*  ftdi_to_clkbits_AM For the AM device, convert a requested baudrate
                    to encoded divisor and the achievable baudrate
    Function is only used internally
    \internal

    See AN120
   clk/1   -> 0
   clk/1.5 -> 1
   clk/2   -> 2
   From /2, 0.125/ 0.25 and 0.5 steps may be taken
   The fractional part has frac_code encoding
*/
static int ftdi_to_clkbits_AM(int baudrate, unsigned long *encoded_divisor)

{
    static const char frac_code[8] = {0, 3, 2, 4, 1, 5, 6, 7};
    static const char am_adjust_up[8] = {0, 0, 0, 1, 0, 3, 2, 1};
    static const char am_adjust_dn[8] = {0, 0, 0, 1, 0, 1, 2, 3};
    int divisor, best_divisor, best_baud, best_baud_diff;
    int i;
    divisor = 24000000 / baudrate;

    // Round down to supported fraction (AM only)
    divisor -= am_adjust_dn[divisor & 7];

    // Try this divisor and the one above it (because division rounds down)
    best_divisor = 0;
    best_baud = 0;
    best_baud_diff = 0;
    for (i = 0; i < 2; i++)
    {
        int try_divisor = divisor + i;
        int baud_estimate;
        int baud_diff;

        // Round up to supported divisor value
        if (try_divisor <= 8)
        {
            // Round up to minimum supported divisor
            try_divisor = 8;
        }
        else if (divisor < 16)
        {
            // AM doesn't support divisors 9 through 15 inclusive
            try_divisor = 16;
        }
        else
        {
            // Round up to supported fraction (AM only)
            try_divisor += am_adjust_up[try_divisor & 7];
            if (try_divisor > 0x1FFF8)
            {
                // Round down to maximum supported divisor value (for AM)
                try_divisor = 0x1FFF8;
            }
        }
        // Get estimated baud rate (to nearest integer)
        baud_estimate = (24000000 + (try_divisor / 2)) / try_divisor;
        // Get absolute difference from requested baud rate
        if (baud_estimate < baudrate)
        {
            baud_diff = baudrate - baud_estimate;
        }
        else
        {
            baud_diff = baud_estimate - baudrate;
        }
        if (i == 0 || baud_diff < best_baud_diff)
        {
            // Closest to requested baud rate so far
            best_divisor = try_divisor;
            best_baud = baud_estimate;
            best_baud_diff = baud_diff;
            if (baud_diff == 0)
            {
                // Spot on! No point trying
                break;
            }
        }
    }
    // Encode the best divisor value
    *encoded_divisor = (best_divisor >> 3) | (frac_code[best_divisor & 7] << 14);
    // Deal with special cases for encoded value
    if (*encoded_divisor == 1)
    {
        *encoded_divisor = 0;    // 3000000 baud
    }
    else if (*encoded_divisor == 0x4001)
    {
        *encoded_divisor = 1;    // 2000000 baud (BM only)
    }
    return best_baud;
}

/*  ftdi_to_clkbits Convert a requested baudrate for a given system clock  and predivisor
                    to encoded divisor and the achievable baudrate
    Function is only used internally
    \internal

    See AN120
   clk/1   -> 0
   clk/1.5 -> 1
   clk/2   -> 2
   From /2, 0.125 steps may be taken.
   The fractional part has frac_code encoding

   value[13:0] of value is the divisor
   index[9] mean 12 MHz Base(120 MHz/10) rate versus 3 MHz (48 MHz/16) else

   H Type have all features above with
   {index[8],value[15:14]} is the encoded subdivisor

   FT232R, FT2232 and FT232BM have no option for 12 MHz and with
   {index[0],value[15:14]} is the encoded subdivisor

   AM Type chips have only four fractional subdivisors at value[15:14]
   for subdivisors 0, 0.5, 0.25, 0.125
*/
static int ftdi_to_clkbits(int baudrate, unsigned int clk, int clk_div, unsigned long *encoded_divisor)
{
    static const char frac_code[8] = {0, 3, 2, 4, 1, 5, 6, 7};
    int best_baud = 0;
    int divisor, best_divisor;
    if (baudrate >=  clk/clk_div)
    {
        *encoded_divisor = 0;
        best_baud = clk/clk_div;
    }
    else if (baudrate >=  clk/(clk_div + clk_div/2))
    {
        *encoded_divisor = 1;
        best_baud = clk/(clk_div + clk_div/2);
    }
    else if (baudrate >=  clk/(2*clk_div))
    {
        *encoded_divisor = 2;
        best_baud = clk/(2*clk_div);
    }
    else
    {
        /* We divide by 16 to have 3 fractional bits and one bit for rounding */
        divisor = clk*16/clk_div / baudrate;
        if (divisor & 1) /* Decide if to round up or down*/
            best_divisor = divisor /2 +1;
        else
            best_divisor = divisor/2;
        if(best_divisor > 0x20000)
            best_divisor = 0x1ffff;
        best_baud = clk*16/clk_div/best_divisor;
        if (best_baud & 1) /* Decide if to round up or down*/
            best_baud = best_baud /2 +1;
        else
            best_baud = best_baud /2;
        *encoded_divisor = (best_divisor >> 3) | (frac_code[best_divisor & 0x7] << 14);
    }
    return best_baud;
}
/**
    ftdi_convert_baudrate returns nearest supported baud rate to that requested.
    Function is only used internally
    \internal
*/
static int ftdi_convert_baudrate(int baudrate, struct ftdi_context *ftdi,
                                 unsigned short *value, unsigned short *index)
{
    int best_baud;
    unsigned long encoded_divisor;

    if (baudrate <= 0)
    {
        // Return error
        return -1;
    }

#define H_CLK 120000000
#define C_CLK  48000000
    if ((ftdi->type == TYPE_2232H) || (ftdi->type == TYPE_4232H) || (ftdi->type == TYPE_232H))
    {
        if(baudrate*10 > H_CLK /0x3fff)
        {
            /* On H Devices, use 12 000 000 Baudrate when possible
               We have a 14 bit divisor, a 1 bit divisor switch (10 or 16)
               three fractional bits and a 120 MHz clock
               Assume AN_120 "Sub-integer divisors between 0 and 2 are not allowed" holds for
               DIV/10 CLK too, so /1, /1.5 and /2 can be handled the same*/
            best_baud = ftdi_to_clkbits(baudrate, H_CLK, 10, &encoded_divisor);
            encoded_divisor |= 0x20000; /* switch on CLK/10*/
        }
        else
            best_baud = ftdi_to_clkbits(baudrate, C_CLK, 16, &encoded_divisor);
    }
    else if ((ftdi->type == TYPE_BM) || (ftdi->type == TYPE_2232C) || (ftdi->type == TYPE_R) || (ftdi->type == TYPE_230X))
    {
        best_baud = ftdi_to_clkbits(baudrate, C_CLK, 16, &encoded_divisor);
    }
    else
    {
        best_baud = ftdi_to_clkbits_AM(baudrate, &encoded_divisor);
    }
    // Split into "value" and "index" values
    *value = (unsigned short)(encoded_divisor & 0xFFFF);
    if (ftdi->type == TYPE_2232H || ftdi->type == TYPE_4232H || ftdi->type == TYPE_232H)
    {
        *index = (unsigned short)(encoded_divisor >> 8);
        *index &= 0xFF00;
        *index |= ftdi->index;
    }
    else
        *index = (unsigned short)(encoded_divisor >> 16);

    printf("baud: %d => (type=%d,index=%d,divisor=%lu/0x%lx,actual=%d)\n",
    	baudrate, ftdi->type, ftdi->index, encoded_divisor, encoded_divisor, best_baud);


    // Return the nearest baud rate
    return best_baud;
}

/**
    Sets the chip baud rate

    \param ftdi pointer to ftdi_context
    \param baudrate baud rate to set

    \retval  0: all fine
    \retval -1: invalid baudrate
    \retval -2: setting baudrate failed
    \retval -3: USB device unavailable
*/
int ftdi_set_baudrate(struct ftdi_context *ftdi, int baudrate)
{
    unsigned short value, index;
    int actual_baudrate;

    if (ftdi == NULL)
        ftdi_error_return(-3, "USB device unavailable");

    if (ftdi->bitbang_enabled)
    {
        baudrate = baudrate*4;
    }

    actual_baudrate = ftdi_convert_baudrate(baudrate, ftdi, &value, &index);
    if (actual_baudrate <= 0)
        ftdi_error_return (-1, "Silly baudrate <= 0.");

    // Check within tolerance (about 5%)
    if ((actual_baudrate * 2 < baudrate /* Catch overflows */ )
            || ((actual_baudrate < baudrate)
                ? (actual_baudrate * 21 < baudrate * 20)
                : (baudrate * 21 < actual_baudrate * 20)))
        ftdi_error_return (-1, "Unsupported baudrate. Note: bitbang baudrates are automatically multiplied by 4");

    ftdi->baudrate = baudrate;
    return 0;
}

/**
    Enable/disable bitbang modes.

    \param ftdi pointer to ftdi_context
    \param bitmask Bitmask to configure lines.
           HIGH/ON value configures a line as output.
    \param mode Bitbang mode: use the values defined in \ref ftdi_mpsse_mode

    \retval  0: all fine
    \retval -1: can't enable bitbang mode
    \retval -2: USB device unavailable
*/
int ftdi_set_bitmode(struct ftdi_context *ftdi, unsigned char bitmask, unsigned char mode)
{
    unsigned short usb_val;

    if (ftdi == NULL || ftdi->usb_dev == NULL)
        ftdi_error_return(-2, "USB device unavailable");

    usb_val = bitmask; // low byte: bitmask
    usb_val |= (mode << 8);

    ftdi->bitbang_mode = mode;
    ftdi->bitbang_enabled = (mode == BITMODE_RESET) ? 0 : 1;
    return 0;
}

/**
    Disable bitbang mode.

    \param ftdi pointer to ftdi_context

    \retval  0: all fine
    \retval -1: can't disable bitbang mode
    \retval -2: USB device unavailable
*/
int ftdi_disable_bitbang(struct ftdi_context *ftdi)
{
    if (ftdi == NULL || ftdi->usb_dev == NULL)
        ftdi_error_return(-2, "USB device unavailable");

    ftdi->bitbang_enabled = 0;
    return 0;
}

void ftdi_print(struct ftdi_context *ftdi) {
	printf("\nftdi->\n");
	printf("  type = %d\n", ftdi->type);
	printf("  baudrate = %d\n", ftdi->baudrate);
	printf("  bitbang_enabled = %d\n", ftdi->bitbang_enabled);
	printf("  interface = %d\n", ftdi->interface);
	printf("  index = %d\n", ftdi->index);
	printf("\n");
}

int main(int argc, char **argv) {
	struct ftdi_context *ftdi = ftdi_new();
	ftdi_init(ftdi);

	ftdi->type = TYPE_R;
	ftdi->bitbang_mode = 1;
	ftdi_print(ftdi);

	ftdi_set_baudrate(ftdi, 9600);
	ftdi_print(ftdi);

	ftdi_set_baudrate(ftdi, 19200);
	ftdi_print(ftdi);

	ftdi->bitbang_enabled = 1;
	ftdi_set_baudrate(ftdi, 19200);
	ftdi_print(ftdi);

	free(ftdi);
}
