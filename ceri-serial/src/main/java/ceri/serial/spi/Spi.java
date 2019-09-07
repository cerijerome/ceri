package ceri.serial.spi;

import java.io.Closeable;
import java.io.IOException;

public interface Spi extends Closeable {
	enum Direction {
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

	default int speedHz(SpiTransfer xfer) throws IOException {
		int speedHz = xfer.speedHz();
		if (speedHz == 0) speedHz = maxSpeedHz();
		return speedHz;
	}
	
	default int bitsPerWord(SpiTransfer xfer) throws IOException {
		int bitsPerWord = xfer.bitsPerWord();
		if (bitsPerWord == 0) bitsPerWord = bitsPerWord();
		if (bitsPerWord == 0) bitsPerWord = Byte.SIZE;
		return bitsPerWord;
	}


	Spi execute(SpiTransfer xfer) throws IOException;

	default SpiTransfer transfer(int size) {
		return SpiTransfer.of(direction(), size);
	}

}
