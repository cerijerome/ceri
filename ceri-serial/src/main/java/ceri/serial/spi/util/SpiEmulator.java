package ceri.serial.spi.util;

import static ceri.serial.spi.jna.SpiDevUtil.direction;
import static ceri.serial.spi.jna.SpiDevUtil.transferTimeMicros;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.ByteBuffer;
import ceri.common.concurrent.Concurrent;
import ceri.common.data.ByteUtil;
import ceri.common.io.Direction;
import ceri.common.test.PulsePrinter;
import ceri.common.util.Basics;
import ceri.common.util.Validate;
import ceri.jna.util.JnaUtil;
import ceri.jna.util.PointerUtil;
import ceri.serial.spi.Spi;
import ceri.serial.spi.SpiMode;
import ceri.serial.spi.SpiTransfer;
import ceri.serial.spi.jna.SpiDev.spi_ioc_transfer;
import ceri.serial.spi.pulse.PulseCycle;

public class SpiEmulator implements Spi {
	private final Responder responder;
	private SpiMode mode = SpiMode.MODE_0;
	private boolean lsbFirst = false;
	private int bitsPerWord = 0; // 0 => 8 bits
	private int maxSpeedHz = 250_000_000 / 2; // Raspberry Pi 4 default?
	private boolean delay = true;

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

	/**
	 * Prints output data as pulses.
	 */
	public static SpiEmulator pulsePrinter(PrintStream out) {
		return pulsePrinter(out, null);
	}

	/**
	 * Prints output data as pulses with byte separator according to cycle.
	 */
	public static SpiEmulator pulsePrinter(PrintStream out, PulseCycle cycle) {
		var pp = PulsePrinter.builder().out(out).build();
		return of(new Responder() {
			@Override
			public void out(byte[] data) throws IOException {
				for (int i = 0; i < data.length; i++) {
					if (cycle != null && i > 0 && i % cycle.pulseBits == cycle.pulseOffsetBits)
						out.print(' ');
					pp.print(data[i]);
				}
				pp.newLine();
			}
		});
	}

	/**
	 * Echoes duplex data.
	 */
	public static SpiEmulator echo() {
		return of(Responder.ECHO);
	}

	public static SpiEmulator of(Responder responder) {
		return new SpiEmulator(responder);
	}

	protected SpiEmulator(Responder responder) {
		this.responder = responder;
	}

	public SpiEmulator delay(boolean delay) {
		this.delay = delay;
		return this;
	}

	@Override
	public SpiMode mode() throws IOException {
		return mode;
	}

	@Override
	public void mode(SpiMode mode) throws IOException {
		this.mode = mode;
	}

	@Override
	public boolean lsbFirst() throws IOException {
		return lsbFirst;
	}

	@Override
	public void lsbFirst(boolean enabled) throws IOException {
		lsbFirst = enabled;
	}

	@Override
	public int bitsPerWord() throws IOException {
		return bitsPerWord;
	}

	@Override
	public void bitsPerWord(int bitsPerWord) throws IOException {
		this.bitsPerWord = bitsPerWord;
	}

	@Override
	public int maxSpeedHz() throws IOException {
		return maxSpeedHz;
	}

	@Override
	public void maxSpeedHz(int maxSpeedHz) throws IOException {
		this.maxSpeedHz = maxSpeedHz;
	}

	@Override
	public SpiTransfer transfer(Direction direction, int size) {
		Basics.requireNot(direction, null, Direction.none);
		Validate.min(size, 0, "Size");
		return SpiTransfer.of(this::execute, direction, size);
	}

	private void execute(spi_ioc_transfer xfer) throws IOException {
		var out = buffer(xfer.tx_buf, xfer.len);
		var in = buffer(xfer.rx_buf, xfer.len);
		switch (direction(xfer)) {
			case out -> responder.out(read(out));
			case in -> write(in, responder.in(xfer.size()));
			default -> write(in, responder.duplex(read(out)));
		}
		Concurrent.delayMicros(delay ? transferTimeMicros(xfer) : 0L);
	}

	private ByteBuffer buffer(long peer, int len) {
		if (peer == 0L) return ByteBuffer.allocate(0);
		return JnaUtil.buffer(PointerUtil.pointer(peer), 0, len);
	}

	private byte[] read(ByteBuffer in) {
		return ByteUtil.readFrom(in, 0, in.capacity());
	}

	private void write(ByteBuffer out, byte[] data) {
		ByteUtil.writeTo(out, 0, data, 0, Math.min(data.length, out.capacity()));
	}
}
