package ceri.serial.ftdi.util;

import java.util.function.Predicate;
import ceri.common.function.Namer;
import ceri.common.text.ToString;
import ceri.log.io.SelfHealingConfig;
import ceri.serial.ftdi.FtdiDevice;
import ceri.serial.ftdi.jna.LibFtdi.ftdi_interface;
import ceri.serial.ftdi.jna.LibFtdiUtil;
import ceri.serial.libusb.jna.LibUsbFinder;

/**
 * A self-healing FTDI device. It will automatically try to reconnect if a fatal error is detected.
 */
public class SelfHealingFtdiConfig {
	public static final SelfHealingFtdiConfig DEFAULT = builder().build();
	private static final Predicate<Exception> DEFAULT_PREDICATE =
		Namer.predicate(FtdiDevice::isFatal, "Ftdi::isFatal");
	public final LibUsbFinder finder;
	public final ftdi_interface iface;
	public final FtdiConfig ftdi;
	public final SelfHealingConfig selfHealing;

	public static SelfHealingFtdiConfig of(String finder) {
		return of(LibUsbFinder.from(finder));
	}

	public static SelfHealingFtdiConfig of(LibUsbFinder finder) {
		return builder().finder(finder).build();
	}

	public static class Builder {
		LibUsbFinder finder = LibFtdiUtil.FINDER;
		ftdi_interface iface = ftdi_interface.INTERFACE_ANY;
		FtdiConfig ftdi = FtdiConfig.NULL;
		SelfHealingConfig.Builder selfHealing =
			SelfHealingConfig.builder().brokenPredicate(DEFAULT_PREDICATE);

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

		public Builder ftdi(FtdiConfig ftdi) {
			this.ftdi = ftdi;
			return this;
		}

		public Builder selfHealing(SelfHealingConfig selfHealing) {
			this.selfHealing.apply(selfHealing);
			return this;
		}

		public SelfHealingFtdiConfig build() {
			return new SelfHealingFtdiConfig(this);
		}
	}

	public static Builder builder() {
		return new Builder();
	}

	public static Builder builder(SelfHealingFtdiConfig config) {
		return builder().finder(config.finder).iface(config.iface).ftdi(config.ftdi)
			.selfHealing(config.selfHealing);
	}

	SelfHealingFtdiConfig(Builder builder) {
		finder = builder.finder;
		iface = builder.iface;
		ftdi = builder.ftdi;
		selfHealing = builder.selfHealing.build();
	}

	@Override
	public String toString() {
		return ToString.forClass(this, finder, iface, ftdi, selfHealing);
	}

}
