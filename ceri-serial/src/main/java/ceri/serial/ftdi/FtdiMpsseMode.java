package ceri.serial.ftdi;

import ceri.common.data.TypeTranscoder;

/**
 * MPSSE bitbang modes
 */
public enum FtdiMpsseMode {
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

	/* CPU-style fifo mode gets set via EEPROM */

	/** Fast Opto-Isolated Serial Interface Mode, available on 2232x chips */
	BITMODE_OPTO(0x10),
	/** Bitbang on CBUS pins of R-type chips, configure in EEPROM before */
	BITMODE_CBUS(0x20),
	/** Single Channel Synchronous FIFO mode, available on 2232H chips */
	BITMODE_SYNCFF(0x40),
	/** FT1284 mode, available on 232H chips */
	BITMODE_FT1284(0x80);

	public static final TypeTranscoder.Single<FtdiMpsseMode> xcoder =
		TypeTranscoder.single(t -> t.value, FtdiMpsseMode.class);
	public final int value;

	private FtdiMpsseMode(int value) {
		this.value = value;
	}

}
