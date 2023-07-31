package ceri.serial.spi;

import java.io.IOException;

public interface Spi {
	/** A stateless, no-op instance. */
	Spi NULL = new Null() {};

	enum Direction {
		in,
		out,
		duplex;
	}

	SpiMode mode() throws IOException;

	Spi mode(SpiMode mode) throws IOException;

	boolean lsbFirst() throws IOException;

	Spi lsbFirst(boolean enabled) throws IOException;

	int bitsPerWord() throws IOException;

	Spi bitsPerWord(int bitsPerWord) throws IOException;

	int maxSpeedHz() throws IOException;

	Spi maxSpeedHz(int maxSpeedHz) throws IOException;

	SpiTransfer transfer(Direction direction, int size);

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

	/**
	 * A stateless, no-op implementation.
	 */
	interface Null extends Spi {
		@Override
		default SpiMode mode() {
			return SpiMode.MODE_0;
		}

		@Override
		default Spi mode(SpiMode mode) {
			return this;
		}

		@Override
		default boolean lsbFirst() {
			return false;
		}

		@Override
		default Spi lsbFirst(boolean enabled) {
			return this;
		}

		@Override
		default int bitsPerWord() {
			return 0;
		}

		@Override
		default Spi bitsPerWord(int bitsPerWord) {
			return this;
		}

		@Override
		default int maxSpeedHz() {
			return 0;
		}

		@Override
		default Spi maxSpeedHz(int maxSpeedHz) {
			return this;
		}

		@Override
		default SpiTransfer transfer(Direction direction, int size) {
			return SpiTransfer.of(t -> {}, direction, size);
		}
	}
}
