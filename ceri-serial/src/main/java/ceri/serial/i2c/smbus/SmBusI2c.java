package ceri.serial.i2c.smbus;

import java.io.IOException;
import java.util.Arrays;
import ceri.common.data.ByteArray;
import ceri.common.data.ByteProvider;
import ceri.common.data.ByteUtil;
import ceri.common.data.CrcAlgorithm;
import ceri.common.function.Excepts;
import ceri.common.function.Functions;
import ceri.common.math.Maths;
import ceri.common.util.Validate;
import ceri.jna.util.GcMemory;
import ceri.jna.util.JnaUtil;
import ceri.serial.i2c.I2cAddress;
import ceri.serial.i2c.jna.I2cDev;
import ceri.serial.i2c.util.I2cUtil;

/**
 * Provides SMBus functionality using I2C ioctl RDWR.
 */
public class SmBusI2c implements SmBus {
	private static final CrcAlgorithm CRC = CrcAlgorithm.Std.crc8Smbus.algorithm();
	private final Excepts.Consumer<IOException, I2cDev.i2c_msg.ByReference[]> transferFn;
	private final Functions.BoolSupplier smBusPecFn;
	private final I2cAddress address;

	public static SmBusI2c of(
		Excepts.Consumer<IOException, I2cDev.i2c_msg.ByReference[]> transferFn,
		Functions.BoolSupplier smBusPecFn, I2cAddress address) {
		return new SmBusI2c(transferFn, smBusPecFn, address);
	}

	private SmBusI2c(Excepts.Consumer<IOException, I2cDev.i2c_msg.ByReference[]> transferFn,
		Functions.BoolSupplier smBusPecFn, I2cAddress address) {
		this.transferFn = transferFn;
		this.smBusPecFn = smBusPecFn;
		this.address = address;
	}

	@Override
	public void writeQuick(boolean on) throws IOException {
		var msg = on ? I2cUtil.populate(null, address, null, I2cDev.i2c_msg_flag.I2C_M_RD) :
			I2cUtil.populate(null, address, null);
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
		byte[] send = { (byte) command, ByteUtil.byteAt(value, 0), ByteUtil.byteAt(value, 1), 0 };
		write(send);
	}

	@Override
	public int processCall(int command, int value) throws IOException {
		byte[] send = { (byte) command, ByteUtil.byteAt(value, 0), ByteUtil.byteAt(value, 1) };
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
		Validate.slice(values.length, offset, length);
		Validate.max(length, I2cDev.I2C_SMBUS_BLOCK_MAX, "Write block size");
		byte[] send = ByteArray.Encoder.of(1 + 1 + length + 1).writeBytes(command, length)
			.writeFrom(values, offset, length).bytes();
		write(send);
	}

	@Override
	public byte[] blockProcessCall(int command, byte[] values, int offset, int length)
		throws IOException {
		Validate.slice(values.length, offset, length);
		Validate.max(length, I2cDev.I2C_SMBUS_BLOCK_MAX, "Write block size");
		byte[] send = ByteArray.Encoder.of(1 + 1 + length).writeBytes(command, length)
			.writeFrom(values, offset, length).bytes();
		return writeReadBlock(send);
	}

	@Override
	public byte[] readI2cBlockData(int command, int length) throws IOException {
		Validate.range(length, 0, I2cDev.I2C_SMBUS_BLOCK_MAX, "Read block size");
		byte[] send = { (byte) command };
		return writeRead(send, length).copy(0);
	}

	@Override
	public void writeI2cBlockData(int command, byte[] values, int offset, int length)
		throws IOException {
		Validate.slice(values.length, offset, length);
		Validate.max(length, I2cDev.I2C_SMBUS_BLOCK_MAX, "Write block size");
		byte[] send = ByteArray.Encoder.of(1 + length + 1).writeByte(command)
			.writeFrom(values, offset, length).bytes();
		write(send);
	}

	/**
	 * Write command and read data. Receive length should not include PEC.
	 */
	private ByteProvider read(int recvLen) throws IOException {
		boolean pec = pec();
		var recvM = GcMemory.malloc(length(recvLen, pec)).clear().m;
		var msg = I2cUtil.populate(null, address, recvM, I2cDev.i2c_msg_flag.I2C_M_RD);
		transfer(msg);
		byte[] recv = JnaUtil.bytes(recvM);
		if (pec) CRC.start().add(crcAddr(true)).add(recv, 0, recv.length - 1)
			.verify(recv[recv.length - 1]);
		return pec ? ByteArray.Mutable.wrap(recv, 0, recv.length - 1) :
			ByteArray.Mutable.wrap(recv);
	}

	/**
	 * Write data. Data should include final byte for PEC even if PEC is disabled.
	 */
	private void write(byte[] send) throws IOException {
		boolean pec = pec();
		if (pec) send[send.length - 1] =
			CRC.start().add(crcAddr(false)).add(send, 0, send.length - 1).crcByte();
		var msg = I2cUtil.populate(null, address,
			GcMemory.mallocBytes(send, 0, length(send.length - 1, pec)).m);
		transfer(msg);
	}

	/**
	 * Write command and read data. Receive length should not include PEC.
	 */
	private ByteProvider writeRead(byte[] send, int recvLen) throws IOException {
		boolean pec = pec();
		var recvM = GcMemory.malloc(length(recvLen, pec)).clear().m;
		var msgs = I2cDev.i2c_msg.array(2);
		I2cUtil.populate(msgs[0], address, GcMemory.mallocBytes(send).m);
		I2cUtil.populate(msgs[1], address, recvM, I2cDev.i2c_msg_flag.I2C_M_RD);
		transfer(msgs);
		byte[] recv = JnaUtil.bytes(recvM);
		if (pec) CRC.start().add(crcAddr(false)).add(send).add(crcAddr(true))
			.add(recv, 0, recv.length - 1).verify(recv[recv.length - 1]);
		return pec ? ByteArray.Mutable.wrap(recv, 0, recv.length - 1) :
			ByteArray.Mutable.wrap(recv);
	}

	/**
	 * Write command and read data. Receive length is determined by first received byte.
	 */
	private byte[] writeReadBlock(byte[] send) throws IOException {
		boolean pec = pec();
		var recvM = GcMemory.malloc(length(1 + I2cDev.I2C_SMBUS_BLOCK_MAX, pec)).clear().m;
		var msgs = I2cDev.i2c_msg.array(2);
		I2cUtil.populate(msgs[0], address, GcMemory.mallocBytes(send).m);
		I2cUtil.populate(msgs[1], address, 1, recvM, I2cDev.i2c_msg_flag.I2C_M_RD,
			I2cDev.i2c_msg_flag.I2C_M_RECV_LEN);
		transfer(msgs);
		byte[] recv = JnaUtil.bytes(recvM);
		int recvLen = 1 + Math.min(Maths.ubyte(recv[0]), I2cDev.I2C_SMBUS_BLOCK_MAX);
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
		return smBusPecFn.getAsBool();
	}

	private void transfer(I2cDev.i2c_msg.ByReference... msgs) throws IOException {
		transferFn.accept(msgs);
	}
}
