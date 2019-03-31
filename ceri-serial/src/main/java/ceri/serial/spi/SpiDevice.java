package ceri.serial.spi;

import static ceri.serial.jna.clib.Fcntl.O_RDONLY;
import static ceri.serial.jna.clib.Fcntl.O_RDWR;
import static ceri.serial.jna.clib.Fcntl.O_WRONLY;
import java.io.Closeable;
import java.io.IOException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ceri.log.util.LogUtil;
import ceri.serial.jna.clib.TermiosUtil;
import ceri.serial.spi.jna.SpiDev;
import ceri.serial.spi.jna.SpiDev.spi_ioc_transfer;

public class SpiDevice implements Closeable {
	private static final Logger logger = LogManager.getLogger();
	// private final int fd;
	final int fd; // TODO: private

	public static SpiDevice openOut(int bus, int chip) throws IOException {
		return open(bus, chip, O_WRONLY);
	}

	public static SpiDevice openIn(int bus, int chip) throws IOException {
		return open(bus, chip, O_RDONLY);
	}

	public static SpiDevice openInOut(int bus, int chip) throws IOException {
		return open(bus, chip, O_RDWR);
	}

	private static SpiDevice open(int bus, int chip, int flags) throws IOException {
		int fd = SpiDev.open(bus, chip, flags);
		return new SpiDevice(fd);
	}

	private SpiDevice(int fd) {
		this.fd = fd;
	}

	public void mode(SpiMode mode) throws IOException {
		//logger.trace("mode({})", mode);
		if (mode.is32Bit()) SpiDev.setMode32(fd, mode.value);
		else SpiDev.setMode(fd, mode.value);
	}

	public SpiMode mode() throws IOException {
		//logger.trace("mode()");
		return SpiMode.of(SpiDev.getMode32(fd));
	}

	public void message(spi_ioc_transfer transfer) throws IOException {
		//logger.trace("message({})", hashId(transfer));
		SpiDev.message(fd, transfer);
	}

	public boolean lsbFirst() throws IOException {
		//logger.trace("lsbFirst()");
		return SpiDev.isLsbFirst(fd);
	}

	public void lsbFirst(boolean enabled) throws IOException {
		//logger.trace("lsbFirst({})", lsbFirst);
		SpiDev.setLsbFirst(fd, enabled);
	}

	public int bitsPerWord() throws IOException {
		//logger.trace("bitsPerWord()");
		return SpiDev.getBitsPerWord(fd);
	}

	public void bitsPerWord(int bitsPerWord) throws IOException {
		//logger.trace("bitsPerWord({})", bitsPerWord);
		SpiDev.setBitsPerWord(fd, bitsPerWord);
	}

	public int maxSpeedHz() throws IOException {
		//logger.trace("maxSpeedHz()");
		return SpiDev.getMaxSpeedHz(fd);
	}

	public void maxSpeedHz(int maxSpeedHz) throws IOException {
		//logger.trace("maxSpeedHz({})", maxSpeedHz);
		SpiDev.setMaxSpeedHz(fd, maxSpeedHz);
	}

	@Override
	public void close() {
		logger.trace("close()");
		LogUtil.execute(logger, () -> TermiosUtil.close(fd));
	}

}
