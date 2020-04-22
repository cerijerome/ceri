package ceri.serial.i2c;

import ceri.common.collection.ArrayUtil;
import ceri.serial.clib.jna.CException;

public interface SmBus {

	void writeQuick(boolean on) throws CException;

	int readByte() throws CException;

	void writeByte(int value) throws CException;

	int readByteData(int command) throws CException;

	void writeByteData(int command, int value) throws CException;

	int readWordData(int command) throws CException;

	void writeWordData(int command, int value) throws CException;

	int processCall(int command, int value) throws CException;

	byte[] readBlockData(int command) throws CException;

	default void writeBlockData(int command, int... values) throws CException {
		writeBlockData(command, ArrayUtil.bytes(values));
	}
	
	void writeBlockData(int command, byte... values) throws CException;

	byte[] readI2cBlockData(int command, int length) throws CException;

	default void writeI2cBlockData(int command, int... values) throws CException {
		writeI2cBlockData(command, ArrayUtil.bytes(values));
	}
	
	void writeI2cBlockData(int command, byte... values) throws CException;

	default byte[] blockProcessCall(int command, int... values) throws CException {
		return blockProcessCall(command, ArrayUtil.bytes(values));
	}
	
	byte[] blockProcessCall(int command, byte... values) throws CException;

}
