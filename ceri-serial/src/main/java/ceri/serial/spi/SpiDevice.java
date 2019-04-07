package ceri.serial.spi;

import static ceri.serial.jna.clib.Fcntl.O_RDONLY;
import static ceri.serial.jna.clib.Fcntl.O_RDWR;
import static ceri.serial.jna.clib.Fcntl.O_WRONLY;
import java.io.Closeable;
import java.io.IOException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ceri.log.util.LogUtil;
import ceri.serial.jna.clib.Fcntl;
import ceri.serial.jna.clib.TermiosUtil;
import ceri.serial.spi.jna.SpiDev;
import ceri.serial.spi.jna.SpiDev.spi_ioc_transfer;

public class SpiDevice implements Closeable {
	private static final Logger logger = LogManager.getLogger();
	private final int bus;
	private final int chip;
	private final int flags;
	private final int fd;

	public static SpiDevice openOut(int bus, int chip) throws IOException {
		return open(bus, chip, O_WRONLY);
	}

	public static SpiDevice openIn(int bus, int chip) throws IOException {
		return open(bus, chip, O_RDONLY);
	}

	public static SpiDevice openDuplex(int bus, int chip) throws IOException {
		return open(bus, chip, O_RDWR);
	}

	private static SpiDevice open(int bus, int chip, int flags) throws IOException {
		int fd = SpiDev.open(bus, chip, flags);
		return new SpiDevice(fd, bus, chip, flags);
	}

	private SpiDevice(int fd, int bus, int chip, int flags) {
		this.fd = fd;
		this.bus = bus;
		this.chip = chip;
		this.flags = flags;
	}

	public SpiTransfer transfer(int size) {
		if (Fcntl.isReadWrite(flags)) return SpiTransfer.duplex(this, size);
		if (Fcntl.isWrite(flags)) return SpiTransfer.out(this, size);
		return SpiTransfer.in(this, size);
	}

	public int bus() {
		return bus;
	}

	public int chip() {
		return chip;
	}

	public SpiDevice mode(SpiMode mode) throws IOException {
		// logger.trace("mode({})", mode);
		if (mode.is32Bit()) SpiDev.setMode32(fd, mode.value);
		else SpiDev.setMode(fd, mode.value);
		return this;
	}

	public SpiMode mode() throws IOException {
		// logger.trace("mode()");
		return SpiMode.of(SpiDev.getMode32(fd));
	}

	SpiDevice message(spi_ioc_transfer transfer) throws IOException {
		// logger.trace("message({})", hashId(transfer));
		SpiDev.message(fd, transfer);
		return this;
	}

	public boolean lsbFirst() throws IOException {
		// logger.trace("lsbFirst()");
		return SpiDev.isLsbFirst(fd);
	}

	public SpiDevice lsbFirst(boolean enabled) throws IOException {
		// logger.trace("lsbFirst({})", lsbFirst);
		SpiDev.setLsbFirst(fd, enabled);
		return this;
	}

	public int bitsPerWord() throws IOException {
		// logger.trace("bitsPerWord()");
		return SpiDev.getBitsPerWord(fd);
	}

	public SpiDevice bitsPerWord(int bitsPerWord) throws IOException {
		// logger.trace("bitsPerWord({})", bitsPerWord);
		SpiDev.setBitsPerWord(fd, bitsPerWord);
		return this;
	}

	public int maxSpeedHz() throws IOException {
		// logger.trace("maxSpeedHz()");
		return SpiDev.getMaxSpeedHz(fd);
	}

	public SpiDevice maxSpeedHz(int maxSpeedHz) throws IOException {
		// logger.trace("maxSpeedHz({})", maxSpeedHz);
		SpiDev.setMaxSpeedHz(fd, maxSpeedHz);
		return this;
	}

	@Override
	public void close() {
		logger.trace("close()");
		LogUtil.execute(logger, () -> TermiosUtil.close(fd));
	}

}
