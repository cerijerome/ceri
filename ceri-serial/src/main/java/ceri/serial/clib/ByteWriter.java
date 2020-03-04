package ceri.serial.clib;

import com.sun.jna.Memory;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;
import ceri.common.collection.ArrayUtil;
import ceri.serial.clib.jna.CException;
import ceri.serial.jna.JnaUtil;

/**
 * Provides common byte access methods, based on underlying pointer access method.
 */
public interface ByteWriter {

	int writeFrom(Pointer p, int len) throws CException;

	default int write(int... data) throws CException {
		return write(ArrayUtil.bytes(data));
	}

	default int write(byte... data) throws CException {
		return write(data, 0);
	}

	default int write(byte[] data, int offset) throws CException {
		return write(data, offset, data.length - offset);
	}

	default int write(byte[] data, int offset, int len) throws CException {
		Memory m = JnaUtil.malloc(data, offset, len);
		return writeFrom(m, data.length);
	}

	default int writeFrom(Pointer p, int offset, int len) throws CException {
		return writeFrom(p.getPointer(offset), len);
	}

	default int writeShort(int value) throws CException {
		return writeFrom(JnaUtil.shortRef(value).getPointer(), Short.BYTES);
	}

	default int writeInt(int value) throws CException {
		return writeFrom(new IntByReference(value).getPointer(), Integer.BYTES);
	}

}
