package ceri.serial.ftdi.util;

import static ceri.common.function.FunctionUtil.lambdaName;
import static ceri.common.function.FunctionUtil.named;
import java.util.function.Predicate;
import ceri.common.text.ToString;
import ceri.serial.ftdi.FtdiBitMode;
import ceri.serial.ftdi.FtdiLineParams;
import ceri.serial.ftdi.jna.LibFtdi.ftdi_interface;
import ceri.serial.ftdi.jna.LibFtdi.ftdi_mpsse_mode;
import ceri.serial.ftdi.jna.LibFtdiUtil;
import ceri.serial.libusb.jna.LibUsbFinder;

/**
 * A self-healing FTDI device. It will automatically try to reconnect if a fatal error is detected.
 */
public class SelfHealingFtdiConfig {
	public static final SelfHealingFtdiConfig DEFAULT = builder().build();
	static final Predicate<Exception> DEFAULT_PREDICATE =
		named(SelfHealingFtdiConnector::isBroken, "SelfHealingFtdi::isBroken");
	final LibUsbFinder finder;
	final ftdi_interface iface;
	final Integer baud;
	final FtdiLineParams line;
	final FtdiBitMode bitMode;
	final int fixRetryDelayMs;
	final int recoveryDelayMs;
	final Predicate<Exception> brokenPredicate;

	public static SelfHealingFtdiConfig of(String finder) {
		return builder().finder(finder).build();
	}

	public static class Builder {
		LibUsbFinder finder = LibFtdiUtil.FINDER;
		ftdi_interface iface = ftdi_interface.INTERFACE_ANY;
		Integer baud = 9600;
		FtdiLineParams line = FtdiLineParams.DEFAULT;
		FtdiBitMode bitmode = FtdiBitMode.BITBANG;
		int fixRetryDelayMs = 2000;
		int recoveryDelayMs = fixRetryDelayMs / 2;
		Predicate<Exception> brokenPredicate = DEFAULT_PREDICATE;

		Builder() {}

		public Builder finder(String descriptor) {
			return finder(LibUsbFinder.from(descriptor));
		}

		public Builder finder(LibUsbFinder finder) {
			this.finder = finder;
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

		public Builder bitmode(FtdiBitMode bitmode) {
			this.bitmode = bitmode;
			return this;
		}

		public Builder bitmode(ftdi_mpsse_mode mode) {
			return bitmode(FtdiBitMode.of(mode));
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

	SelfHealingFtdiConfig(Builder builder) {
		finder = builder.finder;
		iface = builder.iface;
		baud = builder.baud;
		line = builder.line;
		bitMode = builder.bitmode;
		fixRetryDelayMs = builder.fixRetryDelayMs;
		recoveryDelayMs = builder.recoveryDelayMs;
		brokenPredicate = builder.brokenPredicate;
	}

	@Override
	public String toString() {
		return ToString.forClass(this, finder, iface, baud, line, bitMode, fixRetryDelayMs,
			recoveryDelayMs, lambdaName(brokenPredicate));
	}

}
