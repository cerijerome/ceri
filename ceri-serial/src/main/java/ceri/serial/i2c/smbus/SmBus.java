package ceri.serial.i2c.smbus;

import java.io.IOException;
import ceri.common.array.Array;
import ceri.jna.clib.jna.CException;

public interface SmBus {
	/** A stateless, no-op instance. */
	SmBus NULL = new Null() {};

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
		writeBlockData(command, Array.BYTE.of(values));
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
		writeI2cBlockData(command, Array.BYTE.of(values));
	}

	default void writeI2cBlockData(int command, byte[] values) throws IOException {
		writeI2cBlockData(command, values, 0);
	}

	default void writeI2cBlockData(int command, byte[] values, int offset) throws IOException {
		writeI2cBlockData(command, values, offset, values.length - offset);
	}

	void writeI2cBlockData(int command, byte[] values, int offset, int length) throws IOException;

	default byte[] blockProcessCall(int command, int... values) throws IOException {
		return blockProcessCall(command, Array.BYTE.of(values));
	}

	default byte[] blockProcessCall(int command, byte[] values) throws IOException {
		return blockProcessCall(command, values, 0);
	}

	default byte[] blockProcessCall(int command, byte[] values, int offset) throws IOException {
		return blockProcessCall(command, values, offset, values.length - offset);
	}

	byte[] blockProcessCall(int command, byte[] values, int offset, int length) throws IOException;

	/**
	 * A stateless, no-op implementation.
	 */
	interface Null extends SmBus {
		@Override
		default void writeQuick(boolean on) {}

		@Override
		default int readByte() {
			return 0;
		}

		@Override
		default void writeByte(int value) throws CException {}

		@Override
		default int readByteData(int command) {
			return 0;
		}

		@Override
		default void writeByteData(int command, int value) throws CException {}

		@Override
		default int readWordData(int command) throws CException {
			return 0;
		}

		@Override
		default void writeWordData(int command, int value) throws CException {}

		@Override
		default int processCall(int command, int value) throws CException {
			return 0;
		}

		@Override
		default byte[] readBlockData(int command) throws CException {
			return Array.BYTE.empty;
		}

		@Override
		default void writeBlockData(int command, byte[] values, int offset, int length)
			throws CException {}

		@Override
		default byte[] readI2cBlockData(int command, int length) throws CException {
			return new byte[length];
		}

		@Override
		default void writeI2cBlockData(int command, byte[] values, int offset, int length)
			throws CException {}

		@Override
		default byte[] blockProcessCall(int command, byte[] values, int offset, int length)
			throws CException {
			return Array.BYTE.empty;
		}
	}
}
