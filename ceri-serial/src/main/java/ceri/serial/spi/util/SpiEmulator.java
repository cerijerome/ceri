package ceri.serial.spi.util;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import ceri.common.concurrent.ConcurrentUtil;
import ceri.serial.spi.Spi;
import ceri.serial.spi.SpiMode;
import ceri.serial.spi.SpiTransfer;

public class SpiEmulator implements Spi {
	private final int bus;
	private final int chip;
	private final Direction direction;
	private final Responder responder;
	private SpiMode mode = SpiMode.MODE_0;
	private boolean lsbFirst = false;
	private int bitsPerWord = 0; // 0 => 8 bits
	private int maxSpeedHz = 250000000 / 65536; // Raspberry Pi default?

	public static interface Responder {
		Responder ECHO = new Responder() {};

		@SuppressWarnings("unused")
		default void out(byte[] data) throws IOException {}

		@SuppressWarnings("unused")
		default byte[] in(int size) throws IOException {
			return new byte[size];
		}

		@SuppressWarnings("unused")
		default byte[] duplex(byte[] data) throws IOException {
			return data;
		}
	}

	public static SpiEmulator echo(int bus, int chip, Direction direction) {
		return of(bus, chip, direction, Responder.ECHO);
	}

	public static SpiEmulator of(int bus, int chip, Direction direction, Responder responder) {
		return new SpiEmulator(bus, chip, direction, responder);
	}

	private SpiEmulator(int bus, int chip, Direction direction, Responder responder) {
		this.bus = bus;
		this.chip = chip;
		this.direction = direction;
		this.responder = responder;
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
	public SpiMode mode() {
		return mode;
	}

	@Override
	public Spi mode(SpiMode mode) {
		this.mode = mode;
		return this;
	}

	@Override
	public boolean lsbFirst() {
		return lsbFirst;
	}

	@Override
	public Spi lsbFirst(boolean enabled) {
		lsbFirst = enabled;
		return this;
	}

	@Override
	public int bitsPerWord() {
		return bitsPerWord;
	}

	@Override
	public Spi bitsPerWord(int bitsPerWord) {
		this.bitsPerWord = bitsPerWord;
		return this;
	}

	@Override
	public int maxSpeedHz() {
		return maxSpeedHz;
	}

	@Override
	public Spi maxSpeedHz(int maxSpeedHz) {
		this.maxSpeedHz = maxSpeedHz;
		return this;
	}

	@Override
	public void close() {}

	@Override
	public Spi execute(SpiTransfer xfer) throws IOException {
		switch (xfer.direction()) {
		case out:
			responder.out(read(xfer));
			break;
		case in:
			write(xfer, responder.in(xfer.size()));
			break;
		case duplex:
			write(xfer, responder.duplex(read(xfer)));
			break;
		}
		ConcurrentUtil.delayMicros(transferTimeMicros(xfer));
		return this;
	}

	private byte[] read(SpiTransfer xfer) {
		byte[] buffer = new byte[xfer.size()];
		xfer.out().position(0).limit(xfer.size()).get(buffer);
		return buffer;
	}

	private void write(SpiTransfer xfer, byte[] data) {
		if (xfer.in().capacity() == 0) return;
		xfer.in().clear().put(data);
	}

	private long transferTimeMicros(SpiTransfer xfer) throws IOException {
		int speedHz = speedHz(xfer);
		if (speedHz == 0) return xfer.delayMicros();
		return (xfer.size() * Byte.SIZE * TimeUnit.SECONDS.toMicros(1) / speedHz) +
			xfer.delayMicros();
	}

}
