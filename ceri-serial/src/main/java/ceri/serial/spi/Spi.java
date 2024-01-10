package ceri.serial.spi;

import java.io.IOException;
import ceri.common.io.Direction;

public interface Spi {
	/** A stateless, no-op instance. */
	Spi NULL = new Null() {};

	SpiMode mode() throws IOException;

	void mode(SpiMode mode) throws IOException;

	boolean lsbFirst() throws IOException;

	void lsbFirst(boolean enabled) throws IOException;

	int bitsPerWord() throws IOException;

	void bitsPerWord(int bitsPerWord) throws IOException;

	int maxSpeedHz() throws IOException;

	void maxSpeedHz(int maxSpeedHz) throws IOException;

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
		default void mode(SpiMode mode) {}

		@Override
		default boolean lsbFirst() {
			return false;
		}

		@Override
		default void lsbFirst(boolean enabled) {}

		@Override
		default int bitsPerWord() {
			return 0;
		}

		@Override
		default void bitsPerWord(int bitsPerWord) {}

		@Override
		default int maxSpeedHz() {
			return 0;
		}

		@Override
		default void maxSpeedHz(int maxSpeedHz) {}

		@Override
		default SpiTransfer transfer(Direction direction, int size) {
			return SpiTransfer.of(t -> {}, direction, size);
		}
	}
}
