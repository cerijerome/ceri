package ceri.serial.ftdi.util;

import static ceri.common.function.FunctionUtil.namedPredicate;
import static ceri.serial.libusb.jna.LibUsbFinder.libusb_find_criteria_string;
import java.util.function.Predicate;
import ceri.common.text.ToStringHelper;
import ceri.serial.ftdi.FtdiBitmode;
import ceri.serial.ftdi.FtdiLineParams;
import ceri.serial.ftdi.jna.LibFtdi.ftdi_interface;
import ceri.serial.ftdi.jna.LibFtdi.ftdi_mpsse_mode;
import ceri.serial.ftdi.jna.LibFtdiUtil;
import ceri.serial.libusb.jna.LibUsbFinder.libusb_device_criteria;

/**
 * A self-healing ftdi device. It will automatically reconnect if the cable is removed and
 * reinserted.
 */
public class SelfHealingFtdiConfig {
	public static final SelfHealingFtdiConfig NULL = builder().build();
	static final Predicate<Exception> DEFAULT_PREDICATE =
		namedPredicate(SelfHealingFtdi::isBroken, "SelfHealingFtdi::isBroken");
	final libusb_device_criteria find;
	final ftdi_interface iface;
	final Integer baud;
	final FtdiLineParams line;
	final FtdiBitmode bitmode;
	final int fixRetryDelayMs;
	final int recoveryDelayMs;
	final Predicate<Exception> brokenPredicate;

	public static SelfHealingFtdiConfig of() {
		return builder().find(LibFtdiUtil.finder()).build();
	}

	public static class Builder {
		libusb_device_criteria find = null;
		ftdi_interface iface = ftdi_interface.INTERFACE_ANY;
		Integer baud = 9600; // null?
		FtdiLineParams line = FtdiLineParams.DEFAULT;
		FtdiBitmode bitmode = FtdiBitmode.BITBANG;
		int fixRetryDelayMs = 2000;
		int recoveryDelayMs = fixRetryDelayMs / 2;
		Predicate<Exception> brokenPredicate = DEFAULT_PREDICATE;

		Builder() {}

		public Builder find(String find) {
			return find(libusb_find_criteria_string(find));
		}

		public Builder find(libusb_device_criteria find) {
			this.find = find;
			return this;
		}

		public Builder iface(ftdi_interface iface) {
			this.iface = iface;
			return this;
		}

		public Builder baud(int baud) {
			this.baud = baud;
			return this;
		}

		public Builder line(FtdiLineParams line) {
			this.line = line;
			return this;
		}

		public Builder bitmode(FtdiBitmode bitmode) {
			this.bitmode = bitmode;
			return this;
		}

		public Builder bitmode(ftdi_mpsse_mode mode) {
			return bitmode(FtdiBitmode.of(mode));
		}

		public Builder fixRetryDelayMs(int fixRetryDelayMs) {
			this.fixRetryDelayMs = fixRetryDelayMs;
			return this;
		}

		public Builder recoveryDelayMs(int recoveryDelayMs) {
			this.recoveryDelayMs = recoveryDelayMs;
			return this;
		}

		public Builder brokenPredicate(Predicate<Exception> brokenPredicate) {
			this.brokenPredicate = brokenPredicate;
			return this;
		}

		public SelfHealingFtdiConfig build() {
			return new SelfHealingFtdiConfig(this);
		}
	}

	public static Builder builder() {
		return new Builder();
	}

	public boolean enabled() {
		return find != null;
	}

	SelfHealingFtdiConfig(Builder builder) {
		find = builder.find;
		iface = builder.iface;
		baud = builder.baud;
		line = builder.line;
		bitmode = builder.bitmode;
		fixRetryDelayMs = builder.fixRetryDelayMs;
		recoveryDelayMs = builder.recoveryDelayMs;
		brokenPredicate = builder.brokenPredicate;
	}

	@Override
	public String toString() {
		return ToStringHelper.createByClass(this, find, iface, baud, line, bitmode, fixRetryDelayMs,
			recoveryDelayMs, brokenPredicate).toString();
	}

}
