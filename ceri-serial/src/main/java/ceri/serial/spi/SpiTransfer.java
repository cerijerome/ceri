package ceri.serial.spi;

import static ceri.common.collection.ArrayUtil.EMPTY_BYTE;
import static ceri.common.validation.ValidationUtil.validateRange;
import static ceri.serial.jna.JnaUtil.buffer;
import static ceri.serial.jna.JnaUtil.ubyte;
import static ceri.serial.jna.JnaUtil.ushort;
import static com.sun.jna.Pointer.nativeValue;
import java.io.IOException;
import java.nio.ByteBuffer;
import com.sun.jna.Memory;
import ceri.serial.spi.Spi.Direction;
import ceri.serial.spi.jna.SpiDev.spi_ioc_transfer;

/**
 * Wrapper for the spi_ioc_transfer data structure. Order of operations:
 * <ol>
 * <li>Create with constructor or Spi.transfer() to use the direction</li>
 * <li></li>
 * <li></li>
 * </ol>
 * 
 */
public class SpiTransfer {
	private final Spi spi;
	private final int sizeMax;
	private final ByteBuffer out;
	private final ByteBuffer in;
	private final Direction direction;
	private final spi_ioc_transfer transfer;

	static SpiTransfer of(Spi spi, int size) {
		Memory outMem = spi.direction() == Direction.in ? null : new Memory(size);
		Memory inMem = spi.direction() == Direction.out ? null : new Memory(size);
		return new SpiTransfer(spi, outMem, inMem, size);
	}

	private SpiTransfer(Spi spi, Memory outMem, Memory inMem, int size) {
		this.spi = spi;
		direction = spi.direction();
		transfer = new spi_ioc_transfer();
		transfer.tx_buf = nativeValue(outMem);
		transfer.rx_buf = nativeValue(inMem);
		transfer.len = size;
		sizeMax = size;
		out = buffer(outMem);
		in = buffer(inMem);
	}

	spi_ioc_transfer transfer() {
		return transfer;
	}

	public byte[] read() {
		if (in.capacity() == 0) return EMPTY_BYTE;
		byte[] buffer = new byte[size()];
		in.clear().get(buffer);
		return buffer;
	}

	public SpiTransfer execute() throws IOException {
		spi.execute(this);
		return this;
	}
	
	public SpiTransfer write(byte[] data) {
		if (out.capacity() == 0) return this;
		out.clear().put(data);
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

	public int csChange() {
		return ubyte(transfer.cs_change);
	}

	public SpiTransfer csChange(int csChange) {
		transfer.cs_change = (byte) csChange;
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
