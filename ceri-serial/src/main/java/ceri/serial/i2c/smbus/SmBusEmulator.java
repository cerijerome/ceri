package ceri.serial.i2c.smbus;

import java.io.IOException;
import ceri.common.array.Array;
import ceri.common.data.ByteArray;
import ceri.common.data.Bytes;
import ceri.common.math.Maths;
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
		if (on) read(Array.BYTE.empty, 0);
		else write(Array.BYTE.empty);
	}

	@Override
	public int readByte() throws IOException {
		return Maths.ubyte(read(Array.BYTE.empty, Byte.BYTES)[0]);
	}

	@Override
	public void writeByte(int value) throws IOException {
		write(Array.BYTE.of(value));
	}

	@Override
	public int readByteData(int command) throws IOException {
		return Maths.ubyte(read(Array.BYTE.of(command), Byte.BYTES)[0]);
	}

	@Override
	public void writeByteData(int command, int value) throws IOException {
		write(Array.BYTE.of(command, value));
	}

	@Override
	public int readWordData(int command) throws IOException {
		return Maths.ushort(
			Bytes.fromLsb(read(Array.BYTE.of(command), Short.BYTES), 0, Short.BYTES));
	}

	@Override
	public void writeWordData(int command, int value) throws IOException {
		write(ByteArray.Encoder.of().writeByte(command).writeShortLsb(value).bytes());
	}

	@Override
	public int processCall(int command, int value) throws IOException {
		return Maths.ushort(Bytes.fromLsb(
			read(ByteArray.Encoder.of().writeByte(command).writeShortLsb(value).bytes(), Short.BYTES), 0,
			Short.BYTES));
	}

	@Override
	public byte[] readBlockData(int command) throws IOException {
		return read(Array.BYTE.of(command), I2cEmulator.ANY_READ_LEN);
	}

	@Override
	public void writeBlockData(int command, byte[] values, int offset, int length)
		throws IOException {
		write(ByteArray.Encoder.of().writeByte(command).writeFrom(values, offset, length).bytes());
	}

	@Override
	public byte[] readI2cBlockData(int command, int length) throws IOException {
		return read(Array.BYTE.of(command), length);
	}

	@Override
	public void writeI2cBlockData(int command, byte[] values, int offset, int length)
		throws IOException {
		writeBlockData(command, values, offset, length);
	}

	@Override
	public byte[] blockProcessCall(int command, byte[] values, int offset, int length)
		throws IOException {
		return read(ByteArray.Encoder.of().writeByte(command).writeFrom(values, offset, length).bytes(),
			I2cEmulator.ANY_READ_LEN);
	}

	private void write(byte[] command) throws IOException {
		i2c.slaveWrite(address, command);
	}

	private byte[] read(byte[] command, int readLen) throws IOException {
		return i2c.slaveRead(address, command, readLen);
	}
}
