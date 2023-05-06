package ceri.serial.i2c.smbus;

import java.io.IOException;
import ceri.common.collection.ArrayUtil;
import ceri.jna.clib.jna.CException;

public interface SmBus {
	static final SmBus NULL = new Null();
	
	void writeQuick(boolean on) throws IOException;

	int readByte() throws IOException;

	void writeByte(int value) throws IOException;

	int readByteData(int command) throws IOException;

	void writeByteData(int command, int value) throws IOException;

	int readWordData(int command) throws IOException;

	void writeWordData(int command, int value) throws IOException;

	int processCall(int command, int value) throws IOException;

	byte[] readBlockData(int command) throws IOException;

	default void writeBlockData(int command, int... values) throws IOException {
		writeBlockData(command, ArrayUtil.bytes(values));
	}

	default void writeBlockData(int command, byte[] values) throws IOException {
		writeBlockData(command, values, 0);
	}

	default void writeBlockData(int command, byte[] values, int offset) throws IOException {
		writeBlockData(command, values, offset, values.length - offset);
	}

	void writeBlockData(int command, byte[] values, int offset, int length) throws IOException;

	byte[] readI2cBlockData(int command, int length) throws IOException;

	default void writeI2cBlockData(int command, int... values) throws IOException {
		writeI2cBlockData(command, ArrayUtil.bytes(values));
	}

	default void writeI2cBlockData(int command, byte[] values) throws IOException {
		writeI2cBlockData(command, values, 0);
	}

	default void writeI2cBlockData(int command, byte[] values, int offset) throws IOException {
		writeI2cBlockData(command, values, offset, values.length - offset);
	}

	void writeI2cBlockData(int command, byte[] values, int offset, int length) throws IOException;

	default byte[] blockProcessCall(int command, int... values) throws IOException {
		return blockProcessCall(command, ArrayUtil.bytes(values));
	}

	default byte[] blockProcessCall(int command, byte[] values) throws IOException {
		return blockProcessCall(command, values, 0);
	}

	default byte[] blockProcessCall(int command, byte[] values, int offset) throws IOException {
		return blockProcessCall(command, values, offset, values.length - offset);
	}

	byte[] blockProcessCall(int command, byte[] values, int offset, int length) throws IOException;

	/**
	 * A no-op implementation of the SmBus interface.
	 */
	static class Null implements SmBus {
		protected Null() {}

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
			return new byte[length];
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
}
