package ceri.serial.ftdi.jna;

import java.util.regex.Pattern;
import ceri.common.math.Maths;
import ceri.common.text.Parse;
import ceri.common.text.Regex;
import ceri.serial.libusb.jna.LibUsb;
import ceri.serial.libusb.jna.LibUsbException;
import ceri.serial.libusb.jna.LibUsbFinder;
import ceri.serial.libusb.jna.LibUsbUtil;

public class LibFtdiUtil {
	private static final Pattern FIND_BY_DEVICE_NODE = Pattern.compile("d:(\\d+)/(\\d+)");
	private static final Pattern FIND_BY_VENDOR_INDEX =
		Pattern.compile("i:(\\w+):(\\w+)(?::(\\d+))?");
	private static final Pattern FIND_BY_VENDOR_SERIAL = Pattern.compile("s:(\\w+):(\\w+):(\\w+)");
	public static final LibUsbFinder FINDER =
		LibUsbFinder.builder().vendor(LibFtdi.FTDI_VENDOR_ID).build();

	private LibFtdiUtil() {}

	/**
	 * Require FTDI USB device handle to be set.
	 */
	public static void requireDev(LibFtdi.ftdi_context ftdi) throws LibUsbException {
		requireCtx(ftdi);
		LibUsbUtil.require(ftdi.usb_dev);
	}

	/**
	 * Require FTDI USB context to be set.
	 */
	public static void requireCtx(LibFtdi.ftdi_context ftdi) throws LibUsbException {
		require(ftdi);
		LibUsbUtil.require(ftdi.usb_ctx, "LibUsb context");
	}

	/**
	 * Require FTDI context to be set.
	 */
	public static void require(LibFtdi.ftdi_context ftdi) throws LibUsbException {
		LibUsbUtil.require(ftdi, "Ftdi context");
	}

	/**
	 * Returns FTDI vendor for 'any' value.
	 */
	public static int vendor(int vendor) {
		return vendor == 0 ? LibFtdi.FTDI_VENDOR_ID : vendor;
	}

	/**
	 * Creates a device finder based on descriptor formats:
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
	public static LibUsbFinder finder(String descriptor) {
		var m = Regex.match(FIND_BY_DEVICE_NODE, descriptor);
		if (m.hasMatch()) return LibUsbFinder.builder().bus(Integer.decode(m.group(1)))
			.address(Integer.decode(m.group(2))).build();
		m = Regex.match(FIND_BY_VENDOR_INDEX, descriptor);
		if (m.hasMatch()) return LibUsbFinder.builder().vendor(Integer.decode(m.group(1)))
			.product(Integer.decode(m.group(2))).index(Parse.parseInt(m.group(3), 0)).build();
		m = Regex.match(FIND_BY_VENDOR_SERIAL, descriptor);
		if (m.hasMatch()) return LibUsbFinder.builder().vendor(Integer.decode(m.group(1)))
			.product(Integer.decode(m.group(2))).serial(m.group(3)).build();
		throw new IllegalArgumentException("Invalid descriptor format: " + descriptor);
	}

	/**
	 * Attempt to determine chip type from device and serial ids.
	 */
	public static LibFtdi.ftdi_chip_type guessChipType(int device, int serial) {
		return switch (Maths.ushort(device)) {
			case 0x0200 -> serial == 0 ? LibFtdi.ftdi_chip_type.TYPE_BM :
				LibFtdi.ftdi_chip_type.TYPE_AM;
			case 0x0400 -> LibFtdi.ftdi_chip_type.TYPE_BM;
			case 0x0500 -> LibFtdi.ftdi_chip_type.TYPE_2232C;
			case 0x0600 -> LibFtdi.ftdi_chip_type.TYPE_R;
			case 0x0700 -> LibFtdi.ftdi_chip_type.TYPE_2232H;
			case 0x0800 -> LibFtdi.ftdi_chip_type.TYPE_4232H;
			case 0x0900 -> LibFtdi.ftdi_chip_type.TYPE_232H;
			case 0x1000 -> LibFtdi.ftdi_chip_type.TYPE_230X;
			default -> LibFtdi.ftdi_chip_type.TYPE_BM;
		};
	}

	/**
	 * Determine if exception is a LibUsbException with given error.
	 */
	public static boolean isError(Exception e, LibUsb.libusb_error error) {
		return e instanceof LibUsbException le && le.error == error;
	}
}
