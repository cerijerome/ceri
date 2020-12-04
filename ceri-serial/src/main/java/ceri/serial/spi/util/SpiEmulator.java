package ceri.serial.spi.util;

import static ceri.common.validation.ValidationUtil.validateMin;
import static ceri.common.validation.ValidationUtil.validateNotNull;
import static ceri.serial.spi.jna.SpiDevUtil.direction;
import static ceri.serial.spi.jna.SpiDevUtil.transferTimeMicros;
import java.io.IOException;
import java.nio.ByteBuffer;
import ceri.common.concurrent.ConcurrentUtil;
import ceri.common.data.ByteUtil;
import ceri.serial.jna.JnaUtil;
import ceri.serial.spi.Spi;
import ceri.serial.spi.SpiMode;
import ceri.serial.spi.SpiTransfer;
import ceri.serial.spi.jna.SpiDev.spi_ioc_transfer;

public class SpiEmulator implements Spi {
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

	public static SpiEmulator echo() {
		return of(Responder.ECHO);
	}

	public static SpiEmulator of(Responder responder) {
		return new SpiEmulator(responder);
	}

	private SpiEmulator(Responder responder) {
		this.responder = responder;
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
	public SpiTransfer transfer(Direction direction, int size) {
		validateNotNull(direction, "Direction");
		validateMin(size, 0, "Size");
		return SpiTransfer.of(this::execute, direction, size);
	}

	private void execute(spi_ioc_transfer xfer) throws IOException {
		ByteBuffer out = JnaUtil.buffer(JnaUtil.pointer(xfer.tx_buf), 0, xfer.len);
		ByteBuffer in = JnaUtil.buffer(JnaUtil.pointer(xfer.rx_buf), 0, xfer.len);
		switch (direction(xfer)) {
		case out:
			responder.out(read(out));
			break;
		case in:
			write(in, responder.in(xfer.size()));
			break;
		default:
			write(in, responder.duplex(read(out)));
			break;
		}
		ConcurrentUtil.delayMicros(transferTimeMicros(xfer));
	}

	private byte[] read(ByteBuffer in) {
		return ByteUtil.readFrom(in, 0, in.capacity());
	}

	private void write(ByteBuffer out, byte[] data) {
		ByteUtil.writeTo(out, 0, data, 0, Math.min(data.length, out.capacity()));
	}

}
