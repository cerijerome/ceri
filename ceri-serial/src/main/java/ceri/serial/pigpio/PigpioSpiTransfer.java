package ceri.serial.pigpio;

import static ceri.common.collection.ArrayUtil.EMPTY_BYTE;
import static ceri.serial.jna.JnaUtil.buffer;
import java.io.IOException;
import java.nio.ByteBuffer;
import com.sun.jna.Memory;

public class PigpioSpiTransfer {
	private final PigpioSpi spi;
	private final int size;
	private final ByteBuffer out;
	private final ByteBuffer in;

	public static PigpioSpiTransfer out(PigpioSpi spi, int size) {
		return new PigpioSpiTransfer(spi, new Memory(size), null, size);
	}

	public static PigpioSpiTransfer in(PigpioSpi spi, int size) {
		return new PigpioSpiTransfer(spi, null, new Memory(size), size);
	}

	public static PigpioSpiTransfer duplex(PigpioSpi spi, int size) {
		return new PigpioSpiTransfer(spi, new Memory(size), new Memory(size), size);
	}

	private PigpioSpiTransfer(PigpioSpi spi, Memory outMem, Memory inMem, int size) {
		this.spi = spi;
		this.size = size;
		out = outMem == null ? null : buffer(outMem);
		in = inMem == null ? null : buffer(inMem);
	}

	public byte[] read() {
		if (in == null) return EMPTY_BYTE;
		byte[] buffer = new byte[size];
		in.clear().get(buffer);
		return buffer;
	}

	public PigpioSpiTransfer write(byte[] data) {
		if (out == null) return this;
		out.clear().put(data);
		return this;
	}

	public ByteBuffer out() {
		return out;
	}

	public ByteBuffer in() {
		return in;
	}

	public int execute() throws IOException {
		if (in == null && out == null) return 0;
		if (out == null) return spi.read(in);
		if (in == null) return spi.write(out);
		return spi.xfer(out, in);
	}
}
