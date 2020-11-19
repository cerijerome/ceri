package ceri.serial.spi;

import static ceri.serial.clib.OpenFlag.O_RDONLY;
import static ceri.serial.clib.OpenFlag.O_RDWR;
import static ceri.serial.clib.OpenFlag.O_WRONLY;
import static ceri.serial.spi.Spi.Direction.duplex;
import static ceri.serial.spi.Spi.Direction.in;
import static ceri.serial.spi.Spi.Direction.out;
import java.io.IOException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ceri.log.util.LogUtil;
import ceri.serial.clib.OpenFlag;
import ceri.serial.clib.jna.CLib;
import ceri.serial.spi.jna.SpiDev;

public class SpiDevice implements Spi {
	private static final Logger logger = LogManager.getLogger();
	private final int bus;
	private final int chip;
	private final Direction direction;
	private final int fd;

	public static SpiDevice open(int bus, int chip, Direction direction) throws IOException {
		int fd = SpiDev.open(bus, chip, openFlag(direction).value);
		return new SpiDevice(fd, bus, chip, direction);
	}

	private static OpenFlag openFlag(Direction direction) {
		if (direction == out) return O_WRONLY;
		if (direction == in) return O_RDONLY;
		if (direction == duplex) return O_RDWR;
		throw new IllegalArgumentException("Invalid direction: " + direction);
	}

	private SpiDevice(int fd, int bus, int chip, Direction direction) {
		this.fd = fd;
		this.bus = bus;
		this.chip = chip;
		this.direction = direction;
	}

	@Override
	public int bus() {
		return bus;
	}

	@Override
	public int chip() {
		return chip;
	}

	@Override
	public Direction direction() {
		return direction;
	}

	@Override
	public SpiDevice mode(SpiMode mode) throws IOException {
		if (mode.is32Bit()) SpiDev.setMode32(fd, mode.value);
		else SpiDev.setMode(fd, mode.value);
		return this;
	}

	@Override
	public SpiMode mode() throws IOException {
		return SpiMode.of(SpiDev.getMode32(fd));
	}

	@Override
	public Spi execute(SpiTransfer transfer) throws IOException {
		SpiDev.message(fd, transfer.transfer());
		return this;
	}

	@Override
	public boolean lsbFirst() throws IOException {
		return SpiDev.isLsbFirst(fd);
	}

	@Override
	public SpiDevice lsbFirst(boolean enabled) throws IOException {
		SpiDev.setLsbFirst(fd, enabled);
		return this;
	}

	@Override
	public int bitsPerWord() throws IOException {
		return SpiDev.getBitsPerWord(fd);
	}

	@Override
	public SpiDevice bitsPerWord(int bitsPerWord) throws IOException {
		SpiDev.setBitsPerWord(fd, bitsPerWord);
		return this;
	}

	@Override
	public int maxSpeedHz() throws IOException {
		return SpiDev.getMaxSpeedHz(fd);
	}

	@Override
	public SpiDevice maxSpeedHz(int maxSpeedHz) throws IOException {
		SpiDev.setMaxSpeedHz(fd, maxSpeedHz);
		return this;
	}

	@Override
	public void close() {
		logger.trace("close()");
		LogUtil.execute(logger, () -> CLib.close(fd));
	}

}
