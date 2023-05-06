package ceri.serial.spi;

import static ceri.common.validation.ValidationUtil.validateMin;
import static ceri.common.validation.ValidationUtil.validateNotNull;
import static ceri.jna.clib.OpenFlag.O_RDONLY;
import static ceri.jna.clib.OpenFlag.O_RDWR;
import static ceri.jna.clib.OpenFlag.O_WRONLY;
import static ceri.serial.spi.Spi.Direction.in;
import static ceri.serial.spi.Spi.Direction.out;
import java.io.IOException;
import ceri.jna.clib.CFileDescriptor;
import ceri.jna.clib.FileDescriptor;
import ceri.jna.clib.OpenFlag;
import ceri.serial.spi.jna.SpiDev;
import ceri.serial.spi.jna.SpiDev.spi_ioc_transfer;

/**
 * Spi instance using a file descriptor. SpiDeviceConfig can be used to open a descriptor.
 */
public class SpiDevice implements Spi {
	private final FileDescriptor fd;

	/**
	 * Opens the SPI file descriptor. Can be used as the open function for a SelfHealingFd.
	 */
	public static CFileDescriptor open(int bus, int chip, Direction direction) throws IOException {
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
	public SpiDevice mode(SpiMode mode) throws IOException {
		if (mode.is32Bit()) SpiDev.setMode32(fd.fd(), mode.value);
		else SpiDev.setMode(fd.fd(), mode.value);
		return this;
	}

	@Override
	public SpiMode mode() throws IOException {
		return SpiMode.of(SpiDev.getMode32(fd.fd()));
	}

	@Override
	public boolean lsbFirst() throws IOException {
		return SpiDev.isLsbFirst(fd.fd());
	}

	@Override
	public SpiDevice lsbFirst(boolean enabled) throws IOException {
		SpiDev.setLsbFirst(fd.fd(), enabled);
		return this;
	}

	@Override
	public int bitsPerWord() throws IOException {
		return SpiDev.getBitsPerWord(fd.fd());
	}

	@Override
	public SpiDevice bitsPerWord(int bitsPerWord) throws IOException {
		SpiDev.setBitsPerWord(fd.fd(), bitsPerWord);
		return this;
	}

	@Override
	public int maxSpeedHz() throws IOException {
		return SpiDev.getMaxSpeedHz(fd.fd());
	}

	@Override
	public SpiDevice maxSpeedHz(int maxSpeedHz) throws IOException {
		SpiDev.setMaxSpeedHz(fd.fd(), maxSpeedHz);
		return this;
	}

	@Override
	public SpiTransfer transfer(Direction direction, int size) {
		validateNotNull(direction, "Direction");
		validateMin(size, 0, "Size");
		return SpiTransfer.of(this::execute, direction, size);
	}

	private void execute(spi_ioc_transfer transfer) throws IOException {
		SpiDev.message(fd.fd(), transfer);
	}

	private static OpenFlag openFlag(Direction direction) {
		if (direction == out) return O_WRONLY;
		if (direction == in) return O_RDONLY;
		return O_RDWR;
	}
}
