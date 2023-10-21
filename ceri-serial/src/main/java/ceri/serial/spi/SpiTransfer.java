package ceri.serial.spi;

import static ceri.common.collection.ArrayUtil.EMPTY_BYTE;
import static ceri.common.math.MathUtil.ubyte;
import static ceri.common.math.MathUtil.ushort;
import static ceri.common.validation.ValidationUtil.validateRange;
import static ceri.jna.util.JnaUtil.buffer;
import static com.sun.jna.Pointer.nativeValue;
import java.io.IOException;
import java.nio.ByteBuffer;
import com.sun.jna.Memory;
import ceri.common.function.ExceptionConsumer;
import ceri.common.io.Direction;
import ceri.jna.util.GcMemory;
import ceri.serial.spi.jna.SpiDev.spi_ioc_transfer;

/**
 * Wrapper for the spi_ioc_transfer data structure.
 */
public class SpiTransfer {
	private final ExceptionConsumer<IOException, spi_ioc_transfer> executor;
	private final int sizeMax;
	private final ByteBuffer out;
	private final ByteBuffer in;
	private final Direction direction;
	private final spi_ioc_transfer transfer;

	public static SpiTransfer of(ExceptionConsumer<IOException, spi_ioc_transfer> executor,
		Direction direction, int size) {
		var outMem = direction == Direction.in ? GcMemory.NULL : GcMemory.malloc(size);
		var inMem = direction == Direction.out ? GcMemory.NULL : GcMemory.malloc(size).clear();
		return new SpiTransfer(executor, outMem.m, inMem.m, direction, size);
	}

	private SpiTransfer(ExceptionConsumer<IOException, spi_ioc_transfer> executor, Memory outMem,
		Memory inMem, Direction direction, int size) {
		this.executor = executor;
		transfer = new spi_ioc_transfer();
		transfer.tx_buf = nativeValue(outMem);
		transfer.rx_buf = nativeValue(inMem);
		transfer.len = size;
		this.direction = direction;
		sizeMax = size;
		out = buffer(outMem);
		in = buffer(inMem);
	}

	public byte[] read() {
		if (in().capacity() == 0) return EMPTY_BYTE;
		byte[] buffer = new byte[size()];
		in().clear().get(buffer);
		return buffer;
	}

	public SpiTransfer execute() throws IOException {
		executor.accept(transfer);
		return this;
	}

	public SpiTransfer write(byte[] data) {
		if (out().capacity() == 0) return this;
		out().clear().put(data);
		return limit(data.length);
	}

	public ByteBuffer out() {
		return out;
	}

	public ByteBuffer in() {
		return in;
	}

	public Direction direction() {
		return direction;
	}

	public int sizeMax() {
		return sizeMax;
	}

	public int size() {
		return transfer.len;
	}

	public SpiTransfer limit(int size) {
		validateRange(size, 0, sizeMax);
		transfer.len = size;
		return this;
	}

	public int speedHz() {
		return transfer.speed_hz;
	}

	public SpiTransfer speedHz(int speedHz) {
		transfer.speed_hz = speedHz;
		return this;
	}

	public int delayMicros() {
		return ushort(transfer.delay_usecs);
	}

	public SpiTransfer delayMicros(int delayMicros) {
		transfer.delay_usecs = (short) delayMicros;
		return this;
	}

	public int bitsPerWord() {
		return ubyte(transfer.bits_per_word);
	}

	public SpiTransfer bitsPerWord(int bitsPerWord) {
		transfer.bits_per_word = (byte) bitsPerWord;
		return this;
	}

	public boolean csChange() {
		return transfer.cs_change != 0;
	}

	public SpiTransfer csChange(boolean enabled) {
		transfer.cs_change = (byte) (enabled ? 1 : 0);
		return this;
	}

	public int txNbits() {
		return ubyte(transfer.tx_nbits);
	}

	public SpiTransfer txNbits(int txNbits) {
		transfer.tx_nbits = (byte) txNbits;
		return this;
	}

	public int rxNbits() {
		return ubyte(transfer.rx_nbits);
	}

	public SpiTransfer rxNbits(int rxNbits) {
		transfer.rx_nbits = (byte) rxNbits;
		return this;
	}

}
