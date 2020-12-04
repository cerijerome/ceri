package ceri.serial.i2c.jna;

import static ceri.common.math.MathUtil.ubyte;
import static ceri.common.math.MathUtil.ushort;
import static ceri.common.validation.ValidationUtil.validateMax;
import static ceri.common.validation.ValidationUtil.validateRange;
import static ceri.serial.i2c.jna.I2cDev.i2c_smbus_transaction_type.I2C_SMBUS_BLOCK_DATA;
import static ceri.serial.i2c.jna.I2cDev.i2c_smbus_transaction_type.I2C_SMBUS_BLOCK_PROC_CALL;
import static ceri.serial.i2c.jna.I2cDev.i2c_smbus_transaction_type.I2C_SMBUS_BYTE;
import static ceri.serial.i2c.jna.I2cDev.i2c_smbus_transaction_type.I2C_SMBUS_BYTE_DATA;
import static ceri.serial.i2c.jna.I2cDev.i2c_smbus_transaction_type.I2C_SMBUS_I2C_BLOCK_DATA;
import static ceri.serial.i2c.jna.I2cDev.i2c_smbus_transaction_type.I2C_SMBUS_PROC_CALL;
import static ceri.serial.i2c.jna.I2cDev.i2c_smbus_transaction_type.I2C_SMBUS_QUICK;
import static ceri.serial.i2c.jna.I2cDev.i2c_smbus_transaction_type.I2C_SMBUS_WORD_DATA;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.Union;
import com.sun.jna.ptr.NativeLongByReference;
import ceri.common.collection.ArrayUtil;
import ceri.common.collection.ImmutableUtil;
import ceri.common.data.FieldTranscoder;
import ceri.common.data.IntAccessor;
import ceri.common.data.TypeTranscoder;
import ceri.common.math.MathUtil;
import ceri.serial.clib.jna.CException;
import ceri.serial.clib.jna.CLib;
import ceri.serial.jna.Struct;

/**
 * I2C device communication, through ioctl commands. Linux-only?
 */
public class I2cDev {

	private I2cDev() {}

	// -------------------------------------------------------------------------
	// i2c.h types
	// -------------------------------------------------------------------------

	/**
	 * Message flags.
	 */
	public static enum i2c_msg_flag {
		I2C_M_RD(0x0001), // read from slave to master
		I2C_M_TEN(0x0010), // 10-bit address
		I2C_M_RECV_LEN(0x0400), // request len as first received byte
		// I2C_FUNC_PROTOCOL_MANGLING flags:
		I2C_M_NO_RD_ACK(0x0800),
		I2C_M_IGNORE_NAK(0x1000),
		I2C_M_REV_DIR_ADDR(0x2000),
		I2C_M_NOSTART(0x4000),
		I2C_M_STOP(0x8000);

		public final int value;

		public static final TypeTranscoder<i2c_msg_flag> xcoder =
			TypeTranscoder.of(t -> t.value, i2c_msg_flag.class);

		private i2c_msg_flag(int value) {
			this.value = value;
		}
	}

	/**
	 * Represents an I2C transaction segment; START, slave address + r/w bit, data (+ PEC), NAK or
	 * ACK. If last message in group, followed by STOP. Behavior changed with
	 * I2C_FUNC_PROTOCOL_MANGLING and flags.
	 */
	public static class i2c_msg extends Struct {
		private static final List<String> FIELDS = List.of("addr", "flags", "len", "buf");
		private static final IntAccessor.Typed<i2c_msg> flagsAccessor =
			IntAccessor.typedUshort(t -> t.flags, (t, b) -> t.flags = b);
		public short addr;
		public short flags;
		public short len;
		public Pointer buf;

		public static class ByReference extends i2c_msg implements Structure.ByReference {}

		public static ByReference[] array(int count) {
			return Struct.<ByReference>array(count, ByReference::new, ByReference[]::new);
		}

		public i2c_msg() {}

		public i2c_msg(Pointer p) {
			super(p);
		}

		public void populate(int address, int flags, int len, Pointer buf) {
			this.addr = (short) address;
			this.flags = (short) flags;
			this.len = (short) len;
			this.buf = buf;
		}

		public FieldTranscoder<i2c_msg_flag> flags() {
			return i2c_msg_flag.xcoder.field(flagsAccessor.from(this));
		}

		public byte addrByte() {
			return (byte) ((addr << 1) | (flags & i2c_msg_flag.I2C_M_RD.value));
		}

		@Override
		protected List<String> getFieldOrder() {
			return FIELDS;
		}
	}

	/**
	 * To determine what functionality is present
	 */
	public static enum i2c_func {
		I2C_FUNC_I2C(0x00000001),
		I2C_FUNC_10BIT_ADDR(0x00000002), // 10-bit addresses supported
		I2C_FUNC_PROTOCOL_MANGLING(0x00000004), // I2C_M_IGNORE_NAK etc.
		I2C_FUNC_SMBUS_PEC(0x00000008),
		I2C_FUNC_NOSTART(0x00000010), // I2C_M_NOSTART
		I2C_FUNC_SLAVE(0x00000020),
		// SMBus 2.0
		I2C_FUNC_SMBUS_BLOCK_PROC_CALL(0x00008000),
		I2C_FUNC_SMBUS_QUICK(0x00010000),
		I2C_FUNC_SMBUS_READ_BYTE(0x00020000),
		I2C_FUNC_SMBUS_WRITE_BYTE(0x00040000),
		I2C_FUNC_SMBUS_READ_BYTE_DATA(0x00080000),
		I2C_FUNC_SMBUS_WRITE_BYTE_DATA(0x00100000),
		I2C_FUNC_SMBUS_READ_WORD_DATA(0x00200000),
		I2C_FUNC_SMBUS_WRITE_WORD_DATA(0x00400000),
		I2C_FUNC_SMBUS_PROC_CALL(0x00800000),
		I2C_FUNC_SMBUS_READ_BLOCK_DATA(0x01000000),
		I2C_FUNC_SMBUS_WRITE_BLOCK_DATA(0x02000000),
		I2C_FUNC_SMBUS_READ_I2C_BLOCK(0x04000000), // I2C-like block xfer
		I2C_FUNC_SMBUS_WRITE_I2C_BLOCK(0x08000000), // w/ 1-byte reg. addr.
		I2C_FUNC_SMBUS_HOST_NOTIFY(0x10000000);

		public static final Set<i2c_func> I2C_FUNC_SMBUS_BYTE =
			Set.of(I2C_FUNC_SMBUS_READ_BYTE, I2C_FUNC_SMBUS_WRITE_BYTE);
		public static final Set<i2c_func> I2C_FUNC_SMBUS_BYTE_DATA =
			Set.of(I2C_FUNC_SMBUS_READ_BYTE_DATA, I2C_FUNC_SMBUS_WRITE_BYTE_DATA);
		public static final Set<i2c_func> I2C_FUNC_SMBUS_WORD_DATA =
			Set.of(I2C_FUNC_SMBUS_READ_WORD_DATA, I2C_FUNC_SMBUS_WRITE_WORD_DATA);
		public static final Set<i2c_func> I2C_FUNC_SMBUS_BLOCK_DATA =
			Set.of(I2C_FUNC_SMBUS_READ_BLOCK_DATA, I2C_FUNC_SMBUS_WRITE_BLOCK_DATA);
		public static final Set<i2c_func> I2C_FUNC_SMBUS_I2C_BLOCK =
			Set.of(I2C_FUNC_SMBUS_READ_I2C_BLOCK, I2C_FUNC_SMBUS_WRITE_I2C_BLOCK);
		public static final Set<i2c_func> I2C_FUNC_SMBUS_EMUL = ImmutableUtil.collectAsSet( //
			Stream.of(Set.of(I2C_FUNC_SMBUS_QUICK, I2C_FUNC_SMBUS_PROC_CALL, I2C_FUNC_SMBUS_PEC),
				I2C_FUNC_SMBUS_BYTE, I2C_FUNC_SMBUS_BYTE_DATA, I2C_FUNC_SMBUS_WORD_DATA,
				I2C_FUNC_SMBUS_BLOCK_DATA, I2C_FUNC_SMBUS_I2C_BLOCK).flatMap(t -> t.stream()));
		public final int value;

		public static final TypeTranscoder<i2c_func> xcoder =
			TypeTranscoder.of(t -> t.value, i2c_func.class);

		private i2c_func(int value) {
			this.value = value;
		}
	}

	public static final int I2C_SMBUS_BLOCK_MAX = 32;

	/**
	 * Data for SMBus Messages.
	 */
	public static class i2c_smbus_data extends Union {
		public static class ByReference extends i2c_smbus_data implements Structure.ByReference {}

		public byte byte_;
		public short word;
		public byte[] block = new byte[I2C_SMBUS_BLOCK_MAX + 2]; // [0] = len, [n+1] = optional PEC

		public void setByte(int value) {
			byte_ = MathUtil.ubyteExact(value);
			setType("byte_");
		}

		public void setWord(int value) {
			word = MathUtil.ushortExact(value);
			setType("word");
		}

		public void setBlockLength(int length) {
			validateRange(length, 1, I2C_SMBUS_BLOCK_MAX, "Length");
			block[0] = (byte) length;
			setType("block");
		}

		public void setBlock(byte[] data) {
			setBlock(data, 0);
		}

		public void setBlock(byte[] data, int offset) {
			setBlock(data, offset, data.length - offset);
		}

		public void setBlock(byte[] data, int offset, int length) {
			ArrayUtil.validateSlice(data.length, offset, length);
			validateMax(length, I2C_SMBUS_BLOCK_MAX, "Data length");
			ArrayUtil.copy(data, offset, block, 1, length);
			setBlockLength(length);
		}

		public byte[] getBlock() {
			int length = Math.min(ubyte(block[0]), I2C_SMBUS_BLOCK_MAX);
			return ArrayUtil.copyOf(block, 1, length);
		}
	}

	/*
	 * i2c_smbus_ioctl_data read or write markers
	 */
	private static final int I2C_SMBUS_READ = 1;
	private static final int I2C_SMBUS_WRITE = 0;

	/**
	 * SMBus transaction types, or "size".
	 */
	public static enum i2c_smbus_transaction_type {
		I2C_SMBUS_QUICK(0),
		I2C_SMBUS_BYTE(1),
		I2C_SMBUS_BYTE_DATA(2),
		I2C_SMBUS_WORD_DATA(3),
		I2C_SMBUS_PROC_CALL(4),
		I2C_SMBUS_BLOCK_DATA(5),
		// I2C_SMBUS_I2C_BLOCK_BROKEN(6), // linux kernel < 2.6.23, fixed length I2C_SMBUS_BLOCK_MAX
		// SMBus 2.0
		I2C_SMBUS_BLOCK_PROC_CALL(7),
		I2C_SMBUS_I2C_BLOCK_DATA(8); // replaces I2C_SMBUS_BLOCK_DATA?

		public final int size;

		public static final TypeTranscoder<i2c_smbus_transaction_type> xcoder =
			TypeTranscoder.of(t -> t.size, i2c_smbus_transaction_type.class);

		private i2c_smbus_transaction_type(int size) {
			this.size = size;
		}
	}

	// -------------------------------------------------------------------------
	// i2c-dev.h types
	// -------------------------------------------------------------------------

	/**
	 * Structure used in I2C_SMBUS ioctl calls.
	 */
	public static class i2c_smbus_ioctl_data extends Struct {
		private static final List<String> FIELDS = List.of("read_write", "command", "size", "data");
		public byte read_write;
		public byte command;
		public int size;
		public i2c_smbus_data.ByReference data;

		@Override
		protected List<String> getFieldOrder() {
			return FIELDS;
		}
	}

	public static final int I2C_RDWR_IOCTL_MAX_MSGS = 42;

	/**
	 * Structure used in I2C_RDWR ioctl calls.
	 */
	public static class i2c_rdwr_ioctl_data extends Struct {
		private static final List<String> FIELDS = List.of("msgs", "nmsgs");
		public i2c_msg.ByReference msgs;
		public int nmsgs;

		public i2c_msg[] msgs() {
			return array(msgs, nmsgs, i2c_msg::new, i2c_msg[]::new);
		}

		@Override
		protected List<String> getFieldOrder() {
			return FIELDS;
		}
	}

	// ioctl codes
	private static final int I2C_RETRIES = 0x0701;
	private static final int I2C_TIMEOUT = 0x0702;
	private static final int I2C_SLAVE = 0x0703;
	private static final int I2C_TENBIT = 0x0704;
	private static final int I2C_FUNCS = 0x0705; // pointer to unsigned long
	private static final int I2C_SLAVE_FORCE = 0x0706;
	private static final int I2C_RDWR = 0x0707; // pointer to struct i2c_rdwr_ioctl_data
	private static final int I2C_PEC = 0x0708; // kernel drivers will use client setting instead
	private static final int I2C_SMBUS = 0x0720; // pointer to struct i2c_smbus_ioctl_data
	// linux device path
	private static final String PATH_FORMAT = "/dev/i2c-%d";

	/**
	 * Path to open to obtain I2C file descriptor. (Not part of i2c lib code)
	 */
	public static String i2c_path(int bus) {
		return String.format(PATH_FORMAT, bus);
	}

	/* Wrappers for ioctl calls */

	/**
	 * Open an I2C device and return a file descriptor.
	 */
	public static int i2c_open(int bus, int flags) throws CException {
		String path = i2c_path(bus);
		return CLib.open(path, flags);
	}

	/**
	 * Specify the number of times a device address should be polled when not acknowledging.
	 */
	public static void i2c_retries(int fd, int retries) throws CException {
		ioctl("I2C_RETRIES", fd, I2C_RETRIES, retries);
	}

	/**
	 * Set timeout in milliseconds. Underlying ioctl call takes units of 10ms.
	 */
	public static void i2c_timeout(int fd, long timeoutMs) throws CException {
		ioctl("I2C_TIMEOUT", fd, I2C_TIMEOUT, timeoutMs / 10);
	}

	/**
	 * Use this slave address. Can be 7 or 10 bits (supported?).
	 */
	public static void i2c_slave(int fd, int address) throws CException {
		ioctl("I2C_SLAVE", fd, I2C_SLAVE, address);
	}

	/**
	 * Use this slave address, even if it is already in use by a driver.
	 */
	public static void i2c_slave_force(int fd, int address) throws CException {
		ioctl("I2C_SLAVE_FORCE", fd, I2C_SLAVE_FORCE, address);
	}

	/**
	 * Enable 10-bit addressing. Underlying ioctl takes 0 for 7 bit, != 0 for 10 bit.
	 */
	public static void i2c_tenbit(int fd, boolean enabled) throws CException {
		ioctl("I2C_TENBIT", fd, I2C_TENBIT, enabled ? 1 : 0);
	}

	/**
	 * Get the adapter functionality mask.
	 */
	public static int i2c_funcs(int fd) throws CException {
		NativeLongByReference valueRef = new NativeLongByReference();
		CLib.ioctl("I2C_FUNCS", fd, I2C_FUNCS, valueRef);
		return valueRef.getValue().intValue();
	}

	/**
	 * Combined read/write transfer with one STOP only. Messages should be contiguous, created with
	 * i2c_msg.array(n).
	 */
	public static void i2c_rdwr(int fd, i2c_msg.ByReference... msgs) throws CException {
		validateMax(msgs.length, I2C_RDWR_IOCTL_MAX_MSGS, "Message count");
		i2c_rdwr_ioctl_data data = new i2c_rdwr_ioctl_data();
		data.msgs = msgs[0];
		data.nmsgs = msgs.length;
		CLib.ioctl("I2C_RDWR", fd, I2C_RDWR, data);
	}

	/**
	 * Set SMBus error checking. Underlying ioctl takes != 0 to use PEC with SMBus
	 */
	public static void i2c_smbus_pec(int fd, boolean enabled) throws CException {
		ioctl("I2C_PEC", fd, I2C_PEC, enabled ? 1 : 0);
	}

	/**
	 * SMBus: quick write, using address read/write bit for on/off.
	 */
	public static void i2c_smbus_write_quick(int fd, boolean on) throws CException {
		smbusIoctl(fd, on ? I2C_SMBUS_READ : I2C_SMBUS_WRITE, 0, I2C_SMBUS_QUICK, null);
	}

	/**
	 * SMBus: read 1 byte, with PEC if enabled.
	 */
	public static int i2c_smbus_read_byte(int fd) throws CException {
		i2c_smbus_data.ByReference data = new i2c_smbus_data.ByReference();
		smbusIoctl(fd, I2C_SMBUS_READ, 0, I2C_SMBUS_BYTE, data);
		return ubyte(data.byte_);
	}

	/**
	 * SMBus: write 1 byte, with PEC if enabled.
	 */
	public static void i2c_smbus_write_byte(int fd, int value) throws CException {
		smbusIoctl(fd, I2C_SMBUS_WRITE, value, I2C_SMBUS_BYTE, null);
	}

	/**
	 * SMBus: send 1-byte command, and read 1-byte data, with PEC if enabled.
	 */
	public static int i2c_smbus_read_byte_data(int fd, int command) throws CException {
		i2c_smbus_data.ByReference data = new i2c_smbus_data.ByReference();
		smbusIoctl(fd, I2C_SMBUS_READ, command, I2C_SMBUS_BYTE_DATA, data);
		return ubyte(data.byte_);
	}

	/**
	 * SMBus: send 1-byte command and 1-byte data, with PEC if enabled.
	 */
	public static void i2c_smbus_write_byte_data(int fd, int command, int value) throws CException {
		i2c_smbus_data.ByReference data = new i2c_smbus_data.ByReference();
		data.setByte(value);
		smbusIoctl(fd, I2C_SMBUS_WRITE, command, I2C_SMBUS_BYTE_DATA, data);
	}

	/**
	 * SMBus: send 1-byte command, and read 2-byte data, with PEC if enabled.
	 */
	public static int i2c_smbus_read_word_data(int fd, int command) throws CException {
		i2c_smbus_data.ByReference data = new i2c_smbus_data.ByReference();
		smbusIoctl(fd, I2C_SMBUS_READ, command, I2C_SMBUS_WORD_DATA, data);
		return ushort(data.word);
	}

	/**
	 * SMBus: send 1-byte command and 2-byte data, with PEC if enabled.
	 */
	public static void i2c_smbus_write_word_data(int fd, int command, int value) throws CException {
		i2c_smbus_data.ByReference data = new i2c_smbus_data.ByReference();
		data.setWord(value);
		smbusIoctl(fd, I2C_SMBUS_WRITE, command, I2C_SMBUS_WORD_DATA, data);
	}

	/**
	 * SMBus: send 1-byte command and 2-byte data, and read 2-byte data, with PEC if enabled.
	 */
	public static int i2c_smbus_process_call(int fd, int command, int value) throws CException {
		i2c_smbus_data.ByReference data = new i2c_smbus_data.ByReference();
		data.setWord(value);
		smbusIoctl(fd, I2C_SMBUS_WRITE, command, I2C_SMBUS_PROC_CALL, data);
		return ushort(data.word);
	}

	/**
	 * SMBus: send 1-byte command, and read 1-byte N, N-byte data, with PEC if enabled.
	 */
	public static byte[] i2c_smbus_read_block_data(int fd, int command) throws CException {
		i2c_smbus_data.ByReference data = new i2c_smbus_data.ByReference();
		smbusIoctl(fd, I2C_SMBUS_READ, command, I2C_SMBUS_BLOCK_DATA, data);
		return data.getBlock();
	}

	/**
	 * SMBus: send 1-byte command, 1-byte N, and N-byte data, with PEC if enabled.
	 */
	public static void i2c_smbus_write_block_data(int fd, int command, byte[] values)
		throws CException {
		i2c_smbus_write_block_data(fd, command, values, 0);
	}

	/**
	 * SMBus: send 1-byte command, 1-byte N, and N-byte data, with PEC if enabled.
	 */
	public static void i2c_smbus_write_block_data(int fd, int command, byte[] values, int offset)
		throws CException {
		i2c_smbus_write_block_data(fd, command, values, offset, values.length - offset);
	}

	/**
	 * SMBus: send 1-byte command, 1-byte N, and N-byte data, with PEC if enabled.
	 */
	public static void i2c_smbus_write_block_data(int fd, int command, byte[] values, int offset,
		int length) throws CException {
		i2c_smbus_data.ByReference data = new i2c_smbus_data.ByReference();
		data.setBlock(values, offset, length);
		smbusIoctl(fd, I2C_SMBUS_WRITE, command, I2C_SMBUS_BLOCK_DATA, data);
	}

	/**
	 * SMBus: send 1-byte command, 1-byte N, and N-byte data, then read 1-byte N, N-byte data, with
	 * PEC if enabled.
	 */
	public static byte[] i2c_smbus_block_process_call(int fd, int command, byte[] values)
		throws CException {
		return i2c_smbus_block_process_call(fd, command, values, 0);
	}

	/**
	 * SMBus: send 1-byte command, 1-byte N, and N-byte data, then read 1-byte N, N-byte data, with
	 * PEC if enabled.
	 */
	public static byte[] i2c_smbus_block_process_call(int fd, int command, byte[] values,
		int offset) throws CException {
		return i2c_smbus_block_process_call(fd, command, values, offset, values.length - offset);
	}

	/**
	 * SMBus: send 1-byte command, 1-byte N, and N-byte data, then read 1-byte N, N-byte data, with
	 * PEC if enabled.
	 */
	public static byte[] i2c_smbus_block_process_call(int fd, int command, byte[] values,
		int offset, int length) throws CException {
		i2c_smbus_data.ByReference data = new i2c_smbus_data.ByReference();
		data.setBlock(values, offset, length);
		smbusIoctl(fd, I2C_SMBUS_WRITE, command, I2C_SMBUS_BLOCK_PROC_CALL, data);
		return data.getBlock();
	}

	/**
	 * SMBus: send 1-byte command, and read N-byte data.
	 */
	public static byte[] i2c_smbus_read_i2c_block_data(int fd, int command, int length)
		throws CException {
		i2c_smbus_data.ByReference data = new i2c_smbus_data.ByReference();
		data.setBlockLength(length);
		smbusIoctl(fd, I2C_SMBUS_READ, command, I2C_SMBUS_I2C_BLOCK_DATA, data);
		return data.getBlock();
	}

	/**
	 * SMBus: send 1-byte command and N-byte data.
	 */
	public static void i2c_smbus_write_i2c_block_data(int fd, int command, byte[] values)
		throws CException {
		i2c_smbus_write_i2c_block_data(fd, command, values, 0);
	}

	/**
	 * SMBus: send 1-byte command and N-byte data.
	 */
	public static void i2c_smbus_write_i2c_block_data(int fd, int command, byte[] values,
		int offset) throws CException {
		i2c_smbus_write_i2c_block_data(fd, command, values, offset, values.length - offset);
	}

	/**
	 * SMBus: send 1-byte command and N-byte data.
	 */
	public static void i2c_smbus_write_i2c_block_data(int fd, int command, byte[] values,
		int offset, int length) throws CException {
		i2c_smbus_data.ByReference data = new i2c_smbus_data.ByReference();
		data.setBlock(values, offset, length);
		smbusIoctl(fd, I2C_SMBUS_WRITE, command, I2C_SMBUS_I2C_BLOCK_DATA, data);
	}

	private static void ioctl(String name, int fd, int request, long value) throws CException {
		CLib.ioctl(name, fd, request, new NativeLong(value, true));
	}

	private static int smbusIoctl(int fd, int readWrite, int command,
		i2c_smbus_transaction_type type, i2c_smbus_data.ByReference data) throws CException {
		i2c_smbus_ioctl_data smbus = new i2c_smbus_ioctl_data();
		smbus.read_write = MathUtil.ubyteExact(readWrite);
		smbus.command = MathUtil.ubyteExact(command);
		smbus.size = type.size;
		smbus.data = data;
		return CLib.ioctl("I2C_SMBUS", fd, I2C_SMBUS, smbus); // smbus.read() if reading?
	}

}
