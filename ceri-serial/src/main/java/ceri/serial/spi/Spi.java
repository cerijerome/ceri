package ceri.serial.spi;

import java.io.IOException;
import ceri.common.io.Direction;

public interface Spi {
	/** A stateless, no-op instance. */
	Spi NULL = new Null() {
		@Override
		public String toString() {
			return Spi.class.getSimpleName() + ".NULL";
		}
	};

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
		default SpiMode mode() throws IOException {
			return SpiMode.MODE_0;
		}

		@Override
		default void mode(SpiMode mode) throws IOException {}

		@Override
		default boolean lsbFirst() throws IOException {
			return false;
		}

		@Override
		default void lsbFirst(boolean enabled) throws IOException {}

		@Override
		default int bitsPerWord() throws IOException {
			return 0;
		}

		@Override
		default void bitsPerWord(int bitsPerWord) throws IOException {}

		@Override
		default int maxSpeedHz() throws IOException {
			return 0;
		}

		@Override
		default void maxSpeedHz(int maxSpeedHz) throws IOException {}

		@Override
		default SpiTransfer transfer(Direction direction, int size) {
			return SpiTransfer.of(_ -> {}, direction, size);
		}
	}
}
