package ceri.serial.ftdi;

import static ceri.serial.ftdi.Ftdi.FTDI_DEVICE_OUT_REQTYPE;
import static ceri.serial.ftdi.FtdiChipType.TYPE_230X;
import static ceri.serial.ftdi.FtdiChipType.TYPE_R;
import static ceri.serial.ftdi.RequestType.SIO_ERASE_EEPROM_REQUEST;
import static ceri.serial.ftdi.RequestType.SIO_WRITE_EEPROM_REQUEST;
import com.sun.jna.Pointer;

public class FtdiEeprom {

	/* Even on 93xx66 at max 256 bytes are used (AN_121) */
	public static final int FTDI_MAX_EEPROM_SIZE = 256;
	/** Max Power adjustment factor. */
	public static final int MAX_POWER_MILLIAMP_PER_UNIT = 2;

	int vendor_id;
	int product_id;

	/**
	 * Was the eeprom structure initialized for the actual connected device?
	 **/
	int initialized_for_connected_device;

	int self_powered;
	int remote_wakeup;
	int is_not_pnp;
	/* Suspend on DBUS7 Low */
	int suspend_dbus7;
	int in_is_isochronous;
	int out_is_isochronous;
	int suspend_pull_downs;
	int use_serial;
	int usb_version;
	/** Use usb version on FT2232 devices */
	int use_usb_version;
	int max_power;
	/** manufacturer name */
	Pointer manufacturer;
	/** product name */
	Pointer product;
	/** serial number */
	Pointer serial;

	/* 2232D/H specific */
	/*
	 * Hardware type, 0 = RS232 Uart, 1 = 245 FIFO, 2 = CPU FIFO, 4 = OPTO Isolate
	 */
	int channel_a_type;
	int channel_b_type;
	/* Driver Type, 1 = VCP */
	int channel_a_driver;
	int channel_b_driver;
	int channel_c_driver;
	int channel_d_driver;
	/* 4232H specific */
	int channel_a_rs485enable;
	int channel_b_rs485enable;
	int channel_c_rs485enable;
	int channel_d_rs485enable;

	/* Special function of FT232R/FT232H devices (and possibly others as well) */
	/** CBUS pin function. See CBUS_xxx defines. */
	int[] cbus_function = new int[10];
	/** Select hight current drive on R devices. */
	int high_current;
	/** Select hight current drive on A channel (2232C */
	int high_current_a;
	/** Select hight current drive on B channel (2232C). */
	int high_current_b;
	/** Select inversion of data lines (bitmask). */
	int invert;
	/** Enable external oscillator. */
	int external_oscillator;

	/* 2232H/4432H Group specific values */
	/*
	 * Group0 is AL on 2322H and A on 4232H Group1 is AH on 2232H and B on 4232H Group2 is BL on
	 * 2322H and C on 4232H Group3 is BH on 2232H and C on 4232H
	 */
	int group0_drive;
	int group0_schmitt;
	int group0_slew;
	int group1_drive;
	int group1_schmitt;
	int group1_slew;
	int group2_drive;
	int group2_schmitt;
	int group2_slew;
	int group3_drive;
	int group3_schmitt;
	int group3_slew;

	int powersave;

	int clock_polarity;
	int data_order;
	int flow_control;

	/** user data **/
	int user_data_addr;
	int user_data_size;
	Pointer user_data;

	/**
	 * eeprom size in bytes. This doesn't get stored in the eeprom but is the only way to pass it to
	 * ftdi_eeprom_build.
	 */
	int size;
	/* EEPROM Type 0x46 for 93xx46, 0x56 for 93xx56 and 0x66 for 93xx66 */
	int chip;
	byte[] buf = new byte[FTDI_MAX_EEPROM_SIZE];
	int release_number;
	
	
	///**
//  Init eeprom with default values for the connected device
//  \param ftdi pointer to ftdi_context
//  \param manufacturer String to use as Manufacturer
//  \param product String to use as Product description
//  \param serial String to use as Serial number description
//
//  \retval  0: all fine
//  \retval -1: No struct ftdi_context
//  \retval -2: No struct ftdi_eeprom
//  \retval -3: No connected device or device not yet opened
//*/
//int ftdi_eeprom_initdefaults(FtdiContext ftdi, char * manufacturer,
//                           char * product, char * serial) {
//  struct ftdi_eeprom *eeprom;
//
//  if (ftdi == null)
//      ftdi_error_return(-1, "No struct ftdi_context");
//
//  if (ftdi.eeprom == null)
//      ftdi_error_return(-2,"No struct ftdi_eeprom");
//
//  eeprom = ftdi.eeprom;
//  memset(eeprom, 0, sizeof(struct ftdi_eeprom));
//
//  if (ftdi.usbDev == null)
//      ftdi_error_return(-3, "No connected device or device not yet opened");
//
//  eeprom.vendor_id = 0x0403;
//  eeprom.use_serial = 1;
//  if ((ftdi.type == TYPE_AM) || (ftdi.type == TYPE_BM) ||
//          (ftdi.type == TYPE_R))
//      eeprom.product_id = 0x6001;
//  else if (ftdi.type == TYPE_4232H)
//      eeprom.product_id = 0x6011;
//  else if (ftdi.type == TYPE_232H)
//      eeprom.product_id = 0x6014;
//  else if (ftdi.type == TYPE_230X)
//      eeprom.product_id = 0x6015;
//  else
//      eeprom.product_id = 0x6010;
//
//  if (ftdi.type == TYPE_AM)
//      eeprom.usb_version = 0x0101;
//  else
//      eeprom.usb_version = 0x0200;
//  eeprom.max_power = 100;
//
//  if (eeprom.manufacturer)
//      free (eeprom.manufacturer);
//  eeprom.manufacturer = null;
//  if (manufacturer) {
//      eeprom.manufacturer = (char *)malloc(strlen(manufacturer)+1);
//      if (eeprom.manufacturer)
//          strcpy(eeprom.manufacturer, manufacturer);
//  }
//
//  if (eeprom.product)
//      free (eeprom.product);
//  eeprom.product = null;
//  if(product) {
//      eeprom.product = (char *)malloc(strlen(product)+1);
//      if (eeprom.product)
//          strcpy(eeprom.product, product);
//  }
//  else {
//      const char* default_product;
//      switch(ftdi.type) {
//          case TYPE_AM:    default_product = "AM"; break;
//          case TYPE_BM:    default_product = "BM"; break;
//          case TYPE_2232C: default_product = "Dual RS232"; break;
//          case TYPE_R:     default_product = "FT232R USB UART"; break;
//          case TYPE_2232H: default_product = "Dual RS232-HS"; break;
//          case TYPE_4232H: default_product = "FT4232H"; break;
//          case TYPE_232H:  default_product = "Single-RS232-HS"; break;
//          case TYPE_230X:  default_product = "FT230X Basic UART"; break;
//          default:
//              ftdi_error_return(-3, "Unknown chip type");
//      }
//      eeprom.product = (char *)malloc(strlen(default_product) +1);
//      if (eeprom.product)
//          strcpy(eeprom.product, default_product);
//  }
//
//  if (eeprom.serial)
//      free (eeprom.serial);
//  eeprom.serial = null;
//  if (serial) {
//      eeprom.serial = (char *)malloc(strlen(serial)+1);
//      if (eeprom.serial)
//          strcpy(eeprom.serial, serial);
//  }
//
//  if (ftdi.type == TYPE_R) {
//      eeprom.max_power = 90;
//      eeprom.size = 0x80;
//      eeprom.cbus_function[0] = CBUS_TXLED;
//      eeprom.cbus_function[1] = CBUS_RXLED;
//      eeprom.cbus_function[2] = CBUS_TXDEN;
//      eeprom.cbus_function[3] = CBUS_PWREN;
//      eeprom.cbus_function[4] = CBUS_SLEEP;
//  }
//  else if (ftdi.type == TYPE_230X) {
//      eeprom.max_power = 90;
//      eeprom.size = 0x100;
//      eeprom.cbus_function[0] = CBUSX_TXDEN;
//      eeprom.cbus_function[1] = CBUSX_RXLED;
//      eeprom.cbus_function[2] = CBUSX_TXLED;
//      eeprom.cbus_function[3] = CBUSX_SLEEP;
//  }
//  else {
//      if(ftdi.type == TYPE_232H) {
//          int i;
//          for (i=0; i<10; i++)
//              eeprom.cbus_function[i] = CBUSH_TRISTATE;
//      }
//      eeprom.size = -1;
//  }
//  switch (ftdi.type) {
//      case TYPE_AM:
//          eeprom.release_number = 0x0200;
//          break;
//      case TYPE_BM:
//          eeprom.release_number = 0x0400;
//          break;
//      case TYPE_2232C:
//          eeprom.release_number = 0x0500;
//          break;
//      case TYPE_R:
//          eeprom.release_number = 0x0600;
//          break;
//      case TYPE_2232H:
//          eeprom.release_number = 0x0700;
//          break;
//      case TYPE_4232H:
//          eeprom.release_number = 0x0800;
//          break;
//      case TYPE_232H:
//          eeprom.release_number = 0x0900;
//          break;
//      case TYPE_230X:
//          eeprom.release_number = 0x1000;
//          break;
//      default:
//          eeprom.release_number = 0x00;
//  }
//  return 0;
//}
//
//int ftdi_eeprom_set_strings(FtdiContext ftdi, char * manufacturer,
//                          char * product, char * serial) {
//  struct ftdi_eeprom *eeprom;
//
//  if (ftdi == null)
//      ftdi_error_return(-1, "No struct ftdi_context");
//
//  if (ftdi.eeprom == null)
//      ftdi_error_return(-2,"No struct ftdi_eeprom");
//
//  eeprom = ftdi.eeprom;
//
//  if (ftdi.usbDev == null)
//      ftdi_error_return(-3, "No connected device or device not yet opened");
//
//  if (manufacturer) {
//      if (eeprom.manufacturer)
//          free (eeprom.manufacturer);
//      eeprom.manufacturer = (char *)malloc(strlen(manufacturer)+1);
//      if (eeprom.manufacturer)
//          strcpy(eeprom.manufacturer, manufacturer);
//  }
//
//  if(product) {
//      if (eeprom.product)
//          free (eeprom.product);
//      eeprom.product = (char *)malloc(strlen(product)+1);
//      if (eeprom.product)
//          strcpy(eeprom.product, product);
//  }
//
//  if (serial) {
//      if (eeprom.serial)
//          free (eeprom.serial);
//      eeprom.serial = (char *)malloc(strlen(serial)+1);
//      if (eeprom.serial) {
//          strcpy(eeprom.serial, serial);
//          eeprom.use_serial = 1;
//      }
//  }
//  return 0;
//}
//
///**
//  Return device ID strings from the eeprom. Device needs to be connected.
//
//  The parameters manufacturer, description and serial may be null
//  or pointer to buffers to store the fetched strings.
//
//  \param ftdi pointer to ftdi_context
//  \param manufacturer Store manufacturer string here if not null
//  \param mnf_len Buffer size of manufacturer string
//  \param product Store product description string here if not null
//  \param prod_len Buffer size of product description string
//  \param serial Store serial string here if not null
//  \param serial_len Buffer size of serial string
//
//  \retval   0: all fine
//  \retval  -1: ftdi context invalid
//  \retval  -2: ftdi eeprom buffer invalid
//*/
//int ftdi_eeprom_get_strings(FtdiContext ftdi,
//                          char *manufacturer, int mnf_len,
//                          char *product, int prod_len,
//                          char *serial, int serial_len) {
//  struct ftdi_eeprom *eeprom;
//
//  if (ftdi == null)
//      ftdi_error_return(-1, "No struct ftdi_context");
//  if (ftdi.eeprom == null)
//      ftdi_error_return(-2, "No struct ftdi_eeprom");
//
//  eeprom = ftdi.eeprom;
//
//  if (manufacturer) {
//      strncpy(manufacturer, eeprom.manufacturer, mnf_len);
//      if (mnf_len > 0)
//          manufacturer[mnf_len - 1] = '\0';
//  }
//
//  if (product) {
//      strncpy(product, eeprom.product, prod_len);
//      if (prod_len > 0)
//          product[prod_len - 1] = '\0';
//  }
//
//  if (serial) {
//      strncpy(serial, eeprom.serial, serial_len);
//      if (serial_len > 0)
//          serial[serial_len - 1] = '\0';
//  }
//
//  return 0;
//}
//
///*FTD2XX doesn't check for values not fitting in the ACBUS Signal options*/
//void set_ft232h_cbus(struct ftdi_eeprom *eeprom, unsigned char * output) {
//  int i;
//  for(i=0; i<5; i++) {
//      int mode_low, mode_high;
//      if (eeprom.cbus_function[2*i]> CBUSH_CLK7_5)
//          mode_low = CBUSH_TRISTATE;
//      else
//          mode_low = eeprom.cbus_function[2*i];
//      if (eeprom.cbus_function[2*i+1]> CBUSH_CLK7_5)
//          mode_high = CBUSH_TRISTATE;
//      else
//          mode_high = eeprom.cbus_function[2*i+1];
//
//      output[0x18+i] = (mode_high <<4) | mode_low;
//  }
//}
///* Return the bits for the encoded EEPROM Structure of a requested Mode
//*
//*/
//static unsigned char type2bit(unsigned char type, enum ftdi_chip_type chip) {
//  switch (chip) {
//      case TYPE_2232H:
//      case TYPE_2232C: {
//          switch (type) {
//              case CHANNEL_IS_UART: return 0;
//              case CHANNEL_IS_FIFO: return 0x01;
//              case CHANNEL_IS_OPTO: return 0x02;
//              case CHANNEL_IS_CPU : return 0x04;
//              default: return 0;
//          }
//      }
//      case TYPE_232H: {
//          switch (type) {
//              case CHANNEL_IS_UART   : return 0;
//              case CHANNEL_IS_FIFO   : return 0x01;
//              case CHANNEL_IS_OPTO   : return 0x02;
//              case CHANNEL_IS_CPU    : return 0x04;
//              case CHANNEL_IS_FT1284 : return 0x08;
//              default: return 0;
//          }
//      }
//      case TYPE_R: {
//          switch (type) {
//              case CHANNEL_IS_UART   : return 0;
//              case CHANNEL_IS_FIFO   : return 0x01;
//              default: return 0;
//          }
//      }
//      case TYPE_230X: /* FT230X is only UART */
//      default: return 0;
//  }
//  return 0;
//}
//
///**
//  Build binary buffer from ftdi_eeprom structure.
//  Output is suitable for ftdi_write_eeprom().
//
//  \param ftdi pointer to ftdi_context
//
//  \retval >=0: size of eeprom user area in bytes
//  \retval -1: eeprom size (128 bytes) exceeded by custom strings
//  \retval -2: Invalid eeprom or ftdi pointer
//  \retval -3: Invalid cbus function setting     (FIXME: Not in the code?)
//  \retval -4: Chip doesn't support invert       (FIXME: Not in the code?)
//  \retval -5: Chip doesn't support high current drive         (FIXME: Not in the code?)
//  \retval -6: No connected EEPROM or EEPROM Type unknown
//*/
//int ftdi_eeprom_build(FtdiContext ftdi) {
//  unsigned char i, j, eeprom_size_mask;
//  unsigned short checksum, value;
//  unsigned char manufacturer_size = 0, product_size = 0, serial_size = 0;
//  int user_area_size, free_start, free_end;
//  struct ftdi_eeprom *eeprom;
//  unsigned char * output;
//
//  if (ftdi == null)
//      ftdi_error_return(-2,"No context");
//  if (ftdi.eeprom == null)
//      ftdi_error_return(-2,"No eeprom structure");
//
//  eeprom= ftdi.eeprom;
//  output = eeprom.buf;
//
//  if (eeprom.chip == -1)
//      ftdi_error_return(-6,"No connected EEPROM or EEPROM type unknown");
//
//  if (eeprom.size == -1) {
//      if ((eeprom.chip == 0x56) || (eeprom.chip == 0x66))
//          eeprom.size = 0x100;
//      else
//          eeprom.size = 0x80;
//  }
//
//  if (eeprom.manufacturer != null)
//      manufacturer_size = strlen(eeprom.manufacturer);
//  if (eeprom.product != null)
//      product_size = strlen(eeprom.product);
//  if (eeprom.serial != null)
//      serial_size = strlen(eeprom.serial);
//
//  // eeprom size check
//  switch (ftdi.type) {
//      case TYPE_AM:
//      case TYPE_BM:
//      case TYPE_R:
//          user_area_size = 96;    // base size for strings (total of 48 characters)
//          break;
//      case TYPE_2232C:
//          user_area_size = 90;     // two extra config bytes and 4 bytes PnP stuff
//          break;
//      case TYPE_230X:
//          user_area_size = 88;     // four extra config bytes + 4 bytes PnP stuff
//          break;
//      case TYPE_2232H:            // six extra config bytes + 4 bytes PnP stuff
//      case TYPE_4232H:
//          user_area_size = 86;
//          break;
//      case TYPE_232H:
//          user_area_size = 80;
//          break;
//      default:
//          user_area_size = 0;
//          break;
//  }
//  user_area_size  -= (manufacturer_size + product_size + serial_size) * 2;
//
//  if (user_area_size < 0)
//      ftdi_error_return(-1,"eeprom size exceeded");
//
//  // empty eeprom
//  if (ftdi.type == TYPE_230X) {
//      /* FT230X have a reserved section in the middle of the MTP,
//         which cannot be written to, but must be included in the checksum */
//      memset(ftdi.eeprom.buf, 0, 0x80);
//      memset((ftdi.eeprom.buf + 0xa0), 0, (FTDI_MAX_EEPROM_SIZE - 0xa0));
//  }
//  else {
//      memset(ftdi.eeprom.buf, 0, FTDI_MAX_EEPROM_SIZE);
//  }
//
//  // Bytes and Bits set for all Types
//
//  // Addr 02: Vendor ID
//  output[0x02] = eeprom.vendor_id;
//  output[0x03] = eeprom.vendor_id >> 8;
//
//  // Addr 04: Product ID
//  output[0x04] = eeprom.product_id;
//  output[0x05] = eeprom.product_id >> 8;
//
//  // Addr 06: Device release number (0400h for BM features)
//  output[0x06] = eeprom.release_number;
//  output[0x07] = eeprom.release_number >> 8;
//
//  // Addr 08: Config descriptor
//  // Bit 7: always 1
//  // Bit 6: 1 if this device is self powered, 0 if bus powered
//  // Bit 5: 1 if this device uses remote wakeup
//  // Bit 4-0: reserved - 0
//  j = 0x80;
//  if (eeprom.self_powered)
//      j |= 0x40;
//  if (eeprom.remote_wakeup)
//      j |= 0x20;
//  output[0x08] = j;
//
//  // Addr 09: Max power consumption: max power = value * 2 mA
//  output[0x09] = eeprom.max_power / MAX_POWER_MILLIAMP_PER_UNIT;
//
//  if ((ftdi.type != TYPE_AM) && (ftdi.type != TYPE_230X)) {
//      // Addr 0A: Chip configuration
//      // Bit 7: 0 - reserved
//      // Bit 6: 0 - reserved
//      // Bit 5: 0 - reserved
//      // Bit 4: 1 - Change USB version
//      // Bit 3: 1 - Use the serial number string
//      // Bit 2: 1 - Enable suspend pull downs for lower power
//      // Bit 1: 1 - Out EndPoint is Isochronous
//      // Bit 0: 1 - In EndPoint is Isochronous
//      //
//      j = 0;
//      if (eeprom.in_is_isochronous)
//          j = j | 1;
//      if (eeprom.out_is_isochronous)
//          j = j | 2;
//      output[0x0A] = j;
//  }
//
//  // Dynamic content
//  // Strings start at 0x94 (TYPE_AM, TYPE_BM)
//  // 0x96 (TYPE_2232C), 0x98 (TYPE_R) and 0x9a (TYPE_x232H)
//  // 0xa0 (TYPE_232H)
//  i = 0;
//  switch (ftdi.type) {
//      case TYPE_2232H:
//      case TYPE_4232H:
//          i += 2;
//      case TYPE_R:
//          i += 2;
//      case TYPE_2232C:
//          i += 2;
//      case TYPE_AM:
//      case TYPE_BM:
//          i += 0x94;
//          break;
//      case TYPE_232H:
//      case TYPE_230X:
//          i = 0xa0;
//          break;
//  }
//  /* Wrap around 0x80 for 128 byte EEPROMS (Internale and 93x46) */
//  eeprom_size_mask = eeprom.size -1;
//  free_end = i & eeprom_size_mask;
//
//  // Addr 0E: Offset of the manufacturer string + 0x80, calculated later
//  // Addr 0F: Length of manufacturer string
//  // Output manufacturer
//  output[0x0E] = i;  // calculate offset
//  output[i & eeprom_size_mask] = manufacturer_size*2 + 2, i++;
//  output[i & eeprom_size_mask] = 0x03, i++; // type: string
//  for (j = 0; j < manufacturer_size; j++) {
//      output[i & eeprom_size_mask] = eeprom.manufacturer[j], i++;
//      output[i & eeprom_size_mask] = 0x00, i++;
//  }
//  output[0x0F] = manufacturer_size*2 + 2;
//
//  // Addr 10: Offset of the product string + 0x80, calculated later
//  // Addr 11: Length of product string
//  output[0x10] = i | 0x80;  // calculate offset
//  output[i & eeprom_size_mask] = product_size*2 + 2, i++;
//  output[i & eeprom_size_mask] = 0x03, i++;
//  for (j = 0; j < product_size; j++) {
//      output[i & eeprom_size_mask] = eeprom.product[j], i++;
//      output[i & eeprom_size_mask] = 0x00, i++;
//  }
//  output[0x11] = product_size*2 + 2;
//
//  // Addr 12: Offset of the serial string + 0x80, calculated later
//  // Addr 13: Length of serial string
//  output[0x12] = i | 0x80; // calculate offset
//  output[i & eeprom_size_mask] = serial_size*2 + 2, i++;
//  output[i & eeprom_size_mask] = 0x03, i++;
//  for (j = 0; j < serial_size; j++) {
//      output[i & eeprom_size_mask] = eeprom.serial[j], i++;
//      output[i & eeprom_size_mask] = 0x00, i++;
//  }
//
//  // Legacy port name and PnP fields for FT2232 and newer chips
//  if (ftdi.type > TYPE_BM) {
//      output[i & eeprom_size_mask] = 0x02; /* as seen when written with FTD2XX */
//      i++;
//      output[i & eeprom_size_mask] = 0x03; /* as seen when written with FTD2XX */
//      i++;
//      output[i & eeprom_size_mask] = eeprom.is_not_pnp; /* as seen when written with FTD2XX */
//      i++;
//  }
//
//  output[0x13] = serial_size*2 + 2;
//
//  if (ftdi.type > TYPE_AM) /* use_serial not used in AM devices */ {
//      if (eeprom.use_serial)
//          output[0x0A] |= USE_SERIAL_NUM;
//      else
//          output[0x0A] &= ~USE_SERIAL_NUM;
//  }
//
//  /* Bytes and Bits specific to (some) types
//     Write linear, as this allows easier fixing*/
//  switch (ftdi.type) {
//      case TYPE_AM:
//          break;
//      case TYPE_BM:
//          output[0x0C] = eeprom.usb_version & 0xff;
//          output[0x0D] = (eeprom.usb_version>>8) & 0xff;
//          if (eeprom.use_usb_version)
//              output[0x0A] |= USE_USB_VERSION_BIT;
//          else
//              output[0x0A] &= ~USE_USB_VERSION_BIT;
//
//          break;
//      case TYPE_2232C:
//
//          output[0x00] = type2bit(eeprom.channel_a_type, TYPE_2232C);
//          if ( eeprom.channel_a_driver == DRIVER_VCP)
//              output[0x00] |= DRIVER_VCP;
//          else
//              output[0x00] &= ~DRIVER_VCP;
//
//          if ( eeprom.high_current_a == HIGH_CURRENT_DRIVE)
//              output[0x00] |= HIGH_CURRENT_DRIVE;
//          else
//              output[0x00] &= ~HIGH_CURRENT_DRIVE;
//
//          output[0x01] = type2bit(eeprom.channel_b_type, TYPE_2232C);
//          if ( eeprom.channel_b_driver == DRIVER_VCP)
//              output[0x01] |= DRIVER_VCP;
//          else
//              output[0x01] &= ~DRIVER_VCP;
//
//          if ( eeprom.high_current_b == HIGH_CURRENT_DRIVE)
//              output[0x01] |= HIGH_CURRENT_DRIVE;
//          else
//              output[0x01] &= ~HIGH_CURRENT_DRIVE;
//
//          if (eeprom.in_is_isochronous)
//              output[0x0A] |= 0x1;
//          else
//              output[0x0A] &= ~0x1;
//          if (eeprom.out_is_isochronous)
//              output[0x0A] |= 0x2;
//          else
//              output[0x0A] &= ~0x2;
//          if (eeprom.suspend_pull_downs)
//              output[0x0A] |= 0x4;
//          else
//              output[0x0A] &= ~0x4;
//          if (eeprom.use_usb_version)
//              output[0x0A] |= USE_USB_VERSION_BIT;
//          else
//              output[0x0A] &= ~USE_USB_VERSION_BIT;
//
//          output[0x0C] = eeprom.usb_version & 0xff;
//          output[0x0D] = (eeprom.usb_version>>8) & 0xff;
//          output[0x14] = eeprom.chip;
//          break;
//      case TYPE_R:
//          output[0x00] = type2bit(eeprom.channel_a_type, TYPE_R);
//          if (eeprom.high_current == HIGH_CURRENT_DRIVE_R)
//              output[0x00] |= HIGH_CURRENT_DRIVE_R;
//          if (eeprom.external_oscillator)
//              output[0x00] |= 0x02;
//          output[0x01] = 0x40; /* Hard coded Endpoint Size*/
//
//          if (eeprom.suspend_pull_downs)
//              output[0x0A] |= 0x4;
//          else
//              output[0x0A] &= ~0x4;
//          output[0x0B] = eeprom.invert;
//          output[0x0C] = eeprom.usb_version & 0xff;
//          output[0x0D] = (eeprom.usb_version>>8) & 0xff;
//
//          if (eeprom.cbus_function[0] > CBUS_BB_RD)
//              output[0x14] = CBUS_TXLED;
//          else
//              output[0x14] = eeprom.cbus_function[0];
//
//          if (eeprom.cbus_function[1] > CBUS_BB_RD)
//              output[0x14] |= CBUS_RXLED<<4;
//          else
//              output[0x14] |= eeprom.cbus_function[1]<<4;
//
//          if (eeprom.cbus_function[2] > CBUS_BB_RD)
//              output[0x15] = CBUS_TXDEN;
//          else
//              output[0x15] = eeprom.cbus_function[2];
//
//          if (eeprom.cbus_function[3] > CBUS_BB_RD)
//              output[0x15] |= CBUS_PWREN<<4;
//          else
//              output[0x15] |= eeprom.cbus_function[3]<<4;
//
//          if (eeprom.cbus_function[4] > CBUS_CLK6)
//              output[0x16] = CBUS_SLEEP;
//          else
//              output[0x16] = eeprom.cbus_function[4];
//          break;
//      case TYPE_2232H:
//          output[0x00] = type2bit(eeprom.channel_a_type, TYPE_2232H);
//          if ( eeprom.channel_a_driver == DRIVER_VCP)
//              output[0x00] |= DRIVER_VCP;
//          else
//              output[0x00] &= ~DRIVER_VCP;
//
//          output[0x01] = type2bit(eeprom.channel_b_type, TYPE_2232H);
//          if ( eeprom.channel_b_driver == DRIVER_VCP)
//              output[0x01] |= DRIVER_VCP;
//          else
//              output[0x01] &= ~DRIVER_VCP;
//          if (eeprom.suspend_dbus7 == SUSPEND_DBUS7_BIT)
//              output[0x01] |= SUSPEND_DBUS7_BIT;
//          else
//              output[0x01] &= ~SUSPEND_DBUS7_BIT;
//
//          if (eeprom.suspend_pull_downs)
//              output[0x0A] |= 0x4;
//          else
//              output[0x0A] &= ~0x4;
//
//          if (eeprom.group0_drive > DRIVE_16MA)
//              output[0x0c] |= DRIVE_16MA;
//          else
//              output[0x0c] |= eeprom.group0_drive;
//          if (eeprom.group0_schmitt == IS_SCHMITT)
//              output[0x0c] |= IS_SCHMITT;
//          if (eeprom.group0_slew == SLOW_SLEW)
//              output[0x0c] |= SLOW_SLEW;
//
//          if (eeprom.group1_drive > DRIVE_16MA)
//              output[0x0c] |= DRIVE_16MA<<4;
//          else
//              output[0x0c] |= eeprom.group1_drive<<4;
//          if (eeprom.group1_schmitt == IS_SCHMITT)
//              output[0x0c] |= IS_SCHMITT<<4;
//          if (eeprom.group1_slew == SLOW_SLEW)
//              output[0x0c] |= SLOW_SLEW<<4;
//
//          if (eeprom.group2_drive > DRIVE_16MA)
//              output[0x0d] |= DRIVE_16MA;
//          else
//              output[0x0d] |= eeprom.group2_drive;
//          if (eeprom.group2_schmitt == IS_SCHMITT)
//              output[0x0d] |= IS_SCHMITT;
//          if (eeprom.group2_slew == SLOW_SLEW)
//              output[0x0d] |= SLOW_SLEW;
//
//          if (eeprom.group3_drive > DRIVE_16MA)
//              output[0x0d] |= DRIVE_16MA<<4;
//          else
//              output[0x0d] |= eeprom.group3_drive<<4;
//          if (eeprom.group3_schmitt == IS_SCHMITT)
//              output[0x0d] |= IS_SCHMITT<<4;
//          if (eeprom.group3_slew == SLOW_SLEW)
//              output[0x0d] |= SLOW_SLEW<<4;
//
//          output[0x18] = eeprom.chip;
//
//          break;
//      case TYPE_4232H:
//          if (eeprom.channel_a_driver == DRIVER_VCP)
//              output[0x00] |= DRIVER_VCP;
//          else
//              output[0x00] &= ~DRIVER_VCP;
//          if (eeprom.channel_b_driver == DRIVER_VCP)
//              output[0x01] |= DRIVER_VCP;
//          else
//              output[0x01] &= ~DRIVER_VCP;
//          if (eeprom.channel_c_driver == DRIVER_VCP)
//              output[0x00] |= (DRIVER_VCP << 4);
//          else
//              output[0x00] &= ~(DRIVER_VCP << 4);
//          if (eeprom.channel_d_driver == DRIVER_VCP)
//              output[0x01] |= (DRIVER_VCP << 4);
//          else
//              output[0x01] &= ~(DRIVER_VCP << 4);
//
//          if (eeprom.suspend_pull_downs)
//              output[0x0a] |= 0x4;
//          else
//              output[0x0a] &= ~0x4;
//
//          if (eeprom.channel_a_rs485enable)
//              output[0x0b] |= CHANNEL_IS_RS485 << 0;
//          else
//              output[0x0b] &= ~(CHANNEL_IS_RS485 << 0);
//          if (eeprom.channel_b_rs485enable)
//              output[0x0b] |= CHANNEL_IS_RS485 << 1;
//          else
//              output[0x0b] &= ~(CHANNEL_IS_RS485 << 1);
//          if (eeprom.channel_c_rs485enable)
//              output[0x0b] |= CHANNEL_IS_RS485 << 2;
//          else
//              output[0x0b] &= ~(CHANNEL_IS_RS485 << 2);
//          if (eeprom.channel_d_rs485enable)
//              output[0x0b] |= CHANNEL_IS_RS485 << 3;
//          else
//              output[0x0b] &= ~(CHANNEL_IS_RS485 << 3);
//
//          if (eeprom.group0_drive > DRIVE_16MA)
//              output[0x0c] |= DRIVE_16MA;
//          else
//              output[0x0c] |= eeprom.group0_drive;
//          if (eeprom.group0_schmitt == IS_SCHMITT)
//              output[0x0c] |= IS_SCHMITT;
//          if (eeprom.group0_slew == SLOW_SLEW)
//              output[0x0c] |= SLOW_SLEW;
//
//          if (eeprom.group1_drive > DRIVE_16MA)
//              output[0x0c] |= DRIVE_16MA<<4;
//          else
//              output[0x0c] |= eeprom.group1_drive<<4;
//          if (eeprom.group1_schmitt == IS_SCHMITT)
//              output[0x0c] |= IS_SCHMITT<<4;
//          if (eeprom.group1_slew == SLOW_SLEW)
//              output[0x0c] |= SLOW_SLEW<<4;
//
//          if (eeprom.group2_drive > DRIVE_16MA)
//              output[0x0d] |= DRIVE_16MA;
//          else
//              output[0x0d] |= eeprom.group2_drive;
//          if (eeprom.group2_schmitt == IS_SCHMITT)
//              output[0x0d] |= IS_SCHMITT;
//          if (eeprom.group2_slew == SLOW_SLEW)
//              output[0x0d] |= SLOW_SLEW;
//
//          if (eeprom.group3_drive > DRIVE_16MA)
//              output[0x0d] |= DRIVE_16MA<<4;
//          else
//              output[0x0d] |= eeprom.group3_drive<<4;
//          if (eeprom.group3_schmitt == IS_SCHMITT)
//              output[0x0d] |= IS_SCHMITT<<4;
//          if (eeprom.group3_slew == SLOW_SLEW)
//              output[0x0d] |= SLOW_SLEW<<4;
//
//          output[0x18] = eeprom.chip;
//
//          break;
//      case TYPE_232H:
//          output[0x00] = type2bit(eeprom.channel_a_type, TYPE_232H);
//          if ( eeprom.channel_a_driver == DRIVER_VCP)
//              output[0x00] |= DRIVER_VCPH;
//          else
//              output[0x00] &= ~DRIVER_VCPH;
//          if (eeprom.powersave)
//              output[0x01] |= POWER_SAVE_DISABLE_H;
//          else
//              output[0x01] &= ~POWER_SAVE_DISABLE_H;
//
//          if (eeprom.suspend_pull_downs)
//              output[0x0a] |= 0x4;
//          else
//              output[0x0a] &= ~0x4;
//
//          if (eeprom.clock_polarity)
//              output[0x01] |= FT1284_CLK_IDLE_STATE;
//          else
//              output[0x01] &= ~FT1284_CLK_IDLE_STATE;
//          if (eeprom.data_order)
//              output[0x01] |= FT1284_DATA_LSB;
//          else
//              output[0x01] &= ~FT1284_DATA_LSB;
//          if (eeprom.flow_control)
//              output[0x01] |= FT1284_FLOW_CONTROL;
//          else
//              output[0x01] &= ~FT1284_FLOW_CONTROL;
//          if (eeprom.group0_drive > DRIVE_16MA)
//              output[0x0c] |= DRIVE_16MA;
//          else
//              output[0x0c] |= eeprom.group0_drive;
//          if (eeprom.group0_schmitt == IS_SCHMITT)
//              output[0x0c] |= IS_SCHMITT;
//          if (eeprom.group0_slew == SLOW_SLEW)
//              output[0x0c] |= SLOW_SLEW;
//
//          if (eeprom.group1_drive > DRIVE_16MA)
//              output[0x0d] |= DRIVE_16MA;
//          else
//              output[0x0d] |= eeprom.group1_drive;
//          if (eeprom.group1_schmitt == IS_SCHMITT)
//              output[0x0d] |= IS_SCHMITT;
//          if (eeprom.group1_slew == SLOW_SLEW)
//              output[0x0d] |= SLOW_SLEW;
//
//          set_ft232h_cbus(eeprom, output);
//
//          output[0x1e] = eeprom.chip;
//          fprintf(stderr,"FIXME: Build FT232H specific EEPROM settings\n");
//          break;
//      case TYPE_230X:
//          output[0x00] = 0x80; /* Actually, leave the default value */
//          /*FIXME: Make DBUS & CBUS Control configurable*/
//          output[0x0c] = 0;    /* DBUS drive 4mA, CBUS drive 4 mA like factory default */
//          for (j = 0; j <= 6; j++) {
//              output[0x1a + j] = eeprom.cbus_function[j];
//          }
//          output[0x0b] = eeprom.invert;
//          break;
//  }
//
//  /* First address without use */
//  free_start = 0;
//  switch (ftdi.type) {
//      case TYPE_230X:
//          free_start += 2;
//      case TYPE_232H:
//          free_start += 6;
//      case TYPE_2232H:
//      case TYPE_4232H:
//          free_start += 2;
//      case TYPE_R:
//          free_start += 2;
//      case TYPE_2232C:
//          free_start++;
//      case TYPE_AM:
//      case TYPE_BM:
//          free_start += 0x14;
//  }
//
//  /* Arbitrary user data */
//  if (eeprom.user_data && eeprom.user_data_size >= 0) {
//      if (eeprom.user_data_addr < free_start)
//          fprintf(stderr,"Warning, user data starts inside the generated data!\n");
//      if (eeprom.user_data_addr + eeprom.user_data_size >= free_end)
//          fprintf(stderr,"Warning, user data overlaps the strings area!\n");
//      if (eeprom.user_data_addr + eeprom.user_data_size > eeprom.size)
//          ftdi_error_return(-1,"eeprom size exceeded");
//      memcpy(output + eeprom.user_data_addr, eeprom.user_data, eeprom.user_data_size);
//  }
//
//  // calculate checksum
//  checksum = 0xAAAA;
//
//  for (i = 0; i < eeprom.size/2-1; i++) {
//      if ((ftdi.type == TYPE_230X) && (i == 0x12)) {
//          /* FT230X has a user section in the MTP which is not part of the checksum */
//          i = 0x40;
//      }
//      if ((ftdi.type == TYPE_230X) && (i >=  0x40) && (i < 0x50)) {
//          uint16_t data;
//          if (ftdi_read_eeprom_location(ftdi, i, &data)) {
//              fprintf(stderr, "Reading Factory Configuration Data failed\n");
//              i = 0x50;
//          }
//          value = data;
//      }
//      else {
//          value = output[i*2];
//          value += output[(i*2)+1] << 8;
//      }
//      checksum = value^checksum;
//      checksum = (checksum << 1) | (checksum >> 15);
//  }
//
//  output[eeprom.size-2] = checksum;
//  output[eeprom.size-1] = checksum >> 8;
//
//  eeprom.initialized_for_connected_device = 1;
//  return user_area_size;
//}
///* Decode the encoded EEPROM field for the FTDI Mode into a value for the abstracted
//* EEPROM structure
//*
//* FTD2XX doesn't allow to set multiple bits in the interface mode bitfield, and so do we
//*/
//static unsigned char bit2type(unsigned char bits) {
//  switch (bits) {
//      case   0: return CHANNEL_IS_UART;
//      case   1: return CHANNEL_IS_FIFO;
//      case   2: return CHANNEL_IS_OPTO;
//      case   4: return CHANNEL_IS_CPU;
//      case   8: return CHANNEL_IS_FT1284;
//      default:
//          fprintf(stderr," Unexpected value %d for Hardware Interface type\n",
//                  bits);
//  }
//  return 0;
//}
///* Decode 230X / 232R type chips invert bits
//* Prints directly to stdout.
//*/
//static void print_inverted_bits(int invert) {
//  const char *r_bits[] = {"TXD","RXD","RTS","CTS","DTR","DSR","DCD","RI"};
//  int i;
//
//  fprintf(stdout,"Inverted bits:");
//  for (i=0; i<8; i++)
//      if ((invert & (1<<i)) == (1<<i))
//          fprintf(stdout," %s",r_bits[i]);
//
//  fprintf(stdout,"\n");
//}
///**
// Decode binary EEPROM image into an ftdi_eeprom structure.
//
// For FT-X devices use AN_201 FT-X MTP memory Configuration to decode.
//
// \param ftdi pointer to ftdi_context
// \param verbose Decode EEPROM on stdout
//
// \retval 0: all fine
// \retval -1: something went wrong
//
// FIXME: How to pass size? How to handle size field in ftdi_eeprom?
// FIXME: Strings are malloc'ed here and should be freed somewhere
//*/
//int ftdi_eeprom_decode(FtdiContext ftdi, int verbose) {
//  int i, j;
//  unsigned short checksum, eeprom_checksum, value;
//  unsigned char manufacturer_size = 0, product_size = 0, serial_size = 0;
//  int eeprom_size;
//  struct ftdi_eeprom *eeprom;
//  unsigned char *buf = null;
//
//  if (ftdi == null)
//      ftdi_error_return(-1,"No context");
//  if (ftdi.eeprom == null)
//      ftdi_error_return(-1,"No eeprom structure");
//
//  eeprom = ftdi.eeprom;
//  eeprom_size = eeprom.size;
//  buf = ftdi.eeprom.buf;
//
//  // Addr 02: Vendor ID
//  eeprom.vendor_id = buf[0x02] + (buf[0x03] << 8);
//
//  // Addr 04: Product ID
//  eeprom.product_id = buf[0x04] + (buf[0x05] << 8);
//
//  // Addr 06: Device release number
//  eeprom.release_number = buf[0x06] + (buf[0x07]<<8);
//
//  // Addr 08: Config descriptor
//  // Bit 7: always 1
//  // Bit 6: 1 if this device is self powered, 0 if bus powered
//  // Bit 5: 1 if this device uses remote wakeup
//  eeprom.self_powered = buf[0x08] & 0x40;
//  eeprom.remote_wakeup = buf[0x08] & 0x20;
//
//  // Addr 09: Max power consumption: max power = value * 2 mA
//  eeprom.max_power = MAX_POWER_MILLIAMP_PER_UNIT * buf[0x09];
//
//  // Addr 0A: Chip configuration
//  // Bit 7: 0 - reserved
//  // Bit 6: 0 - reserved
//  // Bit 5: 0 - reserved
//  // Bit 4: 1 - Change USB version on BM and 2232C
//  // Bit 3: 1 - Use the serial number string
//  // Bit 2: 1 - Enable suspend pull downs for lower power
//  // Bit 1: 1 - Out EndPoint is Isochronous
//  // Bit 0: 1 - In EndPoint is Isochronous
//  //
//  eeprom.in_is_isochronous  = buf[0x0A]&0x01;
//  eeprom.out_is_isochronous = buf[0x0A]&0x02;
//  eeprom.suspend_pull_downs = buf[0x0A]&0x04;
//  eeprom.use_serial         = !!(buf[0x0A] & USE_SERIAL_NUM);
//  eeprom.use_usb_version    = !!(buf[0x0A] & USE_USB_VERSION_BIT);
//
//  // Addr 0C: USB version low byte when 0x0A
//  // Addr 0D: USB version high byte when 0x0A
//  eeprom.usb_version = buf[0x0C] + (buf[0x0D] << 8);
//
//  // Addr 0E: Offset of the manufacturer string + 0x80, calculated later
//  // Addr 0F: Length of manufacturer string
//  manufacturer_size = buf[0x0F]/2;
//  if (eeprom.manufacturer)
//      free(eeprom.manufacturer);
//  if (manufacturer_size > 0) {
//      eeprom.manufacturer = (char *)malloc(manufacturer_size);
//      if (eeprom.manufacturer) {
//          // Decode manufacturer
//          i = buf[0x0E] & (eeprom_size -1); // offset
//          for (j=0; j<manufacturer_size-1; j++) {
//              eeprom.manufacturer[j] = buf[2*j+i+2];
//          }
//          eeprom.manufacturer[j] = '\0';
//      }
//  }
//  else eeprom.manufacturer = null;
//
//  // Addr 10: Offset of the product string + 0x80, calculated later
//  // Addr 11: Length of product string
//  if (eeprom.product)
//      free(eeprom.product);
//  product_size = buf[0x11]/2;
//  if (product_size > 0) {
//      eeprom.product = (char *)malloc(product_size);
//      if (eeprom.product) {
//          // Decode product name
//          i = buf[0x10] & (eeprom_size -1); // offset
//          for (j=0; j<product_size-1; j++) {
//              eeprom.product[j] = buf[2*j+i+2];
//          }
//          eeprom.product[j] = '\0';
//      }
//  }
//  else eeprom.product = null;
//
//  // Addr 12: Offset of the serial string + 0x80, calculated later
//  // Addr 13: Length of serial string
//  if (eeprom.serial)
//      free(eeprom.serial);
//  serial_size = buf[0x13]/2;
//  if (serial_size > 0) {
//      eeprom.serial = (char *)malloc(serial_size);
//      if (eeprom.serial) {
//          // Decode serial
//          i = buf[0x12] & (eeprom_size -1); // offset
//          for (j=0; j<serial_size-1; j++) {
//              eeprom.serial[j] = buf[2*j+i+2];
//          }
//          eeprom.serial[j] = '\0';
//      }
//  }
//  else eeprom.serial = null;
//
//  // verify checksum
//  checksum = 0xAAAA;
//
//  for (i = 0; i < eeprom_size/2-1; i++) {
//      if ((ftdi.type == TYPE_230X) && (i == 0x12)) {
//          /* FT230X has a user section in the MTP which is not part of the checksum */
//          i = 0x40;
//      }
//      value = buf[i*2];
//      value += buf[(i*2)+1] << 8;
//
//      checksum = value^checksum;
//      checksum = (checksum << 1) | (checksum >> 15);
//  }
//
//  eeprom_checksum = buf[eeprom_size-2] + (buf[eeprom_size-1] << 8);
//
//  if (eeprom_checksum != checksum) {
//      fprintf(stderr, "Checksum Error: %04x %04x\n", checksum, eeprom_checksum);
//      ftdi_error_return(-1,"EEPROM checksum error");
//  }
//
//  eeprom.channel_a_type   = 0;
//  if ((ftdi.type == TYPE_AM) || (ftdi.type == TYPE_BM)) {
//      eeprom.chip = -1;
//  }
//  else if (ftdi.type == TYPE_2232C) {
//      eeprom.channel_a_type   = bit2type(buf[0x00] & 0x7);
//      eeprom.channel_a_driver = buf[0x00] & DRIVER_VCP;
//      eeprom.high_current_a   = buf[0x00] & HIGH_CURRENT_DRIVE;
//      eeprom.channel_b_type   = buf[0x01] & 0x7;
//      eeprom.channel_b_driver = buf[0x01] & DRIVER_VCP;
//      eeprom.high_current_b   = buf[0x01] & HIGH_CURRENT_DRIVE;
//      eeprom.chip = buf[0x14];
//  }
//  else if (ftdi.type == TYPE_R) {
//      /* TYPE_R flags D2XX, not VCP as all others*/
//      eeprom.channel_a_driver = ~buf[0x00] & DRIVER_VCP;
//      eeprom.high_current     = buf[0x00] & HIGH_CURRENT_DRIVE_R;
//      eeprom.external_oscillator = buf[0x00] & 0x02;
//      if ( (buf[0x01]&0x40) != 0x40)
//          fprintf(stderr,
//                  "TYPE_R EEPROM byte[0x01] Bit 6 unexpected Endpoint size."
//                  " If this happened with the\n"
//                  " EEPROM programmed by FTDI tools, please report "
//                  "to libftdi@developer.intra2net.com\n");
//
//      eeprom.chip = buf[0x16];
//      // Addr 0B: Invert data lines
//      // Works only on FT232R, not FT245R, but no way to distinguish
//      eeprom.invert = buf[0x0B];
//      // Addr 14: CBUS function: CBUS0, CBUS1
//      // Addr 15: CBUS function: CBUS2, CBUS3
//      // Addr 16: CBUS function: CBUS5
//      eeprom.cbus_function[0] = buf[0x14] & 0x0f;
//      eeprom.cbus_function[1] = (buf[0x14] >> 4) & 0x0f;
//      eeprom.cbus_function[2] = buf[0x15] & 0x0f;
//      eeprom.cbus_function[3] = (buf[0x15] >> 4) & 0x0f;
//      eeprom.cbus_function[4] = buf[0x16] & 0x0f;
//  }
//  else if ((ftdi.type == TYPE_2232H) || (ftdi.type == TYPE_4232H)) {
//      eeprom.channel_a_driver = buf[0x00] & DRIVER_VCP;
//      eeprom.channel_b_driver = buf[0x01] & DRIVER_VCP;
//
//      if (ftdi.type == TYPE_2232H) {
//          eeprom.channel_a_type   = bit2type(buf[0x00] & 0x7);
//          eeprom.channel_b_type   = bit2type(buf[0x01] & 0x7);
//          eeprom.suspend_dbus7    = buf[0x01] & SUSPEND_DBUS7_BIT;
//      }
//      else {
//          eeprom.channel_c_driver = (buf[0x00] >> 4) & DRIVER_VCP;
//          eeprom.channel_d_driver = (buf[0x01] >> 4) & DRIVER_VCP;
//          eeprom.channel_a_rs485enable = buf[0x0b] & (CHANNEL_IS_RS485 << 0);
//          eeprom.channel_b_rs485enable = buf[0x0b] & (CHANNEL_IS_RS485 << 1);
//          eeprom.channel_c_rs485enable = buf[0x0b] & (CHANNEL_IS_RS485 << 2);
//          eeprom.channel_d_rs485enable = buf[0x0b] & (CHANNEL_IS_RS485 << 3);
//      }
//
//      eeprom.chip = buf[0x18];
//      eeprom.group0_drive   =  buf[0x0c]       & DRIVE_16MA;
//      eeprom.group0_schmitt =  buf[0x0c]       & IS_SCHMITT;
//      eeprom.group0_slew    =  buf[0x0c]       & SLOW_SLEW;
//      eeprom.group1_drive   = (buf[0x0c] >> 4) & 0x3;
//      eeprom.group1_schmitt = (buf[0x0c] >> 4) & IS_SCHMITT;
//      eeprom.group1_slew    = (buf[0x0c] >> 4) & SLOW_SLEW;
//      eeprom.group2_drive   =  buf[0x0d]       & DRIVE_16MA;
//      eeprom.group2_schmitt =  buf[0x0d]       & IS_SCHMITT;
//      eeprom.group2_slew    =  buf[0x0d]       & SLOW_SLEW;
//      eeprom.group3_drive   = (buf[0x0d] >> 4) & DRIVE_16MA;
//      eeprom.group3_schmitt = (buf[0x0d] >> 4) & IS_SCHMITT;
//      eeprom.group3_slew    = (buf[0x0d] >> 4) & SLOW_SLEW;
//  }
//  else if (ftdi.type == TYPE_232H) {
//      eeprom.channel_a_type   = buf[0x00] & 0xf;
//      eeprom.channel_a_driver = (buf[0x00] & DRIVER_VCPH)?DRIVER_VCP:0;
//      eeprom.clock_polarity =  buf[0x01]       & FT1284_CLK_IDLE_STATE;
//      eeprom.data_order     =  buf[0x01]       & FT1284_DATA_LSB;
//      eeprom.flow_control   =  buf[0x01]       & FT1284_FLOW_CONTROL;
//      eeprom.powersave      =  buf[0x01]       & POWER_SAVE_DISABLE_H;
//      eeprom.group0_drive   =  buf[0x0c]       & DRIVE_16MA;
//      eeprom.group0_schmitt =  buf[0x0c]       & IS_SCHMITT;
//      eeprom.group0_slew    =  buf[0x0c]       & SLOW_SLEW;
//      eeprom.group1_drive   =  buf[0x0d]       & DRIVE_16MA;
//      eeprom.group1_schmitt =  buf[0x0d]       & IS_SCHMITT;
//      eeprom.group1_slew    =  buf[0x0d]       & SLOW_SLEW;
//
//      for(i=0; i<5; i++) {
//          eeprom.cbus_function[2*i  ] =  buf[0x18+i] & 0x0f;
//          eeprom.cbus_function[2*i+1] = (buf[0x18+i] >> 4) & 0x0f;
//      }
//      eeprom.chip = buf[0x1e];
//      /*FIXME: Decipher more values*/
//  }
//  else if (ftdi.type == TYPE_230X) {
//      for(i=0; i<4; i++) {
//          eeprom.cbus_function[i] =  buf[0x1a + i] & 0xFF;
//      }
//      eeprom.group0_drive   =  buf[0x0c]       & 0x03;
//      eeprom.group0_schmitt =  buf[0x0c]       & IS_SCHMITT;
//      eeprom.group0_slew    =  buf[0x0c]       & SLOW_SLEW;
//      eeprom.group1_drive   = (buf[0x0c] >> 4) & 0x03;
//      eeprom.group1_schmitt = (buf[0x0c] >> 4) & IS_SCHMITT;
//      eeprom.group1_slew    = (buf[0x0c] >> 4) & SLOW_SLEW;
//
//      eeprom.invert = buf[0xb];
//  }
//
//  if (verbose) {
//      const char *channel_mode[] = {"UART", "FIFO", "CPU", "OPTO", "FT1284"};
//      fprintf(stdout, "VID:     0x%04x\n",eeprom.vendor_id);
//      fprintf(stdout, "PID:     0x%04x\n",eeprom.product_id);
//      fprintf(stdout, "Release: 0x%04x\n",eeprom.release_number);
//
//      if (eeprom.self_powered)
//          fprintf(stdout, "Self-Powered%s", (eeprom.remote_wakeup)?", USB Remote Wake Up\n":"\n");
//      else
//          fprintf(stdout, "Bus Powered: %3d mA%s", eeprom.max_power,
//                  (eeprom.remote_wakeup)?" USB Remote Wake Up\n":"\n");
//      if (eeprom.manufacturer)
//          fprintf(stdout, "Manufacturer: %s\n",eeprom.manufacturer);
//      if (eeprom.product)
//          fprintf(stdout, "Product:      %s\n",eeprom.product);
//      if (eeprom.serial)
//          fprintf(stdout, "Serial:       %s\n",eeprom.serial);
//      fprintf(stdout,     "Checksum      : %04x\n", checksum);
//      if (ftdi.type == TYPE_R) {
//          fprintf(stdout,     "Internal EEPROM\n");
//          fprintf(stdout,"Oscillator: %s\n", eeprom.external_oscillator?"External":"Internal");
//      }
//      else if (eeprom.chip >= 0x46)
//          fprintf(stdout,     "Attached EEPROM: 93x%02x\n", eeprom.chip);
//      if (eeprom.suspend_dbus7)
//          fprintf(stdout, "Suspend on DBUS7\n");
//      if (eeprom.suspend_pull_downs)
//          fprintf(stdout, "Pull IO pins low during suspend\n");
//      if(eeprom.powersave) {
//          if(ftdi.type >= TYPE_232H)
//              fprintf(stdout,"Enter low power state on ACBUS7\n");
//      }
//      if (eeprom.remote_wakeup)
//          fprintf(stdout, "Enable Remote Wake Up\n");
//      fprintf(stdout, "PNP: %d\n",(eeprom.is_not_pnp)?0:1);
//      if (ftdi.type >= TYPE_2232C)
//          fprintf(stdout,"Channel A has Mode %s%s%s\n",
//                  channel_mode[eeprom.channel_a_type],
//                  (eeprom.channel_a_driver)?" VCP":"",
//                  (eeprom.high_current_a)?" High Current IO":"");
//      if (ftdi.type == TYPE_232H) {
//          fprintf(stdout,"FT1284 Mode Clock is idle %s, %s first, %sFlow Control\n",
//                  (eeprom.clock_polarity)?"HIGH":"LOW",
//                  (eeprom.data_order)?"LSB":"MSB",
//                  (eeprom.flow_control)?"":"No ");
//      }
//      if ((ftdi.type == TYPE_2232H) || (ftdi.type == TYPE_4232H))
//          fprintf(stdout,"Channel B has Mode %s%s%s\n",
//                  channel_mode[eeprom.channel_b_type],
//                  (eeprom.channel_b_driver)?" VCP":"",
//                  (eeprom.high_current_b)?" High Current IO":"");
//      if (((ftdi.type == TYPE_BM) || (ftdi.type == TYPE_2232C)) &&
//              eeprom.use_usb_version)
//          fprintf(stdout,"Use explicit USB Version %04x\n",eeprom.usb_version);
//
//      if ((ftdi.type == TYPE_2232H) || (ftdi.type == TYPE_4232H)) {
//          fprintf(stdout,"%s has %d mA drive%s%s\n",
//                  (ftdi.type == TYPE_2232H)?"AL":"A",
//                  (eeprom.group0_drive+1) *4,
//                  (eeprom.group0_schmitt)?" Schmitt Input":"",
//                  (eeprom.group0_slew)?" Slow Slew":"");
//          fprintf(stdout,"%s has %d mA drive%s%s\n",
//                  (ftdi.type == TYPE_2232H)?"AH":"B",
//                  (eeprom.group1_drive+1) *4,
//                  (eeprom.group1_schmitt)?" Schmitt Input":"",
//                  (eeprom.group1_slew)?" Slow Slew":"");
//          fprintf(stdout,"%s has %d mA drive%s%s\n",
//                  (ftdi.type == TYPE_2232H)?"BL":"C",
//                  (eeprom.group2_drive+1) *4,
//                  (eeprom.group2_schmitt)?" Schmitt Input":"",
//                  (eeprom.group2_slew)?" Slow Slew":"");
//          fprintf(stdout,"%s has %d mA drive%s%s\n",
//                  (ftdi.type == TYPE_2232H)?"BH":"D",
//                  (eeprom.group3_drive+1) *4,
//                  (eeprom.group3_schmitt)?" Schmitt Input":"",
//                  (eeprom.group3_slew)?" Slow Slew":"");
//      }
//      else if (ftdi.type == TYPE_232H) {
//          const char *cbush_mux[] = {"TRISTATE","TXLED","RXLED", "TXRXLED","PWREN",
//                               "SLEEP","DRIVE_0","DRIVE_1","IOMODE","TXDEN",
//                               "CLK30","CLK15","CLK7_5"
//                              };
//          fprintf(stdout,"ACBUS has %d mA drive%s%s\n",
//                  (eeprom.group0_drive+1) *4,
//                  (eeprom.group0_schmitt)?" Schmitt Input":"",
//                  (eeprom.group0_slew)?" Slow Slew":"");
//          fprintf(stdout,"ADBUS has %d mA drive%s%s\n",
//                  (eeprom.group1_drive+1) *4,
//                  (eeprom.group1_schmitt)?" Schmitt Input":"",
//                  (eeprom.group1_slew)?" Slow Slew":"");
//          for (i=0; i<10; i++) {
//              if (eeprom.cbus_function[i]<= CBUSH_CLK7_5 )
//                  fprintf(stdout,"C%d Function: %s\n", i,
//                          cbush_mux[eeprom.cbus_function[i]]);
//          }
//      }
//      else if (ftdi.type == TYPE_230X) {
//          const char *cbusx_mux[] = {"TRISTATE","TXLED","RXLED", "TXRXLED","PWREN",
//                               "SLEEP","DRIVE_0","DRIVE_1","IOMODE","TXDEN",
//                               "CLK24","CLK12","CLK6","BAT_DETECT","BAT_DETECT#",
//                               "I2C_TXE#", "I2C_RXF#", "VBUS_SENSE", "BB_WR#",
//                               "BBRD#", "TIME_STAMP", "AWAKE#",
//                              };
//          fprintf(stdout,"DBUS has %d mA drive%s%s\n",
//                  (eeprom.group0_drive+1) *4,
//                  (eeprom.group0_schmitt)?" Schmitt Input":"",
//                  (eeprom.group0_slew)?" Slow Slew":"");
//          fprintf(stdout,"CBUS has %d mA drive%s%s\n",
//                  (eeprom.group1_drive+1) *4,
//                  (eeprom.group1_schmitt)?" Schmitt Input":"",
//                  (eeprom.group1_slew)?" Slow Slew":"");
//          for (i=0; i<4; i++) {
//              if (eeprom.cbus_function[i]<= CBUSX_AWAKE)
//                  fprintf(stdout,"CBUS%d Function: %s\n", i, cbusx_mux[eeprom.cbus_function[i]]);
//          }
//
//          if (eeprom.invert)
//              print_inverted_bits(eeprom.invert);
//      }
//
//      if (ftdi.type == TYPE_R) {
//          const char *cbus_mux[] = {"TXDEN","PWREN","RXLED", "TXLED","TX+RXLED",
//                              "SLEEP","CLK48","CLK24","CLK12","CLK6",
//                              "IOMODE","BB_WR","BB_RD"
//                             };
//          const char *cbus_BB[] = {"RXF","TXE","RD", "WR"};
//
//          if (eeprom.invert)
//              print_inverted_bits(eeprom.invert);
//
//          for (i=0; i<5; i++) {
//              if (eeprom.cbus_function[i]<=CBUS_BB_RD)
//                  fprintf(stdout,"C%d Function: %s\n", i,
//                          cbus_mux[eeprom.cbus_function[i]]);
//              else {
//                  if (i < 4)
//                      /* Running MPROG show that C0..3 have fixed function Synchronous
//                         Bit Bang mode */
//                      fprintf(stdout,"C%d BB Function: %s\n", i,
//                              cbus_BB[i]);
//                  else
//                      fprintf(stdout, "Unknown CBUS mode. Might be special mode?\n");
//              }
//          }
//      }
//  }
//  return 0;
//}
//
///**
// Get a value from the decoded EEPROM structure
//
// \param ftdi pointer to ftdi_context
// \param value_name Enum of the value to query
// \param value Pointer to store read value
//
// \retval 0: all fine
// \retval -1: Value doesn't exist
//*/
//int ftdi_get_eeprom_value(FtdiContext ftdi, enum ftdi_eeprom_value value_name, int* value) {
//  switch (value_name) {
//      case VENDOR_ID:
//          *value = ftdi.eeprom.vendor_id;
//          break;
//      case PRODUCT_ID:
//          *value = ftdi.eeprom.product_id;
//          break;
//      case RELEASE_NUMBER:
//          *value = ftdi.eeprom.release_number;
//          break;
//      case SELF_POWERED:
//          *value = ftdi.eeprom.self_powered;
//          break;
//      case REMOTE_WAKEUP:
//          *value = ftdi.eeprom.remote_wakeup;
//          break;
//      case IS_NOT_PNP:
//          *value = ftdi.eeprom.is_not_pnp;
//          break;
//      case SUSPEND_DBUS7:
//          *value = ftdi.eeprom.suspend_dbus7;
//          break;
//      case IN_IS_ISOCHRONOUS:
//          *value = ftdi.eeprom.in_is_isochronous;
//          break;
//      case OUT_IS_ISOCHRONOUS:
//          *value = ftdi.eeprom.out_is_isochronous;
//          break;
//      case SUSPEND_PULL_DOWNS:
//          *value = ftdi.eeprom.suspend_pull_downs;
//          break;
//      case USE_SERIAL:
//          *value = ftdi.eeprom.use_serial;
//          break;
//      case USB_VERSION:
//          *value = ftdi.eeprom.usb_version;
//          break;
//      case USE_USB_VERSION:
//          *value = ftdi.eeprom.use_usb_version;
//          break;
//      case MAX_POWER:
//          *value = ftdi.eeprom.max_power;
//          break;
//      case CHANNEL_A_TYPE:
//          *value = ftdi.eeprom.channel_a_type;
//          break;
//      case CHANNEL_B_TYPE:
//          *value = ftdi.eeprom.channel_b_type;
//          break;
//      case CHANNEL_A_DRIVER:
//          *value = ftdi.eeprom.channel_a_driver;
//          break;
//      case CHANNEL_B_DRIVER:
//          *value = ftdi.eeprom.channel_b_driver;
//          break;
//      case CHANNEL_C_DRIVER:
//          *value = ftdi.eeprom.channel_c_driver;
//          break;
//      case CHANNEL_D_DRIVER:
//          *value = ftdi.eeprom.channel_d_driver;
//          break;
//      case CHANNEL_A_RS485:
//          *value = ftdi.eeprom.channel_a_rs485enable;
//          break;
//      case CHANNEL_B_RS485:
//          *value = ftdi.eeprom.channel_b_rs485enable;
//          break;
//      case CHANNEL_C_RS485:
//          *value = ftdi.eeprom.channel_c_rs485enable;
//          break;
//      case CHANNEL_D_RS485:
//          *value = ftdi.eeprom.channel_d_rs485enable;
//          break;
//      case CBUS_FUNCTION_0:
//          *value = ftdi.eeprom.cbus_function[0];
//          break;
//      case CBUS_FUNCTION_1:
//          *value = ftdi.eeprom.cbus_function[1];
//          break;
//      case CBUS_FUNCTION_2:
//          *value = ftdi.eeprom.cbus_function[2];
//          break;
//      case CBUS_FUNCTION_3:
//          *value = ftdi.eeprom.cbus_function[3];
//          break;
//      case CBUS_FUNCTION_4:
//          *value = ftdi.eeprom.cbus_function[4];
//          break;
//      case CBUS_FUNCTION_5:
//          *value = ftdi.eeprom.cbus_function[5];
//          break;
//      case CBUS_FUNCTION_6:
//          *value = ftdi.eeprom.cbus_function[6];
//          break;
//      case CBUS_FUNCTION_7:
//          *value = ftdi.eeprom.cbus_function[7];
//          break;
//      case CBUS_FUNCTION_8:
//          *value = ftdi.eeprom.cbus_function[8];
//          break;
//      case CBUS_FUNCTION_9:
//          *value = ftdi.eeprom.cbus_function[9];
//          break;
//      case HIGH_CURRENT:
//          *value = ftdi.eeprom.high_current;
//          break;
//      case HIGH_CURRENT_A:
//          *value = ftdi.eeprom.high_current_a;
//          break;
//      case HIGH_CURRENT_B:
//          *value = ftdi.eeprom.high_current_b;
//          break;
//      case INVERT:
//          *value = ftdi.eeprom.invert;
//          break;
//      case GROUP0_DRIVE:
//          *value = ftdi.eeprom.group0_drive;
//          break;
//      case GROUP0_SCHMITT:
//          *value = ftdi.eeprom.group0_schmitt;
//          break;
//      case GROUP0_SLEW:
//          *value = ftdi.eeprom.group0_slew;
//          break;
//      case GROUP1_DRIVE:
//          *value = ftdi.eeprom.group1_drive;
//          break;
//      case GROUP1_SCHMITT:
//          *value = ftdi.eeprom.group1_schmitt;
//          break;
//      case GROUP1_SLEW:
//          *value = ftdi.eeprom.group1_slew;
//          break;
//      case GROUP2_DRIVE:
//          *value = ftdi.eeprom.group2_drive;
//          break;
//      case GROUP2_SCHMITT:
//          *value = ftdi.eeprom.group2_schmitt;
//          break;
//      case GROUP2_SLEW:
//          *value = ftdi.eeprom.group2_slew;
//          break;
//      case GROUP3_DRIVE:
//          *value = ftdi.eeprom.group3_drive;
//          break;
//      case GROUP3_SCHMITT:
//          *value = ftdi.eeprom.group3_schmitt;
//          break;
//      case GROUP3_SLEW:
//          *value = ftdi.eeprom.group3_slew;
//          break;
//      case POWER_SAVE:
//          *value = ftdi.eeprom.powersave;
//          break;
//      case CLOCK_POLARITY:
//          *value = ftdi.eeprom.clock_polarity;
//          break;
//      case DATA_ORDER:
//          *value = ftdi.eeprom.data_order;
//          break;
//      case FLOW_CONTROL:
//          *value = ftdi.eeprom.flow_control;
//          break;
//      case CHIP_TYPE:
//          *value = ftdi.eeprom.chip;
//          break;
//      case CHIP_SIZE:
//          *value = ftdi.eeprom.size;
//          break;
//      case EXTERNAL_OSCILLATOR:
//          *value = ftdi.eeprom.external_oscillator;
//          break;
//      default:
//          ftdi_error_return(-1, "Request for unknown EEPROM value");
//  }
//  return 0;
//}
//
///**
// Set a value in the decoded EEPROM Structure
// No parameter checking is performed
//
// \param ftdi pointer to ftdi_context
// \param value_name Enum of the value to set
// \param value to set
//
// \retval 0: all fine
// \retval -1: Value doesn't exist
// \retval -2: Value not user settable
//*/
//int ftdi_set_eeprom_value(FtdiContext ftdi, enum ftdi_eeprom_value value_name, int value) {
//  switch (value_name) {
//      case VENDOR_ID:
//          ftdi.eeprom.vendor_id = value;
//          break;
//      case PRODUCT_ID:
//          ftdi.eeprom.product_id = value;
//          break;
//      case RELEASE_NUMBER:
//          ftdi.eeprom.release_number = value;
//          break;
//      case SELF_POWERED:
//          ftdi.eeprom.self_powered = value;
//          break;
//      case REMOTE_WAKEUP:
//          ftdi.eeprom.remote_wakeup = value;
//          break;
//      case IS_NOT_PNP:
//          ftdi.eeprom.is_not_pnp = value;
//          break;
//      case SUSPEND_DBUS7:
//          ftdi.eeprom.suspend_dbus7 = value;
//          break;
//      case IN_IS_ISOCHRONOUS:
//          ftdi.eeprom.in_is_isochronous = value;
//          break;
//      case OUT_IS_ISOCHRONOUS:
//          ftdi.eeprom.out_is_isochronous = value;
//          break;
//      case SUSPEND_PULL_DOWNS:
//          ftdi.eeprom.suspend_pull_downs = value;
//          break;
//      case USE_SERIAL:
//          ftdi.eeprom.use_serial = value;
//          break;
//      case USB_VERSION:
//          ftdi.eeprom.usb_version = value;
//          break;
//      case USE_USB_VERSION:
//          ftdi.eeprom.use_usb_version = value;
//          break;
//      case MAX_POWER:
//          ftdi.eeprom.max_power = value;
//          break;
//      case CHANNEL_A_TYPE:
//          ftdi.eeprom.channel_a_type = value;
//          break;
//      case CHANNEL_B_TYPE:
//          ftdi.eeprom.channel_b_type = value;
//          break;
//      case CHANNEL_A_DRIVER:
//          ftdi.eeprom.channel_a_driver = value;
//          break;
//      case CHANNEL_B_DRIVER:
//          ftdi.eeprom.channel_b_driver = value;
//          break;
//      case CHANNEL_C_DRIVER:
//          ftdi.eeprom.channel_c_driver = value;
//          break;
//      case CHANNEL_D_DRIVER:
//          ftdi.eeprom.channel_d_driver = value;
//          break;
//      case CHANNEL_A_RS485:
//          ftdi.eeprom.channel_a_rs485enable = value;
//          break;
//      case CHANNEL_B_RS485:
//          ftdi.eeprom.channel_b_rs485enable = value;
//          break;
//      case CHANNEL_C_RS485:
//          ftdi.eeprom.channel_c_rs485enable = value;
//          break;
//      case CHANNEL_D_RS485:
//          ftdi.eeprom.channel_d_rs485enable = value;
//          break;
//      case CBUS_FUNCTION_0:
//          ftdi.eeprom.cbus_function[0] = value;
//          break;
//      case CBUS_FUNCTION_1:
//          ftdi.eeprom.cbus_function[1] = value;
//          break;
//      case CBUS_FUNCTION_2:
//          ftdi.eeprom.cbus_function[2] = value;
//          break;
//      case CBUS_FUNCTION_3:
//          ftdi.eeprom.cbus_function[3] = value;
//          break;
//      case CBUS_FUNCTION_4:
//          ftdi.eeprom.cbus_function[4] = value;
//          break;
//      case CBUS_FUNCTION_5:
//          ftdi.eeprom.cbus_function[5] = value;
//          break;
//      case CBUS_FUNCTION_6:
//          ftdi.eeprom.cbus_function[6] = value;
//          break;
//      case CBUS_FUNCTION_7:
//          ftdi.eeprom.cbus_function[7] = value;
//          break;
//      case CBUS_FUNCTION_8:
//          ftdi.eeprom.cbus_function[8] = value;
//          break;
//      case CBUS_FUNCTION_9:
//          ftdi.eeprom.cbus_function[9] = value;
//          break;
//      case HIGH_CURRENT:
//          ftdi.eeprom.high_current = value;
//          break;
//      case HIGH_CURRENT_A:
//          ftdi.eeprom.high_current_a = value;
//          break;
//      case HIGH_CURRENT_B:
//          ftdi.eeprom.high_current_b = value;
//          break;
//      case INVERT:
//          ftdi.eeprom.invert = value;
//          break;
//      case GROUP0_DRIVE:
//          ftdi.eeprom.group0_drive = value;
//          break;
//      case GROUP0_SCHMITT:
//          ftdi.eeprom.group0_schmitt = value;
//          break;
//      case GROUP0_SLEW:
//          ftdi.eeprom.group0_slew = value;
//          break;
//      case GROUP1_DRIVE:
//          ftdi.eeprom.group1_drive = value;
//          break;
//      case GROUP1_SCHMITT:
//          ftdi.eeprom.group1_schmitt = value;
//          break;
//      case GROUP1_SLEW:
//          ftdi.eeprom.group1_slew = value;
//          break;
//      case GROUP2_DRIVE:
//          ftdi.eeprom.group2_drive = value;
//          break;
//      case GROUP2_SCHMITT:
//          ftdi.eeprom.group2_schmitt = value;
//          break;
//      case GROUP2_SLEW:
//          ftdi.eeprom.group2_slew = value;
//          break;
//      case GROUP3_DRIVE:
//          ftdi.eeprom.group3_drive = value;
//          break;
//      case GROUP3_SCHMITT:
//          ftdi.eeprom.group3_schmitt = value;
//          break;
//      case GROUP3_SLEW:
//          ftdi.eeprom.group3_slew = value;
//          break;
//      case CHIP_TYPE:
//          ftdi.eeprom.chip = value;
//          break;
//      case POWER_SAVE:
//          ftdi.eeprom.powersave = value;
//          break;
//      case CLOCK_POLARITY:
//          ftdi.eeprom.clock_polarity = value;
//          break;
//      case DATA_ORDER:
//          ftdi.eeprom.data_order = value;
//          break;
//      case FLOW_CONTROL:
//          ftdi.eeprom.flow_control = value;
//          break;
//      case CHIP_SIZE:
//          ftdi_error_return(-2, "EEPROM Value can't be changed");
//          break;
//      case EXTERNAL_OSCILLATOR:
//          ftdi.eeprom.external_oscillator = value;
//          break;
//      case USER_DATA_ADDR:
//          ftdi.eeprom.user_data_addr = value;
//          break;
//
//      default :
//          ftdi_error_return(-1, "Request to unknown EEPROM value");
//  }
//  ftdi.eeprom.initialized_for_connected_device = 0;
//  return 0;
//}
//
///** Get the read-only buffer to the binary EEPROM content
//
//  \param ftdi pointer to ftdi_context
//  \param buf buffer to receive EEPROM content
//  \param size Size of receiving buffer
//
//  \retval 0: All fine
//  \retval -1: struct ftdi_contxt or ftdi_eeprom missing
//  \retval -2: Not enough room to store eeprom
//*/
//int ftdi_get_eeprom_buf(FtdiContext ftdi, unsigned char * buf, int size) {
//  if (!ftdi || !(ftdi.eeprom))
//      ftdi_error_return(-1, "No appropriate structure");
//
//  if (!buf || size < ftdi.eeprom.size)
//      ftdi_error_return(-1, "Not enough room to store eeprom");
//
//  // Only copy up to FTDI_MAX_EEPROM_SIZE bytes
//  if (size > FTDI_MAX_EEPROM_SIZE)
//      size = FTDI_MAX_EEPROM_SIZE;
//
//  memcpy(buf, ftdi.eeprom.buf, size);
//
//  return 0;
//}
//
///** Set the EEPROM content from the user-supplied prefilled buffer
//
//  \param ftdi pointer to ftdi_context
//  \param buf buffer to read EEPROM content
//  \param size Size of buffer
//
//  \retval 0: All fine
//  \retval -1: struct ftdi_context or ftdi_eeprom or buf missing
//*/
//int ftdi_set_eeprom_buf(FtdiContext ftdi, const unsigned char * buf, int size) {
//  if (!ftdi || !(ftdi.eeprom) || !buf)
//      ftdi_error_return(-1, "No appropriate structure");
//
//  // Only copy up to FTDI_MAX_EEPROM_SIZE bytes
//  if (size > FTDI_MAX_EEPROM_SIZE)
//      size = FTDI_MAX_EEPROM_SIZE;
//
//  memcpy(ftdi.eeprom.buf, buf, size);
//
//  return 0;
//}
//
///** Set the EEPROM user data content from the user-supplied prefilled buffer
//
//  \param ftdi pointer to ftdi_context
//  \param buf buffer to read EEPROM user data content
//  \param size Size of buffer
//
//  \retval 0: All fine
//  \retval -1: struct ftdi_context or ftdi_eeprom or buf missing
//*/
//int ftdi_set_eeprom_user_data(FtdiContext ftdi, const char * buf, int size) {
//  if (!ftdi || !(ftdi.eeprom) || !buf)
//      ftdi_error_return(-1, "No appropriate structure");
//
//  ftdi.eeprom.user_data_size = size;
//  ftdi.eeprom.user_data = buf;
//  return 0;
//}
//
///**
//  Read eeprom location
//
//  \param ftdi pointer to ftdi_context
//  \param eeprom_addr Address of eeprom location to be read
//  \param eeprom_val Pointer to store read eeprom location
//
//  \retval  0: all fine
//  \retval -1: read failed
//  \retval -2: USB device unavailable
//*/
//int ftdi_read_eeprom_location (FtdiContext ftdi, int eeprom_addr, unsigned short *eeprom_val) {
//  unsigned char buf[2];
//
//  if (ftdi == null || ftdi.usbDev == null)
//      ftdi_error_return(-2, "USB device unavailable");
//
//  if (LibUsb.libusb_control_transfer(ftdi.usbDev, FTDI_DEVICE_IN_REQTYPE, SIO_READ_EEPROM_REQUEST, 0, eeprom_addr, buf, 2, ftdi.usb_read_timeout) != 2)
//      ftdi_error_return(-1, "reading eeprom failed");
//
//  *eeprom_val = (0xff & buf[0]) | (buf[1] << 8);
//
//  return 0;
//}
//
///**
//  Read eeprom
//
//  \param ftdi pointer to ftdi_context
//
//  \retval  0: all fine
//  \retval -1: read failed
//  \retval -2: USB device unavailable
//*/
//int ftdi_read_eeprom(FtdiContext ftdi) {
//  int i;
//  unsigned char *buf;
//
//  if (ftdi == null || ftdi.usbDev == null)
//      ftdi_error_return(-2, "USB device unavailable");
//  buf = ftdi.eeprom.buf;
//
//  for (i = 0; i < FTDI_MAX_EEPROM_SIZE/2; i++) {
//      if (LibUsb.libusb_control_transfer(
//                  ftdi.usbDev, FTDI_DEVICE_IN_REQTYPE,SIO_READ_EEPROM_REQUEST, 0, i,
//                  buf+(i*2), 2, ftdi.usb_read_timeout) != 2)
//          ftdi_error_return(-1, "reading eeprom failed");
//  }
//
//  if (ftdi.type == TYPE_R)
//      ftdi.eeprom.size = 0x80;
//  /*    Guesses size of eeprom by comparing halves
//        - will not work with blank eeprom */
//  else if (strrchr((const char *)buf, 0xff) == ((const char *)buf +FTDI_MAX_EEPROM_SIZE -1))
//      ftdi.eeprom.size = -1;
//  else if (memcmp(buf,&buf[0x80],0x80) == 0)
//      ftdi.eeprom.size = 0x80;
//  else if (memcmp(buf,&buf[0x40],0x40) == 0)
//      ftdi.eeprom.size = 0x40;
//  else
//      ftdi.eeprom.size = 0x100;
//  return 0;
//}
//
///*
//  ftdi_read_chipid_shift does the bitshift operation needed for the FTDIChip-ID
//  Function is only used internally
//  \internal
//*/
//static unsigned char ftdi_read_chipid_shift(unsigned char value) {
//  return ((value & 1) << 1) |
//         ((value & 2) << 5) |
//         ((value & 4) >> 2) |
//         ((value & 8) << 4) |
//         ((value & 16) >> 1) |
//         ((value & 32) >> 1) |
//         ((value & 64) >> 4) |
//         ((value & 128) >> 2);
//}
//
///**
//  Read the FTDIChip-ID from R-type devices
//
//  \param ftdi pointer to ftdi_context
//  \param chipid Pointer to store FTDIChip-ID
//
//  \retval  0: all fine
//  \retval -1: read failed
//  \retval -2: USB device unavailable
//*/
//int ftdi_read_chipid(FtdiContext ftdi, unsigned int *chipid) {
//  unsigned int a = 0, b = 0;
//
//  if (ftdi == null || ftdi.usbDev == null)
//      ftdi_error_return(-2, "USB device unavailable");
//
//  if (LibUsb.libusb_control_transfer(ftdi.usbDev, FTDI_DEVICE_IN_REQTYPE, SIO_READ_EEPROM_REQUEST, 0, 0x43, (unsigned char *)&a, 2, ftdi.usb_read_timeout) == 2) {
//      a = a << 8 | a >> 8;
//      if (LibUsb.libusb_control_transfer(ftdi.usbDev, FTDI_DEVICE_IN_REQTYPE, SIO_READ_EEPROM_REQUEST, 0, 0x44, (unsigned char *)&b, 2, ftdi.usb_read_timeout) == 2) {
//          b = b << 8 | b >> 8;
//          a = (a << 16) | (b & 0xFFFF);
//          a = ftdi_read_chipid_shift(a) | ftdi_read_chipid_shift(a>>8)<<8
//              | ftdi_read_chipid_shift(a>>16)<<16 | ftdi_read_chipid_shift(a>>24)<<24;
//          *chipid = a ^ 0xa5f0f7d1;
//          return 0;
//      }
//  }
//
//  ftdi_error_return(-1, "read of FTDIChip-ID failed");
//}
//
///**
//  Write eeprom location
//
//  \param ftdi pointer to ftdi_context
//  \param eeprom_addr Address of eeprom location to be written
//  \param eeprom_val Value to be written
//
//  \retval  0: all fine
//  \retval -1: write failed
//  \retval -2: USB device unavailable
//  \retval -3: Invalid access to checksum protected area below 0x80
//  \retval -4: Device can't access unprotected area
//  \retval -5: Reading chip type failed
//*/
//int ftdi_write_eeprom_location(FtdiContext ftdi, int eeprom_addr,
//                             unsigned short eeprom_val) {
//  int chip_type_location;
//  unsigned short chip_type;
//
//  if (ftdi == null || ftdi.usbDev == null)
//      ftdi_error_return(-2, "USB device unavailable");
//
//  if (eeprom_addr <0x80)
//      ftdi_error_return(-2, "Invalid access to checksum protected area  below 0x80");
//
//
//  switch (ftdi.type) {
//      case TYPE_BM:
//      case  TYPE_2232C:
//          chip_type_location = 0x14;
//          break;
//      case TYPE_2232H:
//      case TYPE_4232H:
//          chip_type_location = 0x18;
//          break;
//      case TYPE_232H:
//          chip_type_location = 0x1e;
//          break;
//      default:
//          ftdi_error_return(-4, "Device can't access unprotected area");
//  }
//
//  if (ftdi_read_eeprom_location( ftdi, chip_type_location>>1, &chip_type))
//      ftdi_error_return(-5, "Reading failed");
//  fprintf(stderr," loc 0x%04x val 0x%04x\n", chip_type_location,chip_type);
//  if ((chip_type & 0xff) != 0x66) {
//      ftdi_error_return(-6, "EEPROM is not of 93x66");
//  }
//
//  if (LibUsb.libusb_control_transfer(ftdi.usbDev, FTDI_DEVICE_OUT_REQTYPE,
//                              SIO_WRITE_EEPROM_REQUEST, eeprom_val, eeprom_addr,
//                              null, 0, ftdi.usb_write_timeout) != 0)
//      ftdi_error_return(-1, "unable to write eeprom");
//
//  return 0;
//}

///**
//  Write eeprom
//
//  \param ftdi pointer to ftdi_context
//
//  \retval  0: all fine
//  \retval -1: read failed
//  \retval -2: USB device unavailable
//  \retval -3: EEPROM not initialized for the connected device;
//*/
//int ftdi_write_eeprom(FtdiContext ftdi) {
//  unsigned short usb_val, status;
//  int i, ret;
//  unsigned char *eeprom;
//
//  if (ftdi == null || ftdi.usbDev == null)
//      ftdi_error_return(-2, "USB device unavailable");
//
//  if(ftdi.eeprom.initialized_for_connected_device == 0)
//      ftdi_error_return(-3, "EEPROM not initialized for the connected device");
//
//  eeprom = ftdi.eeprom.buf;
//
//  /* These commands were traced while running MProg */
//  if ((ret = ftdi_usb_reset(ftdi)) != 0)
//      return ret;
//  if ((ret = ftdi_poll_modem_status(ftdi, &status)) != 0)
//      return ret;
//  if ((ret = ftdi_set_latency_timer(ftdi, 0x77)) != 0)
//      return ret;
//
//  for (i = 0; i < ftdi.eeprom.size/2; i++) {
//      /* Do not try to write to reserved area */
//      if ((ftdi.type == TYPE_230X) && (i == 0x40)) {
//          i = 0x50;
//      }
//      usb_val = eeprom[i*2];
//      usb_val += eeprom[(i*2)+1] << 8;
//      if (LibUsb.libusb_control_transfer(ftdi.usbDev, FTDI_DEVICE_OUT_REQTYPE,
//                                  SIO_WRITE_EEPROM_REQUEST, usb_val, i,
//                                  null, 0, ftdi.usb_write_timeout) < 0)
//          ftdi_error_return(-1, "unable to write eeprom");
//  }
//
//  return 0;
//}

//	private static final int MAGIC = 0x55aa;
//	/**
//	 * Erase eeprom
//	 * This is not supported on FT232R/FT245R according to the MProg manual from FTDI.
// 	 */
//	public void eraseEeprom() {
//	    if (type == TYPE_R || type == TYPE_230X) {
//	        eeprom.chip = 0;
//	        return ;
//	    }
//	    controlTransferOut(SIO_ERASE_EEPROM_REQUEST, 0, 0);
//
//	    /* detect chip type by writing 0x55AA as magic at word position 0xc0
//	       Chip is 93x46 if magic is read at word position 0x00, as wraparound happens around 0x40
//	       Chip is 93x56 if magic is read at word position 0x40, as wraparound happens around 0x80
//	       Chip is 93x66 if magic is only read at word position 0xc0*/
//	    controlTransferOut(SIO_WRITE_EEPROM_REQUEST, MAGIC, 0xc0);
//	    if (ftdi_read_eeprom_location(this, 0x00) == MAGIC) eeprom.chip = 0x46;
//	    else if (ftdi_read_eeprom_location(this, 0x40) == MAGIC) eeprom.chip = 0x56;
//	    else if (ftdi_read_eeprom_location(this, 0xc0) == MAGIC) eeprom.chip = 0x66;
//        else eeprom.chip = -1;
//	    usbDev.controlTransfer(FTDI_DEVICE_OUT_REQTYPE, RequestType.SIO_ERASE_EEPROM_REQUEST.value,
//	                                0, 0, usbWriteTimeout);
//	}
	
	
}
