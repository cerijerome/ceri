package ceri.serial.spi;

import static ceri.common.validation.ValidationUtil.validateMin;
import static ceri.common.validation.ValidationUtil.validateNotNull;
import java.io.IOException;
import java.util.Objects;
import ceri.common.text.ToString;
import ceri.serial.clib.CFileDescriptor;
import ceri.serial.spi.Spi.Direction;

/**
 * Configuration to open SPI file descriptor.
 */
public class SpiDeviceConfig {
	public static final SpiDeviceConfig DEFAULT = of(0, 0);
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
			return new SpiDeviceConfig(bus, chip, direction);
		}
	}

	public static Builder builder() {
		return new Builder();
	}

	SpiDeviceConfig(int bus, int chip, Direction direction) {
		this.bus = bus;
		this.chip = chip;
		this.direction = direction;
	}

	/**
	 * Opens the SPI file descriptor. Can be used as the open function for a SelfHealingFd.
	 */
	public CFileDescriptor open() throws IOException {
		return SpiDevice.open(bus, chip, direction);
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
}
