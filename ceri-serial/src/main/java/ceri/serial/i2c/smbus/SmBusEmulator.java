package ceri.serial.i2c.smbus;

import static ceri.common.math.MathUtil.ubyte;
import static ceri.common.math.MathUtil.ushort;
import static ceri.serial.i2c.util.I2cEmulator.ANY_READ_LEN;
import java.io.IOException;
import ceri.common.array.ArrayUtil;
import ceri.common.data.ByteArray.Encoder;
import ceri.common.data.ByteUtil;
import ceri.serial.i2c.I2cAddress;
import ceri.serial.i2c.util.I2cEmulator;

/**
 * Used with I2cEmulator for emulating SMBus slave device behavior.
 */
public class SmBusEmulator implements SmBus {
	private final I2cEmulator i2c;
	private final I2cAddress address;

	public static SmBusEmulator of(I2cEmulator i2c, I2cAddress address) {
		return new SmBusEmulator(i2c, address);
	}

	private SmBusEmulator(I2cEmulator i2c, I2cAddress address) {
		this.i2c = i2c;
		this.address = address;
	}

	@Override
	public void writeQuick(boolean on) throws IOException {
		if (on) read(ArrayUtil.bytes.empty, 0);
		else write(ArrayUtil.bytes.empty);
	}

	@Override
	public int readByte() throws IOException {
		return ubyte(read(ArrayUtil.bytes.empty, Byte.BYTES)[0]);
	}

	@Override
	public void writeByte(int value) throws IOException {
		write(ArrayUtil.bytes.of(value));
	}

	@Override
	public int readByteData(int command) throws IOException {
		return ubyte(read(ArrayUtil.bytes.of(command), Byte.BYTES)[0]);
	}

	@Override
	public void writeByteData(int command, int value) throws IOException {
		write(ArrayUtil.bytes.of(command, value));
	}

	@Override
	public int readWordData(int command) throws IOException {
		return ushort(
			ByteUtil.fromLsb(read(ArrayUtil.bytes.of(command), Short.BYTES), 0, Short.BYTES));
	}

	@Override
	public void writeWordData(int command, int value) throws IOException {
		write(Encoder.of().writeByte(command).writeShortLsb(value).bytes());
	}

	@Override
	public int processCall(int command, int value) throws IOException {
		return ushort(ByteUtil.fromLsb(
			read(Encoder.of().writeByte(command).writeShortLsb(value).bytes(), Short.BYTES), 0,
			Short.BYTES));
	}

	@Override
	public byte[] readBlockData(int command) throws IOException {
		return read(ArrayUtil.bytes.of(command), ANY_READ_LEN);
	}

	@Override
	public void writeBlockData(int command, byte[] values, int offset, int length)
		throws IOException {
		write(Encoder.of().writeByte(command).writeFrom(values, offset, length).bytes());
	}

	@Override
	public byte[] readI2cBlockData(int command, int length) throws IOException {
		return read(ArrayUtil.bytes.of(command), length);
	}

	@Override
	public void writeI2cBlockData(int command, byte[] values, int offset, int length)
		throws IOException {
		writeBlockData(command, values, offset, length);
	}

	@Override
	public byte[] blockProcessCall(int command, byte[] values, int offset, int length)
		throws IOException {
		return read(Encoder.of().writeByte(command).writeFrom(values, offset, length).bytes(),
			ANY_READ_LEN);
	}

	private void write(byte[] command) throws IOException {
		i2c.slaveWrite(address, command);
	}

	private byte[] read(byte[] command, int readLen) throws IOException {
		return i2c.slaveRead(address, command, readLen);
	}

}
