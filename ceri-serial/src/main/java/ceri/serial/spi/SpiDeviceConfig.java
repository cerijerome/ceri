package ceri.serial.spi;

import static ceri.common.validation.ValidationUtil.validateMin;
import static ceri.common.validation.ValidationUtil.validateNotNull;
import static ceri.serial.clib.OpenFlag.O_RDONLY;
import static ceri.serial.clib.OpenFlag.O_RDWR;
import static ceri.serial.clib.OpenFlag.O_WRONLY;
import static ceri.serial.spi.Spi.Direction.in;
import static ceri.serial.spi.Spi.Direction.out;
import java.util.Objects;
import ceri.common.text.ToString;
import ceri.serial.clib.CFileDescriptor;
import ceri.serial.clib.OpenFlag;
import ceri.serial.clib.jna.CException;
import ceri.serial.spi.Spi.Direction;
import ceri.serial.spi.jna.SpiDev;

/**
 * Configuration to open SPI file descriptor.
 */
public class SpiDeviceConfig {
	private final int bus;
	private final int chip;
	private final Direction direction;

	public static SpiDeviceConfig of(int bus, int chip) {
		return builder().bus(bus).chip(chip).build();
	}
	
	public static SpiDeviceConfig of(int bus, int chip, Direction direction) {
		return builder().bus(bus).chip(chip).direction(direction).build();
	}
	
	public static class Builder {
		int bus = 0;
		int chip = 0;
		Direction direction = Direction.duplex;

		Builder() {}

		public Builder bus(int bus) {
			validateMin(bus, 0);
			this.bus = bus;
			return this;
		}

		public Builder chip(int chip) {
			validateMin(chip, 0);
			this.chip = chip;
			return this;
		}

		public Builder direction(Direction direction) {
			validateNotNull(direction);
			this.direction = direction;
			return this;
		}

		public SpiDeviceConfig build() {
			return new SpiDeviceConfig(this);
		}
	}

	public static Builder builder() {
		return new Builder();
	}

	SpiDeviceConfig(Builder builder) {
		bus = builder.bus;
		chip = builder.chip;
		direction = builder.direction;
	}

	/**
	 * Opens the SPI file descriptor. Can be used as the open function for a SelfHealingFd.
	 */
	public CFileDescriptor open() throws CException {
		return CFileDescriptor.of(SpiDev.open(bus, chip, openFlag(direction).value));
	}

	@Override
	public int hashCode() {
		return Objects.hash(bus, chip, direction);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof SpiDeviceConfig)) return false;
		SpiDeviceConfig other = (SpiDeviceConfig) obj;
		if (bus != other.bus) return false;
		if (chip != other.chip) return false;
		if (!Objects.equals(direction, other.direction)) return false;
		return true;
	}

	@Override
	public String toString() {
		return ToString.forClass(this, bus, chip, direction);
	}

	private static OpenFlag openFlag(Direction direction) {
		if (direction == out) return O_WRONLY;
		if (direction == in) return O_RDONLY;
		return O_RDWR;
	}
}
