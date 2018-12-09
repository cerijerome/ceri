package ceri.serial.ftdi;

import static ceri.common.collection.ImmutableByteArray.wrap;
import static ceri.serial.ftdi.Ftdi.FTDI_DEVICE_OUT_REQTYPE;
import static ceri.serial.ftdi.FtdiChipType.TYPE_AM;
import static ceri.serial.ftdi.RequestType.SIO_SET_BAUDRATE_REQUEST;
import ceri.common.collection.ImmutableByteArray;
import ceri.serial.ftdi.jna.LibFtdiException;
import ceri.serial.libusb.jna.LibUsb;
import ceri.serial.libusb.jna.LibUsbException;

class FtdiBaudRate {
	private static final int BITBANG_MULTIPLIER = 4;
	private static final long H_CLK = 120000000;
	private static final long C_CLK = 48000000;
	private static final long AM_CLK = 24000000;
	private static final ImmutableByteArray fracCode = wrap(0, 3, 2, 4, 1, 5, 6, 7);
	private static final ImmutableByteArray amAdjustUp = wrap(0, 0, 0, 1, 0, 3, 2, 1);
	private static final ImmutableByteArray amAdjustDn = wrap(0, 0, 0, 1, 0, 1, 2, 3);
	private FtdiContext ftdi;
	private long actualBaudRate;
	private long encodedDivisor;
	private short value;
	private short index;

	public static void main(String[] args) throws LibFtdiException {
		int baudRate = 9600;
		FtdiContext ftdi = new FtdiContext();
		ftdi.bitbangMode = FtdiBitMode.BITMODE_BITBANG;

		FtdiBaudRate baud = new FtdiBaudRate(ftdi);
		baud.determineActualBaudRate(baudRate);
		System.out.printf("actual=%d div=%d value=0x%04x index=0x%04x%n", baud.actualBaudRate,
			baud.encodedDivisor, baud.value, baud.index);
		System.out.printf("LibUsb.libusb_control_transfer(ctx, 0x%02x, 0x%02x, 0x%04x, 0x%04x, %d);",
			FTDI_DEVICE_OUT_REQTYPE,
			SIO_SET_BAUDRATE_REQUEST.value, baud.value, baud.index, ftdi.usbWriteTimeout);
	}

	public static void set(FtdiContext ftdi, int baudRate) throws LibFtdiException {
		FtdiBaudRate baud = new FtdiBaudRate(ftdi);
		baud.determineActualBaudRate(baudRate);
		baud.setBaudRate(baudRate);
	}

	private FtdiBaudRate(FtdiContext ftdi) {
		this.ftdi = ftdi;
	}

	private void setBaudRate(int baudRate) throws LibFtdiException {
		try {
			ftdi.usbDev.controlTransfer(FTDI_DEVICE_OUT_REQTYPE,
				(byte) SIO_SET_BAUDRATE_REQUEST.value, value, index, ftdi.usbWriteTimeout);
			ftdi.baudRate = baudRate;
		} catch (LibUsbException e) {
			throw new LibFtdiException("Failed to set baud rate: " + baudRate, -2, e);
		}
	}

	private void determineActualBaudRate(int baudRate) throws LibFtdiException {
		if (ftdi.bitbangEnabled) baudRate = baudRate * BITBANG_MULTIPLIER;
		actualBaudRate = convertBaudRate(baudRate);
		if (actualBaudRate <= 0)
			throw new LibFtdiException("Invalid baud rate: " + actualBaudRate, -1);
		if (toleranceExceeded(baudRate, actualBaudRate)) throw new LibFtdiException("Unsupported baud rate: " + baudRate + "/" + actualBaudRate,
			-1);
	}

	private boolean toleranceExceeded(long baudRate, long actualBaudRate) {
		if (actualBaudRate * 2 < baudRate) return true; // check overflow
		return actualBaudRate < baudRate ? // 21:20 = 5%
			(actualBaudRate * 21 < baudRate * 20) : (baudRate * 21 < actualBaudRate * 20);
	}

	/**
	 * Calculates the nearest supported baud rate to that requested, and sets the fields for value
	 * and index.
	 */
	private long convertBaudRate(long baudRate) {
		long bestBaud;

		if (baudRate <= 0) throw new IllegalArgumentException("Invalid baud rate: " + baudRate);

		if (ftdi.type.isHType()) {
			if (baudRate * 10 > H_CLK / 0x3fff) {
				bestBaud = toClkBits(baudRate, H_CLK, 10);
				encodedDivisor |= 0x20000;
			} else bestBaud = toClkBits(baudRate, C_CLK, 16);
		} else if (ftdi.type != TYPE_AM) bestBaud = toClkBits(baudRate, C_CLK, 16);
		else bestBaud = toClkBitsAM(baudRate);

		// Split into "value" and "index" values
		value = (short) (encodedDivisor & 0xffff);
		if (ftdi.type.isHType())
			index = (short) (((encodedDivisor >> 8) & 0xff00) | ftdi.iface.index);
		else index = (short) ((encodedDivisor >> 16) & 0xffff);

		return bestBaud;
	}

	private long toClkBitsAM(long baudRate) {
		long divisor = AM_CLK / baudRate;
		// Round down to supported fraction (AM only)
		divisor -= adjustment(amAdjustDn, divisor);
		// Try this divisor and the one above it
		long bestDivisor = 0;
		long bestBaud = 0;
		long bestBaudDiff = 0;
		for (int i = 0; i < 2; i++) {
			long tryDivisor = adjustedDivisorAM(divisor, i);
			// Get estimated baud rate (to nearest integer)
			long baudEstimate = (AM_CLK + (tryDivisor / 2)) / tryDivisor;
			long baudDiff = Math.abs(baudRate - baudEstimate);
			if (i != 0 && baudDiff >= bestBaudDiff) continue;
			// Closest to requested baud rate so far
			bestDivisor = tryDivisor;
			bestBaud = baudEstimate;
			bestBaudDiff = baudDiff;
			// Spot on! No point trying
			if (baudDiff == 0) break;
		}
		// Encode the best divisor value
		encodedDivisor = encodedDivisor(bestDivisor);
		// Deal with special cases for encoded value
		if (encodedDivisor == 1) encodedDivisor = 0; // 3000000 baud
		else if (encodedDivisor == 0x4001) encodedDivisor = 1; // 2000000 baud (BM only)
		return bestBaud;
	}

	private static long adjustedDivisorAM(long divisor, int adjust) {
		long tryDivisor = divisor + adjust;
		// Round up to supported divisor value
		if (tryDivisor <= 8) return 8;
		if (divisor < 16) return 16;
		// Round up to supported fraction (AM only)
		tryDivisor += adjustment(amAdjustUp, tryDivisor);
		if (tryDivisor > 0x1fff8) return 0x1fff8;
		return tryDivisor;
	}

	/**
	 * Converts a requested baudRate for a given system clock and predivisor to encoded divisor and
	 * the achievable baudRat
	 */
	private long toClkBits(long baudRate, long clk, long clkDiv) {
		long bestBaud = 0;
		if (baudRate >= clk / clkDiv) {
			encodedDivisor = 0;
			bestBaud = clk / clkDiv;
		} else if (baudRate >= clk / (clkDiv + (clkDiv / 2))) {
			encodedDivisor = 1;
			bestBaud = clk / (clkDiv + (clkDiv / 2));
		} else if (baudRate >= clk / (2 * clkDiv)) {
			encodedDivisor = 2;
			bestBaud = clk / (2 * clkDiv);
		} else {
			long divisor = clk * 16 / clkDiv / baudRate;
			long bestDivisor = halfRound(divisor);
			if (bestDivisor > 0x20000) bestDivisor = 0x1ffff;
			bestBaud = clk * 16 / clkDiv / bestDivisor;
			bestBaud = halfRound(bestBaud);
			encodedDivisor = encodedDivisor(bestDivisor);
		}
		return bestBaud;
	}

	private static int adjustment(ImmutableByteArray array, long index) {
		return array.at((int) index & 0x7);
	}

	private static long encodedDivisor(long bestDivisor) {
		return (bestDivisor >> 3) | (adjustment(fracCode, bestDivisor) << 14);
	}

	private static long halfRound(long value) {
		if ((value & 1) != 0) return (value / 2) + 1;
		return value / 2;
	}

}
