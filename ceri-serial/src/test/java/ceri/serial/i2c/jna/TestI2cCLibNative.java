package ceri.serial.i2c.jna;

import static ceri.common.math.Maths.ubyte;
import static ceri.common.math.Maths.ushort;
import static ceri.serial.i2c.jna.I2cDev.i2c_msg_flag.I2C_M_RD;
import java.util.List;
import java.util.stream.Stream;
import com.sun.jna.LastErrorException;
import com.sun.jna.Pointer;
import ceri.common.data.ByteArray;
import ceri.common.data.ByteProvider;
import ceri.common.test.CallSync;
import ceri.jna.clib.jna.CLib;
import ceri.jna.clib.test.TestCLibNative;
import ceri.jna.type.CUlong;
import ceri.jna.util.JnaLibrary;
import ceri.jna.util.JnaUtil;
import ceri.serial.i2c.jna.I2cDev.i2c_msg;
import ceri.serial.i2c.jna.I2cDev.i2c_rdwr_ioctl_data;
import ceri.serial.i2c.jna.I2cDev.i2c_smbus_ioctl_data;

public class TestI2cCLibNative extends TestCLibNative {
	public final CallSync.Function<Int, Integer> ioctlI2cInt = CallSync.function(null, 0);
	public final CallSync.Function<List<Bytes>, ByteProvider> ioctlI2cBytes =
		CallSync.function(null, ByteProvider.empty());
	public final CallSync.Function<Rw, Integer> ioctlSmBusInt = CallSync.function(null, 0);
	public final CallSync.Function<Rw, ByteProvider> ioctlSmBusBytes =
		CallSync.function(null, ByteProvider.empty());

	public record Int(int request, int value) {}

	public record Bytes(int address, int flags, ByteProvider command, int length) {}

	public record Rw(int read_write, int command, int type, int byte_, int word,
		ByteProvider block) {}

	/**
	 * A wrapper for repeatedly overriding the library in tests.
	 */
	public static JnaLibrary.Ref<TestI2cCLibNative> i2cRef() {
		return CLib.library.ref(TestI2cCLibNative::of);
	}

	public static TestI2cCLibNative of() {
		return new TestI2cCLibNative();
	}

	public static ByteProvider smBusBlock(int... bytes) {
		byte[] block = new byte[34]; // block size;
		for (int i = 0; i < bytes.length; i++)
			block[i] = (byte) bytes[i];
		return ByteProvider.of(block);
	}

	private TestI2cCLibNative() {}

	@Override
	public int ioctl(int fd, CUlong req, Object... objs) throws LastErrorException {
		int request = req.intValue();
		switch (request) {
			case 0x0701: // I2C_RETRIES
			case 0x0702: // I2C_TIMEOUT
			case 0x0703: // I2C_SLAVE
			case 0x0704: // I2C_TENBIT 1/0
			case 0x0706: // I2C_SLAVE_FORCE
			case 0x0708: // I2C_PEC 1/0
				return ioctlI2cInt.apply(new Int(request, ((Number) objs[0]).intValue()));
			case 0x0705: // I2C_FUNCS, pointer to unsigned long
				return ioctlI2cFuncs(request, (Pointer) objs[0]);
			case 0x0707: // I2C_RDWR, pointer to struct i2c_rdwr_ioctl_data
				return ioctlI2cRdwr((i2c_rdwr_ioctl_data) objs[0]);
			case 0x0720: // I2C_SMBUS, pointer to struct i2c_smbus_ioctl_data
				return ioctlSmBus((i2c_smbus_ioctl_data) objs[0]);
			default:
				return super.ioctl(fd, req, objs);
		}
	}

	private int ioctlSmBus(i2c_smbus_ioctl_data smBus) {
		boolean read = smBus.read_write != 0;
		switch (smBus.size) {
			case 0: // I2C_SMBUS_QUICK
				return ioctlSmBusByte(smBus, false);
			case 1: // I2C_SMBUS_BYTE
			case 2: // I2C_SMBUS_BYTE_DATA
				return ioctlSmBusByte(smBus, read);
			case 3: // I2C_SMBUS_WORD_DATA
				return ioctlSmBusWord(smBus, read);
			case 4: // I2C_SMBUS_PROC_CALL
				return ioctlSmBusWord(smBus, true);
			case 5: // I2C_SMBUS_BLOCK_DATA
				return ioctlSmBusBytes(smBus, read);
			case 7: // I2C_SMBUS_BLOCK_PROC_CALL
				return ioctlSmBusBytes(smBus, true);
			case 8: // I2C_SMBUS_I2C_BLOCK_DATA
				return ioctlSmBusBytes(smBus, read);
			default:
				throw new UnsupportedOperationException(
					"Unsupported transaction type: " + smBus.size);
		}
	}

	private int ioctlSmBusByte(i2c_smbus_ioctl_data smBus, boolean read) {
		int value = ioctlSmBusInt.apply(rw(smBus));
		if (read && smBus.data != null) smBus.data.byte_ = (byte) value;
		return 0;
	}

	private int ioctlSmBusWord(i2c_smbus_ioctl_data smBus, boolean read) {
		int value = ioctlSmBusInt.apply(rw(smBus));
		if (read && smBus.data != null) smBus.data.word = (short) value;
		return 0;
	}

	private int ioctlSmBusBytes(i2c_smbus_ioctl_data smBus, boolean read) {
		ByteProvider bp = ioctlSmBusBytes.apply(rw(smBus));
		if (read && smBus.data != null && bp != null)
			bp.copyTo(0, smBus.data.block, 0, bp.length());
		return 0;
	}

	private Rw rw(i2c_smbus_ioctl_data smBus) {
		return smBus.data == null ?
			new Rw(ubyte(smBus.read_write), ubyte(smBus.command), smBus.size, 0, 0, null) :
			new Rw(ubyte(smBus.read_write), ubyte(smBus.command), smBus.size,
				ubyte(smBus.data.byte_), ushort(smBus.data.word),
				ByteArray.Immutable.copyOf(smBus.data.block));
	}

	private int ioctlI2cFuncs(int request, Pointer ref) {
		int funcs = ioctlI2cInt.apply(new Int(request, 0));
		new CUlong(funcs).write(ref, 0);
		return 0;
	}

	private int ioctlI2cRdwr(i2c_rdwr_ioctl_data data) {
		data.write(); // write data to memory before reading back
		i2c_msg[] msgs = data.msgs();
		var list = Stream.of(msgs).map(this::bytes).toList();
		ByteProvider bp = ioctlI2cBytes.apply(list);
		receive(msgs[msgs.length - 1], bp);
		return 0;
	}

	private void receive(i2c_msg msg, ByteProvider received) {
		if (!i2c_msg.FLAGS.has(msg, I2C_M_RD) || msg.buf == null || received.length() == 0) return;
		JnaUtil.write(msg.buf, received.copy(0));
	}

	private Bytes bytes(i2c_msg msg) {
		var buffer = buffer(msg);
		return new Bytes(ushort(msg.addr), ushort(msg.flags), buffer,
			buffer == null ? ushort(msg.len) : buffer.length());
	}

	private ByteProvider buffer(i2c_msg msg) {
		if (i2c_msg.FLAGS.has(msg, I2C_M_RD)) return null;
		if (msg.buf == null) return ByteProvider.empty();
		return ByteProvider.of(msg.buf.getByteArray(0, ushort(msg.len)));
	}
}
