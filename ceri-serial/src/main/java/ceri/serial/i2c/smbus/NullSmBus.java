package ceri.serial.i2c.smbus;

import ceri.common.collection.ArrayUtil;
import ceri.serial.clib.jna.CException;

/**
 * A no-op implementation of the SmBus interface.
 */
public class NullSmBus implements SmBus {
	private static final NullSmBus INSTANCE = new NullSmBus();

	public static NullSmBus instance() {
		return INSTANCE;
	}
	
	private NullSmBus() {}
	
	@Override
	public void writeQuick(boolean on) {}

	@Override
	public int readByte() {
		return 0;
	}

	@Override
	public void writeByte(int value) throws CException {}

	@Override
	public int readByteData(int command) {
		return 0;
	}

	@Override
	public void writeByteData(int command, int value) throws CException {}

	@Override
	public int readWordData(int command) throws CException {
		return 0;
	}

	@Override
	public void writeWordData(int command, int value) throws CException {}

	@Override
	public int processCall(int command, int value) throws CException {
		return 0;
	}

	@Override
	public byte[] readBlockData(int command) throws CException {
		return ArrayUtil.EMPTY_BYTE;
	}

	@Override
	public void writeBlockData(int command, byte[] values, int offset, int length)
		throws CException {}

	@Override
	public byte[] readI2cBlockData(int command, int length) throws CException {
		return ArrayUtil.EMPTY_BYTE;
	}

	@Override
	public void writeI2cBlockData(int command, byte[] values, int offset, int length)
		throws CException {}

	@Override
	public byte[] blockProcessCall(int command, byte[] values, int offset, int length)
		throws CException {
		return ArrayUtil.EMPTY_BYTE;
	}

}
