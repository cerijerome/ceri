package ceri.serial.spi;

import static ceri.common.validation.ValidationUtil.validateMin;
import static ceri.common.validation.ValidationUtil.validateNotNull;
import java.io.IOException;
import ceri.serial.clib.FileDescriptor;
import ceri.serial.spi.jna.SpiDev;
import ceri.serial.spi.jna.SpiDev.spi_ioc_transfer;

/**
 * Spi instance using a file descriptor. SpiDeviceConfig can be used to open a descriptor.  
 */
public class SpiDevice implements Spi {
	private final FileDescriptor fd;

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

}
