package ceri.serial.i2c;

import static ceri.common.data.ByteUtil.byteAt;
import static ceri.common.data.CrcAlgorithm.Std.crc8Smbus;
import static ceri.common.math.MathUtil.byteExact;
import static ceri.common.validation.ValidationUtil.validateMax;
import static ceri.common.validation.ValidationUtil.validateRange;
import static ceri.serial.i2c.jna.I2cDev.i2c_msg_flag.I2C_M_RD;
import static ceri.serial.i2c.jna.I2cDev.i2c_msg_flag.I2C_M_RECV_LEN;
import static ceri.serial.i2c.jna.I2cDev.i2c_msg_flag.I2C_M_TEN;
import static ceri.serial.jna.JnaUtil.byteRefPtr;
import static ceri.serial.jna.JnaUtil.execUbyte;
import static ceri.serial.jna.JnaUtil.execUshort;
import static ceri.serial.jna.JnaUtil.size;
import java.util.Arrays;
import com.sun.jna.Memory;
import com.sun.jna.Pointer;
import ceri.common.data.ByteUtil;
import ceri.common.data.Crc;
import ceri.common.data.CrcAlgorithm;
import ceri.common.data.DataEncoder;
import ceri.common.math.MathUtil;
import ceri.serial.clib.jna.CException;
import ceri.serial.clib.jna.CUtil;
import ceri.serial.i2c.jna.I2cDev;
import ceri.serial.i2c.jna.I2cDev.i2c_msg;
import ceri.serial.i2c.jna.I2cDev.i2c_msg_flag;
import ceri.serial.jna.JnaUtil;
import ceri.serial.jna.MemoryWriter;

/**
 * Provides SMBus functionality using ioctl RdWr. PEC not currently supported.
 */
class SmBusEmulated implements SmBus {
	private static final CrcAlgorithm CRC = crc8Smbus.algorithm();
	private static final int BLOCK_DATA_MAX = 255;
	private final I2cAddress address;
	private final I2c i2c;

	public static void main(String[] args) {
		I2cAddress a = I2cAddress.of(0x33);
		byte ab = a.addressByte(false);
		byte[] b = { 0, 1, 2, 3, 4, 0 };
		int len = b.length - 1;
		boolean pec = true;
		if (pec) b[len] = CRC.start().add(ab).add(b, 0, len++).crcByte();
		b = Arrays.copyOf(b, len);
		System.out.printf("0x%x 0b%s%n", ab, Integer.toBinaryString(ab));
		System.out.println(ByteUtil.toHex(b, ", "));
		System.out.printf("0x%x", CRC.start().add(ab).add(0, 1, 2, 3, 4).crcByte());
	}
	
	SmBusEmulated(I2c i2c, I2cAddress address) {
		this.i2c = i2c;
		this.address = address;
	}

	@Override
	public void writeQuick(boolean on) throws CException {
		if (on) exec(address, 0, null, I2C_M_RD);
		else exec(address, 0, null);
	}

	@Override
	public int readByte() throws CException {
		int len = 1;
		Memory m = new Memory(len + (pec() ? 1 : 0)); // include PEC
		exec(address, size(m), m, I2C_M_RD);
		byte[] read = JnaUtil.byteArray(m);
		if (pec()) CRC.start().add(crcAddr(true)).add(read, 0, len).verify(read[len]);
		return MathUtil.ubyte(read[0]);
	}

	@Override
	public void writeByte(int value) throws CException {
		int len = 1;
		Memory m = new Memory();
		byte[] data = new byte[len + 1]; // include PEC
		DataEncoder.of(data).encodeByte(value);
		if (pec()) data[len] = CRC.start().add(crcAddr(false)).add(data, 0, len++).crcByte();
		Memory m = CUtil.malloc(data, 0, len);
		exec(address, size(m), m);
	}

	@Override
	public int readByteData(int command) throws CException {
		Crc crc = pec() ? CRC.start().add(crcAddr(false)).add(command) : null;
		int len = 1;
		Memory m = new Memory(len + (pec() ? 1 : 0));
		writeRead(address, Byte.BYTES, byteRefPtr(command), size(m), m);
		byte[] read = JnaUtil.byteArray(m);
		if (pec()) CRC.start().add(crcAddr(true)).add(read, 0, len).verify(read[len]);
		return JnaUtil.ubyte(read[0]);
		
		return execUbyte(p -> writeRead(address, Byte.BYTES, byteRefPtr(command), Byte.BYTES, p));
	}

	@Override
	public void writeByteData(int command, int value) throws CException {
		int len = 1 + 1;
		byte[] data = new byte[len + 1]; // include PEC
		DataEncoder.of(data).encodeByte(command).encodeByte(value);
		if (pec()) data[len] = CRC.start().add(crcAddr(false)).add(data, 0, len++).crcByte();
		Memory m = CUtil.malloc(data, 0, len);
		exec(address, size(m), m);
	}

	@Override
	public int readWordData(int command) throws CException {
		return execUshort(p -> writeRead(address, Byte.BYTES, byteRefPtr(command), Short.BYTES, p));
	}

	@Override
	public void writeWordData(int command, int value) throws CException {
		int len = 1 + 2;
		byte[] data = new byte[len + 1]; // include PEC
		DataEncoder.of(data).encodeByte(command).encodeShortLsb(value);
		if (pec()) data[len] = CRC.start().add(crcAddr(false)).add(data, 0, len++).crcByte();
		Memory m = CUtil.malloc(data, 0, len);
		exec(address, size(m), m);
	}

	@Override
	public int processCall(int command, int value) throws CException {
		Memory m = CUtil.malloc(command, byteAt(value, 0), byteAt(value, 1));
		return execUshort(p -> writeRead(address, size(m), m, Short.BYTES, p));
	}

	@Override
	public byte[] readBlockData(int command) throws CException {
		Memory m = new Memory(1 + BLOCK_DATA_MAX);
		writeRead(address, Byte.BYTES, byteRefPtr(command), size(m), m, I2C_M_RECV_LEN);
		return copyFrom(m);
	}

	@Override
	public void writeBlockData(int command, byte... values) throws CException {
		validateMax(values.length, BLOCK_DATA_MAX, "Write block size");
		int len = 1 + 1 + values.length;
		byte[] data = new byte[len + 1]; // include PEC
		DataEncoder.of(data).encodeByte(command).encodeByte(len).copy(values);
		if (pec()) data[len] = CRC.start().add(crcAddr(false)).add(data, 0, len++).crcByte();
		Memory m = CUtil.malloc(data, 0, len);
		exec(address, size(m), m);
	}

	@Override
	public byte[] blockProcessCall(int command, byte... values) throws CException {
		validateMax(values.length, BLOCK_DATA_MAX, "Write block size");
		Memory mw = new Memory(1 + 1 + values.length);
		MemoryWriter.of(mw).write(command, values.length).write(values);
		Memory mr = new Memory(1 + BLOCK_DATA_MAX);
		writeRead(address, size(mw), mw, size(mr), mr, I2C_M_RECV_LEN);
		return copyFrom(mr);
	}

	@Override
	public byte[] readI2cBlockData(int command, int length) throws CException {
		validateRange(length, 0, BLOCK_DATA_MAX, "Read block size");
		Memory m = new Memory(BLOCK_DATA_MAX);
		writeRead(address, Byte.BYTES, byteRefPtr(command), size(m), m);
		return JnaUtil.byteArray(m, 0, length);
	}

	@Override
	public void writeI2cBlockData(int command, byte... values) throws CException {
		validateMax(values.length, BLOCK_DATA_MAX, "Write block size");
		int len = 1 + values.length;
		byte[] data = new byte[len + 1]; // include PEC
		DataEncoder.of(data).encodeByte(command).copy(values);
		if (pec()) data[len] = CRC.start().add(crcAddr(false)).add(data, 0, len++).crcByte();
		Memory m = CUtil.malloc(data, 0, len);
		exec(address, size(m), m);
	}

	private byte[] copyFrom(Memory m) throws CException {
		int size = JnaUtil.ubyte(m);
		if (size >= size(m))
			throw CException.general("Read block size exceeded: %d > %d", size, size(m) - 1);
		return JnaUtil.byteArray(m, 1, size);
	}

	private void exec(I2cAddress address, int writeLen, Pointer writeBuf, i2c_msg_flag... flags)
		throws CException {
		i2c_msg.ByReference msg = new i2c_msg.ByReference();
		populate(msg, address, writeLen, writeBuf, flags);
		I2cDev.i2c_rdwr(fd(), msg);
	}

	private void writeRead(I2cAddress address, int writeLen, Pointer writeBuf, int readLen,
		Pointer readBuf, i2c_msg_flag... flags) throws CException {
		i2c_msg.ByReference[] msgs = i2c_msg.array(2);
		populate(msgs[0], address, writeLen, writeBuf);
		populate(msgs[1], address, readLen, readBuf);
		msgs[1].flags().set(I2C_M_RD).add(flags);
		I2cDev.i2c_rdwr(fd(), msgs);
	}

	private static i2c_msg.ByReference populate(i2c_msg.ByReference msg, I2cAddress address,
		int size, Pointer p, i2c_msg_flag... flags) {
		msg.addr = address.value();
		msg.len = byteExact(size);
		msg.buf = p;
		if (address.tenBit) msg.flags().add(flags).add(I2C_M_TEN);
		else if (flags.length > 0) msg.flags().add(flags);
		return msg;
	}

//	private Memory mallocForWrite(int...data) {
//		boolean pec = i2c.smBusPec();
//		byte[] array = new byte[data.length + (pec ? 1: 0)];
//		for (int i = 0; i < data.length; i++) array[i] = (byte) data[i];
//		if (pec) array[data.length] = 
//		Memory m = new Memory(data.length + (pec ? 1: 0));
//		
//		Memory m = pec ? JnaUtil.malloc(value, CRC.start().add(value).crcByte()) : JnaUtil.malloc(value);
//	}
	
	private byte crcAddr(boolean read) {
		return address.addressByte(read);
	}
	
	private boolean pec() {
		return i2c.smBusPec();
	}
	
	private int fd() {
		return i2c.fd().fd();
	}

}
