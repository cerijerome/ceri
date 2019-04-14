package ceri.serial.spi.util;

import ceri.serial.spi.Spi;
import ceri.serial.spi.SpiMode;
import ceri.serial.spi.SpiTransfer;

public class NullSpi implements Spi {

	@Override
	public int bus() {
		return 0;
	}

	@Override
	public int chip() {
		return 0;
	}

	@Override
	public Direction direction() {
		return Direction.duplex;
	}

	@Override
	public SpiMode mode() {
		return SpiMode.MODE_0;
	}

	@Override
	public Spi mode(SpiMode mode) {
		return this;
	}

	@Override
	public boolean lsbFirst() {
		return false;
	}

	@Override
	public Spi lsbFirst(boolean enabled) {
		return this;
	}

	@Override
	public int bitsPerWord() {
		return 0;
	}

	@Override
	public Spi bitsPerWord(int bitsPerWord) {
		return this;
	}

	@Override
	public int maxSpeedHz() {
		return 0;
	}

	@Override
	public Spi maxSpeedHz(int maxSpeedHz) {
		return this;
	}

	@Override
	public Spi execute(SpiTransfer xfer) {
		return this;
	}

	@Override
	public void close() {}

}
