package ceri.serial.spi;

import static ceri.common.validation.ValidationUtil.validateMin;
import static ceri.common.validation.ValidationUtil.validateNotNull;
import static ceri.jna.clib.OpenFlag.O_RDONLY;
import static ceri.jna.clib.OpenFlag.O_RDWR;
import static ceri.jna.clib.OpenFlag.O_WRONLY;
import java.io.IOException;
import ceri.common.io.Direction;
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
	public void mode(SpiMode mode) throws IOException {
		fd.accept(fd -> {
			if (mode.is32Bit()) SpiDev.setMode32(fd, mode.value);
			else SpiDev.setMode(fd, mode.value);
		});
	}

	@Override
	public SpiMode mode() throws IOException {
		return SpiMode.of(fd.apply(SpiDev::getMode32));
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
		validateNotNull(direction, "Direction");
		validateMin(size, 0, "Size");
		return SpiTransfer.of(this::execute, direction, size);
	}

	private void execute(spi_ioc_transfer transfer) throws IOException {
		fd.accept(fd -> SpiDev.message(fd, transfer));
	}

	private static OpenFlag openFlag(Direction direction) {
		if (direction == Direction.out) return O_WRONLY;
		if (direction == Direction.in) return O_RDONLY;
		return O_RDWR;
	}
}
