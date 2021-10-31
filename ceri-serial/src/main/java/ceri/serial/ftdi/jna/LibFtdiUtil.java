package ceri.serial.ftdi.jna;

import static ceri.common.math.MathUtil.ushort;
import static ceri.serial.ftdi.jna.LibFtdi.FTDI_VENDOR_ID;
import static ceri.serial.ftdi.jna.LibFtdi.ftdi_chip_type.TYPE_2232C;
import static ceri.serial.ftdi.jna.LibFtdi.ftdi_chip_type.TYPE_2232H;
import static ceri.serial.ftdi.jna.LibFtdi.ftdi_chip_type.TYPE_230X;
import static ceri.serial.ftdi.jna.LibFtdi.ftdi_chip_type.TYPE_232H;
import static ceri.serial.ftdi.jna.LibFtdi.ftdi_chip_type.TYPE_4232H;
import static ceri.serial.ftdi.jna.LibFtdi.ftdi_chip_type.TYPE_AM;
import static ceri.serial.ftdi.jna.LibFtdi.ftdi_chip_type.TYPE_BM;
import static ceri.serial.ftdi.jna.LibFtdi.ftdi_chip_type.TYPE_R;
import static java.util.regex.Pattern.compile;
import java.util.regex.Pattern;
import ceri.common.text.RegexUtil;
import ceri.common.util.PrimitiveUtil;
import ceri.serial.ftdi.jna.LibFtdi.ftdi_chip_type;
import ceri.serial.ftdi.jna.LibFtdi.ftdi_context;
import ceri.serial.libusb.jna.LibUsb.libusb_error;
import ceri.serial.libusb.jna.LibUsbException;
import ceri.serial.libusb.jna.LibUsbFinder;
import ceri.serial.libusb.jna.LibUsbUtil;

public class LibFtdiUtil {
	private static final Pattern FIND_BY_DEVICE_NODE = compile("d:(\\d+)/(\\d+)");
	private static final Pattern FIND_BY_VENDOR_INDEX = compile("i:(\\w+):(\\w+)(?::(\\d+))?");
	private static final Pattern FIND_BY_VENDOR_SERIAL = compile("s:(\\w+):(\\w+):(\\w+)");
	public static final LibUsbFinder FINDER = LibUsbFinder.builder().vendor(FTDI_VENDOR_ID).build();

	private LibFtdiUtil() {}

	/**
	 * Require FTDI USB device handle to be set.
	 */
	public static void requireDev(ftdi_context ftdi) throws LibUsbException {
		requireCtx(ftdi);
		LibUsbUtil.require(ftdi.usb_dev);
	}

	/**
	 * Require FTDI USB context to be set.
	 */
	public static void requireCtx(ftdi_context ftdi) throws LibUsbException {
		require(ftdi);
		LibUsbUtil.require(ftdi.usb_ctx, "LibUsb context");
	}

	/**
	 * Require FTDI context to be set.
	 */
	public static void require(ftdi_context ftdi) throws LibUsbException {
		LibUsbUtil.require(ftdi, "Ftdi context");
	}

	/**
	 * Returns FTDI vendor for 'any' value.
	 */
	public static int vendor(int vendor) {
		return vendor == 0 ? FTDI_VENDOR_ID : vendor;
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
		var m = RegexUtil.matched(FIND_BY_DEVICE_NODE, descriptor);
		if (m != null) return LibUsbFinder.builder().bus(Integer.decode(m.group(1)))
			.address(Integer.decode(m.group(2))).build();
		m = RegexUtil.matched(FIND_BY_VENDOR_INDEX, descriptor);
		if (m != null) return LibUsbFinder.builder().vendor(Integer.decode(m.group(1)))
			.product(Integer.decode(m.group(2))).index(PrimitiveUtil.valueOf(m.group(3), 0))
			.build();
		m = RegexUtil.matched(FIND_BY_VENDOR_SERIAL, descriptor);
		if (m != null) return LibUsbFinder.builder().vendor(Integer.decode(m.group(1)))
			.product(Integer.decode(m.group(2))).serial(m.group(3)).build();
		throw new IllegalArgumentException("Invalid descriptor format: " + descriptor);
	}

	/**
	 * Attempt to determine chipt type from device and serial ids.
	 */
	public static ftdi_chip_type guessChipType(int device, int serial) {
		return switch (ushort(device)) {
			case 0x0200 -> serial == 0 ? TYPE_BM : TYPE_AM;
			case 0x0400 -> TYPE_BM;
			case 0x0500 -> TYPE_2232C;
			case 0x0600 -> TYPE_R;
			case 0x0700 -> TYPE_2232H;
			case 0x0800 -> TYPE_4232H;
			case 0x0900 -> TYPE_232H;
			case 0x1000 -> TYPE_230X;
			default -> TYPE_BM;
		};
	}

	/**
	 * Determine if exception is a LibUsbException with given error.
	 */
	public static boolean isError(Exception e, libusb_error error) {
		return e instanceof LibUsbException le && le.error == error;
	}
}
