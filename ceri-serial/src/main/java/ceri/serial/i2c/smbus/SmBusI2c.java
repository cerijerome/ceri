package ceri.serial.i2c.smbus;

import static ceri.common.data.ByteUtil.byteAt;
import static ceri.common.data.CrcAlgorithm.Std.crc8Smbus;
import static ceri.common.math.MathUtil.ubyte;
import static ceri.common.validation.ValidationUtil.validateMax;
import static ceri.common.validation.ValidationUtil.validateRange;
import static ceri.serial.i2c.jna.I2cDev.I2C_SMBUS_BLOCK_MAX;
import static ceri.serial.i2c.jna.I2cDev.i2c_msg_flag.I2C_M_RD;
import static ceri.serial.i2c.jna.I2cDev.i2c_msg_flag.I2C_M_RECV_LEN;
import static ceri.serial.i2c.util.I2cUtil.populate;
import java.io.IOException;
import java.util.Arrays;
import java.util.function.BooleanSupplier;
import com.sun.jna.Memory;
import ceri.common.collection.ArrayUtil;
import ceri.common.data.ByteArray;
import ceri.common.data.ByteArray.Mutable;
import ceri.common.data.ByteProvider;
import ceri.common.data.CrcAlgorithm;
import ceri.common.function.ExceptionConsumer;
import ceri.serial.clib.jna.CUtil;
import ceri.serial.i2c.I2cAddress;
import ceri.serial.i2c.jna.I2cDev.i2c_msg;
import ceri.serial.jna.JnaUtil;

/**
 * Provides SMBus functionality using I2C ioctl RDWR.
 */
public class SmBusI2c implements SmBus {
	private static final CrcAlgorithm CRC = crc8Smbus.algorithm();
	private final ExceptionConsumer<IOException, i2c_msg.ByReference[]> transferFn;
	private final BooleanSupplier smBusPecFn;
	private final I2cAddress address;

	public static SmBusI2c of(ExceptionConsumer<IOException, i2c_msg.ByReference[]> transferFn,
		BooleanSupplier smBusPecFn, I2cAddress address) {
		return new SmBusI2c(transferFn, smBusPecFn, address);
	}

	private SmBusI2c(ExceptionConsumer<IOException, i2c_msg.ByReference[]> transferFn,
		BooleanSupplier smBusPecFn, I2cAddress address) {
		this.transferFn = transferFn;
		this.smBusPecFn = smBusPecFn;
		this.address = address;
	}

	@Override
	public void writeQuick(boolean on) throws IOException {
		i2c_msg.ByReference msg =
			on ? populate(null, address, null, I2C_M_RD) : populate(null, address, null);
		transfer(msg);
	}

	@Override
	public int readByte() throws IOException {
		return read(Byte.BYTES).getUbyte(0);
	}

	@Override
	public void writeByte(int value) throws IOException {
		byte[] send = { (byte) value, 0 };
		write(send);
	}

	@Override
	public int readByteData(int command) throws IOException {
		byte[] send = { (byte) command };
		return writeRead(send, Byte.BYTES).getUbyte(0);
	}

	@Override
	public void writeByteData(int command, int value) throws IOException {
		byte[] send = { (byte) command, (byte) value, 0 };
		write(send);
	}

	@Override
	public int readWordData(int command) throws IOException {
		byte[] send = { (byte) command };
		return writeRead(send, Short.BYTES).getUshortLsb(0);
	}

	@Override
	public void writeWordData(int command, int value) throws IOException {
		byte[] send = { (byte) command, byteAt(value, 0), byteAt(value, 1), 0 };
		write(send);
	}

	@Override
	public int processCall(int command, int value) throws IOException {
		byte[] send = { (byte) command, byteAt(value, 0), byteAt(value, 1) };
		return writeRead(send, Short.BYTES).getUshortLsb(0);
	}

	@Override
	public byte[] readBlockData(int command) throws IOException {
		byte[] send = { (byte) command };
		return writeReadBlock(send);
	}

	@Override
	public void writeBlockData(int command, byte[] values, int offset, int length)
		throws IOException {
		ArrayUtil.validateSlice(values.length, offset, length);
		validateMax(length, I2C_SMBUS_BLOCK_MAX, "Write block size");
		byte[] send = ByteArray.encoder(1 + 1 + length + 1).writeBytes(command, length)
			.writeFrom(values, offset, length).bytes();
		write(send);
	}

	@Override
	public byte[] blockProcessCall(int command, byte[] values, int offset, int length)
		throws IOException {
		ArrayUtil.validateSlice(values.length, offset, length);
		validateMax(length, I2C_SMBUS_BLOCK_MAX, "Write block size");
		byte[] send = ByteArray.encoder(1 + 1 + length).writeBytes(command, length)
			.writeFrom(values, offset, length).bytes();
		return writeReadBlock(send);
	}

	@Override
	public byte[] readI2cBlockData(int command, int length) throws IOException {
		validateRange(length, 0, I2C_SMBUS_BLOCK_MAX, "Read block size");
		byte[] send = { (byte) command };
		return writeRead(send, length).copy(0);
	}

	@Override
	public void writeI2cBlockData(int command, byte[] values, int offset, int length)
		throws IOException {
		ArrayUtil.validateSlice(values.length, offset, length);
		validateMax(length, I2C_SMBUS_BLOCK_MAX, "Write block size");
		byte[] send = ByteArray.encoder(1 + length + 1).writeByte(command)
			.writeFrom(values, offset, length).bytes();
		write(send);
	}

	/**
	 * Write command and read data. Receive length should not include PEC.
	 */
	private ByteProvider read(int recvLen) throws IOException {
		boolean pec = pec();
		Memory recvM = new Memory(length(recvLen, pec));
		i2c_msg.ByReference msg = populate(null, address, recvM, I2C_M_RD);
		transfer(msg);
		byte[] recv = JnaUtil.byteArray(recvM);
		if (pec) CRC.start().add(crcAddr(true)).add(recv, 0, recv.length - 1)
			.verify(recv[recv.length - 1]);
		return pec ? Mutable.wrap(recv, 0, recv.length - 1) : Mutable.wrap(recv);
	}

	/**
	 * Write data. Data should include final byte for PEC even if PEC is disabled.
	 */
	private void write(byte[] send) throws IOException {
		boolean pec = pec();
		if (pec) send[send.length - 1] =
			CRC.start().add(crcAddr(false)).add(send, 0, send.length - 1).crcByte();
		i2c_msg.ByReference msg =
			populate(null, address, CUtil.malloc(send, 0, length(send.length - 1, pec)));
		transfer(msg);
	}

	/**
	 * Write command and read data. Receive length should not include PEC.
	 */
	private ByteProvider writeRead(byte[] send, int recvLen) throws IOException {
		boolean pec = pec();
		Memory recvM = new Memory(length(recvLen, pec));
		i2c_msg.ByReference[] msgs = i2c_msg.array(2);
		populate(msgs[0], address, CUtil.malloc(send));
		populate(msgs[0], address, recvM, I2C_M_RD);
		transfer(msgs);
		byte[] recv = JnaUtil.byteArray(recvM);
		if (pec) CRC.start().add(crcAddr(false)).add(send).add(crcAddr(true))
			.add(recv, 0, recv.length - 1).verify(recv[recv.length - 1]);
		return pec ? Mutable.wrap(recv, 0, recv.length - 1) : Mutable.wrap(recv);
	}

	/**
	 * Write command and read data. Receive length is determined by first received byte.
	 */
	private byte[] writeReadBlock(byte[] send) throws IOException {
		boolean pec = pec();
		Memory recvM = new Memory(length(1 + I2C_SMBUS_BLOCK_MAX, pec));
		i2c_msg.ByReference[] msgs = i2c_msg.array(2);
		populate(msgs[0], address, CUtil.malloc(send));
		populate(msgs[0], address, 1, recvM, I2C_M_RD, I2C_M_RECV_LEN);
		transfer(msgs);
		byte[] recv = JnaUtil.byteArray(recvM);
		int recvLen = 1 + Math.min(ubyte(recv[0]), I2C_SMBUS_BLOCK_MAX);
		if (pec) CRC.start().add(crcAddr(false)).add(send).add(crcAddr(true)).add(recv, 0, recvLen)
			.verify(recv[recvLen]);
		return Arrays.copyOfRange(recv, 1, recvLen);
	}

	private byte crcAddr(boolean read) {
		return address.frames(read)[0];
	}

	private int length(int length, boolean pec) {
		return pec ? length + 1 : length;
	}

	private boolean pec() {
		return smBusPecFn.getAsBoolean();
	}

	private void transfer(i2c_msg.ByReference... msgs) throws IOException {
		transferFn.accept(msgs);
	}

}
