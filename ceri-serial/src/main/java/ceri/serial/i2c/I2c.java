package ceri.serial.i2c;

import static ceri.common.math.MathUtil.byteExact;
import static ceri.serial.clib.OpenFlag.O_RDWR;
import static ceri.serial.i2c.jna.I2cDev.i2c_msg_flag.I2C_M_RD;
import static ceri.serial.jna.JnaUtil.byteRef;
import static ceri.serial.jna.JnaUtil.size;
import java.io.Closeable;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.sun.jna.Memory;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.ShortByReference;
import ceri.common.collection.ArrayUtil;
import ceri.common.util.StartupValues;
import ceri.log.util.LogUtil;
import ceri.serial.clib.FileDescriptor;
import ceri.serial.clib.jna.CException;
import ceri.serial.clib.jna.CUtil;
import ceri.serial.i2c.jna.I2cDev;
import ceri.serial.i2c.jna.I2cDev.i2c_func;
import ceri.serial.i2c.jna.I2cDev.i2c_msg;
import ceri.serial.i2c.jna.I2cDev.i2c_msg_flag;
import ceri.serial.jna.ByteReader;
import ceri.serial.jna.ByteWriter;
import ceri.serial.jna.JnaUtil;

public class I2c implements Closeable {
	private static final Logger logger = LogManager.getLogger();
	private static final int SCAN_7BIT_LIMIT = 0x80;
	private final FileDescriptor fd;
	private final State state = new State();

	public static void main(String[] args) throws CException {
		StartupValues values = StartupValues.of(args);
		int device = values.next().asInt(0x33);
		try (I2c i2c = open(1)) {
			System.out.println("Scan: " + i2c.scan7Bit());
			I2cAddress addr = I2cAddress.of(device);
			i2c.smBus(addr, false).writeQuick(true);
			// i2c.smBus(addr, false).writeQuick(false);
		}
	}

	/**
	 * Keep state to avoid unneeded ioctl calls.
	 */
	private static class State {
		static final int NO_ADDRESS = -1;
		int address = NO_ADDRESS;
		boolean tenBit = false;
		boolean pec = false;
	}

	/**
	 * Open a handle to the I2C bus.
	 */
	public static I2c open(int bus) throws CException {
		if (bus < 0) throw new IllegalArgumentException("Invalid bus number: " + bus);
		return new I2c(FileDescriptor.of(I2cDev.i2c_open(bus, O_RDWR.value)));
	}

	private I2c(FileDescriptor fd) {
		this.fd = fd;
	}

	/**
	 * Specify the number of times a device address should be polled when not acknowledging.
	 */
	public I2c retries(int count) throws CException {
		I2cDev.i2c_retries(fd.fd(), count);
		return this;
	}

	/**
	 * Specify the call timeout.
	 */
	public I2c timeout(int timeoutMs) throws CException {
		I2cDev.i2c_timeout(fd.fd(), timeoutMs);
		return this;
	}

	/**
	 * Determine functionality supported by the bus.
	 */
	public Set<i2c_func> functions() throws CException {
		return i2c_func.xcoder.decodeAll(I2cDev.i2c_funcs(fd.fd()));
	}

	/**
	 * Verify the address is in use.
	 */
	public boolean exists(I2cAddress address) {
		try {
			reader(address).readByte();
			return true;
		} catch (CException e) {
			return false;
		}
	}

	/**
	 * Scan 7-bit address range for existing devices.
	 */
	public Set<I2cAddress> scan7Bit() {
		return IntStream.range(0, SCAN_7BIT_LIMIT).mapToObj(i -> I2cAddress.of7Bit(i))
			.filter(this::exists).collect(Collectors.toSet());
	}

	/**
	 * Accessor to read bytes using file descriptor.
	 */
	public ByteReader reader(I2cAddress address) {
		return (p, offset, len) -> {
			selectDevice(address);
			return fd.readInto(p, offset, len);
		};
	}

	/**
	 * Accessor to write bytes using file descriptor.
	 */
	public ByteWriter writer(I2cAddress address) {
		return new ByteWriter() {
			@Override
			public ByteWriter writeFrom(Pointer p, int offset, int len) throws CException {
				selectDevice(address);
				fd.writeFrom(p, offset, len);
				return this;
			}
		};
	}

	public void smBusPec(boolean on) throws CException {
		I2cDev.i2c_smbus_pec(fd.fd(), on);
		state.pec = on;
	}

	/**
	 * Provide SMBus functionality. Emulated SMBus uses ioctl read-writes calls.
	 */
	public SmBus smBus(I2cAddress address, boolean emulated) {
		if (address.tenBit) throw new UnsupportedOperationException(
			"SMBus not supported for 10-bit addresses");
		return emulated ? new SmBusEmulated(this, address) : new SmBusActual(this, address);
	}

	/**
	 * Send byte command to address, and read unsigned short response, using ioctl.
	 */
	public int readWordData(I2cAddress address, int command) throws CException {
		ShortByReference resultRef = new ShortByReference();
		readWrite(address, Byte.BYTES, byteRef(command).getPointer(), Short.BYTES,
			resultRef.getPointer());
		return JnaUtil.ushort(resultRef);
	}

	/**
	 * Send byte command to address, and read byte array response, using ioctl.
	 */
	public byte[] readData(I2cAddress address, int command, int readLen) throws CException {
		return readData(address, ArrayUtil.bytes(command), readLen);
	}

	/**
	 * Send ascii command to address, and read byte array response, using ioctl.
	 */
	public byte[] readData(I2cAddress address, String command, int readLen) throws CException {
		return readData(address, command.getBytes(StandardCharsets.ISO_8859_1), readLen);
	}

	/**
	 * Send byte array command to address, and read byte array response, using ioctl.
	 */
	public byte[] readData(I2cAddress address, byte[] command, int readLen) throws CException {
		Memory write = CUtil.malloc(command);
		Memory read = new Memory(readLen);
		readWrite(address, size(write), write, size(read), read);
		return JnaUtil.byteArray(read);
	}

	@Override
	public void close() {
		LogUtil.close(logger, fd);
	}

	/* Start: Shared with SMBus */

	FileDescriptor fd() {
		return fd;
	}

	void selectDevice(I2cAddress address) throws CException {
		setAddressBits(address.tenBit);
		try {
			setSlaveAddress(address.address);
		} catch (CException e) {
			forceSlaveAddress(address.address);
		}
	}

	boolean smBusPec() {
		return state.pec;
	}

	/* End: Shared with SMBus */

	private void readWrite(I2cAddress address, int writeLen, Pointer writeBuf, int readLen,
		Pointer readBuf) throws CException {
		i2c_msg.ByReference[] msgs = i2c_msg.array(2);
		populate(msgs[0], address, writeLen, writeBuf);
		populate(msgs[1], address, readLen, readBuf, I2C_M_RD);
		I2cDev.i2c_rdwr(fd.fd(), msgs);
	}

	private static void populate(i2c_msg msg, I2cAddress address, int size, Pointer p,
		i2c_msg_flag... flags) {
		msg.addr = address.value();
		msg.len = byteExact(size);
		msg.buf = p;
		msg.flags().set(flags);
		if (address.tenBit) msg.flags().add(i2c_msg_flag.I2C_M_TEN);
	}

	private void setSlaveAddress(int address) throws CException {
		if (state.address == address) return;
		I2cDev.i2c_slave(fd.fd(), address);
		state.address = address;
	}

	private void forceSlaveAddress(int address) throws CException {
		I2cDev.i2c_slave_force(fd.fd(), address);
		state.address = address;
	}

	private void setAddressBits(boolean tenBit) throws CException {
		if (state.tenBit == tenBit) return;
		I2cDev.i2c_tenbit(fd.fd(), tenBit);
		state.address = State.NO_ADDRESS;
		state.tenBit = tenBit;
	}

}
