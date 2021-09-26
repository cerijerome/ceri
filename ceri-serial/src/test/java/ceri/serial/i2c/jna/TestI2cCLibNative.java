package ceri.serial.i2c.jna;

import static ceri.common.math.MathUtil.ubyte;
import static ceri.common.math.MathUtil.ushort;
import static ceri.serial.i2c.jna.I2cDev.i2c_msg_flag.I2C_M_RD;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import com.sun.jna.LastErrorException;
import com.sun.jna.NativeLong;
import com.sun.jna.ptr.NativeLongByReference;
import ceri.common.data.ByteArray;
import ceri.common.data.ByteProvider;
import ceri.common.test.CallSync;
import ceri.serial.clib.test.TestCLibNative;
import ceri.serial.i2c.jna.I2cDev.i2c_msg;
import ceri.serial.i2c.jna.I2cDev.i2c_rdwr_ioctl_data;
import ceri.serial.i2c.jna.I2cDev.i2c_smbus_ioctl_data;
import ceri.serial.jna.JnaUtil;

public class TestI2cCLibNative extends TestCLibNative {
	// List<?> = int request, int value
	public final CallSync.Apply<List<?>, Integer> ioctlI2cInt = CallSync.function(null, 0);
	// List<?> = int address, int flags, [ByteProvider command | int length], ...
	public final CallSync.Apply<List<?>, ByteProvider> ioctlI2cBytes =
		CallSync.function(null, ByteProvider.empty());
	// List<?> = int read_write, int command, int type, int byte_, int word, ByteProvider block
	public final CallSync.Apply<List<?>, Integer> ioctlSmBusInt = CallSync.function(null, 0);
	// List<?> = int read_write, int command, int type, int byte_, int word, ByteProvider block
	public final CallSync.Apply<List<?>, ByteProvider> ioctlSmBusBytes =
		CallSync.function(null, ByteProvider.empty());

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
	protected int ioctl(Fd f, int request, Object... objs) throws LastErrorException {
		switch (request) {
			case 0x0701: // I2C_RETRIES
			case 0x0702: // I2C_TIMEOUT
			case 0x0703: // I2C_SLAVE
			case 0x0704: // I2C_TENBIT 1/0
			case 0x0706: // I2C_SLAVE_FORCE
			case 0x0708: // I2C_PEC 1/0
				return ioctlI2cInt.apply(List.of(request, ((Number) objs[0]).intValue()));
			case 0x0705: // I2C_FUNCS, pointer to unsigned long
				return ioctlI2cFuncs(request, (NativeLongByReference) objs[0]);
			case 0x0707: // I2C_RDWR, pointer to struct i2c_rdwr_ioctl_data
				return ioctlI2cRdwr((i2c_rdwr_ioctl_data) objs[0]);
			case 0x0720: // I2C_SMBUS, pointer to struct i2c_smbus_ioctl_data
				return ioctlSmBus((i2c_smbus_ioctl_data) objs[0]);
			default:
				return super.ioctl(f, request, objs);
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
		int value = ioctlSmBusInt.apply(list(smBus));
		if (read && smBus.data != null) smBus.data.byte_ = (byte) value;
		return 0;
	}

	private int ioctlSmBusWord(i2c_smbus_ioctl_data smBus, boolean read) {
		int value = ioctlSmBusInt.apply(list(smBus));
		if (read && smBus.data != null) smBus.data.word = (short) value;
		return 0;
	}

	private int ioctlSmBusBytes(i2c_smbus_ioctl_data smBus, boolean read) {
		ByteProvider bp = ioctlSmBusBytes.apply(list(smBus));
		if (read && smBus.data != null && bp != null)
			bp.copyTo(0, smBus.data.block, 0, bp.length());
		return 0;
	}

	private List<?> list(i2c_smbus_ioctl_data smBus) {
		List<Object> list = new ArrayList<>();
		Collections.addAll(list, (int) ubyte(smBus.read_write), (int) ubyte(smBus.command),
			smBus.size);
		if (smBus.data != null) Collections.addAll(list, (int) ubyte(smBus.data.byte_),
			ushort(smBus.data.word), ByteArray.Immutable.copyOf(smBus.data.block));
		return Collections.unmodifiableList(list);
	}

	private int ioctlI2cFuncs(int request, NativeLongByReference ref) {
		int funcs = ioctlI2cInt.apply(List.of(request, 0));
		ref.setValue(new NativeLong(funcs));
		return 0;
	}

	private int ioctlI2cRdwr(i2c_rdwr_ioctl_data data) {
		data.write(); // write data to memory before reading back
		i2c_msg[] msgs = data.msgs();
		List<Object> list = new ArrayList<>();
		for (var msg : msgs)
			append(list, msg);
		ByteProvider bp = ioctlI2cBytes.apply(list);
		receive(msgs[msgs.length - 1], bp);
		return 0;
	}

	private void receive(i2c_msg msg, ByteProvider received) {
		if (!msg.flags().has(I2C_M_RD) || msg.buf == null || received.length() == 0) return;
		JnaUtil.write(msg.buf, received.copy(0));
	}

	private void append(List<Object> list, i2c_msg msg) {
		Collections.addAll(list, ushort(msg.addr), ushort(msg.flags));
		if (msg.flags().has(I2C_M_RD)) list.add(ushort(msg.len));
		else if (msg.buf == null) list.add(ByteProvider.empty());
		else list.add(ByteProvider.of(msg.buf.getByteArray(0, ushort(msg.len))));
	}

}
