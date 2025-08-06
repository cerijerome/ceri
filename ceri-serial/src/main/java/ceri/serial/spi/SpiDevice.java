package ceri.serial.spi;

import static ceri.common.validation.ValidationUtil.validateMin;
import java.io.IOException;
import java.util.Objects;
import ceri.common.function.Excepts.Function;
import ceri.common.io.Direction;
import ceri.common.property.TypedProperties;
import ceri.common.text.ToString;
import ceri.common.util.BasicUtil;
import ceri.jna.clib.CFileDescriptor;
import ceri.jna.clib.FileDescriptor;
import ceri.jna.clib.FileDescriptor.Open;
import ceri.serial.spi.jna.SpiDev;
import ceri.serial.spi.jna.SpiDev.spi_ioc_transfer;

/**
 * Spi instance using a file descriptor. SpiDeviceConfig can be used to open a descriptor.
 */
public class SpiDevice implements Spi {
	private final FileDescriptor fd;

	/**
	 * Configuration to open SPI file descriptor.
	 */
	public static class Config {
		public static final Config DEFAULT = of(0, 0);
		private final Function<IOException, Config, FileDescriptor> openFn;
		public final int bus;
		public final int chip;
		public final Direction direction;

		public static Config of(int bus, int chip) {
			return builder().bus(bus).chip(chip).build();
		}

		public static Config of(int bus, int chip, Direction direction) {
			return builder().bus(bus).chip(chip).direction(direction).build();
		}

		public static class Builder {
			Function<IOException, Config, FileDescriptor> openFn = SpiDevice::open;
			int bus = 0;
			int chip = 0;
			Direction direction = Direction.duplex;

			Builder() {}

			public Builder openFn(Function<IOException, Config, FileDescriptor> openFn) {
				this.openFn = openFn;
				return this;
			}

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
				BasicUtil.requireNot(direction, null, Direction.none);
				this.direction = direction;
				return this;
			}

			public Config build() {
				return new Config(this);
			}
		}

		public static Builder builder() {
			return new Builder();
		}

		Config(Builder builder) {
			openFn = builder.openFn;
			bus = builder.bus;
			chip = builder.chip;
			direction = builder.direction;
		}

		/**
		 * Opens the SPI file descriptor. Can be used as the open function for a SelfHealingFd.
		 */
		public FileDescriptor open() throws IOException {
			return openFn.apply(this);
		}

		@Override
		public int hashCode() {
			return Objects.hash(bus, chip, direction);
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) return true;
			return (obj instanceof Config other) && bus == other.bus && chip == other.chip
				&& Objects.equals(direction, other.direction);
		}

		@Override
		public String toString() {
			return ToString.forClass(this, bus, chip, direction);
		}
	}

	/**
	 * Properties for config.
	 */
	public static class Properties extends TypedProperties.Ref {
		private static final String BUS_KEY = "bus";
		private static final String CHIP_KEY = "chip";
		private static final String DIRECTION_KEY = "direction";

		public Properties(TypedProperties properties, String... groups) {
			super(properties, groups);
		}

		public Config config() {
			var b = Config.builder();
			parse(BUS_KEY).asInt().accept(b::bus);
			parse(CHIP_KEY).asInt().accept(b::chip);
			parse(DIRECTION_KEY).asEnum(Direction.class).accept(b::direction);
			return b.build();
		}
	}

	/**
	 * Opens the SPI file descriptor.
	 */
	public static CFileDescriptor open(Config config) throws IOException {
		return open(config.bus, config.chip, config.direction);
	}

	/**
	 * Opens the SPI file descriptor.
	 */
	public static CFileDescriptor open(int bus, int chip, Direction direction) throws IOException {
		BasicUtil.requireNot(direction, null, Direction.none);
		validateMin(bus, 0, "Bus number");
		validateMin(chip, 0, "Chip number");
		return CFileDescriptor.of(SpiDev.open(bus, chip, openFlag(direction).value));
	}

	/**
	 * Creates an instance using given file descriptor.
	 */
	public static SpiDevice of(FileDescriptor fd) {
		return new SpiDevice(fd);
	}

	private SpiDevice(FileDescriptor fd) {
		this.fd = fd;
	}

	@Override
	public void mode(SpiMode mode) throws IOException {
		fd.accept(fd -> {
			if (mode.is32Bit()) SpiDev.setMode32(fd, mode.value());
			else SpiDev.setMode(fd, mode.value());
		});
	}

	@Override
	public SpiMode mode() throws IOException {
		return new SpiMode(fd.apply(SpiDev::getMode32));
	}

	@Override
	public boolean lsbFirst() throws IOException {
		return fd.apply(SpiDev::isLsbFirst);
	}

	@Override
	public void lsbFirst(boolean enabled) throws IOException {
		fd.accept(fd -> SpiDev.setLsbFirst(fd, enabled));
	}

	@Override
	public int bitsPerWord() throws IOException {
		return fd.apply(SpiDev::getBitsPerWord);
	}

	@Override
	public void bitsPerWord(int bitsPerWord) throws IOException {
		fd.accept(fd -> SpiDev.setBitsPerWord(fd, bitsPerWord));
	}

	@Override
	public int maxSpeedHz() throws IOException {
		return fd.apply(SpiDev::getMaxSpeedHz);
	}

	@Override
	public void maxSpeedHz(int maxSpeedHz) throws IOException {
		fd.accept(fd -> SpiDev.setMaxSpeedHz(fd, maxSpeedHz));
	}

	@Override
	public SpiTransfer transfer(Direction direction, int size) {
		BasicUtil.requireNot(direction, null, Direction.none);
		validateMin(size, 0, "Size");
		return SpiTransfer.of(this::execute, direction, size);
	}

	private void execute(spi_ioc_transfer transfer) throws IOException {
		fd.accept(fd -> SpiDev.message(fd, transfer));
	}

	private static Open openFlag(Direction direction) {
		if (direction == Direction.out) return Open.WRONLY;
		if (direction == Direction.in) return Open.RDONLY;
		return Open.RDWR;
	}
}
