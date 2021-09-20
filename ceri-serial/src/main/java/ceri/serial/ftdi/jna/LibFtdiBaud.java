package ceri.serial.ftdi.jna;

import static ceri.common.math.MathUtil.ushort;
import static ceri.serial.libusb.jna.LibUsb.libusb_error.LIBUSB_ERROR_INVALID_PARAM;
import ceri.common.data.ByteArray.Immutable;
import ceri.common.data.ByteProvider;
import ceri.serial.ftdi.jna.LibFtdi.ftdi_chip_type;
import ceri.serial.ftdi.jna.LibFtdi.ftdi_context;
import ceri.serial.libusb.jna.LibUsbException;

/**
 * Utility to convert desired baud rate into supported rate by chip type.
 */
public class LibFtdiBaud {
	private static final ByteProvider fracCode = Immutable.wrap(0, 3, 2, 4, 1, 5, 6, 7);
	private static final ByteProvider amAdjustUp = Immutable.wrap(0, 0, 0, 1, 0, 3, 2, 1);
	private static final ByteProvider amAdjustDn = Immutable.wrap(0, 0, 0, 1, 0, 1, 2, 3);
	private static final int MASK = 0x7;
	private static final int H_CLK = 120000000;
	private static final int C_CLK = 48000000;
	private static final int AM_CLK = 24000000;
	private static final int DIVISOR_MAX = 0x1ffff;
	private static final int DIVISOR_10X = 0x20000;
	private static final int DIVISOR_AM_MAX = 0x1fff8;
	private static final int DIVISOR_AM_SPECIAL = 0x4001; // Meaning?
	private final ftdi_chip_type type;
	private final int index;
	private int actualRate;
	private long encodedDivisor;

	public static LibFtdiBaud from(ftdi_context ftdi, int rate) throws LibUsbException {
		if (ftdi.bitbang_enabled) rate <<= 2; // x4 baud for bitbang
		return of(ftdi.type, ftdi.index, rate);
	}

	public static LibFtdiBaud of(ftdi_chip_type type, int index, int rate) throws LibUsbException {
		return new LibFtdiBaud(type, index, rate);
	}

	private LibFtdiBaud(ftdi_chip_type type, int index, int rate) throws LibUsbException {
		this.type = type;
		this.index = index;
		convert(rate);
		validate(rate, actualRate);
	}

	public int actualRate() {
		return actualRate;
	}

	public int value() {
		return ushort(encodedDivisor);
	}

	public int index() {
		if (ftdi_chip_type.isHType(type)) return (int) (((encodedDivisor >> 8) & 0xff00) | index);
		return ushort(encodedDivisor >> 16);
	}

	private void convert(int rate) {
		if (rate <= 0) return;
		if (ftdi_chip_type.isHType(type) && (rate * 10 > H_CLK / 0x3fff)) {
			toClockBits(rate, H_CLK, 10);
			encodedDivisor |= DIVISOR_10X; // switch on CLK/10
		} else if (ftdi_chip_type.isAmType(type)) {
			toClockBitsAm(rate);
		} else toClockBits(rate, C_CLK, 16);
	}

	private void toClockBits(int rate, int clk, int clkDiv) {
		if (rate >= clk / clkDiv) {
			encodedDivisor = 0;
			actualRate = clk / clkDiv;
		} else if (rate >= clk / (clkDiv + (clkDiv >>> 1))) {
			encodedDivisor = 1;
			actualRate = clk / (clkDiv + (clkDiv >>> 1));
		} else if (rate >= clk / (clkDiv << 1)) {
			encodedDivisor = 2;
			actualRate = clk / (clkDiv << 1);
		} else {
			int bestDivisor = bestDivisor((clk << 4) / clkDiv / rate);
			actualRate = (clk << 4) / clkDiv / bestDivisor;
			actualRate = (actualRate >> 1) + (actualRate & 1);
			encodedDivisor = (bestDivisor >> 3) | (fracCode(bestDivisor) << 14);
		}
	}

	private static int bestDivisor(int divisor) {
		divisor = (divisor >> 1) + (divisor & 1);
		if (divisor > DIVISOR_MAX + 1) divisor = DIVISOR_MAX;
		return divisor;
	}

	private void toClockBitsAm(int rate) {
		int divisor = initDivisorAm(rate);
		int bestDivisor = 0;
		int bestDiff = 0;
		for (int i = 0; i < 2; i++) {
			int tryDivisor = tryDivisorAm(divisor + i);
			int estimate = (AM_CLK + (tryDivisor / 2)) / tryDivisor;
			int diff = estimate < rate ? rate - estimate : estimate - rate;
			if (i == 0 || diff < bestDiff) {
				bestDivisor = tryDivisor;
				actualRate = estimate;
				bestDiff = diff;
				if (diff == 0) break;
			}
		}
		encodeDivisorAm(bestDivisor);
	}

	private void encodeDivisorAm(int divisor) {
		encodedDivisor = (divisor >> 3) | (fracCode(divisor) << 14);
		if (encodedDivisor == 1) encodedDivisor = 0; // 3000000 baud
		else if (encodedDivisor == DIVISOR_AM_SPECIAL) // never happens?
			encodedDivisor = 1; // 2000000 baud (BM only)
	}

	private int initDivisorAm(int rate) {
		int divisor = AM_CLK / rate;
		return divisor - amAdjustDn(divisor);
	}

	private int tryDivisorAm(int divisor) {
		if (divisor <= 8) return 8;
		if (divisor < 16) return 16;
		divisor += amAdjustUp(divisor);
		return Math.min(divisor, DIVISOR_AM_MAX);
	}

	private static int fracCode(int index) {
		return fracCode.getByte(index & MASK);
	}

	private static int amAdjustUp(int index) {
		return amAdjustUp.getByte(index & MASK);
	}

	private static int amAdjustDn(int index) {
		return amAdjustDn.getByte(index & MASK);
	}

	private static void validate(int rate, int actual) throws LibUsbException {
		if (actual <= 0)
			throw LibUsbException.of(LIBUSB_ERROR_INVALID_PARAM, "Baudrate <= 0: " + actual);
		if (isUnsupported(rate, actual)) throw LibUsbException.of(LIBUSB_ERROR_INVALID_PARAM,
			"Unsupported baudrate: %d (%d)", rate, actual);
	}

	private static boolean isUnsupported(int rate, int actual) {
		if (actual * 2 < rate) return true;
		if (actual < rate) return (actual * 21 < rate * 20);
		return (rate * 21 < actual * 20);
	}

}
