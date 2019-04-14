package ceri.serial.spi;

import java.io.Closeable;
import java.io.IOException;

public interface Spi extends Closeable {
	static enum Direction {
		in,
		out,
		duplex;
	}

	int bus();

	int chip();

	Direction direction();

	SpiMode mode() throws IOException;

	Spi mode(SpiMode mode) throws IOException;

	boolean lsbFirst() throws IOException;

	Spi lsbFirst(boolean enabled) throws IOException;

	int bitsPerWord() throws IOException;

	Spi bitsPerWord(int bitsPerWord) throws IOException;

	int maxSpeedHz() throws IOException;

	Spi maxSpeedHz(int maxSpeedHz) throws IOException;

	default int actualMaxSpeedHz(int defaultMaxSpeedHz) throws IOException {
		int maxSpeedHz = maxSpeedHz();
		return maxSpeedHz == 0 ? defaultMaxSpeedHz : maxSpeedHz;
	}
	
	Spi execute(SpiTransfer xfer) throws IOException;

	default SpiTransfer transfer(int size) {
		return SpiTransfer.of(direction(), size);
	}

}
